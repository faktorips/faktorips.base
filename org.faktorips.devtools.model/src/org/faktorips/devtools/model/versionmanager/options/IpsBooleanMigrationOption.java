/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.versionmanager.options;

import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;

/**
 * Configuration for an {@link AbstractIpsProjectMigrationOperation}. Should be used if it is not
 * clear what a migration should do. We need a specific implementation because the binder needs
 * concrete types at runtime.
 *
 * @since 22.6
 */
public class IpsBooleanMigrationOption extends IpsMigrationOption<Boolean> {

    public IpsBooleanMigrationOption(String id, String text, Boolean defaultValue) {
        super(id, text, defaultValue);
    }
}
