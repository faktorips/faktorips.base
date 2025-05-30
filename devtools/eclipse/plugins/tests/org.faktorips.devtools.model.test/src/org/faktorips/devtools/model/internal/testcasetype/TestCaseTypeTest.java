/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcasetype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.model.util.CollectionUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Joerg Ortmann
 */
public class TestCaseTypeTest extends AbstractDependencyTest {

    private IIpsProject ipsProject;
    private ITestCaseType type;
    private IIpsPackageFragmentRoot root;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        type = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculationTest");
    }

    @Test
    public void testNewParameter() {
        ITestValueParameter param = type.newInputTestValueParameter();
        assertNotNull(param);
        assertEquals(1, type.getInputTestParameters().length);
        assertEquals(0, type.getExpectedResultTestParameters().length);
        assertTrue(param.isInputOrCombinedParameter());
        assertFalse(param.isExpextedResultOrCombinedParameter());
        assertFalse(param.isCombinedParameter());

        ITestValueParameter param2 = type.newExpectedResultValueParameter();
        assertNotNull(param2);
        assertEquals(1, type.getExpectedResultTestParameters().length);
        assertEquals(1, type.getInputTestParameters().length);
        assertFalse(param2.isInputOrCombinedParameter());
        assertTrue(param2.isExpextedResultOrCombinedParameter());
        assertFalse(param2.isCombinedParameter());

        ITestPolicyCmptTypeParameter param3 = type.newInputTestPolicyCmptTypeParameter();
        assertNotNull(param3);
        assertEquals(2, type.getInputTestParameters().length);
        assertEquals(1, type.getExpectedResultTestParameters().length);
        assertTrue(param3.isInputOrCombinedParameter());
        assertFalse(param3.isExpextedResultOrCombinedParameter());
        assertFalse(param3.isCombinedParameter());

        ITestPolicyCmptTypeParameter param4 = type.newExpectedResultPolicyCmptTypeParameter();
        assertNotNull(param4);
        assertEquals(2, type.getInputTestParameters().length);
        assertEquals(2, type.getExpectedResultTestParameters().length);
        assertFalse(param4.isInputOrCombinedParameter());
        assertTrue(param4.isExpextedResultOrCombinedParameter());
        assertFalse(param4.isCombinedParameter());

        ITestPolicyCmptTypeParameter param5 = type.newCombinedPolicyCmptTypeParameter();
        assertNotNull(param5);
        assertEquals(3, type.getInputTestParameters().length);
        assertEquals(3, type.getExpectedResultTestParameters().length);
        assertTrue(param5.isInputOrCombinedParameter());
        assertTrue(param5.isExpextedResultOrCombinedParameter());
        assertTrue(param5.isCombinedParameter());

        ITestRuleParameter param6 = type.newExpectedResultRuleParameter();
        assertNotNull(param6);
        assertEquals(3, type.getInputTestParameters().length);
        assertEquals(4, type.getExpectedResultTestParameters().length);
        assertFalse(param6.isInputOrCombinedParameter());
        assertTrue(param6.isExpextedResultOrCombinedParameter());
        assertFalse(param6.isCombinedParameter());

        // assert the correct storing of elements
        assertEquals(param, type.getInputTestParameters()[0]);
        assertEquals(param3, type.getInputTestParameters()[1]);
        assertEquals(param2, type.getExpectedResultTestParameters()[0]);
        assertEquals(param4, type.getExpectedResultTestParameters()[1]);
        assertEquals(param6, type.getExpectedResultTestParameters()[3]);
        // the combined element will be returned in the input and exp result list
        assertEquals(param5, type.getExpectedResultTestParameters()[2]);
        assertEquals(param5, type.getInputTestParameters()[2]);
    }

    @Test
    public void testGetParameters() {
        assertEquals(0, type.getInputTestParameters().length);
        ITestValueParameter param = type.newInputTestValueParameter();
        ITestValueParameter param2 = type.newInputTestValueParameter();
        ITestPolicyCmptTypeParameter param3 = type.newInputTestPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter param4 = type.newInputTestPolicyCmptTypeParameter();
        assertEquals(4, type.getInputTestParameters().length);
        assertEquals(param, type.getInputTestParameters()[0]);
        assertEquals(param2, type.getInputTestParameters()[1]);
        assertEquals(param3, type.getInputTestParameters()[2]);
        assertEquals(param4, type.getInputTestParameters()[3]);

        assertEquals(0, type.getExpectedResultTestParameters().length);
        ITestValueParameter param5 = type.newExpectedResultValueParameter();
        ITestValueParameter param6 = type.newExpectedResultValueParameter();
        ITestPolicyCmptTypeParameter param7 = type.newExpectedResultPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter param8 = type.newExpectedResultPolicyCmptTypeParameter();
        ITestRuleParameter param9 = type.newExpectedResultRuleParameter();
        assertEquals(5, type.getExpectedResultTestParameters().length);
        assertEquals(param5, type.getExpectedResultTestParameters()[0]);
        assertEquals(param6, type.getExpectedResultTestParameters()[1]);
        assertEquals(param7, type.getExpectedResultTestParameters()[2]);
        assertEquals(param8, type.getExpectedResultTestParameters()[3]);
        assertEquals(param9, type.getExpectedResultTestParameters()[4]);
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element typeEl = XmlUtil.getFirstElement(docEl);
        type.initFromXml(typeEl);
        assertEquals(4, type.getInputTestParameters().length);
        assertEquals(5, type.getExpectedResultTestParameters().length);
    }

    @Test
    public void testToXml() {
        ITestValueParameter valueParamInput = type.newInputTestValueParameter();
        ITestValueParameter valueParamInput2 = type.newInputTestValueParameter();
        ITestValueParameter valueParamExpectedResult = type.newExpectedResultValueParameter();
        ITestRuleParameter ruleParamExpectedResult = type.newExpectedResultRuleParameter();
        ruleParamExpectedResult.setName("Rule1");
        type.newInputTestPolicyCmptTypeParameter().setName("in1");
        type.newExpectedResultPolicyCmptTypeParameter().setName("ex1");

        valueParamInput.setName("val1");
        valueParamInput.setValueDatatype("Integer");
        valueParamInput.setName("val2");
        valueParamInput2.setValueDatatype("Decimal");
        valueParamExpectedResult.setName("valEx");
        valueParamExpectedResult.setValueDatatype("Money");
        Document doc = newDocument();
        Element el = type.toXml(doc);

        // overwrite parameter
        valueParamInput.setValueDatatype("Test");
        valueParamExpectedResult.setValueDatatype("Test2");

        // read the xml which was written before
        type.initFromXml(el);
        assertEquals(3, type.getInputTestParameters().length);
        assertEquals(3, type.getExpectedResultTestParameters().length);
        assertEquals("Integer", ((ITestValueParameter)type.getInputTestParameters()[0]).getValueDatatype());
        assertEquals("Decimal", ((ITestValueParameter)type.getInputTestParameters()[1]).getValueDatatype());
        assertEquals("Money", ((ITestValueParameter)type.getExpectedResultTestParameters()[0]).getValueDatatype());
        assertEquals("Rule1", type.getExpectedResultTestParameters()[1].getName());
    }

    @Test
    public void testDependsOn() throws Exception {
        List<IDependency> dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(0, dependsOnList.size());

        IPolicyCmptType pcType1 = newPolicyCmptType(root, "PolicyCmptType1");
        IPolicyCmptType pcType2 = newPolicyCmptType(root, "PolicyCmptType2");
        IPolicyCmptType pcType3 = newPolicyCmptType(root, "PolicyCmptType3");

        // test dependency to policy cmpt type of root
        ITestPolicyCmptTypeParameter param1 = type.newInputTestPolicyCmptTypeParameter();
        param1.setPolicyCmptType(pcType1.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(1, dependsOnList.size());
        IDependency dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType1.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(type, dependency, param1, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);

        // test dependency to policy cmpt type of child
        ITestPolicyCmptTypeParameter param2 = param1.newTestPolicyCmptTypeParamChild();
        param2.setPolicyCmptType(pcType2.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(2, dependsOnList.size());
        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType1.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(type, dependency, param1, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);

        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType2.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(type, dependency, param2, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);

        // test duplicate dependency
        ITestPolicyCmptTypeParameter param3 = param1.newTestPolicyCmptTypeParamChild();
        param3.setPolicyCmptType(pcType1.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(2, dependsOnList.size());
        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType1.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));

        List<IDependencyDetail> details = type.getDependencyDetails(dependency);
        DependencyDetail detail1 = new DependencyDetail(param1, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        DependencyDetail detail2 = new DependencyDetail(param3, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        assertEquals(2, details.size());
        assertTrue(details.contains(detail1));
        assertTrue(details.contains(detail2));

        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType2.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));

        // test dependency to policy cmpt type child of child
        ITestPolicyCmptTypeParameter param4 = param3.newTestPolicyCmptTypeParamChild();
        param4.setPolicyCmptType(pcType3.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(3, dependsOnList.size());
        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType1.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertDependencyDetailContained(type, dependency, param1, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);

        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType2.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertDependencyDetailContained(type, dependency, param2, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);

        dependency = IpsObjectDependency.createReferenceDependency(type.getQualifiedNameType(),
                pcType3.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertDependencyDetailContained(type, dependency, param4, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
    }

    @Test
    public void testGetTestRuleCandidates() {
        IValidationRule[] testRuleParameters = type.getTestRuleCandidates(ipsProject);
        assertEquals(0, testRuleParameters.length);

        // create policy cmpts with validation rules
        IPolicyCmptType policyCmptTypeBase = newPolicyCmptType(root, "PolicyCmptBase");
        IPolicyCmptType policyCmptType1 = newPolicyCmptType(root, "PolicyCmpt1");
        policyCmptType1.setSupertype(policyCmptTypeBase.getQualifiedName());
        IPolicyCmptType policyCmptType2 = newPolicyCmptType(root, "PolicyCmpt2");
        IValidationRule ruleBase = policyCmptTypeBase.newRule();
        ruleBase.setName("RuleBase");
        ruleBase.setMessageCode("RuleBase");
        IValidationRule rule1 = policyCmptType1.newRule();
        rule1.setName("Rule1");
        rule1.setMessageCode("Rule1");
        IValidationRule rule2 = policyCmptType2.newRule();
        rule2.setName("Rule2");
        rule2.setMessageCode("Rule2");

        type.newInputTestPolicyCmptTypeParameter(); // dummy with no rules
        ITestPolicyCmptTypeParameter param1 = type.newInputTestPolicyCmptTypeParameter();
        param1.setPolicyCmptType(policyCmptType1.getQualifiedName());
        param1.setName("PolicyCmpt1");
        ITestPolicyCmptTypeParameter childParam1 = param1.newTestPolicyCmptTypeParamChild();
        childParam1.setPolicyCmptType(policyCmptType2.getQualifiedName());
        childParam1.setAssociation("Association1");
        childParam1.setName("ChildPolicyCmpt2");
        testRuleParameters = type.getTestRuleCandidates(ipsProject);
        assertEquals(3, testRuleParameters.length);
        List<IValidationRule> testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(rule1));
        assertTrue(testRuleParametersList.contains(rule2));
        assertTrue(testRuleParametersList.contains(ruleBase));
    }

    @Test
    public void testGetAllTestParameter() {
        ITestPolicyCmptTypeParameter testParameter1 = type.newInputTestPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter testParameter1_1 = testParameter1.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter testParameter1_2 = testParameter1.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter testParameter1_1_1 = testParameter1_1.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter testParameter2 = type.newExpectedResultPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter testParameter3 = type.newCombinedPolicyCmptTypeParameter();
        ITestRuleParameter ruleParameter = type.newExpectedResultRuleParameter();
        ITestValueParameter valueParameter = type.newInputTestValueParameter();

        ITestParameter[] params = ((TestCaseType)type).getAllTestParameter();
        List<ITestParameter> paramsList = Arrays.asList(params);
        assertEquals("" + 8, "" + params.length);
        assertTrue(paramsList.contains(testParameter1));
        assertTrue(paramsList.contains(testParameter1_1));
        assertTrue(paramsList.contains(testParameter1_2));
        assertTrue(paramsList.contains(testParameter1_1_1));
        assertTrue(paramsList.contains(testParameter2));
        assertTrue(paramsList.contains(testParameter3));
        assertTrue(paramsList.contains(ruleParameter));
        assertTrue(paramsList.contains(valueParameter));
    }

    @Test
    public void testFindAllMetaObjects() {
        String testCaseTypeQName = "pack.MyTestCaseType";
        String testCaseTypeProj2QName = "otherpack.MyTestCaseTypeProj2";
        String testCase1QName = "pack.MyTableContent1";
        String testCase2QName = "pack.MyTableContent2";
        String testCase3QName = "pack.MyTableContent3";
        String testCaseProj2QName = "otherpack.MyTableContentProj2";

        IIpsProject referencingProject = newIpsProject("referencingProject");
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        referencingProject.setIpsObjectPath(path);

        IIpsProject independentProject = newIpsProject("independentProject");

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all ipsProject that holds a reference to the
         * ipsProject of the object. So the search for a Object in e.g. ipsProject have to search
         * for instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates
         * are found.
         */

        IIpsProject leaveProject1 = newIpsProject("LeaveProject1");
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        IIpsProject leaveProject2 = newIpsProject("LeaveProject2");
        path = leaveProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject2.setIpsObjectPath(path);

        TestCaseType testCaseType = newTestCaseType(ipsProject, testCaseTypeQName);
        TestCase testCase1 = newTestCase(testCaseType, testCase1QName);
        TestCase testCase2 = newTestCase(testCaseType, testCase2QName);
        TestCase testCase3 = newTestCase(ipsProject, testCase3QName);

        Collection<IIpsSrcFile> resultList = testCaseType.searchMetaObjectSrcFiles(true);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(testCase1.getIpsSrcFile()));
        assertTrue(resultList.contains(testCase2.getIpsSrcFile()));
        assertFalse(resultList.contains(testCase3.getIpsSrcFile()));

        resultList = testCaseType.searchMetaObjectSrcFiles(false);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(testCase1.getIpsSrcFile()));
        assertTrue(resultList.contains(testCase2.getIpsSrcFile()));
        assertFalse(resultList.contains(testCase3.getIpsSrcFile()));

        TestCase testCaseProj2 = newTestCase(referencingProject, testCaseProj2QName);
        testCaseProj2.setTestCaseType(testCaseTypeQName);

        resultList = testCaseType.searchMetaObjectSrcFiles(true);
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(testCase1.getIpsSrcFile()));
        assertTrue(resultList.contains(testCase2.getIpsSrcFile()));
        assertTrue(resultList.contains(testCaseProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(testCase3.getIpsSrcFile()));

        TestCaseType testCaseTypeProj2 = newTestCaseType(independentProject, testCaseTypeProj2QName);

        resultList = testCaseTypeProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, resultList.size());
    }

}
