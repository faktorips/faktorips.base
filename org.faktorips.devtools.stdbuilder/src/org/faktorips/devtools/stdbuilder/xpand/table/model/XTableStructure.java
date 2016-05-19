/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.table.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

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
        try {
            IColumn[] columns = ((ITableStructure)getIpsObjectPartContainer()).getColumns();
            List<XColumn> result = new ArrayList<XColumn>();
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].isValid(getIpsProject())) {
                    XColumn xColumn = getModelNode(columns[i], XColumn.class);
                    xColumn.setIndexInList(i);
                    result.add(xColumn);

                }
            }
            return result;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            return getIpsObjectPartContainer().isValid(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected abstract String getBaseSuperclassName();

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        return new LinkedHashSet<String>();
    }

    @Override
    public abstract LinkedHashSet<String> getImplementedInterfaces();

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        return new LinkedHashSet<String>();
    }

    public String getFieldNameForNullRow() {
        return "NULL_ROW";
    }
}
