/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component link. Defines a link for a policy component class within a test case
 * defination.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptLink extends IpsObjectPart implements ITestPolicyCmptLink {

    /** Tags */
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
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws CoreException {
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
    public ITestPolicyCmpt findTarget() throws CoreException {
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

    /**
     * This object has no parts.
     */
    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    @Override
    public ITestPolicyCmpt newTargetTestPolicyCmptChild() {
        ITestPolicyCmpt param = newTargetTestPolicyCmptChildInternal(getNextPartId());
        return param;
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
    public boolean isAccoziation() {
        return targetChild == null;
    }

    @Override
    public IIpsElement[] getChildren() {
        IIpsElement[] childrenArray = null;
        if (targetChild != null) {
            childrenArray = new IIpsElement[1];
            childrenArray[0] = targetChild;
        } else {
            childrenArray = new IIpsElement[0];
        }
        return childrenArray;
    }

    @Override
    protected void reinitPartCollections() {
        targetChild = null;
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof TestPolicyCmpt) {
            targetChild = (TestPolicyCmpt)part;
            return;
        }
        throw new RuntimeException("Unknown part type: " + part.getClass()); //$NON-NLS-1$
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        if (targetChild != null && part == targetChild) {
            if (!targetChild.isRoot()) {
                // delete also this link that refers to test policy component
                delete();
            }
            targetChild = null;
            return;
        }
        throw new RuntimeException("Unknown part type: " + part.getClass()); //$NON-NLS-1$
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(TestPolicyCmpt.TAG_NAME)) {
            return newTargetTestPolicyCmptChildInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    @Override
    public ITestCase getTestCase() {
        return ((ITestPolicyCmpt)getParent()).getTestCase();
    }

    public void validateGroup(MessageList messageList, IIpsProject ipsProject) throws CoreException {
        // check all messages only once, thus if the same test link is used more than one
        // only one message are added to the list of validation errors

        // validate if the test policy component type parameter exists
        ITestPolicyCmptTypeParameter testCaseTypeParam = findTestPolicyCmptTypeParameter(ipsProject);
        if (messageList.getMessageByCode(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND) == null) {
            if (testCaseTypeParam == null) {
                String text = NLS
                        .bind(Messages.TestPolicyCmptLink_ValidationError_TestCaseTypeParamNotFound, getName());
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
                String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_ModelAssociationNotFound,
                        testCaseTypeParam.getAssociation());
                Message msg = new Message(MSGCODE_MODEL_LINK_NOT_FOUND, text, Message.ERROR, this,
                        ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
                messageList.add(msg);
            }
        }
    }

    public void validateSingle(MessageList messageList, IIpsProject ipsProject) throws CoreException {
        // validate if the test case type param exists
        ITestPolicyCmptTypeParameter param = null;
        try {
            param = findTestPolicyCmptTypeParameter(ipsProject);
        } catch (CoreException e) {
            // ignore exception, the param will be used to indicate errors
        }

        // check if the corresponding test parameter exists
        if (param == null) {
            String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_TestCaseTypeNotFound,
                    getTestPolicyCmptTypeParameter());
            Message msg = new Message(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND, text, Message.ERROR, this,
                    PROPERTY_POLICYCMPTTYPE);
            messageList.add(msg);
        }

        // for assoziations check if the target is in the test case
        if (isAccoziation()) {
            if (getTestCase().findTestPolicyCmpt(getTarget()) == null) {
                String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_AssoziationNotFound, getTarget());
                Message msg = new Message(MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE, text, Message.ERROR, this,
                        PROPERTY_POLICYCMPTTYPE);
                messageList.add(msg);
            }
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateGroup(list, ipsProject);
        validateSingle(list, ipsProject);
    }

    @Override
    public String getName() {
        return getTestPolicyCmptTypeParameter() + "(" + getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
