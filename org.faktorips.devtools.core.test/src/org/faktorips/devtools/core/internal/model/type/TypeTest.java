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
        assertTrue(methods[0].isSame(m1));
        assertEquals("int", methods[0].getDatatype());
        assertEquals(Modifier.PUBLISHED, methods[0].getModifier());
        assertEquals("p", methods[0].getParameters()[0].getName());
        
        assertTrue(methods[1].isSame(m2));
    }
    


}
