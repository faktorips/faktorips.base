/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.jaxb.ProductComponentXmlAdapter;
import org.w3c.dom.Element;

/**
 * An abstract implementation of configurable policy component that leaves it open how to access the
 * effective from date.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractConfigurableModelObject extends AbstractModelObject implements IConfigurableModelObject {

    /** The product component this configurable model object is based on. */
    @XmlJavaTypeAdapter(value = ProductComponentXmlAdapter.class)
    @XmlAttribute(name = "product-component.id")
    private IProductComponent productCmpt;

    private IProductComponentGeneration productCmptGeneration;

    public AbstractConfigurableModelObject() {
        super();
    }

    public AbstractConfigurableModelObject(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    /**
     * Sets the new product component.
     * 
     * @deprecated use {@link #setProductComponent(IProductComponent)}
     */
    @Deprecated
    public void setProductCmpt(IProductComponent productCmpt) {
        setProductComponent(productCmpt);
    }

    /**
     * Sets the new product component.
     */
    public void setProductComponent(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
        this.productCmptGeneration = null;
    }

    public IProductComponent getProductComponent() {
        return productCmpt;
    }

    public IProductComponentGeneration getProductCmptGeneration() {
        if (productCmptGeneration == null) {
            if (productCmpt == null) {
                return null;
            }
            productCmptGeneration = getProductComponentGenerationFromRepository();
        }
        return productCmptGeneration;
    }

    /**
     * Gets the product component generation from the repository. The default implementation uses
     * the generation's valid from date and the {@link #getEffectiveFromAsCalendar()} method to
     * identify the generation. You can change this
     */
    protected IProductComponentGeneration getProductComponentGenerationFromRepository() {
        return productCmpt.getGenerationBase(getEffectiveFromAsCalendar());
    }

    /**
     * Sets the new product component generation. This method can be overridden in subclasses, e.g.
     * to implement a notification mechanism.
     */
    public void setProductCmptGeneration(IProductComponentGeneration newGeneration) {
        if (newGeneration != null) {
            setProductComponent(newGeneration.getProductComponent());
        } else {
            setProductComponent(null);
        }
        productCmptGeneration = newGeneration;
    }

    /**
     * Copies the product component and product component generation from the other object.
     */
    protected final void copyProductCmptAndGenerationInternal(AbstractConfigurableModelObject otherObject) {
        this.productCmpt = otherObject.productCmpt;
        this.productCmptGeneration = otherObject.productCmptGeneration;
    }

    /**
     * This method is called when the effective from date has changed, so that the reference to the
     * product component generation can be cleared. If this policy component contains child
     * components, this method will also clear the reference to their product component generations.
     * <p>
     * The product component generation is cleared if and only if there is a new effective from
     * date. If {@link #getEffectiveFromAsCalendar()} returns <code>null</code> the product
     * component generation is not reset, for example if this model object was removed from its
     * parent.
     * <p>
     * Resetting the product component generation is done via
     * {@link #resetProductCmptGenerationAfterEffectiveFromHasChanged()}. If you want to change the
     * behavior of resetting the product component better overwrite
     * {@link #resetProductCmptGenerationAfterEffectiveFromHasChanged()} instead of this method.
     */
    public void effectiveFromHasChanged() {
        if (getEffectiveFromAsCalendar() != null) {
            resetProductCmptGenerationAfterEffectiveFromHasChanged();
        }
    }

    /**
     * This method is called by {@link #effectiveFromHasChanged()} to set the reference to the
     * product component generation to <code>null</code> after the effective date has changed. The
     * method can be overridden if the generation is not identified by the effective date, but by
     * some other method.
     */
    protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
        productCmptGeneration = null;
    }

    /**
     * Initializes the policy component with the defaults from it's product component generation.
     */
    public void initialize() {
        // subclasses may override this method
    }

    @Override
    public void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, null, null);
    }

    @Override
    public void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store,
            XmlCallback xmlCallback) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, "");
    }

    @Override
    public void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store,
            XmlCallback xmlCallback,
            String currPath) {

        String productCmptId = objectEl.getAttribute("productCmpt");
        if (!"".equals(productCmptId)) {
            IProductComponent productCmpt = productRepository.getExistingProductComponent(productCmptId);
            setProductComponent(productCmpt);
        }
        if (initWithProductDefaultsBeforeReadingXmlData) {
            this.initialize();
        }
        super.initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback,
                currPath);
    }

}
