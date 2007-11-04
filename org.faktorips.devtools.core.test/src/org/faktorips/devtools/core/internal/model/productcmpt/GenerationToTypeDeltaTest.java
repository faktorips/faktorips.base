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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * 
 * @author Jan Ortmann
 */
public class GenerationToTypeDeltaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IPolicyCmptType superPolicyCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        superPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        superProductCmptType = superPolicyCmptType.findProductCmptType(ipsProject);
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
    }
    
    public void testEmpty() throws CoreException {
        IGenerationToTypeDelta delta = generation.computeDeltaToModel();
        assertEquals(0, delta.getEntries().length);
        assertEquals(true, delta.isEmpty());
        assertEquals(generation, delta.getProductCmptGeneration());
        assertEquals(productCmptType, delta.getProductCmptType());
        delta.fix();
    }
    
    public void getEntriesByType() throws CoreException {
        productCmptType.newProductCmptTypeAttribute("a1");
        productCmptType.newProductCmptTypeAttribute("a2");
        
        IGenerationToTypeDelta delta = generation.computeDeltaToModel();
        assertEquals(2, delta.getEntries(DeltaType.MISSING_PROPERTY_VALUE).length);
        assertEquals(0, delta.getEntries(DeltaType.VALUE_SET_MISMATCH).length);
    }
    
    public void testLinksWithMissingAssociation() throws CoreException {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        IProductCmptLink link = generation.newLink(association.getName());
        assertEquals(1, generation.getNumOfLinks());
        
        IGenerationToTypeDelta delta = generation.computeDeltaToModel();
        assertTrue(delta.isEmpty());
        
        association.delete();
        delta = generation.computeDeltaToModel();
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(1, entries.length);
        
        delta.fix();
        assertEquals(0, generation.getNumOfLinks());
        assertTrue(link.isDeleted());
    }
     
    public void testAttributes() throws CoreException {
        IProductCmptTypeAttribute attribute1 = productCmptType.newProductCmptTypeAttribute("a1");
        IProductCmptTypeAttribute attribute2 = superProductCmptType.newProductCmptTypeAttribute("a2");

        IGenerationToTypeDelta delta = generation.computeDeltaToModel();
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(2, entries.length);
        assertEquals("a2", entries[0].getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(ProdDefPropertyType.VALUE, entries[0].getPropertyType());
        assertEquals("a1", entries[1].getPropertyName());
        
        delta.fix();
        delta = generation.computeDeltaToModel();
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertNotNull(generation.getAttributeValue("a1"));
        assertNotNull(generation.getAttributeValue("a2"));

        attribute1.delete();
        attribute2.delete();
        productCmptType.newProductCmptTypeAttribute("a3");
        
        delta = generation.computeDeltaToModel();
        entries = delta.getEntries();
        assertEquals(3, entries.length);
        assertEquals("a3", entries[0].getPropertyName());
        assertEquals("a2", entries[1].getPropertyName());
        assertEquals("a1", entries[2].getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());
        assertEquals(ProdDefPropertyType.VALUE, entries[0].getPropertyType());
        
        delta.fix();
        delta = generation.computeDeltaToModel();
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertNull(generation.getAttributeValue("a1"));
        assertNull(generation.getAttributeValue("a2"));
        assertNotNull(generation.getAttributeValue("a3"));
    }
    
    public void testTypeMismatch() throws CoreException {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("premium");
        IGenerationToTypeDelta delta = generation.computeDeltaToModel();
        delta.fix();
        assertNotNull(generation.getAttributeValue("premium"));
        
        attribute.delete();
        productCmptType.newFormulaSignature("premium");
        delta = generation.computeDeltaToModel();
        assertEquals(1, delta.getEntries().length);
        assertEquals("premium", delta.getEntries()[0].getPropertyName());
        assertEquals(DeltaType.PROPERTY_TYPE_MISMATCH, delta.getEntries()[0].getDeltaType());
        assertEquals(ProdDefPropertyType.VALUE, delta.getEntries()[0].getPropertyType());
        
        delta.fix();
        assertNull(generation.getAttributeValue("premium"));
        assertNotNull(generation.getFormula("premium"));
    }
    
    public void testValueSetTypeMismatch() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setProductRelevant(true);
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attr.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("10");
        
        IGenerationToTypeDelta delta = generation.computeDeltaToModel();
        delta.fix();
        assertNotNull(generation.getConfigElement("a1"));
        range = (IRangeValueSet)generation.getConfigElement("a1").getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("10", range.getUpperBound());
        
        attr.setValueSetType(ValueSetType.ENUM);
        delta = generation.computeDeltaToModel();
        assertEquals(1, delta.getEntries().length);
        assertEquals(DeltaType.VALUE_SET_MISMATCH, delta.getEntries()[0].getDeltaType());
        assertEquals(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET, delta.getEntries()[0].getPropertyType());
        delta.fix();
        IValueSet valueSet = generation.getConfigElement("a1").getValueSet();
        assertTrue(valueSet instanceof IEnumValueSet);
        
        
        attr.setValueSetType(ValueSetType.ALL_VALUES);
        delta = generation.computeDeltaToModel();
        assertEquals(0, delta.getEntries().length);
    }
}
