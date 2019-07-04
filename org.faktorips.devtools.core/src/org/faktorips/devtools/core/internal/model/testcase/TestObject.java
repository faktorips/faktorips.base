/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test object class. Superclass of all test objects for a specific test case.
 * 
 * @author Joerg Ortmann
 */
public abstract class TestObject extends IpsObjectPart implements ITestObject {

    /**
     * Specifies the default type, will be used if the corresponding test case type parameter is not
     * specified or not found
     */
    public static final TestParameterType DEFAULT_TYPE = TestParameterType.COMBINED;

    public TestObject(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public abstract ITestObject getRoot();

    @Override
    public abstract boolean isRoot();

    @Override
    public boolean isInput() {
        TestObject root = (TestObject)getRoot();
        TestCase testCase = (TestCase)root.getParent();
        try {
            ITestParameter param = null;
            if (this instanceof ITestPolicyCmpt) {
                param = testCase.findTestPolicyCmptTypeParameter((ITestPolicyCmpt)this, getIpsProject());
            } else if (this instanceof ITestPolicyCmptLink) {
                param = testCase.findTestPolicyCmptTypeParameter((ITestPolicyCmptLink)this, getIpsProject());
            }
            if (param != null) {
                return testCase.isTypeOrDefault(param, TestParameterType.INPUT);
            }
            // CSOFF: Empty Statement
        } catch (CoreException e) {
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exception check type of root
        }
        // CSON: Empty Statement
        return testCase.isTypeOrDefault(root.getTestParameterName(), TestParameterType.INPUT, DEFAULT_TYPE);
    }

    @Override
    public boolean isExpectedResult() {
        // TODO: mit Joerg klaeren
        TestObject root = (TestObject)getRoot();
        TestCase testCase = (TestCase)root.getParent();
        try {
            ITestParameter param = null;
            if (this instanceof ITestPolicyCmpt) {
                param = testCase.findTestPolicyCmptTypeParameter((ITestPolicyCmpt)this, getIpsProject());
            } else if (this instanceof ITestPolicyCmptLink) {
                param = testCase.findTestPolicyCmptTypeParameter((ITestPolicyCmptLink)this, getIpsProject());
            }

            if (param != null) {
                return testCase.isTypeOrDefault(param, TestParameterType.EXPECTED_RESULT);
            }
            // CSOFF: Empty Statement
        } catch (CoreException e) {
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exception check type of root
        }
        // CSON: Empty Statement
        return testCase.isTypeOrDefault(root.getTestParameterName(), TestParameterType.EXPECTED_RESULT, DEFAULT_TYPE);
    }

    @Override
    public boolean isCombined() {
        TestObject root = (TestObject)getRoot();
        TestCase testCase = (TestCase)root.getParent();
        try {
            ITestParameter param = null;
            if (this instanceof ITestPolicyCmpt) {
                param = testCase.findTestPolicyCmptTypeParameter((ITestPolicyCmpt)this, getIpsProject());
            } else if (this instanceof ITestPolicyCmptLink) {
                param = testCase.findTestPolicyCmptTypeParameter((ITestPolicyCmptLink)this, getIpsProject());
            }

            if (param != null) {
                return testCase.isTypeOrDefault(param, TestParameterType.COMBINED);
            }
            // CSOFF: Empty Statement
        } catch (CoreException e) {
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exception check type of root
        }
        // CSON: Empty Statement
        return testCase.isTypeOrDefault(root.getTestParameterName(), TestParameterType.COMBINED, DEFAULT_TYPE);
    }

    @Override
    protected Element createElement(Document doc) {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }

}
