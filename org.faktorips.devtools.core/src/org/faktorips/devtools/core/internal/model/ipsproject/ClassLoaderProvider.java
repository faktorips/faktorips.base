/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a classloader for the classpath defined in a given Java project.
 * 
 * @author Jan Ortmann
 */
public class ClassLoaderProvider {

    private boolean includeProjectsOutputLocation;
    private IJavaProject javaProject;
    private URLClassLoader classLoader;

    /** <code>true</code> if the jars should be copied as temporary jars */
    private boolean copyJars = false;

    /** The directory where the jar files are copies to (if copyJars = true). */
    private File tempFileDir = null;

    /**
     * a list of IPaths that contain the class files, either a path to a file if it's a Jar-File or
     * to a directory if it's a directory containing class files.
     */
    private List<IPath> classfileContainers = new ArrayList<IPath>();

    /** listeners that are informed if the contents of the classpath changes */
    private List<IClasspathContentsChangeListener> classpathContentsChangeListeners = new ArrayList<IClasspathContentsChangeListener>();

    /**
     * resource change listener that is used to test for changes of the classpath elements (jars and
     * class directories)
     */
    private IResourceChangeListener resourceChangeListener;

    /**
     * Class loader used as parent of the class loader created by this provider.
     */
    private ClassLoader parentClassLoader = null;

    public ClassLoaderProvider(IJavaProject project, boolean includeProjectsOutputLocation, boolean copyJars) {
        this(project, ClassLoader.getSystemClassLoader(), includeProjectsOutputLocation, copyJars);
    }

    public ClassLoaderProvider(IJavaProject project, ClassLoader parentClassLoader,
            boolean includeProjectsOutputLocation, boolean copyJars) {
        ArgumentCheck.notNull(project);
        ArgumentCheck.notNull(parentClassLoader);
        javaProject = project;
        this.parentClassLoader = parentClassLoader;
        this.includeProjectsOutputLocation = includeProjectsOutputLocation;
        this.copyJars = copyJars;
    }

    /**
     * Returns the classloader for the Java project this is a provider for.
     */
    public ClassLoader getClassLoader() throws CoreException {
        if (classLoader == null) {
            try {
                setUpTempFileDir();
                classLoader = getProjectClassloader(javaProject);
                IWorkspace workspace = javaProject.getProject().getWorkspace();
                if (resourceChangeListener != null) {
                    workspace.removeResourceChangeListener(resourceChangeListener);
                }
                resourceChangeListener = new ChangeListener();
                javaProject
                        .getProject()
                        .getWorkspace()
                        .addResourceChangeListener(resourceChangeListener,
                                IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD);

            } catch (IOException e) {
                throw new CoreException(new IpsStatus(e));
            }
        }
        return classLoader;
    }

    /**
     * Adds the listener as one to be informed about changes to the classpath contents. In this case
     * the listener should get a new classloader if he wants to use classes that are up-to-date .
     */
    public void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
        classpathContentsChangeListeners.add(listener);
    }

    /**
     * Removes the listener from the list.
     */
    public void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
        classpathContentsChangeListeners.remove(listener);
    }

    /**
     * Notifies the listeners and forces that a new classloader is constructed upon the next
     * request.
     */
    private void classpathContentsChanged() {
        classLoader = null;
        List<IClasspathContentsChangeListener> copy = new CopyOnWriteArrayList<IClasspathContentsChangeListener>(
                classpathContentsChangeListeners);
        for (IClasspathContentsChangeListener listener : copy) {
            listener.classpathContentsChanges(javaProject);
        }
    }

    /**
     * Returns a classloader containing the project's output location and all it's libraries (jars).
     */
    private URLClassLoader getProjectClassloader(IJavaProject project) throws IOException, CoreException {
        List<URL> urlsList = new ArrayList<URL>();
        accumulateClasspath(project, urlsList);
        URL[] urls = urlsList.toArray(new URL[urlsList.size()]);
        return new URLClassLoader(urls, parentClassLoader);
    }

    private void accumulateClasspath(IJavaProject currentProject, List<URL> urlsList) throws IOException, CoreException {
        if (isExistingProject(currentProject)) {
            return;
        }

        IPath projectPath = currentProject.getProject().getLocation();
        IPath root = projectPath.removeLastSegments(currentProject.getProject().getFullPath().segmentCount());

        if (isIncludeOutputLocation(currentProject)) {
            IPath outLocation = currentProject.getOutputLocation();
            IPath output = root.append(outLocation);
            urlsList.add(output.toFile().toURI().toURL());
            addClassfileContainer(output);
        }

        Set<IPath> jreEntries = getJrePathEntries(currentProject);
        IClasspathEntry[] entry = currentProject.getResolvedClasspath(true);
        accumulateClasspathEntry(urlsList, root, jreEntries, entry);

        String[] requiredProjectNames = currentProject.getRequiredProjectNames();
        if (requiredProjectNames != null && requiredProjectNames.length > 0) {
            for (String requiredProjectName : requiredProjectNames) {
                accumulateClasspath(currentProject.getJavaModel().getJavaProject(requiredProjectName), urlsList);
            }
        }
    }

    private boolean isExistingProject(IJavaProject currentProject) {
        return currentProject == null || !currentProject.exists();
    }

    private boolean isIncludeOutputLocation(IJavaProject currentProject) {
        return currentProject != javaProject || includeProjectsOutputLocation;
    }

    private void setUpTempFileDir() {
        if (copyJars) {
            if (tempFileDir == null) {
                boolean tmpDirCreated = initTempDir(javaProject);
                if (!tmpDirCreated) {
                    copyJars = false;
                }
            } else {
                cleanupTemp(tempFileDir);
            }
        }
    }

    private void accumulateClasspathEntry(List<URL> urlsList, IPath root, Set<IPath> jreEntries, IClasspathEntry[] entry)
            throws IOException {
        for (IClasspathEntry element : entry) {
            if (jreEntries.contains(element.getPath())) {
                // assume that JRE libs are already available via the parent class loader
                continue;
            }
            if (element.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                IPath jarPath;
                /*
                 * evaluate the correct path of the classpath entry; if the entry path contains no
                 * device then the root path will be added in front of the path, otherwise the path
                 * is already an absolute path (e.g. external libraries) Remark: IPath#isAbsolute
                 * didn't work in this case
                 */
                if (StringUtils.isEmpty(element.getPath().getDevice())) {
                    jarPath = root.append(element.getPath());
                } else {
                    jarPath = element.getPath();
                }

                IPath currentPath;
                if (copyJars) {
                    currentPath = copyJar(jarPath, tempFileDir);
                } else {
                    currentPath = jarPath;
                }
                if (currentPath != null) {
                    urlsList.add(currentPath.toFile().toURI().toURL());
                    addClassfileContainer(jarPath);
                }
            }
        }
    }

    private Set<IPath> getJrePathEntries(IJavaProject javaProject) throws JavaModelException {
        Set<IPath> jreEntries = new HashSet<IPath>();
        IClasspathContainer jreContainer = JavaCore.getClasspathContainer(new Path(
                "org.eclipse.jdt.launching.JRE_CONTAINER"), javaProject); //$NON-NLS-1$
        if (jreContainer == null) {
            IpsPlugin.log(new IpsStatus("No JRE Classpath Container found for project " + javaProject)); //$NON-NLS-1$
        } else {
            IClasspathEntry[] entries = jreContainer.getClasspathEntries();
            for (int i = 0; i < entries.length; i++) {
                jreEntries.add(entries[i].getPath());
            }
        }
        return jreEntries;
    }

    /**
     * Creates a temporary directory to store temporary jars in the plug-in state location. (The
     * plug-in state area is a file directory within the platform's .metadata area where a plug-in
     * is free to create files (see {@link Plugin#getStateLocation()}). Each project gets its own
     * temporary directory because each project gets its own classloader provider.) Cleanup the
     * directory if it already exists. NOTE: this is necessary because the virtual machine doesn't
     * delete all temporary jars correctly, the jars from which a class was instantiated by the
     * classloader are not deleted automatically when the virtual machine terminates.
     * 
     * @return <code>true</code> if the directory is now available, <code>false</code> if it cannot
     *         be created
     */
    private boolean initTempDir(IJavaProject project) {
        tempFileDir = IpsPlugin.getDefault().getStateLocation().append(project.getProject().getName()).toFile();
        if (tempFileDir.exists()) {
            cleanupTemp(tempFileDir);
        }
        if (!tempFileDir.exists()) {
            return tempFileDir.mkdirs();
        }
        return true;
    }

    private void cleanupTemp(File root) {
        File[] files = root.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory()) {
                cleanupTemp(files[i]);
            }
            if (!files[i].delete()) {
                files[i].deleteOnExit();
            }
        }
        classfileContainers.clear();
    }

    /**
     * Copies the given jar as temporary jar.
     */
    private IPath copyJar(IPath jarPath, File tempFileDir) throws IOException {
        File jarFile = jarPath.toFile();
        if (jarFile == null) {
            return null;
        }
        if (!jarFile.exists() || !jarFile.isFile()) {
            return null;
        }
        int index = jarFile.getName().lastIndexOf('.');
        String name = jarFile.getName();
        File copy;
        if (index == -1) {
            copy = File.createTempFile(name + "tmp", "jar", tempFileDir); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (index < 3) {
            // File.createTempFile required that the prefix is at least three characters long!
            copy = File.createTempFile(name.substring(0, index) + "tmp", name.substring(index), tempFileDir); //$NON-NLS-1$
        } else {
            copy = File.createTempFile(name.substring(0, index), name.substring(index), tempFileDir);
        }
        copy.deleteOnExit();
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(jarFile);
            os = new FileOutputStream(copy);
            byte[] buffer = new byte[16384];
            int bytesRead = 0;
            while (bytesRead > -1) {
                bytesRead = is.read(buffer);
                if (bytesRead > 0) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } finally {
            closeStream(is, os);
        }
        return new Path(copy.getPath());
    }

    private void closeStream(Closeable... closeables) {
        Throwable pendingThrowable = closeAndReturnCaughtException(closeables);
        throwIfNecessary(pendingThrowable);
    }

    private Throwable closeAndReturnCaughtException(Closeable... closeables) {
        Throwable pending = null;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException throwable) {
                    if (pending == null) {
                        pending = throwable;
                    }
                }
            }
        }
        return pending;
    }

    private void throwIfNecessary(Throwable pendingThrowable) {
        if (pendingThrowable != null) {
            if (pendingThrowable instanceof RuntimeException) {
                throw (RuntimeException)pendingThrowable;
            } else {
                throw new RuntimeException(pendingThrowable);
            }
        }
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
