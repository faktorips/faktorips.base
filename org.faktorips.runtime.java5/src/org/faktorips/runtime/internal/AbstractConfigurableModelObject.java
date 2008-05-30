/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.w3c.dom.Element;

/**
 * An abstract implementation of configurable policy component that leaves it open how to 
 * access the effective from date.  
 * 
 * @author Jan Ortmann
 */ 
public abstract class AbstractConfigurableModelObject extends AbstractModelObject implements IConfigurableModelObject {

    // The product component this policy component is based on.
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
     */
    public void setProductCmpt(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
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
            productCmptGeneration = productCmpt.getGenerationBase(getEffectiveFromAsCalendar());
        }
        return productCmptGeneration;
    }
   
    /**
     * This method should be called when effective from date has changed, so that the reference to
     * the product component generation is cleared. If this policy component contains child
     * components, this method should also clear the reference to their product component generations.
     */
    public void effectiveFromHasChanged() {
        productCmptGeneration = null;
    }
    
    /**
     * Initializes the policy component with the defaults from it's product component generation.
     */
    public void initialize() {
        
    }

    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl 
     *      Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData
     *      <code>true</code> if the policy component should be initialized with the product defaults.
     * @param productRepository 
     *      The repository that contains the product components.
     * @param store
     *      The store where unresolved references are stored in (so that they can be resolved after all
     *      objects have been intialized from xml).
     */
    public void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store) {
        
        String productCmptId = objectEl.getAttribute("productCmpt");
        if (!"".equals(productCmptId)) {
            IProductComponent productCmpt = productRepository.getExistingProductComponent(productCmptId); 
            setProductCmpt(productCmpt);
        }
        if (initWithProductDefaultsBeforeReadingXmlData) {
            this.initialize();
        }
        super.initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store);
    }
}
