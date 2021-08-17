/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import org.eclipse.core.runtime.IStatus;

public interface IIpsWorkspaceInteractions {

    void runInDisplayThreadAsync(Runnable runnable);

    void runInDisplayThreadAsyncIfNotCurrentDisplay(Runnable runnable);

    void runInDisplayThreadSync(Runnable runnable);

    void showErrorDialog(IStatus status);

}
