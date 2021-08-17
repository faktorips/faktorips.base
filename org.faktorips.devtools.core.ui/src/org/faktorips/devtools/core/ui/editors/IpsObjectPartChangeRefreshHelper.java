/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.swt.events.DisposeListener;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;

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
     * <p>
     * This constructor is package private because you should use the factory method
     * {@link #createAndInit(IIpsObject, Viewer)}.
     * 
     * @param ipsObject the IPS object to observe. Must not be <code>null</code>.
     * @param viewerToRefresh the viewer that should be refreshed on changes on the IPS object. Must
     *            not be <code>null</code>.
     */
    IpsObjectPartChangeRefreshHelper(IIpsObject ipsObject, Viewer viewerToRefresh) {
        Assert.isNotNull(ipsObject);
        Assert.isNotNull(viewerToRefresh);
        this.viewerToRefresh = viewerToRefresh;
        this.ipsObject = ipsObject;
        changeListener = this::handleEvent;
        disposeListener = $ -> dispose();
    }

    /**
     * This factory method creates a new {@link IpsObjectPartChangeRefreshHelper} and initializes
     * the refresh listener. The created helper is returned. However normally you do not need the
     * helper because it should run and dispose totally self-sufficient.
     * <p>
     * If either the {@link IIpsObject} or the {@link Viewer} is null this method does nothing and
     * returns <code>null</code>
     * 
     * @param ipsObject The {@link IIpsObject} for which you want to register the refresh
     * @param viewerToRefresh The {@link Viewer} that needs to be refreshed when ipsObject has
     *            changed
     * @return The {@link IpsObjectPartChangeRefreshHelper} that was created and initialized or
     *         <code>null</code> if it could not be created
     */
    public static IpsObjectPartChangeRefreshHelper createAndInit(IIpsObject ipsObject, Viewer viewerToRefresh) {
        if (ipsObject != null && viewerToRefresh != null) {
            IpsObjectPartChangeRefreshHelper helper = new IpsObjectPartChangeRefreshHelper(ipsObject, viewerToRefresh);
            helper.init();
            return helper;
        } else {
            return null;
        }
    }

    /**
     * Initializes the helper, adds a change listener to the IPS model.
     */
    public void init() {
        IIpsModel.get().addChangeListener(changeListener);
        viewerToRefresh.getControl().addDisposeListener(disposeListener);
    }

    protected void handleEvent(ContentChangeEvent event) {
        if (event.isAffected(ipsObject)) {
            if (!viewerToRefresh.getControl().isDisposed()) {
                viewerToRefresh.refresh();
            }
        }
    }

    /**
     * Removes the change listener from the IPS model.
     */
    public void dispose() {
        IIpsModel.get().removeChangeListener(changeListener);
    }

}
