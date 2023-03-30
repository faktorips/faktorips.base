/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * 
 * @author Jan Ortmann
 */
public interface IAttributeValue extends IPropertyValue {

    String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

    String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$

    String PROPERTY_VALUE_HOLDER = "valueHolder"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "ATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the value provides the value for,
     * can't be found.
     */
    String MSGCODE_UNKNWON_ATTRIBUTE = MSGCODE_PREFIX + "UnknownAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not an element of the value set defined
     * in the model attribute.
     */
    String MSGCODE_VALUE_NOT_IN_SET = MSGCODE_PREFIX + "ValueNotInSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the value provides the value for,
     * can't be found.
     */
    String MSGCODE_INVALID_VALUE_HOLDER = MSGCODE_PREFIX + "InvalidValueHolder"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute defines the wrong Valuetype
     */
    String MSGCODE_INVALID_VALUE_TYPE = MSGCODE_PREFIX + "InvalidValueType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that one supported language not set
     */
    String MSGCODE_MULTILINGUAL_NOT_SET = MSGCODE_PREFIX + "MultilingualNotSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the hidden attribute value is not set to its default
     * value
     */
    String MSGCODE_HIDDEN_ATTRIBUTE = MSGCODE_PREFIX + "HiddenAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a mandatory attribute value is empty or null.
     */
    String MSGCODE_MANDATORY_VALUE_NOT_DEFINED = MSGCODE_PREFIX + "MandatoryValueNotDefined"; //$NON-NLS-1$

    @Override
    String getPropertyValue();

    /**
     * Getting the value holder object containing one or multiple values.
     * 
     * @return The value holder object holding the value of this attribute value.
     */
    IValueHolder<?> getValueHolder();

    /**
     * Setting the value holder object containing one or multiple values.
     * 
     * @param valueHolder A value holder containing the values for this attribute value.
     */
    void setValueHolder(IValueHolder<?> valueHolder);

    /**
     * Returns the name of the product component type's attribute this is a value for.
     */
    String getAttribute();

    /**
     * Sets the name of the product component type's attribute this is a value for.
     * 
     * @throws NullPointerException if name is <code>null</code>.
     */
    void setAttribute(String name);

    /**
     * Returns the product component type attribute this object provides the value for.
     */
    IProductCmptTypeAttribute findAttribute(IIpsProject ipsProject);

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IAttributeValue}.
     * 
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    IAttributeValue findTemplateProperty(IIpsProject ipsProject);

}
