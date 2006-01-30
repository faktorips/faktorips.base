package org.faktorips.devtools.stdbuilder.pctype;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaModelException;
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
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.stdbuilder.Util;
import org.faktorips.runtime.internal.PolicyComponentImpl;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class PolicyCmptTypeImplCuBuilder extends AbstractPcTypeBuilder {

    private final static String ATTRIBUTE_FIELD_COMMENT = "ATTRIBUTE_FIELD_COMMENT";

    private final static String ATTRIBUTE_IMPLEMENTATION_GETTER_JAVADOC = "ATTRIBUTE_IMPLEMENTATION_GETTER_JAVADOC";

    private final static String ATTRIBUTE_IMPLEMENTATION_SETTER_JAVADOC = "ATTRIBUTE_IMPLEMENTATION_SETTER_JAVADOC";
    private final static String ATTRIBUTE_VALUESET_IMPLEMENTATION_GETTER_JAVADOC = "ATTRIBUTE_VALUESET_IMPLEMENTATION_GETTER_JAVADOC";

    private final static String CONSTRUCTOR_POLICY_JAVADOC = "CONSTRUCTOR_POLICY_JAVADOC";

    private final static String VALIDATEDEPS_IMPLEMENTATION_JAVADOC = "VALIDATEDEPS_IMPLEMENTATION_JAVADOC";

    private final static String VALIDATESELF_IMPLEMENTATION_JAVADOC = "VALIDATESELF_IMPLEMENTATION_JAVADOC";

    private final static String CREATEMESSAGEFOR_POLICY_JAVADOC = "CREATEMESSAGEFOR_POLICY_JAVADOC";

    private final static String EXECMESSAGE_POLICY_JAVADOC = "EXECMESSAGE_POLICY_JAVADOC";

    private final static String PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC = "PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC";

    private final static String PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC = "PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC";

    private final static String PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC = "PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC";
    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private final static String ATTRIBUTE_DERIVED_GETTER_JAVADOC = "ATTRIBUTE_DERIVED_GETTER_JAVADOC";

    private PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder;
    private PolicyCmptTypeImplCuBuilder policyCmptTypeImplBuilder;
    private ProductCmptInterfaceCuBuilder productCmptInterfaceBuilder;
    private ProductCmptImplCuBuilder productCmptImplBuilder;

    private static final String INITIALIZE_JAVADOC = "INITIALIZE_JAVADOC";

    public PolicyCmptTypeImplCuBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(PolicyCmptTypeImplCuBuilder.class));
        setMergeEnabled(true);
    }

    public void setPolicyCmptTypeImplBuilder(PolicyCmptTypeImplCuBuilder policyCmptTypeImplCuBuilder) {
        this.policyCmptTypeImplBuilder = policyCmptTypeImplCuBuilder;
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    PolicyCmptTypeImplCuBuilder getPolicyCmptTypeImplBuilder() {
        return policyCmptTypeImplBuilder;
    }

    PolicyCmptTypeInterfaceCuBuilder getPolicyCmptTypeInterfaceBuilder() {
        return policyCmptTypeInterfaceBuilder;
    }

    public void setProductCmptImplBuilder(ProductCmptImplCuBuilder productCmptImplBuilder) {
        this.productCmptImplBuilder = productCmptImplBuilder;
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceCuBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (policyCmptTypeInterfaceBuilder == null) {
            builderName = PolicyCmptTypeInterfaceCuBuilder.class.getName();
        }

        if (policyCmptTypeImplBuilder == null) {
            builderName = PolicyCmptTypeImplCuBuilder.class.getName();
        }

        if (productCmptInterfaceBuilder == null) {
            builderName = ProductCmptInterfaceCuBuilder.class.getName();
        }

        if (productCmptImplBuilder == null) {
            builderName = ProductCmptImplCuBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }

    /**
     * override
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    /**
     * override
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { getInterfaceName() };
    }

    /**
     * override
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        createInitMethod(methodsBuilder);
        createPkGetter(methodsBuilder);
        createPkSetter(methodsBuilder);
        createPkImplGetter(methodsBuilder);
        buildValidation(methodsBuilder);
        buildAbstractMethods(methodsBuilder);
        buildMethods(methodsBuilder);
    }

    /**
     * override
     */
    protected void generateCodeForAttribute(IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Datatype datatype = BuilderHelper.findAttributeDatatype(a);

        String fieldName = null;
        if (!a.isProductRelevant()
                && (a.getAttributeType() == AttributeType.CHANGEABLE
                        || a.getAttributeType() == AttributeType.COMPUTED || (a.getAttributeType() == AttributeType.CONSTANT && a
                        .getModifier() != Modifier.PUBLISHED))) {
            fieldName = createAttributeField(memberVarsBuilder, a, datatype, datatypeHelper);
        } else {
            if (a.isProductRelevant()
                    && (a.getAttributeType() == AttributeType.CHANGEABLE || a.getAttributeType() == AttributeType.COMPUTED)) {
                fieldName = createAttributeField(memberVarsBuilder, a, datatype, datatypeHelper);
            }
        }

        if (a.getAttributeType() != AttributeType.DERIVED) {
            // Derived IAttribute werden in Extension Klasse behandelt
            createAttributeGetterMethod(methodsBuilder, a, datatype, fieldName);
        }

        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            createAttributeSetterMethod(methodsBuilder, a, datatype, fieldName);
        }

        if (!a.isProductRelevant()) {
            if (!a.getValueSet().isAllValues()) {
                if (a.getModifier() != Modifier.PUBLISHED) {
                    createAttributeValueSetField(memberVarsBuilder, a, datatype, datatypeHelper);
                }
                String valueSetFieldName = getPolicyValueSetFieldName(a);
                createAttributeValueSetMethods(methodsBuilder, a, datatype, datatypeHelper,
                    valueSetFieldName);
            }
        } else { // a.isProductRelevant()
            if (!a.getValueSet().isAllValues()) {
                createAttributeValueSetMethods(methodsBuilder, a, datatype, datatypeHelper);
            }
        }

        // -- from ext
        if (a.getAttributeType() == AttributeType.DERIVED) {
            createAttributeDerivedGetterMethod(methodsBuilder, a, datatype);
            if (!a.isProductRelevant()) {
                createAttributeComputeMethod(methodsBuilder, a, datatype);
            }
        } else if (a.getAttributeType() == AttributeType.COMPUTED && !a.isProductRelevant()) {
            createAttributeComputeMethod(methodsBuilder, a, datatype);
        }
    }

    /**
     * Open up visibility for relation builder. Might get removed after the builders have been cleaned up.
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.builder.AbstractPcTypeBuilder#isContainerRelation(org.faktorips.devtools.core.model.pctype.IRelation)
     */
    public boolean isContainerRelation(IRelation relation) {
        return super.isContainerRelation(relation);
    }

    /**
     * overidden
     */
    protected void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        //TODO needs to be refactored
        PolicyCmptTypeImplRelationBuilder relationBuilder = new PolicyCmptTypeImplRelationBuilder(
                this);
        relationBuilder.buildRelation(memberVarsBuilder, methodsBuilder, relation);

    }

    protected void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] relations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
//      TODO needs to be refactored
        PolicyCmptTypeImplRelationBuilder relationBuilder = new PolicyCmptTypeImplRelationBuilder(
            this);
        relationBuilder.buildContainerRelation(memberVarsBuilder, methodsBuilder, containerRelation, relations);

    }

    /**
     * overidden
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        createConstructor(builder, true);
        createConstructor(builder, false);
    }

    /**
     * overidden
     */
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * overidden
     */
    protected String getSuperclass() throws CoreException {
        String javaSupertype = PolicyComponentImpl.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(
                getPcType().getSupertype());
            if (supertype != null) {
                javaSupertype = policyCmptTypeImplBuilder.getQualifiedClassName(supertype
                        .getIpsSrcFile());
            }
        }
        return javaSupertype;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return StringUtils.capitalise(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()))
                + "Impl";
    }

    private void buildValidation(JavaCodeFragmentBuilder builder) throws CoreException {
        createMethodValidateSelf(builder, getPcType().getAttributes());
        createMethodValidateDependants(builder);
        buildExecRule(builder);
        buildMessageForRule(builder);
    }

    String getPolicyCmptImplGetNumOfMethodName(IRelation r) {
        return "getAnzahl" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    String getPolicyCmptImplGetMethodName(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    String getPolicyCmptImplGetAllMethodName(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    private void createMethodValidateDependants(JavaCodeFragmentBuilder builder)
            throws CoreException {
        /*
         * public void validateDependants(MessageList ml) { if(NumOfRelToMany() > 0) { TargetType[]
         * rels = GetAllRelationToMany(); for (int i = 0; i < rels.length; i++) {
         * ml.add(rels[i].validate()); } if (NumOfRelTo1() > 0) {
         * ml.add(GetRelationTo1().validate()); } }
         */

        // TODO Methodenname von pcType holen
        String methodName = "validateDependants";
        IRelation[] relations = getPcType().getRelations();

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml);");
        for (int i = 0; i < relations.length; i++) {
            IRelation r = relations[i];
            if (!r.validate().containsErrorMsg()) {
                if (r.getRelationType() == RelationType.COMPOSITION
                        && StringUtils.isEmpty(r.getContainerRelation())) {
                    body.appendln();
                    if (r.is1ToMany()) {
                        IPolicyCmptType target = r.getIpsProject().findPolicyCmptType(r.getTarget());
                        body.append("if(");
                        body.append(getPolicyCmptImplGetNumOfMethodName(r));
                        body.append("() > 0) { ");
                        body.appendClassName(policyCmptTypeInterfaceBuilder
                                .getQualifiedClassName(target.getIpsSrcFile()));
                        body.append("[] rels = ");
                        body.append(getPolicyCmptImplGetAllMethodName(r));
                        body.append("();");
                        body.append("for (int i = 0; i < rels.length; i++)");
                        body.append("{ ml.add(rels[i].validate()); } }");
                    } else {
                        body.append("if(");
                        body.append(getPolicyCmptImplGetNumOfMethodName(r));
                        body.append("() > 0) { ml.add(");
                        body.append(getPolicyCmptImplGetMethodName(r));
                        body.append("().validate()); }");
                    }
                }
            }
        }

        String javaDoc = getLocalizedText(VALIDATEDEPS_IMPLEMENTATION_JAVADOC, getPcType()
                .getName());

        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "ml" }, new String[] { MessageList.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    private void createMethodValidateSelf(JavaCodeFragmentBuilder builder, IAttribute[] attributes)
            throws CoreException {
        /*
         * public void validateSelf(MessageList ml) { super.validateSelf(ml); }
         */

        // TODO Methodenname von pcType holen
        String methodName = "validateSelf";
        String javaDoc = getLocalizedText(VALIDATESELF_IMPLEMENTATION_JAVADOC, getPcType()
                .getName());

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml);");
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            body.append("execRule");
            body.append(StringUtils.capitalise(r.getName()));
            body.append("(ml);");
        }
        // buildValidationValueSet(body, attributes); wegschmeissen ??
        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "ml" }, new String[] { MessageList.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    private void buildMessageForRule(JavaCodeFragmentBuilder builder) throws JavaModelException {
        // nur Behandlung der Fehlermeldungen in dieser Klasse
        // Eigentliche Pruefung muss in Extension-Klassen impl. werden
        // protected Message createMessageForRulePlzVorhanden(Object p0, Object p1) {
        // Object[] replacements = new Object[2];
        // replacements[0] = p0;
        // replacements[1] = p1;
        // MessageFormat mf = new MessageFormat("Die Postleitzahl muss eingegeben werden.");
        // return new Message("HRP01", mf.format(replacements), Message.ERROR); }
        IValidationRule[] rules = getPcType().getRules();

        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            String[] paramNameList = BuilderHelper.extractMessageParameters(r.getMessageText());
            String[] paramTypeList = new String[paramNameList.length];
            JavaCodeFragment body = new JavaCodeFragment();
            if (paramNameList.length > 0) {
                body.append("Object[] replacements = new Object[");
                body.append(paramNameList.length);
                body.append("];");
                for (int j = 0; j < paramNameList.length; j++) {
                    paramTypeList[j] = Object.class.getName();
                    body.append("replacements[");
                    body.append(j);
                    body.append("] = ");
                    body.append(paramNameList[j]);
                    body.append(';');
                }
                body.appendClassName(MessageFormat.class);
                body.append(" mf = new ");
                body.appendClassName(MessageFormat.class);
                body.append("(\"");
                body.append(BuilderHelper.transformMessage(r.getMessageText()));
                body.append("\");");
            }
            body.append("return new ");
            body.appendClassName(Message.class);
            body.append("(\"");
            body.append(r.getMessageCode());
            body.append("\", ");
            if (paramNameList.length > 0) {
                body.append("mf.format(replacements), ");
            } else {
                body.append('\"');
                body.append(r.getMessageText());
                body.append("\", ");
            }

            body.append(r.getMessageSeverity().getJavaSourcecode());
            body.append(");");

            String methodName = "createMessageForRule" + StringUtils.capitalise(r.getName());

            String javaDoc = getLocalizedText(CREATEMESSAGEFOR_POLICY_JAVADOC, r.getName());

            builder.method(java.lang.reflect.Modifier.PROTECTED, Message.class.getName(),
                methodName, paramNameList, paramTypeList, body, javaDoc, ANNOTATION_GENERATED);
        }
    }

    /**
     * @return
     * @throws CoreException
     */
    private String getInterfaceName() throws CoreException {
        return policyCmptTypeInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile());
    }

    private void createConstructor(JavaCodeFragmentBuilder builder, boolean isParamListEmpty)
            throws CoreException {
        /*
         * Beispiel: public MotorPolicy(MotorPolicyPk pc) { super(pc); ... } ... steht fuer
         * Initialisierung von produktrelevanten, aenderbaren Attributen
         */
        String javaDoc = getLocalizedText(CONSTRUCTOR_POLICY_JAVADOC, getUnqualifiedClassName());
        JavaCodeFragment fragment = new JavaCodeFragment();
        String[] paramTypes, paramNames;
        if (!isParamListEmpty) {
            paramTypes = new String[] { productCmptInterfaceBuilder
                    .getQualifiedClassName(getPcType().getIpsSrcFile()) };
            paramNames = new String[] { "pc" };
            fragment.append("super();");
            fragment.append("setProductCmpt(pc);");
            fragment.append("initialize();");
        } else {
            fragment.append("super();");
            paramTypes = new String[0];
            paramNames = new String[0];
        }

        builder.method(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
            paramNames, paramTypes, fragment, javaDoc, ANNOTATION_GENERATED);
    }

    private void createInitMethod(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText(INITIALIZE_JAVADOC);
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            fragment.append("super.initialize();");
        }
        IAttribute[] attributes = getPcType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (!a.validate().containsErrorMsg()) {
                if (a.isProductRelevant() && a.getAttributeType() == AttributeType.CHANGEABLE) {
                    fragment.append(getPolicyFieldValueName(a));
                    fragment.append(" = ");
                    fragment.append(getAttributeValueFromPk(a));
                    fragment.append(";");
                }
            }
        }

        builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.VOID.getJavaClassName(),
            "initialize", new String[0], new String[0], fragment, javaDoc, ANNOTATION_GENERATED);
    }

    private void createPkGetter(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText(PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC);
        String productCmptInterfaceQualifiedName = productCmptInterfaceBuilder
                .getQualifiedClassName(getPcType().getIpsSrcFile());
        String productCmptInterfaceUnqualifiedName = productCmptInterfaceBuilder
                .getUnqualifiedClassName(getPcType().getIpsSrcFile());

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return (");
        body.appendClassName(productCmptInterfaceQualifiedName);
        body.append(") getProductComponent();");

        builder.method(java.lang.reflect.Modifier.PUBLIC, productCmptInterfaceQualifiedName, "get"
                + StringUtils.capitalise(productCmptInterfaceUnqualifiedName), new String[0],
            new String[0], body, javaDoc, ANNOTATION_GENERATED);
    }

    private void createPkSetter(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText(PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC);
        String productCmptInterfaceQualifiedName = productCmptInterfaceBuilder
                .getQualifiedClassName(getPcType().getIpsSrcFile());
        String productCmptInterfaceUnqualifiedName = productCmptInterfaceBuilder
                .getUnqualifiedClassName(getPcType().getIpsSrcFile());

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("setProductCmpt(pc);");
        body.append("if(isInitMode) { initialize(); }");
        String[] paramTypes = new String[] { productCmptInterfaceQualifiedName,
                Datatype.PRIMITIVE_BOOLEAN.getJavaClassName() };
        String[] paramNames = new String[] { "pc", "isInitMode" };

        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(), "set"
                + StringUtils.capitalise(productCmptInterfaceUnqualifiedName), paramNames,
            paramTypes, body, javaDoc, ANNOTATION_GENERATED);
    }

    private void createPkImplGetter(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText(PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC);
        String productCmptImplQualifiedName = productCmptImplBuilder
                .getQualifiedClassName(getPcType().getIpsSrcFile());
        String productCmptImplUnqualifiedName = productCmptImplBuilder
                .getUnqualifiedClassName(getPcType().getIpsSrcFile());

        JavaCodeFragment body = new JavaCodeFragment();
        body.append(" return (");
        body.appendClassName(productCmptImplQualifiedName);
        body.append(") getProductComponent();");

        builder.method(java.lang.reflect.Modifier.PUBLIC, productCmptImplQualifiedName, "get"
                + StringUtils.capitalise(productCmptImplUnqualifiedName), new String[0],
            new String[0], body, javaDoc, ANNOTATION_GENERATED);
    }

    private String getPkGetter() throws CoreException {
        return "get"
                + StringUtils.capitalise(productCmptInterfaceBuilder
                        .getUnqualifiedClassName(getPcType().getIpsSrcFile())) + "()";
    }

    // duplicate in ProductCmptImplCuBuilder and ProductCmptInterfaceCuBuilder
    private String getPcInterfaceGetDefaultValueMethodName(IAttribute a) {
        return "getVorgabewert" + StringUtils.capitalise(a.getName());
    }

    // duplicate in ProductCmptInterfaceCuBuilder, ProductCmptImplCuBuilder
    private String getPcInterfaceGetValueMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param buffer
     * @param a
     * @throws CoreException
     */
    private String getAttributeValueFromPk(IAttribute a) throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = getPcInterfaceGetDefaultValueMethodName(a);
        } else {
            methodName = getPcInterfaceGetValueMethodName(a);
        }
        return getPkGetter() + '.' + methodName + "()";
    }

    // Duplicate is also in ProductCmptGenerationBuilder
    private String getPolicyCmptImplComputeMethodName(IAttribute a) {
        return "compute" + StringUtils.capitalise(a.getName());
    }

    // -- from ext
    private void createAttributeComputeMethod(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName(),
            getPolicyCmptImplComputeMethodName(a), BuilderHelper.extractParameterNames(parameters),
            BuilderHelper.transformParameterTypesToJavaClassNames(a.getIpsProject(), parameters),
            getMethodBodyReturningDefaultOrNull(a, datatype), null, ANNOTATION_MODIFIABLE);
    }

    // -- from ext
    private JavaCodeFragment getMethodBodyReturningDefaultOrNull(IAttribute a, Datatype datatype) {
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln(" // TODO implement method");
        body.append("return ");
        JavaCodeFragment initialValueExpression = null;
        DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
        if (helper != null) {
            helper.newInstance(a.getDefaultValue());
        }
        if (initialValueExpression != null) {
            body.append(initialValueExpression);
            body.append(";");
        } else {
            body.append("null;");
        }
        return body;
    }

    // -- from ext
    private void createAttributeDerivedGetterMethod(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        String[] paramNames = BuilderHelper.extractParameterNames(parameters);
        String[] paramTypes = BuilderHelper.transformParameterTypesToJavaClassNames(a
                .getIpsProject(), parameters);
        JavaCodeFragment paramFragment = new JavaCodeFragment();
        JavaCodeFragment variableFragement = new JavaCodeFragment();

        paramFragment.append('(');
        for (int i = 0; i < paramNames.length; i++) {
            variableFragement.appendClassName(paramTypes[i]);
            variableFragement.append(' ');
            variableFragement.append(paramNames[i]);
            variableFragement.append(" = ");
            Datatype paramDataype = a.getIpsProject().findDatatype(parameters[i].getDatatype());
            DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(paramDataype);
            if (helper != null) {
                JavaCodeFragment nullExpressionFragment = helper.nullExpression();
                variableFragement.append(nullExpressionFragment);
            } else {
                variableFragement.append("null");
            }
            variableFragement.append(';');
            if (i > 0) {
                paramFragment.append(", ");
            }
            paramFragment.append(paramNames[i]);
        }
        paramFragment.append(")");

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("// TODO Belegung der Berechnungsparameter implementieren");
        body.append(variableFragement);
        body.append(" return ");
        if (a.isProductRelevant()) {
            body.append(getPkGetter());
            body.append('.');
        }
        body.append("compute" + StringUtils.capitalise(a.getName()));
        body.append(paramFragment);
        body.append(";");

        String javaDoc = getLocalizedText(ATTRIBUTE_DERIVED_GETTER_JAVADOC, a.getName());

        builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName(),
            getPcInterfaceGetValueMethodName(a), new String[0], new String[0], body, javaDoc,
            ANNOTATION_MODIFIABLE);
    }

    // --from ext
    private void buildMethod(JavaCodeFragmentBuilder builder, IMethod method) throws CoreException {
        Datatype datatype = getPcType().getIpsProject().findDatatype(method.getDatatype());

        builder.method(Util.getJavaModifier(method.getModifier()), datatype.getJavaClassName(),
            method.getName(), method.getParameterNames(), BuilderHelper
                    .transformParameterTypesToJavaClassNames(method.getIpsProject(), method
                            .getParameters()), new JavaCodeFragment(method.getBody()), method
                    .getDescription(), ANNOTATION_MODIFIABLE);
    }

    // --

    // --from ext
    private void buildExecRule(JavaCodeFragmentBuilder builder) throws JavaModelException {
        // private void execRulePlzVorhanden(MessageList ml) {
        // if (false) {
        // ml.add(createMessageForRulePlzVorhanden()); } }
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            String javaDoc = getLocalizedText(EXECMESSAGE_POLICY_JAVADOC, r.getName());
            JavaCodeFragment body = new JavaCodeFragment();
            body.append("if(false) ");
            body.appendOpenBracket();
            body.append("ml.add(createMessageForRule");
            body.append(StringUtils.capitalise(r.getName()));
            body.append('(');
            String[] paramList = BuilderHelper.extractMessageParameters(r.getMessageText());
            for (int j = 0; j < paramList.length; j++) {
                if (j > 0) {
                    body.append(", ");
                }
                body.append('\"');
                body.append(paramList[j]);
                body.append('\"');
            }
            body.append("));");
            body.appendCloseBracket();

            builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.VOID.getJavaClassName(),
                "execRule" + StringUtils.capitalise(r.getName()), new String[] { "ml" },
                new String[] { MessageList.class.getName() }, body, javaDoc, ANNOTATION_MODIFIABLE);
        }
    }

    // --

    private String getPolicyValueSetFieldName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private String getPolicySetMethodName(IAttribute a) {
        return "set" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @param datatype
     * @param field
     * @throws CoreException
     */
    private void createAttributeSetterMethod(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            String fieldName) throws CoreException {
        String methodName = getPolicySetMethodName(a);
        String javaDoc = getLocalizedText(ATTRIBUTE_IMPLEMENTATION_SETTER_JAVADOC, a.getName());
        JavaCodeFragment body = new JavaCodeFragment();
        body.append(fieldName);
        body.append(" = newValue;");
        builder.method(Util.getJavaModifier(a.getModifier()), Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "newValue" }, new String[] { datatype.getJavaClassName() },
            body, javaDoc, ANNOTATION_GENERATED);
    }

    private void createAttributeGetterMethod(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            String fieldName) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        if (fieldName == null) {
            if (a.getAttributeType() == AttributeType.CONSTANT) {
                if (a.isProductRelevant()) {
                    body.append(getAttributeValueFromPk(a));
                } else {
                    body.append(a.getName());
                }
            }
        } else {
            body.append(fieldName);
        }
        body.append(";");

        String javaDoc = getLocalizedText(ATTRIBUTE_IMPLEMENTATION_GETTER_JAVADOC, a.getName());

        builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName(),
            getPolicyGetMethodName(a), new String[0], new String[0], body, javaDoc,
            ANNOTATION_GENERATED);
    }

    private String getPolicyGetMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    /**
     * Methode wird nur aufgerufen, falls ein Field erzeugt werden soll Also kein Aufruf fuer
     * konstante, produktrelevante IAttribute
     * 
     * @param a
     * @param datatype
     * @return
     */
    private String createAttributeField(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        if (a.getAttributeType() == AttributeType.CONSTANT && a.getModifier() == Modifier.PUBLISHED) {
            return null; // field wird im Interface definiert
        }
        int flags;
        if (getPcType().isExtensionCompilationUnitGenerated()) {
            flags = java.lang.reflect.Modifier.PROTECTED;
            ;
        } else {
            flags = a.getAttributeType() == AttributeType.COMPUTED ? java.lang.reflect.Modifier.PROTECTED
                    : java.lang.reflect.Modifier.PRIVATE;
        }
        if (a.getAttributeType() == AttributeType.CONSTANT) {
            flags = flags | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC;
        }
        JavaCodeFragment initialValueExpression = helper.newInstance(a.getDefaultValue());
        String comment = getLocalizedText(ATTRIBUTE_FIELD_COMMENT, a.getName());
        String fieldName = getPolicyFieldValueName(a);

        builder.javaDoc(comment, ANNOTATION_GENERATED);
        builder.varDeclaration(flags, datatype.getJavaClassName(), fieldName,
            initialValueExpression);
        return fieldName;
    }

    private String getPolicyFieldValueName(IAttribute a) {
        return a.getName();
    }

    private void buildAbstractMethod(JavaCodeFragmentBuilder builder, IMethod method)
            throws CoreException {
        Datatype datatype = getPcType().getIpsProject().findDatatype(method.getDatatype());

        builder.methodBegin(java.lang.reflect.Modifier.ABSTRACT
                | Util.getJavaModifier(method.getModifier()), datatype.getJavaClassName(), method
                .getName(), method.getParameterNames(), BuilderHelper
                .transformParameterTypesToJavaClassNames(method.getIpsProject(), method
                        .getParameters()), method.getDescription());
        builder.append(";");
    }

    private void buildAbstractMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        IMethod[] methods = getPcType().getMethods();
        for (int i = methods.length - 1; i >= 0; i--) {
            IMethod m = methods[i];
            if (!m.validate().containsErrorMsg()) {
                if (m.isAbstract()) {
                    try {
                        buildAbstractMethod(builder, m);
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus(IStatus.ERROR, "Error building method "
                                + m.getName() + " of " + getPcType(), e));
                    }
                }
            }
        }
    }

    // --from ext
    private void buildMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        IMethod[] methods = getPcType().getMethods();
        for (int i = methods.length - 1; i >= 0; i--) {
            IMethod m = methods[i];
            if (!m.validate().containsErrorMsg()) {
                if (!m.isAbstract()) {
                    try {
                        buildMethod(builder, m);
                    } catch (Exception e) {
                        addToBuildStatus(new IpsStatus(IStatus.ERROR, "Error building method "
                                + m.getName() + " of " + getPcType(), e));
                    }
                }
            }
        }
    }

    // --

    private void createAttributeValueSetField(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {

        String fieldName = getPolicyValueSetFieldName(a);
        String dataTypeValueSet;
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();

        if (a.getValueSet().isRange()) {
            dataTypeValueSet = helper.getRangeJavaClassName();
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(helper.getRangeJavaClassName());
            initialValueExpression.append("( ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet())
                    .getLowerBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet())
                    .getUpperBound()));
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

        builder.javaDoc(comment, ANNOTATION_GENERATED);
        builder.varDeclaration(java.lang.reflect.Modifier.PROTECTED
                | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC,
            dataTypeValueSet, fieldName, initialValueExpression);
    }

    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper,
            String fieldName) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(fieldName);
        body.append(";");
        createAttributeValueSetMethods(builder, a, datatype, helper, body);
    }

    private String getPolicyImplGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private String getPolicyImplGetValueSetMethodName(IAttribute a) {
        return "getWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper,
            JavaCodeFragment bodyMax) throws CoreException {

        String methodNameMax = getPolicyImplGetMaxValueSetMethodName(a);
        String methodName = getPolicyImplGetValueSetMethodName(a);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(methodNameMax);
        body.append("();");
        String javaDocMax = getLocalizedText(JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
        String javaDoc = getLocalizedText(JAVA_GETTER_METHOD_VALUESET, a.getName());

        if (a.getValueSet().isRange()) {

            builder.method(Util.getJavaModifier(a.getModifier()), helper.getRangeJavaClassName(),
                methodNameMax, new String[] {}, new String[] {}, bodyMax, javaDocMax);

            builder.method(Util.getJavaModifier(a.getModifier()), helper.getRangeJavaClassName(),
                methodName, new String[] {}, new String[] {}, body, javaDoc, ANNOTATION_MODIFIABLE);
        } else {

            builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName()
                    + "[]", methodNameMax, new String[] {}, new String[] {}, bodyMax, javaDocMax,
                ANNOTATION_GENERATED);

            builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName()
                    + "[]", methodName, new String[] {}, new String[] {}, body, javaDoc,
                ANNOTATION_MODIFIABLE);
        }
    }

    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        String methodName = getPolicyImplGetMaxValueSetMethodName(a);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(getPkGetter());
        body.append('.');
        body.append(methodName);
        body.append("();");
        createAttributeValueSetMethods(builder, a, datatype, helper, body);
    }

    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }
}