/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.StandardJavaProjectConfigurator;

/**
 * Implementations for the Faktor-IPS model extension point {@value ExtensionPoints#ADD_IPS_NATURE}
 * must implement this interface.
 * <p>
 * A project configurator is called after the IPS-Nature has been added to a project to configure
 * the project, for example by adding dependencies to Faktor-IPS runtime libraries.
 * 
 * @author Florian Orendi
 */
public interface IIpsProjectConfigurator {

    /**
     * Checks whether the passed project can be configured using this extension.
     * 
     * @implSpec Check for hints that this project is not just a standard Eclipse-Java-project, for
     *           example the existence of a file for a different kind of dependency management.
     * 
     * @implNote If no extension is responsible for a project, the standard configurator
     *           {@link StandardJavaProjectConfigurator} will be used.
     * 
     * @param project the project to be configured
     * @return whether this configurator can configure the project
     */
    boolean canConfigure(IProject project);

    /**
     * Checks whether Groovy is supported by the extension.
     * 
     * @return {@code true} if Groovy is supported by the extension, else {@code false}
     */
    boolean isGroovySupported();

    /**
     * Configures an existent project, which already contains the IPS-Nature, for the usage of
     * Faktor-IPS.
     * 
     * @param ipsProject the existing {@link IIpsProject}
     * @param creationProperties the required {@link IpsProjectCreationProperties properties for
     *            creating a Faktor-IPS project}
     * @throws CoreException if configuring the Faktor-IPS project failed
     */
    void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
            throws CoreException;
}
