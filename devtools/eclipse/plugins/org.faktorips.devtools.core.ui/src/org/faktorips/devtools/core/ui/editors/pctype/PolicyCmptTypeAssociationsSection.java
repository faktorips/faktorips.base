/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.EnumSet;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.pctype.associationwizard.NewPcTypeAssociationWizard;
import org.faktorips.devtools.core.ui.editors.type.AssociationsSection;
import org.faktorips.devtools.core.ui.wizards.NextButtonDefaultWizardDialog;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.util.memento.Memento;

public class PolicyCmptTypeAssociationsSection extends AssociationsSection {

    public PolicyCmptTypeAssociationsSection(IPolicyCmptType policyCmptType, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(policyCmptType, parent, site, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PolicyCmptTypeAssociationsComposite(getPolicyCmptType(), parent, getSite(), toolkit);
    }

    private IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private static class PolicyCmptTypeAssociationsComposite extends AssociationsComposite {

        private Button wizardNewButton;

        private PolicyCmptTypeAssociationsComposite(IPolicyCmptType policyCmptType, Composite parent,
                IWorkbenchPartSite site, UIToolkit toolkit) {
            /*
             * Create default buttons without the 'New' button, because the 'New' button will be
             * overridden with wizard functionality.
             */
            super(policyCmptType, parent,
                    EnumSet.of(Option.CAN_EDIT, Option.CAN_OVERRIDE, Option.CAN_DELETE, Option.CAN_MOVE,
                            Option.SHOW_EDIT_BUTTON, Option.RENAME_REFACTORING_SUPPORTED,
                            Option.JUMP_TO_SOURCE_CODE_SUPPORTED),
                    site, toolkit);
        }

        @Override
        protected IpsAction createOpenTargetAction() {
            return new OpenTargetPcTypeInEditorAction(getViewer(), getType());
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            getUiToolkit().setDataChangeable(wizardNewButton, flag);
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AssociationEditDialog((IPolicyCmptTypeAssociation)part, shell);
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            createWizardNewButton(buttons, toolkit);
            super.createButtons(buttons, toolkit);
            return true;
        }

        /**
         * Creates the "New..." button to initiate the new-relation-wizard.
         */
        private void createWizardNewButton(Composite buttons, UIToolkit toolkit) {
            wizardNewButton = toolkit.createButton(buttons, Messages.PolicyCmptTypeAssociationsSection_newButton);
            wizardNewButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            wizardNewButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    newWizardClicked();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent event) {
                    // Nothing to do
                }
            });
        }

        /**
         * Opens up the new-association-wizard.
         */
        private void newWizardClicked() {
            IIpsSrcFile file = getIpsObject().getIpsSrcFile();
            boolean dirty = file.isDirty();
            Memento memento = getIpsObject().newMemento();
            IIpsObjectPart newRelation = newIpsPart();
            NextButtonDefaultWizardDialog dialog = new NextButtonDefaultWizardDialog(getShell(),
                    new NewPcTypeAssociationWizard((IPolicyCmptTypeAssociation)newRelation));
            dialog.open();
            if (dialog.getReturnCode() == Window.CANCEL) {
                getIpsObject().setState(memento);
                if (!dirty) {
                    file.markAsClean();
                }
            }
            refresh();
        }

    }

    private static class OpenTargetPcTypeInEditorAction extends IpsAction {

        private final IType type;

        public OpenTargetPcTypeInEditorAction(ISelectionProvider selectionProvider, IType type) {
            super(selectionProvider);
            this.type = type;
            setText(Messages.PolicyCmptTypeAssociationsSection_menuOpenTargetInNewEditor);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IPolicyCmptTypeAssociation);
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IPolicyCmptTypeAssociation) {
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)selected;
                IType target = policyCmptTypeAssociation.findTarget(type.getIpsProject());
                IpsUIPlugin.getDefault().openEditor(target);
            }
        }

    }

}
