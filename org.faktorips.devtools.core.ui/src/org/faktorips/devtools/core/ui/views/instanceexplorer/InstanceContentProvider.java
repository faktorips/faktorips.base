/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;
import org.faktorips.devtools.core.ui.views.InstanceIpsSrcFileViewItem;

/**
 * The content provider for the instance explorer
 * 
 * @author dirmeier
 * 
 */
public class InstanceContentProvider extends DeferredStructuredContentProvider {

    protected static final IIpsSrcFile[] EMPTY_ARRAY = new IIpsSrcFile[0];

    /*
     * indicates whether to search the subtypes for an instance or not
     */
    private boolean subTypeSearch = true;

    private IIpsMetaClass ipsMetaClass;

    protected boolean isSubTypeSearch() {
        return subTypeSearch;
    }

    protected void setSubTypeSearch(boolean subTypeSearch) {
        this.subTypeSearch = subTypeSearch;
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    private InstanceIpsSrcFileViewItem[] collectIpsSrcFileViewItems(IIpsMetaClass ipsMetaClass, IProgressMonitor monitor) {
        this.ipsMetaClass = ipsMetaClass;
        if (ipsMetaClass == null) {
            monitor.done();
            return new InstanceIpsSrcFileViewItem[0];
        }
        try {
            monitor.beginTask(getWaitingLabel(), 2);
            Collection<IIpsSrcFile> metaObjectsSrcFiles = ipsMetaClass.searchMetaObjectSrcFiles(subTypeSearch);
            monitor.worked(1);
            InstanceIpsSrcFileViewItem[] result = InstanceIpsSrcFileViewItem.createItems(metaObjectsSrcFiles,
                    ipsMetaClass);
            monitor.worked(1);
            monitor.done();
            return result;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new InstanceIpsSrcFileViewItem[0];
        }
    }

    public IIpsMetaClass getActualElement() {
        return ipsMetaClass;
    }

    public void removeActualElement() {
        ipsMetaClass = null;
    }

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        if (inputElement instanceof IIpsMetaClass) {
            IIpsMetaClass ipsMetaClass = (IIpsMetaClass)inputElement;
            return collectIpsSrcFileViewItems(ipsMetaClass, monitor);
        }
        return null;
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.InstanceExplorer_waitingLabel;
    }

}
