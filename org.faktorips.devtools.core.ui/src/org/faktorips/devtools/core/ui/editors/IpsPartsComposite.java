/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import java.util.EnumSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.DialogMementoHelper;
import org.faktorips.devtools.core.ui.refactor.IpsPullUpHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRenameHandler;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;

/**
 * A composite that shows parts in a table viewer and provides an area containing a new, edit and
 * delete button.
 */
public abstract class IpsPartsComposite extends ViewerButtonComposite implements ISelectionProvider,
        IDataChangeableReadWriteAccess {

    /** The object the parts belong to. */
    private final IIpsObject ipsObject;

    private final IWorkbenchPartSite site;

    private final UIToolkit uiToolkit;

    /**
     * Flag that controls whether the edit button is shown (edit is also possible via double click).
     */
    private boolean showEditButton;

    private boolean renameRefactoringSupported;

    private boolean pullUpRefactoringSupported;

    /** Flag that controls whether the jump to source code context menu will be provided. */
    private boolean jumpToSourceCodeSupported;

    private ArrayList<IDeleteListener> deleteListeners = new ArrayList<IDeleteListener>();

    /** The table view of this composite */
    private TableViewer tableViewer;

    private Button newButton;

    private Button editButton;

    private Button deleteButton;

    private Button upButton;

    private Button downButton;

    private Button overrideButton;

    /** Table to show the content */
    private Table table;

    /** Listener to start editing on double click. */
    private MouseAdapter editDoubleClickListener;

    /** Flag that controls if a new part can be created. */
    private boolean canCreate;

    /** Flag that controls if a part can be edited. */
    private boolean canEdit;

    /** Flag that controls if a part can be deleted. */
    private boolean canDelete;

    /** Flag that controls if parts can be moved. */
    private boolean canMove;

    /** Flag that controls if a part can override. */
    private boolean canOverride;

    protected enum AttributesForButtons {
        SHOW_EDIT_BUTTON,
        RENAME_REFACTORING_SUPPORTED,
        PULL_UP_REFACTORING_SUPPORTED,
        JUMP_TO_SOURCE_CODE_SUPPORTED,
        CAN_CREATE,
        CAN_EDIT,
        CAN_DELETE,
        CAN_MOVE,
        CAN_OVERRIDE;
    }

    protected IpsPartsComposite(IIpsObject pdObject, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        this(pdObject, parent, site, EnumSet.of(AttributesForButtons.CAN_CREATE, AttributesForButtons.CAN_EDIT,
                AttributesForButtons.CAN_DELETE, AttributesForButtons.CAN_MOVE, AttributesForButtons.SHOW_EDIT_BUTTON),
                toolkit);
    }

    /**
     * @deprecated use constructor with EnumSet
     */
    @Deprecated
    protected IpsPartsComposite(IIpsObject ipsObject, Composite parent, IWorkbenchPartSite site, boolean canCreate,
            boolean canEdit, boolean canDelete, boolean canMove, boolean showEditButton,
            boolean renameRefactoringSupported, boolean pullUpRefactoringSupported, boolean jumpToSourceCodeSupported,
            UIToolkit uiToolkit) {

        super(parent);

        this.ipsObject = ipsObject;
        this.site = site;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canMove = canMove;
        this.showEditButton = showEditButton;
        this.renameRefactoringSupported = renameRefactoringSupported;
        this.pullUpRefactoringSupported = pullUpRefactoringSupported;
        this.jumpToSourceCodeSupported = jumpToSourceCodeSupported;
        this.uiToolkit = uiToolkit;

        initControls(uiToolkit);
        uiToolkit.getFormToolkit().adapt(this);
    }

    protected IpsPartsComposite(IIpsObject ipsObject, Composite parent, IWorkbenchPartSite site,
            EnumSet<AttributesForButtons> attributesForButtons, UIToolkit uiToolkit) {

        super(parent);

        this.ipsObject = ipsObject;
        this.site = site;

        buttonVariety(attributesForButtons);
        supportVariety(attributesForButtons);

        this.uiToolkit = uiToolkit;
        initControls(uiToolkit);
        uiToolkit.getFormToolkit().adapt(this);
    }

    private void supportVariety(EnumSet<AttributesForButtons> attributesForButtons) {
        if (attributesForButtons.contains(AttributesForButtons.JUMP_TO_SOURCE_CODE_SUPPORTED)) {
            jumpToSourceCodeSupported = true;
        }

        if (attributesForButtons.contains(AttributesForButtons.PULL_UP_REFACTORING_SUPPORTED)) {
            pullUpRefactoringSupported = true;
        }

        if (attributesForButtons.contains(AttributesForButtons.RENAME_REFACTORING_SUPPORTED)) {
            renameRefactoringSupported = true;
        }
    }

    private void buttonVariety(EnumSet<AttributesForButtons> attributesForButtons) {
        if (attributesForButtons.contains(AttributesForButtons.CAN_CREATE)) {
            canCreate = true;
        }

        if (attributesForButtons.contains(AttributesForButtons.CAN_DELETE)) {
            canDelete = true;
        }

        if (attributesForButtons.contains(AttributesForButtons.CAN_EDIT)) {
            canEdit = true;
        }
        if (attributesForButtons.contains(AttributesForButtons.CAN_MOVE)) {
            canMove = true;
        }

        if (attributesForButtons.contains(AttributesForButtons.CAN_OVERRIDE)) {
            canOverride = true;
        }

        if (attributesForButtons.contains(AttributesForButtons.SHOW_EDIT_BUTTON)) {
            showEditButton = true;
        }
    }

    void createContextMenu() {
        MenuManager contextMenuManager = new MenuManager();

        if (jumpToSourceCodeSupported) {
            contextMenuManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
        }

        createRefactoringSubContextMenu(contextMenuManager);

        createContextMenuThis(contextMenuManager);

        if (!contextMenuManager.isEmpty()) {
            final Menu contextMenu = contextMenuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
            if (site instanceof IEditorSite) {
                IEditorSite editorSite = (IEditorSite)site;
                editorSite.registerContextMenu(contextMenuManager, getViewer(), false);
            } else {
                site.registerContextMenu(contextMenuManager, getViewer());
            }

            MenuCleaner.addDefaultCleaner(contextMenuManager);
            // Hide the context menu if nothing is selected
            contextMenuManager.addMenuListener(new IMenuListener() {
                @Override
                public void menuAboutToShow(IMenuManager manager) {
                    if (getSelection().isEmpty()) {
                        contextMenu.setVisible(false);
                    }
                }
            });
        }
    }

    private void createRefactoringSubContextMenu(MenuManager contextMenuManager) {
        if (!renameRefactoringSupported && !pullUpRefactoringSupported) {
            return;
        }

        MenuManager refactorSubmenu = new MenuManager(Messages.IpsPartsComposite_submenuRefactor);
        if (renameRefactoringSupported) {
            refactorSubmenu.add(IpsRefactoringHandler.getContributionItem(IpsRenameHandler.CONTRIBUTION_ID,
                    Messages.IpsPartsComposite_labelRenameRefactoring));
            refactorSubmenu.add(new Separator());
        }
        if (pullUpRefactoringSupported) {
            refactorSubmenu.add(IpsRefactoringHandler.getContributionItem(IpsPullUpHandler.CONTRIBUTION_ID,
                    Messages.IpsPartsComposite_labelPullUpRefactoring));
        }
        contextMenuManager.add(new Separator());
        contextMenuManager.add(refactorSubmenu);
    }

    /**
     * Allows subclasses to make additions to the context menu.
     * <p>
     * The default implementation does nothing.
     * 
     * @param contextMenuManager The {@link MenuManager} for the context menu to add menu items to
     */
    protected void createContextMenuThis(MenuManager contextMenuManager) {
        // Empty default implementation
    }

    /**
     * Returns the IpsObject the parts belong to.
     */
    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    public UIToolkit getUiToolkit() {
        return uiToolkit;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public IType getType() {
        return (IType)getIpsObject();
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    @Override
    public boolean isDataChangeable() {
        return canCreateOrCanEdit() || canMove || canDelete || canOverride;
    }

    private boolean canCreateOrCanEdit() {
        return canCreate || canEdit;
    }

    @Override
    public void setDataChangeable(boolean flag) {
        table.setEnabled(true);
        canCreate = flag;
        canEdit = flag;
        canDelete = flag;
        canMove = flag;
        canOverride = flag;
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
    protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
        table = createTable(parent, toolkit);
        setEditDoubleClickListenerEnabled(true);
        registerOpenLinkListener();

        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(createContentProvider());
        ILabelProvider lp = createLabelProvider();
        final MessageCueLabelProvider messageCueLabelProvider = new MessageCueLabelProvider(lp,
                ipsObject.getIpsProject());
        tableViewer.setLabelProvider(messageCueLabelProvider);

        new TableMessageHoverService(tableViewer) {

            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                return messageCueLabelProvider.getMessages(element);
            }

        };

        return tableViewer;
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
     * Open the element in a new editor if the CTRL key was pressed during selection. Subclasses are
     * indent to overwrite this method if this functionality is available.
     */
    protected void openLink() {
        // nothing to do
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
        if (canOverride) {
            if (buttonCreated) {
                createButtonSpace(buttons, toolkit);
            }
            createOverrideButton(buttons, toolkit);
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
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    // CSON: IllegalCatch
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
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    // CSON: IllegalCatch
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
                    deletePart();
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    // CSON: IllegalCatch
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
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    // CSON: IllegalCatch
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
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    // CSON: IllegalCatch
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

    }

    protected final void createOverrideButton(Composite buttons, UIToolkit toolkit) {
        overrideButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_override);
        overrideButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        overrideButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                overrideClicked();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    public void overrideClicked() {
        // muss be implemented in subclass
    }

    @Override
    protected void updateButtonEnabledStates() {
        boolean itemSelected = false;

        if (isItemSelected()) {
            itemSelected = true;
        }

        updateStateNewButton();
        updateStateEditButton(itemSelected);
        updateStateDeleteButton(itemSelected);
        updateStateUpButton(itemSelected);
        updateStateDownButton(itemSelected);
        updateStateOverrideButton();
    }

    private boolean isItemSelected() {
        return getViewer().getSelection() != null && !getViewer().getSelection().isEmpty();
    }

    private void updateStateNewButton() {
        if (newButton != null) {
            newButton.setEnabled(canCreate);
        }
    }

    private void updateStateEditButton(boolean itemSelected) {
        if (editButton != null) {
            editButton.setEnabled(itemSelected);
            editButton
                    .setText((canEdit ? Messages.IpsPartsComposite_buttonEdit : Messages.IpsPartsComposite_buttonShow));
        }
    }

    private void updateStateDeleteButton(boolean itemSelected) {
        if (deleteButton != null) {
            deleteButton.setEnabled(itemSelected && canDelete);
        }
    }

    private void updateStateUpButton(boolean itemSelected) {
        if (upButton != null) {
            upButton.setEnabled(itemSelected && canMove && !isFirstElementSelected());
        }
    }

    private void updateStateDownButton(boolean itemSelected) {
        if (downButton != null) {
            downButton.setEnabled(itemSelected && canMove && !isLastElementSelected());
        }
    }

    private void updateStateOverrideButton() {
        if (overrideButton != null) {
            try {
                overrideButton.setEnabled(getType().hasExistingSupertype(getType().getIpsProject()));
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final IIpsObjectPart getSelectedPart() {
        return (IIpsObjectPart)getSelectedObject();
    }

    @Override
    public ISelection getSelection() {
        return getViewer().getSelection();
    }

    @Override
    public void setSelection(ISelection selection) {
        getViewer().setSelection(selection);
    }

    private void newPart() {
        /*
         * TODO AW 10-11-2011: Should use new DialogMementoHelper but that is not possible as long
         * as createEditDialog may return null.
         */
        try {
            IIpsSrcFile file = ipsObject.getIpsSrcFile();
            boolean dirty = file.isDirty();
            Memento memento = ipsObject.newMemento();
            IIpsObjectPart newPart = newIpsPart();
            EditDialog dialog = createNewDialog(newPart, getShell());
            if (dialog == null) {
                return;
            }
            dialog.open();
            if (dialog.getReturnCode() == Window.CANCEL) {
                ipsObject.setState(memento);
                if (!dirty) {
                    file.markAsClean();
                }
                editPartCanceled();
            } else if (dialog.getReturnCode() == Window.OK) {

                newPartConfirmed(newPart);
            }
            // CSOFF: IllegalCatch
        } catch (Exception ex) {
            // CSON: IllegalCatch
            IpsPlugin.logAndShowErrorDialog(ex);
        }

        refresh();
    }

    protected void editPart() {
        if (getSelection().isEmpty()) {
            return;
        }

        DialogMementoHelper dialogHelper = new DialogMementoHelper() {
            @Override
            protected Dialog createDialog() {
                EditDialog editDialog = createEditDialog(getSelectedPart(), getShell());
                if (editDialog != null) {
                    editDialog.setDataChangeable(isDataChangeable());
                }
                return editDialog;
            }
        };
        int returnCode = dialogHelper.openDialogWithMemento(getSelectedPart());
        if (returnCode == Window.OK) {
            editPartConfirmed();
        } else if (returnCode == Window.CANCEL) {
            editPartCanceled();
        }

        refresh();
    }

    /**
     * Sets the newPart as active generation.The part was created by calling {@link #newIpsPart()}.
     * The method will be called if the ok button in new dialog was pressed.
     * 
     * <p>
     * 
     * By default this method delegates to {@link #editPartConfirmed()}. Othervise only override
     * {@link #editPartConfirmed()}.
     * <p>
     * 
     * @param newPart The part was created by calling {@link #newIpsPart()}.
     * 
     * @since 3.7
     */
    protected void newPartConfirmed(IIpsObjectPart newPart) {
        editPartConfirmed();
    }

    protected void editPartConfirmed() {
        // Empty default implementation
    }

    protected void editPartCanceled() {
        // Empty default implementation
    }

    private void deletePart() {
        if (getSelection().isEmpty()) {
            return;
        }

        try {
            Table tableControl = (Table)getViewer().getControl();
            int selectedIndexAfterDeletion = Math
                    .min(tableControl.getSelectionIndex(), tableControl.getItemCount() - 2);
            IIpsObjectPart part = getSelectedPart();
            if (!fireAboutToDelete(part)) {
                return;
            }
            deleteIpsPart(part);
            if (selectedIndexAfterDeletion >= 0) {
                Object selected = tableViewer.getElementAt(selectedIndexAfterDeletion);
                tableViewer.setSelection(new StructuredSelection(selected), true);
            }
            fireDeleted(part);
            // CSOFF: IllegalCatch
        } catch (Exception ex) {
            // CSON: IllegalCatch
            IpsPlugin.logAndShowErrorDialog(ex);
        }

        refresh();
    }

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
        TableViewer viewerControl = (TableViewer)getViewer();
        if (viewerControl.getSelection().isEmpty()) {
            return;
        }

        Table tableControl = viewerControl.getTable();
        int[] newSelection = moveParts(tableControl.getSelectionIndices(), up);
        viewerControl.refresh();
        tableControl.setSelection(newSelection);
        viewerControl.getControl().setFocus();

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
     * Creates a dialog to edit a new part. The part was created by calling {@link #newIpsPart()}.
     * <p>
     * By default this method delegates to {@link #createEditDialog(IIpsObjectPart, Shell)}. If you
     * want to create different dialogs or settings depending on new button or edit button was
     * clicked, override this method. Othervise only override
     * {@link #createEditDialog(IIpsObjectPart, Shell)}.
     * <p>
     * 
     * @since 3.7
     */
    protected EditDialog createNewDialog(IIpsObjectPart part, Shell shell) {
        return createEditDialog(part, shell);
    }

    /**
     * Creates a dialog to edit the part.
     */
    protected abstract EditDialog createEditDialog(IIpsObjectPart part, Shell shell);

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

    protected void addDeleteListener(IDeleteListener listener) {
        deleteListeners.add(listener);
    }

    protected void removeDeleteListener(IDeleteListener listener) {
        deleteListeners.remove(listener);
    }

    private boolean fireAboutToDelete(IIpsObjectPart part) {
        for (IDeleteListener listener : deleteListeners) {
            boolean accept = listener.aboutToDelete(part);
            if (!accept) {
                return false;
            }
        }
        return true;
    }

    private void fireDeleted(IIpsObjectPart part) {
        for (IDeleteListener listener : deleteListeners) {
            listener.deleted(part);
        }
    }

}
