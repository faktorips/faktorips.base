/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class VersionProviderFactoryExtensionsTest {

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

    @Mock
    private IVersionProvider<?> versionProvider;

    @Before
    public void setUpExtensionRegistry() throws Exception {
        when(extensionRegistry.getConfigurationElementsFor(IpsModelActivator.PLUGIN_ID,
                VersionProviderFactoryExtensions.EXTENSION_POINT_ID_VERSION_PROVIDER))
                        .thenReturn(new IConfigurationElement[] { configElementDummy, configElementVersionProvider });
        when(configElementVersionProvider.getAttribute(VersionProviderFactoryExtensions.EXTENSION_ATTRIBUTE_ID))
                .thenReturn(MY_VERSION_PROVIDER_ID);
        when(configElementVersionProvider.createExecutableExtension(ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS))
                .thenReturn(versionProviderFactory);
    }

    @Test
    public void testGet() {
        VersionProviderFactoryExtensions versionProviderFactoryExtensions = new VersionProviderFactoryExtensions(
                new ExtensionPoints(extensionRegistry, IpsModelActivator.PLUGIN_ID));

        Map<String, IVersionProviderFactory> versionProviderFactoriesById = versionProviderFactoryExtensions.get();

        assertThat(versionProviderFactoriesById.size(), is(1));
        assertThat(versionProviderFactoriesById.get(MY_VERSION_PROVIDER_ID), is(sameInstance(versionProviderFactory)));
        assertThat(versionProviderFactoriesById.get("foobar"), is(nullValue()));
        assertThat(versionProviderFactoriesById.get(""), is(nullValue()));
    }

}
