/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.eclipse.swt.widgets.Menu;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRenameHandler;

/**
 * A section to display and edit a type's associations.
 * 
 * @author Jan Ortmann
 */
public abstract class AssociationsSection extends SimpleIpsPartsSection {

    private final IpsObjectEditorPage editorPage;

    protected AssociationsSection(IpsObjectEditorPage editorPage, IType type, Composite parent, UIToolkit toolkit) {
        super(type, parent, Messages.AssociationsSection_title, toolkit);
        this.editorPage = editorPage;
        getAssociationsComposite().createContextMenu();
    }

    protected abstract AssociationsComposite getAssociationsComposite();

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

            super(type, parent, canCreate, canEdit, canDelete, canMove, showEditButton, toolkit);
            openTargetAction = createOpenTargetAction();
        }

        protected abstract IpsAction createOpenTargetAction();

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return getType().newAssociation();
        }

        private void createContextMenu() {
            MenuManager refactorSubmenu = new MenuManager(Messages.AssociationsSection_submenuRefactor);
            refactorSubmenu.add(IpsRefactoringHandler.getContributionItem(IpsRenameHandler.CONTRIBUTION_ID));

            MenuManager menuManager = new MenuManager();
            menuManager.add(refactorSubmenu);
            menuManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
            menuManager.add(new Separator());
            menuManager.add(openTargetAction);

            Menu contextMenu = menuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
            editorPage.getSite().registerContextMenu(menuManager, getSelectionProvider());

            menuManager.addMenuListener(MenuCleaner.createAdditionsCleaner());
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
