/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.type;

import java.util.EnumSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.wizards.type.ConstrainableAssociationPmo;
import org.faktorips.devtools.core.ui.wizards.type.ConstrainableAssociationWizard;

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
    protected abstract class AssociationsComposite extends IpsPartsComposite {

        private IpsAction openTargetAction;
        private IType type;

        protected AssociationsComposite(IType type, Composite parent, UIToolkit toolkit) {
            this(type, parent, EnumSet.of(AttributesForButtons.CAN_CREATE, AttributesForButtons.CAN_EDIT,
                    AttributesForButtons.CAN_OVERRIDE, AttributesForButtons.CAN_DELETE, AttributesForButtons.CAN_MOVE,
                    AttributesForButtons.SHOW_EDIT_BUTTON, AttributesForButtons.RENAME_REFACTORING_SUPPORTED,
                    AttributesForButtons.JUMP_TO_SOURCE_CODE_SUPPORTED), toolkit);
        }

        protected AssociationsComposite(IType type, Composite parent,
                EnumSet<AttributesForButtons> attributesForButtons, UIToolkit toolkit) {
            super(type, parent, getSite(), attributesForButtons, toolkit);
            this.type = type;
            openTargetAction = createOpenTargetAction();
        }

        protected abstract IpsAction createOpenTargetAction();

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
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
            return new AssociationContentProvider();
        }

        @Override
        public void overrideClicked() {
            ConstrainableAssociationPmo pmo = new ConstrainableAssociationPmo(type);
            ConstrainableAssociationWizard wizard = new ConstrainableAssociationWizard(pmo);
            WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
            wizardDialog.open();
        }

        private class AssociationContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return getType().getAssociations().toArray();
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
