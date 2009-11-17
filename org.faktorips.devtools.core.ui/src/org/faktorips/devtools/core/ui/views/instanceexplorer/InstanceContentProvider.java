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

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;
import org.faktorips.devtools.core.ui.views.IpsSrcFileViewItem;

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

    public void dispose() {

    }

    private IpsSrcFileViewItem[] collectIpsSrcFileViewItems(IIpsMetaClass ipsMetaClass, IProgressMonitor monitor) {
        this.ipsMetaClass = ipsMetaClass;
        if (ipsMetaClass == null) {
            monitor.done();
            return new IpsSrcFileViewItem[0];
        }
        try {
            monitor.beginTask(getWaitingLabel(), 2);
            IIpsSrcFile[] metaObjectsSrcFiles = ipsMetaClass.findAllMetaObjectSrcFiles(ipsMetaClass.getIpsProject(),
                    subTypeSearch);
            monitor.worked(1);
            IpsSrcFileViewItem[] result = IpsSrcFileViewItem.createItems(metaObjectsSrcFiles, ipsMetaClass);
            monitor.worked(1);
            monitor.done();
            return result;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new IpsSrcFileViewItem[0];
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

    @Override
    protected String getWaitingLabel() {
        return "collecting instances...";
    }
}
