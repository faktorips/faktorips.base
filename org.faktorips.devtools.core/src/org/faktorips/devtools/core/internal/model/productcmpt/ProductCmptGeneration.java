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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

public class ProductCmptGeneration extends IpsObjectGeneration implements IProductCmptGeneration {

    private final PropertyValueCollection propertyValueCollection = new PropertyValueCollection();

    private List<IProductCmptLink> links = new ArrayList<IProductCmptLink>(0);

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
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmpt().findProductCmptType(ipsProject);
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmpt().findPolicyCmptType(ipsProject);
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
        return propertyValueCollection.getPropertyValue(property);
    }

    @Override
    public boolean hasPropertyValue(IProductCmptProperty property) {
        return getPropertyValue(property) != null;
    }

    @Override
    public IPropertyValue getPropertyValue(String propertyName) {
        return propertyValueCollection.getPropertyValue(propertyName);
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
    public IPropertyValue newPropertyValue(IProductCmptProperty property) {
        IPropertyValue newPropertyValue = propertyValueCollection.newPropertyValue(this, property, getNextPartId());
        objectHasChanged();
        return newPropertyValue;
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
        return propertyValueCollection.getPropertyValue(IAttributeValue.class, attribute);
    }

    @Override
    public int getNumOfAttributeValues() {
        return getAttributeValues().length;
    }

    @Override
    public IAttributeValue newAttributeValue() {
        IAttributeValue value = propertyValueCollection.newPropertyValue(this, getNextPartId(), IAttributeValue.class);
        objectHasChanged();
        return value;
    }

    @Override
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute) {
        IAttributeValue newPropertyValue = propertyValueCollection.newPropertyValue(this, attribute, getNextPartId(),
                IAttributeValue.class);
        objectHasChanged();
        return newPropertyValue;
    }

    @Override
    @Deprecated
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute, String value) {
        IAttributeValue attrValue = newAttributeValue(attribute);
        attrValue.setValue(value);
        return attrValue;
    }

    @Override
    public IConfigElement[] getConfigElements() {
        List<IConfigElement> configElements = propertyValueCollection.getPropertyValues(IConfigElement.class);
        return configElements.toArray(new IConfigElement[configElements.size()]);
    }

    @Override
    public IConfigElement getConfigElement(String attributeName) {
        return propertyValueCollection.getPropertyValue(IConfigElement.class, attributeName);
    }

    @Override
    public int getNumOfConfigElements() {
        return getConfigElements().length;
    }

    @Override
    public IConfigElement newConfigElement() {
        IConfigElement value = propertyValueCollection.newPropertyValue(this, getNextPartId(), IConfigElement.class);
        objectHasChanged();
        return value;
    }

    @Override
    public IConfigElement newConfigElement(IPolicyCmptTypeAttribute attribute) {
        IConfigElement newElement = newConfigElementInternal(attribute, getNextPartId());
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
     * Creates a new config element without updating the src file.
     */
    private IConfigElement newConfigElementInternal(IPolicyCmptTypeAttribute attribute, String id) {
        IConfigElement e = propertyValueCollection.newPropertyValue(this, attribute, id, IConfigElement.class);
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
        ITableContentUsage value = propertyValueCollection.newPropertyValue(this, getNextPartId(),
                ITableContentUsage.class);
        objectHasChanged();
        return value;
    }

    @Override
    public ITableContentUsage newTableContentUsage(ITableStructureUsage structureUsage) {
        ITableContentUsage tableUsage = propertyValueCollection.newPropertyValue(this, structureUsage, getNextPartId(),
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
        return propertyValueCollection.getPropertyValue(IFormula.class, formulaName);
    }

    @Override
    public IFormula newFormula() {
        IFormula value = propertyValueCollection.newPropertyValue(this, getNextPartId(), IFormula.class);
        objectHasChanged();
        return value;
    }

    @Override
    public IFormula newFormula(IProductCmptTypeMethod signature) {
        IFormula newPropertyValue = propertyValueCollection.newPropertyValue(this, signature, getNextPartId(),
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
        children.addAll(links);
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        IIpsObjectPart newPart = null;
        if (IPolicyCmptTypeAssociation.class.isAssignableFrom(partType)) {
            newPart = newLinkInternal(getNextPartId());
        } else if (IPropertyValue.class.isAssignableFrom(partType)) {
            Class<? extends IPropertyValue> propertyValueType = partType.asSubclass(IPropertyValue.class);
            newPart = propertyValueCollection.newPropertyValue(this, getNextPartId(), propertyValueType);
        }
        return newPart;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(IProductCmptLink.TAG_NAME)) {
            ProductCmptLink newLinkInternal = newLinkInternal(id);
            return newLinkInternal;
        } else {
            IIpsObjectPart newPartThis = propertyValueCollection.newPropertyValue(this, xmlTagName, getNextPartId());
            return newPartThis;
        }
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        boolean result = false;
        if (part instanceof IProductCmptLink) {
            result = links.add((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            result = propertyValueCollection.addPropertyValue((IPropertyValue)part);
        }
        return result;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptLink) {
            return links.remove(part);
        } else if (part instanceof IPropertyValue) {
            return propertyValueCollection.removePropertyValue((IPropertyValue)part);
        } else {
            return false;
        }
    }

    @Override
    protected void reinitPartCollectionsThis() {
        propertyValueCollection.clear();
        links.clear();
    }

    @Override
    public boolean isContainerFor(IProductCmptProperty property) {
        return property.isChangingOverTime();
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

        IPropertyValueContainerToTypeDelta delta = computeDeltaToModel(ipsProject);
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
        return propertyValueCollection.getPropertyValue(ITableContentUsage.class, rolename);
    }

    @Override
    public int getNumOfValidationRules() {
        return getValidationRuleConfigs().size();
    }

    @Override
    public IValidationRuleConfig getValidationRuleConfig(String validationRuleName) {
        return propertyValueCollection.getPropertyValue(IValidationRuleConfig.class, validationRuleName);
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
        return propertyValueCollection.newPropertyValue(this, ruleToBeConfigured, getNextPartId(),
                IValidationRuleConfig.class);
    }

    @Override
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured) {
        IValidationRuleConfig ruleConfig = newValidationRuleInternal(ruleToBeConfigured, getNextPartId());
        objectHasChanged();
        return ruleConfig;
    }

    private class AssociationsValidator extends TypeHierarchyVisitor<IProductCmptType> {

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

    @Override
    public String getProductCmptType() {
        return getProductCmpt().getProductCmptType();
    }

}
