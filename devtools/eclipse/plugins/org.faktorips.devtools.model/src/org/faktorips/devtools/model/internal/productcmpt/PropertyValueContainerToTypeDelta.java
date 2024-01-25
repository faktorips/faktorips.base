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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.AbstractFixDifferencesComposite;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.DatatypeMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.HiddenAttributeMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.InheritedLinkTemplateMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.InheritedPropertyTemplateMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.InheritedUndefinedLinkTemplateMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.InheritedUndefinedPropertyTemplateMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.LinkChangingOverTimeMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.LinkWithoutAssociationEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.MissingTemplateLinkEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.MultilingualMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.PropertyTypeMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.RemovedTemplateLinkEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.ValueHolderMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.ValueSetMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.WrongRuntimeIdForLinkEntry;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of IProductCmptToTypeDelta.
 *
 * @author Jan Ortmann
 */
public abstract class PropertyValueContainerToTypeDelta extends AbstractFixDifferencesComposite
        implements IPropertyValueContainerToTypeDelta {

    private final IIpsProject ipsProject;
    private final IPropertyValueContainer propertyValueContainer;
    private final IProductCmptType productCmptType;
    private final List<IDeltaEntry> entries = new ArrayList<>();
    private final IProductCmptLinkContainer linkContainer;

    public PropertyValueContainerToTypeDelta(IPropertyValueContainer propertyValueContainer,
            IProductCmptLinkContainer linkContainer, IIpsProject ipsProject) {

        this.linkContainer = linkContainer;
        ArgumentCheck.notNull(propertyValueContainer);
        ArgumentCheck.notNull(ipsProject);

        this.propertyValueContainer = propertyValueContainer;
        this.ipsProject = ipsProject;
        productCmptType = propertyValueContainer.findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return;
        }
        createEntriesForProperties();
        createEntriesForLinks();
        if (getLinkContainer().isUsingTemplate()) {
            createEntriesForTemplateLinks();
        }
        createAdditionalEntriesAndChildren();
    }

    protected void createEntriesForLinks() {
        List<IProductCmptLink> links = getLinkContainer().getLinksAsList();
        for (IProductCmptLink link : links) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)getProductCmptType()
                    .findAssociation(link.getAssociation(), getIpsProject());
            if (association == null) {
                LinkWithoutAssociationEntry linkWithoutAssociationEntry = new LinkWithoutAssociationEntry(link);
                addEntry(linkWithoutAssociationEntry);
            } else if (!link.getProductCmptLinkContainer().isContainerFor(association)) {
                LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(association, link);
                addEntry(entry);
            }
            IProductCmpt target = link.findTarget(ipsProject);
            if (target != null && !Objects.equals(link.getTargetRuntimeId(), target.getRuntimeId())) {
                WrongRuntimeIdForLinkEntry entry = new WrongRuntimeIdForLinkEntry(link);
                addEntry(entry);
            }
        }
    }

    public void createEntriesForTemplateLinks() {
        IProductCmptLinkContainer templateContainer = getLinkContainer().findTemplate(getIpsProject());
        if (templateContainer == null) {
            return;
        }
        for (IProductCmptLink templateLink : templateContainer.getLinksAsList()) {
            if (isLinkAbsent(templateLink, getLinkContainer())) {
                addEntry(new MissingTemplateLinkEntry(templateLink, getLinkContainer()));
            }
        }
        for (IProductCmptLink link : getLinkContainer().getLinksAsList()) {
            if (link.getTemplateValueStatus() != TemplateValueStatus.DEFINED && isLinkAbsent(link, templateContainer)) {
                addEntry(new RemovedTemplateLinkEntry(link));
            }
            checkInheritedFromTemplateMismatch(link);
        }
    }

    private void checkInheritedFromTemplateMismatch(IProductCmptLink link) {
        if (link.getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            Cardinality internalValue = (Cardinality)link.getInternalValueGetter().apply(link);
            IProductCmptLink templatePropertyValue = link.findTemplateProperty(ipsProject);
            if (templatePropertyValue != null) {
                Cardinality templateValue = templatePropertyValue.getCardinality();
                if (internalValue.compareTo(templateValue) != 0) {
                    IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)getProductCmptType()
                            .findAssociation(link.getAssociation(), getIpsProject());
                    InheritedLinkTemplateMismatchEntry valueSetTemplateMismatchEntry = new InheritedLinkTemplateMismatchEntry(
                            association, link, internalValue, templateValue);
                    addEntry(valueSetTemplateMismatchEntry);
                }
            } else {
                IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)getProductCmptType()
                        .findAssociation(link.getAssociation(), getIpsProject());
                InheritedUndefinedLinkTemplateMismatchEntry undefinedLinkTemplateMismatchEntry = new InheritedUndefinedLinkTemplateMismatchEntry(
                        association, link);
                addEntry(undefinedLinkTemplateMismatchEntry);
            }
        }
    }

    private boolean isLinkAbsent(final IProductCmptLink templateLink, IProductCmptLinkContainer container) {
        return templateLink.getTemplateValueStatus() != TemplateValueStatus.UNDEFINED
                && matchingLinkIsMissing(templateLink, container);
    }

    private boolean matchingLinkIsMissing(final IProductCmptLink linkToFind, IProductCmptLinkContainer container) {
        return !container.getLinksAsList(linkToFind.getAssociation()).stream()
                .anyMatch(link -> link != null && Objects.equals(linkToFind.getTarget(), link.getTarget()));
    }

    protected IProductCmptLinkContainer getLinkContainer() {
        return linkContainer;
    }

    @Override
    public IIpsElement getCorrespondingIpsElement() {
        return propertyValueContainer;
    }

    /**
     * Adding additional entries or children to this delta element. This method is called at the end
     * of the constructor.
     *
     * @throws IpsException May throw any core exception
     */
    protected abstract void createAdditionalEntriesAndChildren() throws IpsException;

    private void createEntriesForProperties() {
        Map<String, IProductCmptProperty> propertiesMap = ((ProductCmptType)productCmptType)
                .findProductCmptPropertyMap(getIpsProject());
        checkForMissingPropertyValues(propertiesMap);
        checkForInconsistentPropertyValues(propertiesMap);
    }

    private void checkForMissingPropertyValues(Map<String, IProductCmptProperty> propertiesMap) {
        for (IProductCmptProperty property : propertiesMap.values()) {
            if (propertyValueContainer.isContainerFor(property)
                    && propertyValueContainer.getPropertyValues(property.getPropertyName()).isEmpty()) {
                createMissingEntry(property);
            }
        }
    }

    private void createMissingEntry(IProductCmptProperty property) {
        for (PropertyValueType type : property.getPropertyValueTypes()) {
            MissingPropertyValueEntry missingPropertyValueEntry = new MissingPropertyValueEntry(propertyValueContainer,
                    property, type);
            addEntry(missingPropertyValueEntry);
        }
    }

    private void checkForInconsistentPropertyValues(Map<String, IProductCmptProperty> propertiesMap) {
        List<? extends IPropertyValue> propertyValues = propertyValueContainer.getAllPropertyValues();
        for (IPropertyValue propertyValue : propertyValues) {
            IProductCmptProperty property = propertiesMap.get(propertyValue.getPropertyName());
            if (property == null || !propertyValueContainer.isContainerFor(property)) {
                ValueWithoutPropertyEntry valueWithoutPropertyEntry = new ValueWithoutPropertyEntry(propertyValue);
                addEntry(valueWithoutPropertyEntry);
            } else {
                PropertyValueType propertyValueType = propertyValue.getPropertyValueType();
                checkInheritedFromTemplateMismatch(propertyValueType, property, propertyValue);
                if (!property.getPropertyValueTypes().contains(propertyValueType)) {
                    PropertyTypeMismatchEntry propertyTypeMismatchEntry = new PropertyTypeMismatchEntry(
                            propertyValueContainer, property, propertyValue);
                    addEntry(propertyTypeMismatchEntry);
                } else {
                    if (PropertyValueType.CONFIGURED_VALUESET.equals(propertyValueType)) {
                        checkForValueSetMismatch((IPolicyCmptTypeAttribute)property,
                                (IConfiguredValueSet)propertyValue);
                    }
                    if (PropertyValueType.ATTRIBUTE_VALUE.equals(propertyValueType)) {
                        checkForValueMismatch((IProductCmptTypeAttribute)property, (IAttributeValue)propertyValue);
                        checkForMultilingualMismatch((IProductCmptTypeAttribute)property,
                                (IAttributeValue)propertyValue);
                        checkForHiddenAttributeMismatch((IProductCmptTypeAttribute)property,
                                (IAttributeValue)propertyValue);
                    }
                }
            }
        }
        entries.addAll(DatatypeMismatchEntry.forEachMismatch(propertyValues));
    }

    private void checkInheritedFromTemplateMismatch(PropertyValueType propertyValueType,
            IProductCmptProperty property,
            IPropertyValue propertyValue) {
        if (propertyValue.getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            Object internalValue = propertyValueType.getInternalValueGetter().apply(propertyValue);
            IPropertyValue templatePropertyValue = propertyValue.findTemplateProperty(ipsProject);
            if (templatePropertyValue != null) {
                Object templateValue = propertyValueType.getValueGetter().apply(templatePropertyValue);
                Comparator<Object> valueComparator = propertyValueType.getValueComparator();
                if (valueComparator.compare(internalValue, templateValue) != 0) {
                    InheritedPropertyTemplateMismatchEntry valueSetTemplateMismatchEntry = new InheritedPropertyTemplateMismatchEntry(
                            property, propertyValue, templatePropertyValue, internalValue, templateValue);
                    addEntry(valueSetTemplateMismatchEntry);
                }
            } else {
                InheritedUndefinedPropertyTemplateMismatchEntry undefinedPropertyTemplateMismatchEntry = new InheritedUndefinedPropertyTemplateMismatchEntry(
                        property, propertyValue);
                addEntry(undefinedPropertyTemplateMismatchEntry);
            }
        }
    }

    private void checkForValueSetMismatch(IPolicyCmptTypeAttribute attribute, IConfiguredValueSet element) {
        IValueSet valueSet = element.getValueSet();
        if (attribute.getValueSet().isUnrestricted() || attribute.getValueSet().isDerived()
                || attribute.getValueSet().isStringLength()
                || element.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return;
        }
        if (!valueSet.isSameTypeOfValueSet(attribute.getValueSet())) {
            ValueSetMismatchEntry valueSetMismatchEntry = new ValueSetMismatchEntry(attribute, element);
            addEntry(valueSetMismatchEntry);
        }
    }

    /* private */void checkForValueMismatch(IProductCmptTypeAttribute attribute, IAttributeValue value) {
        if (attribute.isMultiValueAttribute() != (value.getValueHolder().isMultiValue())) {
            addEntry(new ValueHolderMismatchEntry(value, attribute));
        }
    }

    private void checkForMultilingualMismatch(IProductCmptTypeAttribute attribute, IAttributeValue value) {
        if (attribute
                .isMultilingual() != (value.getValueHolder().getValueType().equals(ValueType.INTERNATIONAL_STRING))) {
            addEntry(new MultilingualMismatchEntry(value, attribute));
        }
    }

    protected void checkForHiddenAttributeMismatch(IProductCmptTypeAttribute attribute, IAttributeValue value) {
        HiddenAttributeMismatchEntry mismatchEntry = new HiddenAttributeMismatchEntry(value, attribute);
        if (mismatchEntry.isMismatch()) {
            addEntry(mismatchEntry);
        }
    }

    /**
     * Adding the {@link IDeltaEntry} to the list of delta entries.
     *
     * @param newEntry The {@link IDeltaEntry} you want to add to this delta container
     */
    protected void addEntry(IDeltaEntry newEntry) {
        entries.add(newEntry);
    }

    @Override
    public IPropertyValueContainer getPropertyValueContainer() {
        return propertyValueContainer;
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    @Override
    protected boolean isEmptyThis() {
        return entries.isEmpty();
    }

    @Override
    public IDeltaEntry[] getEntries() {
        return entries.toArray(new IDeltaEntry[entries.size()]);
    }

    @Override
    public IDeltaEntry[] getEntries(DeltaType type) {
        List<IDeltaEntry> result = new ArrayList<>(entries.size());
        for (IDeltaEntry entry : entries) {
            if (entry.getDeltaType().equals(type)) {
                result.add(entry);
            }
        }
        return result.toArray(new IDeltaEntry[result.size()]);
    }

    @Override
    protected void fix() {
        for (IDeltaEntry entry : entries) {
            entry.fix();
        }
    }

    /**
     * @return Returns the ipsProject.
     */
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    class HierarchyVisitor extends TypeHierarchyVisitor<IProductCmptType> {

        private List<ITableStructureUsage> tableStructureUsages = new ArrayList<>();
        private List<IProductCmptTypeAttribute> attributes = new ArrayList<>();

        public HierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            List<ITableStructureUsage> tsu = currentType.getTableStructureUsages();
            for (ITableStructureUsage element : tsu) {
                tableStructureUsages.add(element);
            }
            List<IProductCmptTypeAttribute> attr = currentType.getProductCmptTypeAttributes();
            for (IProductCmptTypeAttribute element : attr) {
                attributes.add(element);
            }
            return true;

        }

        boolean containsTableStructureUsage(String rolename) {
            for (ITableStructureUsage tsu : tableStructureUsages) {
                if (tsu.getRoleName().equals(rolename)) {
                    return true;
                }
            }
            return false;
        }

        boolean containsAttribute(String name) {
            for (IProductCmptTypeAttribute attribute : attributes) {
                if (attribute.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }

    }
}
