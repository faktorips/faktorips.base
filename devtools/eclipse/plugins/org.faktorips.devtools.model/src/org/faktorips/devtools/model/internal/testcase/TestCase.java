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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.testcasetype.TestValueParameter;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
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
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.devtools.model.type.ITypeHierarchy;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * Test case class. Defines a concrete test case based on a test case type definition.
 * 
 * @author Joerg Ortmann
 */
public class TestCase extends IpsObject implements ITestCase {

    /** Name of corresponding test case type */
    private String testCaseTypeName = ""; //$NON-NLS-1$

    /** Children */
    private List<IIpsObjectPart> testObjects = new ArrayList<>();

    public TestCase(IIpsSrcFile file) {
        super(file);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return testObjects.toArray(new IIpsElement[testObjects.size()]);
    }

    @Override
    protected void reinitPartCollectionsThis() {
        testObjects.clear();
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof ITestObject) {
            testObjects.add(part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof ITestObject) {
            removeTestObject((ITestObject)part);
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (TestPolicyCmpt.TAG_NAME.equals(xmlTagName)) {
            return newTestPolicyCmptInternal(id);
        } else if (TestValue.TAG_NAME.equals(xmlTagName)) {
            return newTestValueInternal(id);
        } else if (TestRule.TAG_NAME.equals(xmlTagName)) {
            return newTestRuleInternal(id);
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TEST_CASE;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        testCaseTypeName = element.getAttribute(PROPERTY_TEST_CASE_TYPE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TEST_CASE_TYPE, testCaseTypeName);
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        Set<IpsObjectDependency> dependencies = new HashSet<>();
        // the test case depends on the test case type
        if (IpsStringUtils.isNotEmpty(testCaseTypeName)) {
            IpsObjectDependency dependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                    new QualifiedNameType(testCaseTypeName, IpsObjectType.TEST_CASE_TYPE));
            dependencies.add(dependency);
            addDetails(details, dependency, this, PROPERTY_TEST_CASE_TYPE);

        }
        // add dependency to product cmpts
        ITestPolicyCmpt[] testCmpts = getTestPolicyCmpts();
        for (ITestPolicyCmpt testCmpt : testCmpts) {
            addDependenciesForTestPolicyCmpt(dependencies, details, testCmpt);
        }
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    /**
     * Adds the dependencies to the given list for the given test policy cmpt and their childs
     */
    private void addDependenciesForTestPolicyCmpt(Set<IpsObjectDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details,
            ITestPolicyCmpt cmpt) {
        if (cmpt == null) {
            return;
        }
        if (cmpt.hasProductCmpt()) {
            IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                    new QualifiedNameType(cmpt.getProductCmpt(), IpsObjectType.PRODUCT_CMPT));
            dependencies.add(dependency);
            addDetails(details, dependency, cmpt, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
        }
        ITestPolicyCmptLink[] testLinks = cmpt.getTestPolicyCmptLinks();
        for (ITestPolicyCmptLink testLink : testLinks) {
            // get the dependencies for the childs of the given test policy cmpt
            if (testLink.isComposition()) {
                addDependenciesForTestPolicyCmpt(dependencies, details, testLink.findTarget());
            }
        }
    }

    @Override
    public ITestPolicyCmpt[] getAllTestPolicyCmpt() {
        List<ITestPolicyCmpt> allPolicyCmpts = new ArrayList<>();
        ITestPolicyCmpt[] testCmpts = getTestPolicyCmpts();
        for (ITestPolicyCmpt testCmpt : testCmpts) {
            addChildTestPolicyCmpt(allPolicyCmpts, testCmpt);
        }
        return allPolicyCmpts.toArray(new ITestPolicyCmpt[allPolicyCmpts.size()]);
    }

    @Override
    public ITestObject[] getAllTestObjects() {
        ITestPolicyCmpt[] testPolicyCmpts = getAllTestPolicyCmpt();
        ITestRule[] testRuleObjects = getTestRuleObjects();
        ITestValue[] testValues = getTestValues();

        ITestObject[] result = new ITestObject[testPolicyCmpts.length + testRuleObjects.length + testValues.length];
        System.arraycopy(testPolicyCmpts, 0, result, 0, testPolicyCmpts.length);
        System.arraycopy(testRuleObjects, 0, result, testPolicyCmpts.length, testRuleObjects.length);
        System.arraycopy(testValues, 0, result, (testRuleObjects.length + testPolicyCmpts.length), testValues.length);

        return result;
    }

    /**
     * Adds all test policy cmpts and its child test policy cmpts to the given list.
     */
    private void addChildTestPolicyCmpt(List<ITestPolicyCmpt> allPolicyCmpts, ITestPolicyCmpt cmpt) {
        allPolicyCmpts.add(cmpt);
        ITestPolicyCmptLink[] testLinks = cmpt.getTestPolicyCmptLinks();
        for (ITestPolicyCmptLink testLink : testLinks) {
            // get the dependencies for the childs of the given test policy cmpt
            if (testLink.isComposition()) {
                addChildTestPolicyCmpt(allPolicyCmpts, testLink.findTarget());
            }
        }
    }

    @Override
    public String[] getReferencedProductCmpts() {
        List<String> relatedProductCmpts = new ArrayList<>();
        ITestPolicyCmpt[] allTestPolicyCmpt = getAllTestPolicyCmpt();
        for (ITestPolicyCmpt element : allTestPolicyCmpt) {
            if (element.hasProductCmpt()) {
                relatedProductCmpts.add(element.getProductCmpt());
            }
        }
        return relatedProductCmpts.toArray(new String[relatedProductCmpts.size()]);
    }

    @Override
    public String getTestCaseType() {
        return testCaseTypeName;
    }

    @Override
    public void setTestCaseType(String testCaseType) {
        String oldTestCaseType = testCaseTypeName;
        testCaseTypeName = testCaseType;
        valueChanged(oldTestCaseType, testCaseType);
    }

    @Override
    public ITestCaseType findTestCaseType(IIpsProject ipsProject) {
        if (IpsStringUtils.isEmpty(testCaseTypeName) || ipsProject == null) {
            return null;
        }
        return (ITestCaseType)ipsProject.findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseTypeName);
    }

    @Override
    public ITestCaseTestCaseTypeDelta computeDeltaToModel(IIpsProject ipsProject) {
        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound != null) {
            return new TestCaseTestCaseTypeDelta(this, testCaseTypeFound);
        }
        // type not found, therefore no delta could be computed
        return null;
    }

    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) {
        ITestCaseTestCaseTypeDelta delta = computeDeltaToModel(ipsProject);
        if (delta != null && !delta.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * The test case uses an old implementation of fix differences. In this implementation the test
     * case handles the fix differences itself. To be compatible with the new implementation, this
     * method delegates to the {@link IFixDifferencesComposite#fixAllDifferencesToModel()} method
     * which delegates to the method {@link #fixDifferences(ITestCaseTestCaseTypeDelta)} of the old
     * implementation.
     * 
     * {@inheritDoc}
     */
    @Override
    public void fixAllDifferencesToModel(IIpsProject ipsProject) {
        computeDeltaToModel(ipsProject).fixAllDifferencesToModel();
    }

    @Override
    public void fixDifferences(ITestCaseTestCaseTypeDelta delta) {
        fixDifferencesTestCaseSide(delta);
        fixDifferencesTestCaseTypeSide(delta);
        fixDifferentOrder(delta);
    }

    private void fixDifferentOrder(ITestCaseTestCaseTypeDelta delta) {
        if (delta.isDifferentTestParameterOrder()) {
            // fix the order of the root test objects
            sortTestObjects();

            // fix child's
            // order links in order of the test parameter
            ITestPolicyCmpt[] cmpts = delta.getTestPolicyCmptWithDifferentSortOrder();
            for (ITestPolicyCmpt cmpt : cmpts) {
                ((TestPolicyCmpt)cmpt).fixDifferentChildSortOrder();
            }

            // order test attributes
            cmpts = delta.getTestPolicyCmptWithDifferentSortOrderTestAttr();
            for (ITestPolicyCmpt cmpt : cmpts) {
                ((TestPolicyCmpt)cmpt).fixDifferentTestAttrValueSortOrder();
            }

            objectHasChanged();
        }
    }

    private void fixDifferencesTestCaseTypeSide(ITestCaseTestCaseTypeDelta delta) {
        ITestValueParameter[] testValueParametersWithMissingTestValue = delta
                .getTestValueParametersWithMissingTestValue();
        ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParametersWithMissingTestPolicyCmpt = delta
                .getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt();
        ITestAttribute[] testAttributesWithMissingTestAttributeValue = delta
                .getTestAttributesWithMissingTestAttributeValue();

        // add missing test value parameters
        for (ITestValueParameter element : testValueParametersWithMissingTestValue) {
            ITestValue testValue = newTestValue();
            testValue.setTestValueParameter(element.getName());
            // set default value to default
            ValueDatatype valueDatatype = ((TestValueParameter)element).findValueDatatype(getIpsProject());
            if (valueDatatype != null) {
                testValue.setValue(valueDatatype.getDefaultValue());
            }
        }

        // add missing test policy cmpt type parameters
        for (ITestPolicyCmptTypeParameter element : testPolicyCmptTypeParametersWithMissingTestPolicyCmpt) {
            if (element.isRoot()) {
                addRootTestPolicyCmpt(element);
            } else {
                throw new RuntimeException("Merge of child test test policy cmpts is not supported!"); //$NON-NLS-1$
            }
        }

        // add missing test attributes
        for (ITestAttribute element : testAttributesWithMissingTestAttributeValue) {
            ITestPolicyCmpt[] testPolicyCmpts = delta.getTestPolicyCmptForMissingTestAttribute(element);
            for (ITestPolicyCmpt testPolicyCmpt : testPolicyCmpts) {
                ITestAttributeValue testAttributeValue = testPolicyCmpt.newTestAttributeValue();
                testAttributeValue.setTestAttribute(element.getName());
                // set default for the new added test attribute value only
                IProductCmptGeneration generation = ((TestPolicyCmpt)testPolicyCmpt)
                        .findProductCmpsCurrentGeneration(testPolicyCmpt.getIpsProject());
                ((TestAttributeValue)testAttributeValue).setDefaultTestAttributeValueInternal(generation);
            }
        }
    }

    /**
     * Adds the missing test object of the given testPolicyCmptTypeParameter
     */
    public ITestPolicyCmpt addRootTestPolicyCmpt(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter) {

        String name = testPolicyCmptTypeParameter.getName();
        ITestPolicyCmpt testPolicyCpmt = newTestPolicyCmpt();
        testPolicyCpmt.setTestPolicyCmptTypeParameter(name);
        // Standard naming if no product component is available
        testPolicyCpmt.setName(name);
        // add test attributes values
        ITestAttribute[] attrs = testPolicyCmptTypeParameter.getTestAttributes();
        for (ITestAttribute attr : attrs) {
            ITestAttributeValue testAttributeValue = testPolicyCpmt.newTestAttributeValue();
            testAttributeValue.setTestAttribute(attr.getName());
            // set default for the added test attribute value
            testAttributeValue.updateDefaultTestAttributeValue();
        }
        sortTestObjects();
        return testPolicyCpmt;
    }

    private void fixDifferencesTestCaseSide(ITestCaseTestCaseTypeDelta delta) {
        ITestValue[] testValuesWithMissingTestValueParam = delta.getTestValuesWithMissingTestValueParam();
        ITestPolicyCmpt[] testPolicyCmptsWithMissingTypeParam = delta.getTestPolicyCmptsWithMissingTypeParam();
        ITestPolicyCmptLink[] testPolicyCmptLinksWithMissingTypeParam = delta
                .getTestPolicyCmptLinkWithMissingTypeParam();
        ITestAttributeValue[] testAttributeValuesWithMissingTestAttribute = delta
                .getTestAttributeValuesWithMissingTestAttribute();
        ITestRule[] testRulesWithMissingTestRuleParam = delta.getTestRulesWithMissingTestValueParam();

        // delete test values
        for (ITestValue element : testValuesWithMissingTestValueParam) {
            element.delete();
        }
        // delta test rules
        for (ITestRule element : testRulesWithMissingTestRuleParam) {
            element.delete();
        }
        // delete root and child test policy cmpts
        for (ITestPolicyCmpt element : testPolicyCmptsWithMissingTypeParam) {
            element.delete();
        }
        // delete test policy cmpt links
        for (ITestPolicyCmptLink element : testPolicyCmptLinksWithMissingTypeParam) {
            element.delete();
        }
        // delete test attribute values
        for (ITestAttributeValue element : testAttributeValuesWithMissingTestAttribute) {
            element.delete();
        }
    }

    @Override
    public void sortTestObjects() {
        List<IIpsObjectPart> orderedTestObject = getCorrectSortOrderOfRootObjects(getIpsProject());
        if (orderedTestObject != null) {
            testObjects = orderedTestObject;
            objectHasChanged();
        }
    }

    /**
     * Returns all root objects in the correct sort order compared to the test case type parameters.
     * If the test parameter doesn't exist order the test object to the end of the test object list.
     */
    private List<IIpsObjectPart> getCorrectSortOrderOfRootObjects(IIpsProject ipsProject) {
        List<IIpsObjectPart> newTestObjectOrder = new ArrayList<>(testObjects.size());
        HashMap<ITestParameter, List<ITestObject>> oldTestObject = new HashMap<>(
                testObjects.size());
        for (IIpsObjectPart iIpsObjectPart : testObjects) {
            ITestObject testObject = (ITestObject)iIpsObjectPart;
            String testParameterName = ""; //$NON-NLS-1$
            ITestParameter testParameter = null;
            if (testObject instanceof ITestPolicyCmpt) {
                testParameterName = ((ITestPolicyCmpt)testObject).getTestPolicyCmptTypeParameter();
                testParameter = ((ITestPolicyCmpt)testObject).findTestPolicyCmptTypeParameter(ipsProject);
            } else if (testObject instanceof ITestValue) {
                testParameterName = ((ITestValue)testObject).getTestValueParameter();
                testParameter = ((ITestValue)testObject).findTestValueParameter(ipsProject);
            } else if (testObject instanceof ITestRule) {
                testParameterName = ((ITestRule)testObject).getTestRuleParameter();
                testParameter = ((ITestRule)testObject).findTestRuleParameter(ipsProject);
            } else {
                throw new RuntimeException("Unsupported test object type: " + testObject.getClass()); //$NON-NLS-1$
            }
            if (testParameter == null) {
                throw new IpsException(
                        new IpsStatus(MessageFormat.format(Messages.TestCase_Error_TestParameterNotFound,
                                testParameterName)));
            }

            List<ITestObject> oldObjectsToTestParam = oldTestObject.computeIfAbsent(testParameter,
                    $ -> new ArrayList<>(1));
            oldObjectsToTestParam.add(testObject);
        }

        ITestCaseType testCaseType = findTestCaseType(ipsProject);
        ITestParameter[] testParameters = testCaseType.getTestParameters();
        for (ITestParameter testParameter : testParameters) {
            List<ITestObject> oldObjectsToTestParam = oldTestObject.get(testParameter);
            // add all elements without a test parameter to the end
            if (oldObjectsToTestParam != null) {
                newTestObjectOrder.addAll(oldObjectsToTestParam);
            }
        }
        return newTestObjectOrder;
    }

    @Override
    public ITestValue newTestValue() {
        ITestValue v = newTestValueInternal(getNextPartId());
        objectHasChanged();
        return v;
    }

    @Override
    public ITestRule newTestRule() {
        ITestRule v = newTestRuleInternal(getNextPartId());
        objectHasChanged();
        return v;
    }

    @Override
    public ITestPolicyCmpt newTestPolicyCmpt() {
        ITestPolicyCmpt p = newTestPolicyCmptInternal(getNextPartId());
        objectHasChanged();
        return p;
    }

    //
    // Getters for test objects
    //

    @Override
    public ITestObject[] getTestObjects() {
        List<TestObject> foundTestObjects = getTestObjects(null, null, null);
        if (foundTestObjects.size() == 0) {
            return new ITestObject[0];
        }

        return foundTestObjects.toArray(new ITestObject[0]);
    }

    @Override
    public ITestPolicyCmpt[] getTestPolicyCmpts() {
        return getTestObjects(null, TestPolicyCmpt.class, null).toArray(new ITestPolicyCmpt[0]);
    }

    @Override
    public ITestValue[] getTestValues() {
        return getTestObjects(null, TestValue.class, null).toArray(new ITestValue[0]);
    }

    @Override
    public ITestRule[] getTestRule(String testRuleParameter) {
        List<TestObject> testRules = getTestObjects(null, TestRule.class, null);
        List<ITestRule> result = new ArrayList<>();
        for (TestObject testObject : testRules) {
            ITestRule element = (ITestRule)testObject;
            if (element.getTestParameterName().equals(testRuleParameter)) {
                result.add(element);
            }
        }
        return result.toArray(new ITestRule[0]);
    }

    @Override
    public ITestRule[] getTestRuleObjects() {
        return getTestObjects(null, TestRule.class, null).toArray(new ITestRule[0]);
    }

    //
    // Getters for input objects
    //

    @Override
    public ITestObject[] getInputTestObjects() {
        return getTestObjects(TestParameterType.INPUT, null, null).toArray(new ITestObject[0]);
    }

    @Override
    public ITestValue[] getInputTestValues() {
        return getTestObjects(TestParameterType.INPUT, TestValue.class, null).toArray(new ITestValue[0]);
    }

    @Override
    public ITestPolicyCmpt[] getInputTestPolicyCmpts() {
        return getTestObjects(TestParameterType.INPUT, TestPolicyCmpt.class, null).toArray(new ITestPolicyCmpt[0]);
    }

    //
    // Getters for expected result objects
    //

    @Override
    public ITestObject[] getExpectedResultTestObjects() {
        return getTestObjects(TestParameterType.EXPECTED_RESULT, null, null).toArray(new ITestObject[0]);
    }

    @Override
    public ITestValue[] getExpectedResultTestValues() {
        return getTestObjects(TestParameterType.EXPECTED_RESULT, TestValue.class, null).toArray(new ITestValue[0]);
    }

    @Override
    public ITestRule[] getExpectedResultTestRules() {
        return getTestObjects(TestParameterType.EXPECTED_RESULT, TestRule.class, null).toArray(new ITestRule[0]);
    }

    @Override
    public ITestPolicyCmpt[] getExpectedResultTestPolicyCmpts() {
        return getTestObjects(TestParameterType.EXPECTED_RESULT, TestPolicyCmpt.class, null)
                .toArray(new ITestPolicyCmpt[0]);
    }

    @Override
    public void removeTestObject(ITestObject testObject) {
        if (testObject.isRoot()) {
            testObjects.remove(testObject);
        } else {
            remove(testObject);
        }
        objectHasChanged();
    }

    //
    // Finder methods to search inside the complete test case structure.
    //

    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmpt,
            IIpsProject ipsProject) {

        return findTestPolicyCmptTypeParameter(testPolicyCmpt, null, ipsProject);
    }

    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmptLink link,
            IIpsProject ipsProject) {
        return findTestPolicyCmptTypeParameter(null, link, ipsProject);
    }

    /**
     * Returns the corresponing test policy componnet type parameter of the given test policy
     * component or the given link. Either the test policy component or the link must be given, but
     * not both together. Returns <code>null</code> if the parameter not found.
     * 
     * @param testPolicyCmptBase The test policy component which policy component type parameter
     *            will be returned.
     * @param link The test policy component link which test link will be returned
     * 
     * @throws IpsException if an error occurs while searching for the object.
     */
    private ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmptBase,
            ITestPolicyCmptLink link,
            IIpsProject ipsProject) {

        ArgumentCheck.isTrue(testPolicyCmptBase != null || link != null);
        ArgumentCheck.isTrue(!(testPolicyCmptBase != null && link != null));

        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound == null) {
            return null;
        }

        // Create a helper path obejct to search the given string path
        TestCaseHierarchyPath hierarchyPath = null;
        if (testPolicyCmptBase != null) {
            hierarchyPath = new TestCaseHierarchyPath(testPolicyCmptBase, false);
        } else if (link != null) {
            hierarchyPath = new TestCaseHierarchyPath(link, false);
        } else {
            throw new IpsException(new IpsStatus(Messages.TestCase_Error_NoLinkOrPolicyCmptGiven));
        }

        // find the root test policy component parameter type
        String testPolicyCmptTypeName = hierarchyPath.next();
        ITestParameter testParam = testCaseTypeFound.getTestParameterByName(testPolicyCmptTypeName);
        if (testParam == null) {
            return null;
        }

        // check the correct instance of the found object
        if (!(testParam instanceof ITestPolicyCmptTypeParameter)) {
            throw new IpsException(
                    new IpsStatus(MessageFormat.format(Messages.TestCase_Error_WrongInstanceParam,
                            testPolicyCmptTypeName, testParam.getClass().getName())));
        }
        if (!testPolicyCmptTypeName.equals(testParam.getName())) {
            // incosistence between test case and test case type
            return null;
        }

        // now search the given path until the test policy component is found
        ITestPolicyCmptTypeParameter policyCmptTypeParam = (ITestPolicyCmptTypeParameter)testParam;
        while (hierarchyPath.hasNext()) {
            testPolicyCmptTypeName = hierarchyPath.next();
            policyCmptTypeParam = policyCmptTypeParam.getTestPolicyCmptTypeParamChild(testPolicyCmptTypeName);
            if (policyCmptTypeParam == null || !testPolicyCmptTypeName.equals(policyCmptTypeParam.getName())) {
                // incosistence between test case and test case type
                return null;
            }
        }

        return policyCmptTypeParam;
    }

    /**
     * Removes the given test parameter object from the parameter list
     */
    private void remove(ITestObject testObject) {
        if (!(testObject instanceof ITestPolicyCmpt testPolicyCmpt) || testPolicyCmpt.isRoot()) {
            removeTestObject(testObject);
        } else {
            TestCaseHierarchyPath hierarchyPath = new TestCaseHierarchyPath(testPolicyCmpt);
            testPolicyCmpt = findTestPolicyCmpt(hierarchyPath.toString());
            if (testPolicyCmpt == null) {
                throw new IpsException(new IpsStatus(
                        MessageFormat.format(Messages.TestCase_Error_TestPolicyCmptNotFound,
                                hierarchyPath.toString())));
            }

            ITestPolicyCmptLink link = (ITestPolicyCmptLink)testPolicyCmpt.getParent();
            if (link != null) {
                ((ITestPolicyCmpt)link.getParent()).removeLink(link);
            }
        }
    }

    @Override
    public ITestPolicyCmpt findTestPolicyCmpt(String testPolicyCmptPath) {
        TestCaseHierarchyPath path = new TestCaseHierarchyPath(testPolicyCmptPath);
        ITestPolicyCmpt pc = null;
        String currElem = path.next();

        List<TestObject> testPoliyCmpts = getTestObjects(null, TestPolicyCmpt.class, currElem);
        if (testPoliyCmpts.size() == 1) {
            assertInstanceOfTestPolicyCmpt(currElem, testPoliyCmpts.get(0));
            pc = searchChildTestPolicyCmpt((ITestPolicyCmpt)testPoliyCmpts.get(0), path);
        } else if (testPoliyCmpts.size() == 0) {
            return null;
        } else {
            throw new IpsException(
                    new IpsStatus(MessageFormat.format(Messages.TestCase_Error_MoreThanOneObject, currElem)));
        }
        return pc;
    }

    /**
     * Assert the correct instance of ITestPolicyCmpt for the given testObject.
     */
    private void assertInstanceOfTestPolicyCmpt(String currElem, ITestObject testObject) {
        if (!(testObject instanceof ITestPolicyCmpt)) {
            throw new IpsException(
                    new IpsStatus(MessageFormat.format(Messages.TestCase_Error_WrongInstanceTestPolicyCmpt,
                            currElem, testObject.getClass().getName())));
        }
    }

    /**
     * Search the test policy component by the given path.
     */
    private ITestPolicyCmpt searchChildTestPolicyCmpt(ITestPolicyCmpt pc, TestCaseHierarchyPath path) {
        String searchedPath = path.toString();
        while (pc != null && path.hasNext()) {
            boolean found = false;
            String currElem = path.next();

            ITestPolicyCmptLink[] prs;
            prs = pc.getTestPolicyCmptLinks(currElem);

            currElem = path.next();
            pc = null;
            for (ITestPolicyCmptLink link : prs) {
                ITestPolicyCmpt pcTarget = link.findTarget();
                if (pcTarget == null) {
                    return null;
                }

                if (currElem.equals(pcTarget.getName())) {
                    if (found) {
                        // exception more than one element found with the given path
                        throw new IpsException(
                                new IpsStatus(
                                        MessageFormat.format(Messages.TestCase_Error_MoreThanOneObject, searchedPath)));
                    }
                    found = true;
                    pc = pcTarget;
                }
            }
        }
        return pc;
    }

    @Override
    public String generateUniqueNameForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String name) {
        String uniqueLabel = name;

        // eval the unique idx of new component
        int idx = 1;
        String newUniqueLabel = uniqueLabel;
        if (newTestPolicyCmpt.isRoot()) {
            ITestPolicyCmpt[] testPolicyCmpts = getTestPolicyCmpts();
            for (int i = 0; i < testPolicyCmpts.length; i++) {
                ITestPolicyCmpt cmpt = testPolicyCmpts[i];
                if (newUniqueLabel.equals(cmpt.getName()) && !cmpt.equals(newTestPolicyCmpt)) {
                    idx++;
                    newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    i = -1;
                }
            }
        } else {
            ITestPolicyCmpt parent = newTestPolicyCmpt.getParentTestPolicyCmpt();
            ITestPolicyCmptLink[] links = parent.getTestPolicyCmptLinks();
            ArrayList<String> names = new ArrayList<>();
            for (ITestPolicyCmptLink link : links) {
                if (link.isComposition()) {
                    ITestPolicyCmpt child = link.findTarget();
                    if (!child.equals(newTestPolicyCmpt)) {
                        names.add(child.getName());
                    }
                }
            }
            while (names.contains(newUniqueLabel)) {
                idx++;
                newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return newUniqueLabel;
    }

    @Override
    public IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) {
        Set<IValidationRule> result = new HashSet<>();
        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound != null) {
            result.addAll(Arrays.asList(testCaseTypeFound.getTestRuleCandidates(ipsProject)));
            result.addAll(getTestCaseTestRuleCandidates(ipsProject));
        }
        return result.toArray(new IValidationRule[result.size()]);
    }

    @Override
    public IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) {
        IValidationRule[] validationRules = getTestRuleCandidates(ipsProject);
        for (IValidationRule validationRule : validationRules) {
            if (validationRule.getName().equals(validationRuleName)) {
                return validationRule;
            }
        }
        return null;
    }

    /**
     * Returns all validation rules of the policy cmpt types of the product cmpt inside this test
     * case.
     */
    private Collection<IValidationRule> getTestCaseTestRuleCandidates(IIpsProject ipsProject) {
        List<IValidationRule> result = new ArrayList<>();
        getValidationRules(getTestPolicyCmpts(), result, ipsProject);
        return result;
    }

    /**
     * Adds all validation rules - of policy cmpts related by the given test policy cmpts - to the
     * given list
     */
    private void getValidationRules(ITestPolicyCmpt[] testPolicyCmpts,
            List<IValidationRule> validationRules,
            IIpsProject ipsProject) {
        for (ITestPolicyCmpt testPolicyCmpt : testPolicyCmpts) {
            getValidationRules(testPolicyCmpt, validationRules, ipsProject);
        }
    }

    /**
     * Add all validaton rules of the corresponding policy cmpt and childs
     */
    private void getValidationRules(ITestPolicyCmpt testPolicyCmpt,
            List<IValidationRule> validationRules,
            IIpsProject ipsProject) {

        // add rules of childs, ignore if the corresponding objects are not found (validation
        // errors)
        ITestPolicyCmptLink[] rs = testPolicyCmpt.getTestPolicyCmptLinks();
        for (ITestPolicyCmptLink element : rs) {
            ITestPolicyCmpt tpc = element.findTarget();
            if (tpc == null) {
                continue;
            }
            getValidationRules(tpc, validationRules, ipsProject);
        }
        ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
        if (typeParam == null) {
            return;
        }
        IPolicyCmptType pct = typeParam.findPolicyCmptType(ipsProject);
        if (pct == null) {
            return;
        }
        validationRules.addAll(pct.getValidationRules());
        IProductCmpt pc = testPolicyCmpt.findProductCmpt(ipsProject);
        if (pc == null) {
            return;
        }
        IPolicyCmptType pctOfPc = pc.findPolicyCmptType(ipsProject);
        if (pctOfPc == null) {
            return;
        }
        if (!pctOfPc.equals(pct)) {
            // add all rules inside the supertype hierarchy
            ITypeHierarchy supertypeHierarchy = pctOfPc.getSupertypeHierarchy();
            validationRules.addAll(supertypeHierarchy.getAllRules(pctOfPc));
        }
    }

    /**
     * Creates a new test policy component without updating the src file.
     */
    private ITestPolicyCmpt newTestPolicyCmptInternal(String id) {
        ITestPolicyCmpt p = new TestPolicyCmpt(this, id);
        testObjects.add(p);
        return p;
    }

    /**
     * Creates a new test value without updating the src file.
     */
    private ITestValue newTestValueInternal(String id) {
        ITestValue v = new TestValue(this, id);
        testObjects.add(v);
        return v;
    }

    /**
     * Creates a new test rule without updating the src file.
     */
    private ITestRule newTestRuleInternal(String id) {
        ITestRule v = new TestRule(this, id);
        testObjects.add(v);
        return v;
    }

    /**
     * Returns the test objects which matches the given type, is instance of the given class and
     * matches the given name. The particular object aspect will only check if the particular field
     * is not <code>null</code>. For instance if all parameter are <code>null</code> then all
     * parameters are returned.
     */
    private List<TestObject> getTestObjects(TestParameterType type, Class<?> parameterClass, String name) {
        List<TestObject> result = new ArrayList<>(testObjects.size());
        for (IIpsObjectPart iIpsObjectPart : testObjects) {
            TestObject testObject = (TestObject)iIpsObjectPart;
            boolean addParameter = true;

            if ((type != null && !isTypeOrDefault(testObject.getTestParameterName(), type, TestObject.DEFAULT_TYPE))
                    || (parameterClass != null && !testObject.getClass().equals(parameterClass))) {
                addParameter = false;
                continue;
            }
            if (name != null && !name.equals(testObject.getName())) {
                addParameter = false;
                continue;
            }
            if (addParameter) {
                result.add(testObject);
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the given type is the type of the corresponding test parameter.
     * If the test parameter couldn't determined return <code>true</code> if the given type is the
     * default type otherwise <code>false</code>.<br>
     * Return <code>false</code> if an error occurs.<br>
     * (Packageprivate helper method.)
     */
    boolean isTypeOrDefault(String testParameterName, TestParameterType type, TestParameterType defaultType) {

        try {
            ITestCaseType testCaseTypeFound = findTestCaseType(getIpsProject());
            if (testCaseTypeFound == null) {
                return type.equals(defaultType);
            }

            ITestParameter testParameter = testCaseTypeFound.getTestParameterByName(testParameterName);
            if (testParameter == null) {
                return type.equals(defaultType);
            }
            return isTypeOrDefault(testParameter, type);
        } catch (IpsException e) {
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exceptions
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given type is the type of the corresponding test
     * parameter.<br>
     * Return <code>false</code> if an error occurs.<br>
     * (Packageprivate helper method.)
     */
    boolean isTypeOrDefault(ITestParameter testParameter, TestParameterType type) {
        // TODO Joerg: aufraeumen, Verwendung von TestParameterType.isTypeMatching
        try {
            // compare the parameters type and return if the type matches the given type
            if ((testParameter.isInputOrCombinedParameter() && type.equals(TestParameterType.INPUT))
                    || (testParameter.isExpextedResultOrCombinedParameter()
                            && type.equals(TestParameterType.EXPECTED_RESULT))) {
                return true;
            }
            if (testParameter.isCombinedParameter() && type.equals(TestParameterType.COMBINED)) {
                return true;
            }
        } catch (Exception e) {
            // TODO ignored exception needs to be documented properly (why is it OK to ignore?)
            // ignore exceptions
        }
        return false;
    }

    @Override
    protected void validateThis(MessageList messageList, IIpsProject ipsProject) {
        super.validateThis(messageList, ipsProject);
        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound == null) {
            String text = MessageFormat.format(Messages.TestCase_ValidateError_TestCaseTypeNotFound, testCaseTypeName);
            Message msg = new Message(MSGCODE_TEST_CASE_TYPE_NOT_FOUND, text, Message.ERROR, this,
                    ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
            messageList.add(msg);
        }
    }

    @Override
    public void clearTestValues(TestParameterType testParameterType) {
        if (TestParameterType.isTypeMatching(TestParameterType.INPUT, testParameterType)) {
            clearAllInputTestValues();
        }
        if (TestParameterType.isTypeMatching(TestParameterType.EXPECTED_RESULT, testParameterType)) {
            clearAllExpectedTestValues();
        }
    }

    private void clearAllInputTestValues() {
        clearTestValues(getInputTestValues());
        clearTestAttributeValues(true);
    }

    private void clearAllExpectedTestValues() {
        clearTestValues(getExpectedResultTestValues());
        clearTestAttributeValues(false);
    }

    private void clearTestValues(ITestValue[] testValues) {
        for (ITestValue testValue : testValues) {
            testValue.setDefaultValue();
        }
    }

    private void clearTestAttributeValues(boolean input) {
        ITestPolicyCmpt[] testPolicyCmpt = getAllTestPolicyCmpt();
        for (ITestPolicyCmpt element : testPolicyCmpt) {
            ITestAttributeValue[] testAttributeValues = element.getTestAttributeValues();
            for (ITestAttributeValue testAttributeValue : testAttributeValues) {
                if ((input && testAttributeValue.isInputAttribute(getIpsProject()))
                        || (!input && testAttributeValue.isExpectedResultAttribute(getIpsProject()))) {
                    testAttributeValue.setDefaultValue();
                }
            }
        }
    }

    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) {
        return ipsProject.findIpsSrcFile(IpsObjectType.TEST_CASE_TYPE, getTestCaseType());
    }

    @Override
    public String getMetaClass() {
        return getTestCaseType();
    }

}
