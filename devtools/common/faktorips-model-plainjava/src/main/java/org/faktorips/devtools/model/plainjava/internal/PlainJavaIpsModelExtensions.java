/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsObjectPathContainer;
import org.faktorips.devtools.model.internal.preferences.DefaultIpsModelPreferences;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.DummyIpsWorkspaceInteractions;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;
import org.faktorips.devtools.model.plugin.IpsModelExtensionsViaExtensionPoints;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.runtime.MessageList;

public class PlainJavaIpsModelExtensions extends IpsModelExtensionsViaExtensionPoints {

    private static /* final */ PlainJavaIpsModelExtensions instance = new PlainJavaIpsModelExtensions();
    private Function<IIpsProject, List<IIpsObjectPathEntry>> projectDependenciesProvider;

    private Map<String, IVersionProviderFactory> versionProviderFactoryOverrides = new LinkedHashMap<>();

    protected PlainJavaIpsModelExtensions() {
        super(new PlainJavaRegistryProvider().getRegistry());
    }

    public static PlainJavaIpsModelExtensions get() {
        return instance;
    }

    /**
     * <em><strong>For testing with a custom {@link IExtensionRegistry} only.</strong></em>
     * 
     * @param testInstance an PlainJavaIpsModelExtensions with test data
     */
    protected static void setInstanceForTest(PlainJavaIpsModelExtensions testInstance) {
        instance = testInstance;
    }

    @Deprecated
    @Override
    public org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector[] getIpsLoggingFrameworkConnectors() {
        return new org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector[0];
    }

    @Override
    public IIpsModelPreferences getModelPreferences() {
        return new DefaultIpsModelPreferences();
    }

    @Override
    public IIpsWorkspaceInteractions getWorkspaceInteractions() {
        return new DummyIpsWorkspaceInteractions();
    }

    @Override
    public IClassLoaderProviderFactory getClassLoaderProviderFactory() {
        return new UrlClassLoaderProviderFactory();
    }

    @Override
    public IDependencyGraphPersistenceManager getDependencyGraphPersistenceManager() {
        // TODO FIPS-8693 do we need persistence to support incremental builds?
        return $ -> null;
    }

    public void setVersionProviderFactory(String id, IVersionProviderFactory factory) {
        versionProviderFactoryOverrides.put(id, factory);
    }

    @Override
    public Map<String, IVersionProviderFactory> getVersionProviderFactories() {
        var versionProviderFactories = new HashMap<>(super.getVersionProviderFactories());
        versionProviderFactories.putAll(versionProviderFactoryOverrides);
        return versionProviderFactories;
    }

    @Override
    public List<IIpsObjectPathContainerType> getIpsObjectPathContainerTypes() {
        return List.of(new PlainJavaJDTClasspathContainer());
    }

    /**
     * Returns a function that compiles a list of {@link IIpsObjectPathEntry IIpsObjectPathEntries}
     * for a {@link IIpsProject}.
     */
    public Function<IIpsProject, List<IIpsObjectPathEntry>> getProjectDependenciesProvider() {
        return projectDependenciesProvider;
    }

    /**
     * Sets a function that compiles a list of {@link IIpsObjectPathEntry IIpsObjectPathEntries} for
     * a {@link IIpsProject}.
     */
    public void setProjectDependenciesProvider(
            Function<IIpsProject, List<IIpsObjectPathEntry>> projectDependenciesProvider) {
        this.projectDependenciesProvider = projectDependenciesProvider;
    }

    private static final class PlainJavaJDTClasspathContainer implements IIpsObjectPathContainerType {

        @Override
        public String getId() {
            return "JDTClasspathContainer";
        }

        @Override
        public IIpsObjectPathContainer newContainer(IIpsProject ipsProject, String optionalPath) {
            return new PlainJavaIpsObjectPathContainer("JDTClasspathContainer", optionalPath, ipsProject);
        }

        private static final class PlainJavaIpsObjectPathContainer extends AbstractIpsObjectPathContainer {
            private volatile List<IIpsObjectPathEntry> entries;

            private PlainJavaIpsObjectPathContainer(String containerId, String optionalPath, IIpsProject ipsProject) {
                super(containerId, optionalPath, ipsProject);
            }

            @Override
            public List<IIpsObjectPathEntry> resolveEntries() {
                List<IIpsObjectPathEntry> result = entries;
                if (result == null) {
                    synchronized (this) {
                        if (entries == null) {
                            // CSOFF: InnerAssignment
                            // Efficient lazy initialization pattern from "Effective Java", 3rd
                            // edition, p.334
                            entries = result = PlainJavaIpsModelExtensions.get()
                                    .getProjectDependenciesProvider()
                                    .apply(getIpsProject());
                            // CSON: InnerAssignment
                        }
                    }
                }
                return result;
            }

            @Override
            public MessageList validate() {
                return MessageList.of();
            }
        }
    }

    private static final class UrlClassLoaderProviderFactory implements IClassLoaderProviderFactory {
        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent) {
            return new UrlClassLoaderProvider(ipsProject, parent);
        }

        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
            return new UrlClassLoaderProvider(ipsProject);
        }
    }

    private static final class UrlClassLoaderProvider implements IClassLoaderProvider {
        private final IIpsProject ipsProject;
        private final ClassLoader parent;

        private UrlClassLoaderProvider(IIpsProject ipsProject) {
            this(ipsProject, null);
        }

        public UrlClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent) {
            this.ipsProject = ipsProject;
            this.parent = parent;
        }

        @Override
        public ClassLoader getClassLoader() {
            try {
                URL url = ipsProject.getJavaProject().getOutputLocation().toUri().toURL();
                return parent == null ? new URLClassLoader(new URL[] { url })
                        : new URLClassLoader(new URL[] { url }, parent);
            } catch (MalformedURLException e) {
                throw new IpsException(e.getMessage(), e);
            }
        }

        @Override
        public void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // ignore
        }

        @Override
        public void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // ignore
        }
    }

}
