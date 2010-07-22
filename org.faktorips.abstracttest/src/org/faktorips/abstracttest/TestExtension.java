/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IExtension} interface for testing purposes. Not all of the
 * methods are implemented but can be implemented as needed. Those methods that are not implemented
 * throw a RuntimeException.
 * 
 * @author Peter Erzberger
 */
@SuppressWarnings("deprecation")
public class TestExtension implements IExtension {

    private IConfigurationElement[] elements;
    private String simpleIdentifier;
    private String namespaceIdentifier = "";

    /**
     * Creates a TestExtension with the specified IConfigurationElement and simpleIdentifier.
     */
    public TestExtension(IConfigurationElement[] elements, String simpleIdentifier) {
        this(elements, "", simpleIdentifier);
    }

    /**
     * Creates a TestExtension with the specified IConfigurationElement namespaceIdentifier and
     * simpleIdentifier.
     */
    public TestExtension(IConfigurationElement[] elements, String namespaceIdentifier, String simpleIdentifier) {
        ArgumentCheck.notNull(elements, this);
        ArgumentCheck.notNull(simpleIdentifier, this);
        this.elements = elements;
        this.simpleIdentifier = simpleIdentifier;
        this.namespaceIdentifier = namespaceIdentifier != null ? namespaceIdentifier : "";
    }

    @Override
    public IConfigurationElement[] getConfigurationElements() throws InvalidRegistryObjectException {
        return elements;
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
    public String getExtensionPointUniqueIdentifier() throws InvalidRegistryObjectException {
        return namespaceIdentifier + "." + simpleIdentifier;
    }

    /**
     * Returns an empty label.
     */
    @Override
    public String getLabel() throws InvalidRegistryObjectException {
        return "";
    }

    @Override
    public String getNamespace() throws InvalidRegistryObjectException {
        return getNamespaceIdentifier();
    }

    @Override
    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
        return namespaceIdentifier;
    }

    @Override
    public String getSimpleIdentifier() throws InvalidRegistryObjectException {
        return simpleIdentifier;
    }

    @Override
    public String getUniqueIdentifier() throws InvalidRegistryObjectException {
        StringBuffer buf = new StringBuffer();
        buf.append(namespaceIdentifier);
        if (!StringUtils.isEmpty(namespaceIdentifier)) {
            buf.append('.');

        }
        buf.append(simpleIdentifier);
        return buf.toString();
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
