/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Migration to version 3.1.0.rfinal.
 * 
 * @author dirmeier
 */
public class Migration_3_1_0_rc1a extends EmptyMigration {

    public Migration_3_1_0_rc1a(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "3.1.0.rfinal"; //$NON-NLS-1$
    }

}
