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

import java.util.GregorianCalendar;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class for loading product variants or varied product components respectively.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ProductVariantRuntimeHelper {

    protected static final String ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT = "variedProductCmpt";
    protected static final String ATTRIBUTE_NAME_RUNTIME_ID = "runtimeID";

    /**
     * Determines whether the given XML element is a product variant. If it isn't the given product
     * component is initialized normally (by calling its initFromXml() method). If however the XML
     * element is a product variant XML, the given product component is initialized with the data of
     * the original product component and the variations defined by the variant.
     * 
     * @param runtimeRepository the runtime repository calling this method. It will be used to load
     *            the original product component.
     * @param productCmpt the yet un-initialized product component.
     * @param prodCmptElement the XML element that either contains normal product component XML data
     *            or product variant XML data.
     */
    public void initProductComponent(IRuntimeRepository runtimeRepository,
            ProductComponent productCmpt,
            Element prodCmptElement) {
        if (isProductVariantXML(prodCmptElement)) {
            loadAndVaryProductComponent(runtimeRepository, prodCmptElement, productCmpt);
        } else {
            productCmpt.initFromXml(prodCmptElement);
        }
    }

    protected void loadAndVaryProductComponent(IRuntimeRepository runtimeRepository,
            Element variationElement,
            ProductComponent productCmptToBeLoaded) {
        ProductComponent originalCmpt = getOriginalProdCmpt(runtimeRepository, variationElement);
        loadAndVary(originalCmpt, variationElement, productCmptToBeLoaded);
    }

    /**
     * Determines whether the given XML element is a variation of a product component generation. If
     * it isn't the given generation is initialized normally (by calling its initFromXml() method).
     * If however the XML element is a product variant XML, the given generation is initialized with
     * the data of the original product component generation and the variations defined by the
     * variant.
     * 
     * @param runtimeRepository the runtime repository calling this method. It will be used to load
     *            the original product component.
     * @param generationValidFrom the valid from date of the requested product component generation
     * @param genElement the XML element that either contains normal product component generation
     *            XML data or a variation's XML data.
     * @param productCmptGen the yet un-initialized product component generation
     */
    public void initProductComponentGeneration(IRuntimeRepository runtimeRepository,
            GregorianCalendar generationValidFrom,
            Element genElement,
            ProductComponentGeneration productCmptGen) {
        if (isProductVariantXML(genElement)) {
            loadAndVaryProductComponentGeneration(runtimeRepository, generationValidFrom, genElement, productCmptGen);
        } else {
            productCmptGen.initFromXml(genElement);
        }
    }

    protected void loadAndVaryProductComponentGeneration(IRuntimeRepository runtimeRepository,
            GregorianCalendar generationValidFrom,
            Element generationVariationElement,
            ProductComponentGeneration productCmptGenToBeLoaded) {
        ProductComponent originalCmpt = getOriginalProdCmpt(runtimeRepository, generationVariationElement);
        ProductComponentGeneration originalGeneration = (ProductComponentGeneration)originalCmpt
                .getGenerationBase(generationValidFrom);
        loadAndVary(originalGeneration, generationVariationElement, productCmptGenToBeLoaded);
    }

    protected void loadAndVary(IXmlPersistenceSupport originalObject,
            Element variationXML,
            IClRepositoryObject objectToInitialize) {
        Document document = (Document)variationXML.getOwnerDocument().cloneNode(false);
        Element originalElement = originalObject.toXml(document);
        objectToInitialize.initFromXml(originalElement);
        objectToInitialize.initFromXml(variationXML);
    }

    protected ProductComponent getOriginalProdCmpt(IRuntimeRepository runtimeRepository, Element element) {
        String originalRuntimeID = element.getAttribute(ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT);
        ProductComponent originalCmpt = (ProductComponent)runtimeRepository.getProductComponent(originalRuntimeID);
        return originalCmpt;
    }

    protected boolean isProductVariantXML(Element productVariantElement) {
        return productVariantElement.hasAttribute(ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT);
    }

    /**
     * Searches the product variant XML for a variant component with the given runtime id. This
     * method does not return <code>null</code>. Instead a {@link RuntimeException} is thrown in
     * case no variant component XML could be found.
     * 
     * @param productVariantElement the document element of the product variant XML file
     * @param variationRuntimeID the searched runtime id. This is the runtime id a product component
     *            was requested for.
     * @return the variant component XML element with the given runtime id.
     */
    protected Element findVariationElement(Element productVariantElement, String variationRuntimeID) {
        Node rootCmpt = productVariantElement.getFirstChild();
        if ("ProductVariantCmpt".equals(rootCmpt.getNodeName())) {
            Element variationElement = recursiveFindProductComponentElement(rootCmpt, variationRuntimeID);
            if (variationElement == null) {
                throw new RuntimeException("No variant component with runtime id \"" + variationRuntimeID
                        + "\" could be found in XML.");
            }
            return variationElement;
        } else {
            throw new RuntimeException("No variant components defined in XML.");
        }
    }

    private Element recursiveFindProductComponentElement(Node variantCmptNode, String variationRuntimeID) {
        if (!(variantCmptNode instanceof Element)) {
            return null;
        }
        Element variantCmptElement = (Element)variantCmptNode;
        if (variationRuntimeID.equals(variantCmptElement.getAttribute(ATTRIBUTE_NAME_RUNTIME_ID))) {
            return variantCmptElement;
        } else {
            NodeList children = variantCmptElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                Element foundElement = recursiveFindProductComponentElement(child, variationRuntimeID);
                if (foundElement != null) {
                    return foundElement;
                }
            }
            return null;
        }
    }
}
