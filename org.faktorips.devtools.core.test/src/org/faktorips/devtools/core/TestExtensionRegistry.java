/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IExtensionRegistry} interface for testing purposes. Not all of
 * the methods are implemented but can be implemented as needed. Those methods that are not
 * implemented throw a RuntimeException.
 * 
 * @author Peter Erzberger
 */
public class TestExtensionRegistry implements IExtensionRegistry {

    private IExtensionPoint[] extensionPoints;

    /**
     * Creates a new TestExtensionRegistry for the provided {@link IExtensionPoint}s.
     */
    public TestExtensionRegistry(IExtensionPoint[] extensionPoints) {
        ArgumentCheck.notNull(extensionPoints, this);
        this.extensionPoints = extensionPoints;
    }

    /**
     * Throws RuntimeException
     */
    public boolean addContribution(InputStream is,
            IContributor contributor,
            boolean persist,
            String name,
            ResourceBundle translationBundle,
            Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public void addRegistryChangeListener(IRegistryChangeListener listener) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public void addRegistryChangeListener(IRegistryChangeListener listener, String namespace) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IConfigurationElement[] getConfigurationElementsFor(String namespace,
            String extensionPointName,
            String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IExtension getExtension(String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IExtension getExtension(String extensionPointId, String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IExtension getExtension(String namespace, String extensionPointName, String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IExtensionPoint getExtensionPoint(String extensionPointId) {
        if (extensionPointId == null) {
            return null;
        }
        int index = extensionPointId.lastIndexOf('.');
        String namespace = "";
        String extensionPointName = "";
        if (index != -1) {
            namespace = extensionPointId.substring(0, index);
            extensionPointName = extensionPointId.substring(index + 1);
        } else {
            extensionPointName = extensionPointId;
        }
        return getExtensionPoint(namespace, extensionPointName);
    }

    /**
     * {@inheritDoc}
     */
    public IExtensionPoint getExtensionPoint(String namespace, String extensionPointName) {
        for (int i = 0; i < extensionPoints.length; i++) {
            if (extensionPoints[i].getNamespaceIdentifier().equals(namespace)
                    && extensionPoints[i].getSimpleIdentifier().equals(extensionPointName)) {
                return extensionPoints[i];
            }

        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IExtensionPoint[] getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Throws RuntimeException
     */
    public IExtensionPoint[] getExtensionPoints(String namespace) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public IExtension[] getExtensions(String namespace) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public String[] getNamespaces() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public boolean removeExtension(IExtension extension, Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public boolean removeExtensionPoint(IExtensionPoint extensionPoint, Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public void removeRegistryChangeListener(IRegistryChangeListener listener) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    public void stop(Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    public void addListener(IRegistryEventListener arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    public void addListener(IRegistryEventListener arg0, String arg1) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    public IExtensionPoint[] getExtensionPoints(IContributor arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    public IExtension[] getExtensions(IContributor arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    public void removeListener(IRegistryEventListener arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.6 (Helios)
    public boolean isMultiLanguage() {
        throw new RuntimeException("Not implemented");
    }

}
