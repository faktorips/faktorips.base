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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociationTo1;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociationToMany;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenConstantAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenDerivedAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.IntegerRange;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    private boolean generateDeltaSupport = false;
    private boolean generateCopySupport = false;
    
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    
    public PolicyCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId, boolean changeListenerSupportActive) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class), changeListenerSupportActive);
        setMergeEnabled(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return this;
    }

    public ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return productCmptGenInterfaceBuilder;
    }

    public ProductCmptInterfaceBuilder getProductCmptInterfaceBuilder() {
        return productCmptInterfaceBuilder;
    }

    /**
     * {@inheritDoc}
     */
    protected GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet stringsSet) throws CoreException {
        if (!a.getModifier().isPublished()) {
            return null;
        }
        if (a.isDerived()) {
            return new GenDerivedAttribute(a, this, stringsSet);
        }
        if (a.isChangeable()) {
            return new GenChangeableAttribute(a, this, stringsSet);
        }
        return new GenConstantAttribute(a, this, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAttribute createGenerator(IProductCmptTypeAttribute a, LocalizedStringsSet stringsSet) throws CoreException {
        return new GenProdAttribute(a, this, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected GenAssociation createGenerator(IPolicyCmptTypeAssociation association, LocalizedStringsSet stringsSet) throws CoreException {
        if (association.is1ToMany()) {
            return new GenAssociationToMany(association, this, stringsSet);
        }
        return new GenAssociationTo1(association, this, stringsSet);
    }

    public boolean isGenerateDeltaSupport() {
        return generateDeltaSupport;
    }

    public void setGenerateDeltaSupport(boolean generateDeltaSupport) {
        this.generateDeltaSupport = generateDeltaSupport;
    }
    
    public boolean isGenerateCopySupport() {
        return generateCopySupport;
    }

    public void setGenerateCopySupport(boolean generateCopySupport) {
        this.generateCopySupport = generateCopySupport;
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
        return StringUtils.capitalize(name);
    }
    
    public String getPolicyCmptTypeName(IPolicyCmptType type) throws CoreException {
        return StringUtils.capitalize(type.getName());
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
        List interfaces = new ArrayList();
        IPolicyCmptType type = getPcType();
        IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(getIpsProject());
        if (supertype != null) {
            interfaces.add(getQualifiedClassName(supertype));
        } else {
            if (type.isConfigurableByProductCmptType()) {
                interfaces.add(IConfigurableModelObject.class.getName());
            } else {
                interfaces.add(IModelObject.class.getName());
            }
            if (generateDeltaSupport) {
                interfaces.add(IDeltaSupport.class.getName());
            }
            if (generateCopySupport) {
                interfaces.add(ICopySupport.class.getName());
            }
        }
        if (isFirstDependantTypeInHierarchy(type)) {
            interfaces.add(IDependantObject.class.getName());
        }
        return (String[])interfaces.toArray(new String[interfaces.size()]);
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
        appendLocalizedJavaDoc("INTERFACE", getIpsObject().getName(), getIpsObject().getDescription(), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (getProductCmptType() != null) {
            if (hasValidProductCmptTypeName()) {
                generateMethodGetProductCmpt(methodsBuilder);
                generateMethodSetProductCmpt(methodsBuilder);
                generateMethodGetProductCmptGeneration(getProductCmptType(), methodsBuilder);
            }
        }
        // TODO remove
        // generateCodeForValidationRules(memberVarsBuilder);
    }
    
    /**
     * Generates the code for the rules of the ProductCmptType assigned to this builder.
     */
    //FIXME remove 
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
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
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
        String[] replacements = new String[]{getProductCmptType().getName(), StringUtils.uncapitalize(getProductCmptType().getName()), "initPropertiesWithConfiguratedDefaults"};
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
        return new String[] { StringUtils.uncapitalize(type.getName()), "initPropertiesWithConfiguratedDefaults" };
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getNameForGenerationConcept(type), type.getName(), getPcType().getName()};
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
                EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
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
    //TODO remove
    protected void generateCodeForMethodDefinedInModel(
            IMethod method,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
//        if (method.getModifier() != Modifier.PUBLISHED) {
//            return;
//        }
//        methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
//        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, paramTypes, methodsBuilder);
//        methodsBuilder.appendln(";");
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
    protected void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        //empty implementation
    }

    /**
     * Code sample:
     * <pre>
     * public Money getPremium()
     * </pre>
     */
    public void generateSignatureGetPropertyValue(
            String propName,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(propName, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
    
    /**
     * Returns the getter method to access a property/attribute value.
     */
    public String getMethodNameGetPropertyValue(String propName, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

    public String getMethodNametSetPropertyValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper){
        return getJavaNamingConvention().getSetterMethodName(a.getName(), datatypeHelper.getDatatype());
    }
    
    public void generateCallToMethodSetPropertyValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, 
            JavaCodeFragment value, JavaCodeFragmentBuilder builder){
        builder.append(getMethodNametSetPropertyValue(a, datatypeHelper));
        builder.append('(');
        builder.append(value);
        builder.append(");");
    }
    
    /**
     * Returns the name of the parameter in the setter method for a property,
     * e.g. newValue.
     */
    public String getParamNameForSetPropertyValue(IPolicyCmptTypeAttribute a) {
        return getLocalizedText(a, "PARAM_NEWVALUE_NAME", a.getName());
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAssociationInCommon(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if(association.isQualified()){
            generateMethodGetRefObjectByQualifier(association, methodsBuilder);
        }
        if(!association.isDerivedUnion() && 
           !association.getAssociationType().isCompositionDetailToMaster()){
            generateFieldGetMaxCardinalityFor(association, fieldsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1To1Association(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenAssociation generator = getGenerator(association);
        generator.generateMethods(methodsBuilder, generatesInterface());
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyAssociation(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenAssociation generator = getGenerator(association);
        generator.generateMethods(methodsBuilder, generatesInterface());
    }
    
    /**
     * Returns the name of the method setting the referenced object.
     * e.g. setCoverage(ICoverage newObject)
     */
    public String getMethodNameSetObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_SET_OBJECT_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverages()
     * </pre>
     */
    public void generateSignatureGetNumOfRefObjects(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameGetNumOfRefObjects(association);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "int", methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    public String getMethodNameGetNumOfRefObjects(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_GET_NUM_OF_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     */
    // TODO remove
    public void generateSignatureGetAllRefObjects(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameGetAllRefObjects(association);
        String returnType = getQualifiedClassName(association.findTarget(getIpsProject())) + "[]";
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the referenced objects,
     * e.g. getCoverages()
     */
    // TODO remove
    public String getMethodNameGetAllRefObjects(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_GET_ALL_REF_OBJECTS_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    public void generateSignatureGetRefObject(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameGetRefObject(association);
        String returnType = getQualifiedClassName(association.findTarget(getIpsProject()));
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the single referenced object.
     * e.g. getCoverage()
     */
    public String getMethodNameGetRefObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_GET_REF_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage(ICoverageType qualifier);
     * </pre>
     */
    protected void generateMethodGetRefObjectByQualifier(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_QUALIFIER", StringUtils.capitalize(association.getTargetRoleSingular()), association, methodsBuilder);
        generateSignatureGetRefObjectByQualifier(association, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public ICoverage getCoverage(ICoverageType qualifier)
     * </pre>
     */
    public void generateSignatureGetRefObjectByQualifier(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetRefObject(association);
        String returnType = getQualifiedClassName(association.findTarget(getIpsProject()));
        if(association.is1ToManyIgnoringQualifier()){
            returnType = returnType + "[]";
        }
        IProductCmptType qualifier = association.findQualifier(getIpsProject());
        String qualifierClassName = this.productCmptInterfaceBuilder.getQualifiedClassName(qualifier.getIpsSrcFile());
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName,
                new String[] { "qualifier" }, new String[] { qualifierClassName });
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
            IPolicyCmptTypeAssociation association, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        String targetTypeName = target.getName();
        String role = association.getTargetRoleSingular();
        if (inclProductCmptArg) { 
            String replacements[] = new String[]{targetTypeName, role, getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject()))};
            appendLocalizedJavaDoc("METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", replacements, association, builder);
        } else {
            appendLocalizedJavaDoc("METHOD_NEW_CHILD", new String[]{targetTypeName, role}, association, builder);
        }
        generateSignatureNewChild(association, target, inclProductCmptArg, builder);
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
            IPolicyCmptTypeAssociation association, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        String methodName = getMethodNameNewChild(association);
        String returnType = getQualifiedClassName(association.findTarget(getIpsProject()));
        String[] argNames, argTypes;
        if (inclProductCmptArg) {
            IProductCmptType productCmptType = target.findProductCmptType(getIpsProject());
            argNames = new String[]{getParamNameForProductCmptInNewChildMethod(productCmptType)};
            argTypes = new String[]{productCmptInterfaceBuilder.getQualifiedClassName(productCmptType)};
        } else {
            argNames = EMPTY_STRING_ARRAY;
            argTypes = EMPTY_STRING_ARRAY;
        }
        builder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, argNames, argTypes);
    }
    
    /**
     * Returns the name of the method to create a new child object and add it to the parent. 
     */
    public String getMethodNameNewChild(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_NEW_CHILD_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the parameter in the new child mthod, e.g. coverageType.
     */
    protected String getParamNameForProductCmptInNewChildMethod(IProductCmptType targetProductCmptType) throws CoreException {
        String targetProductCmptClass = productCmptInterfaceBuilder.getQualifiedClassName(targetProductCmptType);
        return StringUtils.uncapitalize(StringUtil.unqualifiedName(targetProductCmptClass));
    }

    /**
     * Code sample:
     * <pre>
     * public void removeCoverage(ICoverage objectToRemove)
     * </pre>
     */
    public void generateSignatureRemoveObject(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameRemoveObject(association);
        String className = getQualifiedClassName(association.findTarget(getIpsProject()));
        String paramName = getParamNameForRemoveObject(association);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{className});
    }
    
    /**
     * Returns the name of the method removing an object from a multi-value association,
     * e.g. removeCoverage()
     */
    public String getMethodNameRemoveObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_REMOVE_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method removing an object from a multi-value association,
     * e.g. objectToRemove
     */
    public String getParamNameForRemoveObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "PARAM_OBJECT_TO_REMOVE_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    // TODO remove
    protected void generateMethodContainsObject(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", association.getTargetRoleSingular(), association, methodsBuilder);
        generateSignatureContainsObject(association, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public boolean containsCoverage(ICoverage objectToTest)
     * </pre>
     */
    // TODO remove
    public void generateSignatureContainsObject(
            IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameContainsObject(association);
        String paramClass = getQualifiedClassName(association.findTarget(getIpsProject()));
        String paramName = getParamNameForContainsObject(association);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "boolean", methodName, new String[]{paramName}, new String[]{paramClass});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    // TODO remove
    public String getMethodNameContainsObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_CONTAINS_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a multi-value association,
     * e.g. objectToTest
     */
    // TODO remove
    public String getParamNameForContainsObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "PARAM_OBJECT_TO_TEST_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Empty implementation.
     * 
     * overidden
     */
    protected void generateCodeForContainerAssociationImplementation(
            IPolicyCmptTypeAssociation containerAssociation,
            List subAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }
    
    public void generateSignatureGetMaxCardinalityFor(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder){
        String methodName = getMethodNameGetMaxCardinalityFor(association);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, IntegerRange.class.getName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
    
    public String getMethodNameGetMaxCardinalityFor(IPolicyCmptTypeAssociation association){
        return getLocalizedText(association, "METHOD_GET_MAX_CARDINALITY_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }
    
    public void generateMethodGetMaxCardinalityFor(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder){
        String[] replacements = new String[]{association.getTargetRoleSingular()};
        appendLocalizedJavaDoc("METHOD_GET_MAX_CARDINALITY", replacements, getPcType(), methodsBuilder);
        generateSignatureGetMaxCardinalityFor(association, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    public String getFieldNameForMsgCode(IValidationRule rule){
        return getLocalizedText(rule, "FIELD_MSG_CODE_NAME", StringUtils.upperCase(rule.getName()));
    }
    
    //FIXME remove
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
    
    protected void generateFieldGetMaxCardinalityFor(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder attrBuilder){
        appendLocalizedJavaDoc("FIELD_MAX_CARDINALITY", association.getTargetRoleSingular(), association, attrBuilder);
        String fieldName = getFieldNameGetMaxCardinalityFor(association);
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new ");
        frag.appendClassName(IntegerRange.class);
        frag.append("(");
        frag.append(association.getMinCardinality());
        frag.append(", ");
        frag.append(association.getMaxCardinality());
        frag.append(")");
        attrBuilder.varDeclaration(
                java.lang.reflect.Modifier.PUBLIC | 
                java.lang.reflect.Modifier.FINAL | 
                java.lang.reflect.Modifier.STATIC, IntegerRange.class, fieldName, frag);
        attrBuilder.appendln();
    }

    /**
     * Returns the name for the field GetMaxCardinalityFor + single target role of the provided association
     */
    public String getFieldNameGetMaxCardinalityFor(IPolicyCmptTypeAssociation association){
        return getLocalizedText(getPcType(), "FIELD_MAX_CARDINALITY_NAME", 
                StringUtils.upperCase(association.getTargetRoleSingular()));
    }
    
    /**
     * Returns the name of the method that returns a reference object at a specified index.
     */
    public String getMethodNameGetRefObjectAtIndex(IPolicyCmptTypeAssociation association){
        //TODO extend JavaNamingConvensions for association accessor an mutator methods 
        return "get" + association.getTargetRoleSingular();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index)
     * </pre>
     */
    public void generateSignatureGetRefObjectAtIndex(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_INDEX", association.getTargetRoleSingular(), association, methodBuilder);
        methodBuilder.signature(java.lang.reflect.Modifier.PUBLIC, getQualifiedClassName(association.findTarget(getIpsProject())), 
                    getMethodNameGetRefObjectAtIndex(association), 
                    new String[]{"index"}, new String[]{Integer.TYPE.getName()});
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index);
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndex(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        generateSignatureGetRefObjectAtIndex(association, methodBuilder);
        methodBuilder.append(';');
    }
 }