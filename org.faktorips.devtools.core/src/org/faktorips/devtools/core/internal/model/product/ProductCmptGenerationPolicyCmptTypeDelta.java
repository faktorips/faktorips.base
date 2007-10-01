/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class ProductCmptGenerationPolicyCmptTypeDelta implements
        IProductCmptGenerationPolicyCmptTypeDelta {
    
    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    
    private IAttribute[] attributesWithMissingConfigElements = new IAttribute[0];
    private IConfigElement[] elementsWithMissingAttributes = new IConfigElement[0];
    private IConfigElement[] elementsWithTypeMismatch = new IConfigElement[0];
    private IConfigElement[] elementsWithValueSetMismatch = new IConfigElement[0]; 
    private IProductCmptLink[] linksWithMissingAssociations = new IProductCmptLink[0];
    private ITableStructureUsage[] tableStructureUsagesWithMissingTableContentUsages = new ITableStructureUsage[0];
    private ITableContentUsage[] tableContentUsagesWithMissingTableStructureUsages = new ITableContentUsage[0];
    
    public ProductCmptGenerationPolicyCmptTypeDelta(IProductCmptGeneration generation) throws CoreException {

        ArgumentCheck.notNull(generation);
        
        this.generation = generation;
        ipsProject = generation.getIpsProject();
        productCmptType = generation.findProductCmptType(ipsProject);
        if (productCmptType==null) {
            return;
        }
        HierarchyVisitor hierarchyVisitor = new HierarchyVisitor(ipsProject);
        hierarchyVisitor.start(productCmptType);
        computeStructureUsagesWithMissingContentUsages(hierarchyVisitor);
        computeContentUsagesWithMissingStructureUsages(hierarchyVisitor);
        computeLinksWithMissingAssociations();
        policyCmptType = productCmptType.findPolicyCmptType(true, productCmptType.getIpsProject());
        if (policyCmptType==null) {
            return;
        }
        ITypeHierarchy hierarchy = policyCmptType.getSupertypeHierarchy();
        computeAttributesWithMissingConfigElements(hierarchy);
        computeElementsWithMissingAttributes(hierarchy);
        computeElementsWithTypeOrValueSetMismatch(hierarchy);
    }
    
    /**
     * @param hierarchy
     */
    private void computeContentUsagesWithMissingStructureUsages(HierarchyVisitor visitor) {
        List missing = new ArrayList();
        ITableContentUsage[] usages = generation.getTableContentUsages();
        for (int i = 0; i < usages.length; i++) {
            if (!visitor.containsTableStructureUsage(usages[i].getStructureUsage())) {
                missing.add(usages[i]);
            }
        }
        tableContentUsagesWithMissingTableStructureUsages = (ITableContentUsage[])missing
                .toArray(new ITableContentUsage[missing.size()]);
    }

    private void computeStructureUsagesWithMissingContentUsages(HierarchyVisitor visitor) {
        List missing = new ArrayList();
        for (Iterator it=visitor.tableStructureUsages.iterator(); it.hasNext(); ) {
            ITableStructureUsage tsu = (ITableStructureUsage)it.next();
            if (generation.getTableContentUsage(tsu.getRoleName()) == null) {
                missing.add(tsu);
            }
        }
        tableStructureUsagesWithMissingTableContentUsages = (ITableStructureUsage[])missing
                .toArray(new ITableStructureUsage[missing.size()]);
    }

    private void computeAttributesWithMissingConfigElements(ITypeHierarchy hierarchy) throws CoreException {
        List missing = new ArrayList();
        IAttribute[] attributes = hierarchy.getAllAttributesRespectingOverride(policyCmptType);
        for (int i=0; i<attributes.length; i++) {
            if (attributes[i].isProductRelevant()) {
                if (generation.getConfigElement(attributes[i].getName())==null) {
                    missing.add(attributes[i]);
                }
            }
        }
        attributesWithMissingConfigElements = (IAttribute[])missing.toArray(new IAttribute[missing.size()]);
    }

    private void computeElementsWithMissingAttributes(ITypeHierarchy hierarchy) {
        List missing = new ArrayList();
        IConfigElement[] elements = generation.getConfigElements();
        for (int i=0; i<elements.length; i++) {
            IAttribute attribute = hierarchy.findAttribute(policyCmptType, elements[i].getPcTypeAttribute());
            if (attribute==null || !attribute.isProductRelevant()) {
                missing.add(elements[i]);
            }
        }
        elementsWithMissingAttributes = (IConfigElement[])missing.toArray(new IConfigElement[missing.size()]);
    }
    
    private void computeElementsWithTypeOrValueSetMismatch(ITypeHierarchy hierarchy) {
        List typeMismatchs = new ArrayList();
        List valueSetMismatchs = new ArrayList();
        IConfigElement[] elements = generation.getConfigElements();
        for (int i=0; i<elements.length; i++) {
            IAttribute attribute =hierarchy.findAttribute(policyCmptType, elements[i].getPcTypeAttribute()); 
            if (attribute != null && attribute.isProductRelevant()) {
                if (attribute.getConfigElementType() != elements[i].getType()) {
                    typeMismatchs.add(elements[i]);
                }

                if (!attribute.getValueSet().getValueSetType().equals(elements[i].getValueSet().getValueSetType())) {
                    valueSetMismatchs.add(elements[i]);
                }
                // No check on contains here - only structural differences are reported by this delta.
            }
        }
        elementsWithTypeMismatch = (IConfigElement[])typeMismatchs.toArray(new IConfigElement[typeMismatchs.size()]);
        elementsWithValueSetMismatch = (IConfigElement[])valueSetMismatchs.toArray(new IConfigElement[valueSetMismatchs.size()]);
    }
    
    private void computeLinksWithMissingAssociations() throws CoreException {
        List result = new ArrayList();
        IProductCmptLink[] links = generation.getLinks();
        for (int i=0; i<links.length; i++) {
            if (productCmptType.findAssociation(links[i].getAssociation(), ipsProject)==null) {
                result.add(links[i]);
            }
        }
        linksWithMissingAssociations = (IProductCmptLink[])result.toArray(new IProductCmptLink[result.size()]);
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
    public IPolicyCmptType getPolicyCmptType() {
        return policyCmptType;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return attributesWithMissingConfigElements.length==0
        	&& elementsWithMissingAttributes.length == 0
        	&& elementsWithTypeMismatch.length == 0
        	&& elementsWithValueSetMismatch.length == 0
        	&& linksWithMissingAssociations.length == 0
            && tableContentUsagesWithMissingTableStructureUsages.length == 0
            && tableStructureUsagesWithMissingTableContentUsages.length == 0
            ;
    }

    /** 
     * {@inheritDoc}
     */
    public IAttribute[] getAttributesWithMissingConfigElements() {
        return attributesWithMissingConfigElements;
    }

    /** 
     * {@inheritDoc}
     */
    public IConfigElement[] getTypeMismatchElements() {
        return elementsWithTypeMismatch;
    }

    /** 
     * {@inheritDoc}
     */
    public IConfigElement[] getConfigElementsWithMissingAttributes() {
        return elementsWithMissingAttributes;
    }

    /** 
     * {@inheritDoc}
     */
    public IProductCmptLink[] getLinksWithMissingAssociations() {
        return linksWithMissingAssociations;
    }

    /**
     * {@inheritDoc}
     */
    public IConfigElement[] getElementsWithValueSetMismatch() {
        return elementsWithValueSetMismatch;
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage[] getTableStructureUsagesWithMissingContentUsages() {
        return tableStructureUsagesWithMissingTableContentUsages;
    }
    
    /**
     * {@inheritDoc}
     */
    public ITableContentUsage[] getTableContentUsagesWithMissingStructureUsages() {
        return tableContentUsagesWithMissingTableStructureUsages;
    }
    
    class HierarchyVisitor extends ProductCmptTypeHierarchyVisitor {

        List tableStructureUsages = new ArrayList();
        
        public HierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        protected boolean visit(IProductCmptType currentType) throws CoreException {
            ITableStructureUsage[] tsu = currentType.getTableStructureUsages();
            for (int i = 0; i < tsu.length; i++) {
                tableStructureUsages.add(tsu[i]);
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
    }
}
