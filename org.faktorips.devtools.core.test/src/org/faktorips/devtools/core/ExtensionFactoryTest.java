/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.devtools.core.model.versionmanager.IExtendableVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExtensionFactoryTest {

    private static final String MY_BASED_ON_FEATURE_MANAGER = "myBasedOnFeatureManager";

    private static final String MY_ID = "myId";

    private static final String MY_FEATURE_ID = "myFeatureId";

    private static final String MY_CONTRIBUTOR_NAME = "myContributorName";

    @Mock
    private IExtensionRegistry extensionRegistry;

    @Mock
    private IIpsFeatureVersionManager ipsFeatureVersionManager;

    @Mock
    private IExtendableVersionManager extendableVersionManager;

    private ExtensionFactory extensionFactory;

    @Before
    public void createExtensionFactory() throws Exception {
        extensionFactory = new ExtensionFactory(extensionRegistry);
    }

    @Test
    public void testCreateIpsFeatureVersionManagers_empty() throws Exception {
        when(extensionRegistry.getConfigurationElementsFor(ExtensionFactory.FEATURE_VERSION_MANAGER)).thenReturn(
                new IConfigurationElement[0]);

        IIpsFeatureVersionManager[] ipsFeatureVersionManagers = extensionFactory.createIpsFeatureVersionManagers();

        assertEquals(1, ipsFeatureVersionManagers.length);
        assertEquals(EmptyIpsFeatureVersionManager.INSTANCE, ipsFeatureVersionManagers[0]);
    }

    @Test
    public void testCreateIpsFeatureVersionManagers_ipsFeatureVersionManager() throws Exception {
        IConfigurationElement extendableVersionManagerElement = mockVersionManager(ipsFeatureVersionManager);
        when(extensionRegistry.getConfigurationElementsFor(ExtensionFactory.FEATURE_VERSION_MANAGER)).thenReturn(
                new IConfigurationElement[] { extendableVersionManagerElement });

        IIpsFeatureVersionManager[] ipsFeatureVersionManagers = extensionFactory.createIpsFeatureVersionManagers();

        assertEquals(1, ipsFeatureVersionManagers.length);
        assertEquals(ipsFeatureVersionManager, ipsFeatureVersionManagers[0]);
        verify(ipsFeatureVersionManager).setFeatureId(MY_FEATURE_ID);
        verify(ipsFeatureVersionManager).setId(MY_ID);
        verify(ipsFeatureVersionManager).setPredecessorId(MY_BASED_ON_FEATURE_MANAGER);
        verify(ipsFeatureVersionManager).setRequiredForAllProjects(true);
    }

    @Test
    public void testCreateIpsFeatureVersionManagers_extendableVersionManager() throws Exception {
        IConfigurationElement extendableVersionManagerElement = mockVersionManager(extendableVersionManager);
        when(extensionRegistry.getConfigurationElementsFor(ExtensionFactory.FEATURE_VERSION_MANAGER)).thenReturn(
                new IConfigurationElement[] { extendableVersionManagerElement });

        IIpsFeatureVersionManager[] ipsFeatureVersionManagers = extensionFactory.createIpsFeatureVersionManagers();

        assertEquals(1, ipsFeatureVersionManagers.length);
        assertEquals(extendableVersionManager, ipsFeatureVersionManagers[0]);
        verify(extendableVersionManager).setContributorName(MY_CONTRIBUTOR_NAME);
        verify(extendableVersionManager).setFeatureId(MY_FEATURE_ID);
        verify(extendableVersionManager).setId(MY_ID);
        verify(extendableVersionManager).setPredecessorId(MY_BASED_ON_FEATURE_MANAGER);
        verify(extendableVersionManager).setRequiredForAllProjects(true);
    }

    private IConfigurationElement mockVersionManager(IIpsFeatureVersionManager ipsFeatureVersionManager) {
        when(TestConfigurationElement.CONTRIBUTOR.getName()).thenReturn(MY_CONTRIBUTOR_NAME);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(ExtensionFactory.ATTRIBUTE_CLASS, IExtendableVersionManager.class.getName());
        attributes.put(ExtensionFactory.ATTRIBUTE_FEATURE_ID, MY_FEATURE_ID);
        attributes.put(ExtensionFactory.ATTRIBUTE_ID, MY_ID);
        attributes.put(ExtensionFactory.ATTRIBUTE_BASED_ON_FEATURE_MANAGER, MY_BASED_ON_FEATURE_MANAGER);
        attributes.put(ExtensionFactory.ATTRIBUTE_REQUIRED_FOR_ALL_PROJECTS, Boolean.toString(true));
        String value = "";
        IConfigurationElement[] children = new IConfigurationElement[0];
        Map<String, Object> executabled = new HashMap<String, Object>();
        executabled.put("class", ipsFeatureVersionManager);
        TestConfigurationElement testConfigurationElement = new TestConfigurationElement(
                ExtensionFactory.FEATURE_VERSION_MANAGER, attributes, value, children, executabled);
        return testConfigurationElement;
    }

}
