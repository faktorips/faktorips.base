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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.core.ui.editors.type.TypeEditorStructurePage;

/**
 * The <code>EnumTypeValuesPage</code> provides controls to edit the values of an
 * <code>IEnumType</code> and is intended to be used with the <code>EnumTypeEditor</code>.
 * 
 * @see EnumTypeEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeValuesPage extends TypeEditorStructurePage {

    /** The enum type the enum type editor this page belongs to is currently editing. */
    private IEnumType enumType;
    private EnumValuesSection enumValuesSection;
    private org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeValuesPage.EnumImportExportActionInEditor importAction;
    private org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeValuesPage.EnumImportExportActionInEditor exportAction;

    /**
     * Creates a new <code>EnumContentValuesPage</code>.
     * 
     * @param editor The <code>EnumTypeEditor</code> this page belongs to.
     * @param splittedStructure If this flag is set to <code>true</code> the enum values table won't
     *            be part of the page.
     */
    public EnumTypeValuesPage(EnumTypeEditor editor, boolean splittedStructure) {
        super(editor, splittedStructure, Messages.EnumTypeValuesPage_title);

        enumType = editor.getEnumType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        try {
            createToolbarActionsAndIcons();
            enumValuesSection = new EnumValuesSection(enumType, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        try {
            createToolbarActionsAndIcons();
            enumValuesSection = new EnumValuesSection(enumType, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // nothing to do
    }

    
    private void createToolbarActionsAndIcons() {
        importAction = new EnumImportExportActionInEditor(getSite().getShell(), enumType, true);
        exportAction = new EnumImportExportActionInEditor(getSite().getShell(), enumType, false);

        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(importAction);
        form.getToolBarManager().add(exportAction);
        
        form.updateToolBar();
    }
    
    /** 
     * Extend <code>EnumImportExportAction</code> in order to react to import operations 
     * and update the view after the operation is completed.
     */
    private class EnumImportExportActionInEditor extends EnumImportExportAction {
        public EnumImportExportActionInEditor(Shell shell, IEnumValueContainer enumValueContainer, boolean isImport) {
            super(shell, enumValueContainer);
            if (isImport) {
                initImportAction();
            } else {
                initExportAction();
            }            
        }

        /**
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            if (super.runInternal(selection)) {
                enumValuesSection.refresh();
            }
        }        
    }
    
}
