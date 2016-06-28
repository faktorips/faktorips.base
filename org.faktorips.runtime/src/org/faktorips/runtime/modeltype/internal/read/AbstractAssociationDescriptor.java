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

import java.lang.reflect.Method;

import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.ModelTypeAssociation;

abstract class AbstractAssociationDescriptor<P extends IModelTypeAssociation> extends PartDescriptor<P> {

    private Method annotatedElement;

    public boolean isValid() {
        return getAnnotatedElement() != null;
    }

    public Method getAnnotatedElement() {
        return annotatedElement;
    }

    public void setAnnotatedElement(Method annotatedElement) {
        this.annotatedElement = annotatedElement;
    }

    @Override
    public P create(IModelElement parentElement) {
        ModelType modelType = (ModelType)parentElement;
        if (isValid()) {
            return createValid(modelType);
        } else {
            // else it must be defined in a super type but overridden (with the same name and
            // target) in this type. That leads to a different implementation being generated
            // but not a new annotation.
            IModelType superType = modelType.getSuperType();
            if (superType != null) {
                IModelTypeAssociation association = superType.getAssociation(getName());
                if (association != null) {
                    @SuppressWarnings("unchecked")
                    P overwritingAssociationFor = (P)((ModelTypeAssociation)association)
                    .createOverwritingAssociationFor(modelType);
                    return overwritingAssociationFor;
                }
            }
            throw new IllegalArgumentException(modelType.getDeclarationClass() + " lists \"" + getName()
                    + "\" as one of it's @IpsAssociations but no matching @IpsAssociation could be found.");
        }
    }

    protected abstract P createValid(ModelType modelType);

}