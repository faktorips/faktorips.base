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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;

/**
 * Base page for <tt>IEnumType</tt> editors providing controls to edit its properties and
 * attributes.
 * <p>
 * This class also adds controls to import an export <tt>IEnumValue</tt>s.
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
     * The <tt>IEnumType</tt> the <tt>EnumTypeEditor</tt> this page belongs to is currently editing.
     */
    IEnumType enumType;

    /** Attributes section to show the <tt>IEnumAttribute</tt>s. */
    EnumAttributesSection enumAttributesSection;

    /** Values section showing the <tt>IEnumValue</tt>s. */
    EnumValuesSection enumValuesSection;

    /**
     * Action to import <tt>IEnumValue</tt>s into the opened <tt>IEnumType</tt> from an external
     * file.
     */
    private EnumImportExportActionInEditor importAction;

    /**
     * Action to export the <tt>IEnumValue</tt>s of the opened <tt>IEnumType</tt> to an external
     * file.
     */
    private EnumImportExportActionInEditor exportAction;

    /**
     * Listener responsible for toggling the import/export actions and to refresh the
     * <tt>EnumAttributesSection</tt> on page change.
     */
    protected ContentsChangeListener changeListener;

    public EnumTypeEditorPage(EnumTypeEditor editor) {
        super(editor, "EnumTypeEditorPage", Messages.EnumTypeStructurePage_title); //$NON-NLS-1$
        setPartName(Messages.EnumTypeStructurePage_title + ' ' + Messages.EnumTypeStructurePage_andLiteral + ' '
                + Messages.EnumTypeValuesPage_title);

        enumType = editor.getEnumType();
        changeListener = new ContentsChangeListener() {
            @Override
            public void contentsChanged(ContentChangeEvent event) {
                if (event.getIpsSrcFile().equals(enumType.getIpsSrcFile())) {
                    updateToolbarActionEnabledStates();
                    enumAttributesSection.refresh();
                }
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
        formBody.setLayout(createPageLayout(1, false));

        createToolbarActions();

        new EnumTypeGeneralInfoSection(enumType, formBody, toolkit);

        Composite remainerSection = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_HORIZONTAL);
        enumAttributesSection = new EnumAttributesSection(this, enumType, remainerSection, toolkit);
        try {
            enumValuesSection = new EnumValuesSection(enumType, formBody, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void createToolbarActions() {
        importAction = new EnumImportExportActionInEditor(getSite().getShell(), enumType, true);
        exportAction = new EnumImportExportActionInEditor(getSite().getShell(), enumType, false);

        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(importAction);
        form.getToolBarManager().add(exportAction);

        form.updateToolBar();

        updateToolbarActionEnabledStates();
    }

    /**
     * Enable the import and export operation if the <tt>IEnumType</tt> contains values and is not
     * abstract.
     */
    protected void updateToolbarActionEnabledStates() {
        boolean enableImportExportActions = enumType.isContainingValues() && !(enumType.isAbstract());
        if (importAction != null) {
            importAction.setEnabled(enableImportExportActions);
        }
        if (exportAction != null) {
            exportAction.setEnabled(enableImportExportActions);
        }
    }

    /**
     * Extend <tt>EnumImportExportAction</tt> in order to react to import operations and update the
     * view after the operation is completed.
     */
    class EnumImportExportActionInEditor extends EnumImportExportAction {

        public EnumImportExportActionInEditor(Shell shell, IEnumValueContainer enumValueContainer, boolean isImport) {
            super(shell, enumValueContainer);
            if (isImport) {
                initImportAction();
            } else {
                initExportAction();
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
