/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type.read;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.faktorips.runtime.model.type.Attribute;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.Type;

public abstract class AbstractAttributeDescriptor<T extends Attribute> extends PartDescriptor<T> {

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
    public T create(ModelElement parentElement) {
        Type type = (Type)parentElement;
        if (isValid()) {
            return createValid(type);
        } else {
            // it could be defined in a super type but overridden (with the same name and
            // datatype) in this type. That leads to a different implementation being generated
            // but not a new annotation.

            Type superType = type.findSuperType()
                    .orElseThrow(() -> new IllegalArgumentException(type.getDeclarationClass() + " lists \"" + getName()
                            + "\" as one of it's @IpsAttributes but no matching @IpsAttribute could be found."));
            Attribute attribute = Optional.ofNullable(superType.getAttribute(getName()))
                    .orElseThrow(() -> new IllegalArgumentException(type.getDeclarationClass() + " lists \"" + getName()
                            + "\" as one of it's @IpsAttributes but no matching @IpsAttribute could be found."));

            @SuppressWarnings("unchecked")
            T overwritingAttribute = (T)attribute.createOverwritingAttributeFor(type);
            return overwritingAttribute;
        }
    }

    protected abstract T createValid(Type type);

}