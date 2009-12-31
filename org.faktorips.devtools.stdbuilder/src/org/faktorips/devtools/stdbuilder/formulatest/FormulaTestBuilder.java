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

package org.faktorips.devtools.stdbuilder.formulatest;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptGenerationCuBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.test.IpsFormulaTestCase;
import org.faktorips.runtime.test.IpsTestResult;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.values.DateUtil;

/**
 * A builder that extracts formula test cases from product components and generates the
 * corresponding formula test cases.
 * 
 * @author Joerg Ortmann
 */
public class FormulaTestBuilder extends DefaultJavaSourceFileBuilder {

    private static final String FORMULA_TEST_CASE_NAME = "formulaTestCaseName";
    private static final String RUNTIME_EXTENSION = "_formulaTest";
    private static final String TEST_METHOD_PREFIX = "test";

    private IIpsProject project;

    private IProductCmpt productCmpt;
    private IProductCmptType productCmptType;

    private IProductCmptNamingStrategy productCmptNamingStrategy;

    // wired builder
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;
    private ProductCmptBuilder productCmptBuilder;

    private Map<IFormula, Integer> formulasToTestForGeneration;
    private Map<String, List<String>> testParameterTypesForGeneration;
    private Map<String, List<String>> testParameterNamesForGeneration;

    public FormulaTestBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(FormulaTestBuilder.class));
        project = builderSet.getIpsProject();
        productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
        productCmptNamingStrategy.setIpsProject(project);
    }

    /**
     * @param productCmptInterfaceBuilder The productCmptInterfaceBuilder to set.
     */
    public synchronized void setProductCmptInterfaceBuilder(ProductCmptInterfaceBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    /**
     * @param productCmptGenImplClassBuilder The productCmptGenImplClassBuilder to set.
     */
    public synchronized void setProductCmptGenImplClassBuilder(ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder) {
        this.productCmptGenImplClassBuilder = productCmptGenImplClassBuilder;
    }

    /**
     * @param productCmptBuilder The productCmptBuilder to set.
     */
    public synchronized void setProductCmptBuilder(ProductCmptBuilder productCmptBuilder) {
        this.productCmptBuilder = productCmptBuilder;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {
            return false;
        }
        if (!ipsSrcFile.exists()) {
            return true;
        }

        productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (!productCmpt.isValid()) {
            return false;
        }
        // build formula test if at least one formula and formula test is specified
        if (!productCmpt.containsFormulaTest()) {
            return false;
        }
        productCmptType = productCmpt.findProductCmptType(project);
        if (productCmptType == null) {
            return false;
        }
        return true;
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);

        formulasToTestForGeneration = new HashMap<IFormula, Integer>();
        testParameterTypesForGeneration = new HashMap<String, List<String>>();
        testParameterNamesForGeneration = new HashMap<String, List<String>>();
    }

    protected String getSuperClassName() {
        return IpsFormulaTestCase.class.getName();
    }

    @Override
    public String getQualifiedClassName() throws CoreException {
        return getQualifiedClassName(productCmpt.getIpsSrcFile());
    }

    @Override
    public String getQualifiedClassName(IIpsObject ipsObject) throws CoreException {
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    @Override
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsSrcFile file = getVirtualIpsSrcFile(ipsSrcFile.getIpsObject());
        String qualifiedClassName = super.getQualifiedClassName(file);
        qualifiedClassName += RUNTIME_EXTENSION;
        return qualifiedClassName;
    }

    @Override
    public String getUnqualifiedClassName() throws CoreException {
        return StringUtil.unqualifiedName(getQualifiedClassName());
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = getFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(true, null);
        }
    }

    /*
     * Returns the file resource of the given ips source file.
     */
    private IFile getFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        IFolder folder = getFolder(ipsSrcFile);
        String fileName = StringUtil.getFilenameWithoutExtension(file.getName());
        fileName = productCmptNamingStrategy.getJavaClassIdentifier(fileName);
        return folder.getFile(fileName + RUNTIME_EXTENSION + ".java");
    }

    /*
     * Returns the package folder for the given ips sourcefile.
     */
    private IFolder getFolder(IIpsSrcFile ipsSrcFile) throws CoreException {
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_FORMULA_TEST_CASE, ipsSrcFile);
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getFolder(pathToPack);
    }

    private IIpsSrcFile getVirtualIpsSrcFile(IIpsObject ipsObject) {
        String name = productCmptNamingStrategy.getJavaClassIdentifier(ipsObject.getName());
        return productCmpt.getIpsSrcFile().getIpsPackageFragment().getIpsSrcFile(
                IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        if (productCmpt == null) {
            throw new CoreException(new IpsStatus("Product component not found! " + getIpsSrcFile()));
        }

        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(StringUtil.unqualifiedName(getQualifiedClassName()));
        mainSection.setSuperClass(getSuperClassName());

        generateConstructor(mainSection.getConstructorBuilder());
        generateMethodGetProductCmptType(productCmptType, mainSection.getMethodBuilder());

        List<String> testMethods = generateTestMethods(productCmpt, mainSection.getMethodBuilder());
        generateExecuteBusinessLogicMethod(productCmptType, testMethods, mainSection.getMethodBuilder());
        generateExecuteAssertsMethod(productCmptType, testMethods, mainSection.getMethodBuilder());

        generateComputeTestMethods(formulasToTestForGeneration, mainSection.getMethodBuilder());
    }

    /*
     * Creates all compute methods which should be tested by the formula test cases
     */
    private void generateComputeTestMethods(Map<IFormula, Integer> formulasToTest, JavaCodeFragmentBuilder builder)
            throws CoreException {
        ProductCmptGenerationCuBuilder generationBuilder = productCmptBuilder.getGenerationBuilder();
        for (Iterator<IFormula> iterator = formulasToTest.keySet().iterator(); iterator.hasNext();) {
            IFormula formula = iterator.next();
            Integer generationId = formulasToTest.get(formula);
            List<String> testParameterNames = testParameterNamesForGeneration.get(formula.getName() + "#"
                    + generationId);
            List<String> testParameterTypes = testParameterTypesForGeneration.get(formula.getName() + "#"
                    + generationId);
            if (testParameterNames == null || testParameterTypes == null) {
                throw new CoreException(new IpsStatus("No formula parameter names and types found for generation id: "
                        + generationId));
            }
            generationBuilder.generateMethodForFormulaForTestCase(formula, builder,
                    getComputeTestMethodSuffix(generationId.intValue()), testParameterNames
                            .toArray(new String[testParameterNames.size()]), testParameterTypes
                            .toArray(new String[testParameterTypes.size()]));
        }
    }

    private String getComputeTestMethodSuffix(int generationId) {
        return "ForTest_" + generationId;
    }

    /*
     * Code sample: <pre> public FtPolicyA___2006__01_formulaTest(String formulaTestCaseName) {
     * super(formulaTestCaseName); } </pre>
     */
    private void generateConstructor(JavaCodeFragmentBuilder builder) throws CoreException {
        String className = getUnqualifiedClassName();
        appendLocalizedJavaDoc("CONSTRUCTOR", className, getIpsObject(), builder);
        String[] argNames = new String[] { FORMULA_TEST_CASE_NAME };
        String[] argTypes = new String[] { String.class.getName() };
        builder.methodBegin(Modifier.PUBLIC, null, className, argNames, argTypes);
        builder.append("super(formulaTestCaseName);");
        builder.methodEnd();
    }

    /*
     * Code sample:
     * 
     * <pre> public IFtPolicyType getFtPolicyType() { return (IFtPolicyType)
     * getRepository().getProductComponent("FtPolicyA 2006-01"); } </pre>
     */
    private void generateMethodGetProductCmptType(IProductCmptType type, JavaCodeFragmentBuilder builder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_RODUCTCMPT_TYPE", null, getIpsObject(), builder);
        generateSignatureGetProductCmptGeneration(type, builder);
        builder.openBracket();

        builder.append("return (");
        builder.appendClassName(productCmptInterfaceBuilder.getQualifiedClassName(type));
        builder.append(")");
        builder.append(MethodNames.GET_REPOSITORY);
        builder.append("().");
        builder.append(MethodNames.GET_PRODUCT_COMPONENT);
        builder.append("(\"");
        builder.append(productCmpt.getRuntimeId());
        builder.appendln("\");");

        builder.closeBracket();
    }

    /*
     * Code sample:
     * 
     * <pre> public IMotorProductGen getMotorProductGen() </pre>
     */
    private void generateSignatureGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String genName = productCmptInterfaceBuilder.getQualifiedClassName(type);
        String methodName = ((StandardBuilderSet)getBuilderSet()).getGenerator(type).getMethodNameGetProductCmpt();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, genName, methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /*
     * Generates an empty method.
     */
    private void generateExecuteBusinessLogicMethod(IProductCmptType productCmptType,
            List<String> testMethods,
            JavaCodeFragmentBuilder builder) {
        String javaDoc = getJavaDocCommentForOverriddenMethod();
        JavaCodeFragment body = new JavaCodeFragment();
        appendOverrideAnnotation(builder, false);
        builder.method(Modifier.PUBLIC, "void", "executeBusinessLogic", EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY, body,
                javaDoc, ANNOTATION_GENERATED);
    }

    /*
     * Code sample:
     * 
     * <pre> public void executeAsserts(IpsTestResult result) { IFtPolicyType productCmpt =
     * getFtPolicyType(); testFtPolicyA___2006__01___20070101(productCmpt, result); } </pre>
     */
    private void generateExecuteAssertsMethod(IProductCmptType productCmptType,
            List<String> testMethods,
            JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getJavaDocCommentForOverriddenMethod();
        JavaCodeFragment body = new JavaCodeFragment();

        String genClassName = productCmptInterfaceBuilder.getQualifiedClassName(productCmptType);
        body.appendClassName(genClassName);
        body.append(" productCmpt = ");
        body.append(((StandardBuilderSet)getBuilderSet()).getGenerator(productCmptType).getMethodNameGetProductCmpt());
        body.appendln("();");
        body.appendln();

        // generate the test method call for each given test method
        for (Iterator<String> iter = testMethods.iterator(); iter.hasNext();) {
            String testMethodName = iter.next();
            body.append(testMethodName);
            body.append("(");
            body.append("productCmpt");
            body.appendln(", result);");
        }

        appendOverrideAnnotation(builder, false);
        builder.method(Modifier.PUBLIC, "void", "executeAsserts", new String[] { "result" },
                new String[] { IpsTestResult.class.getName() }, body, javaDoc, ANNOTATION_GENERATED);
    }

    /*
     * Code sample:
     * 
     * <pre> public void testFtPolicyA___2006__01___20070101(IProductComponent productCmpt,
     * IpsTestResult result) { FtPolicyTypeGen productComponentGen = (FtPolicyTypeGen)
     * productCmpt.getGenerationBase( DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
     * Object formulaResult = null; formulaResult = productComponentGen.computeFormulaResult(new
     * Integer(1), null); assertEquals(Decimal.valueOf("104.1"), formulaResult, result,
     * productComponentGen.toString(), "formulaResult.Formeltest"); </pre>
     */
    private List<String> generateTestMethods(IProductCmpt productCmpt, JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        ArrayList<String> testMethods = new ArrayList<String>();
        IIpsObjectGeneration[] gen = productCmpt.getGenerationsOrderedByValidDate();
        for (int i = 0; i < gen.length; i++) {
            appendTestMethodsContentForGeneration((IProductCmptGeneration)gen[i], codeBuilder, testMethods);
        }
        return testMethods;
    }

    /**
     * {@inheritDoc}
     * 
     * Returns true.
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    /*
     * @see generateTestMethods
     */
    private void appendTestMethodsContentForGeneration(IProductCmptGeneration generation,
            JavaCodeFragmentBuilder builder,
            ArrayList<String> testMethods) throws CoreException {
        IFormula[] formulas = generation.getFormulas();
        for (int i = 0; i < formulas.length; i++) {
            IFormulaTestCase[] testCases = formulas[i].getFormulaTestCases();
            for (int j = 0; j < testCases.length; j++) {
                appendTestMethodsContentForGenerationFormulaTest(generation, builder, testMethods, testCases[j]);
            }
        }
    }

    private void appendTestMethodsContentForGenerationFormulaTest(IProductCmptGeneration generation,
            JavaCodeFragmentBuilder builder,
            ArrayList<String> testMethods,
            IFormulaTestCase formulaTestCase) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        IFormula formula = formulaTestCase.getFormula();

        String testMethodName = TEST_METHOD_PREFIX
                + StringUtil.unqualifiedName(productCmptBuilder.getQualifiedClassName(generation)) + "_"
                + getJavaMethodSuffix(formula.getName()) + "_" + getJavaMethodSuffix(formulaTestCase.getName());

        // store the formula to indicate the generation of the test method
        boolean multipleFormulaTest = false;
        if (formulasToTestForGeneration.get(formula) != null) {
            Integer genNr = formulasToTestForGeneration.get(formula);
            multipleFormulaTest = genNr != null && genNr.intValue() == generation.getGenerationNo();
        }

        formulasToTestForGeneration.put(formula, new Integer(generation.getGenerationNo()));

        // append compute method call
        IMethod method = formula.findFormulaSignature(getIpsProject());
        body.append("formulaResult = ");
        body.append(method.getName());
        body.append(getComputeTestMethodSuffix(generation.getGenerationNo()));
        body.append("(");

        // append the method parameters in the correct order
        IParameter[] params = method.getParameters();
        Map<Integer, String> identifierNameIdx = new HashMap<Integer, String>(params.length);
        for (int k = 0; k < params.length; k++) {
            identifierNameIdx.put(new Integer(k), params[k].getName());
        }
        List<String> testParameterNames = new LinkedList<String>();
        List<String> testParameterTypes = new LinkedList<String>();
        List<IFormulaTestInputValue> orderedInputValue = new ArrayList<IFormulaTestInputValue>(params.length);
        for (int k = 0; k < params.length; k++) {
            String identifier = identifierNameIdx.get(new Integer(k));
            Datatype datatype = getIpsProject().findDatatype(params[k].getDatatype());
            if (datatype instanceof IPolicyCmptType || datatype instanceof IProductCmptType) {
                IFormulaTestInputValue[] formulaTestInputValues = formulaTestCase.getFormulaTestInputValues();
                // insert all attributes and their test values which are used in the formula
                // in the value cache
                for (int l = 0; l < formulaTestInputValues.length; l++) {
                    String identifierInFormula = formulaTestInputValues[l].getIdentifier();
                    if (identifierInFormula.startsWith(identifier)) {
                        ValueDatatype datatypeOfParam = formulaTestInputValues[l]
                                .findDatatypeOfFormulaParameter(project);
                        if (!multipleFormulaTest) {
                            // store parameter names and types to append the formula test signature
                            String parameterName = identifierInFormula.replaceAll("\\.", "_");
                            if (!testParameterNames.contains(parameterName)) {
                                testParameterNames.add(parameterName);
                                testParameterTypes.add(datatypeOfParam.getQualifiedName());
                                body.addImport(datatypeOfParam.getJavaClassName());
                            }
                        }
                        if (!orderedInputValue.contains(formulaTestInputValues[l])) {
                            orderedInputValue.add(formulaTestInputValues[l]);
                        }
                    }
                }
            } else {
                orderedInputValue.add(formulaTestCase.getFormulaTestInputValue(identifier));
            }
        }

        // store pctype parameter types and names
        if (!multipleFormulaTest) {
            testParameterNamesForGeneration.put(formula.getName() + "#" + generation.getGenerationNo(),
                    testParameterNames);
            testParameterTypesForGeneration.put(formula.getName() + "#" + generation.getGenerationNo(),
                    testParameterTypes);
        }

        // add compute method call with parameters values
        for (int k = 0; k < orderedInputValue.size(); k++) {
            if (k > 0) {
                body.append(", ");
            }
            Object inputValue = orderedInputValue.get(k);
            if (inputValue instanceof IFormulaTestInputValue) {
                IFormulaTestInputValue forumulaInputValue = (IFormulaTestInputValue)inputValue;
                DatatypeHelper inputValueHelper = project.getDatatypeHelper(forumulaInputValue
                        .findDatatypeOfFormulaParameter(project));
                body.append(inputValueHelper.newInstance(forumulaInputValue.getValue()));
            } else if (inputValue instanceof String) {
                body.append((String)inputValue);
            } else {
                body.append("null");
            }
        }
        body.appendln(");");

        // append assert method call
        body.append("assertEquals(");
        DatatypeHelper valueHelper = project.getDatatypeHelper(method.findDatatype(project));
        body.append(valueHelper.newInstance(formulaTestCase.getExpectedResult()));
        body.append(", formulaResult, result, productComponentGen.toString(), \"");
        body.append(formula.getName());
        body.append(".");
        body.append(formulaTestCase.getName());
        body.append("\"");
        body.appendln(");");

        appendLocalizedJavaDoc("METHOD_TEST_METHODS", getNameForGenerationConcept(productCmptType), getIpsObject(),
                builder);

        builder.signature(Modifier.PUBLIC, "void", testMethodName, new String[] { "productCmpt", "result" },
                new String[] { IProductComponent.class.getName(), IpsTestResult.class.getName() });
        builder.openBracket();

        // add top of test method content
        String productCmptGenClassName = productCmptGenImplClassBuilder.getQualifiedClassName(productCmptType);
        builder.appendClassName(productCmptGenClassName);
        builder.append(" productComponentGen = (");
        builder.appendClassName(productCmptGenClassName);
        builder.append(") productCmpt.getGenerationBase(");
        builder.appendClassName(DateUtil.class.getName());
        builder.append(".parseIsoDateStringToGregorianCalendar(\"");
        builder.append(DateUtil.gregorianCalendarToIsoDateString(generation.getValidFrom()));
        builder.appendln("\"));");
        builder.appendln();
        builder.appendln("Object formulaResult = null;");

        // add generated body
        builder.append(body);

        builder.closeBracket();
        testMethods.add(testMethodName);
    }

    private String getJavaMethodSuffix(String name) {
        return name.replaceAll(" ", "_").replaceAll("\\(", "_").replaceAll("\\)", "_");
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements, IIpsElement ipsElement) {
        // TODO AW: Not implemented yet.
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        // TODO AW: Not implemented yet.
        throw new RuntimeException("Not implemented yet.");
    }

}
