/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Stores and provides inserted properties required for creating {@link IIpsProject IIpsProjects}.
 * 
 * @author Florian Orendi
 */
public class IpsProjectCreationProperties {

    private String runtimeIdPrefix;
    private String sourceFolderName;
    private String basePackageName;
    private String persistenceSupport;

    private boolean isModelProject;
    private boolean isProductDefinitionProject;
    private boolean isPersistentProject;
    private boolean isGroovySupport;

    private List<Locale> locales;

    /**
     * Default constructor.
     * <p>
     * Initializes the default: a Model-Project with Groovy support enabled, if available.
     */
    public IpsProjectCreationProperties() {
        initializeDefaults();
    }

    private void initializeDefaults() {
        runtimeIdPrefix = Messages.IpsProjectCreation_defaultRuntimeIdPrefix;
        sourceFolderName = Messages.IpsProjectCreation_defaultSourceFolderName;
        basePackageName = Messages.IpsProjectCreation_defaultBasePackageName;
        persistenceSupport = PersistenceSupportNames.ID_GENERIC_JPA_2;
        isModelProject = true;
        isProductDefinitionProject = false;
        isPersistentProject = false;
        isGroovySupport = IpsClasspathContainerInitializer.isGroovySupportAvailable();
        locales = new ArrayList<>();
    }

    public String getRuntimeIdPrefix() {
        return runtimeIdPrefix;
    }

    public void setRuntimeIdPrefix(String runtimeIdPrefix) {
        this.runtimeIdPrefix = runtimeIdPrefix;
    }

    public String getSourceFolderName() {
        return sourceFolderName;
    }

    public void setSourceFolderName(String sourceFolderName) {
        this.sourceFolderName = sourceFolderName;
    }

    public String getBasePackageName() {
        return basePackageName;
    }

    public void setBasePackageName(String basePackageName) {
        this.basePackageName = basePackageName;
    }

    public String getPersistenceSupport() {
        return persistenceSupport;
    }

    public void setPersistenceSupport(String persistenceSupport) {
        this.persistenceSupport = persistenceSupport;
    }

    public boolean isModelProject() {
        return isModelProject;
    }

    public void setModelProject(boolean isModelProject) {
        this.isModelProject = isModelProject;
    }

    public boolean isProductDefinitionProject() {
        return isProductDefinitionProject;
    }

    public void setProductDefinitionProject(boolean isProductDefinitionProject) {
        this.isProductDefinitionProject = isProductDefinitionProject;
    }

    public boolean isPersistentProject() {
        return isPersistentProject;
    }

    public void setPersistentProject(boolean isPersistentProject) {
        this.isPersistentProject = isPersistentProject;
    }

    public boolean isGroovySupport() {
        return isGroovySupport;
    }

    public void setGroovySupport(boolean isGroovySupport) {
        this.isGroovySupport = isGroovySupport;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    /**
     * Checks whether all required properties for creating a Faktor-IPS project are set.
     * 
     * @implNote Does not check whether the properties have a valid format.
     * 
     * @return An error message which is empty if there are no errors.
     */
    public String checkForRequiredProperties() {
        boolean existingRuntimeIDPrefix = getRuntimeIdPrefix() != null
                && IpsStringUtils.isNotEmpty(getRuntimeIdPrefix());
        boolean existingSourceFolderName = sourceFolderName != null && IpsStringUtils.isNotEmpty(sourceFolderName);
        boolean existingBasePackageName = basePackageName != null && IpsStringUtils.isNotEmpty(basePackageName);
        boolean existingLocales = locales != null && !locales.isEmpty();

        if (existingRuntimeIDPrefix && existingSourceFolderName
                && existingBasePackageName && existingLocales) {
            return IpsStringUtils.EMPTY;
        }

        StringBuilder errorMessage = new StringBuilder(
                "There are missing properties for creating an IPS project:\n"); //$NON-NLS-1$

        if (!existingRuntimeIDPrefix) {
            errorMessage.append("runtime-ID prefix;\n"); //$NON-NLS-1$
        }
        if (!existingSourceFolderName) {
            errorMessage.append("source folder name;\n"); //$NON-NLS-1$
        }
        if (!existingBasePackageName) {
            errorMessage.append("base package name;\n"); //$NON-NLS-1$
        }
        if (!existingLocales) {
            errorMessage.append("locales;\n"); //$NON-NLS-1$
        }

        if (isPersistentProject) {
            boolean existingPersistenceSupport = persistenceSupport != null
                    && IpsStringUtils.isNotEmpty(persistenceSupport);
            if (!existingPersistenceSupport) {
                errorMessage.append("persistenceSupport;\n"); //$NON-NLS-1$
            }
        }

        if (isGroovySupport && !IpsClasspathContainerInitializer.isGroovySupportAvailable()) {
            errorMessage.append("\nGroovy support is activeted but not installed.\n"); //$NON-NLS-1$
        }

        return errorMessage.toString();
    }
}
