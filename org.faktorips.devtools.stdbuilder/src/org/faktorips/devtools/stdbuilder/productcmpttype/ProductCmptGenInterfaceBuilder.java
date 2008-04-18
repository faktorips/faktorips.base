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
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationTo1;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationToMany;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

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
        GenProdAssociation generator = getGenerator(association);
        generator.generate(generatesInterface());
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenProdAssociation generator = getGenerator(containerAssociation);
        generator.generateCodeForDerivedUnionAssociationDefinition(methodsBuilder);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation containerAssociation, List implementationAssociations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
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

    protected GenProdAssociation createGenerator(IProductCmptTypeAssociation association, LocalizedStringsSet stringsSet)
            throws CoreException {
        if (association.is1ToMany()) {
            return new GenProdAssociationToMany(association, this, stringsSet);
        }
        return new GenProdAssociationTo1(association, this, stringsSet);
    }  

    public ProductCmptInterfaceBuilder getProductCmptInterfaceBuilder() {
        return productCmptTypeInterfaceBuilder;
    }
}
