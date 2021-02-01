/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

public abstract class TestMockingUtils {

    public static IExtensionPoint mockExtensionPoint(String namespace, String simpleId, IExtension... value) {
        IExtensionPoint extensionPoint = mock(IExtensionPoint.class);
        when(extensionPoint.getExtensions()).thenReturn(value);
        when(extensionPoint.getNamespaceIdentifier()).thenReturn(namespace);
        when(extensionPoint.getSimpleIdentifier()).thenReturn(simpleId);
        return extensionPoint;
    }

    public static IExtension mockExtension(String identifier, IConfigurationElement... elements) {
        IExtension extension = mock(IExtension.class);
        when(extension.getConfigurationElements()).thenReturn(elements);
        when(extension.getExtensionPointUniqueIdentifier()).thenReturn(identifier);
        when(extension.getUniqueIdentifier()).thenReturn(identifier);
        return extension;
    }
}
