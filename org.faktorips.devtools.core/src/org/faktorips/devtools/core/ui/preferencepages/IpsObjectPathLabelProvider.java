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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
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
            text = ((IIpsProjectRefEntry) element).getReferencedIpsProject().getName();
        }
        else if (element instanceof IIpsArchiveEntry) {
            IIpsArchiveEntry entry = (IIpsArchiveEntry) element;
            IPath archivePath = entry.getArchivePath();
            IFile archiveFileInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(archivePath);
			boolean isExternal = (archiveFileInWorkspace == null);
            
			text = archivePath.lastSegment() + " - " + 
                (isExternal ? archivePath.toOSString()
                		    : archiveFileInWorkspace.getFullPath().toOSString());
        }
        else if (element instanceof IIpsObjectPathEntryAttribute) {
            IIpsObjectPathEntryAttribute att = (IIpsObjectPathEntryAttribute) element;
            String label = getLabelFromAttributeType(att);
            String content = getContentFromAttribute(att);
            return label + ": " + content; //$NON-NLS-1$
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
        else if (element instanceof IIpsObjectPathEntryAttribute) {
            IIpsObjectPathEntryAttribute att = (IIpsObjectPathEntryAttribute) element;
            if (att.isTocPath()) {
                image = IpsPlugin.getDefault().getImage("TableContents.gif"); //$NON-NLS-1$
            }
            if (att.isPackageNameForDerivedSources() || att.isPackageNameForMergableSources()) {
                image = IpsPlugin.getDefault().getImage("IpsPackageFragment.gif"); //$NON-NLS-1$
            }
        }

        return image;
    }

    private String getLabelFromAttributeType(IIpsObjectPathEntryAttribute attribute) {
        String result = ""; //$NON-NLS-1$
        
        if (attribute.getType().equals( IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED)
                || attribute.getType().equals( IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED))
            result = Messages.IpsObjectPathLabelProvider_base_package_derived;
        
        if (attribute.getType().equals( IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES)
                || attribute.getType().equals( IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES))
            result = Messages.IpsObjectPathLabelProvider_output_folder_derived;
        
        if (attribute.getType().equals( IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES)
                || attribute.getType().equals( IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES))
            result = Messages.IpsObjectPathLabelProvider_output_folder_mergable;
        
        if (attribute.getType().equals( IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED)
                || attribute.getType().equals( IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED))
            result = Messages.IpsObjectPathLabelProvider_package_name_derived;
        
        if (attribute.getType().equals( IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE)
                || attribute.getType().equals( IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE))
            result = Messages.IpsObjectPathLabelProvider_package_name_mergable;
        
        if (attribute.getType().equals( IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH))
            result = Messages.IpsObjectPathLabelProvider_toc_file;
        
        return result;
    }

    private String getContentFromAttribute(IIpsObjectPathEntryAttribute attribute) {
        String result = Messages.IpsObjectPathLabelProvider_default;
        
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
