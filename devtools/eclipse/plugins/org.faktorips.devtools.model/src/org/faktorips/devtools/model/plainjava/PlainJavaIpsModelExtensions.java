/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.CurrencyDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.IPreSaveProcessor;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.internal.preferences.DefaultIpsModelPreferences;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.DummyDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.plugin.DummyIpsWorkspaceInteractions;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.util.SortorderSet;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

public class PlainJavaIpsModelExtensions implements IIpsModelExtensions {

    private static /* final */ PlainJavaIpsModelExtensions instance = new PlainJavaIpsModelExtensions();

    private volatile Map<String, Datatype> datatypes = new LinkedHashMap<>();

    protected PlainJavaIpsModelExtensions() {
        // singleton
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

    @Override
    public SortorderSet<IFunctionResolverFactory<JavaCodeFragment>> getFlFunctionResolverFactories() {
        // TODO Auto-generated method stub
        return new SortorderSet<>();
    }

    @Override
    public IIpsFeatureVersionManager[] getIpsFeatureVersionManagers() {
        // TODO Auto-generated method stub
        return new IIpsFeatureVersionManager[0];
    }

    @Deprecated
    @Override
    public org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector[] getIpsLoggingFrameworkConnectors() {
        return new org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector[0];
    }

    @Override
    public Map<AVersion, IIpsProjectMigrationOperationFactory> getRegisteredMigrationOperations(
            String contributorName) {
        // TODO Auto-generated method stub
        return Collections.emptyMap();
    }

    @Override
    public IIpsModelPreferences getModelPreferences() {
        // TODO Auto-generated method stub
        return new DefaultIpsModelPreferences();
    }

    @Override
    public IIpsWorkspaceInteractions getWorkspaceInteractions() {
        // TODO Auto-generated method stub
        return new DummyIpsWorkspaceInteractions();
    }

    @Override
    public List<IIpsProjectConfigurator> getIpsProjectConfigurators() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public IdentifierFilter getIdentifierFilter() {
        // TODO Auto-generated method stub
        return new IdentifierFilter(Collections.emptyList());
    }

    @Override
    public IDependencyGraphPersistenceManager getDependencyGraphPersistenceManager() {
        return new DummyDependencyGraphPersistenceManager();
    }

    @Override
    public IClassLoaderProviderFactory getClassLoaderProviderFactory() {
        return new UrlClassLoaderProviderFactory();
    }

    @Override
    public List<IDeepCopyOperationFixup> getDeepCopyOperationFixups() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Map<Class<?>, List<IExtensionPropertyDefinition>> getExtensionPropertyDefinitions() {
        // TODO Auto-generated method stub
        return Collections.emptyMap();
    }

    @Override
    public Map<String, IVersionProviderFactory> getVersionProviderFactories() {
        // TODO Auto-generated method stub
        return Collections.emptyMap();
    }

    @Override
    public List<IPreSaveProcessor> getPreSaveProcessors(IpsObjectType ipsObjectType) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<IIpsObjectPathContainerType> getIpsObjectPathContainerTypes() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Map<String, Datatype> getPredefinedDatatypes() {
        if (datatypes.isEmpty()) {
            synchronized (datatypes) {
                if (datatypes.isEmpty()) {
                    Arrays.asList(
                            Datatype.STRING,
                            Datatype.INTEGER,
                            Datatype.LONG,
                            Datatype.BOOLEAN,
                            new DateDatatype(),
                            Datatype.GREGORIAN_CALENDAR,
                            Datatype.DECIMAL,
                            Datatype.MONEY,
                            Datatype.DOUBLE,
                            Datatype.PRIMITIVE_BOOLEAN,
                            Datatype.PRIMITIVE_INT,
                            Datatype.PRIMITIVE_LONG,
                            Datatype.BIG_DECIMAL,
                            new LocalDateDatatype(),
                            new LocalTimeDatatype(),
                            new LocalDateTimeDatatype(),
                            new MonthDayDatatype(),
                            new CurrencyDatatype())
                            .forEach(d -> datatypes.put(d.getName(), d));
                }
            }
        }
        return Collections.unmodifiableMap(datatypes);
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
                return parent == null
                        ? new URLClassLoader(new URL[] { url })
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
