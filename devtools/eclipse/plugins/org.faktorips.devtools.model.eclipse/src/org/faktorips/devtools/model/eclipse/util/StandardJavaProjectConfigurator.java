/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.util;

import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.eclipse.internal.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.extensions.IpsProjectConfigurerExtension;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;

/**
 * Configures a standard Java IPS-Project.
 * <p>
 * Use this standard configurator if no extension of the extension point
 * {@link IpsProjectConfigurerExtension#EXTENSION_POINT_ID_ADD_IPS_NATURE} is responsible for
 * configuring the project.
 * 
 * @since 21.6
 * @author Florian Orendi
 */
public class StandardJavaProjectConfigurator implements IIpsProjectConfigurator {

    /**
     * Configures a {@link IJavaProject} for the usage of Faktor-IPS.
     * 
     * @param javaProject The java project to be configured
     * @throws IpsException if configuring failed
     * @deprecated this method is only here to support the deprecated
     *                 {@link EclipseProjectUtil#createIpsProject(IJavaProject, String, boolean, boolean, boolean, java.util.List)}
     *                 method without code duplication
     */
    @Deprecated
    public static void configureDefaultIpsProject(IJavaProject javaProject) {
        try {
            configureJavaProject(javaProject, false, false);
        } catch (JavaModelException e) {
            throw new IpsException(e);
        }
    }

    @Override
    public boolean canConfigure(AJavaProject javaProject) {
        return true;
    }

    @Override
    public boolean isGroovySupported(AJavaProject javaProject) {
        return IpsClasspathContainerInitializer.isGroovySupportAvailable();
    }

    @Override
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        boolean isJodaSupportAvailable = IpsClasspathContainerInitializer.isJodaSupportAvailable();
        boolean isGroovySupportAvailable = IpsClasspathContainerInitializer.isGroovySupportAvailable()
                && creationProperties.isGroovySupport();
        try {
            configureJavaProject(javaProject, isJodaSupportAvailable, isGroovySupportAvailable);
        } catch (JavaModelException e) {
            throw new IpsException(e);
        }
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
                .newEntryPath(addJodaSupport, addGroovySupport, JaxbSupportVariant.None),
                new IAccessRule[0], extraAttributes, false);
        entries[oldEntries.length] = ipsContainerEntry;
        javaProject.setRawClasspath(entries, null);
    }
}
