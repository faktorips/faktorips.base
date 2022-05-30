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
import java.util.Arrays;

import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.type.ProductAssociation;
import org.faktorips.runtime.model.type.Type;

public class ProductAssociationCollector
        extends AssociationCollector<ProductAssociation, ProductAssociationCollector.ProductAssociationDescriptor> {

    public ProductAssociationCollector() {
        super(Arrays.<AnnotationProcessor<?, ProductAssociationDescriptor>> asList(new ProductIpsAssociationProcessor(),
                new IpsAssociationLinksProcessor<>(),
                new IpsAssociationAdderProcessorNoCardinality<ProductAssociationCollector.ProductAssociationDescriptor>(),
                new IpsAssociationAdderProcessorWithCardinality<>(),
                new IpsAssociationRemoverProcessor<ProductAssociationDescriptor>()));
    }

    @Override
    protected ProductAssociationDescriptor createDescriptor() {
        return new ProductAssociationDescriptor();
    }

    static class ProductIpsAssociationProcessor extends IpsAssociationProcessor<ProductAssociationDescriptor> {

        @Override
        public void process(ProductAssociationDescriptor descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            super.process(descriptor, annotatedDeclaration, annotatedElement);
            descriptor.setChangingOverTime(
                    IProductComponentGeneration.class.isAssignableFrom(annotatedDeclaration.getImplementationClass()));
        }
    }

    protected static class ProductAssociationDescriptor extends AbstractAssociationDescriptor<ProductAssociation> {

        private boolean changingOverTime;
        private Method getLinksMethod;
        private Method addWithCardinality;

        public boolean isChangingOverTime() {
            return changingOverTime;
        }

        public void setGetLinksMethod(Method getLinksMethod) {
            this.getLinksMethod = getLinksMethod;
        }

        public void setChangingOverTime(boolean changingOverTime) {
            this.changingOverTime = changingOverTime;
        }

        public void setAddMethodWithCardinality(Method addWithCardinality) {
            this.addWithCardinality = addWithCardinality;
        }

        @Override
        protected ProductAssociation createValid(Type type) {
            return new ProductAssociation(type, getAnnotatedElement(), getAddMethod(), addWithCardinality,
                    getRemoveMethod(),
                    changingOverTime,
                    getLinksMethod);
        }

    }

}
