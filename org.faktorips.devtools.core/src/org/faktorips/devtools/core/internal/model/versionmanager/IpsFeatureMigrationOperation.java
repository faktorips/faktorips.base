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

package org.faktorips.devtools.core.internal.model.versionmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;
import org.osgi.framework.Version;

/**
 * Operation to migrate the content created with one version of FaktorIps to match the needs of
 * another version.
 * 
 * @author Thorsten Guenther
 */
public class IpsFeatureMigrationOperation extends AbstractIpsFeatureMigrationOperation {

    private MessageList result;
    private ArrayList<AbstractIpsProjectMigrationOperation> operations = new ArrayList<AbstractIpsProjectMigrationOperation>();
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
    protected final void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        String msg = NLS.bind(Messages.IpsContentMigrationOperation_labelMigrateProject, projectToMigrate.getName());
        monitor.beginTask(msg, 1010 + operations.size() * 1000);
        try {
            executeInternal(monitor);
        } finally {
            monitor.done();
        }
    }

    private void executeInternal(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        try {
            monitor.worked(10);
            result = new MessageList();
            for (int i = 0; i < operations.size(); i++) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                AbstractIpsProjectMigrationOperation operation = operations.get(i);
                result.add(operation.migrate(new SubProgressMonitor(monitor, 1000)));
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
        } catch (Throwable t) {
            rollback();
            throw new CoreException(new IpsStatus(t));
        }

        monitor.subTask(Messages.IpsContentMigrationOperation_labelSaveChanges);
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        projectToMigrate.findAllIpsSrcFiles(result);
        IProgressMonitor saveMonitor = new SubProgressMonitor(monitor, 1000);
        saveMonitor.beginTask(Messages.IpsContentMigrationOperation_labelSaveChanges, result.size());

        // at this point, we do not allow the user to cancel this operation any more because
        // we now start to save all the modifications - which has to be done atomically.
        monitor.setCanceled(false);
        for (int i = 0; i < result.size(); i++) {
            IIpsSrcFile file = (result.get(i));
            if (file.isDirty()) {
                file.save(true, monitor);
            }
            saveMonitor.worked(1);
        }
        saveMonitor.done();
        updateIpsProject();
    }

    private void rollback() {
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        try {
            projectToMigrate.findAllIpsSrcFiles(result);
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Error during rollback of migration. Rollback might have failed", e)); //$NON-NLS-1$
        }
        for (int i = 0; i < result.size(); i++) {
            IIpsSrcFile file = (result.get(i));
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
        Hashtable<String, String> features = new Hashtable<String, String>();
        for (int i = 0; i < operations.size(); i++) {
            AbstractIpsProjectMigrationOperation operation = operations.get(i);
            String version = features.get(operation.getFeatureId());
            if (version == null) {
                features.put(operation.getFeatureId(), operation.getTargetVersion());
            } else if (Version.parseVersion(version).compareTo(Version.parseVersion(operation.getTargetVersion())) < 0) {
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
        StringBuffer description = new StringBuffer();
        for (int i = 0; i < operations.size(); i++) {
            AbstractIpsProjectMigrationOperation operation = operations.get(i);
            description.append("-> ").append(operation.getTargetVersion()).append(SystemUtils.LINE_SEPARATOR); //$NON-NLS-1$
            description.append(operation.getDescription()).append(SystemUtils.LINE_SEPARATOR);
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

}
