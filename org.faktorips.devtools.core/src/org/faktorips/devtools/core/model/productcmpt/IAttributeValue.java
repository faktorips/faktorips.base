/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * 
 * @author Jan Ortmann
 */
public interface IAttributeValue extends IPropertyValue {

    public static final String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$

    /**
     * @deprecated Since the property value is deprecated also this property constant is. You should
     *             use {@link #PROPERTY_VALUE_HOLDER} instead
     */
    @Deprecated
    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    public static final String PROPERTY_VALUE_HOLDER = "valueHolder"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "ATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the value provides the value for,
     * can't be found.
     */
    public static final String MSGCODE_UNKNWON_ATTRIBUTE = MSGCODE_PREFIX + "UnknownAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not an element of the value set defined
     * in the model attribute.
     */
    public static final String MSGCODE_VALUE_NOT_IN_SET = MSGCODE_PREFIX + "ValueNotInSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the value provides the value for,
     * can't be found.
     */
    public static final String MSGCODE_INVALID_VALUE_HOLDER = MSGCODE_PREFIX + "InvalidValueHolder"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute defines the wrong Valuetype
     */
    public static final String MSGCODE_INVALID_VALUE_TYPE = MSGCODE_PREFIX + "InvalidValueType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that one supported language not set
     */
    public static final String MSGCODE_MULTILINGUAL_NOT_SET = MSGCODE_PREFIX + "MultilingualNotSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the hidden attribute value is not set to its default
     * value
     */
    public static final String MSGCODE_HIDDEN_ATTRIBUTE = MSGCODE_PREFIX + "HiddenAttribute"; //$NON-NLS-1$

    @Override
    public String getPropertyValue();

    /**
     * Returns the attribute's value.
     * <p>
     * For multi valued attributes this method returns the string representation of the list.
     * 
     * @deprecated Since 3.7 we support multi valued attributes. You should use
     *             {@link #getValueHolder()} instead.
     */
    @Deprecated
    public String getValue();

    /**
     * Sets the attribute's value.
     * <p>
     * For multi valued attributes this method set a the string as the first and only element.
     * 
     * @deprecated Since 3.7 we support multi valued attributes. You should use
     *             {@link #setValueHolder(IValueHolder)} instead, for example:
     *             {@code attributeValue.setValueHolder(AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue, "myValue"));}
     *             .
     */
    @Deprecated
    public void setValue(String newValue);

    /**
     * Getting the value holder object containing one or multiple values.
     * 
     * @return The value holder object holding the value of this attribute value.
     */
    public IValueHolder<?> getValueHolder();

    /**
     * Setting the value holder object containing one or multiple values.
     * 
     * @param valueHolder A value holder containing the values for this attribute value.
     */
    public void setValueHolder(IValueHolder<?> valueHolder);

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
    public IProductCmptTypeAttribute findAttribute(IIpsProject ipsProject);

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IAttributeValue}.
     * 
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    public IAttributeValue findTemplateProperty(IIpsProject ipsProject);

}
