/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Migration to version 3.1.0.ms2.
 * 
 * @author dirmeier
 */
public class Migration_3_1_0_ms1 extends EmptyMigration {

    public Migration_3_1_0_ms1(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "3.1.0.ms2"; //$NON-NLS-1$
    }

}
