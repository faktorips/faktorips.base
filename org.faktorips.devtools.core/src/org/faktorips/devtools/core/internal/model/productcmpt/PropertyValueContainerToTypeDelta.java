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
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.AbstractDeltaEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.PropertyTypeMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ValueSetMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
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
public class PropertyValueContainerToTypeDelta implements IPropertyValueContainerToTypeDelta {

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
    }

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
            if (propertyValueContainer.getPropertyValue(property) == null) {
                // no value found for the property with the given type, but we might have a type
                // mismatch
                if (propertyValueContainer.getPropertyValue(property.getPropertyName()) == null) {
                    new MissingPropertyValueEntry(this, property);
                }
                // we create the entry for the type mismatch in checkForInconsistentPropertyValues()
                // if we created it here, too, we would create two entries for the same aspect
            }
        }
    }

    private void checkForInconsistentPropertyValues(LinkedHashMap<String, IProductCmptProperty> propertiesMap,
            ProductCmptPropertyType propertyType) throws CoreException {
        List<IPropertyValue> values = propertyValueContainer.getPropertyValues(propertyType);
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
                    new PropertyTypeMismatchEntry(this, property2, value);
                } else {
                    new ValueWithoutPropertyEntry(this, value);
                }
            } else {
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
            new ValueSetMismatchEntry(this, attribute, element);
        }
    }

    /**
     * This method should only be called by {@link AbstractDeltaEntry} !!!
     */
    public void addEntry(IDeltaEntry newEntry) {
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
    public boolean isEmpty() {
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
    public void fix() {
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
