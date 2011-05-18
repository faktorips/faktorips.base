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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * 
 * @author Jan Ortmann
 */
public interface IAttributeValue extends IPropertyValue {

    public final static String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ATRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the value provides the value for,
     * can't be found.
     */
    public final static String MSGCODE_UNKNWON_ATTRIBUTE = MSGCODE_PREFIX + "UnknownAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not an element of the value set defined
     * in the model attribute.
     */
    public final static String MSGCODE_VALUE_NOT_IN_SET = MSGCODE_PREFIX + "ValueNotInSet"; //$NON-NLS-1$

    /**
     * Returns the product component generation this value belongs to.
     */
    @Override
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the attribute's value.
     */
    public String getValue();

    /**
     * Sets the attribute's value.
     */
    public void setValue(String newValue);

    /**
     * Returns the name of the product component type's attribute this is a value for.
     */
    public String getAttribute();

    /**
     * Sets the name of the product component type's attribute this is a value for.
     * 
     * @throws NullPointerException if name is <code>null</code>.
     */
    public void setAttribute(String name);

    /**
     * Returns the product component type attribute this object provides the value for.
     */
    public IProductCmptTypeAttribute findAttribute(IIpsProject ipsProject) throws CoreException;

}
