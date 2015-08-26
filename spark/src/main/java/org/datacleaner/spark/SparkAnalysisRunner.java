/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.spark;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.Resource;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

public class SparkAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(SparkAnalysisRunner.class);

    private final SparkJobContext _sparkJobContext;
    private final JavaSparkContext _sparkContext;

    public SparkAnalysisRunner(JavaSparkContext sparkContext, SparkJobContext sparkJobContext) {
        _sparkContext = sparkContext;
        _sparkJobContext = sparkJobContext;
    }

    public void run() {
        run(_sparkJobContext.getAnalysisJob());
    }

    @Override
    public AnalysisResultFuture run(AnalysisJob job) {
        assert job == _sparkJobContext.getAnalysisJob();

        final AnalysisJob analysisJob = _sparkJobContext.getAnalysisJob();
        final Datastore datastore = analysisJob.getDatastore();

        final JavaRDD<InputRow> inputRowsRDD = openSourceDatastore(datastore);

        final JavaPairRDD<String, NamedAnalyzerResult> namedAnalyzerResultsRDD = inputRowsRDD
                .mapPartitionsToPair(new RowProcessingFunction(_sparkJobContext));

        JavaPairRDD<String, NamedAnalyzerResult> reducedNamedAnalyzerResultsRDD = namedAnalyzerResultsRDD
                .reduceByKey(new AnalyzerResultReduceFunction(_sparkJobContext));

        JavaPairRDD<String, AnalyzerResult> finalAnalyzerResultsRDD = reducedNamedAnalyzerResultsRDD
                .mapValues(new ExtractAnalyzerResultFromTupleFunction());
        
        finalAnalyzerResultsRDD.persist(StorageLevel.MEMORY_AND_DISK());

        // log analyzer results
        final List<Tuple2<String, AnalyzerResult>> results = finalAnalyzerResultsRDD.collect();

        logger.info("Finished! Number of AnalyzerResult objects: {}", results.size());
        for (Tuple2<String, AnalyzerResult> analyzerResultTuple : results) {
            final String key = analyzerResultTuple._1;
            final AnalyzerResult result = analyzerResultTuple._2;
            logger.info("AnalyzerResult: " + key + "->" + result);
        }

        // log accumulators
        final Map<String, Accumulator<Integer>> accumulators = _sparkJobContext.getAccumulators();
        for (Entry<String, Accumulator<Integer>> entry : accumulators.entrySet()) {
            final String name = entry.getKey();
            final Accumulator<Integer> accumulator = entry.getValue();
            logger.info("Accumulator: {} -> {}", name, accumulator.value());
        }

        return new SparkAnalysisResultFuture(results);
    }

    private JavaRDD<InputRow> openSourceDatastore(Datastore datastore) {
        if (datastore instanceof CsvDatastore) {
            final CsvDatastore csvDatastore = (CsvDatastore) datastore;
            final Resource resource = csvDatastore.getResource();
            final String datastorePath = resource.getQualifiedPath();

            final CsvConfiguration csvConfiguration = csvDatastore.getCsvConfiguration();

            final JavaRDD<String> rawInput = _sparkContext.textFile(datastorePath);
            final JavaRDD<Object[]> parsedInput = rawInput.map(new CsvParserMapper(csvConfiguration));
            final JavaRDD<InputRow> inputRowsRDD = parsedInput.map(new ValuesToInputRowMapper(_sparkJobContext));

            return inputRowsRDD;
        }

        throw new UnsupportedOperationException("Unsupported datastore type or configuration: " + datastore);
    }
}
