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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.runtime.internal.DependantObject;
import org.faktorips.runtime.internal.MethodNames;

/**
 * 
 * @author Jan Ortmann
 */
public class GenAssociationToMany extends GenAssociation {

    public GenAssociationToMany(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAssociation association)
            throws CoreException {
        super(genPolicyCmptType, association);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRolePlural());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethodNameAddOrSetObject() {
        return getMethodNameAddObject();
    }

    /**
     * Returns the name of the parameter for the method that tests if an object is references in a
     * multi-value association, e.g. objectToTest.
     */
    @Override
    public String getParamNameForContainsObject() {
        return getLocalizedText("PARAM_OBJECT_TO_TEST_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the method returning the number of referenced objects, e.g.
     * getNumOfCoverages().
     */
    protected String getMethodNameContainsObject(IAssociation association) {
        return getLocalizedText("METHOD_CONTAINS_OBJECT_NAME", StringUtils.capitalize(association
                .getTargetRoleSingular()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * public List&lt;ICoverage&gt; getCoverages()
     * </pre>
     */
    @Override
    public void generateSignatureGetAllRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetAllRefObjects();
        String returnType;
        if (isUseTypesafeCollections()) {
            returnType = List.class.getName() + "<" + targetInterfaceName + ">";
        } else {
            returnType = targetInterfaceName + "[]";
        }
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Returns the name of the method that returns a reference object at a specified index.
     */
    @Override
    public String getMethodNameGetRefObjectAtIndex() {
        // TODO extend JavaNamingConventions for association accessor and mutator methods
        return "get" + StringUtils.capitalize(association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void addCoverage(ICoverage objectToAdd)
     * </pre>
     */
    public void generateSignatureAddObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameAddObject();
        String paramName = getParamNameForAddObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[] { paramName },
                new String[] { targetInterfaceName });
    }

    /**
     * Returns the name of the method adding an object to a multi-value association, e.g.
     * addCoverage().
     */
    public String getMethodNameAddObject() {
        return getLocalizedText("METHOD_ADD_OBJECT_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the parameter for the method adding an object to a multi-value
     * association, e.g. objectToAdd.
     */
    public String getParamNameForAddObject() {
        return getLocalizedText("PARAM_OBJECT_TO_ADD_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void removeCoverage(ICoverage objectToRemove)
     * </pre>
     */
    public void generateSignatureRemoveObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameRemoveObject();
        String paramName = getParamNameForRemoveObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[] { paramName },
                new String[] { targetInterfaceName });
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
     * Returns the name of the parameter for the method removing an object from a multi-value
     * association, e.g. objectToRemove.
     */
    @Override
    public String getParamNameForRemoveObject() {
        return getLocalizedText("PARAM_OBJECT_TO_REMOVE_NAME", association.getTargetRoleSingular());
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        super.generateMemberVariables(builder, ipsProject, generatesInterface);
        if (!isDerivedUnion()) {
            JavaCodeFragment initialValueExpression = new JavaCodeFragment();
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(ArrayList.class);
            if (isUseTypesafeCollections()) {
                initialValueExpression.append("<");
                initialValueExpression.appendClassName(targetInterfaceName);
                initialValueExpression.append(">");
            }
            initialValueExpression.append("()");
            String comment = getLocalizedText("FIELD_ASSOCIATION_JAVADOC", association.getName());
            builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);

            if (isGenerateJaxbSupport()) {
                if (!isCompositionDetailToMaster()) {
                    builder.annotationLn("javax.xml.bind.annotation.XmlElement", "name=\"" + association.getName()
                            + "\", type=" + targetImplClassName + ".class");
                    if (!isCompositionMasterToDetail()) {
                        builder.annotationLn("javax.xml.bind.annotation.XmlIDREF");
                    }
                }
                builder.annotationLn("javax.xml.bind.annotation.XmlElementWrapper", "name", association
                        .getTargetRolePlural());
            }

            builder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, List.class.getName()
                    + (isUseTypesafeCollections() ? "<" + targetInterfaceName + ">" : ""), fieldName,
                    initialValueExpression);
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
            generateMethodGetNumOfRefObjects(builder);
            generateMethodContainsObject(builder);
            generateMethodGetAllRefObjects(builder);
            if (!association.isDerivedUnion()) {
                generateMethodAddObjectInterface(builder);
                generateMethodRemoveObjectInterface(builder);
                generateNewChildMethodsIfApplicable(builder, generatesInterface);
                generateMethodGetRefObjectAtIndexInterface(builder);
            }
        } else {
            if (isDerivedUnion()) {
                generateMethodContainsObjectForContainerAssociation(builder);
            } else {
                generateMethodGetNumOfForNoneContainerAssociation(builder);
                generateMethodContainsObjectForNoneContainerAssociation(builder);
                generateMethodGetAllObjectsForNoneDerivedUnion(builder);
                generateMethodGetRefObjectAtIndex(builder);
                generateNewChildMethodsIfApplicable(builder, generatesInterface);
                generateMethodAddObject(builder);
                generateMethodRemoveObject(builder);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     return coverages.size();
     * }
     * </pre>
     */
    private void generateMethodGetNumOfForNoneContainerAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetNumOfRefObjects(methodsBuilder);
        methodsBuilder.openBracket();
        if (association.is1ToMany()) {
            methodsBuilder.appendln("return " + fieldName + ".size();");
        } else {
            methodsBuilder.appendln("return " + fieldName + "==null ? 0 : 1;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     return coverages.contains(objectToTest);
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForNoneContainerAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        String paramName = getParamNameForContainsObject();
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureContainsObject(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return " + fieldName + ".contains(" + paramName + ");");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages() {
     *     return (ICoverage[])coverages.toArray(new ICoverage[coverages.size()]);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverage&gt; getCoverages() {
     *     return Collections.unmodifiableList(coverages);
     * }
     * </pre>
     */
    protected void generateMethodGetAllObjectsForNoneDerivedUnion(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetAllRefObjects(methodsBuilder);
        methodsBuilder.openBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("return ");
            methodsBuilder.appendClassName(Collections.class.getName());
            methodsBuilder.append(".unmodifiableList(");
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln(");");
        } else {
            methodsBuilder.append("return (");
            methodsBuilder.appendClassName(targetImplClassName);
            methodsBuilder.append("[])");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(".toArray(new ");
            methodsBuilder.appendClassName(targetImplClassName);
            methodsBuilder.append('[');
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln(".size()]);");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample
     * 
     * <pre>
     * public IMotorCoverage getMotorCoverage(int index) {
     *     return (IMotorCoverage)motorCoverages.get(index);
     * }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * public IMotorCoverage getMotorCoverage(int index) {
     *     return motorCoverages.get(index);
     * }
     * </pre>
     */
    @Override
    protected void generateMethodGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.openBracket();
        methodBuilder.append("return ");
        if (isUseTypesafeCollections()) {

        } else {
            methodBuilder.append("(");
            methodBuilder.appendClassName(targetInterfaceName);
            methodBuilder.append(')');
        }
        methodBuilder.append(fieldName);
        methodBuilder.append(".get(index);");
        methodBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd) {
     *     if(objectToAdd == null) {
     *         throw new IllegalArgumentException(&quot;Can't add null to ...&quot;);
     *     }
     *     if (coverages.contains(objectToAdd)) {
     *         return;
     *     }
     *     coverages.add(objectToAdd);
     * }
     * </pre>
     */
    protected void generateMethodAddObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureAddObject(methodsBuilder);
        String paramName = getParamNameForAddObject();
        methodsBuilder.openBracket();
        methodsBuilder.append("if (" + paramName + " == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("throw new ");
        methodsBuilder.appendClassName(NullPointerException.class);
        methodsBuilder.append("(\"Can't add null to association " + association.getName() + " of \" + this);");
        methodsBuilder.closeBracket();
        methodsBuilder.append("if(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".contains(" + paramName + "))");
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return;");
        methodsBuilder.closeBracket();
        if (association.isCompositionMasterToDetail()) {
            if (target != null && target.isDependantType()) {
                methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "this"));
            }
        }
        generateChangeListenerSupportBeforeChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_ADDED, paramName);
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".add(" + paramName + ");");
        if (association.isAssoziation() && reverseAssociation != null) {
            methodsBuilder.append(getGenPolicyCmptType().getBuilderSet().getGenerator(target).getGenerator(
                    reverseAssociation).generateCodeToSynchronizeReverseAssoziation(paramName, targetInterfaceName));
        }
        generateChangeListenerSupportAfterChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_ADDED, paramName);
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
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
    protected void generateMethodRemoveObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String paramName = getParamNameForRemoveObject();
        IPolicyCmptTypeAssociation reverseAssociation = association.findInverseAssociation(getIpsProject());

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureRemoveObject(methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + "== null)");
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return;");
        methodsBuilder.closeBracket();

        generateChangeListenerSupportBeforeChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_REMOVED, paramName);
        if (reverseAssociation != null || association.isComposition() && target != null && target.isDependantType()) {
            methodsBuilder.append("if(");
        }
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".remove(" + paramName + ")");
        if (reverseAssociation != null || association.isComposition() && target != null && target.isDependantType()) {
            methodsBuilder.append(")");
            methodsBuilder.openBracket();
            if (association.isAssoziation()) {
                methodsBuilder.append(generateCodeToCleanupOldReference(association, reverseAssociation, paramName));
            } else {
                methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "null"));
            }
            methodsBuilder.closeBracket();
        } else {
            methodsBuilder.appendln(';');
        }
        generateChangeListenerSupportAfterChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_REMOVED, paramName);
        methodsBuilder.closeBracket();
    }

    private JavaCodeFragment generateCodeToCleanupOldReference(IPolicyCmptTypeAssociation association,
            IPolicyCmptTypeAssociation reverseAssociation,
            String varToCleanUp) throws CoreException {

        JavaCodeFragment body = new JavaCodeFragment();
        if (!association.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (reverseAssociation.is1ToMany()) {
            String removeMethod = ((GenAssociationToMany)getGenPolicyCmptType().getGenerator(reverseAssociation))
                    .getMethodNameRemoveObject();
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String targetClass = getGenPolicyCmptType().getBuilderSet().getGenerator(
                    (IPolicyCmptType)association.findTarget(getIpsProject())).getQualifiedName(false);
            String setMethod = getGenPolicyCmptType().getGenerator(reverseAssociation).getMethodNameSetObject();
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
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages();
     * </pre>
     */
    protected void generateMethodGetNumOfRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_NUM_OF", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetNumOfRefObjects(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    @Override
    protected void generateMethodContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureContainsObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages();
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverage&gt; getCoverages();
     * </pre>
     */
    protected void generateMethodGetAllRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_ALL_REF_OBJECTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetAllRefObjects(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index);
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndexInterface(JavaCodeFragmentBuilder methodBuilder)
            throws CoreException {
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.append(';');
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd);
     * </pre>
     */
    protected void generateMethodAddObjectInterface(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_ADD_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureAddObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void removeCoverage(ICoverage objectToRemove);
     * </pre>
     */
    protected void generateMethodRemoveObjectInterface(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_REMOVE_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureRemoveObject(methodsBuilder);
        methodsBuilder.appendln(";");
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
        return getJavaNamingConvention().getMemberVarName(association.getTargetRolePlural());
    }

    /**
     * Code sample for 1-Many composition
     * 
     * <pre>
     * copy.child2s.addAll(child2s);
     * </pre>
     */
    @Override
    public void generateMethodCopyPropertiesForAssociation(String paramName, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String field = getFieldNameForAssociation();
        methodsBuilder.appendln(paramName + "." + field + ".addAll(" + field + ");");
    }

    protected void generateCodeForCopyPropertiesForComposition(String paramName,
            JavaCodeFragmentBuilder methodsBuilder,
            String field,
            IPolicyCmptType targetType,
            String targetTypeQName) throws CoreException {
        String varOrig = StringUtils.uncapitalize(QNameUtil.getUnqualifiedName(targetTypeQName));
        String varCopy = "copy" + StringUtils.capitalize(varOrig);
        methodsBuilder.append("for (");
        methodsBuilder.appendClassName(Iterator.class);
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(getQualifiedClassName(targetType, true));
            methodsBuilder.append(">");
        }
        methodsBuilder.appendln(" it = " + field + ".iterator(); it.hasNext();) {");

        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.append(" " + varOrig + " = ( ");
        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.appendln(")it.next();");

        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.append(" " + varCopy + " = ( ");
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

    /**
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    public void generateCodeForRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder, String paramName)
            throws CoreException {
        String fieldName = getFieldNameForAssociation();
        methodsBuilder.appendln(fieldName + ".remove(" + paramName + ");");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     return getNumOfCoveragesInternal();
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfForContainerAssociationImplementation(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);

        IPolicyCmptType supertype = (IPolicyCmptType)getGenPolicyCmptType().getPolicyCmptType().findSupertype(
                getIpsProject());
        appendOverrideAnnotation(methodsBuilder, getIpsProject(), (supertype == null || supertype.isAbstract()));
        generateSignatureGetNumOfRefObjects(methodsBuilder);
        methodsBuilder.openBracket();
        String methodName = getMethodNameGetNumOfRefObjectsInternal();
        methodsBuilder.append("return " + methodName + "();");
        methodsBuilder.closeBracket();
    }

    /**
     * Returns the name of the internal method returning the number of referenced objects, e.g.
     * getNumOfCoveragesInternal()
     */
    private String getMethodNameGetNumOfRefObjectsInternal() {
        return getLocalizedText("METHOD_GET_NUM_OF_INTERNAL_NAME", StringUtils.capitalize(association
                .getTargetRolePlural()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages() {
     *     ICoverage[] result = new ICoverage[getNumOfCoveragesInternal()];
     *     ICoverage[] superResult = super.getCoverages();
     *     System.arraycopy(superResult, 0, result, 0, superResult.length);
     *     int counter = superResult.length;
     *     ICoverage[] elements;
     *     counter = 0;
     *     elements = getTplCoverages();
     *     for (int i = 0; i &lt; elements.length; i++) {
     *         result[counter] = elements[i];
     *         counter++;
     *     }
     *     return result;
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverage&gt; getCoverages() {
     *     List&lt;ICoverage&gt; result = new ArrayList&lt;ICoverage&gt;(getNumOfCoveragesInternal());
     *            result.addAll(super.getCoverages());
     *     result.addAll(getTplCoverages());
     *     return result;
     * }
     * </pre>
     */
    protected void generateMethodGetAllRefObjectsForContainerAssociationImplementation(List<IAssociation> subAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);

        IPolicyCmptType supertype = (IPolicyCmptType)getGenPolicyCmptType().getPolicyCmptType().findSupertype(
                getIpsProject());
        appendOverrideAnnotation(methodsBuilder, getIpsProject(), (supertype == null || supertype.isAbstract()));
        generateSignatureGetAllRefObjects(methodsBuilder);
        String classname = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);

        methodsBuilder.openBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendClassName(List.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(classname);
            methodsBuilder.append("> result = new ");
            methodsBuilder.appendClassName(ArrayList.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(classname);
            methodsBuilder.append(">(" + getMethodNameGetNumOfRefObjectsInternal() + "());");
        } else {
            methodsBuilder.appendClassName(classname);
            methodsBuilder.append("[] result = new ");
            methodsBuilder.appendClassName(classname);
            methodsBuilder.append("[" + getMethodNameGetNumOfRefObjectsInternal() + "()];");
        }

        if (supertype != null && !supertype.isAbstract()) {
            if (isUseTypesafeCollections()) {
                // result.addAll(super.getCoverages());
                methodsBuilder.append("result.addAll(super.");
                methodsBuilder.appendln(getMethodNameGetAllRefObjects() + "());");
            } else {
                // ICoverage[] superResult = super.getCoverages();
                // System.arraycopy(superResult, 0, result, 0, superResult.length);
                // int counter = superResult.length;
                methodsBuilder.appendClassName(classname);
                methodsBuilder.append("[] superResult = super.");
                methodsBuilder.appendln(getMethodNameGetAllRefObjects() + "();");

                methodsBuilder.appendln("System.arraycopy(superResult, 0, result, 0, superResult.length);");
                methodsBuilder.appendln("int counter = superResult.length;");
            }
        } else {
            if (!isUseTypesafeCollections()) {
                methodsBuilder.append("int counter = 0;");
            }
        }

        boolean elementsVarDefined = false;
        for (int i = 0; i < subAssociations.size(); i++) {
            IPolicyCmptTypeAssociation subrel = (IPolicyCmptTypeAssociation)subAssociations.get(i);
            GenAssociation subrelGenerator = getGenPolicyCmptType().getGenerator(subrel);
            if (subrel.is1ToMany()) {
                String method = subrelGenerator.getMethodNameGetAllRefObjects();
                if (isUseTypesafeCollections()) {
                    methodsBuilder.appendln("result.addAll(" + method + "());");
                } else {
                    if (!elementsVarDefined) {
                        methodsBuilder.appendClassName(classname);
                        methodsBuilder.append("[] ");
                        elementsVarDefined = true;
                    }
                    methodsBuilder.appendln("elements = " + method + "();");
                    methodsBuilder.appendln("for (int i=0; i<elements.length; i++) {");
                    methodsBuilder.appendln("result[counter++] = elements[i];");
                    methodsBuilder.appendln("}");
                }
            } else {
                String method = subrelGenerator.getMethodNameGetRefObject();
                methodsBuilder.appendln("if (" + method + "()!=null) {");
                if (isUseTypesafeCollections()) {
                    methodsBuilder.appendln("result.add(" + method + "());");
                } else {
                    methodsBuilder.appendln("result[counter++] = " + method + "();");
                }
                methodsBuilder.appendln("}");
            }
        }
        methodsBuilder.append("return result;");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
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
    protected void generateMethodGetNumOfInternalForContainerAssociationImplementation(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(null, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        String methodName = getMethodNameGetNumOfRefObjectsInternal();
        methodsBuilder.signature(java.lang.reflect.Modifier.PRIVATE, "int", methodName, new String[] {},
                new String[] {});
        methodsBuilder.openBracket();
        methodsBuilder.append("int num = 0;");
        IPolicyCmptType supertype = (IPolicyCmptType)getGenPolicyCmptType().getPolicyCmptType().findSupertype(
                getIpsProject());
        if (supertype != null && !supertype.isAbstract()) {
            String methodName2 = getMethodNameGetNumOfRefObjects();
            methodsBuilder.appendln("num += super." + methodName2 + "();");
        }
        for (int i = 0; i < implAssociations.size(); i++) {
            methodsBuilder.appendln();
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)implAssociations.get(i);
            GenAssociation gen = getGenPolicyCmptType().getGenerator(association);
            methodsBuilder.append("num += ");
            if (association.is1ToMany()) {
                methodsBuilder.append(gen.getMethodNameGetNumOfRefObjects() + "();");
            } else {
                String field = gen.getFieldNameForAssociation();
                methodsBuilder.append(field + "==null ? 0 : 1;");
            }
        }
        methodsBuilder.append("return num;");
        methodsBuilder.closeBracket();
    }

    public void generateCodeForContainerAssociationImplementation(List<IAssociation> associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodGetNumOfForContainerAssociationImplementation(associations, methodsBuilder);
        generateMethodGetAllRefObjectsForContainerAssociationImplementation(associations, methodsBuilder);
        generateMethodGetNumOfInternalForContainerAssociationImplementation(associations, methodsBuilder);
    }

    /**
     * Code sample
     * 
     * <pre>
     * public void validateDependants(MessageList ml, String businessFunction) {
     *     super.validateDependants(ml, businessFunction);
     *     if (getNumOfFtCoverages() &gt; 0) {
     *         IFtCoverage[] rels = getFtCoverages();
     *         for (int i = 0; i &lt; rels.length; i++) {
     *             ml.add(rels[i].validate(businessFunction));
     *         }
     *     }
     * }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * public void validateDependants(MessageList ml, String businessFunction) {
     *     super.validateDependants(ml, businessFunction);
     *     if (getNumOfFtCoverages() &gt; 0) {
     *         List&lt;IFtCoverage&gt; rels = getFtCoverages();
     *         for (IFtCoverage rel : rels) {
     *             ml.add(rel.validate(businessFunction));
     *         }
     *     }
     * }
     * </pre>
     */
    public void generateCodeForValidateDependants(JavaCodeFragment body) throws CoreException {
        IPolicyCmptType target = getTargetPolicyCmptType();
        body.append("if(");
        body.append(getMethodNameGetNumOfRefObjects());
        body.append("() > 0) { ");
        if (isUseTypesafeCollections()) {
            body.append("for (");
            body.appendClassName(getQualifiedClassName(target, true));
            body.append(" rel : ");
            body.append(getMethodNameGetAllRefObjects());
            body.append("())");
            body.append("{ ml.add(rel.validate(context)); } }");
        } else {
            body.appendClassName(getQualifiedClassName(target, true));
            body.append("[]");
            body.append(" rels = ");
            body.append(getMethodNameGetAllRefObjects());
            body.append("();");
            body.append("for (int i = 0; i < rels.length; i++)");
            body.append("{ ml.add(rels[i].validate(context)); } }");
        }
    }

    public JavaCodeFragment generateCodeToSynchronizeReverseAssoziation(String varName, String varClassName)
            throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        if (!reverseAssociation.is1ToMany()) {
            code.append(varName + " != null && ");
        }
        code.append("! " + varName + ".");
        code.append(getMethodNameContainsObject(association) + "(this)");
        code.appendln(") {");
        code.append(varName + "." + getMethodNameAddObject());
        code.appendln("(this);");
        code.appendln("}");
        return code;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * for (Iterator it = visitedSubChilds.iterator(); it.hasNext();) {
     *     IVisitedSubChild child = (IVisitedSubChild)it.next();
     *     child.accept(visitor);
     * }
     * </pre>
     * 
     * Java 5 Code sample:
     * 
     * <pre>
     * for (IVisitedSubChild child : visitedSubChilds) {
     *     child.accept(visitor);
     * }
     * </pre> {@inheritDoc}
     */
    public void generateSnippetForAcceptVisitor(String paramName, JavaCodeFragmentBuilder builder) throws CoreException {
        String varName = getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
        if (isUseTypesafeCollections()) {
            builder.append("for (");
            builder.appendClassName(targetInterfaceName);
            builder.appendln(" " + varName + " : " + fieldName + ") {");
        } else {
            builder.append("for (");
            builder.appendClassName(Iterator.class);
            builder.appendln(" it = " + fieldName + ".iterator(); it.hasNext();) {");
            builder.appendClassName(targetInterfaceName);
            builder.appendln(" " + varName + " = (");
            builder.appendClassName(targetInterfaceName);
            builder.appendln(")it.next();");
        }
        builder.appendln(varName + "." + MethodNames.ACCEPT_VISITOR + "(" + paramName + ");");
        builder.appendln("}");
    }

    @Override
    public void getGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean forInterface) {

        // TODO AW: Not implemented yet.
    }

}
