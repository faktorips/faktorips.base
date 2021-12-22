/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.SelectionStatusBarPublisher;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * The <code>EnumContentEditorPage</code> shows general information about an
 * <code>IEnumContent</code> and provides controls to edit, import and export its values. It is
 * intended to be used with the <code>EnumContentEditor</code>.
 * <p>
 * This page is a listener for changes in the IPS model: If the <code>IEnumType</code> the edited
 * <code>IEnumContent</code> is built upon changes the enabled states of the tool bar buttons will
 * be updated.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentEditorPage extends IpsObjectEditorPage implements ContentsChangeListener {

    /**
     * The <code>UIToolkit</code> used for updating the changeability states.
     */
    private UIToolkit uiToolkit;

    /** Values section showing the <code>IEnumValue</code>s. */
    private EnumValuesSection enumValuesSection;

    /**
     * The <code>IEnumContent</code> the <code>EnumContentEditor</code> this page belongs to is
     * currently editing.
     */
    private IEnumContent enumContent;

    /** The action to open a <code>FixEnumContentWizard</code>. */
    private IAction openFixEnumContentDialogAction;

    private EnumImportExportActionInEditor importAction;

    private EnumImportExportActionInEditor exportAction;

    private SelectionStatusBarPublisher selectionStatusBarPublisher;

    /**
     * Creates a new <code>EnumContentEditorPage</code>.
     * 
     * @param editor The <code>EnumContentEditor</code> this page belongs to.
     */
    public EnumContentEditorPage(EnumContentEditor editor) {
        super(editor, "EnumContentEditorPage", Messages.EnumContentValuesPage_title); //$NON-NLS-1$
        enumContent = editor.getEnumContent();
        enumContent.getIpsModel().addChangeListener(this);
    }

    @Override
    public void dispose() {
        enumContent.getIpsModel().removeChangeListener(this);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));

        uiToolkit = toolkit;

        createToolbarActions();

        createToolbar();

        new EnumContentGeneralInfoSection(this, enumContent, formBody, toolkit);
        try {
            enumValuesSection = new EnumValuesSection(enumContent, getEditorSite(), formBody, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        selectionStatusBarPublisher = new SelectionStatusBarPublisher(getEditorSite());

        TableViewer tab = enumValuesSection.getEnumValueTableViewer();
        tab.addSelectionChangedListener(
                event -> selectionStatusBarPublisher.updateMarkedRows(rowsFromSelection(event.getSelection())));
    }

    private List<Integer> rowsFromSelection(ISelection selection) {
        List<Integer> rowNumbers = new ArrayList<>();
        if (!selection.isEmpty()) {
            Collection<IEnumValue> rows = TypedSelection.createAnyCount(IEnumValue.class, selection).getElements();
            for (IEnumValue row : rows) {
                rowNumbers.add(row.getEnumValueContainer().getIndexOfEnumValue(row));
            }
        }
        return rowNumbers;
    }

    private void createToolbarActions() {
        openFixEnumContentDialogAction = new OpenFixEnumContentWizardAction(this, enumContent, getSite().getShell());
        importAction = new EnumImportExportActionInEditor(getSite().getShell(), enumContent, true);
        exportAction = new EnumImportExportActionInEditor(getSite().getShell(), enumContent, false);

    }

    private void createToolbar() {
        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(openFixEnumContentDialogAction);
        form.getToolBarManager().add(importAction);
        form.getToolBarManager().add(exportAction);

        form.updateToolBar();
        updateToolbarActionsEnabledStates();
    }

    @Override
    protected void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        Composite header = getManagedForm().getForm().getForm().getHead();
        uiToolkit.setDataChangeable(header, true);
        updateToolbarVisibilityState(changeable);
    }

    /**
     * Update the visibility of the toolbar items based on the current changeability state.
     * <p>
     * The export functionality should be always available.
     * 
     * @param changeable Whether the page content is changeable
     */
    protected void updateToolbarVisibilityState(boolean changeable) {
        IToolBarManager toolBarManager = getManagedForm().getForm().getToolBarManager();
        Arrays.stream(toolBarManager.getItems())
                .filter(item -> item.getId() == null || !item.getId().equals(exportAction.getId()))
                .forEach(item -> item.setVisible(changeable));
        toolBarManager.update(true);
    }

    /**
     * Updates the enabled states of the tool bar.
     * <p>
     * The <code>OpenFixEnumContentWizardAction</code> will be enabled if the <code>IEnumType</code>
     * the <code>IEnumContent</code> to edit is built upon
     * <ul>
     * <li>does not exist or is missing.
     * <li>is abstract.
     * <li>defines its values in the model.
     * <li>defines not the exact number of <code>IEnumAttribute</code>s as there are
     * <code>IEnumAttributeValue</code>s in the <code>IEnumContent</code> to edit.
     * <li>defines not the same enumeration attribute names as stored in the
     * <code>IEnumContent</code> to edit.
     * <li>defines not the same ordering of <code>IEnumAttribute</code>s as stored in the
     * <code>IEnumContent</code> to edit.
     * </ul>
     */
    private void updateToolbarActionsEnabledStates() {
        try {
            boolean enableOpenFixEnumTypeDialogAction = enumContent.isFixToModelRequired();
            openFixEnumContentDialogAction.setEnabled(enableOpenFixEnumTypeDialogAction);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        IEnumType enumType;
        enumType = enumContent.findEnumType(enumContent.getIpsProject());

        /*
         * Return if the content changed was not the EnumContent to be edited or the referenced
         * EnumType.
         */
        IIpsSrcFile changedIpsSrcFile = event.getIpsSrcFile();
        if (!(changedIpsSrcFile.equals(enumContent.getIpsSrcFile()))) {
            if (enumType != null) {
                if (!(changedIpsSrcFile.equals(enumType.getIpsSrcFile()))) {
                    return;
                }
            }
        }

        updateToolbarActionsEnabledStates();
    }

    public EnumValuesSection getEnumValuesSection() {
        return enumValuesSection;
    }

    /** Executes the <code>EnumImportExportOperation</code> and refreshes the view. */
    private class EnumImportExportActionInEditor extends EnumImportExportAction {

        /**
         * ID used for identifying the import action within the toolbar.
         */
        private static final String IMPORT_ACTION_ID = "enum_content_import_action"; //$NON-NLS-1$
        /**
         * ID used for identifying the export action within the toolbar.
         */
        private static final String EXPORT_ACTION_ID = "enum_content_export_action"; //$NON-NLS-1$

        public EnumImportExportActionInEditor(Shell shell, IEnumValueContainer enumValueContainer, boolean isImport) {
            super(shell, enumValueContainer);
            if (isImport) {
                initImportAction();
                setId(IMPORT_ACTION_ID);
            } else {
                initExportAction();
                setId(EXPORT_ACTION_ID);
            }
        }

        @Override
        public void run(IStructuredSelection selection) {
            if (super.runInternal(selection)) {
                enumValuesSection.refresh();
            }
        }
    }
}
