/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * A {@link Attribute} represents an attribute from a PolicyCmptType or a ProductCmptType.
 */
@SuppressWarnings("deprecation")
public abstract class Attribute extends TypePart implements IModelTypeAttribute {

    private final IpsAttribute attributeAnnotation;

    private final Class<?> datatype;

    private final boolean changingOverTime;

    public Attribute(Type type, IpsAttribute attributeAnnotation, IpsExtensionProperties extensionProperties,
            Class<?> datatype, boolean changingOverTime) {
        super(attributeAnnotation.name(), type, extensionProperties);
        this.attributeAnnotation = attributeAnnotation;
        this.datatype = datatype;
        this.changingOverTime = changingOverTime;
    }

    /**
     * Checks whether this attribute is changing over time. For product attribute that means the
     * attribute resides in the generation. For policy attributes the optional product configuration
     * ({@link #isProductRelevant()}) resides in the generation.
     * 
     * @return whether or not this attribute is changing over time.
     */
    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    /**
     * Returns if this attribute is product relevant.
     */
    @Override
    public abstract boolean isProductRelevant();

    /**
     * @return this attribute's datatype <code>Class</code>.
     * @throws ClassNotFoundException if the datatype's class can not be loaded.
     */
    @Override
    public Class<?> getDatatype() throws ClassNotFoundException {
        return datatype;
    }

    /**
     * @return what kind of attribute this is.
     */
    public AttributeKind getAttributeKind() {
        return attributeAnnotation.kind();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link #getAttributeKind()}
     */
    @Deprecated
    @Override
    public AttributeType getAttributeType() {
        return AttributeType.forName(getAttributeKind().name());
    }

    /**
     * @return the kind of value set restricting this attribute
     */
    public ValueSetKind getValueSetKind() {
        return attributeAnnotation.valueSetKind();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link #getValueSetKind()}
     */
    @Deprecated
    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.valueOf(getValueSetKind().name());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatype.getSimpleName());
        sb.append('(');
        sb.append(getAttributeKind());
        sb.append(", ");
        sb.append(getValueSetKind());
        if (isProductRelevant()) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Creates an attribute model for a sub type in which this attribute is overwritten. This is
     * necessary to retrieve information contained in the class annotation such as labels if no
     * getter is generated for the overwritten attribute in the sub class.
     * 
     * @param subType a model type representing a sub type of this attribute's model type
     * @return a {@link Attribute} for the given sub model type
     */
    public abstract Attribute createOverwritingAttributeFor(Type subType);

    protected Object getRelevantProductObject(IProductComponent productComponent, Calendar effectiveDate) {
        return getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime());
    }

}
