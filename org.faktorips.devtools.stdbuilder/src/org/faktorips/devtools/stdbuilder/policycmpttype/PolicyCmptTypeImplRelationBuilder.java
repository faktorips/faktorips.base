package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * helper class for PolicyCmptTypeImplCuBuilder, responsible for code related to relation handling
 */
public class PolicyCmptTypeImplRelationBuilder extends RelationImplBuilder {

    private final static String RELATION_FIELD_COMMENT = "RELATION_FIELD_COMMENT";
    private final static String RELATION_IMPLEMENTATION_ADD_JAVADOC = "RELATION_IMPLEMENTATION_ADD_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_CONTAINS_JAVADOC = "RELATION_CONTAINS_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_GETALL_JAVADOC = "RELATION_IMPLEMENTATION_GETALL_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_GETTER_JAVADOC = "RELATION_IMPLEMENTATION_GETTER_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_NUMOF_JAVADOC = "RELATION_IMPLEMENTATION_NUMOF_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_REMOVE_JAVADOC = "RELATION_IMPLEMENTATION_REMOVE_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_SETTER_JAVADOC = "RELATION_IMPLEMENTATION_SETTER_JAVADOC";

    private PolicyCmptImplClassBuilder policyCmptTypeImplCuBuilder;

    public PolicyCmptTypeImplRelationBuilder(PolicyCmptImplClassBuilder cuBuilder) {
        super(cuBuilder.getPcType());
        policyCmptTypeImplCuBuilder = cuBuilder;
    }

    protected void buildContainerRelation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation containerRelation,
            IRelation[] subRelations) throws CoreException {
        if (containerRelation == null) {
            throw new CoreException(new IpsStatus("container relation is null"));
        }
        if (containerRelation.is1ToMany()) {
            build1ToManyRelation(memberVarsBuilder, methodsBuilder, containerRelation, subRelations);
        } else {
            build1To1Relation(memberVarsBuilder, methodsBuilder, containerRelation, subRelations);
        }
    }

    private PolicyCmptImplClassBuilder getPolicyCmptTypeImplBuilder() {
        return policyCmptTypeImplCuBuilder;
    }

    private void build1To1Relation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        if (subRelations == null && !relation.isReadOnlyContainer()) {
            createRelationField(memberVarsBuilder, relation, target);
            createRelationSetterMethodImplementation(methodsBuilder, relation, target);
        }
        createRelationGetterMethodImplementation(methodsBuilder, relation, target, subRelations);
        createRelationGetNumOfMethodImplementation(methodsBuilder, relation, subRelations);
    }

    private void build1ToManyRelation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        if (subRelations == null) {
            if (!relation.isReadOnlyContainer()) {
                createRelationField(memberVarsBuilder, relation, target);
                createRelationAddMethodImplementation(methodsBuilder, relation, target);
                createRelationRemoveMethodImplementation(methodsBuilder, relation, target);
            }
        }
        createRelationContainsMethodImplementation(methodsBuilder, relation, target);
        createRelationGetNumOfMethodImplementation(methodsBuilder, relation, subRelations);
        createRelationGetAllMethodImplementation(methodsBuilder, relation, target, subRelations);
    }

    private void createRelationField(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {
        String javaClassname = relation.is1ToMany() ? List.class.getName()
                : getPolicyCmptTypeImplBuilder().getQualifiedClassName(target.getIpsSrcFile());
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();
        if (relation.is1ToMany()) {
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(ArrayList.class);
            initialValueExpression.append("()");
        } else {
            initialValueExpression.append("null");
        }
        String comment = getPolicyCmptTypeImplBuilder().getLocalizedText(relation, RELATION_FIELD_COMMENT,
            relation.getName());
        methodsBuilder.javaDoc(comment,
                JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.varDeclaration(
            Modifier.PRIVATE, javaClassname, getPolicyCmptImplFieldName(relation),
            initialValueExpression);

    }

    // duplicate in PolicyCmptTypeInterfaceRelationBuilder
    private String getPolicyCmptInterfaceAddPolicyCmptTypeMethod(IRelation r) {
        return "add" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationAddMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {
        /*
         * if(refObject == null) { throw new IllegalArgumentException("Can't add null to
         * SimpleHomeCoverageRole"); } if(simpleHomeCoveragesRole.contains(refObject)) { return; }
         * simpleHomeCoveragesRole.add(refObject);
         */
        String fieldname = getPolicyCmptImplFieldName(relation);
        IRelation reverseRelation = null;
        if (StringUtils.isNotEmpty(relation.getReverseRelation())) {
            reverseRelation = target.getRelation(relation.getReverseRelation());
            if (reverseRelation == null) {
                throw new CoreException(new IpsStatus("reverse relation not found: "
                        + relation.getReverseRelation()));
            }
        }
        String methodName = getPolicyCmptInterfaceAddPolicyCmptTypeMethod(relation);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if(refObject == null) {");
        body.append("throw new ");
        body.appendClassName(NullPointerException.class);
        body.append("(\"Can't add null to \" + this); }");
        body.append("if(");
        body.append(fieldname);
        body.append(".contains(refObject)) { return; }");
        body.append(fieldname);
        body.append(".add(refObject);");
        if (reverseRelation != null) {
            synchronizeReverseRelation(fieldname, relation, reverseRelation, body);
        }

        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation, 
            RELATION_IMPLEMENTATION_ADD_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(
            Modifier.PUBLIC,
            Datatype.VOID.getJavaClassName(),
            methodName,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                    .getQualifiedClassName(target.getIpsSrcFile()) }, body, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private void createRelationContainsMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {
        if (!relation.is1ToMany()) {
            throw new IllegalArgumentException(
                    "contains-method is only valid for 1 to many relations");
        }
        // TODO Methodenname von der IRelation erfragen
        String methodname = getContainsMethodName(relation);
        JavaCodeFragment body = new JavaCodeFragment();

        if (relation.isReadOnlyContainer()) {
            body.appendClassName(getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                    .getQualifiedClassName(target.getIpsSrcFile()));
            body.append("[] targets = ");
            body.append(getGetAllMethod(relation));
            body.append("();");
            body.append("for(int i=0;i < targets.length;i++) {");
            body.append("if(targets[i] == refObject) return true; }");
            body.append("return false;");
        } else {
            body.append("return ");
            body.append(getField(relation));
            body.append(".contains(refObject);");
        }

        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation,
            RELATION_IMPLEMENTATION_CONTAINS_JAVADOC, relation.getTargetRolePlural());

        methodsBuilder.method(
            Modifier.PUBLIC,
            Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            methodname,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                    .getQualifiedClassName(target.getIpsSrcFile()) }, body, javaDoc,
                    JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private void createRelationGetAllMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target,
            IRelation[] subRelations) throws CoreException {
        String methodName = getGetAllMethod(relation);
        String classname = getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                .getQualifiedClassName(target.getIpsSrcFile());
        String fieldname = getField(relation);
        JavaCodeFragment code;
        if (subRelations == null) {
            code = getGetAllMethodBody(relation, classname, fieldname);
        } else {
            code = getContainerRelationGetAllMethodBody(relation, classname, subRelations);
        }
        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation,
            RELATION_IMPLEMENTATION_GETALL_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(Modifier.PUBLIC, classname + "[]",
            methodName, new String[0], new String[0], code, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private void createRelationGetNumOfMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        String methodName = getNumOfMethod(relation);
        JavaCodeFragment body;
        if (subRelations == null) {
            body = new JavaCodeFragment();
            body.append("return ");
            if (relation.isReadOnlyContainer()) {
                body.append('0');
            } else {
                body.append(getField(relation));
                if (relation.is1ToMany()) {
                    body.append(".size()");
                } else {
                    body.append(" == null ? 0 : 1");
                }
            }
            body.append(";");
        } else {
            body = getContainerRelationGetNumOfMethodBody(relation, subRelations);
        }
        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation, 
            RELATION_IMPLEMENTATION_NUMOF_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(Modifier.PUBLIC,
            Datatype.PRIMITIVE_INT.getJavaClassName(), methodName, new String[0], new String[0],
            body, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private void createRelationGetterMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target,
            IRelation[] subRelations) throws CoreException {
        String methodName = getGetterMethod(relation);
        String classname = getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                .getQualifiedClassName(target.getIpsSrcFile());
        JavaCodeFragment body;
        if (subRelations == null) {
            body = new JavaCodeFragment();
            body.append("return ");
            if (relation.isReadOnlyContainer()) {
                body.append("null");
            } else {
                body.append(getField(relation));
            }
            body.append(";");
        } else {
            body = getContainerRelationGetterMethodBody(relation, subRelations);
        }
        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation, 
            RELATION_IMPLEMENTATION_GETTER_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(Modifier.PUBLIC, classname, methodName,
            new String[0], new String[0], body, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private String getPolicyCmptImplRemoveMethodName(IRelation r) {
        return "remove" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationRemoveMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {
        // TODO Rueckrichtung

        String methodName = getPolicyCmptImplRemoveMethodName(relation);
        IRelation reverseRelation = null;
        if (StringUtils.isNotEmpty(relation.getReverseRelation())) {
            reverseRelation = target.getRelation(relation.getReverseRelation());
            if (reverseRelation == null) {
                throw new CoreException(new IpsStatus("reverse relation not found: "
                        + relation.getReverseRelation()));
            }
        }
        JavaCodeFragment body = new JavaCodeFragment();
        String fieldname = getField(relation);

        body.append("{ if(refObject == null) return;");
        if (reverseRelation != null) {
            body.append("if(");
        }
        body.append(fieldname);
        body.append(".remove(refObject)");
        if (reverseRelation != null) {
            body.append(") {");
            cleanupOldReference(relation, reverseRelation, body);
            body.append(" }");
        } else {
            body.append(';');
        }
        body.append('}');

        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation, 
            RELATION_IMPLEMENTATION_REMOVE_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(
            Modifier.PUBLIC,
            Datatype.VOID.getJavaClassName(),
            methodName,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                    .getQualifiedClassName(target.getIpsSrcFile()) }, body, javaDoc,
                    JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private String getPolicyCmptImplSetMethodName(IRelation r) {
        return "set" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationSetterMethodImplementation(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {

        String methodName = getPolicyCmptImplSetMethodName(relation);
        String targetImplClass = getPolicyCmptTypeImplBuilder().getQualifiedClassName(target.getIpsSrcFile());
        String fieldname = getField(relation);
        String classname = getPolicyCmptTypeImplBuilder().getInterfaceBuilder()
                .getQualifiedClassName(target.getIpsSrcFile());
        IRelation reverseRelation = null;
        if (StringUtils.isNotEmpty(relation.getReverseRelation())) {
            reverseRelation = target.getRelation(relation.getReverseRelation());
            if (reverseRelation == null) {
                throw new CoreException(new IpsStatus("reverse relation not found: "
                        + relation.getReverseRelation()));
            }
        }
        JavaCodeFragment body = new JavaCodeFragment();

        body.append("if(refObject == ");
        body.append(fieldname);
        body.append(") return;");
        if (reverseRelation != null) {
            body.appendClassName(classname);
            body.append(" oldRefObject = ");
            body.append(fieldname);
            body.append(';');
            body.append(fieldname);
            body.append(" = null;");
            cleanupOldReference(relation, reverseRelation, body);
        }
        body.append(fieldname);
        body.append(" = (");
        body.appendClassName(targetImplClass);
        body.append(") refObject;");
        if (reverseRelation != null) {
            synchronizeReverseRelation(fieldname, relation, reverseRelation, body);
        }

        String javaDoc = getPolicyCmptTypeImplBuilder().getLocalizedText(relation,
            RELATION_IMPLEMENTATION_SETTER_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(Modifier.PUBLIC,
            Datatype.VOID.getJavaClassName(), methodName, new String[] { "refObject" },
            new String[] { classname }, body, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    protected void buildRelation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation) throws CoreException {
        if (getPolicyCmptTypeImplBuilder().isContainerRelation(relation)) {
            return;
        }
        if (relation.is1ToMany()) {
            build1ToManyRelation(memberVarsBuilder, methodsBuilder, relation, null);
        } else {
            build1To1Relation(memberVarsBuilder, methodsBuilder, relation, null);
        }
    }

    private String getContainsMethodName(IRelation relation) {
        return "contains" + relation.getTargetRoleSingular();
    }

    private void synchronizeReverseRelation(String fieldname,
            IRelation relation,
            IRelation reverseRelation,
            JavaCodeFragment body) throws CoreException {
        body.append("if(");
        if (!relation.is1ToMany()) {
            body.append("refObject != null && ");
        }
        if (reverseRelation.is1ToMany()) {
            body.append("! refObject.");
            body.append(getContainsMethodName(reverseRelation) + "(this)");
        } else {
            body.append("refObject.");
            body.append(getGetterMethod(reverseRelation));
            body.append("() != this");
        }
        body.append(") {");
        body.append("refObject.");
        if (reverseRelation.is1ToMany()) {
            body.append(getPolicyCmptInterfaceAddPolicyCmptTypeMethod(reverseRelation));
        } else {
            body.append(getPolicyCmptImplSetMethodName(reverseRelation));
        }
        body.append("(this); }");
    }

    private void cleanupOldReference(IRelation relation,
            IRelation reverseRelation,
            JavaCodeFragment body) throws CoreException {
        if (relation.is1ToMany()) {
            body.append("refObject.");
        } else {
            body.append("if(oldRefObject != null) { oldRefObject.");
        }
        if (reverseRelation.is1ToMany()) {
            body.append(getPolicyCmptImplRemoveMethodName(reverseRelation) + "(this);");
        } else {
            body.append(getPolicyCmptImplSetMethodName(reverseRelation) + "(null);");
        }
        if (!relation.is1ToMany()) {
            body.append(" }");
        }
    }

    private JavaCodeFragment getGetAllMethodBody(IRelation relation,
            String classname,
            String fieldname) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append(" return ");
        if (relation.isReadOnlyContainer()) {
            code.append("new ");
            code.appendClassName(classname);
            code.append("[0]");
        } else {
            code.append('(');
            code.appendClassName(classname);
            code.append("[]) ");
            code.append(fieldname);
            code.append(".toArray(new ");
            code.appendClassName(classname);
            code.append('[');
            code.append(fieldname);
            code.append(".size()])");
        }
        code.append(";");
        return code;
    }

    private String getPolicyCmptImplFieldName(IRelation r) {
        return r.is1ToMany() ? StringUtils.uncapitalise(r.getTargetRolePlural()) : StringUtils
                .uncapitalise(r.getTargetRoleSingular());
    }

    protected String getNumOfMethod(IRelation rel) throws CoreException {
        return getPolicyCmptTypeImplBuilder().getPolicyCmptImplGetNumOfMethodName(rel);
    }

    protected String getGetterMethod(IRelation rel) throws CoreException {
        return getPolicyCmptTypeImplBuilder().getPolicyCmptImplGetMethodName(rel);
    }

    protected String getGetAllMethod(IRelation rel) throws CoreException {
        return getPolicyCmptTypeImplBuilder().getPolicyCmptImplGetAllMethodName(rel);
    }

    protected String getField(IRelation rel) throws CoreException {
        return getPolicyCmptImplFieldName(rel);
    }

    protected boolean is1ToMany(IRelation rel) throws CoreException {
        return rel.is1ToMany();
    }

}