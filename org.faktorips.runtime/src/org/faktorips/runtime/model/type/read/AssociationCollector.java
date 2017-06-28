/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.type.Association;

public abstract class AssociationCollector<T extends Association, D extends AbstractAssociationDescriptor<T>>
        extends TypePartCollector<T, D> {

    public AssociationCollector(List<AnnotationProcessor<?, D>> annotationAccessors) {
        super(annotationAccessors);
    }

    @Override
    protected String[] getNames(AnnotatedDeclaration annotatedDeclaration) {
        if (annotatedDeclaration.is(IpsAssociations.class)) {
            return annotatedDeclaration.get(IpsAssociations.class).value();
        } else {
            return NO_NAMES;
        }
    }

    @Override
    protected void addPart(LinkedHashMap<String, T> result, T part) {
        super.addPart(result, part);
        if (IpsStringUtils.isNotEmpty(part.getNamePlural())) {
            result.put(IpsStringUtils.toLowerFirstChar(part.getNamePlural()), part);
        }
    }

    static class IpsAssociationProcessor<D extends AbstractAssociationDescriptor<? extends Association>>
            extends AnnotationProcessor<IpsAssociation, D> {

        public IpsAssociationProcessor() {
            super(IpsAssociation.class);
        }

        @Override
        public String getName(IpsAssociation annotation) {
            return annotation.name();
        }

        @Override
        public void process(D descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setAnnotatedElement((Method)annotatedElement);
        }

    }

}
