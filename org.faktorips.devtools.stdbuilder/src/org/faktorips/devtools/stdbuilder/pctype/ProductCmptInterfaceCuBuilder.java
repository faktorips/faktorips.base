package org.faktorips.devtools.stdbuilder.pctype;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.runtime.ProductComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class ProductCmptInterfaceCuBuilder extends BaseJavaSourceFileBuilder {

    private final static String ATTRIBUTE_INTERFACE_GETTER_JAVADOC = "ATTRIBUTE_INTERFACE_GETTER_JAVADOC";
    private final static String ATTRIBUTE_INTERFACE_COMPUTE_JAVADOC = "ATTRIBUTE_INTERFACE_COMPUTE_JAVADOC";
    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_CREATE_POLICY_CMPT_METHOD = "JAVA_CREATE_POLICY_CMPT_METHOD";

    private PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder;

    public ProductCmptInterfaceCuBuilder(IJavaPackageStructure packageStructure, String kindId) throws CoreException {
        super(packageStructure, kindId,
                new LocalizedStringsSet(ProductCmptInterfaceCuBuilder.class));
        setMergeEnabled(true);
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    private void checkIfDependentBuildersSet() {
        String builderName = null;

        if (policyCmptTypeInterfaceBuilder == null) {
            builderName = PolicyCmptTypeInterfaceCuBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.builder.JavaSourceFileBuilder#getUnqualifiedClassName(org.faktorips.devtools.core.model.IIpsObject)
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "Pk";
    }

    /**
     * Extends the visibility of the super class method.
     * 
     * @see org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder#getJavaCodeFragementBuilder()
     */
    public JavaCodeFragmentBuilder getJavaCodeFragementBuilder() {
        return super.getJavaCodeFragementBuilder();
    }

    IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder#generateInternal()
     */
    protected void generateInternal() throws CoreException {
        checkIfDependentBuildersSet();
        getJavaCodeFragementBuilder().javaDoc(null, ANNOTATION_GENERATED);
        getJavaCodeFragementBuilder().interfaceBegin(getUnqualifiedClassName(), getSupertypeName());
        getJavaCodeFragementBuilder().appendln();

        ProductCmptInterfaceRelationBuilder relationBuilder = new ProductCmptInterfaceRelationBuilder(
                this);
        if (!getPolicyCmptType().isAbstract()) {
            buildCreateMethod();
        }
        buildAttributes(getPolicyCmptType().getAttributes());
        relationBuilder.buildRelations();
        /*
         * Im alten code werden die container relations mit dieser Methode gebaut. Dies führt dazu
         * dass unter bestimmten Umständen Methoden doppelt generiert werden. Ich habe nicht
         * verstanden warum das im alten Code nicht der Fall ist. Jedenfalls wird mit dieser Methode
         * im alten Code unnötige Methoden erzeugt die schon im Superinterface deklariert wurden.
         * Sieht also so aus als benötigt man den Aufruf nicht.
         */
        // relationBuilder.buildContainerRelations();
        getJavaCodeFragementBuilder().classEnd();
    }

    private String getSupertypeName() throws CoreException {
        String javaSupertype = ProductComponent.class.getName();
        if (StringUtils.isNotEmpty(getPolicyCmptType().getSupertype())) {
            IPolicyCmptType supertype = getPolicyCmptType().getIpsProject().findPolicyCmptType(
                getPolicyCmptType().getSupertype());
            javaSupertype = supertype == null ? javaSupertype : getQualifiedClassName(supertype
                    .getIpsSrcFile());
        }
        return javaSupertype;

    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#isBuilderFor(IIpsObject)
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    protected void buildAttribute(IAttribute a) throws CoreException {
        if (a.isProductRelevant()) {
            Datatype datatype = getPolicyCmptType().getIpsProject().findDatatype(a.getDatatype());
            DatatypeHelper helper = getPolicyCmptType().getIpsProject().getDatatypeHelper(datatype);
            if (helper == null) {
                throw new CoreException(new IpsStatus("Error building attribute " + a.getName()
                        + " of " + getPolicyCmptType() + ". No datatype helper found for datatype "
                        + datatype));
            }
            if (a.getAttributeType() == AttributeType.COMPUTED
                    || a.getAttributeType() == AttributeType.DERIVED) {
                createAttributeComputeDeclaration(a, datatype);
            } else {
                createAttributeGetterDeclaration(a, datatype);
            }
            buildAttributeValueSetDeclaration(a, datatype, helper);
        }
    }

    private void buildAttributeValueSetDeclaration(IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        if (a.getValueSet().isAllValues()) {
            return;
        }

        String javaDocMax = getLocalizedText(JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
        if (a.getValueSet().isRange()) {
            getJavaCodeFragementBuilder().methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT,
                helper.getRangeJavaClassName(), getProductInterfaceGetMaxValueSetMethodName(a),
                new String[0], new String[0], javaDocMax, ANNOTATION_GENERATED);
        } else {
            getJavaCodeFragementBuilder().methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT,
                datatype.getJavaClassName() + "[]", getProductInterfaceGetMaxValueSetMethodName(a),
                new String[0], new String[0], javaDocMax, ANNOTATION_GENERATED);
        }
        getJavaCodeFragementBuilder().append(';');
    }

    private String getProductInterfaceGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @param datatype
     * @throws CoreException
     * @throws JavaModelException
     */
    private void createAttributeComputeDeclaration(IAttribute a, Datatype datatype)
            throws JavaModelException, CoreException {
        String methodName = "compute" + StringUtils.capitalise(a.getName());

        String javaDoc = getLocalizedText(ATTRIBUTE_INTERFACE_COMPUTE_JAVADOC, a.getName());

        Parameter[] parameters = a.getFormulaParameters();
        getJavaCodeFragementBuilder().methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT,
            datatype.getJavaClassName(), methodName,
            BuilderHelper.extractParameterNames(parameters),
            BuilderHelper.transformParameterTypesToJavaClassNames(a.getIpsProject(), parameters),
            javaDoc, ANNOTATION_GENERATED);
        getJavaCodeFragementBuilder().append(';');
    }

    /**
     * @param a
     * @param datatype
     */
    private void createAttributeGetterDeclaration(IAttribute a, Datatype datatype)
            throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = getPcInterfaceGetDefaultValueMethodName(a);
        } else {
            methodName = getPcInterfaceGetValueMethodName(a);
        }
        String javaDoc = getLocalizedText(ATTRIBUTE_INTERFACE_GETTER_JAVADOC, a.getName());

        getJavaCodeFragementBuilder().methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT,
            datatype.getJavaClassName(), methodName, new String[0], new String[0], javaDoc,
            ANNOTATION_GENERATED);
        getJavaCodeFragementBuilder().append(';');
    }

    private void buildCreateMethod() throws CoreException {
        String javaDoc = getLocalizedText(JAVA_CREATE_POLICY_CMPT_METHOD);
        getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            policyCmptTypeInterfaceBuilder.getQualifiedClassName(getPolicyCmptType()
                    .getIpsSrcFile()),
            "create" + StringUtils.capitalise(getPolicyCmptType().getName()), new String[0],
            new String[0], javaDoc, ANNOTATION_GENERATED);
        getJavaCodeFragementBuilder().append(';');
        return;
    }

    // duplicate in ProductCmptImplCuBuilder and PolicyCmptTypeImplCuBuilder
    private String getPcInterfaceGetDefaultValueMethodName(IAttribute a) {
        return "getVorgabewert" + StringUtils.capitalise(a.getName());
    }

    // duplicate in ProductCmptImplCuBuilder
    private String getPcInterfaceGetValueMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }
}