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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class SingleTypeSelectIpsObjectContext implements ISelectIpsObjectContext {

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
    public List<IIpsSrcFile> getIpsSrcFiles(IProgressMonitor progressMonitor) throws CoreException {
        progressMonitor.beginTask("Selecting files...", 4); //$NON-NLS-1$
        progressMonitor.worked(1);
        IIpsSrcFile[] srcFiles = project.findIpsSrcFiles(ipsObjectType);
        progressMonitor.worked(2);
        List<IIpsSrcFile> result = Arrays.asList(srcFiles);
        progressMonitor.done();
        return result;
    }

}
