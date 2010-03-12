/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.runtime.internal.MethodNames;

/**
 * 
 * @author Jan Ortmann
 */
public class GenAssociationTo1 extends GenAssociation {

    public GenAssociationTo1(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAssociation association)
            throws CoreException {
        super(genPolicyCmptType, association);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureSetObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameSetObject();
        String paramName = getParamNameForSetObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[] { paramName },
                new String[] { targetInterfaceName });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethodNameAddOrSetObject() {
        return getMethodNameSetObject();
    }

    /**
     * Returns the name of the method setting the referenced object. e.g. setCoverage(ICoverage
     * newObject)
     */
    @Override
    public String getMethodNameSetObject() {
        return getMethodNameSetObject(association);
    }

    /**
     * Returns the name of the method setting the referenced object. e.g. setCoverage(ICoverage
     * newObject).
     */
    public String getMethodNameSetObject(IAssociation association) {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the parameter for the method that tests if an object is references in a
     * multi-value association, e.g. objectToTest.
     */
    public String getParamNameForSetObject() {
        return getLocalizedText("PARAM_OBJECT_TO_SET_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the method removing an object from a multi-value association, e.g.
     * removeCoverage().
     */
    public String getMethodNameRemoveObject() {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME", StringUtils
                .capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the method removing an object from a multi-value association, e.g.
     * removeCoverage().
     */
    public String getMethodNameRemoveObject(IAssociation association) {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME", StringUtils
                .capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    @Override
    public void generateSignatureGetRefObject(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetRefObject();
        builder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Returns the name of the method returning the single referenced object. e.g. getCoverage()
     */
    @Override
    public String getMethodNameGetRefObject() {
        return getMethodNameGetRefObject(association);
    }

    /**
     * Returns the name of the method returning the single referenced object. e.g. getCoverage()
     */
    public String getMethodNameGetRefObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText("METHOD_GET_REF_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        super.generateMemberVariables(builder, ipsProject, generatesInterface);
        String comment = getLocalizedText("FIELD_ASSOCIATION_JAVADOC", association.getName());
        builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);

        getGenType().getBuilderSet().addAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD,
                association, builder);

        if (!isDerivedUnion() && !isCompositionDetailToMaster()) {

            getGenType().getBuilderSet().addAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION,
                    association, builder);

            // TODO: introduce Generator for JAXB annotations
            if (isGenerateJaxbSupport()) {
                builder.annotationLn("javax.xml.bind.annotation.XmlElement", "name=\"" + association.getName()
                        + "\", type=" + targetImplClassName + ".class");
                if (!isCompositionMasterToDetail()) {
                    builder.annotationLn("javax.xml.bind.annotation.XmlIDREF");
                }
            }

            builder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, targetImplClassName, fieldName,
                    new JavaCodeFragment("null"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        super.generateMethods(builder, ipsProject, generatesInterface);
        if (generatesInterface) {
            generateMethodGetRefObject(builder);
            if (!association.isDerivedUnion() && !association.getAssociationType().isCompositionDetailToMaster()) {
                generateMethodSetObject(builder);
                generateNewChildMethodsIfApplicable(builder, generatesInterface);
            }
        } else {
            if (association.isCompositionDetailToMaster()) {
                generateMethodGetTypesafeParentObject(builder);
                return;
            }
            if (!isDerivedUnion()) {
                generateMethodGetRefObjectBasedOnMemberVariable(builder);
                if (association.isAssoziation()) {
                    generateMethodSetRefObjectForAssociation(builder);
                } else if (association.isCompositionMasterToDetail()) {
                    generateMethodSetRefObjectForComposition(builder);
                }
                generateNewChildMethodsIfApplicable(builder, generatesInterface);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *     if (coverage != null) {
     *         ((Coverage)coverage).setContractInternal(null);
     *     }
     *     if (newObject != null) {
     *         ((Coverage)newObject).setContractInternal(this);
     *     }
     * }
     * </pre>
     */
    protected void generateMethodSetRefObjectForComposition(JavaCodeFragmentBuilder builder) throws CoreException {
        if (association.isCompositionDetailToMaster()) {
            return; // setter defined in base class.
        }

        String paramName = getParamNameForSetObject();
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureSetObject(builder);

        builder.openBracket();

        generateChangeListenerSupportBeforeChange(builder, ChangeEventType.ASSOCIATION_OBJECT_CHANGED, paramName);

        if (target.isDependantType() && inverseAssociation != null) {
            builder.appendln("if(" + fieldName + " != null) {");
            builder.append(generateCodeToSynchronizeReverseComposition(fieldName, "null"));;
            builder.appendln("}");

            builder.appendln("if(" + paramName + " != null) {");
            builder.append(generateCodeToSynchronizeReverseComposition(paramName, "this"));;
            builder.appendln("}");
        }

        builder.append(fieldName);
        builder.append(" = (");
        builder.appendClassName(targetImplClassName);
        builder.append(")" + paramName + ";");

        generateChangeListenerSupportAfterChange(builder, ChangeEventType.ASSOCIATION_OBJECT_CHANGED, paramName);
        builder.closeBracket();
    }

    /**
     * Code sample:
     * 
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
     *     if (refObject != null &amp;&amp; refObject.getHomePolicy() != this) {
     *         refObject.setHomePolicy(this);
     *     }
     * }
     * </pre>
     */
    protected void generateMethodSetRefObjectForAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        String paramName = getParamNameForSetObject();
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureSetObject(methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + " == ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(") return;");
        if (inverseAssociation != null) {
            methodsBuilder.appendClassName(targetImplClassName);
            methodsBuilder.append(" oldRefObject = ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(';');
            methodsBuilder.append(fieldName);
            methodsBuilder.append(" = null;");
            methodsBuilder.append(generateCodeToCleanupOldReference("oldRefObject"));
        }
        generateChangeListenerSupportBeforeChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_CHANGED, paramName);
        methodsBuilder.append(fieldName);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append(")" + paramName + ";");
        if (inverseAssociation != null) {
            methodsBuilder.append(getGenType().getBuilderSet().getGenerator(target).getGenerator(inverseAssociation)
                    .generateCodeToSynchronizeReverseAssoziation(fieldName, targetImplClassName));
        }
        generateChangeListenerSupportAfterChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_CHANGED, paramName);
        methodsBuilder.closeBracket();
    }

    public JavaCodeFragment generateCodeToCleanupOldReference(String varToCleanUp) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        if (!association.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (inverseAssociation.is1ToMany()) {
            String removeMethod = getMethodNameRemoveObject(inverseAssociation);
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String setMethod = getMethodNameSetObject(inverseAssociation);
            body.append(varToCleanUp);
            body.append("." + setMethod + "(null);");
        }
        if (!association.is1ToMany()) {
            body.append(" }");
        }
        return body;
    }

    @Override
    public JavaCodeFragment generateCodeToSynchronizeReverseAssoziation(String varName, String varClassName)
            throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        if (!inverseAssociation.is1ToMany()) {
            code.append(varName + " != null && ");
        }
        code.append(varName + ".");
        code.append(getMethodNameGetRefObject());
        code.append("() != this");
        code.append(") {");
        if (!varClassName.equals(getGenType().getQualifiedName(false))) {
            code.append("((");
            code.appendClassName(getGenType().getQualifiedName(false));
            code.append(")" + varName + ").");
        } else {
            code.append(varName + ".");
        }
        code.append(getMethodNameSetObject());
        code.appendln("(this);");
        code.appendln("}");
        return code;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return coverage;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectBasedOnMemberVariable(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        if (!association.isCompositionDetailToMaster()) {
            methodsBuilder.appendln("return " + fieldName + ";");
        } else {
            methodsBuilder.append("return (");
            methodsBuilder.appendClassName(targetInterfaceName);
            methodsBuilder.append(")" + MethodNames.GET_PARENT + "();");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * Note that if this is the inverse of a derived union then no code is generated (see comment
     * below).
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return coverage; // if inverse is not derived union
     * }
     * 
     * </pre>
     */
    protected void generateMethodGetTypesafeParentObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (inverseAssociation != null && inverseAssociation.isDerivedUnion()) {
            // in case of inverse of derived union
            // we don't need to generate this method
            // because no field is generated for the parent
            return;
        }
        // in case of non derived union we can directly return the field
        // note this is necessary because this getter method is used
        // to assert that a child is only related to one parent
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");

        methodsBuilder.append(getFieldNameForAssociation());
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject);
     * </pre>
     */
    protected void generateMethodSetObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_SET_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureSetObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage();
     * </pre>
     */
    protected void generateMethodGetRefObject(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT", StringUtils.capitalize(association.getTargetRoleSingular()),
                builder);
        generateSignatureGetRefObject(builder);
        builder.appendln(";");
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
     */
    @Override
    protected void generateCodeForCopyPropertiesForComposition(String paramName,
            String copyMapName,
            JavaCodeFragmentBuilder methodsBuilder,
            String field,
            org.faktorips.devtools.core.model.pctype.IPolicyCmptType targetType,
            String targetTypeQName) throws CoreException {

        methodsBuilder.appendln("if (");
        methodsBuilder.append(field);
        methodsBuilder.appendln("!=null) {");
        methodsBuilder.append(paramName);
        methodsBuilder.append(".");
        methodsBuilder.append(field);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.append(")");
        methodsBuilder.append(field);
        methodsBuilder.append(".");
        methodsBuilder.append(MethodNames.NEW_COPY);
        methodsBuilder.appendln("();");
        if (targetType.isDependantType() && inverseAssociation != null) {
            methodsBuilder.append(paramName);
            methodsBuilder.append(".");
            methodsBuilder.append(field);
            methodsBuilder.append(".");
            methodsBuilder.append(getMethodNameSetParentObjectInternal(true));
            methodsBuilder.append("(");
            methodsBuilder.append(paramName);
            methodsBuilder.appendln(");");
        }
        methodsBuilder.appendln("}");
        // TODO 2.5 - code - compare
        // methodsBuilder.append("if (").append(field).appendln("!=null) {");
        // methodsBuilder.append(paramName).append(".").append(field).append(" = (");
        // methodsBuilder.appendClassName(targetTypeQName);
        // methodsBuilder.append(")").append(field).append(".").append(PolicyCmptImplClassBuilder.METHOD_NEW_COPY)
        // //
        // .append("(").append(copyMapName).appendln(");");
        // if (targetType.isDependantType()) {
        // methodsBuilder.append(paramName).append(".").append(field).append(".").append(MethodNames.SET_PARENT)
        // .append("(").append(paramName).appendln(");");
        // }
        // methodsBuilder.append(copyMapName).append(".put(").append(field).append(", ").append(paramName).append('.')
        // .append(field).appendln(");");
        // methodsBuilder.appendln("}");
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        super.generateConstants(builder, ipsProject, generatesInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFieldNameForAssociation() throws CoreException {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
    }

    /**
     * Code sample for 1-1 composition
     * 
     * <pre>
     * copy.child1 = child1;
     * </pre>
     */
    @Override
    public void generateMethodCopyPropertiesForAssociation(String paramName, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String field = getFieldNameForAssociation();
        methodsBuilder.appendln(paramName + "." + field + " = " + field + ";");
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    @Override
    public void generateCodeForRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder, String paramName)
            throws CoreException {
        String fieldName = getFieldNameForAssociation();
        methodsBuilder.appendln("if (" + fieldName + "==" + paramName + ") {");
        methodsBuilder.appendln(fieldName + " = null;");
        methodsBuilder.appendln("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     if(getTplCoverage()!=null) {
     *         return getTplCoverage();
     *     }
     *     if (getCollisionCoverage()!=null) {
     *         return getCollisionCoverage();
     *     }
     *     return null;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectForContainerAssociationImplementation(List<IAssociation> subAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        for (int i = 0; i < subAssociations.size(); i++) {
            IPolicyCmptTypeAssociation subrel = (IPolicyCmptTypeAssociation)subAssociations.get(i);
            GenAssociation subrelGenerator = ((GenPolicyCmptType)getGenType()).getGenerator(subrel);
            String accessCode;
            accessCode = subrelGenerator.getMethodNameGetRefObject() + "()";
            methodsBuilder.appendln("if (" + accessCode + "!=null) {");
            methodsBuilder.appendln("return " + accessCode + ";");
            methodsBuilder.appendln("}");
        }
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
    }

    @Override
    public void generateCodeForContainerAssociationImplementation(List<IAssociation> associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodGetRefObjectForContainerAssociationImplementation(associations, methodsBuilder);
    }

    @Override
    public void generateCodeForValidateDependants(JavaCodeFragment body) throws CoreException {
        String field = getFieldNameForAssociation();
        body.append("if (" + field + "!=null) {");
        body.append("ml.add(" + field + ".validate(context));");
        body.append("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * if (coverage != null) {
     *     coverage.accept(visitor);
     * }
     * </pre> {@inheritDoc}
     */
    @Override
    public void generateSnippetForAcceptVisitor(String paramName, JavaCodeFragmentBuilder builder) throws CoreException {
        builder.appendln("if (" + fieldName + " != null) {");
        builder.appendln(fieldName + "." + MethodNames.ACCEPT_VISITOR + "(" + paramName + ");");
        builder.appendln("}");
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

    }

    /**
     * Code sample:
     * 
     * <pre>
     * if (copyMap.containsKey(policyHolder)) {
     *     copy.policyHolder = (Person)copyMap.get(policyHolder);
     * }
     * </pre>
     * 
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    @Override
    public void generateCodeForCopyAssociation(String varCopy, String varCopyMap, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.append("if (") //
                .append(varCopyMap).append(".containsKey(").append(getFieldNameForAssociation()) //
                .appendln("))").openBracket();
        methodsBuilder.append(varCopy).append('.').append(getFieldNameForAssociation()).append(" = ") //
                .append('(').append(getUnqualifiedClassName(getTargetPolicyCmptType(), false)).append(')') //
                .append(varCopyMap).append(".get(").append(getFieldNameForAssociation()).appendln(");");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample for associated person:
     * 
     * <pre>
     * if (person != null)
     *   Person copyPerson = (Person)copyMap.get(person);
     *   ((Person)person).copyAssociations(copyPerson, copyMap);
     * }
     * </pre> {@inheritDoc}
     * 
     * @throws CoreException
     */
    @Override
    public void generateCodeForCopyComposition(String varCopy, String varCopyMap, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.append("if (").append(fieldName).append(" != null)").openBracket();
        generateSnippetForCopyCompositions(varCopyMap, fieldName, methodsBuilder);
        methodsBuilder.closeBracket();
    }

}
