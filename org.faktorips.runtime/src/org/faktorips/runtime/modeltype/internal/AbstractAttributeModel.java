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

import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * A {@link AbstractAttributeModel} represents an attribute from a PolicyCmptType or a
 * ProductCmptType.
 */
public abstract class AbstractAttributeModel extends ModelPart implements IModelTypeAttribute {

    private final IpsAttribute attributeAnnotation;

    private final Class<?> datatype;

    private final boolean changingOverTime;

    public AbstractAttributeModel(ModelType modelType, IpsAttribute attributeAnnotation,
            IpsExtensionProperties extensionProperties, Class<?> datatype, boolean changingOverTime) {
        super(attributeAnnotation.name(), modelType, extensionProperties);
        this.attributeAnnotation = attributeAnnotation;
        this.datatype = datatype;
        this.changingOverTime = changingOverTime;
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
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

    /**
     * Creates an attribute model for a sub type in which this attribute is overwritten. This is
     * necessary to retrieve information contained in the class annotation such as labels if no
     * getter is generated for the overwritten attribute in the sub class.
     * 
     * @param subModelType a model type representing a sub type of this attribute's model type
     * @return a {@link AbstractAttributeModel} for the given sub model type
     */
    public abstract AbstractAttributeModel createOverwritingAttributeFor(ModelType subModelType);

    protected Object getRelevantProductObject(IProductComponent productComponent, Calendar effectiveDate) {
        return getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime());
    }

}
