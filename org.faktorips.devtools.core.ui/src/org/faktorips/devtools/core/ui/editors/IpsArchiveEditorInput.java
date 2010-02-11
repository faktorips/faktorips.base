/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEditorInput extends PlatformObject implements IEditorInput {

    private IIpsSrcFile srcFile;

    public IpsArchiveEditorInput(IIpsSrcFile fileFromArchive) {
        ArgumentCheck.notNull(fileFromArchive);
        srcFile = fileFromArchive;
    }

    /**
     * Returns the ips source file this is an editor input from. The source file comes from an ips
     * archive.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return srcFile;
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor() {
        IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
        return registry.getImageDescriptor(StringUtil.getFileExtension(srcFile.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return srcFile.getName();
    }

    /**
     * {@inheritDoc}
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getToolTipText() {
        return srcFile.getQualifiedNameType().getName();
    }

    /**
     * {@inheritDoc}
     * 
     * returns the equality of the underlying <code>IpsSrcFile</code> resources. Note: the default
     * UI functionality uses this method to decide if a new editor should be opened or an already
     * open editor will be reactivated.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IpsArchiveEditorInput)) {
            return false;
        }
        IpsArchiveEditorInput other = (IpsArchiveEditorInput)obj;
        return srcFile.equals(other.getIpsSrcFile());
    }
}
