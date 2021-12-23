/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.devtools.model.util.RefactorUtil;
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
            moveRenamePackageFragement(targetPackageFragement, getResultingPackageName(targetPackageFragement),
                    modificationSet, pm);
        } catch (CoreRuntimeException e) {
            modificationSet.undo();
            throw e;
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
        } catch (CoreRuntimeException e) {
            modificationSet.undo();
            throw e;
        }
        return modificationSet;
    }

    /**
     * Calculates the name of the package after it is moved or renamed.
     * 
     * @param targetPackageFragement the package the original package should be moved into
     * @return the resulting name of the moved package fragment
     */
    private String getResultingPackageName(IIpsPackageFragment targetPackageFragement) {
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
            IProgressMonitor monitor) throws CoreRuntimeException {

        IIpsPackageFragmentRoot currTargetRoot = targetPackageFragement.getRoot();
        IIpsPackageFragmentRoot sourceRoot = getSourceRoot();

        // 1) Find all files contained in this folder.
        ArrayList<FileInfo> files = getRelativeFileNames(StringUtils.EMPTY,
                (AFolder)originalPackageFragment.getEnclosingResource());

        // 2) Move them all.
        boolean createSubPackage = false;
        for (FileInfo fileInfos : files) {

            IIpsPackageFragment targetPackage = currTargetRoot
                    .getIpsPackageFragment(buildPackageName(StringUtils.EMPTY, newName, fileInfos.getPath()));

            if (targetPackage == null) {
                continue;
            }
            if (targetPackage.getParentIpsPackageFragment().equals(originalPackageFragment)) {
                createSubPackage = true;
            }

            createPackageFragmentIfNotExist(currTargetRoot, targetPackage, monitor);

            IIpsPackageFragment sourcePackage = sourceRoot.getIpsPackageFragment(
                    buildPackageName(originalPackageFragment.getName(), StringUtils.EMPTY, fileInfos.getPath()));
            IIpsSrcFile sourceFile = sourcePackage.getIpsSrcFile(fileInfos.getFileName());
            if (sourceFile != null) {
                modificationSet.addBeforeChanged(sourceFile);
                IIpsObject ipsObject = sourceFile.getIpsObject();
                IIpsSrcFile targetFile = targetPackage.getIpsSrcFile(fileInfos.getFileName());
                String targetName = targetPackage.getName() + '.' + targetFile.getIpsObjectName();
                RefactoringStatus status = moveIpsObject(ipsObject, targetName, currTargetRoot, monitor);
                if (status.hasError()) {
                    IpsPlugin.log(new IpsStatus(NLS.bind("Error moving file {0}.\n{1}", sourceFile.getName(), //$NON-NLS-1$
                            status.toString())));
                }
            } else {
                try {
                    moveOtherFiles(sourcePackage, targetPackage, fileInfos, monitor);
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }

        // 3) Remove remaining folders(only if no sub package was to be created).
        if (!(createSubPackage)) {
            if (isSourceFolderEmpty(((AFolder)originalPackageFragment.getEnclosingResource()))) {
                originalPackageFragment.getEnclosingResource().delete(monitor);
            }
        }

        // 4) fix .sortorder
        IIpsPackageFragment originalParentIpsPackageFragment = originalPackageFragment.getParentIpsPackageFragment();
        if (originalParentIpsPackageFragment != null) {
            Comparator<IIpsElement> childOrderComparator = originalParentIpsPackageFragment.getChildOrderComparator();
            if (childOrderComparator instanceof DefinedOrderComparator) {
                RefactorUtil.updateSortOrder(originalParentIpsPackageFragment, originalPackageFragment,
                        targetPackageFragement.getParentIpsPackageFragment(), currTargetRoot.getIpsPackageFragment(
                                buildPackageName(StringUtils.EMPTY, newName, StringUtils.EMPTY)));
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

    /**
     * Checks recursively if the {@link IFolder} is empty.
     */
    private boolean isSourceFolderEmpty(AFolder folder) throws CoreRuntimeException {
        for (AResource member : folder) {
            if (member.getType() == AResourceType.FILE) {
                return false;
            } else if (member.getType() == AResourceType.FOLDER) {
                if (!isSourceFolderEmpty((AFolder)member)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createPackageFragmentIfNotExist(IIpsPackageFragmentRoot currTargetRoot,
            IIpsPackageFragment targetPackage,
            IProgressMonitor monitor) throws CoreRuntimeException {
        if (!targetPackage.exists()) {
            currTargetRoot.createPackageFragment(targetPackage.getName(), true, monitor);
        }
    }

    /**
     * We do not have a IIpsSrcFile, so move the file as resource operation.
     */
    private void moveOtherFiles(IIpsPackageFragment sourcePackage,
            IIpsPackageFragment targetPackage,
            FileInfo fileInfos,
            IProgressMonitor monitor) throws CoreException {
        AFolder folder = (AFolder)sourcePackage.getEnclosingResource();
        IFile rawFile = folder.getFile(fileInfos.getFileName()).unwrap();
        IPath destination = ((IFolder)targetPackage.getCorrespondingResource()).getFullPath()
                .append(fileInfos.getFileName());
        if (rawFile.exists()) {
            rawFile.move(destination, true, monitor);
        } else {
            if (!((AFolder)targetPackage.getCorrespondingResource()).getFolder(fileInfos.getFileName()).exists()) {
                IFolder rawFolder = folder.getFolder(fileInfos.getFileName()).unwrap();
                rawFolder.move(destination, true, monitor);
            }
        }
    }

    /**
     * Recursively descend the path down the folders and collect all files found in the given list.
     */
    private ArrayList<FileInfo> getRelativeFileNames(String path, AFolder folder) throws CoreRuntimeException {
        ArrayList<FileInfo> files = new ArrayList<>();
        SortedSet<? extends AResource> members = folder.getMembers();

        if (members.size() == 0) {
            files.add(new FileInfo(StringUtil.getPackageName(path), StringUtil.unqualifiedName(path)));
        }

        for (AResource member : members) {
            if (member.getType() == AResourceType.FOLDER) {
                String pathName = path.length() > 0 ? (path + IIpsPackageFragment.SEPARATOR + member.getName())
                        : member.getName();
                files.addAll(getRelativeFileNames(pathName, (AFolder)member));
            } else if (member.getType() == AResourceType.FILE) {
                if (member.getName().equals(IIpsPackageFragment.SORT_ORDER_FILE_NAME)) {
                    // put .sortorder first, because otherwise it would be destroyed by moving the
                    // contained files away
                    files.add(0, new FileInfo(path, member.getName()));
                } else {
                    files.add(new FileInfo(path, member.getName()));
                }
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

    /**
     * Move the {@link IIpsObject} to the target
     */
    private RefactoringStatus moveIpsObject(IIpsObject ipsObject,
            String targetName,
            IIpsPackageFragmentRoot targetRoot,
            IProgressMonitor pm) throws CoreRuntimeException {

        IIpsPackageFragmentRoot root = targetRoot;
        if (root == null) {
            root = ipsObject.getIpsPackageFragment().getRoot();
        }
        IIpsPackageFragment targetIpsPackageFragment = root.getIpsPackageFragment(QNameUtil.getPackageName(targetName));
        IIpsRefactoring ipsMoveRefactoring = IpsPlugin.getIpsRefactoringFactory().createMoveRefactoring(ipsObject,
                targetIpsPackageFragment);
        PerformRefactoringOperation operation = new PerformRefactoringOperation(ipsMoveRefactoring.toLtkRefactoring(),
                CheckConditionsOperation.ALL_CONDITIONS);
        Abstractions.getWorkspace().run(operation, pm);
        return operation.getConditionStatus();
    }

    /**
     * Returns the affected {@link IIpsSrcFile IIpsSrcFiles}.
     */
    public Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        Set<IIpsSrcFile> affectedFiles = new HashSet<>();
        ArrayList<FileInfo> files = getRelativeFileNames(StringUtils.EMPTY,
                (AFolder)originalPackageFragment.getEnclosingResource());
        IIpsPackageFragmentRoot sourceRoot = getSourceRoot();
        for (FileInfo fileInfos : files) {
            IIpsPackageFragment sourcePackage = sourceRoot.getIpsPackageFragment(
                    buildPackageName(originalPackageFragment.getName(), StringUtils.EMPTY, fileInfos.getPath()));
            IIpsSrcFile sourceFile = sourcePackage.getIpsSrcFile(fileInfos.getFileName());
            if (sourceFile != null && sourceFile.exists()) {
                affectedFiles.add(sourceFile);
            }
        }
        return affectedFiles;
    }

    /**
     * Checks the initial conditions.
     */
    public void checkInitialConditions(RefactoringStatus status) throws CoreRuntimeException {
        if (status.isOK()) {
            if (!packageValid(originalPackageFragment)) {
                status.addFatalError(NLS.bind(Messages.MoveRenamePackageHelper_errorPackageContainsInvalidObjects,
                        originalPackageFragment.getName()));
            }
        }
    }

    /**
     * Returns <code>true</code> if all found {@link IIpsObject IIpsObjects} are valid.
     */
    private boolean packageValid(IIpsPackageFragment fragment) throws CoreRuntimeException {
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

    /**
     * Checks the final conditions on all AffectedIpsSourceFiles. It's necessary to create the
     * target packages. After the check the target package will be deleted.
     */
    public void checkFinalConditions(IIpsPackageFragment targetIpsPackageFragment,
            RefactoringStatus status,
            IProgressMonitor pm) throws CoreRuntimeException {
        // no errors so far
        if (targetIpsPackageFragment != null && status.isOK()) {
            try {
                createPackageFragmentIfNotExist(targetIpsPackageFragment.getRoot(), targetIpsPackageFragment, pm);
                Set<IIpsSrcFile> affectedIpsSrcFiles = getAffectedIpsSrcFiles();
                for (IIpsSrcFile ipsSrcFile : affectedIpsSrcFiles) {
                    checkFinalConditionsOnIpsSrcFile(ipsSrcFile, targetIpsPackageFragment, status, pm);
                }
            } finally {
                if (targetIpsPackageFragment.exists()) {
                    targetIpsPackageFragment.getEnclosingResource().delete(pm);
                }
            }
        }
    }

    /**
     * Checks the new target {@link IIpsPackageFragment} is not valid or already exists.
     */
    public void validateUserInput(IIpsPackageFragment newPackageFragment, RefactoringStatus status) {
        if (status.isOK()) {
            if (newPackageFragment == null) {
                status.addFatalError(Messages.MoveRenamePackageHelper_errorTargetPackageNotValid);
                return;
            } else if (newPackageFragment.exists()) {
                status.addFatalError(NLS.bind(Messages.MoveRenamePackageHelper_errorPackageAlreadyContains,
                        newPackageFragment.getName()));
                return;
            }
            if (!MoveOperation.canMovePackages(new Object[] { originalPackageFragment }, newPackageFragment)) {
                status.addFatalError(NLS.bind(Messages.MoveRenamePackageHelper_errorMessage_disallowMoveIntoSubPackage,
                        originalPackageFragment.getName()));
            }
        }
    }

    /**
     * Calling the refactoring processor for {@link IIpsObject}. The source file of the
     * {@link IIpsObject} will be moved this early. Based on that new source file and on the moved
     * {@link IIpsObject} validation is performed. After validation the file is moved back to
     * perform the real model refactoring.
     */
    private void checkFinalConditionsOnIpsSrcFile(IIpsSrcFile originalFile,
            IIpsPackageFragment targetIpsPackageFragment,
            RefactoringStatus status,
            IProgressMonitor pm) throws CoreRuntimeException {
        createSubPackageFragmentIfNotExist(originalFile, targetIpsPackageFragment, pm);
        IIpsProcessorBasedRefactoring moveRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createMoveRefactoring(originalFile.getIpsObject(), targetIpsPackageFragment);
        try {
            status.merge(moveRefactoring.checkFinalConditions(pm));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void createSubPackageFragmentIfNotExist(IIpsSrcFile originalFile,
            IIpsPackageFragment targetIpsPackageFragment,
            IProgressMonitor pm) throws CoreRuntimeException {
        if (!originalPackageFragment.equals(originalFile.getIpsPackageFragment())) {
            IIpsPackageFragment targetPackage = targetIpsPackageFragment.getRoot()
                    .getIpsPackageFragment(buildPackageName(StringUtils.EMPTY, targetIpsPackageFragment.getName(),
                            originalFile.getIpsPackageFragment().getLastSegmentName()));
            createPackageFragmentIfNotExist(targetIpsPackageFragment.getRoot(), targetPackage, pm);
        }
    }

    /**
     * Returns <code>false</code> because the {@link IpsPackageFragment} is not a file and the
     * {@link IpsSrcFile} inside checks this property self.
     */
    public boolean isSourceFilesSavedRequired() {
        return false;
    }

    /**
     * Stores the path and the filename
     */
    private static final class FileInfo {

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
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("FileInfo [path="); //$NON-NLS-1$
            stringBuilder.append(path);
            stringBuilder.append(", filename="); //$NON-NLS-1$
            stringBuilder.append(filename);
            stringBuilder.append("]"); //$NON-NLS-1$
            return stringBuilder.toString();
        }
    }
}
