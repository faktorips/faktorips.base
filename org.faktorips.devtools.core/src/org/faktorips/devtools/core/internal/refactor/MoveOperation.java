/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.util.StringUtil;

/**
 * Moves (and renames) product components.
 * 
 * @author Thorsten Guenther
 */
public class MoveOperation implements IRunnableWithProgress {

	/**
	 * All product components or package fragements to move/rename.
	 */
	private IIpsElement[] sourceObjects;
	
	/**
	 * The new (package-qualified) names for the objects to move/rename.
	 */
	private String[] targetNames;
	
    /**
     * The ips package fragment root defindes the ips src root where to place the moved objects.
     */
    private IIpsPackageFragmentRoot targetRoot;
	
	/**
	 * Creates a new operation to move or rename the given product. After the run-method has returned,
	 * all references of other products to the moved/renamed one are updated to refer to the new name.
	 * 
	 * @param source The product to rename.
	 * @param target The new location/name.
	 * @throws CoreException If the source does not exist or ist modiefied (if a product component) 
	 * or if the target allready exists.
	 */
	public MoveOperation(IProductCmpt source, String target) throws CoreException {
		this(new IIpsElement[] {source}, new String[] {target});
	}
	
	/**
	 * Move all the given package fragements and product components to the given targets. 
	 * 
	 * @param sources An array containing <code>IProductCmpt</code> or <code>IIpsPackageFragement</code>
	 * objects to move.
	 * @param targets An array of the new, qualified names for the objects to move. The names are object-names, 
	 * not filenames, so do not append any file extension to the name.
	 * @throws CoreException if the both arrays are not of the same lenth.
	 */
	public MoveOperation(IIpsElement[] sources, String[] targets) throws CoreException {
		if (sources.length != targets.length) {
			IpsStatus status = new IpsStatus("Number of source- and target-objects is not the same."); //$NON-NLS-1$
			throw new CoreException(status);
		}
		
		checkSources(sources);
		checkTargets(sources, targets);

		this.sourceObjects = sources;
		this.targetNames = targets;
	}

	/**
	 * Creates a new operation to move or rename the given product. After the run-method has returned,
	 * all references of other products to the moved/renamed one are updated to refer to the new name.
	 * 
	 * @param source The product to rename.
	 * @param target The new location/name.
	 * @throws CoreException If the source does not exist or ist modiefied or if the target allready exists.
	 */
	public MoveOperation(IIpsElement[] sources, IIpsPackageFragment target) throws CoreException {
		this.sourceObjects = prepare(sources);
        this.targetRoot = target.getRoot();
		this.targetNames = getTargetNames(this.sourceObjects, target);
		
        // perform checks, if one check fails an core exception will be thrown
        checkTargetIncludedInSources(sources, target);
        checkSources(sources);
		checkTargets(sources, targetNames);
	}

    /**
	 * Creates the new qualified names for the moved objects.
	 *  
	 * @param sources The objects to move
	 * @param target The package fragment to move to.
	 */
	private String[] getTargetNames(IIpsElement[] sources, IIpsPackageFragment target) {
		String[] result = new String[sources.length];
		
		String prefix = target.getName();
		if (!prefix.equals("")) { //$NON-NLS-1$
			prefix += "."; //$NON-NLS-1$
		}
		for (int i = 0; i < sources.length; i++) {
			if (sources[i] instanceof IIpsPackageFragment) {
                String name = ((IIpsPackageFragment)sources[i]).getName();
                int index = name.lastIndexOf('.');
                if (index == -1) {
                    result[i] = prefix + name;
                }
                else {
                    result[i] = prefix + name.substring(index + 1);
                }
			}
			else {
				result[i] = prefix + sources[i].getName();
			}
		}
		
		return result;
	}
	
	/**
	 * Converts any contained IIpsSrcFiles to the objects contained within.
	 * 
	 * @param rawSources The IIpsElements to prepare.
	 * @throws CoreException If an IIpsSrcFile is contained which can not return the IIpsObject stored within.
	 */
	private IIpsElement[] prepare(IIpsElement[] rawSources) throws CoreException {
		IIpsElement[] result = new IIpsElement[rawSources.length];
		
		for (int i = 0; i < result.length; i++) {
			if (rawSources[i] instanceof IIpsSrcFile) {
				result[i] = ((IIpsSrcFile)rawSources[i]).getIpsObject();
			}
			else {
				result[i] = rawSources[i];
			}
		}
		return result;
	}
    
    public static boolean canMove(IIpsElement[] sources, Object target){
        return canMoveToTarget(target) && canMoveSources(sources) && canMovePackages(sources, target);
    }
    /** 
     * Returns true if the given IIpsElement array contains at least one IIpsProject, fals otherwise.
     */
    private static boolean canMoveSources(IIpsElement[] sources) {
        for (int i = 0; i < sources.length; i++) {
            if(sources[i] instanceof IIpsProject){
                return false;
            }
        }
        return true;
    }

    /**
     * If target object is of type <code>IIpsObject</code>, <code>IIpsObjectPart</code>
     * or <code>IResource</code> false is returned. For all other types returns true.
     */
    private static boolean canMoveToTarget(Object target) {
        return !(target instanceof IIpsObject) & !(target instanceof IIpsObjectPart) & !(target instanceof IResource);
    }
    
    /**
     * Returns true for allowed move operations containing packages.
     * <p>
     * The current implementation returns <code>false</code> if the given target is element of the given array of sources, 
     * e.g. moving an Object in itself. If the given target is a package, this method returns <code>false</code> if the package
     * is a subpackage of the given sources. <code>true</code> otherwise.
     * If the corresponding resource of the target is null return <code>false</code> e.g. target is inside an ips archive.
     */
    private static boolean canMovePackages(IIpsElement[] sources, Object target) {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].equals(target)) {
                return false;
            } else if (sources[i] instanceof IIpsPackageFragment || sources[i] instanceof IIpsPackageFragmentRoot) {
                if (target instanceof IIpsPackageFragment || target instanceof IIpsPackageFragmentRoot) {
                    IFolder sourceFolder = (IFolder)sources[i].getCorrespondingResource();
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
	
	/**
     * {@inheritDoc}
     */
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    Runnable run = new Runnable(){
            public void run() {
                IProgressMonitor currMonitor = monitor;
                if (currMonitor == null) {
                    currMonitor = new NullProgressMonitor();
                }

                for (int i = 0; i < MoveOperation.this.sourceObjects.length; i++) {
                    try {
                        IIpsElement toMove;
                        if (sourceObjects[i] instanceof IIpsSrcFile) {
                            toMove = ((IIpsSrcFile)sourceObjects[i]).getIpsObject();
                        }
                        else {
                            toMove = sourceObjects[i];
                        }
                        
                        if (toMove instanceof IProductCmpt) {
                            moveProductCmpt((IProductCmpt)toMove, MoveOperation.this.targetNames[i], monitor);
                        }
                        else if (toMove instanceof IIpsPackageFragment) {
                            movePackageFragement((IIpsPackageFragment)toMove, MoveOperation.this.targetNames[i], monitor);
                        }
                        else if (toMove instanceof ITableContents) {
                            moveTableContent((ITableContents)toMove, MoveOperation.this.targetNames[i], monitor);
                        }
                        else if (toMove instanceof ITestCase) {
                            moveTestCase((ITestCase)toMove, MoveOperation.this.targetNames[i], monitor);
                        }
                    } catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                }
            }
        };
        BusyIndicator.showWhile(Display.getCurrent(), run);
	}
	
	private void movePackageFragement(IIpsPackageFragment pack, String newName, IProgressMonitor monitor) throws CoreException {
		IIpsPackageFragmentRoot currTargetRoot = targetRoot;
        if (currTargetRoot == null){
            currTargetRoot = pack.getRoot();
        }
        
        IIpsPackageFragment parent = pack.getParentIpsPackageFragment();
		
		// first, find all products contained in this folder
	    ArrayList files = new ArrayList();
		getRelativeFileNames("", (IFolder)pack.getEnclosingResource(), files); //$NON-NLS-1$

		IIpsPackageFragmentRoot sourceRoot = parent.getRoot();
		
		// second, move them all
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			String[] fileInfos = (String[]) iter.next();
			IIpsPackageFragment targetPackage = currTargetRoot.getIpsPackageFragment(buildPackageName("", newName, fileInfos[0])); //$NON-NLS-1$
			if (!targetPackage.exists()) {
                currTargetRoot.createPackageFragment(targetPackage.getName(), true, monitor);
			}
			IIpsSrcFile file = targetPackage.getIpsSrcFile(fileInfos[1]);
			IIpsPackageFragment sourcePackage = sourceRoot.getIpsPackageFragment(buildPackageName(pack.getName(), "", fileInfos[0])); //$NON-NLS-1$
			IIpsSrcFile cmptFile = sourcePackage.getIpsSrcFile(fileInfos[1]);  //$NON-NLS-1$
			if (cmptFile != null) {
				// we got an IIpsSrcFile, so we have to move it correctly
				if (cmptFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
					IProductCmpt cmpt = (IProductCmpt)cmptFile.getIpsObject();
					move(cmpt, file, monitor);
				}
				else if (cmptFile.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
					ITableContents tblcontent = (ITableContents)cmptFile.getIpsObject();
					move(tblcontent, file, monitor);
				}
                else if (cmptFile.getIpsObjectType() == IpsObjectType.TEST_CASE) {
                    ITestCase testCase = (ITestCase)cmptFile.getIpsObject();
                    move(testCase, file, monitor);
                }
                else {
                    // programming error, this is an unsupported type which should be moved, but this type
                    // wasn't checked in the prepare check of the move operation!
                    throw new CoreException(new IpsStatus(NLS.bind(Messages.MoveOperation_msgUnsupportedType, cmptFile.getName())));
                }
			} else {
				// we dont have a IIpsSrcFile, so move the file as resource operation
				IFolder folder = (IFolder)sourcePackage.getEnclosingResource(); 
				IFile rawFile = folder.getFile(fileInfos[1]);
				IPath destination = ((IFolder)targetPackage.getCorrespondingResource()).getFullPath().append(fileInfos[1]);
				if (rawFile.exists()) {
					rawFile.move(destination, true, monitor);
				}
				else {
					if (!((IFolder)targetPackage.getCorrespondingResource()).getFolder(fileInfos[1]).exists()) {
						IFolder rawFolder = folder.getFolder(fileInfos[1]);
						rawFolder.move(destination, true, monitor);
					}
				}
			}
		}

		// third, remove remaining folders
	    pack.getEnclosingResource().delete(true, monitor);
	}
	
	private void moveProductCmpt(IProductCmpt cmpt, String newName, IProgressMonitor monitor) {
		IIpsSrcFile file = createTarget(cmpt, newName);
		move(cmpt, file, monitor);
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
	 * Creates the IIpsSrcFile for the given target. The IpsObjectType associated with 
	 * the new file is the one stored in the given source. The target is created in the 
	 * package fragment root of the given source.
	 */
	private IIpsSrcFile createTarget(IIpsObject source, String targetName) {
        IIpsPackageFragmentRoot currTargetRoot = targetRoot;
        if (currTargetRoot == null){
            currTargetRoot = source.getIpsPackageFragment().getRoot();
        }
		IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targetName));
		return pack.getIpsSrcFile(source.getIpsObjectType().getFileName(getUnqualifiedName(targetName)));
	}
	
	/**
	 * Builds a package name by concatenating the given parts wiht dots. Every of the three parts
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
	private void getRelativeFileNames(String path, IFolder folder, ArrayList result) throws CoreException {
		IResource[] members = folder.members();
		
		if (members.length == 0) {
			result.add(new String[] {StringUtil.getPackageName(path), StringUtil.unqualifiedName(path)});
		}
		
		for (int i = 0; i < members.length; i++) {
			if (members[i].getType() == IResource.FOLDER) {
				getRelativeFileNames(path.length()>0?(path + "." + members[i].getName()):members[i].getName(), (IFolder)members[i], result); //$NON-NLS-1$
			}
			else if (members[i].getType() == IResource.FILE) {
				result.add(new String[] {path, members[i].getName()});
			}
		}
	}

	/**
	 * Returns the package name for the given, full qualified name (which means all segements 
	 * except the last one, segments seperated by dots). The name must not be a filename with extension.
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
	 * Returns the unqualified name for the given, full qualified name. The qualified name must 
	 * not be a filename with extension. 
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
			createCopy(source.getIpsSrcFile(), targetFile, monitor);
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
	private void move(IProductCmpt source, IIpsSrcFile targetFile, IProgressMonitor monitor) {
		try {
            String runtimeId = source.getRuntimeId();
            
			// first, find all objects refering the source (which will be deleted later)
			IProductCmptGeneration[] refs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
            ITestCase[] testCaseRefs = source.getIpsProject().findReferencingTestCases(source.getQualifiedName());
            
			// second, create the target
			createCopy(source.getIpsSrcFile(), targetFile, monitor);
			
			// third a), update references to product cmpt generations
			for (int i = 0; i < refs.length; i++) {
				fixRelations(refs[i], source.getQualifiedName(), targetFile.getIpsObject().getQualifiedName(), monitor);
			}
			
			// third b), update references to test cases
			for (int i = 0; i < testCaseRefs.length; i++) {
			    fixRelations(testCaseRefs[i], source.getQualifiedName(), targetFile.getIpsObject().getQualifiedName(), monitor);
			}
			
			// fourthly, delete the source
			source.getEnclosingResource().delete(true, monitor);
            
            // at least, update the runtime id of the moved product cmpt to the original runtime id
            IProductCmpt productCmpt = (IProductCmpt) targetFile.getIpsObject();
            productCmpt.setRuntimeId(runtimeId);
            productCmpt.getIpsSrcFile().save(true, null);
		} catch (CoreException e) {
			Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.MoveOperation_titleAborted, Messages.MoveOperation_msgAborted);
			IpsPlugin.log(e);
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
	 * Resets the target of all realations of the given generation, if the target equals the 
	 * old name, to the new name.
	 * 
	 * @param generation The generation to fix the relations at.
	 * @param oldName The old, qualified name of the target.
	 * @param newName The new, qualified name of the target
	 * @param monitor Progress monitor to show progress.
	 */
	private void fixRelations(IProductCmptGeneration generation, String oldName, String newName, IProgressMonitor monitor) throws CoreException {
		
		IProductCmptRelation[] relations = generation.getRelations();
		
		for (int i = 0; i < relations.length; i++) {
			String target = relations[i].getTarget();
			if (target.equals(oldName)) {
				relations[i].setTarget(newName);
			}
		}
		
		generation.getIpsObject().getIpsSrcFile().save(true, monitor);
	}

    /**
     * Resets the product component of all test policy cmpt of the given test case, if the product
     * component equals the old name, to the new name.
     * 
     * @throws CoreException
     */
    private void fixRelations(ITestCase testCase, String oldName, String newName, IProgressMonitor monitor)
            throws CoreException {
        ITestPolicyCmpt[] allTestPolicyCmpt = testCase.getAllTestPolicyCmpt();
        for (int i = 0; i < allTestPolicyCmpt.length; i++) {
            if (oldName.equals(allTestPolicyCmpt[i].getProductCmpt())){
                allTestPolicyCmpt[i].setProductCmpt(newName);
            }
        }
    }
    
	/**
	 * Check all targets not to exist. If an existing target is found, a core exception is thrown.
	 * 
	 * @param sources The array of source objects. Used to get the type for the target.
	 * @param targets The qualified names of the targets to test.
	 * @throws CoreException if a target exists.
	 */
	private void checkTargets(IIpsElement[] sources, String[] targets) throws CoreException {
        for (int i = 0; i < targets.length; i++) {
            IIpsPackageFragmentRoot currTargetRoot = targetRoot;

			IIpsElement toTest;
			if (sources[i] instanceof IIpsSrcFile) {
				toTest = ((IIpsSrcFile)sources[i]).getIpsObject();
			}
			else {
				toTest = sources[i];
			}

            if (currTargetRoot == null){
                // no target root, use the source root as target root (e.g. if renaming the files)
                if (toTest instanceof IIpsObject){
                    currTargetRoot = ((IIpsObject) toTest).getIpsPackageFragment().getRoot();
                } else if (toTest instanceof IIpsPackageFragment) {
                    currTargetRoot = ((IIpsPackageFragment) toTest).getRoot();
                }
			}

            if (toTest instanceof IIpsPackageFragment) {
                IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(targets[i]);
                if (pack.exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgPackageExists, targets[i]);
                    IpsStatus status = new IpsStatus(msg);
                    throw new CoreException(status);
                }
            }
            else if (toTest instanceof IProductCmpt) {
				IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targets[i]));
				if (pack.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(getUnqualifiedName(targets[i]))).exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
					IpsStatus status = new IpsStatus(msg);
					throw new CoreException(status);
				}
			}
			else if (toTest instanceof ITableContents) {
				IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targets[i]));
				if (pack.getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName(getUnqualifiedName(targets[i]))).exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
					IpsStatus status = new IpsStatus(msg);
					throw new CoreException(status);
				}
			}
            else if (toTest instanceof ITestCase) {
                IIpsPackageFragment pack = currTargetRoot.getIpsPackageFragment(getPackageName(targets[i]));
                if (pack.getIpsSrcFile(IpsObjectType.TEST_CASE.getFileName(getUnqualifiedName(targets[i]))).exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
                    IpsStatus status = new IpsStatus(msg);
                    throw new CoreException(status);
                }
            }            
		}
	}
	
    /*
     * Check all sources to exist and to be saved. If not so, a CoreException will be thrown.
     */
    private void checkSources(IIpsElement[] source) throws CoreException{
        IpsStatus status = checkSourcesForInvalidContent(source);
        if (status != null){
            throw new CoreException(status);
        }
    }
    
	/**
	 * Check all sources to exist and to be saved. If not so, an IpsStatus containing the error will be returned.
     * Returns <code>null</code> if no error was found.
	 */
	public static IpsStatus checkSourcesForInvalidContent(IIpsElement[] source) throws CoreException {
		for (int i = 0; i < source.length; i++) { 
			IIpsElement toTest;
			if (source[i] instanceof IIpsSrcFile) {
				toTest = ((IIpsSrcFile)source[i]).getIpsObject();
			}
			else {
				toTest = source[i];
			}
			
			if (toTest instanceof IProductCmpt || toTest instanceof ITableContents || toTest instanceof ITestCase) {
                IIpsObject ipsObject = (IIpsObject)toTest;
				if (!ipsObject.getIpsSrcFile().exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgSourceMissing, getQualifiedSourceName(ipsObject));
					return new IpsStatus(msg); 
				}
				
				if (ipsObject.getIpsSrcFile().isDirty()) {
					String msg = NLS.bind(Messages.MoveOperation_msgSourceModified, getQualifiedSourceName(ipsObject));
                    return new IpsStatus(msg); 
				}
			}
			
            if (toTest instanceof IIpsPackageFragment) {
				IIpsPackageFragment pack = (IIpsPackageFragment)toTest;
				if (!pack.exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgPackageMissing, pack.getName());
                    return new IpsStatus(msg); 
				}
                IpsStatus status = checkSourcesForInvalidContent(pack.getChildren());
                if (status != null){
                    return status;
                }
                status = checkSourcesForInvalidContent(pack.getChildIpsPackageFragments());
                if (status != null){
                    return status;
                }
			}
            else if (toTest instanceof IProductCmpt) {
                IProductCmpt productCmpt = (IProductCmpt)toTest;
                if (!productCmpt.exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_msgErrorProductCmptIsMissing, productCmpt.getName()); 
                    return new IpsStatus(msg); 
                }
            }
            else if (toTest instanceof ITableContents) {
				ITableContents table = (ITableContents)toTest;
				if (!table.exists()) {
					String msg = NLS.bind(Messages.MoveOperation_tableContentIsMissing, table.getName()); 
                    return new IpsStatus(msg); 
				}
			}
            else if (toTest instanceof ITestCase) {
                ITestCase testCase = (ITestCase)toTest;
                if (!testCase.exists()) {
                    String msg = NLS.bind(Messages.MoveOperation_testCaseIsMissing, testCase.getName());
                    return new IpsStatus(msg); 
                }
            }            
			else {
				// localisation of the following messages is neccessary because
				// the exception is excpected to be
				// catched later and the messages are expected to be displayed
				// to the user.
				String msg = null;
				if (toTest instanceof IIpsObject) {
					msg = NLS.bind(Messages.MoveOperation_msgUnsupportedType, ((IIpsObject)toTest).getIpsObjectType().getName());
				} 
				else {
					msg = NLS.bind(Messages.MoveOperation_msgUnsupportedObject, toTest.getName());
				}
				return new IpsStatus(msg); 
			}
		}
        return null;
	}
	
    private void checkTargetIncludedInSources(IIpsElement[] sources, IIpsPackageFragment target) throws CoreException {
        if (isTargetIncludedInSources(sources, target)){
            throw new CoreException(new IpsStatus(Messages.MoveOperation_msgErrorTheTargetIsIncludedInTheSource));
        }
    }
    
    /**
     * Check if the target is included in the source or child of source. Returns <code>true</code> if the given target
     * IIpsPackageFragment is inside one of one source elements. The source elements wich will be checked are IIpsPackageFragmentRoots
     * or IIpsPackageFragment all other types will be ignored. Returns <code>false</code> if the target is not inside the sources.
     * This check is very importend for the move operation, because the target will be deleted after the move operation, thus if the 
     * target is inside one of source this package will be deleted!
     */
    public static boolean isTargetIncludedInSources(IIpsElement[] sources, IIpsPackageFragment target) throws CoreException {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] instanceof IIpsPackageFragmentRoot) {
                if (isTargetInPackageFragment(target, ((IIpsPackageFragmentRoot)target).getDefaultIpsPackageFragment()
                        .getChildIpsPackageFragments())) {
                    return true;
                }
            }
            else if (sources[i] instanceof IIpsPackageFragment) {
                if (target.equals(sources[i])){
                    return true;
                }
                if (isTargetInPackageFragment(target, ((IIpsPackageFragment)sources[i]).getChildIpsPackageFragments())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isTargetInPackageFragment(IIpsPackageFragment frgmt, IIpsPackageFragment[] packageFragments) throws CoreException {
        for (int i = 0; i < packageFragments.length; i++) {
            if(packageFragments[i].equals(frgmt)){
                return true;
            }
            if (isTargetInPackageFragment(frgmt, packageFragments[i].getChildIpsPackageFragments())){
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
	
}
