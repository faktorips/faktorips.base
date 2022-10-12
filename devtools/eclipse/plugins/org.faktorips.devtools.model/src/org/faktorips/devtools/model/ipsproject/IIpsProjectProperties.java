/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.datatype.IDynamicValueDatatype;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.model.enumtype.EnumType;
import org.faktorips.values.Decimal;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Properties of the IPS project. The IPS project can't keep the properties itself, as it is a
 * handle. The properties are persisted in the ".ipsproject" file.
 * 
 * @author Jan Ortmann
 */
public interface IIpsProjectProperties {

    String ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION = "changesInTimeNamingConvention"; //$NON-NLS-1$

    String TAG_NAME = "IpsProject"; //$NON-NLS-1$

    String PROPERTY_BUILDER_SET_ID = "builderSetId"; //$NON-NLS-1$

    String PROPERTY_CONTAINER_RELATIONS_MUST_BE_IMPLEMENTED = "containerRelationIsImplementedRuleEnabled"; // $NON-NLS-1$ //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the IPS artifact builder set id is unknown.
     */
    String MSGCODE_UNKNOWN_BUILDER_SET_ID = MSGCODE_PREFIX + "UnknwonBuilderSetId"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a used predefined data type is unknown.
     */
    String MSGCODE_UNKNOWN_PREDEFINED_DATATYPE = MSGCODE_PREFIX + "UnknownPredefinedDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the minimum required version number for a specific
     * feature is missing.
     */
    String MSGCODE_MISSING_MIN_FEATURE_ID = MSGCODE_PREFIX + "MissingMinFeatureId"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component naming strategy can't be
     * found.
     */
    String MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY = MSGCODE_PREFIX
            + "InvalidProductCmptNamingStrategy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the language identifier used as locale for a
     * supported language is not a valid ISO 639 language code.
     */
    String MSGCODE_SUPPORTED_LANGUAGE_UNKNOWN_LOCALE = MSGCODE_PREFIX
            + "SupportedLanguageUnknownLocale"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the feature ID given for a
     * {@link #getFeatureConfiguration(String) feature configuration} is not known, as it is not
     * contained in the list of {@link #getRequiredIpsFeatureIds() required feature IDs}.
     */
    String MSGCODE_FEATURE_CONFIGURATION_UNKNOWN_FEATURE = MSGCODE_PREFIX
            + "FeatureConfigurationUnknownFeature"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that more than one supported language is marked as
     * default language.
     */
    String MSGCODE_MORE_THAN_ONE_DEFAULT_LANGUAGE = MSGCODE_PREFIX + "MoreThanOneDefaultLanguage"; //$NON-NLS-1$

    String MSGCODE_INVALID_OPTIONAL_CONSTRAINT = MSGCODE_PREFIX + "invalidOptionalConstraint"; //$NON-NLS-1$

    String MSGCODE_INVALID_VERSION_SETTING = MSGCODE_PREFIX + "invalidVersionSetting"; //$NON-NLS-1$

    String MSGCODE_INVALID_MARKER_ENUMS = MSGCODE_PREFIX + "invalidMarkerEnums"; //$NON-NLS-1$

    String PROPERTY_VERSION = "version"; //$NON-NLS-1$

    String PROPERTY_VERSION_PROVIDER_ID = "versionProviderId"; //$NON-NLS-1$

    /**
     * Returns the time stamp of the last persistent modification of this object.
     */
    long getLastPersistentModificationTimestamp();

    /**
     * Sets the time stamp of the last persistent modification of this object.
     */
    void setLastPersistentModificationTimestamp(long timestamp);

    /**
     * Validates the project properties.
     */
    MessageList validate(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns id of the builder set used to generate sourcecode from the model / product
     * definition.
     */
    String getBuilderSetId();

    /**
     * Sets the id of the builder set used to generate sourcecode from the model / product
     * definition.
     */
    void setBuilderSetId(String id);

    /**
     * Returns the IpsProject specific configuration of the IIpsArtefactBuilderSet.
     */
    IIpsArtefactBuilderSetConfigModel getBuilderSetConfig();

    /**
     * Sets the IpsProjects specific configuration of the IIpsArtefactBuilderSet.
     */
    void setBuilderSetConfig(IIpsArtefactBuilderSetConfigModel config);

    /**
     * Returns the object path to lookup objects.
     */
    IIpsObjectPath getIpsObjectPath();

    /**
     * Sets the object path.
     */
    void setIpsObjectPath(IIpsObjectPath path);

    /**
     * Returns <code>true</code> if this is a project containing a (part of a) model, otherwise
     * <code>false</code>. The model is made up of police component types, product component types
     * and so on.
     */
    boolean isModelProject();

    /**
     * Sets if this is project containing model elements or not.
     */
    void setModelProject(boolean modelProject);

    /**
     * Returns <code>true</code> if this is a project containing product definition data, otherwise
     * <code>false</code>. Product definition projects are shown in the product definition
     * perspective.
     */
    boolean isProductDefinitionProject();

    /**
     * Sets if this project contains product definition data.
     */
    void setProductDefinitionProject(boolean productDefinitionProject);

    /**
     * Returns <code>true</code> if this is a project that supports persistence, otherwise
     * <code>false</code>. Persistent projects can store and retrieve policy component types to/from
     * a relational database.
     */
    boolean isPersistenceSupportEnabled();

    /**
     * Sets if this project supports persistence.
     */
    void setPersistenceSupport(boolean persistentProject);

    /**
     * Returns the strategy how product component names are composed.
     */
    IProductCmptNamingStrategy getProductCmptNamingStrategy();

    /**
     * Sets the strategy how product component names are composed.
     */
    void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy);

    /**
     * Returns the strategy used to name database tables used for persisting policy component types.
     * Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    ITableNamingStrategy getTableNamingStrategy();

    /**
     * Sets the strategy how database table names are composed. Does nothing if persistence support
     * is not enabled.
     */
    void setTableNamingStrategy(ITableNamingStrategy newStrategy);

    /**
     * Returns the strategy used to name database columns used for persisting policy component
     * types. Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    ITableColumnNamingStrategy getTableColumnNamingStrategy();

    /**
     * Sets the strategy how database table column names are composed. Does nothing if persistence
     * support is not enabled.
     */
    void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy);

    /**
     * Sets the naming convention for changes over time (by id) used in the generated sourcecode.
     * 
     * @see IChangesOverTimeNamingConvention
     */
    void setChangesOverTimeNamingConventionIdForGeneratedCode(String changesInTimeConventionIdForGeneratedCode);

    /**
     * Returns the id of the naming convention for changes over time used in the generated
     * sourcecode.
     * 
     * @see IChangesOverTimeNamingConvention
     */
    String getChangesOverTimeNamingConventionIdForGeneratedCode();

    /**
     * Returns predefined data types (by id) used by this project. Predefined data types are those
     * that are defined by the extension <code>datatypeDefinition</code>.
     */
    String[] getPredefinedDatatypesUsed();

    /**
     * Sets the predefined data types (by id) used by this project. Predefined data types are those
     * that are defined by the extension <code>datatypeDefinition</code>.
     * 
     * @throws NullPointerException if data types is <code>null</code>.
     */
    void setPredefinedDatatypesUsed(String[] datatypes);

    /**
     * Sets the predefined data types used by this project. Predefined data types are those that are
     * defined by the extension <code>datatypeDefinition</code>.
     * <p>
     * If one of the data types isn't a predefined one, the project properties become invalid.
     * 
     * @throws NullPointerException if data types is <code>null</code>.
     */
    void setPredefinedDatatypesUsed(ValueDatatype[] datatypes);

    /**
     * Returns the value data types that are defined in this project.
     */
    IDynamicValueDatatype[] getDefinedValueDatatypes();

    /**
     * Returns all (value and other) data types that are defined in this project.
     */
    List<Datatype> getDefinedDatatypes();

    /**
     * Sets the value data types that are defined in this project.
     */
    void setDefinedDatatypes(IDynamicValueDatatype[] datatypes);

    /**
     * Sets the data types that are defined in this project.
     */
    void setDefinedDatatypes(Datatype[] datatypes);

    /**
     * Adds the defined value data type. If the project properties contain another data type with
     * the same id, the new data type replaces the old one.
     * 
     * @throws NullPointerException if data type is <code>null</code>.
     */
    void addDefinedDatatype(IDynamicValueDatatype datatype);

    /**
     * Adds the defined data type. If the project properties contain another data type with the same
     * id, the new data type replaces the old one.
     * 
     * @throws NullPointerException if data type is <code>null</code>.
     */
    void addDefinedDatatype(Datatype datatype);

    /**
     * Returns the prefix to be used for new runtime-IDs for product components.
     */
    String getRuntimeIdPrefix();

    /**
     * Sets the new prefix to be used for new runtime-IDs for product components.
     * 
     * @throws NullPointerException if the given prefix is <code>null</code>.
     */
    void setRuntimeIdPrefix(String runtimeIdPrefix);

    /**
     * Returns <code>true</code> if the rule is enabled, otherwise <code>false</code>. See the
     * message code for the violation of this rule for further details.
     * 
     * @see org.faktorips.devtools.model.type.IType#MSGCODE_MUST_SPECIFY_DERIVED_UNION
     */
    boolean isDerivedUnionIsImplementedRuleEnabled();

    /**
     * @see #isDerivedUnionIsImplementedRuleEnabled()
     */
    void setDerivedUnionIsImplementedRuleEnabled(boolean enabled);

    /**
     * Returns <code>true</code> if the rule is enabled, otherwise <code>false</code>. See the
     * message code for the violation of this rule for further details.
     * 
     * @see org.faktorips.devtools.model.productcmpt.IProductCmptGeneration#MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE
     */
    boolean isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled();

    /**
     * Check if the option to allow rules without references is enabled or not
     * 
     * @return true if the option is enabled
     */
    boolean isRulesWithoutReferencesAllowedEnabled();

    /**
     * @see #isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()
     */
    void setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(boolean enabled);

    /**
     * @see #isRulesWithoutReferencesAllowedEnabled()
     */
    void setRulesWithoutReferencesAllowedEnabled(boolean enabled);

    /**
     * Check if the inverse associations have to be type safe or not. Due to Issue FIPS-85 we need
     * to have to possibility to use the inverse association of the super type as inverse
     * association for a concrete type. When this property is true, these unsafe inverse
     * associations are allowed. Otherwise if this property is false you have to create a concrete
     * inverse association for every subset of a derived union with an inverse association.
     * <p>
     * If you have enabled the persistence/jpa mode, this proeprty have to be false!
     * 
     * @return true when unsafe inverse associations are allowed
     */
    boolean isSharedDetailToMasterAssociations();

    /**
     * Set this parameter true when unsafe inverse association are allowed. Set false when only type
     * safe inverse associations are allowed. . @see {@link #isSharedDetailToMasterAssociations()}
     * 
     * @param sharedDetailToMasterAssociations True to allow unsafe inverse associations
     */
    void setSharedDetailToMasterAssociations(boolean sharedDetailToMasterAssociations);

    /**
     * @return The IDs of all required features.
     */
    String[] getRequiredIpsFeatureIds();

    /**
     * @param featureId The id of the feature the minimum version has to be returned
     * @return The version number for the given feature id or <code>null</code>, if no entry is
     *             found for the given feature id.
     */
    String getMinRequiredVersionNumber(String featureId);

    /**
     * Sets the minimum version for the given feature id. If the feature id was not used before, a
     * new entry with the given feature id is created.
     * 
     * @param featureId The id of the required feature.
     * @param version The minimum version number for this feature.
     */
    void setMinRequiredVersionNumber(String featureId, String version);

    /**
     * Adds the given resource path to the list of excluded resources in the product definition.
     */
    void addResourcesPathExcludedFromTheProductDefiniton(String resourcesPath);

    /**
     * Returns <code>true</code> if the given resource will be excluded from the product
     * definition.<br>
     * If the given resource is relevant for the product definition the method returns
     * <code>false</code>.
     */
    boolean isResourceExcludedFromProductDefinition(String location);

    /**
     * Returns the resourcesPathExcludedFromTheProductDefiniton
     * 
     * @return A {@link Set} of {@link String} that contains the list of excluded paths for the
     *             product definition
     */
    Set<String> getResourcesPathExcludedFromTheProductDefiniton();

    /**
     * Setting the resourcesPathExcludedFromTheProductDefiniton.
     * 
     */
    void setResourcesPathExcludedFromTheProductDefiniton(
            Set<String> resourcesPathExcludedFromTheProductDefiniton);

    /**
     * Returns the persistence options for this IPS project, or <code>null</code> if the project
     * does not support persistence.
     * 
     * @see #isPersistenceSupportEnabled()
     */
    IPersistenceOptions getPersistenceOptions();

    /**
     * Returns an unmodifiable view on the set of languages supported by this IPS project.
     */
    Set<ISupportedLanguage> getSupportedLanguages();

    /**
     * Returns the {@link ISupportedLanguage} with the given {@link Locale} or null if the locale is
     * not supported.
     * 
     * @param locale The locale to retrieve the {@link ISupportedLanguage} for
     * 
     * @throws NullPointerException If the parameter is null
     */
    ISupportedLanguage getSupportedLanguage(Locale locale);

    /**
     * Returns whether the language corresponding to the given {@link Locale} is supported by this
     * IPS project.
     * 
     * @param locale The {@link Locale} to check whether it is supported
     * 
     * @throws NullPointerException If the parameter is null
     */
    boolean isSupportedLanguage(Locale locale);

    /**
     * Returns the {@link ISupportedLanguage} that is currently set as default language or the first
     * language if no supported language is explicitly set as default.
     */
    ISupportedLanguage getDefaultLanguage();

    /**
     * Sets the given {@link ISupportedLanguage} to be the default language.
     * 
     * @param language The {@link ISupportedLanguage} to set as default language
     * 
     * @throws NullPointerException If the parameter is null
     */
    void setDefaultLanguage(ISupportedLanguage language);

    /**
     * Sets the {@link ISupportedLanguage} with the given {@link Locale} to be the default language.
     * 
     * @param locale The {@link Locale} of the {@link ISupportedLanguage} to set as the default
     *            language
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If there is no supported language with the given locale
     */
    void setDefaultLanguage(Locale locale);

    /**
     * Adds the language identified by the given {@link Locale} to the supported languages of this
     * IPS project.
     * <p>
     * A call to this operation does nothing if the language is already supported.
     * 
     * @param locale The {@link Locale} identifying the language to be supported from now on
     * 
     * @throws NullPointerException If the parameter is null
     */
    void addSupportedLanguage(Locale locale);

    /**
     * Removes the given {@link ISupportedLanguage} from the list of supported languages causing it
     * to be no longer supported.
     * <p>
     * Does nothing if there is no such supported language.
     * 
     * @param supportedLanguage The {@link ISupportedLanguage} that is no longer supported
     * 
     * @throws NullPointerException If the parameter is null
     */
    void removeSupportedLanguage(ISupportedLanguage supportedLanguage);

    /**
     * Removes the {@link ISupportedLanguage} with the given {@link Locale} from the list of
     * supported languages causing it to be no longer supported.
     * <p>
     * Does nothing if there is no supported language with the given locale.
     * 
     * @param locale The {@link Locale} of the {@link ISupportedLanguage} to remove
     * 
     * @throws NullPointerException If the parameter is null
     */
    void removeSupportedLanguage(Locale locale);

    /**
     * Return the version that was set in this project properties.
     * <p>
     * You should never call this method directly! Instead you should get the
     * {@link IVersionProvider} from {@link IIpsProject} by calling
     * {@link IIpsProject#getVersionProvider()}. This method only returns the valid version if it is
     * configured directly in the project properties. However the version of the project may come
     * from different locations depending on its {@link IVersionProvider}.
     * 
     * @return the version string of this project
     */
    String getVersion();

    /**
     * Setting a new version that should be stored in this project properties.
     * <p>
     * You should never call this method directly! Instead you should use the
     * {@link IVersionProvider} from {@link IIpsProject} by calling
     * {@link IIpsProject#getVersionProvider()}. The version could be set in different locations
     * depending on the {@link IVersionProvider}. Use
     * {@link IVersionProvider#setProjectVersion(org.faktorips.devtools.model.IVersion)} to always
     * write to the correct location.
     * 
     * 
     * @param version The new version of this project
     */
    void setVersion(String version);

    /**
     * Provides the id of the version provider that is configured in these project properties. If no
     * version provider id is set - this method returns <code>null</code>, the
     * {@code DefaultVersionProvider} should be used.
     * 
     * @return The configured {@link IVersionProvider} that should be used to handle the project's
     *             version. Could be <code>null</code> if no explicit version provider is set and
     *             the {@code DefaultVersionProvider} should be used.
     */
    String getVersionProviderId();

    /**
     * Set the id of the version provider that should be used by the corresponding project to handle
     * its versions. If set to <code>null</code> the {@code DefaultVersionProvider} is used.
     * 
     * @param versionProviderId The id of an extended {@link IVersionProvider}.
     */
    void setVersionProviderId(String versionProviderId);

    /**
     * Returns the id of the release extension associated with this project or {@code null} if no
     * release extension is configured.
     * 
     * @return The id of the release extension for this project
     */
    @CheckForNull
    String getReleaseExtensionId();

    /**
     * Setting the id of the release extension associated with this project
     * 
     * @param releaseExtensionId The new id of the release extension for this project or
     *            {@code null} to remove a previously set extension
     */
    void setReleaseExtensionId(@CheckForNull String releaseExtensionId);

    /**
     * Setting the default currency for this project
     * 
     * @param defaultCurrency the default currency
     */
    void setDefaultCurrency(Currency defaultCurrency);

    /**
     * Getting the default currency configured for this project
     * 
     * @return the default currency
     */
    Currency getDefaultCurrency();

    /**
     * Returns whether the given {@link IFunctionResolverFactory} is active for the
     * {@link IIpsProject} configured by these {@link IIpsProjectProperties}
     * 
     * @param factory the {@link IFunctionResolverFactory} in question
     * @return whether the given {@link IFunctionResolverFactory} is active for the
     *             {@link IIpsProject}
     */
    boolean isActive(IFunctionResolverFactory<?> factory);

    /**
     * Returns the language in which the expression language's functions are used. E.g. the
     * <code>if</code> function is called IF in English, but WENN in German.
     */
    Locale getFormulaLanguageLocale();

    /**
     * Sets the language in which the expression language's functions are used.
     */
    void setFormulaLanguageLocale(Locale locale);

    /**
     * Returns a set of strings representing the qualified names of all {@link EnumType EnumTypes}
     * that are used to define markers for {@link IValidationRule IValidationRules}.
     */
    LinkedHashSet<String> getMarkerEnums();

    /**
     * Adds an {@link EnumType} represented by its qualifiedName to the set of existing marker
     * enums.
     */
    void addMarkerEnum(String qualifiedName);

    /**
     * Removes an {@link EnumType} represented by its qualifiedName from the set of existing marker
     * enums.
     */
    void removeMarkerEnum(String qualifiedName);

    /**
     * Check if the option to allow usage of marker enums is enabled or not
     * 
     * @return <code>true</code> if the option is enabled
     */
    boolean isMarkerEnumsEnabled();

    /**
     * @see #isMarkerEnumsEnabled()
     */
    void setMarkerEnumsEnabled(boolean enabled);

    /**
     * Check if the default state for changing over time flag on {@link IProductCmptType
     * IProductCmptTypes} is enabled or disabled.
     * 
     * @return <code>false</code> if the default is disabled, <code>true</code> if the default is
     *             enabled or not configured in the .ipsproject file
     * @see IProductCmptType#setChangingOverTime(boolean)
     */
    boolean isChangingOverTimeDefaultEnabled();

    /**
     * @see #isChangingOverTimeDefaultEnabled()
     */
    void setChangingOverTimeDefault(boolean enabled);

    /**
     * Returns the threshold used to find a common property value when inferring a template.
     */
    Decimal getInferredTemplatePropertyValueThreshold();

    /**
     * @see #getInferredTemplatePropertyValueThreshold()
     */
    void setInferredTemplatePropertyValueThreshold(Decimal inferredTemplatePropertyValueThreshold);

    /**
     * Returns the threshold used to find a common link when inferring a template.
     */
    Decimal getInferredTemplateLinkThreshold();

    /**
     * @see #getInferredTemplateLinkThreshold()
     */
    void setInferredTemplateLinkThreshold(Decimal inferredTemplateLinkThreshold);

    /**
     * Returns the {@link IIpsFeatureConfiguration} for the feature identified by the given ID.
     *
     * @see #getRequiredIpsFeatureIds()
     */
    IIpsFeatureConfiguration getFeatureConfiguration(String featureId);

    /**
     * Returns the severity for validation messages when two product components have the same kindId
     * and versionId.
     */
    Severity getDuplicateProductComponentSeverity();

    /**
     * @see #getDuplicateProductComponentSeverity()
     */
    void setDuplicateProductComponentSeverity(Severity duplicateProductComponentSeverity);

    /**
     * Returns the severity for validation messages when model and persistence constraints don't
     * match
     */
    Severity getPersistenceColumnSizeChecksSeverity();

    /**
     * @see #getPersistenceColumnSizeChecksSeverity()
     */
    void setPersistenceColumnSizeChecksSeverity(Severity duplicateProductComponentSeverity);

    /**
     * Returns which format is used to save table contents.
     *
     * @since 20.12
     *
     * @see TableContentFormat
     */
    TableContentFormat getTableContentFormat();

    /**
     * @see #getTableContentFormat()
     */
    void setTableContentFormat(TableContentFormat tableContentFormat);

    /**
     * Returns true if newly created policy component types should always have
     * {@link IPolicyCmptType#isGenerateValidatorClass()} enabled.
     *
     * @since 20.12
     */
    boolean isGenerateValidatorClassDefaultEnabled();

    /**
     * @see #isGenerateValidatorClassDefaultEnabled()
     */
    void setGenerateValidatorClassDefault(boolean enabled);

    /**
     * Returns true if newly created policy component type attributes should always have
     * {@link IPolicyCmptTypeAttribute#isGenericValidationEnabled()} enabled.
     *
     * @since 21.6
     */
    boolean isGenericValidationDefaultEnabled();

    /**
     * @see #isGenericValidationDefaultEnabled()
     */
    void setGenericValidationDefault(boolean enabled);

    /**
     * Returns {@code true} if non-standard blanks should be escaped to their XML entity (e.g.
     * non-breaking space {@code U+00A0} to {@code &#160;}).
     * 
     */
    boolean isEscapeNonStandardBlanks();

    /**
     * @see #isEscapeNonStandardBlanks()
     */
    void setEscapeNonStandardBlanks(boolean enabled);

    /**
     * Whether Ips-Files should be validated against their XSD schema. When {@code true} set the
     * xmlns header of the XML to the corresponding xsd schema of the ips object.
     */
    boolean isValidateIpsSchema();

    /**
     * @see #isValidateIpsSchema()
     */
    void setValidateIpsSchema(boolean validateIpsSchema);
}
