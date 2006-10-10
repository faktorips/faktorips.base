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

package org.faktorips.devtools.stdbuilder.testcasetype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestResult;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Generates a Java source file for a test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeClassBuilder extends DefaultJavaSourceFileBuilder {
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    
    // property key for the constructor's Javadoc.
    private final static String CONSTRUCTOR_JAVADOC = "CONSTRUCTOR_JAVADOC";
    private final static String INITINPUTFROMXML_JAVADOC = "INITINPUTFROMXML_JAVADOC";
    private final static String INITEXPECTEDRESULTFROMXML_JAVADOC = "INITEXPECTEDRESULTFROMXML_JAVADOC";
    private final static String EXECUTEBUSINESSLOGIC_JAVADOC = "EXECUTEBUSINESSLOGIC_JAVADOC";
    private static final String EXECUTEASSERTS_JAVADOC = "EXECUTEASSERTS_JAVADOC";
    private static final String EXECUTEBUSINESSLOGIC_TODO = "EXECUTEBUSINESSLOGIC_TODO";
    private static final String ASSERT_TODO = "ASSERT_TODO";
    private static final String ASSERT_DUMMY_EXPECTED = "ASSERT_DUMMY_EXPECTED";
    private static final String ASSERT_DUMMY_ACTUAL = "ASSERT_DUMMY_ACTUAL";
    private static final String INPUT_PREFIX = "INPUT_PREFIX";
    private static final String EXPECTED_RESULT_PREFIX = "EXPECTED_RESULT_PREFIX";
    private static final String RULE_NOT_VIOLATED_PREFIX = "RULE_NOT_VIOLATED_PREFIX";
    private static final String RULE_VIOLATED_PREFIX = "RULE_VIOLATED_PREFIX";
    private static final String VIOLATED_CONSTANT_NAME = "VIOLATED_CONSTANT_NAME";
    private static final String NOT_VIOLATED_CONSTANT_NAME = "NOT_VIOLATED_CONSTANT_NAME";
    private static final String ASSERT_RULE_METHOD_JAVADOC = "ASSERT_RULE_METHOD_JAVADOC";
    private static final String ASSERT_FAIL_NO_VIOLATION_EXPECTED = "ASSERT_FAIL_NO_VIOLATION_EXPECTED";
    private static final String ASSERT_FAIL_VIOLATION_EXPECTED = "ASSERT_FAIL_VIOLATION_EXPECTED";
    
    private String inputPrefix;
    private String expectedResultPrefix;
    private String violationTypePrefixViolated;
    private String violationTypePrefixNotViolated ;
    private String violatedConstantName;
    private String notViolatedConstantName ;
    private String assertFailNoValidationExpected;
    private String assertFailViolationExpected;
    
    private IIpsProject project;
    private ITestCaseType testCaseType;
    
    // Contains the cached datatype helper
    private HashMap dataTypeHelpers = new HashMap();
    
    public TestCaseTypeClassBuilder(
            IIpsArtefactBuilderSet builderSet, 
            String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(TestCaseTypeClassBuilder.class));
        setMergeEnabled(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.TEST_CASE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status)
            throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        inputPrefix = getLocalizedText(getIpsSrcFile(), INPUT_PREFIX);
        expectedResultPrefix = getLocalizedText(getIpsSrcFile(), EXPECTED_RESULT_PREFIX);   
        violationTypePrefixViolated = getLocalizedText(getIpsSrcFile(), RULE_VIOLATED_PREFIX);
        violationTypePrefixNotViolated = getLocalizedText(getIpsSrcFile(), RULE_NOT_VIOLATED_PREFIX);
        violatedConstantName = getLocalizedText(getIpsSrcFile(), VIOLATED_CONSTANT_NAME);
        notViolatedConstantName = getLocalizedText(getIpsSrcFile(), NOT_VIOLATED_CONSTANT_NAME);
        assertFailNoValidationExpected = getLocalizedText(getIpsSrcFile(), ASSERT_FAIL_NO_VIOLATION_EXPECTED);
        assertFailViolationExpected = getLocalizedText(getIpsSrcFile(), ASSERT_FAIL_VIOLATION_EXPECTED);
        
        testCaseType = (ITestCaseType)getIpsObject(); 
        project = testCaseType.getIpsProject();
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterBuildProcess(int buildKind) throws CoreException {
        // nothing to do
    }
    
    /**
     * {@inheritDoc}
     */
    protected JavaCodeFragment generateCodeForJavatype() throws CoreException {
        
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        
        codeBuilder.classBegin(Modifier.PUBLIC, getUnqualifiedClassName(), getSuperClassName() , new String[0]);
        buildMemberVariables(codeBuilder, testCaseType);
        buildConstructor(codeBuilder);
        buildHelperMethods(codeBuilder);
        buildSuperMethodImplementation(codeBuilder, testCaseType);
        codeBuilder.classEnd();
        
        return codeBuilder.getFragment();
    }
    
    protected String getSuperClassName(){
        return IpsTestCase2.class.getName();
    }

    //
    // helper methods for the builder
    //

    /**
     * Generates the member variables.
     * 
     * @throws CoreException if an error occurs
     */
    private void buildMemberVariables(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType) throws CoreException {
        buildMemberForTestRuleParameter(codeBuilder, testCaseType.getTestRuleParameters(), expectedResultPrefix);
        buildMemberForTestValueParameter(codeBuilder, testCaseType.getInputTestValueParameters(), inputPrefix);
        buildMemberForTestValueParameter(codeBuilder, testCaseType.getExpectedResultTestValueParameters(), expectedResultPrefix);
        
        buildMemberForTestPolicyCmptParameter(codeBuilder, testCaseType.getInputTestPolicyCmptTypeParameters(), inputPrefix);
        buildMemberForTestPolicyCmptParameter(codeBuilder, testCaseType.getExpectedResultTestPolicyCmptTypeParameters(), expectedResultPrefix);
    }

    /*
     * Generates the code for the member test value type parameter declaration. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * private [Datatype] [input,expected][TestValueParameter.name];
     * </pre>
     */
    private void buildMemberForTestValueParameter(JavaCodeFragmentBuilder codeBuilder, ITestValueParameter[] testValueParams, 
            String variablePrefix) throws CoreException {
        for (int i = 0; i < testValueParams.length; i++) {
            if (!testValueParams[i].isValid()){
                continue;
            }
            ITestValueParameter testValueParam = testValueParams[i];
        	DatatypeHelper helper = getCachedDatatypeHelper(testValueParam);
            codeBuilder.javaDoc("", ANNOTATION_GENERATED);
            codeBuilder.varDeclaration(Modifier.PRIVATE, helper.getJavaClassName(), 
                    variablePrefix + StringUtils.capitalise(testValueParam.getName()));
        }
    }
    
    /*
     * Generates the code for the member test rule parameter declaration. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * private List expectedViolated[TestRuleParameter.name];
     * private List expectedNotViolated[TestRuleParameter.name];
     * </pre>
     */
    private void buildMemberForTestRuleParameter(JavaCodeFragmentBuilder codeBuilder, ITestRuleParameter[] testRuleParams, 
            String variablePrefix) throws CoreException {
        if (testRuleParams.length == 0){
            // only generate variables if rules exists
            return;
        }
        // generate constant variables to indicate violated or not violated failure
        codeBuilder.javaDoc("", ANNOTATION_GENERATED);
        codeBuilder.varDeclaration(Modifier.PRIVATE + Modifier.FINAL + Modifier.STATIC, String.class, 
                violatedConstantName, new JavaCodeFragment("\"violated\""));
        codeBuilder.javaDoc("", ANNOTATION_GENERATED);
        codeBuilder.varDeclaration(Modifier.PRIVATE + Modifier.FINAL + Modifier.STATIC, String.class, 
                notViolatedConstantName, new JavaCodeFragment("\"notViolated\""));
        
        for (int i = 0; i < testRuleParams.length; i++) {
            if (!testRuleParams[i].isValid()){
                continue;
            }
            // create two lists, the list will be filled in the init expected result from xml method
            //   because the violation type depends on the runtime xml test case
            // violation type: violated
            codeBuilder.javaDoc("", ANNOTATION_GENERATED);
            codeBuilder.varDeclaration(Modifier.PRIVATE, List.class, 
                    getRuleMemberVariableName(variablePrefix, violationTypePrefixViolated, testRuleParams[i]) );
            // violation type: not violated
            codeBuilder.javaDoc("", ANNOTATION_GENERATED);
            codeBuilder.varDeclaration(Modifier.PRIVATE, List.class, 
                    getRuleMemberVariableName(variablePrefix, violationTypePrefixNotViolated, testRuleParams[i]));
        }
    }
    
    /*
     * Returns the variable name of a rule member variable.<br>
     * The variablePrefix could be e.g. "input" or "expected result" (but "input" is not supported yet).<br>
     * The violationTypePrefix could be e.g. "violated" or "not violated".
     */
    private String getRuleMemberVariableName(String variablePrefix, String violationTypePrefix, ITestRuleParameter parameter){
        return variablePrefix + StringUtils.capitalise(violationTypePrefix) + StringUtils.capitalise(parameter.getName());
    }
    
    /*
     * Returns the corresponding helper for the given ITestValueParameter from a cached list.
     */
    private DatatypeHelper getCachedDatatypeHelper(ITestValueParameter testValueParameter) throws CoreException{
    	ValueDatatype valueDatatype = testValueParameter.findValueDatatype();
        if (valueDatatype == null)
            return null;
        
        DatatypeHelper helper = (DatatypeHelper) dataTypeHelpers.get(valueDatatype.getJavaClassName());
    	if (helper != null)
    		return helper;
    	
        try {
            helper = project.getDatatypeHelper(valueDatatype);
            if (helper == null) {
                throw new CoreException(new IpsStatus("No datatype helper found for datatype " + valueDatatype));            
            }
            return helper;
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(IStatus.ERROR,
                    "Error building value parameter " + testValueParameter.getName() + " of "
                            + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
        }
    }
    
    /*
     * Generates the code for the member test policy component type parameter declaration. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * private [PolicyCmptTypeParameter.name] [input,expected][PolicyCmptTypeParameter.name];
     * </pre>
     */
    private void buildMemberForTestPolicyCmptParameter(JavaCodeFragmentBuilder codeBuilder, ITestPolicyCmptTypeParameter[] policyTypeParams, 
            String variablePrefix) throws CoreException {
        for (int i = 0; i < policyTypeParams.length; i++) {
            if (!policyTypeParams[i].isValid()){
                continue;
            }
            codeBuilder.javaDoc("", ANNOTATION_GENERATED);
            codeBuilder.varDeclaration(Modifier.PRIVATE, getQualifiedNameFromTestPolicyCmptParam(policyTypeParams[i]), 
                    variablePrefix + policyTypeParams[i].getName());
        }
    }
    
    /*
     * Returns the qualified name of the policy component where the given test policy component type parameter points to.
     */
    protected String getQualifiedNameFromTestPolicyCmptParam(ITestPolicyCmptTypeParameter testPolicyTypeParam) throws CoreException{
        IPolicyCmptType policyCmptType = testPolicyTypeParam.findPolicyCmptType();
        if ( policyCmptType== null){
        	throw new CoreException(
        			new IpsStatus("Policy component type " + testPolicyTypeParam.getPolicyCmptType() + " not found for test policy component type parameter " 
        					+ testPolicyTypeParam.getName())); 
        }
        String pcTypePackage = getBuilderSet().getPackage(DefaultBuilderSet.KIND_POLICY_CMPT_IMPL, policyCmptType.getIpsSrcFile());
        return StringUtil.qualifiedName(pcTypePackage, policyCmptType.getName());
    }
    
    /*
     * Generates the constructor. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * public NeuzugangFamilieType(String qualifiedName) throws Exception{
     *   super(qualifiedName);
     *   repository = new ClassloaderRuntimeRepository(this.getClass().getClassLoader(), "org.faktorips.integrationtest.internal");
     * }
     * </pre>
     */
    private void buildConstructor(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        String className = getUnqualifiedClassName();
        String javaDoc = getLocalizedText(getIpsSrcFile(), CONSTRUCTOR_JAVADOC);
        String[] argNames = new String[] { "qualifiedName" };
        String[] argClassNames = new String[] { "java.lang.String" };
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("super(qualifiedName);");
        body.appendln(MARKER_BEGIN_USER_CODE);
        body.appendln(MARKER_END_USER_CODE);
        codeBuilder.javaDoc(javaDoc, ANNOTATION_RESTRAINED_MODIFIABLE);
        codeBuilder.methodBegin(Modifier.PUBLIC, null, className, 
                argNames, argClassNames, new String[]{ParserConfigurationException.class.getName()});
        codeBuilder.append(body);
        codeBuilder.methodEnd();
    }

    /*
     * Generates the super method implenetations.
     */
    private void buildSuperMethodImplementation(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType) throws CoreException {
        buildMethodInitInputFromXml(codeBuilder, testCaseType);
        buildMethodInitExpectedResultFromXml(codeBuilder, testCaseType);
        buildMethodsForAssertRules(codeBuilder, testCaseType.getTestRuleParameters(), expectedResultPrefix);
        buildMethodExecuteBusinessLogic(codeBuilder, testCaseType);
        buildMethodExecuteAsserts(codeBuilder, testCaseType);
    }

    /*
     * Generates the method initInputFromXml. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * protected void initInputFromXml(Element element) {
     * ...
     * }
     * </pre>
     */    
    private void buildMethodInitInputFromXml(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType) throws CoreException {
        String javaDoc = getLocalizedText(getIpsSrcFile(), INITINPUTFROMXML_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln(MARKER_BEGIN_USER_CODE);
        body.appendln(MARKER_END_USER_CODE);
        buildInitForTestPolicyCmptParameter(body, testCaseType.getInputTestPolicyCmptTypeParameters(), inputPrefix);
        buildInitForTestValueParameter(body, testCaseType.getInputTestValueParameters(), inputPrefix);
        buildMethodInit(codeBuilder, "initInputFromXml", body, javaDoc);
    }
    
    /*
     * Generates the method initExpectedResultFromXml. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * protected void initExpectedResultFromXml(Element element) {
     * Element inputElement;
     * ...
     * }
     * </pre>
     */        
    private void buildMethodInitExpectedResultFromXml(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType)  throws CoreException {
        String javaDoc = getLocalizedText(getIpsSrcFile(), INITEXPECTEDRESULTFROMXML_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln(MARKER_BEGIN_USER_CODE);
        body.appendln(MARKER_END_USER_CODE);        
        buildInitForTestPolicyCmptParameter(body, testCaseType.getExpectedResultTestPolicyCmptTypeParameters(), expectedResultPrefix);
        buildInitForTestValueParameter(body, testCaseType.getExpectedResultTestValueParameters(), expectedResultPrefix);
        buildInitForTestRuleParameter(body, testCaseType.getTestRuleParameters(), expectedResultPrefix);
        buildMethodInit(codeBuilder, "initExpectedResultFromXml", body, javaDoc);
    }
    
    /*
     * Capsulates the generation of the method declaration.
     */
    protected void buildMethodInit(JavaCodeFragmentBuilder codeBuilder, String methodName, JavaCodeFragment body, String javaDoc){
        codeBuilder.method(Modifier.PUBLIC, "void", methodName, new String[]{"element"}, 
                new String[]{Element.class.getName()}, body, javaDoc, ANNOTATION_RESTRAINED_MODIFIABLE);
    }
    
    /*
     * Generates the body for the initInputFromXml method.<br>
     * For each test policy component type parameter in the given policyTypeParams list.
     * <p> 
     * Example:
     * <p>
     * <pre>
    *   childElement  = XmlUtil.getFirstElement(element, "[PolicyCmptTypeParameter.name]");
    *   if (inputElement!=null){
    *        [PolicyCmptTypeParameter.name] = new [PolicyCmptTypeParameter.name]();
    *        [PolicyCmptTypeParameter.name].initFromXml(childElement, true, getRepository(), null);
    *   }
     * </pre>
     */    
    private void buildInitForTestPolicyCmptParameter(JavaCodeFragment body, ITestPolicyCmptTypeParameter[] policyTypeParams, String variablePrefix) throws CoreException {
        if (policyTypeParams.length > 0){
            body.appendClassName(Element.class);
            body.appendln(" childElement = null;");
        }
        
        for (int i = 0; i < policyTypeParams.length; i++) {
            if (!policyTypeParams[i].isValid()){
                continue;
            }
            ITestPolicyCmptTypeParameter policyTypeParam = policyTypeParams[i];
            body.append("childElement = ");
            body.appendClassName(XmlUtil.class);
            body.appendln(".getFirstElement(element, \"" + policyTypeParams[i].getName() + "\");");
            body.appendln("if (childElement != null){");
            buildConstrutorForTestPolicyCmptParameter(body, policyTypeParam, variablePrefix);
            body.appendln(variablePrefix + policyTypeParam.getName() + ".initFromXml(childElement, true, " + MethodNames.GET_REPOSITORY + "(), null);");
            body.appendln("}");
        }
    }

    /*
     * Generates the construtor for a PolicyCmptParameter in the initInputFromXml method.<br>
     * <p> 
     * Example:
     * <p>
     * <pre>
    *        [PolicyCmptTypeParameter.name] = new [PolicyCmptTypeParameter.name]();
     * </pre>
     */    
    protected void buildConstrutorForTestPolicyCmptParameter(JavaCodeFragment body, ITestPolicyCmptTypeParameter policyTypeParam, String variablePrefix) throws CoreException{
        String qualifiedPolicyCmptName = getQualifiedNameFromTestPolicyCmptParam(policyTypeParam); 
        body.append(variablePrefix + policyTypeParam.getName() + " = new ");
        body.appendClassName(qualifiedPolicyCmptName);
        body.appendln("();");
    }
    
    /*
     * Generates the body for the initInputFromXml method.<br>
     * For each test policy component type parameter in the given policyTypeParams list.
     */    
    private void buildInitForTestValueParameter(JavaCodeFragment body, ITestValueParameter[] valueParams, String variablePrefix) throws CoreException {
        for (int i = 0; i < valueParams.length; i++) {
            if (!valueParams[i].isValid()){
                continue;
            }
        	ITestValueParameter policyTypeParam = valueParams[i];
            body.appendln(variablePrefix + StringUtils.capitalise(policyTypeParam.getName()) + " = ");
            DatatypeHelper dataTypeHelper = getCachedDatatypeHelper(policyTypeParam);
            
            if (dataTypeHelper.getDatatype().isPrimitive()) {
                body.append(dataTypeHelper.newInstanceFromExpression("getValueFromNode(element, \"" + policyTypeParam.getName() + "\")"));
                body.appendln(";");
            }
            else {
                body.appendClassName(dataTypeHelper.getJavaClassName());
                String valueOfMethod = "valueOf";
                if (dataTypeHelper.getDatatype() instanceof GenericValueDatatype){
                    valueOfMethod = ((GenericValueDatatype) dataTypeHelper.getDatatype()).getValueOfMethodName();
                }
                body.appendln("." + valueOfMethod + "(getValueFromNode(element, \"" + policyTypeParam.getName() + "\"));");
            }
        }
    }
    
    /*
     * Generates the body for the initInputFromXml method.<br>
     * For each test rule parameter in the given list.
     * <p> 
     * Example (one rule):
     * <p>
     * <pre>
     *   erwartetVerletztRegeln = new ArrayList();
     *   erwartetNichtVerletztRegeln = new ArrayList();
     *   List rules = getElementsFromNode(element, "Regeln", "testrule");
     *   for (Iterator iter = rules.iterator(); iter.hasNext();) {
     *       Element ruleElement = (Element)iter.next();
     *       String violationType = ruleElement.getAttribute("violationType");
     *       String messageCode = ruleElement.getAttribute("validationRuleMessageCode");
     *       if ("violated".equals(violationType)){
     *           erwartetVerletztRegeln.add(messageCode);
     *       } else if ("notViolated".equals(violationType)){
     *           erwartetNichtVerletztRegeln.add(messageCode);
     *       }
     *   }
     * </pre>
     */
    private void buildInitForTestRuleParameter(JavaCodeFragment body, ITestRuleParameter[] ruleParams, String variablePrefix) throws CoreException {
        for (int i = 0; i < ruleParams.length; i++) {
            if (!ruleParams[i].isValid()){
                continue;
            }
            String rulesVariableNameNotViolated = getRuleMemberVariableName(variablePrefix, violationTypePrefixNotViolated, ruleParams[i]);
            String rulesVariableNameViolated = getRuleMemberVariableName(variablePrefix, violationTypePrefixViolated, ruleParams[i]);
            String ruleListName = "rules" + StringUtils.capitalise(ruleParams[i].getName());
            body.append(rulesVariableNameNotViolated);
            body.append(" = new ");
            body.appendClassName(ArrayList.class.getName());
            body.appendln("();");
            body.append(rulesVariableNameViolated);
            body.append(" = new ");
            body.appendClassName(ArrayList.class.getName());
            body.appendln("();");
            body.appendClassName(List.class.getName());
            body.append(" ");
            body.append(ruleListName);
            body.append(" = getElementsFromNode(element, \"");
            body.append(ruleParams[i].getName());
            body.appendln("\", \"type\", \"testrule\");");
            body.append("for (");
            body.appendClassName(Iterator.class.getName());
            body.append(" iter = ");
            body.append(ruleListName);
            body.appendln(".iterator(); iter.hasNext();){");
            body.appendClassName(Element.class.getName());
            body.appendln(" ruleElement = (Element)iter.next();");
            body.appendln("String violationType = ruleElement.getAttribute(\"violationType\");");
            body.appendln("String messageCode = ruleElement.getAttribute(\"validationRuleMessageCode\");");
            body.appendln("if (\"violated\".equals(violationType)){");
            body.appendln(rulesVariableNameViolated + ".add(messageCode);");
            body.appendln("} else if (\"notViolated\".equals(violationType)){");
            body.appendln(rulesVariableNameNotViolated + ".add(messageCode);");
            body.appendln("}");
            body.appendln("}");
        }
    }
    
    /*
     * Generates the method executeBusinessLogic. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * public void executeBusinessLogic() throws Exception {
     * }
     * </pre>
     */    
    private void buildMethodExecuteBusinessLogic(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType){
        String javaDoc = getLocalizedText(getIpsSrcFile(), EXECUTEBUSINESSLOGIC_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("// TODO " + getLocalizedText(getIpsSrcFile(), EXECUTEBUSINESSLOGIC_TODO));
        codeBuilder.method(Modifier.PUBLIC, "void", "executeBusinessLogic", 
                EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY, body, javaDoc, ANNOTATION_MODIFIABLE);
    }
    
    /*
     * Generates the method executeAsserts. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * public void executeAsserts(IpsTestResult result) throws Exception {
     * }
     * </pre>
     */
    private void buildMethodExecuteAsserts(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType) throws CoreException{
        String javaDoc = getLocalizedText(getIpsSrcFile(), EXECUTEASSERTS_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();
        
        // generate dummy assert with todo remark
        body.appendln("// TODO " + getLocalizedText(getIpsSrcFile(), ASSERT_TODO));
        body.appendln("assertEquals(\"" + getLocalizedText(getIpsSrcFile(), ASSERT_DUMMY_EXPECTED) + "\", \"" + getLocalizedText(getIpsSrcFile(), ASSERT_DUMMY_ACTUAL) + "\", result);");
        
        codeBuilder.method(Modifier.PUBLIC, "void", "executeAsserts", new String[]{"result"}, 
                new String[]{IpsTestResult.class.getName()}, body, javaDoc, ANNOTATION_MODIFIABLE);
    }
    
    /*
     * Generates the methods to execute the assertions of all rules inside the test case.
     */
    private void buildMethodsForAssertRules(JavaCodeFragmentBuilder codeBuilder, ITestRuleParameter[] ruleParams, String variablePrefix) throws CoreException {
        if (ruleParams.length == 0){
            return;
        }
        for (int i = 0; i < ruleParams.length; i++) {
            if (!ruleParams[i].isValid()){
                continue;
            }
            buildMethodAssertRule(codeBuilder, variablePrefix, ruleParams[i].getName());
        }
    }
    
    /*
     * Generates the method to execute the assertion of one rule.<br>
     * Example:
     * <p>
     * <pre>     
     * public void executeValidationErwartetRegeln(MessageList messageList, IpsTestResult result){
     *    for (Iterator iter = erwartetVerletzteRegeln.iterator(); iter.hasNext();) {
     *        String msgCode = (String)iter.next();
     *        if (messageList.getMessageByCode(msgCode) == null){
     *            fail(VERLETZT, NICHT_VERLETZT, result, "Regeln", msgCode, "Fehlende Regelverletzung: " + msgCode);
     *       }
     *    }
     *    for (Iterator iter = erwartetNichtVerletzteRegeln.iterator(); iter.hasNext();) {
     *        String msgCode = (String)iter.next();
     *        if (messageList.getMessageByCode(msgCode) != null){
     *            fail(NICHT_VERLETZT, VERLETZT, result, "Regeln", msgCode, "Regelverletzung: " + msgCode + ". Darf nicht verletzt werden.");
     *        }
     *    }        
     * }
     * </pre>
     */
    private void buildMethodAssertRule(JavaCodeFragmentBuilder codeBuilder, String variablePrefix, String ruleName){
        String ruleContainerNameViolated = variablePrefix + StringUtils.capitalise(violationTypePrefixViolated)
                + StringUtils.capitalise(ruleName);
        String ruleContainerNameNotViolated = variablePrefix + StringUtils.capitalise(violationTypePrefixNotViolated)
                + StringUtils.capitalise(ruleName);

        String methodName = "executeAssertsFor" + ruleName;
        String javaDoc = getLocalizedText(getIpsSrcFile(), ASSERT_RULE_METHOD_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();

        String[] ruleListNames = new String[]{ruleContainerNameViolated, ruleContainerNameNotViolated};
        String[] expectedValues= new String[]{violatedConstantName, notViolatedConstantName};
        String[] actualValues = new String[]{notViolatedConstantName, violatedConstantName};
        String[] failureMessages = new String[]{assertFailViolationExpected, assertFailNoValidationExpected};
        String[] compareOperations = new String[]{" == ", " != "};
        for (int i = 0; i < ruleListNames.length; i++) {
            body.append("for (");
            body.appendClassName(Iterator.class.getName());
            body.append(" iter = ");
            body.append(ruleListNames[i]);
            body.appendln(".iterator(); iter.hasNext();){");
            body.appendln("String msgCode = (String)iter.next();");
            body.append("if (messageList.getMessageByCode(msgCode) ");
            body.append(compareOperations[i]);
            body.appendln(" null){");
            body.append("fail(");
            body.append(expectedValues[i]);
            body.append(", ");
            body.append(actualValues[i]);
            body.append(", result, \"");
            body.append(ruleName);
            body.append("\", msgCode, \"");
            body.append(failureMessages[i]);
            body.appendln("\" + msgCode);");
            body.appendln("}}");
        }
        
        codeBuilder.method(Modifier.PUBLIC, "void", methodName, new String[] { "messageList",
                "result" }, new String[] { MessageList.class.getName(),
                IpsTestResult.class.getName() }, body, javaDoc, ANNOTATION_GENERATED);
    }
    

    protected void buildHelperMethods(JavaCodeFragmentBuilder codeBuilder){
        buildHelperMethodGetValueFromNode(codeBuilder);
        buildHelperMethodGetElementsFromNode(codeBuilder);
    }
    
    /*
     * Generates helper method for getting the value from a node.
     * <p>
     * Example:
     * <p>
     * <pre>
	 *	private String getValueFromNode(Element elem, String nodeName){
	 *	    String value = null;
	 *		Element el = XmlUtil.getFirstElement(elem, nodeName);
	 *	    if (el != null){
	 *	    	Node child = el.getFirstChild();
	 *	    	value = child!=null? child.getNodeValue():null;
	 *	    }
	 *	    return value;
	 *	}
     * </pre>
     */
    protected void buildHelperMethodGetValueFromNode(JavaCodeFragmentBuilder codeBuilder){
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("String value = null;");
        body.appendClassName(Node.class);
        body.append(" el = ");
        body.appendClassName(XmlUtil.class);
        body.appendln(".getFirstElement(elem, nodeName);");
        body.appendln("if (el != null){");
        body.appendln("Node child = el.getFirstChild();");
        body.appendln("value = child!=null? child.getNodeValue():null;");
        body.appendln("}");
        body.appendln("return value;");
        codeBuilder.method(Modifier.PUBLIC,  String.class.getName(), "getValueFromNode", new String[]{"elem", "nodeName"}, 
                new String[]{Element.class.getName(), String.class.getName()}, body, "", ANNOTATION_GENERATED);
    }
    
    /*
     * Generates helper method for getting specific elements from a node. 
     * <p>
     * Example:
     * <p>
     * <pre>
     * private List getElementsFromNode(Element elem, String nodeName, String attributeName, String attributeValue) {
     *    List result = new ArrayList();
     *    NodeList nl = elem.getChildNodes();
     *    for (int i=0, max=nl.getLength(); i<max; i++) {
     *        if (!(nl.item(i) instanceof Element)) {
     *            continue;
     *        }
     *        Element el = (Element)nl.item(i);
     *        String typeAttr = el.getAttribute(attributeName);
     *        if (attributeValue.equals(typeAttr) && el.getNodeName().equals(nodeName)) {
     *            result.add(el);
     *        }
     *    }
     *    return result;
     * }
     */
    protected void buildHelperMethodGetElementsFromNode(JavaCodeFragmentBuilder codeBuilder){
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendClassName(List.class.getName());
        body.append(" result = new ");
        body.appendClassName(ArrayList.class.getName());
        body.appendln("();");
        body.appendClassName(NodeList.class.getName());
        body.appendln(" nl = elem.getChildNodes();");
        body.appendln("for (int i=0, max=nl.getLength(); i<max; i++) {");
        body.append("if (!(nl.item(i) instanceof ");
        body.appendClassName(Element.class.getName());
        body.appendln(")){");
        body.appendln("continue;");
        body.appendln("}");
        body.appendln("Element el = (Element)nl.item(i);");
        body.appendln("String typeAttr = el.getAttribute(attributeName);");
        body.appendln("if (attributeValue.equals(typeAttr) && el.getNodeName().equals(nodeName)) {");
        body.appendln("result.add(el);");
        body.appendln("}}");
        body.appendln("return result;");
        codeBuilder.method(Modifier.PUBLIC,  List.class.getName(), "getElementsFromNode", new String[]{"elem", "nodeName", "attributeName", "attributeValue"}, 
                new String[]{Element.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()}, body, "", ANNOTATION_GENERATED);
    }
}