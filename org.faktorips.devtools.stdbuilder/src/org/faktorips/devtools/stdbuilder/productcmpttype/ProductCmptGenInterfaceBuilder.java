package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenInterfaceBuilder extends AbstractProductCmptTypeBuilder {

    private ProductCmptGenImplClassBuilder implementationBuilder;
    private ProductCmptInterfaceBuilder productCmptTypeInterfaceBuilder;
    
    public ProductCmptGenInterfaceBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptGenInterfaceBuilder.class));
        setMergeEnabled(true);
    }
    
    /**
     * Sets the implementation builder.
     * The implementation builder is needed, as it defines the property names vor vualues and 
     * default values. This can't be done in the interface builder, as only published attributes 
     * are generated in the published interface.
     */
    public void setImplementationBuilder(ProductCmptGenImplClassBuilder implementationBuilder) {
        this.implementationBuilder = implementationBuilder;
    }

    /**
     * @param productCmptTypeInterfaceBuilder The productCmptTypeInterfaceBuilder to set.
     */
    public void setProductCmptTypeInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        this.productCmptTypeInterfaceBuilder = builder;
    }

    /**
     * Overridden.
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getConceptName(ipsSrcFile));
    }
    
    public String getConceptName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return getProductCmptType(ipsSrcFile).getName() + generationAbb;
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String generationConceptName = getChangesInTimeNamingConvention(getIpsObject()).getGenerationConceptNameSingular(
                getLanguageUsedInGeneratedSourceCode(getIpsObject()));
        String javaDoc = getLocalizedText(getIpsObject(), "JAVADOC_INTERFACE", new String[]{generationConceptName, getProductCmptType().getName()});
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
    }

    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        // nothing to do
    }

    protected boolean generatesInterface() {
        return true;
    }

    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        // nothing to do, building an interface.
    }

    protected String getSuperclass() throws CoreException {
        return null;
    }

    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IProductComponentGeneration.class.getName();
        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype != null) {
            String pack = getPackage(supertype.getIpsSrcFile());
            javaSupertype = StringUtil.qualifiedName(pack, getUnqualifiedClassName(supertype.getIpsSrcFile()));
        }
        return new String[] { javaSupertype };
    }

    protected void generateCodeForChangeableAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(a, "JAVADOC_GETTER_METHOD_DEFAULTVALUE", a.getName());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetDefaultValue(a, datatypeHelper, getModifierForInterfaceMethod(), methodsBuilder);
        methodsBuilder.append(';');
    }

    protected void generateCodeForConstantAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(a, "JAVADOC_GETTER_METHOD_VALUE", new String[]{a.getName(), a.getDescription()});
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetValue(a, datatypeHelper, getModifierForInterfaceMethod(), methodsBuilder);
        methodsBuilder.append(';');
    }

    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do, computation methods are not published.
    }
    
    void generateSignatureGetDefaultValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getJavaNamingConvention().getGetterMethodName(implementationBuilder.getDefaultValuePropertyName(a), datatypeHelper.getDatatype());
        builder.methodBegin(modifier, datatypeHelper.getJavaClassName(),
                methodName, new String[0], new String[0]);
    }
    
    void generateSignatureGetValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetValue(a, datatypeHelper);
        builder.methodBegin(modifier, datatypeHelper.getJavaClassName(),
                methodName, new String[0], new String[0]);
    }
    
    public String getMethodNameGetValue(IAttribute a, DatatypeHelper datatypeHelper) throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(implementationBuilder.getValuePropertyName(a), datatypeHelper.getDatatype());
    }

    /**
     * Overridden.
     */
    protected void generateCodeForRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMethodRelationGetMany(relation, methodsBuilder);
    }
    
    private void generateMethodRelationGetMany(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(relation, "JAVADOC_GET_MANY_RELATED_OBJECTS", relation.getTargetRolePlural());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureRelationGetMany(relation, methodsBuilder);        
        methodsBuilder.appendln(";");
    }

    void generateSignatureRelationGetMany(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getJavaNamingConvention().getMultiValueGetterMethodName(implementationBuilder.getPropertyNameRelation(relation));
        IProductCmptType target = relation.findTarget();
        String returnType = productCmptTypeInterfaceBuilder.getQualifiedClassName(target)+ (relation.is1ToMany() ? "[]" : "");
        builder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), 
                returnType, methodName, new String[0], new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelation(IProductCmptTypeRelation containerRelation, List implementationRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }


}
