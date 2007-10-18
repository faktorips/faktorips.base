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

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public class TypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "MotorProduct");
    }
    
    public void testFindAttribute() throws CoreException {
        assertNull(type.findAttribute("unknown", ipsProject));
        
        IAttribute a1 = type.newAttribute();
        a1.setName("a1");
        
        IType supertype = newProductCmptType(ipsProject, "Supertype");
        type.setSupertype(supertype.getQualifiedName());
        
        IAttribute a2 = supertype.newAttribute();
        a2.setName("a2");
        
        IType superSupertype = newProductCmptType(ipsProject, "SuperSupertype");
        supertype.setSupertype(superSupertype.getQualifiedName());
        IAttribute a3 = superSupertype.newAttribute();
        a3.setName("a3");
        
        assertSame(a1, type.findAttribute("a1", ipsProject));
        assertSame(a2, type.findAttribute("a2", ipsProject));
        assertSame(a3, type.findAttribute("a3", ipsProject));
        
        IAttribute a1b = supertype.newAttribute();
        a1b.setName("a1b");
        assertSame(a1, type.findAttribute("a1", ipsProject));
        
        assertNull(type.findAttribute("unknown", ipsProject));
    }
    
    public void testNewAttribute() {
        IAttribute attr = type.newAttribute();
        assertEquals(1, type.getAttributes().length);
        assertEquals(attr, type.getAttributes()[0]);
    }
    
    public void testGetAttribute() {
        assertNull(type.getAttribute("a"));
        
        IAttribute a1 = type.newAttribute();
        type.newAttribute();
        IAttribute a3 = type.newAttribute();
        a1.setName("a1");
        a3.setName("a3");
        
        assertEquals(a1, type.getAttribute("a1"));
        assertEquals(a3, type.getAttribute("a3"));
        assertNull(type.getAttribute("unkown"));
        
        assertNull(type.getAttribute(null));
    }

    public void testGetAttributes() {
        assertEquals(0, type.getAttributes().length);

        IAttribute a1 = type.newAttribute();
        IAttribute[] attributes = type.getAttributes();
        assertEquals(a1, attributes[0]);
        
        IAttribute a2 = type.newAttribute();
        attributes = type.getAttributes();
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
    }

    public void testGetNumOfAttributes() {
        assertEquals(0, type.getNumOfAttributes());
        
        type.newAttribute();
        assertEquals(1, type.getNumOfAttributes());
        
        type.newAttribute();
        assertEquals(2, type.getNumOfAttributes());
    }

    public void testMoveAttributes() {
        IAttribute a1 = type.newAttribute();
        IAttribute a2 = type.newAttribute();
        IAttribute a3 = type.newAttribute();
        
        type.moveAttributes(new int[]{1, 2}, true);
        IAttribute[] attributes = type.getAttributes();
        assertEquals(a2, attributes[0]);
        assertEquals(a3, attributes[1]);
        assertEquals(a1, attributes[2]);
    }
    
    public void testGetAssociationsForTarget() {
        assertEquals(0, type.getAssociationsForTarget(null).length);
        
        IAssociation ass1 = type.newAssociation();
        ass1.setTarget("Target1");
        IAssociation ass2 = type.newAssociation();
        ass2.setTarget("Target2");
        IAssociation ass3 = type.newAssociation();
        ass3.setTarget("Target1");
        
        IAssociation[] ass = type.getAssociationsForTarget("Target1");
        assertEquals(2, ass.length);
        assertEquals(ass1, ass[0]);
        assertEquals(ass3, ass[1]);
        
        ass = type.getAssociationsForTarget("UnknownTarget");
        assertEquals(0, ass.length);
    }
    
    public void testGetMethods() {
        assertEquals(0, type.getMethods().length);
        IMethod m1 = type.newMethod();
        IMethod m2 = type.newMethod();
        assertSame(m1, type.getMethods()[0]);
        assertSame(m2, type.getMethods()[1]);
        
        // make sure a defensive copy is returned.
        type.getMethods()[0] = null;
        assertNotNull(type.getMethods()[0]);
    }

    public void testGetOverrideCandidates() throws CoreException {
        assertEquals(0, type.findOverrideMethodCandidates(false, ipsProject).length);
        
        // create two more types that act as supertype and supertype's supertype 
        IType supertype = newProductCmptType(ipsProject, "Supertype");
        IType supersupertype = newProductCmptType(ipsProject, "Supersupertype");
        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        IMethod m1 = type.newMethod();
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
        m5.setAbstract(true);        
        m5.newParameter("Money", "p1");
        
        IMethod[] candidates = type.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes: 
        // m2 is not a candidate because it is already overridden by m1
        // m4 is not a candidate because it is overridden by m3 and m3 comes first in the hierarchy
        
        // only not implemented abstract methods
        candidates = type.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(1, candidates.length);
        assertEquals(m5, candidates[0]);
        // note: now only m5 is a candidate as it's abstract, m3 is not.
        
        // override the supersupertype method m5 in the supertype
        // => now also m5 is not a candidate any more, if only not implemented abstract methods are requested.
        supertype.overrideMethods(new IMethod[]{m5});
        candidates = type.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(0, candidates.length);
    }
    
    public void testValidate_SupertypeNotFound() throws Exception {
        MessageList ml = type.validate();
        assertNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));

        type.setSupertype("abc");
        ml = type.validate();
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));
        
        IType supertype = newProductCmptType(ipsProject, "Product");
        type.setSupertype(supertype.getQualifiedName());
        ml = type.validate();
        assertNull(ml.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));
    }
    
    public void testValidate_CycleInTypeHirarchy() throws Exception {
        // create two more types that act as supertype and supertype's supertype 
        IType supertype = newProductCmptType(ipsProject, "Product");
        IType supersupertype = newProductCmptType(ipsProject, "BaseProduct");

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        MessageList ml = type.validate();
        Message msg = ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        assertNull(msg);
        
        supersupertype.setSupertype(type.getQualifiedName());
        
        ml = type.validate();
        msg = ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        
        assertNotNull(msg);
        assertEquals(1, msg.getInvalidObjectProperties().length);
        assertEquals(IPolicyCmptType.PROPERTY_SUPERTYPE, msg.getInvalidObjectProperties()[0].getProperty());
        assertEquals(type, msg.getInvalidObjectProperties()[0].getObject());
        
        type.setSupertype(type.getQualifiedName());
        ml = type.validate();
        msg = ml.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY);
        assertNotNull(msg);
    }

    public void testValidate_InconsistentTypeHirachy() throws Exception {
        // create two more types that act as supertype and supertype's supertype 
        // create two more types that act as supertype and supertype's supertype 
        IType supertype = newProductCmptType(ipsProject, "Product");
        IType supersupertype = newProductCmptType(ipsProject, "BaseProduct");

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        MessageList ml = type.validate();
        assertNull(ml.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
        
        supersupertype.setSupertype("abc");
        ml = type.validate();
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    public void testFindOverrideMethodCandidates() throws CoreException {
        assertEquals(0, type.findOverrideMethodCandidates(false, ipsProject).length);
        
        // create two more types that act as supertype and supertype's supertype 
        IType supertype = newProductCmptType(ipsProject, "Product");
        IType supersupertype = newProductCmptType(ipsProject, "BaseProduct");
        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        IMethod m1 = type.newMethod();
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
        m5.setAbstract(true);        
        m5.newParameter("Money", "p1");
        
        IMethod[] candidates = type.findOverrideMethodCandidates(false, ipsProject);
        assertEquals(2, candidates.length);
        assertEquals(m3, candidates[0]);
        assertEquals(m5, candidates[1]);
        // notes: 
        // m2 is not a candidate because it is already overriden by m1
        // m4 is not a candidate because it is the same as m3
        
        // only abstract methods
        candidates = type.findOverrideMethodCandidates(true, ipsProject);
        assertEquals(1, candidates.length);
        assertEquals(m5, candidates[0]);
        // note: now only m5 is a candidate as it's abstract, m2 is not.
    }
    
    public void testOverrideMethods() throws CoreException {
        IType supertype = newProductCmptType(ipsProject, "Product");
        IMethod m1 = supertype.newMethod();
        m1.setModifier(Modifier.PUBLISHED);
        m1.setAbstract(true);
        m1.setDatatype("int");
        m1.setName("m1");
        m1.newParameter("int", "p");

        IMethod m2 = supertype.newMethod();
        m1.setName("m2");
        
        type.overrideMethods(new IMethod[]{m1, m2});
        assertEquals(2, type.getNumOfMethods());
        IMethod[] methods = type.getMethods();
        assertTrue(methods[0].overrides(m1));
        assertEquals("int", methods[0].getDatatype());
        assertEquals(Modifier.PUBLISHED, methods[0].getModifier());
        assertEquals("p", methods[0].getParameters()[0].getName());
        
        assertTrue(methods[1].overrides(m2));
    }
    


}
