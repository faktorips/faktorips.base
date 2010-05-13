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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.RenameAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributesSection extends SimpleIpsPartsSection {

    private IpsObjectEditorPage page;

    public AttributesSection(IpsObjectEditorPage page, IProductCmptType type, Composite parent, UIToolkit toolkit) {
        super(type, parent, Messages.AttributesSection_title, toolkit);
        ArgumentCheck.notNull(page);
        this.page = page;
        ((AttributesComposite)getPartsComposite()).createContextMenu();
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new AttributesComposite((IProductCmptType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    public class AttributesComposite extends IpsPartsComposite {

        public AttributesComposite(IProductCmptType type, Composite parent, UIToolkit toolkit) {
            super(type, parent, toolkit);
        }

        private void createContextMenu() {
            // TODO AW: Duplicate code in attributes section of pctype
            final IEditorSite editorSite = (IEditorSite)page.getEditor().getSite();
            final IWorkbenchAction renameAction = ActionFactory.RENAME.create(editorSite.getWorkbenchWindow());
            final IActionBars actionBars = editorSite.getActionBars();

            /*
             * The action handler is the implementation of the global rename action. It will be set
             * every time when the section needs to be painted hence when it becomes visible to the
             * user. This is necessary because other editor instances are overwriting the setting
             * with their own rename action implementation because they want to pass to it their own
             * selection provider.
             */
            addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent e) {
                    RenameAction ipsRenameActionHandler = new RenameAction(editorSite.getShell(), getPartsComposite());
                    actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), ipsRenameActionHandler);
                    actionBars.updateActionBars();
                }
            });

            MenuManager manager = new MenuManager();
            manager.setRemoveAllWhenShown(true);
            manager.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    manager.add(new Separator());
                    MenuManager refactorSubmenu = new MenuManager(Messages.AttributesSection_submenuRefactor);
                    refactorSubmenu.add(renameAction);
                    manager.add(refactorSubmenu);
                }
            });

            Menu contextMenu = manager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
        }

        public IProductCmptType getProductCmptType() {
            return (IProductCmptType)getIpsObject();
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new DefaultLabelProvider();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider();
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getProductCmptType().newProductCmptTypeAttribute();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IProductCmptTypeAttribute)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getProductCmptType().moveAttributes(indexes, up);
        }

        private class AttributeContentProvider implements IStructuredContentProvider {

            public Object[] getElements(Object inputElement) {
                return getProductCmptType().getProductCmptTypeAttributes();
            }

            public void dispose() {
                // Nothing to do.
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do.
            }

        }

    }

}
