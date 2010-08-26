/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
