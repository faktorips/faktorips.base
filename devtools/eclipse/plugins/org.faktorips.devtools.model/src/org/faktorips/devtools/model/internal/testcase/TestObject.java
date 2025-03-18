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

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
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
            if (this instanceof ITestPolicyCmpt testPolicyCmpt) {
                param = testCase.findTestPolicyCmptTypeParameter(testPolicyCmpt, getIpsProject());
            } else if (this instanceof ITestPolicyCmptLink link) {
                param = testCase.findTestPolicyCmptTypeParameter(link, getIpsProject());
            }
            if (param != null) {
                return testCase.isTypeOrDefault(param, TestParameterType.INPUT);
            }
            // CSOFF: Empty Statement
        } catch (IpsException e) {
            // ignore exception check type of root
        }
        // CSON: Empty Statement
        return testCase.isTypeOrDefault(root.getTestParameterName(), TestParameterType.INPUT, DEFAULT_TYPE);
    }

    @Override
    public boolean isExpectedResult() {
        TestObject root = (TestObject)getRoot();
        TestCase testCase = (TestCase)root.getParent();
        try {
            ITestParameter param = null;
            if (this instanceof ITestPolicyCmpt testPolicyCmpt) {
                param = testCase.findTestPolicyCmptTypeParameter(testPolicyCmpt, getIpsProject());
            } else if (this instanceof ITestPolicyCmptLink link) {
                param = testCase.findTestPolicyCmptTypeParameter(link, getIpsProject());
            }

            if (param != null) {
                return testCase.isTypeOrDefault(param, TestParameterType.EXPECTED_RESULT);
            }
            // CSOFF: Empty Statement
        } catch (IpsException e) {
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
            if (this instanceof ITestPolicyCmpt testPolicyCmpt) {
                param = testCase.findTestPolicyCmptTypeParameter(testPolicyCmpt, getIpsProject());
            } else if (this instanceof ITestPolicyCmptLink link) {
                param = testCase.findTestPolicyCmptTypeParameter(link, getIpsProject());
            }

            if (param != null) {
                return testCase.isTypeOrDefault(param, TestParameterType.COMBINED);
            }
            // CSOFF: Empty Statement
        } catch (IpsException e) {
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
