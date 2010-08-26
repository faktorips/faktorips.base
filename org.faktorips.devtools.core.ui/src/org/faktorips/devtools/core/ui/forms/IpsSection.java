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

package org.faktorips.devtools.core.ui.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccessWithListenerSupport;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IDataChangeableStateChangeListener;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.util.ArgumentCheck;

/**
 * A section is an area of the user interface. The <code>IpsSection</code> is a composite that acts
 * as a wrapper for a SWT section.
 */
public abstract class IpsSection extends Composite implements IDataChangeableReadWriteAccess,
        IDataChangeableReadAccessWithListenerSupport, DisposeListener {

    /** The ui control for the section */
    private Section section;

    /** Flag indicating whether the section is currently refreshing */
    private boolean isRefreshing = false;

    /** The SWT style (for example SWT.TITLE_BAR) */
    private int style;

    /** SWT info how to layout the data (for example GridData.FILL_BOTH) */
    private int layoutData;

    /** The ui toolkit to create new ui elements with */
    private UIToolkit toolkit;

    /** Flag indicating whether the content of this section can be changed by the user */
    private boolean changeable;

    private ArrayList<IDataChangeableStateChangeListener> dataChangeableStateChangeListeners;

    /**
     * The section to delegate the call of <code>setFocus()</code> to if this section does not
     * contain a control that has to get the focus.
     */
    private IpsSection focusSuccessor;

    /**
     * The section which will delegate the call of <code>setFocus()</code> to this section if it
     * does not contain a control that has to get the focus.
     */
    private IpsSection focusPredecessor;

    /** The control that has to be focussed if <code>setFocus()</code> of this section is called. */
    private Control focusCtrl;

    /** Binding context to bind ui elements with model data. */
    protected BindingContext bindingContext;

    /**
     * Creates a new <code>IpsSection</code>.
     * 
     * @param parent The parent ui composite.
     * @param style The section style to use, see {@link Section}.
     * @param layoutData How to layout the data within the section.
     * @param toolkit The ui toolkit to create new ui elements with.
     */
    public IpsSection(Composite parent, int style, int layoutData, UIToolkit toolkit) {
        super(parent, SWT.NONE);

        this.style = style;
        this.layoutData = layoutData;
        this.toolkit = toolkit;

        bindingContext = new BindingContext();
        changeable = true;

        addDisposeListener(this);
    }

    /**
     * Constructs the section's controls.
     * <p>
     * This has to to be called explicitly by subclasses <b>after</b> they have initialized any
     * subclass specific instance variables.
     * <p>
     * The <code>IpsSection</code> does not call this method in it's constructor, because in
     * subclasses in might be necessary to initialize instance variable first, but the call to the
     * super constructor has to be the first statement in the subclass constructor.
     */
    protected void initControls() {
        // Adapt this section to the form toolkit
        toolkit.getFormToolkit().adapt(this);

        // Set layout
        setLayoutData(new GridData(layoutData));
        setLayout(toolkit.createNoMarginGridLayout(1, false));

        // Create the ui section widget that is being wrapped by this composite
        section = toolkit.getFormToolkit().createSection(this, style);
        section.setLayoutData(new GridData(layoutData));

        // Create the client composite for the section
        Composite client = toolkit.createGridComposite(section, 1, false, false);
        client.setLayoutData(new GridData(layoutData));
        initClientComposite(client, toolkit);
        section.setClient(client);
        toolkit.getFormToolkit().paintBordersFor(client);
    }

    /**
     * Creates the ui controls for this section. All subclasses must implement this method on their
     * own.
     * 
     * @param client The composite to create the ui elements into.
     * @param toolkit The ui toolkit used to create new ui elements.
     */
    protected abstract void initClientComposite(Composite client, UIToolkit toolkit);

    /**
     * Returns the ui section widget being wrapped by this composite.
     * 
     * @return A handle to the ui section widget being wrapped by this composite.
     */
    protected Section getSectionControl() {
        return section;
    }

    /**
     * Sets the title of the section.
     * 
     * @param text The text to be displayed as title.
     */
    public void setText(String text) {
        section.setText(text);
    }

    /**
     * @see org.eclipse.ui.forms.widgets.Section#setDescription(String)
     */
    public void setDescription(String description) {
        section.setDescription(description);
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    /**
     * Refreshes the section with the data from the model object(s).
     */
    public void refresh() {
        isRefreshing = true;
        try {
            performRefresh();
        } finally {
            isRefreshing = false;
        }
    }

    /**
     * Refresh the section with actual data from the model object(s), called by
     * <code>refresh()</code>.
     * 
     * @see #refresh()
     */
    protected abstract void performRefresh();

    /**
     * Returns whether the contents of this section can be changed by the user.
     */
    @Override
    public boolean isDataChangeable() {
        return changeable;
    }

    /**
     * Enables or disables whether the section's content can be changed by the user.
     */
    @Override
    public void setDataChangeable(boolean changeable) {
        this.changeable = changeable;
        toolkit.setDataChangeable(section.getClient(), changeable);

        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        List<IDataChangeableStateChangeListener> listeners = new ArrayList<IDataChangeableStateChangeListener>(
                dataChangeableStateChangeListeners);
        for (IDataChangeableStateChangeListener iDataChangeableStateChangeListener : listeners) {
            iDataChangeableStateChangeListener.dataChangeableStateHasChanged(this);
        }
    }

    @Override
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (listener == null) {
            return;
        }

        if (dataChangeableStateChangeListeners == null) {
            dataChangeableStateChangeListeners = new ArrayList<IDataChangeableStateChangeListener>(1);
        }

        dataChangeableStateChangeListeners.add(listener);
    }

    @Override
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        dataChangeableStateChangeListeners.remove(listener);
    }

    /**
     * Sets the successor for focus handling. This successor is only needed if no focusControl is
     * set for this section. If so, the initial setFocus-call is passed to the successor;
     * 
     * @throws NullPointerException if the given successor is <code>null</code>.
     */
    public void setFocusSuccessor(IpsSection successor) {
        ArgumentCheck.notNull(successor);

        focusSuccessor = successor;
        successor.setFocusPredecessor(this);
    }

    /**
     * Adds a control that can gain the focus (by user operation or by system calls). The first
     * control added to this section is the one focussed if this section is initially displayed (if
     * there is no focus predecessor).
     * 
     * @throws NullPointerException if the given focusControl is <code>null</code>.
     */
    public void addFocusControl(Control focusControl) {
        ArgumentCheck.notNull(focusControl);

        if (focusCtrl == null) {
            focusCtrl = focusControl;
        }

        focusControl.addFocusListener(focusHandler);
    }

    /**
     * Set focus to the appropriate control. This is the first editable input available if the focus
     * was not set before or the control that had the focus gained at last.
     */
    @Override
    public boolean setFocus() {
        if (focusCtrl != null) {
            focusCtrl.setFocus();
        } else if (focusSuccessor != null) {
            focusSuccessor.setFocus();
        }

        return true;
    }

    /**
     * Until a control added with <code>addFocusControl</code> gains the Focus by user operation, no
     * focus will be requested by this section after a call to this method.
     */
    private void dontRequestNextFocus() {
        focusCtrl = null;
    }

    /**
     * Set the focus predecessor
     */
    private void setFocusPredecessor(IpsSection predecessor) {
        focusPredecessor = predecessor;
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        if (e.widget == this) {
            bindingContext.dispose();
        }
    }

    /**
     * Returns the ui toolkit.
     */
    public UIToolkit getToolkit() {
        return toolkit;
    }

    /**
     * Listener to keep track of the last focused control of this section.
     */
    private FocusListener focusHandler = new FocusListener() {

        @Override
        public void focusGained(FocusEvent e) {
            focusCtrl = (Control)e.getSource();

            /*
             * To avoid that another sections focusCtrl requests the focus, too, we tell all other
             * sections not to request the focus next time.
             */
            for (IpsSection pre = focusPredecessor; pre != null; pre = pre.focusPredecessor) {
                pre.dontRequestNextFocus();
            }
            for (IpsSection next = focusSuccessor; next != null; next = next.focusSuccessor) {
                next.dontRequestNextFocus();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            // nothing to do
        }

    };

}
