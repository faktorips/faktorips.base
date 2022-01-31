/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a classloader for the classpath defined in a given Java project.
 */
public class ClassLoaderProvider implements IClassLoaderProvider {

    private IJavaProject javaProject;
    private URLClassLoader classLoader;

    /**
     * a list of IPaths that contain the class files, either a path to a file if it's a Jar-File or
     * to a directory if it's a directory containing class files.
     */
    private final List<IPath> classfileContainers = new ArrayList<>();

    /** listeners that are informed if the contents of the classpath changes */
    private final List<IClasspathContentsChangeListener> classpathContentsChangeListeners = new CopyOnWriteArrayList<>();

    /**
     * resource change listener that is used to test for changes of the classpath elements (jars and
     * class directories)
     */
    private IResourceChangeListener resourceChangeListener;

    /**
     * Class loader used as parent of the class loader created by this provider.
     */
    private ClassLoader parentClassLoader = null;

    public ClassLoaderProvider(IJavaProject project) {
        this(project, ClassLoader.getSystemClassLoader());
    }

    public ClassLoaderProvider(IJavaProject project, ClassLoader parentClassLoader) {
        ArgumentCheck.notNull(project);
        ArgumentCheck.notNull(parentClassLoader);
        javaProject = project;
        this.parentClassLoader = parentClassLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            try {
                classLoader = getProjectClassloader(javaProject);
                IWorkspace workspace = javaProject.getProject().getWorkspace();
                if (resourceChangeListener != null) {
                    workspace.removeResourceChangeListener(resourceChangeListener);
                }
                resourceChangeListener = new ChangeListener();
                javaProject.getProject().getWorkspace().addResourceChangeListener(resourceChangeListener,
                        IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD
                                | IResourceChangeEvent.PRE_DELETE);

            } catch (IOException e) {
                throw new CoreRuntimeException(new IpsStatus(e));
            }
        }
        return classLoader;
    }

    @Override
    public void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
        classpathContentsChangeListeners.add(listener);
    }

    @Override
    public void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
        classpathContentsChangeListeners.remove(listener);
    }

    /**
     * Notifies the listeners and forces that a new classloader is constructed upon the next
     * request.
     */
    private void classpathContentsChanged() {
        classLoader = null;
        for (IClasspathContentsChangeListener listener : classpathContentsChangeListeners) {
            listener.classpathContentsChanges(Wrappers.wrap(javaProject).as(AJavaProject.class));
        }
    }

    /**
     * Returns a classloader containing the project's output location and all it's libraries (jars).
     */
    private URLClassLoader getProjectClassloader(IJavaProject project) throws IOException {
        String[] classPathEntries;
        try {
            classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(project);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        URL[] urls = new URL[classPathEntries.length];
        for (int i = 0; i < classPathEntries.length; i++) {
            IPath path = new Path(classPathEntries[i]);
            addClassfileContainer(path);
            urls[i] = path.toFile().toURI().toURL();
        }
        return new URLClassLoader(urls, parentClassLoader);
    }

    /**
     * @param containerLocation is the full path in the file system.
     */
    private void addClassfileContainer(IPath containerLocation) {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        IPath containerPath = containerLocation.removeFirstSegments(workspaceLocation.segmentCount());
        classfileContainers.add(containerPath);
    }

    private class ChangeListener implements IResourceChangeListener {

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (event.getType() == IResourceChangeEvent.PRE_BUILD
                    && event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
                return;
            }
            if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                try {
                    if (classLoader != null) {
                        classLoader.close();
                    }
                } catch (IOException e) {
                    IpsLog.log(e);
                }
                classfileContainers.clear();
                classpathContentsChanged();
            }
            for (IPath container : classfileContainers) {
                IResourceDelta delta = event.getDelta().findMember(container);
                if (delta != null) {
                    classpathContentsChanged();
                    break;
                }
            }
        }
    }

}
