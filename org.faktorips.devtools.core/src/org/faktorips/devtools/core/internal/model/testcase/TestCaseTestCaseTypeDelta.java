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

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation class to compute the delta between a test case and the test case type the test
 * case is based on.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTestCaseTypeDelta implements ITestCaseTestCaseTypeDelta {

    private ITestCase testCase;
    private ITestCaseType testCaseType;

    // TestCase Side
    private ITestValue[] testValuesWithMissingTestValueParam;
    private ITestRule[] testRulesWithMissingTestRuleParam;
    private ITestPolicyCmpt[] testPolicyCmptsWithMissingTypeParam;
    private ITestPolicyCmptLink[] testPolicyCmptLinksWithMissingTypeParam;
    private ITestAttributeValue[] testAttributeValuesWithMissingTestAttribute;
    private ITestPolicyCmpt[] testPolicyCmptWithDifferentSortOrder;
    private ITestPolicyCmpt[] testPolicyCmptWithDifferentSortOrderTestAttr;

    // TestCaseTypeSide
    private ITestValueParameter[] testValueParametersWithMissingTestValue;
    private ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParametersWithMissingTestPolicyCmpt;
    private ITestAttribute[] testAttributesWithMissingTestAttributeValue;

    private boolean differentTestParameterOrder = false;

    // Contains the corresponding test policy cmpt (value) for the missing test attributs (key)
    private HashMap testAttributes2TestPolicyCmpt = new HashMap();

    // Contains test policy cmpt with wrong sort order (childs)
    private List testPolicyCmptChildWithWrongSortOrder = new ArrayList();

    // Contains test policy cmpt with wrong sort order (attributes)
    private List testPolicyCmptWithWrongSortOrderAttribute = new ArrayList();

    // Contains the test case objects with missing test case type parameter
    private List testCaseSideObjects;
    private boolean errorInTestCaseType;

    // ipsproject used to search
    private IIpsProject ipsProject;

    public TestCaseTestCaseTypeDelta(ITestCase testCase, ITestCaseType testCaseType) throws CoreException {
        ArgumentCheck.notNull(testCase);
        ArgumentCheck.notNull(testCaseType);
        this.testCase = testCase;
        this.testCaseType = testCaseType;
        ipsProject = testCase.getIpsProject();

        if (testCaseType.validate(ipsProject).containsErrorMsg()) {
            errorInTestCaseType = true;
            return;
        }

        // test case side
        computeTestValueWithMissingTestParameter();
        computeTestRuleWithMissingTestParameter();
        testCaseSideObjects = computeTestPolicyCmptStructWithMissingTestParameter();

        // test case type side
        computeTestValueParameterWithMissingTestValue();
        computeTestPolicyCmptTypeParameterWithMissingTestPolicyCmpt(testCaseSideObjects);
        computeTestRuleSortOrder();

        testPolicyCmptWithDifferentSortOrderTestAttr = (ITestPolicyCmpt[])testPolicyCmptWithWrongSortOrderAttribute
                .toArray(new ITestPolicyCmpt[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getTestPolicyCmptForMissingTestAttribute(ITestAttribute testAttribute) {
        List testPolicyCmptsWithMissingTestAttr = (List)testAttributes2TestPolicyCmpt.get(testAttribute);
        if (testPolicyCmptsWithMissingTestAttr != null) {
            return (ITestPolicyCmpt[])testPolicyCmptsWithMissingTestAttr.toArray(new ITestPolicyCmpt[0]);
        } else {
            return new ITestPolicyCmpt[0];
        }
    }

    /*
     * Computes all missing test values (test case side)
     */
    private void computeTestValueParameterWithMissingTestValue() {
        List missing = new ArrayList();
        ITestValueParameter[] params = testCaseType.getTestValueParameters();
        List values = Arrays.asList(testCase.getTestValues());
        for (int i = 0; i < params.length; i++) {
            boolean found = false;
            int idxInTestCase = 0;
            for (Iterator iter = values.iterator(); iter.hasNext();) {
                ITestValue value = (ITestValue)iter.next();
                if (value.getTestValueParameter().equals(params[i].getName())) {
                    found = true;
                    // check if the order is equal
                    checkSortOrder(params[i], value);
                    break;
                }
                idxInTestCase++;
            }
            if (!found) {
                missing.add(params[i]);
            }
        }
        testValueParametersWithMissingTestValue = (ITestValueParameter[])missing.toArray(new ITestValueParameter[0]);
    }

    /*
     * Computes all missing test values (test case side)
     */
    private void computeTestRuleSortOrder() {
        if (differentTestParameterOrder) {
            // already different, don't check for further differences
            return;
        }
        ITestRuleParameter[] params = testCaseType.getTestRuleParameters();
        List rules = Arrays.asList(testCase.getTestRuleObjects());
        if (rules.size() == 0) {
            // if no rules exists in the test case then don't do a sort order check
            return;
        }

        for (int i = 0; i < params.length; i++) {
            int idxInTestCase = 0;
            for (Iterator iter = rules.iterator(); iter.hasNext();) {
                ITestRule rule = (ITestRule)iter.next();
                if (rule.getTestRuleParameter().equals(params[i].getName())) {
                    // check if the order is equal
                    checkSortOrder(params[i], rule);
                    break;
                }
            }
            idxInTestCase++;
            if (differentTestParameterOrder) {
                // abort because at least on difference found
                break;
            }
        }
    }

    /*
     * Check the sort order of the root objects
     */
    private void checkSortOrder(ITestParameter testParameter, ITestObject testObject) {
        if (differentTestParameterOrder) {
            // no more check necessary, is already in delta
            return;
        }

        // compare the sort order of the root objects
        List testParams = new ArrayList();
        testParams.addAll(Arrays.asList(testCaseType.getTestParameters()));
        List testObjects = new ArrayList();
        testObjects.addAll(Arrays.asList(testCase.getTestObjects()));

        // if the test parameter is a rule param and no test objects exists don't check the order
        if (testParameter instanceof ITestRuleParameter && testCase.getTestRule(testParameter.getName()).length == 0) {
            return;
        }

        removeTestRulesWithSameParamFromLists(testParams, testObjects);
        List cleanedList = removeTestPolicyCmptParamsWithNoTestObjectFromLists(testParams, testObjects);

        int idxInTestCaseType = cleanedList.indexOf(testParameter);
        int idxInTestCase = testObjects.indexOf(testObject);
        if (idxInTestCase == -1) {
            throw new RuntimeException("Object not found in test case: " + testObject); //$NON-NLS-1$
        }
        // if (idxInTestCaseType == -1) {
        //            throw new RuntimeException("Object not found in test case type: " + testParameter); //$NON-NLS-1$
        // }

        if (idxInTestCaseType != idxInTestCase) {
            differentTestParameterOrder = true;
        }
    }

    private List removeTestPolicyCmptParamsWithNoTestObjectFromLists(List testParams, List testObjects) {
        List testObjectParams = new ArrayList(testObjects.size());
        // remove all parameter with no test objects
        for (int i = 0; i < testObjects.size(); i++) {
            testObjectParams.add(((ITestObject)testObjects.get(i)).getTestParameterName());
        }

        List paramsWithTestObjectsOnly = new ArrayList(testParams.size());
        for (int i = 0; i < testParams.size(); i++) {
            ITestParameter testParameter = (ITestParameter)testParams.get(i);
            if (!(testParameter instanceof ITestPolicyCmptTypeParameter)) {
                // non ITestPolicyCmptTypeParameter are mandatory
                paramsWithTestObjectsOnly.add(testParameter);
                continue;
            }
            if (testObjectParams.contains(testParameter.getName())) {
                paramsWithTestObjectsOnly.add(testParameter);
            }
        }
        return paramsWithTestObjectsOnly;
    }

    /*
     * Removes not distinct test rule object (wich have the same test rule parameter from the list)
     * and removes test rule parameter which have no test rule (test rules are optional)
     */
    private void removeTestRulesWithSameParamFromLists(List testParams, List testObjects) {
        // Removes not distinct test rule object (wich have the same test rule parameter from the
        // list)
        List elementsToRemove = new ArrayList();
        String prevTestRuleParam = null;
        for (Iterator iter = testObjects.iterator(); iter.hasNext();) {
            ITestObject element = (ITestObject)iter.next();
            if (element instanceof ITestRule) {
                String testRuleParam = ((ITestRule)element).getTestRuleParameter();
                if (testRuleParam.equals(prevTestRuleParam)) {
                    elementsToRemove.add(element);
                }
                prevTestRuleParam = testRuleParam;
            }
        }
        for (Iterator iter = elementsToRemove.iterator(); iter.hasNext();) {
            testObjects.remove(iter.next());
        }

        // Removes test rule parameter which have no test rule (test rules are optional)
        elementsToRemove = new ArrayList();
        for (Iterator iter = testParams.iterator(); iter.hasNext();) {
            ITestParameter element = (ITestParameter)iter.next();
            if (element instanceof ITestRuleParameter) {
                if (testCase.getTestRule(element.getName()).length == 0) {
                    elementsToRemove.add(element);
                }
            }
        }
        for (Iterator iter = elementsToRemove.iterator(); iter.hasNext();) {
            testParams.remove(iter.next());
        }
    }

    /*
     * Check if the child has a different sort order
     */
    private boolean hasChildDifferntSortOrder(ITestPolicyCmpt cmpt) {
        if (testPolicyCmptChildWithWrongSortOrder.contains(cmpt)) {
            return true;
        }

        return false;
    }

    /*
     * Computes all missing test policy cmpts, test policy cmpt links and test attribute values
     * (test case side)
     */
    private void computeTestPolicyCmptTypeParameterWithMissingTestPolicyCmpt(List testCaseSideObjects)
            throws CoreException {
        List missingTestPolicyCmptTypeParameter = new ArrayList();
        missingTestPolicyCmptTypeParameter.addAll(Arrays.asList(testCaseType.getTestPolicyCmptTypeParameters()));
        List missingTestAttributes = new ArrayList();
        List differentSortOrderForTestPolicyCmpts = new ArrayList();

        // search the corresponding test case type parameter object for each test case side object
        // in the given list
        // and if found remove it from the list. The resulting list contains test parameter object
        // with no corresponding
        // object on the test case side.
        for (Iterator iter = testCaseSideObjects.iterator(); iter.hasNext();) {
            IIpsObjectPart element = (IIpsObjectPart)iter.next();
            if (element instanceof ITestPolicyCmpt) {
                ITestPolicyCmptTypeParameter param = ((ITestPolicyCmpt)element)
                        .findTestPolicyCmptTypeParameter(ipsProject);
                // ignore if the test parameter wasn't found, this was already checked on the test
                // case side
                if (param != null) {
                    missingTestPolicyCmptTypeParameter.remove(param);
                    computeTestAttributeWithMissingTestAttributeValue(param, (ITestPolicyCmpt)element,
                            missingTestAttributes);

                    if (((ITestPolicyCmpt)element).isRoot()) {
                        // check if the order is equal
                        checkSortOrder(param, (ITestPolicyCmpt)element);
                    } else {
                        ITestPolicyCmpt parent = ((ITestPolicyCmpt)element).getParentTestPolicyCmpt();
                        if (differentSortOrderForTestPolicyCmpts.contains(parent)) {
                            continue;
                        }

                        if (hasChildDifferntSortOrder((ITestPolicyCmpt)element)) {
                            differentTestParameterOrder = true;
                            differentSortOrderForTestPolicyCmpts.add(parent);
                        }
                    }
                }
            }
        }

        // remove root parameter, because root parameter will be added by the user
        List toRemove = new ArrayList();
        for (Iterator iterator = missingTestPolicyCmptTypeParameter.iterator(); iterator.hasNext();) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)iterator.next();
            if (testPolicyCmptTypeParameter.isRoot()) {
                toRemove.add(testPolicyCmptTypeParameter);
            }
        }
        for (Iterator iterator = toRemove.iterator(); iterator.hasNext();) {
            missingTestPolicyCmptTypeParameter.remove(iterator.next());
        }

        testPolicyCmptTypeParametersWithMissingTestPolicyCmpt = (ITestPolicyCmptTypeParameter[])missingTestPolicyCmptTypeParameter
                .toArray(new ITestPolicyCmptTypeParameter[0]);
        testAttributesWithMissingTestAttributeValue = (ITestAttribute[])missingTestAttributes
                .toArray(new ITestAttribute[0]);
        testPolicyCmptWithDifferentSortOrder = (ITestPolicyCmpt[])differentSortOrderForTestPolicyCmpts
                .toArray(new ITestPolicyCmpt[0]);
    }

    /*
     * Computes all missing test attribute values (test case side)
     */
    private void computeTestAttributeWithMissingTestAttributeValue(ITestPolicyCmptTypeParameter param,
            ITestPolicyCmpt cmpt,
            List missingTestAttributes) throws CoreException {
        List testAttributes = new ArrayList();
        testAttributes.addAll(Arrays.asList(param.getTestAttributes()));
        ITestAttributeValue[] testAttrValues = cmpt.getTestAttributeValues();
        for (int i = 0; i < testAttrValues.length; i++) {
            // ignore if the test attribute wasn't found, this was already checked on the test case
            // side
            ITestAttribute testAttr = testAttrValues[i].findTestAttribute(testCase.getIpsProject());
            if (testAttr != null) {
                testAttributes.remove(testAttr);
            }
        }
        // add the resulting list of test attributes (these objects wasn't found by the test case
        // side)
        for (Iterator iter = testAttributes.iterator(); iter.hasNext();) {
            ITestAttribute testAttr = (ITestAttribute)iter.next();
            if (!missingTestAttributes.contains(testAttr)) {
                missingTestAttributes.add(testAttr);
            }
        }

        // if there are missing test attributes, store the corresponding test policy cmpt
        for (Iterator iter = testAttributes.iterator(); iter.hasNext();) {
            ITestAttribute testAttr = (ITestAttribute)iter.next();
            List cmptWithMissingTestAttrList = (List)testAttributes2TestPolicyCmpt.get(testAttr);
            if (cmptWithMissingTestAttrList == null) {
                cmptWithMissingTestAttrList = new ArrayList(1);
            }
            cmptWithMissingTestAttrList.add(cmpt);
            testAttributes2TestPolicyCmpt.put(testAttr, cmptWithMissingTestAttrList);
            // furthermore indicate a different sort oder of the test attributes
            addDifferentTestAttributeSortOrder(cmpt);
        }
    }

    /*
     * Computes all missing test value parameters (test case type side).
     */
    private void computeTestValueWithMissingTestParameter() throws CoreException {
        List missing = new ArrayList();
        ITestValue[] testValues = testCase.getTestValues();
        for (int i = 0; i < testValues.length; i++) {
            ITestParameter testParameter = testCaseType.getTestParameterByName(testValues[i].getTestValueParameter());
            if (testParameter == null) {
                // not found by name
                missing.add(testValues[i]);
            } else if (!(testParameter instanceof ITestValueParameter)) {
                // wrong instanceof
                missing.add(testValues[i]);
            }
        }
        testValuesWithMissingTestValueParam = (ITestValue[])missing.toArray(new ITestValue[0]);
    }

    /*
     * Computes all missing test value parameters (test case type side).
     */
    private void computeTestRuleWithMissingTestParameter() throws CoreException {
        List missing = new ArrayList();
        ITestRule[] testRules = testCase.getTestRuleObjects();
        for (int i = 0; i < testRules.length; i++) {
            ITestParameter testParameter = testCaseType.getTestParameterByName(testRules[i].getTestRuleParameter());
            if (testParameter == null) {
                // not found by name
                missing.add(testRules[i]);
            } else if (!(testParameter instanceof ITestRuleParameter)) {
                // wrong instanceof
                missing.add(testRules[i]);
            }
        }
        testRulesWithMissingTestRuleParam = (ITestRule[])missing.toArray(new ITestRule[0]);
    }

    /*
     * Computes all missing test case type side objects.
     */
    private List computeTestPolicyCmptStructWithMissingTestParameter() throws CoreException {
        List missingTestPolicyCmpts = new ArrayList();
        List missingTestPolicyCmptLinks = new ArrayList();
        List missingTestAttributeValues = new ArrayList();

        // helper list to store all test policy cmpt and test policy cmpt links
        // will be used later to search for params without these test case objects
        List allTestPolicyCmpt = new ArrayList();

        ITestPolicyCmpt[] testPolicyCmpts = testCase.getTestPolicyCmpts();
        for (int i = 0; i < testPolicyCmpts.length; i++) {
            // store only root objects in the list of test case objetcs, don't care about child
            // elements
            // this is done by validation inside the test case
            allTestPolicyCmpt.add(testPolicyCmpts[i]);
            ITestPolicyCmpt cmpt = testPolicyCmpts[i];
            computeTestPolicyCmptStructWithMissingTestParameter(cmpt, missingTestPolicyCmpts,
                    missingTestPolicyCmptLinks, missingTestAttributeValues, allTestPolicyCmpt);
        }

        testPolicyCmptsWithMissingTypeParam = (ITestPolicyCmpt[])missingTestPolicyCmpts.toArray(new ITestPolicyCmpt[0]);
        testPolicyCmptLinksWithMissingTypeParam = (ITestPolicyCmptLink[])missingTestPolicyCmptLinks
                .toArray(new ITestPolicyCmptLink[0]);
        testAttributeValuesWithMissingTestAttribute = (ITestAttributeValue[])missingTestAttributeValues
                .toArray(new ITestAttributeValue[0]);

        return allTestPolicyCmpt;
    }

    /*
     * Computes all missing test case type side objects, starting with given test policy cmpt.
     */
    private void computeTestPolicyCmptStructWithMissingTestParameter(ITestPolicyCmpt cmpt,
            List missingTestPolicyCmpts,
            List missingTestPolicyCmptLinks,
            List missingTestAttributeValues,
            List allTestPolicyCmpt) throws CoreException {

        ITestPolicyCmptTypeParameter param = cmpt.findTestPolicyCmptTypeParameter(ipsProject);
        if (param == null) {
            missingTestPolicyCmpts.add(cmpt);
        } else {
            // search the sub content of the test policy cmpt
            computeTestPolicyCmptLinkStructWithMissingTestParameter(cmpt.getTestPolicyCmptLinks(),
                    missingTestPolicyCmpts, missingTestPolicyCmptLinks, missingTestAttributeValues, allTestPolicyCmpt);
            computeTestAttributeValuesWithMissingTestAttribute(cmpt, missingTestAttributeValues);
            computeSortOrderOfTestAttributes(cmpt, param);
        }
    }

    /*
     * Computes (compares) the sort order of the test attributes inside the given cmpt with the
     * given param
     */
    private void computeSortOrderOfTestAttributes(ITestPolicyCmpt cmpt, ITestPolicyCmptTypeParameter param) {
        if (testPolicyCmptWithWrongSortOrderAttribute.contains(cmpt)) {
            // don't check, because it was already detected that the test policy cmpt's test
            // attribute values
            // are in a different sort order
            return;
        }
        ITestAttributeValue[] testAttrValue = cmpt.getTestAttributeValues();
        ITestAttribute[] testAttr = param.getTestAttributes();
        if (testAttrValue.length != testAttr.length) {
            addDifferentTestAttributeSortOrder(cmpt);
            return;
        }
        for (int i = 0; i < testAttr.length; i++) {
            if (!testAttr[i].getName().equals(testAttrValue[i].getTestAttribute())) {
                addDifferentTestAttributeSortOrder(cmpt);
                return;
            }
        }
    }

    /*
     * Computes all missing test case type side objects, starting with given test policy cmpt link.
     */
    private List computeTestPolicyCmptLinkStructWithMissingTestParameter(ITestPolicyCmptLink[] testPolicyCmptLinks,
            List missingTestPolicyCmpt,
            List missingTestPolicyCmptLink,
            List missingTestAttributeValue,
            List allTestPolicyCmpt) throws CoreException {

        // TODO mit Joerg besprechen: Wieso reichen wir die ganzen Listen durch die Gegend.
        List objects = new ArrayList();

        ITestPolicyCmptTypeParameter prevParam = null;
        for (int i = 0; i < testPolicyCmptLinks.length; i++) {
            objects.add(testPolicyCmptLinks[i]);
            ITestPolicyCmptTypeParameter param = testPolicyCmptLinks[i].findTestPolicyCmptTypeParameter(ipsProject);
            if (param == null) {
                missingTestPolicyCmptLink.add(testPolicyCmptLinks[i]);
            } else {
                if (testPolicyCmptLinks[i].isComposition()) {
                    ITestPolicyCmpt cmpt = testPolicyCmptLinks[i].findTarget();
                    if (cmpt == null) {
                        // ignore error if target of link not found
                        continue;
                    }
                    // add the child test policy cmpt, thus the attributes could be checked later
                    allTestPolicyCmpt.add(cmpt);

                    // check the sort order
                    if (prevParam != null && isWrongIfTestPolicyCmptLinkSortOrder(prevParam, param)) {
                        testPolicyCmptChildWithWrongSortOrder.add(cmpt);
                    }

                    computeTestPolicyCmptStructWithMissingTestParameter(cmpt, missingTestPolicyCmpt,
                            missingTestPolicyCmptLink, missingTestAttributeValue, allTestPolicyCmpt);
                }
            }
            prevParam = param;
        }
        return objects;
    }

    /*
     * Check if the sort order of the given link is wrong. Returns true if the order is different.
     */
    private boolean isWrongIfTestPolicyCmptLinkSortOrder(ITestPolicyCmptTypeParameter prevParam,
            ITestPolicyCmptTypeParameter param) {
        ArgumentCheck.isTrue(!param.isRoot());
        ITestPolicyCmptTypeParameter parentPrev = (ITestPolicyCmptTypeParameter)prevParam.getParent();
        ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter)param.getParent();
        ArgumentCheck.isTrue(parent == parentPrev);
        ITestPolicyCmptTypeParameter[] childs = parent.getTestPolicyCmptTypeParamChilds();
        boolean prevBefore = false;
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] == prevParam) {
                prevBefore = true;
            } else if (childs[i] == param && !prevBefore) {
                return true;
            }
        }
        return false;
    }

    /*
     * Computes the missing test attribues (test case type side).
     */
    private void computeTestAttributeValuesWithMissingTestAttribute(ITestPolicyCmpt cmpt, List missingTestAttributeValue)
            throws CoreException {
        ITestAttributeValue testAttributeValues[] = cmpt.getTestAttributeValues();
        for (int i = 0; i < testAttributeValues.length; i++) {
            ITestAttribute testAttribute = testAttributeValues[i].findTestAttribute(testCase.getIpsProject());
            if (testAttribute == null) {
                missingTestAttributeValue.add(testAttributeValues[i]);
                // indicate a different sort oder of the test attributes
                addDifferentTestAttributeSortOrder(cmpt);
            }
        }
    }

    /*
     * Adds and indicates the different sort order of test attributes
     */
    private void addDifferentTestAttributeSortOrder(ITestPolicyCmpt policyCmpt) {
        if (testPolicyCmptWithWrongSortOrderAttribute.contains(policyCmpt)) {
            return;
        }
        testPolicyCmptWithWrongSortOrderAttribute.add(policyCmpt);
        differentTestParameterOrder = true;

        // assert that the list which will will be used in the interface is not filled yet,
        // if true there is a coding error, means wrong execution order of method calls!
        ArgumentCheck.isTrue(testPolicyCmptWithDifferentSortOrder == null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return errorInTestCaseType || testValuesWithMissingTestValueParam.length == 0
                && testPolicyCmptsWithMissingTypeParam.length == 0
                && testPolicyCmptLinksWithMissingTypeParam.length == 0
                && testAttributeValuesWithMissingTestAttribute.length == 0
                && testAttributesWithMissingTestAttributeValue.length == 0
                && testPolicyCmptTypeParametersWithMissingTestPolicyCmpt.length == 0
                && testValueParametersWithMissingTestValue.length == 0 && testRulesWithMissingTestRuleParam.length == 0
                && !differentTestParameterOrder;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDifferentTestParameterOrder() {
        return differentTestParameterOrder;
    }

    /**
     * {@inheritDoc}
     */
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }

    /**
     * {@inheritDoc}
     */
    public ITestCase getTestCase() {
        return testCase;
    }

    //
    // Missing test case type side objects
    //

    public ITestValue[] getTestValuesWithMissingTestValueParam() {
        return testValuesWithMissingTestValueParam;
    }

    public ITestRule[] getTestRulesWithMissingTestValueParam() {
        return testRulesWithMissingTestRuleParam;
    }

    public ITestPolicyCmpt[] getTestPolicyCmptsWithMissingTypeParam() {
        return testPolicyCmptsWithMissingTypeParam;
    }

    public ITestPolicyCmptLink[] getTestPolicyCmptLinkWithMissingTypeParam() {
        return testPolicyCmptLinksWithMissingTypeParam;
    }

    public ITestAttributeValue[] getTestAttributeValuesWithMissingTestAttribute() {
        return testAttributeValuesWithMissingTestAttribute;
    }

    public ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrder() {
        return testPolicyCmptWithDifferentSortOrder;
    }

    //
    // Missing test case type side objects
    //

    public ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrderTestAttr() {
        return testPolicyCmptWithDifferentSortOrderTestAttr;
    }

    public ITestValueParameter[] getTestValueParametersWithMissingTestValue() {
        return testValueParametersWithMissingTestValue;
    }

    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt() {
        return testPolicyCmptTypeParametersWithMissingTestPolicyCmpt;
    }

    public ITestAttribute[] getTestAttributesWithMissingTestAttributeValue() {
        return testAttributesWithMissingTestAttributeValue;
    }
}
