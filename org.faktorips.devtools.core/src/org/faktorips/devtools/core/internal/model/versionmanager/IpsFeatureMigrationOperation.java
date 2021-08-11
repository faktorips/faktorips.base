/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IpsMigrationOption;
import org.faktorips.runtime.MessageList;
import org.osgi.framework.Version;

/**
 * Operation to migrate the content created with one version of FaktorIps to match the needs of
 * another version.
 * 
 * @author Thorsten Guenther
 */
public class IpsFeatureMigrationOperation extends AbstractIpsFeatureMigrationOperation {

    private MessageList result;
    private ArrayList<AbstractIpsProjectMigrationOperation> operations = new ArrayList<>();
    private IIpsProject projectToMigrate;

    public IpsFeatureMigrationOperation(IIpsProject projectToMigrate) {
        this.projectToMigrate = projectToMigrate;
    }

    @Override
    public IIpsProject getIpsProject() {
        return projectToMigrate;
    }

    public void addMigrationPath(AbstractIpsProjectMigrationOperation[] path) {
        operations.addAll(Arrays.asList(path));
    }

    @Override
    public final void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        IProgressMonitor theMonitor = monitor != null ? monitor : new NullProgressMonitor();

        String msg = NLS.bind(Messages.IpsContentMigrationOperation_labelMigrateProject, projectToMigrate.getName());
        theMonitor.beginTask(msg, 1010 + operations.size() * 1000);
        try {
            executeInternal(theMonitor);
        } finally {
            theMonitor.done();
        }
    }

    @SuppressWarnings("deprecation")
    private void executeInternal(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        try {
            monitor.worked(10);
            result = new MessageList();
            for (AbstractIpsProjectMigrationOperation operation : operations) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                result.add(operation.migrate(new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1000)));
            }
        } catch (CoreException e) {
            rollback();
            throw (e);
        } catch (InvocationTargetException e) {
            rollback();
            throw (e);
        } catch (InterruptedException e) {
            rollback();
            throw (e);
            // CSOFF: IllegalCatch
        } catch (Throwable t) {
            rollback();
            throw new CoreException(new IpsStatus(t));
            // CSON: IllegalCatch
        }

        monitor.subTask(Messages.IpsContentMigrationOperation_labelSaveChanges);
        ArrayList<IIpsSrcFile> files = new ArrayList<>();
        projectToMigrate.findAllIpsSrcFiles(files);
        IProgressMonitor saveMonitor = new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1000);
        saveMonitor.beginTask(Messages.IpsContentMigrationOperation_labelSaveChanges, files.size());

        // at this point, we do not allow the user to cancel this operation any more because
        // we now start to save all the modifications - which has to be done atomically.
        monitor.setCanceled(false);
        for (IIpsSrcFile element : files) {
            IIpsSrcFile file = (element);
            if (file.isDirty()) {
                file.save(true, monitor);
            }
            saveMonitor.worked(1);
        }
        saveMonitor.done();
        updateIpsProject();
    }

    private void rollback() {
        ArrayList<IIpsSrcFile> files = new ArrayList<>();
        try {
            projectToMigrate.findAllIpsSrcFiles(files);
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Error during rollback of migration. Rollback might have failed", e)); //$NON-NLS-1$
        }
        for (IIpsSrcFile element : files) {
            IIpsSrcFile file = (element);
            if (file.isDirty()) {
                file.discardChanges();
                file.markAsClean();
            }
        }

    }

    private void updateIpsProject() throws CoreException {
        if (isEmpty()) {
            return;
        }
        IIpsProjectProperties props = projectToMigrate.getProperties();

        // for every migrated feature find the maximum target version.
        Hashtable<String, String> features = new Hashtable<>();
        for (AbstractIpsProjectMigrationOperation operation : operations) {
            String version = features.get(operation.getFeatureId());
            if (version == null) {
                features.put(operation.getFeatureId(), operation.getTargetVersion());
            } else if (Version.parseVersion(version)
                    .compareTo(Version.parseVersion(operation.getTargetVersion())) < 0) {
                features.put(operation.getFeatureId(), operation.getTargetVersion());
            }
        }

        for (Enumeration<String> keys = features.keys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            props.setMinRequiredVersionNumber(key, features.get(key));
        }
        projectToMigrate.setProperties(props);
    }

    @Override
    public MessageList getMessageList() {
        return result;
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        for (AbstractIpsProjectMigrationOperation operation : operations) {
            description.append("-> ").append(operation.getTargetVersion()).append(System.lineSeparator()); //$NON-NLS-1$
            description.append(operation.getDescription()).append(System.lineSeparator());
        }
        return description.toString();
    }

    @Override
    public boolean isEmpty() {
        boolean empty = true;

        for (int i = 0; i < operations.size() && empty; i++) {
            AbstractIpsProjectMigrationOperation operation = operations.get(i);
            empty = empty && operation.isEmpty();
        }
        return empty;
    }

    @Override
    public Collection<IpsMigrationOption> getOptions() {
        return operations.stream()
                .map(AbstractIpsProjectMigrationOperation::getOptions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
