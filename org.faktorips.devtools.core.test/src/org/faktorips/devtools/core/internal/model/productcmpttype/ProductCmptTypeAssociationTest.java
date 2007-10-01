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
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociationTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent = null;
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
        association = productType.newAssociation();
        
        ipsProject.getIpsModel().addChangeListener(this);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ipsProject.getIpsModel().removeChangeListener(this);
    }

    public void testFindPolicyCmptTypeRelation() throws CoreException {
        assertNull(association.findPolicyCmptTypeRelation(ipsProject));
        
        association.setTarget(coverageTypeType.getQualifiedName());
        assertNull(association.findPolicyCmptTypeRelation(ipsProject));
        
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy");
        productType.setPolicyCmptType(policyType.getQualifiedName());
        
        org.faktorips.devtools.core.model.pctype.IRelation policyTypeRelation = policyType.newRelation();
        policyTypeRelation.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        assertNull(association.findPolicyCmptTypeRelation(ipsProject));

        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "Coverage");
        policyTypeRelation.setTarget(coverageType.getQualifiedName());
        assertNull(association.findPolicyCmptTypeRelation(ipsProject));

        coverageTypeType.setPolicyCmptType(coverageType.getQualifiedName());
        assertEquals(policyTypeRelation, association.findPolicyCmptTypeRelation(ipsProject));
    }
    
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#toXml(org.w3c.dom.Document)}.
     */
    public void testToXml() {
        association.setTarget("pack1.CoverageType");
        association.setTargetRoleSingular("CoverageType");
        association.setTargetRolePlural("CoverageTypes");
        association.setMinCardinality(2);
        association.setMaxCardinality(4);
        association.setReadOnlyContainer(true);
        association.setImplementedContainerRelation("BaseCoverageType");
        association.setAggregationKind(AggregationKind.SHARED);
        
        Element el = association.toXml(newDocument());
        association = productType.newAssociation();
        association.initFromXml(el);
        
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(2, association.getMinCardinality());
        assertEquals(4, association.getMaxCardinality());
        assertTrue(association.isReadOnlyContainer());
        assertEquals("BaseCoverageType", association.getImplementedContainerRelation());
        assertEquals(AggregationKind.SHARED, association.getAggregationKind());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}.
     */
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        association.initFromXml(el);
        assertEquals(AggregationKind.SHARED, association.getAggregationKind());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertTrue(association.isReadOnlyContainer());
        assertEquals("BaseCoverageType", association.getImplementedContainerRelation());
        assertEquals("blabla", association.getDescription());
    }

    public void testPropertyConstants() {
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_AGGREGATION_KIND);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_READ_ONLY_CONTAINER);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_IMPLEMENTED_CONTAINER_RELATION);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        testPropertyAccessReadWrite(IProductCmptTypeAssociation.class, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
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
        association.setTarget("Target");
        assertEquals("Target", association.getTarget());
        assertEquals(association, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTargetRoleSingular(java.lang.String)}.
     */
    public void testSetTargetRoleSingular() {
        association.setTargetRoleSingular("TargetRole");
        assertEquals("TargetRole", association.getTargetRoleSingular());
        assertEquals(association, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTargetRolePlural(java.lang.String)}.
     */
    public void testSetTargetRolePlural() {
        association.setTargetRolePlural("TargetRole");
        assertEquals("TargetRole", association.getTargetRolePlural());
        assertEquals(association, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setMinCardinality(int)}.
     */
    public void testSetMinCardinality() {
        association.setMinCardinality(2);
        assertEquals(2, association.getMinCardinality());
        assertEquals(association, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setMaxCardinality(int)}.
     */
    public void testSetMaxCardinality() {
        association.setMinCardinality(42);
        assertEquals(42, association.getMinCardinality());
        assertEquals(association, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#isContainerRelationImplementation()}.
     */
    public void testIsContainerRelationImplementation() {
        association.setImplementedContainerRelation("");
        assertFalse(association.isContainerRelationImplementation());
        association.setImplementedContainerRelation("someContainerRelation");
        assertTrue(association.isContainerRelationImplementation());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setImplementedContainerRelation(java.lang.String)}.
     */
    public void testSetImplementedContainerRelation() {
        association.setImplementedContainerRelation("someRelation");
        assertEquals("someRelation", association.getImplementedContainerRelation());
        assertEquals(association, lastEvent.getPart());
    }
    
    public void testFindContainerRelation() {
        // TODO test
    }

    public void testFindContainerRelation_GivenRelation() {
        // TODO test
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
        
    }

}
