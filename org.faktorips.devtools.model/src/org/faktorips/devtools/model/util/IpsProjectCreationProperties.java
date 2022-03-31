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
import java.util.function.Supplier;

import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Stores and provides properties required for creating {@link IIpsProject IIpsProjects}.
 * 
 * @author Florian Orendi
 */
public class IpsProjectCreationProperties {

    public static final String MSG_CODE_MISSING_PROPERTY = "MISSING_PROPERTY"; //$NON-NLS-1$
    public static final String PROPERTY_RUNTIME_ID_PREFIX = "runtimeIdPrefix"; //$NON-NLS-1$
    public static final String PROPERTY_SOURCE_FOLDER_NAME = "sourceFolderName"; //$NON-NLS-1$
    public static final String PROPERTY_BASE_PACKAGE_NAME = "basePackageName"; //$NON-NLS-1$
    public static final String PROPERTY_PERSISTENCE_SUPPORT = "persistenceSupport"; //$NON-NLS-1$
    public static final String PROPERTY_LOCALES = "locales"; //$NON-NLS-1$

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
     * 
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
        isGroovySupport = true;
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

    private String getPropertyName(String property) {
        switch (property) {
            case PROPERTY_RUNTIME_ID_PREFIX:
                return Messages.IpsProjectCreationProperties_runtimeIdPrefix;
            case PROPERTY_SOURCE_FOLDER_NAME:
                return Messages.IpsProjectCreationProperties_sourceFolderName;
            case PROPERTY_BASE_PACKAGE_NAME:
                return Messages.IpsProjectCreationProperties_basePackageName;
            case PROPERTY_PERSISTENCE_SUPPORT:
                return Messages.IpsProjectCreationProperties_persistenceSupport;
            case PROPERTY_LOCALES:
                return Messages.IpsProjectCreationProperties_locales;

            default:
                return property;
        }
    }

    private void validateNonEmpty(MessageList messages, String property, Supplier<String> getter) {
        if (IpsStringUtils.isEmpty(getter.get())) {
            missingProperty(messages, property);
        }
    }

    private void missingProperty(MessageList messages, String property) {
        messages.newError(MSG_CODE_MISSING_PROPERTY,
                Messages.bind(Messages.IpsProjectCreationProperties_MsgText_MissingProperty,
                        getPropertyName(property)),
                this,
                property);
    }

    /**
     * Checks whether all required properties for creating a Faktor-IPS project are set.
     * 
     * @implNote This method does not check whether the properties have a valid format or are
     *           available
     * 
     * @return an empty {@link MessageList} if all required properties are filled, one containing at
     *         least one error message otherwise
     */
    public MessageList validateRequiredProperties() {
        MessageList messages = new MessageList();
        validateNonEmpty(messages, PROPERTY_RUNTIME_ID_PREFIX, this::getRuntimeIdPrefix);
        validateNonEmpty(messages, PROPERTY_SOURCE_FOLDER_NAME, this::getSourceFolderName);
        validateNonEmpty(messages, PROPERTY_BASE_PACKAGE_NAME, this::getBasePackageName);
        if (isPersistentProject) {
            validateNonEmpty(messages, PROPERTY_PERSISTENCE_SUPPORT, this::getPersistenceSupport);
        }
        if (locales == null || locales.isEmpty()) {
            missingProperty(messages, PROPERTY_LOCALES);
        }
        return messages;
    }

    /**
     * Validates whether these {@link IpsProjectCreationProperties} allow the given
     * {@link IJavaProject} to be turned into an {@link IIpsProject}.
     * 
     * @return an empty {@link MessageList} if all validation succeeds, one containing at least one
     *         error message otherwise
     * @see #validateRequiredProperties()
     * @see IIpsProjectConfigurator#validate(AJavaProject, IpsProjectCreationProperties)
     */
    public MessageList validate(AJavaProject javaProject) {
        MessageList errorMessages = validateRequiredProperties();
        IpsProjectConfigurators.applicableTo(javaProject)
                .map(c -> c.validate(javaProject, this))
                .forEach(errorMessages::add);
        return errorMessages;
    }
}
