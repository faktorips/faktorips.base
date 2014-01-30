/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Empty Migration
 */
public class Migration_2_2_1_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_2_2_1_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "Some bugs fixed."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "2.2.2.rfinal"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        return new MessageList();
    }

}
