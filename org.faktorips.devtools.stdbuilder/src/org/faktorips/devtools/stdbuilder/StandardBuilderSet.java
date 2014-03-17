/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.GenericBuilderKindId;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumContentBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumPropertyBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.XpandBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptGenerationClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
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

    public static final String ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset";

    /**
     * Configuration property that enables/disables the generation of a copy method.
     * 
     * @see ICopySupport
     */
    public static final String CONFIG_PROPERTY_GENERATE_COPY_SUPPORT = "generateCopySupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of delta computation.
     * 
     * @see IDeltaSupport
     */
    public static final String CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT = "generateDeltaSupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of the visitor support.
     * 
     * @see IDeltaSupport
     */
    public static final String CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT = "generateVisitorSupport"; //$NON-NLS-1$

    /**
     * Configuration property that is supposed to be used to read a configuration value from the
     * IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public static final String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the use of enums, if supported by the target
     * java version.
     */
    public static final String CONFIG_PROPERTY_USE_ENUMS = "useJavaEnumTypes"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public static final String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport"; //$NON-NLS-1$

    /**
     * Configuration property contains the persistence provider implementation.
     */
    public static final String CONFIG_PROPERTY_PERSISTENCE_PROVIDER = "persistenceProvider"; //$NON-NLS-1$

    /**
     * Configuration property contains the kind of formula compiling.
     */
    public static final String CONFIG_PROPERTY_FORMULA_COMPILING = "formulaCompiling"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether toXml() methods should be
     * generated.
     */
    public static final String CONFIG_PROPERTY_TO_XML_SUPPORT = "toXMLSupport"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether to generate camel case constant
     * names with underscore separator or without. For example if this property is true, the
     * constant for the name checkAnythingRule would be generated as CHECK_ANYTHING_RULE, if the
     * property is false the constant name would be CHECKANYTHINGRUL.
     */
    public static final String CONFIG_PROPERTY_CAMELCASE_SEPARATED = "camelCaseSeparated"; //$NON-NLS-1$

    private static final String EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY = "artefactBuilderFactory";

    private ModelService modelService;

    private GeneratorModelContext generatorModelContext;

    private Map<String, IPersistenceProvider> allSupportedPersistenceProvider;

    private final String version;

    public StandardBuilderSet() {
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
    public void clean(IProgressMonitor monitor) {
        super.clean(monitor);
        modelService.clear();
    }

    @Override
    public boolean isSupportTableAccess() {
        return true;
    }

    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) throws CoreException {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();

        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        code.appendClassName(getTableImplBuilder().getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_THIS_REPOSITORY + "(), \"" + tableContentsQualifiedName //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmtpyResult("); //$NON-NLS-1$

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
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
            ExprCompiler<JavaCodeFragment> exprCompiler) throws CoreException {
        if (exprCompiler instanceof ExtendedExprCompiler) {
            return new StandardIdentifierResolver(formula, (ExtendedExprCompiler)exprCompiler, this);
        } else {
            throw new RuntimeException(
                    "Illegal expression compiler, only ExtendedExpressionCompiler is allowed but found "
                            + exprCompiler.getClass());
        }
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    @Override
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {
        modelService = new ModelService();
        generatorModelContext = new GeneratorModelContext(config, this, getIpsProject());
        super.initialize(config);
    }

    @Override
    protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() throws CoreException {
        // create policy component type builders
        LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> builders = new LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder>();
        builders.put(BuilderKindIds.POLICY_CMPT_TYPE_INTERFACE, new PolicyCmptClassBuilder(true, this,
                generatorModelContext, modelService));
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false, this, generatorModelContext,
                modelService);
        builders.put(BuilderKindIds.POLICY_CMPT_TYPE_IMPLEMEMENTATION, policyCmptClassBuilder);

        // create product component type builders
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_INTERFACE, new ProductCmptClassBuilder(true, this,
                generatorModelContext, modelService));
        ProductCmptClassBuilder productCmptClassBuilder = new ProductCmptClassBuilder(false, this,
                generatorModelContext, modelService);
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_IMPLEMEMENTATION, productCmptClassBuilder);
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_INTERFACE, new ProductCmptGenerationClassBuilder(true,
                this, generatorModelContext, modelService));
        ProductCmptGenerationClassBuilder productCmptGenerationClassBuilder = new ProductCmptGenerationClassBuilder(
                false, this, generatorModelContext, modelService);
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_IMPLEMEMENTATION, productCmptGenerationClassBuilder);

        // table structure builders
        TableImplBuilder tableImplBuilder = new TableImplBuilder(this);
        builders.put(BuilderKindIds.TABLE, tableImplBuilder);
        TableRowBuilder tableRowBuilder = new TableRowBuilder(this);
        builders.put(BuilderKindIds.TABLE_ROW, tableRowBuilder);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        builders.put(BuilderKindIds.TABLE_CONTENT, new TableContentBuilder(this));

        // test case type builders
        builders.put(BuilderKindIds.TEST_CASE_TYPE, new TestCaseTypeClassBuilder(this));

        // test case builder
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder(this);
        builders.put(BuilderKindIds.TEST_CASE, testCaseBuilder);

        // toc file builder
        TocFileBuilder tocFileBuilder = new TocFileBuilder(this);
        builders.put(BuilderKindIds.TOC_FILE, tocFileBuilder);

        builders.put(BuilderKindIds.BUSINESS_FUNCTION, new BusinessFunctionBuilder(this));
        // New enum type builder
        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder(this);
        builders.put(BuilderKindIds.ENUM_TYPE, enumTypeBuilder);
        builders.put(BuilderKindIds.ENUM_XML_ADAPTER, new EnumXmlAdapterBuilder(this, enumTypeBuilder));
        builders.put(BuilderKindIds.ENUM_CONTENT, new EnumContentBuilder(this));
        builders.put(BuilderKindIds.ENUM_PROPERTY, new EnumPropertyBuilder(this));

        // product component builders
        ProductCmptBuilder productCmptBuilder = new ProductCmptBuilder(this);
        builders.put(BuilderKindIds.PRODUCT_CMPT_IMPLEMENTATION, productCmptBuilder);
        IIpsArtefactBuilder productCmptXmlBuilder = new ProductCmptXMLBuilder(IpsObjectType.PRODUCT_CMPT, this);
        builders.put(BuilderKindIds.PRODUCT_CMPT_XML, productCmptXmlBuilder);

        productCmptBuilder.setProductCmptImplBuilder(productCmptClassBuilder);
        productCmptBuilder.setProductCmptGenImplBuilder(productCmptGenerationClassBuilder);

        // test case builder
        testCaseBuilder.setJavaSourceFileBuilder(policyCmptClassBuilder);

        builders.put(BuilderKindIds.VALIDATION_RULE_MESSAGES, new ValidationRuleMessagesPropertiesBuilder(this));

        List<IIpsArtefactBuilder> extendingBuilders = getExtendingArtefactBuilders();
        for (IIpsArtefactBuilder ipsArtefactBuilder : extendingBuilders) {
            GenericBuilderKindId id = new GenericBuilderKindId(ipsArtefactBuilder.getName());
            if (builders.containsKey(id)) {
                id = new GenericBuilderKindId();
            }
            builders.put(id, ipsArtefactBuilder);
        }

        builders.put(BuilderKindIds.POLICY_CMPT_MODEL_TYPE, new ModelTypeXmlBuilder(IpsObjectType.POLICY_CMPT_TYPE,
                this));
        builders.put(BuilderKindIds.PRODUCT_CMPT_MODEL_TYPE, new ModelTypeXmlBuilder(IpsObjectType.PRODUCT_CMPT_TYPE,
                this));
        tocFileBuilder.setGenerateEntriesForModelTypes(true);

        return builders;
    }

    /**
     * Returns all builders registered with the standard builder set through the extension point
     * "artefactBuilder".
     * 
     * @return a list containing all builders that extend this builder set.
     */
    private List<IIpsArtefactBuilder> getExtendingArtefactBuilders() {
        List<IIpsArtefactBuilder> builders = new ArrayList<IIpsArtefactBuilder>();

        ExtensionPoints extensionPoints = new ExtensionPoints(StdBuilderPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension(EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY);
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configurationElements) {
                if (EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY.equals(configElement.getName())) {
                    IIpsArtefactBuilderFactory builderFactory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, "class", IIpsArtefactBuilderFactory.class); //$NON-NLS-1$
                    IIpsArtefactBuilder builder = builderFactory.createBuilder(this);
                    builders.add(builder);
                }
            }
        }
        return builders;
    }

    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return new EnumTypeDatatypeHelper(getEnumTypeBuilder(), datatypeAdapter);
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
        return true;
    }

    /**
     * Returns whether JAXB support is to be generated by this builder.
     */
    public boolean isGenerateJaxbSupport() {
        return getConfig().getPropertyValueAsBoolean(CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT);
    }

    /**
     * Returns whether toXml() methods are to be generated.
     */
    public boolean isGenerateToXmlSupport() {
        return generatorModelContext.isGenerateToXmlSupport();
    }

    /**
     * Returns whether to generate camel case constant names with underscore separator or without.
     * For example if this property is true, the constant for the property
     * checkAnythingAndDoSomething would be generated as CHECK_ANYTHING_AND_DO_SOMETHING, if the
     * property is false the constant name would be CHECKANYTHINGANDDOSOMETHING.
     */
    public boolean isGenerateSeparatedCamelCase() {
        return generatorModelContext.isGenerateSeparatedCamelCase();
    }

    public FormulaCompiling getFormulaCompiling() {
        String kind = getConfig().getPropertyValueAsString(CONFIG_PROPERTY_FORMULA_COMPILING);
        try {
            return FormulaCompiling.valueOf(kind);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            // if value is not set correctly we use Both as default value
            return FormulaCompiling.Both;
        }
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
        if (allSupportedPersistenceProvider == null) {
            initSupportedPersistenceProviderMap();
        }
        String persistenceProviderKey = (String)getConfig().getPropertyValue(CONFIG_PROPERTY_PERSISTENCE_PROVIDER);
        if (StringUtils.isEmpty(persistenceProviderKey) || "none".equalsIgnoreCase(persistenceProviderKey)) {
            return null;
        }
        return allSupportedPersistenceProvider.get(persistenceProviderKey);
    }

    private void initSupportedPersistenceProviderMap() {
        allSupportedPersistenceProvider = new HashMap<String, IPersistenceProvider>(2);
        allSupportedPersistenceProvider.put(IPersistenceProvider.PROVIDER_IMPLEMENTATION_ECLIPSE_LINK_1_1,
                new EclipseLink1PersistenceProvider());
        allSupportedPersistenceProvider.put(IPersistenceProvider.PROVIDER_IMPLEMENTATION_GENERIC_JPA_2_0,
                new GenericJPA2PersistenceProvider());
    }

    public String getJavaClassName(Datatype datatype) {
        return getJavaClassName(datatype, true);
    }

    public String getJavaClassName(Datatype datatype, boolean interfaces) {
        if (datatype instanceof IPolicyCmptType) {
            return getModelNode((IPolicyCmptType)datatype, XPolicyCmptClass.class).getQualifiedName(
                    BuilderAspect.getValue(interfaces));
        } else if (datatype instanceof IProductCmptType) {
            return modelService.getModelNode((IProductCmptType)datatype, XProductCmptGenerationClass.class,
                    generatorModelContext).getQualifiedName(BuilderAspect.getValue(interfaces));
        } else {
            return datatype.getJavaClassName();
        }
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

        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        for (IIpsArtefactBuilder builder : getArtefactBuilders()) {
            IIpsArtefactBuilder builderTemp = builder;
            if (builderTemp instanceof ProductCmptBuilder) {
                builderTemp = ((ProductCmptBuilder)builder).getGenerationBuilder();
            }
            if (!(builderTemp instanceof JavaSourceFileBuilder)) {
                continue;
            }
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builderTemp;
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsObjectPartContainer.getAdapter(IIpsSrcFile.class);
            try {
                if (javaBuilder.isBuilderFor(ipsSrcFile)) {
                    javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
                } else if (javaBuilder instanceof XpandBuilder<?>) {
                    XpandBuilder<?> xpandBuilder = (XpandBuilder<?>)javaBuilder;
                    if (xpandBuilder.isGenerateingArtifactsFor(ipsObjectPartContainer)) {
                        javaElements.addAll(xpandBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
                    }
                }

            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        return javaElements;
    }

    /**
     * Returns the <tt>ProductCmptGenImplClassBuilder</tt> or <tt>null</tt> if non has been
     * assembled yet.
     */
    public final ProductCmptGenerationClassBuilder getProductCmptGenImplClassBuilder() {
        return getBuilderById(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_IMPLEMEMENTATION,
                ProductCmptGenerationClassBuilder.class);
    }

    public final ProductCmptBuilder getProductCmptBuilder() {
        return getBuilderById(BuilderKindIds.PRODUCT_CMPT_IMPLEMENTATION, ProductCmptBuilder.class);
    }

    /**
     * Returns the <tt>PolicyCmptClassBuilder</tt> or <tt>null</tt> if non has been assembled yet.
     */
    public final PolicyCmptClassBuilder getPolicyCmptImplClassBuilder() {
        return getBuilderById(BuilderKindIds.POLICY_CMPT_TYPE_IMPLEMEMENTATION, PolicyCmptClassBuilder.class);
    }

    /**
     * Returns the <tt>ProductCmptClassBuilder</tt> or <tt>null</tt> if non has been assembled yet.
     */
    public final ProductCmptClassBuilder getProductCmptImplClassBuilder() {
        return getBuilderById(BuilderKindIds.PRODUCT_CMPT_TYPE_IMPLEMEMENTATION, ProductCmptClassBuilder.class);
    }

    public TableImplBuilder getTableImplBuilder() {
        return getBuilderById(BuilderKindIds.TABLE, TableImplBuilder.class);
    }

    public TableRowBuilder getTableRowBuilder() {
        return getBuilderById(BuilderKindIds.TABLE_ROW, TableRowBuilder.class);
    }

    public EnumTypeBuilder getEnumTypeBuilder() {
        return getBuilderById(BuilderKindIds.ENUM_TYPE, EnumTypeBuilder.class);
    }

    public String getValidationMessageBundleBaseName(IIpsSrcFolderEntry entry) {
        return generatorModelContext.getValidationMessageBundleBaseName(entry);
    }

    public <T extends AbstractGeneratorModelNode> T getModelNode(IIpsObjectPartContainer object, Class<T> type) {
        return modelService.getModelNode(object, type, generatorModelContext);
    }

    public ModelService getModelService() {
        return modelService;
    }

    public GeneratorModelContext getGeneratorModelContext() {
        return generatorModelContext;
    }

    public enum FormulaCompiling {

        Subclass,
        XML,
        Both;

        public boolean isCompileToSubclass() {
            return this == Subclass || this == Both;
        }

        public boolean isCompileToXml() {
            return this == XML || this == Both;
        }
    }
}
