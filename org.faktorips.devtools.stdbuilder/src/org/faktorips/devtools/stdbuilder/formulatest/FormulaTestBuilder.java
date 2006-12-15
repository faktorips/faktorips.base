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

package org.faktorips.devtools.stdbuilder.formulatest;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.internal.model.product.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
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
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    
    private static final String RUNTIME_EXTENSION = "_formulaTest";
    private static final String TEST_METHOD_PREFIX = "test";
    
    private IIpsProject project;
    
    private IProductCmpt productCmpt;
    private IProductCmptType productCmptType;
    
    private IProductCmptNamingStrategy productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
    
    // wired builder
    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder; 
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder; 
    private ProductCmptBuilder productCmptBuilder;
    
    public FormulaTestBuilder(
            IIpsArtefactBuilderSet builderSet, 
            String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(FormulaTestBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * @param policyCmptInterfaceBuilder The policyCmptInterfaceBuilder to set.
     */
    public synchronized void setPolicyCmptInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder) {
        this.policyCmptInterfaceBuilder = policyCmptInterfaceBuilder;
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

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            return false;
        }
        productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (!productCmpt.isValid()) {
            return false;
        }
        // build formula test if at least one formula and formula test is specified
        if (!productCmpt.containsFormulaTest()) {
            return false;
        }
        IPolicyCmptType pcType = productCmpt.findPolicyCmptType();
        productCmptType = pcType == null ? null : pcType.findProductCmptType();
        if (pcType == null) {
            return false;
        }

        project = productCmpt.getIpsProject();

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status)
            throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
    }

    protected String getSuperClassName(){
        return IpsFormulaTestCase.class.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getQualifiedClassName() throws CoreException {
        return getQualifiedClassName(productCmpt.getIpsSrcFile());
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedClassName(IIpsObject ipsObject) throws CoreException {
        return getQualifiedClassName(ipsObject.getIpsSrcFile());
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        IIpsSrcFile file = getVirtualIpsSrcFile(ipsSrcFile.getIpsObject());
        String qualifiedClassName = super.getQualifiedClassName(file);
        qualifiedClassName += RUNTIME_EXTENSION;
        return qualifiedClassName;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName() throws CoreException {
        return StringUtil.unqualifiedName(getQualifiedClassName());
    }
    
    private IIpsSrcFile getVirtualIpsSrcFile(IIpsObject ipsObject) {
        String name = productCmptNamingStrategy.getJavaClassIdentifier(ipsObject.getName());
        return productCmpt.getIpsSrcFile().getIpsPackageFragment().getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

    /**
     * {@inheritDoc}
     */
    protected JavaCodeFragment generateCodeForJavatype() throws CoreException {
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        codeBuilder.classBegin(Modifier.PUBLIC, StringUtil.unqualifiedName(getQualifiedClassName()),
                getSuperClassName(), new String[0]);

        generateConstructor(codeBuilder);
        generateMethodGetProductCmptType(productCmptType, codeBuilder);
        
        List testMethods = generateTestMethods(productCmpt, codeBuilder);
        generateExecuteBusinessLogicMethod(productCmptType, testMethods ,codeBuilder);
        generateExecuteAssertsMethod(productCmptType, testMethods ,codeBuilder);
        
        codeBuilder.classEnd();
        return codeBuilder.getFragment();
    }
    
    /*
     * Code sample:
     * <pre>
     *  public FtPolicyA___2006__01_formulaTest(String formulaTestCaseName) {
     *      super(formulaTestCaseName);
     *  }
     * </pre>
     */
    private void generateConstructor(JavaCodeFragmentBuilder builder) throws CoreException {
        String className = getUnqualifiedClassName();
        appendLocalizedJavaDoc("CONSTRUCTOR", className, getIpsObject(), builder);
        String[] argNames = new String[] { "formulaTestCaseName"};
        String[] argTypes = new String[] { String.class.getName()};
        builder.methodBegin(Modifier.PUBLIC, null, className, argNames, argTypes);
        builder.append("super(formulaTestCaseName);");
        builder.methodEnd();
    }
    
    /*
     * Code sample:
     * 
     * <pre>
     *  public IFtPolicyType getFtPolicyType() {
     *      return (IFtPolicyType) getRepository().getProductComponent("FtPolicyA 2006-01");
     *  }
     *  </pre>
     */
    private void generateMethodGetProductCmptType(IProductCmptType type, JavaCodeFragmentBuilder builder) throws CoreException {
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
     * <pre>
     *  public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    private void generateSignatureGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String genName = productCmptInterfaceBuilder.getQualifiedClassName(type);
        String methodName = policyCmptInterfaceBuilder.getMethodNameGetProductCmpt(type);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, genName, methodName, new String[0], new String[0]);
    }
    
    /*
     * Generates an empty method.
     */
    private void generateExecuteBusinessLogicMethod(IProductCmptType productCmptType,
            List testMethods,
            JavaCodeFragmentBuilder builder) {
        String javaDoc = getJavaDocCommentForOverriddenMethod();
        JavaCodeFragment body = new JavaCodeFragment();
        builder.method(Modifier.PUBLIC, "void", "executeBusinessLogic", EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY, body, javaDoc, ANNOTATION_MODIFIABLE);
    }
    
    /*
     * Code sample:
     * 
     * <pre>
     *  public void executeAsserts(IpsTestResult result) {
     *      IFtPolicyType productCmpt = getFtPolicyType();
     *      testFtPolicyA___2006__01___20070101(productCmpt, result);
     *  }
     * </pre>
     */
    private void generateExecuteAssertsMethod(IProductCmptType productCmptType, List testMethods, JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getJavaDocCommentForOverriddenMethod();
        JavaCodeFragment body = new JavaCodeFragment();
        
        String genClassName = productCmptInterfaceBuilder.getQualifiedClassName(productCmptType);
        body.appendClassName(genClassName);
        body.append(" productCmpt = ");
        body.append(policyCmptInterfaceBuilder.getMethodNameGetProductCmpt(productCmptType));
        body.appendln("();");
        body.appendln();
        
        // generate the test method call for each given test method
        for (Iterator iter = testMethods.iterator(); iter.hasNext();) {
            String testMethodName = (String)iter.next();
            body.append(testMethodName);
            body.append("(");
            body.append("productCmpt");
            body.appendln(", result);");
        }
        
        builder.method(Modifier.PUBLIC, "void", "executeAsserts", new String[] { "result" },
                new String[] { IpsTestResult.class.getName() }, body, javaDoc, ANNOTATION_GENERATED);
    }
    
    /*
     * Code sample:
     * 
     * <pre>
     * public void testFtPolicyA___2006__01___20070101(IProductComponent productCmpt, IpsTestResult result) {
     *      FtPolicyTypeGen productComponentGen = (FtPolicyTypeGen) productCmpt.getGenerationBase(
     *          DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
     *      Object formulaResult = null;
     *      formulaResult = productComponentGen.computeFormulaResult(new Integer(1), null);
     *      assertEquals(Decimal.valueOf("104.1"), formulaResult, result, productComponentGen.toString(), 
     *          "formulaResult.Formeltest");
     * </pre>
     */
    private List generateTestMethods(IProductCmpt productCmpt, JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        ArrayList testMethods = new ArrayList();
        
        IIpsObjectGeneration[] gen = productCmpt.getGenerations();
        for (int i = 0; i < gen.length; i++) {
            appendTestMethodsContentForGeneration((IProductCmptGeneration) gen[i], codeBuilder, testMethods);
        }
        
        return testMethods;
    }

    /*
     * @see generateTestMethods
     */
    private void appendTestMethodsContentForGeneration(IProductCmptGeneration generation, JavaCodeFragmentBuilder builder, ArrayList testMethods) throws CoreException {
        String testMethodName = TEST_METHOD_PREFIX
                + StringUtil.unqualifiedName(productCmptBuilder.getQualifiedClassName(generation));
        appendLocalizedJavaDoc("METHOD_TEST_METHODS", policyCmptInterfaceBuilder
                .getNameForGenerationConcept(productCmptType), getIpsObject(), builder);

        builder.signature(Modifier.PUBLIC, "void", testMethodName, new String[] { "productCmpt", "result" },
                new String[] { IProductComponent.class.getName(), IpsTestResult.class.getName() });  
        builder.openBracket();
        
        JavaCodeFragment body = new JavaCodeFragment();
        String productCmptGenClassName = productCmptGenImplClassBuilder.getQualifiedClassName(productCmptType);
        body.appendClassName(productCmptGenClassName);
        body.append(" productComponentGen = (");
        body.appendClassName(productCmptGenClassName);
        body.append(") productCmpt.getGenerationBase(");
        body.appendClassName(DateUtil.class.getName());
        body.append(".parseIsoDateStringToGregorianCalendar(\"");
        body.append(DateUtil.gregorianCalendarToIsoDateString(generation.getValidFrom()));
        body.appendln("\"));");
        body.appendln();
        body.appendln("Object formulaResult = null;");
        IConfigElement[] formulas = generation.getConfigElements(ConfigElementType.FORMULA);
        for (int i = 0; i < formulas.length; i++) {
            IFormulaTestCase[] testCases = formulas[i].getFormulaTestCases();
            for (int j = 0; j < testCases.length; j++) {
                // append compute method call
                IAttribute attribute = formulas[i].findPcTypeAttribute();
                body.append("formulaResult = productComponentGen.");
                body.append(productCmptGenImplClassBuilder.getMethodNameComputeValue(attribute));
                body.append("(");

                // append the method parameters in the correct order
                Parameter[] params = attribute.getFormulaParameters();
                Map identifierNameIdx = new HashMap(params.length);
                for (int k = 0; k < params.length; k++) {
                    identifierNameIdx.put(new Integer(params[k].getIndex()), params[k].getName());
                }
                List orderedInputValue = new ArrayList(params.length);
                for (int k = 0; k < params.length; k++) {
                    String identifier = (String) identifierNameIdx.get(new Integer(k));
                    orderedInputValue.add(testCases[j].getFormulaTestInputValue(identifier));
                }
                for (int k = 0; k < orderedInputValue.size(); k++) {
                    if (k > 0) {
                        body.append(", ");
                    }
                    IFormulaTestInputValue inputValue = (IFormulaTestInputValue)orderedInputValue.get(k);
                    if (inputValue == null){
                        body.append("null");
                    } else {
                        DatatypeHelper inputValueHelper = project.getDatatypeHelper(inputValue.findDatatypeOfFormulaParameter());
                        body.append(inputValueHelper.newInstance(inputValue.getValue()));
                    }
                }
                body.appendln(");");
                
                // append assert method call
                body.append("assertEquals(");
                DatatypeHelper valueHelper = project.getDatatypeHelper(formulas[i].findPcTypeAttribute().findDatatype());
                body.append(valueHelper.newInstance(testCases[j].getExpectedResult()));
                body.append(", formulaResult, result, productComponentGen.toString(), \"");
                body.append(formulas[i].getName());
                body.append(".");
                body.append(testCases[j].getName());
                body.append("\"");
                body.appendln(");");
            }
        }
        builder.append(body);
        builder.closeBracket();
        
        testMethods.add(testMethodName);
    }
}