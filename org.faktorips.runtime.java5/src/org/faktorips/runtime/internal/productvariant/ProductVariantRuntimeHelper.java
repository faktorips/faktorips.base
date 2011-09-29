/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.productvariant;

import java.lang.reflect.Constructor;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class for loading product variants or varied product components respectively.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ProductVariantRuntimeHelper {

    protected static final String ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT = "variedProductCmpt";
    protected static final String ATTRIBUTE_NAME_RUNTIME_ID = "runtimeID";

    /**
     * The given product component is initialized with the data of the original product component
     * and the variations defined by the variant.
     * 
     * @param originalProductComponent the original product component that provides default values
     * @param productComponentToBeInitialized the yet un-initialized product component.
     * @param prodCmptElement the XML element containing product component's variation data.
     */
    public void initProductComponentVariation(ProductComponent originalProductComponent,
            ProductComponent productComponentToBeInitialized,
            Element prodCmptElement) {
        loadAndVary(originalProductComponent, prodCmptElement, productComponentToBeInitialized);
    }

    /**
     * The given generation is initialized with the data of the original product component
     * generation and the variations defined by the variant.
     * 
     * @param runtimeRepository the runtime repository calling this method. It will be used to load
     *            the original product component.
     * @param tocEntry the {@link GenerationTocEntry} for the requested
     *            {@link ProductComponentGeneration}
     * @param genElement the XML element containing the variation of a product component generation.
     */

    public IProductComponentGeneration initProductComponentGenerationVariation(IRuntimeRepository runtimeRepository,
            GenerationTocEntry tocEntry,
            Element genElement) {
        GregorianCalendar validFrom = tocEntry.getValidFrom().toGregorianCalendar(TimeZone.getDefault());
        ProductComponent originalCmpt = getOriginalProdCmpt(runtimeRepository, genElement);
        ProductComponent variedCmpt = (ProductComponent)runtimeRepository.getProductComponent(tocEntry.getParent()
                .getIpsObjectId());
        ProductComponentGeneration originalGeneration = (ProductComponentGeneration)originalCmpt
                .getGenerationBase(validFrom);
        ProductComponentGeneration variedProductCmptGeneration = createNewInstance(originalGeneration, variedCmpt);
        loadAndVary(originalGeneration, genElement, variedProductCmptGeneration);
        return variedProductCmptGeneration;
    }

    /**
     * Creates a new instance of the given {@link ProductComponentGeneration}'s class. Uses the
     * given {@link ProductComponent} as new parent.
     * 
     * @param parentProductCmpt the new parent component.
     * @param generationTemplate the generation a new instance is created of
     * @return a new {@link ProductComponentGeneration} instance of the same class as this class
     */
    protected ProductComponentGeneration createNewInstance(ProductComponentGeneration generationTemplate,
            ProductComponent parentProductCmpt) {
        try {
            Constructor<? extends ProductComponentGeneration> constructor = generationTemplate.getClass()
                    .getConstructor(parentProductCmpt.getClass());
            return constructor.newInstance(parentProductCmpt);
        } catch (Exception e) {
            throw new RuntimeException("Could not create a new instance of class \"" + getClass().getName()
                    + "\" (ProductComponent: \"" + parentProductCmpt.getId() + "\" validfrom "
                    + generationTemplate.getValidFrom(TimeZone.getDefault()) + ")", e);
        }
    }

    protected void loadAndVary(IXmlPersistenceSupport originalObject,
            Element variationXML,
            IClRepositoryObject objectToInitialize) {
        Document document = (Document)variationXML.getOwnerDocument().cloneNode(false);
        Element originalElement = originalObject.toXml(document);
        objectToInitialize.initFromXml(originalElement);
        objectToInitialize.initFromXml(variationXML);
    }

    public ProductComponent getOriginalProdCmpt(IRuntimeRepository runtimeRepository, Element element) {
        String originalRuntimeID = element.getAttribute(ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT);
        ProductComponent originalCmpt = (ProductComponent)runtimeRepository.getProductComponent(originalRuntimeID);
        return originalCmpt;
    }

    public boolean isProductVariantXML(Element productVariantElement) {
        return productVariantElement.hasAttribute(ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT);
    }

}
