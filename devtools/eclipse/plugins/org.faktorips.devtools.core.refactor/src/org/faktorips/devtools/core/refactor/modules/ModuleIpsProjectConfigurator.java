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

import static org.faktorips.devtools.model.builder.JaxbSupportVariant.ClassicJAXB;
import static org.faktorips.devtools.model.builder.JaxbSupportVariant.JakartaXmlBinding3;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.eclipse.util.StandardJavaProjectConfigurator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.IpsProjectConfigurators;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Configures a new Faktor-IPS project if it is a Java 9+ module.
 */
public class ModuleIpsProjectConfigurator implements IIpsProjectConfigurator {

    @Override
    public boolean canConfigure(AJavaProject javaProject) {
        try {
            IJavaProject eclipseJavaProject = javaProject.unwrap();
            return eclipseJavaProject.getModuleDescription() != null;
        } catch (JavaModelException | ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean isGroovySupported(AJavaProject javaProject) {
        // groovy will be added (or not) depending on other configurators - unless this is the only
        // one, the we delegate to the StandardJavaProjectConfigurator
        if (IpsProjectConfigurators.applicableTo(javaProject).allMatch((IIpsProjectConfigurator c) -> c == this)) {
            return new StandardJavaProjectConfigurator().isGroovySupported(javaProject);
        } else {
            return false;
        }
    }

    @Override
    public MessageList validate(AJavaProject javaProject, IpsProjectCreationProperties creationProperties) {
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
    public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
        AJavaProject javaProject = ipsProject.getJavaProject();
        if (IpsProjectConfigurators.applicableTo(javaProject).allMatch((IIpsProjectConfigurator c) -> c == this)) {
            new StandardJavaProjectConfigurator().configureIpsProject(ipsProject, creationProperties);
        }
        try {
            addRequiredModules(javaProject, creationProperties);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    private void addRequiredModules(AJavaProject javaProject, IpsProjectCreationProperties creationProperties)
            throws CoreException {
        List<String> requiredModules = new ArrayList<>();
        requiredModules.add("org.faktorips.runtime");
        if (creationProperties.isGroovySupport()) {
            requiredModules.add("org.faktorips.runtime.groovy");
        }
        if (creationProperties.isPersistentProject()) {
            requiredModules.add("javax.persistence");
        }
        if (ClassicJAXB.equals(creationProperties.getJaxbSupport())) {
            requiredModules.add(ClassicJAXB.getIpsPackage());
        }
        if (JakartaXmlBinding3.equals(creationProperties.getJaxbSupport())) {
            requiredModules.add(JakartaXmlBinding3.getIpsPackage());
        }
        Modules.addRequired(javaProject.unwrap(), true, requiredModules);
    }

}
