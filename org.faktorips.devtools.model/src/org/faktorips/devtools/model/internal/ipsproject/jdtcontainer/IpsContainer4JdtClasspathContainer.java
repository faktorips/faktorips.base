/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.jdtcontainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsObjectPathContainer;
import org.faktorips.devtools.model.internal.ipsproject.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * An IPS object path container entry that is based on a JDT classpath container.
 * <p>
 * {@link IIpsProjectRefEntry} instances resolved/created by this container are always marked as
 * re-export= <code>false</code>. The flag tells the {@link IIpsObjectPath} to not follow these
 * project references. Otherwise IPS objects might be found multiple times and thus cause errors,
 * due to the fact that all transitive references have already been resolved by the JDT container.
 * 
 */
public class IpsContainer4JdtClasspathContainer extends AbstractIpsObjectPathContainer {

    private static final String MSG_CODE_INVALID_CLASSPATH_CONTAINER_PATH = "Invalid-ClasspathContainer-Path"; //$NON-NLS-1$

    private IClasspathContainer jdtClasspathContainer = null;

    private List<IIpsObjectPathEntry> resolvedEntries = new CopyOnWriteArrayList<IIpsObjectPathEntry>();

    private JdtClasspathResolver jdtClasspathResolver;

    private JdtClasspathEntryCreator entryCreator;

    /**
     * Creates an IPS container that references the JDT classpath container with the specified path.
     * 
     * @param jdtContainerPath The JDT classpath container path
     * @param ipsProject The {@link IIpsProject} that holds this container
     */
    public IpsContainer4JdtClasspathContainer(String jdtContainerPath, IIpsProject ipsProject) {
        super(IpsContainer4JdtClasspathContainerType.ID, jdtContainerPath, ipsProject);
        jdtClasspathResolver = new JdtClasspathResolver();
        entryCreator = new JdtClasspathEntryCreator(getIpsObjectPath());
    }

    void setContainerResolver(JdtClasspathResolver containerResolver) {
        this.jdtClasspathResolver = containerResolver;
    }

    void setEntryCreator(JdtClasspathEntryCreator entryCreator) {
        this.entryCreator = entryCreator;
    }

    @Override
    public String getName() {
        IClasspathContainer cpContainer;
        try {
            cpContainer = findClasspathContainer();
        } catch (JavaModelException e) {
            cpContainer = null;
        }
        if (cpContainer != null) {
            return cpContainer.getDescription();
        }
        return "Unresolved: " + super.getContainerId() + '[' + getOptionalPath() + ']'; //$NON-NLS-1$
    }

    @Override
    public List<IIpsObjectPathEntry> resolveEntries() {
        IClasspathContainer currentContainer;
        try {
            currentContainer = getClasspathContainer();
        } catch (JavaModelException e) {
            throw new CoreRuntimeException(e);
        }
        synchronized (this) {
            if (jdtClasspathContainer != null) {
                if (jdtClasspathContainer == currentContainer) {
                    return Collections.unmodifiableList(resolvedEntries);
                }
            } else {
                if (currentContainer == null) {
                    return new ArrayList<IIpsObjectPathEntry>(0);
                }
            }
            jdtClasspathContainer = currentContainer;
            updateResolvedEntries();
            return Collections.unmodifiableList(resolvedEntries);
        }
    }

    private IClasspathContainer getClasspathContainer() throws JavaModelException {
        IJavaProject javaProject = getIpsProject().getJavaProject();
        return jdtClasspathResolver.getClasspathContainer(javaProject, getOptionalPath());
    }

    private void updateResolvedEntries() {
        resolvedEntries = new CopyOnWriteArrayList<IIpsObjectPathEntry>();
        IClasspathEntry[] entries = jdtClasspathContainer.getClasspathEntries();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry jdtEntry = jdtClasspathResolver.getResolvedClasspathEntry(entries[i]);
            IIpsObjectPathEntry ipsEntry = entryCreator.createIpsEntry(jdtEntry);
            if (ipsEntry != null) {
                resolvedEntries.add(ipsEntry);
            }
        }
    }

    /**
     * Returns the JDT classpath container identified by the given IPS container entry.
     * 
     * @return The identified JDT classpath container or <code>null</code> of the entry does not
     *         identify one.
     * 
     * @throws JavaModelException if an error occurs while accessing the classpath container.
     * @throws NullPointerException if containerEntry is <code>null</code>.
     */
    public IClasspathContainer findClasspathContainer() throws JavaModelException {
        IJavaProject javaProject = getIpsProject().getJavaProject();
        if (javaProject == null) {
            return null;
        }
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (getOptionalPath().equals(entries[i].getPath())) {
                    return getClasspathContainer();
                }
            }
        }
        return null;
    }

    @Override
    public MessageList validate() {
        MessageList result = new MessageList();
        IClasspathContainer container;
        try {
            container = findClasspathContainer();
        } catch (JavaModelException e) {
            IpsLog.log(e);
            container = null;
        }
        if (container != null) {
            return result;
        }
        Message msg = new Message(MSG_CODE_INVALID_CLASSPATH_CONTAINER_PATH, NLS.bind(
                Messages.IpsContainer4JdtClasspathContainer_err_invalidClasspathContainer, getOptionalPath()),
                Message.ERROR, this);
        result.add(msg);
        return result;
    }

    protected static class JdtClasspathResolver {

        protected IClasspathContainer getClasspathContainer(IJavaProject javaProject, IPath classpathContainerPath)
                throws JavaModelException {
            return JavaCore.getClasspathContainer(classpathContainerPath, javaProject);
        }

        public IClasspathEntry getResolvedClasspathEntry(IClasspathEntry classpathEntry) {
            return JavaCore.getResolvedClasspathEntry(classpathEntry);
        }

    }

}
