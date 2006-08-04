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

    private ITestPolicyCmpt policyCmptTypeObjectInput;
    
    /**
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCase type = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        policyCmptTypeObjectInput = type.newExpectedResultPolicyCmpt();
    }
 
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl,"PolicyCmptTypeObject",0);
        policyCmptTypeObjectInput.initFromXml(paramEl);
        assertEquals("base.Test1", policyCmptTypeObjectInput.getTestPolicyCmptType());   
        assertEquals("productCmpt1", policyCmptTypeObjectInput.getProductCmpt());
        assertEquals("Label1", policyCmptTypeObjectInput.getLabel());
        assertEquals(2, policyCmptTypeObjectInput.getTestPolicyCmptRelations().length);
        assertEquals(3, policyCmptTypeObjectInput.getTestAttributeValues().length);
        assertRelation(policyCmptTypeObjectInput.getTestPcTypeRelation("relation2"), "base.Test2");  
        
        assertTrue(policyCmptTypeObjectInput.getTestPolicyCmptRelations()[0].isAccoziation());
        assertFalse(policyCmptTypeObjectInput.getTestPolicyCmptRelations()[0].isComposition());
        assertFalse(policyCmptTypeObjectInput.getTestPolicyCmptRelations()[1].isAccoziation());
        assertTrue(policyCmptTypeObjectInput.getTestPolicyCmptRelations()[1].isComposition());
    }

    public void testToXml() {
        policyCmptTypeObjectInput.setTestPolicyCmptType("base.Test2");
        policyCmptTypeObjectInput.setProductCmpt("productCmpt1");
        policyCmptTypeObjectInput.setLabel("Label1");
        policyCmptTypeObjectInput.newTestPolicyCmptRelation();
        ITestPolicyCmptRelation relation = policyCmptTypeObjectInput.newTestPolicyCmptRelation();
        relation.setTestPolicyCmptType("relation1");
        ITestPolicyCmpt targetChild = relation.newTargetTestPolicyCmptChild();
        targetChild.setTestPolicyCmptType("base.Test4");
        policyCmptTypeObjectInput.newTestAttributeValue();
        
        Element el = policyCmptTypeObjectInput.toXml(newDocument());
        
        policyCmptTypeObjectInput.setTestPolicyCmptType("base.Test3");
        policyCmptTypeObjectInput.setProductCmpt("productCmpt2");
        policyCmptTypeObjectInput.setLabel("Label2");
        policyCmptTypeObjectInput.newTestAttributeValue();
        policyCmptTypeObjectInput.newTestAttributeValue();
        policyCmptTypeObjectInput.newTestPolicyCmptRelation();
        
        policyCmptTypeObjectInput.initFromXml(el);
        assertEquals("base.Test2", policyCmptTypeObjectInput.getTestPolicyCmptType());
        assertEquals("Label1", policyCmptTypeObjectInput.getLabel());
        assertEquals(2, policyCmptTypeObjectInput.getTestPolicyCmptRelations().length);
        assertEquals(1, policyCmptTypeObjectInput.getTestAttributeValues().length);
        assertRelation(policyCmptTypeObjectInput.getTestPcTypeRelation("relation1"),
                "base.Test4");
        assertEquals("productCmpt1", policyCmptTypeObjectInput.getProductCmpt());
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
