/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.refactor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.ArchiveIpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.ArchiveIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.StringUtil;

/**
 * Moves (and renames) product components.
 * 
 * @author Thorsten Guenther
 */
public class MoveOperation implements IRunnableWithProgress {

    /**
     * All objects to move/rename.
     */
    private Object[] sourceObjects;

    /**
     * The new (package-qualified) names for the objects to move/rename.
     */
    private String[] targetNames;

    /**
     * If product components are moved/renamed it is also possible to provide new runtime ids.
     */
    private List<String> newRuntimeIds = null;

    /**
     * The ips package fragment root defines the ips src root where to place the moved objects.
     */
    private IIpsPackageFragmentRoot targetRoot;

    private IProject targetProject;

    /**
     * Creates a new operation to move or rename the given product component. After the run-method
     * has returned, all references of other products to the moved/renamed one are updated to refer
     * to the new name.
     * 
     * @param source The product to rename.
     * @param target The new location/name.
     * 
     * @throws CoreException If the source does not exist or is modified (if a product component) or
     *             if the target already exists.
     */
    public MoveOperation(IProductCmpt source, String target) throws CoreException {
        this(new IIpsElement[] { source }, new String[] { target });
    }

    /**
     * Creates a new operation to move or rename the given product component. After the run-method
     * has returned, all references of other products to the moved/renamed one are updated to refer
     * to the new name.
     * 
     * @param source The product to rename.
     * @param target The new location/name.
     * @throws CoreException If the source does not exist or is modified (if a product component) or
     *             if the target already exists.
     */
    public MoveOperation(IProductCmpt source, String target, String newRuntimeId) throws CoreException {
        this(source, target);
        newRuntimeIds = new ArrayList<String>(1);
        newRuntimeIds.add(newRuntimeId);
    }

    /**
     * Move all the given package fragments and product components to the given targets.
     * 
     * @param sources An array containing <code>IProductCmpt</code> or
     *            <code>IIpsPackageFragement</code> objects to move.
     * @param targets An array of the new, qualified names for the objects to move. The names are
     *            object-names, not filenames, so do not append any file extension to the name.
     * @throws CoreException if the both arrays are not of the same length.
     */
    public MoveOperation(IIpsElement[] sources, String[] targets) throws CoreException {
        if (sources.length != targets.length) {
            IpsStatus status = new IpsStatus("Number of source- and target-objects is not the same."); //$NON-NLS-1$
            throw new CoreException(status);
        }

        checkSources(sources);
        checkTargets(sources, targets);

        sourceObjects = sources;
        targetNames = targets;
    }

    /**
     * Creates a new operation to move or rename the given product. After the run-method has
     * returned, all references of other products to the moved/renamed one are updated to refer to
     * the new name.
     * 
     * @param target The new location/name.
     * 
     * @throws CoreException If the source does not exist or is modified or if the target already
     *             exists.
     */
    public MoveOperation(Object[] sources, IIpsPackageFragment target) throws CoreException {
        sourceObjects = prepare(sources);
        targetRoot = target.getRoot();
        targetNames = getTargetNames(sourceObjects, target);

        // perform checks, if one check fails an core exception will be thrown
        checkTargetIncludedInSources(sources, target);
        checkSources(sources);
        checkTargets(sources, targetNames);
    }

    /**
     * Creates a new operation to move the given elements.
     * 
     * @param targetProject The target project where the sources will be moved to
     * @param sources All sources which will be moved to the target
     * @param target The target absolute path
     */
    public MoveOperation(IProject targetProject, Object[] sources, String target) throws CoreException {
        sourceObjects = prepare(sources);
        this.targetProject = targetProject;

        // Initialize the targets for each given source.
        targetNames = new String[sources.length];
        for (int i = 0; i < sources.length; i++) {
            targetNames[i] = target;
        }

        checkSources(sources);
        checkTargets(sources, targetNames);
    }

    /**
     * Returns the new runtime id for the target specified by the given index. Returns
     * <code>null</code> if no new runtime id is provided. In this case the old runtime id, has to
     * used.
     */
    private String getNewRuntimeId(int index) {
        if (newRuntimeIds == null) {
            return null;
        }
        return newRuntimeIds.get(index);
    }

    /**
     * Creates the new qualified names for the moved objects.
     * 
     * @param sources The objects to move
     * 
     * @param target The package fragment to move to.
     */
    private String[] getTargetNames(Object[] sources, IIpsPackageFragment target) {
        return getTargetNames(sources, target.getName(), target.getEnclosingResource().getLocation().toOSString());
    }

    /**
     * Creates the new qualified names for the moved objects or the target folder in case of none
     * IPS elements.
     * 
     * @param sources The objects to move.
     * @param target The unqualified target name of the resource to move to.
     * @param targetFullPath The full location path (absolute) of the target.
     */
    private String[] getTargetNames(Object[] sources, String target, String targetFullPath) {
        String[] result = new String[sources.length];

        for (int i = 0; i < sources.length; i++) {
            if (sources[i] instanceof IIpsElement) {
                String prefix = target;
                if (!prefix.equals("")) { //$NON-NLS-1$
                    prefix += "."; //$NON-NLS-1$
                }
                if (sources[i] instanceof IIpsPackageFragment) {
                    String name = ((IIpsPackageFragment)sources[i]).getName();
                    int index = name.lastIndexOf('.');
                    if (index == -1) {
                        result[i] = prefix + name;
                    } else {
                        result[i] = prefix + name.substring(index + 1);
                    }
                } else {
                    result[i] = prefix + ((IIpsElement)sources[i]).getName();
                }
            } else if (sources[i] instanceof IFile) {
                result[i] = targetFullPath;
            } else if (sources[i] instanceof File) {
                result[i] = targetFullPath;
            }
        }

        return result;
    }

    /**
     * Converts any contained IIpsSrcFiles to the objects contained within.
     * 
     * @param rawSources The IIpsElements to prepare.
     * @throws CoreException If an IIpsSrcFile is contained which can not return the IIpsObject
     *             stored within.
     */
    private Object[] prepare(Object[] rawSources) throws CoreException {
        Object[] result = new Object[rawSources.length];

        for (int i = 0; i < result.length; i++) {
            if (rawSources[i] instanceof IIpsSrcFile) {
                result[i] = ((IIpsSrcFile)rawSources[i]).getIpsObject();
            } else if (rawSources[i] instanceof String) {
                result[i] = new File((String)rawSources[i]);
            } else {
                result[i] = rawSources[i];
            }
        }
        return result;
    }

    public static boolean canMove(Object[] sources, Object target) {
        return canMoveToTarget(target) && canMoveSources(sources) && canMovePackages(sources, target);
    }

    /**
     * Returns true if the given IIpsElement array contains at least one IIpsProject, false
     * otherwise.
     */
    private static boolean canMoveSources(Object[] sources) {
        for (Object source : sources) {
            if (source instanceof IIpsProject) {
                return false;
            }
        }
        return true;
    }

    /**
     * If target object is of type:
     * <ul>
     * <li><code>IIpsObject</code>
     * <li><code>IIpsObjectPart</code>
     * <li><code>IFile</code>
     * <li><code>ArchiveIpsPackageFragment</code>
     * <li><code>ArchiveIpsPackageFragmentRoot</code>
     * </ul>
     * false is returned. For all other types returns true.
     */
    private static boolean canMoveToTarget(Object target) {
        return !(target instanceof IIpsObject) & !(target instanceof IIpsObjectPart) & !(target instanceof IFile)
                & !(target instanceof IIpsSrcFile) & !(target instanceof ArchiveIpsPackageFragment)
                & !(target instanceof ArchiveIpsPackageFragmentRoot);
    }

    /**
     * Returns true for allowed move operations containing packages.
     * <p>
     * The current implementation returns <code>false</code> if the given target is element of the
     * given array of sources, e.g. moving an Object in itself. If the given target is a package,
     * this method returns <code>false</code> if the package is a subpackage of the given sources.
     * <code>true</code> otherwise. If the corresponding resource of the target is null return
     * <code>false</code> e.g. target is inside an ips archive.
     */
    private static boolean canMovePackages(Object[] sources, Object target) {
        for (Object source : sources) {
            if (source.equals(target)) {
                return false;
            } else if (source instanceof IIpsPackageFragment || source instanceof IIpsPackageFragmentRoot) {
                if (target instanceof IIpsPackageFragment || target instanceof IIpsPackageFragmentRoot) {
                    IFolder sourceFolder = (IFolder)((IIpsElement)source).getCorrespondingResource();
                    IResource targetResource = ((IIpsElement)target).getCorrespondingResource();
                    if (!(targetResource instanceof IFolder)) {
                        return false;
                    }
                    IFolder targetFolder = (IFolder)targetResource;
                    if (sourceFolder.getFullPath().isPrefixOf(targetFolder.getFullPath())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                IProgressMonitor currMonitor = monitor;
                if (currMonitor == null) {
                    currMonitor = new NullProgressMonitor();
                }

                currMonitor.beginTask("Move", sourceObjects.length); //$NON-NLS-1$
                for (int i = 0; i < sourceObjects.length; i++) {
                    try {
                        currMonitor.internalWorked(1);

                        Object toMove = null;
                        if (sourceObjects[i] instanceof IIpsSrcFile) {
                            toMove = ((IIpsSrcFile)sourceObjects[i]).getIpsObject();
                        } else {
                            toMove = sourceObjects[i];
                        }

                        if (toMove instanceof IProductCmpt) {
                            moveProductCmpt((IProductCmpt)toMove, targetNames[i], getNewRuntimeId(i), currMonitor);
                        } else if (toMove instanceof IIpsPackageFragment) {
                            movePackageFragement((IIpsPackageFragment)toMove, targetNames[i], currMonitor);
                        } else if (toMove instanceof ITableContents) {
                            moveTableContent((ITableContents)toMove, targetNames[i], currMonitor);
                        } else if (toMove instanceof ITestCase) {
                            moveTestCase((ITestCase)toMove, targetNames[i], currMonitor);
                        } else if (toMove instanceof IFile) {
                            moveNoneIpsElement((IFile)sourceObjects[i], targetNames[i]);
                        } else if (toMove instanceof IIpsObject) {
                            moveIpsObject((IIpsObject)sourceObjects[i], targetNames[i], targetRoot, currMonitor);
                        } else if (toMove instanceof File) {
                            moveNoneIpsElement((File)sourceObjects[i], targetNames[i]);
                        }
                    } catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                }
                currMonitor.done();
            }
        };

        BusyIndicator.showWhile(getDisplay(), run);
    }

    private void moveIpsObject(IIpsObject ipsObject,
            String targetName,
            IIpsPackageFragmentRoot targetRoot,
            IProgressMonitor pm) throws CoreException {

        ProcessorBasedRefactoring moveRefactoring = ipsObject.getMoveRefactoring();
        IIpsMoveProcessor moveProcessor = (IIpsMoveProcessor)moveRefactoring.getProcessor();
        IIpsPackageFragment targetIpsPackageFragment = targetRoot.getIpsPackageFragment(QNameUtil
                .getPackageName(targetName));
        moveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        IWorkspaceRunnable operation = new PerformRefactoringOperation(moveRefactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, pm);
    }

    private void moveNoneIpsElement(File file, String targetName) {
        if (targetProject == null) {
            targetProject = targetRoot.getIpsProject().getProject();
        }
        String fileName = file.getAbsolutePath();
        if (fileName.startsWith(targetProject.getLocation().toOSString())) {
            fileName = fileName.substring(targetProject.getLocation().toOSString().length());
        }

        IFile sourceFile = targetProject.getFile(fileName);
        if (sourceFile.exists()) {
            moveNoneIpsElement(sourceFile, targetName);
        } else {
            copyNoneIpsElement(fileName, targetName);
        }
    }

    private void copyNoneIpsElement(String fileName, String targetName) {
        IContainer targetFolder = getTargetContainer(targetName);
        CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
        operation.copyFiles(new String[] { fileName }, targetFolder);
    }

    private void moveNoneIpsElement(IFile sourceFile, String targetName) {
        IContainer targetFolder = getTargetContainer(targetName);
        MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(Display.getCurrent().getActiveShell());
        operation.copyResources(new IResource[] { sourceFile }, targetFolder);
    }

    private IContainer getTargetContainer(String targetName) {
        if (targetProject == null) {
            targetProject = targetRoot.getIpsProject().getProject();
        }
        String folderName = ""; //$NON-NLS-1$
        if (targetName.startsWith(targetProject.getLocation().toOSString())) {
            folderName = targetName.substring(targetProject.getLocation().toOSString().length());
        } else {
            folderName = targetName;
        }

        IContainer targetFolder = null;
        if (folderName.length() == 0) {
            targetFolder = targetProject;
        } else {
            targetFolder = targetProject.getFolder(folderName);
        }
        return targetFolder;
    }

    private void movePackageFragement(IIpsPackageFragment pack, String newName, IProgressMonitor monitor)
            throws CoreException {

        IIpsPackageFragmentRoot currTargetRoot = targetRoot;
        if (currTargetRoot == null) {
            currTargetRoot = pack.getRoot();
        }

        IIpsPackageFragment parent = pack.getParentIpsPackageFragment();

        // 1) Find all files contained in this folder.
        ArrayList<String[]> files = new ArrayList<String[]>();
        getRelativeFileNames("", (IFolder)pack.getEnclosingResource(), files); //$NON-NLS-1$

        IIpsPackageFragmentRoot sourceRoot = parent.getRoot();

        // 2) Move them all.
        boolean createSubPackage = false;
        for (String[] fileInfos : files) {
            IIpsPackageFragment targetPackage = currTargetRoot.getIpsPackageFragment(buildPackageName(
                    "", newName, fileInfos[0])); //$NON-NLS-1$
            if (targetPackage.getParentIpsPackageFragment().equals(pack)) {
                createSubPackage = true;
            }

            if (!targetPackage.exists()) {
                currTargetRoot.createPackageFragment(targetPackage.getName(), true, monitor);
            }
            IIpsSrcFile targetFile = targetPackage.getIpsSrcFile(fileInfos[1]);
            IIpsPackageFragment sourcePackage = sourceRoot.getIpsPackageFragment(buildPackageName(pack.getName(),
                    "", fileInfos[0])); //$NON-NLS-1$
            IIpsSrcFile sourceFile = sourcePackage.getIpsSrcFile(fileInfos[1]);
            if (sourceFile != null) {
                // We have an IIpsSrcFile, so we have to move it correctly.
                if (sourceFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
                    IProductCmpt cmpt = (IProductCmpt)sourceFile.getIpsObject();
                    move(cmpt, targetFile, null, monitor);
                } else if (sourceFile.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
                    ITableContents tblcontent = (ITableContents)sourceFile.getIpsObject();
                    move(tblcontent, targetFile, monitor);
                } else if (sourceFile.getIpsObjectType() == IpsObjectType.TEST_CASE) {
                    ITestCase testCase = (ITestCase)sourceFile.getIpsObject();
                    move(testCase, targetFile, monitor);
                } else {
                    IIpsObject ipsObject = sourceFile.getIpsObject();
                    moveIpsObject(ipsObject, targetPackage.getName() + '.' + targetFile.getIpsObjectName(),
                            currTargetRoot, monitor);
                }
            } else {
                // We do not have a IIpsSrcFile, so move the file as resource operation.
                IFolder folder = (IFolder)sourcePackage.getEnclosingResource();
                IFile rawFile = folder.getFile(fileInfos[1]);
                IPath destination = ((IFolder)targetPackage.getCorrespondingResource()).getFullPath().append(
                        fileInfos[1]);
                if (rawFile.exists()) {
                    rawFile.move(destination, true, monitor);
                } else {
                    if (!((IFolder)targetPackage.getCorrespondingResource()).getFolder(fileInfos[1]).exists()) {
                        IFolder rawFolder = folder.getFolder(fileInfos[1]);
                        rawFolder.move(destination, true, monitor);
                    }
                }
            }
        }

        // 3) Remove remaining folders(only if no sub package was to be created).
        if (!(createSubPackage)) {
            pack.getEnclosingResource().delete(true, monitor);
        }
    }

    private void moveProductCmpt(IProductCmpt cmpt, String newName, String newRuntimeId, IProgressMonitor monitor) {
        IIpsSrcFile file = createTarget(cmpt, newName);
        move(cmpt, file, newRuntimeId, monitor);
    }

    private void moveTableContent(ITableContents content, String newName, IProgressMonitor monitor) {
        IIpsSrcFile file = createTarget(content, newName);
        move(content, file, monitor);
    }

    private void moveTestCase(ITestCase testCase, String newName, IProgressMonitor monitor) {
        IIpsSrcFile file = createTarget(testCase, newName);
        move(testCase, file, monitor);
    }

    /**
     * Creates the IIpsSrcFile for the given target. The IpsObjectType associated with the new file
     * is the one stored in the given source. The target is created in the package fragment root of
     * the given source.
     */
    private IIpsSrcFile createTarget(IIpsObject source, String targetName) {
        IIpsPackageFragmentRoot currTargetRoot = targetRoot;
        if (currTargetRoot == null) {
            currTargetRoot = source.getIpsPackageFragment().getRoot();
        }
        IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targetName));
        return pack.getIpsSrcFile(source.getIpsObjectType().getFileName(getUnqualifiedName(targetName)));
    }

    /**
     * Builds a package name by concatenating the given parts with dots. Each one of the three parts
     * can be empty.
     */
    private String buildPackageName(String prefix, String middle, String postfix) {
        String result = prefix;

        if (!result.equals("") && !middle.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            result += "."; //$NON-NLS-1$
        }

        if (!middle.equals("")) { //$NON-NLS-1$
            result += middle;
        }

        if (!result.equals("") && !postfix.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            result += "."; //$NON-NLS-1$
        }

        if (!postfix.equals("")) { //$NON-NLS-1$
            result += postfix;

        }
        return result;
    }

    /**
     * Recursively descend the path down the folders and collect all files found in the given list.
     */
    private void getRelativeFileNames(String path, IFolder folder, ArrayList<String[]> result) throws CoreException {
        IResource[] members = folder.members();

        if (members.length == 0) {
            result.add(new String[] { StringUtil.getPackageName(path), StringUtil.unqualifiedName(path) });
        }

        for (IResource member : members) {
            if (member.getType() == IResource.FOLDER) {
                getRelativeFileNames(
                        path.length() > 0 ? (path + "." + member.getName()) : member.getName(), (IFolder)member, result); //$NON-NLS-1$
            } else if (member.getType() == IResource.FILE) {
                result.add(new String[] { path, member.getName() });
            }
        }
    }

    /**
     * Returns the package name for the given, full qualified name (which means all segments except
     * the last one, segments separated by dots). The name must not be a filename with extension.
     */
    private String getPackageName(String qualifiedName) {
        String result = ""; //$NON-NLS-1$
        int index = qualifiedName.lastIndexOf('.');
        if (index > -1) {
            result = qualifiedName.substring(0, index);
        }
        return result;
    }

    /**
     * Returns the unqualified name for the given, full qualified name. The qualified name must not
     * be a filename with extension.
     */
    private String getUnqualifiedName(String qualifiedName) {
        String result = qualifiedName;
        int index = qualifiedName.lastIndexOf('.');
        if (index > -1) {
            result = qualifiedName.substring(index + 1);
        }
        return result;
    }

    /**
     * Moves one table content to the given target file.
     */
    private void move(ITableContents source, IIpsSrcFile targetFile, IProgressMonitor monitor) {
        try {
            IProductCmptGeneration[] refs = source.getIpsProject().findReferencingProductCmptGenerations(
                    source.getQualifiedNameType());

            // copy
            createCopy(source.getIpsSrcFile(), targetFile, monitor);

            // update references
            for (IProductCmptGeneration ref : refs) {
                fixTableContentsRelations(ref, source.getQualifiedName(), targetFile.getIpsObject().getQualifiedName(),
                        monitor);
            }

            // delete the source
            source.getEnclosingResource().delete(true, monitor);

        } catch (CoreException e) {
            Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
            MessageDialog.openError(shell, Messages.MoveOperation_titleAborted, Messages.MoveOperation_msgAborted);
            IpsPlugin.log(e);
        }

    }

    /**
     * Moves one product component to the given target file.
     */
    private void move(IProductCmpt source, IIpsSrcFile targetFile, String newRuntimeId, IProgressMonitor monitor) {
        try {
            String runtimeId = source.getRuntimeId();

            // create a tmpTargetFile to avoid case problems with windows file systems
            String tmpFileName = new Date().getTime() + "." + targetFile.getIpsObjectType().getFileExtension(); //$NON-NLS-1$
            IIpsSrcFile tmpTargetFile = targetFile.getIpsPackageFragment().getIpsSrcFile(tmpFileName);
            String qualifiedTargetName = targetFile.getQualifiedNameType().getName();

            // first, find all objects referring the source (which will be deleted later)
            IProductCmptGeneration[] refs = source.getIpsProject().findReferencingProductCmptGenerations(
                    source.getQualifiedNameType());
            List<ITestCase> testCaseRefs = source.getIpsModel().searchReferencingTestCases(source);

            // second, create the target
            createCopy(source.getIpsSrcFile(), tmpTargetFile, monitor);

            // third a), update references to product component generations
            for (IProductCmptGeneration ref : refs) {
                fixRelations(ref, source.getQualifiedName(), qualifiedTargetName, monitor);
            }

            // third b), update references to test cases
            for (ITestCase iTestCase : testCaseRefs) {
                fixRelations(iTestCase, source.getQualifiedName(), qualifiedTargetName);
            }

            // fourth, delete the source
            source.getEnclosingResource().delete(true, monitor);
            // move tmp file to correct file
            tmpTargetFile.getCorrespondingFile().move(targetFile.getCorrespondingFile().getFullPath(), true, monitor);

            // at least, update the runtime id of the moved product cmpt to the original runtime id
            IProductCmpt productCmpt = (IProductCmpt)targetFile.getIpsObject();
            productCmpt.setRuntimeId(newRuntimeId != null ? newRuntimeId : runtimeId);
            productCmpt.getIpsSrcFile().save(true, null);

        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                    Messages.MoveOperation_msgAborted, e));
        }
    }

    /**
     * Moves one test case to the given target file.
     */
    private void move(ITestCase source, IIpsSrcFile targetFile, IProgressMonitor monitor) {
        try {
            createCopy(source.getIpsSrcFile(), targetFile, monitor);
            source.getEnclosingResource().delete(true, monitor);
        } catch (CoreException e) {
            Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
            MessageDialog.openError(shell, Messages.MoveOperation_titleAborted, Messages.MoveOperation_msgAborted);
            IpsPlugin.log(e);
        }
    }

    private void createCopy(IIpsSrcFile source, IIpsSrcFile targetFile, IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment pack = targetFile.getIpsPackageFragment();
        if (!pack.exists()) {
            pack.getRoot().createPackageFragment(pack.getName(), true, monitor);
        }
        pack.createIpsFile(targetFile.getName(), source.getContentFromEnclosingResource(), true, monitor);
    }

    /**
     * Resets the target of all relations of the given generation, if the target equals the old
     * name, to the new name.
     * 
     * @param generation The generation to fix the relations at.
     * @param oldName The old, qualified name of the target.
     * @param newName The new, qualified name of the target
     * @param monitor Progress monitor to show progress.
     */
    private void fixRelations(IProductCmptGeneration generation,
            String oldName,
            String newName,
            IProgressMonitor monitor) throws CoreException {

        IProductCmptLink[] relations = generation.getLinks();

        for (IProductCmptLink relation : relations) {
            String target = relation.getTarget();
            if (target.equals(oldName)) {
                relation.setTarget(newName);
            }
        }

        generation.getIpsSrcFile().save(true, monitor);
    }

    /**
     * Resets the target of all table content usages of the given generation, if the table content
     * usage equals the old name, to the new name.
     * 
     * @param generation The generation to fix the table content.
     * @param oldName The old, qualified name of the target.
     * @param newName The new, qualified name of the target
     * @param monitor Progress monitor to show progress.
     */
    private void fixTableContentsRelations(IProductCmptGeneration generation,
            String oldName,
            String newName,
            IProgressMonitor monitor) throws CoreException {
        ITableContentUsage[] tcu = generation.getTableContentUsages();

        for (ITableContentUsage element : tcu) {
            String target = element.getTableContentName();
            if (target.equals(oldName)) {
                element.setTableContentName(newName);
            }
        }
        generation.getIpsSrcFile().save(true, monitor);
    }

    /**
     * Resets the product component of all test policy components of the given test case, if the
     * product component equals the old name, to the new name.
     */
    private void fixRelations(ITestCase testCase, String oldName, String newName) throws CoreException {
        ITestPolicyCmpt[] allTestPolicyCmpt = testCase.getAllTestPolicyCmpt();
        for (ITestPolicyCmpt element : allTestPolicyCmpt) {
            if (oldName.equals(element.getProductCmpt())) {
                element.setProductCmptAndNameAfterIfApplicable(newName);
            }
        }
        testCase.getIpsSrcFile().save(true, null);
    }

    /**
     * Check all targets not to exist. If an existing target is found, a core exception is thrown.
     * 
     * @param sources The array of source objects. Used to get the type for the target.
     * @param targets The qualified names of the targets to test.
     * 
     * @throws CoreException if a target exists.
     */
    private void checkTargets(Object[] sources, String[] targets) throws CoreException {
        for (int i = 0; i < targets.length; i++) {
            IIpsPackageFragmentRoot currTargetRoot = targetRoot;

            IIpsElement toTest = null;
            if (sources[i] instanceof IIpsSrcFile) {
                toTest = ((IIpsSrcFile)sources[i]).getIpsObject();
            } else if (sources[i] instanceof IIpsElement) {
                toTest = (IIpsElement)sources[i];
            }

            if (currTargetRoot == null) {
                // no target root, use the source root as target root (e.g. if renaming the files)
                if (toTest instanceof IIpsObject) {
                    currTargetRoot = ((IIpsObject)toTest).getIpsPackageFragment().getRoot();
                } else if (toTest instanceof IIpsPackageFragment) {
                    currTargetRoot = ((IIpsPackageFragment)toTest).getRoot();
                }
            }

            if (toTest instanceof IIpsPackageFragment) {
                IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(targets[i]);
                if (pack.exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgPackageExists, targets[i]);
                    IpsStatus status = new IpsStatus(msg);
                    throw new CoreException(status);
                }
            } else if (toTest instanceof IProductCmpt) {
                IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targets[i]));
                if (pack.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(getUnqualifiedName(targets[i]))).exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
                    IpsStatus status = new IpsStatus(msg);
                    throw new CoreException(status);
                }
            } else if (toTest instanceof ITableContents) {
                IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targets[i]));
                if (pack.getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName(getUnqualifiedName(targets[i])))
                        .exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
                    IpsStatus status = new IpsStatus(msg);
                    throw new CoreException(status);
                }
            } else if (toTest instanceof ITestCase) {
                IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targets[i]));
                if (pack.getIpsSrcFile(IpsObjectType.TEST_CASE.getFileName(getUnqualifiedName(targets[i]))).exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
                    IpsStatus status = new IpsStatus(msg);
                    throw new CoreException(status);
                }
            }
        }
    }

    /**
     * Check all sources to exist and to be saved. If not so, a CoreException will be thrown.
     */
    private void checkSources(Object[] source) throws CoreException {
        IpsStatus status = checkSourcesForInvalidContent(source);
        if (status != null) {
            throw new CoreException(status);
        }
    }

    /**
     * Check all sources to exist and to be saved. If not so, an IpsStatus containing the error will
     * be returned. Returns <code>null</code> if no error was found.
     */
    public static IpsStatus checkSourcesForInvalidContent(Object[] sources) throws CoreException {
        for (Object currentSource : sources) {
            IIpsElement toTest = null;
            if (currentSource instanceof IIpsSrcFile) {
                toTest = ((IIpsSrcFile)currentSource).getIpsObject();
            } else if (currentSource instanceof IIpsElement) {
                toTest = (IIpsElement)currentSource;
            }

            if (toTest instanceof IIpsPackageFragment) {
                IIpsPackageFragment pack = (IIpsPackageFragment)toTest;
                if (!pack.exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgPackageMissing, pack.getName());
                    return new IpsStatus(msg);
                }
                IpsStatus status = checkSourcesForInvalidContent(pack.getChildren());
                if (status != null) {
                    return status;
                }
                status = checkSourcesForInvalidContent(pack.getChildIpsPackageFragments());
                if (status != null) {
                    return status;
                }

            } else if (toTest instanceof IIpsObject) {
                IIpsObject ipsObject = (IIpsObject)toTest;
                if (!(ipsObject.getIpsSrcFile().exists())) {
                    String msg = NLS.bind(Messages.MoveOperation_msgSourceMissing, getQualifiedSourceName(ipsObject));
                    return new IpsStatus(msg);
                }

                if (ipsObject.getIpsSrcFile().isDirty()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgSourceModified, getQualifiedSourceName(ipsObject));
                    return new IpsStatus(msg);
                }

                if (!(ipsObject.exists())) {
                    String msg = NLS.bind(Messages.MoveOperation_errorIpsObjectMissing, ipsObject.getName());
                    return new IpsStatus(msg);
                }

            } else if (currentSource instanceof IFile) {
                if (!((IFile)currentSource).exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_errorMessageSourceNotExists, ((IFile)currentSource)
                            .getLocation().toOSString());
                    return new IpsStatus(msg);
                }

            } else if (currentSource instanceof String) {
                if (!(new File((String)currentSource).exists())) {
                    String msg = NLS.bind(Messages.MoveOperation_errorMessageSourceNotExists, currentSource);
                    return new IpsStatus(msg);
                }

            } else {
                /*
                 * Localization of the following messages is necessary because the exception is
                 * expected to be caught later and the messages are expected to be displayed to the
                 * user.
                 */
                String msg = null;
                if (toTest instanceof IIpsObject) {
                    msg = NLS.bind(Messages.MoveOperation_msgUnsupportedType, ((IIpsObject)toTest).getIpsObjectType()
                            .getDisplayName());
                } else if (toTest != null) {
                    msg = NLS.bind(Messages.MoveOperation_msgUnsupportedObject, toTest.getName());
                } else {
                    msg = NLS.bind(Messages.MoveOperation_msgUnsupportedObject, currentSource);
                }
                return new IpsStatus(msg);
            }
        }

        return null;
    }

    private void checkTargetIncludedInSources(Object[] sources, IIpsPackageFragment target) throws CoreException {
        if (isTargetIncludedInSources(sources, target)) {
            throw new CoreException(new IpsStatus(Messages.MoveOperation_msgErrorTheTargetIsIncludedInTheSource));
        }
    }

    /**
     * Check if the target is included in the source or child of source. Returns <code>true</code>
     * if the given target IIpsPackageFragment is inside one of one source elements. The source
     * elements which will be checked are IIpsPackageFragmentRoots or IIpsPackageFragment all other
     * types will be ignored. Returns <code>false</code> if the target is not inside the sources.
     * This check is very important for the move operation, because the target will be deleted after
     * the move operation, thus if the target is inside one of source this package will be deleted!
     */
    public static boolean isTargetIncludedInSources(Object[] sources, IIpsPackageFragment target) throws CoreException {
        for (Object source : sources) {
            if (source instanceof IIpsPackageFragmentRoot) {
                if (isTargetInPackageFragment(target, ((IIpsPackageFragmentRoot)source).getDefaultIpsPackageFragment()
                        .getChildIpsPackageFragments())) {
                    return true;
                }
            } else if (source instanceof IIpsPackageFragment) {
                if (target.equals(source)) {
                    return true;
                }
                if (isTargetInPackageFragment(target, ((IIpsPackageFragment)source).getChildIpsPackageFragments())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTargetInPackageFragment(IIpsPackageFragment frgmt, IIpsPackageFragment[] packageFragments)
            throws CoreException {
        for (IIpsPackageFragment packageFragment : packageFragments) {
            if (packageFragment.equals(frgmt)) {
                return true;
            }
            if (isTargetInPackageFragment(frgmt, packageFragment.getChildIpsPackageFragments())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the qualified source-name (including file extension) for the given ips object.
     */
    public static String getQualifiedSourceName(IIpsObject product) {
        return product.getQualifiedName() + "." + product.getIpsObjectType().getFileExtension(); //$NON-NLS-1$
    }

    private Shell getShell() {
        Display display = getDisplay();
        if (display != null) {
            return display.getActiveShell();
        } else {
            return new Shell((Display)null);
        }

    }

    private Display getDisplay() {
        return Display.getCurrent();
    }

}
