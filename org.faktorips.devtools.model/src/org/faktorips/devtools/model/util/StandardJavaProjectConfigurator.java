/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;

/**
 * Configures a standard Java IPS-Project.
 * <p>
 * Use this standard configurator if no extension of the extension point
 * {@link ExtensionPoints#ADD_IPS_NATURE} is responsible for configuring the project.
 * 
 * @author Florian Orendi
 */
public class StandardJavaProjectConfigurator {

    /**
     * Configures a {@link IJavaProject} for the usage of Faktor-IPS.
     * 
     * @param javaProject The java project to be configured
     * @throws CoreException if configuring failed
     */
    public static void configureIpsProject(IJavaProject javaProject)
            throws CoreException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        if (targetVersionIsAtLeast5(javaProject)) {
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];
            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
            entries[oldEntries.length] = JavaCore.newContainerEntry(IpsClasspathContainerInitializer
                    .newDefaultEntryPath());
            javaProject.setRawClasspath(entries, null);
        }
    }

    /**
     * Checks whether the java version of a {@link IJavaProject} is at least Java 1.5.
     * 
     * @param javaProject The project to be checked
     * @return {@code true} whether the java version is at least 1.5, else {@code false}
     */
    private static boolean targetVersionIsAtLeast5(IJavaProject javaProject) {
        String[] targetVersion = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true).split("\\."); //$NON-NLS-1$
        return (Integer.parseInt(targetVersion[0]) == 1 && Integer.parseInt(targetVersion[1]) >= 5)
                || Integer.parseInt(targetVersion[0]) > 1;
    }
}
