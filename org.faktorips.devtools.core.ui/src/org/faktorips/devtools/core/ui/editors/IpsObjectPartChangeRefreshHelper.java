/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * Helper that refreshes a viewer if it observes changes in an IPS object.
 * 
 * To observe changes to an IPS object a change listener is registered with the IPS model. That
 * listener is removed automatically when the viewer is disposed.
 */
public class IpsObjectPartChangeRefreshHelper {
    private final IIpsObject ipsObject;
    private final Viewer viewerToRefresh;
    private final ContentsChangeListener changeListener;
    private final DisposeListener disposeListener;

    /**
     * Creates a helper that refreshed the given viewer when the given IPS object changes.
     * 
     * @param ipsObject the IPS object to observe. Must not be <code>null</code>.
     * @param viewerToRefresh the viewer that should be refreshed on changes on the IPS object. Must
     *            not be <code>null</code>.
     */
    public IpsObjectPartChangeRefreshHelper(IIpsObject ipsObject, Viewer viewerToRefresh) {
        Assert.isNotNull(ipsObject);
        Assert.isNotNull(viewerToRefresh);
        this.viewerToRefresh = viewerToRefresh;
        this.ipsObject = ipsObject;
        changeListener = new ContentsChangeListener() {

            @Override
            public void contentsChanged(ContentChangeEvent event) {
                handleEvent(event);
            }

        };
        disposeListener = new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        };
    }

    public static IpsObjectPartChangeRefreshHelper createAndInit(IIpsObject ipsObject, Viewer viewerToRefresh) {
        IpsObjectPartChangeRefreshHelper helper = new IpsObjectPartChangeRefreshHelper(ipsObject, viewerToRefresh);
        helper.init();
        return helper;
    }

    /**
     * Initializes the helper, adds a change listener to the IPS model.
     */
    public void init() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(changeListener);
        viewerToRefresh.getControl().addDisposeListener(disposeListener);
    }

    protected void handleEvent(ContentChangeEvent event) {
        if (event.isAffected(ipsObject)) {
            viewerToRefresh.refresh();
        }
    }

    /**
     * Removes the change listener from the IPS model.
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(changeListener);
    }

}
