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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.views.IpsSrcFileViewItem;

/**
 * The content provider for the instance explorer
 * 
 * @author dirmeier
 * 
 */
public class InstanceContentProvider implements IStructuredContentProvider {

    protected static final IIpsSrcFile[] EMPTY_ARRAY = new IIpsSrcFile[0];

    /*
     * indicates wether to search the subtypes for an instance or not
     */
    private boolean subTypeSearch = true;

    private IpsSrcFileViewItem[] items;

    private IIpsMetaClass ipsMetaClass;

    protected boolean isSubTypeSearch() {
        return subTypeSearch;
    }

    protected void setSubTypeSearch(boolean subTypeSearch) {
        this.subTypeSearch = subTypeSearch;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement != ipsMetaClass) {
            throw new AssertionError("input element not correct");
        }
        return items;
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof IIpsMetaClass) {
            IIpsMetaClass newIpsMetaClass = (IIpsMetaClass)newInput;
            if (ipsMetaClass != newIpsMetaClass) {
                asyncSetInputData(newIpsMetaClass, null);
            }
        }
    }

    public void asyncSetInputData(final IIpsMetaClass element, IJobChangeListener jobListener) {
        Job updatereJob = new Job("Loading Items") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("select items", 100);
                SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 95);
                fillContentProvider(element, subMonitor);
                monitor.worked(5);
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        if (jobListener != null) {
            updatereJob.addJobChangeListener(jobListener);
        }
        updatereJob.schedule();
    }

    private void fillContentProvider(IIpsMetaClass ipsMetaClass, IProgressMonitor monitor) {
        this.ipsMetaClass = ipsMetaClass;
        if (ipsMetaClass == null) {
            items = new IpsSrcFileViewItem[0];
            monitor.done();
            return;
        }
        try {
            IIpsSrcFile[] metaObjectsSrcFiles = ipsMetaClass.findAllMetaObjectSrcFiles(ipsMetaClass.getIpsProject(),
                    subTypeSearch);
            items = IpsSrcFileViewItem.createItems(metaObjectsSrcFiles, ipsMetaClass);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            items = new IpsSrcFileViewItem[0];
        }
    }

    public IIpsMetaClass getActualElement() {
        return ipsMetaClass;
    }
}
