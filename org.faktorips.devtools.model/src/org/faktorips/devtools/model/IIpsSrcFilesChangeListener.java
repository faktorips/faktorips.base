/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.eclipse.core.resources.IResourceDelta;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * This listener is notified if at least one {@link IIpsSrcFile} is modified in a resource change
 * event. For every resource change event you get exactly one {@link IpsSrcFilesChangedEvent}
 * containing every changed {@link IIpsSrcFile} and the corresponding {@link IResourceDelta}.
 * 
 * @author dirmeier
 */
@FunctionalInterface
public interface IIpsSrcFilesChangeListener {

    /**
     * This method is called by the model if at least one {@link IIpsSrcFile} have changed in a
     * resource change event
     * 
     * @param event the {@link IpsSrcFilesChangedEvent} containing all {@link IIpsSrcFile source
     *            files} that have changed
     */
    void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event);

}
