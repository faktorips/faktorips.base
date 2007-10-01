/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.lang.reflect.Modifier;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * Generates the compilation unit that represents the product component generation. Note that only
 * for product component's that contain a config element of type formula a Java compilation unit is
 * generated. This is neccessary as the formula is compiled into Java sourcecode and this Java
 * sourcecode is placed in the compilation unit generated for a product component's generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationCuBuilder extends DefaultJavaSourceFileBuilder {

    // property key for the constructor's Javadoc.
    private final static String CONSTRUCTOR_JAVADOC = "CONSTRUCTOR_JAVADOC"; //$NON-NLS-1$

    // the product component generation sourcecode is generated for.
    private IProductCmptGeneration generation;

    // builders needed
    private ProductCmptImplClassBuilder productCmptImplBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplBuilder;
    
    /**
     * Constructs a new builder.
     */
    public ProductCmptGenerationCuBuilder(
            IIpsArtefactBuilderSet builderSet, 
            String kindId){
        super(builderSet, kindId, new LocalizedStringsSet(
                ProductCmptGenerationCuBuilder.class));
    }
    
    public void setProductCmptGeneration(IProductCmptGeneration generation){
        ArgumentCheck.notNull(generation);
        this.generation = generation;
    }
    
    public void setProductCmptImplBuilder(ProductCmptImplClassBuilder builder) {
        this.productCmptImplBuilder = builder;
    }

    public void setProductCmptGenImplBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplBuilder = builder;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return true;
    }
    
    public IProductCmptType getProductCmptType() throws CoreException {
        return generation.getProductCmpt().findProductCmptType(getIpsProject());
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForJavatype() throws CoreException {
        if(generation == null){
            addToBuildStatus(new IpsStatus("The generation needs to be set for this " + ProductCmptGenerationCuBuilder.class)); //$NON-NLS-1$
            return;
        }
        IProductCmptType pcType = generation.getProductCmpt().findProductCmptType(getIpsProject());
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getUnqualifiedClassName());
        mainSection.setSuperClass(productCmptGenImplBuilder.getQualifiedClassName(pcType.getIpsSrcFile()));

        buildConstructor(mainSection.getConstructorSectionBuilder());
        IFormula[] formulas = generation.getFormulas();
        for (int i = 0; i < formulas.length; i++) {
            try {
                if(!formulas[i].validate().containsErrorMsg()){
                    generateMethodForFormula(formulas[i], mainSection.getMethodSectionBuilder());
                }
            } catch (Exception e) {
                addToBuildStatus(new IpsStatus("Error generating code for " + formulas[i], e)); //$NON-NLS-1$
            }
        }
    }

    /*
     * Generates the constructor. 
     * <p> 
     * Example:
     * <p>
     * <pre> 
     * public MotorPolicyPk0(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName,
     *     policyComponentType); 
     * } 
     * </pre>
     */
    private void buildConstructor(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        Locale language = getLanguageUsedInGeneratedSourceCode(generation);
        String genName = getChangesInTimeNamingConvention(generation).getGenerationConceptNameSingular(language);
        String javaDoc = getLocalizedText(getIpsSrcFile(), CONSTRUCTOR_JAVADOC, genName);
        String className = getUnqualifiedClassName();
        String[] argNames = new String[] { "productCmpt" }; //$NON-NLS-1$
        String[] argClassNames = new String[] { productCmptImplBuilder.getQualifiedClassName(generation.getProductCmpt().findProductCmptType(getIpsProject())) };
        JavaCodeFragment body = new JavaCodeFragment(
                "super(productCmpt);"); //$NON-NLS-1$
        codeBuilder.method(Modifier.PUBLIC, null, className, argNames,
            argClassNames, body, javaDoc);
    }

    /*
     * Generates the method to compute a value as specified by a formula configuration element and
     */
    private void generateMethodForFormula(IFormula formula, JavaCodeFragmentBuilder builder) throws CoreException {
        IProductCmptTypeMethod method = formula.findFormulaSignature(getIpsProject());
        if(method.validate().containsErrorMsg()){
            return;   
        }
        ValueDatatype datatype = (ValueDatatype)method.findDatatype(getIpsProject());
        DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        
        productCmptGenImplBuilder.generateSignatureForFormula(method, datatypeHelper, Modifier.PUBLIC, true, builder);
        builder.openBracket();
        builder.append("try {"); //$NON-NLS-1$
        builder.append("return "); //$NON-NLS-1$
        builder.append(compileFormulaToJava(formula, method));
        builder.appendln(";"); //$NON-NLS-1$
        builder.append("} catch (Exception e) {"); //$NON-NLS-1$
        builder.appendClassName(StringBuffer.class);
        builder.append(" parameterValues=new StringBuffer();"); //$NON-NLS-1$
        IParameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i>0) {
                builder.append("parameterValues.append(\", \");"); //$NON-NLS-1$
            }
            builder.append("parameterValues.append(\"" + parameters[i].getName() + "=\");"); //$NON-NLS-1$ //$NON-NLS-2$
            ValueDatatype valuetype = getIpsProject().findValueDatatype(parameters[i].getDatatype());
            if (valuetype!=null && valuetype.isPrimitive()) { // optimitation: we search for value types only as only those can be primitives!
                builder.append("parameterValues.append(" + parameters[i].getName() + ");"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                builder.append("parameterValues.append(" + parameters[i].getName() + " == null ? \"null\" : " + parameters[i].getName() + ".toString());"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        builder.append("throw new "); //$NON-NLS-1$
        builder.appendClassName(FormulaExecutionException.class);
        builder.append("(toString(), ");   //$NON-NLS-1$
        builder.appendQuoted(StringUtils.escape(formula.getExpression()));
        builder.appendln(", parameterValues.toString(), e);"); //$NON-NLS-1$
        builder.appendln("}"); //$NON-NLS-1$
        
        builder.closeBracket();
    }
    
    private JavaCodeFragment compileFormulaToJava(IFormula formula, IProductCmptTypeMethod formulaSignature) {
        String expression = formula.getExpression();
        if (StringUtils.isEmpty(expression)) {
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("null"); //$NON-NLS-1$
            return fragment;
        }
        try {
            ExprCompiler compiler = formula.getExprCompiler(getIpsProject());
            CompilationResult result = compiler.compile(expression);
            if (result.successfull()) {
                Datatype attributeDatatype = formulaSignature.findDatatype(getIpsProject());
                if (result.getDatatype().equals(attributeDatatype)) {
                    return result.getCodeFragment();
                }
                ConversionCodeGenerator conversion = compiler.getConversionCodeGenerator();
                JavaCodeFragment convertedFrag = conversion.getConversionCode(result.getDatatype(), attributeDatatype, result.getCodeFragment());
                if(convertedFrag == null){
                    return new JavaCodeFragment("// Unable to convert the expression \"" +  //$NON-NLS-1$
                            result.getCodeFragment().getSourcecode() + "\" of datatype " + result.getDatatype() +  //$NON-NLS-1$
                            " to the datatype " + attributeDatatype); //$NON-NLS-1$
                }
                return convertedFrag;
            }
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment
                    .appendln("// The expression compiler reported the following errors while compiling the formula:"); //$NON-NLS-1$
            fragment.append("// "); //$NON-NLS-1$
            fragment.appendln(expression);
            MessageList messages = result.getMessages();
            for (int i = 0; i < messages.getNoOfMessages(); i++) {
                fragment.append("// "); //$NON-NLS-1$
                fragment.append(messages.getText());
            }
            return fragment;
        } catch (CoreException e) {
            addToBuildStatus(new IpsStatus("Error compiling formula " + formula.getExpression() //$NON-NLS-1$
                    + " of config element " + formula+ ".", e)); //$NON-NLS-1$ //$NON-NLS-2$
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.appendln("// An excpetion occured while compiling the following formula:"); //$NON-NLS-1$
            fragment.append("// "); //$NON-NLS-1$
            fragment.appendln(expression);
            fragment.append("// See the error log for details."); //$NON-NLS-1$
            return fragment;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Returns true.
     */
    public boolean buildsDerivedArtefacts() {
        return true;
    }
}
