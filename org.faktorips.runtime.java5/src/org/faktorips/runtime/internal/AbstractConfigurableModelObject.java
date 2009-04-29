/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
 * An abstract implementation of configurable policy component that leaves it open how to 
 * access the effective from date.  
 * 
 * @author Jan Ortmann
 */ 
public abstract class AbstractConfigurableModelObject extends AbstractModelObject implements IConfigurableModelObject {

    /** The product component this configurable model object is based on. */
	@XmlJavaTypeAdapter(value = ProductComponentXmlAdapter.class)
	@XmlAttribute(name="product-component.id")
    private IProductComponent productCmpt;
    
    private IProductComponentGeneration productCmptGeneration;

    public AbstractConfigurableModelObject() {
        super();
    }

    public AbstractConfigurableModelObject(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    public AbstractConfigurableModelObject(IProductComponentGeneration generation) {
        this.productCmptGeneration = generation;
        if (generation!=null) {
            productCmpt = productCmptGeneration.getProductComponent();
        }
    }

    /**
     * Sets the new product component.
     */
    public void setProductCmpt(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
        this.productCmptGeneration = null;
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponent getProductComponent() {
        return productCmpt;
    }

    public IProductComponentGeneration getProductCmptGeneration() {
        if (productCmptGeneration==null) {
            if (productCmpt==null) {
                return null;
            }
            productCmptGeneration = getProductComponentGenerationFromRepository();
        }
        return productCmptGeneration;
    }
    
    /**
     * Gets the product component generation from the repository. The default implementation
     * uses the generation's valid from date and the {@link #getEffectiveFromAsCalendar()} method to
     * identify the generation. You can change this 
     */
    protected IProductComponentGeneration getProductComponentGenerationFromRepository() {
        return productCmpt.getGenerationBase(getEffectiveFromAsCalendar());    
    }
   
    /**
     * Sets the new product component generation.
     */
    protected void setProductCmptGeneration(IProductComponentGeneration newGeneration) {
        if (newGeneration!=null) {
            setProductCmpt(newGeneration.getProductComponent());
        } else {
            setProductCmpt(null);
        }
        productCmptGeneration = newGeneration;
    }
   
    /**
     * This method should be called when effective from date has changed, so that the reference to
     * the product component generation is cleared. If this policy component contains child
     * components, this method should also clear the reference to their product component generations.
     */
    public void effectiveFromHasChanged() {
        resetProductCmptGenerationAfterEffectiveFromHasChanged();
    }
    
    /**
     * This method is called by {@link #effectiveFromHasChanged()} to set the reference to the
     * product component generation to <code>null</code> after the effective date has changed.
     * The method can be overridden if the generation is not identified by the effective date, but
     * by some other method. 
     */
    protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
        productCmptGeneration = null;
    }
    
    /**
     * Initializes the policy component with the defaults from it's product component generation.
     */
    public void initialize() {
        
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, null, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store,
            XmlCallback xmlCallback) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, "");
    }
    
    /**
     * {@inheritDoc}
     */  
    public void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store,
            XmlCallback xmlCallback,
            String currPath) {
        
        String productCmptId = objectEl.getAttribute("productCmpt");
        if (!"".equals(productCmptId)) {
            IProductComponent productCmpt = productRepository.getExistingProductComponent(productCmptId); 
            setProductCmpt(productCmpt);
        }
        if (initWithProductDefaultsBeforeReadingXmlData) {
            this.initialize();
        }
        super.initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, currPath);
    }
}
