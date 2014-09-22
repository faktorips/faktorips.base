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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for all product components.
 * <p>
 * Deliberately implements {@link IXmlPersistenceSupport} directly. Letting
 * {@link IProductComponent} extend {@link IXmlPersistenceSupport} would have published it, which is
 * undesired.
 */
public abstract class ProductComponent extends RuntimeObject implements IProductComponent, IXmlPersistenceSupport {

    private static final String IS_NULL = "isNull";

    private static final String VALID_TO = "validTo";

    // the component's id that identifies it in the repository
    private String id;

    // The repository the component uses to resolve references to other components.
    private transient IRuntimeRepository repository;

    // the component's kindId
    private String productKindId;

    // the component's versionId
    private String versionId;

    // the date at which this product component expires. Set to null indicates no
    // limitation
    private DateTime validTo;

    /**
     * Creates a new product component with the indicate id, kind id and version id.
     * 
     * @param repository The component registry the component uses to resolve references to other
     *            components.
     * @param id The component's runtime id.
     * @param productKindId The component's kind id
     * @param versionId The component's version id
     * 
     * @throws NullPointerException if repository, id, productKindId, or versionId is
     *             <code>null</code>.
     */
    public ProductComponent(IRuntimeRepository repository, String id, String productKindId, String versionId) {
        if (repository == null) {
            throw new NullPointerException("RuntimeRepository was null!");
        }
        if (id == null) {
            throw new NullPointerException("Id was null!");
        }
        if (productKindId == null) {
            throw new NullPointerException("ProductKindId was null");
        }
        if (versionId == null) {
            throw new NullPointerException("VersionId was null");
        }
        this.repository = repository;
        this.id = id;
        this.productKindId = productKindId;
        this.versionId = versionId;
    }

    public String getKindId() {
        return productKindId;
    }

    public String getVersionId() {
        return versionId;
    }

    public final String getId() {
        return id;
    }

    public DateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(DateTime validTo) {
        this.validTo = validTo;
    }

    public IRuntimeRepository getRepository() {
        return repository;
    }

    public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
        return getRepository().getProductComponentGeneration(id, effectiveDate);
    }

    public IProductComponentGeneration getLatestProductComponentGeneration() {
        return getRepository().getLatestProductComponentGeneration(this);
    }

    /**
     * Initializes the generation with the data from the xml element.
     * 
     * @throws NullPointerException if cmptElement is <code>null</code>.
     */
    public void initFromXml(Element cmptElement) {
        Element validToNode = (Element)cmptElement.getElementsByTagName(VALID_TO).item(0);
        if (validToNode == null || Boolean.parseBoolean(validToNode.getAttribute(IS_NULL))) {
            validTo = null;
        } else {
            validTo = DateTime.parseIso(validToNode.getTextContent());
        }
        Map<String, Element> propertyElements = ProductComponentXmlUtil.getPropertyElements(cmptElement);
        doInitPropertiesFromXml(propertyElements);
        doInitTableUsagesFromXml(propertyElements);
        doInitReferencesFromXml(ProductComponentXmlUtil.getLinkElements(cmptElement));
        initExtensionPropertiesFromXml(cmptElement);
    }

    /**
     * Initializes the properties with the data in the map.
     * 
     * @param map the map of property elements
     */
    protected void doInitPropertiesFromXml(Map<String, Element> map) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to super.doInitPropertiesFromXml().
    }

    /**
     * Initializes the table content usages with the data in the map. The map contains the table
     * structure usage roles as key and the qualified table content name as value.
     * 
     * @param map the map of property elements
     */
    protected void doInitTableUsagesFromXml(Map<String, Element> map) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to super.doInitTableUsagesFromXml().
    }

    /**
     * 
     * @param linkElements the XML elements used to initialize {@link ProductComponentLink}
     *            instances.
     */
    protected void doInitReferencesFromXml(Map<String, List<Element>> linkElements) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to super.doInitReferencesFromXml().
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Creates an XML {@link Element} that represents this product component's data.
     * <p/>
     * Throws an {@link UnsupportedOperationException} if the support for toXml
     * ("Generate toXml Support") is not activated in the FIPS standard builder.
     * 
     * @param document a document, that can be used to create XML elements.
     */
    public Element toXml(Document document) {
        return toXml(document, true);
    }

    /**
     * Creates an XML {@link Element} that represents this product component's data.
     * <p/>
     * Throws an {@link UnsupportedOperationException} if the support for toXml
     * ("Generate toXml Support") is not activated in the FIPS standard builder.
     * 
     * @param document a document, that can be used to create XML elements.
     * @param includeGenerations <code>true</code> if the created XML element should include the
     *            data of all the product component's generations, <code>false</code> if generations
     *            should be ignored when creating the XML output.
     */
    public Element toXml(Document document, boolean includeGenerations) {
        Element prodCmptElement = document.createElement("ProductComponent");
        writeValidToToXml(prodCmptElement);
        writePropertiesToXml(prodCmptElement);
        writeTableUsagesToXml(prodCmptElement);
        writeReferencesToXml(prodCmptElement);
        writeExtensionPropertiesToXml(prodCmptElement);
        if (includeGenerations) {
            List<IProductComponentGeneration> generations = getRepository().getProductComponentGenerations(this);
            for (IProductComponentGeneration generation : generations) {
                ProductComponentGeneration gen = (ProductComponentGeneration)generation;
                prodCmptElement.appendChild(gen.toXml(document));
            }
        }
        return prodCmptElement;
    }

    private void writeValidToToXml(Element prodCmptElement) {
        Element validToElement = prodCmptElement.getOwnerDocument().createElement(VALID_TO);
        if (validTo != null) {
            validToElement.setAttribute(IS_NULL, Boolean.FALSE.toString());
            validToElement.setTextContent(validTo.toIsoFormat());
            prodCmptElement.appendChild(validToElement);
        } else {
            validToElement.setAttribute(IS_NULL, Boolean.TRUE.toString());
        }
    }

    /**
     * Subclasses override this method to write their properties into the given XML element.
     * <p/>
     * The standard implementation throws an {@link UnsupportedOperationException} if the support
     * for toXml ("Generate toXml Support") is not activated in the FIPS standard builder. Generated
     * classes override but do <em>NOT</em> call super.
     * 
     * @param element the XML element to write the properties to
     */
    protected void writePropertiesToXml(Element element) {
        throw new UnsupportedOperationException(
                "The method toXml() is currently not supported, as the required methods were not generated. To activate toXml() please check your FIPS Builder properties and make sure \"Generated toXml Support\" is set to true.");
    }

    /**
     * This is a utility method called by generated code. The given {@link Element} is the element
     * representing this {@link ProductComponent}.
     * 
     * @param element the element all table usages should be added to
     */
    protected void writeTableUsagesToXml(Element element) {
        /*
         * Nothing to be done base class. Note that this method is deliberately not declared
         * abstract to allow calls to super.writeTableUsagesToXml() in subclasses.
         */
    }

    /**
     * This method is used for writing a table usage to the XML of the given {@link Element}.
     * 
     * @param element the element where the table usage will be added to
     * @param structureUsage the value for the structureUsage XML attribute
     * @param tableContentName the name of the used table content
     */
    protected void writeTableUsageToXml(Element element, String structureUsage, String tableContentName) {
        ValueToXmlHelper.addTableUsageToElement(element, structureUsage, tableContentName);
    }

    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
        return null;
    }

    public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
        return new ArrayList<IProductComponentLink<? extends IProductComponent>>();
    }

    /**
     * This is a utility method called by generated code. The given {@link Element} is the element
     * representing this {@link ProductComponentGeneration}.
     * 
     * @param element the element all table usages should be added to
     * @since 3.8
     */
    protected void writeReferencesToXml(Element element) {

        /*
         * Nothing to be done base class. Note that this method is deliberately not declaredtoXml
         * abstract to allow calls to super.writeReferencesToXml() in subclasses.
         */
    }

}
