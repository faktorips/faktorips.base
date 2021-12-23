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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.builder.EmptyBuilderSet;
import org.faktorips.devtools.model.internal.ipsproject.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * A class that hold information about IIpsArtefactBuilderSets that are registered with the
 * corresponding extension point.
 * 
 * @see IpsModel#getIpsArtefactBuilderSetInfos()
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetInfo implements IIpsArtefactBuilderSetInfo {

    private static final String BUILDER_SET_PROPERTY_DEF = "builderSetPropertyDef"; //$NON-NLS-1$
    private Class<?> builderSetClass;
    private String builderSetId;
    private String builderSetLabel;
    private String namespace;
    private String builderSetClassName;
    private Map<String, IIpsBuilderSetPropertyDef> propertyDefinitions;

    private IpsArtefactBuilderSetInfo(String namespace, String builderSetClassName, String builderSetId,
            String builderSetLabel, Map<String, IIpsBuilderSetPropertyDef> propertyDefinitions) {

        super();
        this.namespace = namespace;
        this.builderSetClassName = builderSetClassName;
        this.builderSetId = builderSetId;
        this.builderSetLabel = builderSetLabel;
        this.propertyDefinitions = propertyDefinitions;
    }

    /**
     * Creates an IIpsArtefactBuilderSet if one has been registered with the provided
     * <code>builderSetId</code> at the artefact builder set extension point. Otherwise an
     * <code>EmptyBuilderSet</code> will be returned.
     */
    @Override
    public IIpsArtefactBuilderSet create(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        try {
            IIpsArtefactBuilderSet builderSet = (IIpsArtefactBuilderSet)getBuilderSetClass().getConstructor()
                    .newInstance();
            builderSet.setId(getBuilderSetId());
            builderSet.setLabel(getBuilderSetLabel());
            builderSet.setIpsProject(ipsProject);
            return builderSet;
        } catch (ClassCastException e) {
            IpsLog.log(new IpsStatus("The registered builder set " + getBuilderSetClass() + //$NON-NLS-1$
                    " doesn't implement the " + IIpsArtefactBuilderSet.class + " interface.", e)); //$NON-NLS-1$ //$NON-NLS-2$
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            IpsLog.log(new IpsStatus("Unable to instantiate the builder set " + getBuilderSetClass(), e)); //$NON-NLS-1$
        }
        return new EmptyBuilderSet();
    }

    @Override
    public IIpsArtefactBuilderSetConfigModel createDefaultConfiguration(IIpsProject ipsProject) {
        IpsArtefactBuilderSetConfigModel configModel = new IpsArtefactBuilderSetConfigModel();
        for (IIpsBuilderSetPropertyDef propertyDef : propertyDefinitions.values()) {
            if (propertyDef.isAvailable(ipsProject)) {
                configModel.setPropertyValue(propertyDef.getName(), propertyDef.getDefaultValue(ipsProject),
                        propertyDef.getDescription());
            }
        }
        return configModel;
    }

    /**
     * Returns the class of the IIpsArtefactBuilderSet implementation class.
     */
    private Class<?> getBuilderSetClass() {
        if (builderSetClass == null) {
            try {
                builderSetClass = Platform.getBundle(namespace).loadClass(builderSetClassName);
            } catch (ClassNotFoundException e) {
                IpsLog.log(new IpsStatus("Unable to load the IpsArtefactBuilderSet class " + builderSetClassName + //$NON-NLS-1$
                        " with the id " + builderSetId)); //$NON-NLS-1$
            }
        }
        return builderSetClass;
    }

    /**
     * Returns the id by which the <code>IIpsArtefactBuilderSet</code> is registered with the
     * system.
     */
    @Override
    public String getBuilderSetId() {
        return builderSetId;
    }

    /**
     * Returns the label for the corresponding <code>IIpsArtefactBuilderSet</code>.
     */
    @Override
    public String getBuilderSetLabel() {
        return builderSetLabel;
    }

    /**
     * Returns the property definition object for the specified name or <code>null</code> if it
     * doesn't exist.
     */
    @Override
    public IIpsBuilderSetPropertyDef getPropertyDefinition(String name) {
        return propertyDefinitions.get(name);
    }

    /**
     * Returns the properties defined for this IpsArtefactBuilderSet.
     */
    @Override
    public IIpsBuilderSetPropertyDef[] getPropertyDefinitions() {
        return propertyDefinitions.values().toArray(new IIpsBuilderSetPropertyDef[propertyDefinitions.size()]);
    }

    /**
     * Validates the provided IIpsArtefactBuilderSetConfig against this definition. Especially the
     * properties of the configuration are checked for their existence and the correct value.
     */
    @Override
    public MessageList validateIpsArtefactBuilderSetConfig(IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        MessageList msgList = new MessageList();

        String[] names = builderSetConfig.getPropertyNames();
        for (String name : names) {
            Message msg = validateIpsBuilderSetPropertyValue(ipsProject, name, builderSetConfig.getPropertyValue(name));
            if (msg != null) {
                msgList.add(msg);
            }
        }
        return msgList;
    }

    /**
     * Validates the property value of the property of an IpsArtefactBuilderSetConfig specified by
     * the propertyName. It returns <code>null</code> if validation is correct otherwise a
     * {@link Message} object is returned.
     */
    @Override
    public Message validateIpsBuilderSetPropertyValue(IIpsProject ipsProject,
            String propertyName,
            String propertyValue) {
        IIpsBuilderSetPropertyDef propertyDef = propertyDefinitions.get(propertyName);
        if (propertyDef == null) {
            String text = MessageFormat.format(Messages.IpsArtefactBuilderSetInfo_propertyNotSupported, builderSetId,
                    propertyName);
            return new Message(MSG_CODE_PROPERTY_NOT_SUPPORTED, text, Message.ERROR);
        }
        Message msg = propertyDef.validateValue(ipsProject, propertyValue);
        if (msg != null) {
            return msg;
        }
        return null;
    }

    private static final Map<String, IIpsBuilderSetPropertyDef> retrieveBuilderSetProperties(
            IExtensionRegistry registry,
            String builderSetId,
            IIpsModel ipsModel,
            IConfigurationElement element,
            ALog logger) {

        IConfigurationElement[] builderSetPropertyDefElements = element.getChildren(BUILDER_SET_PROPERTY_DEF);
        Map<String, IIpsBuilderSetPropertyDef> builderSetPropertyDefs = new HashMap<>();
        for (IConfigurationElement builderSetPropertyDefElement : builderSetPropertyDefElements) {
            IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef
                    .loadExtensions(builderSetPropertyDefElement, registry, builderSetId, logger, ipsModel);
            if (propertyDef != null) {
                builderSetPropertyDefs.put(propertyDef.getName(), propertyDef);
            }
        }
        return builderSetPropertyDefs;
    }

    /**
     * Loads IpsArtefactBuilderSetInfos from the extension registry.
     */
    public static final void loadExtensions(IExtensionRegistry registry,
            ALog logger,
            List<IIpsArtefactBuilderSetInfo> builderSetInfoList,
            IIpsModel ipsModel) {
        if (Abstractions.isEclipseRunning()) {
            IExtensionPoint point = registry.getExtensionPoint(IpsModelActivator.PLUGIN_ID, "artefactbuilderset"); //$NON-NLS-1$
            IExtension[] extensions = point.getExtensions();

            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                if (configElements.length > 0) {
                    IConfigurationElement element = configElements[0];
                    if (element.getName().equals("builderSet")) { //$NON-NLS-1$
                        if (StringUtils.isEmpty(extension.getUniqueIdentifier())) {
                            logger.log(new IpsStatus("The identifier of the IpsArtefactBuilderSet extension is empty")); //$NON-NLS-1$
                            continue;
                        }
                        String builderSetClassName = element.getAttribute("class"); //$NON-NLS-1$
                        if (StringUtils.isEmpty(builderSetClassName)) {
                            logger.log(new IpsStatus(
                                    "The class attribute of the IpsArtefactBuilderSet extension with the extension id " //$NON-NLS-1$
                                            +
                                            extension.getUniqueIdentifier() + " is not specified.")); //$NON-NLS-1$
                            continue;
                        }

                        Map<String, IIpsBuilderSetPropertyDef> builderSetPropertyDefs = retrieveBuilderSetProperties(
                                registry, extension.getUniqueIdentifier(), ipsModel, element, logger);
                        builderSetInfoList
                                .add(new IpsArtefactBuilderSetInfo(extension.getNamespaceIdentifier(),
                                        builderSetClassName,
                                        extension.getUniqueIdentifier(), extension.getLabel(), builderSetPropertyDefs));
                    }
                }
            }
        }
    }
}
