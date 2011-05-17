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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
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
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

public class ProductCmptGeneration extends IpsObjectGeneration implements IProductCmptGeneration {

    private final AttributeValueContainer attributeValueContainer;

    private List<IConfigElement> configElements = new ArrayList<IConfigElement>(0);

    private List<IProductCmptLink> links = new ArrayList<IProductCmptLink>(0);

    private List<ITableContentUsage> tableContentUsages = new ArrayList<ITableContentUsage>(0);

    private List<IFormula> formulas = new ArrayList<IFormula>(0);

    private List<IValidationRuleConfig> validationRules = new ArrayList<IValidationRuleConfig>(0);

    public ProductCmptGeneration(ITimedIpsObject ipsObject, String id) {
        super(ipsObject, id);
        attributeValueContainer = new AttributeValueContainer(this);
    }

    public ProductCmptGeneration() {
        attributeValueContainer = new AttributeValueContainer(this);
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmpt().findProductCmptType(ipsProject);
    }

    void dependsOn(Set<IDependency> dependencies, Map<IDependency, List<IDependencyDetail>> details) {
        addRelatedProductCmptQualifiedNameTypes(dependencies, details);
        addRelatedTableContentsQualifiedNameTypes(dependencies, details);
    }

    /**
     * Add the qualified name types of all related table contents inside the given generation to the
     * given set
     */
    private void addRelatedTableContentsQualifiedNameTypes(Set<IDependency> qaTypes,
            Map<IDependency, List<IDependencyDetail>> details) {

        ITableContentUsage[] tableContentUsages = getTableContentUsages();
        for (ITableContentUsage tableContentUsage : tableContentUsages) {
            IDependency dependency = IpsObjectDependency.createReferenceDependency(getIpsObject()
                    .getQualifiedNameType(), new QualifiedNameType(tableContentUsage.getTableContentName(),
                    IpsObjectType.TABLE_CONTENTS));
            qaTypes.add(dependency);
            addDetails(details, dependency, tableContentUsage, ITableContentUsage.PROPERTY_TABLE_CONTENT);
        }
    }

    /**
     * Add the qualified name types of all related product cmpt's inside the given generation to the
     * given set
     */
    private void addRelatedProductCmptQualifiedNameTypes(Set<IDependency> qaTypes,
            Map<IDependency, List<IDependencyDetail>> details) {

        IProductCmptLink[] relations = getLinks();
        for (IProductCmptLink relation : relations) {
            IDependency dependency = IpsObjectDependency.createReferenceDependency(getIpsObject()
                    .getQualifiedNameType(), new QualifiedNameType(relation.getTarget(), IpsObjectType.PRODUCT_CMPT));
            qaTypes.add(dependency);
            addDetails(details, dependency, relation, IProductCmptLink.PROPERTY_TARGET);
        }
    }

    @Override
    public IPropertyValue getPropertyValue(IProductCmptProperty property) {
        if (property == null) {
            return null;
        }
        ProductCmptPropertyType type = property.getProdDefPropertyType();
        if (type.equals(ProductCmptPropertyType.VALUE)) {
            return attributeValueContainer.getPropertyValue(property);
        }
        if (type.equals(ProductCmptPropertyType.TABLE_CONTENT_USAGE)) {
            return getTableContentUsage(property.getPropertyName());
        }
        if (type.equals(ProductCmptPropertyType.FORMULA)) {
            return getFormula(property.getPropertyName());
        }
        if (type.equals(ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET)) {
            return getConfigElement(property.getPropertyName());
        }
        throw new RuntimeException("Unknown property type " + type); //$NON-NLS-1$
    }

    @Override
    public IPropertyValue getPropertyValue(String propertyName) {
        if (propertyName == null) {
            return null;
        }
        IPropertyValue value = attributeValueContainer.getPropertyValue(propertyName);
        if (value != null) {
            return value;
        }
        value = getTableContentUsage(propertyName);
        if (value != null) {
            return value;
        }
        value = getFormula(propertyName);
        if (value != null) {
            return value;
        }
        value = getValidationRuleConfig(propertyName);
        if (value != null) {
            return value;
        }
        return getConfigElement(propertyName);
    }

    @Override
    public List<IPropertyValue> getPropertyValues(ProductCmptPropertyType type) {
        if (type == null) {
            return new ArrayList<IPropertyValue>();
        }
        if (ProductCmptPropertyType.VALUE.equals(type)) {
            return attributeValueContainer.getPropertyValues(type);
        }
        if (ProductCmptPropertyType.TABLE_CONTENT_USAGE.equals(type)) {
            return new ArrayList<IPropertyValue>(tableContentUsages);
        }
        if (ProductCmptPropertyType.FORMULA.equals(type)) {
            return new ArrayList<IPropertyValue>(formulas);
        }
        if (ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(type)) {
            return new ArrayList<IPropertyValue>(configElements);
        }
        if (ProductCmptPropertyType.VALIDATION_RULE_CONFIG.equals(type)) {
            return new ArrayList<IPropertyValue>(validationRules);
        }
        throw new RuntimeException("Unknown type " + type); //$NON-NLS-1$
    }

    @Override
    public IPropertyValue newPropertyValue(IProductCmptProperty property) {
        ProductCmptPropertyType type = property.getProdDefPropertyType();
        if (ProductCmptPropertyType.VALUE.equals(type)) {
            return attributeValueContainer.newPropertyValue(property, getNextPartId());
        }
        if (ProductCmptPropertyType.TABLE_CONTENT_USAGE.equals(type)) {
            return newTableContentUsage((ITableStructureUsage)property);
        }
        if (ProductCmptPropertyType.FORMULA.equals(type)) {
            return newFormula((IProductCmptTypeMethod)property);
        }
        if (ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(type)) {
            return newConfigElement((IPolicyCmptTypeAttribute)property);
        }
        if (ProductCmptPropertyType.VALIDATION_RULE_CONFIG.equals(type)) {
            return newConfigElement((IPolicyCmptTypeAttribute)property);
        }
        throw new RuntimeException("Unknown type " + type); //$NON-NLS-1$
    }

    @Override
    public IGenerationToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException {
        return new GenerationToTypeDelta(this, ipsProject);
    }

    @Override
    public void sortPropertiesAccordingToModel(IIpsProject ipsProject) throws CoreException {
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return;
        }
        PropertyValueComparator comparator = new PropertyValueComparator(this, getIpsProject());
        List<IAttributeValue> attributeValues = attributeValueContainer.getAttributeValues();
        Collections.sort(attributeValues, comparator);
        Collections.sort(configElements, comparator);
        /*
         * TODO IProductCmptLink is no IPropertyValue as is IValidationRuleConfig. Both cannot be
         * sorted using above comparator.
         */
        Collections.sort(tableContentUsages, comparator);
        Collections.sort(formulas, comparator);
    }

    @Override
    public IAttributeValue[] getAttributeValues() {
        List<IAttributeValue> attributeValues = attributeValueContainer.getAttributeValues();
        return attributeValues.toArray(new IAttributeValue[attributeValues.size()]);
    }

    @Override
    public IAttributeValue getAttributeValue(String attribute) {
        return attributeValueContainer.getAttributeValue(attribute);
    }

    @Override
    public int getNumOfAttributeValues() {
        return attributeValueContainer.getAttributeValues().size();
    }

    @Override
    public IAttributeValue newAttributeValue() {
        return attributeValueContainer.newAttributeValue(getNextPartId());
    }

    @Override
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute) {
        IAttributeValue newValue = attributeValueContainer.newAttributeValue(getNextPartId(), attribute);
        return newValue;
    }

    @Override
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute, String value) {
        IAttributeValue newValue = attributeValueContainer.newAttributeValue(getNextPartId(), attribute, value);
        return newValue;
    }

    @Override
    public IConfigElement[] getConfigElements() {
        return configElements.toArray(new IConfigElement[configElements.size()]);
    }

    @Override
    public IConfigElement getConfigElement(String attributeName) {
        for (IConfigElement each : configElements) {
            if (each.getPolicyCmptTypeAttribute().equals(attributeName)) {
                return each;
            }
        }
        return null;
    }

    @Override
    public int getNumOfConfigElements() {
        return configElements.size();
    }

    @Override
    public IConfigElement newConfigElement() {
        return newConfigElement(null);
    }

    @Override
    public IConfigElement newConfigElement(IPolicyCmptTypeAttribute attribute) {
        IConfigElement newElement = newConfigElementInternal(getNextPartId(), attribute);
        /*
         * this is necessary because though broadcasting has been stopped the modified status will
         * still be changed. To enable the triggering of the modification event in the
         * objectHasChanged() method it is necessary to clear the modification status first
         */
        // TODO pk possible better solution: send a modification event after resuming broadcasting
        getIpsSrcFile().markAsClean();
        objectHasChanged();
        return newElement;
    }

    /**
     * Creates a new attribute without updating the src file.
     */
    private ConfigElement newConfigElementInternal(String id, IPolicyCmptTypeAttribute attribute) {
        ConfigElement e = new ConfigElement(this, id);
        if (attribute != null) {
            try {
                ((IpsModel)getIpsModel()).stopBroadcastingChangesMadeByCurrentThread();
                e.setPolicyCmptTypeAttribute(attribute.getName());
                e.setValue(attribute.getDefaultValue());
                e.setValueSetCopy(attribute.getValueSet());
            } finally {
                ((IpsModel)getIpsModel()).resumeBroadcastingChangesMadeByCurrentThread();
            }
        }
        configElements.add(e);
        return e;
    }

    @Override
    public IProductCmptLink[] getLinks() {
        return links.toArray(new ProductCmptLink[links.size()]);
    }

    @Override
    public IProductCmptLink[] getLinks(String typeLink) {
        List<IProductCmptLink> result = new ArrayList<IProductCmptLink>();
        for (IProductCmptLink link : links) {
            if (link.getAssociation().equals(typeLink)) {
                result.add(link);
            }
        }
        return result.toArray(new ProductCmptLink[result.size()]);
    }

    @Override
    public int getNumOfLinks() {
        return links.size();
    }

    @Override
    public IProductCmptLink newLink(IProductCmptTypeAssociation association) {
        return newLink(association.getName());
    }

    @Override
    public IProductCmptLink newLink(String associationName) {
        ProductCmptLink newRelation = newLinkInternal(getNextPartId());
        newRelation.setAssociation(associationName);
        objectHasChanged();
        return newRelation;
    }

    @Override
    public IProductCmptLink newLink(String associationName, IProductCmptLink insertBefore) {
        ProductCmptLink newRelation = newLinkInternal(getNextPartId(), insertBefore);
        newRelation.setAssociation(associationName);
        objectHasChanged();
        return newRelation;
    }

    public IProductCmptLink newLink() {
        return newLinkInternal(getNextPartId());
    }

    @Override
    public boolean canCreateValidLink(IProductCmpt target, IAssociation association, IIpsProject ipsProject)
            throws CoreException {

        if (association == null || target == null || !getIpsSrcFile().isMutable()) {
            return false;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return false;
        }
        // it is not valid to create more than one relation with the same type and target.
        if (!isFirstRelationOfThisType(association, target, ipsProject)) {
            return false;
        }
        // is correct type
        IProductCmptType targetType = target.findProductCmptType(ipsProject);
        if (targetType == null) {
            return false;
        }
        if (!targetType.isSubtypeOrSameType(association.findTarget(ipsProject), ipsProject)) {
            return false;
        }

        return this.getLinks(association.getName()).length < association.getMaxCardinality()
                && ProductCmptLink.willBeValid(target, association, ipsProject);
    }

    private boolean isFirstRelationOfThisType(IAssociation association, IProductCmpt target, IIpsProject ipsProject)
            throws CoreException {

        for (IProductCmptLink link : links) {
            if (link.findAssociation(ipsProject).equals(association)
                    && link.getTarget().equals(target.getQualifiedName())) {
                return false;
            }
        }
        return true;
    }

    private ProductCmptLink newLinkInternal(String id, IProductCmptLink insertBefore) {
        ProductCmptLink newRelation = new ProductCmptLink(this, id);
        if (insertBefore == null) {
            links.add(newRelation);
        } else {
            int index = links.indexOf(insertBefore);
            if (index == -1) {
                links.add(newRelation);
            } else {
                links.add(index, newRelation);
            }
        }
        return newRelation;
    }

    private ProductCmptLink newLinkInternal(String id) {
        return newLinkInternal(id, null);
    }

    @Override
    public boolean moveLink(IProductCmptLink toMove, IProductCmptLink target, boolean before) {
        // if toMove and target are the same we have to do nothing
        if (toMove == target) {
            return true;
        }
        if (toMove == null || target == null) {
            return false;
        }
        if (!links.contains(target)) {
            return false;
        }
        boolean removed = links.remove(toMove);
        if (!removed) {
            return false;
        }
        int index = links.indexOf(target);
        if (!before) {
            index++;
        }
        links.add(index, toMove);
        toMove.setAssociation(target.getAssociation());
        objectHasChanged();
        return true;
    }

    @Override
    public ITableContentUsage newTableContentUsage() {
        return newTableContentUsage(null);
    }

    @Override
    public ITableContentUsage newTableContentUsage(ITableStructureUsage structureUsage) {
        ITableContentUsage newUsage = newTableContentUsageInternal(getNextPartId(), structureUsage);
        objectHasChanged();
        return newUsage;
    }

    @Override
    public int getNumOfTableContentUsages() {
        return getTableContentUsages().length;
    }

    @Override
    public ITableContentUsage[] getTableContentUsages() {
        return tableContentUsages.toArray(new ITableContentUsage[tableContentUsages.size()]);
    }

    private ITableContentUsage newTableContentUsageInternal(String id, ITableStructureUsage structureUsage) {
        ITableContentUsage retValue = new TableContentUsage(this, id,
                structureUsage == null ? "" : structureUsage.getRoleName()); //$NON-NLS-1$
        tableContentUsages.add(retValue);
        return retValue;
    }

    /**
     * Returns true if the generation contains a formula config element, otherwise false.
     */
    public boolean containsFormula() {
        return formulas.size() > 0;
    }

    @Override
    public int getNumOfFormulas() {
        return formulas.size();
    }

    @Override
    public IFormula[] getFormulas() {
        return formulas.toArray(new IFormula[formulas.size()]);
    }

    @Override
    public IFormula getFormula(String formulaName) {
        if (formulaName == null) {
            return null;
        }
        for (IFormula formula : formulas) {
            if (formulaName.equals(formula.getName())) {
                return formula;
            }
        }
        return null;
    }

    @Override
    public IFormula newFormula() {
        return newFormula(null);
    }

    @Override
    public IFormula newFormula(IProductCmptTypeMethod signature) {
        IFormula newFormula = newFormulaInternal(getNextPartId(), signature);
        objectHasChanged();
        return newFormula;
    }

    private IFormula newFormulaInternal(String id, IProductCmptTypeMethod signature) {
        IFormula newFormula = new Formula(this, id, signature == null ? "" : signature.getFormulaName()); //$NON-NLS-1$
        formulas.add(newFormula);
        return newFormula;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        int size = getNumOfAttributeValues() + configElements.size() + tableContentUsages.size() + formulas.size()
                + links.size();
        List<IIpsElement> children = new ArrayList<IIpsElement>(size);
        children.addAll(attributeValueContainer.getAttributeValues());
        children.addAll(configElements);
        children.addAll(tableContentUsages);
        children.addAll(formulas);
        children.addAll(links);
        children.addAll(validationRules);
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType.equals(IAttributeValue.class)) {
            return attributeValueContainer.newPartThis(partType, getNextPartId());
        } else if (partType.equals(IConfigElement.class)) {
            return newConfigElement();
        } else if (partType.equals(IPolicyCmptTypeAssociation.class)) {
            return newLink();
        } else if (partType.equals(ITableContentUsage.class)) {
            return newTableContentUsage();
        } else if (partType.equals(IFormula.class)) {
            return newFormula();
        } else if (partType.equals(IValidationRuleConfig.class)) {
            return newValidationRuleConfig();
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(AttributeValue.TAG_NAME)) {
            return attributeValueContainer.newPartThis(xmlTag, id);
        } else if (xmlTagName.equals(ConfigElement.TAG_NAME)) {
            return newConfigElementInternal(id, null);
        } else if (xmlTagName.equals(IProductCmptLink.TAG_NAME)) {
            return newLinkInternal(id);
        } else if (xmlTagName.equals(ITableContentUsage.TAG_NAME)) {
            return newTableContentUsageInternal(id, null);
        } else if (xmlTagName.equals(Formula.TAG_NAME)) {
            return newFormulaInternal(id, null);
        } else if (xmlTagName.equals(IValidationRuleConfig.TAG_NAME)) {
            return newValidationRuleInternal(id, null);
        }
        return null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            attributeValueContainer.addPartThis(part);
            return true;
        } else if (part instanceof IConfigElement) {
            configElements.add((IConfigElement)part);
            return true;
        } else if (part instanceof IProductCmptLink) {
            links.add((IProductCmptLink)part);
            return true;
        } else if (part instanceof ITableContentUsage) {
            tableContentUsages.add((ITableContentUsage)part);
            return true;
        } else if (part instanceof IFormula) {
            formulas.add((IFormula)part);
            return true;
        } else if (part instanceof IValidationRuleConfig) {
            validationRules.add((IValidationRuleConfig)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            attributeValueContainer.removePartThis(part);
            return true;
        } else if (part instanceof IConfigElement) {
            configElements.remove(part);
            return true;
        } else if (part instanceof IProductCmptLink) {
            links.remove(part);
            return true;
        } else if (part instanceof ITableContentUsage) {
            tableContentUsages.remove(part);
            return true;
        } else if (part instanceof IFormula) {
            formulas.remove(part);
            return true;
        } else if (part instanceof IValidationRuleConfig) {
            validationRules.remove(part);
            return true;
        }

        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        attributeValueContainer.clear();
        configElements.clear();
        links.clear();
        tableContentUsages.clear();
        formulas.clear();
        validationRules.clear();
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = getProductCmpt().findProductCmptType(ipsProject);
        // no type information available, so no further validation possible
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TEMPLATE, Messages.ProductCmptGeneration_msgTemplateNotFound,
                    Message.ERROR, this));
            return;
        }

        IGenerationToTypeDelta delta = computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        for (IDeltaEntry entrie : entries) {
            if (entrie.getDeltaType() == DeltaType.MISSING_PROPERTY_VALUE) {
                String text = NLS.bind(Messages.ProductCmptGeneration_msgAttributeWithMissingConfigElement,
                        ((IDeltaEntryForProperty)entrie).getDescription());
                list.add(new Message(MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT, text, Message.WARNING, this));
            }
        }

        new AssociationsValidator(ipsProject, list).start(type);

        IIpsProjectProperties props = getIpsProject().getReadOnlyProperties();
        if (props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()) {
            validateIfReferencedProductComponentsAreValidOnThisGenerationsValidFromDate(list, ipsProject);
        }
    }

    protected void validateIfReferencedProductComponentsAreValidOnThisGenerationsValidFromDate(MessageList msgList,
            IIpsProject ipsProject) throws CoreException {

        IProductCmptLink[] links = getLinks();
        for (IProductCmptLink link : links) {
            IAssociation association = link.findAssociation(ipsProject);
            // associations of type association will be excluded from this constraint. If the type
            // of the association
            // cannot be determined then the link will not be evaluated
            if (association == null || association.isAssoziation()) {
                continue;
            }
            IProductCmpt productCmpt = link.findTarget(ipsProject);
            if (productCmpt != null) {
                if (getValidFrom() != null && productCmpt.findGenerationEffectiveOn(getValidFrom()) == null) {
                    String dateString = IpsPlugin.getDefault().getIpsPreferences().getDateFormat()
                            .format(getValidFrom().getTime());
                    String generationName = IpsPlugin.getDefault().getIpsPreferences()
                            .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
                    String text = NLS.bind(
                            Messages.ProductCmptGeneration_msgNoGenerationInLinkedTargetForEffectiveDate, new Object[] {
                                    productCmpt.getQualifiedName(), generationName, dateString });
                    msgList.add(new Message(MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE, text, Message.ERROR, link));
                }
            }
        }
    }

    @Override
    public ITableContentUsage getTableContentUsage(String rolename) {
        if (rolename == null) {
            return null;
        }
        for (ITableContentUsage element : tableContentUsages) {
            if (rolename.equals(element.getStructureUsage())) {
                return element;
            }
        }
        return null;
    }

    @Override
    public int getNumOfValidationRules() {
        return validationRules.size();
    }

    @Override
    public IValidationRuleConfig getValidationRuleConfig(String validationRuleName) {
        if (validationRuleName == null) {
            return null;
        }
        for (IValidationRuleConfig ruleConfig : validationRules) {
            if (ruleConfig.getName().equals(validationRuleName)) {
                return ruleConfig;
            }
        }
        return null;
    }

    @Override
    public List<IValidationRuleConfig> getValidationRuleConfigs() {
        return new ArrayList<IValidationRuleConfig>(validationRules);
    }

    @Override
    public IValidationRuleConfig newValidationRuleConfig() {
        IValidationRuleConfig ruleConfig = newValidationRuleInternal(getNextPartId(), null);
        objectHasChanged();
        return ruleConfig;
    }

    /**
     * Creates a new inactive {@link ValidationRuleConfig} for this generation.
     * 
     * @param id the part-ID to be assigned to the new validation rule
     * 
     * @return new validation rule
     */
    private IValidationRuleConfig newValidationRuleInternal(String id, IValidationRule ruleToBeConfigured) {
        IValidationRuleConfig ruleConfig = new ValidationRuleConfig(this, id,
                ruleToBeConfigured != null ? ruleToBeConfigured.getName() : ""); //$NON-NLS-1$
        ruleConfig.setActive(ruleToBeConfigured != null ? ruleToBeConfigured.isActivatedByDefault() : false);
        validationRules.add(ruleConfig);
        return ruleConfig;
    }

    @Override
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured) {
        IValidationRuleConfig ruleConfig = newValidationRuleInternal(getNextPartId(), ruleToBeConfigured);
        objectHasChanged();
        return ruleConfig;
    }

    class AssociationsValidator extends TypeHierarchyVisitor<IProductCmptType> {

        private final MessageList list;

        public AssociationsValidator(IIpsProject ipsProject, MessageList list) {
            super(ipsProject);
            this.list = list;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            List<IAssociation> associations = currentType.getAssociations();
            for (IAssociation association : associations) {
                if (association.isDerivedUnion()) {
                    continue;
                }
                IProductCmptLink[] relations = getLinks(association.getTargetRoleSingular());

                // get all messages for the relation types and add them
                MessageList relMessages = association.validate(ipsProject);
                if (!relMessages.isEmpty()) {
                    list.add(relMessages, new ObjectProperty(association.getTargetRoleSingular(), null), true);
                }

                if (association.getMinCardinality() > relations.length) {
                    String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
                    Object[] params = { new Integer(relations.length), associationLabel,
                            new Integer(association.getMinCardinality()) };
                    String msg = NLS.bind(Messages.ProductCmptGeneration_msgNotEnoughRelations, params);
                    ObjectProperty prop1 = new ObjectProperty(this, null);
                    ObjectProperty prop2 = new ObjectProperty(association.getTargetRoleSingular(), null);
                    list.add(new Message(MSGCODE_NOT_ENOUGH_RELATIONS, msg, Message.ERROR, new ObjectProperty[] {
                            prop1, prop2 }));
                }

                int maxCardinality = association.getMaxCardinality();
                if (maxCardinality < relations.length) {
                    String associationLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
                    Object[] params = { new Integer(relations.length), "" + maxCardinality, associationLabel }; //$NON-NLS-1$
                    String msg = NLS.bind(Messages.ProductCmptGeneration_msgTooManyRelations, params);
                    ObjectProperty prop1 = new ObjectProperty(this, null);
                    ObjectProperty prop2 = new ObjectProperty(association.getTargetRoleSingular(), null);
                    list.add(new Message(MSGCODE_TOO_MANY_RELATIONS, msg, Message.ERROR, new ObjectProperty[] { prop1,
                            prop2 }));
                }

                Set<String> targets = new HashSet<String>();
                String msg = null;
                for (IProductCmptLink relation : relations) {
                    String target = relation.getTarget();
                    if (!targets.add(target)) {
                        if (msg == null) {
                            String associationLabel = IpsPlugin.getMultiLanguageSupport()
                                    .getLocalizedLabel(association);
                            msg = NLS.bind(Messages.ProductCmptGeneration_msgDuplicateTarget, associationLabel, target);
                        }
                        list.add(new Message(MSGCODE_DUPLICATE_RELATION_TARGET, msg, Message.ERROR, association
                                .getTargetRoleSingular()));
                    }
                }
            }

            return true;
        }

    }

}
