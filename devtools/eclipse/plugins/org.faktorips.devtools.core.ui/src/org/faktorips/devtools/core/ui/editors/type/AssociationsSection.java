/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.EnumSet;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.wizards.type.ConstrainableAssociationPmo;
import org.faktorips.devtools.core.ui.wizards.type.ConstrainableAssociationWizard;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.type.IType;

/**
 * A section to display and edit a type's associations.
 * 
 * @author Jan Ortmann
 */
public abstract class AssociationsSection extends SimpleIpsPartsSection {

    protected AssociationsSection(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(type, parent, site, ExpandableComposite.TITLE_BAR, Messages.AssociationsSection_title, toolkit);
    }

    protected IType getType() {
        return (IType)getIpsObject();
    }

    /**
     * A composite that shows a type's associations in a viewer and allows to edit associations in a
     * dialog, create new associations and delete associations.
     */
    protected abstract static class AssociationsComposite extends IpsPartsComposite {

        private IpsAction openTargetAction;
        private IType type;

        protected AssociationsComposite(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
            this(type, parent, EnumSet.of(Option.CAN_CREATE, Option.CAN_EDIT, Option.CAN_OVERRIDE, Option.CAN_DELETE,
                    Option.CAN_MOVE, Option.SHOW_EDIT_BUTTON, Option.RENAME_REFACTORING_SUPPORTED,
                    Option.JUMP_TO_SOURCE_CODE_SUPPORTED), site, toolkit);
        }

        protected AssociationsComposite(IType type, Composite parent, EnumSet<Option> attributesForButtons,
                IWorkbenchPartSite site, UIToolkit toolkit) {
            super(type, parent, site, attributesForButtons, toolkit);
            this.type = type;
            openTargetAction = createOpenTargetAction();
        }

        protected abstract IpsAction createOpenTargetAction();

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getType().newAssociation();
        }

        @Override
        protected void createContextMenuThis(MenuManager contextMenuManager) {
            contextMenuManager.add(new Separator());
            contextMenuManager.add(openTargetAction);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveAssociations(indexes, up);
        }

        @Override
        protected void openLink() {
            openTargetAction.run();
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new AssociationsLabelProvider();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new AssociationContentProvider(getType());
        }

        @Override
        public void overrideClicked() {
            ConstrainableAssociationPmo pmo = new ConstrainableAssociationPmo(type);
            ConstrainableAssociationWizard wizard = new ConstrainableAssociationWizard(pmo);
            WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
            if (wizardDialog.open() == Window.OK) {
                refresh();
            }
        }

        private static class AssociationContentProvider implements IStructuredContentProvider {

            private IType type;

            public AssociationContentProvider(IType type) {
                this.type = type;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return type.getAssociations().toArray();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        }

    }

}
