/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.faktorips.devtools.abstraction.Wrappers.get;
import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.eclipse.mapping.PathMapping.toEclipsePath;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.eclipse.mapping.SeverityMapping;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;
import org.faktorips.runtime.Severity;

public class EclipseJavaProject extends EclipseJavaElement implements AJavaProject {

    /**
     * Validation message code to indicate that the corresponding Java Project has build path
     * errors.
     */
    public static final String MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS = "IPSPROJECT-JavaProjectHasBuildPathErrors"; //$NON-NLS-1$

    EclipseJavaProject(IJavaProject javaProject) {
        super(javaProject);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IJavaProject unwrap() {
        return (IJavaProject)super.unwrap();
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
    public APackageFragmentRoot toPackageFragmentRoot(String externalLibraryPath) {
        IPackageFragmentRoot packageFragmentRoot = javaProject().getPackageFragmentRoot(externalLibraryPath);
        return wrap(packageFragmentRoot).as(APackageFragmentRoot.class);
    }

    @Override
    public APackageFragmentRoot toPackageFragmentRoot(AResource resource) {
        IPackageFragmentRoot packageFragmentRoot = javaProject()
                .getPackageFragmentRoot(((EclipseResource)resource).unwrap());
        return wrap(packageFragmentRoot).as(APackageFragmentRoot.class);
    }

    @Override
    public AProject getProject() {
        return getResource().getProject();
    }

    @Override
    public boolean hasBuildState() {
        return javaProject().hasBuildState();
    }

    @Override
    public Runtime.Version getSourceVersion() {
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
            throw new IpsException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Examines this {@link IJavaProject} and its relation to the given resource. Returns
     * {@code true} if the given resource corresponds to a classpath entry of the Java project.
     * Returns {@code true} if the given resource corresponds to a folder that is either the Java
     * project's default output location or the output location of one of the project's classpath
     * entries. {@code False} otherwise.
     */
    @Override
    public boolean isJavaFolder(AResource resource) {
        try {
            IPath outputPath = javaProject().getOutputLocation();
            IPath relativePath = toEclipsePath(resource.getWorkspaceRelativePath());
            if (Objects.equals(outputPath, relativePath)) {
                return true;
            }
            IClasspathEntry[] entries = javaProject().getResolvedClasspath(true);
            for (IClasspathEntry entry : entries) {
                if (Objects.equals(entry.getOutputLocation(), relativePath)
                        || Objects.equals(entry.getPath(), relativePath)) {
                    return true;
                }
            }
            return false;
        } catch (CoreException e) {
            Abstractions.getLog().log(e);
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
                    Message msg = new Message(MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS, text,
                            Message.WARNING,
                            this);
                    result.add(msg);
                }
            }
        } catch (JavaModelException e) {
            throw new IpsException(e.getMessage(), e);
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

    @Override
    public Map<String, String> getOptions() {
        return javaProject().getOptions(true);
    }

    @Override
    public Set<APackageFragmentRoot> getAllPackageFragmentRoots() {
        try {
            return Arrays.stream(javaProject().getAllPackageFragmentRoots())
                    .map(p -> Wrappers.wrap(p).as(APackageFragmentRoot.class))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (JavaModelException e) {
            throw new IpsException(e.getMessage(), e);
        }
    }
}
