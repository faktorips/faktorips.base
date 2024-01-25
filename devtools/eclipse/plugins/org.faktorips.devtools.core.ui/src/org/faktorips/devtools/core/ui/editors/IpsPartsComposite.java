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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
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
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.memento.Memento;

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

    private ArrayList<IDeleteListener> deleteListeners = new ArrayList<>();

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

    protected enum Option {
        SHOW_EDIT_BUTTON,
        RENAME_REFACTORING_SUPPORTED,
        PULL_UP_REFACTORING_SUPPORTED,
        JUMP_TO_SOURCE_CODE_SUPPORTED,
        CAN_CREATE,
        CAN_EDIT,
        CAN_DELETE,
        CAN_MOVE,
        CAN_OVERRIDE,
        NONE;
    }

    private final EnumSet<Option> options;

    protected IpsPartsComposite(IIpsObject ipsObject, Composite parent, IWorkbenchPartSite site,
            EnumSet<Option> options, UIToolkit uiToolkit) {

        super(parent);
        this.options = options;
        this.ipsObject = ipsObject;
        this.site = site;
        this.uiToolkit = uiToolkit;
        initControls(uiToolkit);
        uiToolkit.getFormToolkit().adapt(this);
        IpsObjectPartChangeRefreshHelper.createAndInit(getIpsObject(), tableViewer);

    }

    protected IpsPartsComposite(IIpsObject pdObject, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        this(pdObject, parent, site, EnumSet.of(Option.CAN_CREATE, Option.CAN_EDIT, Option.CAN_DELETE, Option.CAN_MOVE,
                Option.SHOW_EDIT_BUTTON), toolkit);
    }

    // CSOFF: ParameterNumberCheck
    /**
     * @deprecated use constructor with EnumSet
     */
    @Deprecated
    protected IpsPartsComposite(IIpsObject ipsObject, Composite parent, IWorkbenchPartSite site, boolean canCreate,
            boolean canEdit, boolean canDelete, boolean canMove, boolean showEditButton,
            boolean renameRefactoringSupported, boolean pullUpRefactoringSupported, boolean jumpToSourceCodeSupported,
            UIToolkit uiToolkit) {

        super(parent);
        List<Option> optionList = new ArrayList<>();
        addOptionToList(optionList, canCreate, Option.CAN_CREATE);
        addOptionToList(optionList, canEdit, Option.CAN_EDIT);
        addOptionToList(optionList, canDelete, Option.CAN_DELETE);
        addOptionToList(optionList, canMove, Option.CAN_MOVE);
        addOptionToList(optionList, showEditButton, Option.SHOW_EDIT_BUTTON);
        addOptionToList(optionList, renameRefactoringSupported, Option.RENAME_REFACTORING_SUPPORTED);
        addOptionToList(optionList, pullUpRefactoringSupported, Option.PULL_UP_REFACTORING_SUPPORTED);
        addOptionToList(optionList, jumpToSourceCodeSupported, Option.JUMP_TO_SOURCE_CODE_SUPPORTED);
        addNoneIfEmptyList(optionList);

        options = EnumSet.copyOf(optionList);

        this.ipsObject = ipsObject;
        this.site = site;
        this.uiToolkit = uiToolkit;
        initControls(uiToolkit);
        uiToolkit.getFormToolkit().adapt(this);
        IpsObjectPartChangeRefreshHelper.createAndInit(getIpsObject(), tableViewer);
    }

    // CSON: ParameterNumberCheck
    public EnumSet<Option> getOptions() {
        return options;
    }

    private void addNoneIfEmptyList(List<Option> optionList) {
        if (optionList.isEmpty()) {
            optionList.add(Option.NONE);
        }
    }

    private void addOptionToList(List<Option> optionList, boolean value, Option enumValue) {
        if (value) {
            optionList.add(enumValue);
        }
    }

    void createContextMenu() {
        MenuManager contextMenuManager = new MenuManager();

        if (isOptionInclude(Option.JUMP_TO_SOURCE_CODE_SUPPORTED)) {
            contextMenuManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
        }

        createRefactoringSubContextMenu(contextMenuManager);

        createContextMenuThis(contextMenuManager);

        if (!contextMenuManager.isEmpty()) {
            final Menu contextMenu = contextMenuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
            if (site instanceof IEditorSite editorSite) {
                editorSite.registerContextMenu(contextMenuManager, getViewer(), false);
            } else {
                site.registerContextMenu(contextMenuManager, getViewer());
            }

            MenuCleaner.addDefaultCleaner(contextMenuManager);
            // Hide the context menu if nothing is selected
            contextMenuManager.addMenuListener($ -> {
                if (getSelection().isEmpty()) {
                    contextMenu.setVisible(false);
                }
            });
        }
    }

    private boolean isOptionInclude(Option option) {
        return getOptions().contains(option);
    }

    private boolean isOptionExclude(Option option) {
        return !getOptions().contains(option);
    }

    private void createRefactoringSubContextMenu(MenuManager contextMenuManager) {
        if (isOptionExclude(Option.RENAME_REFACTORING_SUPPORTED)
                && isOptionExclude(Option.PULL_UP_REFACTORING_SUPPORTED)) {
            return;
        }

        MenuManager refactorSubmenu = new MenuManager(Messages.IpsPartsComposite_submenuRefactor);
        if (isOptionInclude(Option.RENAME_REFACTORING_SUPPORTED)) {
            refactorSubmenu.add(IpsRefactoringHandler.getContributionItem(IpsRenameHandler.CONTRIBUTION_ID,
                    Messages.IpsPartsComposite_labelRenameRefactoring));
            refactorSubmenu.add(new Separator());
        }
        if (isOptionInclude(Option.PULL_UP_REFACTORING_SUPPORTED)) {
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

    public IType getType() {
        return (IType)getIpsObject();
    }

    protected Button getDeleteButton() {
        return deleteButton;
    }

    protected Button getCreateButton() {
        return newButton;
    }

    protected Button getEditButton() {
        return editButton;
    }

    protected Button getUpButton() {
        return upButton;
    }

    protected Button getDownButton() {
        return downButton;
    }

    protected Button getOverrideButton() {
        return overrideButton;
    }

    @Override
    public boolean isDataChangeable() {
        return canCreateOrCanEdit() || isOptionInclude(Option.CAN_MOVE) || isOptionInclude(Option.CAN_DELETE)
                || isOptionInclude(Option.CAN_OVERRIDE);
    }

    private boolean canCreateOrCanEdit() {
        return isOptionInclude(Option.CAN_CREATE) || isOptionInclude(Option.CAN_EDIT);
    }

    @Override
    public void setDataChangeable(boolean flag) {
        table.setEnabled(true);
        addOptionIfNeeded(flag, Option.CAN_CREATE);
        addOptionIfNeeded(flag, Option.CAN_EDIT);
        addOptionIfNeeded(flag, Option.CAN_DELETE);
        addOptionIfNeeded(flag, Option.CAN_MOVE);
        addOptionIfNeeded(flag, Option.CAN_OVERRIDE);
        updateButtonEnabledStates();
    }

    public boolean isCanCreate() {
        return isOptionInclude(Option.CAN_CREATE);
    }

    public void setCanCreate(boolean canCreate) {
        addOptionIfNeeded(canCreate, Option.CAN_CREATE);
        updateButtonEnabledStates();
    }

    public boolean isCanDelete() {
        return isOptionInclude(Option.CAN_DELETE);
    }

    public void setCanDelete(boolean canDelete) {
        addOptionIfNeeded(canDelete, Option.CAN_DELETE);
        updateButtonEnabledStates();
    }

    public boolean isCanEdit() {
        return isOptionInclude(Option.CAN_EDIT);

    }

    public void setCanEdit(boolean canEdit) {
        addOptionIfNeeded(canEdit, Option.CAN_EDIT);
        updateButtonEnabledStates();
    }

    public boolean isCanMove() {
        return isOptionInclude(Option.CAN_MOVE);
    }

    public void setCanMove(boolean canMove) {
        addOptionIfNeeded(canMove, Option.CAN_MOVE);
        updateButtonEnabledStates();
    }

    private void addOptionIfNeeded(boolean state, Option option) {
        if (state) {
            if (isOptionExclude(option)) {
                getOptions().add(option);
            }
        } else {
            getOptions().remove(option);
        }
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
            protected MessageList getMessagesFor(Object element) {
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
        if (isOptionInclude(Option.CAN_CREATE)) {
            createNewButton(buttons, toolkit);
            buttonCreated = true;
        }
        if (isOptionInclude(Option.CAN_EDIT) && isOptionInclude(Option.SHOW_EDIT_BUTTON)) {
            createEditButton(buttons, toolkit);
            buttonCreated = true;
        }
        if (isOptionInclude(Option.CAN_DELETE)) {
            createDeleteButton(buttons, toolkit);
            buttonCreated = true;
        }
        if (isOptionInclude(Option.CAN_MOVE)) {
            if (buttonCreated) {
                createButtonSpace(buttons, toolkit);
            }
            createMoveButtons(buttons, toolkit);
            buttonCreated = true;
        }
        if (isOptionInclude(Option.CAN_OVERRIDE)) {
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
        newButton = createButton(buttons, toolkit, Messages.IpsPartsComposite_buttonNew, new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                newPart();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createEditButton(Composite buttons, UIToolkit toolkit) {
        editButton = createButton(buttons, toolkit, Messages.IpsPartsComposite_buttonEdit, new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editPart();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createDeleteButton(Composite buttons, UIToolkit toolkit) {
        deleteButton = createButton(buttons, toolkit, Messages.IpsPartsComposite_buttonDelete, new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deletePart();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createMoveButtons(Composite buttons, UIToolkit toolkit) {
        upButton = createButton(buttons, toolkit, Messages.IpsPartsComposite_buttonUp, new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveParts(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

        downButton = createButton(buttons, toolkit, Messages.IpsPartsComposite_buttonDown, new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveParts(false);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    protected final void createOverrideButton(Composite buttons, UIToolkit toolkit) {
        overrideButton = createButton(buttons, toolkit, Messages.IpsPartsComposite_override, new SelectionListener() {
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

    private Button createButton(Composite buttons,
            UIToolkit toolkit,
            String message,
            SelectionListener selectionLister) {
        Button button = toolkit.createButton(buttons, message);
        button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        button.addSelectionListener(selectionLister);
        return button;
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
            newButton.setEnabled(isOptionInclude(Option.CAN_CREATE));
        }
    }

    private void updateStateEditButton(boolean itemSelected) {
        if (editButton != null) {
            editButton.setEnabled(itemSelected);
            editButton.setText((isOptionInclude(Option.CAN_EDIT) ? Messages.IpsPartsComposite_buttonEdit
                    : Messages.IpsPartsComposite_buttonShow));
        }
    }

    private void updateStateDeleteButton(boolean itemSelected) {
        if (deleteButton != null) {
            deleteButton.setEnabled(itemSelected && isOptionInclude(Option.CAN_DELETE));
        }
    }

    private void updateStateUpButton(boolean itemSelected) {
        if (upButton != null) {
            upButton.setEnabled(itemSelected && isOptionInclude(Option.CAN_MOVE) && !isFirstElementSelected());
        }
    }

    private void updateStateDownButton(boolean itemSelected) {
        if (downButton != null) {
            downButton.setEnabled(itemSelected && isOptionInclude(Option.CAN_MOVE) && !isLastElementSelected());
        }
    }

    private void updateStateOverrideButton() {
        if (overrideButton != null) {
            overrideButton.setEnabled(getType().hasExistingSupertype(getType().getIpsProject())
                    && isOptionInclude(Option.CAN_OVERRIDE));
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
                tableViewer.setSelection(new StructuredSelection(newPart), true);
            }
        } catch (IpsException ex) {
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
     * The method will be called if the OK button in new dialog was pressed.
     * 
     * <p>
     * 
     * By default this method delegates to {@link #editPartConfirmed()}. Otherwise only override
     * {@link #editPartConfirmed()}.
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
        String deleteDialogMessage = NLS.bind(Messages.IpsPartsComposite_deleteElementConfirm,
                getSelectedPart().getName());
        boolean deleteAttribute = MessageDialog.openConfirm(getShell(),
                Messages.IpsPartsComposite_deleteElement,
                deleteDialogMessage);
        if (deleteAttribute) {
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
            } catch (IpsException ex) {
                IpsPlugin.logAndShowErrorDialog(ex);
            }
            refresh();
        }
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
     */
    protected abstract IIpsObjectPart newIpsPart();

    /**
     * Subclasses may overwrite this operation to perform additional tasks to be done when deleting
     * the given IPS part.
     * 
     */
    protected void deleteIpsPart(IIpsObjectPart partToDelete) {
        partToDelete.delete();
    }

    /**
     * Creates a dialog to edit a new part. The part was created by calling {@link #newIpsPart()}.
     * <p>
     * By default this method delegates to {@link #createEditDialog(IIpsObjectPart, Shell)}. If you
     * want to create different dialogs or settings depending on new button or edit button was
     * clicked, override this method. Otherwise only override
     * {@link #createEditDialog(IIpsObjectPart, Shell)}.
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
