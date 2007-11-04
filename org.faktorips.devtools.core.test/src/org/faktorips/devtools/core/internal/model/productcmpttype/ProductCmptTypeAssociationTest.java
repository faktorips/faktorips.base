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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productType;
    private IProductCmptType coverageTypeType;
    private IProductCmptTypeAssociation association; 

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        coverageTypeType = newProductCmptType(ipsProject, "CoverageType");
        association = productType.newProductCmptTypeAssociation();
    }
    
    public void testFindPolicyCmptTypeRelation() throws CoreException {
        assertNull(association.findMatchingPolicyCmptTypeRelation(ipsProject));
        
        association.setTarget(coverageTypeType.getQualifiedName());
        assertNull(association.findMatchingPolicyCmptTypeRelation(ipsProject));
        
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy");
        productType.setPolicyCmptType(policyType.getQualifiedName());
        
        org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation policyTypeRelation = policyType.newPolicyCmptTypeAssociation();
        policyTypeRelation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertNull(association.findMatchingPolicyCmptTypeRelation(ipsProject));

        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "Coverage");
        policyTypeRelation.setTarget(coverageType.getQualifiedName());
        assertNull(association.findMatchingPolicyCmptTypeRelation(ipsProject));

        coverageTypeType.setPolicyCmptType(coverageType.getQualifiedName());
        assertEquals(policyTypeRelation, association.findMatchingPolicyCmptTypeRelation(ipsProject));
    }
    
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer#toXml(org.w3c.dom.Document)}.
     */
    public void testToXml() {
        association.setTarget("pack1.CoverageType");
        association.setTargetRoleSingular("CoverageType");
        association.setTargetRolePlural("CoverageTypes");
        association.setMinCardinality(2);
        association.setMaxCardinality(4);
        association.setDerivedUnion(true);
        association.setSubsettedDerivedUnion("BaseCoverageType");
        association.setAssociationType(AssociationType.AGGREGATION);
        
        Element el = association.toXml(newDocument());
        association = productType.newProductCmptTypeAssociation();
        association.initFromXml(el);
        
        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(2, association.getMinCardinality());
        assertEquals(4, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}.
     */
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        association.initFromXml(el);
        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertEquals("blabla", association.getDescription());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#findTarget()}.
     * @throws CoreException 
     */
    public void testFindTarget() throws CoreException {
        association.setTarget("");
        assertNull(association.findTarget(ipsProject));
        
        association.setTarget("unknown");
        assertNull(association.findTarget(ipsProject));
        
        association.setTarget(coverageTypeType.getQualifiedName());
        assertEquals(coverageTypeType, association.findTarget(ipsProject));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTarget(java.lang.String)}.
     */
    public void testSetTarget() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET, association, "newTarget");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTargetRoleSingular(java.lang.String)}.
     */
    public void testSetTargetRoleSingular() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR, association, "newRole");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTargetRolePlural(java.lang.String)}.
     */
    public void testSetTargetRolePlural() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL, association, "newRoles");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setMinCardinality(int)}.
     */
    public void testSetMinCardinality() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY, association, new Integer(42));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setMaxCardinality(int)}.
     */
    public void testSetMaxCardinality() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY, association, new Integer(42));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#isSubsetOfADerivedUnion()}.
     */
    public void testIsSubsetOfADerivedUnion() {
        association.setSubsettedDerivedUnion("");
        assertFalse(association.isSubsetOfADerivedUnion());
        association.setSubsettedDerivedUnion("someContainerRelation");
        assertTrue(association.isSubsetOfADerivedUnion());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setSubsettedDerivedUnion(java.lang.String)}.
     */
    public void testSetSubsettedDerivedUnion() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION, association, "SomeUnion");
    }

}
