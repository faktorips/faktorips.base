/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.core.model.versionmanager.IExtendableVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;

public class ExtensionFactory {

    public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    public static final String ATTRIBUTE_FEATURE_ID = "featureId"; //$NON-NLS-1$

    public static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

    public static final String ATTRIBUTE_REQUIRED_FOR_ALL_PROJECTS = "requiredForAllProjects"; //$NON-NLS-1$

    public static final String ATTRIBUTE_BASED_ON_FEATURE_MANAGER = "basedOnFeatureManager"; //$NON-NLS-1$

    public static final String FEATURE_VERSION_MANAGER = "org.faktorips.devtools.core.faktorIpsFeatureVersionManager"; //$NON-NLS-1$

    private final IExtensionRegistry extensionRegistry;

    public ExtensionFactory(IExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    public IIpsFeatureVersionManager[] createIpsFeatureVersionManagers() {
        IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor(FEATURE_VERSION_MANAGER);
        List<IIpsFeatureVersionManager> result = new ArrayList<IIpsFeatureVersionManager>();
        for (IConfigurationElement element : elements) {
            try {
                IIpsFeatureVersionManager manager = (IIpsFeatureVersionManager)element
                        .createExecutableExtension(ATTRIBUTE_CLASS);
                if (manager instanceof IExtendableVersionManager) {
                    IExtendableVersionManager extendableVersionManager = (IExtendableVersionManager)manager;
                    extendableVersionManager.setContributorName(element.getContributor().getName());
                }
                manager.setFeatureId(element.getAttribute(ATTRIBUTE_FEATURE_ID));
                manager.setId(element.getAttribute(ATTRIBUTE_ID));
                manager.setPredecessorId(element.getAttribute(ATTRIBUTE_BASED_ON_FEATURE_MANAGER));
                manager.setRequiredForAllProjects(Boolean.parseBoolean(element
                        .getAttribute(ATTRIBUTE_REQUIRED_FOR_ALL_PROJECTS)));
                result.add(manager);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        if (result.isEmpty()) {
            result.add(EmptyIpsFeatureVersionManager.INSTANCE);
        }
        return result.toArray(new IIpsFeatureVersionManager[result.size()]);
    }

}
