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
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPcTypeRelationTest extends AbstractIpsPluginTest {

    private ITestPolicyCmptRelation testPcTypeRelation;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCase type = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testPcTypeRelation = type.newExpectedResultPolicyCmpt().newTestPolicyCmptRelation();
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testPcTypeRelation.initFromXml(paramEl);
        assertEquals("relation1", testPcTypeRelation.getTestPolicyCmptType());
        assertEquals("base.target1", testPcTypeRelation.getTarget());
    }

    public void testToXml() {
        testPcTypeRelation.setTestPolicyCmptType("relation2");
        testPcTypeRelation.setTarget("base.target2");
        Element el = testPcTypeRelation.toXml(newDocument());
        testPcTypeRelation.setTestPolicyCmptType("test1");
        testPcTypeRelation.setTarget("test2");
        testPcTypeRelation.initFromXml(el);
        assertEquals("relation2", testPcTypeRelation.getTestPolicyCmptType());
        assertEquals("base.target2", testPcTypeRelation.getTarget());
    }
    
}
