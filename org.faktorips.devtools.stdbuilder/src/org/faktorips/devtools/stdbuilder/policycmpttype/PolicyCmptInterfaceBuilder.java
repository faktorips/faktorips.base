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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.EnumValueSet;
import org.faktorips.valueset.IntegerRange;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    public PolicyCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId, boolean changeListenerSupportActive) {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class), changeListenerSupportActive);
        setMergeEnabled(true);
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }
    
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getPolicyCmptTypeName(ipsSrcFile));
    }
    
    public String getPolicyCmptTypeName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return StringUtils.capitalise(name);
    }
    
    public String getPolicyCmptTypeName(IPolicyCmptType type) throws CoreException {
        return StringUtils.capitalise(type.getName());
    }

    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (productCmptInterfaceBuilder == null) {
            builderName = ProductCmptInterfaceBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException("One of the builders this builder depends on is not set: " + builderName);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        IPolicyCmptType type = getPcType();
        String[] interfaces;
        if (isFirstDependantTypeInHierarchy(type)) {
            interfaces = new String[2];
            interfaces[1] = IDependantObject.class.getName();
        } else {
            interfaces = new String[1];
        }
        IPolicyCmptType supertype = type.findSupertype();
        if (supertype != null) {
            interfaces[0] = getQualifiedClassName(supertype);
        } else if (type.isConfigurableByProductCmptType()) {
            interfaces[0] = IConfigurableModelObject.class.getName();
        } else {
            interfaces[0] = IModelObject.class.getName();
        }
        return interfaces;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("INTERFACE", getIpsObject().getName(), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        if (getPcType().findProductCmptType()!=null) {
            generateMethodGetProductCmpt(methodsBuilder);
            generateMethodSetProductCmpt(methodsBuilder);
            generateMethodGetProductCmptGeneration(getProductCmptType(), methodsBuilder);
        }
        generateCodeForValidationRules(memberVarsBuilder);
    }

    /**
     * Generates the code for the rules of the ProductCmptType assigned to this builder.
     */
    protected void generateCodeForValidationRules(JavaCodeFragmentBuilder memberVarsBuilder){
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            generateFieldForMsgCode(rules[i], memberVarsBuilder);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorProduct getMotorProduct();
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getProductCmptType().getName(), getPcType().getName()};
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT", replacements, getPcType(), methodsBuilder);
        generateSignatureGetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.append(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public IMotorProduct getMotorProduct()
     * </pre>
     */
    public void generateSignatureGetProductCmpt(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType = productCmptInterfaceBuilder.getQualifiedClassName(type);
        String methodName = getMethodNameGetProductCmpt(type);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }
    
    /**
     * Returns the name of the method to access the product component, e.g. getMotorProduct
     */
    public String getMethodNameGetProductCmpt(IProductCmptType type) throws CoreException {
        return getLocalizedText(type, "METHOD_GET_PRODUCTCMPT_NAME", type.getName());
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults);
     * </pre>
     */
    protected void generateMethodSetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getProductCmptType().getName(), StringUtils.uncapitalise(getProductCmptType().getName()), "initPropertiesWithConfiguratedDefaults"};
        appendLocalizedJavaDoc("METHOD_SET_PRODUCTCMPT", replacements, getProductCmptType(), methodsBuilder);
        generateSignatureSetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults)
     * </pre>
     */
    public void generateSignatureSetProductCmpt(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameSetProductCmpt(type);
        String[] paramTypes = new String[] { productCmptInterfaceBuilder.getQualifiedClassName(type), "boolean" };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, getMethodParamNamesSetProductCmpt(type), paramTypes);
    }
    
    /**
     * Returns the name of the method to set the product component, e.g. setMotorProduct
     */
    public String getMethodNameSetProductCmpt(IProductCmptType type) throws CoreException {
        return getLocalizedText(type, "METHOD_SET_PRODUCTCMPT_NAME", type.getName());
    }

    /**
     * Returns the method parameters for the method: setProductCmpt.
     */
    public String[] getMethodParamNamesSetProductCmpt(IProductCmptType type) throws CoreException {
        return new String[] { StringUtils.uncapitalise(type.getName()), "initPropertiesWithConfiguratedDefaults" };
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getNameForGenerationConcept(type), type.getName(), type.findPolicyCmptyType().getName()};
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT_GENERATION", replacements, type, methodsBuilder);
        generateSignatureGetProductCmptGeneration(type, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    public void generateSignatureGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String genName = productCmptGenInterfaceBuilder.getQualifiedClassName(type);
        String methodName = getMethodNameGetProductCmptGeneration(type);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, genName, methodName, 
                new String[0], new String[0]);
    }
    
    /**
     * Returns the name of the method to access the product component generation, e.g. getMotorProductGen
     */
    public String getMethodNameGetProductCmptGeneration(IProductCmptType type) throws CoreException {
        String[] replacements = new String[]{type.getName(), getAbbreviationForGenerationConcept(type), getNameForGenerationConcept(type)};
        return getLocalizedText(type, "METHOD_GET_PRODUCTCMPT_GENERATION_NAME", replacements);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(
            IMethod method,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, paramTypes, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code samples:
     * <pre>
     * public void calculatePremium(IPolicy policy)
     * public ICoverage getCoverageWithHighestSumInsured()
     * </pre>
     */
    public void generateSignatureForMethodDefinedInModel(
        IMethod method,
        int javaModifier,
        Datatype returnType,
        Datatype[] paramTypes,
        JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String[] paramClassNames = new String[paramTypes.length];
        for (int i = 0; i < paramClassNames.length; i++) {
            if (paramTypes[i] instanceof IPolicyCmptType) {
                paramClassNames[i] = getQualifiedClassName((IPolicyCmptType)paramTypes[i]);
            } else {
                paramClassNames[i] = paramTypes[i].getJavaClassName();
            }
        }
        String returnClassName;
        if  (returnType instanceof IPolicyCmptType) {
            returnClassName = getQualifiedClassName((IPolicyCmptType)returnType);
        } else {
            returnClassName = returnType.getJavaClassName();
        }
        methodsBuilder.signature(javaModifier, returnClassName, method.getName(), 
                method.getParameterNames(), paramClassNames);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, 
            JavaCodeFragmentBuilder constantBuilder, JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (!(attribute.getModifier() == org.faktorips.devtools.core.model.pctype.Modifier.PUBLISHED)) {
            return;
        }
        generateFieldConstantForProperty(attribute, datatypeHelper.getDatatype(), constantBuilder);
        super.generateCodeForAttribute(attribute, datatypeHelper, constantBuilder, memberVarsBuilder, methodsBuilder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
     
        if (attribute.isProductRelevant()) {
            generateMethodGetPropertyValue(attribute, datatypeHelper, methodsBuilder);
        } else {
            generateFieldConstPropertyValue(attribute, datatypeHelper, constantBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForChangeableAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMethodGetPropertyValue(attribute, datatypeHelper, methodsBuilder);
        generateMethodSetPropertyValue(attribute, datatypeHelper, methodsBuilder);
        DatatypeHelper nonPrimitiveDatatypeHelper = StdBuilderHelper.
            getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), datatypeHelper);
        if(ValueSetType.RANGE.equals(attribute.getValueSet().getValueSetType())){
            generateFieldMaxRangeFor(attribute, nonPrimitiveDatatypeHelper, memberVarsBuilder);
            productCmptGenInterfaceBuilder.generateMethodGetRangeFor(attribute, nonPrimitiveDatatypeHelper, methodsBuilder);
        }
        else if(ValueSetType.ENUM.equals(attribute.getValueSet().getValueSetType()) ||
                datatypeHelper.getDatatype() instanceof EnumDatatype){
            generateFieldMaxAllowedValuesFor(attribute, nonPrimitiveDatatypeHelper, memberVarsBuilder);
            productCmptGenInterfaceBuilder.generateMethodGetAllowedValuesFor(
                    attribute, datatypeHelper.getDatatype(), methodsBuilder);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMethodGetPropertyValue(attribute, datatypeHelper, methodsBuilder);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMethodGetPropertyValue(attribute, datatypeHelper, methodsBuilder);
    }
    
    void generateFieldConstPropertyValue(
            IAttribute a,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        
        String comment = getLocalizedText(a, "FIELD_VALUE_JAVADOC", a.getName());
        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        String varName = getJavaNamingConvention().getConstantClassVarName(a.getName());
        int modifier = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC; 
        JavaCodeFragment initialValueExpression = helper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(modifier, helper.getJavaClassName(), varName, initialValueExpression);
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Money getPremium();
     * </pre>
     */
    public void generateMethodGetPropertyValue(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String description = StringUtils.isEmpty(a.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR + a.getDescription();
        String[] replacements = new String[]{a.getName(), description};
        appendLocalizedJavaDoc("METHOD_GETVALUE", replacements, a, methodsBuilder);
        generateSignatureGetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.appendln(";");
    }
        
    /**
     * Code sample:
     * <pre>
     * public Money getPremium()
     * </pre>
     */
    public void generateSignatureGetPropertyValue(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(a, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(), methodName, new String[0], new String[0]);
    }
    
    /**
     * Returns the getter method to access a property/attribute value.
     */
    public String getMethodNameGetPropertyValue(IAttribute a, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(a.getName(), datatype);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setPremium(Money newValue);
     * </pre>
     */
    public void generateMethodSetPropertyValue(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String description = StringUtils.isEmpty(a.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR + a.getDescription();
        String[] replacements = new String[]{a.getName(), description};
        appendLocalizedJavaDoc("METHOD_SETVALUE", replacements, a, methodsBuilder);
        generateSignatureSetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public void setPremium(Money newValue)
     * </pre>
     */
    public void generateSignatureSetPropertyValue(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getJavaNamingConvention().getSetterMethodName(a.getName(), datatypeHelper.getDatatype());
        String paramName = getParamNameForSetPropertyValue(a);
        methodsBuilder.signature(modifier, "void", methodName, new String[]{paramName}, new String[]{datatypeHelper.getJavaClassName()});
    }
    
    /**
     * Returns the name of the parameter in the setter method for a property,
     * e.g. newValue.
     */
    public String getParamNameForSetPropertyValue(IAttribute a) {
        return getLocalizedText(a, "PARAM_NEWVALUE_NAME", a.getName());
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForRelationInCommon(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if(!relation.isReadOnlyContainer() && 
           !relation.getRelationType().isCompositionDetailToMaster()){
            generateFieldGetMaxCardinalityFor(relation, fieldsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1To1Relation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        IPolicyCmptType target = relation.findTarget();
        generateMethodGetRefObject(relation, methodsBuilder);
        if (!relation.isReadOnlyContainer() && !relation.getRelationType().isCompositionDetailToMaster()) {
            generateMethodSetObject(relation, methodsBuilder);
            generateNewChildMethodsIfApplicable(relation, target, methodsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyRelation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        IPolicyCmptType target = relation.findTarget();
        generateMethodGetNumOfRefObjects(relation, methodsBuilder);
        generateMethodGetAllRefObjects(relation, methodsBuilder);
        generateMethodContainsObject(relation, methodsBuilder);
        if (!relation.isReadOnlyContainer()) {
            generateMethodAddObject(relation, methodsBuilder);
            generateMethodRemoveObject(relation, methodsBuilder);
            generateNewChildMethodsIfApplicable(relation, target, methodsBuilder);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages();
     * </pre>
     */
    protected void generateMethodGetNumOfRefObjects(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_NUM_OF", relation.getTargetRolePlural(), relation, methodsBuilder);
        generateSignatureGetNumOfRefObjects(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverages()
     * </pre>
     */
    public void generateSignatureGetNumOfRefObjects(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameGetNumOfRefObjects(relation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "int", methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    public String getMethodNameGetNumOfRefObjects(IRelation relation) {
        return getLocalizedText(relation, "METHOD_GET_NUM_OF_NAME", StringUtils.capitalise(relation.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages();
     * </pre>
     */
    protected void generateMethodGetAllRefObjects(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_ALL_REF_OBJECTS", relation.getTargetRolePlural(), relation, methodsBuilder);
        generateSignatureGetAllRefObjects(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     */
    public void generateSignatureGetAllRefObjects(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameGetAllRefObjects(relation);
        String returnType = getQualifiedClassName(relation.findTarget()) + "[]";
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the referenced objects,
     * e.g. getCoverages()
     */
    public String getMethodNameGetAllRefObjects(IRelation relation) {
        return getLocalizedText(relation, "METHOD_GET_ALL_REF_OBJECTS_NAME", StringUtils.capitalise(relation.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage();
     * </pre>
     */
    protected void generateMethodGetRefObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT", StringUtils.capitalise(relation.getTargetRoleSingular()), relation, methodsBuilder);
        generateSignatureGetRefObject(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    public void generateSignatureGetRefObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameGetRefObject(relation);
        String returnType = getQualifiedClassName(relation.findTarget());
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the single referenced object.
     * e.g. getCoverage()
     */
    public String getMethodNameGetRefObject(IRelation relation) {
        return getLocalizedText(relation, "METHOD_GET_REF_OBJECT_NAME", relation.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd);
     * </pre>
     */
    protected void generateMethodAddObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_ADD_OBJECT", relation.getTargetRoleSingular(), relation, methodsBuilder);
        generateSignatureAddObject(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public void addCoverage(ICoverage objectToAdd)
     * </pre>
     */
    public void generateSignatureAddObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameAddObject(relation);
        String className = getQualifiedClassName(relation.findTarget());
        String paramName = getParamNameForAddObject(relation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{className});
    }
    
    /**
     * Returns the name of the method adding an object to a multi-value relation,
     * e.g. getCoverage()
     */
    public String getMethodNameAddObject(IRelation relation) {
        return getLocalizedText(relation, "METHOD_ADD_OBJECT_NAME", relation.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method adding an object to a multi-value relation,
     * e.g. objectToAdd
     */
    public String getParamNameForAddObject(IRelation relation) {
        return getLocalizedText(relation, "PARAM_OBJECT_TO_ADD_NAME", relation.getTargetRoleSingular());
    }
    
    /**
     * Code sample without product component parameter:
     * <pre>
     * [Javadoc]
     * public Coverage newCoverage();
     * </pre>
     * 
     * Code sample with product component parameter:
     * [Javadoc]
     * <pre>
     * public Coverage newCoverage(CoverageType coverageType);
     * </pre>
     */
    public void generateMethodNewChild(
            IRelation relation, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        String targetTypeName = target.getName();
        String role = relation.getTargetRoleSingular();
        if (inclProductCmptArg) { 
            String replacements[] = new String[]{targetTypeName, role, getParamNameForProductCmptInNewChildMethod(target.findProductCmptType())};
            appendLocalizedJavaDoc("METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", replacements, relation, builder);
        } else {
            appendLocalizedJavaDoc("METHOD_NEW_CHILD", new String[]{targetTypeName, role}, relation, builder);
        }
        generateSignatureNewChild(relation, target, inclProductCmptArg, builder);
        builder.appendln(";");
    }

    /**
     * Code sample without product component argument:
     * <pre>
     * public Coverage newCoverage()
     * </pre>
     * 
     * Code sample with product component argument:
     * <pre>
     * public Coverage newCoverage(ICoverageType coverageType)
     * </pre>
     */
    public void generateSignatureNewChild(
            IRelation relation, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        String methodName = getMethodNameNewChild(relation);
        String returnType = getQualifiedClassName(relation.findTarget());
        String[] argNames, argTypes;
        if (inclProductCmptArg) {
            IProductCmptType productCmptType = target.findProductCmptType();
            argNames = new String[]{getParamNameForProductCmptInNewChildMethod(productCmptType)};
            argTypes = new String[]{productCmptInterfaceBuilder.getQualifiedClassName(productCmptType)};
        } else {
            argNames = new String[0];
            argTypes = new String[0];
        }
        builder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, argNames, argTypes);
    }
    
    /**
     * Returns the name of the method to create a new child object and add it to the parent. 
     */
    public String getMethodNameNewChild(IRelation relation) {
        return getLocalizedText(relation, "METHOD_NEW_CHILD_NAME", relation.getTargetRoleSingular());
    }

    /**
     * Returns the name of the parameter in the new child mthod, e.g. coverageType.
     */
    protected String getParamNameForProductCmptInNewChildMethod(IProductCmptType targetProductCmptType) throws CoreException {
        String targetProductCmptClass = productCmptInterfaceBuilder.getQualifiedClassName(targetProductCmptType);
        return StringUtils.uncapitalise(StringUtil.unqualifiedName(targetProductCmptClass));
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void removeCoverage(ICoverage objectToRemove);
     * </pre>
     */
    protected void generateMethodRemoveObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_REMOVE_OBJECT", relation.getTargetRoleSingular(), relation, methodsBuilder);
        generateSignatureRemoveObject(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public void removeCoverage(ICoverage objectToRemove)
     * </pre>
     */
    public void generateSignatureRemoveObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameRemoveObject(relation);
        String className = getQualifiedClassName(relation.findTarget());
        String paramName = getParamNameForRemoveObject(relation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{className});
    }
    
    /**
     * Returns the name of the method removing an object from a multi-value relation,
     * e.g. removeCoverage()
     */
    public String getMethodNameRemoveObject(IRelation relation) {
        return getLocalizedText(relation, "METHOD_REMOVE_OBJECT_NAME", relation.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method removing an object from a multi-value relation,
     * e.g. objectToRemove
     */
    public String getParamNameForRemoveObject(IRelation relation) {
        return getLocalizedText(relation, "PARAM_OBJECT_TO_REMOVE_NAME", relation.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    protected void generateMethodContainsObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", relation.getTargetRoleSingular(), relation, methodsBuilder);
        generateSignatureContainsObject(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public boolean containsCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureContainsObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameContainsObject(relation);
        String paramClass = getQualifiedClassName(relation.findTarget());
        String paramName = getParamNameForContainsObject(relation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "boolean", methodName, new String[]{paramName}, new String[]{paramClass});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    public String getMethodNameContainsObject(IRelation relation) {
        return getLocalizedText(relation, "METHOD_CONTAINS_OBJECT_NAME", relation.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a multi-value relation,
     * e.g. objectToTest
     */
    public String getParamNameForContainsObject(IRelation relation) {
        return getLocalizedText(relation, "PARAM_OBJECT_TO_TEST_NAME", relation.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject);
     * </pre>
     */
    protected void generateMethodSetObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_OBJECT", relation.getTargetRoleSingular(), relation, methodsBuilder);
        generateSignatureSetObject(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public void setCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureSetObject(
            IRelation relation,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameSetObject(relation);
        String paramClass = getQualifiedClassName(relation.findTarget());
        String paramName = getParamNameForSetObject(relation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{paramClass});
    }
    
    /**
     * Returns the name of the method setting the referenced object.
     * e.g. setCoverage(ICoverage newObject)
     */
    public String getMethodNameSetObject(IRelation relation) {
        return getLocalizedText(relation, "METHOD_SET_OBJECT_NAME", relation.getTargetRoleSingular());
    }
    
    /**
     * Returns the name of the method that adds an object to a toMany relation or that sets
     * the object in a to1 relation respectively.
     */
    public String getMethodNameAddOrSetObject(IRelation relation) {
        if (relation.is1ToMany()) {
            return getMethodNameAddObject(relation);
        } else {
            return getMethodNameSetObject(relation);
        }
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a multi-value relation,
     * e.g. objectToTest
     */
    public String getParamNameForSetObject(IRelation relation) {
        return getLocalizedText(relation, "PARAM_OBJECT_TO_SET_NAME", relation.getTargetRoleSingular());
    }
    
    /**
     * Empty implementation.
     * 
     * overidden
     */
    protected void generateCodeForContainerRelationImplementation(
            IRelation containerRelation,
            List subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }
    
    public void generateSignatureGetMaxCardinalityFor(IRelation relation, JavaCodeFragmentBuilder methodsBuilder){
        String methodName = getMethodNameGetMaxCardinalityFor(relation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, IntegerRange.class.getName(), methodName, new String[0], new String[0]);
    }
    
    public String getMethodNameGetMaxCardinalityFor(IRelation relation){
        return getLocalizedText(relation, "METHOD_GET_MAX_CARDINALITY_NAME", StringUtils.capitalise(relation.getTargetRoleSingular()));
    }
    
    public void generateMethodGetMaxCardinalityFor(IRelation relation, JavaCodeFragmentBuilder methodsBuilder){
        String[] replacements = new String[]{relation.getTargetRoleSingular()};
        appendLocalizedJavaDoc("METHOD_GET_MAX_CARDINALITY", replacements, getPcType(), methodsBuilder);
        generateSignatureGetMaxCardinalityFor(relation, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    public String getPropertyName(IAttribute a, Datatype datatype){
        if(datatype.isPrimitive()){
            return getLocalizedText(a, "FIELD_PROPERTY_PRIMITIVE_NAME", StringUtils.upperCase(a.getName()));    
        }
        return getLocalizedText(a, "FIELD_PROPERTY_NAME", StringUtils.upperCase(a.getName()));
    }
    
    public void generateFieldConstantForProperty(IAttribute a, Datatype datatype, JavaCodeFragmentBuilder membersBuilder){
        if(a.getOverwrites()){
            return;
        }
        appendLocalizedJavaDoc("FIELD_PROPERTY_NAME", a.getName(), a, membersBuilder);
        membersBuilder.append("public final static ");
        membersBuilder.appendClassName(String.class);
        membersBuilder.append(' ');
        membersBuilder.append(getPropertyName(a, datatype));
        membersBuilder.append(" = \"");
        membersBuilder.append(a.getName());
        membersBuilder.appendln("\";");
    }
    
    public String getFieldNameForMsgCode(IValidationRule rule){
        return getLocalizedText(rule, "FIELD_MSG_CODE_NAME", StringUtils.upperCase(rule.getName()));
    }
    
    private void generateFieldForMsgCode(IValidationRule rule, JavaCodeFragmentBuilder membersBuilder){
        appendLocalizedJavaDoc("FIELD_MSG_CODE", rule.getName(), rule, membersBuilder);
        membersBuilder.append("public final static ");
        membersBuilder.appendClassName(String.class);
        membersBuilder.append(' ');
        membersBuilder.append(getFieldNameForMsgCode(rule));
        membersBuilder.append(" = \"");
        membersBuilder.append(rule.getMessageCode());
        membersBuilder.appendln("\";");
    }
    
    public String getFieldNameMaxRangeFor(IAttribute a){
        return getLocalizedText(a, "FIELD_MAX_RANGE_FOR_NAME", StringUtils.upperCase(a.getName()));
    }
    
    public void generateFieldMaxRangeFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder membersBuilder){
        appendLocalizedJavaDoc("FIELD_MAX_RANGE_FOR", a.getName(), a, membersBuilder);
        IRangeValueSet range = (IRangeValueSet)a.getValueSet();
        JavaCodeFragment containsNullFrag = new JavaCodeFragment();
        containsNullFrag.append(range.getContainsNull());
        JavaCodeFragment frag = helper.newRangeInstance(
                createCastExpression(range.getLowerBound(), helper), 
                createCastExpression(range.getUpperBound(), helper), createCastExpression(range.getStep(), helper), 
                containsNullFrag);
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | 
                                      java.lang.reflect.Modifier.FINAL | 
                                      java.lang.reflect.Modifier.STATIC, 
                                      helper.getRangeJavaClassName(), 
                                      getFieldNameMaxRangeFor(a), frag);
    }
    
    private JavaCodeFragment createCastExpression(String bound, DatatypeHelper helper){
        JavaCodeFragment frag = new JavaCodeFragment();
        if(StringUtils.isEmpty(bound)){
            frag.append('(');
            frag.appendClassName(helper.getJavaClassName());
            frag.append(')');
        }
        frag.append(helper.newInstance(bound));
        return frag;
    }
    
    public String getFieldNameMaxAllowedValuesFor(IAttribute a){
        return getLocalizedText(a, "FIELD_MAX_ALLOWED_VALUES_FOR_NAME", StringUtils.upperCase(a.getName()));
    }

    public void generateFieldMaxAllowedValuesFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder membersBuilder){
        appendLocalizedJavaDoc("FIELD_MAX_ALLOWED_VALUES_FOR", a.getName(), a, membersBuilder);
        String[] valueIds = new String[0];
        boolean containsNull = false;
        if(a.getValueSet() instanceof IEnumValueSet){
            IEnumValueSet set = (IEnumValueSet)a.getValueSet();
            valueIds = set.getValues();
            containsNull = set.getContainsNull();
        }
        else if(helper.getDatatype() instanceof EnumDatatype){
            valueIds = ((EnumDatatype)helper.getDatatype()).getAllValueIds(true);
            containsNull = true;
        }
        else{
            throw new IllegalArgumentException("This method can only be call with a value for parameter 'a' " +
                    "that is an IAttibute that bases on an EnumDatatype or contains an EnumValueSet.");
        }
        JavaCodeFragment frag = null;
        if(helper.getDatatype().isPrimitive()){
            Datatype wrapperType = ((ValueDatatype)helper.getDatatype()).getWrapperType();
            helper = getIpsSrcFile().getIpsProject().getDatatypeHelper(wrapperType);
            containsNull = false;
        }
        frag = helper.newEnumValueSetInstance(valueIds, containsNull);
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | 
                java.lang.reflect.Modifier.FINAL | 
                java.lang.reflect.Modifier.STATIC, 
                EnumValueSet.class, 
                getFieldNameMaxAllowedValuesFor(a), frag);
    }
    
    protected void generateFieldGetMaxCardinalityFor(IRelation relation, JavaCodeFragmentBuilder attrBuilder){
        appendLocalizedJavaDoc("FIELD_MAX_CARDINALITY", relation.getTargetRoleSingular(), relation, attrBuilder);
        String fieldName = getFieldNameGetMaxCardinalityFor(relation);
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new ");
        frag.appendClassName(IntegerRange.class);
        frag.append("(");
        frag.append(relation.getMinCardinality());
        frag.append(", ");
        frag.append(relation.getMaxCardinality());
        frag.append(")");
        attrBuilder.varDeclaration(
                java.lang.reflect.Modifier.PUBLIC | 
                java.lang.reflect.Modifier.FINAL | 
                java.lang.reflect.Modifier.STATIC, IntegerRange.class, fieldName, frag);
        attrBuilder.appendln();
    }

    /**
     * Returns the name for the field GetMaxCardinalityFor + single target role of the provided relation
     */
    public String getFieldNameGetMaxCardinalityFor(IRelation relation){
        return getLocalizedText(getPcType(), "FIELD_MAX_CARDINALITY_NAME", 
                StringUtils.upperCase(relation.getTargetRoleSingular()));
    }
 }