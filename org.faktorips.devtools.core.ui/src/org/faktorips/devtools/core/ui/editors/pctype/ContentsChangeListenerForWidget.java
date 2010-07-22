/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
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
        ArgumentCheck
                .notNull(
                        newWidget,
                        "Hint: Be sure to create the listener in the methods that are responsible for creating the controls. Sometimes this happens AFTER the object was created. E.g. in IpsSections"); //$NON-NLS-1$
        if (widget != null) { // widget can still be null
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
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        disposedInternal(e);
    }

}
