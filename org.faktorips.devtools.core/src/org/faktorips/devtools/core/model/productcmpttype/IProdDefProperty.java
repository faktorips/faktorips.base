/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
