/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.product.deltaentries.AbstractDeltaEntry;
import org.faktorips.devtools.core.internal.model.product.deltaentries.MissingPropertyEntry;
import org.faktorips.devtools.core.internal.model.product.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.core.internal.model.product.deltaentries.PropertyTypeMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpttype2.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IDeltaEntry;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpttype2.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype2.ProductCmptTypeHierarchyVisitor;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of IProductCmptToTypeDelta.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationToTypeDelta implements IProductCmptGenerationToTypeDelta {

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;
    private IProductCmptType productCmptType;
    private List entries = new ArrayList();
    
    public ProductCmptGenerationToTypeDelta(IProductCmptGeneration generation) throws CoreException {
        ArgumentCheck.notNull(generation);
        this.generation = generation;
        ipsProject = generation.getIpsProject();
        productCmptType = generation.findProductCmptType(ipsProject);
        if (productCmptType==null) {
            return;
        }
        createEntriesForProperties();
    }
    
    private void createEntriesForProperties() throws CoreException {
        for (int i=0; i<ProdDefPropertyType.ALL_TYPES.length; i++) {
            ProdDefPropertyType propertyType = ProdDefPropertyType.ALL_TYPES[i]; 
            Map propertiesMap = ((ProductCmptType)productCmptType).getProdDefPropertiesMap(propertyType, ipsProject);
            checkForMissingPropertyValues(propertiesMap, propertyType);
            checkForInconsistentPropertyValues(propertiesMap, propertyType);
        }
    }
    
    private void checkForMissingPropertyValues(Map propertiesMap, ProdDefPropertyType propertyType) {
        for (Iterator it=propertiesMap.values().iterator(); it.hasNext(); ) {
            IProdDefProperty property = (IProdDefProperty)it.next();
            if (generation.getPropertyValue(property)==null) {
                new MissingPropertyValueEntry(this, property);
            }
        }
    }

    private void checkForInconsistentPropertyValues(Map propertiesMap, ProdDefPropertyType propertyType) throws CoreException {
        IPropertyValue[] values = generation.getPropertyValues(propertyType);
        for (int i = 0; i < values.length; i++) {
            IProdDefProperty property = (IProdDefProperty)propertiesMap.get(values[i].getPropertyName());
            if (property == null) {
                new MissingPropertyEntry(this, values[i]);
            } else {
                if (!property.getProdDefPropertyType().equals(values[i].getPropertyType())) {
                    // the map contains only properties for the current property type
                    // so we have to search if the property exists with a different type.
                    // as this happends not very often, this is fast for the most delta computations
                    IProdDefProperty property2 = productCmptType.findProdDefProperty(values[i].getPropertyName(), ipsProject);
                    if (property2!=null) {
                        // property2 must have a different type, otherwise it would have been in the property map!
                        new PropertyTypeMismatchEntry(this, property2, values[i]);
                    }
                }
            }
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
        return entries.size()==0;
    }

    /**
     * {@inheritDoc}
     */
    public IDeltaEntry[] getEntries() {
        return (IDeltaEntry[])entries.toArray(new IDeltaEntry[entries.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void fix() {
        for (Iterator it = entries.iterator(); it.hasNext();) {
            IDeltaEntry entry = (IDeltaEntry)it.next();
            entry.fix();
        }
    }
    
    class HierarchyVisitor extends ProductCmptTypeHierarchyVisitor {

        List tableStructureUsages = new ArrayList();
        List attributes = new ArrayList();
        
        public HierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        protected boolean visit(IProductCmptType currentType) throws CoreException {
            ITableStructureUsage[] tsu = currentType.getTableStructureUsages();
            for (int i = 0; i < tsu.length; i++) {
                tableStructureUsages.add(tsu[i]);
            }
            IProductCmptTypeAttribute[] attr = currentType.getAttributes();
            for (int i = 0; i < attr.length; i++) {
                attributes.add(attr[i]);
            }
            return true;
            
        }

        boolean containsTableStructureUsage(String rolename) {
            for (Iterator it = tableStructureUsages.iterator(); it.hasNext();) {
                ITableStructureUsage tsu = (ITableStructureUsage)it.next();
                if (tsu.getRoleName().equals(rolename)) {
                    return true;
                }
            }
            return false;
        }
        
        boolean containsAttribute(String name) {
            for (Iterator it = attributes.iterator(); it.hasNext();) {
                IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)it.next();
                if (attribute.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    

}
