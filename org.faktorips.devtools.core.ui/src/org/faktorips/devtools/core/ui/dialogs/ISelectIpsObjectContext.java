/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
     */
    public abstract List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException;

    /**
     * Configure a viewer filter to include or exclude some objects. Note that this filter is
     * important because the Dialog does not only show objects collected by
     * {@link #getIpsSrcFiles(IProgressMonitor)} but also elements from the history.
     */
    public abstract ViewerFilter getContextFilter();

}
