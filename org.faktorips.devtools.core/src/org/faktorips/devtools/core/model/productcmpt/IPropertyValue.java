/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * Base interface for properties stored in product component generations like formulas, table
 * content usages, product component attributes and configuration elements.
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
     * Returns the property this object provides a value for. Returns <code>null</code> if the
     * property can't be found.
     * 
     * @param ipsProject The IPS project which search path is used.
     * 
     * @throws CoreException if an error occurs
     */
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type of the product definition property.
     * <p>
     * If type is {@link ProdDefPropertyType#VALUE} the property can be casted to
     * {@link IAttributeValue}.
     * <p>
     * If type is {@link ProdDefPropertyType#FORMULA} the property can be casted to {@link IFormula}.
     * <p>
     * If type is {@link ProdDefPropertyType#TABLE_CONTENT_USAGE} the property can be casted to
     * {@link ITableContentUsage}.
     * <p>
     * If type is {@link ProdDefPropertyType#DEFAULT_VALUE_AND_VALUESET} the property can be casted
     * to {@link IConfigElement}.
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
