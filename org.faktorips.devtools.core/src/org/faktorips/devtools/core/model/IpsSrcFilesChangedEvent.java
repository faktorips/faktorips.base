/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResourceDelta;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class IpsSrcFilesChangedEvent {

    private Map<IIpsSrcFile, IResourceDelta> changedSrcFiles;

    public IpsSrcFilesChangedEvent(Map<IIpsSrcFile, IResourceDelta> changedSrcFiles) {
        this.changedSrcFiles = changedSrcFiles;
    }

    public Set<IIpsSrcFile> getChangedIpsSrcFiles() {
        return changedSrcFiles.keySet();
    }

    public Collection<IResourceDelta> getResourceDeltas() {
        return changedSrcFiles.values();
    }

    public IResourceDelta getResourceDelta(IIpsSrcFile ipsSrcFile) {
        return changedSrcFiles.get(ipsSrcFile);
    }

}
