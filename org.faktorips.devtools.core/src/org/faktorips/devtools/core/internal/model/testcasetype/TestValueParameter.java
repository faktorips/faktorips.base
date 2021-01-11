/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test value parameter class. Defines a test value for a specific test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestValueParameter extends TestParameter implements ITestValueParameter {

    final static String TAG_NAME = "ValueParameter"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    public TestValueParameter(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public String getDatatype() {
        return getValueDatatype();
    }

    @Override
    public void setDatatype(String datatype) {
        setValueDatatype(datatype);
    }

    @Override
    public void setTestParameterType(TestParameterType testParameterType) {
        // a test value parameter supports only input type or expected result type
        ArgumentCheck.isTrue(testParameterType.equals(TestParameterType.INPUT)
                || testParameterType.equals(TestParameterType.EXPECTED_RESULT));
        TestParameterType oldType = type;
        type = testParameterType;
        valueChanged(oldType, testParameterType);
    }

    @Override
    public String getValueDatatype() {
        return datatype;
    }

    @Override
    public void setValueDatatype(String datatypeId) {
        String oldDatatype = datatype;
        datatype = datatypeId;
        valueChanged(oldDatatype, datatypeId);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) {
        return ipsProject.findValueDatatype(datatype);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        datatype = element.getAttribute(PROPERTY_VALUEDATATYPE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_VALUEDATATYPE, datatype);
    }

    @Override
    public ITestParameter getRootParameter() {
        // no childs are supported, the test value parameter is always a root element
        return this;
    }

    @Override
    public boolean isRoot() {
        // no childs are supported, the test value parameter is always a root element
        return true;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ValueDatatype datatype = findValueDatatype(ipsProject);
        if (datatype == null) {
            String text = NLS.bind(Messages.TestValueParameter_ValidateError_ValueDatatypeNotFound, getDatatype());
            Message msg = new Message(MSGCODE_VALUEDATATYPE_NOT_FOUND, text, Message.ERROR, this,
                    PROPERTY_VALUEDATATYPE);
            list.add(msg);
        }

        // check the correct type
        if (isCombinedParameter() || (!isInputOrCombinedParameter() && !isExpextedResultOrCombinedParameter())) {
            String text = NLS.bind(Messages.TestValueParameter_ValidationError_TypeNotAllowed, type, name);
            Message msg = new Message(MSGCODE_WRONG_TYPE, text, Message.ERROR, this, PROPERTY_TEST_PARAMETER_TYPE);
            list.add(msg);
        }
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

}
