/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
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
import org.faktorips.util.message.MessageList;

/**
 * Empty migration.
 * 
 * @author Jan Ortmann
 */
public class Migration_1_0_0_rc2_b extends AbstractMigrationOperation {

    public Migration_1_0_0_rc2_b(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    public String getDescription() {
        return "Some bugs fixed"; //$NON-NLS-1$
    }

    public String getTargetVersion() {
        return "1.0.0.rc2-c"; //$NON-NLS-1$
    }

    public boolean isEmpty() {
        return true;
    }

    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        return new MessageList();
    }

}
