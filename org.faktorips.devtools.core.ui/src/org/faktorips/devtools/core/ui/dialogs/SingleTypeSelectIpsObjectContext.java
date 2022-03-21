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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class SingleTypeSelectIpsObjectContext extends CachingOpenIpsObjectContext {

    private final IpsObjectType ipsObjectType;
    private final ViewerFilter filter;
    private final IIpsProject project;

    public SingleTypeSelectIpsObjectContext(IIpsProject project, IpsObjectType ipsObjectType, ViewerFilter filter) {
        Assert.isNotNull(project);
        this.project = project;
        this.ipsObjectType = ipsObjectType;
        this.filter = filter;
    }

    @Override
    public ViewerFilter getContextFilter() {
        return filter;
    }

    @Override
    public List<IIpsSrcFile> loadIpsSrcFiles(IProgressMonitor progressMonitor) {
        progressMonitor.beginTask("Selecting files...", 4); //$NON-NLS-1$
        progressMonitor.worked(1);
        IIpsSrcFile[] srcFiles = project.findIpsSrcFiles(ipsObjectType);
        progressMonitor.worked(2);
        List<IIpsSrcFile> result = Arrays.asList(srcFiles);
        progressMonitor.done();
        return result;
    }

}
