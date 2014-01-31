/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Utility class needed by the Faktor-IPS refactoring support.
 * 
 * @author Alexander Weickmann
 */
public final class RefactorUtil {

    /**
     * Copies the given <tt>IIpsSrcFile</tt> into a new source file at the desired destination
     * package. Returns a handle to the new <tt>IIpsSrcFile</tt>.
     * 
     * @param toBeCopied The <tt>IIpsSrcFile</tt> to be copied.
     * @param targetIpsPackageFragment The destination IPS package.
     * @param copyName The name of the copied source file.
     * @param progressMonitor A progress monitor to report progress to or <tt>null</tt>.
     * 
     * @throws NullPointerException If <tt>toBeCopied</tt>, <tt>targetIpsPackageFragment</tt> or
     *             <tt>newName</tt> is <tt>null</tt>
     * @throws IllegalArgumentException if the source file to be copied is dirty
     */
    public static final IIpsSrcFile copyIpsSrcFile(IIpsSrcFile toBeCopied,
            IIpsPackageFragment targetIpsPackageFragment,
            String copyName,
            IProgressMonitor progressMonitor) throws CoreException {

        ArgumentCheck.notNull(new Object[] { toBeCopied, targetIpsPackageFragment, copyName });
        ArgumentCheck.isTrue(!toBeCopied.isDirty());

        IPath destinationFolder = targetIpsPackageFragment.getCorrespondingResource().getFullPath();

        String targetSrcFileName = getTargetFileName(toBeCopied, copyName);
        IPath destinationPath = destinationFolder.append(targetSrcFileName);

        toBeCopied.getCorrespondingResource().copy(destinationPath, true, progressMonitor);
        return targetIpsPackageFragment.getIpsSrcFile(targetSrcFileName);
    }

    public static String getTargetFileName(IIpsSrcFile toBeCopied, String copyName) {
        return copyName + "." + toBeCopied.getIpsObjectType().getFileExtension();//$NON-NLS-1$
    }

    /**
     * Copies the given <tt>IIpsSrcFile</tt> into a temporary source file at the destination
     * package. The file name will contain a time stamp. Returns a handle to the temporary
     * <tt>IIpsSrcFile</tt>.
     * 
     * @param toBeCopied The <tt>IIpsSrcFile</tt> to be copied.
     * @param targetIpsPackageFragment The destination IPS package.
     * @param copyName The name of the copied source file.
     * @param progressMonitor A progress monitor to report progress to or <tt>null</tt>.
     * 
     * @throws NullPointerException If <tt>toBeCopied</tt>, <tt>targetIpsPackageFragment</tt> or
     *             <tt>newName</tt> is <tt>null</tt>.
     */
    public static final IIpsSrcFile copyIpsSrcFileToTemporary(IIpsSrcFile toBeCopied,
            IIpsPackageFragment targetIpsPackageFragment,
            String copyName,
            IProgressMonitor progressMonitor) throws CoreException {

        ArgumentCheck.notNull(new Object[] { toBeCopied, targetIpsPackageFragment, copyName });

        long timestamp = new Date().getTime();
        return copyIpsSrcFile(toBeCopied, targetIpsPackageFragment, copyName + timestamp, progressMonitor);
    }

    /**
     * Moves the given <tt>IIpsSrcFile</tt> into a new source file at the desired destination
     * package. Returns a handle to the new <tt>IIpsSrcFile</tt>.
     * 
     * @param originalSrcFile The <tt>IIpsSrcFile</tt> to be moved.
     * @param targetIpsPackageFragment The destination IPS package.
     * @param newName The new name of the source file.
     * @param pm A progress monitor to report progress to or <tt>null</tt>.
     * 
     * @throws NullPointerException If <tt>originalSrcFile</tt>, <tt>targetIpsPackageFragment</tt>
     *             or <tt>newName</tt> is <tt>null</tt>
     * @throws IllegalArgumentException if the source file to be copied is dirty
     * 
     */
    public static IIpsSrcFile moveIpsSrcFile(IIpsSrcFile originalSrcFile,
            IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IProgressMonitor pm) throws CoreException {
        // we need to copy and delete the file because at least the subclipse svn adaptder get some
        // problems when we moving the files there and back again twice
        IIpsSrcFile targetSrcFile;
        if (targetIpsPackageFragment.equals(originalSrcFile.getIpsPackageFragment())
                && isOnlyCapitalizationChanged(originalSrcFile, newName)) {
            IIpsSrcFile tempSrcFile = copyIpsSrcFileToTemporary(originalSrcFile, targetIpsPackageFragment, newName, pm);
            originalSrcFile.delete();
            targetSrcFile = copyIpsSrcFile(tempSrcFile, targetIpsPackageFragment, newName, pm);
            tempSrcFile.delete();
        } else {
            targetSrcFile = copyIpsSrcFile(originalSrcFile, targetIpsPackageFragment, newName, pm);
            originalSrcFile.delete();
        }
        return targetSrcFile;
    }

    public static boolean isOnlyCapitalizationChanged(IIpsSrcFile fileToBeCopied, String newName) {
        String oldName = StringUtil.getFilenameWithoutExtension(fileToBeCopied.getName());
        return newName.toLowerCase().equals(oldName.toLowerCase());
    }

    private RefactorUtil() {
        // Utility class not to be instantiated.
    }

}
