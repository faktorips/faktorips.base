/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenAssociationToMany extends GenAssociation {
    
    public GenAssociationToMany(IPolicyCmptTypeAssociation association, BasePolicyCmptTypeBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(association, builder, stringsSet);
    }
    
    /**
     * {@inheritDoc}
     */
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRolePlural());
    }
    
    /**
     * {@inheritDoc}
     */
    public String getMethodNameAddOrSetObject() {
        return getMethodNameAddObject();
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a multi-value association,
     * e.g. objectToTest
     */
    public String getParamNameForContainsObject() {
        return getLocalizedText("PARAM_OBJECT_TO_TEST_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverages()
     * </pre>
     */
    protected void generateSignatureGetNumOfRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetNumOfRefObjects();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "int", methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    protected String getMethodNameGetNumOfRefObjects() {
        return getLocalizedText("METHOD_GET_NUM_OF_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * public boolean containsCoverage(ICoverage objectToTest)
     * </pre>
     */
    protected void generateSignatureContainsObject() throws CoreException {
        String methodName = getMethodNameContainsObject();
        String paramName = getParamNameForContainsObject();
        getMethodBuilder().signature(java.lang.reflect.Modifier.PUBLIC, "boolean", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    protected String getMethodNameContainsObject() {
        return getLocalizedText("METHOD_CONTAINS_OBJECT_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    protected String getMethodNameContainsObject(IAssociation association) {
        return getLocalizedText("METHOD_CONTAINS_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     */
    public void generateSignatureGetAllRefObjects() throws CoreException {
        String methodName = getMethodNameGetAllRefObjects();
        String returnType = targetInterfaceName + "[]";
        getMethodBuilder().signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the referenced objects,
     * e.g. getCoverages()
     */
    protected String getMethodNameGetAllRefObjects() {
        return getLocalizedText("METHOD_GET_ALL_REF_OBJECTS_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index)
     * </pre>
     */
    protected void generateSignatureGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_INDEX", association.getTargetRoleSingular(), methodBuilder);
        methodBuilder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, getMethodNameGetRefObjectAtIndex(), 
                    new String[]{"index"}, new String[]{Integer.TYPE.getName()});
    }
    
    /**
     * Returns the name of the method that returns a reference object at a specified index.
     */
    public String getMethodNameGetRefObjectAtIndex(){
        //TODO extend JavaNamingConvensions for association accessor an mutator methods 
        return "get" + association.getTargetRoleSingular();
    }

    /**
     * Code sample:
     * <pre>
     * public void addCoverage(ICoverage objectToAdd)
     * </pre>
     */
    public void generateSignatureAddObject() throws CoreException {
        String methodName = getMethodNameAddObject();
        String paramName = getParamNameForAddObject();
        getMethodBuilder().signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * Returns the name of the method adding an object to a multi-value association,
     * e.g. getCoverage()
     */
    public String getMethodNameAddObject() {
        return getLocalizedText("METHOD_ADD_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method adding an object to a multi-value association,
     * e.g. objectToAdd
     */
    public String getParamNameForAddObject() {
        return getLocalizedText("PARAM_OBJECT_TO_ADD_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * public void removeCoverage(ICoverage objectToRemove)
     * </pre>
     */
    public void generateSignatureRemoveObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameRemoveObject();
        String paramName = getParamNameForRemoveObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * Returns the name of the method removing an object from a multi-value association,
     * e.g. removeCoverage()
     */
    public String getMethodNameRemoveObject() {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method removing an object from a multi-value association,
     * e.g. objectToRemove
     */
    public String getParamNameForRemoveObject() {
        return getLocalizedText("PARAM_OBJECT_TO_REMOVE_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        super.generateMemberVariables(builder, generatesInterface);
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
    public void generateMethods(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        
        if(generatesInterface){
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
                return;
            }
            generateMethodGetNumOfForNoneContainerAssociation(builder);
            generateMethodContainsObjectForNoneContainerAssociation(builder);
            generateMethodGetAllObjectsForNoneDerivedUnion(builder);
            generateMethodGetRefObjectAtIndex(builder);
            generateNewChildMethodsIfApplicable(builder, generatesInterface);
            generateMethodAddObject(builder);
            generateMethodRemoveObject(builder);
        }
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
        generateChangeListenerSupport("RELATION_OBJECT_ADDED" , paramName);
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
    
    /**
     * Code sample:
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
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    protected void generateMethodContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureContainsObject();
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages();
     * </pre>
     */
    protected void generateMethodGetAllRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_ALL_REF_OBJECTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetAllRefObjects();
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index);
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndexInterface(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.append(';');
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd);
     * </pre>
     */
    protected void generateMethodAddObjectInterface(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_ADD_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureAddObject();
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
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
    
    
}
