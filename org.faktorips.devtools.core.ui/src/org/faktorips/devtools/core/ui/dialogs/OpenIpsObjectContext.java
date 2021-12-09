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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This is the default context for the {@link OpenIpsObjectSelectionDialog}. Per default it selects
 * all IPS objects, you can specify a filter to select product definition types only.
 * 
 * @author dirmeier
 */
public class OpenIpsObjectContext extends CachingOpenIpsObjectContext {

    private final boolean onlyProductDefinitionTypes;

    public OpenIpsObjectContext(boolean onlyProductDefinitionTypes) {
        this.onlyProductDefinitionTypes = onlyProductDefinitionTypes;
    }

    protected boolean isAllowedSrcFile(IIpsSrcFile srcFile) {
        return !onlyProductDefinitionTypes || srcFile.getIpsObjectType().isProductDefinitionType();
    }

    @Override
    public List<IIpsSrcFile> loadIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreRuntimeException {
        List<IIpsSrcFile> result = new ArrayList<>();
        IIpsProject[] projects = IIpsModel.get().getIpsProjects();
        progressMonitor.beginTask(
                org.faktorips.devtools.core.ui.actions.Messages.OpenIpsObjectSelectionDialog_processName,
                projects.length * 2);
        for (IIpsProject project : projects) {
            List<IIpsSrcFile> list = new ArrayList<>();
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
