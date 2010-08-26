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

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.devtools.core.util.TreeSetHelper;
import org.w3c.dom.Element;

/**
 * Test case type class. Definition of a test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseType extends IpsObject implements ITestCaseType {

    private List<ITestParameter> testParameters = new ArrayList<ITestParameter>();

    public TestCaseType(IIpsSrcFile file) {
        super(file);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return testParameters.toArray(new IIpsElement[testParameters.size()]);
    }

    @Override
    protected void reinitPartCollectionsThis() {
        testParameters.clear();
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof ITestParameter) {
            testParameters.add((ITestParameter)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof ITestParameter) {
            testParameters.remove(part);
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
        if (TestPolicyCmptTypeParameter.TAG_NAME.equals(xmlTagName)) {
            return newTestPolicyCmptTypeParameterInternal(id);
        } else if (TestValueParameter.TAG_NAME.equals(xmlTagName)) {
            return newTestValueParameterInternal(id);
        } else if (TestRuleParameter.TAG_NAME.equals(xmlTagName)) {
            return newTestRuleParameterInternal(id);
        }
        return null;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TEST_CASE_TYPE;
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) throws CoreException {
        Set<IDependency> dependencies = new HashSet<IDependency>();
        addDependenciesForTestPolicyCmptTypeParams(dependencies, details, getTestPolicyCmptTypeParameters());
        return dependencies.toArray(new IDependency[dependencies.size()]);

    }

    /**
     * Adds the qualified names for all test policy cmpt type parameters (root and childs) to the
     * given list
     */
    private void addDependenciesForTestPolicyCmptTypeParams(Set<IDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details,
            ITestPolicyCmptTypeParameter[] parameters) {

        for (ITestPolicyCmptTypeParameter parameter : parameters) {
            if (StringUtils.isNotEmpty(parameter.getPolicyCmptType())) {
                IDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                        new QualifiedNameType(parameter.getPolicyCmptType(), IpsObjectType.POLICY_CMPT_TYPE));
                dependencies.add(dependency);
                addDetails(details, dependency, parameter, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
            }
            addDependenciesForTestPolicyCmptTypeParameterAttributes(parameter.getTestAttributes(), details,
                    dependencies);
            addDependenciesForTestPolicyCmptTypeParams(dependencies, details, parameter
                    .getTestPolicyCmptTypeParamChilds());
        }
    }

    private void addDependenciesForTestPolicyCmptTypeParameterAttributes(ITestAttribute[] attributes,
            Map<IDependency, List<IDependencyDetail>> details,
            Set<IDependency> dependencies) {

        for (ITestAttribute attribute : attributes) {
            if (!StringUtils.isEmpty(attribute.getPolicyCmptType())) {
                IDependency dependency = new DatatypeDependency(getQualifiedNameType(), attribute.getPolicyCmptType());
                dependencies.add(dependency);
                addDetails(details, dependency, attribute, ITestAttribute.PROPERTY_POLICYCMPTTYPE_OF_ATTRIBUTE);
            }
        }
    }

    @Override
    public ITestValueParameter[] getTestValueParameters() {
        return getTestParameters(null, TestValueParameter.class, null).toArray(new ITestValueParameter[0]);
    }

    @Override
    public ITestRuleParameter[] getTestRuleParameters() {
        return getTestParameters(null, TestRuleParameter.class, null).toArray(new ITestRuleParameter[0]);
    }

    @Override
    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParameters() {
        return getTestParameters(null, TestPolicyCmptTypeParameter.class, null).toArray(
                new ITestPolicyCmptTypeParameter[0]);
    }

    //
    // Getters for input parameters
    //

    @Override
    public ITestParameter[] getInputTestParameters() {
        return getTestParameters(TestParameterType.INPUT, null, null).toArray(new ITestParameter[0]);
    }

    @Override
    public ITestValueParameter[] getInputTestValueParameters() {
        return getTestParameters(TestParameterType.INPUT, TestValueParameter.class, null).toArray(
                new ITestValueParameter[0]);
    }

    @Override
    public ITestPolicyCmptTypeParameter[] getInputTestPolicyCmptTypeParameters() {
        return getTestParameters(TestParameterType.INPUT, TestPolicyCmptTypeParameter.class, null).toArray(
                new ITestPolicyCmptTypeParameter[0]);
    }

    @Override
    public ITestValueParameter getInputTestValueParameter(String inputTestValueParameter) throws CoreException {
        ITestValueParameter[] parameters = getTestParameters(TestParameterType.INPUT, TestValueParameter.class,
                inputTestValueParameter).toArray(new ITestValueParameter[0]);
        if (parameters.length == 0) {
            return null;
        }

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOneValueParamWithTypeAndName, TestParameterType.INPUT,
                inputTestValueParameter)));
    }

    @Override
    public ITestPolicyCmptTypeParameter getInputTestPolicyCmptTypeParameter(String inputTestPolicyCmptTypeParameter)
            throws CoreException {

        ITestPolicyCmptTypeParameter[] parameters = getTestParameters(TestParameterType.INPUT,
                TestPolicyCmptTypeParameter.class, inputTestPolicyCmptTypeParameter).toArray(
                new ITestPolicyCmptTypeParameter[0]);

        if (parameters.length == 0) {
            return null;
        }

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOnePolicyParamWithTypeAndName, TestParameterType.INPUT,
                inputTestPolicyCmptTypeParameter)));
    }

    @Override
    public ITestParameter getTestParameterByName(String testParameterName) throws CoreException {
        List<TestParameter> foundTestParameter = getTestParameters(null, null, testParameterName);
        if (foundTestParameter.size() == 0) {
            return null;
        }

        return foundTestParameter.get(0);
    }

    @Override
    public ITestParameter[] getTestParameters() {
        List<TestParameter> foundTestParameter = getTestParameters(null, null, null);
        if (foundTestParameter.size() == 0) {
            return new ITestParameter[0];
        }

        return foundTestParameter.toArray(new ITestParameter[0]);
    }

    //
    // Getters for expected result parameters
    //

    @Override
    public ITestParameter[] getExpectedResultTestParameters() {
        return getTestParameters(TestParameterType.EXPECTED_RESULT, null, null).toArray(new ITestParameter[0]);
    }

    @Override
    public ITestValueParameter[] getExpectedResultTestValueParameters() {
        return getTestParameters(TestParameterType.EXPECTED_RESULT, TestValueParameter.class, null).toArray(
                new ITestValueParameter[0]);
    }

    @Override
    public ITestPolicyCmptTypeParameter[] getExpectedResultTestPolicyCmptTypeParameters() {
        return getTestParameters(TestParameterType.EXPECTED_RESULT, TestPolicyCmptTypeParameter.class, null).toArray(
                new ITestPolicyCmptTypeParameter[0]);
    }

    @Override
    public ITestValueParameter getExpectedResultTestValueParameter(String expResultTestValueParameter)
            throws CoreException {
        ITestValueParameter[] parameters = getTestParameters(TestParameterType.EXPECTED_RESULT,
                TestValueParameter.class, expResultTestValueParameter).toArray(new ITestValueParameter[0]);
        if (parameters.length == 0) {
            return null;
        }

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseType_Error_MoreThanOneParamWithTypeAndName,
                TestParameterType.INPUT, expResultTestValueParameter)));
    }

    @Override
    public ITestPolicyCmptTypeParameter getExpectedResultTestPolicyCmptTypeParameter(String expResultTestPolicyCmptTypeParameter)
            throws CoreException {
        ITestPolicyCmptTypeParameter[] parameters = getTestParameters(TestParameterType.EXPECTED_RESULT,
                TestPolicyCmptTypeParameter.class, expResultTestPolicyCmptTypeParameter).toArray(
                new ITestPolicyCmptTypeParameter[0]);

        if (parameters.length == 0) {
            return null;
        }

        if (parameters.length == 1) {
            return parameters[0];
        }

        throw new CoreException(new IpsStatus(NLS.bind(
                Messages.TestCaseType_Error_MoreThanOnePolicyParamWithTypeAndName, TestParameterType.INPUT,
                expResultTestPolicyCmptTypeParameter)));
    }

    //
    // Create methods for input test parameters
    //

    @Override
    public ITestValueParameter newInputTestValueParameter() {
        TestValueParameter param = newTestValueParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.INPUT);
        objectHasChanged();
        return param;
    }

    /**
     * Sets the type of the test parameter. The following types could be set.
     * <p>
     * <ul>
     * <li>INPUT: the test parameter specifies only test case input objects
     * <li>EXPECTED_RESULT: the test parameter specifies only test case expected result objects
     * <li>COMBINED: the test parameter specifies both, input and expected result objects
     * </ul>
     */
    @Override
    public ITestPolicyCmptTypeParameter newInputTestPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.INPUT);
        objectHasChanged();
        return param;
    }

    //
    // Creates methods for expected result parameters
    //

    @Override
    public ITestValueParameter newExpectedResultValueParameter() {
        TestValueParameter param = newTestValueParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        objectHasChanged();
        return param;
    }

    @Override
    public ITestPolicyCmptTypeParameter newExpectedResultPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        objectHasChanged();
        return param;
    }

    @Override
    public TestRuleParameter newExpectedResultRuleParameter() {
        TestRuleParameter param = newTestRuleParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        objectHasChanged();
        return param;
    }

    //
    // Create methods for combined test parameters
    //

    @Override
    public ITestValueParameter newCombinedValueParameter() {
        TestValueParameter param = newTestValueParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.COMBINED);
        objectHasChanged();
        return param;
    }

    @Override
    public ITestPolicyCmptTypeParameter newCombinedPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.COMBINED);
        objectHasChanged();
        return param;
    }

    /**
     * Removes the test parameter from the test case type. Package private because the parameter in
     * this package removes itself from the list when delete is calling.
     */
    void removeTestParameter(TestParameter param) {
        testParameters.remove(param);
    }

    /**
     * Returns the test parameters which matches the given type, is instance of the given class and
     * matches the given name. The particular object aspect will only check if the particular field
     * is not <code>null</code>. For instance if all parameter are <code>null</code> then all
     * parameters are returned.
     */
    private List<TestParameter> getTestParameters(TestParameterType type, Class<?> parameterClass, String name) {
        List<TestParameter> result = new ArrayList<TestParameter>(testParameters.size());
        for (ITestParameter iTestParameter : testParameters) {
            TestParameter parameter = (TestParameter)iTestParameter;
            boolean addParameter = true;
            if (parameter.getTestParameterType() != null && type != null
                    && !TestParameterType.isTypeMatching(type, parameter.getTestParameterType())) {
                addParameter = false;
                continue;
            }
            if (parameterClass != null && !parameter.getClass().equals(parameterClass)) {
                addParameter = false;
                continue;
            }
            if (name != null && !name.equals(parameter.getName())) {
                addParameter = false;
                continue;
            }
            if (addParameter) {
                result.add(parameter);
            }
        }
        return result;
    }

    /**
     * Creates a new test policy component type parameter without updating the src file.
     */
    private TestPolicyCmptTypeParameter newTestPolicyCmptTypeParameterInternal(String id) {
        TestPolicyCmptTypeParameter p = new TestPolicyCmptTypeParameter(this, id);
        testParameters.add(p);
        return p;
    }

    /**
     * Creates a new test value parameter without updating the src file.
     */
    private TestValueParameter newTestValueParameterInternal(String id) {
        TestValueParameter p = new TestValueParameter(this, id);
        testParameters.add(p);
        return p;
    }

    /**
     * Creates a new test rule parameter without updating the src file.
     */
    private TestRuleParameter newTestRuleParameterInternal(String id) {
        TestRuleParameter p = new TestRuleParameter(this, id);
        testParameters.add(p);
        return p;
    }

    @Override
    public String generateUniqueNameForTestAttribute(ITestAttribute testAttribute, String name) {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        String uniqueName = name;

        int idx = 1;
        ITestAttribute[] testAttribues = testPolicyCmptTypeParam.getTestAttributes();
        for (int i = 0; i < testAttribues.length; i++) {
            if (!(testAttribues[i] == testAttribute) && testAttribues[i].getName().equals(uniqueName)) {
                uniqueName = name + " (" + idx++ + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return uniqueName;
    }

    @Override
    public int[] moveTestParameters(int[] indexes, boolean up) {
        ListElementMover<ITestParameter> mover = new ListElementMover<ITestParameter>(testParameters);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }

    @Override
    public IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) throws CoreException {
        List<IValidationRule> validationRules = new ArrayList<IValidationRule>();
        getValidationRules(getTestPolicyCmptTypeParameters(), validationRules, ipsProject);
        return validationRules.toArray(new IValidationRule[0]);
    }

    @Override
    public IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) throws CoreException {
        IValidationRule[] validationRules = getTestRuleCandidates(ipsProject);
        for (IValidationRule validationRule : validationRules) {
            if (validationRule.getName().equals(validationRuleName)) {
                return validationRule;
            }
        }
        return null;
    }

    /**
     * Get all validation rules from all policy cmpts which are related inside this test case type
     */
    private void getValidationRules(ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParameters,
            List<IValidationRule> validationRules,
            IIpsProject ipsProject) throws CoreException {

        for (ITestPolicyCmptTypeParameter parameter : testPolicyCmptTypeParameters) {
            IPolicyCmptType policyCmptType = parameter.findPolicyCmptType(ipsProject);
            if (policyCmptType == null) {
                continue;
            }
            validationRules.addAll(Arrays.asList(policyCmptType.getSupertypeHierarchy().getAllRules(policyCmptType)));
            getValidationRules(parameter.getTestPolicyCmptTypeParamChilds(), validationRules, ipsProject);
        }
    }

    /**
     * Returns all test parameters inside the test case type.
     */
    public ITestParameter[] getAllTestParameter() throws CoreException {
        List<ITestParameter> allParameters = new ArrayList<ITestParameter>();
        ITestParameter[] parameters = getTestParameters();
        for (ITestParameter parameter : parameters) {
            getAllChildTestParameter(parameter, allParameters);
        }
        return allParameters.toArray(new ITestParameter[allParameters.size()]);
    }

    private void getAllChildTestParameter(ITestParameter testParameter, List<ITestParameter> allParameters)
            throws CoreException {

        allParameters.add(testParameter);
        IIpsElement[] elems = testParameter.getChildren();
        for (IIpsElement elem : elems) {
            if (elem instanceof ITestParameter) {
                getAllChildTestParameter((ITestParameter)elem, allParameters);
            }
        }
    }

    @Override
    public IIpsSrcFile[] searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {
        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllTestCaseSrcFiles(this)));
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public boolean hasDescriptionSupport() {
        return true;
    }

    @Override
    public boolean hasLabelSupport() {
        return true;
    }

}
