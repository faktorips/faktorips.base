/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.faktorips.datatype.Datatype;

/*
 * Class to identify a column inside the table
 */
public class ColumnIdentifier {
    private String property;
    private Datatype datatype;
    private int index;

    public ColumnIdentifier(String property, Datatype datatype, int index) {
        this.property = property;
        this.datatype = datatype;
        this.index = index;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public int getIndex() {
        return index;
    }

    public String getProperty() {
        return property;
    }
}
