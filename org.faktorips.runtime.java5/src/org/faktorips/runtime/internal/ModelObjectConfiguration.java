/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.internal;

import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.w3c.dom.Element;

/**
 * Manages a product component and the corresponding generation for use with configurable model
 * objects. Used by all configurable policy component classes to manage their product configuration.
 */
public class ModelObjectConfiguration {

    private static final String XML_ATTRIBUTE_PRODUCT_CMPT = "productCmpt";

    /** The product component this configurable model object is based on. */
    private IProductComponent productCmpt;

    private IProductComponentGeneration productCmptGeneration;

    public ModelObjectConfiguration() {
        super();
    }

    public ModelObjectConfiguration(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    /**
     * Sets the new product component.
     */
    public void setProductComponent(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
        this.productCmptGeneration = null;
    }

    /**
     * Sets the product component generation <b>null</b>.
     */
    public void resetProductCmptGeneration() {
        productCmptGeneration = null;
    }

    /**
     * Returns the product component that configures a policy component.
     */
    public IProductComponent getProductComponent() {
        return productCmpt;
    }

    /**
     * Returns the product component generation that configures a policy component.
     */
    public IProductComponentGeneration getProductCmptGeneration(Calendar effectiveFrom) {
        if (productCmpt == null) {
            return null;
        } else {
            return getProductCmptGenerationInternal(effectiveFrom);
        }
    }

    private IProductComponentGeneration getProductCmptGenerationInternal(Calendar effectiveFrom) {
        if (productCmptGeneration == null) {
            productCmptGeneration = loadProductCmptGeneration(effectiveFrom);
        }
        return productCmptGeneration;
    }

    /**
     * Gets the product component generation valid from the given date.
     */
    private IProductComponentGeneration loadProductCmptGeneration(Calendar effectiveFrom) {
        return productCmpt.getGenerationBase(effectiveFrom);
    }

    /**
     * Sets the new product component generation. Also changes the product component. If the
     * argument is <code>null</code> however, both product component and product component
     * generation are set to <code>null</code>.
     */
    public void setProductCmptGeneration(IProductComponentGeneration newGeneration) {
        if (newGeneration == null) {
            setProductComponent(null);
        } else {
            updateProductCmptAndGeneration(newGeneration);
        }
    }

    private void updateProductCmptAndGeneration(IProductComponentGeneration newGeneration) {
        setProductComponent(newGeneration.getProductComponent());
        productCmptGeneration = newGeneration;
    }

    /**
     * Copies the product component and product component generation from the other object.
     */
    public void copy(ModelObjectConfiguration otherObject) {
        this.productCmpt = otherObject.productCmpt;
        this.productCmptGeneration = otherObject.productCmptGeneration;
    }

    /**
     * Loads the product component this {@link ModelObjectConfiguration} manages. Does nothing, if
     * no product component qualified name can be found in the XML element.
     * 
     * @param objectEl the XML element containing the product component qualified name
     * @param productRepository the {@link IRuntimeRepository} to load the product component from
     */
    public void initFromXml(Element objectEl, IRuntimeRepository productRepository) {
        String productCmptId = objectEl.getAttribute(XML_ATTRIBUTE_PRODUCT_CMPT);
        if (!IpsStringUtils.isEmpty(productCmptId)) {
            IProductComponent existingProductCmpt = productRepository.getExistingProductComponent(productCmptId);
            setProductComponent(existingProductCmpt);
        }
    }

}
