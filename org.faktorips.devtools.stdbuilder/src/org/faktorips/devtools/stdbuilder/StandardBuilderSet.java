/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.formulatest.FormulaTestBuilder;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassJaxbAnnGenFactory;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.ValidationRuleMessagesPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.persistence.PolicyCmptImplClassJpaAnnGenFactory;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.ArgumentCheck;

/**
 * An <code>IpsArtefactBuilderSet</code> implementation that assembles the standard Faktor-IPS
 * <tt>IIpsArtefactBuilder</tt>s.
 * 
 * @author Peter Erzberger
 */
public class StandardBuilderSet extends DefaultBuilderSet {

    public final static String ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset";

    /**
     * Configuration property that enables/disables the generation of a copy method.
     * 
     * @see ICopySupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_COPY_SUPPORT = "generateCopySupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of delta computation.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT = "generateDeltaSupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of the visitor support.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT = "generateVisitorSupport"; //$NON-NLS-1$

    /**
     * Configuration property that is supposed to be used to read a configuration value from the
     * IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public final static String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the use of typesafe collections, if supported by
     * the target java version.
     */
    public final static String CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS = "useTypesafeCollections"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the use of enums, if supported by the target
     * java version.
     */
    public final static String CONFIG_PROPERTY_USE_ENUMS = "useJavaEnumTypes"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public final static String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport"; //$NON-NLS-1$

    /**
     * Configuration property contains the persistence provider implementation.
     */
    public final static String CONFIG_PROPERTY_PERSISTENCE_PROVIDER = "persistenceProvider"; //$NON-NLS-1$

    /**
     * Configuration property contains kind of formula compiling.
     */
    public final static String CONFIG_PROPERTY_FORMULA_COMPILING = "formulaCompiling"; //$NON-NLS-1$

    private TableImplBuilder tableImplBuilder;

    private TableRowBuilder tableRowBuilder;

    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;

    private PolicyCmptImplClassBuilder policyCmptImplClassBuilder;

    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;

    private ProductCmptImplClassBuilder productCmptImplClassBuilder;

    private EnumTypeBuilder enumTypeBuilder;

    private Map<String, CachedPersistenceProvider> allSupportedPersistenceProvider;

    private final String version;

    private final Map<IType, GenType> ipsObjectTypeGenerators;
    private final AnnotationGeneratorFactory[] annotationGeneratorFactories;

    private Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorsMap;

    public StandardBuilderSet() {
        ipsObjectTypeGenerators = new HashMap<IType, GenType>(1000);

        annotationGeneratorFactories = new AnnotationGeneratorFactory[] {
                new PolicyCmptImplClassJpaAnnGenFactory(this), // JPA support
                new PolicyCmptImplClassJaxbAnnGenFactory(this) }; // Jaxb support

        initSupportedPersistenceProviderMap();

        version = "3.0.0"; //$NON-NLS-1$
        // Following code sections sets the version to the stdbuilder-plugin/bundle version.
        // Most of the time we hardwire the version of the generated code here, but from time to
        // time
        // we want to sync it with the plugin version, so the code remains here.
        //
        // Version versionObj =
        // Version.parseVersion((String)StdBuilderPlugin.getDefault().getBundle(
        // ).getHeaders().get(org
        // .osgi.framework.Constants.BUNDLE_VERSION));
        // StringBuffer buf = new StringBuffer();
        // buf.append(versionObj.getMajor());
        // buf.append('.');
        // buf.append(versionObj.getMinor());
        // buf.append('.');
        // buf.append(versionObj.getMicro());
        // version = buf.toString();
    }

    @Override
    public void afterBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    @Override
    public void beforeBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    @Override
    public boolean isSupportTableAccess() {
        return true;
    }

    private void clearGenerators() {
        ipsObjectTypeGenerators.clear();
    }

    public GenType getGenerator(IType type) throws CoreException {
        if (type == null) {
            return null;
        }

        if (type instanceof IPolicyCmptType) {
            return getGenerator((IPolicyCmptType)type);
        }
        if (type instanceof IProductCmptType) {
            return getGenerator((IProductCmptType)type);
        }

        throw new CoreException(new IpsStatus("Unkown subclass " + type.getClass())); //$NON-NLS-1$
    }

    public GenPolicyCmptType getGenerator(IPolicyCmptType policyCmptType) throws CoreException {
        if (policyCmptType == null) {
            return null;
        }

        GenPolicyCmptType generator = (GenPolicyCmptType)ipsObjectTypeGenerators.get(policyCmptType);
        if (generator == null) {
            generator = new GenPolicyCmptType(policyCmptType, this);
            ipsObjectTypeGenerators.put(policyCmptType, generator);
        }

        return generator;
    }

    public GenProductCmptType getGenerator(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType == null) {
            return null;
        }

        GenProductCmptType generator = (GenProductCmptType)ipsObjectTypeGenerators.get(productCmptType);
        if (generator == null) {
            generator = new GenProductCmptType(productCmptType, this);
            ipsObjectTypeGenerators.put(productCmptType, generator);
        }

        return generator;
    }

    @Override
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();

        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        result.addAllIdentifierUsed(argResults);
        code.appendClassName(tableImplBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_THIS_REPOSITORY + "(), \"" + tableContents.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmtpyResult("); //$NON-NLS-1$

        // TODO pk: findRow is not correct in general. JO: Why?
        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", "); //$NON-NLS-1$
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get"); //$NON-NLS-1$
        code.append(StringUtils.capitalize(fct.findAccessedColumn().getName()));
        code.append("()"); //$NON-NLS-1$

        return result;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {
        return new AbstractParameterIdentifierResolver(formula, exprCompiler) {

            @Override
            protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
                    EnumTypeDatatypeAdapter datatype,
                    ExprCompiler exprCompiler,
                    String value) throws CoreException {
                ExtendedExprCompiler compiler = (ExtendedExprCompiler)exprCompiler;
                fragment.append(enumTypeBuilder.getNewInstanceCodeFragement(datatype, value,
                        compiler.getRuntimeRepositoryExpression()));
            }

            @Override
            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                try {

                    if (datatype instanceof IPolicyCmptType) {
                        return getGenerator((IPolicyCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                    if (datatype instanceof IProductCmptType) {
                        return getGenerator((IProductCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }

                } catch (CoreException e) {
                    return null;
                }

                return null;
            }
        };
    }

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {
        return new AbstractParameterIdentifierResolver(formula, exprCompiler) {

            @Override
            protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
                    EnumTypeDatatypeAdapter datatype,
                    ExprCompiler exprCompiler,
                    String value) throws CoreException {
                ExtendedExprCompiler compiler = (ExtendedExprCompiler)exprCompiler;
                fragment.append(enumTypeBuilder.getNewInstanceCodeFragement(datatype, value,
                        compiler.getRuntimeRepositoryExpression()));
            }

            @Override
            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                try {

                    if (datatype instanceof IPolicyCmptType) {
                        return getGenerator((IPolicyCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                    if (datatype instanceof IProductCmptType) {
                        return getGenerator((IProductCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }

                } catch (CoreException e) {
                    return null;
                }

                return null;
            }

            @Override
            protected CompilationResult compile(IParameter param, String attributeName) {
                CompilationResult compile = super.compile(param, attributeName);
                try {
                    Datatype datatype = param.findDatatype(getIpsProject());
                    if (datatype instanceof IType) {
                        /*
                         * instead of using the types getter method to get the value for an
                         * identifier, the given datatype plus the attribute will be used as new
                         * parameter identifier, this parameter identifier will also be used as
                         * parameter inside the formula method which uses this code fragment
                         */
                        String code = param.getName() + "_" + attributeName; //$NON-NLS-1$
                        return new CompilationResultImpl(code, compile.getDatatype());
                    }
                } catch (CoreException ignored) {
                    // the exception was already handled in the compile method of the super class
                }
                return compile;
            }

        };
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    @Override
    protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
        // create policy component type builders
        policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(this);
        policyCmptInterfaceBuilder = new PolicyCmptInterfaceBuilder(this);

        // create product component type builders
        productCmptInterfaceBuilder = new ProductCmptInterfaceBuilder(this);
        productCmptImplClassBuilder = new ProductCmptImplClassBuilder(this);
        productCmptGenInterfaceBuilder = new ProductCmptGenInterfaceBuilder(this);
        productCmptGenImplClassBuilder = new ProductCmptGenImplClassBuilder(this);

        // table structure builders
        tableImplBuilder = new TableImplBuilder(this);
        tableRowBuilder = new TableRowBuilder(this);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        IIpsArtefactBuilder tableContentCopyBuilder = new TableContentBuilder(this);

        // test case type builders
        TestCaseTypeClassBuilder testCaseTypeClassBuilder = new TestCaseTypeClassBuilder(this);

        // test case builder
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder(this);

        // formula test builder
        FormulaTestBuilder formulaTestBuilder = new FormulaTestBuilder(this);

        // toc file builder
        TocFileBuilder tocFileBuilder = new TocFileBuilder(this);

        BusinessFunctionBuilder businessFunctionBuilder = new BusinessFunctionBuilder(this);
        // New enum type builder
        enumTypeBuilder = new EnumTypeBuilder(this);
        EnumXmlAdapterBuilder enumXmlAdapterBuilder = new EnumXmlAdapterBuilder(this, enumTypeBuilder);
        IIpsArtefactBuilder enumContentBuilder = new XmlContentFileCopyBuilder(IpsObjectType.ENUM_CONTENT, this);

        //
        // wire up the builders
        //
        productCmptGenImplClassBuilder.setEnumTypeBuilder(enumTypeBuilder);

        // product component builders
        ProductCmptBuilder productCmptGenerationImplBuilder = new ProductCmptBuilder(this);
        IIpsArtefactBuilder productCmptContentCopyBuilder = new ProductCmptXMLBuilder(IpsObjectType.PRODUCT_CMPT, this,
                productCmptGenerationImplBuilder);

        productCmptGenerationImplBuilder.setProductCmptImplBuilder(productCmptImplClassBuilder);
        productCmptGenerationImplBuilder.setProductCmptGenImplBuilder(productCmptGenImplClassBuilder);
        productCmptGenInterfaceBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        productCmptGenImplClassBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        productCmptGenImplClassBuilder.setProductCmptGenInterfaceBuilder(productCmptGenInterfaceBuilder);

        // test case builder
        testCaseBuilder.setJavaSourceFileBuilder(policyCmptImplClassBuilder);

        // formula test builder
        formulaTestBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        formulaTestBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        formulaTestBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);

        // toc file builders
        tocFileBuilder.setPolicyCmptImplClassBuilder(policyCmptImplClassBuilder);
        tocFileBuilder.setProductCmptTypeImplClassBuilder(productCmptImplClassBuilder);
        tocFileBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        tocFileBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);
        tocFileBuilder.setTableImplBuilder(tableImplBuilder);
        tocFileBuilder.setTestCaseTypeClassBuilder(testCaseTypeClassBuilder);
        tocFileBuilder.setTestCaseBuilder(testCaseBuilder);
        tocFileBuilder.setFormulaTestBuilder(formulaTestBuilder);
        tocFileBuilder.setEnumTypeBuilder(enumTypeBuilder);
        tocFileBuilder.setEnumXmlAdapterBuilder(enumXmlAdapterBuilder);

        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
                this);

        createAnnotationGeneratorMap();

        if (ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())) {
            ModelTypeXmlBuilder policyModelTypeBuilder = new ModelTypeXmlBuilder(IpsObjectType.POLICY_CMPT_TYPE, this);
            ModelTypeXmlBuilder productModelTypeBuilder = new ModelTypeXmlBuilder(IpsObjectType.PRODUCT_CMPT_TYPE, this);
            tocFileBuilder.setPolicyModelTypeXmlBuilder(policyModelTypeBuilder);
            tocFileBuilder.setProductModelTypeXmlBuilder(productModelTypeBuilder);
            tocFileBuilder.setGenerateEntriesForModelTypes(true);
            return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptInterfaceBuilder,
                    productCmptImplClassBuilder, productCmptGenInterfaceBuilder, productCmptGenImplClassBuilder,
                    policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                    tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                    formulaTestBuilder, tocFileBuilder, validationMessagesBuilder, policyModelTypeBuilder,
                    productModelTypeBuilder, businessFunctionBuilder, enumTypeBuilder, enumContentBuilder,
                    enumXmlAdapterBuilder };
        } else {
            tocFileBuilder.setGenerateEntriesForModelTypes(false);
            return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptInterfaceBuilder,
                    productCmptImplClassBuilder, productCmptGenInterfaceBuilder, productCmptGenImplClassBuilder,
                    policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                    tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                    formulaTestBuilder, tocFileBuilder, validationMessagesBuilder, businessFunctionBuilder,
                    enumTypeBuilder, enumContentBuilder };
        }

    }

    private void createAnnotationGeneratorMap() throws CoreException {
        annotationGeneratorsMap = new HashMap<AnnotatedJavaElementType, List<IAnnotationGenerator>>();
        List<AnnotationGeneratorFactory> factories = getAnnotationGeneratorFactoriesRequiredForProject();

        for (AnnotatedJavaElementType type : AnnotatedJavaElementType.values()) {
            ArrayList<IAnnotationGenerator> annotationGenerators = new ArrayList<IAnnotationGenerator>();
            for (AnnotationGeneratorFactory annotationGeneratorFactory : factories) {
                IAnnotationGenerator annotationGenerator = annotationGeneratorFactory.createAnnotationGenerator(type);
                if (annotationGenerator == null) {
                    continue;
                }
                annotationGenerators.add(annotationGenerator);
            }
            annotationGeneratorsMap.put(type, annotationGenerators);
        }
    }

    private List<AnnotationGeneratorFactory> getAnnotationGeneratorFactoriesRequiredForProject() {
        List<AnnotationGeneratorFactory> factories = new ArrayList<AnnotationGeneratorFactory>();
        for (AnnotationGeneratorFactory annotationGeneratorFactorie : annotationGeneratorFactories) {
            if (annotationGeneratorFactorie.isRequiredFor(getIpsProject())) {
                factories.add(annotationGeneratorFactorie);
            }
        }
        return factories;
    }

    /**
     * Returns a code fragment containing all annotations to the given Java Element Type and
     * IpsElement.
     * 
     * @param type Determines the type of annotation to generate. See
     *            {@link AnnotatedJavaElementType} for a list of possible types.
     * @param ipsElement The IPS element to create the annotations for.
     */
    public JavaCodeFragment addAnnotations(AnnotatedJavaElementType type, IIpsElement ipsElement) {
        JavaCodeFragment code = new JavaCodeFragment();
        List<IAnnotationGenerator> generators = annotationGeneratorsMap.get(type);
        if (generators == null) {
            return code;
        }
        for (IAnnotationGenerator generator : generators) {
            code.append(generator.createAnnotation(ipsElement));
        }
        return code;
    }

    /**
     * Returns a code fragment containing all annotations to the given Java Element Type and
     * IpsElement using the given builder.
     * 
     * @param type Determines the type of annotation to generate. See
     *            {@link AnnotatedJavaElementType} for a list of possible types.
     * @param ipsElement The IPS element to create the annotations for. <br/>
     *            <code>Null</code> is permitted for certain AnnotatedJavaElementTypes which do not
     *            need further information. This is the case if <code>type</code> is
     *            POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD.
     * 
     * @param builder The builder for the Java Code Fragment to be generated.
     */
    public void addAnnotations(AnnotatedJavaElementType type, IIpsElement ipsElement, JavaCodeFragmentBuilder builder) {
        List<IAnnotationGenerator> generators = annotationGeneratorsMap.get(type);
        if (generators == null) {
            return;
        }
        for (IAnnotationGenerator generator : generators) {
            if (!generator.isGenerateAnnotationFor(ipsElement)) {
                continue;
            }
            builder.append(generator.createAnnotation(ipsElement));
            builder.appendln();
        }
        return;
    }

    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return new EnumTypeDatatypeHelper(enumTypeBuilder, datatypeAdapter);
    }

    /**
     * Returns the standard builder plugin version in the format [major.minor.micro]. The version
     * qualifier is not included in the version string.
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Returns whether Java5 enums shall be used in the code generated by this builder.
     */
    public boolean isUseEnums() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS).booleanValue();
    }

    /**
     * Returns if Java 5 typesafe collections shall be used in the code generated by this builder.
     */
    public boolean isUseTypesafeCollections() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS)
                .booleanValue();
    }

    /**
     * Returns whether JAXB support is to be generated by this builder.
     */
    public boolean isGenerateJaxbSupport() {
        return getConfig().getPropertyValueAsBoolean(CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT);
    }

    public FormulaCompiling getFormulaCompiling() {
        String kind = getConfig().getPropertyValueAsString(CONFIG_PROPERTY_FORMULA_COMPILING);
        try {
            return FormulaCompiling.valueOf(kind);
        } catch (Exception e) {
            // if value is not set correctly we use Both as default value
            return FormulaCompiling.Both;
        }
    }

    private void initSupportedPersistenceProviderMap() {
        allSupportedPersistenceProvider = new HashMap<String, CachedPersistenceProvider>(2);
        allSupportedPersistenceProvider.put(IPersistenceProvider.PROVIDER_IMPLEMENTATION_ECLIPSE_LINK_1_1,
                CachedPersistenceProvider.create(EclipseLink1PersistenceProvider.class));
        allSupportedPersistenceProvider.put(IPersistenceProvider.PROVIDER_IMPLEMENTATION_GENERIC_JPA_2_0,
                CachedPersistenceProvider.create(GenericJPA2PersistenceProvider.class));
    }

    @Override
    public boolean isPersistentProviderSupportConverter() {
        IPersistenceProvider persistenceProviderImpl = getPersistenceProviderImplementation();
        return persistenceProviderImpl != null && getPersistenceProviderImplementation().isSupportingConverters();
    }

    @Override
    public boolean isPersistentProviderSupportOrphanRemoval() {
        IPersistenceProvider persistenceProviderImpl = getPersistenceProviderImplementation();
        return persistenceProviderImpl != null && getPersistenceProviderImplementation().isSupportingOrphanRemoval();
    }

    /**
     * Returns the persistence provider or <code>null</code> if no
     */
    public IPersistenceProvider getPersistenceProviderImplementation() {
        String persistenceProviderKey = (String)getConfig().getPropertyValue(CONFIG_PROPERTY_PERSISTENCE_PROVIDER);
        if (StringUtils.isEmpty(persistenceProviderKey) || "none".equalsIgnoreCase(persistenceProviderKey)) {
            return null;
        }
        CachedPersistenceProvider pProviderCached = allSupportedPersistenceProvider.get(persistenceProviderKey);
        if (pProviderCached == null) {
            StdBuilderPlugin.log(new IpsStatus(IStatus.WARNING,
                    "Unknow persistence provider  \"" + persistenceProviderKey //$NON-NLS-1$
                            + "\". Supported provider are: " + allSupportedPersistenceProvider.keySet().toString()));
            return null;
        }

        if (pProviderCached.cachedProvider == null) {
            try {
                pProviderCached.cachedProvider = pProviderCached.persistenceProviderClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return pProviderCached.cachedProvider;
    }

    public String getJavaClassName(Datatype datatype) throws CoreException {
        if (datatype instanceof IPolicyCmptType) {
            return getGenerator((IPolicyCmptType)datatype).getQualifiedName(true);
        }

        if (datatype instanceof IProductCmptType) {
            return getGenerator((IProductCmptType)datatype).getQualifiedName(true);
        }

        return datatype.getJavaClassName();
    }

    /**
     * Returns a list containing all <tt>IJavaElement</tt>s this builder set generates for the given
     * <tt>IIpsObjectPartContainer</tt>.
     * <p>
     * Returns an empty list if no <tt>IJavaElement</tt>s are generated for the provided
     * <tt>IIpsObjectPartContainer</tt>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsObjectPartContainer The <tt>IIpsObjectPartContainer</tt> to obtain the generated
     *            <tt>IJavaElement</tt>s for.
     * 
     * @throws NullPointerException If the parameter is null
     */
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        /*
         * Clear generators so they will be re-initialized. We need to do this as if for example a
         * new attribute is added to a type due to a pull up refactoring the generator won't notice
         * it until another build happens but during the refactoring builds won't happen.
         */
        clearGenerators();

        // Re-creation of generators might fail due to cached validation errors
        ipsObjectPartContainer.getIpsModel().clearValidationCache();

        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        for (IIpsArtefactBuilder builder : getArtefactBuilders()) {
            if (builder instanceof ProductCmptBuilder) {
                builder = ((ProductCmptBuilder)builder).getGenerationBuilder();
            }
            if (!(builder instanceof JavaSourceFileBuilder)) {
                continue;
            }
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builder;
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsObjectPartContainer.getAdapter(IIpsSrcFile.class);
            try {
                if (javaBuilder.isBuilderFor(ipsSrcFile)) {
                    javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }

        return javaElements;
    }

    /**
     * Returns the <tt>ProductCmptGenImplClassBuilder</tt> or <tt>null</tt> if non has been
     * assembled yet.
     */
    public final ProductCmptGenImplClassBuilder getProductCmptGenImplClassBuilder() {
        return productCmptGenImplClassBuilder;
    }

    /**
     * Returns the <tt>ProductCmptGenInterfaceBuilder</tt> or <tt>null</tt> if non has been
     * assembled yet.
     */
    public final ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return productCmptGenInterfaceBuilder;
    }

    /**
     * Returns the <tt>PolicyCmptImplClassBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final PolicyCmptImplClassBuilder getPolicyCmptImplClassBuilder() {
        return policyCmptImplClassBuilder;
    }

    /**
     * Returns the <tt>PolicyCmptInterfaceBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final PolicyCmptInterfaceBuilder getPolicyCmptInterfaceBuilder() {
        return policyCmptInterfaceBuilder;
    }

    /**
     * Returns the <tt>ProductCmptImplClassBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final ProductCmptImplClassBuilder getProductCmptImplClassBuilder() {
        return productCmptImplClassBuilder;
    }

    /**
     * Returns the <tt>ProductCmptInterfaceBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final ProductCmptInterfaceBuilder getProductCmptInterfaceBuilder() {
        return productCmptInterfaceBuilder;
    }

    private static class CachedPersistenceProvider {
        Class<? extends IPersistenceProvider> persistenceProviderClass;
        IPersistenceProvider cachedProvider = null;

        private static CachedPersistenceProvider create(Class<? extends IPersistenceProvider> pPClass) {
            CachedPersistenceProvider providerCache = new CachedPersistenceProvider();
            providerCache.persistenceProviderClass = pPClass;
            return providerCache;
        }
    }

    public enum FormulaCompiling {

        Subclass,
        XML,
        Both;

        public boolean compileToSubclass() {
            return this == Subclass || this == Both;
        }

        public boolean compileToXml() {
            return this == XML || this == Both;
        }
    }
}
