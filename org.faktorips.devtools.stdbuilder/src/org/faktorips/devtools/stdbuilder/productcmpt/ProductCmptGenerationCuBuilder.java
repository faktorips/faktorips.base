package org.faktorips.devtools.stdbuilder.productcmpt;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
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
public class ProductCmptGenerationCuBuilder extends SimpleJavaSourceFileBuilder {

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
            IProductCmptGeneration generation,
            IJavaPackageStructure packageStructure, 
            String kindId)
            throws CoreException {
        super(packageStructure, kindId, new LocalizedStringsSet(
                ProductCmptGenerationCuBuilder.class));
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

    protected void generateInternal() throws CoreException {
        if (generation.validate().containsErrorMsg()) {
            return;
        }
        IPolicyCmptType pcType = generation.getProductCmpt().findPolicyCmptType();
        if (pcType == null) {
            return;
        }
        getJavaCodeFragementBuilder().classBegin(Modifier.PUBLIC, getUnqualifiedClassName(),
            productCmptGenImplBuilder.getQualifiedClassName(pcType.getIpsSrcFile()), new String[0]);
        buildConstructor();
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.FORMULA);
        for (int i = 0; i < elements.length; i++) {
            try {
                generateMethodComputeValue(elements[i]);
            } catch (Exception e) {
                addToBuildStatus(new IpsStatus("Error generating code for " + elements[i], e));
            }
        }
        getJavaCodeFragementBuilder().classEnd();
    }
    
    /*
     * Generates the constructor. <p> Example: <p><pre> public MotorPolicyPk0(RuntimeRepository
     * repository, String qName, Class policyComponentType) { super(registry, qName,
     * policyComponentType); } </pre>
     */
    private void buildConstructor() throws CoreException {
        String className = getUnqualifiedClassName();
        String javaDoc = getLocalizedText(getIpsSrcFile(), CONSTRUCTOR_JAVADOC);
        String[] argNames = new String[] { "productCmpt" };
        String[] argClassNames = new String[] { productCmptImplBuilder.getQualifiedClassName(generation.getProductCmpt().findProductCmptType()) };
        JavaCodeFragment body = new JavaCodeFragment(
                "super(productCmpt);");
        getJavaCodeFragementBuilder().method(Modifier.PUBLIC, null, className, argNames,
            argClassNames, body, javaDoc);
    }

    /*
     * Generates the method to compute a value as specified by a formula configuration element and
     */
    private void generateMethodComputeValue(IConfigElement formulaElement) throws CoreException {
        JavaCodeFragmentBuilder builder = getJavaCodeFragementBuilder();
        IAttribute attribute = formulaElement.findPcTypeAttribute();
        Datatype datatype = attribute.getIpsProject().findDatatype(attribute.getDatatype());
        DatatypeHelper datatypeHelper = attribute.getIpsProject().getDatatypeHelper(datatype);

        String javaDoc = getLocalizedText(formulaElement, COMPUTE_METHOD_JAVADOC, StringUtils
                .capitalise(attribute.getName()));
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        productCmptGenImplBuilder.generateSignatureComputeValue(attribute, datatypeHelper, Modifier.PUBLIC, builder);
        builder.openBracket();
        builder.append("return ");
        builder.append(compileFormulaToJava(formulaElement, attribute));
        builder.appendln(";");
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
                return conversion.getConversionCode(result.getDatatype(), attributeDatatype, result.getCodeFragment());
            }
            addToBuildStatus(new IpsStatus(
                    "The expression compiler reported errors while compiling the formula "
                            + formula + " of config element " + formulaElement + "."));
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

}
