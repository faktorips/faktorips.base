/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.table;

import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.internal.tableindex.RangeType;

/**
 * This class is a helper class for the method initKeyMap in table. It represents a range key item
 * with additional informations involving index. {@link XIndex#getKeyItemsForInitKeyMap()}
 */
public class XColumnRangeSearchStructure extends AbstractGeneratorModelNode {

    private String searchStructureName;
    private String prevSearchStructureName;
    private GenericTypeStringWrapper genericType;

    public XColumnRangeSearchStructure(IColumnRange columnRange, GeneratorModelContext context,
            ModelService modelService, String searchStructureName, String prevSearchStructureName,
            GenericTypeStringWrapper genericType) {
        super(columnRange, context, modelService);
        this.searchStructureName = searchStructureName;
        this.prevSearchStructureName = prevSearchStructureName;
        this.genericType = genericType;
    }

    public String getSearchStrucutreName() {
        return searchStructureName;
    }

    public String getPrevSearchStructureName() {
        return prevSearchStructureName;
    }

    public String getGenericType() {
        return genericType.toString();
    }

    public String getGenericTypeClass() {
        return genericType.getGenericClass();
    }

    public String getGenericTypeParams() {
        return genericType.paramsWithBracket();
    }

    /**
     * @return parameter name of the column range if the column type is one column (from or to),
     *         else return empty String
     */
    public String getRangeStructureParameter() {
        return getRangeStructureParameter(getColumnRange());
    }

    public String getRangeStructureParameter(IColumnRange cr) {
        String parameter = "";

        ColumnRangeType columnRangeType = cr.getColumnRangeType();
        if (columnRangeType.isOneColumnFrom()) {
            parameter = addImport(RangeType.class) + "." + RangeType.LOWER_BOUND_EQUAL.name();
        } else if (columnRangeType.isOneColumnTo()) {
            parameter = addImport(RangeType.class) + "." + RangeType.UPPER_BOUND_EQUAL.name();
        }

        return parameter;
    }

    private IColumnRange getColumnRange() {
        return (IColumnRange)getIpsObjectPartContainer();
    }

    public XColumn[] getColumns() {
        IColumn[] iColumns = getColumnRange().getColumns();
        XColumn[] columns = new XColumn[iColumns.length];
        for (int i = 0; i < iColumns.length; i++) {
            columns[i] = getModelNode(iColumns[i], XColumn.class);
        }
        return columns;
    }
}
