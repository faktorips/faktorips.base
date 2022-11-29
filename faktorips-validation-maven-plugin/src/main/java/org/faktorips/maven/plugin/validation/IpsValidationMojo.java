/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.validation;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

/**
 * Validates a Faktor-IPS project.
 */
@Mojo(name = "faktorips-validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class IpsValidationMojo extends AbstractMojo {

    /**
     * Whether to skip mojo execution.
     */
    @Parameter(property = "faktorips.skipValidation", defaultValue = "false")
    private boolean skip;

    @Component
    private Logger logger;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("skipping mojo execution");
            return;
        }

        // TODO Implement Validation
    }
}
