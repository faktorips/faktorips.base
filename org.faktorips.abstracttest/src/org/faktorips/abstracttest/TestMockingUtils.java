/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
