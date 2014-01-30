/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableAccessFunction extends AtomicIpsObjectPart implements ITableAccessFunction {

    private String accessedColumn;
    private String type;
    private String[] argTypes = new String[0];

    public TableAccessFunction(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    public TableAccessFunction() {
        super();
    }

    @Override
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return null;
    }

    @Override
    public String getName() {
        return getTableStructure().getName() + '.' + getAccessedColumn();
    }

    @Override
    public String getAccessedColumn() {
        return accessedColumn;
    }

    @Override
    public void setAccessedColumn(String columnName) {
        accessedColumn = columnName;
    }

    @Override
    public IColumn findAccessedColumn() {
        return getTableStructure().getColumn(accessedColumn);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String newType) {
        type = newType;
    }

    @Override
    public void setArgTypes(String[] types) {
        // make a defensive copy.
        argTypes = new String[types.length];
        System.arraycopy(types, 0, argTypes, 0, types.length);
    }

    @Override
    public String[] getArgTypes() {
        String[] types = new String[argTypes.length];
        System.arraycopy(argTypes, 0, types, 0, argTypes.length);
        return types; // return defensive copy
    }

}
