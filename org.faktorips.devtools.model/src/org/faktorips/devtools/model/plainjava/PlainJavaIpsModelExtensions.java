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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.faktorips.codegen.JavaCodeFragment;
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
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.plugin.DummyIpsWorkspaceInteractions;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.util.SortorderSet;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.osgi.framework.Version;

public class PlainJavaIpsModelExtensions implements IIpsModelExtensions {

    private static /* final */ PlainJavaIpsModelExtensions instance = new PlainJavaIpsModelExtensions();

    private PlainJavaIpsModelExtensions() {
        // singleton
    }

    public static PlainJavaIpsModelExtensions get() {
        return instance;
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
    public Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredMigrationOperations(String contributorName) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IClassLoaderProviderFactory getClassLoaderProviderFactory() {
        // TODO Auto-generated method stub
        return null;
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

}
