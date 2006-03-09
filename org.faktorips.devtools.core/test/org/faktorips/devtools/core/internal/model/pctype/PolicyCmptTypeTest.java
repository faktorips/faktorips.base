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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class PolicyCmptTypeTest extends IpsPluginTest implements ContentsChangeListener {
    
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private PolicyCmptType pcType;
    private ContentChangeEvent lastEvent;
    private IIpsProject pdProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        root = pdProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)sourceFile.getIpsObject();
        pcType.setConfigurableByProductCmptType(false);
    }
    
    public void testIsExtensionCompilationUnitGenerated() throws CoreException {
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        
        // force generation
        pcType.setForceExtensionCompilationUnitGeneration(true);
        assertTrue(pcType.isExtensionCompilationUnitGenerated());
        
        // validation rule
        pcType.setForceExtensionCompilationUnitGeneration(false);
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        IValidationRule rule = pcType.newRule();
        assertTrue(pcType.isExtensionCompilationUnitGenerated());
        
        // method
        rule.delete();
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        IMethod method = pcType.newMethod();
        assertTrue(pcType.isExtensionCompilationUnitGenerated());
        method.setAbstract(true);
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        method.delete();

        // attribute
        IAttribute attribute = pcType.newAttribute();
        attribute.setAttributeType(AttributeType.COMPUTED);
        attribute.setProductRelevant(true);
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        attribute.setProductRelevant(false);
        assertTrue(pcType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.DERIVED);
        assertTrue(pcType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.CONSTANT);
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        
    }
    
    public void testGetChildren() {
        IAttribute a1 = pcType.newAttribute();
        IMethod m1 = pcType.newMethod();
        IRelation r1 = pcType.newRelation();
        IValidationRule rule1 = pcType.newRule();
        
        IIpsElement[] elements = pcType.getChildren();
        assertEquals(4, elements.length);
        assertEquals(a1, elements[0]);
        assertEquals(m1, elements[1]);
        assertEquals(r1, elements[2]);
        assertEquals(rule1, elements[3]);
    }

    public void testNewAttribute() {
        sourceFile.getIpsModel().addChangeListener(this);
        IAttribute a = pcType.newAttribute();
        assertSame(pcType, a.getIpsObject());
        assertEquals(1, pcType.getNumOfAttributes());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        assertEquals(0, a.getId());

        IMethod m = pcType.newMethod();
        assertEquals(1, m.getId());
        IAttribute a2 = pcType.newAttribute();
        assertEquals(2, a2.getId());
    }

    public void testGetAttributes() {
        assertEquals(0, pcType.getAttributes().length);
        IAttribute a1 = pcType.newAttribute();
        IAttribute a2 = pcType.newAttribute();
        assertSame(a1, pcType.getAttributes()[0]);
        assertSame(a2, pcType.getAttributes()[1]);
        
        // make sure a defensive copy is returned.
        pcType.getAttributes()[0] = null;
        assertNotNull(pcType.getAttributes()[0]);
    }
    
    public void testGetAttribute() {
        IAttribute a1 = pcType.newAttribute();
        a1.setName("a1");
        IAttribute a2 = pcType.newAttribute();
        a2.setName("a2");
        IAttribute a3 = pcType.newAttribute();
        a3.setName("a2"); // same name!
        
        assertEquals(a1, pcType.getAttribute("a1"));
        assertEquals(a2, pcType.getAttribute("a2"));
        assertNull(pcType.getAttribute("b"));
        assertNull(pcType.getAttribute(null));
    }

    public void testNewMethod() {
        sourceFile.getIpsModel().addChangeListener(this);
        IMethod m = pcType.newMethod();
        assertEquals(0, m.getId());
        assertSame(pcType, m.getIpsObject());
        assertEquals(1, pcType.getNumOfMethods());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());

        IMethod m2 = pcType.newMethod();
        assertEquals(1, m2.getId());    
    }

    public void testGetMethods() {
        assertEquals(0, pcType.getMethods().length);
        IMethod m1 = pcType.newMethod();
        IMethod m2 = pcType.newMethod();
        assertSame(m1, pcType.getMethods()[0]);
        assertSame(m2, pcType.getMethods()[1]);
        
        // make sure a defensive copy is returned.
        pcType.getMethods()[0] = null;
        assertNotNull(pcType.getMethods()[0]);
    }

    public void testNewRule() {
        sourceFile.getIpsModel().addChangeListener(this);
        IValidationRule r = pcType.newRule();
        assertEquals(0, r.getId());    
        assertSame(pcType, r.getIpsObject());
        assertEquals(1, pcType.getNumOfRules());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        
        IValidationRule r2 = pcType.newRule();
        assertEquals(1, r2.getId());    
    }

    public void testGetRules() {
        assertEquals(0, pcType.getRules().length);
        IValidationRule r1 = pcType.newRule();
        IValidationRule r2 = pcType.newRule();
        assertSame(r1, pcType.getRules()[0]);
        assertSame(r2, pcType.getRules()[1]);
        
        // make sure a defensive copy is returned.
        pcType.getRules()[0] = null;
        assertNotNull(pcType.getRules()[0]);
    }

    public void testNewRelation() {
        sourceFile.getIpsModel().addChangeListener(this);
        IRelation r = pcType.newRelation();
        assertEquals(0, r.getId());    
        assertSame(pcType, r.getIpsObject());
        assertEquals(1, pcType.getNumOfRelations());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        
        IRelation r2 = pcType.newRelation();
        assertEquals(1, r2.getId());    
    }

    public void testGetRelations() {
        assertEquals(0, pcType.getRelations().length);
        IRelation r1 = pcType.newRelation();
        IRelation r2 = pcType.newRelation();
        assertSame(r1, pcType.getRelations()[0]);
        assertSame(r2, pcType.getRelations()[1]);
        
        // make sure a defensive copy is returned.
        pcType.getRelations()[0] = null;
        assertNotNull(pcType.getRelations()[0]);
    }
    
    public void testGetProductRelevantRelations() {
    	IRelation[] relations = pcType.getProductRelevantRelations();
    	assertEquals(0, relations.length);
    	
    	IRelation rel1 = pcType.newRelation();
    	IRelation rel2 = pcType.newRelation();
    	
    	rel1.setProductRelevant(false);
    	rel2.setProductRelevant(true);
    	
    	relations = pcType.getProductRelevantRelations();
    	assertEquals(1, relations.length);
    	assertSame(rel2, relations[0]);
    }
    
    public void testGetRelation() {
        assertNull(pcType.getRelation("unkown"));
        IRelation r1 = pcType.newRelation();
        r1.setTargetRoleSingular("r1");
        IRelation r2 = pcType.newRelation();
        r2.setTargetRoleSingular("r2");
        IRelation r3 = pcType.newRelation();
        r3.setTargetRoleSingular("r2");
        assertEquals(r2, pcType.getRelation("r2"));
    }
    
    public void testDependsOn() throws Exception {
        IPolicyCmptType a = newPolicyCmptType(root, "A");
        IPolicyCmptType b = newPolicyCmptType(root, "B");
        IPolicyCmptType c = newPolicyCmptType(root, "C");
        c.setSupertype(a.getQualifiedName());
        c.newRelation().setTarget(b.getQualifiedName());
        List dependsOnList = CollectionUtil.toArrayList(c.dependsOn());
        assertTrue(dependsOnList.contains(a.getQualifiedNameType()));
        assertTrue(dependsOnList.contains(b.getQualifiedNameType()));
    }
    
    public void testGetPdObjectType() {
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, pcType.getIpsObjectType());
    }

    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();
        pcType.setConfigurableByProductCmptType(false);
        pcType.initFromXml(element);
        assertTrue(pcType.isConfigurableByProductCmptType());
        assertEquals("Product", pcType.getUnqualifiedProductCmptType());
        assertEquals("SuperType", pcType.getSupertype());
        assertTrue(pcType.isAbstract());
        assertEquals("blabla", pcType.getDescription());
        
        IAttribute[] a = pcType.getAttributes();
        assertEquals(1, a.length);
        
        IMethod[] m = pcType.getMethods();
        assertEquals(1, m.length);
        
        IValidationRule[] rules = pcType.getRules();
        assertEquals(1, rules.length);
        
        IRelation[] r = pcType.getRelations();
        assertEquals(1, r.length);
        
        pcType.initFromXml(element);
        assertEquals(1, pcType.getNumOfAttributes());
        assertEquals(1, pcType.getNumOfMethods());
        assertEquals(1, pcType.getNumOfRelations());
        assertEquals(1, pcType.getNumOfRules());
        
        // test if the object references have remained the same
        assertSame(a[0], pcType.getAttributes()[0]);
        assertSame(r[0], pcType.getRelations()[0]);
        assertSame(m[0], pcType.getMethods()[0]);
        assertSame(rules[0], pcType.getRules()[0]);
    }
    
    public void testToXml() throws CoreException {
    	pcType.setConfigurableByProductCmptType(true);
    	pcType.setUnqualifiedProductCmptType("Product");
        pcType.setDescription("blabla");
        pcType.setAbstract(true);
        pcType.setSupertype("NewSuperType");
        IAttribute a1 = pcType.newAttribute();
        a1.setName("a1");
        IAttribute a2 = pcType.newAttribute();
        a2.setName("a2");
        IMethod m1 = pcType.newMethod();
        m1.setName("m1");
        IMethod m2 = pcType.newMethod();
        m2.setName("m2");
        IValidationRule rule1 = pcType.newRule();
        rule1.setName("rule1");
        IValidationRule rule2 = pcType.newRule();
        rule2.setName("rule2");
        IRelation r1 = pcType.newRelation();
        r1.setTarget("t1");
        IRelation r2 = pcType.newRelation();
        r2.setTarget("t2");
        
        Element element = pcType.toXml(this.newDocument());
        
        PolicyCmptType copy = (PolicyCmptType)newIpsObject(pdProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copy.setConfigurableByProductCmptType(false);
        copy.initFromXml(element);
        assertTrue(copy.isConfigurableByProductCmptType());
        assertEquals("Product", copy.getUnqualifiedProductCmptType());
        assertEquals("NewSuperType", copy.getSupertype());
        assertTrue(copy.isAbstract());
        assertEquals("blabla", copy.getDescription());
        IAttribute[] attributes = copy.getAttributes();
        assertEquals(2, attributes.length);
        assertEquals("a1", attributes[0].getName());
        assertEquals("a2", attributes[1].getName());
        IMethod[] methods = copy.getMethods();
        assertEquals("m1", methods[0].getName());
        assertEquals("m2", methods[1].getName());
        IValidationRule[] rules= copy.getRules();
        assertEquals("rule1", rules[0].getName());
        assertEquals("rule2", rules[1].getName());
        IRelation[] relations = copy.getRelations();
        assertEquals("t1", relations[0].getTarget());
        assertEquals("t2", relations[1].getTarget());
    }
    
    public void testGetSupertypeHierarchy() throws CoreException {
        ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
        assertNotNull(hierarchy);
    }
    
    public void testGetSubtypeHierarchy() throws CoreException {
        ITypeHierarchy hierarchy = pcType.getSubtypeHierarchy();
        assertNotNull(hierarchy);
    }
    
    public void testGetOverrideCandidates() throws CoreException {
        assertEquals(0, pcType.findOverrideCandidates(false).length);
        
        // create two more types that act as supertype and supertype's supertype 
        IIpsSrcFile file1 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        IPolicyCmptType supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        IPolicyCmptType supersupertype = (PolicyCmptType)file2.getIpsObject();
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        IMethod m1 = pcType.newMethod();
        m1.setName("calc");
        
        // supertype methods
        IMethod m2 = supertype.newMethod();
        m2.setName("calc");
        IMethod m3 = supertype.newMethod();
        m3.setName("calc");
        m3.setParameters(new Parameter[]{new Parameter(0, "p1", "Decimal")});
        
        // supersupertype methods
        IMethod m4 = supersupertype.newMethod();
        m4.setName("calc");
        m4.setParameters(new Parameter[]{new Parameter(0, "p1", "Decimal")});
        
        IMethod m5 = supersupertype.newMethod();
        m5.setName("calc");
        m5.setAbstract(true);
        m5.setParameters(new Parameter[]{new Parameter(0, "p1", "Money")});
        
        IMethod m6 = supersupertype.newMethod();
        m6.setModifier(Modifier.PRIVATE);
        m6.setName("getPremium");
        
        IMethod[] candidates = pcType.findOverrideCandidates(false);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes: 
        // m2 is not a candidate because it is already overriden by m1
        // m4 is not a candidate because it is the same as m3
        // m6 is not a candidate because it is private
        
        // only abstract methods
        candidates = pcType.findOverrideCandidates(true);
        assertEquals(1, candidates.length);
        assertEquals(m5, candidates[0]);
        // note: now only m5 is a candidate as it's abstract, m2 is not.
    }
    
    public void testOverride() throws CoreException {
        IIpsSrcFile file1 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        IPolicyCmptType supertype = (PolicyCmptType)file1.getIpsObject();
        IMethod m1 = supertype.newMethod();
        m1.setModifier(Modifier.PUBLISHED);
        m1.setAbstract(true);
        m1.setDatatype("int");
        m1.setName("m1");
        m1.setParameters(new Parameter[]{new Parameter(0, "p", "int")});

        IMethod m2 = supertype.newMethod();
        m1.setName("m2");
        
        pcType.override(new IMethod[]{m1, m2});
        assertEquals(2, pcType.getNumOfMethods());
        IMethod[] methods = pcType.getMethods();
        assertTrue(methods[0].isSame(m1));
        assertEquals("int", methods[0].getDatatype());
        assertEquals(Modifier.PUBLISHED, methods[0].getModifier());
        assertEquals("p", methods[0].getParameters()[0].getName());
        
        assertTrue(methods[1].isSame(m2));
    }
    
    public void testValidate_MustOverrideAbstractMethod() throws CoreException {
        int numOfMsg = pcType.validate().getNoOfMessages();
        
        // a supertype with a method and connect the pctype to it
        IPolicyCmptType superType = this.newPolicyCmptType(root, "Supertype");
        pcType.setSupertype(superType.getQualifiedName());
        IMethod superMethod = superType.newMethod();
        superMethod.setName("calc");

        // method is not abstract so no error message should be returned.
        MessageList list = pcType.validate();
        assertEquals(numOfMsg, list.getNoOfMessages());

        // set method to abstract, now the error should be reported
        superMethod.setAbstract(true);
        list = pcType.validate();
        assertTrue(list.getNoOfMessages() > numOfMsg);
        Message msg = list.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD);
        assertEquals(pcType, msg.getInvalidObjectProperties()[0].getObject());

        // "implement" the method in pcType => error should no be reported anymore
        pcType.override(new IMethod[]{superMethod});
        list = pcType.validate();
        assertEquals(numOfMsg, list.getNoOfMessages());
        
        // create another level in the supertype hierarchy with an abstract method on the new supersupertype.
        // an error should be reported
        IPolicyCmptType supersuperType = this.newPolicyCmptType(root, "Supersupertype");
        superType.setSupertype(supersuperType.getQualifiedName());
        IMethod supersuperMethod = supersuperType.newMethod();
        supersuperMethod.setName("calc2");
        supersuperMethod.setAbstract(true);
        list = pcType.validate();
        assertTrue(list.getNoOfMessages()>numOfMsg);
        msg = list.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD);
        assertEquals(pcType, msg.getInvalidObjectProperties()[0].getObject());
        
        // "implement" the method in the supertype => error should no be reported anymore
        superType.override(new IMethod[]{supersuperMethod});
        list = pcType.validate();
        assertEquals(numOfMsg, list.getNoOfMessages());
        
    }
    
    public void testGetProductCmptType() {
    	pcType.setUnqualifiedProductCmptType("MotorProduct");
    	assertEquals(pack.getName() + '.' + "MotorProduct", pcType.getProductCmptType());
    }
    
    public void testFindProductCmptType() throws CoreException {
    	pcType.setUnqualifiedProductCmptType("MotorProduct");
    	pcType.setConfigurableByProductCmptType(false);
    	assertNull(pcType.findProductCmptType());
    	
    	pcType.setConfigurableByProductCmptType(true);
    	assertNotNull(pcType.findProductCmptType());
    	
    	pcType.setUnqualifiedProductCmptType("");
    	assertNull(pcType.findProductCmptType());
    }
    
    /** 
     * Overridden.
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

    public void testNewPart() {
    	try {
    		assertTrue(pcType.newPart(IAttribute.class) instanceof IAttribute);
    		assertTrue(pcType.newPart(IMethod.class) instanceof IMethod);
    		assertTrue(pcType.newPart(IRelation.class) instanceof IRelation);
    		assertTrue(pcType.newPart(IValidationRule.class) instanceof IValidationRule);
    		
    		pcType.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}