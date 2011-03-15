/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.message.MessageList;

/**
 * Properties of the IPS project. The IPS project can't keep the properties itself, as it is a
 * handle. The properties are persisted in the ".ipsproject" file.
 * 
 * @author Jan Ortmann
 */
public interface IIpsProjectProperties {

    public final static String PROPERTY_BUILDER_SET_ID = "builderSetId"; //$NON-NLS-1$

    public final static String PROPERTY_CONTAINER_RELATIONS_MUST_BE_IMPLEMENTED = "containerRelationIsImplementedRuleEnabled"; // $NON-NLS-1$ //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the IPS artifact builder set id is unknown.
     */
    public final static String MSGCODE_UNKNOWN_BUILDER_SET_ID = MSGCODE_PREFIX + "UnknwonBuilderSetId"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a used predefined data type is unknown.
     */
    public final static String MSGCODE_UNKNOWN_PREDEFINED_DATATYPE = MSGCODE_PREFIX + "UnknownPredefinedDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the minimum required version number for a specific
     * feature is missing.
     */
    public final static String MSGCODE_MISSING_MIN_FEATURE_ID = MSGCODE_PREFIX + "MissingMinFeatureId"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component naming strategy can't be
     * found.
     */
    public final static String MSGCODE_INVALID_PRODUCT_CMPT_NAMING_STRATEGY = MSGCODE_PREFIX
            + "InvalidProductCmptNamingStrategy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the language identifier used as locale for a
     * supported language is not a valid ISO 639 language code.
     */
    public final static String MSGCODE_SUPPORTED_LANGUAGE_UNKNOWN_LOCALE = MSGCODE_PREFIX
            + "SupportedLanguageUnknownLocale"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that more than one supported language is marked as
     * default language.
     */
    public final static String MSGCODE_MORE_THAN_ONE_DEFAULT_LANGUAGE = MSGCODE_PREFIX + "MoreThanOneDefaultLanguage"; //$NON-NLS-1$

    public static final String MSGCODE_INVALID_OPTIONAL_CONSTRAINT = MSGCODE_PREFIX + "invalidOptionalConstraint"; //$NON-NLS-1$

    /**
     * Returns the time stamp of the last persistent modification of this object.
     */
    public Long getLastPersistentModificationTimestamp();

    /**
     * Sets the time stamp of the last persistent modification of this object.
     */
    public void setLastPersistentModificationTimestamp(Long timestamp);

    /**
     * Validates the project properties.
     */
    public MessageList validate(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns id of the builder set used to generate sourcecode from the model / product
     * definition.
     */
    public String getBuilderSetId();

    /**
     * Sets the id of the builder set used to generate sourcecode from the model / product
     * definition.
     */
    public void setBuilderSetId(String id);

    /**
     * Returns the IpsProject specific configuration of the IIpsArtefactBuilderSet.
     */
    public IIpsArtefactBuilderSetConfigModel getBuilderSetConfig();

    /**
     * Sets the IpsProjects specific configuration of the IIpsArtefactBuilderSet.
     */
    public void setBuilderSetConfig(IIpsArtefactBuilderSetConfigModel config);

    /**
     * Returns the object path to lookup objects.
     */
    public IIpsObjectPath getIpsObjectPath();

    /**
     * Sets the object path.
     */
    public void setIpsObjectPath(IIpsObjectPath path);

    /**
     * Returns <code>true</code> if this is a project containing a (part of a) model, otherwise
     * <code>false</code>. The model is made up of police component types, product component types
     * and so on.
     */
    public boolean isModelProject();

    /**
     * Sets if this is project containing model elements or not.
     */
    public void setModelProject(boolean modelProject);

    /**
     * Returns <code>true</code> if this is a project containing product definition data, otherwise
     * <code>false</code>. Product definition projects are shown in the product definition
     * perspective.
     */
    public boolean isProductDefinitionProject();

    /**
     * Sets if this project contains product definition data.
     */
    public void setProductDefinitionProject(boolean productDefinitionProject);

    /**
     * Returns <code>true</code> if this is a project that supports persistence, otherwise
     * <code>false</code>. Persistent projects can store and retrieve policy component types to/from
     * a relational database.
     */
    public boolean isPersistenceSupportEnabled();

    /**
     * Sets if this project supports persistence.
     */
    public void setPersistenceSupport(boolean persistentProject);

    /**
     * Returns the strategy how product component names are composed.
     */
    public IProductCmptNamingStrategy getProductCmptNamingStrategy();

    /**
     * Sets the strategy how product component names are composed.
     */
    public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy);

    /**
     * Returns the strategy used to name database tables used for persisting policy component types.
     * Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    public ITableNamingStrategy getTableNamingStrategy();

    /**
     * Sets the strategy how database table names are composed. Does nothing if persistence support
     * is not enabled.
     */
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy);

    /**
     * Returns the strategy used to name database columns used for persisting policy component
     * types. Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    public ITableColumnNamingStrategy getTableColumnNamingStrategy();

    /**
     * Sets the strategy how database table column names are composed. Does nothing if persistence
     * support is not enabled.
     */
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy);

    /**
     * Sets the naming convention for changes over time (by id) used in the generated sourcecode.
     * 
     * @see IChangesOverTimeNamingConvention
     */
    public void setChangesOverTimeNamingConventionIdForGeneratedCode(String changesInTimeConventionIdForGeneratedCode);

    /**
     * Returns the id of the naming convention for changes over time used in the generated
     * sourcecode.
     * 
     * @see IChangesOverTimeNamingConvention
     */
    public String getChangesOverTimeNamingConventionIdForGeneratedCode();

    /**
     * Returns predefined data types (by id) used by this project. Predefined data types are those
     * that are defined by the extension <code>datatypeDefinition</code>.
     */
    public String[] getPredefinedDatatypesUsed();

    /**
     * Sets the predefined data types (by id) used by this project. Predefined data types are those
     * that are defined by the extension <code>datatypeDefinition</code>.
     * 
     * @throws NullPointerException if data types is <code>null</code>.
     */
    public void setPredefinedDatatypesUsed(String[] datatypes);

    /**
     * Sets the predefined data types used by this project. Predefined data types are those that are
     * defined by the extension <code>datatypeDefinition</code>.
     * <p>
     * If one of the data types isn't a predefined one, the project properties become invalid.
     * 
     * @throws NullPointerException if data types is <code>null</code>.
     */
    public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes);

    /**
     * Returns the value data types that are defined in this project.
     */
    public DynamicValueDatatype[] getDefinedValueDatatypes();

    /**
     * Returns all (value and other) data types that are defined in this project.
     */
    public List<Datatype> getDefinedDatatypes();

    /**
     * Sets the value data types that are defined in this project.
     */
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes);

    /**
     * Sets the data types that are defined in this project.
     */
    public void setDefinedDatatypes(Datatype[] datatypes);

    /**
     * Adds the defined value data type. If the project properties contain another data type with
     * the same id, the new data type replaces the old one.
     * 
     * @throws NullPointerException if data type is <code>null</code>.
     */
    public void addDefinedDatatype(DynamicValueDatatype datatype);

    /**
     * Adds the defined data type. If the project properties contain another data type with the same
     * id, the new data type replaces the old one.
     * 
     * @throws NullPointerException if data type is <code>null</code>.
     */
    public void addDefinedDatatype(Datatype datatype);

    /**
     * Returns the prefix to be used for new runtime-IDs for product components.
     */
    public String getRuntimeIdPrefix();

    /**
     * Sets the new prefix to be used for new runtime-IDs for product components.
     * 
     * @throws NullPointerException if the given prefix is <code>null</code>.
     */
    public void setRuntimeIdPrefix(String runtimeIdPrefix);

    /**
     * Returns <code>true</code> if the Java project belonging to the IPS project, contains (value)
     * classes that are used as defined dynamic data type, otherwise <code>false</code>.
     * <p>
     * Note that is preferable to develop and access these classes either in a separate Java project
     * or to provide them in a JAR file. The reason for this is that in this scenario the clean
     * build won't work properly. When the IpsBuilder builds the project the dynamic data type needs
     * to load the class it is based upon. However as the Java builder hasn't compiled the Java
     * source file into a class file the dynamic data type won't find it's class, the data type
     * becomes invalid and hence we can't build the project.
     * 
     * @see DynamicValueDatatype
     * @see org.faktorips.devtools.core.internal.model.ipsproject.ClassLoaderProvider
     */
    public boolean isJavaProjectContainsClassesForDynamicDatatypes();

    /**
     * @see #isJavaProjectContainsClassesForDynamicDatatypes()
     */
    public void setJavaProjectContainsClassesForDynamicDatatypes(boolean newValue);

    /**
     * Returns <code>true</code> if the rule is enabled, otherwise <code>false</code>. See the
     * message code for the violation of this rule for further details.
     * 
     * @see org.faktorips.devtools.core.model.type.IType#MSGCODE_MUST_SPECIFY_DERIVED_UNION
     */
    public boolean isDerivedUnionIsImplementedRuleEnabled();

    /**
     * @see #isDerivedUnionIsImplementedRuleEnabled()
     */
    public void setDerivedUnionIsImplementedRuleEnabled(boolean enabled);

    /**
     * Returns <code>true</code> if the rule is enabled, otherwise <code>false</code>. See the
     * message code for the violation of this rule for further details.
     * 
     * @see org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration#MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE
     */
    public boolean isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled();

    /**
     * Check if the option to allow rules without references is enabled or not
     * 
     * @return true if the option is enabled
     */
    public boolean isRulesWithoutReferencesAllowedEnabled();

    /**
     * @see #isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()
     */
    public void setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(boolean enabled);

    /**
     * @see #isRulesWithoutReferencesAllowedEnabled()
     */
    public void setRulesWithoutReferencesAllowedEnabled(boolean enabled);

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
    public String[] getRequiredIpsFeatureIds();

    /**
     * @param featureId The id of the feature the minimum version has to be returned
     * @return The version number for the given feature id or <code>null</code>, if no entry is
     *         found for the given feature id.
     */
    public String getMinRequiredVersionNumber(String featureId);

    /**
     * Sets the minimum version for the given feature id. If the feature id was not used before, a
     * new entry with the given feature id is created.
     * 
     * @param featureId The id of the required feature.
     * @param version The minimum version number for this feature.
     */
    public void setMinRequiredVersionNumber(String featureId, String version);

    /**
     * Adds the given resource path to the list of excluded resources in the product definition.
     */
    public void addResourcesPathExcludedFromTheProductDefiniton(String resourcesPath);

    /**
     * Returns <code>true</code> if the given resource will be excluded from the product definition.<br>
     * If the given resource is relevant for the product definition the method returns
     * <code>false</code>.
     */
    public boolean isResourceExcludedFromProductDefinition(String location);

    /**
     * Returns the resourcesPathExcludedFromTheProductDefiniton
     * 
     * @return A {@link Set} of {@link String} that contains the list of excluded paths for the
     *         product definition
     */
    public Set<String> getResourcesPathExcludedFromTheProductDefiniton();

    /**
     * Setting the resourcesPathExcludedFromTheProductDefiniton.
     * 
     */
    public void setResourcesPathExcludedFromTheProductDefiniton(Set<String> resourcesPathExcludedFromTheProductDefiniton);

    /**
     * Returns the assigned user group allowed for a question in the IPS project.
     */
    public EnumType getQuestionAssignedUserGroup();

    /**
     * Returns the question status allowed in the IPS project.
     */
    public EnumType getQuestionStatus();

    /**
     * Returns the persistence options for this IPS project, or <code>null</code> if the project
     * does not support persistence.
     * 
     * @see #isPersistenceSupportEnabled()
     */
    public IPersistenceOptions getPersistenceOptions();

    /**
     * Returns an unmodifiable view on the set of languages supported by this IPS project.
     */
    public Set<ISupportedLanguage> getSupportedLanguages();

    /**
     * Returns the {@link ISupportedLanguage} with the given {@link Locale} or null if the locale is
     * not supported.
     * 
     * @param locale The locale to retrieve the {@link ISupportedLanguage} for
     * 
     * @throws NullPointerException If the parameter is null
     */
    public ISupportedLanguage getSupportedLanguage(Locale locale);

    /**
     * Returns whether the language corresponding to the given {@link Locale} is supported by this
     * IPS project.
     * 
     * @param locale The {@link Locale} to check whether it is supported
     * 
     * @throws NullPointerException If the parameter is null
     */
    public boolean isSupportedLanguage(Locale locale);

    /**
     * Returns the {@link ISupportedLanguage} that is currently set as default language or null if
     * there is no default language set at this moment.
     */
    public ISupportedLanguage getDefaultLanguage();

    /**
     * Sets the given {@link ISupportedLanguage} to be the default language.
     * 
     * @param language The {@link ISupportedLanguage} to set as default language
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setDefaultLanguage(ISupportedLanguage language);

    /**
     * Sets the {@link ISupportedLanguage} with the given {@link Locale} to be the default language.
     * 
     * @param locale The {@link Locale} of the {@link ISupportedLanguage} to set as the default
     *            language
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If there is no supported language with the given locale
     */
    public void setDefaultLanguage(Locale locale);

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
    public void addSupportedLanguage(Locale locale);

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
    public void removeSupportedLanguage(ISupportedLanguage supportedLanguage);

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
    public void removeSupportedLanguage(Locale locale);

    /**
     * Return the version of this project. The version is needed in deployment process and should be
     * updated for a new deployment.
     * 
     * @return the version string of this project
     */
    public String getVersion();

    /**
     * Setting a new version for this project. This should be done by deployment process.
     * 
     * @param version The new version of this project
     */
    public void setVersion(String version);

    /**
     * Getting the id of the release extension associated with this project
     * 
     * @return The id of the release extension for this project
     */
    public String getReleaseExtensionId();

}
