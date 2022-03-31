/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;

public class TableAccessFunction implements ITableAccessFunction {

    private final ITableStructure tableStructure;

    private final String description;

    private final String type;

    private final List<String> argTypeNames;

    private final IColumn column;

    public TableAccessFunction(IIndex key, IColumn column) {
        ArgumentCheck.notNull(key);
        ArgumentCheck.notNull(column);
        this.column = column;
        tableStructure = key.getTableStructure();
        type = column.getDatatype();

        StringBuilder params = new StringBuilder();
        IKeyItem[] items = key.getKeyItems();
        List<String> tmpArgTypes = new ArrayList<>();
        for (IKeyItem item : items) {
            if (!tmpArgTypes.isEmpty()) {
                params.append(", "); //$NON-NLS-1$
            }
            params.append(item.getAccessParameterName());
            tmpArgTypes.add(item.getDatatype());
        }
        argTypeNames = Collections.unmodifiableList(tmpArgTypes);
        description = MessageFormat.format(Messages.TableAccessFunctionDescription, params, column.getName());
    }

    @Override
    public ITableStructure getTableStructure() {
        return tableStructure;
    }

    @Override
    public String getAccessedColumnName() {
        return column.getName();
    }

    @Override
    public IColumn getAccessedColumn() {
        return column;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<String> getArgTypes() {
        return argTypeNames;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IIpsProject getIpsProject() {
        return tableStructure.getIpsProject();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + argTypeNames.hashCode();
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((tableStructure == null) ? 0 : tableStructure.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TableAccessFunction other = (TableAccessFunction)obj;
        if (!argTypeNames.equals(other.argTypeNames)) {
            return false;
        }
        if (!column.equals(other.column)) {
            return false;
        }
        if (!description.equals(other.description)) {
            return false;
        }
        if (ObjectUtils.notEqual(tableStructure, tableStructure)) {
            return false;
        }
        if (ObjectUtils.notEqual(type, other.type)) {
            return false;
        }
        return true;
    }

}
