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

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
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
    public static TestParameterType DEFAULT_TYPE = TestParameterType.COMBINED;

    @Override
    public abstract ITestObject getRoot();

    @Override
    public abstract boolean isRoot();

    public TestObject(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

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
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exception check type of root
        }
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
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exception check type of root
        }
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
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exception check type of root
        }
        return testCase.isTypeOrDefault(root.getTestParameterName(), TestParameterType.COMBINED, DEFAULT_TYPE);

    }

    @Override
    protected Element createElement(Document doc) {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }

    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
    }

}
