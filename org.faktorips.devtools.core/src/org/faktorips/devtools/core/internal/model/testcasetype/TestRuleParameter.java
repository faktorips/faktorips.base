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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test rule parameter class. Defines a validation rule for a specific test case type.
 * @author Joerg Ortmann
 */
public class TestRuleParameter extends TestParameter implements ITestRuleParameter {

    final static String TAG_NAME = "RuleParameter"; //$NON-NLS-1$
    
    /**
     * @param parent
     * @param id
     */
    public TestRuleParameter(IIpsObject parent, int id) {
        super(parent, id);
    }
    
    /**
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ValidationRuleDef.gif"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isRoot() {
        // no childs are supported, the test value parameter is always a root element        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestParameter getRootParameter() {
        // no childs are supported, the test value parameter is always a root element
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTestParameterType(TestParameterType testParameterType) {
        // a test rule parameter supports only input type
        ArgumentCheck.isTrue(testParameterType.equals(TestParameterType.EXPECTED_RESULT));
        TestParameterType oldType = this.type;
        this.type = testParameterType;
        valueChanged(oldType, testParameterType);
    }
 
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);

        // check if the validation rule has the expected result type
        if (!isExpextedResultParameter()) {
            String text = NLS.bind(Messages.TestRuleParameter_ValidationError_WrongParameterType, name);
            Message msg = new Message(MSGCODE_NOT_EXPECTED_RESULT, text, Message.ERROR, this, PROPERTY_TEST_PARAMETER_TYPE);
            list.add(msg);
        }
    }
}
