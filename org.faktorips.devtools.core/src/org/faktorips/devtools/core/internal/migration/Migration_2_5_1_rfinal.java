/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from version 2.5.1.rfinal to version 2.5.2.rfinal
 * 
 * @author dirmeier
 */
public class Migration_2_5_1_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_2_5_1_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "IMPORTANT: \n" //$NON-NLS-1$
                + "\n" //$NON-NLS-1$
                + "In this version we have changed the signature of some copy methods. The changes only affects " //$NON-NLS-1$
                + "the parameters of the copy methods. Unfortunately the changes will lead to errors in the java code " //$NON-NLS-1$
                + "of existing Faktor-IPS projects when a clean build is executed after this Faktor-IPS version has been installed. " //$NON-NLS-1$
                + "This is because of a bug in JMerge. To fix the generated code you have to execute two search/replace operations:\n" //$NON-NLS-1$
                + "\n" + "1.\nSearch:\n" //$NON-NLS-1$//$NON-NLS-2$
                + "public IModelObject newCopyInternal(Map<AbstractModelObject, AbstractModelObject> copyMap)\n" //$NON-NLS-1$
                + "Replace with:\n" + "public IModelObject newCopyInternal(Map<IModelObject, IModelObject> copyMap)\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "\n" + "2.\nSearch (with regular Expressions!):\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "protected void copyProperties\\((.*), Map<AbstractModelObject, AbstractModelObject> copyMap\\)\n" //$NON-NLS-1$
                + "Replace with:" + "protected void copyProperties\\($1, Map<IModelObject, IModelObject> copyMap\\)"; //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public String getTargetVersion() {
        return "2.5.2.rfinal"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        return new MessageList();
    }
}
