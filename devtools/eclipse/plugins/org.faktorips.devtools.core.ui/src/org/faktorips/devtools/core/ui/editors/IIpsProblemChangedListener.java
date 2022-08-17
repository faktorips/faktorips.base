/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.resources.IResource;

/**
 * Can be added to a IpsProblemMarkerManager to get notified about ips problem marker changes. Used
 * to update error ticks.
 * 
 * @author Joerg Ortmann
 */
public interface IIpsProblemChangedListener {
    /**
     * Called when problems changed. This call is posted in an aynch exec, therefore passed
     * resources must not exist.
     * 
     * @param changedResources A set with elements of type <code>IResource</code> that describe the
     *            resources that had an problem change.
     */
    void problemsChanged(IResource[] changedResources);
}
