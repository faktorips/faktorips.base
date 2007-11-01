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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.Described;


/**
 * A property of a product component type. 
 * 
 * @author Jan Ortmann
 */
public interface IProdDefProperty extends Described {

    /**
     * Returns the type of the property.
     */
    public ProdDefPropertyType getProdDefPropertyType();
    
    /**
     * Returns the name of the property.
     */
    public String getPropertyName();
    
    /**
     * Returns the name of the datatype.
     */
    public String getPropertyDatatype();
    

}
