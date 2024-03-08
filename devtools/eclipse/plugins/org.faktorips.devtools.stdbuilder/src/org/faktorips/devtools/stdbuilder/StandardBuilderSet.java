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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.GenericBuilderKindId;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.fl.StandardIdentifierResolver;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.java.JavaSourceFileBuilder;
import org.faktorips.devtools.model.builder.labels.LabelAndDescriptionPropertiesBuilder;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.builder.xmodel.table.XTable;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.devtools.stdbuilder.dthelper.DatatypeHelperFactory;
import org.faktorips.devtools.stdbuilder.dthelper.DatatypeHelperFactoryDefinition;
import org.faktorips.devtools.stdbuilder.dthelper.LocalDateHelperVariant;
import org.faktorips.devtools.stdbuilder.enumtype.EnumContentBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumPropertyBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink25PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink3_0PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2_1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.Jakarta2_2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.Jakarta3_0PersistenceProvider;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
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
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.ArgumentCheck;

/**
 * An {@link IIpsArtefactBuilderSet} implementation that assembles the standard Faktor-IPS
 * {@link IIpsArtefactBuilder IIpsArtefactBuilders}.
 */
public class StandardBuilderSet extends JavaBuilderSet {

    public static final String ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset";

    private static final String EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY = "artefactBuilderFactory";

    private Map<String, IPersistenceProvider> allSupportedPersistenceProvider;

    private final String version;

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
    public boolean isSupportTableAccess() {
        return true;
    }

    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) {

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
            ExprCompiler<JavaCodeFragment> exprCompiler) {
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
    protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() {
        // create policy component type builders
        LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> builders = new LinkedHashMap<>();
        builders.put(BuilderKindIds.POLICY_CMPT_TYPE_INTERFACE,
                new PolicyCmptClassBuilder(true, this, getGeneratorModelContext(), getModelService()));
        builders.put(BuilderKindIds.POLICY_CMPT_TYPE_IMPLEMEMENTATION,
                new PolicyCmptClassBuilder(false, this, getGeneratorModelContext(), getModelService()));
        builders.put(BuilderKindIds.POLICY_CMPT_VALIDATOR_CLASS,
                new PolicyCmptValidatorBuilder(this, getGeneratorModelContext(), getModelService()));

        // create product component type builders
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_INTERFACE,
                new ProductCmptClassBuilder(true, this, getGeneratorModelContext(), getModelService()));
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_IMPLEMEMENTATION,
                new ProductCmptClassBuilder(false, this, getGeneratorModelContext(), getModelService()));
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_INTERFACE,
                new ProductCmptGenerationClassBuilder(true, this, getGeneratorModelContext(), getModelService()));
        builders.put(BuilderKindIds.PRODUCT_CMPT_TYPE_GENERATION_IMPLEMEMENTATION,
                new ProductCmptGenerationClassBuilder(false, this, getGeneratorModelContext(), getModelService()));

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
        if (IpsStringUtils.isEmpty(persistenceProviderKey) || "none".equalsIgnoreCase(persistenceProviderKey)) {
            return null;
        }
        return allSupportedPersistenceProvider.get(persistenceProviderKey);
    }

    private void initSupportedPersistenceProviderMap() {
        allSupportedPersistenceProvider = Map
                .of(PersistenceSupportNames.ID_ECLIPSE_LINK_1_1, new EclipseLink1PersistenceProvider(),
                        PersistenceSupportNames.ID_ECLIPSE_LINK_2_5, new EclipseLink25PersistenceProvider(),
                        PersistenceSupportNames.ID_ECLIPSE_LINK_3_0, new EclipseLink3_0PersistenceProvider(),
                        PersistenceSupportNames.ID_GENERIC_JPA_2, new GenericJPA2PersistenceProvider(),
                        PersistenceSupportNames.ID_GENERIC_JPA_2_1, new GenericJPA2_1PersistenceProvider(),
                        PersistenceSupportNames.ID_JAKARTA_PERSISTENCE_2_2, new Jakarta2_2PersistenceProvider(),
                        PersistenceSupportNames.ID_JAKARTA_PERSISTENCE_3_0, new Jakarta3_0PersistenceProvider());
    }

    public LocalDateHelperVariant getLocalDateHelperVariant() {
        return LocalDateHelperVariant
                .fromString(getConfig().getPropertyValueAsString(CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT));
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
            if (!(builderTemp instanceof JavaSourceFileBuilder javaBuilder)) {
                continue;
            }
            IIpsSrcFile ipsSrcFile = ipsObjectPartContainer.getAdapter(IIpsSrcFile.class);
            if (javaBuilder.isBuilderFor(ipsSrcFile)) {
                javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
            } else if (javaBuilder instanceof XtendBuilder<?> xtendBuilder) {
                if (xtendBuilder.isGeneratingArtifactsFor(ipsObjectPartContainer)) {
                    javaElements.addAll(xtendBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
                }
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

    public TableBuilder getTableBuilder() {
        return getBuilderById(BuilderKindIds.TABLE, TableBuilder.class);
    }

    public TableRowBuilder getTableRowBuilder() {
        return getBuilderById(BuilderKindIds.TABLE_ROW, TableRowBuilder.class);
    }

    public EnumTypeBuilder getEnumTypeBuilder() {
        return getBuilderById(BuilderKindIds.ENUM_TYPE, EnumTypeBuilder.class);
    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (datatypeHelperFactoryRegistry.hasFactory(datatype)) {
            DatatypeHelperFactory factory = datatypeHelperFactoryRegistry.getFactory(datatype);
            return factory.createDatatypeHelper(datatype, this);
        }

        return super.getDatatypeHelper(datatype);
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        super.setIpsProject(ipsProject);
        synchronized (ipsProject) {
            datatypeHelperFactoryRegistry = new DatatypeHelperFactoryRegistry();
        }
    }

    @Override
    public boolean usesUnifiedValueSets() {
        return ValueSetMethods.Unified.name().equals(
                getConfig().getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_UNIFY_VALUE_SET_METHODS));
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
