/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.internal.toc.ITocEntryFactory;
import org.osgi.framework.BundleContext;

/**
 * The plugin class for this Faktor-IPS standard builder plugin.
 * 
 * @author Peter Erzberger
 */
public class StdBuilderPlugin extends Plugin {

    /**
     * The plugin id like it is defined in the plugin.xml file
     */
    public static final String PLUGIN_ID = "org.faktorips.devtools.stdbuilder"; //$NON-NLS-1$

    /**
     * The id of the standard builder set extension like it is defined in the plugin.xml file
     */
    public static final String STANDARD_BUILDER_EXTENSION_ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset"; //$NON-NLS-1$

    // The shared instance.
    private static StdBuilderPlugin plugin;

    private List<ITocEntryBuilderFactory> tocEntryBuilderFactories;

    private List<ITocEntryFactory<?>> tocEntryFactories;

    private ClassLoader contextFinder;

    /**
     * The constructor.
     */
    public StdBuilderPlugin() {
        super();
        plugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static StdBuilderPlugin getDefault() {
        return plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        loadTocEntryBuilders();
        loadTocEntryFactories();
        initContextFinder();
    }

    private void loadTocEntryBuilders() {
        tocEntryBuilderFactories = new ArrayList<>();
        ExtensionPoints extensionPoints = new ExtensionPoints(PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension("tocEntryBuilderFactory");
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configurationElements) {
                if ("tocEntryBuilderFactory".equals(configElement.getName())) { //$NON-NLS-1$
                    ITocEntryBuilderFactory builderFactory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, "class", ITocEntryBuilderFactory.class); //$NON-NLS-1$
                    if (builderFactory != null) {
                        tocEntryBuilderFactories.add(builderFactory);
                    }
                }
            }
        }
    }

    private void loadTocEntryFactories() {
        tocEntryFactories = new ArrayList<>();
        ExtensionPoints extensionPoints = new ExtensionPoints(PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension("tocEntryFactory");
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configurationElements) {
                if ("tocEntryFactory".equals(configElement.getName())) { //$NON-NLS-1$
                    ITocEntryFactory<?> tocEntryFactory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, "class", ITocEntryFactory.class); //$NON-NLS-1$
                    tocEntryFactories.add(tocEntryFactory);
                }
            }
        }
    }

    /**
     * When starting the plug-In, the current context classloader should be the eclipse context
     * finder which is able to find all needed resources independent of the current bundle.
     */
    private void initContextFinder() {
        contextFinder = Thread.currentThread().getContextClassLoader();
    }

    public ClassLoader getContextFinder() {
        return contextFinder;
    }

    /**
     * Logs the core exception
     */
    public static final void log(CoreException e) {
        log(e.getStatus());
    }

    /**
     * Logs the core exception
     */
    public static final void log(CoreRuntimeException e) {
        Throwable cause = e.getCause();
        if (cause instanceof CoreException) {
            log(((CoreException)cause).getStatus());
        } else {
            log(new IpsStatus(e));
        }
    }

    /**
     * Logs the status.
     */
    public static final void log(IStatus status) {
        plugin.getLog().log(status);
    }

    /**
     * @return a defensive copy of the {@link ITocEntryBuilderFactory}s currently registered with
     *         this plugin.
     */
    public List<ITocEntryBuilderFactory> getTocEntryBuilderFactories() {
        return new ArrayList<>(tocEntryBuilderFactories);
    }

    /**
     * @return a defensive copy of the {@link ITocEntryFactory}s currently registered with this
     *         plugin.
     */
    public List<ITocEntryFactory<?>> getTocEntryFactories() {
        return new ArrayList<>(tocEntryFactories);
    }
}
