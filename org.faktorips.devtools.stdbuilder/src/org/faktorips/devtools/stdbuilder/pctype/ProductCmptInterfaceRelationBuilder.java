package org.faktorips.devtools.stdbuilder.pctype;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * helper class for ProductCmptInterfaceCuBuilder, responsible for code related to relation handling
 */
public class ProductCmptInterfaceRelationBuilder extends RelationInterfaceBuilder {

    private final static String RELATION_INTERFACE_GETALL_JAVADOC = "RELATION_INTERFACE_GETALL_JAVADOC";
    private final static String RELATION_INTERFACE_GETTER_JAVADOC = "RELATION_INTERFACE_GETTER_JAVADOC";
    private final static String RELATION_INTERFACE_NUMOF_JAVADOC = "RELATION_INTERFACE_NUMOF_JAVADOC";


    public ProductCmptInterfaceRelationBuilder(ProductCmptInterfaceCuBuilder cuBuilder) {
        super(cuBuilder.getPolicyCmptType(), cuBuilder);
    }

    private ProductCmptInterfaceCuBuilder getProductCmptInterfaceBuilder() {
        return (ProductCmptInterfaceCuBuilder)getCuBuilder();
    }

    private void build1To1Relation(IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetterMethod1To1(relation, target);
        createRelationGetNumOfMethod(relation);
    }

    private void build1ToManyRelation(IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetAllMethod(relation, target);
        createRelationGetterMethod1ToMany(relation, target);
        createRelationGetNumOfMethod(relation);
    }

    protected void buildRelation(IRelation relation) throws CoreException {
        build1ToManyRelation(relation);
        // folgende Zeilen auskommentiert bis genauer Umgang mit relationen geklärt ist. Jan
        // if (relation.is1ToMany()) {
        // build1ToManyRelation(relation);
        // } else {
        // build1To1Relation(relation);
        // }
    }

    /**
     * @param relation
     * @param target
     */
    private void createRelationGetAllMethod(IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getProductCmptGetAllMethodName(relation);
        String returnType = getProductCmptInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile())
                + "[]";
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_GETALL_JAVADOC,
            relation.getTargetRoleSingular());
        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType, methodName, new String[0],
            new String[0], javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().append(';');
    }

    private void createRelationGetNumOfMethod(IRelation relation) throws CoreException {
        String methodName = getProductCmptNumOfMethodName(relation);
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_NUMOF_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, Datatype.PRIMITIVE_INT.getJavaClassName(),
            methodName, new String[0], new String[0], javaDoc,
            BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().append(';');
    }

    private void createRelationGetterMethod1To1(IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getProductCmptInterfaceGetMethodName(relation);
        String targetQualifiedName = getProductCmptInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());
        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().javaDoc(javaDoc);
        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, targetQualifiedName, methodName, new String[0],
            new String[0], javaDoc, BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().append(';');
    }

    private void createRelationGetterMethod1ToMany(IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getProductCmptInterfaceGetMethodName(relation);
        String targetQualifiedName = getProductCmptInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());

        String javaDoc = getCuBuilder().getLocalizedText(RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());

        getCuBuilder().getJavaCodeFragementBuilder().javaDoc(javaDoc);
        getCuBuilder().getJavaCodeFragementBuilder().methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, targetQualifiedName, methodName,
            new String[] { "productCmptName" }, new String[] { String.class.getName() }, javaDoc,
            BaseJavaSourceFileBuilder.ANNOTATION_GENERATED);
        getCuBuilder().getJavaCodeFragementBuilder().append(';');
    }

    private String getProductCmptGetAllMethodName(IRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRolePlural()) + "Pk";
    }

    // duplicated in ProductCmptImplRelationBuilder
    private String getProductCmptNumOfMethodName(IRelation relation) {
        return "getAnzahl" + StringUtils.capitalise(relation.getTargetRolePlural()) + "Pk";
    }

    private String getProductCmptInterfaceGetMethodName(IRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRoleSingular()) + "Pk";
    }
}
