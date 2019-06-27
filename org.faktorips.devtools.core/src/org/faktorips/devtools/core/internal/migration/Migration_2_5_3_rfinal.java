/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from version 2.5..2rfinal to version 2.5.2.rfinal
 * 
 * @author dirmeier
 */
public class Migration_2_5_3_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_2_5_3_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "The generated method 'getLinks()' in generated product component generation classes now " //$NON-NLS-
                + System.getProperty("line.separator") //$NON-NLS-
                + " considers associations defined in super types by calling 'super.getLinks()'."; //$NON-NLS-
    }

    @Override
    public String getTargetVersion() {
        return "2.5.4.rfinal"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        return new MessageList();
    }
}
