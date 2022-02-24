// CSOFF: FileLengthCheck
// CSOFF: RegexpHeaderCheck
/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.datatype.IDynamicValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.datatype.DynamicValueDatatype;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathManifestReader;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathXmlPersister;
import org.faktorips.devtools.model.internal.ipsproject.Messages;
import org.faktorips.devtools.model.internal.productcmpt.NoVersionIdProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsFeatureConfiguration;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.model.ipsproject.TableContentFormat;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.util.IpsProjectPropertiesForOldVersion;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.fl.AssociationNavigationFunctionsResolver;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.IoUtil;
import org.faktorips.values.Decimal;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * An IPS project's properties. The project can't keep the properties on its own, as it is a handle.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectProperties implements IIpsProjectProperties {

    public static final String ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION = "changesInTimeNamingConvention"; //$NON-NLS-1$

    public static final String TAG_NAME = "IpsProject"; //$NON-NLS-1$

    private static final String ADDITIONAL_SETTINGS_TAG_NAME = "AdditionalSettings"; //$NON-NLS-1$

    private static final String SETTING_TAG_NAME = "Setting"; //$NON-NLS-1$

    private static final String SETTING_ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

    private static final String SETTING_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

    private static final String SETTING_ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

    private static final String SETTING_REFERENCED_PRODUCT_COMPONENTS_ARE_VALID_ON_THIS_GENERATIONS_VALID_FROM_DATE = "referencedProductComponentsAreValidOnThisGenerationsValidFromDate"; //$NON-NLS-1$

    private static final String SETTING_DERIVED_UNION_IS_IMPLEMENTED = "derivedUnionIsImplemented"; //$NON-NLS-1$

    private static final String ATTRIBUTE_PERSISTENT_PROJECT = "persistentProject"; //$NON-NLS-1$

    private static final String SETTING_RULES_WITHOUT_REFERENCE = "rulesWithoutReferencesAllowed"; //$NON-NLS-1$

    private static final String SETTING_SHARED_ASSOCIATIONS = "sharedDetailToMasterAssociations"; //$NON-NLS-1$

    private static final String SETTING_FORMULA_LANGUAGE_LOCALE = "formulaLanguageLocale"; //$NON-NLS-1$

    private static final String SETTING_MARKER_ENUMS = "markerEnums"; //$NON-NLS-1$

    private static final String SETTING_BUSINESS_FUNCTIONS_FOR_VALIDATION_RULES = "businessFunctionsForValidationRules"; //$NON-NLS-1$

    private static final String SETTING_CHANGING_OVER_TIME_DEFAULT = "changingOverTimeDefault"; //$NON-NLS-1$

    private static final String SETTING_INFERRED_TEMPLATE_LINK_THRESHOLD = "inferredTemplateLinkThreshold"; //$NON-NLS-1$

    private static final String SETTING_INFERRED_TEMPLATE_PROPERTY_VALUE_THRESHOLD = "inferredTemplatePropertyValueThreshold"; //$NON-NLS-1$

    private static final String SETTING_DUPLICATE_PRODUCT_COMPONENT_SERVERITY = "duplicateProductComponentSeverity"; //$NON-NLS-1$

    private static final String SETTING_PERSISTENCE_COLUMN_SIZE_CHECKS_SERVERITY = "persistenceColumnSizeChecksSeverity"; //$NON-NLS-1$

    private static final String SETTING_TABLE_CONTENT_FORMAT = "tableContentFormat"; //$NON-NLS-1$

    private static final String SETTING_GENERATE_VALIDATOR_CLASS_BY_DEFAULT = "generateValidatorClassDefault"; //$NON-NLS-1$

    private static final String SETTING_GENERIC_VALIDATION_BY_DEFAULT = "genericValidationDefault"; //$NON-NLS-1$

    private static final String SETTING_ESCAPE_NON_STANDARD_BLANKS = "escapeNonStandardBlanks"; //$NON-NLS-1$

    private static final String SETTING_VALIDATE_IPS_SCHEMA = "validateIpsSchema"; //$NON-NLS-1$

    private static final String VERSION_ATTRIBUTE = "version"; //$NON-NLS-1$

    private static final String RELEASE_EXTENSION_ID_ATTRIBUTE = "releaseExtensionId"; //$NON-NLS-1$

    private static final String PRODUCT_RELEASE_DEPRECATED = "productRelease"; //$NON-NLS-1$
    private static final String PRODUCT_RELEASE = "ProductRelease"; //$NON-NLS-1$

    private static final String VERSION_PROVIDER_ATTRIBUTE = "versionProvider"; //$NON-NLS-1$

    private static final String VERSION_TAG_NAME = "Version"; //$NON-NLS-1$

    private static final String DEFAULT_CURRENCY_ELEMENT = "DefaultCurrency"; //$NON-NLS-1$

    private static final String DEFAULT_CURRENCY_VALUE_ATTR = "value"; //$NON-NLS-1$

    private static final String MARKER_ENUMS_DELIMITER = ";"; //$NON-NLS-1$

    private static final String FEATURE_CONFIGURATIONS_ELEMENT = "FeatureConfigurations"; //$NON-NLS-1$

    private static final String FEATURE_ID_ATTRIBUTE = "featureId"; //$NON-NLS-1$

    private boolean createdFromParsableFileContents = true;

    private boolean modelProject;
    private boolean productDefinitionProject;
    private boolean persistentProject;

    /**
     * the version of this project, used for release
     */
    private String version;
    /**
     * The id of the release extension that is associated with this project
     */
    private String releaseExtensionId;

    private String changesInTimeConventionIdForGeneratedCode = IChangesOverTimeNamingConvention.VAA;

    private IProductCmptNamingStrategy productCmptNamingStrategy;

    /**
     * The ID of the {@link IProductCmptNamingStrategy} to report validation errors when strategy
     * was not found
     */
    private String productCmptNamingStrategyId = null;

    private String builderSetId = ""; //$NON-NLS-1$
    private IIpsArtefactBuilderSetConfigModel builderSetConfig = new IpsArtefactBuilderSetConfigModel();

    private IIpsObjectPath path = null;

    private String[] predefinedDatatypesUsed = new String[0];

    // all datatypes defined in the project including(!) the value datatypes.
    private List<Datatype> definedDatatypes = new ArrayList<>(0);
    private String runtimeIdPrefix = ""; //$NON-NLS-1$
    private boolean derivedUnionIsImplementedRuleEnabled = true;
    private boolean referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled = true;
    private boolean rulesWithoutReferencesAllowed = false;
    private boolean sharedDetailToMasterAssociations = false;
    private boolean enableMarkerEnums = true;
    private boolean businessFunctionsForValidationRules = false;
    private boolean changingOverTimeDefault = false;
    private boolean generateValidatorClassByDefault = false;
    private boolean genericValidationByDefault = false;
    private boolean escapeNonStandardBlanks = false;
    private boolean validateIpsSchema = true;
    private Decimal inferredTemplateLinkThreshold = Decimal.valueOf(1);
    private Decimal inferredTemplatePropertyValueThreshold = Decimal.valueOf(0.8);
    private Severity duplicateProductComponentSeverity = Severity.WARNING;
    private Severity persistenceColumnSizeChecksSeverity = Severity.WARNING;
    private TableContentFormat tableContentFormat = TableContentFormat.XML;

    private LinkedHashSet<String> markerEnums = new LinkedHashSet<>();
    private Map<String, String> requiredFeatures = new HashMap<>();

    // hidden resource names in the model and product explorer
    private Set<String> resourcesPathExcludedFromTheProductDefiniton = new HashSet<>(10);
    private long lastPersistentModificationTimestamp;

    private IPersistenceOptions persistenceOptions = new PersistenceOptions();

    /** The set of natural languages supported by the IPS project. */
    private LinkedHashSet<ISupportedLanguage> supportedLanguages = new LinkedHashSet<>(2);

    private IProductCmptNamingStrategy defaultCmptNamingStrategy;

    // default currency for this project - default is EUR
    private Currency defaultCurrency = Currency.getInstance("EUR"); //$NON-NLS-1$

    private Locale formulaLanguageLocale = Locale.GERMAN;

    private String versionProviderId;

    private final Map<String, IpsFeatureConfiguration> featureConfigurations = new LinkedHashMap<>();

    /**
     * Used to check if the additional setting "markerEnums" is configured in the .ipsproject file.
     */
    private boolean markerEnumsConfiguredInIpsProjectFile = false;

    public IpsProjectProperties(IIpsProject ipsProject) {
        super();
        path = new IpsObjectPath(ipsProject);
    }

    /**
     * Copy constructor.
     */
    public IpsProjectProperties(IIpsProject ipsProject, IpsProjectProperties props) {
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element el = props.toXml(doc);
        initFromXml(ipsProject, el);
        createdFromParsableFileContents = props.createdFromParsableFileContents;
    }

    public static final IpsProjectProperties createFromXml(IIpsProject ipsProject, Element element) {
        IpsProjectProperties data = new IpsProjectProperties(ipsProject);
        data.initFromXml(ipsProject, element);
        return data;
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        try {
            MessageList list = new MessageList();
            if (validateBuilderSetId(ipsProject, list)) {
                validateBuilderSetConfig(ipsProject, list);
            }
            validateProductCmptNamingStrategy(list);
            validateUsedPredefinedDatatype(ipsProject, list);
            validateIpsObjectPath(list);
            validateRequiredFeatures(list);
            DynamicValueDatatype[] valuetypes = getDefinedValueDatatypes();
            for (DynamicValueDatatype valuetype : valuetypes) {
                list.add(valuetype.checkReadyToUse());
            }
            validatePersistenceOption(list);
            validateVersion(list);
            validateSupportedLanguages(list);
            validateFeatureConfigurations(list);
            validateDeprecatedBusinessFunctions(list);
            return list;
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            // CSON: IllegalCatch
            // if runtime exceptions are not converted into core exceptions the stack trace gets
            // lost in the logging file and they are hard to find
            throw new CoreException(new IpsStatus(e));
        }
    }

    @SuppressWarnings("deprecation")
    private void validateDeprecatedBusinessFunctions(MessageList msgList) {
        if (isBusinessFunctionsForValidationRulesEnabled()) {
            msgList.add(new Message(org.faktorips.devtools.model.businessfct.BusinessFunction.MSGCODE_DEPRECATED,
                    org.faktorips.devtools.model.internal.businessfct.Messages.BusinessFunction_deprecated,
                    Message.WARNING,
                    new ObjectProperty(this, SETTING_BUSINESS_FUNCTIONS_FOR_VALIDATION_RULES)));
        }
    }

    private void validatePersistenceOption(MessageList msgList) {
        if (isPersistenceSupportEnabled()) {
            String text = NLS.bind(Messages.IpsProjectProperties_error_persistenceAndSharedAssociationNotAllowed,
                    SETTING_SHARED_ASSOCIATIONS, ATTRIBUTE_PERSISTENT_PROJECT);
            if (isSharedDetailToMasterAssociations()) {
                msgList.add(new Message(IIpsProjectProperties.MSGCODE_INVALID_OPTIONAL_CONSTRAINT, text, Message.ERROR,
                        this));
            }
        }
    }

    private void validateProductCmptNamingStrategy(MessageList msgList) {
        if (productCmptNamingStrategy == null) {
            String text = NLS.bind(Messages.IpsProjectProperties_unknownNamingStrategy, productCmptNamingStrategyId);
            msgList.add(new Message(IIpsProjectProperties.MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY, text,
                    Message.ERROR, this));
        }
    }

    private void validateBuilderSetConfig(IIpsProject ipsProject, MessageList msgList) {
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel().getIpsArtefactBuilderSetInfo(builderSetId);
        msgList.add(builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, builderSetConfig));

    }

    private void validateRequiredFeatures(MessageList list) {
        IIpsFeatureVersionManager[] managers = IIpsModelExtensions.get().getIpsFeatureVersionManagers();
        for (IIpsFeatureVersionManager manager : managers) {
            if (manager.isRequiredForAllProjects() && getMinRequiredVersionNumber(manager.getFeatureId()) == null) {
                String text = NLS.bind(Messages.IpsProjectProperties_msgMissingMinFeatureId, manager.getFeatureId());
                list.add(new Message(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID, text, Message.ERROR, this));
            }
        }
    }

    private void validateUsedPredefinedDatatype(IIpsProject ipsProject, MessageList list) {
        IIpsModel model = ipsProject.getIpsModel();
        for (String element : predefinedDatatypesUsed) {
            if (!model.isPredefinedValueDatatype(element)) {
                String text = NLS.bind(Messages.IpsProjectProperties_msgUnknownDatatype, element);
                Message msg = new Message(IIpsProjectProperties.MSGCODE_UNKNOWN_PREDEFINED_DATATYPE, text,
                        Message.ERROR, this);
                list.add(msg);
            }
        }
    }

    private boolean validateBuilderSetId(IIpsProject ipsProject, MessageList list) {
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel().getIpsArtefactBuilderSetInfo(builderSetId);
        if (builderSetInfo == null) {
            String text = Messages.IpsProjectProperties_msgUnknownBuilderSetId + builderSetId;
            Message msg = new Message(IIpsProjectProperties.MSGCODE_UNKNOWN_BUILDER_SET_ID, text, Message.ERROR, this,
                    IIpsProjectProperties.PROPERTY_BUILDER_SET_ID);
            list.add(msg);
            return false;
        }
        return true;
    }

    /**
     * Validate the IPS object path entry.
     */
    private void validateIpsObjectPath(MessageList list) throws CoreException {
        list.add(path.validate());
    }

    private void validateVersion(MessageList list) {
        if (StringUtils.isNotEmpty(getVersion()) && StringUtils.isNotEmpty(getVersionProviderId())) {
            list.newError(MSGCODE_INVALID_VERSION_SETTING, Messages.IpsProjectProperties_err_versionOrVersionProvider,
                    new ObjectProperty(this, PROPERTY_VERSION), new ObjectProperty(this, PROPERTY_VERSION_PROVIDER_ID));
        }
    }

    private void validateSupportedLanguages(MessageList list) {
        validateSupportedLanguagesIsoConformity(list);
        validateSupportedLanguagesOneDefaultLanguage(list);
    }

    private void validateSupportedLanguagesIsoConformity(MessageList list) {
        String[] isoLanguages = Locale.getISOLanguages();
        List<String> isoLanguagesList = Arrays.asList(isoLanguages);
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            String languageString = supportedLanguage.getLocale().getLanguage();
            if (!(isoLanguagesList.contains(languageString))) {
                String text = NLS.bind(Messages.IpsProjectProperties_msgSupportedLanguageUnknownLocale, languageString);
                Message msg = new Message(IIpsProjectProperties.MSGCODE_SUPPORTED_LANGUAGE_UNKNOWN_LOCALE, text,
                        Message.ERROR);
                list.add(msg);
                break;
            }
        }
    }

    private void validateSupportedLanguagesOneDefaultLanguage(MessageList list) {
        int defaultLanguageCount = 0;
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            if (supportedLanguage.isDefaultLanguage()) {
                defaultLanguageCount++;
            }
            if (defaultLanguageCount == 2) {
                String text = Messages.IpsProjectProperties_msgMoreThanOneDefaultLanguage;
                Message msg = new Message(IIpsProjectProperties.MSGCODE_MORE_THAN_ONE_DEFAULT_LANGUAGE, text,
                        Message.ERROR);
                list.add(msg);
                break;
            }
        }
    }

    private void validateFeatureConfigurations(MessageList list) {
        Set<String> requiredIpsFeatureIds = new LinkedHashSet<>();
        for (String featureId : getRequiredIpsFeatureIds()) {
            requiredIpsFeatureIds.add(featureId);
        }
        for (Entry<String, IpsFeatureConfiguration> featureConfigurationEntry : featureConfigurations.entrySet()) {
            String featureId = featureConfigurationEntry.getKey();
            if (!requiredIpsFeatureIds.contains(featureId)) {
                String text = NLS.bind(Messages.IpsProjectProperties_msgUnknownFeatureIdForConfiguration, featureId);
                Message msg = new Message(IIpsProjectProperties.MSGCODE_FEATURE_CONFIGURATION_UNKNOWN_FEATURE, text,
                        Message.ERROR);
                list.add(msg);
            }
        }
    }

    /**
     * Returns <code>true</code> if this property object was created by reading a .ipsproject file
     * containing parsable XML data, otherwise <code>false</code>.
     */
    public boolean isCreatedFromParsableFileContents() {
        return createdFromParsableFileContents;
    }

    /**
     * Sets if if this property object was created by reading a .ipsproject file containing parsable
     * XML data, or not.
     */
    public void setCreatedFromParsableFileContents(boolean flag) {
        createdFromParsableFileContents = flag;
    }

    @Override
    public String getBuilderSetId() {
        return builderSetId;
    }

    @Override
    public void setBuilderSetId(String id) {
        ArgumentCheck.notNull(id);
        builderSetId = id;
    }

    @Override
    public IIpsObjectPath getIpsObjectPath() {
        return path;
    }

    @Override
    public boolean isModelProject() {
        return modelProject;
    }

    @Override
    public void setModelProject(boolean modelProject) {
        this.modelProject = modelProject;
    }

    @Override
    public boolean isProductDefinitionProject() {
        return productDefinitionProject;
    }

    @Override
    public void setProductDefinitionProject(boolean productDefinitionProject) {
        this.productDefinitionProject = productDefinitionProject;
    }

    @Override
    public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
        if (productCmptNamingStrategy == null) {
            return defaultCmptNamingStrategy;

        }
        return productCmptNamingStrategy;
    }

    @Override
    public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy) {
        ArgumentCheck.notNull(newStrategy);
        productCmptNamingStrategy = newStrategy;
        productCmptNamingStrategyId = newStrategy.getExtensionId();
    }

    /**
     * For testing purposes only!
     */
    public void setProductCmptNamingStrategyInternal(IProductCmptNamingStrategy newStrategy, String id) {
        productCmptNamingStrategy = newStrategy;
        productCmptNamingStrategyId = id;
    }

    @Override
    public void setChangesOverTimeNamingConventionIdForGeneratedCode(String changesInTimeConventionIdForGeneratedCode) {
        this.changesInTimeConventionIdForGeneratedCode = changesInTimeConventionIdForGeneratedCode;
    }

    @Override
    public String getChangesOverTimeNamingConventionIdForGeneratedCode() {
        return changesInTimeConventionIdForGeneratedCode;
    }

    @Override
    public void setIpsObjectPath(IIpsObjectPath path) {
        ArgumentCheck.notNull(path);
        this.path = path;
    }

    @Override
    public String[] getPredefinedDatatypesUsed() {
        return predefinedDatatypesUsed;
    }

    @Override
    public void setPredefinedDatatypesUsed(String[] datatypes) {
        ArgumentCheck.notNull(datatypes);
        predefinedDatatypesUsed = datatypes;
    }

    @Override
    public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes) {
        ArgumentCheck.notNull(datatypes);
        predefinedDatatypesUsed = new String[datatypes.length];
        for (int i = 0; i < datatypes.length; i++) {
            predefinedDatatypesUsed[i] = datatypes[i].getQualifiedName();
        }
    }

    @Override
    public List<Datatype> getDefinedDatatypes() {
        return definedDatatypes;
    }

    @Override
    public DynamicValueDatatype[] getDefinedValueDatatypes() {
        List<DynamicValueDatatype> valuetypes = new ArrayList<>(definedDatatypes.size());
        for (Datatype datatype : definedDatatypes) {
            if (datatype.isValueDatatype()) {
                valuetypes.add((DynamicValueDatatype)datatype);
            }
        }
        return valuetypes.toArray(new DynamicValueDatatype[valuetypes.size()]);
    }

    @Override
    public void setDefinedDatatypes(IDynamicValueDatatype[] datatypes) {
        definedDatatypes = new ArrayList<>(datatypes.length);
        for (IDynamicValueDatatype datatype : datatypes) {
            definedDatatypes.add(datatype);
        }
    }

    @Override
    public void setDefinedDatatypes(Datatype[] datatypes) {
        definedDatatypes = new ArrayList<>(datatypes.length);
        for (Datatype datatype : datatypes) {
            definedDatatypes.add(datatype);
        }
    }

    public Element toXml(Document doc) {
        createIpsProjectDescriptionComment(doc);
        Element projectEl = doc.createElement(TAG_NAME);
        projectEl.setAttribute("modelProject", "" + modelProject); //$NON-NLS-1$ //$NON-NLS-2$
        projectEl.setAttribute("productDefinitionProject", "" + productDefinitionProject); //$NON-NLS-1$ //$NON-NLS-2$
        projectEl.setAttribute("runtimeIdPrefix", runtimeIdPrefix); //$NON-NLS-1$
        projectEl.setAttribute(ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION, changesInTimeConventionIdForGeneratedCode);
        projectEl.setAttribute(ATTRIBUTE_PERSISTENT_PROJECT, Boolean.toString(persistentProject));

        // required features
        createRequiredIpsFeaturesComment(projectEl);
        Element features = doc.createElement("RequiredIpsFeatures"); //$NON-NLS-1$
        projectEl.appendChild(features);

        for (String key : requiredFeatures.keySet()) {
            Element feature = doc.createElement("RequiredIpsFeature"); //$NON-NLS-1$
            features.appendChild(feature);
            feature.setAttribute("id", key); //$NON-NLS-1$
            feature.setAttribute("minVersion", requiredFeatures.get(key)); //$NON-NLS-1$
        }

        // artefact builder set
        createIpsArtefactBuilderSetDescriptionComment(projectEl);

        Element builderSetEl = doc.createElement(IIpsArtefactBuilderSet.XML_ELEMENT);
        projectEl.appendChild(builderSetEl);
        builderSetEl.setAttribute("id", builderSetId); //$NON-NLS-1$
        builderSetEl.appendChild(builderSetConfig.toXml(doc));

        // naming strategy
        if (productCmptNamingStrategy != null) {
            createProductCmptNamingStrategyDescriptionComment(projectEl);
            projectEl.appendChild(productCmptNamingStrategy.toXml(doc));
        }

        // object path
        IpsObjectPathXmlPersister xmlIpsObjectPathPersistor = new IpsObjectPathXmlPersister();
        createDescriptionComment(xmlIpsObjectPathPersistor.getXmlFormatDescription(), projectEl);
        projectEl.appendChild(xmlIpsObjectPathPersistor.store(doc, ((IpsObjectPath)path)));

        toXmlDatatypes(doc, projectEl);
        toXmlResourcesExcludeFromProdDef(doc, projectEl);
        toXmlProductRelease(doc, projectEl);
        toXmlVersion(doc, projectEl);

        // optional constraints
        toXmlForAdditionalSettings(doc, projectEl);

        // persistence options
        toXmlPersistenceOptions(doc, projectEl);

        // supported languages
        createSupportedLanguagesDescriptionComment(projectEl);
        Element supportedLanguagesEl = doc.createElement("SupportedLanguages"); //$NON-NLS-1$
        projectEl.appendChild(supportedLanguagesEl);
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            supportedLanguagesEl.appendChild(supportedLanguage.toXml(doc));
        }

        // default currency
        String s = "Setting the default currency for this project using the ISO 4217 code of the currency (e.g. EUR for euro or USD for US Dollar)"; //$NON-NLS-1$
        createDescriptionComment(s, projectEl);
        Element defaultCurrencyElement = doc.createElement(DEFAULT_CURRENCY_ELEMENT);
        defaultCurrencyElement.setAttribute(DEFAULT_CURRENCY_VALUE_ATTR, defaultCurrency.getCurrencyCode());
        projectEl.appendChild(defaultCurrencyElement);

        toXmlFeatureConfigurations(doc, projectEl);

        return projectEl;
    }

    private void toXmlDatatypes(Document doc, Element projectEl) {
        createDatatypeDescriptionComment(projectEl);
        Element datatypesEl = doc.createElement("Datatypes"); //$NON-NLS-1$
        projectEl.appendChild(datatypesEl);
        Element predefinedTypesEl = doc.createElement("UsedPredefinedDatatypes"); //$NON-NLS-1$
        datatypesEl.appendChild(predefinedTypesEl);
        for (String element : predefinedDatatypesUsed) {
            Element datatypeEl = doc.createElement("Datatype"); //$NON-NLS-1$
            datatypeEl.setAttribute("id", element); //$NON-NLS-1$
            predefinedTypesEl.appendChild(datatypeEl);
        }
        Element definedDatatypesEl = doc.createElement("DatatypeDefinitions"); //$NON-NLS-1$
        datatypesEl.appendChild(definedDatatypesEl);
        writeDefinedDataTypesToXML(doc, definedDatatypesEl);
    }

    private void toXmlResourcesExcludeFromProdDef(Document doc, Element projectEl) {
        createResourcesExcludedFromProductDefinitionComment(projectEl);
        Element resourcesExcludedFromProdDefEl = doc.createElement("ResourcesExcludedFromProductDefinition"); //$NON-NLS-1$
        projectEl.appendChild(resourcesExcludedFromProdDefEl);
        for (String exclResource : resourcesPathExcludedFromTheProductDefiniton) {
            Element resourceExcludedEl = doc.createElement("Resource"); //$NON-NLS-1$
            resourceExcludedEl.setAttribute("path", exclResource); //$NON-NLS-1$
            resourcesExcludedFromProdDefEl.appendChild(resourceExcludedEl);
        }
    }

    private void toXmlProductRelease(Document doc, Element projectEl) {
        createProductReleaseComment(projectEl);
        if (StringUtils.isNotEmpty(releaseExtensionId)) {
            Element release = doc.createElement(PRODUCT_RELEASE);
            release.setAttribute(RELEASE_EXTENSION_ID_ATTRIBUTE, releaseExtensionId);
            projectEl.appendChild(release);
        }
    }

    private void toXmlVersion(Document doc, Element projectEl) {
        createVersionComment(projectEl);
        if (StringUtils.isNotEmpty(versionProviderId) || StringUtils.isNotEmpty(version)) {
            Element release = doc.createElement(VERSION_TAG_NAME);
            if (StringUtils.isNotEmpty(versionProviderId)) {
                release.setAttribute(VERSION_PROVIDER_ATTRIBUTE, versionProviderId);
            }
            if (StringUtils.isNotEmpty(version)) {
                release.setAttribute(VERSION_ATTRIBUTE, version);
            }
            projectEl.appendChild(release);
        }
    }

    private void toXmlForAdditionalSettings(Document doc, Element projectEl) {
        createAdditionalSettingsDescriptionComment(projectEl);
        Element additionalSettingsEl = doc.createElement(ADDITIONAL_SETTINGS_TAG_NAME);
        projectEl.appendChild(additionalSettingsEl);

        additionalSettingsEl.appendChild(
                createSettingElement(doc, SETTING_DERIVED_UNION_IS_IMPLEMENTED, derivedUnionIsImplementedRuleEnabled));

        additionalSettingsEl.appendChild(createSettingElement(doc,
                SETTING_REFERENCED_PRODUCT_COMPONENTS_ARE_VALID_ON_THIS_GENERATIONS_VALID_FROM_DATE,
                referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled));

        additionalSettingsEl
                .appendChild(createSettingElement(doc, SETTING_RULES_WITHOUT_REFERENCE, rulesWithoutReferencesAllowed));

        additionalSettingsEl.appendChild(
                createSettingElement(doc, SETTING_SHARED_ASSOCIATIONS, isSharedDetailToMasterAssociations()));

        additionalSettingsEl.appendChild(
                createSettingElement(doc, SETTING_FORMULA_LANGUAGE_LOCALE, formulaLanguageLocale.getLanguage()));

        additionalSettingsEl.appendChild(
                createSettingElement(doc, SETTING_MARKER_ENUMS, isMarkerEnumsEnabled(), getMarkerEnumsAsString()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_BUSINESS_FUNCTIONS_FOR_VALIDATION_RULES,
                isBusinessFunctionsForValidationRulesEnabled()));

        additionalSettingsEl.appendChild(
                createSettingElement(doc, SETTING_CHANGING_OVER_TIME_DEFAULT, isChangingOverTimeDefaultEnabled()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_INFERRED_TEMPLATE_LINK_THRESHOLD,
                getInferredTemplateLinkThreshold().toString()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_INFERRED_TEMPLATE_PROPERTY_VALUE_THRESHOLD,
                getInferredTemplatePropertyValueThreshold().toString()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_DUPLICATE_PRODUCT_COMPONENT_SERVERITY,
                getDuplicateProductComponentSeverity().toString()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_PERSISTENCE_COLUMN_SIZE_CHECKS_SERVERITY,
                getPersistenceColumnSizeChecksSeverity().toString()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_TABLE_CONTENT_FORMAT,
                getTableContentFormat().name()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_GENERATE_VALIDATOR_CLASS_BY_DEFAULT,
                isGenerateValidatorClassDefaultEnabled()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_GENERIC_VALIDATION_BY_DEFAULT,
                isGenericValidationDefaultEnabled()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_ESCAPE_NON_STANDARD_BLANKS,
                isEscapeNonStandardBlanks()));

        additionalSettingsEl.appendChild(createSettingElement(doc, SETTING_VALIDATE_IPS_SCHEMA,
                isValidateIpsSchema()));
    }

    private String getMarkerEnumsAsString() {
        return StringUtils.join(getMarkerEnums(), MARKER_ENUMS_DELIMITER);
    }

    private void toXmlPersistenceOptions(Document doc, Element projectEl) {
        createPersistenceOptionsDescriptionComment(projectEl);
        Element persistenceOptionsEl = doc.createElement("PersistenceOptions"); //$NON-NLS-1$
        persistenceOptionsEl.setAttribute(IPersistenceOptions.MAX_TABLE_NAME_LENGTH_ATTRIBUTENAME,
                String.valueOf(getPersistenceOptions().getMaxTableNameLength()));
        persistenceOptionsEl.setAttribute(IPersistenceOptions.MAX_COLUMN_NAME_LENGTH_ATTRIBUTENAME,
                String.valueOf(getPersistenceOptions().getMaxColumnNameLenght()));
        projectEl.appendChild(persistenceOptionsEl);
        persistenceOptionsEl.setAttribute(IPersistenceOptions.ALLOW_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS, "" //$NON-NLS-1$
                + Boolean.valueOf(getPersistenceOptions().isAllowLazyFetchForSingleValuedAssociations()));
        persistenceOptionsEl.setAttribute(IPersistenceOptions.MAX_TABLE_COLUMN_SIZE,
                String.valueOf(getPersistenceOptions().getMaxTableColumnSize()));
        persistenceOptionsEl.setAttribute(IPersistenceOptions.MAX_TABLE_COLUMN_SCALE,
                String.valueOf(getPersistenceOptions().getMaxTableColumnScale()));
        persistenceOptionsEl.setAttribute(IPersistenceOptions.MAX_TABLE_COLUMN_PRECISION,
                String.valueOf(getPersistenceOptions().getMaxTableColumnPrecision()));

        ITableNamingStrategy tableNamingStrategy = getPersistenceOptions().getTableNamingStrategy();
        ITableColumnNamingStrategy tableColumnNamingStrategy = getPersistenceOptions().getTableColumnNamingStrategy();

        persistenceOptionsEl.appendChild(tableNamingStrategy.toXml(doc));
        persistenceOptionsEl.appendChild(tableColumnNamingStrategy.toXml(doc));
    }

    private void toXmlFeatureConfigurations(Document doc, Element projectEl) {
        if (!featureConfigurations.isEmpty()) {
            Element featureConfigurationsElement = doc.createElement(FEATURE_CONFIGURATIONS_ELEMENT);
            for (Entry<String, IpsFeatureConfiguration> featureConfiguration : featureConfigurations.entrySet()) {
                Element featureConfigurationElement = featureConfiguration.getValue().toXml(doc);
                featureConfigurationElement.setAttribute(FEATURE_ID_ATTRIBUTE, featureConfiguration.getKey());
                featureConfigurationsElement.appendChild(featureConfigurationElement);
            }
            projectEl.appendChild(featureConfigurationsElement);
        }
    }

    @Override
    public LinkedHashSet<String> getMarkerEnums() {
        return markerEnums;
    }

    @Override
    public void addMarkerEnum(String qualifiedName) {
        ArgumentCheck.notNull(qualifiedName);
        getMarkerEnums().add(qualifiedName.trim());
    }

    @Override
    public void removeMarkerEnum(String qualifiedName) {
        ArgumentCheck.notNull(qualifiedName);
        getMarkerEnums().remove(qualifiedName);
    }

    @Override
    public boolean isMarkerEnumsEnabled() {
        return enableMarkerEnums;
    }

    @Override
    public void setMarkerEnumsEnabled(boolean enabled) {
        enableMarkerEnums = enabled;
    }

    private Element createSettingElement(Document doc, String name, boolean enabled) {
        Element constraintElement = doc.createElement(SETTING_TAG_NAME);
        constraintElement.setAttribute(SETTING_ATTRIBUTE_NAME, name);
        constraintElement.setAttribute(SETTING_ATTRIBUTE_ENABLED, Boolean.toString(enabled));
        return constraintElement;
    }

    protected Element createSettingElement(Document doc, String name, String value) {
        Element constraintElement = doc.createElement(SETTING_TAG_NAME);
        constraintElement.setAttribute(SETTING_ATTRIBUTE_NAME, name);
        constraintElement.setAttribute(SETTING_ATTRIBUTE_VALUE, value);
        return constraintElement;
    }

    private Element createSettingElement(Document doc, String name, boolean enable, String value) {
        Element constraintElement = doc.createElement(SETTING_TAG_NAME);
        constraintElement.setAttribute(SETTING_ATTRIBUTE_NAME, name);
        constraintElement.setAttribute(SETTING_ATTRIBUTE_ENABLED, Boolean.toString(enable));
        constraintElement.setAttribute(SETTING_ATTRIBUTE_VALUE, value);
        return constraintElement;
    }

    public void initFromXml(IIpsProject ipsProject, Element element) {
        modelProject = Boolean.valueOf(element.getAttribute("modelProject")).booleanValue(); //$NON-NLS-1$
        productDefinitionProject = Boolean.valueOf(element.getAttribute("productDefinitionProject")).booleanValue(); //$NON-NLS-1$
        persistentProject = ValueToXmlHelper.isAttributeTrue(element, ATTRIBUTE_PERSISTENT_PROJECT);
        runtimeIdPrefix = element.getAttribute("runtimeIdPrefix"); //$NON-NLS-1$
        changesInTimeConventionIdForGeneratedCode = element.getAttribute(ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION);
        changesInTimeConventionIdForGeneratedCode = StringUtils.isEmpty(changesInTimeConventionIdForGeneratedCode)
                ? IChangesOverTimeNamingConvention.VAA
                : changesInTimeConventionIdForGeneratedCode;

        Element artefactEl = XmlUtil.getFirstElement(element, IIpsArtefactBuilderSet.XML_ELEMENT);
        if (artefactEl != null) {
            builderSetId = artefactEl.getAttribute("id"); //$NON-NLS-1$
            Element artefactConfigEl = XmlUtil.getFirstElement(artefactEl,
                    IIpsArtefactBuilderSetConfigModel.XML_ELEMENT);
            if (artefactConfigEl != null) {
                builderSetConfig = new IpsArtefactBuilderSetConfigModel();
                builderSetConfig.initFromXml(artefactConfigEl);
            }
        } else {
            builderSetId = ""; //$NON-NLS-1$
        }
        initProductCmptNamingStrategyFromXml(ipsProject,
                XmlUtil.getFirstElement(element, IProductCmptNamingStrategy.XML_TAG_NAME));
        Element pathEl = XmlUtil.getFirstElement(element, IpsObjectPathXmlPersister.XML_TAG_NAME);
        if (pathEl != null) {
            if (isUsingManifest(pathEl)) {
                createObjectPathFromManifest(ipsProject);
            } else {
                path = new IpsObjectPathXmlPersister().read(ipsProject, pathEl);
            }
        } else {
            path = new IpsObjectPath(ipsProject);
        }
        Element datatypesEl = XmlUtil.getFirstElement(element, "Datatypes"); //$NON-NLS-1$
        if (datatypesEl == null) {
            predefinedDatatypesUsed = new String[0];
            definedDatatypes.clear();
            return;
        }
        initUsedPredefinedDatatypesFromXml(XmlUtil.getFirstElement(datatypesEl, "UsedPredefinedDatatypes")); //$NON-NLS-1$
        initDefinedDatatypesFromXml(ipsProject, XmlUtil.getFirstElement(datatypesEl, "DatatypeDefinitions")); //$NON-NLS-1$
        initRequiredFeatures(XmlUtil.getFirstElement(element, "RequiredIpsFeatures")); //$NON-NLS-1$
        initResourcesExcludedFromProductDefinition(
                XmlUtil.getFirstElement(element, "ResourcesExcludedFromProductDefinition")); //$NON-NLS-1$
        initReleaseExtension(XmlUtil.getFirstElement(element, PRODUCT_RELEASE));
        initVersion(XmlUtil.getFirstElement(element, VERSION_TAG_NAME));
        initAdditionalSettings(element);
        initPersistenceOptions(element);
        initSupportedLanguages(element);
        initDefaultCurrency(element);
        initFeatureConfigurations(element);

        initCompatibilityMode(element);
    }

    private void initCompatibilityMode(Element element) {
        initCompatibilityModelProductRelease(XmlUtil.getFirstElement(element, PRODUCT_RELEASE_DEPRECATED));
    }

    private void initCompatibilityModelProductRelease(Element productReleaseDeprecated) {
        if (productReleaseDeprecated != null) {
            if (StringUtils.isEmpty(version)) {
                version = productReleaseDeprecated.getAttribute(VERSION_ATTRIBUTE);
            }
            if (StringUtils.isEmpty(releaseExtensionId)) {
                releaseExtensionId = productReleaseDeprecated.getAttribute(RELEASE_EXTENSION_ID_ATTRIBUTE);
            }
        }
    }

    private boolean isUsingManifest(Element pathEl) {
        String usingManifest = pathEl.getAttribute(IpsObjectPathXmlPersister.ATTRIBUTE_NAME_USE_MANIFEST);

        return Boolean.parseBoolean(usingManifest);
    }

    private void createObjectPathFromManifest(IIpsProject ipsProject) {
        IFile file = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);
        if (file.exists()) {
            createObjectPathFromExistingManifest(ipsProject, file);
        } else {
            path = new IpsObjectPath(ipsProject);
            path.setUsingManifest(true);
        }
    }

    private void createObjectPathFromExistingManifest(IIpsProject ipsProject, IFile file) {
        InputStream contents = null;
        try {
            contents = file.getContents();
            Manifest manifest = new Manifest(contents);
            IpsBundleManifest bundleManifest = new IpsBundleManifest(manifest);
            path = new IpsObjectPathManifestReader(bundleManifest, ipsProject).readIpsObjectPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        } finally {
            IoUtil.close(contents);
        }
    }

    private void initRequiredFeatures(Element el) {
        requiredFeatures = new HashMap<>();

        if (el == null) {
            return;
        }

        NodeList nl = el.getElementsByTagName("RequiredIpsFeature"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element child = (Element)nl.item(i);
            requiredFeatures.put(child.getAttribute("id"), child.getAttribute("minVersion")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private void initProductCmptNamingStrategyFromXml(IIpsProject ipsProject, Element el) {
        defaultCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategyFactory()
                .newProductCmptNamingStrategy(ipsProject);
        if (el != null) {
            productCmptNamingStrategyId = el.getAttribute("id"); //$NON-NLS-1$
            if (StringUtils.isEmpty(productCmptNamingStrategyId)) {
                return;
            }
            IProductCmptNamingStrategyFactory factory = ipsProject.getIpsModel().getCustomModelExtensions()
                    .getProductCmptNamingStrategyFactory(productCmptNamingStrategyId);
            if (factory != null) {
                productCmptNamingStrategy = factory.newProductCmptNamingStrategy(ipsProject);
                productCmptNamingStrategy.initFromXml(el);
            }
        }
    }

    private void initUsedPredefinedDatatypesFromXml(Element element) {
        if (element == null) {
            predefinedDatatypesUsed = new String[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype"); //$NON-NLS-1$
        predefinedDatatypesUsed = new String[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            predefinedDatatypesUsed[i] = ((Element)nl.item(i)).getAttribute("id"); //$NON-NLS-1$
        }
    }

    private void initDefinedDatatypesFromXml(IIpsProject ipsProject, Element element) {
        if (element == null) {
            definedDatatypes.clear();
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype"); //$NON-NLS-1$
        definedDatatypes = new ArrayList<>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element)nl.item(i);
            Datatype datatype = createDefinedDatatype(ipsProject, el);
            definedDatatypes.add(datatype);
        }
    }

    private Datatype createDefinedDatatype(IIpsProject ipsProject, Element element) {
        if (!element.hasAttribute("valueObject") //$NON-NLS-1$
                || Boolean.valueOf(element.getAttribute("valueObject")).booleanValue()) { //$NON-NLS-1$
            return DynamicValueDatatype.createFromXml(ipsProject, element);
        }
        String javaClass = element.getAttribute("javaClass"); //$NON-NLS-1$
        String qName = element.getAttribute("id"); //$NON-NLS-1$
        return new JavaClass2DatatypeAdaptor(qName, javaClass);
    }

    private void initResourcesExcludedFromProductDefinition(Element element) {
        if (element == null) {
            return;
        }
        NodeList nl = element.getElementsByTagName("Resource"); //$NON-NLS-1$
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Element child = (Element)nl.item(i);
            Attr resourcePath = child.getAttributeNode("path"); //$NON-NLS-1$
            if (resourcePath != null && StringUtils.isNotEmpty(resourcePath.getValue())) {
                resourcesPathExcludedFromTheProductDefiniton.add(resourcePath.getValue());
            }
        }
    }

    private void initReleaseExtension(Element productReleaseElement) {
        if (productReleaseElement == null) {
            return;
        }
        version = productReleaseElement.getAttribute(VERSION_ATTRIBUTE);
        releaseExtensionId = productReleaseElement.getAttribute(RELEASE_EXTENSION_ID_ATTRIBUTE);
        versionProviderId = productReleaseElement.getAttribute(VERSION_PROVIDER_ATTRIBUTE);
    }

    private void initVersion(Element element) {
        if (element == null) {
            return;
        }
        version = element.getAttribute(VERSION_ATTRIBUTE);
        versionProviderId = element.getAttribute(VERSION_PROVIDER_ATTRIBUTE);
    }

    /**
     * Reads the definition of optional constraints from the given &lt;IpsProject&gt; XML
     * <code>Element</code>.
     * 
     * @param element The &lt;IpsProject&gt; XML <code>Element</code>.
     */
    private void initAdditionalSettings(Element element) {
        // migration for 1.0 files
        if (element.hasAttribute("containerRelationIsImplementedRuleEnabled")) { //$NON-NLS-1$
            derivedUnionIsImplementedRuleEnabled = Boolean
                    .valueOf(element.getAttribute("containerRelationIsImplementedRuleEnabled")).booleanValue(); //$NON-NLS-1$
        }

        // migration for 2.0-rc files
        if (element.hasAttribute("derivedUnionIsImplementedRuleEnabled")) { //$NON-NLS-1$
            derivedUnionIsImplementedRuleEnabled = Boolean
                    .valueOf(element.getAttribute("derivedUnionIsImplementedRuleEnabled")).booleanValue(); //$NON-NLS-1$
        }

        // since 2.0: read from <AdditionalSettings> (and not from <OptionalConstraints> anymore)
        Element additionalSettingsEl = XmlUtil.getFirstElement(element, ADDITIONAL_SETTINGS_TAG_NAME);

        // migration for pre-2.0 files
        if (additionalSettingsEl == null) {
            return;
        }

        NodeList nl = additionalSettingsEl.getElementsByTagName(SETTING_TAG_NAME);
        int length = nl.getLength();

        IpsProjectPropertiesForOldVersion defaultForOld = new IpsProjectPropertiesForOldVersion();

        defaultForOld.add(SETTING_VALIDATE_IPS_SCHEMA,
                IpsProjectProperties::setValidateIpsSchema,
                false);

        for (int i = 0; i < length; ++i) {
            Element child = (Element)nl.item(i);
            if (isInvalidSettingElement(child)) {
                continue;
            }

            String name = child.getAttribute(SETTING_ATTRIBUTE_NAME);
            boolean enabled = isEnabledSetting(child);
            String value = child.getAttribute(SETTING_ATTRIBUTE_VALUE);

            applySetting(name, enabled, value);
            initFunctionsLanguageLocale(name, value);

            defaultForOld.checkIfFound(name);
        }

        defaultForOld.applyNewValue(this);

        if (!markerEnumsConfiguredInIpsProjectFile) {
            setMarkerEnumsEnabled(false);
        }
    }

    private boolean isInvalidSettingElement(Element child) {
        return !child.hasAttribute(SETTING_ATTRIBUTE_NAME)
                || !(child.hasAttribute(SETTING_ATTRIBUTE_VALUE) || child.hasAttribute(SETTING_ATTRIBUTE_ENABLED));
    }

    private boolean isEnabledSetting(Element child) {
        String enabledAttributeValue = child.getAttribute(SETTING_ATTRIBUTE_ENABLED);
        if (StringUtils.isEmpty(enabledAttributeValue)) {
            String value = child.getAttribute(SETTING_ATTRIBUTE_VALUE);
            // only if the value is 'false' we assume it is disabled. This is useful
            // for example for marker enums where you could skip the enabled attribute
            return !Boolean.FALSE.toString().equals(value);
        } else {
            return Boolean.valueOf(enabledAttributeValue).booleanValue();
        }
    }

    // CSOFF: CyclomaticComplexity
    // Complexity is high but it is a simple if-else-cascade
    private void applySetting(String name, boolean enabled, String value) {
        if (name.equals(SETTING_DERIVED_UNION_IS_IMPLEMENTED)) {
            derivedUnionIsImplementedRuleEnabled = enabled;
        } else if (name.equals(SETTING_REFERENCED_PRODUCT_COMPONENTS_ARE_VALID_ON_THIS_GENERATIONS_VALID_FROM_DATE)) {
            referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled = enabled;
        } else if (name.equals(SETTING_RULES_WITHOUT_REFERENCE)) {
            rulesWithoutReferencesAllowed = enabled;
        } else if (name.equals(SETTING_SHARED_ASSOCIATIONS)) {
            setSharedDetailToMasterAssociations(enabled);
        } else if (name.equals(SETTING_MARKER_ENUMS)) {
            setMarkerEnumsEnabled(enabled);
            initMarkerEnums(value);
            markerEnumsConfiguredInIpsProjectFile = true;
        } else if (name.equals(SETTING_BUSINESS_FUNCTIONS_FOR_VALIDATION_RULES)) {
            setBusinessFunctionsForValidationRules(enabled);
        } else if (name.equals(SETTING_CHANGING_OVER_TIME_DEFAULT)) {
            setChangingOverTimeDefault(enabled);
        } else if (name.equals(SETTING_GENERATE_VALIDATOR_CLASS_BY_DEFAULT)) {
            setGenerateValidatorClassDefault(enabled);
        } else if (name.equals(SETTING_GENERIC_VALIDATION_BY_DEFAULT)) {
            setGenericValidationDefault(enabled);
        } else if (name.equals(SETTING_ESCAPE_NON_STANDARD_BLANKS)) {
            setEscapeNonStandardBlanks(enabled);
        } else if (name.equals(SETTING_INFERRED_TEMPLATE_LINK_THRESHOLD)) {
            setInferredTemplateLinkThreshold(Decimal.valueOf(value));
        } else if (name.equals(SETTING_INFERRED_TEMPLATE_PROPERTY_VALUE_THRESHOLD)) {
            setInferredTemplatePropertyValueThreshold(Decimal.valueOf(value));
        } else if (name.equals(SETTING_DUPLICATE_PRODUCT_COMPONENT_SERVERITY)) {
            setDuplicateProductComponentSeverity(Severity.valueOf(value));
        } else if (name.contentEquals(SETTING_PERSISTENCE_COLUMN_SIZE_CHECKS_SERVERITY)) {
            setPersistenceColumnSizeChecksSeverity(Severity.valueOf(value));
        } else if (name.contentEquals(SETTING_TABLE_CONTENT_FORMAT)) {
            setTableContentFormat(TableContentFormat.valueById(value));
        } else if (name.contentEquals(SETTING_VALIDATE_IPS_SCHEMA)) {
            setValidateIpsSchema(enabled);
        }
    }

    // CSON: CyclomaticComplexity

    private void initMarkerEnums(String value) {
        getMarkerEnums().clear();
        if (!value.isEmpty()) {
            String[] splitString = value.split(MARKER_ENUMS_DELIMITER);
            for (String qualifiedName : splitString) {
                addMarkerEnum(qualifiedName.trim());
            }
        }
    }

    private void initFunctionsLanguageLocale(String name, String value) {
        if (name.equals(SETTING_FORMULA_LANGUAGE_LOCALE) && value != null) {
            formulaLanguageLocale = new Locale(value);
        }
    }

    private void initPersistenceOptions(Element element) {
        persistenceOptions = new PersistenceOptions(XmlUtil.getFirstElement(element, IPersistenceOptions.XML_TAG_NAME));
    }

    private void initSupportedLanguages(Element element) {
        Element supportedLanguagesEl = XmlUtil.getFirstElement(element, "SupportedLanguages"); //$NON-NLS-1$

        // Null if the project hasn't been migrated from version 3.0 to version 3.1 yet
        if (supportedLanguagesEl != null) {
            int childrenCount = supportedLanguagesEl.getElementsByTagName("SupportedLanguage").getLength(); //$NON-NLS-1$
            for (int i = 0; i < childrenCount; i++) {
                ISupportedLanguage supportedLanguage = new SupportedLanguage();
                Element childElement = XmlUtil.getElement(supportedLanguagesEl, i);
                supportedLanguage.initFromXml(childElement);
                supportedLanguages.add(supportedLanguage);
            }
        }
    }

    private void initDefaultCurrency(Element element) {
        Element defaultCurrencyElement = XmlUtil.getFirstElement(element, DEFAULT_CURRENCY_ELEMENT);
        if (defaultCurrencyElement == null) {
            // use default value;
            return;
        }
        String value = defaultCurrencyElement.getAttribute(DEFAULT_CURRENCY_VALUE_ATTR);
        defaultCurrency = readDefaultCurrency(value);
    }

    private Currency readDefaultCurrency(String value) {
        try {
            return Currency.getInstance(value);
        } catch (IllegalArgumentException e) {
            return defaultCurrency;
        }
    }

    private void initFeatureConfigurations(Element element) {
        Element featureConfigurationsElement = XmlUtil.getFirstElement(element, FEATURE_CONFIGURATIONS_ELEMENT);
        if (featureConfigurationsElement != null) {
            featureConfigurations.clear();
            NodeList featureConfigurationElements = featureConfigurationsElement
                    .getElementsByTagName(IpsFeatureConfiguration.FEATURE_CONFIGURATION_ELEMENT);
            for (int i = 0; i < featureConfigurationElements.getLength(); i++) {
                Element featureConfigurationElement = (Element)featureConfigurationElements.item(i);
                String featureId = featureConfigurationElement.getAttribute(FEATURE_ID_ATTRIBUTE);
                IpsFeatureConfiguration featureConfiguration = new IpsFeatureConfiguration();
                featureConfiguration.initFromXml(featureConfigurationElement);
                setFeatureConfiguration(featureId, featureConfiguration);
            }
        }
    }

    private void writeDefinedDataTypesToXML(Document doc, Element parent) {
        for (Datatype datatype : definedDatatypes) {
            Element datatypeEl = doc.createElement("Datatype"); //$NON-NLS-1$
            if (datatype.getQualifiedName() != null) {
                datatypeEl.setAttribute("id", datatype.getQualifiedName()); //$NON-NLS-1$
            }
            if (datatype instanceof DynamicValueDatatype) {
                datatypeEl.setAttribute("valueObject", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                ((DynamicValueDatatype)datatype).writeToXml(datatypeEl);
            } else {
                JavaClass2DatatypeAdaptor javaClassDatatype = (JavaClass2DatatypeAdaptor)datatype;
                datatypeEl.setAttribute("valueObject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
                datatypeEl.setAttribute("javaClass", javaClassDatatype.getJavaClassName()); //$NON-NLS-1$
            }
            parent.appendChild(datatypeEl);
        }
    }

    @Override
    public void addDefinedDatatype(IDynamicValueDatatype newDatatype) {
        addDefinedDatatype((Datatype)newDatatype);
    }

    @Override
    public void addDefinedDatatype(Datatype newDatatype) {
        /* replace, if Datatype already registered */
        for (int i = 0; i < definedDatatypes.size(); i++) {
            if (definedDatatypes.get(i).getQualifiedName() != null && newDatatype.getQualifiedName() != null
                    && definedDatatypes.get(i).getQualifiedName().equals(newDatatype.getQualifiedName())) {
                definedDatatypes.set(i, newDatatype);
                return;
            }
        }
        definedDatatypes.add(newDatatype);
    }

    @Override
    public String getRuntimeIdPrefix() {
        return runtimeIdPrefix;
    }

    @Override
    public void setRuntimeIdPrefix(String runtimeIdPrefix) {
        if (runtimeIdPrefix == null) {
            throw new NullPointerException("RuntimeIdPrefix can not be null"); //$NON-NLS-1$
        }
        this.runtimeIdPrefix = runtimeIdPrefix;
    }

    @Override
    public boolean isDerivedUnionIsImplementedRuleEnabled() {
        return derivedUnionIsImplementedRuleEnabled;
    }

    @Override
    public void setDerivedUnionIsImplementedRuleEnabled(boolean enabled) {
        derivedUnionIsImplementedRuleEnabled = enabled;
    }

    @Override
    public boolean isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled() {
        return referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled;
    }

    @Override
    public boolean isRulesWithoutReferencesAllowedEnabled() {
        return rulesWithoutReferencesAllowed;
    }

    @Override
    public void setRulesWithoutReferencesAllowedEnabled(boolean enabled) {
        rulesWithoutReferencesAllowed = enabled;
    }

    @Override
    public void setSharedDetailToMasterAssociations(boolean sharedDetailToMasterAssociations) {
        this.sharedDetailToMasterAssociations = sharedDetailToMasterAssociations;
    }

    @Override
    public boolean isSharedDetailToMasterAssociations() {
        return sharedDetailToMasterAssociations;
    }

    @Override
    public void setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(boolean enabled) {
        referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled = enabled;
    }

    // @formatter:off
    private void createIpsProjectDescriptionComment(Node parentEl) {
        String s = "This XML file contains the properties of the enclosing IPS project. It contains the following " + System.lineSeparator() //$NON-NLS-1$
                + " information:" + System.lineSeparator() //$NON-NLS-1$
                + "The generator used to transform the model to Java sourcecode and the product definition into the runtime format." + System.lineSeparator() //$NON-NLS-1$
                + "The path where to search for model and product definition files. This is basically the same concept as the  Java " + System.lineSeparator() //$NON-NLS-1$
                + "classpath. A strategy that defines how to name product components and what names are valid." + System.lineSeparator() //$NON-NLS-1$
                + "The datatypes that can be used in the model. Datatypes used in the model fall into two categeories:" + System.lineSeparator() //$NON-NLS-1$
                + " * Predefined datatype" + System.lineSeparator() //$NON-NLS-1$
                + "   Predefined datatypes are defined by the datatype definition extension. Faktor-IPS predefines datatypes for" + System.lineSeparator() //$NON-NLS-1$
                + "   the standard Java classes like Boolean, String, Integer, etc. and some additional types, for example Money." + System.lineSeparator() //$NON-NLS-1$
                + "   You can add you own datatype be providing an extension and then use it from every IPS project." + System.lineSeparator() //$NON-NLS-1$
                + " * User defined datatype (or dynamic datatype)" + System.lineSeparator() //$NON-NLS-1$
                + "   If you want to use a Java class that represents a value as datatype, but do not want to provide an extension for" + System.lineSeparator() //$NON-NLS-1$
                + "   it, you can register this class as datatype in this file. See the details in the description of the datatype " + System.lineSeparator() //$NON-NLS-1$
                + "   section below how to register the class. Naturally, the class must be available via the project's Java classpath." + System.lineSeparator() //$NON-NLS-1$
                + "   It is strongly recommended to provide the class via a JAR file or in a separate Java project." + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<IpsProject>" + System.lineSeparator() //$NON-NLS-1$
                + "    productDefinitionProject                           True if this project contains elements of the product definition." + System.lineSeparator() //$NON-NLS-1$
                + "    modelProject                                       True if this project contains the model or part of it." + System.lineSeparator() //$NON-NLS-1$
                + "    runtimeIdPrefix                                    " + System.lineSeparator() //$NON-NLS-1$
                + "    " + ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION + "                      Specifies the naming conventions for changes in time that " + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "                                                       are used throughout the system. Possible values are VAA and PM" + System.lineSeparator() //$NON-NLS-1$
                + "    <IpsArtefactBuilderSet/>                           The generator used. Details below." + System.lineSeparator() //$NON-NLS-1$
                + "    <IpsObjectPath/>                                   The object path to search for model and product definition" + System.lineSeparator() //$NON-NLS-1$
                + "                                                       objects. Details below." + System.lineSeparator() //$NON-NLS-1$
                + "    <ProductCmptNamingStrategy/>                       The strategy used for product component names. Details below." + System.lineSeparator() //$NON-NLS-1$
                + "    <Datatypes/>                                       The datatypes used in the model. Details below." + System.lineSeparator() //$NON-NLS-1$
                + "    <OptionalConstraints/>                             Definition of optional constraints. Details below." + System.lineSeparator() //$NON-NLS-1$
                + "    <SupportedLanguages/>                              List of supported natural languages. Details below." + System.lineSeparator()  //$NON-NLS-1$
                + "</IpsProject>" + System.lineSeparator(); //$NON-NLS-1$
        createDescriptionComment(s, parentEl, "    "); //$NON-NLS-1$
    }
    // @formatter:on

    // @formatter:off
    private void createProductCmptNamingStrategyDescriptionComment(Element parentEl) {
        String s = "Product Component Naming Strategy" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "The naming strategy defines the structure of product component names and how characters that are not allowed" + System.lineSeparator() //$NON-NLS-1$
                + "in Java identifiers are replaced by the code generator. In order to deal with different versions of " + System.lineSeparator() //$NON-NLS-1$
                + "a product you need a strategy to derive the version from the product component name. " + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "Currently Faktor-IPS includes the following strategy:" + System.lineSeparator() //$NON-NLS-1$
                + " * DateBasedProductCmptNamingStrategy" + System.lineSeparator() //$NON-NLS-1$
                + "   The product component name is made up of a \"unversioned\" name and a date format for the version id." + System.lineSeparator() //$NON-NLS-1$
                + "   <ProductCmptNamingStrategy id=\"org.faktorips.devtools.core.DateBasedProductCmptNamingStrategy\">" + System.lineSeparator() //$NON-NLS-1$
                + "       <DateBasedProductCmptNamingStrategy " + System.lineSeparator() //$NON-NLS-1$
                + "           dateFormatPattern=\"yyyy-MM\"                           Format of the version id according to" + System.lineSeparator() //$NON-NLS-1$
                + "                                                                   java.text.DateFormat" + System.lineSeparator() //$NON-NLS-1$
                + "           postfixAllowed=\"true\"                                 True if the date format can be followed by" + System.lineSeparator() //$NON-NLS-1$
                + "                                                                   an optional postfix." + System.lineSeparator() //$NON-NLS-1$
                + "           versionIdSeparator=\" \">                               The separator between \"unversioned name\"" + System.lineSeparator() //$NON-NLS-1$
                + "                                                                   and version id." + System.lineSeparator() //$NON-NLS-1$
                + "           <JavaIdentifierCharReplacements>                        Definition replacements for charcacters invalid " + System.lineSeparator() //$NON-NLS-1$
                + "                                                                   in Java identifiers." + System.lineSeparator() //$NON-NLS-1$
                + "               <Replacement replacedChar=\" \" replacement=\"___\"/> Example: Replace Blank with three underscores" + System.lineSeparator() //$NON-NLS-1$
                + "               <Replacement replacedChar=\"-\" replacement=\"__\"/>  Example: Replace Hyphen with two underscores" + System.lineSeparator() //$NON-NLS-1$
                + "           </JavaIdentifierCharReplacements>" + System.lineSeparator() //$NON-NLS-1$
                + "       </DateBasedProductCmptNamingStrategy>" + System.lineSeparator() //$NON-NLS-1$
                + "    </ProductCmptNamingStrategy>" + System.lineSeparator(); //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    // @formatter:off
    private void createDatatypeDescriptionComment(Node parentEl) {
        String s = "Datatypes" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "In the datatypes section the value datatypes allowed in the model are defined." + System.lineSeparator() //$NON-NLS-1$
                + "See also the discussion at the top this file." + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<UsedPredefinedDatatypes>" + System.lineSeparator() //$NON-NLS-1$
                + "    <Datatype id=\"Money\"\\>                                 The id of the datatype that should be used." + System.lineSeparator() //$NON-NLS-1$
                + "</UsedPredefinedDatatypes>" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<DatatypeDefinitions>" + System.lineSeparator() //$NON-NLS-1$
                + "    <Datatype id=\"PaymentMode\"                             The datatype's id used in the model to refer to it." + System.lineSeparator() //$NON-NLS-1$
                + "        javaClass=\"org.faktorips.sample.PaymentMode\"       The Java class the datatype represents" + System.lineSeparator() //$NON-NLS-1$
                + "        valueObject=\"true|false\"                           True indicates this is a value object (according to the value object pattern.) " + System.lineSeparator() //$NON-NLS-1$
                + "        --- the following attributes are only needed for value objects ---" + System.lineSeparator() //$NON-NLS-1$
                + "        isEnumType=\"true|false\"                            True if this is an enumeration of values." + System.lineSeparator() //$NON-NLS-1$
                + "        valueOfMethod=\"getPaymentMode\"                     Name of the method that takes a String and returns an" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             object instance/value." + System.lineSeparator() //$NON-NLS-1$
                + "        isParsableMethod=\"isPaymentMode\"                   Name of the method that evaluates if a given string" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             can be parsed to an instance." + System.lineSeparator() //$NON-NLS-1$
                + "        valueToStringMethod=\"toString\"                     Name of the method that transforms an object instance" + System.lineSeparator() //$NON-NLS-1$
                + "                                                              to a String (that can be parsed via the valueOfMethod)" + System.lineSeparator() //$NON-NLS-1$
                + "        getAllValuesMethod=\"getAllPaymentModes\"            For enums only: The name of the method that returns all values" + System.lineSeparator() //$NON-NLS-1$
                + "        isSupportingNames=\"true\"                           For enums only: True indicates that a string" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             representation for the user other than the one defined by the valueToStringMethod exists." + System.lineSeparator() //$NON-NLS-1$
                + "        getNameMethod=\"getName\">                           For enums only: The name of the method that returns" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             the string representation for the user, if" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             isSupportingNames=true" + System.lineSeparator() //$NON-NLS-1$
                + "        <NullObjectId isNull=\"false\">n</NullObjectId>      Marks a value as a NullObject. This has to be used," + System.lineSeparator() //$NON-NLS-1$
                + "                                                             if the Java class implements the null object pattern," + System.lineSeparator() //$NON-NLS-1$"
                + "                                                             otherwise omitt this element. The element's text" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             defines the null object's id. Calling the valueOfMethod" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             with this name must return the null object instance. If" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             the null object's id is null, leave the text empty" + System.lineSeparator() //$NON-NLS-1$
                + "                                                             and set the isNull attribute to true." + System.lineSeparator() //$NON-NLS-1$
                + "    </Datatype>" + System.lineSeparator() //$NON-NLS-1$
                + "</DatatypeDefinitions>" + System.lineSeparator(); //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    // @formatter:off
    private void createIpsArtefactBuilderSetDescriptionComment(Node parentEl) {
        String s = "Artefact builder set" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "In this section the artefact builder set (code generator) is defined. Faktor-IPS comes with a standard builder set." + System.lineSeparator() //$NON-NLS-1$
                + "However the build and generation mechanism is completly decoupled from the modeling and product definition capabilities" + System.lineSeparator() //$NON-NLS-1$
                + "and you can write your own builders and generators. A different builder set is defined by providing an extension for" + System.lineSeparator() //$NON-NLS-1$
                + "the extension point \"org.faktorips.devtools.core.artefactbuilderset\" defined by Faktor-IPS plugin" + System.lineSeparator() //$NON-NLS-1$
                + "A builder set is activated for an IPS project by defining the IpsArtefactBuilderSet tag. The attribute \"id\" specifies" + System.lineSeparator() //$NON-NLS-1$
                + "the builder set implementation that is registered as an extension. Note: The unique identifier of the extension is to specify." + System.lineSeparator() //$NON-NLS-1$
                + "<IpsArtefactBuilderSet id=\"org.faktorips.devtools.stdbuilder.ipsstdbuilderset\"/> A builder set can be configured by specifing a " + System.lineSeparator()//$NON-NLS-1$
                + "nested tag <IpsArtefactBuilderSetConfig/>. A configuration contains a set of properties which are specified by nested" + System.lineSeparator()//$NON-NLS-1$
                + "<Property name=\"\" value=\"\"/> tags. The possible properties and their values is specific to the selected builder set." + System.lineSeparator()//$NON-NLS-1$
                + "The initially generated .ipsproject file contains the set of possible configuration properties for the selected builder set" + System.lineSeparator()//$NON-NLS-1$
                + "including their descriptions." + System.lineSeparator(); //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    // @formatter:off
    private void createRequiredIpsFeaturesComment(Node parentEl) {
        String s = "Required Ips-Features" + System.lineSeparator() + " " + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "In this section, all required features are listed with the minimum version for these features." + System.lineSeparator() //$NON-NLS-1$
                + "By default, the feature with id \"org.faktorips.feature\" is always required (because this is the core " + System.lineSeparator() //$NON-NLS-1$
                + "feature of Faktor-IPS. Other features can be required if plugins providing extensions for any extension points" + System.lineSeparator() //$NON-NLS-1$
                + "defined by Faktor-IPS are used." + System.lineSeparator() //$NON-NLS-1$
                + "If a required feature is missing or a required feature has a version less than the minimum version number " + System.lineSeparator() //$NON-NLS-1$
                + "this project will not be build (an error is created)." + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<RequiredIpsFeatures>" + System.lineSeparator() //$NON-NLS-1$
                + "    <RequiredIpsFeature id=\"org.faktorips.feature\"    The id of the required feature." + System.lineSeparator() //$NON-NLS-1$
                + "        minVersion=\"0.9.38\"                           The minimum version number of this feature" + System.lineSeparator() //$NON-NLS-1$
                + "</RequiredIpsFeatures>" + System.lineSeparator() //$NON-NLS-1$
                + ""; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    // @formatter:off
    private void createResourcesExcludedFromProductDefinitionComment(Node parentEl) {
        String s = "Resources excluded from the product definition" + System.lineSeparator() + " " + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "In this section, all resources which will be excluded (hidden) in the product definition are listed." + System.lineSeparator() //$NON-NLS-1$
                + "The resource must be identified by its full path, relative to the project the resource belongs to." + System.lineSeparator() //$NON-NLS-1$
                + "" + System.lineSeparator() + " " + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "<ResourcesExcludedFromProductDefinition>" + System.lineSeparator() //$NON-NLS-1$
                + "    <Resource path=\"src\"/>" + "              Example: The 1st excluded resource, identified by its path." + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "    <Resource path=\"build/build.xml\"/>" + "  Example: The 2nd excluded resource, identified by its path." + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "</ResourcesExcludedFromProductDefinition>" + System.lineSeparator() //$NON-NLS-1$
                + ""; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    private void createProductReleaseComment(Element parentEl) {
        String s = "Product Release" + System.lineSeparator() + " " + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "In this section, the product defintion release is configured. You could reference an release extension" //$NON-NLS-1$
                + System.lineSeparator()
                + "by specifying the releaseExtensionId. This extension is used by the release builder wizard." //$NON-NLS-1$
                + System.lineSeparator()
                + "The version for the latest release is configured in a separate element below." //$NON-NLS-1$
                + "The version of the latest release is also configured in this element. If you use the release builder wizard" //$NON-NLS-1$
                + System.lineSeparator()
                + "you should not set this version manually but using the release builder wizard." //$NON-NLS-1$
                + System.lineSeparator() + " " + System.lineSeparator() //$NON-NLS-1$
                + "<" + PRODUCT_RELEASE + " " + RELEASE_EXTENSION_ID_ATTRIBUTE + "=\"id-of-the-extension\"" + "/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + System.lineSeparator();
        createDescriptionComment(s, parentEl);
    }

    // @formatter:off
    private void createVersionComment(Element parentEl) {
        String s = "Version" + System.lineSeparator() + " " + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "In this section, the version for this project is specified. In alternativ to directly see a version" //$NON-NLS-1$
                + System.lineSeparator()
                + "it is possible to configure a version provider." //$NON-NLS-1$
                + System.lineSeparator()
                + "Examples:" + System.lineSeparator()//$NON-NLS-1$
                + "<" + VERSION_TAG_NAME + " " + VERSION_ATTRIBUTE + "=\"1.2.3\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "or" + System.lineSeparator() //$NON-NLS-1$
                + "<" + VERSION_TAG_NAME + " " + VERSION_PROVIDER_ATTRIBUTE + "=\"org.faktorips.devtools.core.bundleVersionProvider\"/>" //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator();
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    private void createAdditionalSettingsDescriptionComment(Node parentEl) {
        // @formatter:off
        String s = ADDITIONAL_SETTINGS_TAG_NAME + System.lineSeparator()
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "Some of the settings defined in the Faktor-IPS metamodel are optional." + System.lineSeparator() //$NON-NLS-1$
                + "In this section you can enable or disable these additional settings." + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<" + ADDITIONAL_SETTINGS_TAG_NAME + ">" + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$
                + "    <!-- True if Faktor-IPS checks if all derived unions are implemented in none abstract classes. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"true\"" + " name=\"" + SETTING_DERIVED_UNION_IS_IMPLEMENTED + "\"/>" + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "    <!-- True if Faktor-IPS checks if referenced product components are valid on the effective date " + System.lineSeparator() //$NON-NLS-1$
                + "        of the referencing product component generation. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"true\"" + " name=\"" + SETTING_REFERENCED_PRODUCT_COMPONENTS_ARE_VALID_ON_THIS_GENERATIONS_VALID_FROM_DATE + "\"/>" + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "    <!-- True to allow rules without references -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"true\"" + " name=\"" + SETTING_RULES_WITHOUT_REFERENCE + "\"/>" + System.lineSeparator() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "    <!-- True to allow shared associations. Shared associations are detail-to-master associationis that can be used" + System.lineSeparator() //$NON-NLS-1$
                + "        by multiple master-to-detail associations-->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"true\"" + " name=\"" + SETTING_SHARED_ASSOCIATIONS + "\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + System.lineSeparator()
                + "    <!-- Set the language in which the expression language's functions are used. E.g. the 'if' function is called IF in English, but WENN in German." + System.lineSeparator() //$NON-NLS-1$
                + "        Only English (en) and German (de) are supported at the moment. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_FORMULA_LANGUAGE_LOCALE + "\" value=\"en\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Represents the qualified name of the marker enums seperated by \";\". For further processing only the first entered qualified name will be considered -->" + System.lineSeparator() //$NON-NLS-1$
                + "        True to allow usage of marker enums. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"true\"" + " name=\"" + SETTING_MARKER_ENUMS + "\" value=\"markerEnumName\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + System.lineSeparator()
                + "    <!-- True to allow business functions for validation rules. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"true\"" + " name=\"" + SETTING_BUSINESS_FUNCTIONS_FOR_VALIDATION_RULES + "\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + System.lineSeparator()
                + "    <!-- False to set the default state of changing over time flag on product component types to disabled. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " enabled=\"false\"" + " name=\"" + SETTING_CHANGING_OVER_TIME_DEFAULT + "\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + System.lineSeparator()
                + "    <!-- When inferring a template from multiple product components, Faktor-IPS checks whether a link is used in all or at least many of those product components." + System.lineSeparator() //$NON-NLS-1$
                + "        This setting determines, which ratio is considered 'many'. The default value of 1.0 only considers links used by all selected product components.-->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_INFERRED_TEMPLATE_LINK_THRESHOLD + "\" value=\"1.0\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- When inferring a template from multiple product components, Faktor-IPS checks whether a property value is used in all or at least many of those product components." + System.lineSeparator() //$NON-NLS-1$
                + "        This setting determines, which ratio is considered 'many'. The default value of 0.8 only considers values used in at least 80% of all selected product components.-->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_INFERRED_TEMPLATE_PROPERTY_VALUE_THRESHOLD + "\" value=\"0.8\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Severity for validation messages when two product components have the same kindId and versionId." + System.lineSeparator() //$NON-NLS-1$
                + "        Possible values are ERROR, WARNING, INFO, and NONE. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_DUPLICATE_PRODUCT_COMPONENT_SERVERITY + "\" value=\"ERROR\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Severity for validation messages when model and persistence constraints don't match." + System.lineSeparator() //$NON-NLS-1$
                + "        Possible values are ERROR, WARNING, INFO, and NONE. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_PERSISTENCE_COLUMN_SIZE_CHECKS_SERVERITY + "\" value=\"ERROR\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Format for table content data." + System.lineSeparator() //$NON-NLS-1$
                + "        Possible values are XML and CSV, where XML is the Faktor-IPS standard and CSV is a more compact form optimised for large tables." + System.lineSeparator() //$NON-NLS-1$
                + "        Changing this setting will only affect tables when they are saved the next time. -->" + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_TABLE_CONTENT_FORMAT + "\" value=\"XML\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Default value for 'generate validation class' property on new PolicyCmptTypes." + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_GENERATE_VALIDATOR_CLASS_BY_DEFAULT + "\" value=\"true\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Whether non-standard blanks such as the non-breaking space should be escaped as XML entities when writing XML files." + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_ESCAPE_NON_STANDARD_BLANKS + "\" value=\"false\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                + "    <!-- Whether Ips-Files should be validated against their XSD schema." + System.lineSeparator() //$NON-NLS-1$
                + "    <" + SETTING_TAG_NAME + " name=\"" + SETTING_VALIDATE_IPS_SCHEMA + "\" value=\"true\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator()
                //
                // Check if the inverse associations have to be type safe or not. Due to Issue
                // FIPS-85 we need to have to possibility to use the inverse association of the super type as
                // inverse association for a concrete type. When this property is false, these unsafe inverse
                // associations are allowed. Otherwise if this property is true you have to create a concrete
                // inverse association for every subset of a derived union with an inverse association.

                + "</" + ADDITIONAL_SETTINGS_TAG_NAME + ">" + System.lineSeparator(); //$NON-NLS-1$ //$NON-NLS-2$
        createDescriptionComment(s, parentEl);
        // @formatter:on
    }

    // @formatter:off
    private void createPersistenceOptionsDescriptionComment(Node parentEl) {
        String s = "PersistenceOptions" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "In this section you can adjust parameters relating to the persistence of IPolicyCmptTypes." + System.lineSeparator() //$NON-NLS-1$
                + "The table and column naming strategies define how identifier names are transformed into" + System.lineSeparator() //$NON-NLS-1$
                + "database table and column names. The attributes maxTableNameLength and maxColumnNameLength" + System.lineSeparator() //$NON-NLS-1$
                + "constrain the maximum possible length of a table or column name." + System.lineSeparator() //$NON-NLS-1$
                + "The attribute maxTableColumnScale limits the scale of columns representing floating point" + System.lineSeparator() //$NON-NLS-1$
                + "numbers while maxTableColumnPrecision limits their precision. The number of characters in a" + System.lineSeparator() //$NON-NLS-1$
                + "String column is limited by maxTableColumnSize." + System.lineSeparator() //$NON-NLS-1$
                + "All limits are in byte length but are only validated in character length in Faktor-IPS, as the" + System.lineSeparator() //$NON-NLS-1$
                + "mapping of multi-byte characters depends on the encoding and database used." + System.lineSeparator() //$NON-NLS-1$
                + "The attribute " //$NON-NLS-1$
                + IPersistenceOptions.ALLOW_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS
                + " defines if it is allowed to use lazy fetching " + System.lineSeparator() //$NON-NLS-1$
                + "on the association side which holds a single value (to-one relationship side)." + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<PersistenceOptions maxColumnNameLength=\"30\" maxTableNameLength=\"30\"" + System.lineSeparator() //$NON-NLS-1$
                + "        maxTableColumnPrecision=\"31\"  maxTableColumnScale=\"31\" maxTableColumnSize=\"1000\"" + System.lineSeparator() //$NON-NLS-1$
                + "        allowLazyFetchForSingleValuedAssociations=\"true\">" + System.lineSeparator() //$NON-NLS-1$
                + "    <TableNamingStrategy id=\"org.faktorips.devtools.model.CamelCaseToUpperUnderscoreTableNamingStrategy\"/>" + System.lineSeparator() //$NON-NLS-1$
                + "    <TableColumnNamingStrategy id=\"org.faktorips.devtools.model.CamelCaseToUpperUnderscoreColumnNamingStrategy\"/>" + System.lineSeparator() //$NON-NLS-1$
                + "</PersistenceOptions>" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "Currently Faktor-IPS includes the strategies CamelCaseToUpperUnderscoreTableNamingStrategy" + System.lineSeparator() //$NON-NLS-1$
                + "for tables and CamelCaseToUpperUnderscoreColumnNamingStrategy for columns, example:" + System.lineSeparator() //$NON-NLS-1$
                + "    IdentifierName1 -> IDENTIFIER_NAME1" + System.lineSeparator(); //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    // @formatter:off
    private void createSupportedLanguagesDescriptionComment(Node parentEl) {
        String s = "Supported Languages" + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "This section lists all natural languages that are supported by this IPS project." //$NON-NLS-1$
                + System.lineSeparator()
                + "Each language is identified by it's locale which is the ISO 639 language code, " //$NON-NLS-1$
                + System.lineSeparator()
                + "e.g. 'en' for English." //$NON-NLS-1$
                + System.lineSeparator()
                + "Exactly one supported language must be marked as default language. The default language " + System.lineSeparator() //$NON-NLS-1$
                + "will be used if a language is requested that is not supported by this IPS project." + System.lineSeparator() //$NON-NLS-1$
                + " " + System.lineSeparator() //$NON-NLS-1$
                + "<SupportedLanguages>" + System.lineSeparator() //$NON-NLS-1$
                + "    <SupportedLanguage locale=\"en\" defaultLanguage=\"true\"/>" + System.lineSeparator() //$NON-NLS-1$
                + "    <SupportedLanguage locale=\"de\"/>" + System.lineSeparator() //$NON-NLS-1$
                + "</SupportedLanguages>" + System.lineSeparator(); //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }
    // @formatter:on

    private void createDescriptionComment(String text, Node parent) {
        createDescriptionComment(text, parent, "        "); //$NON-NLS-1$
    }

    private void createDescriptionComment(String text, Node parent, String indentation) {
        StringBuilder indentedText = new StringBuilder();
        indentedText.append(System.lineSeparator());
        StringTokenizer tokenizer = new StringTokenizer(text, System.lineSeparator());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            indentedText.append(indentation);
            indentedText.append(token);
            indentedText.append(System.lineSeparator());
        }
        indentedText.append(indentation.substring(4));
        Document doc = parent.getOwnerDocument();
        if (doc == null) {
            doc = (Document)parent;
        }
        Comment comment = doc.createComment(indentedText.toString());
        parent.appendChild(comment);
    }

    @Override
    public IIpsArtefactBuilderSetConfigModel getBuilderSetConfig() {
        return builderSetConfig;
    }

    @Override
    public void setBuilderSetConfig(IIpsArtefactBuilderSetConfigModel config) {
        ArgumentCheck.notNull(config);
        builderSetConfig = config;
    }

    @Override
    public String[] getRequiredIpsFeatureIds() {
        return requiredFeatures.keySet().toArray(new String[requiredFeatures.size()]);
    }

    @Override
    public String getMinRequiredVersionNumber(String featureId) {
        return requiredFeatures.get(featureId);
    }

    @Override
    public void setMinRequiredVersionNumber(String featureId, String version) {
        requiredFeatures.put(featureId, version);
    }

    @Override
    public void addResourcesPathExcludedFromTheProductDefiniton(String resourcesPath) {
        resourcesPathExcludedFromTheProductDefiniton.add(resourcesPath);
    }

    @Override
    public boolean isResourceExcludedFromProductDefinition(String location) {
        return resourcesPathExcludedFromTheProductDefiniton.contains(location);
    }

    @Override
    public Set<String> getResourcesPathExcludedFromTheProductDefiniton() {
        return resourcesPathExcludedFromTheProductDefiniton;
    }

    @Override
    public void setResourcesPathExcludedFromTheProductDefiniton(
            Set<String> resourcesPathExcludedFromTheProductDefiniton) {
        this.resourcesPathExcludedFromTheProductDefiniton = resourcesPathExcludedFromTheProductDefiniton;
    }

    @Override
    public long getLastPersistentModificationTimestamp() {
        return lastPersistentModificationTimestamp;
    }

    @Override
    public void setLastPersistentModificationTimestamp(long timestamp) {
        lastPersistentModificationTimestamp = timestamp;
    }

    @Override
    public boolean isPersistenceSupportEnabled() {
        return persistentProject;
    }

    @Override
    public void setPersistenceSupport(boolean persistentProject) {
        this.persistentProject = persistentProject;
    }

    @Override
    public IPersistenceOptions getPersistenceOptions() {
        return persistenceOptions;
    }

    @Override
    public ITableColumnNamingStrategy getTableColumnNamingStrategy() {
        return getPersistenceOptions().getTableColumnNamingStrategy();
    }

    @Override
    public ITableNamingStrategy getTableNamingStrategy() {
        return getPersistenceOptions().getTableNamingStrategy();
    }

    @Override
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy) {
        getPersistenceOptions().setTableColumnNamingStrategy(newStrategy);
    }

    @Override
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy) {
        getPersistenceOptions().setTableNamingStrategy(newStrategy);
    }

    public void setPersistenceOptions(IPersistenceOptions persistenceOptions) {
        this.persistenceOptions = persistenceOptions;
    }

    @Override
    public Set<ISupportedLanguage> getSupportedLanguages() {
        return Collections.unmodifiableSet(supportedLanguages);
    }

    @Override
    public ISupportedLanguage getSupportedLanguage(Locale locale) {
        ArgumentCheck.notNull(locale);
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            if (supportedLanguage.getLocale().equals(locale)) {
                return supportedLanguage;
            }
        }
        return null;
    }

    @Override
    public boolean isSupportedLanguage(Locale locale) {
        ArgumentCheck.notNull(locale);
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            if (supportedLanguage.getLocale().getLanguage().equals(locale.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ISupportedLanguage getDefaultLanguage() {
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            if (supportedLanguage.isDefaultLanguage()) {
                return supportedLanguage;
            }
        }
        if (!supportedLanguages.isEmpty()) {
            return supportedLanguages.iterator().next();
        }
        return new SupportedLanguage(IIpsModel.get().getMultiLanguageSupport().getLocalizationLocale(), true);
    }

    @Override
    public void setDefaultLanguage(Locale locale) {
        ISupportedLanguage language = getSupportedLanguage(locale);
        if (language == null) {
            throw new IllegalArgumentException("There is no supported language with the locale '" + locale + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
        setDefaultLanguage(language);
    }

    @Override
    public void setDefaultLanguage(ISupportedLanguage language) {
        clearDefaultLanguage();
        ((SupportedLanguage)language).setDefaultLanguage(true);
    }

    private void clearDefaultLanguage() {
        ISupportedLanguage currentDefaultLanguage = getDefaultLanguage();
        ((SupportedLanguage)currentDefaultLanguage).setDefaultLanguage(false);
    }

    @Override
    public void addSupportedLanguage(Locale locale) {
        ArgumentCheck.notNull(locale);
        supportedLanguages.add(new SupportedLanguage(locale));
    }

    @Override
    public void removeSupportedLanguage(ISupportedLanguage supportedLanguage) {
        ArgumentCheck.notNull(supportedLanguage);
        supportedLanguages.remove(supportedLanguage);
    }

    @Override
    public void removeSupportedLanguage(Locale locale) {
        ISupportedLanguage language = getSupportedLanguage(locale);
        if (language != null) {
            supportedLanguages.remove(language);
        }
    }

    @Override
    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersionProviderId() {
        return versionProviderId;
    }

    @Override
    public void setVersionProviderId(String versionProviderId) {
        this.versionProviderId = versionProviderId;
    }

    @Override
    public String getReleaseExtensionId() {
        return releaseExtensionId;
    }

    @Override
    public void setReleaseExtensionId(String releaseExtensionId) {
        this.releaseExtensionId = releaseExtensionId;
    }

    @Override
    public boolean isActive(IFunctionResolverFactory<?> factory) {
        if (factory instanceof AssociationNavigationFunctionsResolver) {
            return false;
        }
        return true;
    }

    @Override
    public Locale getFormulaLanguageLocale() {
        return formulaLanguageLocale;
    }

    @Override
    public void setFormulaLanguageLocale(Locale locale) {
        formulaLanguageLocale = locale;
    }

    @Override
    public boolean isBusinessFunctionsForValidationRulesEnabled() {
        return businessFunctionsForValidationRules;
    }

    @Override
    public void setBusinessFunctionsForValidationRules(boolean enabled) {
        businessFunctionsForValidationRules = enabled;
    }

    @Override
    public boolean isChangingOverTimeDefaultEnabled() {
        return changingOverTimeDefault;
    }

    @Override
    public void setChangingOverTimeDefault(boolean enabled) {
        changingOverTimeDefault = enabled;
    }

    @Override
    public boolean isGenerateValidatorClassDefaultEnabled() {
        return generateValidatorClassByDefault;
    }

    @Override
    public void setGenerateValidatorClassDefault(boolean enabled) {
        generateValidatorClassByDefault = enabled;
    }

    @Override
    public boolean isGenericValidationDefaultEnabled() {
        return genericValidationByDefault;
    }

    @Override
    public void setGenericValidationDefault(boolean enabled) {
        genericValidationByDefault = enabled;
    }

    @Override
    public boolean isEscapeNonStandardBlanks() {
        return escapeNonStandardBlanks;
    }

    @Override
    public void setEscapeNonStandardBlanks(boolean enabled) {
        this.escapeNonStandardBlanks = enabled;
    }

    @Override
    public Decimal getInferredTemplateLinkThreshold() {
        return inferredTemplateLinkThreshold;
    }

    @Override
    public void setInferredTemplateLinkThreshold(Decimal inferredTemplateLinkThreshold) {
        this.inferredTemplateLinkThreshold = inferredTemplateLinkThreshold;
    }

    @Override
    public Decimal getInferredTemplatePropertyValueThreshold() {
        return inferredTemplatePropertyValueThreshold;
    }

    @Override
    public void setInferredTemplatePropertyValueThreshold(Decimal inferredTemplatePropertyValueThreshold) {
        this.inferredTemplatePropertyValueThreshold = inferredTemplatePropertyValueThreshold;
    }

    @Override
    public @CheckForNull IIpsFeatureConfiguration getFeatureConfiguration(String featureId) {
        return featureConfigurations.get(featureId);
    }

    /**
     * Sets the {@link IIpsFeatureConfiguration} for the feature identified by the given ID.
     */
    public void setFeatureConfiguration(String featureId, IpsFeatureConfiguration featureConfiguration) {
        ArgumentCheck.notNull(featureId, "featureId must not be null"); //$NON-NLS-1$
        ArgumentCheck.notNull(featureConfiguration, "featureConfiguration must not be null"); //$NON-NLS-1$
        featureConfigurations.put(featureId, featureConfiguration);
    }

    @Override
    public Severity getDuplicateProductComponentSeverity() {
        return duplicateProductComponentSeverity;
    }

    @Override
    public void setDuplicateProductComponentSeverity(Severity duplicateProductComponentSeverity) {
        this.duplicateProductComponentSeverity = duplicateProductComponentSeverity;
    }

    @Override
    public Severity getPersistenceColumnSizeChecksSeverity() {
        return persistenceColumnSizeChecksSeverity;
    }

    @Override
    public void setPersistenceColumnSizeChecksSeverity(Severity persistenceColumnSizeChecksSeverity) {
        this.persistenceColumnSizeChecksSeverity = persistenceColumnSizeChecksSeverity;
    }

    @Override
    public TableContentFormat getTableContentFormat() {
        return tableContentFormat;
    }

    @Override
    public void setTableContentFormat(TableContentFormat tableContentFormat) {
        this.tableContentFormat = tableContentFormat;
    }

    @Override
    public boolean isValidateIpsSchema() {
        return validateIpsSchema;
    }

    @Override
    public void setValidateIpsSchema(boolean validateIpsSchema) {
        this.validateIpsSchema = validateIpsSchema;
    }

}
// CSON: RegexpHeaderCheck
// CSON: FileLengthCheck