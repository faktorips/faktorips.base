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
import java.util.Arrays;

import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.modeltype.IProductModelAssociation;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.ProductModelAssociation;

public class ProductModelAssociationCollector extends
        AssociationCollector<IProductModelAssociation, ProductModelAssociationCollector.ProductAssociationDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public ProductModelAssociationCollector() {
        super(
                Arrays.<AnnotationProcessor<?, ProductAssociationDescriptor>> asList(new IpsAssociationProcessor<ProductModelAssociationCollector.ProductAssociationDescriptor>()));
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
            descriptor.setChangingOverTime(IProductComponentGeneration.class.isAssignableFrom(annotatedDeclaration
                    .getImplementationClass()));
        }

    }

    static class ProductAssociationDescriptor extends AbstractAssociationDescriptor<IProductModelAssociation> {

        private boolean changingOverTime;

        public boolean isChangingOverTime() {
            return changingOverTime;
        }

        public void setChangingOverTime(boolean changingOverTime) {
            this.changingOverTime = changingOverTime;
        }

        @Override
        protected IProductModelAssociation createValid(ModelType modelType) {
            return new ProductModelAssociation(modelType, getAnnotatedElement(), changingOverTime);
        }
    }

}
