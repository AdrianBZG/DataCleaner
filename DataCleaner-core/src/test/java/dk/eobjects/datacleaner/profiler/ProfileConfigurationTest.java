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
package dk.eobjects.datacleaner.profiler;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;

public class ProfileConfigurationTest extends DataCleanerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ProfileManagerTest.initProfileManager();
	}

	public void testDeserializeAndSerialize() throws Exception {
		DocumentBuilder db = DomHelper.getDocumentBuilder();
		Document document = db
				.parse(getTestResourceAsFile("serialized_profile_configuration.xml"));

		DataContext dc = new DataContext(getTestDbConnection());

		ProfileConfiguration deserializedConfiguration = ProfileConfiguration
				.deserialize(document.getDocumentElement(), dc);

		assertSame(StandardMeasuresProfile.class, deserializedConfiguration
				.getProfileDescriptor().getProfileClass());

		Map<String, String> properties = deserializedConfiguration
				.getProfileProperties();
		assertEquals(2, properties.size());
		assertEquals("bar1", properties.get("foo1"));
		assertEquals("bar2", properties.get("foo2"));

		Column[] columns = deserializedConfiguration.getColumns();
		assertEquals(2, columns.length);
		assertEquals(
				"{JdbcColumn[name=CUSTOMERNAME,columnNumber=1,type=VARCHAR,nullable=true,indexed=false,nativeType=VARCHAR,columnSize=255],"
						+ "JdbcColumn[name=PHONE,columnNumber=4,type=VARCHAR,nullable=true,indexed=false,nativeType=VARCHAR,columnSize=255]}",
				ArrayUtils.toString(columns));

		Element serializedConfiguration = deserializedConfiguration
				.serialize(document);

		StringWriter sw = new StringWriter();
		DomHelper.transform(serializedConfiguration, new StreamResult(sw));
		assertEquals(
				"<?xml version=_1.0_ encoding=_UTF-8_?><configuration profileClass=_dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile_><property name=_foo1_>bar1</property><property name=_foo2_>bar2</property><column schema=_APP_ table=_CUSTOMERS_>CUSTOMERNAME</column><column schema=_APP_ table=_CUSTOMERS_>PHONE</column></configuration>",
				sw.toString().replace('\"', '_'));
	}
}