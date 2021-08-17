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

import java.lang.reflect.Method;

import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.ModelElementCreator;

class SimpleGetterMethodModelDescriptor<T extends ModelElement> extends PartDescriptor<T> {
    private Method getterMethod;
    private ModelElementCreator<T> modelElementCreator;

    public SimpleGetterMethodModelDescriptor(ModelElementCreator<T> modelElementCreator) {
        this.modelElementCreator = modelElementCreator;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public void setGetterMethod(Method getterMethod) {
        this.getterMethod = getterMethod;
    }

    @Override
    public T create(ModelElement parentElement) {
        return modelElementCreator.create(parentElement, getName(), getGetterMethod());
    }

}