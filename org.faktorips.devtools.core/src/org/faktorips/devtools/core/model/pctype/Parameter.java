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

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;


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
    	// Parameter-Klasse entfaellt demnaechst und wird durch IParameter und die zug. Implementierung ersetzt
    	// Dann wird dieser Hack ueberfluessig
    	IpsModel model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
    	model.getValidationResultCache().removeStaleData(null);
        name = newName;
    }
    
    public String getDatatype() {
        return datatype;
    }
    
    public void setDatatype(String newDatatype) {
    	// Parameter-Klasse entfaellt demnaechst und wird durch IParameter und die zug. Implementierung ersetzt
    	// Dann wird dieser Hack ueberfluessig
    	IpsModel model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
    	model.getValidationResultCache().removeStaleData(null);
        datatype = newDatatype;
    }
    
    public String toString() {
        return index + " " + datatype + " " + name; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
