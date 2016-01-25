/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;

/** Base class for operations that modify property values. */
public abstract class AbstractPropertyValueOperation implements IWorkspaceRunnable {

    private Set<IIpsSrcFile> filesToSave = new HashSet<IIpsSrcFile>();

    public AbstractPropertyValueOperation() {
        super();
    }

    void checkForSave(IPropertyValue propertyValue) {
        IIpsSrcFile ipsSrcFile = propertyValue.getIpsSrcFile();
        if (!ipsSrcFile.isDirty()) {
            filesToSave.add(ipsSrcFile);
        }
    }

    void save(IProgressMonitor monitor) {
        for (IIpsSrcFile ipsSrcFile : filesToSave) {
            try {
                ipsSrcFile.save(true, new SubProgressMonitor(monitor, 1));
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

}