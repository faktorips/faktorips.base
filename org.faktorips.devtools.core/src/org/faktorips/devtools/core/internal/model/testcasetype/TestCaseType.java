/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.w3c.dom.Element;

/**
 * Test case type class. Definition of a test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseType extends IpsObject implements ITestCaseType {

    /* Tags */
    static final String TAG_NAME_INPUT = "Input"; //$NON-NLS-1$
    static final String TAG_NAME_EXPECTED_RESULT = "ExpectedResult"; //$NON-NLS-1$

    /* Children */
    private List testParameters = new ArrayList();

    public TestCaseType(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return (IIpsElement[])testParameters.toArray(new IIpsElement[0]);
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        testParameters = new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof ITestParameter) {
            testParameters.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (TestPolicyCmptTypeParameter.TAG_NAME.equals(xmlTagName)) {
            return newTestPolicyCmptTypeParameterInternal(id);
        } else if (TestValueParameter.TAG_NAME.equals(xmlTagName)) {
            return newTestValueParameterInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TEST_CASE_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter[] getTestValueParameters() {
        return (ITestValueParameter[])getTestParameters(null, TestValueParameter.class, null).toArray(
                new ITestValueParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParameters() {
        return (ITestPolicyCmptTypeParameter[])getTestParameters(null, TestPolicyCmptTypeParameter.class, null)
                .toArray(new ITestPolicyCmptTypeParameter[0]);
    }
    
    //
    // Getters for input parameters
    //

    /**
     * {@inheritDoc}
     */
    public ITestParameter[] getInputTestParameters() {
        return (ITestParameter[])getTestParameters(TestParameterType.INPUT, null, null).toArray(new ITestParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter[] getInputTestValueParameters() {
        return (ITestValueParameter[])getTestParameters(TestParameterType.INPUT, TestValueParameter.class, null)
                .toArray(new ITestValueParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter[] getInputTestPolicyCmptTypeParameters() {
        return (ITestPolicyCmptTypeParameter[])getTestParameters(TestParameterType.INPUT,
                TestPolicyCmptTypeParameter.class, null).toArray(new ITestPolicyCmptTypeParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter getInputTestValueParameter(String inputTestValueParameter) throws CoreException {
        ITestValueParameter[] parameters = (ITestValueParameter[])getTestParameters(TestParameterType.INPUT,
                TestValueParameter.class, inputTestValueParameter).toArray(new ITestValueParameter[0]);
        if (parameters.length == 0)
            return null;

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOneValueParamWithRoleAndName, TestParameterType.INPUT,
                inputTestValueParameter)));
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter getInputTestPolicyCmptTypeParameter(String inputTestPolicyCmptTypeParameter)
            throws CoreException {
        ITestPolicyCmptTypeParameter[] parameters = (ITestPolicyCmptTypeParameter[])getTestParameters(
                TestParameterType.INPUT, TestPolicyCmptTypeParameter.class, inputTestPolicyCmptTypeParameter).toArray(
                new ITestPolicyCmptTypeParameter[0]);

        if (parameters.length == 0)
            return null;

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOnePolicyParamWithRoleAndName,
                TestParameterType.INPUT, inputTestPolicyCmptTypeParameter)));
    }

    /**
     * {@inheritDoc}
     */
    public ITestParameter getTestParameterByName(String testParameterName) throws CoreException {
        List foundTestParameter = getTestParameters(null, null, testParameterName);
        if (foundTestParameter.size() == 0)
            return null;

        if (foundTestParameter.size() == 1) {
            return (ITestParameter)foundTestParameter.get(0);
        }

        throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseType_Error_MoreThanOneParamWithName, testParameterName)));
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestParameter[] getTestParameters() throws CoreException {
        List foundTestParameter = getTestParameters(null, null, null);
        if (foundTestParameter.size() == 0)
            return null;

        return (ITestParameter[]) foundTestParameter.toArray(new ITestParameter[0]);
    }

    //
    // Getters for expected result parameters
    //

    /**
     * {@inheritDoc}
     */
    public ITestParameter[] getExpectedResultTestParameters() {
        return (ITestParameter[])getTestParameters(TestParameterType.EXPECTED_RESULT, null, null).toArray(
                new ITestParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter[] getExpectedResultTestValueParameters() {
        return (ITestValueParameter[])getTestParameters(TestParameterType.EXPECTED_RESULT, TestValueParameter.class,
                null).toArray(new ITestValueParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter[] getExpectedResultTestPolicyCmptTypeParameters() {
        return (ITestPolicyCmptTypeParameter[])getTestParameters(TestParameterType.EXPECTED_RESULT,
                TestPolicyCmptTypeParameter.class, null).toArray(new ITestPolicyCmptTypeParameter[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter getExpectedResultTestValueParameter(String expResultTestValueParameter)
            throws CoreException {
        ITestValueParameter[] parameters = (ITestValueParameter[])getTestParameters(TestParameterType.EXPECTED_RESULT,
                TestValueParameter.class, expResultTestValueParameter).toArray(new ITestValueParameter[0]);
        if (parameters.length == 0)
            return null;

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOneParamWithRoleAndName, TestParameterType.INPUT,
                expResultTestValueParameter)));
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter getExpectedResultTestPolicyCmptTypeParameter(String expResultTestPolicyCmptTypeParameter)
            throws CoreException {
        ITestPolicyCmptTypeParameter[] parameters = (ITestPolicyCmptTypeParameter[])getTestParameters(
                TestParameterType.EXPECTED_RESULT, TestPolicyCmptTypeParameter.class,
                expResultTestPolicyCmptTypeParameter).toArray(new ITestPolicyCmptTypeParameter[0]);

        if (parameters.length == 0)
            return null;

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOnePolicyParamWithRoleAndName,
                TestParameterType.INPUT, expResultTestPolicyCmptTypeParameter)));
    }

    //
    // Create methods for input test parameters
    //

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter newInputTestValueParameter() {
        TestValueParameter param = newTestValueParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.INPUT);
        updateSrcFile();
        return param;
    }

    /**
     * Sets the role of the test parameter. The following roles could be set.
     * <p><ul>
     * <li>INPUT: the test parameter specifies only test case input objects
     * <li>EXPECTED_RESULT: the test parameter specifies only test case expected result objects
     * <li>COMBINED: the test parameter specifies both, input and expected result objects
     * </ul>
     */
    public ITestPolicyCmptTypeParameter newInputTestPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.INPUT);
        updateSrcFile();
        return param;
    }

    //
    // Creates methods for expected result parameters
    //

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter newExpectedResultValueParameter() {
        TestValueParameter param = newTestValueParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        updateSrcFile();
        return param;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter newExpectedResultPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        updateSrcFile();
        return param;
    }

    //
    // Create methods for combined test parameters
    //

    /**
     * {@inheritDoc}
     */
    public ITestValueParameter newCombinedValueParameter() {
        TestValueParameter param = newTestValueParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.COMBINED);
        updateSrcFile();
        return param;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter newCombinedPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.COMBINED);
        updateSrcFile();
        return param;
    }

    /**
     * Removes the test parameter from the test case type. Package private because the paramter in
     * this package removes itselfs from the list when delete is calling.
     */
    void removeTestParameter(TestParameter param) {
        testParameters.remove(param);
    }

    /*
     * Returns the test parameters which matches the given role, is instance of the given 
     * class and matches the given name. The particular object aspect will only check if the particular field is not
     * <code>null</code>. For instance if all parameter are <code>null</code> then all
     * parameters are returned.
     */
    private List getTestParameters(TestParameterType role, Class parameterClass, String name) {
        List result = new ArrayList(testParameters.size());
        for (Iterator iter = testParameters.iterator(); iter.hasNext();) {
            TestParameter parameter = (TestParameter)iter.next();
            boolean addParameter = true;
            if (parameter.getTestParameterType() != null && role != null
                    && ! TestParameterType.isRoleMatching(role, parameter.getTestParameterType())) {
                addParameter = false;
                continue;
            }
            if (parameterClass != null && ! parameter.getClass().equals(parameterClass)) {
                addParameter = false;
                continue;
            }
            if (name != null && ! name.equals(parameter.getName())) {
                addParameter = false;
                continue;
            }
            if (addParameter)
                result.add(parameter);
        }
        return result;
    }

    /*
     * Creates a new test policy component type parameter without updating the src file.
     */
    private TestPolicyCmptTypeParameter newTestPolicyCmptTypeParameterInternal(int id) {
        TestPolicyCmptTypeParameter p = new TestPolicyCmptTypeParameter(this, id);
        testParameters.add(p);
        return p;
    }

    /*
     * Creates a new test value parameter without updating the src file.
     */
    private TestValueParameter newTestValueParameterInternal(int id) {
        TestValueParameter p = new TestValueParameter(this, id);
        testParameters.add(p);
        return p;
    }
}
