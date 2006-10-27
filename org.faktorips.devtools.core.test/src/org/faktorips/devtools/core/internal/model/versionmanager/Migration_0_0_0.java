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
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Migration-Operation for tests.
 * 
 * @author Thorsten Guenther
 */
public class Migration_0_0_0 extends AbstractMigrationOperation {

    /**
     * @param projectToMigrate
     * @param featureId
     */
    public Migration_0_0_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "0.0.1";
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
    public String getDescription() {
        return "Some real cool stuff";
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        MessageList result = new MessageList();
        result.add(new Message("first", "first", Message.ERROR));
        return result;
    }
    
}
