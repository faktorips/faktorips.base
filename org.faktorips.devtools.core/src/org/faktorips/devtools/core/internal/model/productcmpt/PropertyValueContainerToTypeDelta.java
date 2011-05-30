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
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.AbstractFixDifferencesComposite;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.PropertyTypeMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ValueSetMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of IProductCmptToTypeDelta.
 * 
 * @author Jan Ortmann
 */
public abstract class PropertyValueContainerToTypeDelta extends AbstractFixDifferencesComposite implements
        IPropertyValueContainerToTypeDelta {

    private final IIpsProject ipsProject;
    private final IPropertyValueContainer propertyValueContainer;
    private final IProductCmptType productCmptType;
    private final List<IDeltaEntry> entries = new ArrayList<IDeltaEntry>();

    public PropertyValueContainerToTypeDelta(IPropertyValueContainer propertyValueContainer, IIpsProject ipsProject)
            throws CoreException {
        ArgumentCheck.notNull(propertyValueContainer);
        ArgumentCheck.notNull(ipsProject);
        this.propertyValueContainer = propertyValueContainer;
        this.ipsProject = ipsProject;
        productCmptType = propertyValueContainer.findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return;
        }
        createEntriesForProperties();
        createAdditionalEntriesAndChildren();
    }

    @Override
    public IIpsElement getCorrespondingIpsElement() {
        return propertyValueContainer;
    }

    /**
     * Adding additional entries or children to this delta element. This method is called at the end
     * of the constructor.
     * 
     * @throws CoreException May throw any core exception
     */
    protected abstract void createAdditionalEntriesAndChildren() throws CoreException;

    private void createEntriesForProperties() throws CoreException {
        for (ProductCmptPropertyType propertyType : ProductCmptPropertyType.values()) {
            LinkedHashMap<String, IProductCmptProperty> propertiesMap = ((ProductCmptType)productCmptType)
                    .getProductCpmtPropertyMap(propertyType, getIpsProject());
            checkForMissingPropertyValues(propertiesMap);
            checkForInconsistentPropertyValues(propertiesMap, propertyType);
        }
    }

    private void checkForMissingPropertyValues(LinkedHashMap<String, IProductCmptProperty> propertiesMap) {
        for (IProductCmptProperty property : propertiesMap.values()) {
            if (isRelevantProperty(property) && propertyValueContainer.getPropertyValue(property) == null) {
                // no value found for the property with the given type, but we might have a type
                // mismatch
                if (propertyValueContainer.getPropertyValue(property.getPropertyName()) == null) {
                    MissingPropertyValueEntry missingPropertyValueEntry = new MissingPropertyValueEntry(
                            propertyValueContainer, property);
                    addEntry(missingPropertyValueEntry);
                }
                // we create the entry for the type mismatch in checkForInconsistentPropertyValues()
                // if we created it here, too, we would create two entries for the same aspect
            }
        }
    }

    protected boolean isRelevantProperty(IProductCmptProperty property) {
        return property.isChangingOverTime() == propertyValueContainer.isChangingOverTimeContainer();
    }

    private void checkForInconsistentPropertyValues(LinkedHashMap<String, IProductCmptProperty> propertiesMap,
            ProductCmptPropertyType propertyType) throws CoreException {
        List<? extends IPropertyValue> values = propertyValueContainer.getPropertyValues(propertyType.getValueClass());
        for (IPropertyValue value : values) {
            IProductCmptProperty property = propertiesMap.get(value.getPropertyName());
            if (property == null) {
                // the map contains only properties for the current property type
                // so we have to search if the property exists with a different type.
                IProductCmptProperty property2 = productCmptType.findProductCmptProperty(value.getPropertyName(),
                        getIpsProject());
                if (property2 != null) {
                    // property2 must have a different type, otherwise it would have been in the
                    // property map!
                    PropertyTypeMismatchEntry propertyTypeMismatchEntry = new PropertyTypeMismatchEntry(
                            propertyValueContainer, property2, value);
                    addEntry(propertyTypeMismatchEntry);
                } else {
                    ValueWithoutPropertyEntry valueWithoutPropertyEntry = new ValueWithoutPropertyEntry(value);
                    addEntry(valueWithoutPropertyEntry);
                }
            } else {
                if (!isRelevantProperty(property)) {
                    // the relevance changed (changingOverTime selected or unselected)
                    ValueWithoutPropertyEntry deltaEntry = new ValueWithoutPropertyEntry(value);
                    addEntry(deltaEntry);
                }
                if (ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(propertyType)) {
                    checkForValueSetMismatch((IPolicyCmptTypeAttribute)property, (IConfigElement)value);
                }
            }
        }
    }

    private void checkForValueSetMismatch(IPolicyCmptTypeAttribute attribute, IConfigElement element) {
        if (attribute.getValueSet().isUnrestricted()) {
            return;
        }
        if (!element.getValueSet().isSameTypeOfValueSet(attribute.getValueSet())) {
            ValueSetMismatchEntry valueSetMismatchEntry = new ValueSetMismatchEntry(attribute, element);
            addEntry(valueSetMismatchEntry);
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
        return entries.size() == 0;
    }

    @Override
    public IDeltaEntry[] getEntries() {
        return entries.toArray(new IDeltaEntry[entries.size()]);
    }

    @Override
    public IDeltaEntry[] getEntries(DeltaType type) {
        List<IDeltaEntry> result = new ArrayList<IDeltaEntry>(entries.size());
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

        List<ITableStructureUsage> tableStructureUsages = new ArrayList<ITableStructureUsage>();
        List<IProductCmptTypeAttribute> attributes = new ArrayList<IProductCmptTypeAttribute>();

        public HierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
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
