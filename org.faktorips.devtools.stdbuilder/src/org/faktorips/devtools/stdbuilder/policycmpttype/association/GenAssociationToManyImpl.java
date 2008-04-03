/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for the implementation aspects of associations. 
 * 
 * @author Jan Ortmann
 */
public class GenAssociationToManyImpl extends GenAssociationToMany {

    private String targetImplClassName;
    
    public GenAssociationToManyImpl(IPolicyCmptTypeAssociation association, PolicyCmptImplClassBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(association, builder, stringsSet, true);
        targetImplClassName = builder.getQualifiedClassName(target);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException {
        super.generateMemberVariables(builder);
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();
        initialValueExpression.append("new ");
        initialValueExpression.appendClassName(ArrayList.class);
        initialValueExpression.append("()");
        String comment = getLocalizedText("FIELD_RELATION_JAVADOC", association.getName());
        builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        builder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, List.class, fieldName, initialValueExpression);
    }

    /**
     * {@inheritDoc}
     */
    public void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isDerivedUnion()) {
            return;
        }
        generateMethodGetNumOfForNoneContainerAssociation(builder);
        generateMethodContainsObjectForNoneContainerAssociation(builder);
        generateMethodGetAllObjectsForNoneDerivedUnion(builder);
        generateMethodGetRefObjectAtIndex(builder);
        generateNewChildMethodsIfApplicable(builder);
        generateMethodAddObject(builder);
        generateMethodRemoveObject(builder);
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
    private void generateMethodGetNumOfForNoneContainerAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetNumOfRefObjects(methodsBuilder);
        methodsBuilder.openBracket();
        String field = fieldName;
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
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     return coverages.contains(objectToTest);
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForNoneContainerAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String paramName = getParamNameForContainsObject();
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureContainsObject();
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return " + fieldName + ".contains(" + paramName + ");");
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
    protected void generateMethodGetAllObjectsForNoneDerivedUnion(
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetAllRefObjects();
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return (");
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append("[])");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".toArray(new ");
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append('[');
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".size()]);");
        methodsBuilder.closeBracket();
    }
    
    /**
     * <pre>
     * public IMotorCoverage getMotorCoverage(int index) {
     *      return (IMotorCoverage)motorCoverages.get(index);
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.openBracket();
        methodBuilder.append("return (");
        methodBuilder.appendClassName(targetInterfaceName);
        methodBuilder.append(')');
        methodBuilder.append(fieldName);
        methodBuilder.append(".get(index);");
        methodBuilder.closeBracket();
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
    protected void generateMethodAddObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureAddObject();
        String paramName = getParamNameForAddObject();
        IPolicyCmptTypeAssociation reverseAssociation = association.findInverseAssociation(getIpsProject());
        methodsBuilder.openBracket();
        methodsBuilder.append("if (" + paramName + " == null) {");
        methodsBuilder.append("throw new ");
        methodsBuilder.appendClassName(NullPointerException.class);
        methodsBuilder.append("(\"Can't add null to association " + association.getName() + " of \" + this); }");
        methodsBuilder.append("if(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".contains(" + paramName + ")) { return; }");
        if (association.isCompositionMasterToDetail()) {
            if (target!=null && target.isDependantType()) {
                methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "this"));
            }
        }
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".add(" + paramName + ");");
        PolicyCmptImplClassBuilder implClassBuilder = (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
        if (association.isAssoziation() && reverseAssociation!=null) {
            methodsBuilder.append(implClassBuilder.generateCodeToSynchronizeReverseAssoziation(paramName, targetInterfaceName, association, reverseAssociation));
        }
        implClassBuilder.generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "RELATION_OBJECT_ADDED" , fieldName, paramName);
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
    protected void generateMethodRemoveObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String paramName = getParamNameForRemoveObject();
        IPolicyCmptTypeAssociation reverseAssociation = association.findInverseAssociation(getIpsProject());
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureRemoveObject(methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + "== null) {return;}");
        
        if (reverseAssociation != null || (association.isComposition() && target!=null && target.isDependantType())) {
            methodsBuilder.append("if(");
        }
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".remove(" + paramName + ")");
        PolicyCmptImplClassBuilder implClassBuilder = (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
        if (reverseAssociation != null || (association.isComposition() && target!=null && target.isDependantType())) {
            methodsBuilder.append(") {");
            if (association.isAssoziation()) {
                methodsBuilder.append(implClassBuilder.generateCodeToCleanupOldReference(association, reverseAssociation, paramName));
            } else {
                methodsBuilder.append(generateCodeToSynchronizeReverseComposition(paramName, "null"));
            }
            methodsBuilder.append(" }");
        } else {
            methodsBuilder.append(';');
        }
        implClassBuilder.generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "RELATION_OBJECT_REMOVED", fieldName, paramName);
        methodsBuilder.closeBracket();
    }
    
}
