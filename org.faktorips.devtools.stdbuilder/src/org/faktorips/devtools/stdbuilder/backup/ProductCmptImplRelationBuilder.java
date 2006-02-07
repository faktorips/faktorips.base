package org.faktorips.devtools.stdbuilder.backup;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.stdbuilder.policycmpttype.RelationImplBuilder;

/**
 * helper class for ProductCmptImplCuBuilder, responsible for code related to relation handling
 */
public class ProductCmptImplRelationBuilder extends RelationImplBuilder {

    private ProductCmptImplCuBuilder productCmptImplCuBuilder;

    private final static String RELATION_FIELD_COMMENT = "RELATION_FIELD_COMMENT";
    private final static String RELATION_IMPLEMENTATION_GETALL_JAVADOC = "RELATION_IMPLEMENTATION_GETALL_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_GETTER_JAVADOC = "RELATION_IMPLEMENTATION_GETTER_JAVADOC";
    private final static String RELATION_IMPLEMENTATION_NUMOF_JAVADOC = "RELATION_IMPLEMENTATION_NUMOF_JAVADOC";

    public ProductCmptImplRelationBuilder(ProductCmptImplCuBuilder cuBuilder) {
        super(cuBuilder.getPcType());
        productCmptImplCuBuilder = cuBuilder;
    }

    private ProductCmptImplCuBuilder getProductCmptImplBuilder() {
        return productCmptImplCuBuilder;
    }

    private void build1To1Relation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        if (subRelations == null && !relation.isReadOnlyContainer()) {
            createRelationFieldFor1to1(memberVarsBuilder, relation, target);
        }
        createRelationGetterMethod1To1(methodsBuilder, relation, target, subRelations);
        createRelationGetNumOfMethod1To1(methodsBuilder, relation, subRelations);
    }

    private void build1ToManyRelation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        if (subRelations == null && !relation.isReadOnlyContainer()) {
            createRelationFieldFor1toMany(memberVarsBuilder, relation, target);
        }
        createRelationGetAllMethod(methodsBuilder, relation, target, subRelations);
        createRelationGetterMethod1ToMany(methodsBuilder, relation, target);
        createRelationGetNumOfMethod1ToMany(methodsBuilder, relation, subRelations);
    }

    protected void buildRelation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation) throws CoreException {
        if (getProductCmptImplBuilder().isContainerRelation(relation)) {
            return;
        }
        build1ToManyRelation(memberVarsBuilder, methodsBuilder, relation, null);
        // folgende Zeilen auskommentiert bis genauer Umgang mit relationen geklaert ist. Jan
        // if (relation.is1ToMany()) {
        // build1ToManyRelation(relation);
        // } else {
        // build1To1Relation(relation);
        // }
    }

    // duplicate in ProductCmptImplCuBuilder
    private String getProductCmptRelation1ToManyFieldName(IRelation relation) {
        return StringUtils.uncapitalise(relation.getTargetRolePlural()) + "Pks";
    }

    // duplicate in ProductCmptImplCuBuilder
    private String getProductCmptRelation1To1FieldName(IRelation relation) {
        return StringUtils.uncapitalise(relation.getTargetRoleSingular()) + "Pk";
    }

    private void createRelationFieldFor1to1(JavaCodeFragmentBuilder memberVarsBuilder,
            IRelation relation,
            IPolicyCmptType target) {
        String comment = getProductCmptImplBuilder().getLocalizedText(RELATION_FIELD_COMMENT,
            relation.getName());
        memberVarsBuilder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, String.class,
            getProductCmptRelation1To1FieldName(relation), new JavaCodeFragment("null"));
    }

    private void createRelationFieldFor1toMany(JavaCodeFragmentBuilder memberVarsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {
        String comment = getProductCmptImplBuilder().getLocalizedText(RELATION_FIELD_COMMENT,
            relation.getName());

        String fieldName;
        if (relation.is1ToMany()) {
            fieldName = getProductCmptRelation1ToManyFieldName(relation);
        } else {
            fieldName = getProductCmptRelation1To1FieldName(relation);
        }
        memberVarsBuilder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, String.class.getName() + "[]",
            fieldName, new JavaCodeFragment("new String[0]"));
    }

    // dupliate in ProductCmptInterfaceRelationBuilder
    private String getProductCmptGetAllMethodName(IRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRolePlural()) + "Pk";
    }

    private void createRelationGetAllMethod(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target,
            IRelation[] subRelations) throws CoreException {
        String methodName = getProductCmptGetAllMethodName(relation);
        String targetQualifiedName = getProductCmptImplBuilder().getProductCmptInterfaceBuilder()
                .getQualifiedClassName(target.getIpsSrcFile());

        JavaCodeFragment body;
        if (subRelations == null) {
            body = getRelationGetAllMethodBody(relation, targetQualifiedName);
        } else {
            body = getContainerRelationGetAllMethodBody(relation, targetQualifiedName, subRelations);
        }

        String javaDoc = getProductCmptImplBuilder().getLocalizedText(
            RELATION_IMPLEMENTATION_GETTER_JAVADOC, relation.getTargetRoleSingular());

        methodsBuilder.method(Modifier.PUBLIC, targetQualifiedName + "[]", methodName,
            new String[0], new String[0], body, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private JavaCodeFragment getRelationGetAllMethodBody(IRelation relation,
            String targetQualifiedName) throws CoreException {
        JavaCodeFragment body;
        String fieldName = relation.is1ToMany() ? getProductCmptRelation1ToManyFieldName(relation)
                : getProductCmptRelation1To1FieldName(relation);
        body = new JavaCodeFragment();
        if (relation.isReadOnlyContainer()) {
            body.append("return new ");
            body.appendClassName(targetQualifiedName);
            body.append("[0];");
        } else {
            // CoveragePk[] result = new CoveragePk[coveragesPks.length];
            body.appendClassName(targetQualifiedName);
            body.append("[] result = new ");
            body.appendClassName(targetQualifiedName);
            body.append("[");
            body.append(fieldName);
            body.appendln(".length];");

            body.appendln("for (int i=0; i<result.length; i++) {");
            body.appendln("result[i] = (");
            body.appendClassName(targetQualifiedName);
            body.append(")getRegistry().getProductComponent(");
            body.append(fieldName);
            body.appendln("[i]);");
            body.appendln("}");
            body.appendln("return result;");
        }
        return body;
    }

    // duplicat in ProductCmptInterfaceRelationBuilder
    private String getProductCmptNumOfMethodName(IRelation relation) {
        return "getAnzahl" + StringUtils.capitalise(relation.getTargetRolePlural()) + "Pk";
    }

    private void createRelationGetNumOfMethod(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations,
            String returnExpression) throws CoreException {
        String methodName = getProductCmptNumOfMethodName(relation);
        JavaCodeFragment body;
        if (subRelations == null) {
            body = new JavaCodeFragment();
            body.append("return ");
            if (relation.isReadOnlyContainer()) {
                body.append('0');
            } else {
                body.append(relation.is1ToMany() ? getProductCmptRelation1ToManyFieldName(relation)
                        : getProductCmptRelation1To1FieldName(relation));
                body.append(returnExpression);
            }
            body.append(';');
        } else {
            body = getContainerRelationGetNumOfMethodBody(relation, subRelations);
        }
        String javaDoc = getProductCmptImplBuilder().getLocalizedText(
            RELATION_IMPLEMENTATION_NUMOF_JAVADOC, relation.getTargetRoleSingular());
        methodsBuilder.method(Modifier.PUBLIC, Datatype.PRIMITIVE_INT.getJavaClassName(),
            methodName, new String[0], new String[0], body, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private void createRelationGetNumOfMethod1To1(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        createRelationGetNumOfMethod(methodsBuilder, relation, subRelations, " == null ? 0 : 1");
    }

    private void createRelationGetNumOfMethod1ToMany(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IRelation[] subRelations) throws CoreException {
        createRelationGetNumOfMethod(methodsBuilder, relation, subRelations, ".length");
    }

    private void createRelationGetterMethod1To1(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target,
            IRelation[] subRelations) throws CoreException {
        String methodName = getProductCmptImplGetMethodName(relation);
        String targetQualifiedName = getProductCmptImplBuilder().getProductCmptInterfaceBuilder()
                .getQualifiedClassName(target.getIpsSrcFile());
        JavaCodeFragment body;
        if (subRelations == null) {
            body = new JavaCodeFragment();
            body.append("return (");
            if (relation.isReadOnlyContainer()) {
                body.append("null");
            } else {
                body.appendClassName(targetQualifiedName);
                body.append(") getRegistry().getProductComponent(");
                body.append(relation.is1ToMany() ? getProductCmptRelation1ToManyFieldName(relation)
                        : getProductCmptRelation1To1FieldName(relation));
                body.append(")");
            }
            body.append(';');
        } else {
            body = getContainerRelationGetterMethodBody(relation, subRelations);
        }

        String javaDoc = getProductCmptImplBuilder().getLocalizedText(
            RELATION_IMPLEMENTATION_GETTER_JAVADOC, relation.getTargetRoleSingular());
        methodsBuilder.method(Modifier.PUBLIC, targetQualifiedName, methodName, new String[0],
            new String[0], body, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    private String getProductCmptImplGetMethodName(IRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRoleSingular()) + "Pk";
    }

    private void createRelationGetterMethod1ToMany(JavaCodeFragmentBuilder methodsBuilder,
            IRelation relation,
            IPolicyCmptType target) throws CoreException {
        String methodName = getProductCmptImplGetMethodName(relation);
        String targetQualifiedName = getProductCmptImplBuilder().getProductCmptInterfaceBuilder()
                .getQualifiedClassName(target.getIpsSrcFile());

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendClassName(targetQualifiedName);
        body.append("[] productCmpts = ");
        body.append(getProductCmptGetAllMethodName(relation));
        body.appendln("();");
        body.append("for (int i=0; i<productCmpts.length; i++)");
        body.appendOpenBracket();
        body.append("if (productCmpts[i].getQualifiedName().equals(productCmptName))");
        body.appendOpenBracket();
        body.append("return (");
        body.appendClassName(targetQualifiedName);
        body.appendln(")getRegistry().getProductComponent(productCmptName);");
        body.appendCloseBracket();
        body.appendCloseBracket();
        body.appendln("return null;");

        String javaDoc = getProductCmptImplBuilder().getLocalizedText(
            RELATION_IMPLEMENTATION_GETTER_JAVADOC, relation.getTargetRoleSingular());
        methodsBuilder.method(Modifier.PUBLIC, targetQualifiedName, methodName,
            new String[] { "productCmptName" }, new String[] { String.class.getName() }, body,
            javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }

    protected void buildContainerRelation(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            IRelation containerRelation,
            IRelation[] subRelations) throws CoreException {
        if (containerRelation == null) {
            throw new CoreException(new IpsStatus("container relation is null"));
        }
        // derzeit nur 1ToManyRelation
        build1ToManyRelation(memberVarsBuilder, methodsBuilder, containerRelation, subRelations);
    }

    protected String getNumOfMethod(IRelation rel) throws CoreException {
        return getProductCmptNumOfMethodName(rel);
    }

    protected String getGetterMethod(IRelation rel) throws CoreException {
        return getProductCmptImplGetMethodName(rel);
    }

    protected String getGetAllMethod(IRelation rel) throws CoreException {
        return getProductCmptGetAllMethodName(rel);
    }

    protected String getField(IRelation rel) throws CoreException {
        return rel.is1ToMany() ? getProductCmptRelation1ToManyFieldName(rel)
                : getProductCmptRelation1To1FieldName(rel);
    }

    protected boolean is1ToMany(IRelation rel) throws CoreException {
        // Beziehungen zwischen Produktkomponenten derzeit immer 1ToMany
        return true;
    }

}
