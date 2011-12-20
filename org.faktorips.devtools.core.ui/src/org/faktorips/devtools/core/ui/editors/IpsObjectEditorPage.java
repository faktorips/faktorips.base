/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccessWithListenerSupport;
import org.faktorips.devtools.core.ui.IDataChangeableStateChangeListener;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Abstract base class for pages of an IPS object editor.
 */
public abstract class IpsObjectEditorPage extends FormPage implements IDataChangeableReadAccessWithListenerSupport {

    /** The horizontal space between two sections. */
    public final static int HORIZONTAL_SECTION_SPACE = 15;

    /** The vertical space between two sections. */
    public final static int VERTICAL_SECTION_SPACE = 10;

    private UIToolkit uiToolkit;

    private boolean dataChangeable = true;

    private ArrayList<IDataChangeableStateChangeListener> dataChangeableStateChangeListeners;

    /**
     * Creates a new <tt>IpsObjectEditorPage</tt>.
     * 
     * @param editor The editor the page belongs to.
     * @param id Page id used to identify the page.
     * @param tabPageName The page name shown at the bottom of the editor as tab page.
     */
    public IpsObjectEditorPage(IpsObjectEditor editor, String id, String tabPageName) {
        super(editor, id, tabPageName);
        uiToolkit = new UIToolkit(new FormToolkit(Display.getCurrent()));
    }

    /** Returns the <tt>IpsObjectEditor</tt> this page belongs to. */
    public IpsObjectEditor getIpsObjectEditor() {
        return (IpsObjectEditor)getEditor();
    }

    /**
     * Returns the <tt>IIpsObject</tt> of the <tt>IIpsSrcFile</tt> being edited. Returns
     * <tt>null</tt> if the source file couldn't determine the IPS object (e.g. if the source file
     * is stored outside an IPS package).
     */
    public IIpsObject getIpsObject() {
        /*
         * Null checking is necessary since it might be the case that the IPS source file cannot be
         * determined. E.g. in the special case that one tries to open an IPS source file which is
         * not in an IPS package.
         */
        if (getIpsObjectEditor() != null && getIpsObjectEditor().getIpsSrcFile() != null
                && getIpsObjectEditor().getIpsSrcFile().exists()) {
            return getIpsObjectEditor().getIpsObject();
        } else {
            return null;
        }
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        if (getIpsObject() == null) {
            // No valid IPS source file, create nothing
            return;
        }

        ScrolledForm form = managedForm.getForm();
        form.setText(getIpsObjectEditor().getUniformPageTitle());
        createPageContent(form.getBody(), new UIToolkit(managedForm.getToolkit()));
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        form.reflow(true);

        registerSelectionProviderActivation(getPartControl());
    }

    /**
     * Searches for composites that implement the {@link ICompositeWithSelectableViewer} interface
     * and registers them with the {@link SelectionProviderIntermediate} of the
     * {@link IpsObjectEditor}.
     */
    protected final void registerSelectionProviderActivation(final Control container) {
        if (container instanceof ICompositeWithSelectableViewer) {
            Viewer viewer = ((ICompositeWithSelectableViewer)container).getViewer();
            if (viewer != null) {
                getIpsObjectEditor().getSelectionProviderIntermediate().registerListenersFor(viewer);
            }
        }

        if (!(container instanceof Composite)) {
            return;
        }

        for (Control child : ((Composite)container).getChildren()) {
            if (child instanceof Composite) {
                registerSelectionProviderActivation(child);
            }
        }
    }

    /**
     * Subclasses might implement this method to provide the visible content of this page.
     * <p>
     * The default implementation just creates the page toolbar.
     * 
     * @param formBody The root composite where the content of this page needs to be added to.
     * @param toolkit The layout conform toolkit to create widgets with.
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        IToolBarManager toolbarManager = getManagedForm().getForm().getToolBarManager();
        createToolbarActions(toolbarManager);
        getManagedForm().getForm().updateToolBar();
    }

    /**
     * Subclasses might implement this method to provide actions to the page toolbar.
     * <p>
     * The default implementation does nothing.
     * 
     * @param toolbarManager The toolbar manager for the page toolbar to add actions to
     */
    protected void createToolbarActions(IToolBarManager toolbarManager) {
        // The default implementation does nothing
    }

    /**
     * Creates a grid layout for the page with the indicated number of columns and the default
     * margins.
     * 
     * @param numOfColumns Number of columns in the grid.
     * @param equalSize Set to <code>true</code> if the columns should have the same size.
     */
    protected GridLayout createPageLayout(int numOfColumns, boolean equalSize) {
        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;

        return layout;
    }

    /**
     * Creates a grid composite for the inner page structure. The composite has no margins but the
     * default spacing settings.
     * 
     * @param numOfColumns Number of columns in the grid.
     * @param equalSize Set to <tt>true</tt> if the columns should have the same size.
     */
    protected Composite createGridComposite(UIToolkit toolkit,
            Composite parent,
            int numOfColumns,
            boolean equalSize,
            int gridData) {

        Composite composite = toolkit.getFormToolkit().createComposite(parent);

        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;

        composite.setLayout(layout);
        composite.setLayoutData(new GridData(gridData));

        return composite;
    }

    /**
     * Refreshes the page with the data from the model.
     * <p>
     * The default implementation refreshes all ancestors that are instances of <tt>IpsSection</tt>.
     * By ancestors we mean the children of the composite that represents this page and its
     * children.
     */
    public void refresh() {
        if (!(getPartControl() instanceof Composite)) {
            return;
        }

        refresh((Composite)getPartControl());
    }

    private void refresh(Composite composite) {
        Control[] children = composite.getChildren();
        for (Control element : children) {
            if (element instanceof IpsSection) {
                ((IpsSection)element).refresh();
            } else if (element instanceof Composite) {
                refresh((Composite)element);
            }
        }
    }

    /**
     * Returns <tt>true</tt> if the content shown on this page is changeable, otherwise
     * <tt>false</tt>.
     */
    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * Evaluates the new data changeable state and updates it, if it has changed.
     * <p>
     * If the user can't change the data shown in the editor's at all, he also can't change the data
     * shown on this page. If the user can change editor's data in general, the
     * <tt>computeDataChangeableState()</tt> is called to evaluate if the data shown on this page
     * can be changed.
     * 
     * @see #computeDataChangeableState()
     */
    public void updateDataChangeableState() {
        if (!getIpsObjectEditor().isDataChangeable()) {
            setDataChangeable(false);
        } else {
            setDataChangeable(computeDataChangeableState());
        }
    }

    /**
     * Evaluates whether the data shown on this page is changeable by the user. This method does not
     * consider the state of the IPS object editor.
     * <p>
     * The default implementation returns <tt>true</tt>, subclasses may override.
     */
    protected boolean computeDataChangeableState() {
        return true;
    }

    /**
     * Resets the data changeable state to it's default, which is <tt>true</tt>, so that it matches
     * the initial state of controls which are by default enabled / editable.
     */
    protected void resetDataChangeableState() {
        dataChangeable = true;
    }

    private void setDataChangeable(boolean changeable) {
        if (changeable == dataChangeable) {
            return;
        }

        dataChangeable = changeable;
        uiToolkit.setDataChangeable(getPartControl(), changeable);
        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        List<IDataChangeableStateChangeListener> listeners = new CopyOnWriteArrayList<IDataChangeableStateChangeListener>(
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

    @Override
    public void dispose() {
        uiToolkit.dispose();
        super.dispose();
    }

}
