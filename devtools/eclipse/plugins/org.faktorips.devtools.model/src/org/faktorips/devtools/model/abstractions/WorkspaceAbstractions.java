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

import java.io.File;
import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Consumer;

import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsModelActivator;

public final class WorkspaceAbstractions {

    private static final AWorkspaceAbstractionsImplementation IMPLEMENTATION = get();

    private WorkspaceAbstractions() {
        // util
    }

    private static AWorkspaceAbstractionsImplementation get() {
        AWorkspaceAbstractionsImplementationProvider osgiProvider = IpsModelActivator.get();
        if (osgiProvider != null) {
            return osgiProvider.getWorkspaceAbstractionsImplementation();
        }
        Optional<AWorkspaceAbstractionsImplementationProvider> implementationProvider = ServiceLoader
                .load(AWorkspaceAbstractionsImplementationProvider.class).stream()
                .map(Provider::get)
                .sorted(Comparator.comparing(AWorkspaceAbstractionsImplementationProvider::getPriority).reversed())
                .findFirst();
        return implementationProvider
                .orElseThrow(() -> new IpsException("No workspace abstractions implementation provider found!")) //$NON-NLS-1$
                .getWorkspaceAbstractionsImplementation();
    }

    public static IpsModel createIpsModel() {
        return IMPLEMENTATION.createIpsModel();
    }

    /**
     * Creates a new {@link IIpsProject} of the given name. <em>This method shall only be called by
     * {@link IIpsModel#getIpsModel()}.</em>
     * 
     * @param ipsModel the {@link IIpsModel} to register the project with
     * @param name the name of the project the Faktor-IPS project shall wrap
     */
    public static IpsProject createIpsProject(IIpsModel ipsModel, String name) {
        return IMPLEMENTATION.createIpsProject(ipsModel, name);
    }

    /**
     * Creates a new {@link IIpsProject} from the given {@link AProject} or returns the existing
     * {@link IIpsProject}.
     * 
     * @param ipsModel the {@link IIpsModel} to register the project with
     * @param project the Java project the Faktor-IPS project shall wrap
     */
    public static IIpsProject createIpsProject(IIpsModel ipsModel, AProject project) {
        return IMPLEMENTATION.createIpsProject(ipsModel, project);
    }

    public static void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        IMPLEMENTATION.addRequiredEntriesToIpsObjectPath(ipsObjectPath, entryAdder);
    }

    /**
     * Returns the actual {@link File} corresponding to the given {@link IIpsArchive}.
     */
    public static File getFileFromArchivePath(IIpsArchive ipsArchive) {
        return IMPLEMENTATION.getFileFromArchivePath(ipsArchive);
    }

    public static IIpsModelExtensions getIpsModelExtensions() {
        return IMPLEMENTATION.getIpsModelExtensions();
    }

    public interface AWorkspaceAbstractionsImplementation {
        IpsModel createIpsModel();

        /**
         * Creates a new {@link IIpsProject} of the given name. <em>This method shall only be called
         * by {@link IIpsModel#getIpsModel()}.</em>
         * 
         * @param ipsModel the {@link IIpsModel} to register the project with
         * @param name the name of the project the Faktor-IPS project shall wrap
         */
        default IpsProject createIpsProject(IIpsModel ipsModel, String name) {
            return new IpsProject(ipsModel, name);
        }

        /**
         * Creates a new {@link IIpsProject} from the given {@link AProject} or returns the existing
         * {@link IIpsProject}.
         * 
         * @param ipsModel the {@link IIpsModel} to register the project with
         * @param project the Java project the Faktor-IPS project shall wrap
         * 
         * @implSpec may use {@link #initializeProject(IIpsProject)} to initialize the project's
         *               properties
         */
        default IIpsProject createIpsProject(IIpsModel ipsModel, AProject project) {
            if (project.isIpsProject()) {
                return ipsModel.getIpsProject(project);
            }

            IIpsProject ipsProject = ipsModel.getIpsProject(project);

            initializeProject(ipsProject);

            return ipsProject;
        }

        /**
         * Initializes the given {@link IIpsProject}'s {@link IIpsProjectProperties} by loading and
         * setting them.
         * 
         * @param ipsProject the {@link IIpsProject} being created (by
         *            {@link #createIpsProject(IIpsModel, AProject)})
         * @implNote calls {@link #initializeProperties(IIpsProjectProperties)} before setting the
         *               {@link IIpsProjectProperties}; <em>Overriding implementations should call
         *               this super-implementation.</em>
         * 
         */
        default void initializeProject(IIpsProject ipsProject) {
            IIpsProjectProperties props = ipsProject.getProperties();
            initializeProperties(props);
            ipsProject.setProperties(props);
        }

        /**
         * Sets initial values in the {@link IIpsProjectProperties}
         * 
         * @param props the properties of the {@link IIpsProject} being created (by
         *            {@link #createIpsProject(IIpsModel, AProject)})
         */
        default void initializeProperties(IIpsProjectProperties props) {
            // empty default implementation, there to be overwritten
        }

        /**
         * Adds additional entries to a project's path, for example for workspace references.
         */
        void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
                Consumer<IIpsObjectPathEntry> entryAdder);

        /**
         * Returns the actual {@link File} corresponding to the given {@link IIpsArchive}.
         */
        default File getFileFromArchivePath(IIpsArchive ipsArchive) {
            return ipsArchive.getLocation().toFile();
        }

        IIpsModelExtensions getIpsModelExtensions();
    }

}
