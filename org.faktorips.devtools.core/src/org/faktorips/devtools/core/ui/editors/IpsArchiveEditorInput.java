/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEditorInput implements IEditorInput {

    private IIpsSrcFile srcFile;
    
    public IpsArchiveEditorInput(IIpsSrcFile fileFromArchive) {
        ArgumentCheck.notNull(fileFromArchive);
        this.srcFile = fileFromArchive;
    }
    
    /**
     * Returns the ips source file this is an editor input from. The source file comes from an ips archive.
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
        IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
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
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

}
