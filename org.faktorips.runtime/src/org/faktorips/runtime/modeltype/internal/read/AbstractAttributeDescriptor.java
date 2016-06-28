/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal.read;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.internal.AbstractAttributeModel;
import org.faktorips.runtime.modeltype.internal.ModelType;

abstract class AbstractAttributeDescriptor<T extends IModelTypeAttribute> extends PartDescriptor<T> {

    private AnnotatedElement annotatedElement;

    private Method setterMethod;

    public boolean isValid() {
        return getAnnotatedElement() instanceof Field || getAnnotatedElement() instanceof Method;
    }

    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    public void setAnnotatedElement(AnnotatedElement annotatedElement) {
        this.annotatedElement = annotatedElement;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public void setSetterMethod(Method setterMethod) {
        this.setterMethod = setterMethod;
    }

    @Override
    public T create(IModelElement parentElement) {
        ModelType modelType = (ModelType)parentElement;
        if (isValid()) {
            return createValid(modelType);
        } else {
            // it could be defined in a super type but overridden (with the same name and
            // datatype) in this type. That leads to a different implementation being generated
            // but not a new annotation.
            IModelType superType = modelType.getSuperType();
            if (superType != null) {
                IModelTypeAttribute attribute = superType.getAttribute(getName());
                if (attribute != null) {
                    @SuppressWarnings("unchecked")
                    T overwritingAttribute = (T)((AbstractAttributeModel)attribute)
                            .createOverwritingAttributeFor(modelType);
                    return overwritingAttribute;
                }
            }
            throw new IllegalArgumentException(modelType.getDeclarationClass() + " lists \"" + getName()
                    + "\" as one of it's @IpsAttributes but no matching @IpsAttribute could be found.");
        }
    }

    protected abstract T createValid(ModelType modelType);

}