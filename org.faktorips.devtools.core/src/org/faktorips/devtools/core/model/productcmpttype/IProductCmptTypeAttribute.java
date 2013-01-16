/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * An attribute of a product component type.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeAttribute extends IAttribute, IValueSetOwner, IProductCmptProperty {

    public final static String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$
    public final static String PROPERTY_VISIBLE = "visible"; //$NON-NLS-1$

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
    public final static String PROPERTY_MULTI_VALUE_ATTRIBUTE = "multiValueAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but single/multiple
     * value configuration differs
     */
    public final static String MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeSingeMultiValueDiffers"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but change over time
     * configuration differs
     */
    public final static String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeDifferentChangeOverTime"; //$NON-NLS-1$

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
     * @throws CoreException if an error occurs.
     */
    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a copy of the given value set and assigns it to this attribute.
     */
    public void setValueSetCopy(IValueSet source);

    /**
     * Configures this attribute to change or be constant over time. If <code>true</code> every
     * {@link IProductCmptGeneration} may specify a different value for this attribute. If
     * <code>false</code> the value is the same for all generations.
     * 
     * @param changesOverTime whether or not this attribute should change over time
     */
    public void setChangingOverTime(boolean changesOverTime);

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
}
