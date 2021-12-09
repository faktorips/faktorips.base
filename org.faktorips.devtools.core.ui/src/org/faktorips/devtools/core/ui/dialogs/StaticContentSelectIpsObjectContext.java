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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * In this context for {@link OpenIpsObjectSelectionDialog} the elements have to be set explicitly
 * by calling setElements(..) just before open the dialog
 * 
 * @author Cornelius Dirmeier
 */
public class StaticContentSelectIpsObjectContext implements ISelectIpsObjectContext {

    private List<IIpsSrcFile> ipsSrcFiles;

    /**
     * Setting the content of this context. You have to make sure that setting the elements is
     * called before open the dialog. If you want to implement an asynchrony content selection you
     * have to implement your own {@link ISelectIpsObjectContext}
     */
    public void setElements(IIpsSrcFile[] iIpsSrcFiles) {
        ipsSrcFiles = Arrays.asList(iIpsSrcFiles);
    }

    @Override
    public ViewerFilter getContextFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IIpsSrcFile) {
                    IIpsSrcFile srcFile = (IIpsSrcFile)element;
                    for (IIpsSrcFile allowedSrcFile : ipsSrcFiles) {
                        if (srcFile.equals(allowedSrcFile)) {
                            return true;
                        }
                    }

                }
                return false;
            }
        };
    }

    @Override
    public List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreRuntimeException {
        return ipsSrcFiles;
    }

    public List<IIpsSrcFile> getLastIpsSrcFiles() {
        return ipsSrcFiles;
    }

}
