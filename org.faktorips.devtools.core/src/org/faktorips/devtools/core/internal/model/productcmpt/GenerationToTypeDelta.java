/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.AbstractDeltaEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.LinkWithoutAssociationEntry;
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
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of IProductCmptToTypeDelta.
 * 
 * @author Jan Ortmann
 */
public class GenerationToTypeDelta implements IGenerationToTypeDelta {

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;
    private IProductCmptType productCmptType;
    private List<IDeltaEntry> entries = new ArrayList<IDeltaEntry>();

    public GenerationToTypeDelta(IProductCmptGeneration generation, IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(generation);
        ArgumentCheck.notNull(ipsProject);
        this.generation = generation;
        this.ipsProject = ipsProject;
        productCmptType = generation.findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return;
        }
        computeLinksWithMissingAssociations();
        createEntriesForProperties();
    }

    private void computeLinksWithMissingAssociations() throws CoreException {
        IProductCmptLink[] links = generation.getLinks();
        for (int i = 0; i < links.length; i++) {
            if (productCmptType.findAssociation(links[i].getAssociation(), ipsProject) == null) {
                new LinkWithoutAssociationEntry(this, links[i]);
            }
        }
    }

    private void createEntriesForProperties() throws CoreException {
        for (int i = 0; i < ProdDefPropertyType.values().length; i++) {
            ProdDefPropertyType propertyType = ProdDefPropertyType.values()[i];
            LinkedHashMap<String, IProdDefProperty> propertiesMap = ((ProductCmptType)productCmptType)
                    .getProdDefPropertiesMap(propertyType, ipsProject);
            checkForMissingPropertyValues(propertiesMap, propertyType);
            checkForInconsistentPropertyValues(propertiesMap, propertyType);
        }
    }

    private void checkForMissingPropertyValues(LinkedHashMap<String, IProdDefProperty> propertiesMap,
            ProdDefPropertyType propertyType) {
        for (Iterator<IProdDefProperty> it = propertiesMap.values().iterator(); it.hasNext();) {
            IProdDefProperty property = it.next();
            if (generation.getPropertyValue(property) == null) {
                // no value found for the property with the given type, but we might have a type
                // mismatch
                if (generation.getPropertyValue(property.getPropertyName()) == null) {
                    new MissingPropertyValueEntry(this, property);
                }
                // we create the entry for the type mismatch in checkForInconsistentPropertyValues()
                // if we created it here, too, we would create two entries for the same aspect
            }
        }
    }

    private void checkForInconsistentPropertyValues(LinkedHashMap<String, IProdDefProperty> propertiesMap,
            ProdDefPropertyType propertyType) throws CoreException {
        IPropertyValue[] values = generation.getPropertyValues(propertyType);
        for (int i = 0; i < values.length; i++) {
            IProdDefProperty property = propertiesMap.get(values[i].getPropertyName());
            if (property == null) {
                // the map contains only properties for the current property type
                // so we have to search if the property exists with a different type.
                IProdDefProperty property2 = productCmptType.findProdDefProperty(values[i].getPropertyName(),
                        ipsProject);
                if (property2 != null) {
                    // property2 must have a different type, otherwise it would have been in the
                    // property map!
                    new PropertyTypeMismatchEntry(this, property2, values[i]);
                } else {
                    new ValueWithoutPropertyEntry(this, values[i]);
                }
            } else {
                if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(propertyType)) {
                    checkForValueSetMismatch((IPolicyCmptTypeAttribute)property, (IConfigElement)values[i]);
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

    /**
     * {@inheritDoc}
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return generation;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return entries.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    public IDeltaEntry[] getEntries() {
        return entries.toArray(new IDeltaEntry[entries.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IDeltaEntry[] getEntries(DeltaType type) {
        List<IDeltaEntry> result = new ArrayList<IDeltaEntry>(entries.size());
        for (IDeltaEntry entry : entries) {
            if (entry.getDeltaType().equals(type)) {
                result.add(entry);
            }
        }
        return result.toArray(new IDeltaEntry[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void fix() {
        for (IDeltaEntry entry : entries) {
            entry.fix();
        }
    }

    class HierarchyVisitor extends ProductCmptTypeHierarchyVisitor {

        List<ITableStructureUsage> tableStructureUsages = new ArrayList<ITableStructureUsage>();
        List<IProductCmptTypeAttribute> attributes = new ArrayList<IProductCmptTypeAttribute>();

        public HierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            ITableStructureUsage[] tsu = currentType.getTableStructureUsages();
            for (int i = 0; i < tsu.length; i++) {
                tableStructureUsages.add(tsu[i]);
            }
            IProductCmptTypeAttribute[] attr = currentType.getProductCmptTypeAttributes();
            for (int i = 0; i < attr.length; i++) {
                attributes.add(attr[i]);
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
