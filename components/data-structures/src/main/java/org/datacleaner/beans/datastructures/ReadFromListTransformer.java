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
package org.datacleaner.beans.datastructures;

import org.datacleaner.api.*;
import org.datacleaner.components.categories.DataStructuresCategory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Transformer for extracting elements from lists.
 */
@Named("Read elements from list")
@Description("Reads all elements of a list, creating a record for each value")
@Categorized(DataStructuresCategory.class)
public class ReadFromListTransformer implements Transformer {

	@Inject
	@Configured
	InputColumn<List<?>> listColumn;

	@Inject
	@Configured
	Class<?> elementType;

	@Inject
	@Configured
	@Description("Verify that expected element type and actual type are the same")
	boolean verifyTypes = false;

	@Inject
	@Provided
	OutputRowCollector outputRowCollector;

	public void setElementType(Class<?> elementType) {
		this.elementType = elementType;
	}

	public void setListColumn(InputColumn<List<?>> listColumn) {
		this.listColumn = listColumn;
	}

	public void setVerifyTypes(boolean verifyTypes) {
		this.verifyTypes = verifyTypes;
	}

	@Override
	public OutputColumns getOutputColumns() {
		String[] columnNames = new String[] { listColumn.getName() + " (element)" };
		Class<?>[] columnTypes = new Class[] { elementType };
		return new OutputColumns(columnNames, columnTypes);
	}

	@Override
	public Object[] transform(InputRow row) {
		List<?> list = row.getValue(listColumn);
		if (list == null || list.isEmpty()) {
			return new Object[1];
		}

		for (Object value : list) {
			if (verifyTypes) {
				value = elementType.cast(value);
			}
			outputRowCollector.putValues(value);
		}

		return null;
	}

}
