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

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;

public abstract class AnnotationProcessor<T extends Annotation, D> {

    private Class<T> annotationClass;

    public AnnotationProcessor(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public boolean accept(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(annotationClass);
    }

    public String getName(AnnotatedElement annotatedElement) {
        return getName(getAnnotation(annotatedElement));
    }

    public T getAnnotation(AnnotatedElement annotatedElement) {
        return annotatedElement.getAnnotation(annotationClass);
    }

    public abstract String getName(T annotation);

    public abstract void process(D descriptor,
            AnnotatedDeclaration annotatedDeclaration,
            AnnotatedElement annotatedElement);

}
