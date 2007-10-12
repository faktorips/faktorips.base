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

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;


/**
 *
 */
public class ProductCmptGenerationPolicyCmptTypeDeltaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IPolicyCmptType policyCmptType;
    private IPolicyCmptType policyCmptSupertype;
    private IProductCmptType productCmptType;
    private IProductCmptType productCmptSupertype;
    
    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        policyCmptSupertype = newPolicyAndProductCmptType(ipsProject, "TestSuperpolicy", "TestSuperproduct");
        productCmptSupertype = policyCmptSupertype.findProductCmptType(ipsProject);
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());
        productCmptType.setSupertype(productCmptSupertype.getQualifiedName());
        
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
    }
    
    public void testGetTableContentUsagesWithMissingStructureUsages() throws Exception {
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(0, delta.getTableContentUsagesWithMissingStructureUsages().length);
        
        ITableContentUsage tcu = generation.newTableContentUsage();
        tcu.setStructureUsage("RateTable");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation); 
        ITableContentUsage[] missing = delta.getTableContentUsagesWithMissingStructureUsages();
        assertEquals(1, missing.length);
        assertSame(tcu, missing[0]);
        
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        tsu.setRoleName("RateTable");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation); 
        assertEquals(0, delta.getTableContentUsagesWithMissingStructureUsages().length);
    }

    public void testIsEmpty() throws CoreException {
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(generation, delta.getProductCmptGeneration());
        assertEquals(policyCmptType, delta.getPolicyCmptType());
        assertTrue(delta.isEmpty());
    }
    
    public void testGetAttributesWithMissingElements() throws CoreException {
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(0, delta.getAttributesWithMissingConfigElements().length);
        
        IAttribute a1 = policyCmptType.newAttribute();
        a1.setName("a1");
        a1.setProductRelevant(true);
        IAttribute a2 = policyCmptSupertype.newAttribute();
        a2.setName("a2");
        a2.setProductRelevant(true);
        IAttribute a3 = policyCmptType.newAttribute();
        a3.setName("a3");
        a3.setProductRelevant(false);
        
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        IAttribute[] missing = delta.getAttributesWithMissingConfigElements();
        assertEquals(2, missing.length);
        assertEquals(a1, missing[0]);
        assertEquals(a2, missing[1]);
        
        a1.setName("a2");
        a1.setOverwrites(true);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        missing = delta.getAttributesWithMissingConfigElements();
        assertEquals(1, missing.length);
        assertEquals(a1, missing[0]);

        a1.setName("a1");
        a1.setOverwrites(false);
        
        IConfigElement ce = generation.newConfigElement();
        ce.setPcTypeAttribute("a2");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        missing = delta.getAttributesWithMissingConfigElements();
        assertEquals(1, missing.length);
        assertEquals(a1, missing[0]);
    }

    public void testGetElementsWithMissingAttributes() throws CoreException {
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(0, delta.getConfigElementsWithMissingAttributes().length);
        
        IConfigElement ce1 = generation.newConfigElement();
        ce1.setPcTypeAttribute("a1");
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPcTypeAttribute("a2");
        
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        IConfigElement[] missing = delta.getConfigElementsWithMissingAttributes();
        assertEquals(2, missing.length);
        assertEquals(ce1, missing[0]);
        assertEquals(ce2, missing[1]);
        
        IAttribute a1 = policyCmptType.newAttribute();
        a1.setName("a1");
        a1.setProductRelevant(false);
        IAttribute a2 = policyCmptSupertype.newAttribute();
        a2.setName("a2");
        a2.setProductRelevant(true);
        
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        missing = delta.getConfigElementsWithMissingAttributes();
        assertEquals(1, missing.length);
        assertEquals(ce1, missing[0]);
        
        a1.setProductRelevant(true);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        missing = delta.getConfigElementsWithMissingAttributes();
        assertEquals(0, missing.length);
    }
    
    public void testGetElementsWithValueSetMismatch() throws CoreException {
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(0, delta.getElementsWithValueSetMismatch().length);
        
        IAttribute a1 = policyCmptType.newAttribute();
        a1.setName("a1");
        a1.setProductRelevant(true);
        a1.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)a1.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        
        IConfigElement ce1 = generation.newConfigElement();
        ce1.setPcTypeAttribute("a1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        ce1.setValueSetType(ValueSetType.ENUM);
        
        // value set mismatch between a1 (range), ce1 (enum)
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        IConfigElement[] elements = delta.getElementsWithValueSetMismatch();
        assertEquals(1, elements.length);
        assertEquals(ce1, elements[0]);

        // a1=enum, ce1=enum => no mismatch
        a1.setValueSetType(ValueSetType.ENUM);
        a1.setDatatype(Datatype.INTEGER.getQualifiedName());
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertTrue(delta.isEmpty());
        
        // a1=allvalue, ce1=enum => mismatch
        a1.setValueSetType(ValueSetType.ALL_VALUES);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        
        // a1=range, ce1=range=> no mismatch
        a1.setValueSetType(ValueSetType.RANGE);
        valueSet = (IRangeValueSet)a1.getValueSet();
        a1.setDatatype(Datatype.INTEGER.getQualifiedName());
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        ce1.setValueSetType(ValueSetType.RANGE);
        valueSet = (IRangeValueSet)ce1.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertTrue(delta.isEmpty());
        
        // a1=range 10-20, ce1=range 0-10 => _NO_ mismatch
        a1.setValueSetType(ValueSetType.RANGE);
        valueSet = (IRangeValueSet)a1.getValueSet();
        a1.setDatatype(Datatype.INTEGER.getQualifiedName());
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        ce1.setValueSetType(ValueSetType.RANGE);
        valueSet = (IRangeValueSet)ce1.getValueSet();
        valueSet.setLowerBound("0");
        valueSet.setUpperBound("10");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertEquals(0, delta.getElementsWithValueSetMismatch().length);

        // a1=range, ce1=allvalues=> mismatch
        a1.setValueSetType(ValueSetType.RANGE);
        valueSet = (IRangeValueSet)a1.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");
        ce1.setValueSetType(ValueSetType.ALL_VALUES);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertEquals(1, delta.getElementsWithValueSetMismatch().length);
        
        // value set mismatch between a1 (range), ce1 (enum),
        // but corresponding attribute is not product relevant => this is not a mismatch as this reported otherwise
        ce1.setValueSetType(ValueSetType.ENUM);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty()); // make sure there is an errror
        a1.setProductRelevant(false);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertEquals(0, delta.getElementsWithValueSetMismatch().length);
        assertEquals(1, delta.getConfigElementsWithMissingAttributes().length);

        // does it work along the pctype hierarchy?
        IAttribute a2 = policyCmptSupertype.newAttribute();
        a2.setName("a2");
        a2.setProductRelevant(true);
        a2.setValueSetType(ValueSetType.RANGE);
        valueSet = (IRangeValueSet)a2.getValueSet();
        valueSet.setLowerBound("10");
        valueSet.setUpperBound("20");

        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPcTypeAttribute("a2");
        ce2.setType(ConfigElementType.POLICY_ATTRIBUTE);
        ce2.setValueSetType(ValueSetType.ENUM);
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        assertEquals(1, delta.getElementsWithValueSetMismatch().length);
        
        // no valuet set mismatch should be reported for missing attributes
        a2.setName("hide");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertEquals(0, delta.getElementsWithValueSetMismatch().length);
        assertEquals(1, delta.getAttributesWithMissingConfigElements().length);
    }
    
    public void testGetLinksWithMissingAssociations() throws CoreException {
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(0, delta.getLinksWithMissingAssociations().length);
        
        IProductCmptLink link1 = generation.newLink("typeRelation1");
        IProductCmptLink link2 = generation.newLink("typeRelation2");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        assertFalse(delta.isEmpty());
        IProductCmptLink[] links = delta.getLinksWithMissingAssociations();
        assertEquals(2, links.length);
        assertEquals(link1, links[0]);
        assertEquals(link2, links[1]);
        
        productCmptType.newProductCmptTypeAssociation().setTargetRoleSingular("typeRelation1");
        productCmptSupertype.newProductCmptTypeAssociation().setTargetRoleSingular("typeRelation2");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);
        links = delta.getLinksWithMissingAssociations();
        assertEquals(0, links.length);
    }
    
    public void testGetTableStructureUsagesWithMissingContentUsages() throws Exception {
        policyCmptType.setConfigurableByProductCmptType(true);
        IProductCmptGenerationPolicyCmptTypeDelta delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation);  
        assertEquals(0, delta.getTableStructureUsagesWithMissingContentUsages().length);
        
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        tsu.setRoleName("Egon");
        
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation); 
        ITableStructureUsage[] missing = delta.getTableStructureUsagesWithMissingContentUsages();
        assertEquals(1, missing.length);
        assertSame(tsu, missing[0]);
        
        ITableContentUsage tcu = generation.newTableContentUsage();
        tcu.setStructureUsage("Egon");
        delta = new ProductCmptGenerationPolicyCmptTypeDelta(generation); 
        assertEquals(0, delta.getTableStructureUsagesWithMissingContentUsages().length);
    }
    
}
