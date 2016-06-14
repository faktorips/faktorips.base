/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.internal.model.enums.EnumValue;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.SearchSelectionBar;
import org.faktorips.devtools.core.ui.editors.SelectionStatusBarPublisher;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.core.ui.util.TypedSelection;

/**
 * The <tt>EnumContentEditorPage</tt> shows general information about an <tt>IEnumContent</tt> and
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
public class EnumContentEditorPage extends IpsObjectEditorPage implements ContentsChangeListener {

    /** Values section showing the <tt>IEnumValue</tt>s. */
    private EnumValuesSection enumValuesSection;

    /**
     * The <tt>IEnumContent</tt> the <tt>EnumContentEditor</tt> this page belongs to is currently
     * editing.
     */
    private IEnumContent enumContent;

    /** The action to open a <tt>FixEnumContentWizard</tt>. */
    private IAction openFixEnumContentDialogAction;

    private EnumImportExportActionInEditor importAction;

    private EnumImportExportActionInEditor exportAction;

    private SelectionStatusBarPublisher selectionStatusBarPublisher;

    /**
     * Creates a new <tt>EnumContentEditorPage</tt>.
     * 
     * @param editor The <tt>EnumContentEditor</tt> this page belongs to.
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
        SearchSelectionBar searchSelectionLogic = new SearchSelectionBar(formBody, toolkit);

        createToolbarActions();

        createToolbar();

        new EnumContentGeneralInfoSection(this, enumContent, formBody, toolkit);
        try {
            enumValuesSection = new EnumValuesSection(enumContent, formBody, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        selectionStatusBarPublisher = new SelectionStatusBarPublisher(getEditorSite());

        TableViewer tab = enumValuesSection.getEnumValueTableViewer();
        tab.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectionStatusBarPublisher.updateMarkedRows(rowsFromSelection(event.getSelection()));
            }
        });

        searchSelectionLogic.init(tab);
    }

    private List<Integer> rowsFromSelection(ISelection selection) {
        List<Integer> rowNumbers = new ArrayList<Integer>();
        if (!selection.isEmpty()) {
            Collection<EnumValue> rows = TypedSelection.createAnyCount(EnumValue.class, selection).getElements();
            for (EnumValue row : rows) {
                rowNumbers.add(calculateEnumRowNr(row));
            }
        }
        return rowNumbers;
    }

    private int calculateEnumRowNr(EnumValue enumValue) {
        int a = 0;
        try {
            IIpsElement[] arr = enumValue.getParent().getChildren();
            while (enumValue != arr[a]) {
                a++;
            }
        } catch (CoreException e) {
            // Just for safety reasons
            e.printStackTrace();
        }
        return a - 1;
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

    /**
     * Updates the enabled states of the tool bar.
     * <p>
     * The <tt>OpenFixEnumContentWizardAction</tt> will be enabled if the <tt>IEnumType</tt> the
     * <tt>IEnumContent</tt> to edit is built upon
     * <ul>
     * <li>does not exist or is missing.
     * <li>is abstract.
     * <li>defines its values in the model.
     * <li>defines not the exact number of <tt>IEnumAttribute</tt>s as there are
     * <tt>IEnumAttributeValue</tt>s in the <tt>IEnumContent</tt> to edit.
     * <li>defines not the same enumeration attribute names as stored in the <tt>IEnumContent</tt>
     * to edit.
     * <li>defines not the same ordering of <tt>IEnumAttribute</tt>s as stored in the
     * <tt>IEnumContent</tt> to edit.
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
