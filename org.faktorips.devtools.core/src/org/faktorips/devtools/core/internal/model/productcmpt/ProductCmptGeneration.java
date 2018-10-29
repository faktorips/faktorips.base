/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class ProductCmptGeneration extends IpsObjectGeneration implements IProductCmptGeneration {

    private final ProductCmptLinkCollection linkCollection = new ProductCmptLinkCollection();

    private final PropertyValueCollection propertyValueCollection = new PropertyValueCollection(this);

    private final ProductPartCollection productPartCollection = new ProductPartCollection(propertyValueCollection,
            linkCollection);

    public ProductCmptGeneration(ITimedIpsObject ipsObject, String id) {
        super(ipsObject, id);
    }

    public ProductCmptGeneration() {
        super();
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return getProductCmpt().findProductCmptType(ipsProject);
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) {
        return getProductCmpt().findPolicyCmptType(ipsProject);
    }

    void dependsOn(Set<IDependency> dependencies, Map<IDependency, List<IDependencyDetail>> details) {
        linkCollection.addRelatedProductCmptQualifiedNameTypes(dependencies, details);
        addRelatedTableContentsQualifiedNameTypes(dependencies, details);
        addDependenciesFromFormulaExpressions(dependencies, details);
    }

    /**
     * Add the qualified name types of all related table contents inside the given generation to the
     * given set
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

    void addDependenciesFromFormulaExpressions(Set<IDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details) {
        IFormula[] formulas = getFormulas();
        for (IFormula formula : formulas) {
            Map<IDependency, ExpressionDependencyDetail> formulaDependencies = formula.dependsOn();
            dependencies.addAll(formulaDependencies.keySet());
            if (details != null) {
                mergeDependencyDetails(details, formulaDependencies);
            }
        }
    }

    private void mergeDependencyDetails(Map<IDependency, List<IDependencyDetail>> details,
            Map<IDependency, ExpressionDependencyDetail> formulaDependencies) {
        for (Entry<IDependency, ExpressionDependencyDetail> entry : formulaDependencies.entrySet()) {
            List<IDependencyDetail> dependenciesDetailsList = details.get(entry.getKey());
            if (dependenciesDetailsList == null) {
                dependenciesDetailsList = new ArrayList<IDependencyDetail>();
                details.put(entry.getKey(), dependenciesDetailsList);
            }
            dependenciesDetailsList.add(entry.getValue());
        }
    }

    @Override
    public boolean isChangingOverTimeContainer() {
        return true;
    }

    @Override
    public List<IPropertyValue> getPropertyValues(IProductCmptProperty property) {
        return propertyValueCollection.getPropertyValues(property);
    }

    @Override
    public boolean hasPropertyValue(IProductCmptProperty property, PropertyValueType type) {
        return getPropertyValue(property, type.getInterfaceClass()) != null;
    }

    @Override
    public List<IPropertyValue> getPropertyValues(String propertyName) {
        return propertyValueCollection.getPropertyValues(propertyName);
    }

    @Override
    public <T extends IPropertyValue> T getPropertyValue(IProductCmptProperty property, Class<T> type) {
        return propertyValueCollection.getPropertyValue(property, type);
    }

    @Override
    public <T extends IPropertyValue> T getPropertyValue(String propertyName, Class<T> type) {
        return propertyValueCollection.getPropertyValue(propertyName, type);
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
    public <T extends IIpsObjectPart> List<T> getProductParts(Class<T> type) {
        return productPartCollection.getProductParts(type);
    }

    public IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException {
        return new ProductCmptGenerationToTypeDelta(this, ipsProject);
    }

    @Override
    public IAttributeValue[] getAttributeValues() {
        List<IAttributeValue> attributeValues = propertyValueCollection.getPropertyValues(IAttributeValue.class);
        return attributeValues.toArray(new IAttributeValue[attributeValues.size()]);
    }

    @Override
    public IAttributeValue getAttributeValue(String attribute) {
        return propertyValueCollection.getPropertyValue(attribute, IAttributeValue.class);
    }

    @Override
    public int getNumOfAttributeValues() {
        return getAttributeValues().length;
    }

    @Override
    public IAttributeValue newAttributeValue() {
        IAttributeValue value = propertyValueCollection.newPropertyValue(getNextPartId(), IAttributeValue.class);
        objectHasChanged();
        return value;
    }

    @Override
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute) {
        IAttributeValue newPropertyValue = propertyValueCollection.newPropertyValue(attribute, getNextPartId(),
                IAttributeValue.class);
        objectHasChanged();
        return newPropertyValue;
    }

    /**
     * @deprecated as of 3.4. Use {@link #newAttributeValue(IProductCmptTypeAttribute)} instead.
     */
    @Override
    @Deprecated
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute, String value) {
        IAttributeValue attrValue = newAttributeValue(attribute);
        IValue<?> iValue;
        if (attribute != null) {
            iValue = ValueFactory.createValue(attribute.isMultilingual(), value);
        } else {
            iValue = ValueFactory.createStringValue(value);
        }
        attrValue.setValueHolder(new SingleValueHolder(attrValue, iValue));
        return attrValue;
    }

    @Override
    public IConfiguredDefault[] getConfiguredDefaults() {
        List<IConfiguredDefault> configuredDefaults = propertyValueCollection
                .getPropertyValues(IConfiguredDefault.class);
        return configuredDefaults.toArray(new IConfiguredDefault[configuredDefaults.size()]);
    }

    @Override
    public IConfiguredValueSet[] getConfiguredValueSets() {
        List<IConfiguredValueSet> configuredValueSets = propertyValueCollection
                .getPropertyValues(IConfiguredValueSet.class);
        return configuredValueSets.toArray(new IConfiguredValueSet[configuredValueSets.size()]);
    }

    @Override
    public IConfiguredDefault getConfiguredDefault(String attributeName) {
        return propertyValueCollection.getPropertyValue(attributeName, IConfiguredDefault.class);
    }

    @Override
    public IConfiguredValueSet getConfiguredValueSet(String attributeName) {
        return propertyValueCollection.getPropertyValue(attributeName, IConfiguredValueSet.class);
    }

    @Override
    public int getNumOfConfigElements() {
        return getConfiguredDefaults().length + getConfiguredValueSets().length;
    }

    @Override
    public IProductCmptLink[] getLinks() {
        List<IProductCmptLink> result = getLinksAsList();
        return result.toArray(new ProductCmptLink[result.size()]);
    }

    @Override
    public IProductCmptLink[] getLinks(String typeLink) {
        List<IProductCmptLink> result = getLinksAsList(typeLink);
        return result.toArray(new ProductCmptLink[result.size()]);
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
            /*
             * In 3.8 objectHasChanged() is now also called if toMove and target are identical,
             * where before it wasn't. This is because moveLink() returns true in that case (even
             * though no real "change" happened). However semantically it seems correct to mark as
             * changed if a link could be moved. Maybe the moveLink() implementation (or java doc)
             * needs to be re-thought?
             */
            objectHasChanged();
        }
        return moved;
    }

    @Override
    public ITableContentUsage newTableContentUsage() {
        ITableContentUsage value = propertyValueCollection.newPropertyValue(getNextPartId(), ITableContentUsage.class);
        objectHasChanged();
        return value;
    }

    @Override
    public ITableContentUsage newTableContentUsage(ITableStructureUsage structureUsage) {
        ITableContentUsage tableUsage = propertyValueCollection.newPropertyValue(structureUsage, getNextPartId(),
                ITableContentUsage.class);
        objectHasChanged();
        return tableUsage;
    }

    @Override
    public int getNumOfTableContentUsages() {
        return getTableContentUsages().length;
    }

    @Override
    public ITableContentUsage[] getTableContentUsages() {
        List<ITableContentUsage> usages = propertyValueCollection.getPropertyValues(ITableContentUsage.class);
        return usages.toArray(new ITableContentUsage[usages.size()]);
    }

    /**
     * Returns true if the generation contains a formula config element, otherwise false.
     */
    public boolean containsFormula() {
        return getNumOfFormulas() > 0;
    }

    @Override
    public int getNumOfFormulas() {
        return getFormulas().length;
    }

    @Override
    public IFormula[] getFormulas() {
        List<IFormula> formulae = propertyValueCollection.getPropertyValues(IFormula.class);
        return formulae.toArray(new IFormula[formulae.size()]);
    }

    @Override
    public IFormula getFormula(String formulaName) {
        return propertyValueCollection.getPropertyValue(formulaName, IFormula.class);
    }

    @Override
    public IFormula newFormula() {
        IFormula value = propertyValueCollection.newPropertyValue(getNextPartId(), IFormula.class);
        objectHasChanged();
        return value;
    }

    @Override
    public IFormula newFormula(IProductCmptTypeMethod signature) {
        IFormula newPropertyValue = propertyValueCollection.newPropertyValue(signature, getNextPartId(),
                IFormula.class);
        objectHasChanged();
        return newPropertyValue;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        int size = getNumOfAttributeValues() + getNumOfConfigElements() + getNumOfTableContentUsages()
                + getNumOfFormulas() + getNumOfValidationRules() + getNumOfLinks();
        List<IIpsElement> children = new ArrayList<IIpsElement>(size);
        children.addAll(propertyValueCollection.getAllPropertyValues());
        children.addAll(getLinksAsList());
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (IPolicyCmptTypeAssociation.class.isAssignableFrom(partType)) {
            return createAndAddNewLinkInternal(getNextPartId());
        } else if (IPropertyValue.class.isAssignableFrom(partType)) {
            Class<? extends IPropertyValue> propertyValueType = partType.asSubclass(IPropertyValue.class);
            return propertyValueCollection.newPropertyValue(getNextPartId(), propertyValueType);
        } else {
            return null;
        }
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(IProductCmptLink.TAG_NAME)) {
            return createAndAddNewLinkInternal(id);
        } else {
            return propertyValueCollection.newPropertyValue(xmlTagName, id);
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
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptLink) {
            return linkCollection.addLink((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            return propertyValueCollection.addPropertyValue((IPropertyValue)part);
        } else {
            return false;
        }
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptLink) {
            return linkCollection.remove((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            return propertyValueCollection.removePropertyValue((IPropertyValue)part);
        } else {
            return false;
        }
    }

    @Override
    protected void reinitPartCollectionsThis() {
        propertyValueCollection.clear();
        linkCollection.clear();
    }

    @Override
    public boolean isContainerFor(IProductCmptProperty property) {
        return property.isChangingOverTime();
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = findProductCmptType(ipsProject);
        // no type information available, so no further validation possible
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TEMPLATE, Messages.ProductCmptGeneration_msgTemplateNotFound, Message.ERROR,
                    this));
            return;
        }

        new ProductCmptLinkContainerValidator(ipsProject, this).startAndAddMessagesToList(type, list);
    }

    @Override
    public ITableContentUsage getTableContentUsage(String rolename) {
        return propertyValueCollection.getPropertyValue(rolename, ITableContentUsage.class);
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

    /**
     * Creates a new inactive {@link ValidationRuleConfig} for this generation.
     * 
     * @param id the part-ID to be assigned to the new validation rule
     * 
     * @return new validation rule
     */
    private IValidationRuleConfig newValidationRuleInternal(IValidationRule ruleToBeConfigured, String id) {
        return propertyValueCollection.newPropertyValue(ruleToBeConfigured, getNextPartId(),
                IValidationRuleConfig.class);
    }

    @Override
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured) {
        IValidationRuleConfig ruleConfig = newValidationRuleInternal(ruleToBeConfigured, getNextPartId());
        objectHasChanged();
        return ruleConfig;
    }

    @Override
    public String getProductCmptType() {
        return getProductCmpt().getProductCmptType();
    }

    @Override
    public boolean isContainerFor(IProductCmptTypeAssociation association) {
        return association.isChangingOverTime();
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
    public List<IProductCmptLink> getLinksIncludingProductCmpt() {
        List<IProductCmptLink> linksAsList = getProductCmpt().getLinksAsList();
        linksAsList.addAll(getLinksAsList());
        return linksAsList;
    }

    @Override
    public <T extends IPropertyValue> List<T> getPropertyValuesIncludingProductCmpt(Class<T> type) {
        List<T> values = getProductCmpt().getPropertyValues(type);
        values.addAll(getPropertyValues(type));
        return values;
    }

    @Override
    public boolean isProductTemplate() {
        return getProductCmpt().isProductTemplate();
    }

    @Override
    public IProductCmptGeneration findTemplate(IIpsProject ipsProject) {
        IProductCmpt template = getProductCmpt().findTemplate(ipsProject);
        if (template == null) {
            return null;
        }
        return template.getGenerationEffectiveOn(getValidFrom());
    }

    @Override
    public boolean isUsingTemplate() {
        return getProductCmpt().isUsingTemplate();
    }

    @Override
    public String getTemplate() {
        return getProductCmpt().getTemplate();
    }

    @Override
    public boolean isPartOfTemplateHierarchy() {
        return getProductCmpt().isPartOfTemplateHierarchy();
    }

    @Override
    public void removeUndefinedLinks() {
        linkCollection.removeUndefinedLinks();
    }

}
