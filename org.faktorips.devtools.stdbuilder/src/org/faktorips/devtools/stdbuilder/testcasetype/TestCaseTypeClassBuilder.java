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
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

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
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestResult;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
  
    private String inputPrefix;
    private String expectedResultPrefix;
    
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
        
        testCaseType = (ITestCaseType) getIpsObject(); 
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
            if (!testValueParams[i].isValid())
                continue;
            ITestValueParameter testValueParam = testValueParams[i];
        	DatatypeHelper helper = getCachedDatatypeHelper(testValueParam);
            codeBuilder.javaDoc("", ANNOTATION_GENERATED);
            codeBuilder.varDeclaration(Modifier.PRIVATE, helper.getJavaClassName(), 
                    variablePrefix + org.apache.commons.lang.StringUtils.capitalise(testValueParam.getName()));
        }
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
            if (!policyTypeParams[i].isValid())
                continue;
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
        codeBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
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
        buildMethodInit(codeBuilder, "initExpectedResultFromXml", body, javaDoc);
    }
    
    /*
     * Capsulates the generation of the method declaration.
     */
    protected void buildMethodInit(JavaCodeFragmentBuilder codeBuilder, String methodName, JavaCodeFragment body, String javaDoc){
        codeBuilder.method(Modifier.PUBLIC, "void", methodName, new String[]{"element"}, 
                new String[]{Element.class.getName()}, body, javaDoc, ANNOTATION_GENERATED);
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
            if (!policyTypeParams[i].isValid())
                continue;
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
            if (!valueParams[i].isValid())
                continue;
        	ITestValueParameter policyTypeParam = valueParams[i];
            body.appendln(variablePrefix + org.apache.commons.lang.StringUtils.capitalise(policyTypeParam.getName()) + " = ");
            DatatypeHelper dataTypeHelper = getCachedDatatypeHelper(policyTypeParam);
            body.appendClassName(dataTypeHelper.getJavaClassName());
            String valueOfMethod = "valueOf";
            if (dataTypeHelper.getDatatype() instanceof GenericValueDatatype){
            	valueOfMethod = ((GenericValueDatatype) dataTypeHelper.getDatatype()).getValueOfMethodName();
            }
            body.appendln("." + valueOfMethod + "(getValueFromNode(element, \"" + policyTypeParam.getName() + "\"));");
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
    private void buildMethodExecuteAsserts(JavaCodeFragmentBuilder codeBuilder, ITestCaseType testCaseType){
        String javaDoc = getLocalizedText(getIpsSrcFile(), EXECUTEASSERTS_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("// TODO " + getLocalizedText(getIpsSrcFile(), ASSERT_TODO));
        body.appendln("assertEquals(\"" + getLocalizedText(getIpsSrcFile(), ASSERT_DUMMY_EXPECTED) + "\", \"" + getLocalizedText(getIpsSrcFile(), ASSERT_DUMMY_ACTUAL) + "\", result);");
        codeBuilder.method(Modifier.PUBLIC, "void", "executeAsserts", new String[]{"result"}, 
                new String[]{IpsTestResult.class.getName()}, body, javaDoc, ANNOTATION_MODIFIABLE);
    }
    
    /*
     * Generates helper methods.
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
    protected void buildHelperMethods(JavaCodeFragmentBuilder codeBuilder){
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
}