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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRenameHandler;

/**
 * A section to display and edit a type's attributes.
 */
public abstract class AttributesSection extends SimpleIpsPartsSection {

    private final IpsObjectEditorPage editorPage;

    protected AttributesSection(IpsObjectEditorPage editorPage, IType type, Composite parent, UIToolkit toolkit) {
        super(type, parent, ExpandableComposite.TITLE_BAR, Messages.AttributesSection_title, toolkit);
        this.editorPage = editorPage;
        getAttributesComposite().createContextMenu();
    }

    protected abstract AttributesComposite getAttributesComposite();

    protected IType getType() {
        return (IType)getIpsObject();
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    protected abstract class AttributesComposite extends IpsPartsComposite {

        protected AttributesComposite(IType type, Composite parent, UIToolkit toolkit) {
            super(type, parent, toolkit);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return getType().newAttribute();
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveAttributes(indexes, up);
        }

        private void createContextMenu() {
            MenuManager refactorSubmenu = new MenuManager(Messages.AttributesSection_submenuRefactor);
            refactorSubmenu.add(IpsRefactoringHandler.getContributionItem(IpsRenameHandler.CONTRIBUTION_ID));

            MenuManager menuManager = new MenuManager();
            menuManager.add(refactorSubmenu);
            menuManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));

            Menu contextMenu = menuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
            editorPage.getSite().registerContextMenu(menuManager, getSelectionProvider());

            menuManager.addMenuListener(MenuCleaner.createAdditionsCleaner());
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider();
        }

        private class AttributeContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return getType().getAttributes().toArray();
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
