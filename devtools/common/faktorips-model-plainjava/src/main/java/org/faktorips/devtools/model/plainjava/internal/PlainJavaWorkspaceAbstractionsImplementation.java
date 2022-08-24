/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava.internal;

import java.util.function.Consumer;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

public enum PlainJavaWorkspaceAbstractionsImplementation implements AWorkspaceAbstractionsImplementation {
    INSTANCE;

    static final String ID = "Plain Java Faktor-IPS"; //$NON-NLS-1$

    static PlainJavaWorkspaceAbstractionsImplementation get() {
        return INSTANCE;
    }

    @Override
    public IpsModel createIpsModel() {
        return new PlainJavaIpsModel();
    }

    @Override
    public IpsProject createIpsProject(IIpsModel model, String name) {
        return new IpsProject(model, name);
    }

    @Override
    public void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        // nothing to do (yet? maybe with Maven....)
    }
}
