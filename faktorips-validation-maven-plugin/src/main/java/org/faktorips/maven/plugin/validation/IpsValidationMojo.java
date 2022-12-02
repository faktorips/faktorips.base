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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaWorkspace;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

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

        PlainJavaWorkspace plainJavaWorkspace = new PlainJavaWorkspace(project.getBasedir().getParentFile());
        PlainJavaImplementation.get().setWorkspace(plainJavaWorkspace);
        AProject aProject = Abstractions.getWorkspace().getRoot().getProject(project.getBasedir().getName());
        IIpsModel ipsModel = IIpsModel.get();
        IIpsProject ipsProject = ipsModel.getIpsProject(aProject);

        MessageList validationResults = ipsProject.validate();

        ipsProject.findAllIpsSrcFiles()
                .parallelStream()
                .map(IIpsSrcFile::getIpsObject)
                .map(o -> o.validate(ipsProject))
                .forEach(validationResults::add);

        logMessages(validationResults, getLog());
    }

    static void logMessages(MessageList messageList, Log log) {
        for (Message message : messageList) {
            StringBuilder sb = new StringBuilder();
            sb.append(message.getText());
            sb.append(" (");
            sb.append(message.getCode());
            sb.append(")");
            message.appendInvalidObjectProperties(sb);
            String messageWithoutSeverity = sb.toString();
            switch (message.getSeverity()) {
                case ERROR:
                    log.error(messageWithoutSeverity);
                    break;
                case WARNING:
                    log.warn(messageWithoutSeverity);
                    break;
                default:
                    log.info(messageWithoutSeverity);
            }
        }
    }
}
