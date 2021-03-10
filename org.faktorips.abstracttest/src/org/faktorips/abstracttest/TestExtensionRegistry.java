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

import java.io.InputStream;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IRegistryEventListener;

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
        this.extensionPoints = extensionPoints;
    }

    /**
     * Throws RuntimeException
     */
    @Override
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
    @Override
    public void addRegistryChangeListener(IRegistryChangeListener listener) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void addRegistryChangeListener(IRegistryChangeListener listener, String namespace) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName) {
        return Arrays.stream(extensionPoints)
                .filter(e -> e.getNamespaceIdentifier().equals(namespace))
                .map(IExtensionPoint::getExtensions)
                .flatMap(Arrays::stream)
                .map(IExtension::getConfigurationElements)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not implemented"));
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IConfigurationElement[] getConfigurationElementsFor(String namespace,
            String extensionPointName,
            String extensionId) {
        return Arrays.stream(extensionPoints)
                .filter(e -> e.getNamespaceIdentifier().equals(namespace))
                .map(e -> e.getExtension(extensionId))
                .filter(e -> e != null)
                .map(IExtension::getConfigurationElements)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not implemented"));
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IExtension getExtension(String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IExtension getExtension(String extensionPointId, String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IExtension getExtension(String namespace, String extensionPointName, String extensionId) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
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

    @Override
    public IExtensionPoint getExtensionPoint(String namespace, String extensionPointName) {
        for (IExtensionPoint extensionPoint : extensionPoints) {
            if (extensionPoint.getNamespaceIdentifier().equals(namespace)
                    && extensionPoint.getSimpleIdentifier().equals(extensionPointName)) {
                return extensionPoint;
            }

        }
        return null;
    }

    @Override
    public IExtensionPoint[] getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IExtensionPoint[] getExtensionPoints(String namespace) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public IExtension[] getExtensions(String namespace) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public String[] getNamespaces() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public boolean removeExtension(IExtension extension, Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public boolean removeExtensionPoint(IExtensionPoint extensionPoint, Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void removeRegistryChangeListener(IRegistryChangeListener listener) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void stop(Object token) throws IllegalArgumentException {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    @Override
    public void addListener(IRegistryEventListener arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    @Override
    public void addListener(IRegistryEventListener arg0, String arg1) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    @Override
    public IExtensionPoint[] getExtensionPoints(IContributor arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    @Override
    public IExtension[] getExtensions(IContributor arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.4 (Ganymede)
    @Override
    public void removeListener(IRegistryEventListener arg0) {
        throw new RuntimeException("Not implemented");
    }

    // @since Eclipse 3.6 (Helios)
    @Override
    public boolean isMultiLanguage() {
        throw new RuntimeException("Not implemented");
    }

}
