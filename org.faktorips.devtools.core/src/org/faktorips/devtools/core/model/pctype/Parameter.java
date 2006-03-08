/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;


/**
 *
 */
public class Parameter {
    
    private int index;
    private String name;
    private String datatype;
    
    public Parameter(int index) {
        this(index, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public Parameter(int index, String name, String datatype) {
        this.index = index;
        this.name = name;
        this.datatype = datatype;
    }
    
    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        name = newName;
    }
    
    public String getDatatype() {
        return datatype;
    }
    
    public void setDatatype(String newDatatype) {
        datatype = newDatatype;
    }
    
    public String toString() {
        return index + " " + datatype + " " + name; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
