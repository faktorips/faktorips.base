/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

public abstract class ValidationRuleMessagesImportOperation implements IWorkspaceRunnable {

    private final InputStream contents;

    private final IIpsPackageFragmentRoot root;

    private final Locale locale;

    private IProgressMonitor monitor = new NullProgressMonitor();

    private IStatus resultStatus = new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, StringUtils.EMPTY);

    public ValidationRuleMessagesImportOperation(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        this.contents = contents;
        this.root = root;
        this.locale = locale;
    }

    /**
     * @return Returns the resultStatus.
     */
    public IStatus getResultStatus() {
        return resultStatus;
    }

    public InputStream getContents() {
        return contents;
    }

    public IIpsPackageFragmentRoot getPackageFragmentRoot() {
        return root;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public void run(IProgressMonitor progressMonitor) throws CoreException {
        if (progressMonitor != null) {
            this.setMonitor(progressMonitor);
        }
        resultStatus = importContent();
    }

    protected abstract IStatus importContent();

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

}