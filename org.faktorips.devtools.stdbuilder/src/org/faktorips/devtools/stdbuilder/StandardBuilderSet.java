/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.GenericValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.GenericBuilderKindId;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.java.JavaSourceFileBuilder;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.datatype.DatatypeDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.devtools.stdbuilder.dthelper.DatatypeHelperFactory;
import org.faktorips.devtools.stdbuilder.dthelper.DatatypeHelperFactoryDefinition;
import org.faktorips.devtools.stdbuilder.dthelper.LocalDateHelperVariant;
import org.faktorips.devtools.stdbuilder.enumtype.EnumContentBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumPropertyBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.labels.LabelAndDescriptionPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink25PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2_1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.Jakarta2_2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XType;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xmodel.table.XTable;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendBuilder;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.EnumTypeBuilderFactory;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.PolicyCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.PolicyCmptValidatorBuilder;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.ProductCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.ProductCmptGenerationClassBuilder;
import org.faktorips.devtools.stdbuilder.xtend.table.TableBuilder;
import org.faktorips.devtools.stdbuilder.xtend.table.TableBuilderFactory;
import org.faktorips.devtools.stdbuilder.xtend.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.xtend.table.TableRowBuilderFactory;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;
import org.faktorips.util.ArgumentCheck;

/**
 * An {@link IIpsArtefactBuilderSet} implementation that assembles the standard Faktor-IPS
 * {@link IIpsArtefactBuilder IIpsArtefactBuilders}.
 */
public class StandardBuilderSet extends DefaultBuilderSet implements IJavaBuilderSet {

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
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public static final String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport"; //$NON-NLS-1$

    /**
     * Configuration property contains the persistence provider implementation.
     * <p>
     * All persistence support IDs are defined in {@link PersistenceSupportNames}.
     */
    public static final String CONFIG_PROPERTY_PERSISTENCE_PROVIDER = PersistenceSupportNames.STD_BUILDER_PROPERTY_PERSISTENCE_PROVIDER;

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
     * Configuration property that enables/disables the generation of serializable support on policy
     * components.
     * 
     * @see Serializable
     */
    public static final String CONFIG_PROPERTY_GENERATE_SERIALIZABLE_POLICY_CMPTS_SUPPORT = "serializablePolicyCmpts"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of getter methods of
     * {@link ProductCmptType} attributes in the according {@link PolicyCmptType} class.
     */
    public static final String CONFIG_PROPERTY_GENERATE_CONVENIENCE_GETTERS = "generateConvenienceGetters"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether to generate camel case constant
     * names with underscore separator or without. For example if this property is true, the
     * constant for the name checkAnythingRule would be generated as CHECK_ANYTHING_RULE, if the
     * property is false the constant name would be CHECKANYTHINGRUL.
     */
    public static final String CONFIG_PROPERTY_CAMELCASE_SEPARATED = "camelCaseSeparated"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether to generate public interfaces or
     * not.
     * <p>
     * Although this property is defined in this abstraction it needs to be configured in the
     * extension point of every specific builder. If it is not specified as a configuration
     * definition of any builder, the default value is <code>true</code>.
     */
    public static final String CONFIG_PROPERTY_PUBLISHED_INTERFACES = "generatePublishedInterfaces"; //$NON-NLS-1$

    /**
     * Configuration property that defines additional annotations that are generated above all
     * generated methods of {@link IPolicyCmptType}, {@link IProductCmptType}, {@link IEnumType},
     * {@link ITableStructure} and {@link ITableContents}
     */
    public static final String CONFIG_PROPERTY_ADDITIONAL_ANNOTATIONS = "additionalAnnotations"; //$NON-NLS-1$

    /**
     * Configuration property that defines annotations that are not removed from generated methods
     * of {@link IPolicyCmptType}, {@link IProductCmptType}, {@link IEnumType},
     * {@link ITableStructure} and {@link ITableContents}
     */
    public static final String CONFIG_PROPERTY_RETAIN_ANNOTATIONS = "retainAnnotations"; //$NON-NLS-1$

    /**
     * Configuration property that defines whether and which builder classes should be generated.
     */
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR = "builderClasses";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_NONE = "None";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_ALL = "All";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_POLICY = "Policies only";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_PRODUCT = "Products only";

    /**
     * Configuration property that defines which variant of local date should be used (joda or
     * java8)
     */
    public static final String CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT = "localDateDatatypeHelperVariant"; //$NON-NLS-1$

    public static final String CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE = "baseClassPolicyComponent"; //$NON-NLS-1$

    public static final String CONFIG_PROPERTY_BASE_CLASS_PRODUCT_CMPT_TYPE = "baseClassProductComponent"; //$NON-NLS-1$

    public static final String CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION = ChangesOverTimeNamingConventionPropertyDef.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION;

    /**
     * Configuration property that reduces the amount of generated comments.
     */
    public static final String CONFIG_PROPERTY_GENERATE_MINIMAL_JAVADOC = "minimalJavadoc"; //$NON-NLS-1$

    /**
     * Configuration property for the unify value set methods option.
     */
    public static final String CONFIG_PROPERTY_UNIFY_VALUE_SET_METHODS = "valueSetMethods";

    private static final String EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY = "artefactBuilderFactory";

    private ModelService modelService;

    private GeneratorModelContext generatorModelContext;

    private Map<String, IPersistenceProvider> allSupportedPersistenceProvider;

    private final String version;

    /**
     * Registry for looking up helpers for data types.
     * <p>
     * Note that the registry is initialized when the IPS project is set (i.e. when
     * {@link #setIpsProject(IIpsProject)} is invoked).
     */
    private DatatypeHelperRegistry datatypeHelperRegistry;

    /**
     * Registry for looking up helper factories for data types.
     * <p>
     * Note that the registry is initialized when the IPS project is set (i.e. when
     * {@link #setIpsProject(IIpsProject)} is invoked).
     */
    private DatatypeHelperFactoryRegistry datatypeHelperFactoryRegistry;

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
        // StringBuilder buf = new StringBuilder();
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
        code.appendClassName(getModelNode(tableStructure, XTable.class).getQualifiedName(BuilderAspect.IMPLEMENTATION));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_THIS_REPOSITORY + "(), \"" + tableContentsQualifiedName //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmptyResult("); //$NON-NLS-1$

        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", "); //$NON-NLS-1$
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get"); //$NON-NLS-1$
        code.append(StringUtils.capitalize(fct.getAccessedColumn().getName()));
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
        LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> builders = new LinkedHashMap<>();
        builders.put(BuilderKindIds.POLICY_CMPT_TYPE_INTERFACE,
                new PolicyCmptClassBuilder(true, this, generatorModelContext, modelService));
        builders.put(BuilderKindIds.POLICY_CMPT_TYPE_IMPLEMEMENTATION,
                new PolicyCmptClassBuilder(false, this, generatorModelContext, modelService));
        builders.put(BuilderKindIds.POLICY_CMPT_VALIDATOR_CLASS,
                new PolicyCmptValidatorBuilder(this, generatorModelContext, modelService));

        // create product component type builders
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_INTERFACE,
                new ProductCmptClassBuilder(true, this, generatorModelContext, modelService));
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_IMPLEMEMENTATION,
                new ProductCmptClassBuilder(false, this, generatorModelContext, modelService));
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_INTERFACE,
                new ProductCmptGenerationClassBuilder(true, this, generatorModelContext, modelService));
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_IMPLEMEMENTATION,
                new ProductCmptGenerationClassBuilder(false, this, generatorModelContext, modelService));

        builders.put(BuilderKindIds.TABLE, new TableBuilderFactory().createBuilder(this));
        builders.put(BuilderKindIds.TABLE_ROW, new TableRowBuilderFactory().createBuilder(this));

        // table content builders
        builders.put(BuilderKindIds.TABLE_CONTENT, new TableContentBuilder(this));

        // test case type builders
        builders.put(BuilderKindIds.TEST_CASE_TYPE, new TestCaseTypeClassBuilder(this));

        // test case builder
        builders.put(BuilderKindIds.TEST_CASE, new TestCaseBuilder(this));

        // toc file builder
        builders.put(BuilderKindIds.TOC_FILE, new TocFileBuilder(this));

        @SuppressWarnings("deprecation")
        org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder businessFunctionBuilder = new org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder(
                this);
        builders.put(BuilderKindIds.BUSINESS_FUNCTION, businessFunctionBuilder);
        // New enum type builder
        builders.put(BuilderKindIds.ENUM_TYPE, new EnumTypeBuilderFactory().createBuilder(this));
        builders.put(BuilderKindIds.ENUM_XML_ADAPTER, new EnumXmlAdapterBuilder(this));
        builders.put(BuilderKindIds.ENUM_CONTENT, new EnumContentBuilder(this));
        builders.put(BuilderKindIds.ENUM_PROPERTY, new EnumPropertyBuilder(this));

        // product component builders
        ProductCmptBuilder productCmptBuilder = new ProductCmptBuilder(this);
        builders.put(BuilderKindIds.PRODUCT_CMPT_IMPLEMENTATION, productCmptBuilder);
        builders.put(BuilderKindIds.PRODUCT_CMPT_XML, new ProductCmptXMLBuilder(IpsObjectType.PRODUCT_CMPT, this));

        builders.put(BuilderKindIds.VALIDATION_RULE_MESSAGES, new ValidationRuleMessagesPropertiesBuilder(this));
        builders.put(BuilderKindIds.LABELS_AND_DESCRIPTIONS, new LabelAndDescriptionPropertiesBuilder(this));

        List<IIpsArtefactBuilder> extendingBuilders = getExtendingArtefactBuilders();
        for (IIpsArtefactBuilder ipsArtefactBuilder : extendingBuilders) {
            GenericBuilderKindId id = new GenericBuilderKindId(ipsArtefactBuilder.getName());
            if (builders.containsKey(id)) {
                id = new GenericBuilderKindId();
            }
            builders.put(id, ipsArtefactBuilder);
        }

        return builders;
    }

    /**
     * Returns all builders registered with the standard builder set through the extension point
     * "artefactBuilder".
     * 
     * @return a list containing all builders that extend this builder set.
     */
    private List<IIpsArtefactBuilder> getExtendingArtefactBuilders() {
        List<IIpsArtefactBuilder> builders = new ArrayList<>();

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

    /**
     * Returns the standard builder plugin version in the format [major.minor.micro]. The version
     * qualifier is not included in the version string.
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Returns the persistence provider or <code>null</code> if no
     */
    @Override
    public IPersistenceProvider getPersistenceProvider() {
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
        allSupportedPersistenceProvider = Map
                .of(PersistenceSupportNames.ID_ECLIPSE_LINK_1_1, new EclipseLink1PersistenceProvider(),
                        PersistenceSupportNames.ID_ECLIPSE_LINK_2_5, new EclipseLink25PersistenceProvider(),
                        PersistenceSupportNames.ID_GENERIC_JPA_2, new GenericJPA2PersistenceProvider(),
                        PersistenceSupportNames.ID_GENERIC_JPA_2_1, new GenericJPA2_1PersistenceProvider(),
                        PersistenceSupportNames.ID_JAKARTA_PERSISTENCE_2_2, new Jakarta2_2PersistenceProvider());
    }

    public LocalDateHelperVariant getLocalDateHelperVariant() {
        return LocalDateHelperVariant
                .fromString(getConfig().getPropertyValueAsString(CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT));
    }

    /**
     * Returns the qualified class name for the given datatype.
     * 
     * @param datatype datatype to retrieve the class name for
     */
    public String getJavaClassName(Datatype datatype) {
        return getJavaClassName(datatype, true);
    }

    /**
     * Resolves the qualified class name for the given datatype.
     * 
     * @param datatype datatype to retrieve the class name for.
     * @param interfaces flag indicating whether the class name should be resolved to the published
     *            interface type
     */
    public String getJavaClassName(Datatype datatype, boolean interfaces) {
        if (datatype instanceof IPolicyCmptType) {
            return getJavaClassNameForPolicyCmptType((IPolicyCmptType)datatype, interfaces);
        } else if (datatype instanceof IProductCmptType) {
            return getJavaClassNameForProductCmptType((IProductCmptType)datatype, interfaces);
        } else {
            return getDatatypeHelper(datatype).getJavaClassName();
        }
    }

    private String getJavaClassNameForPolicyCmptType(IPolicyCmptType type, boolean interfaces) {
        return getJavaClassName(type, interfaces, XPolicyCmptClass.class);
    }

    private String getJavaClassNameForProductCmptType(IProductCmptType type, boolean interfaces) {
        if (type.isChangingOverTime()) {
            return getJavaClassName(type, interfaces, XProductCmptGenerationClass.class);
        } else {
            return getJavaClassName(type, interfaces, XProductCmptClass.class);
        }
    }

    private <T extends XType> String getJavaClassName(IType type, boolean interfaces, Class<T> modelNodeClass) {
        return modelService.getModelNode(type, modelNodeClass, generatorModelContext)
                .getQualifiedName(BuilderAspect.getValue(interfaces));

    }

    @Override
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);
        List<IJavaElement> javaElements = new ArrayList<>();
        for (IIpsArtefactBuilder builder : getArtefactBuilders()) {
            IIpsArtefactBuilder builderTemp = builder;
            if (builderTemp instanceof ProductCmptBuilder) {
                builderTemp = ((ProductCmptBuilder)builder).getGenerationBuilder();
            }
            if (!(builderTemp instanceof JavaSourceFileBuilder)) {
                continue;
            }
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builderTemp;
            IIpsSrcFile ipsSrcFile = ipsObjectPartContainer.getAdapter(IIpsSrcFile.class);
            try {
                if (javaBuilder.isBuilderFor(ipsSrcFile)) {
                    javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
                } else if (javaBuilder instanceof XtendBuilder<?>) {
                    XtendBuilder<?> xtendBuilder = (XtendBuilder<?>)javaBuilder;
                    if (xtendBuilder.isGeneratingArtifactsFor(ipsObjectPartContainer)) {
                        javaElements.addAll(xtendBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        return javaElements;
    }

    /**
     * Returns the <code>ProductCmptGenImplClassBuilder</code> or <code>null</code> if non has been
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
     * Returns the <code>PolicyCmptClassBuilder</code> or <code>null</code> if non has been
     * assembled yet.
     */
    public final PolicyCmptClassBuilder getPolicyCmptImplClassBuilder() {
        return getBuilderById(BuilderKindIds.POLICY_CMPT_TYPE_IMPLEMEMENTATION, PolicyCmptClassBuilder.class);
    }

    /**
     * Returns the <code>ProductCmptClassBuilder</code> or <code>null</code> if non has been
     * assembled yet.
     */
    public final ProductCmptClassBuilder getProductCmptImplClassBuilder() {
        return getBuilderById(BuilderKindIds.PRODUCT_CMPT_TYPE_IMPLEMEMENTATION, ProductCmptClassBuilder.class);
    }

    @Override
    public boolean isGeneratePublishedInterfaces() {
        return generatorModelContext.getBaseGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
    }

    @Override
    protected String getConfiguredAdditionalAnnotations() {
        return generatorModelContext.getBaseGeneratorConfig().getConfiguredAdditionalAnnotations();
    }

    @Override
    protected String getConfiguredRetainedAnnotations() {
        return generatorModelContext.getBaseGeneratorConfig().getConfiguredRetainedAnnotations();
    }

    public TableBuilder getTableBuilder() {
        return getBuilderById(BuilderKindIds.TABLE, TableBuilder.class);
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

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter enumtypeadapter = (EnumTypeDatatypeAdapter)datatype;
            XEnumType enumType = getModelNode(enumtypeadapter.getEnumType(), XEnumType.class);
            return new EnumTypeDatatypeHelper(enumType, (EnumTypeDatatypeAdapter)datatype);
        }

        if (datatypeHelperFactoryRegistry.hasFactory(datatype)) {
            DatatypeHelperFactory factory = datatypeHelperFactoryRegistry.getFactory(datatype);
            return factory.createDatatypeHelper(datatype, this);
        }

        return datatypeHelperRegistry.getDatatypeHelper(datatype);
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        super.setIpsProject(ipsProject);
        synchronized (ipsProject) {
            datatypeHelperRegistry = new DatatypeHelperRegistry(getIpsProject());
            datatypeHelperFactoryRegistry = new DatatypeHelperFactoryRegistry();
        }
    }

    /** Registry for looking up the {@link DatatypeHelper} for a {@link Datatype}. */
    private static class DatatypeHelperRegistry {

        /** Name of the extension point used to register data types and helpers. */
        private static final String DATATYPE_DEFINITION_EXTENSION_POINT = "datatypeDefinition";

        private Map<Datatype, DatatypeHelper> helperMap = new HashMap<>();

        public DatatypeHelperRegistry(IIpsProject ipsProject) {
            super();
            initialize(ipsProject);
        }

        /**
         * Returns the helper registered for the given data type or {@code null} if no helper is
         * registered for that type.
         */
        public DatatypeHelper getDatatypeHelper(Datatype datatype) {
            return helperMap.get(datatype);
        }

        /**
         * Initializes the registered helpers using (all) the helpers provided via the
         * {@link #DATATYPE_DEFINITION_EXTENSION_POINT extension point} and the data type defined in
         * the given projects.
         */
        private void initialize(IIpsProject ipsProject) {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint point = registry.getExtensionPoint(IpsModelActivator.PLUGIN_ID,
                    DATATYPE_DEFINITION_EXTENSION_POINT);
            IExtension[] extensions = point.getExtensions();

            for (IExtension extension : extensions) {
                for (IConfigurationElement configElement : extension.getConfigurationElements()) {
                    registerHelper(new DatatypeDefinition(extension, configElement));
                }
            }

            List<Datatype> definedDatatypes = ipsProject.getProperties().getDefinedDatatypes();
            for (Datatype datatype : definedDatatypes) {
                if (datatype instanceof GenericValueDatatype) {
                    GenericValueDatatype valueDatatype = (GenericValueDatatype)datatype;
                    registerHelper(valueDatatype, new GenericValueDatatypeHelper(valueDatatype));
                }
            }
        }

        private void registerHelper(DatatypeDefinition definition) {
            if (definition.hasDatatype() && definition.hasHelper()) {
                helperMap.put(definition.getDatatype(), definition.getHelper());
            }
        }

        private void registerHelper(Datatype datatype, DatatypeHelper helper) {
            helperMap.put(datatype, helper);
        }
    }

    /** Registry for looking up the {@link DatatypeHelperFactory} for a {@link Datatype}. */
    private static class DatatypeHelperFactoryRegistry {

        /** Name of the extension point used to register data types and helpers. */
        private static final String DATATYPE_HELPER_FACTORY_EXTENSION_POINT = "datatypeHelperFactory";

        private Map<Datatype, DatatypeHelperFactory> factoryMap = new HashMap<>();

        public DatatypeHelperFactoryRegistry() {
            super();
            initialize();
        }

        /**
         * Returns the helper registered for the given data type or {@code null} if no helper is
         * registered for that type.
         */
        public DatatypeHelperFactory getFactory(Datatype datatype) {
            return factoryMap.get(datatype);
        }

        public boolean hasFactory(Datatype d) {
            return factoryMap.containsKey(d);
        }

        /**
         * Initializes the registered factories using (all) the helpers provided via the
         * {@link #DATATYPE_HELPER_FACTORY_EXTENSION_POINT extension point} and the data type
         * defined in the given projects.
         */
        private void initialize() {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint point = registry.getExtensionPoint(StdBuilderPlugin.PLUGIN_ID,
                    DATATYPE_HELPER_FACTORY_EXTENSION_POINT);
            IExtension[] extensions = point.getExtensions();

            for (IExtension extension : extensions) {
                for (IConfigurationElement configElement : extension.getConfigurationElements()) {
                    registerHelper(new DatatypeHelperFactoryDefinition(extension, configElement));
                }
            }
        }

        private void registerHelper(DatatypeHelperFactoryDefinition definition) {
            factoryMap.put(definition.getDatatype(), definition.getFactory());
        }

    }

}
