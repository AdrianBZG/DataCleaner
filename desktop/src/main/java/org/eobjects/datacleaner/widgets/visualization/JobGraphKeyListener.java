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
package org.eobjects.datacleaner.widgets.visualization;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KeyListener} for the {@link JobGraph}.
 */
public class JobGraphKeyListener extends KeyAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphKeyListener.class);

    private final JobGraphContext _graphContext;

    public JobGraphKeyListener(JobGraphContext graphContext) {
        _graphContext = graphContext;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // react to DEL key strokes and delete components that are selected.
        if (e.getKeyChar() == KeyEvent.VK_DELETE) {
            final Object vertex = _graphContext.getSelectedVertex();
            logger.debug("Registered typed DEL. Vertex: {}", vertex);
            if (vertex != null) {
                final AnalysisJobBuilder analysisJobBuilder = _graphContext.getAnalysisJobBuilder();
                if (vertex instanceof TransformerJobBuilder) {
                    final TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) vertex;
                    analysisJobBuilder.removeTransformer(tjb);
                } else if (vertex instanceof AnalyzerJobBuilder) {
                    final AnalyzerJobBuilder<?> ajb = (AnalyzerJobBuilder<?>) vertex;
                    analysisJobBuilder.removeAnalyzer(ajb);
                } else if (vertex instanceof FilterJobBuilder) {
                    final FilterJobBuilder<?, ?> fjb = (FilterJobBuilder<?, ?>) vertex;
                    analysisJobBuilder.removeFilter(fjb);
                } else if (vertex instanceof Table) {
                    final Table table = (Table) vertex;
                    analysisJobBuilder.removeSourceTable(table);
                } else if (vertex instanceof Column) {
                    final Column column = (Column) vertex;
                    analysisJobBuilder.removeSourceColumn(column);
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
