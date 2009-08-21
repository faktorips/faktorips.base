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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.core.ui.editors.type.TypeEditorStructurePage;

/**
 * The <tt>EnumContentPage</tt> shows general information about an <tt>IEnumContent</tt> and
 * provides controls to edit, import and export its values. It is intended to be used with the
 * <tt>EnumContentEditor</tt>.
 * <p>
 * This page is a listener for changes in the IPS model: If the <tt>IEnumType</tt> the edited
 * <tt>IEnumContent</tt> is built upon changes the enabled states of the tool bar buttons will be
 * updated.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentPage extends TypeEditorStructurePage implements ContentsChangeListener {

    /**
     * The <tt>IEnumContent</tt> the <tt>EnumContentEditor</tt> this page belongs to is currently
     * editing.
     */
    private IEnumContent enumContent;

    /** The action to open a <tt>FixEnumContentWizard</tt>. */
    private IAction openFixEnumTypeDialogAction;

    private EnumImportExportActionInEditor importAction;

    private EnumImportExportActionInEditor exportAction;

    EnumValuesSection enumValuesSection;

    /**
     * Creates a new <tt>EnumContentPage</tt>.
     * 
     * @param editor The <tt>EnumContentEditor</tt> this page belongs to.
     */
    public EnumContentPage(EnumContentEditor editor) {
        super(editor, false, Messages.EnumContentValuesPage_title);
        enumContent = editor.getEnumContent();
        enumContent.getIpsModel().addChangeListener(this);
    }

    @Override
    public void dispose() {
        enumContent.getIpsModel().removeChangeListener(this);
    }

    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        createContent(parentContainer, toolkit);
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        createContent(parentContainer, toolkit);
    }

    /** Creates the content of this page. */
    private void createContent(Composite parentContainer, UIToolkit toolkit) {
        createToolbarActions();
        createToolbar();

        new EnumContentGeneralInfoSection(this, enumContent, parentContainer, toolkit);
        try {
            enumValuesSection = new EnumValuesSection(enumContent, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /** Creates the actions for the tool bar. */
    private void createToolbarActions() {
        openFixEnumTypeDialogAction = new OpenFixEnumContentWizardAction(this, enumContent, getSite().getShell());
        importAction = new EnumImportExportActionInEditor(getSite().getShell(), enumContent, true);
        exportAction = new EnumImportExportActionInEditor(getSite().getShell(), enumContent, false);

    }

    /** Creates the tool bar of this page. */
    private void createToolbar() {
        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(openFixEnumTypeDialogAction);
        form.getToolBarManager().add(importAction);
        form.getToolBarManager().add(exportAction);

        form.updateToolBar();
        updateToolbarActionsEnabledStates();
    }

    /**
     * Updates the enabled states of the tool bar.
     * <p>
     * The <tt>OpenFixEnumContentWizardAction</tt> will be enabled if the <tt>IEnumType</tt> the
     * <tt>IEnumContent</tt> to edit is built upon
     * <ul>
     * <li>does not exist or is missing.
     * <li>is abstract.
     * <li>defines its values in the model.
     * <li>defines not the exact number of <tt>IEnumAttribute</tt>s as there are columns in the
     * enumeration values table of the <tt>EnumValuesSection</tt>.
     * </ul>
     */
    private void updateToolbarActionsEnabledStates() {
        boolean enableOpenFixEnumTypeDialogAction = false;

        String enumTypeQualifiedName = enumContent.getEnumType();
        if (enumTypeQualifiedName.equals("")) {
            enableOpenFixEnumTypeDialogAction = true;
        } else {
            IEnumType enumType;
            try {
                enumType = enumContent.findEnumType(enumContent.getIpsProject());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (enumType == null) {
                enableOpenFixEnumTypeDialogAction = true;
            } else {
                if (enumType.isAbstract()
                        || enumType.isContainingValues()
                        || enumType.getEnumAttributesCountIncludeSupertypeCopies(false) != enumContent
                                .getReferencedEnumAttributesCount()) {
                    enableOpenFixEnumTypeDialogAction = true;
                }
            }
        }

        openFixEnumTypeDialogAction.setEnabled(enableOpenFixEnumTypeDialogAction);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {

    }

    public void contentsChanged(ContentChangeEvent event) {
        IEnumType enumType;
        try {
            enumType = enumContent.findEnumType(enumContent.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

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

        EnumContentPage.this.updateToolbarActionsEnabledStates();
    }

    /** Executes the <tt>EnumImportExportOperation</tt> and refreshes the view. */
    private class EnumImportExportActionInEditor extends EnumImportExportAction {

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
                enumValuesSection.refresh();
            }
        }

    }

}
