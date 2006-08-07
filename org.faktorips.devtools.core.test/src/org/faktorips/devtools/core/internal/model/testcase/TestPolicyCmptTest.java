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

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTest extends AbstractIpsPluginTest {

    private ITestPolicyCmpt policyCmptTypeObjectExpected;
    private ITestPolicyCmpt policyCmptTypeObjectInput;
    
    /**
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        policyCmptTypeObjectExpected = testCase.newExpectedResultPolicyCmpt();
        policyCmptTypeObjectInput = testCase.newInputPolicyCmpt();
    }
 
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl,"PolicyCmptTypeObject",0);
        policyCmptTypeObjectExpected.initFromXml(paramEl);
        assertEquals("base.Test1", policyCmptTypeObjectExpected.getTestPolicyCmptType());   
        assertEquals("productCmpt1", policyCmptTypeObjectExpected.getProductCmpt());
        assertEquals("Label1", policyCmptTypeObjectExpected.getLabel());
        assertEquals(2, policyCmptTypeObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(3, policyCmptTypeObjectExpected.getTestAttributeValues().length);
        assertRelation(policyCmptTypeObjectExpected.getTestPcTypeRelation("relation2"), "base.Test2");  
        
        assertTrue(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[0].isAccoziation());
        assertFalse(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[0].isComposition());
        assertFalse(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[1].isAccoziation());
        assertTrue(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[1].isComposition());
    }

    public void testToXml() {
        policyCmptTypeObjectExpected.setTestPolicyCmptType("base.Test2");
        policyCmptTypeObjectExpected.setProductCmpt("productCmpt1");
        policyCmptTypeObjectExpected.setLabel("Label1");
        policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmptRelation relation = policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        relation.setTestPolicyCmptType("relation1");
        ITestPolicyCmpt targetChild = relation.newTargetTestPolicyCmptChild();
        targetChild.setTestPolicyCmptType("base.Test4");
        policyCmptTypeObjectExpected.newTestAttributeValue();
        
        Element el = policyCmptTypeObjectExpected.toXml(newDocument());
        
        policyCmptTypeObjectExpected.setTestPolicyCmptType("base.Test3");
        policyCmptTypeObjectExpected.setProductCmpt("productCmpt2");
        policyCmptTypeObjectExpected.setLabel("Label2");
        policyCmptTypeObjectExpected.newTestAttributeValue();
        policyCmptTypeObjectExpected.newTestAttributeValue();
        policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        
        policyCmptTypeObjectExpected.initFromXml(el);
        assertEquals("base.Test2", policyCmptTypeObjectExpected.getTestPolicyCmptType());
        assertEquals("Label1", policyCmptTypeObjectExpected.getLabel());
        assertEquals(2, policyCmptTypeObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(1, policyCmptTypeObjectExpected.getTestAttributeValues().length);
        assertRelation(policyCmptTypeObjectExpected.getTestPcTypeRelation("relation1"),
                "base.Test4");
        assertEquals("productCmpt1", policyCmptTypeObjectExpected.getProductCmpt());
    }
    
    public void testInputOrExpectedResultObject(){
        // test if newly created child test policy component objects inherit 
        // the correct input or expected result flag
        assertFalse(policyCmptTypeObjectExpected.isInputObject());
        ITestPolicyCmptRelation r = policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPc = r.newTargetTestPolicyCmptChild();
        assertFalse(testPc.isInputObject());
        
        assertTrue(policyCmptTypeObjectInput.isInputObject());
        r = policyCmptTypeObjectInput.newTestPolicyCmptRelation();
        testPc = r.newTargetTestPolicyCmptChild();
        assertTrue(testPc.isInputObject());
    }
    
    private void assertRelation(ITestPolicyCmptRelation relation, String policyCmptTypeName) {
        assertNotNull(relation);
        ITestPolicyCmpt targetChild = null;
        try {
            targetChild = relation.findTarget();
        } catch (CoreException e) {
            fail(e.getLocalizedMessage());
        }
        assertNotNull(targetChild);
        assertEquals(policyCmptTypeName, targetChild.getTestPolicyCmptType());
    }    
}
