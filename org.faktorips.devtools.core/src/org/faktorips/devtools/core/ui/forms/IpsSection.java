/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.forms;

import java.util.ArrayList;
import java.util.Iterator;
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
 * A section is an area of the user interface.
 */
public abstract class IpsSection extends Composite implements IDataChangeableReadWriteAccess, IDataChangeableReadAccessWithListenerSupport, DisposeListener  {
    
    private Section section;
    private boolean isRefreshing = false;

    private int style;
    private int layoutData;
    private UIToolkit toolkit;
    
    private boolean changeable = true;
    
    protected BindingContext bindingContext = new BindingContext();
    
    private ArrayList dataChangeableStateChangeListeners;

    /**
     * The section to delegate the call of <code>setFocus()</code> to
     * if this section does not contain a control that has to get the focus.
     */
    private IpsSection focusSuccessor;
    
    /**
     * The section which will delegate the call of <code>setFocus()</code>
     * if it does not contain a control that has to get the focus.
     */
    private IpsSection focusPredecessor;
    
    /**
     * The control that has to be focussed if <code>setFocus()</code> of this
     * section is called.
     */
	private Control focusCtrl;
	
	/**
	 * Listener to keep track of the last focussed control of this section. 
	 */
	private FocusListener focusHandler = new FocusListener() {
	
		public void focusGained(FocusEvent e) {
			focusCtrl = (Control)e.getSource();
			
			// to avoid that another sections focusCtrl requests the focus, too, we tell all
			// other sections not to request the focus next time.
			for (IpsSection pre = focusPredecessor; pre != null; pre = pre.focusPredecessor) {
				pre.dontRequestNextFocus();
			}
			for (IpsSection next = focusSuccessor; next != null; next = next.focusSuccessor) {
				next.dontRequestNextFocus();
			}
		}
	
		public void focusLost(FocusEvent e) {
		}
	};

    public IpsSection(
            Composite parent, 
            int style, 
            int layoutData, 
            UIToolkit toolkit) {
        super(parent, SWT.NONE);
        this.style = style;
        this.layoutData = layoutData;
        this.toolkit = toolkit;
        addDisposeListener(this);
    }
    
    /**
     * Constructs the section's controls. This has to to be called explicitly
     * by subclasses <b>after</b> they have initialized any subclass specific
     * instance variables. The <code>IpsSection</code> does not call this
     * method in it's constructor, because in subclasses in might be neccessary
     * to initialize instance variable first, but the call to the super constructor
     * has to be the first statement in the subclass constructor.  
     */
    public void initControls() {
        toolkit.getFormToolkit().adapt(this);
        setLayoutData(new GridData(layoutData));
        setLayout(toolkit.createNoMarginGridLayout(1, false));
        section = toolkit.getFormToolkit().createSection(this, style);
        section.setLayoutData(new GridData(layoutData));
        Composite client = toolkit.createGridComposite(section, 1, false, false);
        client.setLayoutData(new GridData(layoutData));
        initClientComposite(client, toolkit);
        section.setClient(client);
        toolkit.getFormToolkit().paintBordersFor(client);
    }

    protected abstract void initClientComposite(Composite client, UIToolkit toolkit);
    
    protected Section getSectionControl() {
        return section;
    }
    
    public void setText(String text) {
        section.setText(text);
    }
    
    public void setDescription(String description) {
        section.setDescription(description);
    }
    
    public boolean isRefreshing() {
        return isRefreshing;
    }
    
    /**
     * Refreshes the section with the date from the model object(s).
     */
    public void refresh() {
        isRefreshing = true;
        try {
            performRefresh();
        }
        finally {
            isRefreshing = false;
        }
    }
    
    protected abstract void performRefresh();

    /**
     * Returns <code>true</code> if the content in this section can be changed by the user,
     * otherwise <code>false</code>.
     */
    public boolean isDataChangeable() {
        return changeable;
    }
    
    /**
     * Enables or disables that the section's content can be changed by the user.
     */
    public void setDataChangeable(boolean changeable) {
        this.changeable = changeable;
        toolkit.setDataChangeable(section.getClient(), changeable);
        if (dataChangeableStateChangeListeners==null) {
            return;
        }
        List listeners = new ArrayList(dataChangeableStateChangeListeners);
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ((IDataChangeableStateChangeListener)it.next()).dataChangeableStateHasChanged(this);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (listener==null) {
            return;
        }
        if (dataChangeableStateChangeListeners==null) {
            dataChangeableStateChangeListeners = new ArrayList(1);
        }
        this.dataChangeableStateChangeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (dataChangeableStateChangeListeners==null) {
            return;
        }
        dataChangeableStateChangeListeners.remove(listener);
    }
    
    /**
     * Set the successor for focus handling. This successor is only needed
     * if no focusControl is set for this section. If so, the initial setFocus-call
     * is passed to the successor;
     * 
     * @throws NullPointerException if the given successor is <code>null</code>.
     */
    public void setFocusSuccessor(IpsSection successor) {
    	ArgumentCheck.notNull(successor);
    	focusSuccessor = successor;
    	successor.setFocusPredecessor(this);
    }
    
    /**
	 * Add a control that can gain the focus (by user operation or by system calls).
	 * The first control added to this section is the one focussed if this section is initially 
	 * displayed (if there is no focus predecessor).
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
	 * Set focus to the appropriate control. This is the first editable input available
	 * if the focus was not set before or the control that had the focus gained at last. 
	 * 
	 * {@inheritDoc}
	 */
	public boolean setFocus() {
		if (focusCtrl != null) {
			focusCtrl.setFocus();
		} else if (focusSuccessor != null) {
			focusSuccessor.setFocus();
		}
		return true;
	}

	/**
	 * Until a control added with <code>addFocusControl</code> gains the Focus
	 * by user operation, no focus will be requested by this section after a 
	 * call to this method.
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

    /**
     * 
     * {@inheritDoc}
     */
    public void widgetDisposed(DisposeEvent e) {
       if (e.widget==this) {
           bindingContext.dispose();
       }
    }

    /**
     * @return Returns the toolkit.
     */
    public UIToolkit getToolkit() {
        return toolkit;
    }
}
