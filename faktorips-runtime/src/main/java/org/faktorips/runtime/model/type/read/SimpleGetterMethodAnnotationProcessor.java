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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NameAccessor;

class SimpleGetterMethodAnnotationProcessor<A extends Annotation, D extends SimpleGetterMethodModelDescriptor<?>>
        extends AnnotationProcessor<A, D> {

    private NameAccessor<A> nameAccessor;

    public SimpleGetterMethodAnnotationProcessor(Class<A> annotationClass, NameAccessor<A> nameAccessor) {
        super(annotationClass);
        this.nameAccessor = nameAccessor;
    }

    @Override
    public void process(D descriptor, AnnotatedDeclaration annotatedDeclaration, AnnotatedElement annotatedElement) {
        descriptor.setGetterMethod(((Method)annotatedElement));
    }

    @Override
    public String getName(A annotation) {
        return nameAccessor.getName(annotation);
    }
}