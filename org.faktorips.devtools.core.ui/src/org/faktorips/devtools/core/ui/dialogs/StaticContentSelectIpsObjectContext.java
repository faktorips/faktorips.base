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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

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
     * 
     * @param iIpsSrcFiles
     */
    public void setElements(IIpsSrcFile[] iIpsSrcFiles) {
        ipsSrcFiles = Arrays.asList(iIpsSrcFiles);
    }

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

    public List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException {
        return ipsSrcFiles;
    }

    public List<IIpsSrcFile> getLastIpsSrcFiles() {
        return ipsSrcFiles;
    }

}
