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

import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Marks classes generated for {@link IPolicyCmptType}, {@link IProductCmptType} and
 * {@link IEnumType} as dirty to trigger the clean build.
 */
public class Migration_19_12_0_Factory implements IIpsProjectMigrationOperationFactory {

    private static final String VERSION = "19.12.0"; //$NON-NLS-1$
    private static final Set<IpsObjectType> TYPES_TO_MIGRATE = ImmutableSet.of(IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.ENUM_TYPE);

    @Override
    public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject projectToMigrate,
            String featureId) {
        return new MarkAsDirtyMigration(projectToMigrate, featureId, TYPES_TO_MIGRATE, VERSION,
                Messages.Migration_19_12_0_description);
    }
}
