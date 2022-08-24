/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Utility class needed by the Faktor-IPS refactoring support.
 * 
 * @author Alexander Weickmann
 */
public final class RefactorUtil {

    private RefactorUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Copies the given <code>IIpsSrcFile</code> into a new source file at the desired destination
     * package. Returns a handle to the new <code>IIpsSrcFile</code>.
     * 
     * @param toBeCopied The <code>IIpsSrcFile</code> to be copied.
     * @param targetIpsPackageFragment The destination IPS package.
     * @param copyName The name of the copied source file.
     * @param progressMonitor A progress monitor to report progress to or <code>null</code>.
     * 
     * @throws NullPointerException If <code>toBeCopied</code>,
     *             <code>targetIpsPackageFragment</code> or <code>newName</code> is
     *             <code>null</code>
     * @throws IllegalArgumentException if the source file to be copied is dirty
     */
    public static IIpsSrcFile copyIpsSrcFile(IIpsSrcFile toBeCopied,
            IIpsPackageFragment targetIpsPackageFragment,
            String copyName,
            IProgressMonitor progressMonitor) {

        ArgumentCheck.notNull(new Object[] { toBeCopied, targetIpsPackageFragment, copyName });
        ArgumentCheck.isTrue(!toBeCopied.isDirty());

        Path destinationFolder = targetIpsPackageFragment.getCorrespondingResource().getWorkspaceRelativePath();

        String targetSrcFileName = getTargetFileName(toBeCopied, copyName);
        try {
            Path destinationPath = destinationFolder.resolve(targetSrcFileName);
            toBeCopied.getCorrespondingResource().copy(destinationPath, progressMonitor);
        } catch (InvalidPathException ipe) {
            throw new IpsException(new IpsStatus(ipe));
        }
        return targetIpsPackageFragment.getIpsSrcFile(targetSrcFileName);
    }

    public static String getTargetFileName(IIpsSrcFile toBeCopied, String copyName) {
        return copyName + "." + toBeCopied.getIpsObjectType().getFileExtension(); //$NON-NLS-1$
    }

    /**
     * Copies the given <code>IIpsSrcFile</code> into a temporary source file at the destination
     * package. The file name will contain a time stamp. Returns a handle to the temporary
     * <code>IIpsSrcFile</code>.
     * 
     * @param toBeCopied The <code>IIpsSrcFile</code> to be copied.
     * @param targetIpsPackageFragment The destination IPS package.
     * @param copyName The name of the copied source file.
     * @param progressMonitor A progress monitor to report progress to or <code>null</code>.
     * 
     * @throws NullPointerException If <code>toBeCopied</code>,
     *             <code>targetIpsPackageFragment</code> or <code>newName</code> is
     *             <code>null</code>.
     */
    public static IIpsSrcFile copyIpsSrcFileToTemporary(IIpsSrcFile toBeCopied,
            IIpsPackageFragment targetIpsPackageFragment,
            String copyName,
            IProgressMonitor progressMonitor) {

        ArgumentCheck.notNull(new Object[] { toBeCopied, targetIpsPackageFragment, copyName });

        long timestamp = new Date().getTime();
        return copyIpsSrcFile(toBeCopied, targetIpsPackageFragment, copyName + timestamp, progressMonitor);
    }

    /**
     * Moves the given <code>IIpsSrcFile</code> into a new source file at the desired destination
     * package. Returns a handle to the new <code>IIpsSrcFile</code>.
     * 
     * @param originalSrcFile The <code>IIpsSrcFile</code> to be moved.
     * @param targetIpsPackageFragment The destination IPS package.
     * @param newName The new name of the source file.
     * @param pm A progress monitor to report progress to or <code>null</code>.
     * 
     * @throws NullPointerException If <code>originalSrcFile</code>,
     *             <code>targetIpsPackageFragment</code> or <code>newName</code> is
     *             <code>null</code>
     * @throws IllegalArgumentException if the source file to be copied is dirty
     * 
     */
    public static IIpsSrcFile moveIpsSrcFile(IIpsSrcFile originalSrcFile,
            IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IProgressMonitor pm) {
        // we need to copy and delete the file because at least the subclipse svn adapter get some
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
        final IIpsElement targetElement = targetSrcFile;

        updateSortOrder(originalSrcFile.getIpsPackageFragment(), originalSrcFile, targetIpsPackageFragment,
                targetElement);
        return targetSrcFile;
    }

    public static void updateSortOrder(IIpsPackageFragment originalIpsPackageFragment,
            IIpsElement originalElement,
            IIpsPackageFragment targetIpsPackageFragment,
            final IIpsElement targetElement) {
        if (originalIpsPackageFragment.equals(targetIpsPackageFragment)) {
            // so the child was renamed
            IpsPackageFragment ipsPackageFragment = (IpsPackageFragment)originalIpsPackageFragment;
            Comparator<IIpsElement> childOrderComparator = ipsPackageFragment.getChildOrderComparator();
            if (childOrderComparator instanceof DefinedOrderComparator) {
                IIpsElement[] elements = ((DefinedOrderComparator)childOrderComparator).getElements();
                for (int i = 0; i < elements.length; i++) {
                    if (elements[i].equals(originalElement)) {
                        elements[i] = targetElement;
                        break;
                    }
                }
                ipsPackageFragment.setChildOrderComparator(new DefinedOrderComparator(elements));
            }
        }
        // else the child was moved
        // leave the old element there, so that the element will be in order after an undo
        // operation. It
        // will be removed when the sort order is next updated.
    }

    public static boolean isOnlyCapitalizationChanged(IIpsSrcFile fileToBeCopied, String newName) {
        String oldName = StringUtil.getFilenameWithoutExtension(fileToBeCopied.getName());
        return newName.toLowerCase().equals(oldName.toLowerCase());
    }

}
