/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES;
import static org.faktorips.devtools.core.ui.preferencepages.IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH;

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
import org.faktorips.devtools.abstraction.eclipse.mapping.PathMapping;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Label provider for IPS object path
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

        return switch (element) {
            case IIpsSrcFolderEntry srcFolderEntry -> srcFolderEntry.getIpsProject().getName() + IPath.SEPARATOR
                    + srcFolderEntry.getSourceFolder().getProjectRelativePath().toString();
            case IIpsProjectRefEntry refEntry -> refEntry.getReferencedIpsProject().getName();
            case IIpsArchiveEntry archiveEntry -> {
                IPath archivePath = PathMapping.toEclipsePath(archiveEntry.getArchiveLocation());
                IFile archiveFileInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(archivePath);

                yield archivePath == null ? "/" //$NON-NLS-1$
                        : archivePath.lastSegment()
                                + " - " //$NON-NLS-1$
                                + (archiveFileInWorkspace != null
                                        ? archiveFileInWorkspace.getParent().getFullPath().toString()
                                        : archivePath.removeLastSegments(1).toString());
            }
            case IIpsObjectPathEntryAttribute att -> getLabelFromAttributeType(att) + ": " //$NON-NLS-1$
                    + getContentFromAttribute(att);
            default -> text;
        };
    }

    @Override
    public Image getImage(Object element) {
        ImageDescriptor imageDescriptor = switch (element) {
            case IIpsSrcFolderEntry $ -> IIpsDecorators.getDefaultImageDescriptor(IpsPackageFragmentRoot.class);
            case IIpsProjectRefEntry $ -> IIpsDecorators.getDefaultImageDescriptor(IpsProject.class);
            case IIpsArchiveEntry $ -> IIpsDecorators.getDefaultImageDescriptor(LibraryIpsPackageFragmentRoot.class);
            case IIpsObjectPathEntryAttribute att when att.isTocPath() -> IIpsDecorators
                    .getDefaultImageDescriptor(TableContents.class);
            case IIpsObjectPathEntryAttribute att when (att.isPackageNameForDerivedSources()
                    || att.isPackageNameForMergableSources()) -> IIpsDecorators
                            .getDefaultImageDescriptor(AbstractIpsPackageFragment.class);
            default -> IpsUIPlugin.getImageHandling().createImageDescriptor("folder_open.gif"); //$NON-NLS-1$
        };
        return resourceManager.get(imageDescriptor);
    }

    private String getLabelFromAttributeType(IIpsObjectPathEntryAttribute attribute) {
        return switch (attribute.getType()) {
            case DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES, SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES -> Messages.IpsObjectPathLabelProvider_output_folder_derived;
            case DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES, SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES -> Messages.IpsObjectPathLabelProvider_output_folder_mergable;
            case DEFAULT_BASE_PACKAGE_DERIVED, SPECIFIC_BASE_PACKAGE_DERIVED -> Messages.IpsObjectPathLabelProvider_package_name_derived;
            case DEFAULT_BASE_PACKAGE_MERGABLE, SPECIFIC_BASE_PACKAGE_MERGABLE -> Messages.IpsObjectPathLabelProvider_package_name_mergable;
            case SPECIFIC_TOC_PATH -> Messages.IpsObjectPathLabelProvider_toc_file;
            default -> IpsStringUtils.EMPTY;
        };
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
