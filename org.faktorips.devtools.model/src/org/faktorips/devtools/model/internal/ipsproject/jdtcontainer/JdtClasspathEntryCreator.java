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

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.mapping.PathMapping;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.MessageList;

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
            IpsLog.log(new IpsStatus(
                    IStatus.WARNING,
                    "IpsContainerBasedOnJdtClasspathContainer: Unsupported kind of ClasspathEntry " //$NON-NLS-1$
                            + entry.getEntryKind()));
            return null;
        }

        /**
         * {@link IIpsProjectRefEntry} instances created by this method are always marked as
         * re-export=<code>false</code>. The flag tells the {@link IIpsObjectPath} to not follow
         * these project references. Otherwise IPS objects might be found multiple times and thus
         * cause errors, due to the fact that all transitive references have already been resolved
         * by the JDT container.
         */
        IIpsProjectRefEntry createIpsProjectRefEntry() {
            IPath path = entry.getPath();
            IIpsProject ipsProject = referenceFactory.getIpsProject(path);
            if (ipsProject.exists()) {
                IpsProjectRefEntry refEntry = referenceFactory.createProjectRefEntry(ipsProject);
                refEntry.setReexported(false);
                return refEntry;
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
            archiveEntry.initStorage(PathMapping.toJavaPath(path));
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
                ipsJarBundleEntry.initStorage(PathMapping.toJavaPath(path));
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
            AProject project = Abstractions.getWorkspace().getRoot().getProject(path.lastSegment());
            IIpsProject ipsProject = IIpsModel.get().getIpsProject(project);
            return ipsProject;
        }

    }

}