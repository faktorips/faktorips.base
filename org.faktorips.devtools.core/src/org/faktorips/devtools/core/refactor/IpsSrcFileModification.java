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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.util.RefactorUtil;

public class IpsSrcFileModification implements IIpsSrcFileModification {

    private final IIpsSrcFile originalIpsSrcFile;

    private final IIpsSrcFile targetIpsSrcFile;

    private final IIpsSrcFileMemento originalContent;

    public static IIpsSrcFileModification createBeforeModification(IIpsSrcFile ipsSrcFile) {
        try {
            return new IpsSrcFileModification(ipsSrcFile, ipsSrcFile.newMemento());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public static IIpsSrcFileModification createBeforeRename(IIpsSrcFile source, IIpsSrcFile target) {
        try {
            return new IpsSrcFileModification(source, target, source.newMemento());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public static IIpsSrcFileModification createRename(IIpsSrcFile original,
            IIpsSrcFile target,
            IIpsSrcFileMemento originalContent) {
        return new IpsSrcFileModification(original, target, originalContent);
    }

    private IpsSrcFileModification(IIpsSrcFile affectedIpsSrcFile, IIpsSrcFileMemento originalContent) {
        this(affectedIpsSrcFile, affectedIpsSrcFile, originalContent);
    }

    private IpsSrcFileModification(IIpsSrcFile originalIpsSrcFile, IIpsSrcFile targetIpsSrcFile,
            IIpsSrcFileMemento originalContent) {
        this.originalIpsSrcFile = originalIpsSrcFile;
        this.targetIpsSrcFile = targetIpsSrcFile;
        this.originalContent = originalContent;
    }

    @Override
    public IIpsSrcFile getTargetIpsSrcFile() {
        return targetIpsSrcFile;
    }

    @Override
    public IIpsSrcFile getOriginalIpsSrcFile() {
        return originalIpsSrcFile;
    }

    @Override
    public IIpsSrcFileMemento getOriginalContent() {
        return originalContent;
    }

    @Override
    public void undo() {
        if (originalIpsSrcFile.exists()) {
            resetChanges(originalIpsSrcFile);
        }
        if (targetIpsSrcFile.exists() && !targetIpsSrcFile.equals(originalIpsSrcFile)) {
            try {
                resetChanges(targetIpsSrcFile);
                if (originalIpsSrcFile.exists()) {
                    originalIpsSrcFile.delete();
                }
                RefactorUtil.moveIpsSrcFile(targetIpsSrcFile, originalIpsSrcFile.getIpsPackageFragment(),
                        originalIpsSrcFile.getName(), new NullProgressMonitor());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private void resetChanges(IIpsSrcFile iIpsSrcFile) {
        iIpsSrcFile.discardChanges();
        try {
            iIpsSrcFile.setMemento(getOriginalContent());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
