/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstractions;

import java.util.function.Consumer;

import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

public final class WorkspaceAbstractions {

    private static final AWorkspaceAbstractionsImplementation IMPLEMENTATION = get();

    private WorkspaceAbstractions() {
        // util
    }

    private static AWorkspaceAbstractionsImplementation get() {
        // unless an implementation resides in another module, we don't need any fancy service
        // loading / plugin mechanism to find them
        return Abstractions.isEclipseRunning() ? EclipseWorkspaceImplementation.get()
                : PlainJavaWorkspaceAbstractionsImplementation.get();
    }

    public static IpsModel createIpsModel() {
        return IMPLEMENTATION.createIpsModel();
    }

    public static void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        IMPLEMENTATION.addRequiredEntriesToIpsObjectPath(ipsObjectPath, entryAdder);
    }

    public interface AWorkspaceAbstractionsImplementation {
        IpsModel createIpsModel();

        /**
         * Adds additional entries to a project's path, for example for workspace references.
         */
        void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
                Consumer<IIpsObjectPathEntry> entryAdder);
    }

}
