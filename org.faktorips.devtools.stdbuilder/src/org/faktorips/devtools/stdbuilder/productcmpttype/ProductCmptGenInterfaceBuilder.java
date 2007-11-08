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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.EnumValueSet;
import org.faktorips.valueset.IntegerRange;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenInterfaceBuilder extends AbstractProductCmptTypeBuilder {

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

    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
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
        generateMethodGetDefaultValue(a, datatypeHelper, methodsBuilder);
        
        //TODO the generateCodeForAttribute method of the abstract builder needs to discriminate against
        //the published modifier

       datatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), datatypeHelper);
       if(datatypeHelper.getDatatype() instanceof EnumDatatype){
           generateMethodGetAllowedValuesFor(a, datatypeHelper.getDatatype(), methodsBuilder);
       }
       else if(ValueSetType.ENUM.equals(a.getValueSet().getValueSetType())){
           generateMethodGetAllowedValuesFor(a, datatypeHelper.getDatatype(), methodsBuilder);
       }
       else if(ValueSetType.RANGE.equals(a.getValueSet().getValueSetType())){
           generateMethodGetRangeFor(a, datatypeHelper, methodsBuilder);
       }
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge();
     * </pre>
     */
    void generateMethodGetDefaultValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_DEFAULTVALUE", a.getName(), a, builder);
        generateSignatureGetDefaultValue(a, datatypeHelper, builder);
        builder.append(';');
    }

    /**
     * Code sample:
     * <pre>
     * public Integer getDefaultMinAge()
     * </pre>
     */
    void generateSignatureGetDefaultValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetDefaultValue(a, datatypeHelper);
        builder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(),
                methodName, new String[0], new String[0]);
    }
    
    /**
     * Returns the name of the method that returns the default value for the indicated
     * attribute.
     */
    public String getMethodNameGetDefaultValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper) {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameDefaultValue(a), datatypeHelper.getDatatype());        
    }
    
    String getPropertyNameDefaultValue(IPolicyCmptTypeAttribute a) {
        return getLocalizedText(a, "PROPERTY_DEFAULTVALUE_NAME", StringUtils.capitalise(a.getName()));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForProductCmptTypeAttribute(
            IProductCmptTypeAttribute a, 
            DatatypeHelper datatypeHelper, 
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder, 
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {
        
        generateMethodGetValue(a, datatypeHelper, methodsBuilder);
    }

    /**
     * Code sample:
     * [Javadoc]
     * <pre>
     * public Integer getTaxRate();
     * </pre>
     */
    void generateMethodGetValue(IProductCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        String description = StringUtils.isEmpty(a.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR + a.getDescription();
        String[] replacements = new String[]{a.getName(), description};
        appendLocalizedJavaDoc("METHOD_GET_VALUE", replacements, a, builder);
        generateSignatureGetValue(a, datatypeHelper, builder);
        builder.append(';');
    }

    /**
     * Code sample:
     * <pre>
     * public Integer getTaxRate()
     * </pre>
     */
    void generateSignatureGetValue(IProductCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetValue(a, datatypeHelper);
        builder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(),
                methodName, new String[0], new String[0]);
    }
    
    public String getMethodNameGetValue(IProductCmptTypeAttribute a, DatatypeHelper datatypeHelper) throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameValue(a), datatypeHelper.getDatatype());
    }
    
    String getPropertyNameValue(IProductCmptTypeAttribute a) {
        return getLocalizedText(a, "PROPERTY_VALUE_NAME", StringUtils.capitalise(a.getName()));
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
    protected void generateCodeForNoneContainerRelation(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (relation.is1ToMany()) {
            generateMethodGetManyRelatedCmpts(relation, methodsBuilder);
            generateMethodGetRelatedCmptAtIndex(relation, methodsBuilder);
        } else {
            generateMethodGet1RelatedCmpt(relation, methodsBuilder);
        }
        if (relation.findMatchingPolicyCmptTypeAssociation(getIpsProject())!=null) {
            generateMethodGetCardinalityForRelation(relation, methodsBuilder);
        }
        generateMethodGetNumOfRelatedCmpts(relation, methodsBuilder);
    }
    
    /**
     * Code sample:
     * [Javadoc]
     * <pre>
     * public CoverageType[] getCoverageTypes();
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", relation.getTargetRolePlural(), relation, methodsBuilder);
        generateSignatureGetManyRelatedCmpts(relation, methodsBuilder);        
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
                returnType, methodName, new String[0], new String[0]);
    }

    String getPropertyNameToManyRelation(IProductCmptTypeAssociation relation) {
        String role = StringUtils.capitalise(relation.getTargetRolePlural());
        return getLocalizedText(relation, "PROPERTY_TOMANY_RELATION_NAME", role);
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public CoverageType getMainCoverageType();
     * </pre>
     */
    void generateMethodGet1RelatedCmpt(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_1_RELATED_CMPT", relation.getTargetRoleSingular(), relation, builder);
        generateSignatureGet1RelatedCmpt(relation, builder);
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
        builder.signature(Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }
    
    String getMethodNameGet1RelatedCmpt(IProductCmptTypeAssociation relation) throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameTo1Relation(relation), Datatype.INTEGER);
    }
    
    String getMethodNameGetManyRelatedCmpts(IProductCmptTypeAssociation relation) throws CoreException {
        return getJavaNamingConvention().getMultiValueGetterMethodName(getPropertyNameToManyRelation(relation));
    }

    String getPropertyNameTo1Relation(IProductCmptTypeAssociation relation) {
        String role = StringUtils.capitalise(relation.getTargetRoleSingular());
        return getLocalizedText(relation, "PROPERTY_TO1_RELATION_NAME", role);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationDefinition(IProductCmptTypeAssociation containerRelation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", containerRelation.getTargetRolePlural(), containerRelation, methodsBuilder);
        generateSignatureContainerRelation(containerRelation, methodsBuilder);
        methodsBuilder.appendln(";");
        
        generateMethodGetNumOfRelatedCmpts(containerRelation, methodsBuilder);
    }
    
    void generateSignatureContainerRelation(IProductCmptTypeAssociation containerRelation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateSignatureGetManyRelatedCmpts(containerRelation, methodsBuilder);        
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationImplementation(IProductCmptTypeAssociation containerRelation, List implementationRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverageTypes();
     * </pre>
     */
    void generateMethodGetNumOfRelatedCmpts(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        String role = relation.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_NUM_OF_RELATED_CMPTS", role, relation, builder);
        generateSignatureGetNumOfRelatedCmpts(relation, builder);
        builder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverageTypes()
     * </pre>
     */
    void generateSignatureGetNumOfRelatedCmpts(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetNumOfRelatedCmpts(relation);
        builder.signature(Modifier.PUBLIC, "int", methodName, new String[0], new String[0]);
    }
    
    public String getMethodNameGetNumOfRelatedCmpts(IProductCmptTypeAssociation relation) {
        String propName = getLocalizedText(relation, "PROPERTY_GET_NUM_OF_RELATED_CMPTS_NAME", relation.getTargetRolePlural());
        return getJavaNamingConvention().getGetterMethodName(propName, Datatype.INTEGER);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public CoverageType getCoverageType(int index);
     * </pre>
     */
    void generateMethodGetRelatedCmptAtIndex(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        String role = relation.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_RELATED_CMPT_AT_INDEX", role, relation, builder);
        generateSignatureGetRelatedCmptsAtIndex(relation, builder);
        builder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public CoverageType getCoverageType(int index)
     * </pre>
     */
    void generateSignatureGetRelatedCmptsAtIndex(IProductCmptTypeAssociation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetRelatedCmptAtIndex(relation);
        IProductCmptType target = relation.findTargetProductCmptType(getIpsProject());
        String returnType = productCmptTypeInterfaceBuilder.getQualifiedClassName(target);
        builder.signature(Modifier.PUBLIC, returnType, methodName, new String[]{"index"}, new String[]{"int"});
    }

    public String getMethodNameGetRelatedCmptAtIndex(IProductCmptTypeAssociation relation) {
        return getJavaNamingConvention().getGetterMethodName(relation.getTargetRoleSingular(), Datatype.INTEGER);
    }
    
    public String getMethodNameGetCardinalityForRelation(IProductCmptTypeAssociation association) throws CoreException{
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText(association, "METHOD_GET_CARDINALITY_FOR_NAME", 
                association.findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular()), IntegerRange.class);
    }
    
    public String[][] getParamGetCardinalityForRelation(IProductCmptTypeAssociation association) throws CoreException{
        String paramName = productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        return new String[][]{new String[]{"productCmpt"}, new String[]{paramName}};
    }
    
    public void generateSignatureGetCardinalityForRelation(
            IProductCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        String methodName = getMethodNameGetCardinalityForRelation(association);
        String[][] params = getParamGetCardinalityForRelation(association);
        methodsBuilder.signature(Modifier.PUBLIC, IntegerRange.class.getName(), methodName, 
                params[0], params[1]);
    }
    
    private void generateMethodGetCardinalityForRelation(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_CARDINALITY_FOR", association.findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular(), 
                association, methodsBuilder);
        generateSignatureGetCardinalityForRelation(association, methodsBuilder);
        methodsBuilder.append(';');
    }
    
    public String getMethodNameGetRangeFor(IPolicyCmptTypeAttribute a, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(getLocalizedText(a, 
                "METHOD_GET_RANGE_FOR_NAME", StringUtils.capitalise(a.getName())), datatype);
    }
    
    public void generateMethodGetRangeFor(IPolicyCmptTypeAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_RANGE_FOR", a.getName(), a, methodsBuilder);
        generateSignatureGetRangeFor(a, helper, methodsBuilder);
        methodsBuilder.append(';');
    }
    
    public void generateSignatureGetRangeFor(IPolicyCmptTypeAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        String methodName = getMethodNameGetRangeFor(a, helper.getDatatype());
        String rangeClassName = helper.getRangeJavaClassName();
        methodsBuilder.signature(Modifier.PUBLIC, rangeClassName, methodName, new String[]{"businessFunction"}, new String[]{String.class.getName()});
    }

    public String getMethodNameGetAllowedValuesFor(IPolicyCmptTypeAttribute a, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(getLocalizedText(
                a, "METHOD_GET_ALLOWED_VALUES_FOR_NAME", StringUtils.capitalise(a.getName())), datatype);
    }
    
    public void generateMethodGetAllowedValuesFor(IPolicyCmptTypeAttribute a, Datatype datatype, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_ALLOWED_VALUES_FOR", a.getName(), a, methodsBuilder);
        generateSignatureGetAllowedValuesFor(a, datatype, methodsBuilder);
        methodsBuilder.append(';');
    }
    
    public void generateSignatureGetAllowedValuesFor(IPolicyCmptTypeAttribute a, Datatype datatype, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        String methodName = getMethodNameGetAllowedValuesFor(a, datatype);
        methodsBuilder.signature(Modifier.PUBLIC, EnumValueSet.class.getName(), methodName, 
                new String[]{"businessFunction"}, new String[]{String.class.getName()});
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
    //TODO this method might go to a type builder when introducted 
    public String getMethodNameGetPropertyValue(String propName, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForModelMethod(
            IProductCmptTypeMethod method, 
            JavaCodeFragmentBuilder fieldsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (method.getModifier().isPublished()) {
            methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
            generateSignatureForModelMethod(method, false, false, methodsBuilder);
            methodsBuilder.append(';');
        }
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
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IParameter[] parameters = method.getParameters();
        int modifier = method.getJavaModifier() | (isAbstract ? Modifier.ABSTRACT : 0);
        boolean resolveTypesToPublishedInterface = method.getModifier().isPublished();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(method.getDatatype(), resolveTypesToPublishedInterface, method.getIpsProject(), policyCmptTypeImplBuilder, productCmptGenImplClassBuilder);
        methodsBuilder.signature(modifier, returnClass, method.getName(), BuilderHelper
                .extractParameterNames(parameters), StdBuilderHelper
                .transformParameterTypesToJavaClassNames(parameters, resolveTypesToPublishedInterface, method.getIpsProject(),
                        policyCmptTypeImplBuilder, productCmptGenImplClassBuilder), parametersFinal);
        
        if (method.isFormulaSignatureDefinition()) {
            methodsBuilder.append(" throws ");
            methodsBuilder.appendClassName(FormulaExecutionException.class);
        }
    }
    
    
}
