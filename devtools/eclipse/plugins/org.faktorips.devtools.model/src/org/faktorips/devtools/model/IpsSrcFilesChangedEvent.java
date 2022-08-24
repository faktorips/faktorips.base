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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * The {@link IpsSrcFilesChangedEvent} is fired for a {@link IIpsSrcFilesChangeListener} and
 * contains a map with changed {@link IIpsSrcFile IPS source files} and the corresponding
 * {@link AResourceDelta resource deltas}.
 * 
 * @author dirmeier
 */
public class IpsSrcFilesChangedEvent {

    private Map<IIpsSrcFile, AResourceDelta> changedSrcFiles;

    /**
     * The constructor for an {@link IpsSrcFilesChangedEvent} needing the changed source file map
     * 
     * @param changedSrcFiles The map with all changed {@link IIpsSrcFile IPS source files} and
     *            corresponding {@link AResourceDelta}
     */
    public IpsSrcFilesChangedEvent(Map<IIpsSrcFile, AResourceDelta> changedSrcFiles) {
        this.changedSrcFiles = changedSrcFiles;
    }

    /**
     * Getting all changed {@link IIpsSrcFile}.
     * 
     * @return A set containing the change files
     */
    public Set<IIpsSrcFile> getChangedIpsSrcFiles() {
        return changedSrcFiles.keySet();
    }

    /**
     * Getting all the {@link AResourceDelta} that corresponds to any changed {@link IIpsSrcFile}
     * 
     * @return A collection of {@link AResourceDelta}, one for every changed {@link IIpsSrcFile}
     */
    public Collection<AResourceDelta> getResourceDeltas() {
        return changedSrcFiles.values();
    }

    /**
     * Getting the {@link AResourceDelta} for a specified changed {@link IIpsSrcFile}. Returning
     * <code>null</code> if the given {@link IIpsSrcFile} have not changed.
     * 
     * @param ipsSrcFile The changed {@link IIpsSrcFile}
     * @return The {@link AResourceDelta} for the changed file
     */
    public AResourceDelta getResourceDelta(IIpsSrcFile ipsSrcFile) {
        return changedSrcFiles.get(ipsSrcFile);
    }

}
