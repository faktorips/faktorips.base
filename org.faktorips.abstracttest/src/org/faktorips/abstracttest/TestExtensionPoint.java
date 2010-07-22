/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.abstracttest;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IExtensionPoint} interface for testing purposes. Not all of the
 * methods are implemented but can be implemented as needed. Those methods that are not implemented
 * throw a RuntimeException.
 * 
 * @author Peter Erzberger
 */
@SuppressWarnings("deprecation")
public class TestExtensionPoint implements IExtensionPoint {

    private IExtension[] extensions;
    private String nameSpaceIdentifier;
    private String simpleIdentifier;

    /**
     * Create a new TestExtensionPoint with the provide {@link IExtension}s a nameSpaceIdentifier
     * and a simpleIdentifier
     */
    public TestExtensionPoint(IExtension[] extensions, String nameSpaceIdentifier, String simpleIdentifier) {
        ArgumentCheck.notNull(extensions, this);
        ArgumentCheck.notNull(nameSpaceIdentifier, this);
        ArgumentCheck.notNull(simpleIdentifier, this);
        this.extensions = extensions;
        this.nameSpaceIdentifier = nameSpaceIdentifier;
        this.simpleIdentifier = simpleIdentifier;
    }

    @Override
    public IConfigurationElement[] getConfigurationElements() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public IContributor getContributor() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public IPluginDescriptor getDeclaringPluginDescriptor() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public IExtension getExtension(String extensionId) throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public IExtension[] getExtensions() throws InvalidRegistryObjectException {
        return extensions;
    }

    @Override
    public String getLabel() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public String getNamespace() throws InvalidRegistryObjectException {
        return getNamespaceIdentifier();
    }

    @Override
    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
        return nameSpaceIdentifier;
    }

    @Override
    public String getSchemaReference() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public String getSimpleIdentifier() throws InvalidRegistryObjectException {
        return simpleIdentifier;
    }

    @Override
    public String getUniqueIdentifier() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public boolean isValid() {
        throw new RuntimeException("Not implemented yet.");
    }

    // @since Eclipse 3.6 (Helios)
    public String getLabel(String locale) throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }
}
