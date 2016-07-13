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
import java.lang.reflect.Method;
import java.util.Arrays;

import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.modeltype.IProductAttributeModel;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.ProductAttributeModel;

public class ProductAttributeModelCollector extends
AttributeCollector<IProductAttributeModel, ProductAttributeModelCollector.ProductAttributeDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public ProductAttributeModelCollector() {
        super(Arrays.asList(new ProductIpsAttributeProcessor(),
                new IpsAttributeSetterProcessor<ProductAttributeDescriptor>()));
    }

    @Override
    protected ProductAttributeDescriptor createDescriptor() {
        return new ProductAttributeDescriptor();
    }

    static class ProductIpsAttributeProcessor extends IpsAttributeProcessor<ProductAttributeDescriptor> {

        @Override
        public void process(ProductAttributeDescriptor descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setChangingOverTime(IProductComponentGeneration.class.isAssignableFrom(annotatedDeclaration
                    .getImplementationClass()));
            descriptor.setAnnotatedElement(annotatedElement);
        }

    }

    static class ProductAttributeDescriptor extends AbstractAttributeDescriptor<IProductAttributeModel> {

        private boolean changingOverTime;

        @Override
        public IProductAttributeModel createValid(ModelType modelType) {
            return new ProductAttributeModel(modelType, isChangingOverTime(), (Method)getAnnotatedElement(),
                    getSetterMethod());
        }

        public boolean isChangingOverTime() {
            return changingOverTime;
        }

        public void setChangingOverTime(boolean changingOverTime) {
            this.changingOverTime = changingOverTime;
        }

    }

}
