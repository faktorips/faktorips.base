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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.MessageFragment;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.IPolicyComponent;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.PolicyCmptChangedEvent;
import org.faktorips.runtime.internal.AbstractConfigurablePolicyComponent;
import org.faktorips.runtime.internal.AbstractConfigurablePolicyComponentPart;
import org.faktorips.runtime.internal.AbstractPolicyComponent;
import org.faktorips.runtime.internal.AbstractPolicyComponentPart;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Element;

public class PolicyCmptImplClassBuilder extends BasePolicyCmptTypeBuilder {

    private final static String VALIDATEDEPS_IMPLEMENTATION_JAVADOC = "VALIDATEDEPS_IMPLEMENTATION_JAVADOC";

    private final static String VALIDATESELF_IMPLEMENTATION_JAVADOC = "VALIDATESELF_IMPLEMENTATION_JAVADOC";

    private final static String CREATEMESSAGEFOR_POLICY_JAVADOC = "CREATEMESSAGEFOR_POLICY_JAVADOC";

    private final static String EXECMESSAGE_POLICY_JAVADOC = "EXECMESSAGE_POLICY_JAVADOC";

    private PolicyCmptInterfaceBuilder interfaceBuilder;
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplBuilder;

    public PolicyCmptImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptImplClassBuilder.class));
        setMergeEnabled(true);
    }

    public void setInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder) {
        this.interfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return interfaceBuilder;
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }
    
    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }
    
    public void setProductCmptGenImplBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplBuilder = builder;
    }

    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        if (getPcType().hasSupertype()) {
            IPolicyCmptType supertype = getPcType().findSupertype();
            if (supertype != null) {
                return getQualifiedClassName(supertype.getIpsSrcFile());
            }
        }
        if (getPcType().isAggregateRoot()) {
            if (getPcType().isConfigurableByProductCmptType()) {
                return AbstractConfigurablePolicyComponent.class.getName(); 
            } else {
                return AbstractPolicyComponent.class.getName();
            }
        } 
        if (getPcType().isConfigurableByProductCmptType()) {
            return AbstractConfigurablePolicyComponentPart.class.getName(); 
        } 
        return AbstractPolicyComponentPart.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return getJavaNamingConvention().getImplementationClassName(StringUtils.capitalise(name));
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { interfaceBuilder.getQualifiedClassName(getPcType()) };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        generateConstructorDefault(builder);
        if (getPcType().findProductCmptType()!=null) {
            generateConstructorWithProductCmptArg(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodInitialize(methodsBuilder);
        if (getPcType().findProductCmptType()!=null) {
            generateMethodGetProductCmpt(methodsBuilder);
            generateMethodGetProductCmptGeneration(methodsBuilder);
            generateMethodSetProductCmpt(methodsBuilder);
        }
        if (getPcType().isAggregateRoot()) {
            generateGetEffectiveFromAsCalendar(methodsBuilder);
        } else {
            if (!getPcType().isAbstract()) {
                generateMethodGetParent(methodsBuilder);
            }
        }
        buildValidation(methodsBuilder);
        generateMethodInitPropertiesFromXml(methodsBuilder);
        generateMethodCreateChildFromXml(methodsBuilder);
        generateMethodCreateUnresolvedReference(methodsBuilder);
    }
    
    protected void generateGetEffectiveFromAsCalendar(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_RESTRAINED_MODIFIABLE);
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Calendar.class, MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR, new String[0], new Class[0]);
        String todoText = getLocalizedText(getPcType(), "METHOD_GET_EFFECTIVE_FROM_TODO");
        methodsBuilder.appendln(JavaSourceFileBuilder.MARKER_BEGIN_USER_CODE);
        methodsBuilder.appendln("return null; // " + getJavaNamingConvention().getToDoMarker() + " " + todoText);
        methodsBuilder.appendln(JavaSourceFileBuilder.MARKER_END_USER_CODE);
        methodsBuilder.methodEnd();
    }
    
    protected void generateMethodGetParent(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int counter=0;
        IRelation reverseComp = null;
        IRelation[] relations = getPcType().getSupertypeHierarchy().getAllRelations(getPcType());
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].getRelationType().isReverseComposition() && !relations[i].isReadOnlyContainer()) {
                reverseComp = relations[i];
                counter++;
            }
        }
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, IPolicyComponent.class, MethodNames.GET_PARENT_POLICY_COMPONENT, new String[0], new Class[0]);
        
        if (counter==1) {
            methodsBuilder.appendln("return " + interfaceBuilder.getMethodNameGetRefObject(reverseComp) + "();");
            methodsBuilder.methodEnd();
            return;
        }
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].getRelationType().isReverseComposition() && !relations[i].isReadOnlyContainer()) {
                String method = interfaceBuilder.getMethodNameGetRefObject(relations[i]);
                methodsBuilder.appendln("if (" + method + "()!=null) {");
                methodsBuilder.appendln("return " + method + "();");
                methodsBuilder.appendln("}");
            }
        }
        methodsBuilder.append("return null;");
        methodsBuilder.methodEnd();
    }

    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if(Modifier.PUBLIC.equals(attribute.getModifier())){
            interfaceBuilder.generateFieldConstantForProperty(attribute, datatypeHelper.getDatatype(), memberVarsBuilder);
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
            generateMethodAttributeGetterFromProductCmpt(attribute, datatypeHelper, methodsBuilder);
        } else {
            if (attribute.getModifier() == org.faktorips.devtools.core.model.pctype.Modifier.PUBLISHED) {
                return;
            }
            interfaceBuilder.generateFieldConstPropertyValue(attribute, datatypeHelper, memberVarsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForChangeableAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        DatatypeHelper nonPrimitiveDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), datatypeHelper);

        if(ValueSetType.RANGE.equals(attribute.getValueSet().getValueSetType())){
            if(Modifier.PUBLIC.equals(attribute.getModifier())){
                interfaceBuilder.generateFieldMaxRangeFor(attribute, nonPrimitiveDatatypeHelper, memberVarsBuilder);
            }
            generateMethodGetRangeFor(attribute, nonPrimitiveDatatypeHelper, methodsBuilder);
        }
        if(ValueSetType.ENUM.equals(attribute.getValueSet().getValueSetType()) ||
                datatypeHelper.getDatatype() instanceof EnumDatatype){
            if(Modifier.PUBLIC.equals(attribute.getModifier())){
                interfaceBuilder.generateFieldMaxAllowedValuesFor(attribute, nonPrimitiveDatatypeHelper, memberVarsBuilder);
            }
            generateMethodGetAllowedValuesFor(attribute, nonPrimitiveDatatypeHelper, methodsBuilder);
        }
        generateFieldForAttribute(attribute, datatypeHelper, memberVarsBuilder);
        generateMethodAttributeGetterFromMemberVar(attribute, datatypeHelper, methodsBuilder);
        generateMethodAttributeSetter(attribute, datatypeHelper, methodsBuilder);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMethodAttributeDerivedGetter(attribute, datatypeHelper, methodsBuilder);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateFieldForAttribute(attribute, datatypeHelper, memberVarsBuilder);
        generateMethodAttributeGetterFromMemberVar(attribute, datatypeHelper, methodsBuilder);
    }
    
    void generateMethodAttributeGetterFromProductCmpt(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaDoc = null; // getLocalizedText(null, a.getName()); // TODO
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
        methodsBuilder.append("().");
        methodsBuilder.append(this.productCmptGenInterfaceBuilder.getMethodNameGetValue(a, datatypeHelper));
        methodsBuilder.append("();");
        methodsBuilder.closeBracket();
    }
    
    protected void generateFieldForAttribute(            
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilders) throws CoreException {
        
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        String comment = getLocalizedText(a, "FIELD_ATTRIBUTE_VALUE_JAVADOC", a.getName());
        String fieldName = getFieldNameForAttribute(a);

        memberVarsBuilders.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilders.varDeclaration(java.lang.reflect.Modifier.PRIVATE, datatypeHelper.getJavaClassName(), fieldName,
            initialValueExpression);
    }
    
    private void generateMethodAttributeDerivedGetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        Parameter[] parameters = a.getFormulaParameters();
        String[] paramNames = BuilderHelper.extractParameterNames(parameters);
        String[] paramTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(
                parameters, a.getIpsProject(), this);

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_MODIFIABLE);
        interfaceBuilder.generateSignatureGetPropertyValue(a, datatypeHelper, builder);
        builder.openBracket();

        if (a.isProductRelevant()) {
            builder.appendln("// TODO Belegung der Berechnungsparameter implementieren");
            JavaCodeFragment paramFragment = new JavaCodeFragment();
            paramFragment.append('(');
            for (int i = 0; i < paramNames.length; i++) {
                builder.appendClassName(paramTypes[i]);
                builder.append(' ');
                builder.append(paramNames[i]);
                builder.append(" = ");
                Datatype paramDataype = a.getIpsProject().findDatatype(parameters[i].getDatatype());
                DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(paramDataype);
                if (helper != null) {
                    JavaCodeFragment nullExpressionFragment = helper.nullExpression();
                    builder.append(nullExpressionFragment);
                } else {
                    builder.append("null");
                }
                builder.appendln(";");
                if (i > 0) {
                    paramFragment.append(", ");
                }
                paramFragment.append(paramNames[i]);
            }
            paramFragment.append(")");
            builder.append(" return ((");
            builder.appendClassName(productCmptGenImplBuilder.getQualifiedClassName(getIpsSrcFile()));
            builder.append(')');
            builder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
            builder.append("()).");
            builder.append(productCmptGenImplBuilder.getMethodNameComputeValue(a));
            builder.append(paramFragment);
            builder.append(";");
        } else {
            builder.append("return ");
            builder.append(datatypeHelper.newInstance(a.getDefaultValue()));
            builder.appendln(";");
        }
        builder.closeBracket();
    }

    /**
     * Returns the name of the field/member variable that stores the values
     * for the property/attribute.
     */
    public String getFieldNameForAttribute(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(a.getName());
    }
    
    /**
     * Code sample:
     * <pre>
     * public Money getPremium() {
     *     return premium;
     * }
     * </pre>
     */
    private void generateMethodAttributeGetterFromMemberVar(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameForAttribute(a));
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * public void setPremium(Money newValue) {
     *     this.premium = newValue;
     * }
     * </pre>
     */
    protected void generateMethodAttributeSetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_RESTRAINED_MODIFIABLE);
        interfaceBuilder.generateSignatureSetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.appendln(MARKER_BEGIN_USER_CODE);
        methodsBuilder.appendln(MARKER_END_USER_CODE);
        methodsBuilder.append("this.");
        methodsBuilder.append(getFieldNameForAttribute(a));
        methodsBuilder.appendln(" = " + interfaceBuilder.getParamNameForSetPropertyValue(a) + ";");
        generateListenerSupport( methodsBuilder, "PolicyCmptChangedEvent.PROPERTY_CHANGED", getFieldNameForAttribute(a) );
        
        methodsBuilder.closeBracket();
    }

    protected void generateListenerSupport(JavaCodeFragmentBuilder methodsBuilder, String event, String fieldName) {
    	if (GENERATE_CHANGE_LISTENER_SUPPORT) {
            methodsBuilder.appendln("if (changeListeners!=null) {");
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(PolicyCmptChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.append(event);
            methodsBuilder.append(", ");
            methodsBuilder.appendQuoted(fieldName);
            methodsBuilder.appendln("));");
            methodsBuilder.appendln("}");
        }
	}

	/**
     * {@inheritDoc}
     */
    protected void generateCodeForRelationInCommon(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1To1Relation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (!relation.isReadOnlyContainer()) {
            IPolicyCmptType target = relation.findTarget();
            generateFieldForRelation(relation, target, fieldsBuilder);
            generateMethodGetRefObjectForNoneContainerRelation(relation, methodsBuilder);
            generateMethodSetRefObject(relation, methodsBuilder);
            generateNewChildMethodsIfApplicable(relation, target, methodsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyRelation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        IPolicyCmptType target = relation.findTarget();
        if (relation.isReadOnlyContainer()) {
            generateMethodContainsObjectForContainerRelation(relation, methodsBuilder);
        } else {
            generateFieldForRelation(relation, target, fieldsBuilder);
            generateMethodGetNumOfForNoneContainerRelation(relation, methodsBuilder);
            generateMethodContainsObjectForNoneContainerRelation(relation, methodsBuilder);
            generateMethodGetAllRefObjectsForNoneContainerRelation(relation, methodsBuilder);
            generateNewChildMethodsIfApplicable(relation, target, methodsBuilder);
            generateMethodAddObject(relation, methodsBuilder);
            generateMethodRemoveObject(relation, methodsBuilder);
        }
    }
    
    protected void generateFieldForRelation(
            IRelation relation,
            IPolicyCmptType target,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaClassname = relation.is1ToMany() ? List.class.getName()
                : getQualifiedClassName(target);
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();
        if (relation.is1ToMany()) {
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(ArrayList.class);
            initialValueExpression.append("()");
        } else {
            initialValueExpression.append("null");
        }
        String comment = getLocalizedText(relation, "FIELD_RELATION_JAVADOC", relation.getName());
        methodsBuilder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, javaClassname, getFieldNameForRelation(relation),
            initialValueExpression);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     return coverages.size();
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfForNoneContainerRelation(IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRefObjects(relation, methodsBuilder);
        methodsBuilder.openBracket();
        String field = getFieldNameForRelation(relation);
        if (relation.is1ToMany()) {
            methodsBuilder.appendln("return " + field + ".size();");
        } else {
            methodsBuilder.appendln("return " + field + "==null ? 0 : 1;");
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     int num = 0; 
     *     num += getNumOfCollisionsCoverages(); 
     *     num += tplCoverage==null ? 0 : 1;
     *     return num;
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfForContainerRelationImplementation(
            IRelation containerRelation, 
            List implRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRefObjects(containerRelation, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("int num = 0;");
        for (int i = 0; i < implRelations.size(); i++) {
            methodsBuilder.appendln();
            IRelation relation = (IRelation)implRelations.get(i);
            methodsBuilder.append("num += ");
            if (relation.is1ToMany()) {
                methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(relation) + "();");
            } else {
                String field = getFieldNameForRelation(relation);
                methodsBuilder.append(field + "==null ? 0 : 1;");
            }
        }
        methodsBuilder.append("return num;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     return coverages.contains(objectToTest);
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForNoneContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String paramName = interfaceBuilder.getParamNameForContainsObject(relation);
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainsObject(relation, methodsBuilder);
        
        methodsBuilder.openBracket();
        String field = getFieldNameForRelation(relation);
        methodsBuilder.appendln("return " + field + ".contains(" + paramName + ");");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     ICoverage[] targets = getCoverages();
     *     for (int i = 0; i < targets.length; i++) {
     *         if (targets[i] == objectToTest)
     *             return true;
     *     }
     *     return false;
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String paramName = interfaceBuilder.getParamNameForContainsObject(relation);
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainsObject(relation, methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(interfaceBuilder.getQualifiedClassName(relation.findTarget()));
        methodsBuilder.append("[] targets = ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetAllRefObjects(relation));
        methodsBuilder.append("();");
        methodsBuilder.append("for(int i=0;i < targets.length;i++) {");
        methodsBuilder.append("if(targets[i] == " + paramName + ") return true; }");
        methodsBuilder.append("return false;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages() {
     *     return (ICoverage[])coverages.toArray(new ICoverage[coverages.size()]);
     * }
     * </pre>
     */
    protected void generateMethodGetAllRefObjectsForNoneContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllRefObjects(relation, methodsBuilder);
        String className = getQualifiedClassName(relation.findTarget());
        String field = getFieldNameForRelation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return (");
        methodsBuilder.appendClassName(className);
        methodsBuilder.append("[])");
        methodsBuilder.append(field);
        methodsBuilder.append(".toArray(new ");
        methodsBuilder.appendClassName(className);
        methodsBuilder.append('[');
        methodsBuilder.append(field);
        methodsBuilder.append(".size()]);");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages() {
     *     ICoverage[] result = new ICoverage[getNumOfCoverages()];
     *     ICoverage[] elements;
     *     counter = 0;
     *     elements = getTplCoverages();
     *     for (int i = 0; i < elements.length; i++) {
     *         result[counter] = elements[i];
     *         counter++;
     *     }
     *     return result;
     * }
     * </pre>
     */
    protected void generateMethodGetAllRefObjectsForContainerRelationImplementation(
            IRelation relation,
            List subRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllRefObjects(relation, methodsBuilder);
        String classname = interfaceBuilder.getQualifiedClassName(relation.findTarget());
        
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(classname);
        methodsBuilder.append("[] result = new ");       
        methodsBuilder.appendClassName(classname);
        methodsBuilder.append("[" + interfaceBuilder.getMethodNameGetNumOfRefObjects(relation) + "()];");       

        boolean elementsVarDefined = false;
        methodsBuilder.append("int counter = 0;");
        for (int i = 0; i < subRelations.size(); i++) {
            IRelation subrel = (IRelation)subRelations.get(i);
            if (subrel.is1ToMany()) {
                if (!elementsVarDefined) {
                    methodsBuilder.appendClassName(classname);   
                    methodsBuilder.append("[] ");
                    elementsVarDefined = true;
                }
                String method = interfaceBuilder.getMethodNameGetAllRefObjects(subrel);
                methodsBuilder.appendln("elements = " + method + "();");
                methodsBuilder.appendln("for (int i=0; i<elements.length; i++) {");
                methodsBuilder.appendln("result[counter++] = elements[i];");
                methodsBuilder.appendln("}");
            } else {
                String method = interfaceBuilder.getMethodNameGetRefObject(subrel);
                methodsBuilder.appendln("if (" + method + "()!=null) {");
                methodsBuilder.appendln("result[counter++] = " + method + "();");
                methodsBuilder.appendln("}");    
            }
        }
        methodsBuilder.append("return result;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return coverage;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectForNoneContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(relation, methodsBuilder);
        String field = getFieldNameForRelation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return " + field + ";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     if(getNumOfTplCoverage() > 0) { 
     *         return getTplCoverage(); 
     *     } 
     *     if (getNumOfCollisionCoverage() > 0) { 
     *         return getCollisionCoverage(); 
     *     } 
     *     return null;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectForContainerRelationImplementation(
            IRelation relation,
            List subRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(relation, methodsBuilder);
        methodsBuilder.openBracket();
        for (int i = 0; i < subRelations.size(); i++) {
            IRelation subrel = (IRelation)subRelations.get(i);
            String accessCode;
            if (subrel.isReadOnlyContainer()) {
                // if the implementation relation is a container relation itself
                // we have to use the access method
                accessCode = interfaceBuilder.getMethodNameGetRefObject(subrel) + "()";
            } else {
                // otherwise we use the field.
                accessCode = getFieldNameForRelation(subrel);
            }
            methodsBuilder.appendln("if (" + accessCode + "!=null) {");
            methodsBuilder.appendln("return " + accessCode + ";");
            methodsBuilder.appendln("}");
        }
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd) {
     *     if(objectToAdd == null) { 
     *         throw new IllegalArgumentException("Can't add null to ...");
     *     }
     *     if (coverages.contains(objectToAdd)) { 
     *         return; 
     *     }
     *     coverages.add(objectToAdd);
     * }
     * </pre>
     */
    protected void generateMethodAddObject (
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureAddObject(relation, methodsBuilder);
        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForAddObject(relation);
        IRelation reverseRelation = relation.findReverseRelation();
        methodsBuilder.openBracket();
        methodsBuilder.append("if (" + paramName + " == null) {");
        methodsBuilder.append("throw new ");
        methodsBuilder.appendClassName(NullPointerException.class);
        methodsBuilder.append("(\"Can't add null to relation " + relation.getName() + " of \" + this); }");
        methodsBuilder.append("if(");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".contains(" + paramName + ")) { return; }");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".add(" + paramName + ");");
        if (reverseRelation != null) {
            String targetClass = interfaceBuilder.getQualifiedClassName(relation.findTarget());
            methodsBuilder.append(generateCodeToSynchronizeReverseRelation(paramName, targetClass, relation, reverseRelation));
        }
        generateListenerSupport(methodsBuilder, "PolicyCmptChangedEvent.PROPERTY_CHILD_ADDED" , fieldname);
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage() {
     *     ICoverage newCoverage = new Coverage();
     *     return newCoverage;
     * }
     * </pre>
     */
    public void generateMethodNewChild(
            IRelation relation, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureNewChild(relation, target, inclProductCmptArg, methodsBuilder);
        String addMethod = relation.is1ToMany() ? interfaceBuilder.getMethodNameAddObject(relation) :
            interfaceBuilder.getMethodNameSetObject(relation);
        String varName = "new" + relation.getTargetRoleSingular();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        methodsBuilder.append(" " + varName + " = new ");
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        if (inclProductCmptArg) {
            methodsBuilder.appendln("(" + interfaceBuilder.getParamNameForProductCmptInNewChildMethod(target.findProductCmptType()) + ");");  
        } else {
            methodsBuilder.appendln("();");
        }
        methodsBuilder.appendln(addMethod + "(" + varName + ");");
        methodsBuilder.appendln(varName + "." + getMethodNameInitialize() + "();");
        methodsBuilder.appendln("return " + varName + ";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void removeMotorCoverage(IMotorCoverage objectToRemove) {
     *     if (objectToRemove == null) {
     *          return;
     *      }
     *      if (motorCoverages.remove(objectToRemove)) {
     *          objectToRemove.setMotorPolicy(null);
     *      }
     *  }
     * </pre>
     */
    protected void generateMethodRemoveObject(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForRemoveObject(relation);
        IRelation reverseRelation = relation.findReverseRelation();

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureRemoveObject(relation, methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + "== null) {return;}");
        
        if (reverseRelation != null) {
            methodsBuilder.append("if(");
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".remove(" + paramName + ")");
        if (reverseRelation != null) {
            methodsBuilder.append(") {");
            methodsBuilder.append(generateCodeToCleanupOldReference(relation, reverseRelation, paramName));
            methodsBuilder.append(" }");
        } else {
            methodsBuilder.append(';');
        }
        generateListenerSupport(methodsBuilder, "PolicyCmptChangedEvent.PROPERTY_CHILD_REMOVED", fieldname);
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *     if (refObject == homeContract)
     *         return;
     *     IHomeContract oldRefObject = homeContract;
     *     homeContract = null;
     *     if (oldRefObject != null) {
     *          oldRefObject.setHomePolicy(null);
     *     }
     *     homeContract = (HomeContract) refObject;
     *     if (refObject != null && refObject.getHomePolicy() != this) {
     *         refObject.setHomePolicy(this);
     *     }
     * }
     * </pre>
     */
    protected void generateMethodSetRefObject(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForSetObject(relation);
        IPolicyCmptType target = relation.findTarget();
        IRelation reverseRelation = relation.findReverseRelation();

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetObject(relation, methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + " == ");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(") return;");
        if (reverseRelation != null) {
            methodsBuilder.appendClassName(getQualifiedClassName(target));
            methodsBuilder.append(" oldRefObject = ");
            methodsBuilder.append(fieldname);
            methodsBuilder.append(';');
            methodsBuilder.append(fieldname);
            methodsBuilder.append(" = null;");
            methodsBuilder.append(generateCodeToCleanupOldReference(relation, reverseRelation, "oldRefObject"));
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        methodsBuilder.append(")" + paramName +";");
        if (reverseRelation != null) {
            methodsBuilder.append(generateCodeToSynchronizeReverseRelation(fieldname, 
                    getQualifiedClassName(target), relation, reverseRelation));
        }
        generateListenerSupport(methodsBuilder, "PolicyCmptChangedEvent.PROPERTY_CHILD_CHANGED", fieldname);
        methodsBuilder.closeBracket();
    }
    
    private JavaCodeFragment generateCodeToSynchronizeReverseRelation(
            String varName,
            String varClassName,
            IRelation relation,
            IRelation reverseRelation) throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        if (!relation.is1ToMany()) {
            code.append(varName + " != null && ");
        }
        if (reverseRelation.is1ToMany()) {
            code.append("! " + varName + ".");
            code.append(interfaceBuilder.getMethodNameContainsObject(reverseRelation) + "(this)");
        } else {
            code.append(varName + ".");
            code.append(interfaceBuilder.getMethodNameGetRefObject(reverseRelation));
            code.append("() != this");
        }
        code.append(") {");
        if (reverseRelation.is1ToMany()) {
            code.append(varName + "." + interfaceBuilder.getMethodNameAddObject(reverseRelation));
        } else {
            String targetClass = getQualifiedClassName(relation.findTarget());
            if (!varClassName.equals(targetClass)) {
                code.append("((");
                code.appendClassName(targetClass);
                code.append(")" + varName + ").");
            } else {
                code.append(varName + ".");
            }
            code.append(interfaceBuilder.getMethodNameSetObject(reverseRelation));
        }
        code.appendln("(this);");
        code.appendln("}");
        return code;
    }
    
    
    private JavaCodeFragment generateCodeToCleanupOldReference(
            IRelation relation, 
            IRelation reverseRelation,
            String varToCleanUp) throws CoreException {
        
        JavaCodeFragment body = new JavaCodeFragment();
        if (!relation.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (reverseRelation.is1ToMany()) {
            String removeMethod = interfaceBuilder.getMethodNameRemoveObject(reverseRelation);
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String targetClass = getQualifiedClassName(relation.findTarget());
            String setMethod = interfaceBuilder.getMethodNameSetObject(reverseRelation);
            body.append("((");
            body.appendClassName(targetClass);
            body.append(")" + varToCleanUp + ")." + setMethod + "(null);");
        }
        if (!relation.is1ToMany()) {
            body.append(" }");
        }
        return body;
    }
    
    /**
     * Returns the name of field/member var for the relation.
     */
    public String getFieldNameForRelation(IRelation relation) throws CoreException {
        if (relation.is1ToMany()) {
            return getJavaNamingConvention().getMemberVarName(relation.getTargetRolePlural());
        } else {
            return getJavaNamingConvention().getMemberVarName(relation.getTargetRoleSingular());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationImplementation(
            IRelation containerRelation,
            List relations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        if (containerRelation.is1ToMany()) {
            generateMethodGetNumOfForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
            generateMethodGetAllRefObjectsForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        } else {
            generateMethodGetRefObjectForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        }
    }

    private void buildValidation(JavaCodeFragmentBuilder builder) throws CoreException {
        generateMethodValidateSelf(builder, getPcType().getAttributes());
        createMethodValidateDependants(builder);
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            if(r.validate().containsErrorMsg()){
                continue;
           }
            generateMethodExecRule(r, builder);
            generateMethodCreateMessageForRule(r, builder);
        }
    }

    private void createMethodValidateDependants(JavaCodeFragmentBuilder builder)
            throws CoreException {
        /*
         * public void validateDependants(MessageList ml) { if(NumOfRelToMany() > 0) { TargetType[]
         * rels = GetAllRelationToMany(); for (int i = 0; i < rels.length; i++) {
         * ml.add(rels[i].validate()); } if (NumOfRelTo1() > 0) {
         * ml.add(GetRelationTo1().validate()); } }
         */

        String methodName = "validateDependants";
        IRelation[] relations = getPcType().getRelations();

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml, businessFunction);");
        for (int i = 0; i < relations.length; i++) {
            IRelation r = relations[i];
            if (!r.validate().containsErrorMsg()) {
                if (r.getRelationType() == RelationType.COMPOSITION
                        && StringUtils.isEmpty(r.getContainerRelation())) {
                    body.appendln();
                    if (r.is1ToMany()) {
                        IPolicyCmptType target = r.getIpsProject().findPolicyCmptType(r.getTarget());
                        body.append("if(");
                        body.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(r));
                        body.append("() > 0) { ");
                        body.appendClassName(interfaceBuilder
                                .getQualifiedClassName(target.getIpsSrcFile()));
                        body.append("[] rels = ");
                        body.append(interfaceBuilder.getMethodNameGetAllRefObjects(r));
                        body.append("();");
                        body.append("for (int i = 0; i < rels.length; i++)");
                        body.append("{ ml.add(rels[i].validate(businessFunction)); } }");
                    } else {
                        String field = getFieldNameForRelation(r);
                        body.append("if (" + field + "!=null) {");
                        body.append("ml.add(" + field + ".validate(businessFunction));");
                        body.append("}");
                    }
                }
            }
        }

        String javaDoc = getLocalizedText(getPcType(), VALIDATEDEPS_IMPLEMENTATION_JAVADOC, getPcType()
                .getName());

        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "ml", "businessFunction" }, 
            new String[] { MessageList.class.getName(), String.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    private void generateMethodValidateSelf(JavaCodeFragmentBuilder builder, IAttribute[] attributes)
            throws CoreException {
        /*
         * public void validateSelf(MessageList ml, String businessFunction) { super.validateSelf(ml, businessFunction); }
         */
        String methodName = "validateSelf";
        String javaDoc = getLocalizedText(getIpsObject(), VALIDATESELF_IMPLEMENTATION_JAVADOC, getPcType()
                .getName());

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if(!");
        body.append("super.");
        body.append(methodName);
        body.append("(ml, businessFunction))");
        body.appendOpenBracket();
        body.append(" return false;");
        body.appendCloseBracket();
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            if(r.validate().isEmpty()){
                body.append("if(!");
                body.append(getMethodExpressionExecRule(r, "ml", "businessFunction"));
                body.append(')');
                body.appendOpenBracket();
                body.append(" return false;");
                body.appendCloseBracket();
            }
        }
        body.appendln(" return true;");
        // buildValidationValueSet(body, attributes); wegschmeissen ??
        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            methodName, new String[] { "ml", "businessFunction" }, 
            new String[] { MessageList.class.getName(), String.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     *   protected Message createMessageForRuleARule(String p0, String p1, String p2) {
     *      ObjectProperty[] objectProperties = new ObjectProperty[] { new ObjectProperty(this, PROPERTY_NAME_A),
     *              new ObjectProperty(this, PROPERTY_NAME_B) };
     *      StringBuffer text = new StringBuffer();
     *      text.append("Check parameters ");
     *      text.append(p0);
     *      text.append(", check if line break works in generated code\n");
     *      text.append(p1);
     *      text.append(" and ");
     *      text.append(p2);
     *      return new Message(MSG_CODE_ARULE, text.toString(), Message.ERROR, objectProperties);
     *  }
     * </pre>
     */
    private void generateMethodCreateMessageForRule(IValidationRule r, JavaCodeFragmentBuilder builder) throws CoreException {
        String pObjectProperties = "objectProperties";
        JavaCodeFragment body = new JavaCodeFragment();
        MessageFragment msgFrag = MessageFragment.createMessageFragment(r.getMessageText(), MessageFragment.VALUES_AS_PARAMETER_NAMES);
        String[] validatedAttributes = r.getValidatedAttributes();
        if(!r.isValidatedAttrSpecifiedInSrc()){
            if(validatedAttributes.length > 0){
                body.appendClassName(ObjectProperty.class);
                body.append("[] ");
                body.append(pObjectProperties);
                body.append(" = new ");
                body.appendClassName(ObjectProperty.class);
                body.append("[]{");
                for (int j = 0; j < validatedAttributes.length; j++) {
                    IAttribute attr = getPcType().getSupertypeHierarchy().findAttribute(getPcType(), validatedAttributes[j]);
                    String propertyConstName = interfaceBuilder.getPropertyName(attr, attr.findDatatype());
                    body.append(" new ");
                    body.appendClassName(ObjectProperty.class);
                    body.append("(this, ");
                    body.append(propertyConstName);
                    body.append(")");
                    if(j < validatedAttributes.length -1){
                        body.append(',');
                    }
                }
                body.appendln("};");
            }
            else{
                body.appendClassName(ObjectProperty.class);
                body.append(" objectProperty");
                body.append(" = new ");
                body.appendClassName(ObjectProperty.class);
                body.appendln("(this);");
            }
        }
        
        body.append(msgFrag.getFrag());
        body.append("return new ");
        body.appendClassName(Message.class);
        body.append('(');
        body.append(interfaceBuilder.getFieldNameForMsgCode(r));
        body.append(", ");
        body.append(msgFrag.getMsgTextExpression());
        body.append(", ");
        body.append(r.getMessageSeverity().getJavaSourcecode());
        body.append(", ");
        body.append(validatedAttributes.length > 0 || r.isValidatedAttrSpecifiedInSrc() ? pObjectProperties : "objectProperty");
        body.append(");");

        List parameterNames = new ArrayList();
        List parameterTypes = new ArrayList();
        if(msgFrag.hasParameters()){
            parameterNames.addAll(Arrays.asList(msgFrag.getParameterNames()));
            for (int j = 0; j < msgFrag.getNumberOfParameters(); j++) {
                parameterTypes.add(String.class.getName());
            }
        }

        if(r.isValidatedAttrSpecifiedInSrc()){
            parameterNames.add(pObjectProperties);
            parameterTypes.add(ObjectProperty.class.getName() + "[]");
        }
        
        String javaDoc = getLocalizedText(r, CREATEMESSAGEFOR_POLICY_JAVADOC, r.getName());
        builder.method(java.lang.reflect.Modifier.PROTECTED, Message.class.getName(),
                "createMessageForRule" + StringUtils.capitalise(r.getName()), (String[])parameterNames.toArray(new String[parameterNames.size()]), 
                (String[])parameterTypes.toArray(new String[parameterTypes.size()]), body, javaDoc, ANNOTATION_GENERATED);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Policy(Product productCmpt) {
     *     super(productCmpt);
     * }
     * </pre>
     */
    protected void generateConstructorWithProductCmptArg(JavaCodeFragmentBuilder builder)
            throws CoreException {

        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getPcType(), builder);
        String[] paramNames = new String[] { "productCmpt" };
        String[] paramTypes = new String[] { 
                productCmptInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile())};
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
                paramNames, paramTypes);
        builder.append("super(productCmpt);");
        builder.methodEnd();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Policy(Product productCmpt, Date effectiveDate) {
     *     super(productCmpt, effectiveDate);
     *     initialize();
     * }
     * </pre>
     */
    protected void generateConstructorDefault(JavaCodeFragmentBuilder builder)
        throws CoreException {

        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getPcType(), builder);
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
                new String[0], new String[0]);
        builder.append("super();");
        builder.methodEnd();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * protected void initialize() {
     *     super.initialize();
     *     paymentMode = getProductGen().getDefaultPaymentMode();
     *     ... and so for other properties
     * }
     * </pre>
     */
    protected void generateMethodInitialize(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_INITIALIZE", getPcType(), builder);
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
                getMethodNameInitialize(), new String[0], new String[0]);
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            builder.append("super." + getMethodNameInitialize() + "();");
        }
        if (getPcType().findProductCmptType()==null) {
            builder.methodEnd();
            return;
        }
        String method = interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType());
        builder.appendln("if (" + method + "()==null) {");
        builder.appendln("return;");
        builder.appendln("}");
        IAttribute[] attributes = getPcType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (!a.validate().containsErrorMsg()) {
                if (a.isProductRelevant() && a.getAttributeType() == AttributeType.CHANGEABLE) {
                    DatatypeHelper datatype = a.getIpsProject().findDatatypeHelper(a.getDatatype());
                    builder.append(getFieldNameForAttribute(a));
                    builder.append(" = ");
                    builder.append(getMethodNameGetValueFromProductCmpt(a, datatype));
                    builder.append(";");
                }
            }
        }
        builder.methodEnd();
    }
    
    /**
     * Returns the method name to initialize the policy component with the default data from
     * the product component.
     */
    public String getMethodNameInitialize() {
        return "initialize";
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IProduct getProduct() {
     *     return (IProduct) getProductComponent();
     * }
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetProductCmpt(getProductCmptType(), builder);
        builder.openBracket();
        String productCmptInterfaceQualifiedName = productCmptInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile());
        builder.append("return (");
        builder.appendClassName(productCmptInterfaceQualifiedName);
        builder.append(")getProductComponent();"); // don't use getMethodNameGetProductComponent() as this results in a recursive call
        // we have to call the generic superclass method here
        builder.closeBracket();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IProductGen getProductGen() {
     *     return (IProductGen) getProduct().getProductGen(getEffectiveFromAsCalendar());
     * }
     * </pre>
     */
    protected void generateMethodGetProductCmptGeneration(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetProductCmptGeneration(getProductCmptType(), builder);
        builder.openBracket();
        builder.appendln("if (getProductComponent()==null) {");
        builder.appendln("return null;");
        builder.appendln("}");
        
        builder.append("return (");
        builder.appendClassName(productCmptGenInterfaceBuilder.getQualifiedClassName(getIpsSrcFile()));
        builder.append(")");
        builder.append(interfaceBuilder.getMethodNameGetProductCmpt(getProductCmptType()));
        builder.append("().");
        builder.append(productCmptInterfaceBuilder.getMethodNameGetGeneration(getPolicyCmptType().findProductCmptType()));
        builder.append('(');
        builder.append(MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR);
        builder.appendln("());");
        builder.closeBracket();
    }
    
    private void generateMethodSetProductCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetProductCmpt(getProductCmptType(), builder);
        String[] paramNames = interfaceBuilder.getMethodParamNamesSetProductCmpt(getProductCmptType());
        builder.openBracket();
        builder.appendln("setProductCmpt(" + paramNames[0] + ");");
        builder.appendln("if(" + paramNames[1] + ") { initialize(); }");
        builder.closeBracket();
    }

    private String getMethodNameGetValueFromProductCmpt(IAttribute a, DatatypeHelper datatype) throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = productCmptGenInterfaceBuilder.getMethodNameGetDefaultValue(a, datatype);
        } else {
            methodName = productCmptGenInterfaceBuilder.getMethodNameGetValue(a, datatype);
        }
        return interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()) + "()." + methodName + "()";
    }
    
    private String getMethodNameExecRule(IValidationRule r){
        return "execRule" + StringUtils.capitalise(r.getName());
    }
    
    private String getMethodExpressionExecRule(IValidationRule r, String messageList, String businessFunction){
        StringBuffer buf = new StringBuffer();
        buf.append(getMethodNameExecRule(r));
        buf.append('(');
        buf.append(messageList);
        buf.append(", ");
        buf.append(businessFunction);
        buf.append(")");
        return  buf.toString();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     *   if ("rules.businessProcess1".equals(businessFunction) || "rules.businessProcess2".equals(businessFunction)) {
     *      //begin-user-code
     *      boolean condition = getA().equals(new Integer(1));
     *      if (condition) {
     *          ml.add(createMessageForRuleARule(String.valueOf(getA()), String.valueOf(getB()), String.valueOf(getHallo())));
     *          return false;
     *      }
     *      return true;
     *      //end-user-code
     *  }
     *  return true;
     * </pre>
     */
    private void generateMethodExecRule(IValidationRule r, JavaCodeFragmentBuilder builder) throws CoreException {
        String parameterBusinessFunction = "businessFunction";
        String javaDoc = getLocalizedText(getIpsObject(), EXECMESSAGE_POLICY_JAVADOC, r.getName());
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln();
        String[] businessFunctions = r.getBusinessFunctions();
        if(!r.isAppliedForAllBusinessFunctions()){
            if(businessFunctions.length > 0){
                body.append("if(");
                for (int j = 0; j < businessFunctions.length; j++) {
                    body.append("\"");
                    body.append(businessFunctions[j]);
                    body.append("\"");
                    body.append(".equals(");
                    body.append(parameterBusinessFunction);
                    body.append(")");
                    if(j < businessFunctions.length - 1){
                        body.appendln(" || ");
                    }
                }
                body.append(")");
                body.appendOpenBracket();
            }
        }
        body.appendln("//begin-user-code");
        body.append("if(");
        if(r.isCheckValueAgainstValueSetRule()){
            IAttribute attr = getPcType().getAttribute(r.getValidatedAttributeAt(0));
            Datatype attrDatatype = attr.findDatatype();
            if(attr.getValueSet().getValueSetType().equals(ValueSetType.ENUM)){
                body.append(productCmptGenInterfaceBuilder.getMethodNameGetAllowedValuesFor(attr, attr.findDatatype()));
                
            }
            else if(attr.getValueSet().getValueSetType().equals(ValueSetType.RANGE)){
                body.append(productCmptGenInterfaceBuilder.getMethodNameGetRangeFor(attr, attrDatatype));
            }
            body.append("(");
            body.append(parameterBusinessFunction);
            body.append(").contains(");
            body.append(interfaceBuilder.getMethodNameGetPropertyValue(attr, attrDatatype));
            body.append("()))");
        }
        else{
            body.append("true) ");
        }
        body.appendOpenBracket();
        body.append("ml.add(createMessageForRule");
        body.append(StringUtils.capitalise(r.getName()));
        MessageFragment msgFrag = MessageFragment.createMessageFragment(r.getMessageText(), MessageFragment.VALUES_AS_PARAMETER_NAMES);
        body.append('(');
        if(msgFrag.hasParameters()){
            String[] parameterNames = msgFrag.getParameterNames();
            for (int j = 0; j < parameterNames.length; j++) {
                body.append("\"\"");
                if(j < parameterNames.length - 1){
                    body.append(", ");
                }
            }
        }

        if(r.isValidatedAttrSpecifiedInSrc()){
            if(msgFrag.hasParameters()){
                body.append(", ");
            }
            body.append("new ");
            body.appendClassName(ObjectProperty.class);
            body.append("[0]");
        }
        
        body.appendln("));");
        body.appendCloseBracket();
        body.appendln(" return true;");
        body.appendln("//end-user-code");
        if(!r.isAppliedForAllBusinessFunctions()){
            if(businessFunctions.length > 0){
                body.appendCloseBracket();
                body.appendln(" return true;");
            }
        }
        builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            getMethodNameExecRule(r), new String[] { "ml", parameterBusinessFunction },
            new String[] { MessageList.class.getName(), String.class.getName() }, body, javaDoc, ANNOTATION_RESTRAINED_MODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(IMethod method, Datatype returnType, Datatype[] paramTypes, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (method.getModifier()==Modifier.PUBLISHED) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_MODIFIABLE);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_MODIFIABLE);
        }
        interfaceBuilder.generateSignatureForMethodDefinedInModel(method, method.getJavaModifier(),
                returnType, paramTypes, methodsBuilder);
        if (method.isAbstract()) {
            methodsBuilder.appendln(";");
            return;
        }
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement model method.");
        methodsBuilder.append("throw new RuntimeException(\"Not implemented yet!\");");
        methodsBuilder.closeBracket();
    }
    
    /**
     * protected void initPropertiesFromXml(HashMap propMap) {
     *     if (propMap.containsKey("prop0")) {
     *         prop0 = (String)propMap.get("prop0");
     *     }
     *     if (propMap.containsKey("prop1")) {
     *         prop1 = (String)propMap.get("prop1");
     *     }
     * }
     */
    private void generateMethodInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, Void.class, MethodNames.INIT_PROPERTIES_FROM_XML, new String[]{"propMap"}, new Class[]{HashMap.class});
        builder.append("super." + MethodNames.INIT_PROPERTIES_FROM_XML + "(propMap);");
        IAttribute[] attributes = getPolicyCmptType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (a.getAttributeType()==AttributeType.DERIVED 
                    || a.getAttributeType()==AttributeType.CONSTANT
                    || !a.isValid()) {
                continue;
            }
            Datatype datatype = a.findDatatype();
            DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
            builder.append("if (propMap.containsKey(");
            builder.appendQuoted(a.getName());
            builder.appendln(")) {");
            String expr = "(String)propMap.get(\"" + a.getName() + "\")";
            builder.append(getFieldNameForAttribute(a) + " = ");
            builder.append(helper.newInstanceFromExpression(expr));
            builder.appendln(";");
            builder.appendln("}");
        }
        builder.methodEnd();
    }
    
    /**
     * <pre>
     * protected AbstractPolicyComponent createChildFromXml(Element childEl) {
     *     AbstractPolicyComponent newChild ) super.createChildFromXml(childEl);
     *     if (newChild!=null) {
     *         return newChild;
     *     }
     *     String className = childEl.getAttribute("class");
     *     if (className.length>0) {
     *         try {
     *             AbstractCoverage abstractCoverage = (AbstractCoverage)Class.forName(className).newInstance();
     *             addAbstractCoverage(abstractCoverage);
     *             initialize();
     *         } catch (Exception e) {
     *             throw new RuntimeException(e);
     *         }
     *     }
     *     if ("Coverage".equals(childEl.getNodeName())) {
     *         (AbstractPolicyComponent)return newCovergae();
     *     }
     *     return null;    
     * }
     * </pre>
     */
    private void generateMethodCreateChildFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, 
                AbstractPolicyComponent.class, 
                MethodNames.CREATE_CHILD_FROM_XML, 
                new String[]{"childEl"}, 
                new Class[]{Element.class});
        
        builder.appendClassName(AbstractPolicyComponent.class);
        builder.append(" newChild = super." + MethodNames.CREATE_CHILD_FROM_XML + "(childEl);");
        builder.appendln("if (newChild!=null) {");
        builder.appendln("return newChild;");
        builder.appendln("}");

        IRelation[] relations = getPolicyCmptType().getRelations();
        for (int i = 0; i < relations.length; i++) {
            IRelation relation = relations[i];
            if (!relation.isForwardComposition() 
                    || relation.isReadOnlyContainer()
                    || !relation.isValid()) {
                continue;
            }
            builder.append("if (");
            builder.appendQuoted(relation.getTargetRoleSingular());
            builder.appendln(".equals(childEl.getNodeName())) {");
            IPolicyCmptType target = relation.findTarget();
            builder.appendln("String className = childEl.getAttribute(\"class\");");
            builder.appendln("if (className.length()>0) {");
            builder.appendln("try {");
            builder.appendClassName(getQualifiedClassName(target));
            String varName = StringUtils.uncapitalise(relation.getTargetRoleSingular());
            builder.append(" " + varName + "=(");
            builder.appendClassName(getQualifiedClassName(target));
            builder.appendln(")Class.forName(className).newInstance();");
            builder.append(interfaceBuilder.getMethodNameAddOrSetObject(relation) + "(" + varName + ");");
            builder.appendln("return " + varName + ";");
            builder.appendln("} catch (Exception e) {");
            builder.appendln("throw new RuntimeException(e);");
            builder.appendln("}"); // catch
            builder.appendln("}"); // if
            if (!target.isAbstract()) {
                builder.append("return (");
                builder.appendClassName(AbstractPolicyComponent.class);
                builder.append(")");
                builder.append(interfaceBuilder.getMethodNameNewChild(relation));
                builder.appendln("();");
            } else {
                builder.appendln("throw new RuntimeException(childEl.toString() + \": Attribute className is missing.\");");
            }
            builder.appendln("}");
        }
        builder.appendln("return null;");
        builder.methodEnd();
    }
    
    /**
     * <pre>
     * protected abstract IUnresolvedReference createUnresolvedReference(
     *     Object objectId,
     *     String targetRole,
     *     String targetId) throws Exception {
     * }
     * </pre>
     */
    private void generateMethodCreateUnresolvedReference(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        String[] argNames = new String[]{"objectId", "targetRole", "targetId"};
        Class[] argClasses= new Class[]{Object.class, String.class, String.class};
        builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, 
                IUnresolvedReference.class, 
                MethodNames.CREATE_UNRESOLVED_REFERENCE, 
                argNames, 
                argClasses, 
                new Class[]{Exception.class});
        
        builder.appendClassName(IUnresolvedReference.class);
        builder.appendln(" ref = super." + MethodNames.CREATE_UNRESOLVED_REFERENCE + "(objectId, targetRole, targetId);");
        builder.appendln("if (ref!=null) {");
        builder.appendln("return ref;");
        builder.appendln("}");
        builder.appendln("return null;");
        builder.methodEnd();
    }
    
    private void generateMethodGetRangeFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        methodBuilder.javaDoc("{@inheritDoc}", ANNOTATION_RESTRAINED_MODIFIABLE);
        productCmptGenInterfaceBuilder.generateSignatureGetRangeFor(a, helper, methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.appendln(MARKER_BEGIN_USER_CODE);
        body.append("return ");
        if(a.isProductRelevant()){
            
            body.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getPcType().findProductCmptType()));
            body.append("().");
            body.append(productCmptGenInterfaceBuilder.getMethodNameGetRangeFor(a, helper.getDatatype()));
            body.appendln("(businessFunction);");
        }
        else{
            body.append(interfaceBuilder.getFieldNameMaxRangeFor(a));
            body.appendln(";");
            
        }
        body.appendln(MARKER_END_USER_CODE);
        body.appendCloseBracket();
        methodBuilder.append(body);
    }
    
    private void generateMethodGetAllowedValuesFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        methodBuilder.javaDoc("{@inheritDoc}", ANNOTATION_RESTRAINED_MODIFIABLE);
        productCmptGenInterfaceBuilder.generateSignatureGetAllowedValuesFor(a, helper.getDatatype(), methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.appendln(MARKER_BEGIN_USER_CODE);
        body.append("return ");
        if(a.isProductRelevant()){
            
            body.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getPcType().findProductCmptType()));
            body.append("().");
            body.append(productCmptGenInterfaceBuilder.getMethodNameGetAllowedValuesFor(a, helper.getDatatype()));
            body.appendln("(businessFunction);");
        }
        else{
            body.append(interfaceBuilder.getFieldNameMaxAllowedValuesFor(a));
            body.appendln(";");
            
        }
        body.appendln(MARKER_END_USER_CODE);
        body.appendCloseBracket();
        methodBuilder.append(body);
    }
    
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addChangeListener(ICoverage objectToAdd) {
     *     if(objectToAdd == null) { 
     *         throw new IllegalArgumentException("Can't add null to ...");
     *     }
     *     if (coverages.contains(objectToAdd)) { 
     *         return; 
     *     }
     *     coverages.add(objectToAdd);
     * }
     * </pre>
     */
    protected void generateMethodAddChangeListener (
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureAddObject(relation, methodsBuilder);
        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForAddObject(relation);
        IRelation reverseRelation = relation.findReverseRelation();
        methodsBuilder.openBracket();
        methodsBuilder.append("if (" + paramName + " == null) {");
        methodsBuilder.append("throw new ");
        methodsBuilder.appendClassName(NullPointerException.class);
        methodsBuilder.append("(\"Can't add null to relation " + relation.getName() + " of \" + this); }");
        methodsBuilder.append("if(");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".contains(" + paramName + ")) { return; }");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".add(" + paramName + ");");
        if (reverseRelation != null) {
            String targetClass = interfaceBuilder.getQualifiedClassName(relation.findTarget());
            methodsBuilder.append(generateCodeToSynchronizeReverseRelation(paramName, targetClass, relation, reverseRelation));
        }
        methodsBuilder.closeBracket();
    }
    
}