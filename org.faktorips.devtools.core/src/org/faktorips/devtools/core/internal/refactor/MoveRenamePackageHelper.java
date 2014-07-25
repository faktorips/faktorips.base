/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.refactor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.StringUtil;

/**
 * Helper-Class for move or rename a {@link IIpsPackageFragment}.
 * 
 */
public final class MoveRenamePackageHelper {

    private IIpsPackageFragment originalPackageFragment;

    public MoveRenamePackageHelper(IIpsPackageFragment ipsPackageFragment) {
        this.originalPackageFragment = ipsPackageFragment;
    }

    /**
     * Move a {@link IIpsPackageFragment} to a new package.
     */
    public IpsRefactoringModificationSet movePackageFragment(IIpsPackageFragment targetPackageFragement,
            IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(originalPackageFragment);
        try {
            moveRenamePackageFragement(targetPackageFragement, getTargetPackageName(targetPackageFragement),
                    modificationSet, pm);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return modificationSet;
    }

    /**
     * Rename a {@link IIpsPackageFragment} to a new name.
     */
    public IpsRefactoringModificationSet renamePackageFragment(String newName, IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(originalPackageFragment);
        try {
            moveRenamePackageFragement(originalPackageFragment, newName, modificationSet, pm);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return modificationSet;
    }

    private String getTargetPackageName(IIpsPackageFragment targetPackageFragement) {
        if (targetPackageFragement.isDefaultPackage()) {
            return originalPackageFragment.getLastSegmentName();
        } else if (originalPackageFragment.isDefaultPackage()) {
            return targetPackageFragement.getName();
        } else {
            return targetPackageFragement.getName() + IIpsPackageFragment.SEPARATOR
                    + originalPackageFragment.getLastSegmentName();
        }
    }

    private void moveRenamePackageFragement(IIpsPackageFragment targetPackageFragement,
            String newName,
            IpsRefactoringModificationSet modificationSet,
            IProgressMonitor monitor) throws CoreException {

        IIpsPackageFragmentRoot currTargetRoot = targetPackageFragement.getRoot();
        IIpsPackageFragmentRoot sourceRoot = getSourceRoot();

        // 1) Find all files contained in this folder.
        ArrayList<FileInfo> files = getRelativeFileNames(StringUtils.EMPTY,
                (IFolder)originalPackageFragment.getEnclosingResource());

        // 2) Move them all.
        boolean createSubPackage = false;
        for (FileInfo fileInfos : files) {

            IIpsPackageFragment targetPackage = currTargetRoot.getIpsPackageFragment(buildPackageName(
                    StringUtils.EMPTY, newName, fileInfos.getPath()));

            if (targetPackage.getParentIpsPackageFragment().equals(originalPackageFragment)) {
                createSubPackage = true;
            }

            createPackageFragmentIfNotExist(currTargetRoot, targetPackage, monitor);

            IIpsPackageFragment sourcePackage = sourceRoot.getIpsPackageFragment(buildPackageName(
                    originalPackageFragment.getName(), StringUtils.EMPTY, fileInfos.getPath()));
            IIpsSrcFile sourceFile = sourcePackage.getIpsSrcFile(fileInfos.getFileName());
            if (sourceFile != null) {
                modificationSet.addBeforeChanged(sourceFile);
                IIpsObject ipsObject = sourceFile.getIpsObject();
                IIpsSrcFile targetFile = targetPackage.getIpsSrcFile(fileInfos.getFileName());
                String targetName = targetPackage.getName() + '.' + targetFile.getIpsObjectName();
                RefactoringStatus status = moveIpsObject(ipsObject, targetName, currTargetRoot, monitor);
                if (!status.isOK()) {
                    // TODO Hier gibt es Meldungen, wohin damit?
                    System.out.println("Status NOK:" + sourceFile.getName() + "\n" + status.toString());
                }
            } else {
                moveOtherFiles(sourcePackage, targetPackage, fileInfos, monitor);
            }
        }

        // 3) Remove remaining folders(only if no sub package was to be created).
        if (!(createSubPackage)) {
            if (isSourceFolderEmpty(((IFolder)originalPackageFragment.getEnclosingResource()))) {
                originalPackageFragment.getEnclosingResource().delete(true, monitor);
            }
        }
    }

    private IIpsPackageFragmentRoot getSourceRoot() {
        IIpsPackageFragment sourceParent = originalPackageFragment.getParentIpsPackageFragment();
        IIpsPackageFragmentRoot sourceRoot;
        if (sourceParent != null) {
            sourceRoot = sourceParent.getRoot();
        } else {
            sourceRoot = originalPackageFragment.getRoot();
        }
        return sourceRoot;
    }

    private boolean isSourceFolderEmpty(IFolder folder) throws CoreException {
        IResource[] members = folder.members();
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                return false;
            } else if (member.getType() == IResource.FOLDER) {
                if (!isSourceFolderEmpty((IFolder)member)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createPackageFragmentIfNotExist(IIpsPackageFragmentRoot currTargetRoot,
            IIpsPackageFragment targetPackage,
            IProgressMonitor monitor) throws CoreException {
        if (!targetPackage.exists()) {
            currTargetRoot.createPackageFragment(targetPackage.getName(), true, monitor);
        }
    }

    // We do not have a IIpsSrcFile, so move the file as resource operation.
    private void moveOtherFiles(IIpsPackageFragment sourcePackage,
            IIpsPackageFragment targetPackage,
            FileInfo fileInfos,
            IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)sourcePackage.getEnclosingResource();
        IFile rawFile = folder.getFile(fileInfos.getFileName());
        IPath destination = ((IFolder)targetPackage.getCorrespondingResource()).getFullPath().append(
                fileInfos.getFileName());
        if (rawFile.exists()) {
            rawFile.move(destination, true, monitor);
        } else {
            if (!((IFolder)targetPackage.getCorrespondingResource()).getFolder(fileInfos.getFileName()).exists()) {
                IFolder rawFolder = folder.getFolder(fileInfos.getFileName());
                rawFolder.move(destination, true, monitor);
            }
        }
    }

    /**
     * Recursively descend the path down the folders and collect all files found in the given list.
     */
    private ArrayList<FileInfo> getRelativeFileNames(String path, IFolder folder) throws CoreException {
        ArrayList<FileInfo> files = new ArrayList<FileInfo>();
        IResource[] members = folder.members();

        if (members.length == 0) {
            files.add(new FileInfo(StringUtil.getPackageName(path), StringUtil.unqualifiedName(path)));
        }

        for (IResource member : members) {
            if (member.getType() == IResource.FOLDER) {
                String pathName = path.length() > 0 ? (path + IIpsPackageFragment.SEPARATOR + member.getName())
                        : member.getName();
                files.addAll(getRelativeFileNames(pathName, (IFolder)member));
            } else if (member.getType() == IResource.FILE) {
                files.add(new FileInfo(path, member.getName()));
            }
        }
        return files;
    }

    /**
     * Builds a package name by concatenating the given parts with dots. Each one of the three parts
     * can be empty.
     */
    private String buildPackageName(String prefix, String middle, String postfix) {
        String result = prefix;

        if (StringUtils.isNotEmpty(result) && StringUtils.isNotEmpty(middle)) {
            result += IIpsPackageFragment.SEPARATOR;
        }

        if (StringUtils.isNotEmpty(middle)) {
            result += middle;
        }

        if (StringUtils.isNotEmpty(result) && StringUtils.isNotEmpty(postfix)) {
            result += IIpsPackageFragment.SEPARATOR;
        }

        if (StringUtils.isNotEmpty(postfix)) {
            result += postfix;
        }
        return result;
    }

    private RefactoringStatus moveIpsObject(IIpsObject ipsObject,
            String targetName,
            IIpsPackageFragmentRoot targetRoot,
            IProgressMonitor pm) throws CoreException {

        IIpsPackageFragmentRoot root = targetRoot;
        if (root == null) {
            root = ipsObject.getIpsPackageFragment().getRoot();
        }
        IIpsPackageFragment targetIpsPackageFragment = root.getIpsPackageFragment(QNameUtil.getPackageName(targetName));
        IIpsRefactoring ipsMoveRefactoring = IpsPlugin.getIpsRefactoringFactory().createMoveRefactoring(ipsObject,
                targetIpsPackageFragment);
        PerformRefactoringOperation operation = new PerformRefactoringOperation(ipsMoveRefactoring.toLtkRefactoring(),
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, pm);
        return operation.getConditionStatus();
    }

    /**
     * Returns the affected {@link IIpsSrcFile IIpsSrcFiles}.
     */
    public Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        Set<IIpsSrcFile> affectedFiles = new HashSet<IIpsSrcFile>();
        try {
            ArrayList<FileInfo> files = getRelativeFileNames(StringUtils.EMPTY,
                    (IFolder)originalPackageFragment.getEnclosingResource());
            IIpsPackageFragmentRoot sourceRoot = getSourceRoot();
            for (FileInfo fileInfos : files) {
                IIpsPackageFragment sourcePackage = sourceRoot.getIpsPackageFragment(buildPackageName(
                        originalPackageFragment.getName(), StringUtils.EMPTY, fileInfos.getPath()));
                IIpsSrcFile sourceFile = sourcePackage.getIpsSrcFile(fileInfos.getFileName());
                if (sourceFile != null && sourceFile.exists()) {
                    affectedFiles.add(sourceFile);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return affectedFiles;
    }

    /**
     * Checks the initial conditions.
     */
    public void checkInitialConditions(RefactoringStatus status) throws CoreException {
        if (!packageValid(originalPackageFragment)) {
            status.addFatalError(NLS.bind(Messages.MoveRenamePackageHelper_errorPackageContainsInvalidObjects,
                    originalPackageFragment.getName()));
        }

    }

    private boolean packageValid(IIpsPackageFragment fragment) throws CoreException {
        for (IIpsPackageFragment childFragment : fragment.getChildIpsPackageFragments()) {
            if (!(packageValid(childFragment))) {
                return false;
            }
        }
        for (IIpsSrcFile ipsSrcFile : fragment.getIpsSrcFiles()) {
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            if (!(ipsObject.isValid(ipsSrcFile.getIpsProject()))) {
                return false;
            }
        }
        return true;
    }

    public void checkFinalConditions(RefactoringStatus status) {
        // TODO hier auch packageValid Testen mit dem Target?
    }

    private static class FileInfo {

        private final String path;
        private final String filename;

        public FileInfo(String path, String filename) {
            this.path = path;
            this.filename = filename;
        }

        public String getPath() {
            return this.path;
        }

        public String getFileName() {
            return this.filename;
        }

        @Override
        public String toString() {
            return "FileInfo [path=" + path + ", filename=" + filename + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
}
