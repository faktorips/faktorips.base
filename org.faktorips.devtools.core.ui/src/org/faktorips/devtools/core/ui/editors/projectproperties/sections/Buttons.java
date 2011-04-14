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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.ISelectionProviderActivation;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.core.ui.editors.projectproperties.Messages;

/**
 * A composite that shows parts in a table viewer and provides an area containing a new, edit and
 * delete button.
 */
public abstract class Buttons extends ViewerButtonComposite implements ISelectionProviderActivation,
        ISelectionProvider, IDataChangeableReadWriteAccess {

    protected Button newButton;
    protected Button editButton;
    protected Button deleteButton;
    protected Button upButton;
    protected Button downButton;

    /** Flag that controls if a new part can be created. */
    private boolean canCreate;

    /** Flag that controls if a part can be edited. */
    private boolean canEdit;

    /** Flag that controls if a part can be deleted. */
    private boolean canDelete;

    /** Flag that controls if parts can be moved. */
    private boolean canMove;

    /** Table to show the content */
    private Table table;

    /** Listener to start editing on double click. */
    private MouseAdapter editDoubleClickListener;

    /**
     * Flag that control if the edit button is shown (edit is also possible via double click.
     */
    private boolean showEditButton;

    /** The table view of this composite */
    protected TableViewer viewer;

    protected UIToolkit uiToolkit;

    protected IIpsProjectProperties iIpsProjectProperties;

    private Section section;
    private GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

    private int style;

    public Buttons(IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit, int style) {
        this(iIpsProjectProperties, parent, true, true, true, true, true, toolkit, style);
    }

    public Buttons(IIpsProjectProperties iIpsProjectProperties, Composite parent, boolean canCreate, boolean canEdit,
            boolean canDelete, boolean canMove, boolean showEditButton, UIToolkit toolkit, int style) {

        super(parent);
        this.iIpsProjectProperties = iIpsProjectProperties;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canMove = canMove;
        this.showEditButton = showEditButton;
        uiToolkit = toolkit;
        toolkit.getFormToolkit().adapt(this);
        this.style = style;
    }

    public UIToolkit getUiToolkit() {
        return uiToolkit;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    @Override
    public boolean isDataChangeable() {
        return canCreate || canEdit || canMove || canDelete;
    }

    /** Determines whether the refactor context menu is shown. */
    public boolean isRefactoringSupported() {
        return false;
    }

    @Override
    public void setDataChangeable(boolean flag) {
        table.setEnabled(true);
        canCreate = flag;
        canEdit = flag;
        canDelete = flag;
        canMove = flag;
        updateButtonEnabledStates();
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
        updateButtonEnabledStates();
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
        updateButtonEnabledStates();
    }

    public boolean isCanEdit() {
        return canEdit;

    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        updateButtonEnabledStates();
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
        updateButtonEnabledStates();
    }

    @Override
    protected Viewer createViewer(Composite parent, UIToolkit toolkit) {
        table = createTable(parent, toolkit);

        setEditDoubleClickListenerEnabled(true);
        registerOpenLinkListener();

        viewer = new TableViewer(table);

        viewer.setContentProvider(createContentProvider());
        ILabelProvider lp = createLabelProvider();
        viewer.setLabelProvider(lp);
        fillViewer();
        return viewer;
    }

    public void createDefinedDatatypeTable() {

    }

    abstract protected void fillViewer();

    protected void initControls() {
        // Create the UI section widget that is being wrapped by this composite
        section = uiToolkit.getFormToolkit().createSection(this, style);
        section.setLayoutData(layoutData);
    }

    protected void initClientComposite(Composite client, UIToolkit toolkit) {

    }

    public void setText(String text) {

    }

    /**
     * Create the table for the viewer in this component. If you want to create another Table for
     * example a table with a different style you could override this method
     * 
     * @param parent the composite the table is nested in
     * @param toolkit the toolkit to create the table
     * @return the new table
     */
    protected Table createTable(Composite parent, UIToolkit toolkit) {
        return toolkit.getFormToolkit().createTable(parent, SWT.NONE);
    }

    /**
     * If mouse down and the CTRL key is pressed then the open link method is called
     */
    private void registerOpenLinkListener() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    openLink();
                }
            }
        };
        table.addMouseListener(adapter);
    }

    /**
     * Open the element in a new editor if the CTRL key was pressed durring selection. Subclasses
     * are indent to overwrite this method if this functionality is available.
     */
    protected void openLink() {
        // nothing do do
    }

    /**
     * Enable or disable the listener to start editing on double click.
     * 
     * @param enabled <code>true</code> to enable editing on double click.
     */
    protected void setEditDoubleClickListenerEnabled(boolean enabled) {
        if (enabled) {
            if (editDoubleClickListener == null) {
                editDoubleClickListener = new MouseAdapter() {
                    @Override
                    public void mouseDoubleClick(MouseEvent e) {
                        editPart();
                    }
                };
            }
            table.addMouseListener(editDoubleClickListener);
        } else if (editDoubleClickListener != null) {
            table.removeMouseListener(editDoubleClickListener);
        }
    }

    /**
     * Creates the content provider for the table viewer.
     */
    protected abstract IStructuredContentProvider createContentProvider();

    /**
     * Creates the label provider for the table viewer. Returns the default label provider by
     * default
     */
    protected ILabelProvider createLabelProvider() {
        return new DefaultLabelProvider();
    }

    /**
     * Creates new, edit and delete buttons (if enabled). Can be overridden if other buttons are
     * needed.
     */
    @Override
    protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
        boolean buttonCreated = false;
        if (canCreate) {
            createNewButton(buttons, toolkit);
            buttonCreated = true;
        }
        if (canEdit && showEditButton) {
            createEditButton(buttons, toolkit);
            buttonCreated = true;
        }
        if (canDelete) {
            createDeleteButton(buttons, toolkit);
            buttonCreated = true;
        }
        if (canMove) {
            if (buttonCreated) {
                createButtonSpace(buttons, toolkit);
            }
            createMoveButtons(buttons, toolkit);
            buttonCreated = true;
        }
        return buttonCreated;
    }

    protected final void createButtonSpace(Composite buttons, UIToolkit toolkit) {
        Label spacer = toolkit.createLabel(buttons, null);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        data.heightHint = 5;
        spacer.setLayoutData(data);
    }

    protected final void createNewButton(Composite buttons, UIToolkit toolkit) {
        newButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonNew);
        newButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        newButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    newPart();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createEditButton(Composite buttons, UIToolkit toolkit) {
        editButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonEdit);
        editButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        editButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    editPart();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createDeleteButton(Composite buttons, UIToolkit toolkit) {
        deleteButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonDelete);
        deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        deleteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    deleteDatatype();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createMoveButtons(Composite buttons, UIToolkit toolkit) {
        upButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonUp);
        upButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        upButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveParts(true);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
        downButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonDown);
        downButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        downButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveParts(false);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

    }

    @Override
    protected void updateButtonEnabledStates() {
        boolean itemSelected = false;

        if (getViewer().getSelection() != null && !getViewer().getSelection().isEmpty()) {
            itemSelected = true;
        }

        if (newButton != null) {
            newButton.setEnabled(canCreate);
        }

        if (editButton != null) {
            editButton.setEnabled(itemSelected);
            editButton
                    .setText((canEdit ? Messages.IpsPartsComposite_buttonEdit : Messages.IpsPartsComposite_buttonShow));
        }

        if (deleteButton != null) {
            deleteButton.setEnabled(itemSelected && canDelete);
        }

        if (upButton != null) {
            upButton.setEnabled(itemSelected && canMove && !isFirstElementSelected());
        }

        if (downButton != null) {
            downButton.setEnabled(itemSelected && canMove && !isLastElementSelected());
        }
    }

    /**
     * In case this partcomposite's viewer is a {@link TableViewer} this method returns
     * <code>true</code> if the viewers selected element is the first element in the table.
     * <code>false</code> else.
     * 
     * @return <code>true</code> only if the table-viewers selected element is the first element in
     *         the table.
     */
    private boolean isFirstElementSelected() {
        return isFirstOrLastElementSelected(true);
    }

    /**
     * In case this partcomposite's viewer is a {@link TableViewer} this method returns
     * <code>true</code> if the viewers selected element is the last element in the table.
     * <code>false</code> else.
     * 
     * @return <code>true</code> only if the table-viewers selected element is the last element in
     *         the table.
     */
    private boolean isLastElementSelected() {
        return isFirstOrLastElementSelected(false);
    }

    private boolean isFirstOrLastElementSelected(boolean firstElement) {
        Viewer viewer = getViewer();
        if (viewer instanceof TableViewer) {
            IStructuredContentProvider contentProvider = (IStructuredContentProvider)((TableViewer)viewer)
                    .getContentProvider();
            Object selectedObject = getSelectedObject();
            Object[] elements = contentProvider.getElements(null);
            if (selectedObject != null && elements.length > 0
                    && elements[firstElement ? 0 : elements.length - 1] == selectedObject) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ISelection getSelection() {
        return getViewer().getSelection();
    }

    @Override
    public void setSelection(ISelection selection) {
        getViewer().setSelection(selection);
    }

    private void newPart() throws CoreException {
        EditDialog dialog = createEditDialog(null, getShell());
        if (dialog == null) {
            return;
        }
        dialog.open();
        if (dialog.getReturnCode() == Window.CANCEL) {
            return;
        }
    }

    protected void editPart() {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }
        ISelection selection = viewer.getSelection();
        EditDialog dialog = null;
        if (selection instanceof StructuredSelection) {
            StructuredSelection structuredSelection = (StructuredSelection)selection;
            try {
                dialog = createEditDialog(structuredSelection.getFirstElement(), getShell());
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        if (dialog == null) {
            return;
        }
        dialog.open();
    }

    private void deleteDatatype() {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }

        try {
            Table table = (Table)getViewer().getControl();
            int selectedIndexAfterDeletion = Math.min(table.getSelectionIndex(), table.getItemCount() - 2);
            deleteItem();
            if (selectedIndexAfterDeletion >= 0) {
                Object selected = viewer.getElementAt(selectedIndexAfterDeletion);
                viewer.setSelection(new StructuredSelection(selected), true);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        refresh();
    }

    public abstract void deleteItem();

    /**
     * Adds the listener as one being notified when the selected part changes.
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        getViewer().addSelectionChangedListener(listener);
    }

    /**
     * Removes the listener as one being notified when the selected part changes.
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        getViewer().removeSelectionChangedListener(listener);
    }

    private void moveParts(boolean up) {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }

        Table table = viewer.getTable();
        int[] newSelection = moveParts(table.getSelectionIndices(), up);
        viewer.refresh();
        table.setSelection(newSelection);
        viewer.getControl().setFocus();

        refresh();
    }

    /**
     * Creates a new part.
     * 
     * @throws CoreException May throw this exception at any time.
     */
    protected abstract IIpsObjectPart newIpsPart() throws CoreException;

    /**
     * Subclasses may overwrite this operation to perform additional tasks to be done when deleting
     * the given ips part.
     * 
     * @throws CoreException May throw this exception at any time.
     */
    protected void deleteIpsPart(IIpsObjectPart partToDelete) throws CoreException {
        partToDelete.delete();
    }

    /**
     * Creates a dialog to edit the part.
     */
    protected abstract EditDialog createEditDialog(Object object, Shell shell) throws CoreException;

    /**
     * Moves the parts identified by the indexes in the model object up or down.
     * 
     * @param indexes Array of indexes identifying the parts to move
     * @param up Flag indicating whether to move up or down
     * 
     * @return the new indices of the moved parts.
     */
    protected int[] moveParts(int[] indexes, boolean up) {
        return indexes;
    }

    private ArrayList<IDeleteListener> deleteListeners = new ArrayList<IDeleteListener>();

    protected void addDeleteListener(IDeleteListener listener) {
        deleteListeners.add(listener);
    }

    protected void removeDeleteListener(IDeleteListener listener) {
        deleteListeners.remove(listener);
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return viewer;
    }

    @Override
    public boolean isActivated() {
        if (viewer == null) {
            return false;
        }
        return viewer.getTable().getDisplay().getCursorControl() == viewer.getTable() ? true : false;
    }

}
