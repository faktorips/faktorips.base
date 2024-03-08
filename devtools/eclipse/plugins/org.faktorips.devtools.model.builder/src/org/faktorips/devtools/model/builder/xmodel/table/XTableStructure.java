/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.XClass;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * This is the generator model node representing a {@link ITableStructure}.
 * 
 * @author dirmeier
 */
public abstract class XTableStructure extends XClass {

    public XTableStructure(ITableStructure tableStructure, GeneratorModelContext context, ModelService modelService) {
        super(tableStructure, context, modelService);
    }

    public List<XColumn> getValidColumns() {
        IColumn[] columns = ((ITableStructure)getIpsObjectPartContainer()).getColumns();
        List<XColumn> result = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].isValid(getIpsProject())) {
                XColumn xColumn = getModelNode(columns[i], XColumn.class);
                xColumn.setIndexInList(i);
                result.add(xColumn);

            }
        }
        return result;
    }

    @Override
    public boolean isValidForCodeGeneration() {
        return getIpsObjectPartContainer().isValid(getIpsProject());
    }

    @Override
    protected abstract String getBaseSuperclassName();

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        return new LinkedHashSet<>();
    }

    @Override
    public abstract LinkedHashSet<String> getImplementedInterfaces();

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        return new LinkedHashSet<>();
    }

    public String getFieldNameForNullRow() {
        return "NULL_ROW";
    }
}
