/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.IVersionProviderFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VersionProviderExtensionPointTest {

    private static final String MY_VERSION_PROVIDER_ID = "myVersionProviderId";

    @Mock
    private IExtensionRegistry extensionRegistry;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProjectProperties properties;

    @Mock
    private IConfigurationElement configElementDummy;

    @Mock
    private IConfigurationElement configElementVersionProvider;

    @Mock
    private IVersionProviderFactory versionProviderFactory;

    @InjectMocks
    private VersionProviderExtensionPoint versionProviderExtensionPoint;

    @Before
    public void setUpProject() {
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        when(properties.getVersionProviderId()).thenReturn(MY_VERSION_PROVIDER_ID);
    }

    @Before
    public void setUpExtensionRegistry() throws Exception {
        when(
                extensionRegistry.getConfigurationElementsFor(IpsPlugin.PLUGIN_ID,
                        VersionProviderExtensionPoint.VERSION_PROVIDER_EXTENSION)).thenReturn(
                new IConfigurationElement[] { configElementDummy, configElementVersionProvider });
        when(configElementDummy.getAttribute(VersionProviderExtensionPoint.EXTENSION_ATTRIBUTE_ID)).thenReturn("");
        when(configElementVersionProvider.getAttribute(VersionProviderExtensionPoint.EXTENSION_ATTRIBUTE_ID))
                .thenReturn(MY_VERSION_PROVIDER_ID);
        when(configElementVersionProvider.createExecutableExtension(ExtensionPoints.ATTRIBUTE_CLASS)).thenReturn(
                versionProviderFactory);
    }

    @Test
    public void testGetExtendedVersionProvider() throws Exception {
        IVersionProvider<?> versionProvider = mock(IVersionProvider.class);
        doReturn(versionProvider).when(versionProviderFactory).createVersionProvider(ipsProject);

        IVersionProvider<?> extendedVersionProvider = versionProviderExtensionPoint.getExtendedVersionProvider();

        assertSame(versionProvider, extendedVersionProvider);
    }

}
