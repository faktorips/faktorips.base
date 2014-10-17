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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for product component generations
 * <p>
 * Deliberately implements {@link IXmlPersistenceSupport} directly. Letting
 * {@link IProductComponentGeneration} extend {@link IXmlPersistenceSupport} would have published
 * it, which is undesired.
 * 
 */
public abstract class ProductComponentGeneration extends RuntimeObject implements IProductComponentGeneration,
IXmlPersistenceSupport {

    // the product component this generation belongs to.
    private ProductComponent productCmpt;

    private DateTime validFrom;

    // handles the formulas
    private final FormulaHandler formulaHandler;

    private Map<String, ValidationRuleConfiguration> nameToValidationRuleConfigMap = new HashMap<String, ValidationRuleConfiguration>();

    public ProductComponentGeneration(ProductComponent productCmpt) {
        this.productCmpt = productCmpt;
        this.formulaHandler = new FormulaHandler(getRepository());
    }

    /**
     * {@inheritDoc}
     */
    public IConfigurableModelObject createPolicyComponent() {
        throw new RuntimeException("Product component does not configure a policy component.");
    }

    public final IProductComponent getProductComponent() {
        return productCmpt;
    }

    public final IProductComponentGeneration getPreviousGeneration() {
        return getRepository().getPreviousProductComponentGeneration(this);
    }

    public final IProductComponentGeneration getNextGeneration() {
        return getRepository().getNextProductComponentGeneration(this);
    }

    public IRuntimeRepository getRepository() {
        return productCmpt.getRepository();
    }

    public final long getValidFromInMillisec(TimeZone zone) {
        return validFrom.toDate(zone).getTime();
    }

    public final Date getValidFrom(TimeZone zone) {
        return validFrom.toDate(zone);
    }

    /**
     * Sets the new valid from date.
     * 
     * @throws org.faktorips.runtime.IllegalRepositoryModificationException if the repository this
     *             generation belongs to does not allow to modify its contents. The method is
     *             provided to ease the development of test cases.
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
     * <p>
     * IPSPV-199 : removed the cleaning of <code>nameToValidationRuleConfigMap</code> because if the
     * method <code>initFromXML</code> is called twice, the product variant would have no validation
     * rules when cleaning is used.
     * 
     * @param genElement An XML element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    protected void doInitValidationRuleConfigsFromXml(Element genElement) {
        NodeList nl = genElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && "ValidationRuleConfig".equals(node.getNodeName())) {
                Element childElement = (Element)nl.item(i);
                ValidationRuleConfiguration config = new ValidationRuleConfiguration(childElement);
                nameToValidationRuleConfigMap.put(config.getRuleName(), config);
            }
        }
    }

    /**
     * Returning the map from the names of validation rules to the validation rule configuration.
     * Not intended to use by client, only for test cases.
     */
    /* private */Map<String, ValidationRuleConfiguration> getNameToValidationRuleConfigMap() {
        return nameToValidationRuleConfigMap;
    }

    protected Element getRangeElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, ValueToXmlHelper.XML_TAG_RANGE);
    }

    protected NodeList getEnumNodeList(Element configElement) {
        Element enumElement = getEnumElement(configElement);
        NodeList nl = enumElement.getElementsByTagName(ValueToXmlHelper.XML_TAG_VALUE);
        return nl;
    }

    private Element getEnumElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, ValueToXmlHelper.XML_TAG_ENUM);
    }

    private Element getValueSetElement(Element configElement) {
        if (configElement == null) {
            throw new NullPointerException();
        }
        Element valueSetElement = XmlUtil.getFirstElement(configElement, ValueToXmlHelper.XML_TAG_VALUE_SET);
        if (valueSetElement == null) {
            throw new NullPointerException();
        }
        return valueSetElement;
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
            maxCardinality = new Integer(Integer.MAX_VALUE);
        } else {
            maxCardinality = Integer.valueOf(maxStr);
        }

        Integer minCardinality = Integer.valueOf(relationElement.getAttribute("minCardinality"));
        cardinalityMap.put(targetId, new IntegerRange(minCardinality, maxCardinality));
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

    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
        throw new RuntimeException("Not implemented yet.");
    }

    public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValidationRuleActivated(String ruleName) {
        ValidationRuleConfiguration ruleConfig = nameToValidationRuleConfigMap.get(ruleName);
        return ruleConfig != null && ruleConfig.isActive();
    }

    /**
     * Creates an XML {@link Element} that represents this product component generation's data.
     * <p/>
     * Throws an {@link UnsupportedOperationException} if the support for toXml
     * ("Generate toXml Support") is not activated in the FIPS standard builder.
     * 
     * @param document a document, that can be used to create XML elements.
     */
    public Element toXml(Document document) {
        Element genElement = document.createElement("ProductComponentGeneration");
        writePropertiesToXml(genElement);
        writeTableUsagesToXml(genElement);
        writeReferencesToXml(genElement);
        writeFormulaToXml(genElement);
        writeValidationRuleConfigsToXml(genElement);
        writeExtensionPropertiesToXml(genElement);
        return genElement;
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
         * Nothing to be done base class. Note that this method is deliberately not declaredtoXml
         * abstract to allow calls to super.writeReferencesToXml() in subclasses.
         */
    }

    protected void writeReferenceToXml(Element element, IProductComponentLink<? extends IProductComponent> link) {
        Element linkElement = element.getOwnerDocument().createElement("Link");
        linkElement.setAttribute("association", link.getAssociationName());
        linkElement.setAttribute("target", link.getTarget().getId());
        linkElement.setAttribute("minCardinality", Integer.toString(link.getCardinality().getLowerBound()));
        linkElement.setAttribute("maxCardinality", Integer.toString(link.getCardinality().getUpperBound()));
        linkElement.setAttribute("defaultCardinality", Integer.toString(link.getCardinality().getDefaultCardinality()));
        element.appendChild(linkElement);
    }

    /**
     * This method is used for writing a formulas to the XML of the given {@link Element}.
     */
    protected void writeFormulaToXml(Element element) {
        formulaHandler.writeFormulaToXml(element);
    }

    protected void writeValidationRuleConfigsToXml(Element genElement) {
        for (String vRuleName : nameToValidationRuleConfigMap.keySet()) {
            Element vRuleElement = genElement.getOwnerDocument().createElement("ValidationRuleConfig");
            ValidationRuleConfiguration vRule = nameToValidationRuleConfigMap.get(vRuleName);
            vRuleElement.setAttribute("ruleName", vRuleName);
            vRuleElement.setAttribute("active", Boolean.toString(vRule.isActive()));
            genElement.appendChild(vRuleElement);
        }
    }

    /**
     * Subclasses override this method to write their properties into the given XML element.
     * <p/>
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
