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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent = null;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmptType superSuperProductCmptType;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        productCmptType = newProductCmptType(ipsProject, "Product");
        superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProduct");
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());
        ipsProject.getIpsModel().addChangeListener(this);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ipsProject.getIpsModel().removeChangeListener(this);
    }
    
    public void testValidatePolicyCmptType() throws CoreException {
        MessageList ml = productCmptType.validate();
        assertNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
        
        productCmptType.setPolicyCmptType("Unknown");
        ml = productCmptType.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
        
        productCmptType.setPolicyCmptType(superProductCmptType.getQualifiedName());
        ml = productCmptType.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST));
    }
    
    public void testNewMethod() {
        IMethod method = productCmptType.newMethod();
        assertNotNull(method);
        assertEquals(productCmptType, method.getParent());
        assertEquals(1, productCmptType.getNumOfMethods());
        assertEquals(method, productCmptType.getMethods()[0]);
        assertEquals(1, productCmptType.getMethods().length);
        assertEquals(1, productCmptType.getProductCmptTypeMethods().length);
    }    
    
    public void testNewProductCmptTypeMethod() {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        assertNotNull(method);
        assertEquals(productCmptType, method.getParent());
        assertEquals(1, productCmptType.getNumOfMethods());
        assertEquals(method, productCmptType.getMethods()[0]);
        assertEquals(1, productCmptType.getMethods().length);
        assertEquals(1, productCmptType.getProductCmptTypeMethods().length);
    }
    
    public void testNewTableStructureUsage() {
        ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
        assertNotNull(tsu);
        assertEquals(productCmptType, tsu.getParent());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(tsu, productCmptType.getTableStructureUsages()[0]);
    }
    
    public void testFindSupertype() throws CoreException {
        assertEquals(superProductCmptType, productCmptType.findSupertype(ipsProject));
        assertNull(superSuperProductCmptType.findSupertype(ipsProject));
        productCmptType.setSupertype("unknownType");
        assertNull(productCmptType.findSupertype(ipsProject));
    }
    
    public void testFindTableStructureUsageInSupertypeHierarchy() throws CoreException {
        assertNull(superSuperProductCmptType.findTableStructureUsageInSupertypeHierarchy(null, true, ipsProject));
        assertNull(superSuperProductCmptType.findTableStructureUsageInSupertypeHierarchy(null, false, ipsProject));
        assertNull(superSuperProductCmptType.findTableStructureUsageInSupertypeHierarchy("someRole", true, ipsProject));
        assertNull(superSuperProductCmptType.findTableStructureUsageInSupertypeHierarchy("someRole", false, ipsProject));
        
        assertNull(productCmptType.findTableStructureUsageInSupertypeHierarchy("someRole", true, ipsProject));
        assertNull(productCmptType.findTableStructureUsageInSupertypeHierarchy("someRole", false, ipsProject));

        ITableStructureUsage tsu1 = productCmptType.newTableStructureUsage();
        tsu1.setRoleName("role1");
        assertNull(productCmptType.findTableStructureUsageInSupertypeHierarchy("role1", false, ipsProject));
        assertEquals(tsu1, productCmptType.findTableStructureUsageInSupertypeHierarchy("role1", true, ipsProject));
        assertNull(productCmptType.findTableStructureUsageInSupertypeHierarchy("unkownRole", true, ipsProject));
        
        ITableStructureUsage tsu2 = superSuperProductCmptType.newTableStructureUsage();
        tsu2.setRoleName("role2");
        assertEquals(tsu2, productCmptType.findTableStructureUsageInSupertypeHierarchy("role2", true, ipsProject));
        assertEquals(tsu2, productCmptType.findTableStructureUsageInSupertypeHierarchy("role2", false, ipsProject));

        tsu2.setRoleName("role1");
        assertEquals(tsu1, productCmptType.findTableStructureUsageInSupertypeHierarchy("role1", true, ipsProject));
        assertEquals(tsu2, productCmptType.findTableStructureUsageInSupertypeHierarchy("role1", false, ipsProject));
        
    }
    
    public void testNewMemento() {
        Memento memento = productCmptType.newMemento();
        assertNotNull(memento);
        
        productCmptType.newAttribute();
        memento = productCmptType.newMemento();
        assertNotNull(memento);
    }

    public void testSetPolicyCmptType() {
        productCmptType.setPolicyCmptType("NewType");
        assertEquals("NewType", productCmptType.getPolicyCmptType());
        assertEquals(productCmptType.getIpsSrcFile(), lastEvent.getIpsSrcFile());
    }
    
    public void testConfiguresPolicyCmptType() {
        productCmptType.setPolicyCmptType(null);
        assertFalse(productCmptType.isConfigurationForPolicyCmptType());
       
        productCmptType.setPolicyCmptType("");
        assertFalse(productCmptType.isConfigurationForPolicyCmptType());
        
        productCmptType.setPolicyCmptType("NewType");
        assertTrue(productCmptType.isConfigurationForPolicyCmptType());
    }

    public void testFindPolicyCmptType() throws CoreException {
        productCmptType.setPolicyCmptType("");
        assertNull(productCmptType.findPolicyCmptType(true, ipsProject));
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        
        productCmptType.setPolicyCmptType("UnknownType");
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        assertNull(productCmptType.findPolicyCmptType(true, ipsProject));

        productCmptType.setPolicyCmptType("Policy");
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(false, ipsProject));
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(true, ipsProject));
        
        productCmptType.setPolicyCmptType("");
        superProductCmptType.setPolicyCmptType("Policy");
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        assertEquals(policyCmptType, productCmptType.findPolicyCmptType(true, ipsProject));
        
        productCmptType.setPolicyCmptType("Unkown");
        assertNull(productCmptType.findPolicyCmptType(false, ipsProject));
        assertNull(productCmptType.findPolicyCmptType(true, ipsProject));
    }

    public void testNewAttribute() {
        IAttribute a1 = productCmptType.newAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(a1, productCmptType.getAttributes()[0]);
        assertEquals(productCmptType, a1.getProductCmptType());
        
        assertEquals(a1, lastEvent.getPart());
    }

    public void testGetAttribute() {
        assertNull(productCmptType.getAttribute("a"));
        
        IAttribute a1 = productCmptType.newAttribute();
        productCmptType.newAttribute();
        IAttribute a3 = productCmptType.newAttribute();
        a1.setName("a1");
        a3.setName("a3");
        
        assertEquals(a1, productCmptType.getAttribute("a1"));
        assertEquals(a3, productCmptType.getAttribute("a3"));
        assertNull(productCmptType.getAttribute("unkown"));
        
        assertNull(productCmptType.getAttribute(null));
    }

    public void testGetAttributes() {
        assertEquals(0, productCmptType.getAttributes().length);

        IAttribute a1 = productCmptType.newAttribute();
        IAttribute[] attributes = productCmptType.getAttributes();
        assertEquals(a1, attributes[0]);
        
        IAttribute a2 = productCmptType.newAttribute();
        attributes = productCmptType.getAttributes();
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
    }

    public void testGetNumOfAttributes() {
        assertEquals(0, productCmptType.getNumOfAttributes());
        
        productCmptType.newAttribute();
        assertEquals(1, productCmptType.getNumOfAttributes());
        
        productCmptType.newAttribute();
        assertEquals(2, productCmptType.getNumOfAttributes());
    }

    public void testMoveAttributes() {
        IAttribute a1 = productCmptType.newAttribute();
        IAttribute a2 = productCmptType.newAttribute();
        IAttribute a3 = productCmptType.newAttribute();
        
        productCmptType.moveAttributes(new int[]{1, 2}, true);
        IAttribute[] attributes = productCmptType.getAttributes();
        assertEquals(a2, attributes[0]);
        assertEquals(a3, attributes[1]);
        assertEquals(a1, attributes[2]);
        
        assertTrue(lastEvent.isAffected(a1));
        assertTrue(lastEvent.isAffected(a2));
        assertTrue(lastEvent.isAffected(a3));
    }
    
    public void testInitFromXml() {
        Element rootEl = getTestDocument().getDocumentElement();
        productCmptType.setPolicyCmptType("Bla");

        productCmptType.initFromXml(XmlUtil.getElement(rootEl, 0));
        assertEquals("Policy", productCmptType.getPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfRelations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }
    
    public void testToXml() throws CoreException {
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmptType.newAttribute().setName("attr");
        productCmptType.newRelation().setTargetRoleSingular("role");
        productCmptType.newTableStructureUsage().setRoleName("roleTsu");
        productCmptType.newMethod().setName("method1");
        
        Element el = productCmptType.toXml(newDocument());
        productCmptType = newProductCmptType(ipsProject, "Copy");
        productCmptType.initFromXml(el);
        
        assertEquals(policyCmptType.getQualifiedName(), productCmptType.getPolicyCmptType());
        assertEquals(1, productCmptType.getNumOfAttributes());
        assertEquals(1, productCmptType.getNumOfRelations());
        assertEquals(1, productCmptType.getNumOfTableStructureUsages());
        assertEquals(1, productCmptType.getNumOfMethods());
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

}
