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

package org.faktorips.devtools.stdbuilder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.osgi.framework.BundleContext;

/**
 * The plugin class for this faktor ips standard builder plugin.
 * 
 * @author Peter Erzberger
 */
public class StdBuilderPlugin extends Plugin {

    /**
     * The plugin id like it is defined in the plugin.xml file
     */
    public final static String PLUGIN_ID = "org.faktorips.devtools.stdbuilder"; //$NON-NLS-1$

    /**
     * The id of the standard builder set extension like it is defined in the plugin.xml file
     */
    public final static String STANDARD_BUILDER_EXTENSION_ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset"; //$NON-NLS-1$

    // The shared instance.
    private static StdBuilderPlugin plugin;

    private Map<IpsObjectType, ITocEntryBuilder> ipsObjectTypeToTocEntryBuilderMap;

    /**
     * Returns the shared instance.
     */
    public static StdBuilderPlugin getDefault() {
        return plugin;
    }

    /**
     * The constructor.
     */
    public StdBuilderPlugin() {
        super();
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        loadTocEntryBuilders();
    }

    /**
     * Logs the core exception
     */
    public final static void log(CoreException e) {
        log(e.getStatus());
    }

    /**
     * Logs the status.
     */
    public final static void log(IStatus status) {
        plugin.getLog().log(status);
    }

    private void loadTocEntryBuilders() {
        ipsObjectTypeToTocEntryBuilderMap = new HashMap<IpsObjectType, ITocEntryBuilder>();
        ExtensionPoints extensionPoints = new ExtensionPoints(PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension("tocEntryBuilder");
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configurationElements) {
                if ("tocEntryBuilder".equals(configElement.getName())) { //$NON-NLS-1$
                    ITocEntryBuilder builder = ExtensionPoints.createExecutableExtension(extension, configElement,
                            "class", ITocEntryBuilder.class); //$NON-NLS-1$
                    String ipsObjectName = ExtensionPoints.createExecutableExtension(extension, configElement,
                            "ipsObjectType", IpsObjectType.class).getId(); //$NON-NLS-1$
                    IpsObjectType objectType = IpsObjectType.getTypeForName(ipsObjectName);
                    ipsObjectTypeToTocEntryBuilderMap.put(objectType, builder);
                }
            }
        }
    }

    public Map<IpsObjectType, ITocEntryBuilder> getTocEntryBuilderMap() {
        return ipsObjectTypeToTocEntryBuilderMap;
    }

}
