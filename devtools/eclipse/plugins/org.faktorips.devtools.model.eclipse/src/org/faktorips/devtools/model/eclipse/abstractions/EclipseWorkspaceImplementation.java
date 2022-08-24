/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.eclipse.abstractions;

import java.util.function.Consumer;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.eclipse.internal.EclipseIpsModel;
import org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.model.eclipse.ipsproject.EclipseIpsProject;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.osgi.service.component.annotations.Component;

@Component(service = AWorkspaceAbstractionsImplementation.class, property = "type=eclipse")
public class EclipseWorkspaceImplementation implements AWorkspaceAbstractionsImplementation {

    public static final String PLUGIN_ID = "org.faktorips.devtools.model.eclipse"; //$NON-NLS-1$

    @Override
    public IpsModel createIpsModel() {
        return new EclipseIpsModel();
    }

    @Override
    public IpsProject createIpsProject(IIpsModel model, String name) {
        return new EclipseIpsProject(model, name);
    }

    @Override
    public void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        IpsContainer4JdtClasspathContainer.addRequiredEntriesToIpsObjectPath(ipsObjectPath, entryAdder);
    }
}
