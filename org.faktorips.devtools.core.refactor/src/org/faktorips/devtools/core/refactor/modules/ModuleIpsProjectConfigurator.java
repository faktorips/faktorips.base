/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.modules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.IpsProjectConfigurators;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.devtools.model.util.StandardJavaProjectConfigurator;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Configures a new Faktor-IPS project if it is a Java 9+ module.
 */
public class ModuleIpsProjectConfigurator implements IIpsProjectConfigurator {

    @Override
    public boolean canConfigure(IJavaProject javaProject) {
        try {
            return javaProject.getModuleDescription() != null;
        } catch (JavaModelException e) {
            return false;
        }
    }

    @Override
    public boolean isGroovySupported(IJavaProject javaProject) {
        // groovy will be added (or not) depending on other configurators - unless this is the only
        // one, the we delegate to the StandardJavaProjectConfigurator
        if (IpsProjectConfigurators.applicableTo(javaProject).allMatch((IIpsProjectConfigurator c) -> c == this)) {
            return new StandardJavaProjectConfigurator().isGroovySupported(javaProject);
        } else {
            return false;
        }
    }

    @Override
    public MessageList validate(IJavaProject javaProject, IpsProjectCreationProperties creationProperties) {
        if (creationProperties.isPersistentProject()) {
            String persistenceAPI = creationProperties.getPersistenceSupport();
            switch (persistenceAPI) {
                case PersistenceSupportNames.ID_ECLIPSE_LINK_1_1:
                case PersistenceSupportNames.ID_ECLIPSE_LINK_2_5:
                    return MessageList.of(Message.newError("INCOMPATIBLE_WITH_MODULES",
                            "The chosen persistence API(" + persistenceAPI + ") does not support Java modules",
                            creationProperties, IpsProjectCreationProperties.PROPERTY_PERSISTENCE_SUPPORT));
            }
        }
        return MessageList.of();
    }

    @Override
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
            throws CoreRuntimeException {
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        if (IpsProjectConfigurators.applicableTo(javaProject).allMatch((IIpsProjectConfigurator c) -> c == this)) {
            new StandardJavaProjectConfigurator().configureIpsProject(ipsProject, creationProperties);
        }
        try {
            addRequiredModules(javaProject, creationProperties);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void addRequiredModules(IJavaProject javaProject, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        List<String> requiredModules = new ArrayList<>();
        requiredModules.add("org.faktorips.runtime");
        if (creationProperties.isGroovySupport()) {
            requiredModules.add("org.faktorips.runtime.groovy");
        }
        if (creationProperties.isPersistentProject()) {
            requiredModules.add("javax.persistence");
        }
        Modules.addRequired(javaProject, true, requiredModules);
    }

}
