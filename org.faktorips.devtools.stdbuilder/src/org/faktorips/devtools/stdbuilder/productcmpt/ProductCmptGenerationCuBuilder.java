package org.faktorips.devtools.stdbuilder.productcmpt;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.stdbuilder.pctype.ProductCmptImplCuBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.RuntimeRepository;
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

    // private final static String KIND_PC_IMPL = "pcimplementation";

    private ProductCmptImplCuBuilder productCmptImplBuilder;

    // the product component generation sourcecode is generated for.
    private IProductCmptGeneration generation;

    /**
     * Constructs a new builder.
     */
    public ProductCmptGenerationCuBuilder(IJavaPackageStructure packageStructure, String kindId)
            throws CoreException {
        super(packageStructure, kindId, new LocalizedStringsSet(
                ProductCmptGenerationCuBuilder.class));
    }

    public void setProductCmptImplBuilder(ProductCmptImplCuBuilder productCmptImplBuilder) {
        this.productCmptImplBuilder = productCmptImplBuilder;
    }

    private void checkIfDependentBuildersSet() {
        String builderName = null;

        if (productCmptImplBuilder == null) {
            builderName = ProductCmptImplCuBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }

    private IProductCmpt getProductCmpt() {
        return (IProductCmpt)getIpsObject();
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {

        if (!ipsSrcFile.exists() || !ipsSrcFile.isContentParsable()) {
            return super.getUnqualifiedClassName(ipsSrcFile);
        }
        IProductCmpt lProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (!lProductCmpt.javaTypeMustBeGenerated()) {
            IPolicyCmptType pcType = lProductCmpt.findPolicyCmptType();
            if (pcType == null) {
                return super.getUnqualifiedClassName(ipsSrcFile);
            }
            return productCmptImplBuilder.getUnqualifiedClassName(lProductCmpt.findPolicyCmptType()
                    .getIpsSrcFile());
        }
        return super.getUnqualifiedClassName(ipsSrcFile);
    }

    protected void generateInternal() throws CoreException {
        checkIfDependentBuildersSet();
        if (!getProductCmpt().javaTypeMustBeGenerated()) {
            cancelGeneration();
            return;
        }

        IIpsObjectGeneration[] generations = getProductCmpt().getGenerations();
        if (generations.length == 0) {
            return;
        }
        generation = (IProductCmptGeneration)generations[0];
        if (generation.validate().containsErrorMsg()) {
            return;
        }
        IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType();
        if (pcType == null) {
            return;
        }

        getJavaCodeFragementBuilder().classBegin(Modifier.PUBLIC, getUnqualifiedClassName(),
            productCmptImplBuilder.getQualifiedClassName(pcType.getIpsSrcFile()), new String[0]);
        buildConstructor();
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.FORMULA);
        for (int i = 0; i < elements.length; i++) {
            buildComputationMethod(elements[i]);
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
        String javaDoc = getLocalizedText(CONSTRUCTOR_JAVADOC);
        String[] argNames = new String[] { "repository", "qName", "policyComponentType" };
        String[] argClassNames = new String[] { RuntimeRepository.class.getName(),
                String.class.getName(), Class.class.getName() };
        JavaCodeFragment body = new JavaCodeFragment(
                "super(repository, qName, policyComponentType);");
        getJavaCodeFragementBuilder().method(Modifier.PUBLIC, null, className, argNames,
            argClassNames, body, javaDoc);
    }

    // Duplicate method in PoicyCmptTypeImplCuBuilder
    private String getPolicyCmptImplComputeMethodName(IAttribute a) {
        return "compute" + StringUtils.capitalise(a.getName());
    }

    /*
     * Generates the method to compute a value as specified by a formula configuration element and
     */
    private void buildComputationMethod(IConfigElement formulaElement) {
        try {
            IAttribute attribute = formulaElement.findPcTypeAttribute();
            Datatype datatype = attribute.getIpsProject().findDatatype(attribute.getDatatype());
            String methodName = getPolicyCmptImplComputeMethodName(attribute);
            String javaDoc = getLocalizedText(COMPUTE_METHOD_JAVADOC, StringUtils
                    .capitalise(attribute.getName()));
            JavaCodeFragment body = new JavaCodeFragment();
            body.append("return ");
            body.append(compileFormulaToJava(formulaElement, attribute));
            body.append(';');
            getJavaCodeFragementBuilder().method(
                Modifier.PUBLIC,
                datatype.getJavaClassName(),
                methodName,
                BuilderHelper.extractParameterNames(attribute.getFormulaParameters()),
                BuilderHelper.transformParameterTypesToJavaClassNames(attribute.getIpsProject(),
                    attribute.getFormulaParameters()), body, javaDoc);
        } catch (CoreException e) {
            addToBuildStatus(new IpsStatus("Error building compute method for " + formulaElement, e));
        }
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

    /**
     * Overridden.
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

}
