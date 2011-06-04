/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

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

        protected AssociationsComposite(IType type, Composite parent, UIToolkit toolkit) {
            this(type, parent, true, true, true, true, true, toolkit);
        }

        protected AssociationsComposite(IType type, Composite parent, boolean canCreate, boolean canEdit,
                boolean canDelete, boolean canMove, boolean showEditButton, UIToolkit toolkit) {

            super(type, parent, getSite(), canCreate, canEdit, canDelete, canMove, showEditButton, true, false, true,
                    toolkit);
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
