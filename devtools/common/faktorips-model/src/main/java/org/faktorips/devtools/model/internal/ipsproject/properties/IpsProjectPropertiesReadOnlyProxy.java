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

import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.datatype.IDynamicValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsFeatureConfiguration;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.model.ipsproject.TableContentFormat;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.Decimal;

/**
 * A proxy implementation of the IIpsProjectProperties interface that delegates all method calls
 * which do not change the object to the underlying IIpsProjectProperties implementation. Calls to
 * methods that would change the object throw a runtime exception since this proxy prohibits
 * manipulation of the underlying instance.
 * 
 * @author Peter Erzberger
 */
public class IpsProjectPropertiesReadOnlyProxy implements IIpsProjectProperties {

    private static final String ERROR_READ_ONLY = "This is a read only object and can therefore not be manipulated."; //$NON-NLS-1$

    private IIpsProjectProperties propertiesInternal;

    public IpsProjectPropertiesReadOnlyProxy(IIpsProjectProperties propertiesInternal) {
        ArgumentCheck.notNull(propertiesInternal, this);
        this.propertiesInternal = propertiesInternal;
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void addDefinedDatatype(IDynamicValueDatatype datatype) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public IIpsArtefactBuilderSetConfigModel getBuilderSetConfig() {
        return propertiesInternal.getBuilderSetConfig();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public String getBuilderSetId() {
        return propertiesInternal.getBuilderSetId();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public String getChangesOverTimeNamingConventionIdForGeneratedCode() {
        return propertiesInternal.getChangesOverTimeNamingConventionIdForGeneratedCode();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public IDynamicValueDatatype[] getDefinedValueDatatypes() {
        return propertiesInternal.getDefinedValueDatatypes();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public List<Datatype> getDefinedDatatypes() {
        return propertiesInternal.getDefinedDatatypes();
    }

    /**
     * {@inheritDoc}
     * 
     * Remark: Returns not a read only object of the IPS object path. This is an accepted
     * inconsistency to this read only object.
     */
    @Override
    public IIpsObjectPath getIpsObjectPath() {
        return propertiesInternal.getIpsObjectPath();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public String getMinRequiredVersionNumber(String featureId) {
        return propertiesInternal.getMinRequiredVersionNumber(featureId);
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public String[] getPredefinedDatatypesUsed() {
        return propertiesInternal.getPredefinedDatatypesUsed();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
        return propertiesInternal.getProductCmptNamingStrategy();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public String[] getRequiredIpsFeatureIds() {
        return propertiesInternal.getRequiredIpsFeatureIds();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public String getRuntimeIdPrefix() {
        return propertiesInternal.getRuntimeIdPrefix();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public boolean isDerivedUnionIsImplementedRuleEnabled() {
        return propertiesInternal.isDerivedUnionIsImplementedRuleEnabled();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public boolean isModelProject() {
        return propertiesInternal.isModelProject();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public boolean isProductDefinitionProject() {
        return propertiesInternal.isProductDefinitionProject();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public boolean isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled() {
        return propertiesInternal.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled();
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setBuilderSetConfig(IIpsArtefactBuilderSetConfigModel config) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setBuilderSetId(String id) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setChangesOverTimeNamingConventionIdForGeneratedCode(String changesInTimeConventionIdForGeneratedCode) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setDefinedDatatypes(IDynamicValueDatatype[] datatypes) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void addDefinedDatatype(Datatype datatype) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setDefinedDatatypes(Datatype[] datatypes) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setDerivedUnionIsImplementedRuleEnabled(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setIpsObjectPath(IIpsObjectPath path) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setMinRequiredVersionNumber(String featureId, String version) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isRulesWithoutReferencesAllowedEnabled() {
        return propertiesInternal.isRulesWithoutReferencesAllowedEnabled();
    }

    @Override
    public boolean isSharedDetailToMasterAssociations() {
        return propertiesInternal.isSharedDetailToMasterAssociations();
    }

    @Override
    public Set<String> getResourcesPathExcludedFromTheProductDefiniton() {
        return propertiesInternal.getResourcesPathExcludedFromTheProductDefiniton();
    }

    @Override
    public void setResourcesPathExcludedFromTheProductDefiniton(
            Set<String> resourcesPathExcludedFromTheProductDefiniton) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void addResourcesPathExcludedFromTheProductDefiniton(String resourcesPath) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isResourceExcludedFromProductDefinition(String location) {
        return propertiesInternal.isResourceExcludedFromProductDefinition(location);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setModelProject(boolean modelProject) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setPredefinedDatatypesUsed(String[] datatypes) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setProductDefinitionProject(boolean productDefinitionProject) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setRulesWithoutReferencesAllowedEnabled(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setSharedDetailToMasterAssociations(boolean sharedDetailToMasterAssociations) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setRuntimeIdPrefix(String runtimeIdPrefix) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public MessageList validate(IIpsProject ipsProject) {
        return propertiesInternal.validate(ipsProject);
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public long getLastPersistentModificationTimestamp() {
        return propertiesInternal.getLastPersistentModificationTimestamp();
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setLastPersistentModificationTimestamp(long timestamp) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public IPersistenceOptions getPersistenceOptions() {
        return propertiesInternal.getPersistenceOptions();
    }

    @Override
    public ITableColumnNamingStrategy getTableColumnNamingStrategy() {
        return propertiesInternal.getTableColumnNamingStrategy();
    }

    @Override
    public ITableNamingStrategy getTableNamingStrategy() {
        return propertiesInternal.getTableNamingStrategy();
    }

    @Override
    public boolean isPersistenceSupportEnabled() {
        return propertiesInternal.isPersistenceSupportEnabled();
    }

    @Override
    public void setPersistenceSupport(boolean persistentProject) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    // --
    // ## Methods related to multi-language support
    // --

    @Override
    public Set<ISupportedLanguage> getSupportedLanguages() {
        return propertiesInternal.getSupportedLanguages();
    }

    @Override
    public ISupportedLanguage getSupportedLanguage(Locale locale) {
        return propertiesInternal.getSupportedLanguage(locale);
    }

    @Override
    public boolean isSupportedLanguage(Locale locale) {
        return propertiesInternal.isSupportedLanguage(locale);
    }

    @Override
    public ISupportedLanguage getDefaultLanguage() {
        return propertiesInternal.getDefaultLanguage();
    }

    @Override
    public void setDefaultLanguage(ISupportedLanguage supportedLanguage) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setDefaultLanguage(Locale locale) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void addSupportedLanguage(Locale locale) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void removeSupportedLanguage(ISupportedLanguage supportedLanguage) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void removeSupportedLanguage(Locale locale) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setDefaultCurrency(Currency defaultCurrency) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public Currency getDefaultCurrency() {
        return propertiesInternal.getDefaultCurrency();
    }

    @Override
    public String getVersion() {
        return propertiesInternal.getVersion();
    }

    @Override
    public void setVersion(String version) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public String getVersionProviderId() {
        return propertiesInternal.getVersionProviderId();
    }

    @Override
    public void setVersionProviderId(String versionProviderId) {
        propertiesInternal.getVersionProviderId();
    }

    @Override
    public String getReleaseExtensionId() {
        return propertiesInternal.getReleaseExtensionId();
    }

    @Override
    public void setReleaseExtensionId(String releaseExtensionId) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isActive(IFunctionResolverFactory<?> factory) {
        return propertiesInternal.isActive(factory);
    }

    @Override
    public Locale getFormulaLanguageLocale() {
        return propertiesInternal.getFormulaLanguageLocale();
    }

    @Override
    public void setFormulaLanguageLocale(Locale locale) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public LinkedHashSet<String> getMarkerEnums() {
        return propertiesInternal.getMarkerEnums();
    }

    @Override
    public void addMarkerEnum(String qualifiedName) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void removeMarkerEnum(String qualifiedName) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isMarkerEnumsEnabled() {
        return propertiesInternal.isMarkerEnumsEnabled();
    }

    @Override
    public void setMarkerEnumsEnabled(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isChangingOverTimeDefaultEnabled() {
        return propertiesInternal.isChangingOverTimeDefaultEnabled();
    }

    @Override
    public void setChangingOverTimeDefault(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public void setInferredTemplatePropertyValueThreshold(Decimal relativePropertyValueThreshold) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public Decimal getInferredTemplatePropertyValueThreshold() {
        return propertiesInternal.getInferredTemplatePropertyValueThreshold();
    }

    @Override
    public void setInferredTemplateLinkThreshold(Decimal relativeLinkThreshold) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public Decimal getInferredTemplateLinkThreshold() {
        return propertiesInternal.getInferredTemplateLinkThreshold();
    }

    @Override
    public IIpsFeatureConfiguration getFeatureConfiguration(String featureId) {
        return propertiesInternal.getFeatureConfiguration(featureId);
    }

    @Override
    public void setDuplicateProductComponentSeverity(Severity duplicateProductComponentSeverity) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public Severity getDuplicateProductComponentSeverity() {
        return propertiesInternal.getDuplicateProductComponentSeverity();
    }

    @Override
    public void setPersistenceColumnSizeChecksSeverity(Severity duplicateProductComponentSeverity) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public Severity getPersistenceColumnSizeChecksSeverity() {
        return propertiesInternal.getPersistenceColumnSizeChecksSeverity();
    }

    @Override
    public TableContentFormat getTableContentFormat() {
        return propertiesInternal.getTableContentFormat();
    }

    @Override
    public void setTableContentFormat(TableContentFormat tableContentFormat) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isGenerateValidatorClassDefaultEnabled() {
        return propertiesInternal.isGenerateValidatorClassDefaultEnabled();
    }

    @Override
    public void setGenerateValidatorClassDefault(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isGenericValidationDefaultEnabled() {
        return propertiesInternal.isGenericValidationDefaultEnabled();
    }

    @Override
    public void setGenericValidationDefault(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isEscapeNonStandardBlanks() {
        return propertiesInternal.isEscapeNonStandardBlanks();
    }

    @Override
    public void setEscapeNonStandardBlanks(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }

    @Override
    public boolean isValidateIpsSchema() {
        return propertiesInternal.isValidateIpsSchema();
    }

    @Override
    public void setValidateIpsSchema(boolean enabled) {
        throw new RuntimeException(ERROR_READ_ONLY);
    }
}
