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
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
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
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.IPolicyComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    public PolicyCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class));
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
        String javaSupertype = IPolicyComponent.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(getPcType().getSupertype());
            javaSupertype = supertype == null ? javaSupertype : getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return new String[] { javaSupertype };
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
        if (getPcType().isConfigurableByProductCmptType()) {
            generateMethodGetProductCmpt(methodsBuilder);
            generateMethodSetProductCmpt(methodsBuilder);
            generateMethodGetProductCmptGeneration(getProductCmptType(), methodsBuilder);
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
    protected void generateCodeForAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (!(attribute.getModifier() == org.faktorips.devtools.core.model.pctype.Modifier.PUBLISHED)) {
            return;
        }
        super.generateCodeForAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
     
        if (attribute.isProductRelevant()) {
            generateMethodGetPropertyValue(attribute, datatypeHelper, methodsBuilder);
        } else {
            generateFieldConstPropertyValue(attribute, datatypeHelper, memberVarsBuilder);
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
        String varName = getJavaNamingConvention().getMemberVarName(a.getName());
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

        appendLocalizedJavaDoc("METHOD_GETVALUE", a.getName(), a, methodsBuilder);
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
        
        appendLocalizedJavaDoc("METHOD_SETVALUE", a.getName(), a, methodsBuilder);
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
    protected void generateCodeFor1To1Relation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMethodGetRefObject(relation, methodsBuilder);
        if (!relation.isReadOnlyContainer() && !relation.getRelationType().isReverseComposition()) {
            generateMethodSetObject(relation, methodsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyRelation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMethodGetNumOfRefObjects(relation, methodsBuilder);
        generateMethodGetAllRefObjects(relation, methodsBuilder);
        generateMethodContainsObject(relation, methodsBuilder);
        if (!relation.isReadOnlyContainer()) {
            generateMethodAddObject(relation, methodsBuilder);
            generateMethodRemoveObject(relation, methodsBuilder);
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
        return getLocalizedText(relation, "METHOD_GET_NUM_OF_NAME", relation.getTargetRolePlural());
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
        return getLocalizedText(relation, "METHOD_GET_ALL_REF_OBJECTS_NAME", relation.getTargetRolePlural());
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

        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT", relation.getTargetRoleSingular(), relation, methodsBuilder);
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
        if (a.getValueSet() != null && a.getValueSet().getValueSetType() != ValueSetType.ALL_VALUES) {
            String methodNameMax = getPolicyCmptInterfaceGetMaxValueSetMethodName(a);
            String methodName = getPolicyCmptInterfaceGetValueSetMethodName(a);
            String javaDocMax = getLocalizedText(a, JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
            String javaDoc = getLocalizedText(a, JAVA_GETTER_METHOD_VALUESET, a.getName());
            if (a.getValueSet().getValueSetType() == ValueSetType.RANGE) {
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

    private String getPolicyCmptInterfaceValueSetFieldName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetField(JavaCodeFragmentBuilder memberVarsBuilder, IAttribute a, Datatype datatype, DatatypeHelper helper)
            throws CoreException {
        String fieldName = getPolicyCmptInterfaceValueSetFieldName(a);
        String dataTypeValueSet;
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();

        if (a.getValueSet().getValueSetType() == ValueSetType.RANGE) {
            dataTypeValueSet = helper.getRangeJavaClassName();
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(helper.getRangeJavaClassName());
            initialValueExpression.append("( ");
            initialValueExpression.append(helper.newInstance(((IRangeValueSet)a.getValueSet()).getLowerBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((IRangeValueSet)a.getValueSet()).getUpperBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((IRangeValueSet)a.getValueSet()).getStep()));
            initialValueExpression.append(" ) ");
        } else {
            dataTypeValueSet = datatype.getJavaClassName() + "[]";
            initialValueExpression = new JavaCodeFragment();
            String[] elements = ((IEnumValueSet)a.getValueSet()).getValues();
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
        String comment = getLocalizedText(a, "FIELD_VALUESET_JAVADOC", a.getName());
        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(
                java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                        | java.lang.reflect.Modifier.STATIC, dataTypeValueSet, fieldName, initialValueExpression);
    }

    
}