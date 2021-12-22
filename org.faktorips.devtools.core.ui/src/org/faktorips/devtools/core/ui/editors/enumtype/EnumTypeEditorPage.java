/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * Base page for <code>IEnumType</code> editors providing controls to edit its properties and
 * attributes.
 * <p>
 * This class also adds controls to import an export <code>IEnumValue</code>s.
 * 
 * @see EnumTypeEditor
 * 
 * @author Alexander Weickmann
 * @author Roman Grutza
 * 
 * @since 2.3
 */
public class EnumTypeEditorPage extends IpsObjectEditorPage {

    /**
     * The <code>UIToolkit</code> used for updating the changeability states.
     */
    private UIToolkit uiToolkit;

    /**
     * The <code>IEnumType</code> the <code>EnumTypeEditor</code> this page belongs to is currently
     * editing.
     */
    private IEnumType enumType;

    /** Attributes section to show the <code>IEnumAttribute</code>s. */
    private EnumAttributesSection enumAttributesSection;

    /** Values section showing the <code>IEnumValue</code>s. */
    private EnumValuesSection enumValuesSection;

    /**
     * Action to import <code>IEnumValue</code>s into the opened <code>IEnumType</code> from an
     * external file.
     */
    private EnumImportExportActionInEditor importAction;

    /**
     * Action to export the <code>IEnumValue</code>s of the opened <code>IEnumType</code> to an
     * external file.
     */
    private EnumImportExportActionInEditor exportAction;

    /**
     * Listener responsible for toggling the import/export actions and to refresh the
     * <code>EnumAttributesSection</code> on page change.
     */
    private ContentsChangeListener changeListener;

    public EnumTypeEditorPage(EnumTypeEditor editor) {
        super(editor, "EnumTypeEditorPage", Messages.EnumTypeStructurePage_title); //$NON-NLS-1$
        setPartName(Messages.EnumTypeStructurePage_title + ' ' + Messages.EnumTypeStructurePage_andLiteral + ' '
                + Messages.EnumTypeValuesPage_title);

        enumType = editor.getEnumType();
        changeListener = event -> {
            if (event.getIpsSrcFile().equals(enumType.getIpsSrcFile())) {
                updateToolbarActionEnabledStates();
                enumAttributesSection.refresh();
            }
        };
        enumType.getIpsModel().addChangeListener(changeListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        enumType.getIpsModel().removeChangeListener(changeListener);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);
        formBody.setLayout(createPageLayout(1, false));

        uiToolkit = toolkit;

        new EnumTypeGeneralInfoSection(enumType, formBody, toolkit);
        enumAttributesSection = new EnumAttributesSection(enumType, formBody, getSite(), toolkit);
        try {
            enumValuesSection = new EnumValuesSection(enumType, getEditorSite(), formBody, toolkit);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        enumAttributesSection.setEnumValuesSection(enumValuesSection);
    }

    @Override
    protected void createToolbarActions(IToolBarManager toolbarManager) {
        importAction = new EnumImportExportActionInEditor(getSite().getShell(), enumType, true);
        exportAction = new EnumImportExportActionInEditor(getSite().getShell(), enumType, false);

        toolbarManager.add(importAction);
        toolbarManager.add(exportAction);
        toolbarManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));

        IMenuService menuService = getSite().getService(IMenuService.class);
        menuService.populateContributionManager((ContributionManager)toolbarManager,
                MenuUtil.toolbarUri(IpsMenuId.TOOLBAR_ENUM_TYPE_EDITOR_PAGE.getId()));
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
     * Enable the import and export operation if the <code>IEnumType</code> contains values and is
     * not abstract.
     */
    protected void updateToolbarActionEnabledStates() {
        boolean enableImportExportActions;
        enableImportExportActions = enumType.isCapableOfContainingValues();
        if (importAction != null) {
            importAction.setEnabled(enableImportExportActions);
        }
        if (exportAction != null) {
            exportAction.setEnabled(enableImportExportActions);
        }
    }

    /**
     * Extend <code>EnumImportExportAction</code> in order to react to import operations and update
     * the view after the operation is completed.
     */
    class EnumImportExportActionInEditor extends EnumImportExportAction {

        /**
         * ID used for identifying the import action within the toolbar.
         */
        private static final String IMPORT_ACTION_ID = "enum_type_import_action"; //$NON-NLS-1$
        /**
         * ID used for identifying the export action within the toolbar.
         */
        private static final String EXPORT_ACTION_ID = "enum_type_export_action"; //$NON-NLS-1$

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
                if (enumValuesSection != null) {
                    enumValuesSection.refresh();
                }
            }
        }

    }

}
