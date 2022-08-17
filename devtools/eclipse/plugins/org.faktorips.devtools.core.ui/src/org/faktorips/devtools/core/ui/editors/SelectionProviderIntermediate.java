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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;

/**
 * IPostSelectionProvider implementation that delegates to another ISelectionProvider or
 * IPostSelectionProvider. The selection provider used for delegation can be exchanged dynamically.
 * Registered listeners are adjusted accordingly. This utility class may be used in workbench parts
 * with multiple viewers .
 * <p>
 * This class is copied from an article by Marc R. Hoffmann:
 * http://www.eclipse.org/articles/Article-WorkbenchSelections/article.html
 * 
 * @author Marc R. Hoffmann
 */
public class SelectionProviderIntermediate implements IPostSelectionProvider {

    private final ListenerList<ISelectionChangedListener> selectionListeners = new ListenerList<>();

    private final ListenerList<ISelectionChangedListener> postSelectionListeners = new ListenerList<>();

    private ISelectionProvider delegate;

    private ISelectionChangedListener selectionListener = event -> {
        if (event.getSelectionProvider() == delegate) {
            fireSelectionChanged(event.getSelection());
        }
    };

    private ISelectionChangedListener postSelectionListener = event -> {
        if (event.getSelectionProvider() == delegate) {
            firePostSelectionChanged(event.getSelection());
        }
    };

    /**
     * Sets a new selection provider to delegate to. Selection listeners registered with the
     * previous delegate are removed before.
     * 
     * @param newDelegate new selection provider
     */
    public void setSelectionProviderDelegate(ISelectionProvider newDelegate) {
        if (delegate == newDelegate) {
            return;
        }
        if (delegate != null) {
            delegate.removeSelectionChangedListener(selectionListener);
            if (delegate instanceof IPostSelectionProvider) {
                ((IPostSelectionProvider)delegate).removePostSelectionChangedListener(postSelectionListener);
            }
            fireSelectionChanged(StructuredSelection.EMPTY);
            firePostSelectionChanged(StructuredSelection.EMPTY);
        }
        delegate = newDelegate;
        if (newDelegate != null) {
            newDelegate.addSelectionChangedListener(selectionListener);
            if (newDelegate instanceof IPostSelectionProvider) {
                ((IPostSelectionProvider)newDelegate).addPostSelectionChangedListener(postSelectionListener);
            }
            fireSelectionChanged(newDelegate.getSelection());
            firePostSelectionChanged(newDelegate.getSelection());
        }
    }

    protected void fireSelectionChanged(ISelection selection) {
        fireSelectionChanged(selectionListeners, selection);
    }

    protected void firePostSelectionChanged(ISelection selection) {
        fireSelectionChanged(postSelectionListeners, selection);
    }

    private void fireSelectionChanged(ListenerList<?> list, ISelection selection) {
        SelectionChangedEvent event = new SelectionChangedEvent(delegate, selection);
        Object[] listeners = list.getListeners();
        for (Object listener2 : listeners) {
            ISelectionChangedListener listener = (ISelectionChangedListener)listener2;
            listener.selectionChanged(event);
        }
    }

    /**
     * This method registers a listener on the given viewer's control to set the given viewer on
     * focus gained event and remove it on focus lost. It also adds a dispose listener to remove the
     * focus listener on dispose.
     * 
     * @param viewer the viewer that should be activated on controls focus gained
     */
    public void registerListenersFor(final Viewer viewer) {
        final Control control = viewer.getControl();
        final FocusListener listener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setSelectionProviderDelegate(viewer);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setSelectionProviderDelegate(null);
            }

        };
        control.addFocusListener(listener);
        control.addDisposeListener($ -> control.removeFocusListener(listener));
    }

    // IPostSelectionProvider Implementation

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selectionListeners.add(listener);
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        selectionListeners.remove(listener);
    }

    @Override
    public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
        postSelectionListeners.add(listener);
    }

    @Override
    public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
        postSelectionListeners.remove(listener);
    }

    @Override
    public ISelection getSelection() {
        return delegate == null ? null : delegate.getSelection();
    }

    @Override
    public void setSelection(ISelection selection) {
        if (delegate != null) {
            delegate.setSelection(selection);
        }
    }
}
