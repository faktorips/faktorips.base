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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategyFactory;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.devtools.core.model.question.QuestionAssignedUserGroup;
import org.faktorips.devtools.core.model.question.QuestionStatus;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An ips project's properties. The project can't keep the properties on its own, as it is a handle.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectProperties implements IIpsProjectProperties {

    private final static String ATTRIBUTE_PERSISTENT_PROJECT = "persistentProject"; //$NON-NLS-1$

    private final static String OPTIONAL_CONSTRAINT_NAME_RULESWITHOUTREFERENCE = "rulesWithoutReferencesAllowed"; //$NON-NLS-1$

    private final static String OPTIONAL_CONSTRAINT_SHARED_ASSOCIATIONS = "sharedDetailToMasterAssociations"; //$NON-NLS-1$

    private final static String OPTIONAL_CONSTRAINT_ASSOCIATIONS_IN_FORMULAS = "associationsInFormulas"; //$NON-NLS-1$

    public final static IpsProjectProperties createFromXml(IpsProject ipsProject, Element element) {
        IpsProjectProperties data = new IpsProjectProperties();
        data.initFromXml(ipsProject, element);
        return data;
    }

    public final static String TAG_NAME = "IpsProject"; //$NON-NLS-1$

    private boolean createdFromParsableFileContents = true;

    private boolean modelProject;
    private boolean productDefinitionProject;
    private boolean persistentProject;

    public static final String VERSION_ATTRIBUTE = "version"; //$NON-NLS-1$
    /**
     * the version of this project, used for release
     */
    private String releaseVersion;
    private final static String RELEASE_EXTENSION_ID_ATTRIBUTE = "releaseExtensionId"; //$NON-NLS-1$

    private static final String PRODUCT_RELEASE = "productRelease"; //$NON-NLS-1$

    private static final String DEFAULT_CURRENCY_ELEMENT = "DefaultCurrency"; //$NON-NLS-1$
    private static final String DEFAULT_CURRENCY_VALUE_ATTR = "value"; //$NON-NLS-1$

    /**
     * The id of the release extension that is associatied with this project
     */
    private String releaseExtensionId;

    private String changesInTimeConventionIdForGeneratedCode = IChangesOverTimeNamingConvention.VAA;

    private IProductCmptNamingStrategy productCmptNamingStrategy;
    private String productCmptNamingStrategyId = null; // must keep id as well, to report validation
    // errors, if strategy is not found!

    private String builderSetId = ""; //$NON-NLS-1$
    private IIpsArtefactBuilderSetConfigModel builderSetConfig = new IpsArtefactBuilderSetConfigModel();
    private IIpsObjectPath path = new IpsObjectPath(new IpsProject());
    private String[] predefinedDatatypesUsed = new String[0];

    // all datatypes defined in the project including(!) the value datatypes.
    private List<Datatype> definedDatatypes = new ArrayList<Datatype>(0);
    private String runtimeIdPrefix = ""; //$NON-NLS-1$
    private boolean javaProjectContainsClassesForDynamicDatatypes = false;
    private boolean derivedUnionIsImplementedRuleEnabled = true;
    private boolean referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled = true;
    private boolean rulesWithoutReferencesAllowed = false;
    private boolean sharedDetailToMasterAssociations = false;
    private boolean associationsInFormulas = false;

    private Map<String, String> requiredFeatures = new HashMap<String, String>();

    // hidden resource names in the model and product explorer
    private Set<String> resourcesPathExcludedFromTheProductDefiniton = new HashSet<String>(10);
    private Long lastPersistentModificationTimestamp;

    private IPersistenceOptions persistenceOptions = new PersistenceOptions();

    /** The set of natural languages supported by the IPS project. */
    private Set<ISupportedLanguage> supportedLanguages = new LinkedHashSet<ISupportedLanguage>(2);

    private IProductCmptNamingStrategy defaultCmptNamingStrategy;

    // default currency for this project - default is EUR
    private Currency defaultCurrency = Currency.getInstance("EUR"); //$NON-NLS-1$

    public IpsProjectProperties() {
        super();
    }

    /**
     * Copy constructor.
     */
    public IpsProjectProperties(IIpsProject ipsProject, IpsProjectProperties props) {
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element el = props.toXml(doc);
        initFromXml(ipsProject, el);
        createdFromParsableFileContents = props.createdFromParsableFileContents;
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
            validateSupportedLanguages(list);
            return list;
        } catch (RuntimeException e) {
            // if runtime exceptions are not converted into core exceptions the stack trace gets
            // lost in the logging file and they are hard to find
            throw new CoreException(new IpsStatus(e));
        }
    }

    private void validatePersistenceOption(MessageList msgList) {
        if (isPersistenceSupportEnabled()) {
            String text = NLS.bind(Messages.IpsProjectProperties_error_persistenceAndSharedAssociationNotAllowed,
                    OPTIONAL_CONSTRAINT_SHARED_ASSOCIATIONS, ATTRIBUTE_PERSISTENT_PROJECT);
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
        IIpsFeatureVersionManager[] managers = IpsPlugin.getDefault().getIpsFeatureVersionManagers();
        for (IIpsFeatureVersionManager manager : managers) {
            if (getMinRequiredVersionNumber(manager.getFeatureId()) == null) {
                String text = NLS.bind(Messages.IpsProjectProperties_msgMissingMinFeatureId, manager.getFeatureId());
                list.add(new Message(IIpsProjectProperties.MSGCODE_MISSING_MIN_FEATURE_ID, text, Message.ERROR, this));
            }
        }
    }

    private void validateUsedPredefinedDatatype(IIpsProject ipsProject, MessageList list) {
        IIpsModel model = ipsProject.getIpsModel();
        for (int i = 0; i < predefinedDatatypesUsed.length; i++) {
            if (!model.isPredefinedValueDatatype(predefinedDatatypesUsed[i])) {
                String text = NLS.bind(Messages.IpsProjectProperties_msgUnknownDatatype, predefinedDatatypesUsed[i]);
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
     * Validate the ips object path entry.
     */
    private void validateIpsObjectPath(MessageList list) throws CoreException {
        list.add(path.validate());
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
        List<DynamicValueDatatype> valuetypes = new ArrayList<DynamicValueDatatype>(definedDatatypes.size());
        for (Datatype datatype : definedDatatypes) {
            if (datatype.isValueDatatype()) {
                valuetypes.add((DynamicValueDatatype)datatype);
            }
        }
        return valuetypes.toArray(new DynamicValueDatatype[valuetypes.size()]);
    }

    @Override
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes) {
        definedDatatypes = new ArrayList<Datatype>(datatypes.length);
        for (DynamicValueDatatype datatype : datatypes) {
            definedDatatypes.add(datatype);
        }
    }

    @Override
    public void setDefinedDatatypes(Datatype[] datatypes) {
        definedDatatypes = new ArrayList<Datatype>(datatypes.length);
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
        projectEl.setAttribute(
                "javaProjectContainsClassesForDynamicDatatypes", "" + javaProjectContainsClassesForDynamicDatatypes); //$NON-NLS-1$ //$NON-NLS-2$
        projectEl.setAttribute("changesInTimeNamingConvention", changesInTimeConventionIdForGeneratedCode); //$NON-NLS-1$
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
        createDescriptionComment(IpsObjectPath.getXmlFormatDescription(), projectEl);
        projectEl.appendChild(((IpsObjectPath)path).toXml(doc));

        // datatypes
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

        // excludes resources from product definition
        createResourcesExcludedFromProductDefinitionComment(projectEl);
        Element resourcesExcludedFromProdDefEl = doc.createElement("ResourcesExcludedFromProductDefinition"); //$NON-NLS-1$
        projectEl.appendChild(resourcesExcludedFromProdDefEl);
        for (String exclResource : resourcesPathExcludedFromTheProductDefiniton) {
            Element resourceExcludedEl = doc.createElement("Resource"); //$NON-NLS-1$
            resourceExcludedEl.setAttribute("path", exclResource); //$NON-NLS-1$
            resourcesExcludedFromProdDefEl.appendChild(resourceExcludedEl);
        }

        // product definition release
        createProductReleaseComment(projectEl);
        Element release = doc.createElement(PRODUCT_RELEASE);
        release.setAttribute(VERSION_ATTRIBUTE, releaseVersion);
        release.setAttribute(RELEASE_EXTENSION_ID_ATTRIBUTE, releaseExtensionId);
        projectEl.appendChild(release);

        // optional constraints
        createOptionalConstraintsDescriptionComment(projectEl);
        Element optionalConstraintsEl = doc.createElement("OptionalConstraints"); //$NON-NLS-1$
        projectEl.appendChild(optionalConstraintsEl);

        optionalConstraintsEl.appendChild(createConstraintElement(doc, "derivedUnionIsImplemented", //$NON-NLS-1$
                derivedUnionIsImplementedRuleEnabled));

        optionalConstraintsEl.appendChild(createConstraintElement(doc,
                "referencedProductComponentsAreValidOnThisGenerationsValidFromDate", //$NON-NLS-1$
                referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled));

        optionalConstraintsEl.appendChild(createConstraintElement(doc, OPTIONAL_CONSTRAINT_NAME_RULESWITHOUTREFERENCE,
                rulesWithoutReferencesAllowed));

        optionalConstraintsEl.appendChild(createConstraintElement(doc, OPTIONAL_CONSTRAINT_SHARED_ASSOCIATIONS,
                isSharedDetailToMasterAssociations()));

        optionalConstraintsEl.appendChild(createConstraintElement(doc, OPTIONAL_CONSTRAINT_ASSOCIATIONS_IN_FORMULAS,
                isAssociationsInFormulas()));

        // persistence options
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

        return projectEl;
    }

    private Element createConstraintElement(Document doc, String name, boolean enable) {
        Element constraintElement = doc.createElement("Constraint"); //$NON-NLS-1$
        constraintElement.setAttribute("name", name); //$NON-NLS-1$
        constraintElement.setAttribute("enable", "" + enable); //$NON-NLS-1$ //$NON-NLS-2$
        return constraintElement;
    }

    public void initFromXml(IIpsProject ipsProject, Element element) {
        modelProject = Boolean.valueOf(element.getAttribute("modelProject")).booleanValue(); //$NON-NLS-1$
        productDefinitionProject = Boolean.valueOf(element.getAttribute("productDefinitionProject")).booleanValue(); //$NON-NLS-1$
        persistentProject = Boolean.valueOf(element.getAttribute(ATTRIBUTE_PERSISTENT_PROJECT)).booleanValue();
        runtimeIdPrefix = element.getAttribute("runtimeIdPrefix"); //$NON-NLS-1$
        javaProjectContainsClassesForDynamicDatatypes = Boolean.valueOf(
                element.getAttribute("javaProjectContainsClassesForDynamicDatatypes")).booleanValue(); //$NON-NLS-1$
        changesInTimeConventionIdForGeneratedCode = element.getAttribute("changesInTimeNamingConvention"); //$NON-NLS-1$
        changesInTimeConventionIdForGeneratedCode = StringUtils.isEmpty(changesInTimeConventionIdForGeneratedCode) ? IChangesOverTimeNamingConvention.VAA
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
        Element pathEl = XmlUtil.getFirstElement(element, IpsObjectPath.XML_TAG_NAME);
        if (pathEl != null) {
            path = IpsObjectPath.createFromXml(ipsProject, pathEl);
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

        initResourcesExcludedFromProductDefinition(XmlUtil.getFirstElement(element,
                "ResourcesExcludedFromProductDefinition")); //$NON-NLS-1$

        initProductRelease(XmlUtil.getFirstElement(element, PRODUCT_RELEASE));

        initOptionalConstraints(element);

        initPersistenceOptions(element);

        initSupportedLanguages(element);

        initDefaultCurrency(element);
    }

    private void initRequiredFeatures(Element el) {
        requiredFeatures = new HashMap<String, String>();

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
        definedDatatypes = new ArrayList<Datatype>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element)nl.item(i);
            Datatype datatype = createDefinedDatatype(ipsProject, el);
            definedDatatypes.add(datatype);
        }
    }

    private Datatype createDefinedDatatype(IIpsProject ipsProject, Element element) {
        if (!element.hasAttribute("valueObject") || Boolean.valueOf(element.getAttribute("valueObject")).booleanValue()) { //$NON-NLS-1$ //$NON-NLS-2$
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
            Attr path = child.getAttributeNode("path"); //$NON-NLS-1$
            if (path != null && StringUtils.isNotEmpty(path.getValue())) {
                resourcesPathExcludedFromTheProductDefiniton.add(path.getValue());
            }
        }
    }

    private void initProductRelease(Element element) {
        if (element == null) {
            return;
        }
        releaseVersion = element.getAttribute(VERSION_ATTRIBUTE);
        releaseExtensionId = element.getAttribute(RELEASE_EXTENSION_ID_ATTRIBUTE);
    }

    /**
     * Reads the definition of optional constraints from the given &lt;IpsProject&gt; XML
     * <code>Element</code>.
     * 
     * @param element The &lt;IpsProject&gt; XML <code>Element</code>.
     */
    private void initOptionalConstraints(Element element) {
        // migration for 1.0 files
        if (element.hasAttribute("containerRelationIsImplementedRuleEnabled")) { //$NON-NLS-1$
            derivedUnionIsImplementedRuleEnabled = Boolean.valueOf(
                    element.getAttribute("containerRelationIsImplementedRuleEnabled")).booleanValue(); //$NON-NLS-1$
        }

        // migration for 2.0-rc files
        if (element.hasAttribute("derivedUnionIsImplementedRuleEnabled")) { //$NON-NLS-1$
            derivedUnionIsImplementedRuleEnabled = Boolean.valueOf(
                    element.getAttribute("derivedUnionIsImplementedRuleEnabled")).booleanValue(); //$NON-NLS-1$
        }

        // since 2.0: read from <OptionalConstraints>
        Element optionalConstraintsEl = XmlUtil.getFirstElement(element, "OptionalConstraints"); //$NON-NLS-1$

        // migration for pre-2.0 files
        if (optionalConstraintsEl == null) {
            return;
        }

        NodeList nl = optionalConstraintsEl.getElementsByTagName("Constraint"); //$NON-NLS-1$
        int length = nl.getLength();
        for (int i = 0; i < length; ++i) {
            Element child = (Element)nl.item(i);
            if (!child.hasAttribute("name") || !child.hasAttribute("enable")) { //$NON-NLS-1$ //$NON-NLS-2$
                // ignore incomplete entries
                continue;
            }

            String name = child.getAttribute("name"); //$NON-NLS-1$
            boolean enable = Boolean.valueOf(child.getAttribute("enable")).booleanValue(); //$NON-NLS-1$

            if (name.equals("derivedUnionIsImplemented")) { //$NON-NLS-1$
                derivedUnionIsImplementedRuleEnabled = enable;
            } else if (name.equals("referencedProductComponentsAreValidOnThisGenerationsValidFromDate")) { //$NON-NLS-1$
                referencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled = enable;
            } else if (name.equals(OPTIONAL_CONSTRAINT_NAME_RULESWITHOUTREFERENCE)) {
                rulesWithoutReferencesAllowed = enable;
            } else if (name.equals(OPTIONAL_CONSTRAINT_SHARED_ASSOCIATIONS)) {
                setSharedDetailToMasterAssociations(enable);
            } else if (name.equals(OPTIONAL_CONSTRAINT_ASSOCIATIONS_IN_FORMULAS)) {
                setAssociationsInFormulas(enable);
            }
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
        try {
            defaultCurrency = Currency.getInstance(value);
        } catch (IllegalArgumentException e) {
            // use default value
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
    public void addDefinedDatatype(DynamicValueDatatype newDatatype) {
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
    public boolean isJavaProjectContainsClassesForDynamicDatatypes() {
        return javaProjectContainsClassesForDynamicDatatypes;
    }

    @Override
    public void setJavaProjectContainsClassesForDynamicDatatypes(boolean newValue) {
        javaProjectContainsClassesForDynamicDatatypes = newValue;
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

    private void createIpsProjectDescriptionComment(Node parentEl) {
        String s = "This XML file contains the properties of the enclosing IPS project. It contains the following " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " information:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The generator used to transform the model to Java sourcecode and the product definition into the runtime format." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The path where to search for model and product definition files. This is basically the same concept as the  Java " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "classpath. A strategy that defines how to name product components and what names are valid." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The datatypes that can be used in the model. Datatypes used in the model fall into two categeories:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " * Predefined datatype" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   Predefined datatypes are defined by the datatype definition extension. Faktor-IPS predefines datatypes for" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   the standard Java classes like Boolean, String, Integer, etc. and some additional types, for example Money." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   You can add you own datatype be providing an extension and then use it from every IPS project." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " * User defined datatype (or dynamic datatype)" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   If you want to use a Java class that represents a value as datatype, but do not want to provide an extension for" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   it, you can register this class as datatype in this file. See the details in the description of the datatype " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   section below how to register the class. Naturally, the class must be available via the project's Java classpath." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   There you have different options. It is strongly recommended to provide the class via a JAR file or in a separate" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   Java project. However you can also implement the class in this project itself. In this case you have to set the " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   javaProjectContainsClassesForDynamicDatatypes property to true so that Faktor-IPS also looks in this project " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   for the class. The disadvantage of this approach is that a clean build won't work properly. At the beginning" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   of the clean build the Java class is deleted, then Faktor-IPS checks the model, doesn't find the class and" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   reports problems." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<IpsProject>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    productDefinitionProject                           True if this project contains elements of the product definition." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    modelProject                                       True if this project contains the model or part of it." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    runtimeIdPrefix                                    " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    changesInTimeNamingConvention                      Specifies the naming conventions for changes in time that " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                       are used throughout the system. Possible values are VAA and PM" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    javaProjectContainsClassesForDynamicDatatypes      see discussion above" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <IpsArtefactBuilderSet/>                           The generator used. Details below." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <IpsObjectPath/>                                   The object path to search for model and product definition" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                       objects. Details below." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <ProductCmptNamingStrategy/>                       The strategy used for product component names. Details below." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Datatypes/>                                       The datatypes used in the model. Details below." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <OptionalConstraints/>                             Definition of optional constraints. Details below." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <SupportedLanguages/>                              List of supported natural languages. Details below." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "</IpsProject>" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl, "    "); //$NON-NLS-1$
    }

    private void createProductCmptNamingStrategyDescriptionComment(Element parentEl) {
        String s = "Product Component Naming Strategy" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The naming strategy defines the structure of product component names and how characters that are not allowed" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "in Java identifiers are replaced by the code generator. In order to deal with different versions of " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "a product you need a strategy to derive the version from the product component name. " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "Currently Faktor-IPS includes the following strategy:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " * DateBasedProductCmptNamingStrategy" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   The product component name is made up of a \"unversioned\" name and a date format for the version id." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "   <ProductCmptNamingStrategy id=\"org.faktorips.devtools.core.DateBasedProductCmptNamingStrategy\">" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "       <DateBasedProductCmptNamingStrategy " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "           dateFormatPattern=\"yyyy-MM\"                           Format of the version id according to" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                                   java.text.DateFormat" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "           postfixAllowed=\"true\"                                 True if the date format can be followed by" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                                   an optional postfix." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "           versionIdSeparator=\" \">                               The separator between \"unversioned name\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                                   and version id." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "           <JavaIdentifierCharReplacements>                        Definition replacements for charcacters invalid " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                                   in Java identifiers." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "               <Replacement replacedChar=\" \" replacement=\"___\"/> Example: Replace Blank with three underscores" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "               <Replacement replacedChar=\"-\" replacement=\"__\"/>  Example: Replace Hyphen with two underscores" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "           </JavaIdentifierCharReplacements>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "       </DateBasedProductCmptNamingStrategy>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    </ProductCmptNamingStrategy>" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createDatatypeDescriptionComment(Node parentEl) {
        String s = "Datatypes" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "In the datatypes section the value datatypes allowed in the model are defined." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "See also the discussion at the top this file." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<UsedPredefinedDatatypes>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Datatype id=\"Money\"\\>                                 The id of the datatype that should be used." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "</UsedPredefinedDatatypes>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<DatatypeDefinitions>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Datatype id=\"PaymentMode\"                             The datatype's id used in the model to refer to it." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        javaClass=\"org.faktorips.sample.PaymentMode\"       The Java class the datatype represents" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        valueObject=\"true|false\"                           True indicates this is a value object (according to the value object pattern.) " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        --- the following attributes are only needed for value objects ---" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        isEnumType=\"true|false\"                            True if this is an enumeration of values." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        valueOfMethod=\"getPaymentMode\"                     Name of the method that takes a String and returns an" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             object instance/value." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        isParsableMethod=\"isPaymentMode\"                   Name of the method that evaluates if a given string" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             can be parsed to an instance." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        valueToStringMethod=\"toString\"                     Name of the method that transforms an object instance" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                              to a String (that can be parsed via the valueOfMethod)" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        getAllValuesMethod=\"getAllPaymentModes\"            For enums only: The name of the method that returns all values" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        isSupportingNames=\"true\"                           For enums only: True indicates that a string" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             representation for the user other than the one defined by the valueToStringMethod exists. + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$" //$NON-NLS-1$
                + "        getNameMethod=\"getName\">                           For enums only: The name of the method that returns" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             the string representation for the user, if" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             isSupportingNames=true" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        <NullObjectId isNull=\"false\">n</NullObjectId>      Marks a value as a NullObject. This has to be used," + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             if the Java class implements the null object pattern," + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$"
                + "                                                             otherwise omitt this element. The element's text" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             defines the null object's id. Calling the valueOfMethod" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             with this name must return the null object instance. If" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             the null object's id is null, leave the text empty" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                                             and set the isNull attribute to true." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    </Datatype>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "</DatatypeDefinitions>" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createIpsArtefactBuilderSetDescriptionComment(Node parentEl) {
        String s = "Artefact builder set" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "In this section the artefact builder set (code generator) is defined. Faktor-IPS comes with a standard builder set." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "However the build and generation mechanism is completly decoupled from the modeling and product definition capabilities" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "and you can write your own builders and generators. A different builder set is defined by providing an extension for" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "the extension point \"org.faktorips.devtools.core.artefactbuilderset\" defined by Faktor-IPS plugin" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "A builder set is activated for an IPS project by defining the IpsArtefactBuilderSet tag. The attribute \"id\" specifies" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "the builder set implementation that is registered as an extension. Note: The unique identifier of the extension is to specify." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<IpsArtefactBuilderSet id=\"org.faktorips.devtools.stdbuilder.ipsstdbuilderset\"/> A builder set can be configured by specifing a " + SystemUtils.LINE_SEPARATOR//$NON-NLS-1$
                + "nested tag <IpsArtefactBuilderSetConfig/>. A configuration contains a set of properties which are specified by nested" + SystemUtils.LINE_SEPARATOR//$NON-NLS-1$
                + "<Property name=\"\" value=\"\"/> tags. The possible properties and their values is specific to the selected builder set." + SystemUtils.LINE_SEPARATOR//$NON-NLS-1$
                + "The initially generated .ipsproject file contains the set of possible configuration properties for the selected builder set" + SystemUtils.LINE_SEPARATOR//$NON-NLS-1$
                + "including their descriptions." + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createRequiredIpsFeaturesComment(Node parentEl) {
        String s = "Required Ips-Features" + SystemUtils.LINE_SEPARATOR + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "In this section, all required features are listed with the minimum version for these features." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "By default, the feature with id \"org.faktorips.feature\" is always required (because this is the core " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "feature of Faktor-IPS. Other features can be required if plugins providing extensions for any extension points" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "defined by Faktor-IPS are used." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "If a required feature is missing or a required feature has a version less than the minimum version number " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "this project will not be build (an error is created)." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<RequiredIpsFeatures>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <RequiredIpsFeature id=\"org.faktorips.feature\"    The id of the required feature." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "        minVersion=\"0.9.38\"                           The minimum version number of this feature" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "</RequiredIpsFeatures>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + ""; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createResourcesExcludedFromProductDefinitionComment(Node parentEl) {
        String s = "Resources excluded from the product definition" + SystemUtils.LINE_SEPARATOR + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "In this section, all resources which will be excluded (hidden) in the product definition are listed." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The resource must be identified by its full path, relative to the project the resource belongs to." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "" + SystemUtils.LINE_SEPARATOR + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "<ResourcesExcludedFromProductDefinition>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Resource path=\"src\"/>" + "              Example: The 1st excluded resource, identified by its path." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "    <Resource path=\"build/build.xml\"/>" + "  Example: The 2nd excluded resource, identified by its path." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "</ResourcesExcludedFromProductDefinition>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + ""; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createProductReleaseComment(Element parentEl) {
        String s = "Product Release" + SystemUtils.LINE_SEPARATOR + " " + SystemUtils.LINE_SEPARATOR + //$NON-NLS-1$ //$NON-NLS-2$
                "In this section, the product defintion release is configured. You could reference an release extension" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "by specifying the releaseExtensionId. This extension is used by the release builder wizard." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "The version of the latest release is also configured in this element. If you use the release builder wizard" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "you should not set this version manually but using the release builder wizard." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " " + SystemUtils.LINE_SEPARATOR + //$NON-NLS-1$
                "<" //$NON-NLS-1$
                + PRODUCT_RELEASE
                + " " + RELEASE_EXTENSION_ID_ATTRIBUTE + "=\"id-of-the-extension\" " + VERSION_ATTRIBUTE + "=\"1.2.3\"/>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + SystemUtils.LINE_SEPARATOR;
        createDescriptionComment(s, parentEl);
    }

    private void createOptionalConstraintsDescriptionComment(Node parentEl) {
        // @formatter:off
        String s = "OptionalConstraints" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "Some of the contraints defined in the Faktor-IPS metamodel are optional." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "In this section you can enable or disable these optional contraints." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<OptionalConstraints>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <!-- True if Faktor-IPS checks if all derived unions are implemented in none abstract classes. -->" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Constraint name=\"derivedUnionIsImplemented\" enable=\"true\"/>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <!-- True if Faktor-IPS checks if referenced product components are valid on the effective date " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "		    of the referencing product component generation. -->" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Constraint name=\"referencedProductComponentsAreValidOnThisGenerationsValidFromDate\" enable=\"true\"/>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <!-- True to allow rules without references -->" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Constraint name=\"" + OPTIONAL_CONSTRAINT_NAME_RULESWITHOUTREFERENCE + "\" enable=\"true\"/>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "    <!-- True to allow shared associations. Shared associations are detail-to-master associationis that can be used" //$NON-NLS-1$
                + "         by multiple master-to-detail associations-->" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Constraint name=\"" + OPTIONAL_CONSTRAINT_SHARED_ASSOCIATIONS + "\" enable=\"true\"/>" //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                + "    <!-- True to allow navigation via associations in formulas. -->" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <Constraint name=\"" + OPTIONAL_CONSTRAINT_ASSOCIATIONS_IN_FORMULAS + "\" enable=\"true\"/>" //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                //
                // Check if the inverse associations have to be type safe or not. Due to Issue
                // FIPS-85 we need
                // * to have to possibility to use the inverse association of the super type as
                // inverse
                // * association for a concrete type. When this property is false, these unsafe
                // inverse
                // * associations are allowed. Otherwise if this property is true you have to create
                // a concrete
                // * inverse association for every subset of a derived union with an inverse
                // association.

                + "</OptionalConstraints>" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
//        @formatter:on
    }

    private void createPersistenceOptionsDescriptionComment(Node parentEl) {
        String s = "PersistenceOptions" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "In this section you can adjust parameters relating the persistence of IPolicyCmptTypes." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The table and column naming strategies define how identifier names are transformed into" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "database table and column names. The attributes maxTableNameLength and maxColumnNameLength" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "constrain the maximum possible length of a table or column name." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "The attribute " + IPersistenceOptions.ALLOW_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS + " defines if is is allowed to use lazy fetching " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "on the association side which holds a single value (to-one relationship side)." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<PersistenceOptions maxColumnNameLength=\"255\" maxTableNameLength=\"255\">" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <TableNamingStrategy id=\"org.faktorips.devtools.core.CamelCaseToUpperUnderscoreTableNamingStrategy\">" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <TableColumnNamingStrategy id=\"org.faktorips.devtools.core.CamelCaseToUpperUnderscoreColumnNamingStrategy\">" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "</PersistenceOptions>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "Currently Faktor-IPS includes the strategies CamelCaseToUpperUnderscoreTableNamingStrategy" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "for tables and CamelCaseToUpperUnderscoreColumnNamingStrategy for columns, examples:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    IdentifierName1 -> IDENTIFIER_NAME1" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createSupportedLanguagesDescriptionComment(Node parentEl) {
        String s = "Supported Languages" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "This section lists all natural languages that are supported by this IPS project." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "Each language is identified by it's locale which is the ISO 639 language code, " //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "e.g. 'en' for English." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "Exactly one supported language must be marked as default language. The default language " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "will be used if a language is requested that is not supported by this IPS project." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "<SupportedLanguages>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <SupportedLanguage locale=\"en\" defaultLanguage=\"true\"/>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    <SupportedLanguage locale=\"de\"/>" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "</SupportedLanguages>" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$
        createDescriptionComment(s, parentEl);
    }

    private void createDescriptionComment(String text, Node parent) {
        createDescriptionComment(text, parent, "        "); //$NON-NLS-1$
    }

    private void createDescriptionComment(String text, Node parent, String indentation) {
        StringBuffer indentedText = new StringBuffer();
        indentedText.append(SystemUtils.LINE_SEPARATOR);
        StringTokenizer tokenizer = new StringTokenizer(text, SystemUtils.LINE_SEPARATOR);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            indentedText.append(indentation);
            indentedText.append(token);
            indentedText.append(SystemUtils.LINE_SEPARATOR);
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
    public void setResourcesPathExcludedFromTheProductDefiniton(Set<String> resourcesPathExcludedFromTheProductDefiniton) {
        this.resourcesPathExcludedFromTheProductDefiniton = resourcesPathExcludedFromTheProductDefiniton;
    }

    @Override
    public Long getLastPersistentModificationTimestamp() {
        return lastPersistentModificationTimestamp;
    }

    @Override
    public void setLastPersistentModificationTimestamp(Long timestamp) {
        lastPersistentModificationTimestamp = timestamp;
    }

    @Override
    public EnumType getQuestionAssignedUserGroup() {
        DefaultEnumType type = new DefaultEnumType("QuestionAssignedUserGroup", QuestionAssignedUserGroup.class); //$NON-NLS-1$
        new QuestionAssignedUserGroup(type,
                "undefined", Messages.IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_UNDEFINED); //$NON-NLS-1$
        new QuestionAssignedUserGroup(type,
                "business", Messages.IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_BUSINESS); //$NON-NLS-1$
        new QuestionAssignedUserGroup(type,
                "implementation", Messages.IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_IMPLEMENTATION); //$NON-NLS-1$
        new QuestionAssignedUserGroup(type,
                "cooperate", Messages.IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_COOPERATE); //$NON-NLS-1$
        return type;
    }

    @Override
    public EnumType getQuestionStatus() {
        DefaultEnumType type = new DefaultEnumType("QuestionStatus", QuestionStatus.class); //$NON-NLS-1$
        new QuestionStatus(type, "open", Messages.IpsProjectProperties_ENUM_QUESTION_STATUS_OPEN); //$NON-NLS-1$
        new QuestionStatus(type, "closed", Messages.IpsProjectProperties_ENUM_QUESTION_STATUS_CLOSED); //$NON-NLS-1$
        new QuestionStatus(type, "deferred", Messages.IpsProjectProperties_ENUM_QUESTION_STATUS_DEFERRED); //$NON-NLS-1$
        return type;
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
        return null;
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
        if (currentDefaultLanguage != null) {
            ((SupportedLanguage)currentDefaultLanguage).setDefaultLanguage(false);
        }
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
        return releaseVersion;
    }

    @Override
    public void setVersion(String version) {
        this.releaseVersion = version;
    }

    @Override
    public String getReleaseExtensionId() {
        return releaseExtensionId;
    }

    @Override
    public void setAssociationsInFormulas(boolean associationsInFormulas) {
        this.associationsInFormulas = associationsInFormulas;
    }

    @Override
    public boolean isAssociationsInFormulas() {
        return associationsInFormulas;
    }

}
