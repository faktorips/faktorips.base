/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.Description;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.model.internal.ipsobject.TimedIpsObject;
import org.faktorips.devtools.model.internal.productcmpt.template.RemoveTemplateOperation;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValidations;
import org.faktorips.devtools.model.internal.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.model.internal.type.TypeValidations;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of product component.
 * 
 * @author Jan Ortmann
 */
public class ProductCmpt extends TimedIpsObject implements IProductCmpt {

    private final ProductCmptLinkCollection linkCollection = new ProductCmptLinkCollection();

    private final PropertyValueCollection propertyValueCollection = new PropertyValueCollection(this);

    private final ProductPartCollection productPartCollection = new ProductPartCollection(propertyValueCollection,
            linkCollection);

    private final IpsObjectType ipsObjectType;

    private String productCmptType = ""; //$NON-NLS-1$

    private String runtimeId = ""; //$NON-NLS-1$

    private String template = null;

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
        this.ipsObjectType = file.getIpsObjectType();
    }

    public ProductCmpt() {
        super();
        ipsObjectType = IpsObjectType.PRODUCT_CMPT;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return ipsObjectType;
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration(int index) {
        return (IProductCmptGeneration)getGeneration(index);
    }

    @Override
    public IProductCmptGeneration getFirstGeneration() {
        return (IProductCmptGeneration)super.getFirstGeneration();
    }

    @Override
    public IProductCmptGeneration getLatestProductCmptGeneration() {
        IIpsObjectGeneration latestGeneration = getLatestGeneration();
        return latestGeneration == null ? null : (IProductCmptGeneration)latestGeneration;
    }

    @Override
    public IProductCmptKind getKindId() {
        return ProductCmptKind.createProductCmptKind(getName(), getIpsProject());
    }

    @Override
    public String getVersionId() throws CoreException {
        try {
            return getIpsProject().getProductCmptNamingStrategy().getVersionId(getName());
        } catch (IllegalArgumentException e) {
            throw new CoreException(new IpsStatus("Can't get version id for " + this, e)); //$NON-NLS-1$
        }
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) {
        IProductCmptType foundProductCmptType = findProductCmptType(ipsProject);
        if (foundProductCmptType == null) {
            return null;
        }
        return foundProductCmptType.findPolicyCmptType(ipsProject);
    }

    @Override
    public String getProductCmptType() {
        return productCmptType;
    }

    @Override
    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return ipsProject.findProductCmptType(productCmptType);
    }

    @Override
    public boolean isUsingTemplate() {
        return StringUtils.isNotEmpty(getTemplate());
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(String newTemplate) {
        if (StringUtils.isEmpty(newTemplate)) {
            resetTemplateStatus();
        }
        String oldTemplate = template;
        this.template = newTemplate;
        valueChanged(oldTemplate, template, IProductCmpt.PROPERTY_TEMPLATE);
    }

    @Override
    public void resetTemplateStatus() {
        if (isUsingTemplate()) {
            try {
                getEnclosingResource().getWorkspace().run(new RemoveTemplateOperation(this), new NullProgressMonitor());
            } catch (CoreException e) {
                IpsLog.log(e);
            }
        }
    }

    @Override
    public IProductCmpt findTemplate(IIpsProject ipsProject) {
        if (isUsingTemplate()) {
            return ipsProject.findProductTemplate(template);
        } else {
            return null;
        }
    }

    @Override
    protected IpsObjectGeneration createNewGeneration(String id) {
        return new ProductCmptGeneration(this, id);
    }

    @Override
    public boolean isChangingOverTimeContainer() {
        return false;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = ProductCmptValidations.validateProductCmptType(this, productCmptType, list, ipsProject);
        if (type == null) {
            return;
        }
        if (!validateTypeHierarchy(list, ipsProject, type)) {
            return;
        }
        validateUniqueVersionIdKindId(list, ipsProject);
        validateName(list, ipsProject);
        if (isProductTemplate()) {
            TemplateValidations.validateTemplateCycle(this, list, ipsProject);
        } else {
            validateRuntimeId(list, ipsProject);
        }
        validateLinks(list, ipsProject, type);
        validateDifferencesToModel(list, ipsProject);
        ProductCmptValidations.validateTemplate(this, type, list, ipsProject);
    }

    private void validateUniqueVersionIdKindId(MessageList list, IIpsProject ipsProject) {
        Severity duplicateProductComponentSeverity = ipsProject.getReadOnlyProperties()
                .getDuplicateProductComponentSeverity();
        if (!Severity.NONE.equals(duplicateProductComponentSeverity)) {
            if (ipsProject.findProductCmptByUnqualifiedName(getName()).size() > 1) {
                list.add(new Message(MSGCODE_DUPLICATE_KINDID_VERSIONID,
                        NLS.bind(Messages.ProductCmpt_Error_IdsNotUnique,
                                IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention()
                                        .getVersionConceptNameSingular()),
                        duplicateProductComponentSeverity,
                        this));
            }
        }
    }

    private boolean validateTypeHierarchy(MessageList list, IIpsProject ipsProject, IProductCmptType type) {
        Message message = TypeValidations.validateTypeHierachy(type, ipsProject);
        if (message != null) {
            String typeLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(type);
            String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, typeLabel);
            list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, msg, Message.ERROR, type,
                    PROPERTY_PRODUCT_CMPT_TYPE));
            // do not continue validation if hierarchy is invalid
            return false;
        }
        return true;
    }

    private void validateName(MessageList list, IIpsProject ipsProject) {
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
        MessageList list2 = strategy.validate(getName());
        for (Message msg : list2) {
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_NAME);
            list.add(msgNew);
        }
    }

    private void validateRuntimeId(MessageList list, IIpsProject ipsProject) throws CoreException {
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
        MessageList list2 = strategy.validateRuntimeId(getRuntimeId());
        for (Message msg : list2) {
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_RUNTIME_ID);
            list.add(msgNew);
        }

        list2 = getIpsProject().checkForDuplicateRuntimeIds(getIpsSrcFile());
        list.add(list2);
    }

    private void validateLinks(MessageList list, IIpsProject ipsProject, IProductCmptType type) {
        new ProductCmptLinkContainerValidator(ipsProject, this).startAndAddMessagesToList(type, list);
    }

    private void validateDifferencesToModel(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (containsDifferenceToModel(ipsProject)) {
            list.newError(MSGCODE_DIFFERENCES_TO_MODEL, Messages.ProductCmpt_Error_DifferencesToModel0, this);
        }
    }

    @Override
    public boolean containsGenerationFormula() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            if (((ProductCmptGeneration)generation).containsFormula()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        Set<IDependency> dependencySet = new HashSet<>();

        if (!StringUtils.isEmpty(productCmptType)) {
            IDependency dependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                    new QualifiedNameType(productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE));
            dependencySet.add(dependency);
            addDetails(details, dependency, this, PROPERTY_PRODUCT_CMPT_TYPE);
        }
        if (isUsingTemplate()) {
            IpsObjectDependency dependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                    new QualifiedNameType(template, IpsObjectType.PRODUCT_TEMPLATE));
            dependencySet.add(dependency);
            addDetails(details, dependency, this, PROPERTY_TEMPLATE);
        }

        linkCollection.addRelatedProductCmptQualifiedNameTypes(dependencySet, details);
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            ((ProductCmptGeneration)generation).dependsOn(dependencySet, details);
        }
        addRelatedTableContentsQualifiedNameTypes(dependencySet, details);
        addDependenciesFromFormulaExpressions(dependencySet, details);

        return dependencySet.toArray(new IDependency[dependencySet.size()]);
    }

    /**
     * Add the qualified name types of all related table contents.
     */
    private void addRelatedTableContentsQualifiedNameTypes(Set<IDependency> qaTypes,
            Map<IDependency, List<IDependencyDetail>> details) {

        ITableContentUsage[] tableContentUsages = getTableContentUsages();
        for (ITableContentUsage tableContentUsage : tableContentUsages) {
            IDependency dependency = IpsObjectDependency.createReferenceDependency(
                    getIpsObject().getQualifiedNameType(),
                    new QualifiedNameType(tableContentUsage.getTableContentName(), IpsObjectType.TABLE_CONTENTS));
            qaTypes.add(dependency);
            addDetails(details, dependency, tableContentUsage, ITableContentUsage.PROPERTY_TABLE_CONTENT);
        }
    }

    private void addDependenciesFromFormulaExpressions(Set<IDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details) {
        for (IFormula formula : getFormulas()) {
            Map<IDependency, IExpressionDependencyDetail> formulaDependencies = formula.dependsOn();
            dependencies.addAll(formulaDependencies.keySet());
            if (details != null) {
                mergeDependencyDetails(details, formulaDependencies);
            }
        }
    }

    private void mergeDependencyDetails(Map<IDependency, List<IDependencyDetail>> details,
            Map<IDependency, IExpressionDependencyDetail> formulaDependencies) {
        for (Entry<IDependency, IExpressionDependencyDetail> entry : formulaDependencies.entrySet()) {
            List<IDependencyDetail> dependenciesDetailsList = details.computeIfAbsent(entry.getKey(),
                    $ -> new ArrayList<>());
            dependenciesDetailsList.add(entry.getValue());
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
        element.setAttribute(PROPERTY_TEMPLATE, template);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        runtimeId = element.getAttribute(PROPERTY_RUNTIME_ID);
        template = element.getAttribute(PROPERTY_TEMPLATE);
    }

    @Override
    public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject)
            throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, date, ipsProject);
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

    @Override
    public void setRuntimeId(String runtimeId) {
        String oldId = this.runtimeId;
        this.runtimeId = runtimeId;
        valueChanged(oldId, runtimeId, IProductCmpt.PROPERTY_RUNTIME_ID);
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        IProductCmptType cmptType = findProductCmptType(getIpsProject());
        if (cmptType != null) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(cmptType);
        } else {
            return getProductCmptType();
        }
    }

    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        return !computeDeltaToModel(ipsProject).isEmpty();
    }

    @Override
    public void fixAllDifferencesToModel(final IIpsProject ipsProject) throws CoreException {
        ((IpsModel)getIpsModel())
                .executeModificationsWithSingleEvent(new SingleEventModification<>(getIpsSrcFile()) {

                    @Override
                    protected boolean execute() throws CoreException {
                        computeDeltaToModel(ipsProject).fixAllDifferencesToModel();
                        return true;
                    }

                });
    }

    @Override
    public IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException {
        return new ProductCmptToTypeDelta(this, ipsProject);
    }

    @Override
    public boolean isReferencingProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate) {
        List<IProductCmptLink> links = getLinksIncludingGenerations();
        for (IProductCmptLink link : links) {
            if (productCmptCandidate.getQualifiedName().equals(link.getTarget())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE, getProductCmptType());
    }

    @Override
    public String getMetaClass() {
        return getProductCmptType();
    }

    @Override
    public <T extends IPropertyValue> T getPropertyValue(IProductCmptProperty property, Class<T> type) {
        return propertyValueCollection.getPropertyValue(property, type);
    }

    @Override
    public List<IPropertyValue> getPropertyValues(IProductCmptProperty property) {
        return propertyValueCollection.getPropertyValues(property);
    }

    @Override
    public boolean hasPropertyValue(IProductCmptProperty property, PropertyValueType propertyValueType) {
        return getPropertyValue(property, propertyValueType.getInterfaceClass()) != null;
    }

    @Override
    public List<IPropertyValue> getPropertyValues(String propertyName) {
        return propertyValueCollection.getPropertyValues(propertyName);
    }

    @Override
    public <T extends IPropertyValue> T getPropertyValue(String propertyName, Class<T> type) {
        return propertyValueCollection.getPropertyValue(propertyName, type);
    }

    @Override
    public <T extends IIpsObjectPart> List<T> getProductParts(Class<T> type) {
        return productPartCollection.getProductParts(type);
    }

    @Override
    public <T extends IPropertyValue> List<T> getPropertyValues(Class<T> type) {
        return propertyValueCollection.getPropertyValues(type);
    }

    @Override
    public List<IPropertyValue> getAllPropertyValues() {
        return propertyValueCollection.getAllPropertyValues();
    }

    @Override
    public <T extends IPropertyValue> T newPropertyValue(IProductCmptProperty property, Class<T> type) {
        T newPropertyValue = propertyValueCollection.newPropertyValue(property, getNextPartId(), type);
        objectHasChanged();
        return newPropertyValue;
    }

    @Override
    public List<IPropertyValue> newPropertyValues(IProductCmptProperty property) {
        List<IPropertyValue> newPropertyValues = propertyValueCollection.newPropertyValues(this, property,
                getNextPartId());
        objectHasChanged();
        return newPropertyValues;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        IIpsObjectPart part = super.newPartThis(xmlTag, id);
        if (part != null) {
            return part;
        }
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(IIpsObjectGeneration.TAG_NAME)) {
            return newGenerationInternal(id);
        } else if (xmlTagName.equals(IProductCmptLink.TAG_NAME)) {
            return createAndAddNewLinkInternal(id);
        } else {
            return propertyValueCollection.newPropertyValue(xmlTagName, id);
        }
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        IIpsObjectPart part = super.newPartThis(partType);
        if (part != null) {
            return part;
        }
        if (IPolicyCmptTypeAssociation.class.isAssignableFrom(partType)) {
            return createAndAddNewLinkInternal(getNextPartId());
        } else if (IPropertyValue.class.isAssignableFrom(partType)) {
            Class<? extends IPropertyValue> propertyValueType = partType.asSubclass(IPropertyValue.class);
            return propertyValueCollection.newPropertyValue(getNextPartId(), propertyValueType);
        } else {
            return null;
        }
    }

    /**
     * Creates a link without a corresponding association name. The association thus remains
     * undefined. Moreover the association must not be set as this method is used for XML
     * initialization which in turn must not trigger value changes (and setAssociation() would).
     * 
     * @param id the future part id of the new link
     */
    private IProductCmptLink createAndAddNewLinkInternal(String id) {
        ProductCmptLink newLink = new ProductCmptLink(this, id);
        linkCollection.addLink(newLink);
        return newLink;
    }

    @Override
    public List<IAttributeValue> getAttributeValues() {
        return propertyValueCollection.getPropertyValues(IAttributeValue.class);
    }

    @Override
    public IAttributeValue getAttributeValue(String attribute) {
        return propertyValueCollection.getPropertyValue(attribute, IAttributeValue.class);
    }

    @Override
    public boolean isContainerFor(IProductCmptProperty property) {
        return property.isChangingOverTime() == isChangingOverTimeContainer();
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        List<IIpsElement> children = new ArrayList<>();
        if (allowGenerations()) {
            IIpsElement[] childrenThis = super.getChildrenThis();
            children.addAll(Arrays.asList(childrenThis));
        }
        children.addAll(propertyValueCollection.getAllPropertyValues());
        children.addAll(getLinksAsList());
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    public Element toXml(Document doc) {
        Element xmlElement = super.toXml(doc);
        if (!allowGenerations()) {
            writeDummyGeneration(doc, xmlElement);
        }
        return xmlElement;
    }

    /**
     * For compatibility reasons we always keep one generation also if the type does not support
     * generations (is not changing over time). In the {@link #getChildrenThis()} method we hide
     * this dummy generation. Hence the toXml framework methods do not write it automatically.
     * 
     * Try to add the generation after the last {@link Description} element of this
     * {@link ProductCmpt}.
     */
    private void writeDummyGeneration(Document doc, Element rootElement) {
        IProductCmptGeneration generation = getFirstGeneration();
        Element generationElement = generation.toXml(doc);

        Node lastDescriptionOfProductCmpt = null;
        NodeList childNodes = rootElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (Description.XML_TAG_NAME.equals(item.getNodeName())) {
                lastDescriptionOfProductCmpt = item;
            }
        }
        if (lastDescriptionOfProductCmpt != null) {
            rootElement.insertBefore(generationElement, lastDescriptionOfProductCmpt.getNextSibling());
        } else {
            // if XSD validation is on, this will lead to a validation error
            rootElement.appendChild(generationElement);
        }
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (super.addPartThis(part)) {
            return true;
        }
        if (part instanceof IProductCmptLink) {
            return linkCollection.addLink((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            IPropertyValue propertyValue = (IPropertyValue)part;
            return propertyValueCollection.addPropertyValue(propertyValue);
        } else {
            return false;
        }
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (super.removePartThis(part)) {
            return true;
        }
        if (part instanceof IProductCmptLink) {
            return linkCollection.remove((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            IPropertyValue propertyValue = (IPropertyValue)part;
            return propertyValueCollection.removePropertyValue(propertyValue);
        } else {
            return false;
        }
    }

    @Override
    protected void reinitPartCollectionsThis() {
        super.reinitPartCollectionsThis();
        propertyValueCollection.clear();
        linkCollection.clear();
    }

    @Override
    public List<IProductCmptGeneration> getProductCmptGenerations() {
        List<IProductCmptGeneration> generations = new ArrayList<>();
        List<IIpsObjectGeneration> ipsObjectGenerations = getGenerations();
        for (IIpsObjectGeneration ipsObjectGeneration : ipsObjectGenerations) {
            generations.add((IProductCmptGeneration)ipsObjectGeneration);
        }
        return generations;
    }

    @Override
    public IProductCmptGeneration getGenerationEffectiveOn(GregorianCalendar date) {
        return (IProductCmptGeneration)super.getGenerationEffectiveOn(date);
    }

    @Override
    public IProductCmptGeneration getBestMatchingGenerationEffectiveOn(GregorianCalendar date) {
        return (IProductCmptGeneration)super.getBestMatchingGenerationEffectiveOn(date);
    }

    @Override
    public IProductCmptGeneration getGenerationByEffectiveDate(GregorianCalendar date) {
        return (IProductCmptGeneration)super.getGenerationByEffectiveDate(date);
    }

    @Override
    public List<IPropertyValue> findPropertyValues(IProductCmptCategory category,
            GregorianCalendar effectiveDate,
            IIpsProject ipsProject) throws CoreException {

        IProductCmptGeneration generation = getGenerationByEffectiveDate(effectiveDate);
        return category != null ? findPropertyValuesForSpecificCategory(generation, category, ipsProject)
                : findPropertyValuesForNoCategory(generation);
    }

    private List<IPropertyValue> findPropertyValuesForSpecificCategory(IProductCmptGeneration generation,
            IProductCmptCategory category,
            IIpsProject ipsProject) throws CoreException {

        List<IPropertyValue> propertyValues = new ArrayList<>();

        IProductCmptType contextType = findProductCmptType(ipsProject);
        if (contextType == null) {
            return propertyValues;
        }

        for (IProductCmptProperty property : category.findProductCmptProperties(contextType, true, ipsProject)) {
            propertyValues.addAll(getPropertyValues(property));
            if (generation != null) {
                propertyValues.addAll(generation.getPropertyValues(property));
            }
        }

        return propertyValues;
    }

    private List<IPropertyValue> findPropertyValuesForNoCategory(IProductCmptGeneration generation) {
        List<IPropertyValue> propertyValues = new ArrayList<>();
        propertyValues.addAll(getAllPropertyValues());
        propertyValues.addAll(generation.getAllPropertyValues());
        return propertyValues;
    }

    @Override
    public boolean isContainerFor(IProductCmptTypeAssociation association) {
        return association.isChangingOverTime() == isChangingOverTimeContainer();
    }

    @Override
    public int getNumOfLinks() {
        return linkCollection.size();
    }

    @Override
    public IProductCmptLink newLink(IProductCmptTypeAssociation association) {
        return newLink(association.getName());
    }

    @Override
    public IProductCmptLink newLink(String associationName) {
        IProductCmptLink newLink = linkCollection.createAndAddNewLink(this, associationName, getNextPartId());
        objectHasChanged();
        return newLink;
    }

    @Override
    public IProductCmptLink newLink(String associationName, IProductCmptLink insertAbove) {
        IProductCmptLink newLink = linkCollection.createAndInsertNewLink(this, associationName, getNextPartId(),
                insertAbove);
        objectHasChanged();
        return newLink;
    }

    @Override
    public boolean canCreateValidLink(IProductCmpt target,
            IProductCmptTypeAssociation association,
            IIpsProject ipsProject) throws CoreException {
        return ProductCmptLinkContainerUtil.canCreateValidLink(this, target, association, ipsProject);
    }

    @Override
    public boolean moveLink(IProductCmptLink toMove, IProductCmptLink target, boolean above) {
        boolean moved = linkCollection.moveLink(toMove, target, above);
        if (moved) {
            objectHasChanged();
        }
        return moved;
    }

    @Override
    public List<IProductCmptLink> getLinksAsList() {
        return linkCollection.getLinks();
    }

    @Override
    public List<IProductCmptLink> getLinksAsList(String associationName) {
        return linkCollection.getLinks(associationName);
    }

    @Override
    public List<IProductCmptLink> getLinksIncludingGenerations() {
        List<IProductCmptLink> linksAsList = getLinksAsList();
        for (IProductCmptGeneration generation : getProductCmptGenerations()) {
            linksAsList.addAll(generation.getLinksAsList());
        }
        return linksAsList;
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return this;
    }

    @Override
    public ITableContentUsage getTableContentUsage(String rolename) {
        return propertyValueCollection.getPropertyValue(rolename, ITableContentUsage.class);
    }

    @Override
    public ITableContentUsage[] getTableContentUsages() {
        List<ITableContentUsage> usages = propertyValueCollection.getPropertyValues(ITableContentUsage.class);
        return usages.toArray(new ITableContentUsage[usages.size()]);
    }

    @Override
    public IFormula[] getFormulas() {
        List<IFormula> formulas = propertyValueCollection.getPropertyValues(IFormula.class);
        return formulas.toArray(new IFormula[formulas.size()]);
    }

    @Override
    public IFormula getFormula(String formulaName) {
        return propertyValueCollection.getPropertyValue(formulaName, IFormula.class);
    }

    /**
     * Returns <code>true</code> if the {@link IProductCmptType}, this {@link IProductCmpt} is based
     * on, allows changing over time. If not, <code>false</code> is returned.
     * <p>
     * In case that the corresponding {@link IProductCmptType} can not be found, we assume that
     * changing over time is enabled. In that case <code>true</code> is returned.
     */
    @Override
    public boolean allowGenerations() {
        IProductCmptType productComponentType = findProductCmptType(getIpsProject());
        return productComponentType == null ? true : productComponentType.isChangingOverTime();
    }

    @Override
    public boolean isProductTemplate() {
        return getIpsObjectType().equals(IpsObjectType.PRODUCT_TEMPLATE);
    }

    @Override
    public boolean isPartOfTemplateHierarchy() {
        return isProductTemplate() || isUsingTemplate();
    }

    @Override
    public void removeUndefinedLinks() {
        linkCollection.removeUndefinedLinks();
    }

    @Override
    public int getNumOfValidationRules() {
        return getValidationRuleConfigs().size();
    }

    @Override
    public IValidationRuleConfig getValidationRuleConfig(String validationRuleName) {
        return propertyValueCollection.getPropertyValue(validationRuleName, IValidationRuleConfig.class);
    }

    @Override
    public List<IValidationRuleConfig> getValidationRuleConfigs() {
        return propertyValueCollection.getPropertyValues(IValidationRuleConfig.class);
    }

    @Override
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured) {
        IValidationRuleConfig ruleConfig = propertyValueCollection.newPropertyValue(ruleToBeConfigured, getNextPartId(),
                IValidationRuleConfig.class);
        objectHasChanged();
        return ruleConfig;
    }

}
