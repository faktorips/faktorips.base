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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

/**
 * Label provider for IPS object path
 * @author Roman Grutza
 */
public class IpsObjectPathLabelProvider extends LabelProvider {

    /**
     * {@inheritDoc}
     */
    public String getText(Object element) {
        if (element instanceof IIpsSrcFolderEntry) {
            return ((IIpsSrcFolderEntry) element).getSourceFolder().getProjectRelativePath().toString();
        }

        if (element instanceof IFolder) {
            return Messages.IpsObjectPathLabelProvider_0 + ((IFolder) element).getProjectRelativePath().toString();
        }
        return super.getText(element);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        if (element instanceof IIpsSrcFolderEntry)
            return IpsPlugin.getDefault().getImage("IpsPackageFragmentRoot.gif"); //$NON-NLS-1$
        return IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$
    }
}
