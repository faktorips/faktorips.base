package org.faktorips.devtools.stdbuilder.pctype;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * helper class for PolicyCmptTypeInterfaceCuBuilder, responsible for code related to relation
 * handling
 */
public class PolicyCmptTypeInterfaceRelationBuilder extends RelationInterfaceBuilder {

    private final static String RELATION_INTERFACE_ADD_JAVADOC = "RELATION_INTERFACE_ADD_JAVADOC";
    private final static String RELATION_INTERFACE_CONTAINS_JAVADOC = "RELATION_CONTAINS_JAVADOC";
    private final static String RELATION_INTERFACE_GETALL_JAVADOC = "RELATION_INTERFACE_GETALL_JAVADOC";
    private final static String RELATION_INTERFACE_GETTER_JAVADOC = "RELATION_INTERFACE_GETTER_JAVADOC";
    private final static String RELATION_INTERFACE_NUMOF_JAVADOC = "RELATION_INTERFACE_NUMOF_JAVADOC";
    private final static String RELATION_INTERFACE_REMOVE_JAVADOC = "RELATION_INTERFACE_REMOVE_JAVADOC";
    private final static String RELATION_INTERFACE_SETTER_JAVADOC = "RELATION_INTERFACE_SETTER_JAVADOC";

    public PolicyCmptTypeInterfaceRelationBuilder(PolicyCmptTypeInterfaceCuBuilder cuBuilder) {
        super(cuBuilder.getPcType(), cuBuilder);
    }

    private PolicyCmptTypeInterfaceCuBuilder getPolicyCmptTypeInterfaceBuilder() {
        return (PolicyCmptTypeInterfaceCuBuilder)getCuBuilder();
    }

    private void build1To1Relation(IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetterMethodDeclaration(relation, target);
        createRelationGetNumOfMethodDeclaration(relation);
        if (!relation.isReadOnlyContainer()) {
            createRelationSetterMethodDeclaration(relation, target);
        }
    }

    private void build1ToManyRelation(IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetNumOfMethodDeclaration(relation);
        createRelationGetAllMethodDeclaration(relation, target);
        createRelationContainsMethodDeclaration(relation, target);
        if (!relation.isReadOnlyContainer()) {
            createRelationAddMethodDeclaration(relation, target);
            createRelationRemoveMethodDeclaration(relation, target);
        }
    }

    protected void buildRelation(IRelation relation) throws CoreException {
        if (relation.is1ToMany()) {
            build1ToManyRelation(relation);
        } else {
            build1To1Relation(relation);
        }
    }

    // duplicate in PolicyCmptTypeImplRelationBuilder
    private String getPolicyCmptInterfaceAddPolicyCmptTypeMethod(IRelation r) {
        return "add" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationAddMethodDeclaration(IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getPolicyCmptInterfaceAddPolicyCmptTypeMethod(relation);
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_ADD_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            methodName,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

    private void createRelationContainsMethodDeclaration(IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodname = "contains" + relation.getTargetRoleSingular(); // TODO von IRelation
        // abfragen
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_CONTAINS_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            methodname,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetAllPcTypeMethodName(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    private void createRelationGetAllMethodDeclaration(IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String methodName = getPolicyCmptTypeInterfaceGetAllPcTypeMethodName(relation);
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_GETALL_JAVADOC,
            relation.getTargetRoleSingular());
        String returnType = getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile())
                + "[]";
        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType, methodName, new String[0],
            new String[0], javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetNumOfMethodName(IRelation r) {
        return "getAnzahl" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    private void createRelationGetNumOfMethodDeclaration(IRelation relation) throws CoreException {
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_NUMOF_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, Integer.TYPE,
            getPolicyCmptTypeInterfaceGetNumOfMethodName(relation), new String[0], new Class[0],
            javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetMethod(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationGetterMethodDeclaration(IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());
        String returnType = getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());
        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType,
            getPolicyCmptTypeInterfaceGetMethod(relation), new String[0], new String[0], javaDoc,
            BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

    private String getPolicyCmptTypeInterfaceRemoveMethodName(IRelation r) {
        return "remove" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationRemoveMethodDeclaration(IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_REMOVE_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            getPolicyCmptTypeInterfaceRemoveMethodName(relation),
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

    private String getPolicyCmptTypeInterfaceSetMethodName(IRelation r) {
        return "set" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationSetterMethodDeclaration(IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_SETTER_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            getPolicyCmptTypeInterfaceSetMethodName(relation),
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().appendln(";");
    }

}
