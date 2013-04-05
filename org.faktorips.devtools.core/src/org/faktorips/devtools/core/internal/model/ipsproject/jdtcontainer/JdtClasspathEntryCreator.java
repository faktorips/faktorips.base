/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject.jdtcontainer;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.util.message.MessageList;

public class JdtClasspathEntryCreator {

    private final IpsObjectPath ipsObjectPath;

    public JdtClasspathEntryCreator(IpsObjectPath ipsObjectPath) {
        this.ipsObjectPath = ipsObjectPath;
    }

    public IIpsObjectPathEntry createIpsEntry(IClasspathEntry entry) {
        return new EntryCreator(entry, ipsObjectPath).createIpsEntry();
    }

    protected static class EntryCreator {

        private final IClasspathEntry entry;
        private ReferenceFactory referenceFactory;

        public EntryCreator(IClasspathEntry entry, IpsObjectPath ipsObjectPath) {
            this.entry = entry;
            referenceFactory = new ReferenceFactory(ipsObjectPath);
        }

        void setReferenceFactory(ReferenceFactory projectResolver) {
            this.referenceFactory = projectResolver;
        }

        public IIpsObjectPathEntry createIpsEntry() {
            if (entry == null) {
                return null;
            }
            if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                return createIpsProjectRefEntry();
            } else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                return createLibraryEntry();
            }
            IpsPlugin
                    .log(new IpsStatus(
                            IStatus.WARNING,
                            "IpsContainerBasedOnJdtClasspathContainer: Unsupported kind of ClasspathEntry " + entry.getEntryKind())); //$NON-NLS-1$
            return null;
        }

        IIpsProjectRefEntry createIpsProjectRefEntry() {
            IPath path = entry.getPath();
            IIpsProject ipsProject = referenceFactory.getIpsProject(path);
            if (ipsProject.exists()) {
                return referenceFactory.createProjectRefEntry(ipsProject);
            }
            return null;
        }

        IpsObjectPathEntry createLibraryEntry() {
            IPath path = entry.getPath();
            IpsObjectPathEntry ipsArchiveEntry = createIpsArchiveEntry(path);
            if (ipsArchiveEntry != null) {
                return ipsArchiveEntry;
            }
            IpsBundleEntry ipsBundleEntry = createBundleEntry(path);
            return ipsBundleEntry;
        }

        private IpsObjectPathEntry createIpsArchiveEntry(IPath path) {
            IpsArchiveEntry archiveEntry = referenceFactory.createArchiveEntry();
            archiveEntry.initStorage(path);
            MessageList messageList = archiveEntry.validate();
            if (messageList.containsErrorMsg()) {
                return null;
            } else {
                return archiveEntry;
            }
        }

        private IpsBundleEntry createBundleEntry(IPath path) {
            IpsBundleEntry ipsJarBundleEntry = referenceFactory.createIpsBundleEntry();
            try {
                ipsJarBundleEntry.initStorage(path);
            } catch (IOException e) {
                // this seem to be no jar bundle
                return null;
            }
            MessageList msgList = ipsJarBundleEntry.validate();
            if (msgList.containsErrorMsg()) {
                return null;
            } else {
                return ipsJarBundleEntry;
            }
        }
    }

    protected static class ReferenceFactory {

        private final IpsObjectPath ipsObjectPath;

        public ReferenceFactory(IpsObjectPath ipsObjectPath) {
            this.ipsObjectPath = ipsObjectPath;
        }

        public IpsBundleEntry createIpsBundleEntry() {
            return new IpsBundleEntry(ipsObjectPath);
        }

        public IpsArchiveEntry createArchiveEntry() {
            return new IpsArchiveEntry(ipsObjectPath);
        }

        public IpsProjectRefEntry createProjectRefEntry(IIpsProject referencedProject) {
            return new IpsProjectRefEntry(ipsObjectPath, referencedProject);
        }

        public IIpsProject getIpsProject(IPath path) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
            return ipsProject;
        }

    }

}