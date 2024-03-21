/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Base class for product component generations
 * <p>
 * Deliberately implements {@link IXmlPersistenceSupport} directly. Letting
 * {@link IProductComponentGeneration} extend {@link IXmlPersistenceSupport} would have published
 * it, which is undesired.
 * 
 */
public abstract class ProductComponentGeneration extends RuntimeObject
        implements IProductComponentGeneration, IXmlPersistenceSupport {

    private static final String XML_TAG_GENERATION = "Generation";

    private static final String VALID_FROM = "validFrom";

    // the product component this generation belongs to.
    private ProductComponent productCmpt;

    private DateTime validFrom;

    // handles the formulas
    private final FormulaHandler formulaHandler;

    private final ValidationRules validationRules;

    public ProductComponentGeneration(ProductComponent productCmpt) {
        this.productCmpt = productCmpt;
        formulaHandler = new FormulaHandler(this, getRepository());
        validationRules = new ValidationRules(this);
    }

    @Override
    public IConfigurableModelObject createPolicyComponent() {
        throw new RuntimeException("Product component does not configure a policy component.");
    }

    @Override
    public final IProductComponent getProductComponent() {
        return productCmpt;
    }

    @Override
    public final IProductComponentGeneration getPreviousGeneration() {
        return getRepository().getPreviousProductComponentGeneration(this);
    }

    @Override
    public final IProductComponentGeneration getNextGeneration() {
        return getRepository().getNextProductComponentGeneration(this);
    }

    @Override
    public IRuntimeRepository getRepository() {
        return productCmpt.getRepository();
    }

    public final long getValidFromInMillisec(TimeZone zone) {
        return validFrom.toDate(zone).getTime();
    }

    @Override
    public DateTime getValidFrom() {
        return validFrom;
    }

    @Override
    public final Date getValidFrom(TimeZone zone) {
        return validFrom.toDate(zone);
    }

    /**
     * Sets the new valid from date.
     * <p>
     * <strong>Attention:</strong> Conceptually, the valid from date of the first generation must be
     * equal to the valid from date of the product component itself. Therefore, if clients call this
     * method on the first generation of a product component, to achieve data consistency, clients
     * must set the valid from date of the product component, too.
     * 
     * @throws org.faktorips.runtime.IllegalRepositoryModificationException if the repository this
     *             generation belongs to does not allow to modify its contents
     * 
     * @see ProductComponent#setValidFrom(DateTime)
     */
    public void setValidFrom(DateTime newValidFrom) {
        if (getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        if (newValidFrom == null) {
            throw new NullPointerException();
        }
        validFrom = newValidFrom;
    }

    public IFormulaEvaluator getFormulaEvaluator() {
        return formulaHandler.getFormulaEvaluator();
    }

    /**
     * Initializes the generation with the data from the xml element.
     * 
     * @throws IllegalRepositoryModificationException if the component has already been initialized
     *             and the repository prohibit changing its contents.
     * 
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    @Override
    public void initFromXml(Element genElement) {
        if (validFrom != null && getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        validFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
        Map<String, Element> propertyElements = ProductComponentXmlUtil.getPropertyElements(genElement);
        doInitPropertiesFromXml(propertyElements);
        doInitTableUsagesFromXml(propertyElements);
        doInitReferencesFromXml(ProductComponentXmlUtil.getLinkElements(genElement));
        doInitFormulaFromXml(genElement);
        doInitValidationRuleConfigsFromXml(genElement);
        initExtensionPropertiesFromXml(genElement);
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
     * Initializes the links with the data in the map.
     * 
     * @param map the map of property elements
     */
    protected void doInitReferencesFromXml(Map<String, List<Element>> map) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to super.doInitReferencesFromXml().
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
     * Initializes all formulas contained by genElement. If formula evaluation is supported, the map
     * contains the compiled expression for every formula.
     */
    protected void doInitFormulaFromXml(Element genElement) {
        formulaHandler.doInitFormulaFromXml(genElement);
    }

    /**
     * Returns <code>true</code> if the expression of the given formulaSignature not empty.
     */
    protected boolean isFormulaAvailable(String formularSignature) {
        return formulaHandler.isFormulaAvailable(formularSignature);
    }

    /**
     * Creates a map containing the validation rule configurations found in the indicated
     * generation's XML element. For each validation rule configuration the map contains an entry
     * with the rule name as a key and an {@link ValidationRuleConfiguration} instance as value.
     * 
     * @param genElement An XML element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    protected void doInitValidationRuleConfigsFromXml(Element genElement) {
        validationRules.doInitValidationRuleConfigsFromXml(genElement);
    }

    protected Element getRangeElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, ValueToXmlHelper.XML_TAG_RANGE);
    }

    protected NodeList getEnumNodeList(Element configElement) {
        Element enumElement = getEnumElement(configElement);
        return enumElement.getElementsByTagName(ValueToXmlHelper.XML_TAG_VALUE);
    }

    private Element getEnumElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, ValueToXmlHelper.XML_TAG_ENUM);
    }

    private Element getValueSetElement(Element configElement) {
        Objects.requireNonNull(configElement, "The parameter configElement must not be null.");
        return XmlUtil.findFirstElement(configElement, ValueToXmlHelper.XML_TAG_VALUE_SET)
                .orElseThrow(NullPointerException::new);
    }

    /**
     * This method for implementations of the <code>doInitReferencesFromXml</code> method to read
     * the cardinality bounds from an xml dom element. An IntegerRange object is created and added
     * to the provided cardinalityMap.
     */
    public static void addToCardinalityMap(Map<String, IntegerRange> cardinalityMap,
            String targetId,
            Element relationElement) {
        String maxStr = relationElement.getAttribute("maxCardinality");
        Integer maxCardinality = null;
        if ("*".equals(maxStr) || "n".equals(maxStr.toLowerCase())) {
            maxCardinality = Integer.valueOf(Integer.MAX_VALUE);
        } else {
            maxCardinality = Integer.valueOf(maxStr);
        }

        Integer minCardinality = Integer.valueOf(relationElement.getAttribute("minCardinality"));
        cardinalityMap.put(targetId, IntegerRange.valueOf(minCardinality, maxCardinality));
    }

    @Override
    public void setValidationRuleActivated(String ruleName, boolean active) {
        validationRules.setValidationRuleActivated(ruleName, active);
    }

    @Override
    public String toString() {
        return getProductComponent().getId() + "-" + validFrom;
    }

    /**
     * Sets the product component this generation belongs to.
     */
    protected void setProductCmpt(ProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    @Override
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public boolean isValidationRuleActivated(String ruleName) {
        return validationRules.isValidationRuleActivated(ruleName);
    }

    /**
     * Creates an XML {@link Element} that represents this product component generation's data.
     * <p>
     * Throws an {@link UnsupportedOperationException} if the support for toXml ("Generate toXml
     * Support") is not activated in the FIPS standard builder.
     * 
     * @param document a document, that can be used to create XML elements.
     */
    @Override
    public Element toXml(Document document) {
        Element genElement = document.createElement(XML_TAG_GENERATION);
        writeValidFromToXml(genElement);
        writePropertiesToXml(genElement);
        writeTableUsagesToXml(genElement);
        writeReferencesToXml(genElement);
        writeFormulaToXml(genElement);
        writeValidationRuleConfigsToXml(genElement);
        writeExtensionPropertiesToXml(genElement);
        return genElement;
    }

    private void writeValidFromToXml(Element genElement) {
        if (validFrom != null) {
            genElement.setAttribute(VALID_FROM, validFrom.toIsoFormat());
        }
    }

    /**
     * This is a utility method called by generated code. The given {@link Element} is the element
     * representing this {@link ProductComponentGeneration}.
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

    /**
     * This is a utility method called by generated code. The given {@link Element} is the element
     * representing this {@link ProductComponentGeneration}.
     * 
     * @param element the element all table usages should be added to
     */
    protected void writeReferencesToXml(Element element) {
        /*
         * Nothing to be done base class. Note that this method is deliberately not declared
         * abstract to allow calls to super.writeReferencesToXml() in subclasses.
         */
    }

    /**
     * This method is used for writing a formulas to the XML of the given {@link Element}.
     */
    protected void writeFormulaToXml(Element element) {
        formulaHandler.writeFormulaToXml(element);
    }

    protected void writeValidationRuleConfigsToXml(Element genElement) {
        validationRules.writeValidationRuleConfigsToXml(genElement);
    }

    /**
     * Subclasses override this method to write their properties into the given XML element.
     * <p>
     * The standard implementation throws an {@link UnsupportedOperationException} if the support
     * for toXml ("Generate toXml Support") is not activated in the FIPS standard builder. Generated
     * classes override but do <em>NOT</em> call super.
     * 
     * @param generationElement the XML element to write the properties to
     */
    protected void writePropertiesToXml(Element generationElement) {
        throw new UnsupportedOperationException(
                "The method toXml() is currently not supported, as the required methods were not generated. To activate toXml() please check your FIPS Builder properties and make sure \"Generated toXml Support\" is set to true.");
    }
}
