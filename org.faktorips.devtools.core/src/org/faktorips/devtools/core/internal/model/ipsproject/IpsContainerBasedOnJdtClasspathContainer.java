/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * An ips object path container entry that is based on a JDT classpath container.
 * 
 * @author Jan Ortmann
 */
public class IpsContainerBasedOnJdtClasspathContainer implements IIpsObjectPathContainer {

    public final static String KIND = "ClasspathContainer"; //$NON-NLS-1$

    private IClasspathContainer jdtClasspathContainer = null;
    private List<IIpsObjectPathEntry> resolvedEntries = new ArrayList<IIpsObjectPathEntry>(0);

    public IpsContainerBasedOnJdtClasspathContainer() {
        super();
    }

    @Override
    public String getKind() {
        return KIND;
    }

    @Override
    public String getName(IIpsContainerEntry entry) {
        IClasspathContainer cpContainer;
        try {
            cpContainer = findClasspathContainer(entry);
        } catch (JavaModelException e) {
            cpContainer = null;
        }
        if (cpContainer != null) {
            return cpContainer.getDescription();
        }
        return "InvalidContainer: " + entry.getContainerKind() + '[' + entry.getContainerPath() + ']'; //$NON-NLS-1$
    }

    @Override
    public List<IIpsObjectPathEntry> resolveEntries(IIpsContainerEntry containerEntry) throws CoreException {
        IJavaProject javaProject = containerEntry.getIpsProject().getJavaProject();
        if (jdtClasspathContainer != null) {
            IClasspathContainer actualContainer = JavaCore.getClasspathContainer(new Path(containerEntry
                    .getContainerPath()), javaProject);
            if (jdtClasspathContainer == actualContainer) {
                return resolvedEntries;
            }
            jdtClasspathContainer = actualContainer;
        } else {
            jdtClasspathContainer = JavaCore.getClasspathContainer(new Path(containerEntry.getContainerPath()),
                    javaProject);
            if (jdtClasspathContainer == null) {
                return new ArrayList<IIpsObjectPathEntry>(0);
            }
        }
        IpsObjectPath ipsObjectPath = (IpsObjectPath)containerEntry.getIpsObjectPath();
        resolvedEntries = new ArrayList<IIpsObjectPathEntry>();
        IClasspathEntry[] entries = jdtClasspathContainer.getClasspathEntries();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry jdtEntry = JavaCore.getResolvedClasspathEntry(entries[i]);
            IIpsObjectPathEntry ipsEntry = createIpsEntry(jdtEntry, ipsObjectPath);
            if (ipsEntry != null) {
                resolvedEntries.add(ipsEntry);
            }
        }
        return resolvedEntries;
    }

    private IIpsObjectPathEntry createIpsEntry(IClasspathEntry entry, IpsObjectPath ipsObjectPath) {
        if (entry == null) {
            return null;
        }
        if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
            return createIpsProjectRefEntry(entry, ipsObjectPath);
        } else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
            return createIpsArchiveEntry(entry, ipsObjectPath);
        }
        IpsPlugin.log(new IpsStatus(
                "IpsContainerBasedOnJdtClasspathContainer: Unsupported kind of ClasspathEntry " + entry.getEntryKind())); //$NON-NLS-1$
        return null;
    }

    private IIpsProjectRefEntry createIpsProjectRefEntry(IClasspathEntry entry, IpsObjectPath ipsObjectPath) {
        IPath path = entry.getPath();
        IProject project = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
        if (ipsProject.exists()) {
            return new IpsProjectRefEntry(ipsObjectPath, ipsProject);
        }
        return null;
    }

    private IIpsArchiveEntry createIpsArchiveEntry(IClasspathEntry entry, IpsObjectPath ipsObjectPath) {
        IPath path = entry.getPath();
        if (path.isAbsolute()) {
            return null;
        }
        IIpsProject ipsProject = ipsObjectPath.getIpsProject();
        IResource member = ipsProject.getProject().findMember(path);
        if (!(member instanceof IFile)) {
            throw new RuntimeException("Archive must be a file!"); //$NON-NLS-1$
        }
        IpsArchiveEntry archiveEntry = new IpsArchiveEntry(ipsObjectPath);
        archiveEntry.setArchivePath(ipsProject, path);
        return archiveEntry;
    }

    /**
     * Returns the JDT classpath container identified by the given IPS container entry.
     * 
     * @param containerEntry An IPS object path container entry identifying a JDT classpath
     *            container.
     * @return The identified JDT classpath container or <code>null</code> of the entry does not
     *         identify one.
     * 
     * @throws JavaModelException if an error occurs while accessing the classpath container.
     * @throws NullPointerException if containerEntry is <code>null</code>.
     */
    public IClasspathContainer findClasspathContainer(IIpsContainerEntry containerEntry) throws JavaModelException {
        IJavaProject javaProject = containerEntry.getIpsProject().getJavaProject();
        if (javaProject == null) {
            return null;
        }
        IPath classpathContainerPath = new Path(containerEntry.getContainerPath());
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (classpathContainerPath.equals(entries[i].getPath())) {
                    return JavaCore.getClasspathContainer(classpathContainerPath, javaProject);
                }
            }
        }
        return null;
    }

    @Override
    public MessageList validate(IIpsContainerEntry entry) throws CoreException {
        IClasspathContainer container;
        try {
            container = findClasspathContainer(entry);
        } catch (JavaModelException e) {
            IpsPlugin.log(e);
            container = null;
        }
        if (container != null) {
            return null;
        }
        MessageList result = new MessageList();
        Message msg = Message.newError("Invalid-ClasspathContainer-Path", //$NON-NLS-1$
                "Invalid IPS Object Path: The container path '" + entry.getContainerPath() //$NON-NLS-1$
                        + "' does not identify a JDT Classpath Container!"); //$NON-NLS-1$
        result.add(msg);
        return result;
    }
}
