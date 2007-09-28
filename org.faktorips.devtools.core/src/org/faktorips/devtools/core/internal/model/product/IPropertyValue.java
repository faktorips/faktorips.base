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

package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype2.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public interface IPropertyValue {

    /**
     * Returns the product component generation this value belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();
    
    /**
     * Returns the name of the product definition property, this is a value of.
     * 
     * @see IProdDefProperty 
     */
    public String getPropertyName();

    /**
     * Returns the propery this object provides a value for. Returns <code>null</code> if the property can't be found.
     * 
     * @param ispProject The ips project which search path is used.
     * 
     * @throws CoreException if an error occures 
     */
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException;
    
    /**
     * Returns the type of the property. 
     */
    public ProdDefPropertyType getPropertyType();
    
    /**
     * Returns the value.
     */
    public String getPropertyValue();
    
    /**
     * Removes the part from the parent.
     */
    public void delete();
}
