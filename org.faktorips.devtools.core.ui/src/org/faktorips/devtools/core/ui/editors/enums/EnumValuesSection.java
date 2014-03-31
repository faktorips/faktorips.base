/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controlfactories.DefaultControlFactory;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.editors.IpsObjectPartContainerSection;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.editors.enums.EnumEditingSupport.Condition;
import org.faktorips.devtools.core.ui.table.LinkedColumnsTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TableUtil;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The UI section for the <tt>EnumTypeEditorPage</tt> and the <tt>EnumContentEditorPage</tt> that
 * contains the <tt>enumValuesTable</tt> to be edited.
 * <p>
 * If the IPS object being edited is an <tt>IEnumType</tt> then in-place fixing of the
 * <tt>enumValuesTable</tt> will be done. That means, if an <tt>IEnumAttribute</tt> is added there
 * will be a new column in the table, if an <tt>IEnumAttribute</tt> is deleted the corresponding
 * table column will be deleted and so on.
 * <p>
 * Fixing the table when editing <tt>IEnumContent</tt> objects is done manually by the user trough a
 * separate dialog.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeEditorPage
 * @see org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentEditorPage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValuesSection extends IpsObjectPartContainerSection implements ContentsChangeListener {

    /** Key to store the state of the action in the dialog settings. */
    private static final String SETTINGS_KEY_LOCK_AND_SYNC = "lockAndSyncLiteralNames"; //$NON-NLS-1$

    /** The <tt>IEnumValueContainer</tt> holding the <tt>IEnumValue</tt>s to be edited. */
    private final IEnumValueContainer enumValueContainer;

    /**
     * The <tt>IEnumType</tt> holding the <tt>IEnumValue</tt>s to be edited or <tt>null</tt> if an
     * <tt>IEnumContent</tt> is being edited.
     */
    private IEnumType enumType;

    /**
     * The <tt>IEnumContent</tt> holding the <tt>IEnumValue</tt>s to be edited or <tt>null</tt> if
     * an <tt>IEnumType</tt> is being edited.
     */
    private IEnumContent enumContent;

    /** The <tt>IIpsProject</tt> the <tt>IEnumValueContainer</tt> to edit is stored in. */
    private IIpsProject ipsProject;

    /** The UI table widget. */
    private Table enumValuesTable;

    /** The JFace table viewer linking the UI table with the model data. */
    private TableViewer enumValuesTableViewer;

    /** Action to add new <tt>IEnumValue</tt>s. */
    private IAction newEnumValueAction;

    /** Action to delete <tt>IEnumValue</tt>s. */
    private IAction deleteEnumValueAction;

    /** Action to move <tt>IEnumValue</tt>s up by 1. */
    private IAction moveEnumValueUpAction;

    /** Action to move <tt>IEnumValue</tt>s down by 1. */
    private IAction moveEnumValueDownAction;

    /**
     * Action that locks the literal name column and synchronizes it's values with the values of the
     * default provider column.
     */
    private IAction lockAndSyncLiteralNameAction;

    /**
     * Action that enables the user to apply the 'Rename Literal Name' refactoring.
     */
    private IAction renameLiteralNameRefactoringAction;

    /** Action to reset all literal names to the values of their respective default providers. */
    private IAction resetLiteralNamesAction;

    /** Flag indicating whether the 'Lock and Synchronize Literal Names' option is currently active. */
    private boolean lockAndSynchronizeLiteralNames;

    /**
     * Flag indicating whether the section is used to edit the <tt>IEnumValue</tt>s of an
     * <tt>IEnumType</tt> (<tt>true</tt>) or an <tt>IEnumContent</tt> (<tt>false</tt>).
     */
    private boolean enumTypeEditing;

    /**
     * Creates a new <tt>EnumValuesSection</tt> containing the <tt>IEnumValue</tt>s of the given
     * <tt>IEnumValueContainer</tt>.
     * 
     * @param enumValueContainer The <tt>IEnumValue</tt>s of this <tt>IEnumValueContainer</tt> will
     *            be shown.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit that shall be used to create UI elements.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumType</tt>
     *             referenced by the IPS object being edited.
     * @throws NullPointerException If <tt>enumValueContainer</tt> is <tt>null</tt>.
     */
    public EnumValuesSection(final IEnumValueContainer enumValueContainer, Composite parent, UIToolkit toolkit)
            throws CoreException {

        super(enumValueContainer, parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(enumValueContainer);

        this.enumValueContainer = enumValueContainer;
        ipsProject = enumValueContainer.getIpsProject();

        if (enumValueContainer instanceof IEnumType) {
            enumTypeEditing = true;
            enumType = (IEnumType)enumValueContainer;
        } else if (enumValueContainer instanceof IEnumContent) {
            enumContent = (IEnumContent)enumValueContainer;
            enumType = enumContent.findEnumType(ipsProject);
        } else {
            throw new CoreRuntimeException("Illegal Enum Container " + enumValueContainer); //$NON-NLS-1$
        }

        loadDialogSettings();
        initControls();
        createContextMenu();
        setText(Messages.EnumValuesSection_title);

        updateEnabledStates();

        registerAsChangeListenerToEnumValueContainer();

        addMonitoredValidationMessageCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE);
    }

    private void loadDialogSettings() {
        IDialogSettings settings = IpsUIPlugin.getDefault().getDialogSettings();
        if (enumTypeEditing) {
            String synchronizeSetting = settings.get(SETTINGS_KEY_LOCK_AND_SYNC);
            lockAndSynchronizeLiteralNames = synchronizeSetting == null ? false : settings
                    .getBoolean(SETTINGS_KEY_LOCK_AND_SYNC);
        } else {
            lockAndSynchronizeLiteralNames = false;
        }
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        enumValuesTable = toolkit.createTable(client, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.MULTI
                | SWT.FULL_SELECTION);
        enumValuesTableViewer = new TableViewer(enumValuesTable);

        createTableColumns();

        enumValuesTableViewer.refresh();

        enumValuesTableViewer.setContentProvider(new EnumValuesContentProvider(enumValueContainer));

        enumValuesTableViewer.setInput(enumValueContainer);

        // Set the RowDeletor listener to automatically delete empty rows at the end of the table.
        enumValuesTableViewer.addSelectionChangedListener(new RowDeletor(enumValuesTableViewer));
        enumValuesTable.setHeaderVisible(true);
        enumValuesTable.setLinesVisible(true);

        // Fill all space.
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.minimumHeight = 85;
        enumValuesTable.setLayoutData(tableGridData);

        TableUtil.increaseHeightOfTableRows(enumValuesTable, enumValuesTable.getColumnCount(), 5);

        // Key listener for deleting rows with the DEL key.
        enumValuesTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    deleteEnumValueAction.run();
                }
            }
        });
        createTableValidationHoverService();
    }

    /** Creates the table columns. */
    private void createTableColumns() {
        if (enumTypeEditing) {
            createTableColumnsForEnumType();
        } else {
            createTableColumnsForEnumContent();
        }
    }

    /**
     * Creates the table columns based on the <tt>IEnumAttribute</tt>s of the <tt>IEnumType</tt> to
     * edit.
     */
    private void createTableColumnsForEnumType() {
        EnumValueTraversalStrategy previousTraversalStrategy = null;
        for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributesIncludeSupertypeCopies(true)) {
            String columnName = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(currentEnumAttribute);
            try {
                previousTraversalStrategy = addTableColumn(columnName,
                        currentEnumAttribute.findDatatypeIgnoreEnumContents(ipsProject),
                        currentEnumAttribute.findIsUnique(ipsProject),
                        currentEnumAttribute.isEnumLiteralNameAttribute(), currentEnumAttribute.isMultilingual(),
                        previousTraversalStrategy);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    /**
     * Creates the table columns based upon the <tt>IEnumAttributeReference</tt>s (and upon the
     * <tt>IEnumAttribute</tt>s of the referenced <tt>IEnumType</tt> if possible).
     */
    private void createTableColumnsForEnumContent() {
        try {
            IEnumType referencedEnumType = enumContent.findEnumType(ipsProject);
            List<IEnumAttributeReference> enumAttributeReferences = enumContent.getEnumAttributeReferences();
            EnumValueTraversalStrategy previousTraversalStrategy = null;
            for (int i = 0; i < enumContent.getEnumAttributeReferencesCount(); i++) {
                IEnumAttributeReference enumAttributeReference = enumAttributeReferences.get(i);
                if (enumContent.isFixToModelRequired()) {
                    previousTraversalStrategy = addTableColumn(enumAttributeReference.getName(), null, false, false,
                            false, previousTraversalStrategy);
                } else {
                    IEnumAttribute currentEnumAttribute = referencedEnumType.getEnumAttributesIncludeSupertypeCopies(
                            false).get(i);
                    String columnName = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(currentEnumAttribute);
                    previousTraversalStrategy = addTableColumn(columnName,
                            currentEnumAttribute.findDatatype(ipsProject),
                            currentEnumAttribute.findIsUnique(ipsProject),
                            currentEnumAttribute.isEnumLiteralNameAttribute(), currentEnumAttribute.isMultilingual(),
                            previousTraversalStrategy);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Adds a new column with the given name to the end of the table.
     * 
     * @param datatype the datatype to set the correct column style. If data type is null, the
     *            default style configured by the {@link DefaultControlFactory} is used.
     * */
    private EnumValueTraversalStrategy addTableColumn(String columnName,
            ValueDatatype datatype,
            boolean identifierColumnn,
            final boolean literalNameColumn,
            boolean isMultilingual,
            EnumValueTraversalStrategy previousTraversalStrategy) {
        int alignment = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype).getDefaultAlignment();
        TableViewerColumn newColumn = new TableViewerColumn(enumValuesTableViewer, alignment);
        newColumn.setLabelProvider(new EnumValuesLabelProvider(enumTypeEditing));
        newColumn.getColumn().setText(columnName);
        newColumn.getColumn().setWidth(200);

        final int columnIndex = getColumnIndexByName(columnName);

        FormattedCellEditingSupport<IEnumValue, ?> editingSupport;
        if (isMultilingual) {
            editingSupport = createInternationalStringEditingSupport(newColumn, columnIndex);
        } else {
            editingSupport = createStringEditingSupport(datatype, literalNameColumn, newColumn, columnIndex);
        }
        EnumValueTraversalStrategy traversalStrategy = new EnumValueTraversalStrategy(enumValueContainer,
                editingSupport, columnIndex);
        traversalStrategy.setPredecessor(previousTraversalStrategy);
        editingSupport.setTraversalStrategy(traversalStrategy);

        if (identifierColumnn) {
            newColumn.getColumn().setImage(IpsUIPlugin.getImageHandling().getSharedImage("TableKeyColumn.gif", true)); //$NON-NLS-1$
        }

        return traversalStrategy;
    }

    private FormattedCellEditingSupport<IEnumValue, ?> createInternationalStringEditingSupport(TableViewerColumn newColumn,
            int columnIndex) {
        EnumInternationalStringCellModifier cellModifier = new EnumInternationalStringCellModifier(columnIndex,
                IpsPlugin.getMultiLanguageSupport().getLocalizationLocaleOrDefault(ipsProject));
        EnumInternationalStringEditingSupport editingSupport = new EnumInternationalStringEditingSupport(
                enumValuesTableViewer, getToolkit(), cellModifier);
        newColumn.setEditingSupport(editingSupport);
        return editingSupport;
    }

    private FormattedCellEditingSupport<IEnumValue, ?> createStringEditingSupport(ValueDatatype datatype,
            final boolean literalNameColumn,
            TableViewerColumn newColumn,
            final int columnIndex) {
        Condition canEditCondition = new Condition() {

            @Override
            public boolean isEditable() {
                return !literalNameColumn || !lockAndSynchronizeLiteralNames;
            }

        };

        EnumStringCellModifier elementModifier = new EnumStringCellModifier(getShell(), columnIndex);
        EnumEditingSupport editingSupport = new EnumEditingSupport(getToolkit(), enumValuesTableViewer, ipsProject,
                datatype, elementModifier, canEditCondition);
        newColumn.setEditingSupport(editingSupport);
        return editingSupport;
    }

    /**
     * Returns the current index of the column identified by the given name. Returns <tt>null</tt>
     * if no column with the given name exists.
     */
    private int getColumnIndexByName(String columnName) {
        int[] columnOrder = enumValuesTable.getColumnOrder();
        int columnIndex = -1;
        for (int i = 0; i < enumValuesTable.getColumnCount(); i++) {
            if (enumValuesTable.getColumn(columnOrder[i]).getText().equals(columnName)) {
                columnIndex = i;
                break;
            }
        }
        return columnIndex;
    }

    /** Creates the tool bar / context menu actions. */
    private void createActions() {
        newEnumValueAction = new NewEnumValueAction(enumValuesTableViewer);
        deleteEnumValueAction = new DeleteEnumValueAction(enumValuesTableViewer);
        moveEnumValueUpAction = new MoveEnumValueAction(enumValuesTableViewer, true);
        moveEnumValueDownAction = new MoveEnumValueAction(enumValuesTableViewer, false);

        if (enumTypeEditing) {
            lockAndSyncLiteralNameAction = new LockAndSyncLiteralNameAction(this);
            lockAndSyncLiteralNameAction.setChecked(lockAndSynchronizeLiteralNames);
            renameLiteralNameRefactoringAction = new RenameLiteralNameRefactoringAction(enumValuesTableViewer);
            resetLiteralNamesAction = new ResetLiteralNamesAction(enumValuesTableViewer, enumType);
        }
    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        createActions();

        toolBarManager.add(newEnumValueAction);
        toolBarManager.add(deleteEnumValueAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(moveEnumValueUpAction);
        toolBarManager.add(moveEnumValueDownAction);

        if (enumTypeEditing) {
            toolBarManager.add(new Separator());
            toolBarManager.add(lockAndSyncLiteralNameAction);
        }
    }

    /**
     * Registers this section as <tt>ChangeListener</tt> to the <tt>IEnumValueContainer</tt> that is
     * being edited.
     */
    private void registerAsChangeListenerToEnumValueContainer() {
        enumValueContainer.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                enumValueContainer.getIpsModel().removeChangeListener(EnumValuesSection.this);
            }
        });
    }

    /**
     * Toggles the 'Lock and Synchronize Literal Names' option. Updates the list of columns that are
     * skipped by cell editors in the process.
     */
    void toggleLockAndSyncLiteralNames() {
        lockAndSynchronizeLiteralNames = !(lockAndSynchronizeLiteralNames);
        lockAndSyncLiteralNameAction.setChecked(lockAndSynchronizeLiteralNames);

        IDialogSettings settings = IpsUIPlugin.getDefault().getDialogSettings();
        settings.put(SETTINGS_KEY_LOCK_AND_SYNC, lockAndSynchronizeLiteralNames);
    }

    /**
     * Reinitializes the contents of this section:
     * <ul>
     * <li>The <tt>columnNames</tt> will be emptied and created anew.
     * <li>Every table column of the table will be disposed and created anew.
     * <li>The table viewer will be refreshed.
     * <li>The default provider values will be re-initialized.
     */
    public void reinit() {
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            currentColumn.dispose();
        }
        createTableColumns();
        updateTableViewer();
    }

    /** Updates the enabled states of the table and the tool bar actions. */
    private void updateEnabledStates() {
        boolean enabled;
        enabled = enumValueContainer.isCapableOfContainingValues();
        newEnumValueAction.setEnabled(enabled);
        deleteEnumValueAction.setEnabled(enabled);
        if (enumTypeEditing) {
            newEnumValueAction.setEnabled(enabled);
            deleteEnumValueAction.setEnabled(enabled);
            moveEnumValueUpAction.setEnabled(enabled);
            moveEnumValueDownAction.setEnabled(enabled);
            lockAndSyncLiteralNameAction.setEnabled(enabled);
            enumValuesTable.setEnabled(enabled);
            getSectionControl().setEnabled(enabled);
        }
    }

    /** Creates the context menu for the table. */
    private void createContextMenu() {
        MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$

        if (enumTypeEditing) {
            MenuManager refactorMenuManager = new MenuManager(Messages.EnumValuesSection_labelSubmenuRefactor);
            refactorMenuManager.add(renameLiteralNameRefactoringAction);
            menuManager.add(refactorMenuManager);
            menuManager.add(new Separator());
        }

        menuManager.add(newEnumValueAction);
        menuManager.add(deleteEnumValueAction);
        menuManager.add(new Separator());
        menuManager.add(moveEnumValueUpAction);
        menuManager.add(moveEnumValueDownAction);

        if (enumTypeEditing) {
            menuManager.add(new Separator());
            menuManager.add(resetLiteralNamesAction);
        }

        Menu menu = menuManager.createContextMenu(enumValuesTable);
        enumValuesTable.setMenu(menu);
    }

    /**
     * Updates the cell editors for the <tt>enumValuesTableViewer</tt> by creating them anew and
     * overwrites the column properties with actual data. Also refreshes the viewer with actual
     * model data.
     */
    private void updateTableViewer() {
        enumValuesTableViewer.refresh();
    }

    /** Creates the hover service for validation messages for the <tt>enumValuesTableViewer</tt>. */
    private void createTableValidationHoverService() {
        new TableMessageHoverService(enumValuesTableViewer) {

            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return ((IEnumValue)element).validate(enumValueContainer.getIpsProject());
                }
                return null;
            }

        };
    }

    /**
     * Initiates in-place fixing of the <tt>enumValuesTable</tt> if the <tt>IEnumValueContainer</tt>
     * to be edited is an <tt>IEnumType</tt>.
     * <p>
     * Updates the <tt>originalOrderedAttributeValuesMap</tt> and refreshes the <tt>
     * enumValuesTableViewer</tt> when <tt>IEnumValue</tt>s have been added, moved or removed.
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        /*
         * Return if the content changed was not the EnumValueContainer to be edited or the
         * referenced EnumType.
         */
        if (!(event.getIpsSrcFile().equals(enumValueContainer.getIpsSrcFile()))) {
            if (enumType != null) {
                if (!(event.getIpsSrcFile().equals(enumType.getIpsSrcFile()))) {
                    return;
                }
            } else {
                return;
            }
        }

        if (enumType.hasEnumLiteralNameAttribute() && event.getIpsSrcFile().equals(enumValueContainer.getIpsSrcFile())) {
            contentsChangedUpdateLiteralNameColumn(event);
            enumValuesTableViewer.refresh();
        }

        switch (event.getEventType()) {
            case ContentChangeEvent.TYPE_PARTS_CHANGED_POSITIONS:
            case ContentChangeEvent.TYPE_PART_ADDED:
            case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                reinit();
                updateEnabledStates();
                break;
            default:
                break;
        }
    }

    private void contentsChangedUpdateLiteralNameColumn(ContentChangeEvent event) {
        IIpsObjectPart part = event.getPart();
        if (part instanceof IEnumAttributeValue) {
            IEnumAttributeValue changedAttributeValue = (IEnumAttributeValue)part;
            IEnumAttribute changedEnumAttribute;
            changedEnumAttribute = changedAttributeValue.findEnumAttribute(ipsProject);
            if (changedEnumAttribute != null) {
                IEnumLiteralNameAttribute literalNameAttribute = enumType.getEnumLiteralNameAttribute();
                IEnumAttribute providerAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(literalNameAttribute
                        .getDefaultValueProviderAttribute());
                if (providerAttribute != null && providerAttribute.equals(changedEnumAttribute)) {
                    IEnumAttributeValue literalNameValue = changedAttributeValue.getEnumValue().getEnumAttributeValue(
                            literalNameAttribute);
                    if (literalNameValue != null) {
                        updateLiteralName(changedAttributeValue, literalNameValue);
                    }
                }
            }
        }
    }

    private void updateLiteralName(IEnumAttributeValue changedAttributeValue,
            IEnumAttributeValue literalNameAttributeValue) {
        String content = literalNameAttributeValue.getValue().getContentAsString();
        if (!StringUtils.isEmpty(content) && !lockAndSynchronizeLiteralNames) {
            return;
        }
        String newValue = changedAttributeValue.getValue().getDefaultLocalizedContent(ipsProject);
        String literalValue = null;
        if (newValue != null) {
            literalValue = ipsProject.getJavaNamingConvention().getEnumLiteral(newValue);
        }
        literalNameAttributeValue.setValue(ValueFactory.createStringValue(literalValue));
    }

    private static class EnumValueTraversalStrategy extends LinkedColumnsTraversalStrategy<IEnumValue> {

        private final int columnIndex;
        private final IEnumValueContainer enumValueContainer;

        private EnumValueTraversalStrategy(IEnumValueContainer enumValueContainer,
                FormattedCellEditingSupport<IEnumValue, ?> editingSupport, int columnIndex) {
            super(editingSupport);
            this.enumValueContainer = enumValueContainer;
            this.columnIndex = columnIndex;
        }

        @Override
        protected int getColumnIndex() {
            return columnIndex;
        }

        @Override
        protected IEnumValue getPreviousVisibleViewItem(IEnumValue currentViewItem) {
            List<IEnumValue> enumValues = enumValueContainer.getEnumValues();
            int index = enumValues.indexOf(currentViewItem);
            index--;
            if (index < 0) {
                index = 0;
            }
            return enumValues.get(index);
        }

        @Override
        protected IEnumValue getNextVisibleViewItem(IEnumValue currentViewItem) {
            List<IEnumValue> enumValues = enumValueContainer.getEnumValues();
            int index = enumValues.indexOf(currentViewItem);
            index++;
            if (index >= enumValues.size()) {
                try {
                    fireApplyEditorValue();
                    return enumValueContainer.newEnumValue();
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
            return enumValues.get(index);
        }
    }

    /** The content provider for the table viewer. */
    private static class EnumValuesContentProvider implements IStructuredContentProvider {

        private final IEnumValueContainer enumValueContainer;

        public EnumValuesContentProvider(IEnumValueContainer enumValueContainer) {
            this.enumValueContainer = enumValueContainer;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return enumValueContainer.getEnumValues().toArray();
        }

        @Override
        public void dispose() {
            // Nothing to dispose.
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do on input change event.
        }

    }

    /** The label provider for the table viewer. */
    private static class EnumValuesLabelProvider extends CellLabelProvider implements ITableLabelProvider {

        private boolean enumTypeEditing;

        public EnumValuesLabelProvider(boolean enumTypeEditing) {
            this.enumTypeEditing = enumTypeEditing;
        }

        @Override
        public void update(ViewerCell cell) {
            Object element = cell.getElement();
            int columnIndex = cell.getColumnIndex();
            cell.setImage(getColumnImage(element, columnIndex));
            cell.setText(getColumnText(element, columnIndex));
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            int severity = getSeverity((IEnumValue)element, columnIndex);
            ImageDescriptor overlay = IpsProblemOverlayIcon.getOverlay(severity);
            return IpsUIPlugin.getImageHandling().getImage(overlay, false);
        }

        /**
         * Returns <tt>true</tt> if the validation of the given <tt>IEnumValue</tt> detects an error
         * at the given column index, <tt>false</tt> otherwise.
         */
        private int getSeverity(IEnumValue enumValue, int columnIndex) {
            // Don't validate if the indicated column does not exist.
            if (enumValue.getEnumAttributeValues().size() <= columnIndex) {
                return Message.NONE;
            }
            try {
                MessageList messageList = enumValue.validate(enumValue.getIpsProject());
                return messageList.getMessagesFor(enumValue.getEnumAttributeValues().get(columnIndex), null)
                        .getSeverity();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            // There needs to be at least one column to be able to obtain label information.

            // For each requested column there must already be an EnumAttributeValue.
            List<IEnumAttributeValue> enumAttributeValues = ((IEnumValue)element).getEnumAttributeValues();
            if (enumAttributeValues.size() - 1 < columnIndex) {
                return null;
            }

            /*
             * Return text formatted by the IPS DatatypeFormatter if the referenced EnumAttribute
             * can be found and so the data type of the value is known. Return the value directly if
             * the EnumAttribute cannot be found.
             */
            IEnumAttributeValue enumAttributeValue = enumAttributeValues.get(columnIndex);
            IIpsProject ipsProject = enumAttributeValue.getIpsProject();
            String columnValue = IpsPlugin.getMultiLanguageSupport().getLocalizedContent(enumAttributeValue.getValue(),
                    ipsProject);
            try {
                IEnumAttribute enumAttribute = enumAttributeValue.findEnumAttribute(ipsProject);
                if (enumAttribute == null) {
                    return columnValue;
                }
                ValueDatatype valueDatatype;
                if (enumTypeEditing) {
                    valueDatatype = enumAttribute.findDatatypeIgnoreEnumContents(ipsProject);
                } else {
                    valueDatatype = enumAttribute.findDatatype(ipsProject);
                }
                return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(valueDatatype, columnValue);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Listener that reacts to <tt>SelectionChangedEvent</tt>s by deleting all empty rows at the
     * bottom of the table.
     */
    private static class RowDeletor implements ISelectionChangedListener {

        private final TableViewer enumValuesTableViewer;

        public RowDeletor(TableViewer enumValuesTableViewer) {
            this.enumValuesTableViewer = enumValuesTableViewer;
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            removeRedundantRows();
        }

        /**
         * Checks every row from the last up to the currently selected row for emptiness and deletes
         * every empty row until a non-empty row is found.
         * <p>
         * Only tries to delete rows if the table has more than one row.
         */
        private void removeRedundantRows() {
            if (enumValuesTableViewer.getTable().getItemCount() <= 1) {
                return;
            }
            for (int i = enumValuesTableViewer.getTable().getItemCount() - 1; i > enumValuesTableViewer.getTable()
                    .getSelectionIndex(); i--) {
                IEnumValue currentEnumValue = (IEnumValue)enumValuesTableViewer.getElementAt(i);
                if (isRowEmpty(currentEnumValue)) {
                    enumValuesTableViewer.remove(currentEnumValue);
                    currentEnumValue.delete();
                } else {
                    break;
                }
            }
        }

        /**
         * Checks whether a row (<tt>IEnumValue</tt>) is empty or not. Returns <tt>true</tt> if all
         * the given row's values (columns) contain a whitespace string.
         * <p>
         * The value <tt>null</tt> is treated as content. Thus a row that contains <tt>null</tt>
         * values is not empty.
         */
        private boolean isRowEmpty(IEnumValue enumValue) {
            for (IEnumAttributeValue attrValue : enumValue.getEnumAttributeValues()) {
                if (attrValue.getValue() != null) {
                    final String contentAsString = attrValue.getValue().getContentAsString();
                    if (!StringUtils.isEmpty(contentAsString)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}
