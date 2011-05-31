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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * This implementation of {@link ISelectIpsObjectContext} supports a lazy loading mechanism getting
 * the list of source files only on first access and caching the result. This should be the
 * preferred way for {@link ISelectIpsObjectContext} because loading the files before opening the
 * dialog would not gives enough response to the user.
 * 
 * @author dirmeier
 */
public abstract class CachingOpenIpsObjectContext implements ISelectIpsObjectContext {

    // volatile for double checking ideom
    public volatile List<IIpsSrcFile> srcFiles;

    @Override
    public final List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException {
        if (srcFiles == null) {
            synchronized (this) {
                if (srcFiles == null) {
                    srcFiles = new ArrayList<IIpsSrcFile>();
                    srcFiles = loadIpsSrcFiles(progressMonitor);
                }
            }
        }
        return srcFiles;
    }

    /**
     * This method is only called once for this context when loading the list of {@link IIpsSrcFile}
     * s the first time. This method may be performance intensive.
     * 
     * @param progressMonitor The {@link IProgressMonitor} showing the state of loading the source
     *            files
     * @return the list of {@link IIpsSrcFile} to show in the select dialog
     */
    public abstract List<IIpsSrcFile> loadIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException;

}
