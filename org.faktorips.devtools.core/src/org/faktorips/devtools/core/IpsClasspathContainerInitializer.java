/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.Bundle;

public class IpsClasspathContainerInitializer extends ClasspathContainerInitializer {

    public static final String CONTAINER_ID = "org.faktorips.devtools.core.ipsClasspathContainer"; //$NON-NLS-1$

    public static final IPath ENTRY_PATH = new Path(CONTAINER_ID);

    private static final String NAME_VERSION_SEP = "_"; //$NON-NLS-1$

    public static final String RUNTIME_BUNDLE = "org.faktorips.runtime.java5"; //$NON-NLS-1$

    public static final String VALUETYPES_BUNDLE = "org.faktorips.valuetypes.java5"; //$NON-NLS-1$

    public static final String JODA_BUNDLE = "org.faktorips.valuetypes.joda"; //$NON-NLS-1$

    public static final String GROOVY_BUNDLE = "org.faktorips.runtime.groovy"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if container entry specifies that the support library for the JODA
     * library should be included, otherwise <code>false</code>.
     */
    public final static boolean isJodaSupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, JODA_BUNDLE);
    }

    /**
     * Returns <code>true</code> if container entry specifies that the support library for
     * evaluation formulas with Groovy should be included, otherwise <code>false</code>.
     */
    public final static boolean isGroovySupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, GROOVY_BUNDLE);
    }

    /**
     * Returns <code>true</code> if container entry specifies that the given bundle should be
     * included, otherwise <code>false</code>.
     */
    private final static boolean isAdditionalBundleIdsIncluded(IClasspathEntry containerEntry, String bundleId) {
        ArgumentCheck.notNull(containerEntry);
        ArgumentCheck.notNull(bundleId);
        String[] bundleIds = getAdditionalBundleIds(containerEntry);
        for (int i = 0; i < bundleIds.length; i++) {
            if (bundleId.equals(bundleIds[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the additional bundleIds that are specified to be included by the container entry.
     * 
     * @throws NullPointerException if containerPath is <code>null</code>.
     */
    private final static String[] getAdditionalBundleIds(IClasspathEntry containerEntry) {
        return getAdditionalBundleIds(containerEntry.getPath());
    }

    /**
     * Returns the additional bundleIds that are specified to be included by the container path.
     * 
     * @throws NullPointerException if containerPath is <code>null</code>.
     */
    public final static String[] getAdditionalBundleIds(IPath containerPath) {
        if (containerPath.segmentCount() == 2 && !containerPath.lastSegment().isEmpty()) {
            String lastSegment = containerPath.lastSegment();
            return lastSegment.split(","); //$NON-NLS-1$
        }
        return new String[0];
    }

    /**
     * Creates a new container entry path for the Faktor-IPS runtime and valuetypes libraries and
     * for additional optional libraries.
     * 
     * @param includeJoda <code>true</code> if the support library for JODA should be included.
     * @param includeGroovy <code>true</code> if the support library for GROOVY should be included.
     * 
     * @return A Path like
     *         "org.faktorips.devtools.core.ipsClasspathContainer/org.faktorips.valuetypes.joda,org.faktorips.runtime.groovy"
     *         if both additional libraries are included.
     */
    public static final IPath newEntryPath(boolean includeJoda, boolean includeGroovy) {
        List<String> bundleIds = new ArrayList<String>();
        if (includeJoda) {
            bundleIds.add(JODA_BUNDLE);
        }
        if (includeGroovy) {
            bundleIds.add(GROOVY_BUNDLE);
        }
        return newEntryPath(bundleIds);
    }

    /**
     * Creates a new container entry path for the Faktor-IPS runtime and valuetypes libraries and
     * for additional optional libraries.
     * 
     * @return A Path like "org.faktorips.devtools.core.ipsClasspathContainer/bundleId1,bundleId2"
     */
    private static final IPath newEntryPath(List<String> additionalBundles) {
        String path = CONTAINER_ID;
        for (int i = 0; i < additionalBundles.size(); i++) {
            if (i == 0) {
                path = path + '/';
            } else {
                path = path + ',';
            }
            path = path + additionalBundles.get(i);
        }
        return new Path(path);
    }

    public IpsClasspathContainerInitializer() {
        // empty constructor
    }

    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        IClasspathContainer[] respectiveContainers = new IClasspathContainer[] { new IpsClasspathContainer(
                containerPath) };
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, respectiveContainers, null);
    }

    class IpsClasspathContainer implements IClasspathContainer {

        private final IPath containerPath;
        private IClasspathEntry[] entries;

        public IpsClasspathContainer(IPath containerPath) {
            this.containerPath = containerPath;
            ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();

            IClasspathEntry runtime = JavaCore.newLibraryEntry(getBundlePath(RUNTIME_BUNDLE, false),
                    getBundlePath(RUNTIME_BUNDLE, true), null);
            IClasspathEntry valuetypes = JavaCore.newLibraryEntry(getBundlePath(VALUETYPES_BUNDLE, false),
                    getBundlePath(VALUETYPES_BUNDLE, true), null);

            entryList.add(runtime);
            entryList.add(valuetypes);

            String[] addEntries = getAdditionalBundleIds(containerPath);
            for (String additionalEntry : addEntries) {
                IClasspathEntry addEntry = JavaCore.newLibraryEntry(getBundlePath(additionalEntry, false),
                        getBundlePath(additionalEntry, true), null);
                entryList.add(addEntry);
            }
            entries = entryList.toArray(new IClasspathEntry[entryList.size()]);
        }

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            return entries;
        }

        @Override
        public String getDescription() {
            return Messages.IpsClasspathContainerInitializer_containerDescription;
        }

        @Override
        public int getKind() {
            return K_APPLICATION;
        }

        @Override
        public IPath getPath() {
            return containerPath;
        }

        private IPath getBundlePath(String pluginId, boolean sources) {
            Bundle bundle = Platform.getBundle(pluginId);
            if (bundle == null) {
                IpsPlugin
                        .log(new IpsStatus(
                                "Error initializing " + (sources ? "source for " : "") + "classpath container. Bundle " + pluginId + " not found.")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                return null;
            }

            URL installLocation;
            if (sources) {
                try {
                    installLocation = bundle.getEntry("src"); //$NON-NLS-1$ 
                    String fullPath;
                    if (installLocation != null) {
                        URL local = FileLocator.toFileURL(installLocation);
                        fullPath = new File(local.getPath()).getAbsolutePath();
                    } else {
                        fullPath = FileLocator.getBundleFile(bundle).getAbsolutePath();
                        String[] split = fullPath.split(NAME_VERSION_SEP);
                        if (split.length < 2) {
                            return null;
                        }
                        split[split.length - 2] = split[split.length - 2] + ".source"; //$NON-NLS-1$
                        fullPath = StringUtils.EMPTY;
                        for (String string : split) {
                            if (string != split[split.length - 1]) {
                                fullPath += string + NAME_VERSION_SEP;
                            } else {
                                fullPath += string;
                            }
                        }
                    }
                    return Path.fromOSString(fullPath);
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus(
                            "Error initializing classpath container for source bundle " + pluginId, e)); //$NON-NLS-1$ 
                    return null;
                }
            } else {
                try {
                    installLocation = bundle.getEntry("bin"); //$NON-NLS-1$ 
                    String fullPath;
                    if (installLocation != null) {
                        URL local = FileLocator.toFileURL(installLocation);
                        fullPath = new File(local.getPath()).getAbsolutePath();
                    } else {
                        fullPath = FileLocator.getBundleFile(bundle).getAbsolutePath();
                    }
                    return Path.fromOSString(fullPath);
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus("Error initializing classpath container for bundle " + pluginId, e)); //$NON-NLS-1$ 
                    return null;
                }
            }
        }

    }

}
