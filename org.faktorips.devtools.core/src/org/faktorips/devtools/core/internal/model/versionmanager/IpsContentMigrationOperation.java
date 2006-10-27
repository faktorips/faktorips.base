/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsContentMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Operation to migrate the content created with one version of FaktorIps to match the needs of
 * another version.
 * 
 * @author Thorsten Guenther
 */
public class IpsContentMigrationOperation extends AbstractIpsContentMigrationOperation {

    private MessageList result;
    private ArrayList operations = new ArrayList();
    private IIpsProject projectToMigrate;

    public IpsContentMigrationOperation(IIpsProject projectToMigrate) {
        this.projectToMigrate = projectToMigrate;
    }

    public void addMigrationPath(AbstractMigrationOperation[] path) {
        operations.addAll(Arrays.asList(path));
    }

    /**
     * {@inheritDoc}
     */
    protected final void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        // check for unsaved changes - is there a more elegant way???
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (int i = 0; i < windows.length; i++) {
                IWorkbenchPage[] pages = windows[i].getPages();
                for (int j = 0; j < pages.length; j++) {
                    IEditorReference[] editors = pages[j].getEditorReferences();
                    for (int k = 0; k < editors.length; k++) {
                        if (editors[k].isDirty()) {
                            throw new CoreException(new IpsStatus("Can not migrate if unsaved changes exist.")); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        
        String msg = NLS.bind(Messages.IpsContentMigrationOperation_labelMigrateProject, projectToMigrate.getName());
        monitor.beginTask(msg, operations.size());
        
        result = new MessageList();
        for (int i = 0; i < operations.size(); i++) {
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }
            AbstractMigrationOperation operation = (AbstractMigrationOperation)operations.get(i);
            monitor.subTask(operation.getDescription());
            result.add(operation.migrate(monitor));
            monitor.worked(1);
        }
        
        monitor.subTask(Messages.IpsContentMigrationOperation_labelSaveChanges);
        ArrayList result = new ArrayList();
        projectToMigrate.findAllIpsObjects(result);
        for (int i = 0; i < result.size(); i++) {
            ((IIpsObject)result.get(i)).getIpsSrcFile().save(true, monitor);
        }
    }

    /**
     * {@inheritDoc}
     */
    public MessageList getMessageList() {
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        StringBuffer description = new StringBuffer();
        for (int i = 0; i < operations.size(); i++) {
            AbstractMigrationOperation operation = (AbstractMigrationOperation)operations.get(i);
            description.append(operation.getDescription()).append(SystemUtils.LINE_SEPARATOR);
        }
        return description.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        boolean empty = true;

        for (int i = 0; i < operations.size() && empty; i++) {
            AbstractMigrationOperation operation = (AbstractMigrationOperation)operations.get(i);
            empty = empty && operation.isEmpty();
        }
        return empty;
    }
}
