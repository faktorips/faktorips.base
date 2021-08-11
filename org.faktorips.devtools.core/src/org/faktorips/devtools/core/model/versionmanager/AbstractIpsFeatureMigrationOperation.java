/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Abstract class for operations migrating the content of an IPS project created with an older
 * version of Faktor-IPS to match the needs of the current installed version of Faktor-IPS.
 * <p>
 * This class is a container for implementations of <code>IIpsFeatureMigrationOperation</code>s.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractIpsFeatureMigrationOperation extends WorkspaceModifyOperation
        implements IIpsFeatureMigrationOperation {

    // implements IIpsFeatureMigrationOperation as WorkspaceModifyOperation

}
