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
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IParameter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class PolicyCmptTypeTest extends AbstractIpsPluginTest implements ContentsChangeListener {
    
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile sourceFile;
    private PolicyCmptType pcType;
    private ContentChangeEvent lastEvent;
    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)sourceFile.getIpsObject();
        pcType.setConfigurableByProductCmptType(false);
    }
    
    public void testIsSubtype() throws CoreException {
        assertFalse(pcType.isSubtypeOf(null));
        
        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        assertFalse(pcType.isSubtypeOf(supertype));
        pcType.setSupertype(supertype.getQualifiedName());
        assertTrue(pcType.isSubtypeOf(supertype));
        
        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        assertFalse(pcType.isSubtypeOf(supersupertype));
        supertype.setSupertype(supersupertype.getQualifiedName());
        assertTrue(pcType.isSubtypeOf(supersupertype));
        
        assertFalse(supertype.isSubtypeOf(pcType));
    }
    
    public void testIsSubtypeOrSameType() throws CoreException {
        assertFalse(pcType.isSubtypeOrSameType(null));
        
        assertTrue(pcType.isSubtypeOrSameType(pcType));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        assertFalse(pcType.isSubtypeOrSameType(supertype));
        pcType.setSupertype(supertype.getQualifiedName());
        assertTrue(pcType.isSubtypeOrSameType(supertype));
        
        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        assertFalse(pcType.isSubtypeOrSameType(supersupertype));
        supertype.setSupertype(supersupertype.getQualifiedName());
        assertTrue(pcType.isSubtypeOrSameType(supersupertype));
        
        assertFalse(supertype.isSubtypeOf(pcType));
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
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setProductRelevant(true);
        assertFalse(pcType.isExtensionCompilationUnitGenerated());
        attribute.setProductRelevant(false);
        assertTrue(pcType.isExtensionCompilationUnitGenerated());
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
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
        pcType.setConfigurableByProductCmptType(true);
        ITableStructureUsage tsu = pcType.newTableStructureUsage();
        
        IIpsElement[] elements = pcType.getChildren();
        assertEquals(5, elements.length);
        assertEquals(a1, elements[0]);
        assertEquals(m1, elements[1]);
        assertEquals(r1, elements[2]);
        assertEquals(rule1, elements[3]);
        assertEquals(tsu, elements[4]);
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
        assertEquals(2, dependsOnList.size());
        assertTrue(dependsOnList.contains(a.getQualifiedNameType()));
        assertTrue(dependsOnList.contains(b.getQualifiedNameType()));
        
        // test if a cicle in the type hierarchy does not lead to a stack overflow exception
        c.setSupertype(c.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(c.dependsOn());
        assertEquals(1, dependsOnList.size());
        assertTrue(dependsOnList.contains(b.getQualifiedNameType()));
        
        c.setSupertype(a.getQualifiedName());
        a.setSupertype(c.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(c.dependsOn());
        assertEquals(2, dependsOnList.size());
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
        
        // test table structure
        assertEquals(1, pcType.getTableStructureUsages().length);
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
        ITableStructureUsage tsu = pcType.newTableStructureUsage();
        tsu.setRoleName("role1");
        
        Element element = pcType.toXml(this.newDocument());
        
        PolicyCmptType copy = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
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
        assertEquals(1, copy.getTableStructureUsages().length);
        assertEquals("role1", copy.getTableStructureUsages()[0].getRoleName());
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
        assertEquals(0, pcType.findOverrideMethodCandidates(false).length);
        
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
        m3.newParameter("Decimal", "p1");
        
        // supersupertype methods
        IMethod m4 = supersupertype.newMethod();
        m4.setName("calc");
        m4.newParameter("Decimal", "p1");
        
        IMethod m5 = supersupertype.newMethod();
        m5.setName("calc");
        m5.setAbstract(true);        m5.newParameter("Money", "p1");
        
        IMethod[] candidates = pcType.findOverrideMethodCandidates(false);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes: 
        // m2 is not a candidate because it is already overriden by m1
        // m4 is not a candidate because it is the same as m3
        
        // only abstract methods
        candidates = pcType.findOverrideMethodCandidates(true);
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
        IParameter p1 = m1.newParameter();
        p1.setName("p");
        p1.setDatatype("int");
              

        IMethod m2 = supertype.newMethod();
        m1.setName("m2");
        
        pcType.overrideMethods(new IMethod[]{m1, m2});
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
        superType.setAbstract(true);
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
        pcType.overrideMethods(new IMethod[]{superMethod});
        list = pcType.validate();
        assertEquals(numOfMsg, list.getNoOfMessages());
        
        // create another level in the supertype hierarchy with an abstract method on the new supersupertype.
        // an error should be reported
        IPolicyCmptType supersuperType = this.newPolicyCmptType(root, "Supersupertype");
        supersuperType.setAbstract(true);
        superType.setSupertype(supersuperType.getQualifiedName());
        IMethod supersuperMethod = supersuperType.newMethod();
        supersuperMethod.setName("calc2");
        supersuperMethod.setAbstract(true);
        list = pcType.validate();
        assertTrue(list.getNoOfMessages()>numOfMsg);
        msg = list.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD);
        assertEquals(pcType, msg.getInvalidObjectProperties()[0].getObject());
        
        // "implement" the method in the supertype => error should no be reported anymore
        superType.overrideMethods(new IMethod[]{supersuperMethod});
        list = pcType.validate();
        assertEquals(numOfMsg, list.getNoOfMessages());
        
    }
    
    public void testGetProductCmptType() throws CoreException {
    	pcType.setUnqualifiedProductCmptType("MotorProduct");
    	assertEquals(pack.getName() + '.' + "MotorProduct", pcType.getProductCmptType());
        
        pcType = newPolicyCmptType(ipsProject, "Type");
        pcType.setUnqualifiedProductCmptType("MotorProduct");
        assertEquals("MotorProduct", pcType.getProductCmptType());
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
    
    public void testIsAggregateRoot() throws CoreException {
    	assertTrue(pcType.isAggregateRoot());
    	
    	pcType.newRelation().setRelationType(RelationType.ASSOZIATION);
    	assertTrue(pcType.isAggregateRoot());

    	pcType.newRelation().setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
    	assertTrue(pcType.isAggregateRoot());
    	
    	pcType.newRelation().setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
    	assertFalse(pcType.isAggregateRoot());
    	
    	// create a supertype
    	IPolicyCmptType subtype = newPolicyCmptType(ipsProject, "Subtype");
    	IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
    	subtype.setSupertype(supertype.getQualifiedName());
    	subtype.getIpsSrcFile().save(true, null);
    	
    	supertype.newRelation().setRelationType(RelationType.ASSOZIATION);
    	assertTrue(subtype.isAggregateRoot());
    	supertype.newRelation().setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
    	assertTrue(subtype.isAggregateRoot());
    	supertype.newRelation().setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
    	assertFalse(subtype.isAggregateRoot());
        
        IPolicyCmptType invalidType = newPolicyCmptType(ipsProject, "InvalidType");
        invalidType.setSupertype(invalidType.getQualifiedName());
        assertTrue(invalidType.isAggregateRoot());
    }
    
    public void testValidate_SupertypeNotFound() throws Exception {
    	MessageList ml = pcType.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_FOUND));
    	pcType.setSupertype("abc");
    	ml = pcType.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_FOUND));
    }

    public void testValidate_ProductCmptTypeNameMissing() throws Exception {
    	MessageList ml = pcType.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING));
    	pcType.setConfigurableByProductCmptType(true);
    	pcType.setUnqualifiedProductCmptType("");
    	ml = pcType.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING));
    }

    public void testValidate_AbstractMissing() throws Exception {
    	MessageList ml = pcType.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_ABSTRACT_MISSING));
    	pcType.newMethod().setAbstract(true);
    	pcType.setAbstract(false);
    	ml = pcType.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_ABSTRACT_MISSING));
    }

    public void testValidate_CycleInTypeHirarchy() throws Exception {
        // create two more types that act as supertype and supertype's supertype 
        IIpsSrcFile file1 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        PolicyCmptType supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        PolicyCmptType supersupertype = (PolicyCmptType)file2.getIpsObject();

        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        MessageList ml = pcType.validate();
    	Message msg = ml.getMessageByCode(IPolicyCmptType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        assertNull(msg);
    	
    	supersupertype.setSupertype(pcType.getQualifiedName());
    	
    	ml = pcType.validate();
        msg = ml.getMessageByCode(IPolicyCmptType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        assertNotNull(msg);
        assertEquals(1, msg.getInvalidObjectProperties().length);
        assertEquals(IPolicyCmptType.PROPERTY_SUPERTYPE, msg.getInvalidObjectProperties()[0].getProperty());
        assertEquals(pcType, msg.getInvalidObjectProperties()[0].getObject());
    }

// deactivated because at the moment unimplemented container relations are valid...
//    public void testValidate_MustImplementeAbstractRelation() throws Exception {
//    	MessageList ml = pcType.validate();
//    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_ABSTRACT_RELATION));
//    	
//    	PolicyCmptType supertype = newPolicyCmptType(ipsProject, "base.SuperType");
//    	PolicyCmptType target = newPolicyCmptType(ipsProject, "base.Target");
//
//    	pcType.setSupertype(supertype.getQualifiedName());
//    	
//    	IRelation rel = supertype.newRelation();
//    	rel.setTarget(target.getQualifiedName());
//    	rel.setReadOnlyContainer(true);
//    	
//    	ml = pcType.validate();
//    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_ABSTRACT_RELATION));
//    	
//    	pcType.setAbstract(true);
//    	ml = pcType.validate();
//    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_ABSTRACT_RELATION));
//    }

    public void testValidate_InconsistentTypeHirachy() throws Exception {
        // create two more types that act as supertype and supertype's supertype 
        IIpsSrcFile file1 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        PolicyCmptType supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        PolicyCmptType supersupertype = (PolicyCmptType)file2.getIpsObject();

        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
    	supersupertype.setAbstract(false);
    	supersupertype.newMethod().setAbstract(true);

        MessageList ml = pcType.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

    	supersupertype.setSupertype("abc");
    	
    	ml = pcType.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    public void testFindOverwriteAttributeCandidates() throws Exception {
    	IAttribute[] candidates = pcType.findOverrideAttributeCandidates();
    	
    	assertEquals(0, candidates.length);

        PolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        PolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "Supersupertype");

        supertype.setSupertype(supersupertype.getQualifiedName());
        pcType.setSupertype(supertype.getQualifiedName());
    	
    	candidates = pcType.findOverrideAttributeCandidates();
    	assertEquals(0, candidates.length);
    	
    	supersupertype.newAttribute().setName("name");
    	supertype.newAttribute().setName("name2");
    	
    	candidates = pcType.findOverrideAttributeCandidates();
    	assertEquals(2, candidates.length);
    	
    	IAttribute attr = pcType.newAttribute();
    	attr.setName("name");
    	
    	candidates = pcType.findOverrideAttributeCandidates();
    	assertEquals(2, candidates.length);
    	
    	attr.setOverwrites(true);

    	candidates = pcType.findOverrideAttributeCandidates();
    	assertEquals(1, candidates.length);
    }

    public void testValidateProductCmptTypeNameMissmatch() throws Exception {
        pcType.setUnqualifiedProductCmptType(pcType.getName());
        pcType.setConfigurableByProductCmptType(true);

        MessageList ml = pcType.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSMATCH));
        
        pcType.setUnqualifiedProductCmptType(pcType.getName() + "Art");
        
        ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSMATCH));
    }

    public void testValidateProductCmptTypeNameInvalid() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        pcType.setUnqualifiedProductCmptType("a bc");
        MessageList ml = pcType.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_INVALID_PRODUCT_CMPT_TYPE_NAME));
        
        pcType.setUnqualifiedProductCmptType("abc");
        
        ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_INVALID_PRODUCT_CMPT_TYPE_NAME));
    }
    
    public void testValidateMustImplementContainerRelation() throws Exception {
        IPolicyCmptType target = newPolicyCmptType(ipsProject, "TargetType");
        
        MessageList ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        
        IRelation container = pcType.newRelation();
        container.setReadOnlyContainer(true);
        container.setTargetRoleSingular("Target");
        container.setTarget(target.getQualifiedName());
        
        ml = pcType.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        
        // test if the rule is not executed when disabled
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setContainerRelationIsImplementedRuleEnabled(false);
        ipsProject.setProperties(props);
        ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        props.setContainerRelationIsImplementedRuleEnabled(true);
        ipsProject.setProperties(props);

        // type is valid, if it is abstract
        pcType.setAbstract(true);
        ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        pcType.setAbstract(false);
        
        // implement the container relation in the same type
        IRelation relation = pcType.newRelation();
        relation.setReadOnlyContainer(false);
        relation.setContainerRelation(container.getName());
        relation.setTarget(target.getQualifiedName());
        
        ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        
        // delete the relation, now same thing for a subtype
        relation.delete();
        IPolicyCmptType subtype = newPolicyCmptType(ipsProject, "Subtype");
        subtype.setSupertype(pcType.getQualifiedName());
        ml = subtype.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        
        // type is valid, if it is abstract
        subtype.setAbstract(true);
        ml = subtype.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        subtype.setAbstract(false);

        relation = subtype.newRelation();
        relation.setReadOnlyContainer(false);
        relation.setContainerRelation(container.getName());
        relation.setTarget(target.getQualifiedName());
        
        ml = subtype.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
        
        // now same thing for subtype of subtype
        relation.delete();
        IPolicyCmptType subsubtype = newPolicyCmptType(ipsProject, "SubSubtype");
        subsubtype.setSupertype(subtype.getQualifiedName());
        ml = subsubtype.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));

        relation = subtype.newRelation();
        relation.setReadOnlyContainer(false);
        relation.setContainerRelation(container.getName());
        relation.setTarget(target.getQualifiedName());
        
        ml = subtype.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION));
    }
    
    public void testSupertypeNotProductRelevantIfTheTypeIsProductRelevant() throws Exception{
        IPolicyCmptType superPcType = newPolicyCmptType(ipsProject, "Super");
        pcType.setSupertype(superPcType.getQualifiedName());
        
        superPcType.setConfigurableByProductCmptType(false);
        pcType.setConfigurableByProductCmptType(true);
        
        MessageList ml = superPcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
        
        ml = pcType.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
        
        superPcType.setConfigurableByProductCmptType(true);
        ml = pcType.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT));
    }
    
    public void testFindRelationsImplementingContainerRelation() throws CoreException {
        assertEquals(0, pcType.findRelationsImplementingContainerRelation(null, true).length);
        
        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        pcType.setSupertype(supertype.getQualifiedName());
        IRelation container = supertype.newRelation();
        container.setReadOnlyContainer(true);
        container.setTargetRoleSingular("Target");
        
        IRelation[] relations = pcType.findRelationsImplementingContainerRelation(container, true);
        assertEquals(0, relations.length);
        
        IRelation relation0 = pcType.newRelation();
        relation0.setContainerRelation(container.getName());
        pcType.newRelation(); // relation1
        
        relations = pcType.findRelationsImplementingContainerRelation(container, true);
        assertEquals(1, relations.length);
        assertEquals(relation0, relations[0]);
        
        relations = pcType.findRelationsImplementingContainerRelation(container, false);
        assertEquals(1, relations.length);
        assertEquals(relation0, relations[0]);
        
        IRelation relation2 = supertype.newRelation();
        relation2.setContainerRelation(container.getName());
        
        relations = pcType.findRelationsImplementingContainerRelation(container, true);
        assertEquals(2, relations.length);
        assertEquals(relation0, relations[0]);
        assertEquals(relation2, relations[1]);
        
        relations = pcType.findRelationsImplementingContainerRelation(container, false);
        assertEquals(1, relations.length);
        assertEquals(relation0, relations[0]);

        // test if relations above the container relation aren't found
        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        supertype.setSupertype(supersupertype.getQualifiedName());
        IRelation relation3 = supersupertype.newRelation();
        relation3.setContainerRelation(container.getName());
        relations = pcType.findRelationsImplementingContainerRelation(container, true);
        assertEquals(2, relations.length);
        assertEquals(relation0, relations[0]);
        assertEquals(relation2, relations[1]);
    }
    
    public void testNewTableStructure(){
        pcType.setConfigurableByProductCmptType(true);
        sourceFile.getIpsModel().addChangeListener(this);
        ITableStructureUsage tsu = pcType.newTableStructureUsage();
        assertEquals(0, tsu.getId());
        assertSame(pcType, tsu.getIpsObject());
        assertEquals(1, pcType.getNumOfTableStructureUsage());
        assertTrue(sourceFile.isDirty());
        assertEquals(sourceFile, lastEvent.getIpsSrcFile());
        
        ITableStructureUsage tsu2 = pcType.newTableStructureUsage();
        assertEquals(1, tsu2.getId());  
        
        // if the pcType is not configurated by a product cmpt
        // check if the new method returns null
        pcType.setConfigurableByProductCmptType(false);
        assertNull(pcType.newTableStructureUsage());
    }
    
    public void testGetTableStructureUsage(){
        pcType.setConfigurableByProductCmptType(true);
        assertEquals(0, pcType.getTableStructureUsages().length);
        ITableStructureUsage tsu1 = pcType.newTableStructureUsage();
        ITableStructureUsage tsu2 = pcType.newTableStructureUsage();
        tsu1.setRoleName("role1");
        tsu2.setRoleName("role2");
        assertSame(tsu1, pcType.getTableStructureUsages()[0]);
        assertSame(tsu2, pcType.getTableStructureUsages()[1]);
        assertSame(tsu2, pcType.getTableStructureUsage("role2"));
        assertSame(tsu1, pcType.getTableStructureUsage("role1"));
        assertNull(pcType.getTableStructureUsage("role12"));
        
        // make sure a defensive copy is returned.
        pcType.getTableStructureUsages()[0] = null;
        assertNotNull(pcType.getTableStructureUsages()[0]);
    }
}