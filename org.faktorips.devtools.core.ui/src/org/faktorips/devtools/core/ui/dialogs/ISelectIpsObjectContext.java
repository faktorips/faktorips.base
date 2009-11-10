/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A class implementing this interface setting the context for an
 * {@link OpenIpsObjectSelectionDialog}.
 * 
 * @author Cornelius Dirmeier
 */
public interface ISelectIpsObjectContext {

    /**
     * Called by the dialog to get the content elements. Use the progress monitor to visualize work
     * progress.
     * 
     * @param progressMonitor
     * @return
     * @throws CoreException
     */
    public abstract List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException;

    /**
     * Configure a viewer filter to include or exclude some objects. Note that this filter is
     * important because the Dialog does not only show objects collected by
     * {@link #getIpsSrcFiles(IProgressMonitor)} but also elements from the history.
     * 
     * @return
     */
    public abstract ViewerFilter getContextFilter();

}