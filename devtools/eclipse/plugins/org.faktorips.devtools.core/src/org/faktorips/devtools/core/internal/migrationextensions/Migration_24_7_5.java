/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import java.text.MessageFormat;
import java.util.Set;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.EmptyIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class Migration_24_7_5 extends MarkAsDirtyMigration {

    public static final String MSGCODE_IPS_VERSION_TOO_OLD = "IPS_VERSION_TOO_OLD"; //$NON-NLS-1$
    private static final String VERSION = "24.7.5"; //$NON-NLS-1$

    public Migration_24_7_5(IIpsProject projectToMigrate, String featureId) {
        this(projectToMigrate, featureId, VERSION);
    }

    Migration_24_7_5(IIpsProject projectToMigrate, String featureId, String migrationVersion) {
        super(projectToMigrate,
                featureId,
                Set.of(IIpsModel.get().getIpsObjectTypes()),
                migrationVersion,
                Messages.Migration_24_7_5_description);
    }

    @Override
    public MessageList canMigrate() {
        String minRequiredVersionNumber = getIpsProject().getProperties()
                .getMinRequiredVersionNumber(EmptyIpsFeatureVersionManager.INSTANCE.getFeatureId());

        String[] versionParts = minRequiredVersionNumber.split("\\.");
        int majorVersion = Integer.parseInt(versionParts[0]);

        if (majorVersion < 24) {
            return MessageList.of(Message.newError(MSGCODE_IPS_VERSION_TOO_OLD,
                    MessageFormat.format(
                            Messages.Migration_IpsVersionTooOld,
                            minRequiredVersionNumber, getIpsProject().getName(), VERSION,
                            Migration_24_1_0.VERSION)));
        }
        return super.canMigrate();
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_24_7_5(ipsProject, featureId);
        }
    }
}
