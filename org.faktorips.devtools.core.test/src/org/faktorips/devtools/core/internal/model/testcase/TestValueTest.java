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
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestValueTest extends AbstractIpsPluginTest {

    private ITestValue valueObjectInput;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCase type = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        valueObjectInput = type.newInputValue();
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueObject", 0);
        valueObjectInput.initFromXml(paramEl);
        assertEquals("newSumInsured", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }

    public void testToXml() {
        valueObjectInput.setTestValueParameter("Money");
        valueObjectInput.setValue("500");
        Element el = valueObjectInput.toXml(newDocument());
        
        valueObjectInput.setTestValueParameter("Test");
        valueObjectInput.setValue("Test");
        
        valueObjectInput.initFromXml(el);
        assertEquals("Money", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }
}
