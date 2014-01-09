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

    public static final String RUNTIME_BUNDLE = "org.faktorips.runtime.java5"; //$NON-NLS-1$

    public static final String VALUETYPES_BUNDLE = "org.faktorips.valuetypes.java5"; //$NON-NLS-1$

    public static final String JODA_BUNDLE = "org.faktorips.valuetypes.joda"; //$NON-NLS-1$

    public static final String GROOVY_BUNDLE = "org.faktorips.runtime.groovy"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if the given groovy support bundle is available, otherwise
     * <code>false</code>.
     */
    public final static boolean isGroovySupportAvailable() {
        return Platform.getBundle(GROOVY_BUNDLE) != null;
    }

    /**
     * Returns <code>true</code> if the given JODA support bundle is available, otherwise
     * <code>false</code>.
     */
    public final static boolean isJodaSupportAvailable() {
        return Platform.getBundle(JODA_BUNDLE) != null;
    }

    /**
     * Returns <code>true</code> if the given bundle is available, otherwise <code>false</code>.
     */
    public final static boolean isPluginAvailable(String bundleId) {
        return Platform.getBundle(bundleId) != null;
    }

    /**
     * Returns <code>true</code> if container entry specifies that the support library for the JODA
     * library should be included, otherwise <code>false</code>.
     */
    public final static boolean isJodaSupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, JODA_BUNDLE);
    }

    /**
     * Returns <code>true</code> if container entry specifies that the support library for
     * evaluation formulas with Groovy should be included, otherwise <code>false</code>. Returns
     * <code>false</code> if containerEntry is <code>null</code>.
     */
    public final static boolean isGroovySupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, GROOVY_BUNDLE);
    }

    /**
     * Returns <code>true</code> if container entry specifies that the given bundle should be
     * included, otherwise <code>false</code>. Returns <code>false</code> if containerEntry is
     * <code>null</code>.
     */
    private final static boolean isAdditionalBundleIdsIncluded(IClasspathEntry containerEntry, String bundleId) {
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
     * Returns an empty array if containerEntry is <code>null</code>.
     */
    public final static String[] getAdditionalBundleIds(IClasspathEntry containerEntry) {
        if (containerEntry == null) {
            return new String[0];
        }
        return getAdditionalBundleIds(containerEntry.getPath());
    }

    /**
     * Returns the additional bundleIds that are specified to be included by the container path.
     * Returns an empty array if containerPath is <code>null</code>.
     */
    public final static String[] getAdditionalBundleIds(IPath containerPath) {
        if (containerPath == null) {
            return new String[0];
        }
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

    static class IpsClasspathContainer implements IClasspathContainer {

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
                if (isPluginAvailable(additionalEntry)) {
                    IClasspathEntry addEntry = JavaCore.newLibraryEntry(getBundlePath(additionalEntry, false),
                            getBundlePath(additionalEntry, true), null);
                    entryList.add(addEntry);
                } else {
                    IpsPlugin.log(new IpsStatus("Can't create classpath entry for " + additionalEntry //$NON-NLS-1$
                            + ", plugin is not available.")); //$NON-NLS-1$
                }
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

        IPath getBundlePath(String pluginId, boolean sources) {
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
                        fullPath = getSourceBundlePath(FileLocator.getBundleFile(bundle).getAbsolutePath(), pluginId);
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

        /* private */String getSourceBundlePath(String fullPath, String pluginId) {
            // looks strange but does replace every '.' with '\.' to use in split
            String pluginIdForRegext = pluginId.replaceAll("\\.", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
            String[] split = fullPath.split(pluginIdForRegext);

            if (split.length < 2) {
                return null;
            }
            fullPath = StringUtils.EMPTY;
            for (String string : split) {
                if (!string.equals(split[split.length - 1])) {
                    fullPath += string + pluginId;
                } else {
                    fullPath += ".source" + string; //$NON-NLS-1$
                }
            }
            return fullPath;
        }

    }

}
