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
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;

/**
 * Configures a standard Java IPS-Project.
 * <p>
 * Use this standard configurator if no extension of the extension point
 * {@link ExtensionPoints#ADD_IPS_NATURE} is responsible for configuring the project.
 * 
 * @since 21.6
 * @author Florian Orendi
 */
public class StandardJavaProjectConfigurator implements IIpsProjectConfigurator {

    /**
     * Configures a {@link IJavaProject} for the usage of Faktor-IPS.
     * 
     * @param javaProject The java project to be configured
     * @throws CoreException if configuring failed
     * @deprecated this method is only here to support the deprecated
     *             {@link ProjectUtil#createIpsProject(IJavaProject, String, boolean, boolean, boolean, java.util.List)}
     *             method without code duplication
     */
    @Deprecated
    public static void configureDefaultIpsProject(IJavaProject javaProject)
            throws CoreException {
        configureJavaProject(javaProject, false, false);
    }

    @Override
    public boolean canConfigure(IJavaProject javaProject) {
        return true;
    }

    @Override
    public boolean isGroovySupported(IJavaProject javaProject) {
        return IpsClasspathContainerInitializer.isGroovySupportAvailable();
    }

    @Override
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        IJavaProject javaProject = ipsProject.getJavaProject();
        boolean isJodaSupportAvailable = IpsClasspathContainerInitializer.isJodaSupportAvailable();
        boolean isGroovySupportAvailable = IpsClasspathContainerInitializer.isGroovySupportAvailable()
                && creationProperties.isGroovySupport();
        configureJavaProject(javaProject, isJodaSupportAvailable, isGroovySupportAvailable);
    }

    public static void configureJavaProject(IJavaProject javaProject, boolean addJodaSupport, boolean addGroovySupport)
            throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);

        IClasspathAttribute[] extraAttributes = {};
        if (javaProject.getModuleDescription() != null) {
            extraAttributes = new IClasspathAttribute[] {
                    JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") }; //$NON-NLS-1$
        }
        IClasspathEntry ipsContainerEntry = JavaCore.newContainerEntry(IpsClasspathContainerInitializer
                .newEntryPath(addJodaSupport, addGroovySupport),
                new IAccessRule[0], extraAttributes, false);
        entries[oldEntries.length] = ipsContainerEntry;
        javaProject.setRawClasspath(entries, null);
    }
}
