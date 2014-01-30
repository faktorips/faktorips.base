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
 * Migration from Faktor-IPS Version 2.2.5.rfinal to 2.3.0.rc1.
 * 
 * @author Alexander Weickmann
 */
public class Migration_2_2_5_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_2_2_5_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "New Faktor-IPS object types" //$NON-NLS-1$
                + " have been added for modeling enumerations." //$NON-NLS-1$
                + " Abstract enumeration type objects will be created for each table structure that represents an enumeration type" //$NON-NLS-1$
                + " (the old table structures must be deleted manually). The referencing" //$NON-NLS-1$
                + " table contents will also be replaced with new enumeration type objects containing the enumeration values."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "2.3.0.rc1"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        Migration2_2_to2_3.migrate(getIpsProject(), monitor);
        return new MessageList();
    }
}
