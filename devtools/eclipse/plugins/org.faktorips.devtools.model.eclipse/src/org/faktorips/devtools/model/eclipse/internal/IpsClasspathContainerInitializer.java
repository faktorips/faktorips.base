/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.plugin.Messages;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.Bundle;

public class IpsClasspathContainerInitializer extends ClasspathContainerInitializer {

    public static final String CONTAINER_ID = "org.faktorips.devtools.model.eclipse.ipsClasspathContainer"; //$NON-NLS-1$

    public static final IPath ENTRY_PATH = new Path(CONTAINER_ID);

    public static final String RUNTIME_BUNDLE = "org.faktorips.runtime"; //$NON-NLS-1$

    public static final String VALUETYPES_BUNDLE = "org.faktorips.valuetypes"; //$NON-NLS-1$

    public static final String JODA_BUNDLE = "org.faktorips.valuetypes.joda"; //$NON-NLS-1$

    public static final String GROOVY_BUNDLE = "org.faktorips.runtime.groovy"; //$NON-NLS-1$

    public static final String CLASSIC_JAXB_BUNDLE = "org.faktorips.runtime.xml.javax"; //$NON-NLS-1$

    public static final String JAKARTA_BUNDLE = "org.faktorips.runtime.xml.jakarta"; //$NON-NLS-1$

    /**
     * Returns {@code true} if either jakarta or classic jaxb support is available, otherwise
     * <code>false</code>.
     */
    public static final boolean isJaxbSupportAvailable() {
        return Platform.getBundle(CLASSIC_JAXB_BUNDLE) != null || Platform.getBundle(JAKARTA_BUNDLE) != null;
    }

    /**
     * Returns {@code true} if Groovy support is available, otherwise <code>false</code>.
     */
    public static final boolean isGroovySupportAvailable() {
        return Platform.getBundle(GROOVY_BUNDLE) != null;
    }

    /**
     * Returns {@code true} if the given JODA support bundle is available, otherwise
     * <code>false</code>.
     */
    public static final boolean isJodaSupportAvailable() {
        return Platform.getBundle(JODA_BUNDLE) != null;
    }

    /**
     * Returns {@code true} if the given bundle is available, otherwise <code>false</code>.
     */
    public static final boolean isPluginAvailable(String bundleId) {
        return Platform.getBundle(bundleId) != null;
    }

    /**
     * Returns {@code true} if container entry specifies that the support library for the JODA
     * library should be included, otherwise <code>false</code>.
     */
    public static final boolean isJodaSupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, JODA_BUNDLE);
    }

    /**
     * Returns {@code true} if container entry specifies that the support library for evaluation
     * formulas with Groovy should be included, otherwise <code>false</code>. Returns
     * <code>false</code> if containerEntry is <code>null</code>.
     */
    public static final boolean isGroovySupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, GROOVY_BUNDLE);
    }

    /**
     * Returns {@code true} if container entry specifies that the support library for classic jaxb
     * should be included, otherwise <code>false</code>. Returns <code>false</code> if
     * containerEntry is <code>null</code>.
     */
    public static final boolean isClassicJaxbSupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, CLASSIC_JAXB_BUNDLE);
    }

    /**
     * Returns {@code true} if container entry specifies that the support library for jakarta should
     * be included, otherwise <code>false</code>. Returns <code>false</code> if containerEntry is
     * <code>null</code>.
     */
    public static final boolean isJakartaSupportIncluded(IClasspathEntry containerEntry) {
        return isAdditionalBundleIdsIncluded(containerEntry, JAKARTA_BUNDLE);
    }

    /**
     * Returns {@code true} if container entry specifies that the given bundle should be included,
     * otherwise <code>false</code>. Returns <code>false</code> if containerEntry is
     * <code>null</code>.
     */
    private static final boolean isAdditionalBundleIdsIncluded(IClasspathEntry containerEntry, String bundleId) {
        ArgumentCheck.notNull(bundleId);
        String[] bundleIds = getAdditionalBundleIds(containerEntry);
        for (String bundleId2 : bundleIds) {
            if (bundleId.equals(bundleId2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the additional bundleIds that are specified to be included by the container entry.
     * Returns an empty array if containerEntry is <code>null</code>.
     */
    public static final String[] getAdditionalBundleIds(IClasspathEntry containerEntry) {
        if (containerEntry == null) {
            return new String[0];
        }
        return getAdditionalBundleIds(containerEntry.getPath());
    }

    /**
     * Returns the additional bundleIds that are specified to be included by the container path.
     * Returns an empty array if containerPath is <code>null</code>.
     */
    public static final String[] getAdditionalBundleIds(IPath containerPath) {
        if (containerPath == null) {
            return new String[0];
        }
        if (containerPath.segmentCount() == 2 && !containerPath.lastSegment().isEmpty()) {
            String lastSegment = containerPath.lastSegment();
            return lastSegment.split(","); //$NON-NLS-1$
        }
        return new String[0];
    }

    public static IPath newDefaultEntryPath() {
        return newEntryPath(isJodaSupportAvailable(), isGroovySupportAvailable(), JaxbSupportVariant.None);
    }

    /**
     * Creates a new container entry path for the Faktor-IPS runtime and valuetypes libraries and
     * for additional optional libraries.
     *
     * @param includeJoda {@code true} if the support library for JODA should be included.
     * @param includeGroovy {@code true} if the support library for GROOVY should be included.
     * @param jaxbSupport the selected {@link JaxbSupportVariant}.
     *
     * @return A Path like
     *             "org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda,org.faktorips.runtime.groovy"
     *             if both additional libraries are included.
     */
    public static final IPath newEntryPath(boolean includeJoda,
            boolean includeGroovy,
            JaxbSupportVariant jaxbSupport) {
        List<String> bundleIds = new ArrayList<>();
        if (includeJoda) {
            bundleIds.add(JODA_BUNDLE);
        }
        if (includeGroovy) {
            bundleIds.add(GROOVY_BUNDLE);
        }
        if (JaxbSupportVariant.ClassicJAXB.equals(jaxbSupport)) {
            bundleIds.add(CLASSIC_JAXB_BUNDLE);
        }
        if (JaxbSupportVariant.JakartaXmlBinding.equals(jaxbSupport)) {
            bundleIds.add(JAKARTA_BUNDLE);
        }
        return newEntryPath(bundleIds);
    }

    /**
     * Creates a new container entry path for the Faktor-IPS runtime and valuetypes libraries and
     * for additional optional libraries.
     *
     * @return A Path like
     *             "org.faktorips.devtools.model.eclipse.ipsClasspathContainer/bundleId1,bundleId2"
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

    @Override
    public void initialize(IPath containerPath, IJavaProject project) {
        IClasspathContainer[] respectiveContainers = { new IpsClasspathContainer(
                containerPath) };
        try {
            JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, respectiveContainers, null);
        } catch (JavaModelException e) {
            throw new IpsException(e);
        }
    }

    static class IpsClasspathContainer implements IClasspathContainer {

        private final IPath containerPath;
        private IClasspathEntry[] entries;

        public IpsClasspathContainer(IPath containerPath) {
            this.containerPath = containerPath;
            ArrayList<IClasspathEntry> entryList = new ArrayList<>();

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
                    IpsLog.log(new IpsStatus("Can't create classpath entry for " + additionalEntry //$NON-NLS-1$
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
                IpsLog.log(new IpsStatus(
                        "Error initializing " + (sources ? "source for " : "") + "classpath container. Bundle " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                + pluginId + " not found.")); //$NON-NLS-1$
                return null;
            }

            try {
                File local = getBundleEntry(bundle, sources);
                String fullPath;
                if (local != null) {
                    fullPath = local.getCanonicalFile().getAbsolutePath();
                } else {
                    fullPath = getBundleFileName(pluginId, bundle, sources);
                    requireNonNull(fullPath, "Can't get bundle file name for " + pluginId); //$NON-NLS-1$
                }
                return Path.fromOSString(fullPath);
            } catch (IOException e) {
                IpsLog.log(new IpsStatus("Error initializing classpath container for " //$NON-NLS-1$
                        + (sources ? "source " : IpsStringUtils.EMPTY) + "source bundle " + pluginId, e)); //$NON-NLS-1$//$NON-NLS-2$
                return null;
            }
        }

        private File getBundleEntry(Bundle bundle, boolean sources) throws IOException {
            if (bundle.getLocation().endsWith(".jar")) {
                return null;
            }
            var plainBundle = bundle.getEntry(sources ? "src" : "bin"); //$NON-NLS-1$//$NON-NLS-2$
            if (plainBundle != null) {
                URL local = FileLocator.toFileURL(plainBundle);
                return new File(local.getPath());
            } else {
                URL mavenBundle = bundle.getEntry("/"); //$NON-NLS-1$
                URL local = FileLocator.toFileURL(mavenBundle);

                if (sources) {
                    try {
                        local = local.toURI().resolve("../../src/main/java").toURL(); //$NON-NLS-1$
                    } catch (MalformedURLException | URISyntaxException e) {
                        throw new IpsException("Can't convert " + local + " into an URI", e);
                    }
                }
                return new File(local.getPath());
            }
        }

        private String getBundleFileName(String pluginId, Bundle bundle, boolean sourceBundle) {

            return FileLocator.getBundleFileLocation(bundle).map(file -> {
                if (sourceBundle) {
                    return getSourceBundlePath(file.getAbsolutePath(), pluginId);
                }
                return file.getAbsolutePath();
            }).orElse(null);
        }

        /* private */String getSourceBundlePath(String fullPath, String pluginId) {
            String pluginIdForRegext = Pattern.quote(pluginId);
            String[] split = fullPath.split(pluginIdForRegext);

            if (split.length < 2) {
                // pluginId is not in the file name
                String sourceJarPath = fullPath.replace(".jar", "-sources.jar");
                if (new File(sourceJarPath).exists()) {
                    return sourceJarPath;
                }
                return null;
            }
            String result = IpsStringUtils.EMPTY;
            for (String string : split) {
                if (!string.equals(split[split.length - 1])) {
                    result += string + pluginId;
                } else {
                    result += ".source" + string; //$NON-NLS-1$
                }
            }
            return result;
        }

    }

}
