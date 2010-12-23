/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype.association;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.runtime.CardinalityRange;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.IntegerRange;

/**
 * 
 * @author Daniel Hohenberger
 */
public abstract class GenProdAssociation extends GenTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenProdAssociation.class);

    protected IProductCmptTypeAssociation association;

    protected IProductCmptType target;

    public GenProdAssociation(GenProductCmptType genProductCmptType, IProductCmptTypeAssociation association)
            throws CoreException {

        super(genProductCmptType, association, LOCALIZED_STRINGS);
        this.association = association;
        target = association.findTargetProductCmptType(association.getIpsProject());
    }

    protected void generateFieldCardinalityForAssociation(JavaCodeFragmentBuilder fieldsBuilder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_CARDINALITIES_FOR",
                association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular(),
                fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment();
        expression.append(" new ");
        expression.appendClassName(HashMap.class);
        if (isUseTypesafeCollections()) {
            expression.append('<');
            expression.appendClassName(String.class);
            expression.append(", ");
            expression.appendClassName(IntegerRange.class);
            expression.append('>');
        }
        expression.append("(0)");
        fieldsBuilder.varDeclaration(Modifier.PRIVATE, Map.class.getName()
                + (isUseTypesafeCollections() ? "<" + String.class.getName() + ", " + IntegerRange.class.getName()
                        + ">" : ""), getFieldNameCardinalityForAssociation(), expression);
    }

    public String getFieldNameCardinalityForAssociation() throws CoreException {
        return getLocalizedText("FIELD_CARDINALITIES_FOR_NAME",
                association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular());
    }

    /**
     * Generates the getNumOfXXX() method for a container association.
     * <p>
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return getNumOfCoverageTypesInternal();
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfRelatedProductCmpts(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder builder) throws CoreException {

        if (!association.isDerivedUnion()) {
            throw new IllegalArgumentException("Association must be a container association.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);

        IProductCmptType supertype = (IProductCmptType)((GenProductCmptType)getGenType()).getProductCmptType()
                .findSupertype(((GenProductCmptType)getGenType()).getProductCmptType().getIpsProject());
        appendOverrideAnnotation(builder, getIpsProject(), supertype == null || supertype.isAbstract());
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
     * 
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
    protected void generateMethodGetNumOfRelatedProductCmptsInternal(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder builder) throws CoreException {
        if (!association.isDerivedUnion()) {
            throw new IllegalArgumentException("Association must be a container association.");
        }
        builder.javaDoc("", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        String methodName = getMethodNameGetNumOfRelatedCmptsInternal();
        builder.signature(java.lang.reflect.Modifier.PRIVATE, "int", methodName, new String[] {}, new String[] {});
        builder.openBracket();
        builder.appendln("int num = 0;");
        IProductCmptType supertype = (IProductCmptType)((GenProductCmptType)getGenType()).getProductCmptType()
                .findSupertype(((GenProductCmptType)getGenType()).getProductCmptType().getIpsProject());
        if (supertype != null && !supertype.isAbstract()) {
            String methodName2 = getMethodNameGetNumOfRelatedCmpts();
            builder.appendln("num += super." + methodName2 + "();");
        }
        for (IAssociation iAssociation : implAssociations) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)iAssociation;
            builder.append("num += ");
            ((GenProductCmptType)getGenType()).getGenerator(association)
                    .generateCodeGetNumOfRelatedProductCmptsInternal(builder);
        }
        builder.appendln("return num;");
        builder.closeBracket();
    }

    protected abstract void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder)
            throws CoreException;

    /*
     * Returns the name of the internal method returning the number of referenced objects, e.g.
     * getNumOfCoveragesInternal()
     */
    private String getMethodNameGetNumOfRelatedCmptsInternal() {
        return getLocalizedText("METHOD_GET_NUM_OF_INTERNAL_NAME",
                StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public int getNumOfCoverageTypes()
     * </pre>
     */
    void generateSignatureGetNumOfRelatedCmpts(JavaCodeFragmentBuilder builder) {
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
     * 
     * <pre>
     * [Javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageType==null ? 0 : 1;
     * }
     * </pre>
     * 
     * <p>
     * Code sample for 1-many associations:
     * 
     * <pre>
     * [Javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageTypes.length;
     * }
     * </pre>
     * 
     * <p>
     * Java 5 Code sample for 1-many associations:
     * 
     * <pre>
     * [Javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageTypes.size();
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
        ((GenProductCmptType)getGenType()).getGenerator(association).generateCodeGetNumOfRelatedProductCmpts(builder);
        builder.closeBracket();
    }

    protected abstract void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder)
            throws CoreException;

    public void generateSignatureGetCardinalityForAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetCardinalityForAssociation();
        String[][] params = getParamGetCardinalityForAssociation();
        methodsBuilder.signature(Modifier.PUBLIC, CardinalityRange.class.getName(), methodName, params[0], params[1]);
    }

    public String getMethodNameGetCardinalityForAssociation() throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText(
                        "METHOD_GET_CARDINALITY_FOR_NAME",
                        StringUtils.capitalize(association.findMatchingPolicyCmptTypeAssociation(
                                association.getIpsProject()).getTargetRoleSingular())), IntegerRange.class);
    }

    public String[][] getParamGetCardinalityForAssociation() throws CoreException {
        String paramName = getQualifiedInterfaceClassNameForTarget();
        return new String[][] { new String[] { "productCmpt" }, new String[] { paramName } };
    }

    /**
     * Code sample where a 1-1 and a 1-many association implement a container association.
     * 
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[getNumOfCoverageTypes()];
     *     int index = 0;
     *     if (collisionCoverageType!=null) {
     *         result[index++] = getCollisionCoverageType();
     *     }
     *     ITplCoverageType[] tplCoverageTypesObjects = getTplcCoverageTypes();
     *     for (int i=0; i&lt;tplCoverageTypesObjects.length; i++) {
     *         result[index++] = tplCoverageTypes[i];
     *     }
     *     return result;
     * }
     * </pre>
     * 
     * Java 5 Code sample where a 1-1 and a 1-many association implement a container association.
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverageType&gt; getCoverageTypes() {
     *     List&lt;ICoverageType&gt; result = new ArrayList&lt;ICoverageType&gt;(getNumOfCoverageTypes());
     *     if (collisionCoverageType!=null) {
     *         result.add(getCollisionCoverageType());
     *     }
     *     List&lt;ITplCoverageType&gt; tplCoverageTypesObjects = getTplcCoverageTypes();
     *     for (ITplCoverageType tplCoverageTypesObject : tplCoverageTypesObjects) {
     *         result.add(tplCoverageTypesObject);
     *     }
     *     return result;
     * }
     * </pre>
     */
    public void generateMethodGetRelatedCmptsInContainer(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        IProductCmptType supertype = (IProductCmptType)((GenProductCmptType)getGenType()).getProductCmptType()
                .findSupertype(((GenProductCmptType)getGenType()).getProductCmptType().getIpsProject());
        appendOverrideAnnotation(methodsBuilder, getIpsProject(), (supertype == null || supertype.isAbstract()));

        generateSignatureDerivedUnionAssociation(methodsBuilder);

        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendClassName(List.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("> result = new ");
            methodsBuilder.appendClassName(ArrayList.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append(">(");
            methodsBuilder.append(getMethodNameGetNumOfRelatedCmptsInternal());
            methodsBuilder.appendln("());");
        } else {
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[] result = new ");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[");
            methodsBuilder.append(getMethodNameGetNumOfRelatedCmptsInternal());
            methodsBuilder.appendln("()];");
        }

        if (supertype != null && !supertype.isAbstract()) {
            if (isUseTypesafeCollections()) {
                // List<ICoverage> superResult = super.getCoverages();
                // result.addAll(superResult);
                methodsBuilder.appendClassName(List.class.getName());
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(targetClass);
                methodsBuilder.append("> superResult = super.");
                methodsBuilder.appendln(getMethodNameGetManyRelatedCmpts() + "();");
                methodsBuilder.appendln("result.addAll(superResult);");
            } else {
                // ICoverage[] superResult = super.getCoverages();
                // System.arraycopy(superResult, 0, result, 0, superResult.length);
                // int counter = superResult.length;
                methodsBuilder.appendClassName(targetClass);
                methodsBuilder.append("[] superResult = super.");
                methodsBuilder.appendln(getMethodNameGetManyRelatedCmpts() + "();");
                methodsBuilder.appendln("System.arraycopy(superResult, 0, result, 0, superResult.length);");
                methodsBuilder.appendln("int index = superResult.length;");
            }
        } else {
            if (!isUseTypesafeCollections()) {
                methodsBuilder.append("int index = 0;");
            }
        }
        for (IAssociation iAssociation : implAssociations) {
            IProductCmptTypeAssociation implAssociation = (IProductCmptTypeAssociation)iAssociation;
            ((GenProductCmptType)getGenType()).getGenerator(implAssociation).generateCodeGetRelatedCmptsInContainer(
                    methodsBuilder);
        }
        methodsBuilder.appendln("return result;");
        methodsBuilder.closeBracket();
    }

    protected abstract void generateCodeGetRelatedCmptsInContainer(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException;

    String getPropertyNameToManyAssociation() {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        return getLocalizedText("PROPERTY_TOMANY_ASSOCIATION_NAME", role);
    }

    void generateSignatureDerivedUnionAssociation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateSignatureGetManyRelatedCmpts(methodsBuilder);
    }

    protected String getFieldNameTo1Association() {
        return getJavaNamingConvention().getMemberVarName(getPropertyNameTo1Association());
    }

    String getPropertyNameTo1Association() {
        String role = StringUtils.capitalize(association.getTargetRoleSingular());
        return getLocalizedText("PROPERTY_TO1_ASSOCIATION_NAME", role);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public CoverageType[] getCoverageTypes()
     * </pre>
     * 
     * Java 5 Code sample:
     * 
     * <pre>
     * public List&lt;CoverageType&gt; getCoverageTypes()
     * </pre>
     */
    void generateSignatureGetManyRelatedCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetManyRelatedCmpts();
        IProductCmptType target = association.findTargetProductCmptType(association.getIpsProject());
        String returnType;
        if (isUseTypesafeCollections()) {
            returnType = List.class.getName() + "<"
                    + getGenType().getBuilderSet().getGenerator(target).getQualifiedName(true) + ">";
        } else {
            returnType = getGenType().getBuilderSet().getGenerator(target).getQualifiedName(true) + "[]";
        }
        builder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType, methodName,
                EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverageTypeGen[] getCoverageTypeGens(Calendar effectiveDate)
     * </pre>
     * 
     * Java 5 Code sample:
     * 
     * <pre>
     * public List&lt;ICoverageTypeGen&gt; getCoverageTypeGens(Calendar effectiveDate)
     * </pre>
     */
    void generateSignatureGetManyRelatedCmptGens(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetManyRelatedCmpts();
        IProductCmptType target = association.findTargetProductCmptType(association.getIpsProject());
        String returnType;
        if (isUseTypesafeCollections()) {
            returnType = List.class.getName()
                    + "<"
                    + getGenType().getBuilderSet().getGenerator(target)
                            .getQualifiedClassNameForProductCmptTypeGen(true) + ">";
        } else {
            returnType = getGenType().getBuilderSet().getGenerator(target)
                    .getQualifiedClassNameForProductCmptTypeGen(true)
                    + "[]";
        }
        builder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType, methodName,
                new String[] { "effectiveDate" }, new String[] { Calendar.class.getName() });
    }

    String getMethodNameGetManyRelatedCmpts() {
        return getJavaNamingConvention().getMultiValueGetterMethodName(getPropertyNameToManyAssociation());
    }

    public void generateCodeForDerivedUnionAssociationImplementation(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodGetRelatedCmptsInContainer(implAssociations, methodsBuilder);
    }

    public abstract void generateCodeForMethodDoInitReferencesFromXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) throws CoreException;

    protected void generateMethodGetCardinalityForAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_CARDINALITY_FOR",
                association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject()).getTargetRoleSingular(),
                methodsBuilder);
        generateSignatureGetCardinalityForAssociation(methodsBuilder);
        methodsBuilder.append(';');
    }

    public void generateCodeForDerivedUnionAssociationDefinition(JavaCodeFragmentBuilder methodsBuilder)
            throws Exception {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureDerivedUnionAssociation(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    protected String getQualifiedInterfaceClassNameForTarget() throws CoreException {
        return getGenType().getBuilderSet()
                .getGenerator((IProductCmptType)association.findTarget(association.getIpsProject()))
                .getQualifiedName(true);
    }

    protected String getQualifiedInterfaceClassNameForTargetGen() throws CoreException {
        return getGenType().getBuilderSet()
                .getGenerator((IProductCmptType)association.findTarget(association.getIpsProject()))
                .getQualifiedClassNameForProductCmptTypeGen(true);
    }

    protected String getMethodNameGetProductCmptGenerationForTarget() throws CoreException {
        return getGenType().getBuilderSet()
                .getGenerator((IProductCmptType)association.findTarget(association.getIpsProject()))
                .getMethodNameGetProductCmptGeneration();
    }

    /**
     * Java 5 Code sample:
     * 
     * <pre>
     * [javadoc]
     * public ILink&lt;ICoverageType&gt; getLinkForCoverageType(ICoverageType productComponent);
     * </pre>
     */
    protected void generateMethodInterfaceGetRelatedCmptLink(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_RELATED_CMPT_LINK", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureGetRelatedCmptLink(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * public ILink&lt;ICoverageType&gt; getLinkForCoverageType(ICoverageType productComponent)
     * </pre>
     */
    protected void generateSignatureGetRelatedCmptLink(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGet1RelatedCmptLink();
        String returnType = Java5ClassNames.ILink_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget() + ">";
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType,
                methodName, new String[] { "productComponent" },
                new String[] { getQualifiedInterfaceClassNameForTarget() });
    }

    String getMethodNameGet1RelatedCmptLink() {
        return getJavaNamingConvention().getMultiValueGetterMethodName(
                "LinkFor" + StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * if (&quot;CoverageType&quot;.equals(linkName)) {
     *     return getLinkForCoverageType((ICoverageType)target);
     * }
     * </pre>
     */
    public void generateCodeForGetLink(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.append("if (\"");
        methodsBuilder.append(association.getTargetRoleSingular());
        methodsBuilder.appendln("\".equals(linkName)){");
        methodsBuilder.append("return ");
        methodsBuilder.append(getMethodNameGet1RelatedCmptLink());
        methodsBuilder.append("((");
        methodsBuilder.appendClassName(getQualifiedInterfaceClassNameForTarget());
        methodsBuilder.appendln(")target);");
        methodsBuilder.appendln("}");
    }

    public abstract void generateCodeForGetLinks(JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected final void addMethodGetRelatedCmptLinkToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        try {
            IMethod method = generatedJavaType.getMethod(getMethodNameGet1RelatedCmptLink(), new String[] { "Q"
                    + QNameUtil.getUnqualifiedName(getQualifiedInterfaceClassNameForTarget()) + ";" });
            javaElements.add(method);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void addMethodGetCardinalityForAssociationToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        try {
            IMethod expectedMethod = generatedJavaType
                    .getMethod(
                            getMethodNameGetCardinalityForAssociation(),
                            new String[] { "Q"
                                    + QNameUtil.getUnqualifiedName(getQualifiedInterfaceClassNameForTarget()) + ";" });
            javaElements.add(expectedMethod);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final boolean isUseTypesafeCollections(IIpsProject ipsProject) {
        return Boolean.parseBoolean(ipsProject.getProperties().getBuilderSetConfig()
                .getPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS));
    }
}
