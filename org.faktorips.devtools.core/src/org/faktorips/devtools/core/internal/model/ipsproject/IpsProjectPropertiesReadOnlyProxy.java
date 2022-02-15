/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsFeatureConfiguration;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;
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

    private IIpsProjectProperties propertiesInternal;

    public IpsProjectPropertiesReadOnlyProxy(IIpsProjectProperties propertiesInternal) {
        ArgumentCheck.notNull(propertiesInternal, this);
        this.propertiesInternal = propertiesInternal;
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void addDefinedDatatype(DynamicValueDatatype datatype) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
    public DynamicValueDatatype[] getDefinedValueDatatypes() {
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
     * Remark: Returns not a read only object of the ips object path. This is an accepted
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setBuilderSetId(String id) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setChangesOverTimeNamingConventionIdForGeneratedCode(String changesInTimeConventionIdForGeneratedCode) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDefinedDatatype(Datatype datatype) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefinedDatatypes(Datatype[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setDerivedUnionIsImplementedRuleEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setIpsObjectPath(IIpsObjectPath path) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setMinRequiredVersionNumber(String featureId, String version) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void addResourcesPathExcludedFromTheProductDefiniton(String resourcesPath) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setPredefinedDatatypesUsed(String[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setProductDefinitionProject(boolean productDefinitionProject) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setRulesWithoutReferencesAllowedEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void setSharedDetailToMasterAssociations(boolean sharedDetailToMasterAssociations) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    @Override
    public void setRuntimeIdPrefix(String runtimeIdPrefix) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void setDefaultLanguage(Locale locale) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void addSupportedLanguage(Locale locale) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void removeSupportedLanguage(ISupportedLanguage supportedLanguage) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void removeSupportedLanguage(Locale locale) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void setDefaultCurrency(Currency defaultCurrency) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public boolean isAssociationsInFormulas() {
        return propertiesInternal.isAssociationsInFormulas();
    }

    @Override
    public void setAssociationsInFormulas(boolean associationsInFormulas) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
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
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public LinkedHashSet<String> getMarkerEnums() {
        return propertiesInternal.getMarkerEnums();
    }

    @Override
    public void addMarkerEnum(String qualifiedName) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void removeMarkerEnum(String qualifiedName) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public boolean isMarkerEnumsEnabled() {
        return propertiesInternal.isMarkerEnumsEnabled();
    }

    @Override
    public void setMarkerEnumsEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public boolean isBusinessFunctionsForValidationRulesEnabled() {
        return propertiesInternal.isBusinessFunctionsForValidationRulesEnabled();
    }

    @Override
    public void setBusinessFunctionsForValidationRules(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefore not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public boolean isChangingOverTimeDefaultEnabled() {
        return propertiesInternal.isChangingOverTimeDefaultEnabled();
    }

    @Override
    public void setChangingOverTimeDefault(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public void setInferredTemplatePropertyValueThreshold(Decimal relativePropertyValueThreshold) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public Decimal getInferredTemplatePropertyValueThreshold() {
        return propertiesInternal.getInferredTemplatePropertyValueThreshold();
    }

    @Override
    public void setInferredTemplateLinkThreshold(Decimal relativeLinkThreshold) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated."); //$NON-NLS-1$
    }

    @Override
    public Decimal getInferredTemplateLinkThreshold() {
        return propertiesInternal.getInferredTemplateLinkThreshold();
    }

    @Override
    public IIpsFeatureConfiguration getFeatureConfiguration(String featureId) {
        return propertiesInternal.getFeatureConfiguration(featureId);
    }

}
