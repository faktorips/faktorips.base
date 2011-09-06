/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for all product components.
 */
public abstract class ProductComponent extends RuntimeObject implements IProductComponent, IClRepositoryObject {

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
            throw new NullPointerException("RuntimeRepositor was null!");
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

    public final IRuntimeRepository getRepository() {
        return repository;
    }

    public final IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
        return repository.getProductComponentGeneration(id, effectiveDate);
    }

    public IProductComponentGeneration getLatestProductComponentGeneration() {
        return getRepository().getLatestProductComponentGeneration(this);
    }

    /**
     * Initializes the generation with the data from the xml element.
     * 
     * @throws NullPointerException if cmptElement is <code>null</code>.
     */
    public final void initFromXml(Element cmptElement) {
        validTo = DateTime.parseIso(cmptElement.getAttribute("validTo"));
        Map<String, Element> propertyElements = ProductComponentXmlUtil.getPropertyElements(cmptElement);
        doInitPropertiesFromXml(propertyElements);
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

    @Override
    public String toString() {
        return id;
    }

    /**
     * 
     * @param document a document, that can be used to create XML elements.
     * @param includeGenerations <code>true</code> if the created XML element should include the
     *            data of all the product component's generations, <code>false</code> if generations
     *            should be ignored when creating the XML output.
     */
    public final Element toXml(Document document, boolean includeGenerations) {
        Element prodCmptElement = document.createElement("ProductComponent");
        writePropertiesToXml(prodCmptElement);
        writeExtensionPropertiesToXml(prodCmptElement);
        if (includeGenerations) {
            List<IProductComponentGeneration> generations = repository.getProductComponentGenerations(this);
            for (IProductComponentGeneration generation : generations) {
                ProductComponentGeneration gen = (ProductComponentGeneration)generation;
                gen.toXml(prodCmptElement);
            }
        }
        return prodCmptElement;
    }

    /**
     * Writes this product components properties into the given XML element.
     * 
     * @param element the XML element to write the properties to
     */
    protected void writePropertiesToXml(Element element) {
        // Nothing to be done base class
        /*
         * Note that this method is deliberately not declared abstract to allow calls to
         * super.writePropertiesToXML() in subclasses.
         */
    }

}
