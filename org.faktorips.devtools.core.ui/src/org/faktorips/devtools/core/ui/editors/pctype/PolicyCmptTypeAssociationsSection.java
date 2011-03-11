/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.pctype.associationwizard.NewPcTypeAssociationWizard;
import org.faktorips.devtools.core.ui.editors.type.AssociationsSection;
import org.faktorips.util.memento.Memento;

public class PolicyCmptTypeAssociationsSection extends AssociationsSection {

    private PolicyCmptTypeAssociationsComposite associationsComposite;

    public PolicyCmptTypeAssociationsSection(IpsObjectEditorPage editorPage, IPolicyCmptType policyCmptType,
            Composite parent, UIToolkit toolkit) {

        super(editorPage, policyCmptType, parent, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        associationsComposite = new PolicyCmptTypeAssociationsComposite(getPolicyCmptType(), parent, toolkit);
        return associationsComposite;
    }

    @Override
    protected AssociationsComposite getAssociationsComposite() {
        return associationsComposite;
    }

    private IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private class PolicyCmptTypeAssociationsComposite extends AssociationsComposite {

        private Button wizardNewButton;

        private PolicyCmptTypeAssociationsComposite(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
            /*
             * Create default buttons without the 'New' button, because the 'New' button will be
             * overridden with wizard functionality.
             */
            super(policyCmptType, parent, false, true, true, true, true, toolkit);
        }

        @Override
        protected IpsAction createOpenTargetAction() {
            return new OpenTargetPcTypeInEditorAction(getViewer());
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
            wizardNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            wizardNewButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    try {
                        newWizardClicked();
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
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
        private void newWizardClicked() throws CoreException {
            IIpsSrcFile file = getIpsObject().getIpsSrcFile();
            boolean dirty = file.isDirty();
            Memento memento = getIpsObject().newMemento();
            IIpsObjectPart newRelation = newIpsPart();
            WizardDialog dialog = new WizardDialog(getShell(), new NewPcTypeAssociationWizard(
                    (IPolicyCmptTypeAssociation)newRelation));
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

    private class OpenTargetPcTypeInEditorAction extends IpsAction {

        public OpenTargetPcTypeInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
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
                try {
                    IType target = policyCmptTypeAssociation.findTarget(getPolicyCmptType().getIpsProject());
                    IpsUIPlugin.getDefault().openEditor(target);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}
