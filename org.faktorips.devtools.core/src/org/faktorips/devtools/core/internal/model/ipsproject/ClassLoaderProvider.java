/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
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

    public ClassLoaderProvider(IJavaProject project, boolean includeProjectsOutputLocation, boolean copyJars) {
        ArgumentCheck.notNull(project);
        javaProject = project;
        this.includeProjectsOutputLocation = includeProjectsOutputLocation;
        this.copyJars = copyJars;
    }

    /**
     * Returns the classloader for the Java project this is a provider for.
     */
    public ClassLoader getClassLoader() throws CoreException {
        if (classLoader == null) {
            try {
                classLoader = getProjectClassloader(javaProject);
                IWorkspace workspace = javaProject.getProject().getWorkspace();
                if (resourceChangeListener != null) {
                    workspace.removeResourceChangeListener(resourceChangeListener);
                }
                resourceChangeListener = new ChangeListener();
                javaProject.getProject().getWorkspace().addResourceChangeListener(resourceChangeListener,
                        IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD);

            } catch (Exception e) {
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
     * notifies the listeners and forces that a new classloader is constructed upon the next
     * request. make a copy of the listener list, as a listener might decide to deregister (in that
     * case we get a concurrent modification exception from the iterator!)
     */
    private void classpathContentsChanged() {
        List<IClasspathContentsChangeListener> copy = new ArrayList<IClasspathContentsChangeListener>(
                classpathContentsChangeListeners);
        for (IClasspathContentsChangeListener listener : copy) {
            listener.classpathContentsChanges(javaProject);
        }
        classLoader = null;
    }

    /**
     * Returns a classloader containing the project's output location and all it's libraries (jars).
     */
    private URLClassLoader getProjectClassloader(IJavaProject project) throws IOException, CoreException {
        List<URL> urlsList = new ArrayList<URL>();
        accumulateClasspath(project, urlsList);
        URL[] urls = urlsList.toArray(new URL[urlsList.size()]);
        return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    }

    private void accumulateClasspath(IJavaProject currentProject, List<URL> urlsList) throws IOException, CoreException {
        if (currentProject == null || !currentProject.exists()) {
            return;
        }

        IPath projectPath = currentProject.getProject().getLocation();
        IPath root = projectPath.removeLastSegments(currentProject.getProject().getFullPath().segmentCount());

        if (currentProject != javaProject || includeProjectsOutputLocation) {
            IPath outLocation = currentProject.getOutputLocation();
            IPath output = root.append(outLocation);
            urlsList.add(output.toFile().toURI().toURL());
            addClassfileContainer(output);
        }

        IClasspathEntry[] entry = currentProject.getRawClasspath();
        for (IClasspathEntry element : entry) {
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
                    if (tempFileDir == null) {
                        tempFileDir = initTempDir(javaProject);
                    }
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

        String[] requiredProjectNames = currentProject.getRequiredProjectNames();
        if (requiredProjectNames != null && requiredProjectNames.length > 0) {
            for (String requiredProjectName : requiredProjectNames) {
                accumulateClasspath(currentProject.getJavaModel().getJavaProject(requiredProjectName), urlsList);
            }
        }
    }

    /**
     * Creates a temporary directory to store temporary jars in the plugin state location. (The
     * plug-in state area is a file directory within the platform's metadata area where a plug-in is
     * free to create files (see Plugin#getStateLocation()). Each project gets its own temporary
     * directory because each project gets its own classloader provider.) Cleanup the directory if
     * it already exists. NOTE: this is necessary because the virtual machine doesn't delete all
     * temporary jars correctly, the jars from which a class was instantiated by the classloader are
     * not deleted automatically when the virtual machine terminates.
     */
    private File initTempDir(IJavaProject project) {
        File tempFileDir = IpsPlugin.getDefault().getStateLocation().append(project.getProject().getName()).toFile();
        if (tempFileDir.exists()) {
            cleanupTemp(tempFileDir);
        }
        if (!tempFileDir.exists()) {
            tempFileDir.mkdirs();
        }
        return tempFileDir;
    }

    private void cleanupTemp(File root) {
        File[] files = root.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory()) {
                cleanupTemp(files[i]);
            }
            files[i].delete();
        }
    }

    /**
     * Copies the given jar as temporary jar.
     */
    private IPath copyJar(IPath jarPath, File tempFileDir) throws IOException {
        File jarFile = jarPath.toFile();
        if (jarFile == null) {
            return null;
        }
        if (!jarFile.exists()) {
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
        InputStream is = new FileInputStream(jarFile);
        FileOutputStream os = new FileOutputStream(copy);
        byte[] buffer = new byte[16384];
        int bytesRead = 0;
        while (bytesRead > -1) {
            bytesRead = is.read(buffer);
            if (bytesRead > 0) {
                os.write(buffer, 0, bytesRead);
            }
        }
        is.close();
        os.close();
        return new Path(copy.getPath());
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

            // check if one of the previous container have changed and notify that the classloader
            // must re reconstructed next time
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
