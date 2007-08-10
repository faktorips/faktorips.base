/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Default migration that loads every ips object in the source folders of the given project
 * and call a template method called migrate() to allow subclasses to modify the object.
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultMigration extends AbstractMigrationOperation {

    public DefaultMigration(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
            
        MessageList messages = new MessageList();
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IIpsPackageFragment[] packs = roots[i].getIpsPackageFragments();
            for (int j = 0; j < packs.length; j++) {
                migrate(packs[j], messages, monitor);
                if (monitor.isCanceled()) {
                    return messages;
                }
            }
        }
        return messages;
    }
    
    private void migrate(IIpsPackageFragment pack, MessageList list, IProgressMonitor monitor) throws CoreException {
        IIpsSrcFile[] files = pack.getIpsSrcFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                migrate(files[i].getIpsObject());
                if (monitor.isCanceled()) {
                    break;
                }
            } catch (Exception e) {
                list.add(Message.newError("", "An error occured while migrating file " + files[i]));
                IpsPlugin.log(e);
            }
            monitor.worked(1);
        }
    }

    /**
     * This template method is called after the ips object is loaded and before it is saved.
     * Subclasses must implement their migration logik here. Note that an object is only saved physically to disk, 
     * if was either changed or it's enclosing file is markes as dirty.
     * 
     * @see IIpsSrcFile#markAsDirty()
     */
    protected abstract void migrate(IIpsObject object) throws CoreException;

}
