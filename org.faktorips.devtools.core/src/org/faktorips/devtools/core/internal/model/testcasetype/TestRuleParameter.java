/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
 * 
 * @author Joerg Ortmann
 */
public class TestRuleParameter extends TestParameter implements ITestRuleParameter {

    final static String TAG_NAME = "RuleParameter"; //$NON-NLS-1$

    public TestRuleParameter(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
    }

    @Override
    public boolean isRoot() {
        // no childs are supported, the test value parameter is always a root element
        return true;
    }

    @Override
    public ITestParameter getRootParameter() {
        // no childs are supported, the test value parameter is always a root element
        return this;
    }

    @Override
    public String getDatatype() {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }

    @Override
    public void setDatatype(String datatype) {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }

    @Override
    public void setTestParameterType(TestParameterType testParameterType) {
        // a test rule parameter supports only input type
        ArgumentCheck.isTrue(testParameterType.equals(TestParameterType.EXPECTED_RESULT));
        TestParameterType oldType = type;
        type = testParameterType;
        valueChanged(oldType, testParameterType);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        // check if the validation rule has the expected result type
        if (!isExpextedResultOrCombinedParameter()) {
            String text = NLS.bind(Messages.TestRuleParameter_ValidationError_WrongParameterType, name);
            Message msg = new Message(MSGCODE_NOT_EXPECTED_RESULT, text, Message.ERROR, this,
                    PROPERTY_TEST_PARAMETER_TYPE);
            list.add(msg);
        }
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollections() {
        // Nothing to do
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();

    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        return null;
    }

}
