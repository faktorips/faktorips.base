/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.faktorips.datatype.Datatype;

/*
 * Class to identify a column inside the table
 */
public class ColumnIdentifier{
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