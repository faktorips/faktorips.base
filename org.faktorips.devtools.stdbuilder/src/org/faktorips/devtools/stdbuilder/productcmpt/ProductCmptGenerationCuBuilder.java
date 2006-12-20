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
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
    private final static String CONSTRUCTOR_JAVADOC = "CONSTRUCTOR_JAVADOC";

    // property key for the compute method Javadoc.
    private final static String COMPUTE_METHOD_JAVADOC = "COMPUTE_METHOD_JAVADOC";

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
        return generation.getProductCmpt().findProductCmptType();
    }
    
    /**
     * {@inheritDoc}
     */
    protected JavaCodeFragment generateCodeForJavatype() throws CoreException {
        if(generation == null){
            addToBuildStatus(new IpsStatus("The generation needs to be set for this " + ProductCmptGenerationCuBuilder.class));
            return new JavaCodeFragment();
        }
        IPolicyCmptType pcType = generation.getProductCmpt().findPolicyCmptType();
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        codeBuilder.classBegin(Modifier.PUBLIC, getUnqualifiedClassName(),
                productCmptGenImplBuilder.getQualifiedClassName(pcType.getIpsSrcFile()), new String[0]);
        buildConstructor(codeBuilder);
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.FORMULA);
        for (int i = 0; i < elements.length; i++) {
            try {
                if(!elements[i].validate().containsErrorMsg()){
                    generateMethodComputeValue(elements[i], codeBuilder);
                }
            } catch (Exception e) {
                addToBuildStatus(new IpsStatus("Error generating code for " + elements[i], e));
            }
        }
        codeBuilder.classEnd();
        return codeBuilder.getFragment();
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
        String[] argNames = new String[] { "productCmpt" };
        String[] argClassNames = new String[] { productCmptImplBuilder.getQualifiedClassName(generation.getProductCmpt().findProductCmptType()) };
        JavaCodeFragment body = new JavaCodeFragment(
                "super(productCmpt);");
        codeBuilder.method(Modifier.PUBLIC, null, className, argNames,
            argClassNames, body, javaDoc);
    }

    /*
     * Generates the method to compute a value as specified by a formula configuration element and
     */
    private void generateMethodComputeValue(IConfigElement formulaElement, JavaCodeFragmentBuilder builder) throws CoreException {
        IAttribute attribute = formulaElement.findPcTypeAttribute();
        if(attribute.validate().containsErrorMsg()){
            return;   
        }

        ValueDatatype datatype = attribute.findDatatype();
        DatatypeHelper datatypeHelper = attribute.getIpsProject().getDatatypeHelper(datatype);

        String javaDoc = getLocalizedText(formulaElement, COMPUTE_METHOD_JAVADOC, StringUtils
                .capitalise(attribute.getName()));
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        productCmptGenImplBuilder.generateSignatureComputeValue(attribute, datatypeHelper, Modifier.PUBLIC, true, builder);
        builder.openBracket();
        builder.append("try {");
        builder.append("return ");
        builder.append(compileFormulaToJava(formulaElement, attribute));
        builder.appendln(";");
        builder.append("} catch (Exception e) {");
        builder.appendClassName(StringBuffer.class);
        builder.append(" parameterValues=new StringBuffer();");
        Parameter[] parameters = attribute.getFormulaParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i>0) {
                builder.append("parameterValues.append(\", \");");
            }
            builder.append("parameterValues.append(\"" + parameters[i].getName() + "=\");");
            ValueDatatype valuetype = attribute.getIpsProject().findValueDatatype(parameters[i].getDatatype());
            if (valuetype!=null && valuetype.isPrimitive()) { // optimitation: we search for value types only as only those can be primitives!
                builder.append("parameterValues.append(" + parameters[i].getName() + ");");
            } else {
                builder.append("parameterValues.append(" + parameters[i].getName() + " == null ? \"null\" : " + parameters[i].getName() + ".toString());");
            }
        }
        builder.append("throw new ");
        builder.appendClassName(FormulaExecutionException.class);
        builder.append("(toString(), ");  
        builder.appendQuoted(StringUtils.escape(formulaElement.getValue()));
        builder.appendln(", parameterValues.toString(), e);");
        builder.appendln("}");
        
        builder.closeBracket();
    }
    
    private JavaCodeFragment compileFormulaToJava(IConfigElement formulaElement, IAttribute attribute) {
        String formula = formulaElement.getValue();
        if (StringUtils.isEmpty(formula)) {
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("null");
            return fragment;
        }
        try {
            ExprCompiler compiler = formulaElement.getExprCompiler();
            CompilationResult result = compiler.compile(formula);
            if (result.successfull()) {
                Datatype attributeDatatype = attribute.findDatatype();
                if (result.getDatatype().equals(attributeDatatype)) {
                    return result.getCodeFragment();
                }
                ConversionCodeGenerator conversion = compiler.getConversionCodeGenerator();
                JavaCodeFragment convertedFrag = conversion.getConversionCode(result.getDatatype(), attributeDatatype, result.getCodeFragment());
                if(convertedFrag == null){
                    return new JavaCodeFragment("// Unable to convert the expression \"" + 
                            result.getCodeFragment().getSourcecode() + "\" of datatype " + result.getDatatype() + 
                            " to the datatype " + attributeDatatype);
                }
                return convertedFrag;
            }
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment
                    .appendln("// The expression compiler reported the following errors while compiling the formula:");
            fragment.append("// ");
            fragment.appendln(formula);
            MessageList messages = result.getMessages();
            for (int i = 0; i < messages.getNoOfMessages(); i++) {
                fragment.append("// ");
                fragment.append(messages.getText());
            }
            return fragment;
        } catch (CoreException e) {
            addToBuildStatus(new IpsStatus("Error compiling formula " + formulaElement.getValue()
                    + " of config element " + formulaElement + ".", e));
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.appendln("// An excpetion occured while compiling the following formula:");
            fragment.append("// ");
            fragment.appendln(formula);
            fragment.append("// See the error log for details.");
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
