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

import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;

/**
 * A RuntimeException subclass for exceptions that occur during the execution of an
 * {@link AbstractIpsProjectMigrationOperation}.
 */
public class MigrationRuntimeException extends RuntimeException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8394565425850434556L;

    public MigrationRuntimeException(String message) {
        super(message);
    }

    public MigrationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationRuntimeException(Throwable cause) {
        super(cause);
    }

}
