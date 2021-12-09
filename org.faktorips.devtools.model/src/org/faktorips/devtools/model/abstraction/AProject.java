/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import static org.faktorips.devtools.model.abstraction.Wrappers.get;
import static org.faktorips.devtools.model.abstraction.Wrappers.run;
import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;

import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.abstraction.AFolder.PlainJavaFolder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A project is a special container wrapping resources and settings.
 */
public interface AProject extends AContainer {

    /**
     * Returns whether this project is a Faktor-IPS project.
     */
    boolean isIpsProject();

    /**
     * Returns the file with the given name inside this project. It may not {@link #exists() exist}.
     *
     * @param name a file name, will be resolved as a path relative to this project.
     * @return the file identified by the name
     */
    AFile getFile(String name);

    /**
     * Returns the folder with the given name inside this project. It may not {@link #exists()
     * exist}.
     *
     * @param name a folder name, will be resolved as a path relative to this project.
     * @return the folder identified by the name
     */
    AFolder getFolder(String name);

    /**
     * Returns all other projects this project references.
     */
    Set<AProject> getReferencedProjects();

    /**
     * Builds this project. The {@link ABuildKind buildKind} parameter determines, whether an
     * incremental or full build is done and whether output folders are cleaned beforehand.
     *
     * @param buildKind the kind of build to perform
     * @param monitor a progress monitor that is notified about the build process. Individual file
     *            processing is reported to the monitor to allow fine-grained progress reporting.
     *            The monitor may be {@code null} when progress does not need to be reported.
     */
    void build(ABuildKind buildKind, IProgressMonitor monitor);

    public static class AEclipseProject extends AEclipseContainer implements AProject {
        private static final String OLD_NATURE_ID = "org.faktorips.devtools.core.ipsnature"; //$NON-NLS-1$

        AEclipseProject(IProject project) {
            super(project);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IProject unwrap() {
            return (IProject)super.unwrap();
        }

        public IProject project() {
            return unwrap();
        }

        @Override
        public AFile getFile(String name) {
            return wrap(project().getFile(name)).as(AFile.class);
        }

        @Override
        public AFolder getFolder(String name) {
            return wrap(project().getFolder(name)).as(AFolder.class);
        }

        @Override
        public Set<AProject> getReferencedProjects() {
            return wrap(project()::getReferencedProjects).asSetOf(AProject.class);
        }

        @Override
        public void delete(IProgressMonitor monitor) {
            run(() -> project().delete(true, true, monitor));
        }

        @Override
        public boolean isIpsProject() {
            return get(() -> project().isOpen()
                    && (project().hasNature(IIpsProject.NATURE_ID) || project().hasNature(OLD_NATURE_ID)));
        }

        @Override
        public void build(ABuildKind kind, IProgressMonitor monitor) {
            run(() -> project().build(ABuildKind.forEclipse(kind), monitor));
        }

    }

    public static class PlainJavaProject extends PlainJavaFolder implements AProject {

        public PlainJavaProject(File directory) {
            super(directory);
        }

        @Override
        public AResourceType getType() {
            return AResourceType.PROJECT;
        }

        @Override
        public boolean isIpsProject() {
            return directory().toPath().resolve(IIpsProject.PROPERTY_FILE_EXTENSION_INCL_DOT).toFile().exists();
        }

        @Override
        public Set<AProject> getReferencedProjects() {
            // TODO über Maven auflösen?
            return Set.of();
        }

        @Override
        public void delete(IProgressMonitor monitor) {
            if (directory().exists()) {
                delete(monitor);
            }
        }

        @Override
        void create() {
            super.create();
            // TODO muss noch etwas angelegt werden, um den Ordner als Projekt zu markieren? Evtl.
            // eine pom.xml wenn wir ein Maven-Projekt anlegen?
        }

        @Override
        public void build(ABuildKind incrementalBuild, IProgressMonitor monitor) {
            // TODO später...
        }

    }
}
