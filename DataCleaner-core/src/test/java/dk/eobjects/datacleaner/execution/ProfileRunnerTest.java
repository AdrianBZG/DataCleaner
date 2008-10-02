/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.execution;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfileConfiguration;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class ProfileRunnerTest extends DataCleanerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ProfileManagerTest.initProfileManager();
	}

	public void testMultipleProfileDefinitions() throws Exception {
		Connection connection = getTestDbConnection();
		DataContext dataContext = new DataContext(connection);
		Schema schema = getTestDbSchema(connection);

		ProfileRunner profileRunner = new ProfileRunner();

		// Create profile definition for a single column
		Table customersTable = schema.getTableByName("CUSTOMERS");
		Column addressLine2Column = customersTable
				.getColumnByName("ADDRESSLINE2");
		ProfileConfiguration conf1 = new ProfileConfiguration(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		conf1.setColumns(addressLine2Column);
		profileRunner.addConfiguration(conf1);

		// Create profile definition for multiple columns
		Table officesTable = schema.getTableByName("OFFICES");
		Column postalCodeColumn = officesTable.getColumnByName("POSTALCODE");
		Column officeCodeColumn = officesTable.getColumnByName("OFFICECODE");
		ProfileConfiguration conf2 = new ProfileConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf2.setColumns(postalCodeColumn, officeCodeColumn);
		profileRunner.addConfiguration(conf2);

		IProgressObserver po = new IProgressObserver() {
			long _time = 0;
			boolean _begun = false;

			public void init(Object[] executingObjects) {
				assertEquals(2, executingObjects.length);
			}

			public void notifyExecutionBegin(Object executingObject) {
				assertFalse(_begun);
				_begun = true;
				_time = System.currentTimeMillis();
			}

			public void notifyExecutionSuccess(Object executingObject) {
				assertTrue(_begun);
				_begun = false;
				assertTrue("CurrentTime was less than when execution began.",
						_time <= System.currentTimeMillis());
			}

			public void notifyExecutionFailed(Object executingObject,
					Throwable throwable) {
				fail("Execution should not have failed");
			}
		};
		profileRunner.addProgressObserver(po);
		profileRunner.execute(dataContext);
		Table[] profileTables = profileRunner.getResultTables();
		assertEquals(2, profileTables.length);

		List<IProfileResult> profileResultsForTable = profileRunner
				.getResultsForTable(customersTable);
		assertEquals(1, profileResultsForTable.size());
		profileResultsForTable = profileRunner.getResultsForTable(officesTable);
		assertEquals(1, profileResultsForTable.size());

		List<IProfileResult> results = profileRunner.getResults();

		String[] expectations = {
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={ADDRESSLINE2},aaaaa 999={MatrixValue[value=11,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE2\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"ADDRESSLINE2\"]},??? aaaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE2\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"ADDRESSLINE2\"]},aaaaa aa. 9={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE2\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"ADDRESSLINE2\"]}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Standard measures,profileClass=class dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile],matrices={Matrix[columnNames={POSTALCODE,OFFICECODE},Row count={7,7},Null values={0,0},Empty values={0,0},Highest value={NSW 2010,7},Lowest value={02107,1}]}]" };

		assertEquals(expectations.length, results.size());
		assertEquals(results, expectations);
	}

	private void assertEquals(List<IProfileResult> results,
			String[] expectations) {
		for (int j = 0; j < results.size(); j++) {
			String string = results.get(j).toString();
			boolean result = ArrayUtils.indexOf(expectations, string) != -1;
			if (!result) {
				System.err
						.println("Could not find the following string in expectations array:\n"
								+ string.replaceAll("\\\"", "\\\\\""));
				System.err.println("Expectations array is:"
						+ ArrayUtils.toString(expectations));
				assertEquals("?", string);
			}
		}
	}

	public void testProfileDefinitionsWithSameTable() throws Exception {
		Connection connection = getTestDbConnection();
		Schema schema = getTestDbSchema(connection);

		ProfileRunner profileRunner = new ProfileRunner();

		// Create profile definition for a single column
		final Table customersTable = schema.getTableByName("CUSTOMERS");
		Column customerNameColumn = customersTable
				.getColumnByName("CUSTOMERNAME");
		ProfileConfiguration conf1 = new ProfileConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf1.setColumns(customerNameColumn);
		profileRunner.addConfiguration(conf1);

		// Create profile definition for multiple columns
		Column countryColumn = customersTable.getColumnByName("COUNTRY");
		ProfileConfiguration conf2 = new ProfileConfiguration(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		conf2.setColumns(countryColumn, customerNameColumn);
		profileRunner.addConfiguration(conf2);

		IProgressObserver po = new IProgressObserver() {
			private int _notifications = 0;

			public void init(Object[] executingObjects) {
				assertEquals(1, executingObjects.length);
				assertSame(customersTable, executingObjects[0]);
			}

			public void notifyExecutionBegin(Object executingObject) {
				_notifications++;
				assertEquals(1, _notifications);
			}

			public void notifyExecutionSuccess(Object executingObject) {
				_notifications++;
				assertEquals(2, _notifications);
			}

			public void notifyExecutionFailed(Object executingObject,
					Throwable throwable) {
				fail("Execution should not have failed");
			}
		};

		profileRunner.addProgressObserver(po);
		profileRunner.execute(new DataContext(connection));
		List<IProfileResult> results = profileRunner.getResults();

		String[] expectations = {
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={COUNTRY},aaaaaaaaaaa={MatrixValue[value=116,detailQuery=SELECT \"CUSTOMERS\".\"COUNTRY\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"COUNTRY\"]},aaaaa aaaaaaa={MatrixValue[value=6,detailQuery=SELECT \"CUSTOMERS\".\"COUNTRY\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"COUNTRY\"]}],Matrix[columnNames={CUSTOMERNAME},aaaaaaaaaa aaaaaaaaaaaa={MatrixValue[value=22,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaaaa aaaaaaaaaaa aaaaaaaaaaaa={MatrixValue[value=15,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaa aaaaaaaaaaaa aaaa.={MatrixValue[value=13,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaa aaaaaaaaaaaaa, aaa={MatrixValue[value=9,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaaaa aaaaaaa aaaaaaaaaaaa aaa.={MatrixValue[value=9,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaa aaaaaaaaaaaa, aaa.={MatrixValue[value=8,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaa aaaaaaaaaaaa aaaaaaaaaaaa, aaaa.={MatrixValue[value=8,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaa aaaaaaaa aaaaaaaaaaa, aaa={MatrixValue[value=8,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaa aaaaa aaaaaaaa aaaaaaaaa={MatrixValue[value=3,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaa aaaaaaaaa & aaa.={MatrixValue[value=3,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},?????????????.aaa={MatrixValue[value=2,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaaaa.aaa={MatrixValue[value=2,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaa aaa.={MatrixValue[value=2,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaa & aaaa aa.={MatrixValue[value=2,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaaaaaa.aa.aa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaa-aaaa aaaaaaaa aaa.={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaa+ aaaaaaaa aaaaaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaa'a aaaaaaaaaaa, aaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaa'a aaaaaaaa aa.={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaa'a aaaa aaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},a'aaaaaa aaaaaaaaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aa&a aaaaaaaaaaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaa aaaaa+ aaaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaa aaaaa& aa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aa aaaaa a'aaaaaaaaa, aa.={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaa aaaaa aa aaaa, aa.={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaa aaaaaa aaaa aaaaaa, aaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaa 'a' aa aaaaaaaaa, aaa.={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaaaa & aaaaaaa, aa.={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]},aaaaa & aaaaaaa aa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNAME\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNAME\"]}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Standard measures,profileClass=class dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile],matrices={Matrix[columnNames={CUSTOMERNAME},Row count={122},Null values={0},Empty values={0},Highest value={giftsbymail.co.uk},Lowest value={ANG Resellers}]}]" };

		assertEquals(2, results.size());
		assertEquals(results, expectations);
	}

	public void testColumnProfiles() throws Exception {
		Connection connection = getTestDbConnection();
		Schema schema = getTestDbSchema(connection);

		ProfileRunner profileRunner = new ProfileRunner();

		// Create profile definition for a single column
		final Table customersTable = schema.getTableByName("CUSTOMERS");
		final Table employeesTable = schema.getTableByName("EMPLOYEES");
		ProfileConfiguration conf1 = new ProfileConfiguration(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		conf1.setColumns(employeesTable.getColumns()[0], customersTable
				.getColumns()[0]);
		profileRunner.addConfiguration(conf1);

		// Create profile definition for multiple columns
		Column postalCodeColumn = customersTable
				.getColumnByName("ADDRESSLINE1");
		Column officeCodeColumn = customersTable
				.getColumnByName("ADDRESSLINE2");
		ProfileConfiguration conf2 = new ProfileConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf2.setColumns(postalCodeColumn, officeCodeColumn);
		profileRunner.addConfiguration(conf2);

		profileRunner.execute(new DataContext(connection));

		List<IProfileResult> results = profileRunner.getResults();

		String[] expectations = {
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={EMPLOYEENUMBER},9999={MatrixValue[value=23,detailQuery=SELECT \"EMPLOYEES\".\"EMPLOYEENUMBER\", COUNT(*) FROM APP.\"EMPLOYEES\" GROUP BY \"EMPLOYEES\".\"EMPLOYEENUMBER\"]}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Standard measures,profileClass=class dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile],matrices={Matrix[columnNames={ADDRESSLINE1,ADDRESSLINE2},Row count={122,122},Null values={0,MatrixValue[value=109,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE1\", \"CUSTOMERS\".\"ADDRESSLINE2\" FROM APP.\"CUSTOMERS\" WHERE \"CUSTOMERS\".\"ADDRESSLINE2\" IS NULL]},Empty values={0,0},Highest value={Åkergatan 24,Suite 750},Lowest value={1 rue Alsace-Lorraine,2nd Floor}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={CUSTOMERNUMBER},999={MatrixValue[value=122,detailQuery=SELECT \"CUSTOMERS\".\"CUSTOMERNUMBER\", COUNT(*) FROM APP.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"CUSTOMERNUMBER\"]}]}]" };

		assertEquals(expectations.length, results.size());
		assertEquals(results, expectations);
	}
}