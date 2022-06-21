/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpttype;

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * An attribute of a product component type.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeAttribute extends IAttribute, IValueSetOwner, IProductCmptProperty {

    public static final String PROPERTY_VISIBLE = "visible"; //$NON-NLS-1$

    /**
     * This constant defines the multi value property.
     * <p>
     * If this attribute is marked as multi value attribute, the attribute value could hold a list
     * of values. If it is not marked as multi valued, it is a single value attribute and could only
     * hold one value. The concept of multi value attributes is described in FIPS-887.
     * <p>
     * For example a person has only one name (single value attribute) but may speak multiple
     * foreign languages (multi value attribute).
     * 
     * @see #isMultiValueAttribute()
     * @see #setMultiValueAttribute(boolean)
     */
    public static final String PROPERTY_MULTI_VALUE_ATTRIBUTE = "multiValueAttribute"; //$NON-NLS-1$

    /**
     * This constant defines the multilingual property.
     * <p>
     * If this attribute is defined as multilingual the values could be entered in multiple
     * languages. An attribute could only be marked as multilingual if the data type is string.
     * 
     * @see #isMultilingual()
     * @see #setMultilingual(boolean)
     */
    public static final String PROPERTY_MULTILINGUAL = "multilingual"; //$NON-NLS-1$

    /**
     * This constant defines the multilingualSupported property.
     * <p>
     * This is a read only property!
     * <p>
     * The property multilingualSupported is true if the datatype of the attribute is string.
     * 
     * @see #isMultilingualSupported()
     */
    public static final String PROPERTY_MULTILINGUAL_SUPPORTED = "multilingualSupported"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but single/multiple
     * value configuration differs
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeSingeMultiValueDiffers"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but multilingual
     * configuration differs
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_MULTILINGUAL_DIFFERS = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeMultilingualDiffers"; //$NON-NLS-1$

    public static final String MSGCODE_INVALID_VALUE_SET = IAttribute.MSGCODE_PREFIX + "InvalidValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute is hidden but at the same time has a
     * default value that is not contained in the allowed values. For hidden attributes, this
     * <ol>
     * <li>is an error, not just a warning
     * <li>also applies if the default value is {@code null} because a mandatory field that has to
     * be set by the user does not make sense for hidden attributes
     * </ol>
     */
    public static final String MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN = IAttribute.MSGCODE_PREFIX
            + "DefaultNotInValueSetWhileHidden"; //$NON-NLS-1$

    /**
     * Returns the product component type the attribute belongs to.
     */
    public IProductCmptType getProductCmptType();

    /**
     * This method is defined in {@link IValueSetOwner}. It is also added to this interface to
     * provide more detailed documentation.
     * 
     * For component type attributes the allowed values set types are the types returned by
     * {@link IIpsProject#getValueSetTypes(org.faktorips.datatype.ValueDatatype)} using the
     * attribute's data type.
     * 
     * @throws IpsException if an error occurs.
     */
    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws IpsException;

    /**
     * Creates a copy of the given value set and assigns it to this attribute.
     */
    public void setValueSetCopy(IValueSet source);

    /**
     * Returns whether this attribute is a multi value attribute or not.
     * <p>
     * If this method returns false, this attribute defines a single value attribute. The attribute
     * value could only have one value. In case of multi value attributes, the attribute value could
     * hold a list of values. The concept of multi value attributes is described in FIPS-887.
     * <p>
     * For example a person has only one name (single value attribute) but may speak multiple
     * foreign languages (multi value attribute).
     * <p>
     * The default value is false.
     * 
     * @return true if this attribute is a multi value attribute, false if not
     */
    boolean isMultiValueAttribute();

    /**
     * Setting the property multi value attribute for this attribute.
     * <p>
     * If this attribute is marked as multi value attribute, the attribute value could hold a list
     * of values. If it is not marked as multi valued, it is a single value attribute and could only
     * hold one value. The concept of multi value attributes is described in FIPS-887.
     * <p>
     * For example a person has only one name (single value attribute) but may speak multiple
     * foreign languages (multi value attribute).
     * 
     * @param multiValueAttribute true to mark this attribute as multi value attribute, false to
     *            mark as single value attribute
     */
    void setMultiValueAttribute(boolean multiValueAttribute);

    /**
     * Returns whether this attribute is visible or not.
     * <p>
     * If this method returns false, the attribute will not be displayed in the component editor and
     * its value cannot be modified in the editor. If the method returns true, the attribute will be
     * displayed in the editor and can be edited by the user.
     * <p>
     * The default value is true.
     * 
     * @return true if the attribute is visible, false otherwise
     */
    boolean isVisible();

    /**
     * Setting the property <code>visible</code> for this attribute.
     * <p>
     * If this attribute is marked as visible, the attribute will be displayed in the component
     * editor. If not marked as visible, the attribute will not be displayed in the component
     * editor.
     * <p>
     * This flag is useful in combination with overwritten attributes when an insurance class needs
     * the attribute but does not need to modify its value.
     * 
     * @param visible true to mark the attribute as visible, false to mark it as invisible
     */
    void setVisible(boolean visible);

    /**
     * Returns whether this attribute support multi Lingual.
     * 
     * @return true if this attribute is a multi Lingual attribute, false if not
     */
    boolean isMultilingual();

    /**
     * Setting the property <code>Multi Lingual</code> for this attribute.
     * <p>
     * If this attribute is marked as multi Lingual, the text of this attribute can be entered in
     * different languages. If it is not marked as multi Lingual, the text of this attribute can
     * only be entered in one language in the Product-Component editor.
     * 
     * @param multiLingual to mark this attribute as multi lingual attribute, false to mark it as
     *            not supporting multi-lingual
     */
    void setMultilingual(boolean multiLingual);

    /**
     * Returns true if the property multilingual is supported by this attribute. Currently
     * multilingual is only supported for data type string.
     * 
     * @return <code>True</code> if the property multilingual is support, othervise false
     */
    boolean isMultilingualSupported();
}
