/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.IpsArchiveEditorInput;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileExternal;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileImmutable;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileOffRoot;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Factory for creating an {@link IIpsSrcFile} based on an {@link IEditorInput}.
 *
 * @implNote Used pattern: parameterized factory method
 *
 * @author Florian Orendi
 */
public class IpsSrcFileFromEditorInputFactory {

    /**
     * Creates an {@link IIpsSrcFile} based on the passed {@link IEditorInput}.
     *
     * @param editorInput The passed {@link IEditorInput}
     * @return The required {@link IIpsSrcFile} or null if the passed {@link IEditorInput} is not
     *             covered by the factory method
     * @throws PartInitException If creating failed (only possible in case of a passed
     *             {@link IStorageEditorInput})
     */
    public IIpsSrcFile createIpsSrcFile(IEditorInput editorInput) throws PartInitException {
        return switch (editorInput) {
            case null -> null;
            case IFileEditorInput fileEditorInput -> createIpsSrcFileFromFileEditorInput(fileEditorInput);
            case IpsArchiveEditorInput archiveEditorInput -> archiveEditorInput.getIpsSrcFile();
            case IStorageEditorInput storageEditorInput -> createIpsSrcFileFromStorageEditorInput(storageEditorInput);
            default -> null;
        };
    }

    /**
     * Creates an immutable {@link IpsSrcFileImmutable} based on a {@link IStorageEditorInput} and
     * sets its mutable counterpart for it, if the file is an import from a JAR.
     *
     * @param input The passed {@link IStorageEditorInput}
     * @return The required {@link IpsSrcFileImmutable}
     * @throws PartInitException If the required {@link IStorage} does not exist
     */
    private IpsSrcFileImmutable createIpsSrcFileFromStorageEditorInput(IStorageEditorInput input)
            throws PartInitException {
        try {
            IStorage storage = input.getStorage();
            IpsSrcFileImmutable ipsSrcFile = new IpsSrcFileImmutable(storage.getName(), storage.getContents());
            if (storage instanceof IJarEntryResource) {
                IPackageFragmentRoot root = ((IJarEntryResource)storage).getPackageFragmentRoot();
                IJavaProject javaProject = ((IJarEntryResource)storage).getPackageFragmentRoot().getJavaProject();
                String projectName = javaProject.getProject().getName();
                IPath fullPath = storage.getFullPath();
                String fileName = storage.getName();

                IIpsProject ipsProject = IIpsModel.get().getIpsProject(projectName);
                IIpsPackageFragmentRoot ipsRoot = ipsProject.findIpsPackageFragmentRoot(root.getElementName());

                setMutableSrcFileForExternalSrcFile(ipsSrcFile, ipsRoot,
                        fullPath.segments(),
                        fileName);
            }
            return ipsSrcFile;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            throw new PartInitException(e.getStatus());
        }
    }

    /**
     * Creates an {@link IIpsSrcFile} based on a {@link IFileEditorInput}.
     * <p>
     * When a {@link IpsSrcFileOffRoot} has been created, also its mutable counterpart is searched
     * and set, if existent.
     *
     * @param input The passed {@link IFileEditorInput}
     * @return The required {@link IIpsSrcFile}
     */
    private IIpsSrcFile createIpsSrcFileFromFileEditorInput(IFileEditorInput input) {
        IFile file = input.getFile();
        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)IIpsModel.get().getIpsElement(Wrappers.wrap(file).as(AFile.class));

        if (ipsSrcFile instanceof IpsSrcFileOffRoot) {
            IPath projectPath = ((IFile)ipsSrcFile.getCorrespondingFile().unwrap()).getFullPath();
            int position = calculateExternalProjectPosition(projectPath);

            // Checks whether there is an existing, corresponding IPS project within the workspace
            if (position < 0) {
                return ipsSrcFile;
            }

            // Extracts the path of the external project.
            projectPath = projectPath.removeFirstSegments(position);

            // True if no file exits within the external project
            if (projectPath.segmentCount() < 2) {
                return ipsSrcFile;
            }

            String projectName = projectPath.segment(0);
            // Gets the relative path of the original project
            IPath projectRelativePath = projectPath.removeFirstSegments(1);
            String[] segments = projectRelativePath.segments();

            IIpsProject ipsProject = IIpsModel.get().getIpsProject(projectName);
            for (var sourceFolderEntry : getSourceFolders(ipsProject)) {
                if (sourceFolderEntry.outputPath.isPrefixOf(projectRelativePath)) {
                    projectRelativePath = projectRelativePath
                            .removeFirstSegments(sourceFolderEntry.outputPath.segmentCount());
                    projectRelativePath = sourceFolderEntry.sourcePath.append(projectRelativePath);
                    break;
                }
            }

            IIpsPackageFragmentRoot root = findIpsPackageFragmentRoot(ipsProject, projectRelativePath);

            String fileName = projectPath.lastSegment();

            setMutableSrcFileForExternalSrcFile((IpsSrcFileOffRoot)ipsSrcFile, root, segments, fileName);
        }

        return ipsSrcFile;
    }

    /**
     * Searches and returns the root folder of the {@link IIpsProject project} by the indicated
     * {@link IPath path}.<br>
     * Returns <code>null</code> if the root doesn't exist or an error occurs during search.
     */
    private static IIpsPackageFragmentRoot findIpsPackageFragmentRoot(IIpsProject ipsProject,
            IPath projectRelativePath) {
        IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : ipsPackageFragmentRoots) {
            IPath rootPath = ((IResource)root.getCorrespondingResource().unwrap()).getProjectRelativePath();
            if (rootPath.isPrefixOf(projectRelativePath)) {
                return root;
            }
        }
        return null;
    }

    private static List<SourceFolder> getSourceFolders(IIpsProject ipsProject) {
        return Arrays.stream(ipsProject.getIpsObjectPath().getSourceFolderEntries())
                .map(srcFolderEntry -> {
                    AFolder outputFolder = srcFolderEntry.getOutputFolderForDerivedJavaFiles();
                    IPath outputPath = ((IResource)outputFolder.unwrap()).getProjectRelativePath();
                    String basePackageName = srcFolderEntry.getBasePackageNameForDerivedJavaClasses();
                    for (String segment : basePackageName.split("\\.")) {
                        outputPath = outputPath.append(segment);
                    }
                    AFolder sourceFolder = srcFolderEntry.getSourceFolder();
                    IPath sourcePath = ((IResource)sourceFolder.unwrap()).getProjectRelativePath();
                    return new SourceFolder(sourcePath, outputPath);
                }).toList();
    }

    /**
     * Calculates a 0-based offset which specifies the position of an external project with a valid
     * project root within the passed path. This method can be used in order to allocate an
     * imported, external project within the currently opened project.
     *
     * @param fullPath The path from the project root to the {@link IIpsSrcFile}
     * @return The position or -1 of no project has been found
     */
    private int calculateExternalProjectPosition(IPath fullPath) {
        IIpsProject[] allProjects = IIpsModel.get().getIpsProjects();
        List<String> projectNames = Arrays.stream(allProjects)
                .map(IIpsProject::getName)
                .collect(Collectors.toList());
        String[] segments = fullPath.segments();
        for (int i = 0; i < segments.length; i++) {
            if (projectNames.contains(segments[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches for a mutable {@link IpsSrcFile} by its name, following the passed path segments,
     * starting from the passed package root. Afterwards, sets the found mutable file for the passed
     * immutable file.
     *
     * @param externalSrcFile The external, immutable {@link IpsSrcFileExternal}
     * @param root The package root to start from
     * @param segments The path segments to follow in order to find the file
     * @param name The name of the searched file
     */
    private void setMutableSrcFileForExternalSrcFile(IpsSrcFileExternal externalSrcFile,
            IIpsPackageFragmentRoot root,
            String[] segments,
            String name) {

        if (root == null) {
            throw new IpsException(
                    "No mutable counterpart of the external file has been found, even if it should exist. " //$NON-NLS-1$
                            + "Reason: the IPS package root does not exist."); //$NON-NLS-1$
        }
        StringBuilder folderName = new StringBuilder();
        for (int i = 1; i < segments.length - 1; i++) {
            if (i > 1) {
                folderName.append(IIpsPackageFragment.SEPARATOR);
            }
            folderName.append(segments[i]);
        }
        IIpsPackageFragment ipsFolder = root.getIpsPackageFragment(folderName.toString());
        if (ipsFolder == null) {
            throw new IpsException(
                    "The mutable counterpart of the external file has not been found, even if it should exist. " //$NON-NLS-1$
                            + "Reason: the IPS folder does not exist."); //$NON-NLS-1$
        }

        IIpsSrcFile mutableSrcFile = ipsFolder.getIpsSrcFile(name);
        if (mutableSrcFile == null) {
            throw new IpsException(
                    "The mutable counterpart of the external file has not been found, even if it should exist. " //$NON-NLS-1$
                            + "Reason: the IPS SrcFile does not exist."); //$NON-NLS-1$
        }

        externalSrcFile.setMutableIpsSrcFile(mutableSrcFile);
    }

    private static record SourceFolder(IPath sourcePath, IPath outputPath) {
    }
}
