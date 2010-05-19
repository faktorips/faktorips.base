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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class OpenIpsObjectContext implements ISelectIpsObjectContext {

    private final boolean onlyProductDefinitionTypes;

    public OpenIpsObjectContext(boolean onlyProductDefinitionTypes) {
        this.onlyProductDefinitionTypes = onlyProductDefinitionTypes;
    }

    protected boolean isAllowedSrcFile(IIpsSrcFile srcFile) {
        return !onlyProductDefinitionTypes || srcFile.getIpsObjectType().isProductDefinitionType();
    }

    @Override
    public List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
        progressMonitor.beginTask(
                org.faktorips.devtools.core.ui.actions.Messages.OpenIpsObjectSelectionDialog_processName,
                projects.length * 2);
        for (IIpsProject project : projects) {
            List<IIpsSrcFile> list = new ArrayList<IIpsSrcFile>();
            project.findAllIpsSrcFiles(list);
            progressMonitor.worked(1);
            result.addAll(list);
            progressMonitor.worked(1);
        }
        return result;
    }

    @Override
    public ViewerFilter getContextFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IIpsSrcFile) {
                    IIpsSrcFile srcFile = (IIpsSrcFile)element;
                    return isAllowedSrcFile(srcFile);
                }
                return false;
            }
        };
    }

}
