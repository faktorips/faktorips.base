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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.formula.AbstractFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for product component generations
 * 
 */
public abstract class ProductComponentGeneration extends RuntimeObject implements IProductComponentGeneration,
        IXmlPersistenceSupport {

    // the product component this generation belongs to.
    private ProductComponent productCmpt;

    private DateTime validFrom;

    private IFormulaEvaluator formulaEvaluator;

    private Map<String, ValidationRuleConfiguration> nameToValidationRuleConfigMap;

    public ProductComponentGeneration(ProductComponent productCmpt) {
        this.productCmpt = productCmpt;
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
        return formulaEvaluator;
    }

    /**
     * Initializes the generation with the data from the xml element.
     * 
     * @throws IllegalRepositoryModificationException if the component has already been initialized
     *             and the repository prohibit changing its contents.
     * 
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    public final void initFromXml(Element genElement) {
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
     * 
     */
    protected void doInitFormulaFromXml(Element genElement) {
        if (getRepository() != null) {
            IFormulaEvaluatorFactory factory = getRepository().getFormulaEvaluatorFactory();
            if (factory != null) {
                Map<String, String> expressions = new LinkedHashMap<String, String>();
                NodeList formulas = genElement.getElementsByTagName("Formula");
                for (int i = 0; i < formulas.getLength(); i++) {
                    Element aFormula = (Element)formulas.item(i);
                    String name = aFormula.getAttribute("formulaSignature");
                    NodeList nodeList = aFormula
                            .getElementsByTagName(AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
                    if (nodeList.getLength() == 1) {
                        Element expression = (Element)nodeList.item(0);
                        String formulaExpression = expression.getTextContent();
                        expressions.put(name, formulaExpression);
                    } else {
                        throw new RuntimeException("Expression for Formula: " + name + " not found");
                    }
                }
                formulaEvaluator = factory.createFormulaEvaluator(this, expressions);
            }
        }
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
        Map<String, ValidationRuleConfiguration> configMap = new HashMap<String, ValidationRuleConfiguration>();
        NodeList nl = genElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && "ValidationRuleConfig".equals(node.getNodeName())) {
                Element childElement = (Element)nl.item(i);
                ValidationRuleConfiguration config = new ValidationRuleConfiguration(childElement);
                configMap.put(config.getRuleName(), config);
            }
        }
        nameToValidationRuleConfigMap = configMap;
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
        return XmlUtil.getFirstElement(valueSetElement, "Range");
    }

    protected NodeList getEnumNodeList(Element configElement) {
        Element enumElement = getEnumElement(configElement);
        NodeList nl = enumElement.getElementsByTagName("Value");
        return nl;
    }

    private Element getEnumElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, "Enum");
    }

    private Element getValueSetElement(Element configElement) {
        if (configElement == null) {
            throw new NullPointerException();
        }
        Element valueSetElement = XmlUtil.getFirstElement(configElement, "ValueSet");
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

    public Element toXml(Document document) {
        Element genElement = document.createElement("ProductComponentGeneration");
        writePropertiesToXml(genElement);
        writeTableUsagesToXml(genElement);
        writeReferencesToXml(genElement);
        writeFormulaToXml(genElement);
        writeValidationRuleConfigsToXml(genElement);
        /*
         * SW 09.2011: Extension properties are never read from XML. Generations, it seems, will
         * never be extended that way. Use code regardlessly.
         */
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

    protected void writeTableUsageToXml(Element element, String structureUsage, String tableContentName) {
        Element tableContentElement = element.getOwnerDocument().createElement("TableContentUsage");
        tableContentElement.setAttribute("structureUsage", structureUsage);
        ValueToXmlHelper.addValueToElement(tableContentName, tableContentElement, "TableContentName");
        element.appendChild(tableContentElement);
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

    protected void writeFormulaToXml(Element element) {
        if (formulaEvaluator == null) {
            return;
        }
        for (Entry<String, String> expressionEntry : formulaEvaluator.getNameToExpressionMap().entrySet()) {
            Element formula = element.getOwnerDocument().createElement("Formula");
            formula.setAttribute("formulaSignature", expressionEntry.getKey());
            ValueToXmlHelper.addCDataValueToElement(expressionEntry.getValue(), formula,
                    AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
            element.appendChild(formula);
        }
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
     * Adds all product component's properties to the given XML {@link Element}.
     * 
     * @param generationElement the {@link Element} representing the
     *            {@link ProductComponentGeneration}.
     */
    protected void writePropertiesToXml(Element generationElement) {
        /*
         * Nothing to be done base class. Note that this method is deliberately not declared
         * abstract to allow calls to super.writePropertiesToXML() in subclasses.
         */
    }
}
