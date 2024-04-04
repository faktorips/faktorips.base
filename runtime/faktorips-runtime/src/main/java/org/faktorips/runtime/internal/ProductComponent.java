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

import static java.util.Comparator.comparing;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_ATTRIBUTE_STRUCTURE_USAGE;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_CONFIGURED_DEFAULT;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_CONFIGURED_VALUE_SET;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_TABLE_CONTENT_USAGE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.internal.toc.TocEntry;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.values.InternationalString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for all product components.
 * <p>
 * Deliberately implements {@link IXmlPersistenceSupport} directly. Letting
 * {@link IProductComponent} extend {@link IXmlPersistenceSupport} would have published it, which is
 * undesired.
 */
public abstract class ProductComponent extends RuntimeObject implements IProductComponent, IXmlPersistenceSupport {

    public static final String PROPERTY_IMPLEMENTATION_CLASS = TocEntry.PROPERTY_IMPLEMENTATION_CLASS;

    protected static final String ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT = "variedProductCmpt";

    private static final String XML_ELEMENT_GENERATION = "Generation";

    private static final String XML_ATTRIBUTE_VALID_FROM = "validFrom";
    private static final String XML_ATTRIBUTE_IS_NULL = "isNull";
    private static final String XML_ATTRIBUTE_VALID_TO = "validTo";

    private static final Comparator<Element> BY_ATTRIBUTE_NAME = comparing(
            e -> e.getAttribute(XML_ATTRIBUTE_ATTRIBUTE));
    private static final Comparator<Element> BY_TABLE_STRUCTURE_USAGE_NAME = comparing(
            e -> e.getAttribute(XML_ATTRIBUTE_STRUCTURE_USAGE));
    private static final Comparator<Element> VALUE_SET_BEFORE_DEFAULT = comparing(Element::getNodeName).reversed();

    /**
     * The component's id that identifies it in the repository
     */
    private String id;

    /**
     * The repository the component uses to resolve references to other components.
     */
    private transient IRuntimeRepository repository;

    /**
     * The component's kindId
     */
    private final String productKindId;

    /**
     * The component's versionId
     */
    private final String versionId;

    /**
     * The runtimeId of the {@link ProductComponent} this variant is based on. {@code null} if this
     * is not a variant
     */
    private String variedBase;

    /**
     * The date from which this product component is valid.
     */
    private DateTime validFrom;

    /**
     * The date at which this product component expires. Set to null indicates no limitation
     */
    private DateTime validTo;

    /**
     * Handles the formulas
     */
    private final FormulaHandler formulaHandler;

    /**
     * The description for this product component in all configured languages.
     */
    private InternationalString description;

    private final ValidationRules validationRules;

    private String qualifiedName;

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
        formulaHandler = new FormulaHandler(this, this.repository);
        validationRules = new ValidationRules(this);
    }

    @Override
    public String getKindId() {
        return productKindId;
    }

    @Override
    public String getVersionId() {
        return versionId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public boolean isVariant() {
        return variedBase != null;
    }

    @Override
    public IProductComponent getVariedBase() {
        if (isVariant()) {
            return repository.getProductComponent(variedBase);
        }

        return null;
    }

    /**
     * Sets the varied base product, marking this product as its variant.
     *
     * @see #isVariant()
     * @see #getVariedBase()
     * @throws IllegalRepositoryModificationException if the {@link #getRepository() repository}
     *             this product component belongs to is not {@link IRuntimeRepository#isModifiable()
     *             modifiable}.
     */
    public void setVariedBase(ProductComponent variedBase) {
        if (getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        this.variedBase = variedBase == null ? null : variedBase.id;
    }

    @Override
    public DateTime getValidFrom() {
        return validFrom;
    }

    @Override
    public Date getValidFrom(TimeZone zone) {
        return validFrom.toDate(zone);
    }

    /**
     * Sets the new valid from date.
     * <p>
     * <strong>Attention:</strong> Conceptually, the valid from date of the first generation must be
     * equal to the valid from date of the product component itself. Therefore, if clients call this
     * method, then to achieve data consistency clients must set the valid from date of the first
     * generation, too.
     *
     * @throws org.faktorips.runtime.IllegalRepositoryModificationException if the repository this
     *             product component belongs to does not allow to modify its contents
     *
     * @see ProductComponentGeneration#setValidFrom(DateTime)
     */
    public void setValidFrom(DateTime validfrom) {
        if (getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        if (validfrom == null) {
            throw new NullPointerException();
        }
        validFrom = validfrom;
    }

    @Override
    public DateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(DateTime validTo) {
        if (getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        this.validTo = validTo;
    }

    @Override
    public IRuntimeRepository getRepository() {
        return repository;
    }

    @Override
    public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
        if (!isChangingOverTime()) {
            throw new UnsupportedOperationException();
        }
        return getRepository().getProductComponentGeneration(id, effectiveDate);
    }

    @Override
    public IProductComponentGeneration getLatestProductComponentGeneration() {
        if (!isChangingOverTime()) {
            throw new UnsupportedOperationException();
        }
        return getRepository().getLatestProductComponentGeneration(this);
    }

    public IFormulaEvaluator getFormulaEvaluator() {
        return formulaHandler.getFormulaEvaluator();
    }

    @Override
    public String getDescription(Locale locale) {
        String string = description.get(locale);
        if (string == null) {
            return IpsStringUtils.EMPTY;
        } else {
            return string;
        }
    }

    /**
     * Initializes the generation with the data from the xml element.
     *
     * @throws NullPointerException if cmptElement is <code>null</code>.
     */
    @Override
    public void initFromXml(Element cmptElement) {
        String validFromValue = cmptElement.getAttribute(XML_ATTRIBUTE_VALID_FROM);
        validFrom = DateTime.parseIso(validFromValue);
        Element validToNode = (Element)cmptElement.getElementsByTagName(XML_ATTRIBUTE_VALID_TO).item(0);
        if (validToNode == null || Boolean.parseBoolean(validToNode.getAttribute(XML_ATTRIBUTE_IS_NULL))) {
            validTo = null;
        } else {
            validTo = DateTime.parseIso(validToNode.getTextContent());
        }
        Map<String, Element> propertyElements = ProductComponentXmlUtil.getPropertyElements(cmptElement);
        doInitPropertiesFromXml(propertyElements);
        doInitTableUsagesFromXml(propertyElements);
        doInitFormulaFromXml(cmptElement);
        doInitReferencesFromXml(ProductComponentXmlUtil.getLinkElements(cmptElement));
        doInitValidationRuleConfigsFromXml(cmptElement);
        initExtensionPropertiesFromXml(cmptElement);
        description = DescriptionXmlHelper.read(cmptElement);

        variedBase = cmptElement.getAttribute(ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT);
        if (IpsStringUtils.EMPTY.equals(variedBase)) {
            variedBase = null;
        }
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
     * Initializes all formulas contained by Element. If formula evaluation is supported, the map
     * contains the compiled expression for every formula.
     */
    protected void doInitFormulaFromXml(Element element) {
        formulaHandler.doInitFormulaFromXml(element);
    }

    /**
     * Returns <code>true</code> if the expression of the given formulaSignature not empty.
     */
    protected boolean isFormulaAvailable(String formularSignature) {
        return formulaHandler.isFormulaAvailable(formularSignature);
    }

    /**
     * @param linkElements the XML elements used to initialize {@link ProductComponentLink}
     *            instances.
     */
    protected void doInitReferencesFromXml(Map<String, List<Element>> linkElements) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to super.doInitReferencesFromXml().
    }

    /**
     * Creates a map containing the validation rule configurations found in the indicated XML
     * element. For each validation rule configuration the map contains an entry with the rule name
     * as a key and an {@link ValidationRuleConfiguration} instance as value.
     *
     * @param element an XML element containing a product component's data
     * @throws NullPointerException if element is <code>null</code>.
     * @since 3.22
     */
    protected void doInitValidationRuleConfigsFromXml(Element element) {
        validationRules.doInitValidationRuleConfigsFromXml(element);
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Creates an XML {@link Element} that represents this product component's data.
     * <p>
     * Throws an {@link UnsupportedOperationException} if the support for toXml ("Generate toXml
     * Support") is not activated in the FIPS standard builder.
     *
     * @param document a document, that can be used to create XML elements.
     */
    @Override
    public Element toXml(Document document) {
        return toXml(document, true);
    }

    /**
     * Creates an XML {@link Element} that represents this product component's data.
     * <p>
     * Throws an {@link UnsupportedOperationException} if the support for toXml ("Generate toXml
     * Support") is not activated in the FIPS standard builder.
     *
     * @param document a document, that can be used to create XML elements.
     * @param includeGenerations <code>true</code> if the created XML element should include the
     *            data of all the product component's generations, <code>false</code> if generations
     *            should be ignored when creating the XML output.
     */
    public Element toXml(Document document, boolean includeGenerations) {
        IToXmlSupport.check(this);
        Element prodCmptElement = document.createElement("ProductCmpt");

        prodCmptElement.setAttribute(TocEntry.PROPERTY_IMPLEMENTATION_CLASS, getClass().getName());
        prodCmptElement.setAttribute(XmlUtil.XML_ATTRIBUTE_SPACE, XmlUtil.XML_ATTRIBUTE_SPACE_VALUE);

        writeValidFromToXml(prodCmptElement);
        writeValidToToXml(prodCmptElement);
        DescriptionXmlHelper.write(description, prodCmptElement);

        if (getRepository().getNumberOfProductComponentGenerations(this) == 0) {
            Element emptyGenerationElement = prodCmptElement.getOwnerDocument().createElement(XML_ELEMENT_GENERATION);
            if (validFrom != null) {
                emptyGenerationElement.setAttribute(XML_ATTRIBUTE_VALID_FROM, validFrom.toIsoFormat());
            }
            prodCmptElement.appendChild(emptyGenerationElement);
        }
        if (includeGenerations) {
            List<IProductComponentGeneration> generations = getRepository().getProductComponentGenerations(this);
            Collections.sort(generations, comparing(IProductComponentGeneration::getValidFrom));
            for (IProductComponentGeneration generation : generations) {
                ProductComponentGeneration gen = (ProductComponentGeneration)generation;
                prodCmptElement.appendChild(gen.toXml(document));
            }
        }
        ((IToXmlSupport)this).writePropertiesToXml(prodCmptElement);
        writeFormulaToXml(prodCmptElement);
        writeTableUsagesToXml(prodCmptElement);
        writeReferencesToXml(prodCmptElement);
        writeValidationRuleConfigsToXml(prodCmptElement);
        writeExtensionPropertiesToXml(prodCmptElement);

        return sortNodesInAlphabeticalOrder(prodCmptElement);
    }

    private Element sortNodesInAlphabeticalOrder(Element prodCmptElement) {
        sortAttributeValues(prodCmptElement);
        sortConfiguredValues(prodCmptElement);
        sortTableUsageValues(prodCmptElement);

        NodeList childNodes = prodCmptElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals(XML_ELEMENT_GENERATION)) {
                sortAttributeValues((Element)node);
                sortConfiguredValues((Element)node);
            }
        }
        return prodCmptElement;
    }

    private static void sortTableUsageValues(Element root) {
        sortElements(root, BY_TABLE_STRUCTURE_USAGE_NAME, XML_TAG_TABLE_CONTENT_USAGE);
    }

    private static void sortAttributeValues(Element root) {
        sortElements(root, BY_ATTRIBUTE_NAME, XML_TAG_ATTRIBUTE_VALUE);
    }

    private static void sortConfiguredValues(Element root) {
        sortElements(root, BY_ATTRIBUTE_NAME.thenComparing(VALUE_SET_BEFORE_DEFAULT),
                XML_TAG_CONFIGURED_VALUE_SET, XML_TAG_CONFIGURED_DEFAULT);
    }

    private static void sortElements(Element root, Comparator<Element> comparator, String... allowedNodeNames) {
        List<Element> nodes = XmlUtil.getChildElements(root, allowedNodeNames);
        if (nodes.size() > 0) {
            AtomicReference<Node> refChild = new AtomicReference<>(nodes.get(0));
            nodes.stream()
                    .sorted(comparator)
                    .forEach(e -> {
                        root.insertBefore(e, refChild.get());
                        refChild.set(e.getNextSibling());
                    });
        }
    }

    private void writeValidFromToXml(Element prodCmptElement) {
        if (validFrom != null) {
            prodCmptElement.setAttribute(XML_ATTRIBUTE_VALID_FROM, validFrom.toIsoFormat());
        }
    }

    private void writeValidToToXml(Element prodCmptElement) {
        Element validToElement = prodCmptElement.getOwnerDocument().createElement(XML_ATTRIBUTE_VALID_TO);
        if (validTo != null) {
            validToElement.setTextContent(validTo.toIsoFormat());
        } else {
            validToElement.setAttribute(XML_ATTRIBUTE_IS_NULL, Boolean.TRUE.toString());
        }
        prodCmptElement.appendChild(validToElement);
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

    @Override
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
        return null;
    }

    @Override
    public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
        return new ArrayList<>();
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

    /**
     * @since 3.22
     */
    protected void writeValidationRuleConfigsToXml(Element genElement) {
        validationRules.writeValidationRuleConfigsToXml(genElement);
    }

    /**
     * This method is used for writing a formulas to the XML of the given {@link Element}.
     */
    protected void writeFormulaToXml(Element element) {
        formulaHandler.writeFormulaToXml(element);
    }

    @Override
    public boolean isValidationRuleActivated(String ruleName) {
        return validationRules.isValidationRuleActivated(ruleName);
    }

    @Override
    public void setValidationRuleActivated(String ruleName, boolean active) {
        validationRules.setValidationRuleActivated(ruleName, active);
    }

    /** @since 24.7 */
    FormulaHandler getFormulaHandler() {
        return formulaHandler;
    }

}
