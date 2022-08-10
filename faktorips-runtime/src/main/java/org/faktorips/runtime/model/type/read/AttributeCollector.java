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
import java.lang.reflect.Method;
import java.util.List;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.type.Attribute;

public abstract class AttributeCollector<T extends Attribute, D extends AbstractAttributeDescriptor<T>> extends
        TypePartCollector<T, D> {

    public AttributeCollector(List<AnnotationProcessor<?, D>> annotationAccessors) {
        super(annotationAccessors);
    }

    @Override
    protected String[] getNames(AnnotatedDeclaration annotatedDeclaration) {
        if (annotatedDeclaration.is(IpsAttributes.class)) {
            return annotatedDeclaration.get(IpsAttributes.class).value();
        } else {
            return NO_NAMES;
        }
    }

    static class IpsAttributeProcessor<D extends AbstractAttributeDescriptor<? extends Attribute>> extends
            AnnotationProcessor<IpsAttribute, D> {

        public IpsAttributeProcessor() {
            super(IpsAttribute.class);
        }

        @Override
        public String getName(IpsAttribute annotation) {
            return annotation.name();
        }

        @Override
        public void process(D descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setAnnotatedElement(annotatedElement);
        }

    }

    static class IpsAttributeSetterProcessor<D extends AbstractAttributeDescriptor<? extends Attribute>> extends
            AnnotationProcessor<IpsAttributeSetter, D> {

        public IpsAttributeSetterProcessor() {
            super(IpsAttributeSetter.class);
        }

        @Override
        public String getName(IpsAttributeSetter annotation) {
            return annotation.value();
        }

        @Override
        public void process(D descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setSetterMethod((Method)annotatedElement);
        }

    }

}
