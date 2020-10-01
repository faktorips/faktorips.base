/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

public class Migration_20_12_0_Factory implements IIpsProjectMigrationOperationFactory {

    private static final String VERSION = "20.12.0"; //$NON-NLS-1$
    private static final Set<IpsObjectType> TYPES_TO_MIGRATE = ImmutableSet.of(IpsObjectType.TABLE_CONTENTS);

    @Override
    public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject projectToMigrate,
            String featureId) {
        return new MarkAsDirtyMigration(projectToMigrate, featureId, TYPES_TO_MIGRATE, VERSION,
                Messages.Migration_20_12_0_description);
    }
}
