/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Default migration that loads every ips object in the source folders of the given project and call
 * a template method called migrate() to allow subclasses to modify the object.
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultMigration extends AbstractIpsProjectMigrationOperation {

    public DefaultMigration(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messages = new MessageList();
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        try {
            monitor.beginTask("Migrate project: " + getIpsProject().getName(), countPackages() * 10); //$NON-NLS-1$
            beforeFileMigration();
            for (IIpsPackageFragmentRoot root : roots) {
                IIpsPackageFragment[] packs = root.getIpsPackageFragments();
                for (IIpsPackageFragment pack : packs) {
                    migrate(pack, messages, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 10));
                    if (monitor.isCanceled()) {
                        return messages;
                    }
                }
            }
        } finally {
            monitor.done();
        }
        return messages;
    }

    private int countPackages() throws CoreException {
        int packs = 0;
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            packs = packs + root.getIpsPackageFragments().length;
        }
        return packs;
    }

    protected void migrate(IIpsPackageFragment pack, MessageList list, IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)pack.getCorrespondingResource();
        IResource[] members = folder.members();
        monitor.beginTask("Migrate package " + pack.getName(), members.length); //$NON-NLS-1$
        for (IResource member : members) {
            try {
                if (member instanceof IFile) {
                    IFile file = (IFile)member;
                    boolean wasMigrated = migrate(file);
                    if (!wasMigrated) {
                        IIpsSrcFile srcFile = pack.getIpsSrcFile(file.getName());
                        if (srcFile != null) {
                            migrate(srcFile);
                        }
                    }
                    if (monitor.isCanceled()) {
                        break;
                    }
                }
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                String text = "An error occured while migrating file " + member; //$NON-NLS-1$
                System.err.println(text);
                list.add(Message.newError("", text)); //$NON-NLS-1$
                IpsPlugin.log(new IpsStatus(text, e));
            }
            // CSON: IllegalCatch
            monitor.worked(1);
        }
        monitor.done();
    }

    /**
     * This template method is called for all files in an ips package fragments. Subclasses must
     * implement their migration logic here.
     * 
     * @param file the file that should be migrated
     * 
     * @see IIpsSrcFile#markAsDirty()
     * @return true when migration is done and {@link #migrate(IIpsSrcFile)} should not be called
     * @throws CoreException in case of any exception throw a {@link CoreException}
     */
    protected boolean migrate(IFile file) throws CoreException {
        // default do nothing
        return false;
    }

    /**
     * This template method is called after the {@link IIpsSrcFile} is loaded.
     * <p>
     * Subclasses must implement their migration logic here. Note that an object is only saved
     * physically to disk if it was either changed or it's enclosing {@link IIpsSrcFile} is marked
     * as dirty.
     * <p>
     * <strong>Important:</strong> The migration process must be implemented in such a way that it
     * can be executed multiple times on the same {@link IIpsSrcFile} as projects may be migrated
     * several times using the same migration.
     * 
     * @param srcFile the {@link IIpsSrcFile} to migrate
     * 
     * @see IIpsSrcFile#markAsDirty()
     */
    protected abstract void migrate(IIpsSrcFile srcFile) throws CoreException;

    /**
     * Hook method for subclasses to do stuff that is done once before any file is migrated.
     * 
     * @throws CoreException This method may throw this exception at any time.
     */
    protected void beforeFileMigration() throws CoreException {
        // Empty default implementation
    }
}
