package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * helper class for PolicyCmptTypeInterfaceCuBuilder, responsible for code related to relation
 * handling
 */
public class PolicyCmptTypeInterfaceRelationBuilder {

    private final static String RELATION_INTERFACE_ADD_JAVADOC = "RELATION_INTERFACE_ADD_JAVADOC";
    private final static String RELATION_INTERFACE_CONTAINS_JAVADOC = "RELATION_CONTAINS_JAVADOC";
    private final static String RELATION_INTERFACE_GETALL_JAVADOC = "RELATION_INTERFACE_GETALL_JAVADOC";
    private final static String RELATION_INTERFACE_GETTER_JAVADOC = "RELATION_INTERFACE_GETTER_JAVADOC";
    private final static String RELATION_INTERFACE_NUMOF_JAVADOC = "RELATION_INTERFACE_NUMOF_JAVADOC";
    private final static String RELATION_INTERFACE_REMOVE_JAVADOC = "RELATION_INTERFACE_REMOVE_JAVADOC";
    private final static String RELATION_INTERFACE_SETTER_JAVADOC = "RELATION_INTERFACE_SETTER_JAVADOC";

    private PolicyCmptInterfaceBuilder cuBuilder;
    
    public PolicyCmptTypeInterfaceRelationBuilder(PolicyCmptInterfaceBuilder cuBuilder) {
        this.cuBuilder = cuBuilder;
    }

    private PolicyCmptInterfaceBuilder getPolicyCmptTypeInterfaceBuilder() {
        return cuBuilder;
    }

    void build1To1Relation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        if (!relation.isReadOnlyContainer()) {
            createRelationSetterMethodDeclaration(methodsBuilder, relation, target);
        }
    }

    void build1ToManyRelation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationContainsMethodDeclaration(methodsBuilder, relation, target);
        if (!relation.isReadOnlyContainer()) {
            createRelationAddMethodDeclaration(methodsBuilder, relation, target);
            createRelationRemoveMethodDeclaration(methodsBuilder, relation, target);
        }
    }

    // duplicate in PolicyCmptTypeImplRelationBuilder
    private String getPolicyCmptInterfaceAddPolicyCmptTypeMethod(IRelation r) {
        return "add" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationAddMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getPolicyCmptInterfaceAddPolicyCmptTypeMethod(relation);
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(relation, RELATION_INTERFACE_ADD_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            methodName,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private void createRelationContainsMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodname = "contains" + relation.getTargetRoleSingular(); 
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(relation, RELATION_INTERFACE_CONTAINS_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            methodname,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetMethod(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationGetterMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(relation, RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());
        String returnType = getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());
        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType,
            getPolicyCmptTypeInterfaceGetMethod(relation), new String[0], new String[0], javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceRemoveMethodName(IRelation r) {
        return "remove" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationRemoveMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(relation, RELATION_INTERFACE_REMOVE_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            getPolicyCmptTypeInterfaceRemoveMethodName(relation),
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceSetMethodName(IRelation r) {
        return "set" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationSetterMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(relation, RELATION_INTERFACE_SETTER_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            getPolicyCmptTypeInterfaceSetMethodName(relation),
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

}
