/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class IpsSrcFileModificationSet {

    private final Set<IIpsSrcFileModification> modifications;

    public IpsSrcFileModificationSet() {
        this.modifications = new HashSet<IIpsSrcFileModification>();
    }

    public Set<IIpsSrcFileModification> getModifications() {
        return modifications;
    }

    public void append(IpsSrcFileModificationSet ipsSrcFileModificationSet) {
        modifications.addAll(ipsSrcFileModificationSet.getModifications());
    }

    public void add(IIpsSrcFileModification modification) {
        modifications.add(modification);
    }

    public boolean containsModification(IIpsSrcFile ipsSrcFile) {
        return getModification(ipsSrcFile) != null;
    }

    private IIpsSrcFileModification getModification(IIpsSrcFile ipsSrcFile) {
        for (IIpsSrcFileModification ipsSrcFileModification : modifications) {
            if (ipsSrcFileModification.getOriginalIpsSrcFile().equals(ipsSrcFile)) {
                return ipsSrcFileModification;
            }
        }
        return null;
    }

    public void addBeforeChanged(IIpsSrcFile ipsSrcFile) {
        if (!containsModification(ipsSrcFile)) {
            add(IpsSrcFileModification.createBeforeModification(ipsSrcFile));
        }
    }

    public void addRenameModification(IIpsSrcFile original, IIpsSrcFile target) {
        IIpsSrcFileModification modification = getModification(original);
        IpsSrcFileModification ipsSrcFileModification = (IpsSrcFileModification)modification;
        if (modification != null) {
            add(IpsSrcFileModification.createRename(original, target, ipsSrcFileModification.getOriginalContent()));
        } else {
            add(IpsSrcFileModification.createBeforeRename(original, target));
        }
    }

}
