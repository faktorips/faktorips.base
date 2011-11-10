/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * This is a utility class handling IO operations concerning the eclipse resource framework.
 * 
 * @author dirmeier
 */
public class EclipseIOUtil {

    /**
     * Writes the stream as content to the specified file regards the additional boolean flags and
     * forwards the status to the progress monitor.
     * <p>
     * Use this method instead of directly calling
     * {@link IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)} to get ridge of a
     * possibly defense locking version control system (VCS). This methods calls the
     * {@link IWorkspace#validateEdit(IFile[], Object)} method before writing to the file and gives
     * a VCS the possibility to react for example by checking out the requested file.
     * <p>
     * The method was introduced because of issue FIPS-754 
     * 
     * @param file The file you want to write to.
     * @param inputStream the input stream you want to write to the file
     * @param force whether you want to force the write
     * @param keepHistory keep the file history
     * @param progressMonitor the progress monitor to visible the file writing status
     * @throws CoreException In case of a core exception while writing to the file
     * 
     * @see IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)
     */
    public static void writeToFile(IFile file,
            InputStream inputStream,
            boolean force,
            boolean keepHistory,
            IProgressMonitor progressMonitor) throws CoreException {
        if (!file.isReadOnly()
                || file.getWorkspace().validateEdit(new IFile[] { file }, IWorkspace.VALIDATE_PROMPT).isOK()) {
            file.setContents(inputStream, force, keepHistory, new NullProgressMonitor());
        } else {
            IpsPlugin.log(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                    "Cannot write to file " + file.getFullPath() + ". Maybe it is locked or readonly.")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
