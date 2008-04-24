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

package org.faktorips.devtools.stdbuilder.productcmpttype.association;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptTypePart;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.IntegerRange;

/**
 * 
 * @author Daniel Hohenberger
 */
public abstract class GenProdAssociation extends GenProductCmptTypePart {

    protected IProductCmptTypeAssociation association;
    protected IProductCmptType target;

    /**
     * @param part
     * @param builder
     * @param stringsSet
     * @throws CoreException
     */
    public GenProdAssociation(GenProductCmptType genProductCmptType, IProductCmptTypeAssociation association,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(genProductCmptType, association, stringsSet);
        this.association = association;
        this.target = association.findTargetProductCmptType(association.getIpsProject());
    }
    
    protected void generateFieldCardinalityForAssociation(JavaCodeFragmentBuilder fieldsBuilder) throws CoreException{
        appendLocalizedJavaDoc("FIELD_CARDINALITIES_FOR", association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular(), fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment();
        expression.append(" new ");
        expression.appendClassName(HashMap.class);
        expression.append("(0);");
        fieldsBuilder.varDeclaration(Modifier.PRIVATE, Map.class, getFieldNameCardinalityForAssociation(), expression);
    }

    public String getFieldNameCardinalityForAssociation() throws CoreException{
        return getLocalizedText("FIELD_CARDINALITIES_FOR_NAME", association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular());
    }

    /**
     * Generates the getNumOfXXX() method for a container association. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return getNumOfCoverageTypesInternal();
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfRelatedProductCmpts(List implAssociations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!association.isDerivedUnion()) {
            throw new IllegalArgumentException("Association must be a container association.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetNumOfRelatedCmpts(builder);
        builder.openBracket();
        String internalMethodName = getMethodNameGetNumOfRelatedCmptsInternal();
        builder.appendln("return " + internalMethodName + "();");
        builder.closeBracket();
    }
    
    /**
     * Generates the getNumOfXXXInternal() method for a container association. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypesInternal() {
     *     int numOf = 0;
     *     numOf += getNumOfCollisionCoverages();
     *     numOf += getNumOfTplCoverages();
     *     return numOf;
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfRelatedProductCmptsInternal(List implAssociations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!association.isDerivedUnion()) {
            throw new IllegalArgumentException("Association must be a container association.");
        }
        builder.javaDoc("", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        String methodName = getMethodNameGetNumOfRelatedCmptsInternal();
        builder.signature(java.lang.reflect.Modifier.PRIVATE, "int", methodName, new String[]{}, new String[]{});
        builder.openBracket();
        builder.appendln("int num = 0;");
        IProductCmptType supertype = (IProductCmptType)getGenProductCmptType().getProductCmptType().findSupertype(getGenProductCmptType().getProductCmptType().getIpsProject());
        if (supertype!=null && !supertype.isAbstract()) {
            String methodName2 = getMethodNameGetNumOfRelatedCmpts();
            builder.appendln("num += super." + methodName2 + "();");
        }
        for (Iterator it = implAssociations.iterator(); it.hasNext();) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)it.next();
            builder.append("num += ");
            getGenProductCmptType().getGenerator(association).generateCodeGetNumOfRelatedProductCmptsInternal(builder);
        }
        builder.appendln("return num;");
        builder.closeBracket();
    }
    
    protected abstract void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder) throws CoreException;
    
    /*
     * Returns the name of the internal method returning the number of referenced objects,
     * e.g. getNumOfCoveragesInternal()
     */
    private String getMethodNameGetNumOfRelatedCmptsInternal() {
        return getLocalizedText("METHOD_GET_NUM_OF_INTERNAL_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }
    
    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverageTypes()
     * </pre>
     */
    void generateSignatureGetNumOfRelatedCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetNumOfRelatedCmpts();
        builder.signature(Modifier.PUBLIC, "int", methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
    
    public String getMethodNameGetNumOfRelatedCmpts() {
        String propName = getLocalizedText("PROPERTY_GET_NUM_OF_RELATED_CMPTS_NAME", association.getTargetRolePlural());
        return getJavaNamingConvention().getGetterMethodName(propName, Datatype.INTEGER);
    }
    
    /**
     * Generates the getNumOfXXX() method for none container associations. 
     * <p>
     * Code sample for 1-1 associations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageType==null ? 0 : 1;
     * }
     * </pre>
     * <p>
     * Code sample for 1-many associations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageTypes.length;
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        if (association.isDerivedUnion()) {
            throw new IllegalArgumentException("Association needn't be a container association.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetNumOfRelatedCmpts(builder);
        builder.openBracket();
        builder.append("return ");
        getGenProductCmptType().getGenerator(association).generateCodeGetNumOfRelatedProductCmpts(builder);
        builder.closeBracket();
    }
    
    protected abstract void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException;
    
    protected void generateMethodGetCardinalityFor1ToManyAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("@inheritDoc", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetCardinalityForAssociation(methodsBuilder);
        String[][] params = getParamGetCardinalityForAssociation();
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendOpenBracket();
        frag.append("if(");
        frag.append(params[0][0]);
        frag.append(" != null)");
        frag.appendOpenBracket();
        frag.append("return ");
        frag.append('(');
        frag.appendClassName(IntegerRange.class);
        frag.append(')');
        frag.append(getFieldNameCardinalityForAssociation());
        frag.append(".get(");
        frag.append(params[0][0]);
        frag.append(".getId());");
        frag.appendCloseBracket();
        frag.append("return null;");
        frag.appendCloseBracket();
        methodsBuilder.append(frag);
    }
    
    public void generateSignatureGetCardinalityForAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        String methodName = getMethodNameGetCardinalityForAssociation();
        String[][] params = getParamGetCardinalityForAssociation();
        methodsBuilder.signature(Modifier.PUBLIC, IntegerRange.class.getName(), methodName, 
                params[0], params[1]);
    }
    
    public String getMethodNameGetCardinalityForAssociation() throws CoreException{
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_CARDINALITY_FOR_NAME", 
                association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular()), IntegerRange.class);
    }
    
    public String[][] getParamGetCardinalityForAssociation() throws CoreException{
        String paramName = getQualifiedInterfaceClassNameForTarget();
        return new String[][]{new String[]{"productCmpt"}, new String[]{paramName}};
    }
    
    /**
     * Code sample where a 1-1 and a 1-many association implement a container association.
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[getNumOfCoverageTypes()];
     *     int index = 0;
     *     if (collisionCoverageType!=null) {
     *         result[index++] = getCollisionCoverageType();
     *     }
     *     ITplCoverageType[] tplCoverageTypesObjects = getTplcCoverageTypes();
     *     for (int i=0; i<tplCoverageTypesObjects.length; i++) {
     *         result[index++] = tplCoverageTypes[i];
     *     }
     *     return result;
     * }
     * </pre>
     */
    public void generateMethodGetRelatedCmptsInContainer(
            List implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureDerivedUnionAssociation(methodsBuilder);

        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(getMethodNameGetNumOfRelatedCmptsInternal());
        methodsBuilder.appendln("()];");

        IProductCmptType supertype = (IProductCmptType)getGenProductCmptType().getProductCmptType().findSupertype(getGenProductCmptType().getProductCmptType().getIpsProject());
        if (supertype!=null && !supertype.isAbstract()) {
            // ICoverage[] superResult = super.getCoverages();
            // System.arraycopy(superResult, 0, result, 0, superResult.length);
            // int counter = superResult.length;
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[] superResult = super.");       
            methodsBuilder.appendln(getMethodNameGetManyRelatedCmpts(association) + "();");
            methodsBuilder.appendln("System.arraycopy(superResult, 0, result, 0, superResult.length);");
            methodsBuilder.appendln("int index = superResult.length;");
        } else {
            methodsBuilder.append("int index = 0;");
        }
        for (Iterator it = implAssociations.iterator(); it.hasNext();) {
            IProductCmptTypeAssociation implAssociation = (IProductCmptTypeAssociation)it.next();
            getGenProductCmptType().getGenerator(implAssociation).generateCodeGetRelatedCmptsInContainer(methodsBuilder);
        }
        methodsBuilder.appendln("return result;");
        methodsBuilder.closeBracket();
    }

    protected abstract void generateCodeGetRelatedCmptsInContainer(JavaCodeFragmentBuilder methodsBuilder)  throws CoreException;

    String getPropertyNameToManyAssociation(IProductCmptTypeAssociation association) {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        return getLocalizedText("PROPERTY_TOMANY_RELATION_NAME", role);
    }
    
    void generateSignatureDerivedUnionAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateSignatureGetManyRelatedCmpts(association, methodsBuilder);        
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public CoverageType[] getCoverageTypes()
     * </pre>
     */
    void generateSignatureGetManyRelatedCmpts(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetManyRelatedCmpts(association);
        IProductCmptType target = association.findTargetProductCmptType(association.getIpsProject());
        String returnType = getGenProductCmptType().getBuilderSet().getGenerator(target).getQualifiedName(true) + "[]";
        builder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType, methodName,
                EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    String getMethodNameGetManyRelatedCmpts(IProductCmptTypeAssociation association) throws CoreException {
        return getJavaNamingConvention().getMultiValueGetterMethodName(getPropertyNameToManyAssociation(association));
    }

    public void generateCodeForDerivedUnionAssociationImplementation(List implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodGetRelatedCmptsInContainer(implAssociations, methodsBuilder);
    }

    public abstract void generateCodeForMethodDoInitReferencesFromXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation, JavaCodeFragmentBuilder builder) throws CoreException;

    
    protected void generateMethodGetCardinalityForAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_CARDINALITY_FOR", association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular(), 
                methodsBuilder);
        generateSignatureGetCardinalityForAssociation(methodsBuilder);
        methodsBuilder.append(';');
    }

    /**
     * {@inheritDoc}
     */
    public void generateCodeForDerivedUnionAssociationDefinition(JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureDerivedUnionAssociation(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * @return
     * @throws CoreException
     */
    protected String getQualifiedInterfaceClassNameForTarget() throws CoreException {
        return getGenProductCmptType().getBuilderSet().getGenerator(
                (IProductCmptType)association.findTarget(association.getIpsProject())).getQualifiedName(true);
    }
}
