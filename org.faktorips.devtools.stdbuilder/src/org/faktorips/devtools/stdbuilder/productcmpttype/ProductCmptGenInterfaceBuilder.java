/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenConstantAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenDerivedAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.IntegerRange;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenInterfaceBuilder extends BaseProductCmptTypeBuilder {
    
    private ProductCmptInterfaceBuilder productCmptTypeInterfaceBuilder;
    private PolicyCmptImplClassBuilder policyCmptTypeImplBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;
    
    public ProductCmptGenInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptGenInterfaceBuilder.class));
        setMergeEnabled(true);
    }
    
    public void setProductCmptTypeInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        this.productCmptTypeInterfaceBuilder = builder;
    }
    
    public void setPolicyCmptTypeImplBuilder(PolicyCmptImplClassBuilder policyCmptTypeImplBuilder) {
        this.policyCmptTypeImplBuilder = policyCmptTypeImplBuilder;
    }
    
    public void setProductCmptGenImplClassBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplClassBuilder = builder;
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAttribute createGenerator(IProductCmptTypeAttribute a, LocalizedStringsSet stringsSet) throws CoreException {
        return new GenProdAttribute(a, this, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet stringsSet) throws CoreException {
        if (!a.getModifier().isPublished()) {
            return null;
        }
        if (a.isDerived()) {
            return new GenDerivedAttribute(a, this, stringsSet);
        }
        if (a.isChangeable()) {
            return new GenChangeableAttribute(a, this, stringsSet);
        }
        return new GenConstantAttribute(a, this, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = ipsSrcFile.getIpsObjecName() + getAbbreviationForGenerationConcept(ipsSrcFile);
        return getJavaNamingConvention().getPublishedInterfaceName(name);
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return true;
    }

    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IProductComponentGeneration.class.getName();
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype != null) {
            String pack = getPackage(supertype.getIpsSrcFile());
            javaSupertype = StringUtil.qualifiedName(pack, getUnqualifiedClassName(supertype.getIpsSrcFile()));
        }
        return new String[] { javaSupertype };
    }

    protected String getSuperclass() throws CoreException {
        return null;
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String generationConceptName = getChangesInTimeNamingConvention(getIpsObject()).getGenerationConceptNameSingular(
                getLanguageUsedInGeneratedSourceCode(getIpsObject()));
        appendLocalizedJavaDoc("INTERFACE", new String[]{generationConceptName, getProductCmptType().getName()}, getIpsObject(), builder);
    }

    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        // nothing to do
    }

    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        // nothing to do, building an interface.
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        GenChangeableAttribute generator = (GenChangeableAttribute)getGenerator(a);
        if (generator != null) {
            generator.generateCodeForProductCmptType(generatesInterface());
        }
    }

    

    

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethod(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do, computation methods are not published.
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (association.is1ToMany()) {
            generateMethodGetManyRelatedCmpts(association, methodsBuilder);
            generateMethodGetRelatedCmptAtIndex(association, methodsBuilder);
        } else {
            generateMethodGet1RelatedCmpt(association, methodsBuilder);
        }
        if (association.findMatchingPolicyCmptTypeAssociation(getIpsProject())!=null) {
            generateMethodGetCardinalityForAssociation(association, methodsBuilder);
        }
        if (association.is1ToMany()) {
            generateMethodGetNumOfRelatedCmpts(association, methodsBuilder);
        }
    }
    
    /**
     * Code sample:
     * [Javadoc]
     * <pre>
     * public CoverageType[] getCoverageTypes();
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", association.getTargetRolePlural(), association, methodsBuilder);
        generateSignatureGetManyRelatedCmpts(association, methodsBuilder);        
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public CoverageType[] getCoverageTypes()
     * </pre>
     */
    void generateSignatureGetManyRelatedCmpts(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetManyRelatedCmpts(association);
        IProductCmptType target = association.findTargetProductCmptType(getIpsProject());
        String returnType = productCmptTypeInterfaceBuilder.getQualifiedClassName(target) + "[]";
        builder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), 
                returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    String getPropertyNameToManyAssociation(IProductCmptTypeAssociation association) {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        return getLocalizedText(association, "PROPERTY_TOMANY_RELATION_NAME", role);
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public CoverageType getMainCoverageType();
     * </pre>
     */
    void generateMethodGet1RelatedCmpt(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_1_RELATED_CMPT", association.getTargetRoleSingular(), association, builder);
        generateSignatureGet1RelatedCmpt(association, builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public CoverageType getMainCoverageType()
     * </pre>
     */
    void generateSignatureGet1RelatedCmpt(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGet1RelatedCmpt(association);
        IProductCmptType target = association.findTargetProductCmptType(getIpsProject());
        String returnType = productCmptTypeInterfaceBuilder.getQualifiedClassName(target);
        builder.signature(Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
    
    String getMethodNameGet1RelatedCmpt(IProductCmptTypeAssociation association) throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameTo1Association(association), Datatype.INTEGER);
    }
    
    String getMethodNameGetManyRelatedCmpts(IProductCmptTypeAssociation association) throws CoreException {
        return getJavaNamingConvention().getMultiValueGetterMethodName(getPropertyNameToManyAssociation(association));
    }

    String getPropertyNameTo1Association(IProductCmptTypeAssociation association) {
        String role = StringUtils.capitalize(association.getTargetRoleSingular());
        return getLocalizedText(association, "PROPERTY_TO1_RELATION_NAME", role);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", containerAssociation.getTargetRolePlural(), containerAssociation, methodsBuilder);
        generateSignatureDerivedUnionAssociation(containerAssociation, methodsBuilder);
        methodsBuilder.appendln(";");

        if (containerAssociation.is1ToMany()) {
            generateMethodGetNumOfRelatedCmpts(containerAssociation, methodsBuilder);
        }
    }
    
    void generateSignatureDerivedUnionAssociation(IProductCmptTypeAssociation containerAssociation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateSignatureGetManyRelatedCmpts(containerAssociation, methodsBuilder);        
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation containerAssociation, List implementationAssociations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverageTypes();
     * </pre>
     */
    void generateMethodGetNumOfRelatedCmpts(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String role = association.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_NUM_OF_RELATED_CMPTS", role, association, builder);
        generateSignatureGetNumOfRelatedCmpts(association, builder);
        builder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverageTypes()
     * </pre>
     */
    void generateSignatureGetNumOfRelatedCmpts(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetNumOfRelatedCmpts(association);
        builder.signature(Modifier.PUBLIC, "int", methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
    
    public String getMethodNameGetNumOfRelatedCmpts(IProductCmptTypeAssociation association) {
        String propName = getLocalizedText(association, "PROPERTY_GET_NUM_OF_RELATED_CMPTS_NAME", association.getTargetRolePlural());
        return getJavaNamingConvention().getGetterMethodName(propName, Datatype.INTEGER);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public CoverageType getCoverageType(int index);
     * </pre>
     */
    void generateMethodGetRelatedCmptAtIndex(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String role = association.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_RELATED_CMPT_AT_INDEX", role, association, builder);
        generateSignatureGetRelatedCmptsAtIndex(association, builder);
        builder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public CoverageType getCoverageType(int index)
     * </pre>
     */
    void generateSignatureGetRelatedCmptsAtIndex(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetRelatedCmptAtIndex(association);
        IProductCmptType target = association.findTargetProductCmptType(getIpsProject());
        String returnType = productCmptTypeInterfaceBuilder.getQualifiedClassName(target);
        builder.signature(Modifier.PUBLIC, returnType, methodName, new String[]{"index"}, new String[]{"int"});
    }

    public String getMethodNameGetRelatedCmptAtIndex(IProductCmptTypeAssociation association) {
        return getJavaNamingConvention().getGetterMethodName(association.getTargetRoleSingular(), Datatype.INTEGER);
    }
    
    public String getMethodNameGetCardinalityForAssociation(IProductCmptTypeAssociation association) throws CoreException{
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText(association, "METHOD_GET_CARDINALITY_FOR_NAME", 
                association.findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular()), IntegerRange.class);
    }
    
    public String[][] getParamGetCardinalityForAssociation(IProductCmptTypeAssociation association) throws CoreException{
        String paramName = productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        return new String[][]{new String[]{"productCmpt"}, new String[]{paramName}};
    }
    
    public void generateSignatureGetCardinalityForAssociation(
            IProductCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        String methodName = getMethodNameGetCardinalityForAssociation(association);
        String[][] params = getParamGetCardinalityForAssociation(association);
        methodsBuilder.signature(Modifier.PUBLIC, IntegerRange.class.getName(), methodName, 
                params[0], params[1]);
    }
    
    private void generateMethodGetCardinalityForAssociation(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_CARDINALITY_FOR", association.findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular(), 
                association, methodsBuilder);
        generateSignatureGetCardinalityForAssociation(association, methodsBuilder);
        methodsBuilder.append(';');
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do, table usage methods are not published.
    }

    /**
     * Returns the getter method to access a property/attribute value.
     * 
     * @since 2.0
     */
    //TODO this method might go to a type builder when introduced 
    public String getMethodNameGetPropertyValue(String propName, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(
            IMethod method,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (method.getModifier().isPublished()) {
            methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
            generateSignatureForModelMethod((IProductCmptTypeMethod)method, false, false, methodsBuilder);
            methodsBuilder.append(';');
        }
    }

    public void generateSignatureForModelMethod(
            IProductCmptTypeMethod method,
            boolean isAbstract,
            boolean parametersFinal,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateSignatureForModelMethod(method, isAbstract, parametersFinal, methodsBuilder, null, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
        
    /**
     * Code sample:
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age) throws FormulaException
     * </pre>
     */
    public void generateSignatureForModelMethod(
            IProductCmptTypeMethod method,
            boolean isAbstract,
            boolean parametersFinal,
            JavaCodeFragmentBuilder methodsBuilder,
            String methodSuffix, String[] testParameterNames,
            String[] testParameterTypes) throws CoreException {
        boolean formulaTest = (testParameterNames.length > 0 && testParameterTypes.length > 0) || methodSuffix != null;
        
        IParameter[] parameters = method.getParameters();
        int modifier = method.getJavaModifier() | (isAbstract ? Modifier.ABSTRACT : 0);
        boolean resolveTypesToPublishedInterface = method.getModifier().isPublished();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(method.getDatatype(), resolveTypesToPublishedInterface, method.getIpsProject(), policyCmptTypeImplBuilder, productCmptGenImplClassBuilder);

        String[] parameterNames = null;
        if (formulaTest) {
            List parametersWithoutTypes = new ArrayList();
            for (int i = 0; i < parameters.length; i++) {
                Datatype datatype = parameters[i].findDatatype(getIpsProject());
                if (!(datatype instanceof IPolicyCmptType || datatype instanceof IProductCmptType)) {
                    parametersWithoutTypes.add(parameters[i]);
                }
            }
            parameters = (IParameter[]) parametersWithoutTypes.toArray(new IParameter[parametersWithoutTypes.size()]);
        } 
        parameterNames = BuilderHelper.extractParameterNames(parameters);
        String[] parameterTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, resolveTypesToPublishedInterface, method.getIpsProject(),
                        policyCmptTypeImplBuilder, productCmptGenImplClassBuilder);
        String[] parameterInSignatur = parameterNames;
        String[] parameterTypesInSignatur = parameterTypes;
        if (formulaTest){
            // add test parameters
            parameterInSignatur = extendArray(parameterNames, testParameterNames);
            parameterTypesInSignatur = extendArray(parameterTypes, testParameterTypes);
        } else {
            parameterInSignatur = parameterNames;
            parameterTypesInSignatur = parameterTypes;
        }        
        
        String methodName = method.getName();
        // extend the method signature with the given parameter names
        if (methodSuffix != null){
            methodName = method.getName() + methodSuffix;
        } 
        methodsBuilder.signature(modifier, returnClass, methodName, parameterInSignatur, parameterTypesInSignatur, parametersFinal);
        
        if (method.isFormulaSignatureDefinition()) {
            methodsBuilder.append(" throws ");
            methodsBuilder.appendClassName(FormulaExecutionException.class);
        }
    }
    
    private String[] extendArray(String[] source1, String[] source2) {
        String[] dest = new String[source1.length + source2.length];
        System.arraycopy(source1, 0, dest, 0, source1.length);
        System.arraycopy(source2, 0, dest, source1.length, source2.length);
        return dest;
    }    
}
