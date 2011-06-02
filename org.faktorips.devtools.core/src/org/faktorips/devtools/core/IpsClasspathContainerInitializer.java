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

package org.faktorips.devtools.core;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class IpsClasspathContainerInitializer extends ClasspathContainerInitializer {

    public static final String RUNTIME_BUNDLE = "org.faktorips.runtime.java5"; //$NON-NLS-1$

    public static final String VALUETYPES_BUNDLE = "org.faktorips.valuetypes.java5"; //$NON-NLS-1$

    public IpsClasspathContainerInitializer() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        IClasspathContainer[] respectiveContainers = new IClasspathContainer[] { new IpsClasspathContainer(
                containerPath) };
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, respectiveContainers, null);

    }

    private IPath getBundlePath(String pluginId) {
        Bundle bundle = getBundle(pluginId);
        if (bundle == null) {
            return null;
        }

        URL installLocation = bundle.getResource(""); //$NON-NLS-1$

        if (installLocation == null) {
            return null;
        }
        // Install location is something like bundleentry://140/
        URL local = null;
        try {
            local = FileLocator.toFileURL(installLocation);
            return new Path(local.getPath());
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus(
                    "Error initializing classpath variable. Bundle install locaction: " + installLocation, e)); //$NON-NLS-1$
            return null;
        }
    }

    private IPath getSourcPath(String pluginId) {
        Bundle bundle = getBundle(pluginId);
        if (bundle == null) {
            return null;
        }

        URL installLocation = bundle.getEntry(""); //$NON-NLS-1$
        if (installLocation == null) {
            //            installLocation = bundle.getEntry("/src"); //$NON-NLS-1$
            // if (installLocation == null) {
            return null;

            // }
        }
        // Install location is something like bundleentry://140/faktorips-util.jar
        URL local = null;
        try {
            local = FileLocator.toFileURL(installLocation);
            return new Path(local.getPath());
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus(
                    "Error initializing classpath variable. Bundle install locaction: " + installLocation, e)); //$NON-NLS-1$
            return null;
        }
    }

    protected Bundle getBundle(String pluginId) {
        Bundle bundle = Platform.getBundle(pluginId);
        if (bundle == null) {
            IpsPlugin.log(new IpsStatus("Error initializing classpath container. Bundle " + pluginId + "not found.")); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        return bundle;
    }

    class IpsClasspathContainer implements IClasspathContainer {

        private final IPath containerPath;
        private IClasspathEntry[] entries;

        public IpsClasspathContainer(IPath containerPath) {
            this.containerPath = containerPath;
            IClasspathEntry runtime = JavaCore.newLibraryEntry(getBundlePath(RUNTIME_BUNDLE),
                    getSourcPath(RUNTIME_BUNDLE), null);
            IClasspathEntry valuetypes = JavaCore.newLibraryEntry(getBundlePath(VALUETYPES_BUNDLE),
                    getSourcPath(VALUETYPES_BUNDLE), null);
            entries = new IClasspathEntry[] { runtime, valuetypes };
        }

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            return entries;
        }

        @Override
        public String getDescription() {
            return "Faktor-IPS Library";
        }

        @Override
        public int getKind() {
            return K_APPLICATION;
        }

        @Override
        public IPath getPath() {
            return containerPath;
        }

    }

}
