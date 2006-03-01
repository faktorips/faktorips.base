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
    }

    void build1ToManyRelation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        if (!relation.isReadOnlyContainer()) {
            createRelationRemoveMethodDeclaration(methodsBuilder, relation, target);
        }
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
