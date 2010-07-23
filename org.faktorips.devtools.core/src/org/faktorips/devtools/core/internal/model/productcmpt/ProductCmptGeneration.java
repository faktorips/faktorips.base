/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

public class ProductCmptGeneration extends IpsObjectGeneration implements IProductCmptGeneration {

    private List<IAttributeValue> attributeValues = new ArrayList<IAttributeValue>(0);

    private List<IConfigElement> configElements = new ArrayList<IConfigElement>(0);

    private List<IProductCmptLink> links = new ArrayList<IProductCmptLink>(0);

    private List<ITableContentUsage> tableContentUsages = new ArrayList<ITableContentUsage>(0);

    private List<IFormula> formulas = new ArrayList<IFormula>(0);

    public ProductCmptGeneration(ITimedIpsObject ipsObject, String id) {
        super(ipsObject, id);
    }

    public ProductCmptGeneration() {
        // default constructor
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
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfAttributeValues() + getNumOfConfigElements() + getNumOfLinks()
                + getNumOfTableContentUsages() + getNumOfFormulas();
        List<IIpsElement> childrenList = new ArrayList<IIpsElement>(numOfChildren);
        childrenList.addAll(attributeValues);
        childrenList.addAll(configElements);
        childrenList.addAll(tableContentUsages);
        childrenList.addAll(formulas);
        childrenList.addAll(links);
        return childrenList.toArray(new IIpsElement[childrenList.size()]);
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
    public IPropertyValue getPropertyValue(IProdDefProperty property) {
        if (property == null) {
            return null;
        }
        ProdDefPropertyType type = property.getProdDefPropertyType();
        if (type.equals(ProdDefPropertyType.VALUE)) {
            return getAttributeValue(property.getPropertyName());
        }
        if (type.equals(ProdDefPropertyType.TABLE_CONTENT_USAGE)) {
            return getTableContentUsage(property.getPropertyName());
        }
        if (type.equals(ProdDefPropertyType.FORMULA)) {
            return getFormula(property.getPropertyName());
        }
        if (type.equals(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET)) {
            return getConfigElement(property.getPropertyName());
        }
        throw new RuntimeException("Unknown property type " + type); //$NON-NLS-1$
    }

    @Override
    public IPropertyValue getPropertyValue(String propertyName) {
        if (propertyName == null) {
            return null;
        }
        IPropertyValue value = getAttributeValue(propertyName);
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
        return getConfigElement(propertyName);
    }

    @Override
    public IPropertyValue[] getPropertyValues(ProdDefPropertyType type) {
        if (type == null) {
            return new IPropertyValue[0];
        }
        if (ProdDefPropertyType.VALUE.equals(type)) {
            return attributeValues.toArray(new IPropertyValue[attributeValues.size()]);
        }
        if (ProdDefPropertyType.TABLE_CONTENT_USAGE.equals(type)) {
            return tableContentUsages.toArray(new IPropertyValue[tableContentUsages.size()]);
        }
        if (ProdDefPropertyType.FORMULA.equals(type)) {
            return formulas.toArray(new IPropertyValue[formulas.size()]);
        }
        if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(type)) {
            return configElements.toArray(new IPropertyValue[configElements.size()]);
        }
        throw new RuntimeException("Unknown type " + type); //$NON-NLS-1$
    }

    @Override
    public IPropertyValue newPropertyValue(IProdDefProperty property) {
        ProdDefPropertyType type = property.getProdDefPropertyType();
        if (ProdDefPropertyType.VALUE.equals(type)) {
            return newAttributeValue((IProductCmptTypeAttribute)property);
        }
        if (ProdDefPropertyType.TABLE_CONTENT_USAGE.equals(type)) {
            return newTableContentUsage((ITableStructureUsage)property);
        }
        if (ProdDefPropertyType.FORMULA.equals(type)) {
            return newFormula((IProductCmptTypeMethod)property);
        }
        if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(type)) {
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
        Collections.sort(attributeValues, comparator);
        Collections.sort(configElements, comparator);
        // TODO IProductCmptLink is no IPropertyValue --> cannot sort with this comparator
        // Collections.sort(links, comparator);
        Collections.sort(tableContentUsages, comparator);
        Collections.sort(formulas, comparator);
    }

    @Override
    public IAttributeValue[] getAttributeValues() {
        return attributeValues.toArray(new IAttributeValue[attributeValues.size()]);
    }

    @Override
    public IAttributeValue getAttributeValue(String attribute) {
        if (attribute == null) {
            return null;
        }
        for (IAttributeValue value : attributeValues) {
            if (attribute.equals(value.getAttribute())) {
                return value;
            }
        }
        return null;
    }

    @Override
    public int getNumOfAttributeValues() {
        return attributeValues.size();
    }

    @Override
    public IAttributeValue newAttributeValue() {
        return newAttributeValue(null);
    }

    @Override
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute) {
        IAttributeValue newValue = newAttributeValueInternal(getNextPartId(), attribute,
                attribute == null ? "" : attribute.getDefaultValue()); //$NON-NLS-1$
        objectHasChanged();
        return newValue;
    }

    @Override
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute, String value) {
        IAttributeValue newValue = newAttributeValueInternal(getNextPartId(), attribute, value);
        objectHasChanged();
        return newValue;
    }

    /**
     * Creates a new attribute value without updating the src file.
     */
    private AttributeValue newAttributeValueInternal(String id) {
        AttributeValue av = new AttributeValue(this, id);
        attributeValues.add(av);
        return av;
    }

    /**
     * Creates a new attribute value without updating the src file.
     */
    private AttributeValue newAttributeValueInternal(String id, IProductCmptTypeAttribute attr, String value) {
        AttributeValue av = new AttributeValue(this, id, attr == null ? "" : attr.getName(), value); //$NON-NLS-1$
        attributeValues.add(av);
        return av;
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
    public IIpsObjectPart newPart(Class<?> partType) {
        if (partType.equals(IAttributeValue.class)) {
            return newAttributeValue();
        }
        if (partType.equals(IConfigElement.class)) {
            return newConfigElement();
        } else if (partType.equals(IPolicyCmptTypeAssociation.class)) {
            return newLink();
        } else if (partType.equals(ITableContentUsage.class)) {
            return newTableContentUsage();
        } else if (partType.equals(IFormula.class)) {
            return newFormula();
        }
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(AttributeValue.TAG_NAME)) {
            return newAttributeValueInternal(id);
        } else if (xmlTagName.equals(ConfigElement.TAG_NAME)) {
            return newConfigElementInternal(id, null);
        } else if (xmlTagName.equals(IProductCmptLink.TAG_NAME)) {
            return newLinkInternal(id);
        } else if (xmlTagName.equals(ITableContentUsage.TAG_NAME)) {
            return newTableContentUsageInternal(id, null);
        } else if (xmlTagName.equals(Formula.TAG_NAME)) {
            return newFormulaInternal(id, null);
        }
        throw new RuntimeException("Could not create part for tag " + xmlTagName); //$NON-NLS-1$
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            attributeValues.add((IAttributeValue)part);
            return;
        } else if (part instanceof IConfigElement) {
            configElements.add((IConfigElement)part);
            return;
        } else if (part instanceof IProductCmptLink) {
            links.add((IProductCmptLink)part);
            return;
        } else if (part instanceof ITableContentUsage) {
            tableContentUsages.add((ITableContentUsage)part);
            return;
        } else if (part instanceof IFormula) {
            formulas.add((IFormula)part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            attributeValues.remove(part);
            return;
        } else if (part instanceof IConfigElement) {
            configElements.remove(part);
            return;
        } else if (part instanceof IProductCmptLink) {
            links.remove(part);
            return;
        } else if (part instanceof ITableContentUsage) {
            tableContentUsages.remove(part);
            return;
        } else if (part instanceof IFormula) {
            formulas.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    @Override
    protected void reinitPartCollections() {
        attributeValues.clear();
        configElements.clear();
        links.clear();
        tableContentUsages.clear();
        formulas.clear();
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
                        ((IDeltaEntryForProperty)entrie).getPropertyName());
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
                    String dateString = IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(
                            getValidFrom().getTime());
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

    class AssociationsValidator extends ProductCmptTypeHierarchyVisitor {

        private final MessageList list;

        public AssociationsValidator(IIpsProject ipsProject, MessageList list) {
            super(ipsProject);
            this.list = list;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {

            IAssociation[] associations = currentType.getAssociations();
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
                    Object[] params = { new Integer(relations.length), association.getTargetRoleSingular(),
                            new Integer(association.getMinCardinality()) };
                    String msg = NLS.bind(Messages.ProductCmptGeneration_msgNotEnoughRelations, params);
                    ObjectProperty prop1 = new ObjectProperty(this, null);
                    ObjectProperty prop2 = new ObjectProperty(association.getTargetRoleSingular(), null);
                    list.add(new Message(MSGCODE_NOT_ENOUGH_RELATIONS, msg, Message.ERROR, new ObjectProperty[] {
                            prop1, prop2 }));
                }

                int maxCardinality = association.getMaxCardinality();
                if (maxCardinality < relations.length) {
                    Object[] params = { new Integer(relations.length),
                            "" + maxCardinality, association.getTargetRoleSingular() }; //$NON-NLS-1$
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
                            msg = NLS.bind(Messages.ProductCmptGeneration_msgDuplicateTarget, association
                                    .getTargetRoleSingular(), target);
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
