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

import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Consumer;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.plugin.IpsModelActivator;

public final class WorkspaceAbstractions {

    private static final AWorkspaceAbstractionsImplementation IMPLEMENTATION = get();

    private WorkspaceAbstractions() {
        // util
    }

    private static AWorkspaceAbstractionsImplementation get() {
        IpsModelActivator ipsModelActivator = IpsModelActivator.get();
        if (ipsModelActivator != null) {
            return ipsModelActivator.getImplementation();
        }
        Optional<AWorkspaceAbstractionsImplementationProvider> implementationProvider = ServiceLoader
                .load(AWorkspaceAbstractionsImplementationProvider.class).stream()
                .map(Provider::get)
                .sorted(Comparator.comparing(AWorkspaceAbstractionsImplementationProvider::getPriority).reversed())
                .findFirst();
        return implementationProvider
                .orElseThrow(() -> new IpsException("No implementation provider found!")) //$NON-NLS-1$
                .getImplementation();
    }

    public static IpsModel createIpsModel() {
        return IMPLEMENTATION.createIpsModel();
    }

    public static IpsProject createIpsProject(IIpsModel model, String name) {
        return IMPLEMENTATION.createIpsProject(model, name);
    }

    public static void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        IMPLEMENTATION.addRequiredEntriesToIpsObjectPath(ipsObjectPath, entryAdder);
    }

    public interface AWorkspaceAbstractionsImplementation {
        IpsModel createIpsModel();

        IpsProject createIpsProject(IIpsModel model, String name);

        /**
         * Adds additional entries to a project's path, for example for workspace references.
         */
        void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
                Consumer<IIpsObjectPathEntry> entryAdder);
    }

}
