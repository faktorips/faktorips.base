/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.runtime.model.type.ProductAttribute;
import org.faktorips.runtime.model.type.Type;

public class ProductAttributeCollector
        extends AttributeCollector<ProductAttribute, ProductAttributeCollector.ProductAttributeDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public ProductAttributeCollector() {
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
            descriptor.setChangingOverTime(
                    IProductComponentGeneration.class.isAssignableFrom(annotatedDeclaration.getImplementationClass()));
            descriptor.setAnnotatedElement(annotatedElement);
        }

    }

    protected static class ProductAttributeDescriptor extends AbstractAttributeDescriptor<ProductAttribute> {

        private boolean changingOverTime;

        @Override
        public ProductAttribute createValid(Type type) {
            return new ProductAttribute(type, isChangingOverTime(), (Method)getAnnotatedElement(), getSetterMethod());
        }

        public boolean isChangingOverTime() {
            return changingOverTime;
        }

        public void setChangingOverTime(boolean changingOverTime) {
            this.changingOverTime = changingOverTime;
        }

    }

}
