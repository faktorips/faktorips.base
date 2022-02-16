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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.AbstractFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation class to compute the delta between a test case and the test case type the test
 * case is based on.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTestCaseTypeDelta extends AbstractFixDifferencesComposite implements ITestCaseTestCaseTypeDelta {

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

    /** Contains the corresponding test policy cmpt (value) for the missing test attributs (key) */
    private HashMap<ITestAttribute, List<ITestPolicyCmpt>> testAttributes2TestPolicyCmpt = new HashMap<>();

    /** Contains test policy cmpt with wrong sort order (childs) */
    private List<ITestPolicyCmpt> testPolicyCmptChildWithWrongSortOrder = new ArrayList<>();

    /** Contains test policy cmpt with wrong sort order (attributes) */
    private List<ITestPolicyCmpt> testPolicyCmptWithWrongSortOrderAttribute = new ArrayList<>();

    /** Contains the test case objects with missing test case type parameter */
    private List<ITestPolicyCmpt> testCaseSideObjects;

    private boolean errorInTestCaseType;

    /** ipsproject used to search */
    private IIpsProject ipsProject;

    public TestCaseTestCaseTypeDelta(ITestCase testCase, ITestCaseType testCaseType) {
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

        testPolicyCmptWithDifferentSortOrderTestAttr = testPolicyCmptWithWrongSortOrderAttribute
                .toArray(new ITestPolicyCmpt[0]);
    }

    @Override
    public IIpsElement getCorrespondingIpsElement() {
        return getTestCase();
    }

    @Override
    public ITestPolicyCmpt[] getTestPolicyCmptForMissingTestAttribute(ITestAttribute testAttribute) {
        List<?> testPolicyCmptsWithMissingTestAttr = testAttributes2TestPolicyCmpt.get(testAttribute);
        if (testPolicyCmptsWithMissingTestAttr != null) {
            return testPolicyCmptsWithMissingTestAttr.toArray(new ITestPolicyCmpt[0]);
        } else {
            return new ITestPolicyCmpt[0];
        }
    }

    /**
     * Computes all missing test values (test case side)
     */
    private void computeTestValueParameterWithMissingTestValue() {
        List<ITestValueParameter> missing = new ArrayList<>();
        ITestValueParameter[] params = testCaseType.getTestValueParameters();
        List<ITestValue> values = Arrays.asList(testCase.getTestValues());
        for (ITestValueParameter param : params) {
            boolean found = false;
            for (ITestValue value : values) {
                if (value.getTestValueParameter().equals(param.getName())) {
                    found = true;
                    // check if the order is equal
                    checkSortOrder(param, value);
                    break;
                }
            }
            if (!found) {
                missing.add(param);
            }
        }
        testValueParametersWithMissingTestValue = missing.toArray(new ITestValueParameter[0]);
    }

    /**
     * Computes all missing test values (test case side)
     */
    private void computeTestRuleSortOrder() {
        if (differentTestParameterOrder) {
            // already different, don't check for further differences
            return;
        }
        ITestRuleParameter[] params = testCaseType.getTestRuleParameters();
        List<ITestRule> rules = Arrays.asList(testCase.getTestRuleObjects());
        if (rules.size() == 0) {
            // if no rules exists in the test case then don't do a sort order check
            return;
        }

        for (ITestRuleParameter param : params) {
            for (ITestRule rule : rules) {
                if (rule.getTestRuleParameter().equals(param.getName())) {
                    // check if the order is equal
                    checkSortOrder(param, rule);
                    break;
                }
            }
            if (differentTestParameterOrder) {
                // abort because at least on difference found
                break;
            }
        }
    }

    /**
     * Check the sort order of the root objects
     */
    private void checkSortOrder(ITestParameter testParameter, ITestObject testObject) {
        if (differentTestParameterOrder) {
            // no more check necessary, is already in delta
            return;
        }

        // compare the sort order of the root objects
        List<ITestParameter> testParams = new ArrayList<>();
        testParams.addAll(Arrays.asList(testCaseType.getTestParameters()));
        List<ITestObject> testObjects = new ArrayList<>();
        testObjects.addAll(Arrays.asList(testCase.getTestObjects()));

        // if the test parameter is a rule param and no test objects exists don't check the order
        if (testParameter instanceof ITestRuleParameter && testCase.getTestRule(testParameter.getName()).length == 0) {
            return;
        }

        removeTestRulesWithSameParamFromLists(testParams, testObjects);
        List<ITestParameter> cleanedList = removeTestPolicyCmptParamsWithNoTestObjectFromLists(testParams, testObjects);

        int idxInTestCaseType = cleanedList.indexOf(testParameter);
        int idxInTestCase = testObjects.indexOf(testObject);
        if (idxInTestCase == -1) {
            throw new RuntimeException("Object not found in test case: " + testObject); //$NON-NLS-1$
        }

        if (idxInTestCaseType != idxInTestCase) {
            differentTestParameterOrder = true;
        }
    }

    private List<ITestParameter> removeTestPolicyCmptParamsWithNoTestObjectFromLists(List<ITestParameter> testParams,
            List<ITestObject> testObjects) {

        List<String> testObjectParams = new ArrayList<>(testObjects.size());
        // remove all parameter with no test objects
        for (int i = 0; i < testObjects.size(); i++) {
            testObjectParams.add(testObjects.get(i).getTestParameterName());
        }

        List<ITestParameter> paramsWithTestObjectsOnly = new ArrayList<>(testParams.size());
        for (int i = 0; i < testParams.size(); i++) {
            ITestParameter testParameter = testParams.get(i);
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

    /**
     * Removes not distinct test rule object (wich have the same test rule parameter from the list)
     * and removes test rule parameter which have no test rule (test rules are optional)
     */
    private void removeTestRulesWithSameParamFromLists(List<ITestParameter> testParams, List<ITestObject> testObjects) {
        /*
         * Removes not distinct test rule object (wich have the same test rule parameter from the
         * list)
         */
        List<IIpsObjectPart> elementsToRemove = new ArrayList<>();
        String prevTestRuleParam = null;
        for (ITestObject element : testObjects) {
            if (element instanceof ITestRule) {
                String testRuleParam = ((ITestRule)element).getTestRuleParameter();
                if (testRuleParam.equals(prevTestRuleParam)) {
                    elementsToRemove.add(element);
                }
                prevTestRuleParam = testRuleParam;
            }
        }
        for (IIpsObjectPart iIpsObjectPart : elementsToRemove) {
            testObjects.remove(iIpsObjectPart);
        }

        // Removes test rule parameter which have no test rule (test rules are optional)
        elementsToRemove = new ArrayList<>();
        for (ITestParameter element : testParams) {
            if (element instanceof ITestRuleParameter) {
                if (testCase.getTestRule(element.getName()).length == 0) {
                    elementsToRemove.add(element);
                }
            }
        }
        for (IIpsObjectPart iIpsObjectPart : elementsToRemove) {
            testParams.remove(iIpsObjectPart);
        }
    }

    /**
     * Check if the child has a different sort order
     */
    private boolean hasChildDifferntSortOrder(ITestPolicyCmpt cmpt) {
        if (testPolicyCmptChildWithWrongSortOrder.contains(cmpt)) {
            return true;
        }

        return false;
    }

    /**
     * Computes all missing test policy cmpts, test policy cmpt links and test attribute values
     * (test case side)
     */
    private void computeTestPolicyCmptTypeParameterWithMissingTestPolicyCmpt(List<ITestPolicyCmpt> testCaseSideObjects)
            {

        List<ITestPolicyCmptTypeParameter> missingTestPolicyCmptTypeParameter = new ArrayList<>();
        missingTestPolicyCmptTypeParameter.addAll(Arrays.asList(testCaseType.getTestPolicyCmptTypeParameters()));
        List<ITestAttribute> missingTestAttributes = new ArrayList<>();
        List<ITestPolicyCmpt> differentSortOrderForTestPolicyCmpts = new ArrayList<>();

        /*
         * search the corresponding test case type parameter object for each test case side object
         * in the given list and if found remove it from the list. The resulting list contains test
         * parameter object with no corresponding object on the test case side.
         */
        for (ITestPolicyCmpt element : testCaseSideObjects) {
            ITestPolicyCmptTypeParameter param = element
                    .findTestPolicyCmptTypeParameter(ipsProject);
            /*
             * ignore if the test parameter wasn't found, this was already checked on the test case
             * side
             */
            if (param != null) {
                missingTestPolicyCmptTypeParameter.remove(param);
                computeTestAttributeWithMissingTestAttributeValue(param, element,
                        missingTestAttributes);

                if (element.isRoot()) {
                    // check if the order is equal
                    checkSortOrder(param, element);
                } else {
                    ITestPolicyCmpt parent = element.getParentTestPolicyCmpt();
                    if (differentSortOrderForTestPolicyCmpts.contains(parent)) {
                        continue;
                    }

                    if (hasChildDifferntSortOrder(element)) {
                        differentTestParameterOrder = true;
                        differentSortOrderForTestPolicyCmpts.add(parent);
                    }
                }
            }
        }

        // remove root parameter, because root parameter will be added by the user
        List<ITestPolicyCmptTypeParameter> toRemove = new ArrayList<>();
        for (ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter : missingTestPolicyCmptTypeParameter) {
            if (testPolicyCmptTypeParameter.isRoot()) {
                toRemove.add(testPolicyCmptTypeParameter);
            }
        }
        for (ITestPolicyCmptTypeParameter iTestPolicyCmptTypeParameter : toRemove) {
            missingTestPolicyCmptTypeParameter.remove(iTestPolicyCmptTypeParameter);
        }

        testPolicyCmptTypeParametersWithMissingTestPolicyCmpt = missingTestPolicyCmptTypeParameter
                .toArray(new ITestPolicyCmptTypeParameter[0]);
        testAttributesWithMissingTestAttributeValue = missingTestAttributes.toArray(new ITestAttribute[0]);
        testPolicyCmptWithDifferentSortOrder = differentSortOrderForTestPolicyCmpts.toArray(new ITestPolicyCmpt[0]);
    }

    /**
     * Computes all missing test attribute values (test case side)
     */
    private void computeTestAttributeWithMissingTestAttributeValue(ITestPolicyCmptTypeParameter param,
            ITestPolicyCmpt cmpt,
            List<ITestAttribute> missingTestAttributes) {

        List<ITestAttribute> testAttributes = new ArrayList<>();
        testAttributes.addAll(Arrays.asList(param.getTestAttributes()));
        ITestAttributeValue[] testAttrValues = cmpt.getTestAttributeValues();
        for (ITestAttributeValue testAttrValue : testAttrValues) {
            /*
             * ignore if the test attribute wasn't found, this was already checked on the test case
             * side
             */
            ITestAttribute testAttr = testAttrValue.findTestAttribute(testCase.getIpsProject());
            if (testAttr != null) {
                testAttributes.remove(testAttr);
            }
        }
        /*
         * add the resulting list of test attributes (these objects wasn't found by the test case
         * side)
         */
        for (ITestAttribute testAttr : testAttributes) {
            if (!missingTestAttributes.contains(testAttr)) {
                missingTestAttributes.add(testAttr);
            }
        }

        // if there are missing test attributes, store the corresponding test policy cmpt
        for (ITestAttribute testAttr : testAttributes) {
            List<ITestPolicyCmpt> cmptWithMissingTestAttrList = testAttributes2TestPolicyCmpt.computeIfAbsent(testAttr,
                    $ -> new ArrayList<>(1));
            cmptWithMissingTestAttrList.add(cmpt);
            // furthermore indicate a different sort oder of the test attributes
            addDifferentTestAttributeSortOrder(cmpt);
        }
    }

    /**
     * Computes all missing test value parameters (test case type side).
     */
    private void computeTestValueWithMissingTestParameter() {
        List<ITestValue> missing = new ArrayList<>();
        ITestValue[] testValues = testCase.getTestValues();
        for (ITestValue testValue : testValues) {
            ITestParameter testParameter = testCaseType.getTestParameterByName(testValue.getTestValueParameter());
            if (testParameter == null) {
                // not found by name
                missing.add(testValue);
            } else if (!(testParameter instanceof ITestValueParameter)) {
                // wrong instanceof
                missing.add(testValue);
            }
        }
        testValuesWithMissingTestValueParam = missing.toArray(new ITestValue[0]);
    }

    /**
     * Computes all missing test value parameters (test case type side).
     */
    private void computeTestRuleWithMissingTestParameter() {
        List<ITestRule> missing = new ArrayList<>();
        ITestRule[] testRules = testCase.getTestRuleObjects();
        for (ITestRule testRule : testRules) {
            ITestParameter testParameter = testCaseType.getTestParameterByName(testRule.getTestRuleParameter());
            if (testParameter == null) {
                // not found by name
                missing.add(testRule);
            } else if (!(testParameter instanceof ITestRuleParameter)) {
                // wrong instanceof
                missing.add(testRule);
            }
        }
        testRulesWithMissingTestRuleParam = missing.toArray(new ITestRule[0]);
    }

    /**
     * Computes all missing test case type side objects.
     */
    private List<ITestPolicyCmpt> computeTestPolicyCmptStructWithMissingTestParameter() {
        List<ITestPolicyCmpt> missingTestPolicyCmpts = new ArrayList<>();
        List<ITestPolicyCmptLink> missingTestPolicyCmptLinks = new ArrayList<>();
        List<ITestAttributeValue> missingTestAttributeValues = new ArrayList<>();

        /*
         * helper list to store all test policy cmpt and test policy cmpt links will be used later
         * to search for params without these test case objects
         */
        List<ITestPolicyCmpt> allTestPolicyCmpt = new ArrayList<>();

        ITestPolicyCmpt[] testPolicyCmpts = testCase.getTestPolicyCmpts();
        for (ITestPolicyCmpt cmpt : testPolicyCmpts) {
            /*
             * store only root objects in the list of test case objetcs, don't care about child
             * elements this is done by validation inside the test case
             */
            allTestPolicyCmpt.add(cmpt);
            computeTestPolicyCmptStructWithMissingTestParameter(cmpt, missingTestPolicyCmpts,
                    missingTestPolicyCmptLinks, missingTestAttributeValues, allTestPolicyCmpt);
        }

        testPolicyCmptsWithMissingTypeParam = missingTestPolicyCmpts.toArray(new ITestPolicyCmpt[0]);
        testPolicyCmptLinksWithMissingTypeParam = missingTestPolicyCmptLinks.toArray(new ITestPolicyCmptLink[0]);
        testAttributeValuesWithMissingTestAttribute = missingTestAttributeValues.toArray(new ITestAttributeValue[0]);

        return allTestPolicyCmpt;
    }

    /**
     * Computes all missing test case type side objects, starting with given test policy cmpt.
     */
    private void computeTestPolicyCmptStructWithMissingTestParameter(ITestPolicyCmpt cmpt,
            List<ITestPolicyCmpt> missingTestPolicyCmpts,
            List<ITestPolicyCmptLink> missingTestPolicyCmptLinks,
            List<ITestAttributeValue> missingTestAttributeValues,
            List<ITestPolicyCmpt> allTestPolicyCmpt) {

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

    /**
     * Computes (compares) the sort order of the test attributes inside the given cmpt with the
     * given param
     */
    private void computeSortOrderOfTestAttributes(ITestPolicyCmpt cmpt, ITestPolicyCmptTypeParameter param) {
        if (testPolicyCmptWithWrongSortOrderAttribute.contains(cmpt)) {
            /*
             * don't check, because it was already detected that the test policy cmpt's test
             * attribute values are in a different sort order
             */
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

    /**
     * Computes all missing test case type side objects, starting with given test policy cmpt link.
     */
    private List<ITestPolicyCmptLink> computeTestPolicyCmptLinkStructWithMissingTestParameter(
            ITestPolicyCmptLink[] testPolicyCmptLinks,
            List<ITestPolicyCmpt> missingTestPolicyCmpt,
            List<ITestPolicyCmptLink> missingTestPolicyCmptLink,
            List<ITestAttributeValue> missingTestAttributeValue,
            List<ITestPolicyCmpt> allTestPolicyCmpt) {

        // TODO mit Joerg besprechen: Wieso reichen wir die ganzen Listen durch die Gegend.
        List<ITestPolicyCmptLink> objects = new ArrayList<>();

        ITestPolicyCmptTypeParameter prevParam = null;
        for (ITestPolicyCmptLink testPolicyCmptLink : testPolicyCmptLinks) {
            objects.add(testPolicyCmptLink);
            ITestPolicyCmptTypeParameter param = testPolicyCmptLink.findTestPolicyCmptTypeParameter(ipsProject);
            if (param == null) {
                missingTestPolicyCmptLink.add(testPolicyCmptLink);
            } else {
                if (testPolicyCmptLink.isComposition()) {
                    ITestPolicyCmpt cmpt = testPolicyCmptLink.findTarget();
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

    /**
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
        for (ITestPolicyCmptTypeParameter child : childs) {
            if (child == prevParam) {
                prevBefore = true;
            } else if (child == param && !prevBefore) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the missing test attribues (test case type side).
     */
    private void computeTestAttributeValuesWithMissingTestAttribute(ITestPolicyCmpt cmpt,
            List<ITestAttributeValue> missingTestAttributeValue) {

        ITestAttributeValue[] testAttributeValues = cmpt.getTestAttributeValues();
        for (ITestAttributeValue testAttributeValue : testAttributeValues) {
            ITestAttribute testAttribute = testAttributeValue.findTestAttribute(testCase.getIpsProject());
            if (testAttribute == null) {
                missingTestAttributeValue.add(testAttributeValue);
                // indicate a different sort oder of the test attributes
                addDifferentTestAttributeSortOrder(cmpt);
            }
        }
    }

    /**
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

    @Override
    public boolean isEmptyThis() {
        return errorInTestCaseType || testValuesWithMissingTestValueParam.length == 0
                && testPolicyCmptsWithMissingTypeParam.length == 0
                && testPolicyCmptLinksWithMissingTypeParam.length == 0
                && testAttributeValuesWithMissingTestAttribute.length == 0
                && testAttributesWithMissingTestAttributeValue.length == 0
                && testPolicyCmptTypeParametersWithMissingTestPolicyCmpt.length == 0
                && testValueParametersWithMissingTestValue.length == 0 && testRulesWithMissingTestRuleParam.length == 0
                && !differentTestParameterOrder;
    }

    @Override
    public boolean isDifferentTestParameterOrder() {
        return differentTestParameterOrder;
    }

    @Override
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }

    @Override
    public ITestCase getTestCase() {
        return testCase;
    }

    //
    // Missing test case type side objects
    //

    @Override
    public ITestValue[] getTestValuesWithMissingTestValueParam() {
        return testValuesWithMissingTestValueParam;
    }

    @Override
    public ITestRule[] getTestRulesWithMissingTestValueParam() {
        return testRulesWithMissingTestRuleParam;
    }

    @Override
    public ITestPolicyCmpt[] getTestPolicyCmptsWithMissingTypeParam() {
        return testPolicyCmptsWithMissingTypeParam;
    }

    @Override
    public ITestPolicyCmptLink[] getTestPolicyCmptLinkWithMissingTypeParam() {
        return testPolicyCmptLinksWithMissingTypeParam;
    }

    @Override
    public ITestAttributeValue[] getTestAttributeValuesWithMissingTestAttribute() {
        return testAttributeValuesWithMissingTestAttribute;
    }

    @Override
    public ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrder() {
        return testPolicyCmptWithDifferentSortOrder;
    }

    //
    // Missing test case type side objects
    //

    @Override
    public ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrderTestAttr() {
        return testPolicyCmptWithDifferentSortOrderTestAttr;
    }

    @Override
    public ITestValueParameter[] getTestValueParametersWithMissingTestValue() {
        return testValueParametersWithMissingTestValue;
    }

    @Override
    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt() {
        return testPolicyCmptTypeParametersWithMissingTestPolicyCmpt;
    }

    @Override
    public ITestAttribute[] getTestAttributesWithMissingTestAttributeValue() {
        return testAttributesWithMissingTestAttributeValue;
    }

    /**
     * This fix method simply delegates to the
     * {@link ITestCase#fixDifferences(ITestCaseTestCaseTypeDelta)} method because in the old fix
     * differences of {@link ITestCase} the test case does handle the fix itself! {@inheritDoc}
     */
    @Override
    protected void fix() {
        try {
            testCase.fixDifferences(this);
        } catch (IpsException e) {
            IpsLog.log(e);
        }
    }
}
