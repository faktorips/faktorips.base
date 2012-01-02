/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.ArgumentCheck;

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

        String targetSrcFileName = copyName + "." + toBeCopied.getIpsObjectType().getFileExtension(); //$NON-NLS-1$
        IPath destinationPath = destinationFolder.append(targetSrcFileName);

        toBeCopied.getCorrespondingResource().copy(destinationPath, true, progressMonitor);
        return targetIpsPackageFragment.getIpsSrcFile(targetSrcFileName);
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

    private RefactorUtil() {
        // Utility class not to be instantiated.
    }

}
