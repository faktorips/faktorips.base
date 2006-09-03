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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation class to compute the delta between a test case and the test case type the test
 * case is based on.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTestCaseTypeDelta implements ITestCaseTestCaseTypeDelta {
//    private ITestCase testCase;
//    private ITestCaseType testCaseType;

//    // TestCase Side
//    private ITestPolicyCmptRelation[] relationsWithMissingTypeParam;
//    private ITestAttributeValue[] testAttributeValuesWithMissingTestAttribute;
//    private ITestValue[] testValuesWithMissingTestValueParam;
//
//    // TestCaseTypeSide
//    private ITestAttribute[] testAttributesWithMissingTestAttributeValue;
//    private ITestValueParameter[] testValueParametersWithMissingTestValue;

    public TestCaseTestCaseTypeDelta(ITestCase testCase, ITestCaseType testCaseType) throws CoreException {
        ArgumentCheck.notNull(testCase);
        ArgumentCheck.notNull(testCaseType);
//        this.testCase = testCase;
//        this.testCaseType = testCaseType;
        
        computeTestValueWithMissingTestValueParam();
    }
    
    private void computeTestValueWithMissingTestValueParam() throws CoreException {
//        List missing = new ArrayList();
//        ITestValue[] testValues = testCase.getTestValues();
//        for (int i = 0; i < testValues.length; i++) {
//            ITestParameter testParameter = testCaseType.getTestParameterByName(testValues[i].getName());
//            if (testParameter instanceof ITestValueParametdder)
//            missing.add(testValues[i]);
//        }
//        testValuesWithMissingTestValueParam = (ITestValue[]) missing.toArray(new ITestValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestCaseType getTestCaseType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ITestCase getTestCase() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptRelation[] getTestPolicyCmptRelationsWithMissingTestPolicyCmptTypeParam() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ITestAttributeValue[] getTestAttributeValuesWithMissingTestAttribute() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ITestValue[] getTestValuesWithMissingTestValueParameter() {
        return null;
    }

    
    /**
     * {@inheritDoc}
     */
    public ITestAttribute[] getTestAttributesWithMissingTestAttributeValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter[] getTestValueParametersWithMissingTestValue() {
        return null;
    }
}
