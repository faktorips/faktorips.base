/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.util.ArgumentCheck;

/**
 * An abstract base class for contents change listeners that are associated with widgets. These
 * listeners must be removed from the ips model when the widget is disposed. If this listener
 * receives a content changed event when the widget is disposed, the listener consumes the event,
 * otherwise it delegates the call to the template method contentsChangedAndWidgetIsNotDisposed().
 * 
 * @author Jan Ortmann
 */
public abstract class ContentsChangeListenerForWidget implements ContentsChangeListener, DisposeListener {

    private Widget widget;

    public ContentsChangeListenerForWidget() {
        // Default constructor
    }

    public ContentsChangeListenerForWidget(Widget widget) {
        this();
        setWidget(widget);
    }

    public void setWidget(Widget newWidget) {
        ArgumentCheck.notNull(newWidget,
                "Hint: Be sure to create the listener in the methods that are responsible for creating the controls. Sometimes this happens AFTER the object was created. E.g. in IpsSections"); //$NON-NLS-1$
        // widget can still be null
        if (widget != null) {
            widget.removeDisposeListener(this);
        }
        widget = newWidget;
        widget.addDisposeListener(this);
    }

    public Widget getWidget() {
        return widget;
    }

    @Override
    public final void contentsChanged(ContentChangeEvent event) {
        if (widget == null) {
            throw new RuntimeException("The widget hasn't been set!"); //$NON-NLS-1$
        }
        if (!widget.isDisposed()) {
            contentsChangedAndWidgetIsNotDisposed(event);
        }
    }

    public abstract void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event);

    /**
     * Template method that may be extended by subclasses to extend disposal behavior. The default
     * implementation does nothing.
     * 
     * @param e The event that was the cause for the disposal
     */
    public void disposedInternal(DisposeEvent e) {
        // Empty default implementation
    }

    @Override
    public final void widgetDisposed(DisposeEvent e) {
        IIpsModel.get().removeChangeListener(this);
        disposedInternal(e);
    }

}
