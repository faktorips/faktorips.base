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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTypeParameterTest extends AbstractIpsPluginTest {

    private ITestPolicyCmptTypeParameter policyCmptTypeParameterInput;
    
    /**
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType )newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        policyCmptTypeParameterInput = type.newInputPolicyCmptTypeParameter();
    }
    
    public void testIsRootParameter() {
        assertTrue(policyCmptTypeParameterInput.isRootParameter());
        ITestPolicyCmptTypeParameter targetChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();    
        assertFalse(targetChild.isRootParameter());
    }
 
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl,"PolicyCmptTypeParameter",0);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertEquals(2, policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChilds().length);
        assertEquals(3, policyCmptTypeParameterInput.getTestAttributes().length);
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput,
                "policyCmptType1", "base.Test1", "relation1", 2, 3);        
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("policyCmptType3"),
                "policyCmptType3", "base.Test3", "relation3", 6, 7);
        assertTrue(policyCmptTypeParameterInput.isRequiresProductCmpt());
    }

    public void testToXml() {
        policyCmptTypeParameterInput.setName("Name1");
        policyCmptTypeParameterInput.setPolicyCmptType("base.Test2");
        policyCmptTypeParameterInput.setRelation("relation1");
        policyCmptTypeParameterInput.setMinInstances(7);
        policyCmptTypeParameterInput.setMaxInstances(8);        
        policyCmptTypeParameterInput.setRequiresProductCmpt(true);
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter targetChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        targetChild.setRelation("relation1");
        targetChild.setPolicyCmptType("base.Test4");
        targetChild.setName("childpolicyCmptType1");
        targetChild.setMinInstances(7);
        targetChild.setMaxInstances(8);         
        policyCmptTypeParameterInput.newTestAttribute();
       
        Element el = policyCmptTypeParameterInput.toXml(newDocument());
        
        policyCmptTypeParameterInput.setPolicyCmptType("base.Test3");
        policyCmptTypeParameterInput.setRelation("relation2");
        policyCmptTypeParameterInput.setMinInstances(7);
        policyCmptTypeParameterInput.setMaxInstances(8);        
        policyCmptTypeParameterInput.setRequiresProductCmpt(false);
        policyCmptTypeParameterInput.newTestAttribute();
        policyCmptTypeParameterInput.newTestAttribute();
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        
        policyCmptTypeParameterInput.initFromXml(el);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput,
                "Name1", "base.Test2", "relation1", 7, 8);
        assertEquals(3, policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChilds().length);
        assertEquals(1, policyCmptTypeParameterInput.getTestAttributes().length);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput,
                "Name1", "base.Test2", "relation1", 7, 8);        
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("childpolicyCmptType1"),
                "childpolicyCmptType1", "base.Test4", "relation1", 7, 8);
        assertTrue(policyCmptTypeParameterInput.isRequiresProductCmpt());
    }
    
    private void assertTargetTestPolicyCmptTypeParameter(
            ITestPolicyCmptTypeParameter targetChild,
            String name, String policyCmptTypeName, String relationName, int min, int max) {
        assertNotNull(targetChild);
        assertEquals(name, targetChild.getName());
        assertEquals(policyCmptTypeName, targetChild.getPolicyCmptType());
        assertEquals(relationName, targetChild.getRelation());
        assertEquals(min, targetChild.getMinInstances());
        assertEquals(max, targetChild.getMaxInstances());
    }
}
