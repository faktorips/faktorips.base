/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component link. Defines a link for a policy component class within a test case
 * definition.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptLink extends IpsObjectPart implements ITestPolicyCmptLink {

    static final String TAG_NAME = "Link"; //$NON-NLS-1$

    private String testPolicyCmptTypeParameter = ""; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$

    private ITestPolicyCmpt targetChild;

    public TestPolicyCmptLink(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public String getTestPolicyCmptTypeParameter() {
        return testPolicyCmptTypeParameter;
    }

    @Override
    public void setTestPolicyCmptTypeParameter(String newPolicyCmptType) {
        String oldPolicyCmptType = testPolicyCmptTypeParameter;
        testPolicyCmptTypeParameter = newPolicyCmptType;
        valueChanged(oldPolicyCmptType, newPolicyCmptType);
    }

    @Override
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) {
        if (StringUtils.isEmpty(testPolicyCmptTypeParameter)) {
            return null;
        }
        return ((TestCase)getTestCase()).findTestPolicyCmptTypeParameter(this, ipsProject);
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setTarget(String target) {
        String oldTarget = this.target;
        this.target = target;
        valueChanged(oldTarget, target);
    }

    @Override
    public ITestPolicyCmpt findTarget() {
        if (targetChild != null) {
            return targetChild;
        }
        if (StringUtils.isEmpty(target)) {
            return null;
        }

        // the target is an association, search for the target in the test case
        return getTestCase().findTestPolicyCmpt(target);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        testPolicyCmptTypeParameter = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
        target = element.getAttribute(PROPERTY_TARGET);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_POLICYCMPTTYPE, testPolicyCmptTypeParameter);
        element.setAttribute(PROPERTY_TARGET, target);
    }

    @Override
    public ITestPolicyCmpt newTargetTestPolicyCmptChild() {
        return newTargetTestPolicyCmptChildInternal(getNextPartId());
    }

    /**
     * Creates a new test policy component as target for this link without updating the src file.
     */
    private ITestPolicyCmpt newTargetTestPolicyCmptChildInternal(String id) {
        TestPolicyCmpt testPc = new TestPolicyCmpt(this, id);
        targetChild = testPc;
        return testPc;
    }

    @Override
    public boolean isComposition() {
        return targetChild != null;
    }

    @Override
    public boolean isAssociation() {
        return targetChild == null;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        if (targetChild != null) {
            return new IIpsElement[] { targetChild };
        }
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollectionsThis() {
        targetChild = null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof TestPolicyCmpt) {
            targetChild = (TestPolicyCmpt)part;
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (targetChild != null && part == targetChild) {
            if (!targetChild.isRoot()) {
                // delete also this link that refers to test policy component
                delete();
            }
            targetChild = null;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(TestPolicyCmpt.TAG_NAME)) {
            return newTargetTestPolicyCmptChildInternal(id);
        }
        return null;
    }

    @Override
    public ITestCase getTestCase() {
        return ((ITestPolicyCmpt)getParent()).getTestCase();
    }

    public void validateGroup(MessageList messageList, IIpsProject ipsProject) {
        // check all messages only once, thus if the same test link is used more than one
        // only one message are added to the list of validation errors

        // validate if the test policy component type parameter exists
        ITestPolicyCmptTypeParameter testCaseTypeParam = findTestPolicyCmptTypeParameter(ipsProject);
        if (messageList.getMessageByCode(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND) == null) {
            if (testCaseTypeParam == null) {
                String text = MessageFormat.format(
                        Messages.TestPolicyCmptLink_ValidationError_TestCaseTypeParamNotFound,
                        getName());
                Message msg = new Message(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND, text, Message.ERROR, this,
                        PROPERTY_POLICYCMPTTYPE);
                messageList.add(msg);
            }
        }
        // abort the rest of the validation if the test case type parameter not found
        if (testCaseTypeParam == null) {
            return;
        }

        // validate if the model association exists
        if (messageList.getMessageByCode(MSGCODE_MODEL_LINK_NOT_FOUND) == null) {
            IPolicyCmptTypeAssociation modelLink = testCaseTypeParam.findAssociation(ipsProject);
            if (modelLink == null) {
                String text = MessageFormat.format(Messages.TestPolicyCmptLink_ValidationError_ModelAssociationNotFound,
                        testCaseTypeParam.getAssociation());
                Message msg = new Message(MSGCODE_MODEL_LINK_NOT_FOUND, text, Message.ERROR, this,
                        ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
                messageList.add(msg);
            }
        }
    }

    public void validateSingle(MessageList messageList, IIpsProject ipsProject) {
        // validate if the test case type param exists
        ITestPolicyCmptTypeParameter param = null;
        try {
            param = findTestPolicyCmptTypeParameter(ipsProject);
        } catch (IpsException e) {
            // ignore exception, the param will be used to indicate errors
        }

        // check if the corresponding test parameter exists
        if (param == null) {
            String text = MessageFormat.format(Messages.TestPolicyCmptLink_ValidationError_TestCaseTypeNotFound,
                    getTestPolicyCmptTypeParameter());
            Message msg = new Message(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND, text, Message.ERROR, this,
                    PROPERTY_POLICYCMPTTYPE);
            messageList.add(msg);
        }

        // for assoziations check if the target is in the test case
        if (isAssociation()) {
            if (getTestCase().findTestPolicyCmpt(getTarget()) == null) {
                String text = MessageFormat.format(Messages.TestPolicyCmptLink_ValidationError_AssoziationNotFound,
                        getTarget());
                Message msg = new Message(MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE, text, Message.ERROR, this,
                        PROPERTY_POLICYCMPTTYPE);
                messageList.add(msg);
            }
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        validateGroup(list, ipsProject);
        validateSingle(list, ipsProject);
    }

    @Override
    public String getName() {
        return getTestPolicyCmptTypeParameter() + "(" + getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
