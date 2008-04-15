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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenAssociationTo1 extends GenAssociation {

    public GenAssociationTo1(IPolicyCmptTypeAssociation association, BasePolicyCmptTypeBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(association, builder, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * <pre>
     * public void setCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureSetObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameSetObject();
        String paramName = getParamNameForSetObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * {@inheritDoc}
     */
    public String getMethodNameAddOrSetObject() {
        return getMethodNameSetObject();
    }

    /**
     * Returns the name of the method setting the referenced object.
     * e.g. setCoverage(ICoverage newObject)
     */
    public String getMethodNameSetObject() {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Returns the name of the method setting the referenced object.
     * e.g. setCoverage(ICoverage newObject)
     */
    public String getMethodNameSetObject(IAssociation association) {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a multi-value association,
     * e.g. objectToTest
     */
    public String getParamNameForSetObject() {
        return getLocalizedText("PARAM_OBJECT_TO_SET_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Returns the name of the method removing an object from a multi-value association,
     * e.g. removeCoverage()
     */
    public String getMethodNameRemoveObject() {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Returns the name of the method removing an object from a multi-value association,
     * e.g. removeCoverage()
     */
    public String getMethodNameRemoveObject(IAssociation association) {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME", association.getTargetRoleSingular());
    }


    /**
     * Code sample:
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    public void generateSignatureGetRefObject(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetRefObject();
        builder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the single referenced object.
     * e.g. getCoverage()
     */
    public String getMethodNameGetRefObject() {
        return getLocalizedText("METHOD_GET_REF_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the method returning the single referenced object.
     * e.g. getCoverage()
     */
    public String getMethodNameGetRefObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText("METHOD_GET_REF_OBJECT_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        if (!isDerivedUnion() && !isCompositionDetailToMaster()) {
            String comment = getLocalizedText("FIELD_RELATION_JAVADOC", association.getName());
            builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
            builder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, targetImplClassName, fieldName, new JavaCodeFragment("null"));
        }        
    }

    /**
     * {@inheritDoc}
     */
    public void generateMethods(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        
        if(generatesInterface){
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
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        if (association.isCompositionDetailToMaster()) {
            return; // setter defined in base class.
        }
        String paramName = getParamNameForSetObject();
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureSetObject(builder);
        
        builder.openBracket();
        if (target.isDependantType()) {
            builder.appendln("if(" + fieldName + " != null) {");
            builder.append(generateCodeToSynchronizeReverseComposition(fieldName, "null"));;
            builder.appendln("}");
        }

        if (target.isDependantType()) {
            builder.appendln("if(" + paramName + " != null) {");
            builder.append(generateCodeToSynchronizeReverseComposition(paramName, "this"));;
            builder.appendln("}");
        }
        
        builder.append(fieldName);
        builder.append(" = (");
        builder.appendClassName(targetImplClassName);
        builder.append(")" + paramName +";");

        generateChangeListenerSupport("RELATION_OBJECT_CHANGED", paramName);
        builder.closeBracket();
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
    protected void generateMethodSetRefObjectForAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String paramName = getParamNameForSetObject();
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureSetObject(methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + " == ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(") return;");
        if (reverseAssociation != null) {
            methodsBuilder.appendClassName(targetImplClassName);
            methodsBuilder.append(" oldRefObject = ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(';');
            methodsBuilder.append(fieldName);
            methodsBuilder.append(" = null;");
            methodsBuilder.append(generateCodeToCleanupOldReference("oldRefObject"));
        }
        methodsBuilder.append(fieldName);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append(")" + paramName +";");
        if (reverseAssociation != null) {
            methodsBuilder.append(generateCodeToSynchronizeReverseAssoziation(fieldName, targetImplClassName));
        }
        generateChangeListenerSupport("RELATION_OBJECT_CHANGED", paramName);
        methodsBuilder.closeBracket();
    }
    
    public JavaCodeFragment generateCodeToCleanupOldReference(String varToCleanUp) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        if (!association.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (reverseAssociation.is1ToMany()) {
            String removeMethod = getMethodNameRemoveObject(reverseAssociation);
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String setMethod = getMethodNameSetObject(reverseAssociation);
            body.append("((");
            body.appendClassName(targetImplClassName);
            body.append(")" + varToCleanUp + ")." + setMethod + "(null);");
        }
        if (!association.is1ToMany()) {
            body.append(" }");
        }
        return body;
    }

    // TODO refactor
    // hier muesste man den korrekten Generator fuer die reverse association bekommen und
    // dort dann ein generateCodeToSynchronizeReverseAssoziation aufrufen. Implementierung der Method
    // fuer toMany und to1 unterschiedlich.
    public JavaCodeFragment generateCodeToSynchronizeReverseAssoziation(
            String varName,
            String varClassName) throws CoreException {

        GenAssociationToMany toManyGenerator = null;
        if (reverseAssociation.is1ToMany()) {
            toManyGenerator = new GenAssociationToMany(reverseAssociation, (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder(), new LocalizedStringsSet(GenAssociation.class));
        }

        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        code.append(varName + " != null && ");
        if (reverseAssociation.is1ToMany()) {
            code.append("! " + varName + ".");
            code.append(toManyGenerator.getMethodNameContainsObject(reverseAssociation) + "(this)");
        } else {
            code.append(varName + ".");
            code.append(getMethodNameGetRefObject(reverseAssociation));
            code.append("() != this");
        }
        code.append(") {");
        if (reverseAssociation.is1ToMany()) {
            code.append(varName + "." + toManyGenerator.getMethodNameAddObject());
        } else {
            if (!varClassName.equals(targetImplClassName)) {
                code.append("((");
                code.appendClassName(targetImplClassName);
                code.append(")" + varName + ").");
            } else {
                code.append(varName + ".");
            }
            code.append(getMethodNameSetObject(reverseAssociation));
        }
        code.appendln("(this);");
        code.appendln("}");
        return code;
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
    protected void generateMethodGetRefObjectBasedOnMemberVariable(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
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
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return (ICoverage)getParentModelObject();
     * }
     * </pre>
     */
    protected void generateMethodGetTypesafeParentObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetInterfaceName);
        methodsBuilder.append(")" + MethodNames.GET_PARENT + "();");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
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
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage();
     * </pre>
     */
    protected void generateMethodGetRefObject(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT", StringUtils.capitalize(association.getTargetRoleSingular()), builder);
        generateSignatureGetRefObject(builder);
        builder.appendln(";");
    }

}
