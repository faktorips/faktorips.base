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
import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.model.abstraction.mapping.PathMapping.toEclipsePath;

import java.lang.Runtime.Version;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.abstraction.AProject.AEclipseProject;
import org.faktorips.devtools.model.abstraction.AProject.PlainJavaProject;
import org.faktorips.devtools.model.abstraction.AResource.AEclipseResource;
import org.faktorips.devtools.model.abstraction.mapping.SeverityMapping;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;
import org.faktorips.runtime.Severity;

/**
 * A Java project is {@link AProject a project} containing {@link AJavaElement Java elements}.
 */
public interface AJavaProject extends AAbstraction {

    /**
     * Returns the corresponding project.
     */
    AProject getProject();

    /**
     * A Java project exists if its file-system equivalent exists (the path corresponds to an actual
     * file/folder) and is configured to contain Java elements.
     *
     * @return whether this project exists
     */
    boolean exists();

    /**
     * Returns whether this project has previously been built and retains some information about
     * that build.
     */
    boolean hasBuildState();

    /**
     * Returns the default output location for this project as a path relative to the containing
     * workspace.
     */
    Path getOutputLocation();

    /**
     * Returns the package fragment root for an external library
     *
     * @param externalLibraryPath the path to the external library, either a folder or archive file
     */
    APackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath);

    /**
     * Returns the package fragment root for an external library
     *
     * @param resource the path to the root, either a folder or archive file
     */
    APackageFragmentRoot getPackageFragmentRoot(AResource resource);

    /**
     * Returns the Java version number for this project's source code.
     */
    Runtime.Version getSourceVersion();

    /**
     * Returns all other Java projects referenced by this project.
     */
    Set<AJavaProject> getReferencedJavaProjects();

    /**
     * Returns whether the given resource is a Java folder.
     */
    boolean isJavaFolder(AResource resource);

    /**
     * Validates whether this project's build path is configured correctly, adding any errors to the
     * returned list.
     */
    MessageList validateJavaProjectBuildPath();

    /**
     * Returns the Java project matching the given project.
     */
    public static AJavaProject from(AProject project) {
        if (project instanceof AEclipseProject) {
            return Wrappers
                    .wrap(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(project.getName()))
                    .as(AJavaProject.class);
        } else {
            return Wrappers.wrap(project).as(AJavaProject.class);
        }
    }

    public static class AEclipseJavaProject extends AWrapper<IJavaProject> implements AJavaProject {

        AEclipseJavaProject(IJavaProject javaProject) {
            super(javaProject);
        }

        IJavaProject javaProject() {
            return unwrap();
        }

        @Override
        public boolean exists() {
            return javaProject().exists();
        }

        @Override
        public Path getOutputLocation() {
            return get(javaProject()::getOutputLocation).toFile().toPath();
        }

        @Override
        public APackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath) {
            IPackageFragmentRoot packageFragmentRoot = javaProject().getPackageFragmentRoot(externalLibraryPath);
            return wrap(packageFragmentRoot).as(APackageFragmentRoot.class);
        }

        @Override
        public APackageFragmentRoot getPackageFragmentRoot(AResource resource) {
            IPackageFragmentRoot packageFragmentRoot = javaProject()
                    .getPackageFragmentRoot(((AEclipseResource)resource).unwrap());
            return wrap(packageFragmentRoot).as(APackageFragmentRoot.class);
        }

        @Override
        public AProject getProject() {
            return wrap(javaProject().getProject()).as(AProject.class);
        }

        @Override
        public boolean hasBuildState() {
            return javaProject().hasBuildState();
        }

        @Override
        public Version getSourceVersion() {
            String compilerCompliance = javaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
            return Runtime.Version.parse(compilerCompliance);
        }

        @Override
        public Set<AJavaProject> getReferencedJavaProjects() {
            Set<AJavaProject> result = new LinkedHashSet<>();
            try {
                IClasspathEntry[] entries = javaProject().getRawClasspath();
                for (IClasspathEntry entrie : entries) {
                    if (entrie.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                        IJavaProject refProject = javaProject().getJavaModel()
                                .getJavaProject(entrie.getPath().lastSegment());
                        result.add(Wrappers.wrap(refProject).as(AJavaProject.class));
                    }
                }

            } catch (JavaModelException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
            return result;
        }

        /**
         * Examines this {@link IJavaProject} and its relation to the given resource. Returns
         * {@code true} if the given resource corresponds to a classpath entry of the Java project.
         * Returns {@code true} if the given resource corresponds to a folder that is either the
         * Java project's default output location or the output location of one of the project's
         * classpath entries. {@code False} otherwise.
         */
        @Override
        public boolean isJavaFolder(AResource resource) {
            try {
                IPath outputPath = javaProject().getOutputLocation();
                IClasspathEntry[] entries = javaProject().getResolvedClasspath(true);
                if (toEclipsePath(resource.getWorkspaceRelativePath()).equals(outputPath)) {
                    return true;
                }
                for (IClasspathEntry entry : entries) {
                    if (toEclipsePath(resource.getWorkspaceRelativePath()).equals(entry.getOutputLocation())) {
                        return true;
                    }
                    if (toEclipsePath(resource.getWorkspaceRelativePath()).equals(entry.getPath())) {
                        return true;
                    }
                }
                return false;
            } catch (CoreException e) {
                IpsLog.log(e);
                return false;
            }
        }

        @Override
        public MessageList validateJavaProjectBuildPath() {
            MessageList result = new MessageList();
            if (!exists()) {
                return result;
            }

            try {
                IClasspathEntry[] entries = javaProject().getRawClasspath();
                for (IClasspathEntry entry : entries) {
                    if (validateClasspathEntry(javaProject(), entry, false).containsErrorMsg()) {
                        String text = MessageFormat.format(Messages.IpsProject_javaProjectHasInvalidBuildPath,
                                entry.getPath());
                        Message msg = new Message(IIpsProject.MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS, text,
                                Message.WARNING,
                                this);
                        result.add(msg);
                    }
                }
            } catch (JavaModelException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
            return result;
        }

        private MessageList validateClasspathEntry(IJavaProject project,
                IClasspathEntry entry,
                boolean checkSourceAttachment) {
            IJavaModelStatus status = JavaConventions.validateClasspathEntry(project, entry,
                    checkSourceAttachment);
            if (status.isOK()) {
                return MessageLists.emptyMessageList();
            }
            Severity severity = SeverityMapping.toIps(status.getSeverity());
            String message = status.getMessage();
            String code = Integer.toString(status.getCode());
            return MessageList.of(new Message(code, message, severity));
        }

    }

    public static class PlainJavaJavaProject extends AWrapper<PlainJavaProject> implements AJavaProject {

        public PlainJavaJavaProject(PlainJavaProject project) {
            super(project);
        }

        @Override
        public AProject getProject() {
            return unwrap();
        }

        @Override
        public boolean exists() {
            return getProject().exists();
        }

        @Override
        public boolean hasBuildState() {
            // TODO
            return false;
        }

        @Override
        public Path getOutputLocation() {
            // TODO main/derived...?
            return getProject().getWorkspaceRelativePath().resolve("src/main/java"); //$NON-NLS-1$
        }

        @Override
        public APackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath) {
            // TODO
            return Wrappers.wrap(Path.of(externalLibraryPath).toFile())
                    .as(APackageFragmentRoot.class);
        }

        @Override
        public APackageFragmentRoot getPackageFragmentRoot(AResource resource) {
            // TODO main/derived...?
            return Wrappers
                    .wrap(getProject().getFolder("src").getFolder("main").getFolder("java") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            .getFile(resource.getProjectRelativePath()).unwrap())
                    .as(APackageFragmentRoot.class);
        }

        @Override
        public Version getSourceVersion() {
            return Runtime.version();
        }

        @Override
        public Set<AJavaProject> getReferencedJavaProjects() {
            // TODO brauchen wir das oder sind alle Referenzen JARs?
            return Set.of();
        }

        @Override
        public boolean isJavaFolder(AResource resource) {
            return Objects.equals(getPackageFragmentRoot(resource).unwrap(), resource.unwrap());
        }

        @Override
        public MessageList validateJavaProjectBuildPath() {
            // TODO
            return MessageList.of();
        }

    }

}
