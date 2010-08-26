/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.resources.IProject;
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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsContainerEntryBasedOnClasspathContainer extends IpsContainerEntry {

    private IPath classpathContainerPath;

    public IpsContainerEntryBasedOnClasspathContainer(IpsObjectPath path) {
        super(path);
    }

    private IClasspathContainer getClasspathContainer() throws JavaModelException {
        IJavaProject javaProject = getIpsProject().getJavaProject();
        if (javaProject == null) {
            return null;
        }
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
    public String getDescription() {
        return "IPS Object Path Container for " + classpathContainerPath.toString(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * 
     * @throws JavaModelException if an error occurs accessing the JDT classpath container.
     */
    @Override
    protected List<IpsObjectPathEntry> resolveEntriesInternal() throws JavaModelException {
        List<IpsObjectPathEntry> ipsEntries = new ArrayList<IpsObjectPathEntry>();
        IClasspathContainer cpContainer = getClasspathContainer();
        IClasspathEntry[] entries = cpContainer.getClasspathEntries();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
            if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                IPath path = entry.getPath();
                IProject project = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
                ipsEntries.add(new IpsProjectRefEntry((IpsObjectPath)getIpsObjectPath(), ipsProject));
            }
        }
        return ipsEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFromXml(Element element, IProject project) {
        String cpContainer = element.getAttribute("classpathContainer"); //$NON-NLS-1$
        if (cpContainer.length() == 0) {
            classpathContainerPath = null;
        } else {
            classpathContainerPath = new Path(cpContainer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_CONTAINER); //$NON-NLS-1$
        element.setAttribute("classpathContainer", classpathContainerPath.toString()); //$NON-NLS-1$
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList validate() throws CoreException {
        if (getClasspathContainer() == null) {
            MessageList result = new MessageList();
            result.add(Message.newError("Invalid CP-Container", "No Classpath Container for path " //$NON-NLS-1$ //$NON-NLS-2$
                    + classpathContainerPath + "found.")); //$NON-NLS-1$
            return result;
        }
        return null;
    }

}
