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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.MessageFragment;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.DefaultUnresolvedReference;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MsgReplacementParameter;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.DependantObject;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ModelObjectChangedEvent;
import org.faktorips.runtime.internal.ModelObjectDelta;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Element;

public class PolicyCmptImplClassBuilder extends BasePolicyCmptTypeBuilder {

    private final static String FIELD_PARENT_MODEL_OBJECT = "parentModelObject";
    
    private PolicyCmptInterfaceBuilder interfaceBuilder;
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplBuilder;
    private boolean generateDeltaSupport = false;
    private boolean generateCopySupport = false;
    
    public PolicyCmptImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId, boolean changeListenerSupportActive) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptImplClassBuilder.class), changeListenerSupportActive);
        setMergeEnabled(true);
    }
    
    public ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return productCmptGenInterfaceBuilder;
    }
    
    public ProductCmptGenImplClassBuilder getProductCmptGenImplBuilder() {
        return productCmptGenImplBuilder;
    }

    /**
     * {@inheritDoc}
     */
    protected GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet stringsSet) throws CoreException {
        if (a.isDerived()) {
            return new GenDerivedAttributeImpl(a, this, stringsSet);
        }
        if (a.isChangeable()) {
            return new GenChangeableAttributeImpl(a, this, stringsSet);
        }
        return new GenConstantAttribute(a, this, stringsSet, true);
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

    public void setInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder) {
        this.interfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    public PolicyCmptInterfaceBuilder getInterfaceBuilder() {
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
        IPolicyCmptType supertype = (IPolicyCmptType)getPcType().findSupertype(getIpsProject());
        if (supertype != null) {
            return getQualifiedClassName(supertype);
        }
        if (getPcType().isConfigurableByProductCmptType()) {
            return AbstractConfigurableModelObject.class.getName(); 
        } else {
            return AbstractModelObject.class.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return getJavaNamingConvention().getImplementationClassName(StringUtils.capitalize(name));
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        String publishedInterface = interfaceBuilder.getQualifiedClassName(getPcType());
        if (isFirstDependantTypeInHierarchy(getPcType())) {
            return new String[]{publishedInterface, DependantObject.class.getName()};
        }
        return new String[] { publishedInterface };
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
        if (getProductCmptType()!=null) {
            generateConstructorWithProductCmptArg(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IPolicyCmptType type = getPcType();
        generateMethodInitialize(methodsBuilder);
        if (getProductCmptType()!=null) {
            if(hasValidProductCmptTypeName()){
                generateMethodGetProductCmpt(methodsBuilder);
                generateMethodGetProductCmptGeneration(methodsBuilder);
                generateMethodSetProductCmpt(methodsBuilder);
            }
            generateMethodEffectiveFromHasChanged(methodsBuilder);
        }
        if (type.isAggregateRoot()) {
            if (type.isConfigurableByProductCmptType()) {
                generateMethodGetEffectiveFromAsCalendarForAggregateRoot(methodsBuilder);
            }
        } else if (isFirstDependantTypeInHierarchy(type)){
            generateCodeForDependantObjectBaseClass(memberVarsBuilder, methodsBuilder);
        }
        generateMethodRemoveChildModelObjectInternal(methodsBuilder);
        buildValidation(methodsBuilder);
        generateMethodInitPropertiesFromXml(methodsBuilder);
        generateMethodCreateChildFromXml(methodsBuilder);
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (associations[i].isAssoziation()) {
                generateMethodCreateUnresolvedReference(methodsBuilder);
                break;
            }
        }
        if (generateDeltaSupport) {
            generateMethodComputeDelta(methodsBuilder);
        }
        if (generateCopySupport) {
            if ((getClassModifier() & java.lang.reflect.Modifier.ABSTRACT) == 0) {
                generateMethodNewCopy(methodsBuilder);
            }
            generateMethodCopyProperties(methodsBuilder);
        }
    }
    
    protected void generateMethodNewCopy(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, IModelObject.class.getName(), MethodNames.NEW_COPY, new String[0], new String[0]);
        methodsBuilder.openBracket();
        String varName = "newCopy";
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.append(' ');
        methodsBuilder.append(varName);
        methodsBuilder.append(" = new ");
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.append("(");
        if (getPolicyCmptType().isConfigurableByProductCmptType() && getProductCmptType()!=null) {
            methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmpt(getProductCmptType()) + "()");
        }
        methodsBuilder.appendln(");");
        if (getPolicyCmptType().hasSupertype()) {
            methodsBuilder.appendln("super." + getMethodNameCopyProperties() + "(" + varName + ");");
        }
        methodsBuilder.appendln(getMethodNameCopyProperties() + "(" + varName + ");");
        methodsBuilder.appendln("return " + varName + ";");
        methodsBuilder.closeBracket();
    }
    
    protected void generateMethodCopyProperties(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        String paramName = "copy";
        methodsBuilder.signature(java.lang.reflect.Modifier.PROTECTED, "void", getMethodNameCopyProperties(), new String[]{paramName}, new String[]{getUnqualifiedClassName()});
        methodsBuilder.openBracket();
        
        for (Iterator it=getGenAttributes(); it.hasNext(); ) {
            GenAttribute generator = (GenAttribute)it.next();
            if (generator.isMemberVariableRequired()) {
                String field = generator.getMemberVarName();
                methodsBuilder.appendln(paramName + "." + field + " = " + field + ";");
            }
        }
        
        IPolicyCmptTypeAssociation[] associations = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (!associations[i].isValid() || associations[i].isDerived()) {
                continue;
            }
            if (associations[i].isCompositionDetailToMaster()) {
                continue;
            }
            if (associations[i].isAssoziation()) {
                generateMethodCopyPropertiesForAssociation(associations[i], paramName, methodsBuilder);
            } else {
                generateMethodCopyPropertiesForComposition(associations[i], paramName, methodsBuilder);
            }
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample for 1-1 composition
     * 
     * <pre>
     * if (child1 != null) {
     *     copy.child1 = (CpChild1)child1.newCopy();
     *     copy.child1.setParentModelObjectInternal(copy);
     * }
     * </pre>
     * 
     * Code sample for 1-Many composition
     * 
     * <pre>
     * for (Iterator it = child2s.iterator(); it.hasNext();) {
     *     ICpChild2 cpChild2 = (ICpChild2)it.next();
     *     ICpChild2 copycpChild2 = (ICpChild2)cpChild2.newCopy();
     *     ((DependantObject)copycpChild2).setParentModelObjectInternal(copy);
     *     copy.child2s.add(copycpChild2);
     * }
     * </pre>
     */
    protected void generateMethodCopyPropertiesForComposition(IPolicyCmptTypeAssociation composition, String paramName, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String field = getFieldNameForAssociation(composition);
        IPolicyCmptType targetType = composition.findTargetPolicyCmptType(getIpsProject());
        String targetTypeQName = getQualifiedClassName(targetType);
        if (composition.is1ToMany()) {
            String varOrig = QNameUtil.getUnqualifiedName(targetTypeQName);
            String varCopy= "copy" + StringUtils.capitalize(varOrig);
            methodsBuilder.append("for (");
            methodsBuilder.appendClassName(Iterator.class);
            methodsBuilder.appendln(" it = " + field + ".iterator(); it.hasNext();) {");
            
            methodsBuilder.appendClassName(targetTypeQName);
            methodsBuilder.append(" " + varOrig + " = ( ");
            methodsBuilder.appendClassName(targetTypeQName);
            methodsBuilder.appendln(")it.next();");
            
            methodsBuilder.appendClassName(targetTypeQName);
            methodsBuilder.append(" " + varCopy+ " = ( ");
            methodsBuilder.appendClassName(targetTypeQName);
            methodsBuilder.appendln(")" + varOrig + "." + MethodNames.NEW_COPY + "();");
            
            if (targetType.isDependantType()) {
                methodsBuilder.append("((");
                methodsBuilder.appendClassName(DependantObject.class);
                methodsBuilder.append(")" + varCopy + ")." + MethodNames.SET_PARENT + "(" + paramName + ");");
            }

            methodsBuilder.appendln(paramName + "." + field + ".add(" + varCopy + ");");
            methodsBuilder.appendln("}");
            return;
        } 
        // 1-1
        methodsBuilder.appendln("if (" + field + "!=null) {");
        methodsBuilder.append(paramName + "." + field + " = (");
        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.appendln(")" + field + "." + MethodNames.NEW_COPY + "();");
        if (targetType.isDependantType()) {
            methodsBuilder.appendln(paramName + "." + field + "." + MethodNames.SET_PARENT + "(" + paramName + ");");
        }
        methodsBuilder.appendln("}");
    }   
    
    /**
     * Code sample for 1-1 composition
     * <pre>
     * copy.child1 = child1;
     * </pre>
     * 
     * Code sample for 1-Many composition
     * <pre>
     * copy.child2s.addAll(child2s);
     * </pre>
     */
    protected void generateMethodCopyPropertiesForAssociation(IPolicyCmptTypeAssociation association, String paramName, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String field = getFieldNameForAssociation(association);
        if (association.is1ToMany()) {
            methodsBuilder.appendln(paramName + "." + field + ".addAll(" + field + ");");
        } else {
            methodsBuilder.appendln(paramName + "." + field + " = " + field + ";");
        }
    }
        
    private String getMethodNameCopyProperties() {
        return "copyProperties";
    }
    
    /**
     * <pre>
     * public IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options) {
     *     ModelObjectDelta delta = (ModelObjectDelta)super.computeDelta(otherObject, options);
     *     Root otherRoot = (Root)otherObject;
     *     delta.checkPropertyChange(IRoot.PROPERTY_STRINGATTRIBUTE, stringAttribute, otherRoot.stringAttribute, options);
     *     delta.checkPropertyChange(IRoot.PROPERTY_INTATTRIBUTE, intAttribute, otherRoot.intAttribute, options);
     *     delta.checkPropertyChange(IRoot.PROPERTY_BOOLEANATTRIBUTE, booleanAttribute, otherRoot.booleanAttribute, options);
     *     ModelObjectDelta.createChildDeltas(delta, children, otherRoot.children, "Child", options);
     *     ModelObjectDelta.createChildDeltas(delta, somethingElse, otherRoot.somethingElse, "SomethingElse", options);
     *     return delta;
     * }
     */
    protected void generateMethodComputeDelta(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        generateSignatureComputeDelta(methodsBuilder);
        methodsBuilder.openBracket();
        
        if (getPolicyCmptType().hasSupertype()) {
            // code sample: ModelObjectDelta delta = (ModelObjectDelta)super.computeDelta(otherObject, options);
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append(" delta = (");
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append(")super.");
            methodsBuilder.append(MethodNames.COMPUTE_DELTA);
            methodsBuilder.appendln("(otherObject, options);");
        } else {
            // code sample: ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(this, otherObject);
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append(" delta = ");
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append('.');
            methodsBuilder.append(MethodNames.MODELOBJECTDELTA_NEW_EMPTY_DELTA);
            methodsBuilder.appendln("(this, otherObject);");
        }
        
        // code sample: Contract otherContract = (Contract)otherObject;
        String varOther = " other" + StringUtils.capitalize(getPolicyCmptType().getName());
        boolean castForOtherGenerated = false;
        
        // code sample for an attribute:
        // delta.checkPropertyChange(IRoot.PROPERTY_STRINGATTRIBUTE, stringAttribute, otherRoot.stringAttribute, options);
        for (Iterator it=getGenAttributes(); it.hasNext(); ) {
            GenAttribute generator = (GenAttribute)it.next();
            if (generator.needsToBeConsideredInDeltaComputation()) {
                if (!castForOtherGenerated) {
                    castForOtherGenerated = true;
                    generateCodeToCastOtherObject(varOther, methodsBuilder);
                }
                generator.generateDeltaComputation(methodsBuilder, "delta", varOther);
            }
        }
        
        // code sample (note that the generated code is the same for to1 and toMany associations 
        // ModelObjectDelta.createChildDeltas(delta, children, otherRoot.children, "Child", options);

        // code sample for a to1 association:
        // ModelObjectDelta.createChildDeltas(delta, somethingElse, otherRoot.somethingElse, "SomethingElse", options);
        IPolicyCmptTypeAssociation[] associations = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (!associations[i].isValid() || associations[i].isDerived() || !associations[i].isCompositionMasterToDetail()) {
                continue;
            }
            if (!castForOtherGenerated) {
                castForOtherGenerated = true;
                generateCodeToCastOtherObject(varOther, methodsBuilder);
            }
            String fieldName = getFieldNameForAssociation(associations[i]);
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append('.');
            methodsBuilder.append(MethodNames.MODELOBJECTDELTA_CREATE_CHILD_DELTAS);
            methodsBuilder.append("(delta, ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(", ");
            methodsBuilder.append(varOther + '.' + fieldName);
            methodsBuilder.append(", ");
            methodsBuilder.appendQuoted(fieldName); // = rolename
            methodsBuilder.append(", options);");
        }
        
        methodsBuilder.append("return delta;");
        methodsBuilder.closeBracket();
    }
    
    /*
     * helper method for generateMethodComputeDelta
     */
    private void generateCodeToCastOtherObject(String varOther, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.append(varOther);
        methodsBuilder.append(" = (");
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.appendln(")otherObject;");
    }
    
    protected void generateSignatureComputeDelta(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] paramNames = new String[]{"otherObject", "options"};
        String[] paramTypes = new String[]{IModelObject.class.getName(), IDeltaComputationOptions.class.getName()};
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, IModelObjectDelta.class.getName(), MethodNames.COMPUTE_DELTA, 
                paramNames, paramTypes);
    }

    protected void generateMethodGetEffectiveFromAsCalendarForAggregateRoot(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Calendar.class, MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR, EMPTY_STRING_ARRAY, new Class[0]);
        if (getPolicyCmptType().hasSupertype()) {
            methodsBuilder.appendln("return super." + MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR + "();");
        } else {
            String todoText = getLocalizedText(getPcType(), "METHOD_GET_EFFECTIVE_FROM_TODO");
            methodsBuilder.appendln("return null; // " + getJavaNamingConvention().getToDoMarker() + " " + todoText);
        }
        methodsBuilder.methodEnd();
    }
    
    protected void generateMethodEffectiveFromHasChanged(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Void.TYPE, MethodNames.EFFECTIVE_FROM_HAS_CHANGED, EMPTY_STRING_ARRAY, new Class[0]);
        methodsBuilder.appendln("super." + MethodNames.EFFECTIVE_FROM_HAS_CHANGED + "();");

        IPolicyCmptTypeAssociation[] associations = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            IPolicyCmptTypeAssociation r = associations[i];
            if (r.isValid() && r.isCompositionMasterToDetail() && !r.isDerivedUnion()) {
                IPolicyCmptType target = r.findTargetPolicyCmptType(getIpsProject());
                if (!target.isConfigurableByProductCmptType()) {
                    continue;
                }
                methodsBuilder.appendln();
                String field = getFieldNameForAssociation(r);
                if (r.is1ToMany()) {
                    methodsBuilder.append("for (");
                    methodsBuilder.appendClassName(Iterator.class);
                    methodsBuilder.append(" it=" + field + ".iterator(); it.hasNext();) {");
                    methodsBuilder.appendClassName(AbstractConfigurableModelObject.class);
                    methodsBuilder.append(" child = (");
                    methodsBuilder.appendClassName(AbstractConfigurableModelObject.class);
                    methodsBuilder.append(")it.next();");
                    methodsBuilder.append("child." + MethodNames.EFFECTIVE_FROM_HAS_CHANGED + "();");
                    methodsBuilder.append("}");
                } else {
                    methodsBuilder.append("if (" + field + "!=null) {");
                    methodsBuilder.append("((");
                    methodsBuilder.appendClassName(AbstractConfigurableModelObject.class);
                    methodsBuilder.append(")" + field + ")." + MethodNames.EFFECTIVE_FROM_HAS_CHANGED + "();");
                    methodsBuilder.append("}");
                }
            }
        }
        methodsBuilder.methodEnd();
    }
    
    protected void generateMethodRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        String paramName = "childToRemove";
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Void.TYPE, MethodNames.REMOVE_CHILD_MODEL_OBJECT_INTERNAL, 
                new String[] {paramName}, new Class[]{IModelObject.class});
        methodsBuilder.appendln("super." + MethodNames.REMOVE_CHILD_MODEL_OBJECT_INTERNAL + "(" + paramName + ");");
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (associations[i].isValid() && associations[i].getAssociationType().isCompositionMasterToDetail() && !associations[i].isDerivedUnion()) {
                String fieldName = getFieldNameForAssociation(associations[i]);
                if (associations[i].is1ToMany()) {
                    methodsBuilder.appendln(fieldName + ".remove(" + paramName + ");");
                } else {
                    methodsBuilder.appendln("if (" + fieldName + "==" + paramName + ") {");
                    methodsBuilder.appendln(fieldName + " = null;");
                    methodsBuilder.appendln("}");
                }
            }
        }
        methodsBuilder.methodEnd();
    }

    protected void generateCodeForProductCmptTypeAttribute(
            IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String javaDoc = null; // getLocalizedText(null, a.getName()); // TODO
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetPropertyValue(attribute.getName(), datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
        methodsBuilder.append("().");
        methodsBuilder.append(this.productCmptGenInterfaceBuilder.getMethodNameGetValue(attribute, datatypeHelper));
        methodsBuilder.append("();");
        methodsBuilder.closeBracket();
    }
        
    void generateMethodAttributeGetterFromProductCmpt(
            IProductCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaDoc = null; // getLocalizedText(null, a.getName()); // TODO
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetPropertyValue(a.getName(), datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
        methodsBuilder.append("().");
        methodsBuilder.append(this.productCmptGenInterfaceBuilder.getMethodNameGetValue(a, datatypeHelper));
        methodsBuilder.append("();");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Returns the name of the field/member variable that stores the values
     * for the property/attribute.
     */
    public String getFieldNameForAttribute(IPolicyCmptTypeAttribute a) throws CoreException {
        return ((GenAttribute)getGenerator(a)).getMemberVarName();
    }
    
    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder, String eventClassName, String eventConstant, String fieldName) {
    	generateChangeListenerSupport(methodsBuilder, eventClassName, eventConstant, fieldName, null);
	}
    
    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder, String eventClassName, String eventConstant, String fieldName, String paramName) {
    	if (isGenerateChangeListenerSupport()) {
            methodsBuilder.appendln("if (" + MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED + "()) {");
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(ModelObjectChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.appendClassName(eventClassName);
            methodsBuilder.append('.');
            methodsBuilder.append(eventConstant);
            methodsBuilder.append(", ");
            methodsBuilder.appendQuoted(fieldName);
            if(paramName != null) {
            	methodsBuilder.append(", ");
            	methodsBuilder.append(paramName);
            }
            methodsBuilder.appendln("));");
            methodsBuilder.appendln("}");
        }
	}

	/**
     * {@inheritDoc}
     */
    protected void generateCodeForAssociationInCommon(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if(association.isQualified()){
            if(association.isDerivedUnion()){
                generateMethodGetRefObjectsByQualifierForDerivedUnion(association, methodsBuilder);
            }
            else{
                generateMethodGetRefObjectsByQualifierForNonDerivedUnion(association, methodsBuilder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1To1Association(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (association.isCompositionDetailToMaster()) {
            generateMethodGetTypesafeParentObject(association, methodsBuilder);
            return; 
        }
        if (!association.isDerivedUnion()) {
            IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
            generateFieldForAssociation(association, target, fieldsBuilder);
            generateMethodGetRefObjectBasedOnMemberVariable(association, methodsBuilder);
            if (association.isAssoziation()) {
                generateMethodSetRefObjectForAssociation(association, methodsBuilder);
            } else if (association.isCompositionMasterToDetail()) { 
                generateMethodSetRefObjectForComposition(association, methodsBuilder);
            }
            generateNewChildMethodsIfApplicable(association, target, methodsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyAssociation(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        if (association.isDerivedUnion()) {
            generateMethodContainsObjectForContainerAssociation(association, methodsBuilder);
        } else {
            generateFieldForAssociation(association, target, fieldsBuilder);
            generateMethodGetNumOfForNoneContainerAssociation(association, methodsBuilder);
            generateMethodContainsObjectForNoneContainerAssociation(association, methodsBuilder);
            generateMethodGetAllRefObjectsForNoneContainerAssociation(association, methodsBuilder);
            generateMethodGetRefObjectAtIndex(association, methodsBuilder);
            generateNewChildMethodsIfApplicable(association, target, methodsBuilder);
            generateMethodAddObject(association, methodsBuilder);
            generateMethodRemoveObject(association, methodsBuilder);
        }
    }
    
    protected void generateFieldForAssociation(
            IPolicyCmptTypeAssociation association,
            IPolicyCmptType target,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaClassname = association.is1ToMany() ? List.class.getName()
                : getQualifiedClassName(target);
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();
        if (association.is1ToMany()) {
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(ArrayList.class);
            initialValueExpression.append("()");
        } else {
            initialValueExpression.append("null");
        }
        String comment = getLocalizedText(association, "FIELD_RELATION_JAVADOC", association.getName());
        methodsBuilder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, javaClassname, getFieldNameForAssociation(association),
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
    protected void generateMethodGetNumOfForNoneContainerAssociation(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRefObjects(association, methodsBuilder);
        methodsBuilder.openBracket();
        String field = getFieldNameForAssociation(association);
        if (association.is1ToMany()) {
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
     * public int getNumOfCoveragesInternal() {
     *     int num = 0; 
     *     num += super.getNumOfCollisionCoverages(); // generated only if class has none abstract superclass
     *     num += getNumOfCollisionsCoverages(); 
     *     num += tplCoverage==null ? 0 : 1;
     *     return num;
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfInternalForContainerAssociationImplementation(
            IPolicyCmptTypeAssociation containerAssociation, 
            List implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    
        methodsBuilder.javaDoc(null, ANNOTATION_GENERATED);
        String methodName = getMethodNameGetNumOfRefObjectsInternal(containerAssociation);
        methodsBuilder.signature(java.lang.reflect.Modifier.PRIVATE, "int", methodName, new String[]{}, new String[]{});
        methodsBuilder.openBracket();
        methodsBuilder.append("int num = 0;");
        IPolicyCmptType supertype = (IPolicyCmptType)getPcType().findSupertype(getIpsProject());
        if (supertype!=null && !supertype.isAbstract()) {
            String methodName2 = interfaceBuilder.getMethodNameGetNumOfRefObjects(containerAssociation);
            methodsBuilder.appendln("num += super." + methodName2 + "();");
        }
        for (int i = 0; i < implAssociations.size(); i++) {
            methodsBuilder.appendln();
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)implAssociations.get(i);
            methodsBuilder.append("num += ");
            if (association.is1ToMany()) {
                methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(association) + "();");
            } else {
                String field = getFieldNameForAssociation(association);
                methodsBuilder.append(field + "==null ? 0 : 1;");
            }
        }
        methodsBuilder.append("return num;");
        methodsBuilder.closeBracket();
    }
    
    /*
     * Returns the name of the internal method returning the number of referenced objects,
     * e.g. getNumOfCoveragesInternal()
     */
    private String getMethodNameGetNumOfRefObjectsInternal(IPolicyCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_GET_NUM_OF_INTERNAL_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }
    
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     return getNumOfCoveragesInternal();
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfForContainerAssociationImplementation(
            IPolicyCmptTypeAssociation containerAssociation, 
            List implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRefObjects(containerAssociation, methodsBuilder);
        methodsBuilder.openBracket();
        String methodName = getMethodNameGetNumOfRefObjectsInternal(containerAssociation);
        methodsBuilder.append("return " + methodName + "();");
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
    protected void generateMethodContainsObjectForNoneContainerAssociation(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String paramName = interfaceBuilder.getParamNameForContainsObject(association);
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainsObject(association, methodsBuilder);
        
        methodsBuilder.openBracket();
        String field = getFieldNameForAssociation(association);
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
    protected void generateMethodContainsObjectForContainerAssociation(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String paramName = interfaceBuilder.getParamNameForContainsObject(association);
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainsObject(association, methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(interfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject())));
        methodsBuilder.append("[] targets = ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetAllRefObjects(association));
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
    protected void generateMethodGetAllRefObjectsForNoneContainerAssociation(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllRefObjects(association, methodsBuilder);
        String className = getQualifiedClassName(association.findTargetPolicyCmptType(getIpsProject()));
        String field = getFieldNameForAssociation(association);
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
     *     ICoverage[] result = new ICoverage[getNumOfCoveragesInternal()];
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
    protected void generateMethodGetAllRefObjectsForContainerAssociationImplementation(
            IPolicyCmptTypeAssociation association,
            List subAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllRefObjects(association, methodsBuilder);
        String classname = interfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(classname);
        methodsBuilder.append("[] result = new ");       
        methodsBuilder.appendClassName(classname);
        methodsBuilder.append("[" + getMethodNameGetNumOfRefObjectsInternal(association) + "()];");       

        IPolicyCmptType supertype = (IPolicyCmptType)getPcType().findSupertype(getIpsProject());
        if (supertype!=null && !supertype.isAbstract()) {
            // ICoverage[] superResult = super.getCoverages();
            // System.arraycopy(superResult, 0, result, 0, superResult.length);
            // int counter = superResult.length;
            methodsBuilder.appendClassName(classname);
            methodsBuilder.append("[] superResult = super.");       
            methodsBuilder.appendln(interfaceBuilder.getMethodNameGetAllRefObjects(association) + "();");
            
            methodsBuilder.appendln("System.arraycopy(superResult, 0, result, 0, superResult.length);");
            methodsBuilder.appendln("int counter = superResult.length;");
        } else {
            methodsBuilder.append("int counter = 0;");
        }
        
        boolean elementsVarDefined = false;
        for (int i = 0; i < subAssociations.size(); i++) {
            IPolicyCmptTypeAssociation subrel = (IPolicyCmptTypeAssociation)subAssociations.get(i);
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
    protected void generateMethodGetRefObjectBasedOnMemberVariable(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(association, methodsBuilder);
        methodsBuilder.openBracket();
        if (!association.isCompositionDetailToMaster()) {
            String field = getFieldNameForAssociation(association);
            methodsBuilder.appendln("return " + field + ";");
        } else {
            IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
            methodsBuilder.append("return (");
            methodsBuilder.appendClassName(interfaceBuilder.getQualifiedClassName(target));
            methodsBuilder.append(")" + MethodNames.GET_PARENT + "();");
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ISubTypeB getSubTypeB(ISubTypeBConfig qualifier) {
     * //1ToMany public ISubTypeB[] getSubTypeB(ISubTypeBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   //1ToMany List result = new ArrayList();
     *   for (Iterator it = subTypeBs.iterator(); it.hasNext();) {
     *     ISubTypeB subTypeB = (ISubTypeB) it.next();
     *     if(subTypeB.getSubTypeBConfig().equals(qualifier)){
     *       return subTypeB;
     *       //1ToMany result.add(subTypeB); 
     *     }
     *   }
     *   return null;
     *   //1ToMany return (ISubTypeB[]) result.toArray(new ISubTypeB[result.size()]);
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectsByQualifierForNonDerivedUnion(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObjectByQualifier(association, methodsBuilder);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        String className = interfaceBuilder.getQualifiedClassName(target);
        String pcTypeLocalVariable = StringUtils.uncapitalize(getUnqualifiedClassName(target.getIpsSrcFile()));
        String field = getFieldNameForAssociation(association);
        methodsBuilder.openBracket();
        methodsBuilder.append("if(qualifier == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
        if(association.is1ToManyIgnoringQualifier()){
            methodsBuilder.appendClassName(List.class);
            methodsBuilder.append(" result = new ");
            methodsBuilder.appendClassName(ArrayList.class);
            methodsBuilder.append("();");
        }
        methodsBuilder.append("for (");
        methodsBuilder.appendClassName(Iterator.class);
        methodsBuilder.append(" it = ");
        methodsBuilder.append(field);
        methodsBuilder.append(".iterator(); it.hasNext();)");
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(className);
        methodsBuilder.append(' ');
        methodsBuilder.append(pcTypeLocalVariable);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(className);
        methodsBuilder.append(")");
        methodsBuilder.append(" it.next();");
        methodsBuilder.appendln();
        methodsBuilder.append("if(");
        methodsBuilder.append(pcTypeLocalVariable);
        methodsBuilder.append('.');
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmpt(association.findQualifier(getIpsProject())));
        methodsBuilder.append("().equals(qualifier))");
        methodsBuilder.openBracket();
        if(association.is1ToManyIgnoringQualifier()){
            methodsBuilder.append("result.add(");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(");");
        }else {
            methodsBuilder.append("return ");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(';');
        }
        methodsBuilder.closeBracket();
        methodsBuilder.closeBracket();
        if(association.is1ToManyIgnoringQualifier()){
            methodsBuilder.append("return (");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append("[])");
            methodsBuilder.append("result.toArray(new ");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append("[result.size()]);");
        } else {
            methodsBuilder.append("return null;");
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IB getB(IBConfig qualifier) {
     * //1ToMany public IB getB(IBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   IB[] bs = getBs();
     *   //1ToMany List result = new ArrayList();
     *   for (int i = 0; i < bs.length; i++) {
     *     if (bs[i].getBConfig().equals(qualifier)) {
     *       return bs[i];
     *       //1ToMany result.add(bs[i]);
     *     }
     *   }
     *   return null;
     *   //1ToMany return (IB[]) result.toArray(new IB[result.size()]);
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectsByQualifierForDerivedUnion(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObjectByQualifier(association, methodsBuilder);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        String className = interfaceBuilder.getQualifiedClassName(target);
        String allObjectsMethodName = interfaceBuilder.getMethodNameGetAllRefObjects(association);
        String localVarName = "elements";
        methodsBuilder.openBracket();
        methodsBuilder.append("if(qualifier == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
        methodsBuilder.appendClassName(className);
        methodsBuilder.append("[] ");
        methodsBuilder.append(localVarName);
        methodsBuilder.append(" = ");
        methodsBuilder.append(allObjectsMethodName);
        methodsBuilder.append("();");
        if(association.is1ToManyIgnoringQualifier()){
            methodsBuilder.appendClassName(List.class);
            methodsBuilder.append(" result = new ");
            methodsBuilder.appendClassName(ArrayList.class);
            methodsBuilder.append("();");
        }
        methodsBuilder.append("for (int i = 0; i < ");
        methodsBuilder.append(localVarName);
        methodsBuilder.append(".length; i++)");
        methodsBuilder.openBracket();
        methodsBuilder.append("if(");
        methodsBuilder.append(localVarName);
        methodsBuilder.append("[i].");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmpt(association.findQualifier(getIpsProject())));
        methodsBuilder.append("().equals(qualifier))");
        methodsBuilder.openBracket();
        if(association.is1ToManyIgnoringQualifier()){
            methodsBuilder.append("result.add(");
            methodsBuilder.append(localVarName);
            methodsBuilder.append("[i]");
            methodsBuilder.append(");");
        }else {
            methodsBuilder.append("return ");
            methodsBuilder.append(localVarName);
            methodsBuilder.append("[i];");
        }
        methodsBuilder.closeBracket();
        methodsBuilder.closeBracket();
        if(association.is1ToManyIgnoringQualifier()){
            methodsBuilder.append("return (");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append("[])");
            methodsBuilder.append("result.toArray(new ");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append("[result.size()]);");
        } else {
            methodsBuilder.append("return null;");
        }
        methodsBuilder.closeBracket();
    }

    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return (ICoverage)getParentModelObject();
     * }
     * </pre>
     */
    protected void generateMethodGetTypesafeParentObject(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(association, methodsBuilder);
        methodsBuilder.openBracket();
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(interfaceBuilder.getQualifiedClassName(target));
        methodsBuilder.append(")" + MethodNames.GET_PARENT + "();");
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
    protected void generateMethodGetRefObjectForContainerAssociationImplementation(
            IPolicyCmptTypeAssociation association,
            List subAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(association, methodsBuilder);
        methodsBuilder.openBracket();
        for (int i = 0; i < subAssociations.size(); i++) {
            IPolicyCmptTypeAssociation subrel = (IPolicyCmptTypeAssociation)subAssociations.get(i);
            String accessCode;
            accessCode = interfaceBuilder.getMethodNameGetRefObject(subrel) + "()";
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
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureAddObject(association, methodsBuilder);
        String fieldname = getFieldNameForAssociation(association);
        String paramName = interfaceBuilder.getParamNameForAddObject(association);
        IPolicyCmptTypeAssociation reverseAssociation = association.findInverseAssociation(getIpsProject());
        methodsBuilder.openBracket();
        methodsBuilder.append("if (" + paramName + " == null) {");
        methodsBuilder.append("throw new ");
        methodsBuilder.appendClassName(NullPointerException.class);
        methodsBuilder.append("(\"Can't add null to association " + association.getName() + " of \" + this); }");
        methodsBuilder.append("if(");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".contains(" + paramName + ")) { return; }");
        if (association.isCompositionMasterToDetail()) {
            IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
            if (target!=null && target.isDependantType()) {
                methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "this"));
            }
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".add(" + paramName + ");");
        if (association.isAssoziation() && reverseAssociation!=null) {
            String targetClass = interfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
            methodsBuilder.append(generateCodeToSynchronizeReverseAssoziation(paramName, targetClass, association, reverseAssociation));
        }
        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "RELATION_OBJECT_ADDED" , fieldname, paramName);
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
            IPolicyCmptTypeAssociation association, 
            IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureNewChild(association, target, inclProductCmptArg, methodsBuilder);
        String addMethod = association.is1ToMany() ? interfaceBuilder.getMethodNameAddObject(association) :
            interfaceBuilder.getMethodNameSetObject(association);
        String varName = "new" + association.getTargetRoleSingular();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        methodsBuilder.append(" " + varName + " = new ");
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        if (inclProductCmptArg) {
            methodsBuilder.appendln("(" + interfaceBuilder.getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())) + ");");  
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
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String fieldname = getFieldNameForAssociation(association);
        String paramName = interfaceBuilder.getParamNameForRemoveObject(association);
        IPolicyCmptTypeAssociation reverseAssociation = association.findInverseAssociation(getIpsProject());
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureRemoveObject(association, methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + "== null) {return;}");
        
        if (reverseAssociation != null || (association.isComposition() && target!=null && target.isDependantType())) {
            methodsBuilder.append("if(");
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".remove(" + paramName + ")");
        if (reverseAssociation != null || (association.isComposition() && target!=null && target.isDependantType())) {
            methodsBuilder.append(") {");
            if (association.isAssoziation()) {
                methodsBuilder.append(generateCodeToCleanupOldReference(association, reverseAssociation, paramName));
            } else {
                methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "null"));
            }
            methodsBuilder.append(" }");
        } else {
            methodsBuilder.append(';');
        }
        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "RELATION_OBJECT_REMOVED", fieldname, paramName);
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *     if (homeContract!=null) {
     *         ((DependantObject)homeContract).setParentModelObjectInternal(null);
     *     }
     *     homeContract = (HomeContract)newObject;
     *     if (homeContract!=null) {
     *         ((DependantObject)homeContract).setParentModelObjectInternal(this);
     *     }
     * }
     * </pre>
     */
    protected void generateMethodSetRefObjectForComposition(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        if (association.isCompositionDetailToMaster()) {
            return; // setter defined in base class.
        }
        String fieldname = getFieldNameForAssociation(association);
        String paramName = interfaceBuilder.getParamNameForSetObject(association);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetObject(association, methodsBuilder);
        
        methodsBuilder.openBracket();
        if (target.isDependantType()) {
            methodsBuilder.appendln("if(" + fieldname + " != null) {");
            methodsBuilder.append(generateCodeToSynchronizeReverseComposition(fieldname, "null"));;
            methodsBuilder.appendln("}");
        }

        if (target.isDependantType()) {
            methodsBuilder.appendln("if(" + paramName + " != null) {");
            methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "this"));;
            methodsBuilder.appendln("}");
        }
        
        methodsBuilder.append(fieldname);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        methodsBuilder.append(")" + paramName +";");

        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "RELATION_OBJECT_CHANGED", fieldname, paramName);
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
    protected void generateMethodSetRefObjectForAssociation(
            IPolicyCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String fieldname = getFieldNameForAssociation(association);
        String paramName = interfaceBuilder.getParamNameForSetObject(association);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        IPolicyCmptTypeAssociation reverseAssociation = association.findInverseAssociation(getIpsProject());

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetObject(association, methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + " == ");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(") return;");
        if (reverseAssociation != null) {
            methodsBuilder.appendClassName(getQualifiedClassName(target));
            methodsBuilder.append(" oldRefObject = ");
            methodsBuilder.append(fieldname);
            methodsBuilder.append(';');
            methodsBuilder.append(fieldname);
            methodsBuilder.append(" = null;");
            methodsBuilder.append(generateCodeToCleanupOldReference(association, reverseAssociation, "oldRefObject"));
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        methodsBuilder.append(")" + paramName +";");
        if (reverseAssociation != null) {
            methodsBuilder.append(generateCodeToSynchronizeReverseAssoziation(fieldname, 
                    getQualifiedClassName(target), association, reverseAssociation));
        }
        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "RELATION_OBJECT_CHANGED", fieldname, paramName);
        methodsBuilder.closeBracket();
    }
    
    private JavaCodeFragment generateCodeToSynchronizeReverseAssoziation(
            String varName,
            String varClassName,
            IPolicyCmptTypeAssociation association,
            IPolicyCmptTypeAssociation reverseAssociation) throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        if (!association.is1ToMany()) {
            code.append(varName + " != null && ");
        }
        if (reverseAssociation.is1ToMany()) {
            code.append("! " + varName + ".");
            code.append(interfaceBuilder.getMethodNameContainsObject(reverseAssociation) + "(this)");
        } else {
            code.append(varName + ".");
            code.append(interfaceBuilder.getMethodNameGetRefObject(reverseAssociation));
            code.append("() != this");
        }
        code.append(") {");
        if (reverseAssociation.is1ToMany()) {
            code.append(varName + "." + interfaceBuilder.getMethodNameAddObject(reverseAssociation));
        } else {
            String targetClass = getQualifiedClassName(association.findTarget(getIpsProject()));
            if (!varClassName.equals(targetClass)) {
                code.append("((");
                code.appendClassName(targetClass);
                code.append(")" + varName + ").");
            } else {
                code.append(varName + ".");
            }
            code.append(interfaceBuilder.getMethodNameSetObject(reverseAssociation));
        }
        code.appendln("(this);");
        code.appendln("}");
        return code;
    }
    
    /**
     * <pre>
     * ((DependantObject)parentModelObject).setParentModelObjectInternal(this);
     * </pre>
     */
    private JavaCodeFragment generateCodeToSynchronizeReverseComposition(
            String varName, String newValue) throws CoreException {
        
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("((");
        code.appendClassName(DependantObject.class);
        code.append(')');
        code.append(varName);
        code.append(")." + MethodNames.SET_PARENT);
        code.append('(');
        code.append(newValue);
        code.appendln(");");
        return code;
    }
    
    private JavaCodeFragment generateCodeToCleanupOldReference(
            IPolicyCmptTypeAssociation association, 
            IPolicyCmptTypeAssociation reverseAssociation,
            String varToCleanUp) throws CoreException {
        
        JavaCodeFragment body = new JavaCodeFragment();
        if (!association.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (reverseAssociation.is1ToMany()) {
            String removeMethod = interfaceBuilder.getMethodNameRemoveObject(reverseAssociation);
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String targetClass = getQualifiedClassName(association.findTarget(getIpsProject()));
            String setMethod = interfaceBuilder.getMethodNameSetObject(reverseAssociation);
            body.append("((");
            body.appendClassName(targetClass);
            body.append(")" + varToCleanUp + ")." + setMethod + "(null);");
        }
        if (!association.is1ToMany()) {
            body.append(" }");
        }
        return body;
    }

    /**
     * Returns the name of field/member var for the association.
     */
    public String getFieldNameForAssociation(IPolicyCmptTypeAssociation association) throws CoreException {
        if (association.is1ToMany()) {
            return getJavaNamingConvention().getMemberVarName(association.getTargetRolePlural());
        } else {
            return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerAssociationImplementation(
            IPolicyCmptTypeAssociation containerAssociation,
            List associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        if (containerAssociation.is1ToMany()) {
            generateMethodGetNumOfForContainerAssociationImplementation(containerAssociation, associations, methodsBuilder);
            generateMethodGetAllRefObjectsForContainerAssociationImplementation(containerAssociation, associations, methodsBuilder);
            generateMethodGetNumOfInternalForContainerAssociationImplementation(containerAssociation, associations, methodsBuilder);
        } else {
            generateMethodGetRefObjectForContainerAssociationImplementation(containerAssociation, associations, methodsBuilder);
        }
    }

    private void buildValidation(JavaCodeFragmentBuilder builder) throws CoreException {
        generateMethodValidateSelf(builder, getPcType().getPolicyCmptTypeAttributes());
        createMethodValidateDependants(builder);
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            if(r.validate(getIpsProject()).containsErrorMsg()){
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
         * rels = GetAllAssociationToMany(); for (int i = 0; i < rels.length; i++) {
         * ml.add(rels[i].validate()); } if (NumOfRelTo1() > 0) {
         * ml.add(GetAssociationTo1().validate()); } }
         */

        String methodName = "validateDependants";
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml, businessFunction);");
        for (int i = 0; i < associations.length; i++) {
            IPolicyCmptTypeAssociation r = associations[i];
            if (!r.validate(getIpsProject()).containsErrorMsg()) {
                if (r.getAssociationType() == AssociationType.COMPOSITION_MASTER_TO_DETAIL
                        && StringUtils.isEmpty(r.getSubsettedDerivedUnion())) {
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
                        String field = getFieldNameForAssociation(r);
                        body.append("if (" + field + "!=null) {");
                        body.append("ml.add(" + field + ".validate(businessFunction));");
                        body.append("}");
                    }
                }
            }
        }

        String javaDoc = getLocalizedText(getPcType(), "VALIDATE_DEPENDANTS_JAVADOC", getPcType()
                .getName());

        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "ml", "businessFunction" }, 
            new String[] { MessageList.class.getName(), String.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    private void generateMethodValidateSelf(JavaCodeFragmentBuilder builder, IPolicyCmptTypeAttribute[] attributes)
            throws CoreException {
        /*
         * public void validateSelf(MessageList ml, String businessFunction) { super.validateSelf(ml, businessFunction); }
         */
        String methodName = "validateSelf";
        String javaDoc = getLocalizedText(getIpsObject(), "VALIDATE_SELF_JAVADOC", getPcType()
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
            if(r.validate(getIpsProject()).isEmpty()){
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
    private void generateMethodCreateMessageForRule(IValidationRule rule, JavaCodeFragmentBuilder builder) throws CoreException {
        String localVarObjectProperties = "invalidObjectProperties";
        String localVarReplacementParams = "replacementParameters";
        MessageFragment msgFrag = MessageFragment.createMessageFragment(rule.getMessageText(), MessageFragment.VALUES_AS_PARAMETER_NAMES);

        // determine method parameters (name and type)
        String[] methodParamNames;
        String[] methodParamTypes;
        if (!rule.isValidatedAttrSpecifiedInSrc()) {
            methodParamNames = msgFrag.getParameterNames();
            methodParamTypes = msgFrag.getParameterClasses();
        } else {
            int numberOfMethodParams = msgFrag.getNumberOfParameters() + 1;
            methodParamNames = new String[numberOfMethodParams];
            methodParamTypes = new String[numberOfMethodParams];
            System.arraycopy(msgFrag.getParameterNames(), 0, methodParamNames, 0, msgFrag.getNumberOfParameters());
            System.arraycopy(msgFrag.getParameterClasses(), 0, methodParamTypes, 0, msgFrag.getNumberOfParameters());
            methodParamNames[methodParamNames.length-1] = localVarObjectProperties;
            methodParamTypes[methodParamTypes.length-1] = ObjectProperty.class.getName() + "[]";
        }
        
        // code for objectProperties
        JavaCodeFragment body = new JavaCodeFragment();
        String[] validatedAttributes = rule.getValidatedAttributes();
        if(!rule.isValidatedAttrSpecifiedInSrc()){
            body.append(generateCodeForInvalidObjectProperties(localVarObjectProperties, validatedAttributes));
        }
        // code for replacement parameters
        if (msgFrag.hasParameters()) {
            body.append(generateCodeForMsgReplacementParameters(localVarReplacementParams, msgFrag.getParameterNames()));
        }
        
        // code to construct the message's text
        body.append(msgFrag.getFrag());

        // code to create the message and return it.
        body.append("return new ");
        body.appendClassName(Message.class);
        body.append('(');
        body.append(interfaceBuilder.getFieldNameForMsgCode(rule));
        body.append(", ");
        body.append(msgFrag.getMsgTextExpression());
        body.append(", ");
        body.append(rule.getMessageSeverity().getJavaSourcecode());
        body.append(", ");
        body.append(localVarObjectProperties);
        if (msgFrag.hasParameters()) {
            body.append(", ");
            body.append(localVarReplacementParams);
        }
        body.append(");");

        String javaDoc = getLocalizedText(rule, "CREATE_MESSAGE_JAVADOC", rule.getName());
        builder.method(java.lang.reflect.Modifier.PROTECTED, Message.class.getName(),
                getMethodNameCreateMessageForRule(rule), methodParamNames, methodParamTypes, body, javaDoc, ANNOTATION_GENERATED);
    }
    
    private String getMethodNameCreateMessageForRule(IValidationRule rule) {
        return "createMessageForRule" + StringUtils.capitalize(rule.getName());
    }
    
    private JavaCodeFragment generateCodeForInvalidObjectProperties(String pObjectProperties, String[] validatedAttributes) throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        if(validatedAttributes.length > 0){
            code.appendClassName(ObjectProperty.class);
            code.append("[] ");
            code.append(pObjectProperties);
            code.append(" = new ");
            code.appendClassName(ObjectProperty.class);
            code.append("[]{");
            for (int j = 0; j < validatedAttributes.length; j++) {
                IPolicyCmptTypeAttribute attr = getPcType().findPolicyCmptTypeAttribute(validatedAttributes[j], getIpsProject());
                String propertyConstName = getGenerator(attr).getStaticConstantPropertyName();
                code.append(" new ");
                code.appendClassName(ObjectProperty.class);
                code.append("(this, ");
                code.append(propertyConstName);
                code.append(")");
                if(j < validatedAttributes.length -1){
                    code.append(',');
                }
            }
            code.appendln("};");
        }
        else{
            code.appendClassName(ObjectProperty.class);
            code.append(" ");
            code.append(pObjectProperties);
            code.append(" = new ");
            code.appendClassName(ObjectProperty.class);
            code.appendln("(this);");
        }
        return code;
    }
    
    /**
     * Code sample:
     * <pre>
     *   MsgReplacementParameter[] replacementParameters = new MsgReplacementParameter[] {
     *       new MsgReplacementParameter("maxVs", maxVs),
     *   };
     * 
     * </pre>
     */
    private JavaCodeFragment generateCodeForMsgReplacementParameters(String localVar, String[] parameterNames) {
        JavaCodeFragment code = new JavaCodeFragment();
        // MsgReplacementParameter[] replacementParameters = new MsgReplacementParameter[] {
        code.appendClassName(MsgReplacementParameter.class);
        code.append("[] " + localVar + " = new ");
        code.appendClassName(MsgReplacementParameter.class);
        code.appendln("[] {");

        for (int i = 0; i < parameterNames.length; i++) {

            //     new MsgReplacementParameter("paramName", paramName),
            code.append("new ");
            code.appendClassName(MsgReplacementParameter.class);
            code.append("(");
            code.appendQuoted(parameterNames[i]);
            code.append(", ");
            code.append(parameterNames[i]);
            code.append(")");
            if (i!=parameterNames.length-1) {
                code.append(", ");
            }
            code.appendln();
        }
        
        code.appendln("};");
        return code;
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
                productCmptInterfaceBuilder.getQualifiedClassName(getProductCmptType())};
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
                paramNames, paramTypes);
        builder.append("super(productCmpt);");
        generateInitializationForOverrideAttributes(builder);
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
                EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        builder.append("super();");
        generateInitializationForOverrideAttributes(builder);
        builder.methodEnd();
    }

    private void generateInitializationForOverrideAttributes(JavaCodeFragmentBuilder builder) throws CoreException{
        IPolicyCmptTypeAttribute[] attributes = getPcType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if(attributes[i].isChangeable() && attributes[i].isOverwrite() && attributes[i].validate(getIpsProject()).isEmpty()){
                DatatypeHelper helper = getPcType().getIpsProject().getDatatypeHelper(attributes[i].getValueDatatype());
                JavaCodeFragment initialValueExpression = helper.newInstance(attributes[i].getDefaultValue());
                interfaceBuilder.generateCallToMethodSetPropertyValue(attributes[i], helper, initialValueExpression, builder);
            }
        }
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
        IPolicyCmptTypeAttribute[] attributes = getPcType().getPolicyCmptTypeAttributes();
        ArrayList selectedValues = new ArrayList();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (!a.validate(getIpsProject()).containsErrorMsg()) {
                if (a.isProductRelevant() && a.isChangeable() && !a.isOverwrite()) {
                    selectedValues.add(a);
                }
            }
        }
        appendLocalizedJavaDoc("METHOD_INITIALIZE", getPcType(), builder);
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
                getMethodNameInitialize(), EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            builder.append("super." + getMethodNameInitialize() + "();");
        }
        if(selectedValues.isEmpty()){
            builder.methodEnd();
            return;
        }
        if (getProductCmptType()==null) {
            builder.methodEnd();
            return;
        }
        String method = interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType());
        builder.appendln("if (" + method + "()==null) {");
        builder.appendln("return;");
        builder.appendln("}");
        for (Iterator it = selectedValues.iterator(); it.hasNext();) {
            IPolicyCmptTypeAttribute a = (IPolicyCmptTypeAttribute)it.next();
            DatatypeHelper datatype = a.getIpsProject().findDatatypeHelper(a.getDatatype());
            builder.append(getFieldNameForAttribute(a));
            builder.append(" = ");
            builder.append(getMethodNameGetDefaultValueFromProductCmpt(a, datatype));
            builder.append(";");
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
        String productCmptInterfaceQualifiedName = productCmptInterfaceBuilder.getQualifiedClassName(getProductCmptType());
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
        builder.appendClassName(productCmptGenInterfaceBuilder.getQualifiedClassName(getProductCmptType()));
        builder.append(")");
        builder.append(interfaceBuilder.getMethodNameGetProductCmpt(getProductCmptType()));
        builder.append("().");
        builder.append(productCmptInterfaceBuilder.getMethodNameGetGeneration(getProductCmptType()));
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

    private String getMethodNameGetDefaultValueFromProductCmpt(IPolicyCmptTypeAttribute a, DatatypeHelper datatype) throws CoreException {
        String methodName = productCmptGenInterfaceBuilder.getMethodNameGetDefaultValue(a, datatype);
        return interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()) + "()." + methodName + "()";
    }
    
    private String getMethodNameExecRule(IValidationRule r){
        return "execRule" + StringUtils.capitalize(r.getName());
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
    private void generateMethodExecRule(IValidationRule rule, JavaCodeFragmentBuilder builder) throws CoreException {
        String parameterBusinessFunction = "businessFunction";
        String javaDoc = getLocalizedText(getIpsObject(), "EXEC_RULE_JAVADOC", rule.getName());
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln();
        String[] businessFunctions = rule.getBusinessFunctions();
        if(!rule.isAppliedForAllBusinessFunctions()){
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
        if(!rule.isCheckValueAgainstValueSetRule()) {
            body.appendln("//begin-user-code");
            body.appendln(getLocalizedToDo(rule, "EXEC_RULE_IMPLEMENT", rule.getName()));
        }
        
        body.append("if(");
        String[] javaDocAnnotation = ANNOTATION_RESTRAINED_MODIFIABLE;
        if(rule.isCheckValueAgainstValueSetRule()){
            javaDocAnnotation = ANNOTATION_GENERATED;
            IPolicyCmptTypeAttribute attr = getPcType().getPolicyCmptTypeAttribute(rule.getValidatedAttributeAt(0));
            Datatype attrDatatype = attr.findDatatype(getIpsProject());
            body.append('!');
            if(attr.getValueSet().getValueSetType().equals(ValueSetType.ENUM)){
                body.append(productCmptGenInterfaceBuilder.getMethodNameGetAllowedValuesFor(attr, attr.findDatatype(getIpsProject())));
            }
            else if(attr.getValueSet().getValueSetType().equals(ValueSetType.RANGE)){
                body.append(productCmptGenInterfaceBuilder.getMethodNameGetRangeFor(attr, attrDatatype));
            }
            body.append("(");
            body.append(parameterBusinessFunction);
            body.append(").contains(");
            body.append(interfaceBuilder.getMethodNameGetPropertyValue(attr.getName(), attrDatatype));
            body.append("()))");
        }
        else{
            body.append("true) ");
        }
        body.appendOpenBracket();
        boolean generateToDo = false;
        body.append("ml.add(");
        body.append(getMethodNameCreateMessageForRule(rule));
        MessageFragment msgFrag = MessageFragment.createMessageFragment(rule.getMessageText(), MessageFragment.VALUES_AS_PARAMETER_NAMES);
        body.append('(');
        if(msgFrag.hasParameters()){
            String[] parameterNames = msgFrag.getParameterNames();
            for (int j = 0; j < parameterNames.length; j++) {
                body.append("null");
                generateToDo = true;
                if(j < parameterNames.length - 1){
                    body.append(", ");
                }
            }
        }

        if(rule.isValidatedAttrSpecifiedInSrc()){
            generateToDo = true;
            if(msgFrag.hasParameters()){
                body.append(", ");
            }
            body.append("new ");
            body.appendClassName(ObjectProperty.class);
            body.append("[0]");
        }
        
        body.append("));");
        if (generateToDo) {
            body.append(getLocalizedToDo(rule, "EXEC_RULE_COMPLETE_CALL_CREATE_MSG", rule.getName()));
        }
        body.appendln();
        body.appendCloseBracket();
        body.appendln(" return true;");
        if(!rule.isCheckValueAgainstValueSetRule()) {
            body.appendln("//end-user-code");
        }
        if(!rule.isAppliedForAllBusinessFunctions()){
            if(businessFunctions.length > 0){
                body.appendCloseBracket();
                body.appendln(" return true;");
            }
        }
        builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            getMethodNameExecRule(rule), new String[] { "ml", parameterBusinessFunction },
            new String[] { MessageList.class.getName(), String.class.getName() }, body, javaDoc, javaDocAnnotation);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(IMethod method, Datatype returnType, Datatype[] paramTypes, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (method.getModifier()==org.faktorips.devtools.core.model.ipsobject.Modifier.PUBLISHED) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
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
        boolean first = true;
        for (Iterator it=getGenAttributes(); it.hasNext(); ) {
            GenAttribute generator = (GenAttribute)it.next();
            if (!generator.isMemberVariableRequired()) {
                continue;
            }
            if (first) {
                first = false;
                builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
                builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, Void.TYPE, MethodNames.INIT_PROPERTIES_FROM_XML, new String[]{"propMap"}, new Class[]{HashMap.class});
                builder.appendln("super." + MethodNames.INIT_PROPERTIES_FROM_XML + "(propMap);");
            }
            generator.generateInitPropertiesFromXml(builder);
        }
        if (!first) {
            // there is at least one attribute
            builder.appendln(MARKER_BEGIN_USER_CODE);
            builder.appendln(MARKER_END_USER_CODE);
            builder.methodEnd();
        }
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
                AbstractModelObject.class, 
                MethodNames.CREATE_CHILD_FROM_XML, 
                new String[]{"childEl"}, 
                new Class[]{Element.class});
        
        builder.appendClassName(AbstractModelObject.class);
        builder.append(" newChild = super." + MethodNames.CREATE_CHILD_FROM_XML + "(childEl);");
        builder.appendln("if (newChild!=null) {");
        builder.appendln("return newChild;");
        builder.appendln("}");

        IPolicyCmptTypeAssociation[] associations = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            IPolicyCmptTypeAssociation association = associations[i];
            if (!association.isCompositionMasterToDetail() 
                    || association.isDerivedUnion()
                    || !association.isValid()) {
                continue;
            }
            builder.append("if (");
            builder.appendQuoted(association.getTargetRoleSingular());
            builder.appendln(".equals(childEl.getNodeName())) {");
            IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
            builder.appendln("String className = childEl.getAttribute(\"class\");");
            builder.appendln("if (className.length()>0) {");
            builder.appendln("try {");
            builder.appendClassName(getQualifiedClassName(target));
            String varName = StringUtils.uncapitalize(association.getTargetRoleSingular());
            builder.append(" " + varName + "=(");
            builder.appendClassName(getQualifiedClassName(target));
            builder.appendln(")Class.forName(className).newInstance();");
            builder.append(interfaceBuilder.getMethodNameAddOrSetObject(association) + "(" + varName + ");");
            builder.appendln("return " + varName + ";");
            builder.appendln("} catch (Exception e) {");
            builder.appendln("throw new RuntimeException(e);");
            builder.appendln("}"); // catch
            builder.appendln("}"); // if
            if (!target.isAbstract()) {
                builder.append("return (");
                builder.appendClassName(AbstractModelObject.class);
                builder.append(")");
                builder.append(interfaceBuilder.getMethodNameNewChild(association));
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
     * 
     *     if ("InsuredPeson".equals(targetRole)) {
     *         return new DefaultUnresolvedReference(this, objectId, "setInsuredPerson", IInsuredPerson.class, targetId);
     *     }
     *     return super.createUnresolvedReference(objectId, targetRole, targetId);
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

        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (!associations[i].isValid() || !associations[i].isAssoziation()) {
                continue;
            }
            IPolicyCmptTypeAssociation association = associations[i];
            String targetClass = interfaceBuilder.getQualifiedClassName(association.findTargetPolicyCmptType(getIpsProject()));
            builder.append("if (");
            builder.appendQuoted(association.getTargetRoleSingular());
            builder.append(".equals(targetRole)) {");
            builder.append("return new ");
            builder.appendClassName(DefaultUnresolvedReference.class);
            builder.append("(this, objectId, ");
            builder.appendQuoted(interfaceBuilder.getMethodNameAddOrSetObject(association));
            builder.append(", ");
            builder.appendClassName(targetClass);
            builder.append(".class, targetId);");
            builder.append("}");
        }
        builder.appendln("return super." + MethodNames.CREATE_UNRESOLVED_REFERENCE + "(objectId, targetRole, targetId);");
        builder.methodEnd();
    }
    
    private void generateCodeForDependantObjectBaseClass(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateFieldForParent(memberVarsBuilder);
        if (getPcType().isConfigurableByProductCmptType()) {
            generateMethodGetEffectiveFromAsCalendarForDependantObjectBaseClass(methodBuilder);
        }
        generateMethodGetParentModelObject(methodBuilder);
        generateMethodSetParentModelObjectInternal(methodBuilder);
        if (isGenerateChangeListenerSupport()) {
            generateMethodExistsChangeListenerToBeInformed(methodBuilder);
            generateMethodNotifyChangeListeners(methodBuilder);
        }
    }
    
    /**
     * <pre>
     * private AbstractModelObject parentModelObject;
     * </pre>
     */
    private void generateFieldForParent(JavaCodeFragmentBuilder memberVarsBuilder) {
        String javadoc = getLocalizedText(getPolicyCmptType(), "FIELD_PARENT_JAVADOC");
        memberVarsBuilder.javaDoc(javadoc, ANNOTATION_GENERATED);
        memberVarsBuilder.append("private ");
        memberVarsBuilder.appendClassName(AbstractModelObject.class);
        memberVarsBuilder.append(' ');
        memberVarsBuilder.append(FIELD_PARENT_MODEL_OBJECT);
        memberVarsBuilder.appendln(";");
    }
    
    /**
     * <pre>
     * public IModelObject getParentModelObject() {
     *     return parentModelObject;
     * }
     * </pre>
     */
    private void generateMethodGetParentModelObject(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodBuilder.methodBegin(Modifier.PUBLIC, IModelObject.class, MethodNames.GET_PARENT, EMPTY_STRING_ARRAY, new Class[0]);
        methodBuilder.appendln("return " + FIELD_PARENT_MODEL_OBJECT + ";");
        methodBuilder.methodEnd();
    }
    
    /**
     * <pre>
     * public IModelObject getParentModelObject() {
     *     if (parentModelObject!=null && parentModelObject!=newParent) {
     *         parentModelObject.removeChildModelObjectInternal(this);
     *     }
     *     return parentModelObject;
     * }
     * </pre>
     */
    private void generateMethodSetParentModelObjectInternal(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodBuilder.methodBegin(Modifier.PUBLIC, Void.TYPE, MethodNames.SET_PARENT, 
                new String[]{"newParent"}, new Class[]{AbstractModelObject.class});
        methodBuilder.appendln("if (" + FIELD_PARENT_MODEL_OBJECT + "!=null) {");
        methodBuilder.appendln(FIELD_PARENT_MODEL_OBJECT + "." + MethodNames.REMOVE_CHILD_MODEL_OBJECT_INTERNAL + "(this);");
        methodBuilder.appendln("}");
        methodBuilder.appendln(FIELD_PARENT_MODEL_OBJECT + "=newParent;");
        methodBuilder.methodEnd();
    }
    
    /**
     * <pre>
     * protected boolean existsChangeListenerToBeInformed() {
     *     if (super.existsChangeListenerToBeInformed()) {
     *         return true;
     *     }
     *     if (parentModelObject==null) {
     *         return false;
     *     }
     *     return parentModelObject.existsChangeListenerToBeInformed();
     * }
     * </pre>
     */
    private void generateMethodExistsChangeListenerToBeInformed(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodBuilder.methodBegin(Modifier.PUBLIC, Boolean.TYPE, MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED, 
                new String[]{}, new Class[]{});
        methodBuilder.appendln("if (super." + MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED + "()) {");
        methodBuilder.appendln("return true;");
        methodBuilder.appendln("}");
        methodBuilder.appendln("if (" + FIELD_PARENT_MODEL_OBJECT + "==null) {");
        methodBuilder.appendln("return false;");
        methodBuilder.appendln("}");
        methodBuilder.appendln("return " + FIELD_PARENT_MODEL_OBJECT + "." + MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED+ "();");
        methodBuilder.methodEnd();
    }
    
    /**
     * <pre>
     * public void notifyChangeListeners(ModelObjectChangedEvent event) {
     *     super.notifyChangeListeners(event);
     *     if (parentModelObject != null) {
     *         parentModelObject.notifyChangeListeners(event);
     *     }
     * }
     * </pre>
     */
    private void generateMethodNotifyChangeListeners(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodBuilder.methodBegin(Modifier.PUBLIC, Void.TYPE, MethodNames.NOTIFIY_CHANGE_LISTENERS, 
                new String[]{"event"}, new Class[]{IModelObjectChangedEvent.class});
        
        methodBuilder.appendln("super." + MethodNames.NOTIFIY_CHANGE_LISTENERS+ "(event);");
        methodBuilder.appendln("if (" + FIELD_PARENT_MODEL_OBJECT + "!=null) {");
        methodBuilder.appendln(FIELD_PARENT_MODEL_OBJECT + "." + MethodNames.NOTIFIY_CHANGE_LISTENERS + "(event);");
        methodBuilder.appendln("}");
        methodBuilder.methodEnd();
    }

    /**
     * <pre>
     * public Calendar getEffectiveFromAsCalendar() {
     *    IModelObject parent = getParentModelObject();
     *    if (parent instanceof IConfigurableModelObject) {
     *        return ((IConfigurableModelObject)parent).getEffectiveFromAsCalendar();
     *    }
     *    return null;
     * }
     * </pre>
     */
    protected void generateMethodGetEffectiveFromAsCalendarForDependantObjectBaseClass(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Calendar.class, MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR, EMPTY_STRING_ARRAY, new Class[0]);
        methodsBuilder.append("if (" + FIELD_PARENT_MODEL_OBJECT + " instanceof ");
        methodsBuilder.appendClassName(IConfigurableModelObject.class);
        methodsBuilder.append(") {");
        methodsBuilder.append("return ((");
        methodsBuilder.appendClassName(IConfigurableModelObject.class);
        methodsBuilder.appendln(")" + FIELD_PARENT_MODEL_OBJECT + ")." + MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR + "();");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return null;");
        methodsBuilder.methodEnd();
    }

    /**
     * <pre>
     * public IMotorCoverage getMotorCoverage(int index) {
     *      return (IMotorCoverage)motorCoverages.get(index);
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndex(IPolicyCmptTypeAssociation association, JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        String className = interfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        String field = getFieldNameForAssociation(association);
        interfaceBuilder.generateSignatureGetRefObjectAtIndex(association, methodBuilder);
        methodBuilder.openBracket();
        methodBuilder.append("return (");
        methodBuilder.appendClassName(className);
        methodBuilder.append(')');
        methodBuilder.append(field);
        methodBuilder.append(".get(index);");
        methodBuilder.closeBracket();
    }
   
}