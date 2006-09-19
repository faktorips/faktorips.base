/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a classloader for the classpath defined in a given Java project.
 * 
 * @author Jan Ortmann
 */
public class ClassLoaderProvider {

	private IIpsProject ipsProject;
	private URLClassLoader classLoader;
	
	// a list of IResources that contain the class files, either an IFile if it's a Jar-File or an
	// IFolder if it's a directory containing class files.
	private List classfileContainers = new ArrayList();

	// listeners that are informed if the contents of the classpath changes
	private List classpathContentsChangeListeners = new ArrayList();
	
	// resource change listener that is used to test for changes of the classpath elements (jars and class directories)
	private IResourceChangeListener changeListener; 
	
	public ClassLoaderProvider(IIpsProject project) {
		ArgumentCheck.notNull(project);
		this.ipsProject = project;
	}
	
	/**
	 * Returns the classloader for the Java project this is a provider for.
	 */
	public ClassLoader getClassLoader() throws CoreException {
		if (classLoader==null) {
			try {
				classLoader = getProjectClassloader(ipsProject.getJavaProject());
				changeListener = new ChangeListener();
				ipsProject.getProject().getWorkspace().addResourceChangeListener(
						changeListener,
						IResourceChangeEvent.POST_CHANGE
								| IResourceChangeEvent.PRE_BUILD);
				
			} catch (Exception e) {
				throw new CoreException(new IpsStatus(e));
			}
		}
		return classLoader;
	}
	
	/**
	 * Returns the ips project for that this instance provides the classloader.
	 */
	public IIpsProject getIPsProject() {
		return ipsProject;
	}
	
	/**
	 * Adds the listener as one to be informed about changes to the classpath contents. In this
	 * case the listener should get a new classloader if he wants to use classes that are up-to-date . 
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

	/*
	 * notifies the listeners and forces that a new classloader is constructed upon the next request.
	 * make a copy of the listener list, as a listener might decide to deregister
	 * (in that case we get a concurrent modification exception from the iterator!)
	 */
	private void classpathContentsChanged() {
		List copy = new ArrayList(classpathContentsChangeListeners);
		for (Iterator it=copy.iterator(); it.hasNext(); ) {
			IClasspathContentsChangeListener listener = (IClasspathContentsChangeListener)it.next();
			listener.classpathContentsChanges(ipsProject);
		}
		classLoader = null;
	}

	/*
	 * Returns a classloader containing the project's output location and all
	 * it's libraries (jars).
	 */
	private URLClassLoader getProjectClassloader(IJavaProject project)
			throws IOException, CoreException {
		List urlsList = new ArrayList();
		accumulateClasspath(project, urlsList, true);
		URL[] urls = (URL[]) urlsList.toArray(new URL[urlsList.size()]);
		return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
	}

	private void accumulateClasspath(IJavaProject project, List urlsList, boolean javaProjectBelongsToIpsProject)
			throws IOException, CoreException {
		
		IPath projectPath = project.getProject().getLocation();
		IPath root = projectPath.removeLastSegments(project.getProject()
				.getFullPath().segmentCount());
		
		if (!javaProjectBelongsToIpsProject || ipsProject.getProperties().isJavaProjectContainsClassesForDynamicDatatypes()) {
			IPath outLocation = project.getOutputLocation();
			IPath output = root.append(outLocation);
			urlsList.add(output.toFile().toURL());
			addClassfileContainer(output, urlsList);
		}
		IClasspathEntry[] entry = project.getRawClasspath();
		for (int i = 0; i < entry.length; i++) {
			if (entry[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath jarPath = root.append(entry[i].getPath());
				
				IPath copyPath = copyJar(jarPath);
				if (copyPath!=null) {
					urlsList.add(copyPath.toFile().toURL());
					addClassfileContainer(jarPath, urlsList);
				}
			}
		}

		String[] requiredProjectNames = project.getRequiredProjectNames();
		if (requiredProjectNames != null && requiredProjectNames.length > 0) {
			for (int i = 0; i < requiredProjectNames.length; i++) {
				accumulateClasspath(project.getJavaModel().getJavaProject(
						requiredProjectNames[i]), urlsList, false);
			}
		}
	}
	
	private IPath copyJar(IPath jarPath) throws IOException, CoreException {
		IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		File jarFile = jarPath.toFile();
		if (jarFile==null) {
			return null;
		}
		int index = jarFile.getName().lastIndexOf('.');
		String name =  jarFile.getName();
		File copy = File.createTempFile(name.substring(0,index-1),name.substring(index));
		copy.deleteOnExit();
		InputStream is = new FileInputStream(jarFile);
		FileOutputStream os = new FileOutputStream(copy);
		byte[] buffer = new byte[16384];
		int bytesRead = 0;
		while (bytesRead>-1) {
			bytesRead = is.read(buffer);
			if (bytesRead>0) {
				os.write(buffer, 0, bytesRead);
			}
		}
		is.close();
		os.close();
		return new Path(copy.getPath());
}
	
	/**
	 * @param containerLocation is the full path in the filesytem.
	 */
	private void addClassfileContainer(IPath containerLocation, List urls) throws MalformedURLException {
		IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath containerPath = containerLocation.removeFirstSegments(workspaceLocation.segmentCount());
		classfileContainers.add(containerPath);
	}
	
	private class ChangeListener implements IResourceChangeListener {

		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_BUILD
					&& event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
				return;
			}
			for (Iterator it=classfileContainers.iterator(); it.hasNext();) {
				IPath container = (IPath)it.next();
				IResourceDelta delta = event.getDelta().findMember(container);
				if (delta!=null) {
					classpathContentsChanged();
					break;
				}
			}
		}
	}
	
}
