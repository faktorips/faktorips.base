/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Label provider for IPS object path
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathLabelProvider extends LabelProvider {

    private ResourceManager resourceManager;

    public IpsObjectPathLabelProvider() {
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        super.dispose();
    }

    @Override
    public String getText(Object element) {
        String text = super.getText(element);

        if (element instanceof IIpsSrcFolderEntry) {
            IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)element;
            text = entry.getIpsProject().getName() + IPath.SEPARATOR
                    + entry.getSourceFolder().getProjectRelativePath().toString();
        } else if (element instanceof IIpsProjectRefEntry) {
            text = ((IIpsProjectRefEntry)element).getReferencedIpsProject().getName();
        } else if (element instanceof IIpsArchiveEntry) {
            IIpsArchiveEntry entry = (IIpsArchiveEntry)element;
            IPath archivePath = entry.getArchiveLocation();
            IFile archiveFileInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(archivePath);

            text = archivePath.lastSegment()
                    + " - " //$NON-NLS-1$
                    + (archiveFileInWorkspace != null ? archiveFileInWorkspace.getParent().getFullPath().toString()
                            : archivePath.removeLastSegments(1).toString());
        } else if (element instanceof IIpsObjectPathEntryAttribute) {
            IIpsObjectPathEntryAttribute att = (IIpsObjectPathEntryAttribute)element;
            String label = getLabelFromAttributeType(att);
            String content = getContentFromAttribute(att);
            return label + ": " + content; //$NON-NLS-1$
        }

        return text;
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IIpsSrcFolderEntry) {
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "IpsPackageFragmentRoot.gif"); //$NON-NLS-1$
            return (Image)resourceManager.get(imageDescriptor);
        } else if (element instanceof IIpsProjectRefEntry) {
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("IpsProject.gif"); //$NON-NLS-1$
            return (Image)resourceManager.get(imageDescriptor);
        } else if (element instanceof IIpsArchiveEntry) {
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("IpsAr.gif"); //$NON-NLS-1$
            return (Image)resourceManager.get(imageDescriptor);
        } else if (element instanceof IIpsObjectPathEntryAttribute) {
            IIpsObjectPathEntryAttribute att = (IIpsObjectPathEntryAttribute)element;
            if (att.isTocPath()) {
                ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                        "TableContents.gif"); //$NON-NLS-1$
                return (Image)resourceManager.get(imageDescriptor);
            }
            if (att.isPackageNameForDerivedSources() || att.isPackageNameForMergableSources()) {
                ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                        "IpsPackageFragment.gif"); //$NON-NLS-1$
                return (Image)resourceManager.get(imageDescriptor);
            }
        }
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("folder_open.gif"); //$NON-NLS-1$ 
        return (Image)resourceManager.get(imageDescriptor);
    }

    private String getLabelFromAttributeType(IIpsObjectPathEntryAttribute attribute) {
        String result = ""; //$NON-NLS-1$

        if (attribute.getType().equals(IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED)
                || attribute.getType().equals(IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED)) {
            result = Messages.IpsObjectPathLabelProvider_base_package_derived;
        }

        if (attribute.getType().equals(IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES)
                || attribute.getType().equals(IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES)) {
            result = Messages.IpsObjectPathLabelProvider_output_folder_derived;
        }

        if (attribute.getType().equals(IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES)
                || attribute.getType().equals(IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES)) {
            result = Messages.IpsObjectPathLabelProvider_output_folder_mergable;
        }

        if (attribute.getType().equals(IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED)
                || attribute.getType().equals(IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED)) {
            result = Messages.IpsObjectPathLabelProvider_package_name_derived;
        }

        if (attribute.getType().equals(IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE)
                || attribute.getType().equals(IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE)) {
            result = Messages.IpsObjectPathLabelProvider_package_name_mergable;
        }

        if (attribute.getType().equals(IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH)) {
            result = Messages.IpsObjectPathLabelProvider_toc_file;
        }

        return result;
    }

    private String getContentFromAttribute(IIpsObjectPathEntryAttribute attribute) {
        String result = Messages.IpsObjectPathLabelProvider_default;

        // get path from IFolder instance
        if (attribute.getValue() instanceof IFolder) {
            IFolder folder = (IFolder)attribute.getValue();
            result = folder.getProjectRelativePath().toOSString();
        }

        if (attribute.getValue() instanceof String) {
            result = (String)attribute.getValue();
        }

        return result;
    }
}
