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

package org.faktorips.devtools.core.internal.model.productcmpttype2;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IRelation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class RelationTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent = null;
    private IIpsProject ipsProject;
    private IProductCmptType productType;
    private IProductCmptType coverageType;
    private IRelation relation; 

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        coverageType = newProductCmptType(ipsProject, "CoverageType");
        relation = productType.newRelation();
        
        ipsProject.getIpsModel().addChangeListener(this);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ipsProject.getIpsModel().removeChangeListener(this);
    }

    
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#toXml(org.w3c.dom.Document)}.
     */
    public void testToXml() {
        relation.setTarget("pack1.CoverageType");
        relation.setTargetRoleSingular("CoverageType");
        relation.setTargetRolePlural("CoverageTypes");
        relation.setMinCardinality(2);
        relation.setMaxCardinality(4);
        relation.setReadOnlyContainer(true);
        relation.setImplementedContainerRelation("BaseCoverageType");
        
        Element el = relation.toXml(newDocument());
        relation = productType.newRelation();
        relation.initFromXml(el);
        
        assertEquals("pack1.CoverageType", relation.getTarget());
        assertEquals("CoverageType", relation.getTargetRoleSingular());
        assertEquals("CoverageTypes", relation.getTargetRolePlural());
        assertEquals(2, relation.getMinCardinality());
        assertEquals(4, relation.getMaxCardinality());
        assertTrue(relation.isReadOnlyContainer());
        assertEquals("BaseCoverageType", relation.getImplementedContainerRelation());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}.
     */
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        relation.initFromXml(el);
        assertEquals("pack1.CoverageType", relation.getTarget());
        assertEquals("CoverageType", relation.getTargetRoleSingular());
        assertEquals("CoverageTypes", relation.getTargetRolePlural());
        assertEquals(1, relation.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, relation.getMaxCardinality());
        assertTrue(relation.isReadOnlyContainer());
        assertEquals("BaseCoverageType", relation.getImplementedContainerRelation());
        assertEquals("blabla", relation.getDescription());
    }

    public void testPropertyConstants() {
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_READ_ONLY_CONTAINER);
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_IMPLEMENTED_CONTAINER_RELATION);
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_MAX_CARDINALITY);
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_MIN_CARDINALITY);
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_TARGET);
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_TARGET_ROLE_PLURAL);
        testPropertyAccessReadWrite(IRelation.class, IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
    }
        
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#findTarget()}.
     * @throws CoreException 
     */
    public void testFindTarget() throws CoreException {
        relation.setTarget("");
        assertNull(relation.findTarget(ipsProject));
        
        relation.setTarget("unknown");
        assertNull(relation.findTarget(ipsProject));
        
        relation.setTarget(coverageType.getQualifiedName());
        assertEquals(coverageType, relation.findTarget(ipsProject));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#setTarget(java.lang.String)}.
     */
    public void testSetTarget() {
        relation.setTarget("Target");
        assertEquals("Target", relation.getTarget());
        assertEquals(relation, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#setTargetRoleSingular(java.lang.String)}.
     */
    public void testSetTargetRoleSingular() {
        relation.setTargetRoleSingular("TargetRole");
        assertEquals("TargetRole", relation.getTargetRoleSingular());
        assertEquals(relation, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#setTargetRolePlural(java.lang.String)}.
     */
    public void testSetTargetRolePlural() {
        relation.setTargetRolePlural("TargetRole");
        assertEquals("TargetRole", relation.getTargetRolePlural());
        assertEquals(relation, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#setMinCardinality(int)}.
     */
    public void testSetMinCardinality() {
        relation.setMinCardinality(2);
        assertEquals(2, relation.getMinCardinality());
        assertEquals(relation, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#setMaxCardinality(int)}.
     */
    public void testSetMaxCardinality() {
        relation.setMinCardinality(42);
        assertEquals(42, relation.getMinCardinality());
        assertEquals(relation, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#isContainerRelationImplementation()}.
     */
    public void testIsContainerRelationImplementation() {
        relation.setImplementedContainerRelation("");
        assertFalse(relation.isContainerRelationImplementation());
        relation.setImplementedContainerRelation("someContainerRelation");
        assertTrue(relation.isContainerRelationImplementation());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype2.Relation#setImplementedContainerRelation(java.lang.String)}.
     */
    public void testSetImplementedContainerRelation() {
        relation.setImplementedContainerRelation("someRelation");
        assertEquals("someRelation", relation.getImplementedContainerRelation());
        assertEquals(relation, lastEvent.getPart());
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
