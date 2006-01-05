package org.faktorips.devtools.stdbuilder.pctype;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.stdbuilder.Util;
import org.faktorips.runtime.RuntimeRepository;
import org.faktorips.runtime.internal.ProductComponentImpl;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProductCmptImplCuBuilder extends AbstractPcTypeBuilder {

    private final static String ATTRIBUTE_FIELD_COMMENT = "ATTRIBUTE_FIELD_COMMENT";
    private final static String ATTRIBUTE_IMPLEMENTATION_GETTER_JAVADOC = "ATTRIBUTE_IMPLEMENTATION_GETTER_JAVADOC";
    private final static String CONSTRUCTOR_PRODUCT_JAVADOC = "CONSTRUCTOR_PRODUCT_JAVADOC";
    private final static String INITFROMXML_PRODUCT_JAVADOC = "INITFROMXML_PRODUCT_JAVADOC";
    private final static String JAVA_GETTER_METHOD_MAX_VALUESET_JAVADOC = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_CREATE_POLICY_CMPT_METHOD_JAVADOC = "JAVA_CREATE_POLICY_CMPT_METHOD";

    private PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder;
    private PolicyCmptTypeImplCuBuilder policyCmptTypeImplBuilder;
    private ProductCmptInterfaceCuBuilder productCmptInterfaceBuilder;

    public ProductCmptImplCuBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptImplCuBuilder.class));
        setMergeEnabled(true);
    }

    public void setPolicyCmptTypeImplBuilder(PolicyCmptTypeImplCuBuilder policyCmptTypeImplBuilder) {
        this.policyCmptTypeImplBuilder = policyCmptTypeImplBuilder;
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceCuBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    ProductCmptInterfaceCuBuilder getProductCmptInterfaceBuilder() {
        return productCmptInterfaceBuilder;
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    /**
     * Open up visibility. Might get removed after refactoring the relation builder. Overridden
     * IMethod.
     * 
     * @see org.faktorips.devtools.core.builder.AbstractPcTypeBuilder#isContainerRelation(org.faktorips.devtools.core.model.pctype.IRelation)
     */
    public boolean isContainerRelation(IRelation relation) {
        return super.isContainerRelation(relation);
    }

    protected String getSuperclass() throws CoreException {
        String javaSupertype = ProductComponentImpl.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(
                getPcType().getSupertype());
            javaSupertype = supertype == null ? javaSupertype : getQualifiedClassName(supertype
                    .getIpsSrcFile());
        }
        return javaSupertype;
    }

    protected boolean generatesInterface() {
        return false;
    }

    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { productCmptInterfaceBuilder.getQualifiedClassName(getIpsSrcFile()) };
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

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }

    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // TODO Auto-generated method stub
        if (attribute.isProductRelevant()) {
            Datatype datatype = getPcType().getIpsProject().findDatatype(attribute.getDatatype());

            if (!attribute.getValueSet().isAllValues()) {
                createAttributeValueSetField(memberVarsBuilder, attribute, datatype, datatypeHelper);
            }

            if (attribute.getAttributeType() != AttributeType.COMPUTED
                    && attribute.getAttributeType() != AttributeType.DERIVED) {
                createAttributeField(memberVarsBuilder, attribute, datatype);
            }

            if (!attribute.getValueSet().isAllValues()) {
                createAttributeValueSetMethods(methodsBuilder, attribute, datatype, datatypeHelper);
            }

            if (attribute.getAttributeType() != AttributeType.COMPUTED
                    && attribute.getAttributeType() != AttributeType.DERIVED) {
                createAttributeGetterMethod(methodsBuilder, attribute, datatype);
            }
        }
    }

    protected void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        ProductCmptImplRelationBuilder relationBuilder = new ProductCmptImplRelationBuilder(this);
        relationBuilder.buildContainerRelation(memberVarsBuilder, methodsBuilder, containerRelation, subRelations);
    }

    protected void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        ProductCmptImplRelationBuilder relationBuilder = new ProductCmptImplRelationBuilder(this);
        relationBuilder.buildRelation(memberVarsBuilder, methodsBuilder, relation);
    }

    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        /*
         * public PnCPolicyPk(RuntimeRepository repository, String qName, Class policyComponentType) {
         * super(registry, qName, policyComponentType); }
         */
        String className = getUnqualifiedClassName();
        String javaDoc = getLocalizedText(CONSTRUCTOR_PRODUCT_JAVADOC, className);

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super(repository, qName, policyComponentType);");
        builder.method(Modifier.PUBLIC, null, className, new String[] { "repository", "qName",
                "policyComponentType" }, new String[] { RuntimeRepository.class.getName(),
                String.class.getName(), Class.class.getName() }, body, javaDoc,
            ANNOTATION_GENERATED);

    }

    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (!getPcType().isAbstract()) {
            buildCreateMethod(methodsBuilder);
        }
        buildInitFromXml(methodsBuilder);

    }

    protected int getClassModifier() throws CoreException {
        IAttribute[] attributes = getPcType().getSupertypeHierarchy().getAllAttributes(getPcType());
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (a.isProductRelevant()
                    && (a.getAttributeType() == AttributeType.COMPUTED || a.getAttributeType() == AttributeType.DERIVED)) {
                return Modifier.PUBLIC | Modifier.ABSTRACT;
            }
        }
        return Modifier.PUBLIC;
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return StringUtils.capitalise(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()))
                + "PkImpl";
    }

    private void buildInitFromXml(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(INITFROMXML_PRODUCT_JAVADOC, "");

        // super.initFromXml(element);
        // Element genElement = XmlUtil.getFirstElement(element, "Generation");
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("super.initFromXml(element);");
        JavaCodeFragment initFromXmlAttribute = buildInitFromXmlAttribute(getPcType()
                .getAttributes());
        JavaCodeFragment InitFromXmlRelation = buildInitFromXmlRelation(getPcType().getRelations());

        if (initFromXmlAttribute != null || InitFromXmlRelation != null) {
            frag.appendClassName(Element.class);
            frag.append(" genElement = ");
            frag.appendClassName(XmlUtil.class);
            frag.append(".getFirstElement(element, \"");
            frag.append(IpsObjectGeneration.TAG_NAME);
            frag.appendln("\");");
            if (initFromXmlAttribute != null) {
                frag.append(initFromXmlAttribute);
            }
            if (InitFromXmlRelation != null) {
                frag.append(InitFromXmlRelation);
            }
        }

        methodsBuilder.method(Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            "initFromXml", new String[] { "element" }, new String[] { Element.class.getName() },
            frag, javaDoc, ANNOTATION_GENERATED);
    }

    private JavaCodeFragment buildInitFromXmlAttribute(IAttribute[] attributes) throws CoreException {

        if (attributes.length == 0) {
            return null;
        }

        JavaCodeFragment frag = new JavaCodeFragment();
        boolean attributeFound = false;
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (a.validate().containsErrorMsg()) {
                continue;
            }
            if (a.isProductRelevant() && a.getAttributeType() != AttributeType.COMPUTED
                    && a.getAttributeType() != AttributeType.DERIVED) {
                if (attributeFound == false) {
                    frag.appendln();
                    frag.appendClassName(HashMap.class);
                    frag.append(" configMap = ");
                    frag.appendln("getConfigElements(genElement);");
                    frag.appendClassName(Element.class);
                    frag.appendln(" configElement = null;");
                    frag.appendClassName(String.class);
                    frag.appendln(" value = null;");
                    attributeFound = true;
                }
                Datatype datatype = getPcType().getIpsProject().findDatatype(a.getDatatype());
                if (datatype == null) {
                    addToBuildStatus(new IpsStatus("Error initializing attribute " + a.getName()
                            + " of " + getPcType() + " from xml. Datatype not found:" + datatype));
                    return frag;
                }
                DatatypeHelper helper = getPcType().getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    addToBuildStatus(new IpsStatus("Error initializing attribute " + a.getName()
                            + " of " + getPcType()
                            + " from xml. No datatype helper found for datatype " + datatype));
                    return frag;
                }
                String fieldName = getJavaProductValueFieldName(a);
                frag.append("configElement = (");
                frag.appendClassName(Element.class);
                frag.append(") configMap.get(\"");
                frag.append(fieldName);
                frag.appendln("\");");
                frag.append("if (configElement != null) ");
                frag.appendOpenBracket();
                frag.appendln("value = configElement.getAttribute(\"value\");");
                frag.append(fieldName);
                frag.append(" = ");
                JavaCodeFragment initialValueExpression = helper.newInstanceFromExpression("value");
                frag.append(initialValueExpression);
                frag.appendln(';');
                if (!a.getValueSet().isAllValues()) {
                    JavaCodeFragment initValueSet;
                    if (a.getValueSet().isRange()) {
                        initValueSet = buildInitFromXmlRange(a, datatype, helper);
                    } else {
                        initValueSet = buildInitFromXmlEnum(a, datatype, helper);
                    }
                    frag.append(initValueSet);
                }
                frag.appendCloseBracket();
            }
        }
        if (!attributeFound) {
            return null;
        }
        return frag;
    }

    /**
     * @param a
     * @param datatype
     * @param helper
     * @return
     * @throws CoreException
     */
    private JavaCodeFragment buildInitFromXmlEnum(IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        /*
         * NodeList nl = getEnumNodeList(configElement);
         * maxWertebereichChangeablePublishedProdRelevant_Enum = new Zahlungsweise[nl.getLength()];
         * for (int i = 0; i < nl.getLength(); i++) { Element valueElement = (Element)nl.item(i);
         * value = valueElement.getAttribute("value");
         * maxWertebereichChangeablePublishedProdRelevant_Enum[i] =
         * Zahlungsweise.getZahlungsweise(value); }
         */
        String fieldName = getJavaProductValueSetFieldName(a);
        JavaCodeFragment initFromXmlEnum = new JavaCodeFragment();
        initFromXmlEnum.appendClassName(NodeList.class);
        initFromXmlEnum.append(" nl = getEnumNodeList(configElement);");
        initFromXmlEnum.append(fieldName);
        initFromXmlEnum.append(" = new ");
        initFromXmlEnum.appendClassName(datatype.getJavaClassName());
        initFromXmlEnum.append("[nl.getLength()];");
        initFromXmlEnum.append("for (int i = 0; i < nl.getLength(); i++)");
        initFromXmlEnum.appendOpenBracket();
        initFromXmlEnum.append("Element valueElement = (Element)nl.item(i);");
        initFromXmlEnum.append("value = valueElement.getAttribute(\"value\");");
        initFromXmlEnum.append(fieldName);
        initFromXmlEnum.append("[i] = ");
        initFromXmlEnum.append(helper.newInstanceFromExpression("value"));
        initFromXmlEnum.append(';');
        initFromXmlEnum.appendCloseBracket();
        return initFromXmlEnum;
    }

    /**
     * @param body
     * @param a
     * @param datatype
     * @param helper
     * @throws CoreException
     */
    private JavaCodeFragment buildInitFromXmlRange(IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        /*
         * Element rangeElement = getRangeElement(configElement);
         * changeablePublishedProdRelevant_RangeRange = new DecimalRange(
         * Decimal.valueOf(rangeElement.getAttribute("lowerBound")),
         * Decimal.valueOf(rangeElement.getAttribute("upperBound")),
         * Decimal.valueOf(rangeElement.getAttribute("step")));
         */
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(Element.class);
        code.append(" rangeElement = getRangeElement(configElement);");
        code.append(getJavaProductValueSetFieldName(a));
        code.append(" = new ");
        code.appendClassName(helper.getRangeJavaClassName());
        code.append('(');
        code.append(helper.newInstanceFromExpression("rangeElement.getAttribute(\"lowerBound\")"));
        code.append(',');
        code.append(helper.newInstanceFromExpression("rangeElement.getAttribute(\"upperBound\")"));
        code.append(',');
        code.append(helper.newInstanceFromExpression("rangeElement.getAttribute(\"step\")"));
        code.append(");");
        return code;
    }

    private JavaCodeFragment buildInitFromXmlRelation(IRelation[] relations) throws CoreException {
        // before the first relation we read the relation elements into a map and defines temp.
        // variables as
        // as follows:
        // HashMap relationMap = getRelationElements(genElement);
        // Element relationElements = null;

        // for each 1-1 relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        // vertragsteilePk = ((Element)relationElement.get(0)).getAttribute("target");
        // }
        // 
        // for each 1-many relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        // vertragsteilPks[] = new VertragsteilPk[relationElements.length()];
        // for (int i=0; i<vertragsteilsPks.length; i++) {
        // vertragsteilPks[i] = ((Element)relationElement.get(i)).getAttribute("target");
        // }
        // }
        // }

        if (relations.length == 0) {
            return null;
        }

        JavaCodeFragment frag = new JavaCodeFragment();
        boolean relationFound = false;
        for (int i = 0; i < relations.length; i++) {
            IRelation r = relations[i];
            if (!r.isReadOnlyContainer() && r.isProductRelevant()) {
                if (relationFound == false) {
                    frag.appendln();
                    frag.appendClassName(HashMap.class);
                    frag.append(" relationMap = ");
                    frag.appendln("getRelationElements(genElement);");
                    frag.appendClassName(List.class);
                    frag.append(" relationElements = null;");
                    relationFound = true;
                }
                frag.append("relationElements = (");
                frag.appendClassName(List.class);
                frag.append(") relationMap.get(\"");
                frag.append(r.getName());
                frag.appendln("\");");
                frag.append("if (relationElements != null) {");
                String fieldName = r.is1ToMany() ? getProductCmptRelation1ToManyFieldName(r)
                        : getProductCmptRelation1To1FieldName(r);
                // if (r.is1ToMany()) { auskommentiert bis genauer Umgang mit relationen geklärt
                // ist. Jan
                frag.append(fieldName);
                frag.appendln(" = new ");
                frag.appendClassName(String.class);
                frag.appendln("[relationElements.size()];");
                frag.appendln("for (int i=0; i<relationElements.size(); i++) {");
                frag.append(fieldName);
                frag.append("[i] = ((");
                frag.appendClassName(Element.class);
                frag.append(")relationElements.get(i)).getAttribute(\"target\");");
                frag.appendln('}');
                // folgende Zeilen auskommentiert bis genauer Umgang mit relationen geklärt ist. Jan
                // } else {
                // frag.append(r.getJavaField(IRelation.JAVA_PRODUCTCMPT_FIELD).getElementName());
                // frag.append(" = ((Element)relationElements.get(0)).getAttribute(\"target\");");
                // getImportsManager().addImport(Element.class.getName());
                // }
                frag.appendln('}');
            }
        }

        if (!relationFound) {
            return null;
        }
        return frag;
    }

    // duplicate in ProductCmptImplRelationBuilder
    private String getProductCmptRelation1ToManyFieldName(IRelation relation) {
        return StringUtils.uncapitalise(relation.getTargetRolePlural()) + "Pks";
    }

    // duplicate in ProductCmptImplRelationBuilder
    private String getProductCmptRelation1To1FieldName(IRelation relation) {
        return StringUtils.uncapitalise(relation.getTargetRoleSingular()) + "Pk";
    }

    private void createAttributeValueSetField(JavaCodeFragmentBuilder memberVarsBuilder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {

        String dataTypeValueSet;

        if (a.getValueSet().isRange()) {
            dataTypeValueSet = helper.getRangeJavaClassName();
        } else {
            dataTypeValueSet = datatype.getJavaClassName() + "[]";
        }
        String comment = getLocalizedText(ATTRIBUTE_FIELD_COMMENT, a.getName());

        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, dataTypeValueSet,
            getJavaProductValueSetFieldName(a));
    }

    // TODO: Refaktoring, Methode ist genauso im PolicyCmptTypeImplCuBuilder
    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {

        String methodName = getJavaProductImplGetMaxValueSetMethodName(a);
        String javaDocMax = getLocalizedText(JAVA_GETTER_METHOD_MAX_VALUESET_JAVADOC, a.getName());
        JavaCodeFragment body = getFieldMethodBody(getJavaProductValueSetFieldName(a));
        if (a.getValueSet().isRange()) {
            methodsBuilder.method(Modifier.PUBLIC, helper.getRangeJavaClassName(), methodName,
                new String[0], new String[0], body, javaDocMax, ANNOTATION_GENERATED);
        } else {
            methodsBuilder.method(Modifier.PUBLIC, datatype.getJavaClassName() + "[]", methodName,
                new String[0], new String[0], body, javaDocMax, ANNOTATION_GENERATED);
        }
    }

    private JavaCodeFragment getFieldMethodBody(String fieldName) {
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(fieldName);
        body.append(';');
        return body;
    }

    /**
     * Methode wird nur aufgerufen, falls ein Field erzeugt werden soll Also kein Aufruf fuer
     * berechnete IAttribute
     * 
     * @param a
     * @param datatype
     * @return
     */
    private void createAttributeField(JavaCodeFragmentBuilder memberVarsBuilder,
            IAttribute a,
            Datatype datatype) throws CoreException {
        DatatypeHelper helper = getPcType().getIpsProject().getDatatypeHelper(datatype);
        JavaCodeFragment initialValueExpression = helper.newInstance(a.getDefaultValue());
        String comment = getLocalizedText(ATTRIBUTE_FIELD_COMMENT, a.getName());

        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatype.getJavaClassName(),
            getJavaProductValueFieldName(a), initialValueExpression);
    }

    // duplicate in ProductCmptInterfaceCuBuilder
    private String getPcInterfaceGetDefaultValueMethodName(IAttribute a) {
        return "getVorgabewert" + StringUtils.capitalise(a.getName());
    }

    // duplicate in ProductCmptInterfaceCuBuilder, PolicyCmptTypeImplCuBuilder
    private String getPcInterfaceGetValueMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @param datatype
     * @param field
     * @throws CoreException
     */
    private void createAttributeGetterMethod(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype) throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = getPcInterfaceGetDefaultValueMethodName(a);
        } else {
            methodName = getPcInterfaceGetValueMethodName(a);
        }
        StringBuffer body = new StringBuffer();
        body.append("{ return ");
        body.append(getJavaProductValueFieldName(a));
        body.append("; }");

        String javaDoc = getLocalizedText(ATTRIBUTE_IMPLEMENTATION_GETTER_JAVADOC, a.getName());
        methodsBuilder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName(),
            methodName, new String[0], new String[0],
            getFieldMethodBody(getJavaProductValueFieldName(a)), javaDoc, ANNOTATION_GENERATED);
    }

    private void buildCreateMethod(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        String javaDoc = getLocalizedText(JAVA_CREATE_POLICY_CMPT_METHOD_JAVADOC);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return new ");
        body.appendClassName(policyCmptTypeImplBuilder.getQualifiedClassName(getIpsSrcFile()));
        body.append("(this);");
        methodBuilder.method(Modifier.PUBLIC,
            policyCmptTypeInterfaceBuilder.getQualifiedClassName(getIpsSrcFile()),
            "create" + StringUtils.capitalise(getPcType().getName()), new String[0], new String[0],
            body, javaDoc, ANNOTATION_GENERATED);
        return;
    }

    private String getJavaProductValueSetFieldName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private String getJavaProductValueFieldName(IAttribute a) {
        return a.getName();
    }

    private String getJavaProductImplGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#isBuilderFor(IIpsObject)
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }
}