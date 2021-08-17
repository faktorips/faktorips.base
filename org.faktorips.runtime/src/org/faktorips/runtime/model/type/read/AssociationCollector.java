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
import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.type.Association;
import org.faktorips.runtime.model.type.read.ProductAssociationCollector.ProductAssociationDescriptor;

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

    abstract static class IpsAssociationAdderProcessor<D extends AbstractAssociationDescriptor<? extends Association>>
            extends AnnotationProcessor<IpsAssociationAdder, D> {

        private final boolean withCardinality;

        public IpsAssociationAdderProcessor(boolean withCardinality) {
            super(IpsAssociationAdder.class);
            this.withCardinality = withCardinality;
        }

        @Override
        public String getName(IpsAssociationAdder annotation) {
            return annotation.association();
        }

        @Override
        public boolean accept(AnnotatedElement annotatedElement) {
            return super.accept(annotatedElement) && cardinalityMatches(annotatedElement);
        }

        public boolean cardinalityMatches(AnnotatedElement annotatedElement) {
            return !(withCardinality ^ (annotatedElement
                    .getAnnotation(IpsAssociationAdder.class).withCardinality()));
        }

    }

    static class IpsAssociationAdderProcessorNoCardinality<D extends AbstractAssociationDescriptor<? extends Association>>
            extends IpsAssociationAdderProcessor<D> {

        public IpsAssociationAdderProcessorNoCardinality() {
            super(false);
        }

        @Override
        public void process(D descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setAddMethod((Method)annotatedElement);
        }
    }

    static class IpsAssociationAdderProcessorWithCardinality<D extends ProductAssociationDescriptor>
            extends IpsAssociationAdderProcessor<D> {

        public IpsAssociationAdderProcessorWithCardinality() {
            super(true);
        }

        @Override
        public void process(D descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setAddMethodWithCardinality((Method)annotatedElement);
        }
    }

    static class IpsAssociationRemoverProcessor<D extends AbstractAssociationDescriptor<? extends Association>>
            extends AnnotationProcessor<IpsAssociationRemover, D> {

        public IpsAssociationRemoverProcessor() {
            super(IpsAssociationRemover.class);
        }

        @Override
        public String getName(IpsAssociationRemover annotation) {
            return annotation.association();
        }

        @Override
        public void process(D descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setRemoveMethod((Method)annotatedElement);
        }
    }

    static class IpsAssociationLinksProcessor<D extends ProductAssociationDescriptor>
            extends AnnotationProcessor<IpsAssociationLinks, D> {

        public IpsAssociationLinksProcessor() {
            super(IpsAssociationLinks.class);
        }

        @Override
        public String getName(IpsAssociationLinks annotation) {
            return annotation.association();
        }

        @Override
        public void process(ProductAssociationDescriptor descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setGetLinksMethod((Method)annotatedElement);
        }

    }

}
