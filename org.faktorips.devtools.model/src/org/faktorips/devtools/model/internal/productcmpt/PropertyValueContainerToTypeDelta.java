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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.AbstractFixDifferencesComposite;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.DatatypeMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.HiddenAttributeMismatchEntry;
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
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
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
            IProductCmptLinkContainer linkContainer, IIpsProject ipsProject) throws CoreRuntimeException {

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
     * @throws CoreRuntimeException May throw any core exception
     */
    protected abstract void createAdditionalEntriesAndChildren() throws CoreRuntimeException;

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
        List<? extends IPropertyValue> values = propertyValueContainer.getAllPropertyValues();
        for (IPropertyValue value : values) {
            IProductCmptProperty property = propertiesMap.get(value.getPropertyName());
            if (property == null || !propertyValueContainer.isContainerFor(property)) {
                ValueWithoutPropertyEntry valueWithoutPropertyEntry = new ValueWithoutPropertyEntry(value);
                addEntry(valueWithoutPropertyEntry);
            } else {
                if (!property.getPropertyValueTypes().contains(value.getPropertyValueType())) {
                    PropertyTypeMismatchEntry propertyTypeMismatchEntry = new PropertyTypeMismatchEntry(
                            propertyValueContainer, property, value);
                    addEntry(propertyTypeMismatchEntry);
                } else {
                    if (PropertyValueType.CONFIGURED_VALUESET.equals(value.getPropertyValueType())) {
                        checkForValueSetMismatch((IPolicyCmptTypeAttribute)property, (IConfiguredValueSet)value);
                    }
                    if (PropertyValueType.ATTRIBUTE_VALUE.equals(value.getPropertyValueType())) {
                        checkForValueMismatch((IProductCmptTypeAttribute)property, (IAttributeValue)value);
                        checkForMultilingualMismatch((IProductCmptTypeAttribute)property, (IAttributeValue)value);
                        checkForHiddenAttributeMismatch((IProductCmptTypeAttribute)property, (IAttributeValue)value);
                    }
                }
            }
        }
        entries.addAll(DatatypeMismatchEntry.forEachMismatch(values));
    }

    private void checkForValueSetMismatch(IPolicyCmptTypeAttribute attribute, IConfiguredValueSet element) {
        if (attribute.getValueSet().isUnrestricted() || attribute.getValueSet().isStringLength()
                || element.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return;
        }
        if (!element.getValueSet().isSameTypeOfValueSet(attribute.getValueSet())) {
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
