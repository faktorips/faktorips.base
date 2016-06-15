/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * A {@link AbstractModelTypeAttribute} represents an attribute from a PolicyCmptType or a
 * ProductCmptType.
 */
public abstract class AbstractModelTypeAttribute extends ModelPart implements IModelTypeAttribute {

    private final IpsAttribute attributeAnnotation;

    private final Class<?> datatype;

    public AbstractModelTypeAttribute(ModelType modelType, IpsAttribute attributeAnnotation,
            IpsExtensionProperties extensionProperties, Class<?> datatype) {
        super(attributeAnnotation.name(), modelType, extensionProperties);
        this.attributeAnnotation = attributeAnnotation;
        this.datatype = datatype;
    }

    @Override
    public Class<?> getDatatype() throws ClassNotFoundException {
        return datatype;
    }

    @Override
    public AttributeType getAttributeType() {
        return attributeAnnotation.type();
    }

    @Override
    public ValueSetType getValueSetType() {
        return attributeAnnotation.valueSetType();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatype.getSimpleName());
        sb.append('(');
        sb.append(getAttributeType());
        sb.append(", ");
        sb.append(getValueSetType());
        if (isProductRelevant()) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    protected void handleGetterError(IModelObject source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get attribute %s on source object %s.", getName(),
                source), e);
    }

    /**
     * Creates an attribute model for a sub type in which this attribute is overwritten. This is
     * necessary to retrieve information contained in the class annotation such as labels if no
     * getter is generated for the overwritten attribute in the sub class.
     * 
     * @param subModelType a model type representing a sub type of this attribute's model type
     * @return a {@link ModelTypeAttribute} for the given sub model type
     */
    abstract AbstractModelTypeAttribute createOverwritingAttributeFor(ModelType subModelType);

}
