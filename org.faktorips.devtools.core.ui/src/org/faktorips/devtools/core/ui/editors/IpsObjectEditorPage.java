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

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Abstract base class for pages of an ips object editor.
 */
public abstract class IpsObjectEditorPage extends FormPage implements IDataChangeableReadAccessWithListenerSupport {

    // The horizontal space between two sections
    public final static int HORIZONTAL_SECTION_SPACE = 15;

    // The vertical space between two sections
    public final static int VERTICAL_SECTION_SPACE = 10;

    private UIToolkit uiToolkit;
    private boolean dataChangeable = true;

    private ArrayList<IDataChangeableStateChangeListener> dataChangeableStateChangeListeners;

    /**
     * Creates a new <code>IpsObjectEditorPage</code>.
     * 
     * @param editor The editor the page belongs to.
     * @param id Page id used to identify the page.
     * @param title The title shown at the top of the page when the page is selected.
     * @param tabPageName The page name shown at the bottom of the editor as tab page.
     */
    public IpsObjectEditorPage(IpsObjectEditor editor, String id, String tabPageName) {
        super(editor, id, tabPageName);
        uiToolkit = new UIToolkit(new FormToolkit(Display.getCurrent()));
    }

    /**
     * Returns the <code>IpsObjectEditor</code> this page belongs to.
     */
    protected IpsObjectEditor getIpsObjectEditor() {
        return (IpsObjectEditor)getEditor();
    }

    /**
     * Returns the ips object of the ips src file beeing edited. Returns <code>null</code> if the
     * src file couldn't determine the ips object (e.g. if the src file is stored outside an ips
     * package).
     */
    protected IIpsObject getIpsObject() {
        /*
         * null checking is necessary since it might be the case that the ips source file cannot be
         * determined. E.g. in the special case that one tries to open an ips source file which is
         * not in an ips package.
         */
        if (getIpsObjectEditor() != null && getIpsObjectEditor().getIpsSrcFile() != null
                && getIpsObjectEditor().getIpsSrcFile().exists()) {
            return getIpsObjectEditor().getIpsObject();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        if (getIpsObject() == null) {
            // no valid ips src file, create nothing
            return;
        }

        form.setText(getIpsObjectEditor().getUniformPageTitle());
        FormToolkit toolkit = managedForm.getToolkit();
        createPageContent(form.getBody(), new UIToolkit(toolkit));
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        form.reflow(true);

        registerSelectionProviderActivation(getPartControl());
    }

    protected final void registerSelectionProviderActivation(Control container) {
        if (container instanceof ISelectionProviderActivation) {
            getIpsObjectEditor().getSelectionProviderDispatcher().addSelectionProviderActivation(
                    (ISelectionProviderActivation)container);
        }

        if (!(container instanceof Composite)) {
            return;
        }

        Control[] childs = ((Composite)container).getChildren();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof Composite) {
                registerSelectionProviderActivation(childs[i]);
            }
        }

    }

    /**
     * Subclasses might implement this method to provide the visible content of this page.
     * 
     * @param formBody The root composite where the content of this page needs to be added to.
     * @param toolkit The layout conform toolkit to create widgets with.
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {

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
     * @param toolkit
     * @param parent
     * @param numOfColumns Number of columns in the grid.
     * @param equalSize Set to <code>true</code> if the columns should have the same size.
     * @param gridData
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
     * The default implementation refreshes all ancestors that are instances of
     * <code>IpsSection</code>. By ancestors we mean the children of the composite that represents
     * this page and its children.
     */
    public void refresh() {
        if (!(getPartControl() instanceof Composite)) {
            return;
        }

        refresh((Composite)getPartControl());
    }

    private void refresh(Composite composite) {
        Control[] children = composite.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof IpsSection) {
                ((IpsSection)children[i]).refresh();
            } else if (children[i] instanceof Composite) {
                refresh((Composite)children[i]);
            }
        }
    }

    /**
     * Returns <code>true</code> if the content shown on this page is changeable, otherwise
     * <code>false</code>.
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * <p>
     * Evaluates the new data changeable state and updates it, if it has changed.
     * </p>
     * <p>
     * If the user can't change the data shown in the editor's at all, he also can't change the data
     * shown on this page. If the user can change editor's data in general, the
     * <code>computeDataChangeableState()</code> is called to evaluate if the data shown on this
     * page can be changed.
     * </p>
     * 
     * @see #computeDataChangeableState()
     */
    public void updateDataChangeableState() {
        if (getIpsObjectEditor().isDataChangeable() == null || !getIpsObjectEditor().isDataChangeable().booleanValue()) {
            setDataChangeable(false);
        } else {
            setDataChangeable(computeDataChangeableState());
        }
    }

    /**
     * <p>
     * Evaluates whether the data shown on this page is changeable by the user. This method does not
     * consider the state of the ips object editor.
     * </p>
     * <p>
     * The default implementation returns <code>true</code>, subclasses may override.
     * </p>
     */
    protected boolean computeDataChangeableState() {
        return true;
    }

    /**
     * Resets the data changeable state to it's default, which is <code>true</code>, so that it
     * matches the initial state of controls which are by default enabled / editable.
     */
    protected void resetDataChangeableState() {
        dataChangeable = true;
    }

    private void setDataChangeable(boolean changeable) {
        if (changeable == this.dataChangeable) {
            return;
        }

        this.dataChangeable = changeable;
        uiToolkit.setDataChangeable(getPartControl(), changeable);
        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        List<IDataChangeableStateChangeListener> listeners = new ArrayList<IDataChangeableStateChangeListener>(
                dataChangeableStateChangeListeners);
        for (Iterator<IDataChangeableStateChangeListener> it = listeners.iterator(); it.hasNext();) {
            it.next().dataChangeableStateHasChanged(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (listener == null) {
            return;
        }

        if (dataChangeableStateChangeListeners == null) {
            dataChangeableStateChangeListeners = new ArrayList<IDataChangeableStateChangeListener>(1);
        }

        this.dataChangeableStateChangeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener) {
        if (dataChangeableStateChangeListeners == null) {
            return;
        }

        dataChangeableStateChangeListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        uiToolkit.dispose();
        super.dispose();
    }
}
