/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core;

import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeListener;
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
    public TestExtensionRegistry(IExtensionPoint[] extensionPoints){
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
        throw new RuntimeException("Not implemented");
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

}
