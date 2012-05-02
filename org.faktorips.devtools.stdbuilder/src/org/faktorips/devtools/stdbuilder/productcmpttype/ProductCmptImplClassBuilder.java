/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder that generates the Java source file (compilation unit) for the product component type
 * implementation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptImplClassBuilder extends BaseProductCmptImplementationBuilder {

    public ProductCmptImplClassBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(ProductCmptImplClassBuilder.class));
        setMergeEnabled(true);
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getImplementationClassName(ipsSrcFile.getIpsObjectName());
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    @Override
    protected String getSuperclass() throws CoreException {
        String javaSupertype = ProductComponent.class.getName();
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype != null) {
            javaSupertype = getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return javaSupertype;
    }

    @Override
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { GenType.getQualifiedName(getProductCmptType(), getBuilderSet(), true) };
    }

    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String interfaceName = GenType.getUnqualifiedClassName(getProductCmptType(), true);
        appendLocalizedJavaDoc("CLASS", interfaceName, getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     * 
     * <pre>
     * public MotorPolicy(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName, policyComponentType);
     * }
     * </pre>
     */
    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        String className = getUnqualifiedClassName();
        appendLocalizedJavaDoc("CONSTRUCTOR", className, getIpsObject(), builder);
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String versionParam = getChangesInTimeNamingConvention(getIpsObject()).getVersionConceptNameSingular(locale);
        versionParam = StringUtils.uncapitalize(versionParam) + "Id";
        String[] argNames = new String[] { "repository", "id", "kindId", versionParam };
        String[] argTypes = new String[] { IRuntimeRepository.class.getName(), String.class.getName(),
                String.class.getName(), String.class.getName() };
        builder.methodBegin(Modifier.PUBLIC, null, className, argNames, argTypes);
        builder.append("super(repository, id, kindId, " + versionParam + ");");
        builder.methodEnd();
    }

    @Override
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        generateGetGenerationMethod(methodsBuilder);
        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        if (getBuilderSet().isGenerateToXmlSupport()) {
            generateMethodWritePropertiesToXml(methodsBuilder);
        }
        IPolicyCmptType policyCmptType = getPcType();
        if (policyCmptType != null && !policyCmptType.isAbstract()) {
            generateFactoryMethodsForPolicyCmptType(policyCmptType, methodsBuilder, new HashSet<IPolicyCmptType>());
        }
        if (mustGenerateMethodCreatePolicyComponentBase(getProductCmptType())) {
            generateMethodCreatePolicyCmptBase(methodsBuilder);
        }
    }

    /**
     * Generating the body of the create&lt;PolicyComponent&gt; method.
     * <p>
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorPolicy createMotorPolicy() {
     *     return new MotorPolicy(this);
     * }
     * </pre>
     */
    @Override
    protected void generateMethodBodyCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.openBracket();
        methodsBuilder.append("return new ");
        methodsBuilder.appendClassName(getBuilderSet().getGenerator(getPcType()).getQualifiedName(false));
        methodsBuilder.appendln("(this);");
        methodsBuilder.closeBracket();
    }

    private void generateGetGenerationMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_GENERATION", getIpsObject(), methodsBuilder);
        appendOverrideAnnotation(methodsBuilder, true);
        GenProductCmptType genProd = getBuilderSet().getGenerator(getProductCmptType());
        genProd.generateSignatureGetGeneration(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(genProd.getQualifiedClassNameForProductCmptTypeGen(true));
        methodsBuilder.append(")getRepository().getProductComponentGeneration(");
        methodsBuilder.append(MethodNames.GET_PRODUCT_COMPONENT_ID);
        methodsBuilder.append("(), ");
        methodsBuilder.append(genProd.getVarNameEffectiveDate());
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }

    @Override
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        // nothing to do
    }

    @Override
    protected boolean isChangingOverTimeContainer() {
        return false;
    }

    @Override
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        // nothing to do
    }

    @Override
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    @Override
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation derivedUnionAssociation,
            List<IAssociation> implementationAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    @Override
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }

    @Override
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        // nothing to do
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
