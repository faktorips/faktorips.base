package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IRelation;
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
public class ProductCmptGenInterfaceCuBuilder extends AbstractProductCmptTypeBuilder {

    private ProductCmptGenImplCuBuilder implementationBuilder;
    
    public ProductCmptGenInterfaceCuBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptGenInterfaceCuBuilder.class));
        setMergeEnabled(true);
    }
    
    /**
     * Sets the implementation builder.
     * The implementation builder is needed, as it defines the property names vor vualues and 
     * default values. This can't be done in the interface builder, as only published attributes 
     * are generated in the published interface.
     */
    public void setImplementationBuilder(ProductCmptGenImplCuBuilder implementationBuilder) {
        this.implementationBuilder = implementationBuilder;
    }

    /**
     * Overridden.
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "PkGen");
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String generationConceptName = IpsPreferences.getChangesInTimeNamingConvention().getGenerationConceptNameSingular(
                getLanguageUsedInGeneratedSourceCode());
        String javaDoc = getLocalizedText("JAVADOC_INTERFACE", new String[]{generationConceptName, getProductCmptType().getName()});
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
        String javaDoc = getLocalizedText("JAVADOC_GETTER_METHOD_DEFAULTVALUE", a.getName());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetDefaultValue(a, datatypeHelper, getModifierForInterfaceMethod(), methodsBuilder);
        methodsBuilder.append(';');
    }

    protected void generateCodeForConstantAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText("JAVADOC_GETTER_METHOD_VALUE", new String[]{a.getName(), a.getDescription()});
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetValue(a, datatypeHelper, getModifierForInterfaceMethod(), methodsBuilder);
        methodsBuilder.append(';');
    }

    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) {
        // nothing to do in the interface
    }
    
    void generateSignatureGetDefaultValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getJavaNamingConvention().getGetterMethodName(implementationBuilder.getDefaultValuePropertyName(a), datatypeHelper.getDatatype());
        builder.methodBegin(modifier, datatypeHelper.getJavaClassName(),
                methodName, new String[0], new String[0]);
    }
    
    void generateSignatureGetValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getJavaNamingConvention().getGetterMethodName(implementationBuilder.getValuePropertyName(a), datatypeHelper.getDatatype());
        builder.methodBegin(modifier, datatypeHelper.getJavaClassName(),
                methodName, new String[0], new String[0]);
    }

    /**
     * Overridden.
     */
    protected void generateCodeForRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        int modifier = Modifier.ABSTRACT + Modifier.PUBLIC;
        String javaDoc = getLocalizedText("JAVADOC_GET_MANY_RELATED_OBJECTS", relation.getTargetRolePlural());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetManyRelated(relation, modifier, methodsBuilder);        
        methodsBuilder.methodEnd();
    }

    void generateSignatureGetManyRelated(IProductCmptTypeRelation relation, int modifier, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getJavaNamingConvention().getMultiValueGetterMethodName(implementationBuilder.getMemberVarNameManyRelation(relation));
        IProductCmptType target = relation.findTarget();
        builder.methodBegin(modifier, getQualifiedClassName(target)+"[]", 
                methodName, new String[0], new String[0]);
    }

    private String getProductCmptGetAllMethodName(IProductCmptTypeRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRolePlural());
    }

    private String getProductCmptNumOfMethodName(IProductCmptTypeRelation relation) {
        return "getAnzahl" + StringUtils.capitalise(relation.getTargetRolePlural());
    }

    private String getProductCmptInterfaceGetMethodName(IProductCmptTypeRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRoleSingular());
    }


}
