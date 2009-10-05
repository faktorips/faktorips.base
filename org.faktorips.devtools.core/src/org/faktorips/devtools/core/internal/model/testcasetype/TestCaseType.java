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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IDependency;
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
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof ITestParameter) {
            testParameters.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof ITestParameter) {
            testParameters.remove(part);
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
        } else if (TestRuleParameter.TAG_NAME.equals(xmlTagName)) {
            return newTestRuleParameterInternal(id);
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
    public IDependency[] dependsOn() throws CoreException {
        Set dependencies = new HashSet();
        addQualifiedNameTypesForTestPolicyCmptTypeParams(dependencies, getTestPolicyCmptTypeParameters());
        return (IDependency[]) dependencies
                .toArray(new IDependency[dependencies.size()]);

    }

    /*
     * Adds the qualified names for all test policy cmpt type parameters (root and childs) to the
     * given list
     */
    private void addQualifiedNameTypesForTestPolicyCmptTypeParams(Set qualifiedNameTypes,
            ITestPolicyCmptTypeParameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (StringUtils.isNotEmpty(parameters[i].getPolicyCmptType())) {
                qualifiedNameTypes.add(IpsObjectDependency.createReferenceDependency(this
                        .getQualifiedNameType(), new QualifiedNameType(parameters[i].getPolicyCmptType(),
                        IpsObjectType.POLICY_CMPT_TYPE)));
            }
            addQualifiedNameTypesForTestPolicyCmptTypeParams(qualifiedNameTypes, parameters[i]
                    .getTestPolicyCmptTypeParamChilds());
        }
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
    public ITestRuleParameter[] getTestRuleParameters() {
        return (ITestRuleParameter[])getTestParameters(null, TestRuleParameter.class, null).toArray(
                new ITestRuleParameter[0]);
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
                Messages.TestCaseType_Error_MoreThanOneValueParamWithTypeAndName, TestParameterType.INPUT,
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
                Messages.TestCaseType_Error_MoreThanOnePolicyParamWithTypeAndName,
                TestParameterType.INPUT, inputTestPolicyCmptTypeParameter)));
    }

    /**
     * {@inheritDoc}
     */
    public ITestParameter getTestParameterByName(String testParameterName) throws CoreException {
        List foundTestParameter = getTestParameters(null, null, testParameterName);
        if (foundTestParameter.size() == 0)
            return null;

        return (ITestParameter)foundTestParameter.get(0);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestParameter[] getTestParameters() {
        List foundTestParameter = getTestParameters(null, null, null);
        if (foundTestParameter.size() == 0)
            return new ITestParameter[0];

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
                Messages.TestCaseType_Error_MoreThanOneParamWithTypeAndName, TestParameterType.INPUT,
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
                Messages.TestCaseType_Error_MoreThanOnePolicyParamWithTypeAndName,
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
        objectHasChanged();
        return param;
    }

    /**
     * Sets the type of the test parameter. The following types could be set.
     * <p><ul>
     * <li>INPUT: the test parameter specifies only test case input objects
     * <li>EXPECTED_RESULT: the test parameter specifies only test case expected result objects
     * <li>COMBINED: the test parameter specifies both, input and expected result objects
     * </ul>
     */
    public ITestPolicyCmptTypeParameter newInputTestPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.INPUT);
        objectHasChanged();
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
        objectHasChanged();
        return param;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter newExpectedResultPolicyCmptTypeParameter() {
        TestPolicyCmptTypeParameter param = newTestPolicyCmptTypeParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        objectHasChanged();
        return param;
    }

    /**
     * {@inheritDoc}
     */
    public TestRuleParameter newExpectedResultRuleParameter() {
        TestRuleParameter param = newTestRuleParameterInternal(getNextPartId());
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        objectHasChanged();
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
        objectHasChanged();
        return param;
    }

    /**
     * {@inheritDoc}
     */
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

    /*
     * Returns the test parameters which matches the given type, is instance of the given 
     * class and matches the given name. The particular object aspect will only check if the particular field is not
     * <code>null</code>. For instance if all parameter are <code>null</code> then all
     * parameters are returned.
     */
    private List getTestParameters(TestParameterType type, Class parameterClass, String name) {
        List result = new ArrayList(testParameters.size());
        for (Iterator iter = testParameters.iterator(); iter.hasNext();) {
            TestParameter parameter = (TestParameter)iter.next();
            boolean addParameter = true;
            if (parameter.getTestParameterType() != null && type != null
                    && ! TestParameterType.isTypeMatching(type, parameter.getTestParameterType())) {
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
    
    /*
     * Creates a new test rule parameter without updating the src file.
     */
    private TestRuleParameter newTestRuleParameterInternal(int id) {
        TestRuleParameter p = new TestRuleParameter(this, id);
        testParameters.add(p);
        return p;
    }
    
    /**
     * {@inheritDoc}
     */
    public String generateUniqueNameForTestAttribute(ITestAttribute testAttribute, String name){
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        String uniqueName = name;

        int idx = 1;
        ITestAttribute[] testAttribues = testPolicyCmptTypeParam.getTestAttributes();
        for (int i = 0; i < testAttribues.length; i++) {
            if (! (testAttribues[i] == testAttribute) && testAttribues[i].getName().equals(uniqueName)){
                uniqueName = name + " (" + idx++ + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return uniqueName;
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTestParameters(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(testParameters);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }
    
    /**
     * {@inheritDoc}
     */
    public IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) throws CoreException {
        List validationRules = new ArrayList();
        getValidationRules(getTestPolicyCmptTypeParameters(), validationRules, ipsProject);
        return (IValidationRule[]) validationRules.toArray(new IValidationRule[0]);
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) throws CoreException {
        IValidationRule[] validationRules = getTestRuleCandidates(ipsProject);
        for (int i = 0; i < validationRules.length; i++) {
            if (validationRules[i].getName().equals(validationRuleName)){
                return validationRules[i];
            }
        }
        return null;
    }

    /*
     * Get all validation rules from all policy cmpts which are related inside this test case type
     */
    private void getValidationRules(ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParameters, List validationRules, IIpsProject ipsProject) throws CoreException {
        for (int i = 0; i < testPolicyCmptTypeParameters.length; i++) {
            ITestPolicyCmptTypeParameter parameter = testPolicyCmptTypeParameters[i];
            IPolicyCmptType policyCmptType = parameter.findPolicyCmptType(ipsProject);
            if (policyCmptType == null){
                continue;
            }
            validationRules.addAll(Arrays.asList(policyCmptType.getSupertypeHierarchy().getAllRules(policyCmptType)));
            getValidationRules(testPolicyCmptTypeParameters[i].getTestPolicyCmptTypeParamChilds(), validationRules, ipsProject);
        }
    }

    /**
     * Returns all test parameters inside the test case type.
     */
    public ITestParameter[] getAllTestParameter() throws CoreException {
        List allParameters = new ArrayList();
        ITestParameter[] parameters = getTestParameters();
        for (int i = 0; i < parameters.length; i++) {
            getAllChildTestParameter(parameters[i], allParameters);
        }
        return (ITestParameter[]) allParameters.toArray(new ITestParameter[allParameters.size()]);
    }

    private void getAllChildTestParameter(ITestParameter testParameter, List allParameters) throws CoreException {
        allParameters.add(testParameter);
        IIpsElement[] elems = testParameter.getChildren();
        for (int i = 0; i < elems.length; i++) {
            if (elems[i] instanceof ITestParameter){
                getAllChildTestParameter((ITestParameter)elems[i], allParameters);
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.core.model.IIpsMetaClass#findAllMetaObjects(org.faktorips.devtools.core.model.ipsproject.IIpsProject, boolean)
	 */
	/**
	 * {@inheritDoc}
	 */
	public IIpsSrcFile[] findAllMetaObjectSrcFiles(IIpsProject ipsProject,
			boolean includeSubtypes) throws CoreException {
		TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
		IIpsProject[] searchProjects = ipsProject.getReferencingProjectLeavesOrSelf();
		for (IIpsProject project : searchProjects) {
			result.addAll(Arrays.asList(project.findAllTestCaseSrcFiles(this)));
		}
		return result.toArray(new IIpsSrcFile[result.size()]);
	}
}
