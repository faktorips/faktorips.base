package org.faktorips.devtools.stdbuilder.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.stdbuilder.Util;
import org.faktorips.devtools.stdbuilder.backup.ProductCmptInterfaceCuBuilder;
import org.faktorips.runtime.IPolicyComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class PolicyCmptTypeInterfaceCuBuilder extends AbstractPcTypeBuilder {

    private final static String ATTRIBUTE_FIELD_COMMENT = "ATTRIBUTE_FIELD_COMMENT";

    private final static String ATTRIBUTE_INTERFACE_GETTER_JAVADOC = "ATTRIBUTE_INTERFACE_GETTER_JAVADOC";
    private final static String ATTRIBUTE_INTERFACE_SETTER_JAVADOC = "ATTRIBUTE_INTERFACE_SETTER_JAVADOC";

    private final static String ATTRIBUTE_MAX_VALUESET_JAVADOC = "ATTRIBUTE_MAX_VALUESET_JAVADOC";
    private final static String ATTRIBUTE_VALUESET_JAVADOC = "ATTRIBUTE_VALUESET_JAVADOC";

    private final static String PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC = "PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC";
    private final static String PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC = "PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC";
    private final static String PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC = "PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC";
    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private ProductCmptInterfaceCuBuilder productCmptInterfaceBuilder;

    public PolicyCmptTypeInterfaceCuBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(PolicyCmptTypeInterfaceCuBuilder.class));
        setMergeEnabled(true);
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceCuBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return StringUtils.capitalise(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()));
    }

    private void createPkGetter(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC);
        String pcCmptInterfaceQualifiedName = productCmptInterfaceBuilder.getQualifiedClassName(getPcType()
                .getIpsSrcFile());
        String pcCmptInterfaceUnqualifiedName = productCmptInterfaceBuilder.getUnqualifiedClassName(getPcType()
                .getIpsSrcFile());

        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                pcCmptInterfaceQualifiedName, "get" + pcCmptInterfaceUnqualifiedName, new String[0], new String[0],
                javaDoc, ANNOTATION_GENERATED);
        methodsBuilder.append(";");
    }

    private String getPolicyAttributeFieldName(IAttribute a) {
        return a.getName();
    }

    private void createAttributeField(JavaCodeFragmentBuilder memberVarsBuilder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        JavaCodeFragment initialValueExpression = helper.newInstance(a.getDefaultValue());
        String comment = getLocalizedText(ATTRIBUTE_FIELD_COMMENT, a.getName());

        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(
                java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                        | java.lang.reflect.Modifier.STATIC, datatype.getJavaClassName(),
                getPolicyAttributeFieldName(a), initialValueExpression);

    }

    private String getPolicyCmptInterfaceGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private String getPolicyCmptInterfaceGetValueSetMethodName(IAttribute a) {
        return "getWertebereich" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @throws CoreException
     */
    private void createAttributeValueSetDeclaration(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        // TODO: Kommentare der Methoden in die Resourcendatei auslageern !
        if (a.getValueSet() != null && !a.getValueSet().isAllValues()) {
            String methodNameMax = getPolicyCmptInterfaceGetMaxValueSetMethodName(a);
            String methodName = getPolicyCmptInterfaceGetValueSetMethodName(a);
            String javaDocMax = getLocalizedText(JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
            String javaDoc = getLocalizedText(JAVA_GETTER_METHOD_VALUESET, a.getName());
            if (a.getValueSet().isRange()) {
                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        helper.getRangeJavaClassName(), methodNameMax, new String[0], new String[0], javaDocMax,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");

                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        helper.getRangeJavaClassName(), methodName, new String[0], new String[0], javaDoc,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");
            } else { // a.getValueSet().isEnum()

                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        datatype.getJavaClassName() + "[]", methodNameMax, new String[0], new String[0], javaDocMax,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");

                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        datatype.getJavaClassName() + "[]", methodName, new String[0], new String[0], javaDoc,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");
            }
        }
    }

    private String getPolicyCmptInterfaceSetMethodName(IAttribute a) {
        return "set" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @param datatype
     */
    private void createAttributeSetterDeclaration(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype) throws CoreException {
        String methodName = getPolicyCmptInterfaceSetMethodName(a);
        String javaDoc = getLocalizedText(ATTRIBUTE_INTERFACE_SETTER_JAVADOC, a.getName());

        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                Datatype.VOID.getJavaClassName(), methodName, new String[] { "newValue" }, new String[] { datatype
                        .getJavaClassName() }, javaDoc, ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptInterfaceGetMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @param datatype
     */
    private void createAttributeGetterDeclaration(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype) throws CoreException {
        String methodName = getPolicyCmptInterfaceGetMethodName(a);
        String javaDoc = getLocalizedText(ATTRIBUTE_INTERFACE_GETTER_JAVADOC, a.getName());

        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT, datatype
                .getJavaClassName(), methodName, new String[0], new String[0], javaDoc, ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private void buildMethods(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IMethod[] methods = getPcType().getMethods();
        for (int i = methods.length - 1; i >= 0; i--) {
            if (!methods[i].validate().containsErrorMsg()) {
                try {
                    buildMethod(methodsBuilder, methods[i]);
                } catch (Exception e) {
                    addToBuildStatus(new IpsStatus(IStatus.ERROR, "Error building method " + methods[i].getName()
                            + " of " + getPcType(), e));
                }
            }
        }
    }

    private String getPolicyCmptInterfacePublishedInterfaceMethodName(IMethod m) {
        return m.getName();
    }

    private void buildMethod(JavaCodeFragmentBuilder methodsBuilder, IMethod method) throws CoreException {
        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        Datatype datatype = getPcType().getIpsProject().findDatatype(method.getDatatype());
        String methodName = getPolicyCmptInterfacePublishedInterfaceMethodName(method);

        methodsBuilder.methodBegin(java.lang.reflect.Modifier.ABSTRACT | Util.getJavaModifier(method.getModifier()),
                datatype.getJavaClassName(), methodName, method.getParameterNames(), BuilderHelper
                        .transformParameterTypesToJavaClassNames(method.getIpsProject(), method.getParameters()),
                method.getDescription(), ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptInterfaceValueSetFiedName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetField(JavaCodeFragmentBuilder memberVarsBuilder, IAttribute a, Datatype datatype, DatatypeHelper helper)
            throws CoreException {
        String fieldName = getPolicyCmptInterfaceValueSetFiedName(a);
        String dataTypeValueSet;
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();

        if (a.getValueSet().isRange()) {
            dataTypeValueSet = helper.getRangeJavaClassName();
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(helper.getRangeJavaClassName());
            initialValueExpression.append("( ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet()).getLowerBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet()).getUpperBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet()).getStep()));
            initialValueExpression.append(" ) ");
        } else {
            dataTypeValueSet = datatype.getJavaClassName() + "[]";
            initialValueExpression = new JavaCodeFragment();
            String[] elements = ((EnumValueSet)a.getValueSet()).getElements();
            initialValueExpression.append("{ ");
            for (int i = 0; i < elements.length; i++) {
                if (i > 0) {
                    initialValueExpression.append(", ");
                }
                if (elements[i].equals("null")) {
                    initialValueExpression.append(helper.nullExpression());
                } else {
                    initialValueExpression.append(helper.newInstance(elements[i]));
                }
            }
            initialValueExpression.append(" }");
        }
        String comment = getLocalizedText(ATTRIBUTE_FIELD_COMMENT, a.getName());

        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(
                java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                        | java.lang.reflect.Modifier.STATIC, dataTypeValueSet, fieldName, initialValueExpression);
    }

    private void createPkSetter(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC);
        String productCmptQualifiedName = productCmptInterfaceBuilder
                .getQualifiedClassName(getPcType().getIpsSrcFile());
        String productCmptUnqualifiedName = productCmptInterfaceBuilder.getUnqualifiedClassName(getPcType()
                .getIpsSrcFile());

        String[] paramTypes = new String[] { productCmptQualifiedName, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName() };
        String[] paramNames = new String[] { "pc", "isInitMode" };

        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                Datatype.VOID.getJavaClassName(), "set" + StringUtils.capitalise(productCmptUnqualifiedName),
                paramNames, paramTypes, javaDoc, ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (productCmptInterfaceBuilder == null) {
            builderName = ProductCmptInterfaceCuBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException("One of the builders this builder depends on is not set: " + builderName);
        }
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        createPkGetter(methodsBuilder);
        createPkSetter(methodsBuilder);
        buildMethods(methodsBuilder);
    }

    protected boolean generatesInterface() {
        return true;
    }

    /**
     * Empty implementation
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * Returns null. This method will not be called for an interface builder.
     * 
     * @see org.faktorips.devtools.core.builder.AbstractPcTypeBuilder#getSuperclass()
     */
    protected String getSuperclass() throws CoreException {
        return null;
    }

    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IPolicyComponent.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(getPcType().getSupertype());
            javaSupertype = supertype == null ? javaSupertype : getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return new String[] { javaSupertype };
    }

    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Datatype datatype = getPcType().getIpsProject().findDatatype(attribute.getDatatype());
        if (attribute.getModifier() == org.faktorips.devtools.core.model.pctype.Modifier.PUBLISHED) {
            createAttributeGetterDeclaration(methodsBuilder, attribute, datatype);
            if (attribute.getAttributeType() == AttributeType.CHANGEABLE) {
                createAttributeSetterDeclaration(methodsBuilder, attribute, datatype);
            }
            if (!attribute.getValueSet().isAllValues()) {
                createAttributeValueSetDeclaration(methodsBuilder, attribute, datatype, datatypeHelper);
                if (!attribute.isProductRelevant()) {
                    createAttributeValueSetField(memberVarsBuilder, attribute, datatype, datatypeHelper);
                }
            }
            if (attribute.getAttributeType() == AttributeType.CONSTANT && !attribute.isProductRelevant()) {
                createAttributeField(memberVarsBuilder, attribute, datatype, datatypeHelper);
            }
        }
    }

    protected void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        PolicyCmptTypeInterfaceRelationBuilder relationBuilder = new PolicyCmptTypeInterfaceRelationBuilder(this);
        relationBuilder.buildRelation(methodsBuilder, relation);
    }

    /**
     * Empty implementation.
     * 
     * overidden
     */
    protected void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }
}