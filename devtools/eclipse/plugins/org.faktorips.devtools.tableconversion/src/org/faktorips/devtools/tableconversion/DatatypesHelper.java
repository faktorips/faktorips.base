/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion;

import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * Utility methods for finding the datatypes of table columns and enum attributes.
 */
public final class DatatypesHelper {

    private DatatypesHelper() {
    }

    /**
     * Returns an array of datatypes for all columns of the given structure, looked up in the given
     * project. The project should be the one owning the table contents, not the structure, to
     * correctly resolve named datatypes in separated projects.
     */
    public static Datatype[] findTableColumnDatatypes(IIpsProject project, ITableStructure structure) {
        IColumn[] columns = structure.getColumns();
        Datatype[] datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = project.findDatatype(columns[i].getDatatype());
        }
        return datatypes;
    }

    /**
     * Returns an array of datatypes for all enum attributes of the given value container, looked up
     * in the project of the value container. Using the container's project correctly resolves named
     * datatypes when enum type and content live in separated projects.
     */
    public static Datatype[] findEnumAttributeDatatypes(IEnumValueContainer valueContainer,
            boolean includeLiteralName) {
        IEnumType enumType = valueContainer.findEnumType(valueContainer.getIpsProject());
        List<IEnumAttribute> attributes = enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        Datatype[] datatypes = new Datatype[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            datatypes[i] = attributes.get(i).findDatatype(valueContainer.getIpsProject());
        }
        return datatypes;
    }
}
