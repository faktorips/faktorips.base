/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

@Mojo(name = "faktorips-clean", defaultPhase = LifecyclePhase.CLEAN, threadSafe = true)
public class IpsCleanMojo extends AbstractMojo {

    /**
     * Work area. This includes:
     * <ul>
     * <li><b>&lt;work&gt;/configuration</b>: The configuration area (<b>-configuration</b>)
     * <li><b>&lt;work&gt;/data</b>: The data ('workspace') area (<b>-data</b>)
     * </ul>
     */
    @Parameter(defaultValue = "${java.io.tmpdir}/${project.name}/eclipserun-work")
    private File work;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File workDir = work.getAbsoluteFile();
        if (workDir.exists()) {
            try {
                FileUtils.deleteDirectory(workDir);
            } catch (IOException e) {
                throw new MojoExecutionException("Error while cleaning work directory " + workDir.getAbsolutePath(),
                        e);
            }
        }
    }
}
