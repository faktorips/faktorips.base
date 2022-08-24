/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.eclipse.ipsproject;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.IpsBuilder;

public class EclipseProjectNature implements IProjectNature {

    private EclipseIpsProject ipsProject;

    @Override
    public IProject getProject() {
        return ipsProject.getEclipseProject();
    }

    @Override
    public void setProject(IProject project) {
        ipsProject = new EclipseIpsProject(project);
    }

    @Override
    public void configure() {
        try {
            IProjectDescription description = getProject().getDescription();
            ICommand command = getIpsBuildCommand();
            if (command == null) {
                // Add a product definition build command to the build spec
                ICommand newBuildCommand = description.newCommand();
                newBuildCommand.setBuilderName(IpsBuilder.BUILDER_ID);
                addCommandAtFirstPosition(description, newBuildCommand);
            }
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    @Override
    public void deconfigure() {
        // Nothing to do
    }

    /**
     * Finds the specific command for product definition builder.
     */
    private ICommand getIpsBuildCommand() {
        try {
            ICommand[] commands = getProject().getDescription().getBuildSpec();
            for (ICommand command : commands) {
                if (command.getBuilderName().equals(IpsBuilder.BUILDER_ID)) {
                    return command;
                }
            }

            return null;
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    /**
     * Adds the command to the build spec
     */
    private void addCommandAtFirstPosition(IProjectDescription description, ICommand newCommand) {
        ICommand[] oldCommands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[oldCommands.length + 1];
        System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
        newCommands[0] = newCommand;
        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        try {
            getProject().setDescription(description, null);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }
}