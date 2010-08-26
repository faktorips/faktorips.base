/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.instanceexplorer;

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
     * indicates wether to search the subtypes for an instance or not
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
            IIpsSrcFile[] metaObjectsSrcFiles = ipsMetaClass.searchMetaObjectSrcFiles(subTypeSearch);
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

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        if (inputElement instanceof IIpsMetaClass) {
            IIpsMetaClass ipsMetaClass = (IIpsMetaClass)inputElement;
            return collectIpsSrcFileViewItems(ipsMetaClass, monitor);
        }
        return null;
    }

    // TODO AW: Internationalize?
    @Override
    protected String getWaitingLabel() {
        return "collecting instances..."; //$NON-NLS-1$
    }

}
