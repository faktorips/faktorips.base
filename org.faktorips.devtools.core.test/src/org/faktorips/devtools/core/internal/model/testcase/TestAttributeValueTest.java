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

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValueTest  extends AbstractIpsPluginTest {

    private ITestAttributeValue testAttributeValue;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("inputTestPcCmptParam1");

        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        ITestPolicyCmpt tpc = testCase.newTestPolicyCmpt();
        tpc.setTestPolicyCmptTypeParameter("inputTestPcCmptParam1");
        testAttributeValue = tpc.newTestAttributeValue();
        testAttributeValue.setTestAttribute("inputAttribute1");
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testAttributeValue.initFromXml(paramEl);
        assertEquals("attribute1", testAttributeValue.getTestAttribute());
        assertEquals("500", testAttributeValue.getValue());
    }

    public void testToXml() {
        testAttributeValue.setTestAttribute("attribute2");
        testAttributeValue.setValue("500");
        Element el = testAttributeValue.toXml(newDocument());
        testAttributeValue.setTestAttribute("test");
        testAttributeValue.setValue("1000");
        testAttributeValue.initFromXml(el);
        assertEquals("attribute2", testAttributeValue.getTestAttribute());
        assertEquals("500", testAttributeValue.getValue());
    }
}
