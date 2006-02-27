package org.faktorips.devtools.core.internal.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.devtools.core.model.IIpsProject;
import org.w3c.dom.Element;

/**
 * A dynamic value datatype is a generic value datatype where the Java class
 * represented by the datatype is defined by it's qualified class name. The
 * class is resolved, when one of the datatype's method like is isParsable() is
 * called, which needs a static method of the underlying Java class. If the Java
 * class' source belongs to the IpsProject/JavaProject, than this class (the
 * bytecode) might exist for some time and than be unavailable or incomplete,
 * e.g. if the class has compile errors.
 * 
 * @author Jan Ortmann
 */
public class DynamicValueDatatype extends GenericValueDatatype {

	public final static DynamicValueDatatype createFromXml(
			IIpsProject ipsProject, Element element) {

		DynamicValueDatatype datatype = null;
		String isEnumTypeString = element.getAttribute("isEnumType");
		if (StringUtils.isEmpty(isEnumTypeString)
				|| !Boolean.valueOf(isEnumTypeString).booleanValue()) {
			datatype = new DynamicValueDatatype(ipsProject);
		} else {
			DynamicEnumDatatype enumDatatype = new DynamicEnumDatatype(
					ipsProject);
			enumDatatype.setAllValuesMethodName(element
					.getAttribute("getAllValuesMethod"));
			enumDatatype.setGetNameMethodName(element
					.getAttribute("getNameMethod"));
			String isSupporting = element
					.getAttribute("isSupportingNames");
			enumDatatype.setIsSupportingNames(StringUtils.isEmpty(isSupporting) ? false
							: Boolean.valueOf(isSupporting).booleanValue());
			datatype = enumDatatype;
		}

		datatype.setAdaptedClassName(element.getAttribute("valueClass"));
		datatype.setQualifiedName(element.getAttribute("id"));
		datatype.setValueOfMethodName(element.getAttribute("valueOfMethod"));
		datatype.setIsParsableMethodName(element
				.getAttribute("isParsableMethod"));
		datatype.setToStringMethodName(element
				.getAttribute("valueToStringMethod"));
		if (element.hasAttribute("specialNullValue")) {
			datatype.setSpecialNullValue(element
					.getAttribute("specialNullValue"));
		}
		datatype.getAdaptedClass();
		return datatype;
	}

	private IIpsProject ipsProject;

	private Class adaptedClass;

	private String className;

	private IPath classContainerFile;

	private IResourceChangeListener changeListener;

	public DynamicValueDatatype(IIpsProject ipsProject) {
		super();
		this.ipsProject = ipsProject;
	}

	public void setAdaptedClassName(String className) {
		this.className = className;
		removeCachedData();
	}

	public void setAdaptedClass(Class clazz){
		this.adaptedClass = clazz;
	}
	
	public String getAdaptedClassName() {
		return className;
	}

	protected void removeCachedData() {
		super.removeCachedData();
		adaptedClass = null;
	}

	public Class getAdaptedClass() {
		if (adaptedClass == null) {
			try {
				adaptedClass = getProjectClassloader(
						ipsProject.getJavaProject()).loadClass(className);
				// get the URL from the resource the class was loaded from
				URL url = adaptedClass.getResource("/"
						+ adaptedClass.getName().replace('.', '/') + ".class");
				if (url.getProtocol().equals("file")) {
					startListeningToClassContainerChanges(url.getFile());
				} else if (url.getProtocol().equals("jar")) {
					String path = url.getPath();
					String jarFile = path.substring(5, path.indexOf('!'));
					startListeningToClassContainerChanges(jarFile);
				}
			} catch (Exception e) {
				// ok to squeeze, the datatype remains invalid as long as the
				// class can't be loaded.
			}
		}
		return adaptedClass;
	}

	private void accumulateClasspath(IJavaProject project, List urlsList) throws JavaModelException, MalformedURLException{
		IPath root = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath output = root.append(project.getOutputLocation());
		urlsList.add(output.toFile().toURL());
		IClasspathEntry[] entry = project.getRawClasspath();
		for (int i = 0; i < entry.length; i++) {
			if (entry[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath libPath = root.append(entry[i].getPath());
				urlsList.add(libPath.toFile().toURL());
			}
		}

		String[] requiredProjectNames = project.getRequiredProjectNames();
		if(requiredProjectNames != null && requiredProjectNames.length > 0){
			for (int i = 0; i < requiredProjectNames.length; i++) {
				accumulateClasspath(project.getJavaModel().getJavaProject(requiredProjectNames[i]), urlsList);	
			}
		}
	}
	
	/*
	 * Returns a classloader containing the project's output location and all
	 * it's libraries (jars).
	 */
	private URLClassLoader getProjectClassloader(IJavaProject project)
			throws JavaModelException, MalformedURLException {
		List urlsList = new ArrayList();
		accumulateClasspath(project, urlsList);
		URL[] urls = (URL[]) urlsList.toArray(new URL[urlsList.size()]);
		return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
	}

	private void startListeningToClassContainerChanges(String file) {
		IPath absolutPathToProject = ipsProject.getProject().getLocation();
		IPath absolutPathToFile = new Path(file);
		IPath projectRelativePathToFile = absolutPathToFile
				.removeFirstSegments(absolutPathToProject.segmentCount());
		classContainerFile = ipsProject.getProject().getFullPath().append(
				projectRelativePathToFile);
		changeListener = new ChangeListener();
		ipsProject.getProject().getWorkspace().addResourceChangeListener(
				changeListener,
				IResourceChangeEvent.POST_CHANGE
						| IResourceChangeEvent.PRE_BUILD);
	}

	private class ChangeListener implements IResourceChangeListener {

		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_BUILD
					&& event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
				return;
			}
			IResourceDelta delta = event.getDelta().findMember(
					classContainerFile);
			if (delta == null) {
				return;
			}
			ipsProject.getProject().getWorkspace()
					.removeResourceChangeListener(changeListener);
			changeListener = null;
			removeCachedData();
		}
	}
}
