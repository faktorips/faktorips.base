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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
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
        String text = super.getText(element);
        
        if (element instanceof IIpsSrcFolderEntry) {
            IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry) element;
            text = entry.getIpsProject().getName() + IPath.SEPARATOR + 
                   entry.getSourceFolder().getProjectRelativePath().toString();
        }
        else if (element instanceof IIpsProjectRefEntry) {
            text =  ((IIpsProjectRefEntry) element).getReferencedIpsProject().getName();
        }
        else if (element instanceof IIpsArchiveEntry) {
            IIpsArchiveEntry entry = (IIpsArchiveEntry) element;
            text = entry.getArchiveFile().getName() + " - " + 
                   entry.getIpsProject().getName() + IPath.SEPARATOR + entry.getArchiveFile().getProjectRelativePath().toOSString();
        }
        else if (element instanceof IIpsSrcFolderEntryAttribute) {
            IIpsSrcFolderEntryAttribute att = (IIpsSrcFolderEntryAttribute) element;
            String label = getLabelFromAttributeType(att);
            String content = getContentFromAttribute(att);
            return label + ": " + content;
        }
        
        return text;
    }


    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        Image image = IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$ 
        
        if (element instanceof IIpsSrcFolderEntry)
            image = IpsPlugin.getDefault().getImage("IpsPackageFragmentRoot.gif"); //$NON-NLS-1$
        else if (element instanceof IIpsProjectRefEntry) {
            image = IpsPlugin.getDefault().getImage("IpsProject.gif"); //$NON-NLS-1$
        }
        else if (element instanceof IIpsArchiveEntry) {
            image = IpsPlugin.getDefault().getImage("IpsAr.gif"); //$NON-NLS-1$
        }
        else if (element instanceof IIpsSrcFolderEntryAttribute) {
            IIpsSrcFolderEntryAttribute att = (IIpsSrcFolderEntryAttribute) element;
            if (att.getName().equals(IIpsSrcFolderEntryAttribute.SPECIFIC_TOC_PATH) ) {
                image = IpsPlugin.getDefault().getImage("TableContents.gif"); //$NON-NLS-1$
            }
        }

        return image;
    }

    private String getLabelFromAttributeType(IIpsSrcFolderEntryAttribute attribute) {
        String result = "";
        
        if (attribute.getName().equals( IIpsSrcFolderEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED)
                || attribute.getName().equals( IIpsSrcFolderEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED))
            result = "Base package derived";
        
        if (attribute.getName().equals( IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES)
                || attribute.getName().equals( IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES))
            result = "Output folder derived sources";
        
        if (attribute.getName().equals( IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES)
                || attribute.getName().equals( IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES))
            result = "Output folder mergable sources";
        
        if (attribute.getName().equals( IIpsSrcFolderEntryAttribute.SPECIFIC_TOC_PATH))
            result = "Table of contents";
        
        return result;
    }

    private String getContentFromAttribute(IIpsSrcFolderEntryAttribute attribute) {
        String result = "(default)";
        
        // get path from IFolder instance
        if (attribute.getValue() instanceof IFolder) {
            IFolder folder = (IFolder) attribute.getValue();
            result = folder.getProjectRelativePath().toOSString();
        }
        
        if (attribute.getValue() instanceof String) {
            result = (String) attribute.getValue();
        }

        return result;
    }
}
