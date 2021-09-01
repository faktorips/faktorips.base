/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.core.extensions.ExternalTableFormatExtensions;
import org.faktorips.devtools.core.extensions.NewProductDefinitionOperationParticipantExtensions;
import org.faktorips.devtools.core.extensions.TeamOperationsFactoryExtensions;
import org.faktorips.devtools.core.extensions.TocTreeFromDependencyManagerExtension;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.model.INewProductDefinitionOperationParticipant;
import org.faktorips.devtools.core.model.testcase.ITocTreeFromDependencyManagerLoader;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.tableconversion.ITableFormat;

public class IpsCoreExtensions {

    private final Supplier<Set<ITeamOperationsFactory>> teamOperationsFactories;
    private final Supplier<List<ITableFormat>> externalTableFormats;
    private final Supplier<List<INewProductDefinitionOperationParticipant>> newProductDefinitionOperationParticipants;
    private final Supplier<List<ITocTreeFromDependencyManagerLoader>> tocTreeFromDependencyManagerLoader;

    public IpsCoreExtensions(IExtensionRegistry extensionRegistry) {
        ExtensionPoints extensionPoints = new ExtensionPoints(extensionRegistry, IpsPlugin.PLUGIN_ID);
        teamOperationsFactories = new TeamOperationsFactoryExtensions(extensionPoints);
        externalTableFormats = new ExternalTableFormatExtensions(extensionPoints);
        newProductDefinitionOperationParticipants = new NewProductDefinitionOperationParticipantExtensions(
                extensionPoints);
        tocTreeFromDependencyManagerLoader = new TocTreeFromDependencyManagerExtension(extensionPoints);
    }

    /**
     * Returns the {@link ITeamOperationsFactory ITeamOperationsFactories} used to create
     * {@link ITeamOperations} for the {@link ProductReleaseProcessor}.
     */
    public Set<ITeamOperationsFactory> getTeamOperationsFactories() {
        return teamOperationsFactories.get();
    }

    /**
     * Returns an array of all available external table formats.
     */
    public ITableFormat[] getExternalTableFormats() {
        return externalTableFormats.get().toArray(new ITableFormat[0]);
    }

    /**
     * Returns a list of {@link INewProductDefinitionOperationParticipant} instances.
     */
    public List<INewProductDefinitionOperationParticipant> getNewProductDefinitionOperationParticipants() {
        return newProductDefinitionOperationParticipants.get();
    }

    /**
     * Return a list of {@link ITocTreeFromDependencyManagerLoader} instances.
     */
    public List<ITocTreeFromDependencyManagerLoader> getTocTreeFromDependencyManagerLoader() {
        return tocTreeFromDependencyManagerLoader.get();
    }
}
