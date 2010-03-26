/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

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

    /**
     * @param propertiesInternal
     */
    public IpsProjectPropertiesReadOnlyProxy(IIpsProjectProperties propertiesInternal) {
        ArgumentCheck.notNull(propertiesInternal, this);
        this.propertiesInternal = propertiesInternal;
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void addDefinedDatatype(DynamicValueDatatype datatype) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public IIpsArtefactBuilderSetConfigModel getBuilderSetConfig() {
        return propertiesInternal.getBuilderSetConfig();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public String getBuilderSetId() {
        return propertiesInternal.getBuilderSetId();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public String getChangesOverTimeNamingConventionIdForGeneratedCode() {
        return propertiesInternal.getChangesOverTimeNamingConventionIdForGeneratedCode();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public DynamicValueDatatype[] getDefinedValueDatatypes() {
        return propertiesInternal.getDefinedValueDatatypes();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public List<Datatype> getDefinedDatatypes() {
        return propertiesInternal.getDefinedDatatypes();
    }

    /**
     * {@inheritDoc}
     * 
     * Remark: Returns not a read only object of the ips object path. This is an accepted
     * inconsistency to this read only object.
     */
    public IIpsObjectPath getIpsObjectPath() {
        return propertiesInternal.getIpsObjectPath();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public String getMinRequiredVersionNumber(String featureId) {
        return propertiesInternal.getMinRequiredVersionNumber(featureId);
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public String[] getPredefinedDatatypesUsed() {
        return propertiesInternal.getPredefinedDatatypesUsed();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
        return propertiesInternal.getProductCmptNamingStrategy();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public String[] getRequiredIpsFeatureIds() {
        return propertiesInternal.getRequiredIpsFeatureIds();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public String getRuntimeIdPrefix() {
        return propertiesInternal.getRuntimeIdPrefix();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public boolean isDerivedUnionIsImplementedRuleEnabled() {
        return propertiesInternal.isDerivedUnionIsImplementedRuleEnabled();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public boolean isJavaProjectContainsClassesForDynamicDatatypes() {
        return propertiesInternal.isJavaProjectContainsClassesForDynamicDatatypes();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public boolean isModelProject() {
        return propertiesInternal.isModelProject();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public boolean isProductDefinitionProject() {
        return propertiesInternal.isProductDefinitionProject();
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public boolean isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled() {
        return propertiesInternal.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled();
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setBuilderSetConfig(IIpsArtefactBuilderSetConfigModel config) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setBuilderSetId(String id) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setChangesOverTimeNamingConventionIdForGeneratedCode(String changesInTimeConventionIdForGeneratedCode) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * {@inheritDoc}
     */
    public void addDefinedDatatype(Datatype datatype) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * {@inheritDoc}
     */
    public void setDefinedDatatypes(Datatype[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setDerivedUnionIsImplementedRuleEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setIpsObjectPath(IIpsObjectPath path) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setJavaProjectContainsClassesForDynamicDatatypes(boolean newValue) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setJavaSrcLanguage(Locale javaSrcLanguage) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setLoggingFrameworkConnectorId(String loggingFrameworkConnectorId) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setMinRequiredVersionNumber(String featureId, String version) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setModelProject(boolean modelProject) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setPredefinedDatatypesUsed(String[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setProductDefinitionProject(boolean productDefinitionProject) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setRulesWithoutReferencesAllowedEnabled(boolean enabled) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setRuntimeIdPrefix(String runtimeIdPrefix) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        return propertiesInternal.validate(ipsProject);
    }

    /**
     * Returns the value of the underlying IIpsProjectProperties instance.
     */
    public Long getLastPersistentModificationTimestamp() {
        return propertiesInternal.getLastPersistentModificationTimestamp();
    }

    /**
     * Throws a runtime exceptions since manipulation of this object is disallowed.
     */
    public void setLastPersistentModificationTimestamp(Long timestamp) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * {@inheritDoc}
     */
    public EnumType getQuestionAssignedUserGroup() {
        return propertiesInternal.getQuestionAssignedUserGroup();
    }

    /**
     * {@inheritDoc}
     */
    public EnumType getQuestionStatus() {
        return propertiesInternal.getQuestionStatus();
    }

    /**
     * {@inheritDoc}
     */
    public IPersistenceOptions getPersistenceOptions() {
        return propertiesInternal.getPersistenceOptions();
    }

    /**
     * {@inheritDoc}
     */
    public ITableColumnNamingStrategy getTableColumnNamingStrategy() {
        return propertiesInternal.getTableColumnNamingStrategy();
    }

    /**
     * {@inheritDoc}
     */
    public ITableNamingStrategy getTableNamingStrategy() {
        return propertiesInternal.getTableNamingStrategy();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPersistenceSupportEnabled() {
        return propertiesInternal.isPersistenceSupportEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public void setPersistenceSupport(boolean persistentProject) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * {@inheritDoc}
     */
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

    /**
     * {@inheritDoc}
     */
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy) {
        throw new RuntimeException("This is a read only object and can therefor not be manipulated.");
    }

}
