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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class NonIPSMoveOperation implements IRunnableWithProgress {

    /**
     * All objects to move/rename.
     */
    private Object[] sourceObjects;

    /**
     * The new (package-qualified) names for the objects to move/rename.
     */
    private String[] targetNames;

    /**
     * The ips package fragment root defines the ips src root where to place the moved objects.
     */
    private IIpsPackageFragmentRoot targetRoot;

    private IProject targetProject;

    /**
     * Creates a new operation to move or rename the given product. After the run-method has
     * returned, all references of other products to the moved/renamed one are updated to refer to
     * the new name.
     * 
     * @param target The new location/name.
     * 
     * @throws CoreRuntimeException If the source does not exist or is modified or if the target
     *             already exists.
     */
    public NonIPSMoveOperation(Object[] sources, IIpsPackageFragment target) throws CoreRuntimeException {
        sourceObjects = prepare(sources);
        targetRoot = target.getRoot();
        targetNames = getTargetNames(sourceObjects, target);

        // perform checks, if one check fails an core exception will be thrown
        checkSources(sources);
    }

    /**
     * Creates a new operation to move the given elements.
     * 
     * @param targetProject The target project where the sources will be moved to
     * @param sources All sources which will be moved to the target
     * @param target The target absolute path
     */
    public NonIPSMoveOperation(IProject targetProject, Object[] sources, String target) throws CoreRuntimeException {
        sourceObjects = prepare(sources);
        this.targetProject = targetProject;

        // Initialize the targets for each given source.
        targetNames = new String[sources.length];
        for (int i = 0; i < sources.length; i++) {
            targetNames[i] = target;
        }

        checkSources(sources);
    }

    /**
     * Converts any contained IIpsSrcFiles to the objects contained within.
     * 
     * @param rawSources The IIpsElements to prepare.
     * @throws CoreRuntimeException If an IIpsSrcFile is contained which can not return the
     *             IIpsObject stored within.
     */
    private Object[] prepare(Object[] rawSources) throws CoreRuntimeException {
        Object[] result = new Object[rawSources.length];

        for (int i = 0; i < result.length; i++) {
            if (rawSources[i] instanceof String) {
                result[i] = new File((String)rawSources[i]);
            } else {
                result[i] = rawSources[i];
            }
        }
        return result;
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
            if (sources[i] instanceof IFile) {
                result[i] = targetFullPath;
            } else if (sources[i] instanceof File) {
                result[i] = targetFullPath;
            }
        }

        return result;
    }

    /**
     * Check all sources to exist and to be saved. If not so, a CoreException will be thrown.
     */
    private void checkSources(Object[] source) throws CoreRuntimeException {
        IpsStatus status = checkSourcesForInvalidContent(source);
        if (status != null) {
            throw new CoreRuntimeException(status);
        }
    }

    /**
     * Check all sources to exist and to be saved. If not so, an IpsStatus containing the error will
     * be returned. Returns <code>null</code> if no error was found.
     */
    public static IpsStatus checkSourcesForInvalidContent(Object[] sources) {
        for (Object currentSource : sources) {
            if (currentSource instanceof IFile) {
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
                String msg = NLS.bind(Messages.MoveOperation_msgUnsupportedObject, currentSource);
                return new IpsStatus(msg);
            }
        }

        return null;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        ICoreRunnable run = monitor1 -> {
            IProgressMonitor pm = monitor1;
            if (monitor1 == null) {
                pm = new NullProgressMonitor();
            }

            pm.beginTask("Move", sourceObjects.length); //$NON-NLS-1$
            for (int i = 0; i < sourceObjects.length; i++) {
                pm.internalWorked(1);
                Object toMove = sourceObjects[i];
                if (toMove instanceof IFile) {
                    moveNoneIpsElement((IFile)sourceObjects[i], targetNames[i], pm);
                } else if (toMove instanceof File) {
                    moveNoneIpsElement((File)sourceObjects[i], targetNames[i], pm);
                }
            }
            pm.done();
        };

        try {
            ResourcesPlugin.getWorkspace().run(run, monitor);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void moveNoneIpsElement(File file, String targetName, IProgressMonitor pm) {
        if (targetProject == null) {
            targetProject = targetRoot.getIpsProject().getProject().unwrap();
        }
        String fileName = file.getAbsolutePath();
        if (fileName.startsWith(targetProject.getLocation().toOSString())) {
            fileName = fileName.substring(targetProject.getLocation().toOSString().length());
        }

        IFile sourceFile = targetProject.getFile(fileName);
        if (sourceFile.exists()) {
            moveNoneIpsElement(sourceFile, targetName, pm);
        } else {
            copyNoneIpsElement(fileName, targetName, pm);
        }
    }

    private void copyNoneIpsElement(final String fileName, final String targetName, final IProgressMonitor pm) {
        Display.getDefault().syncExec(() -> {
            IContainer targetFolder = getTargetContainer(targetName);
            CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
            operation.copyFilesInCurrentThread(new String[] { fileName }, targetFolder, pm);
        });
    }

    private void moveNoneIpsElement(final IFile sourceFile, final String targetName, final IProgressMonitor pm) {
        Display.getDefault().syncExec(() -> {
            final IContainer targetFolder = getTargetContainer(targetName);
            MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(getShell());
            operation.copyResourcesInCurrentThread(new IResource[] { sourceFile }, targetFolder, pm);
        });
    }

    private IContainer getTargetContainer(String targetName) {
        if (targetProject == null) {
            targetProject = targetRoot.getIpsProject().getProject().unwrap();
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

    private Shell getShell() {
        Display display = getDisplay();
        if (display != null) {
            return display.getActiveShell();
        } else {
            return new Shell((Display)null);
        }

    }

    private Display getDisplay() {
        return Display.getDefault();
    }
}
