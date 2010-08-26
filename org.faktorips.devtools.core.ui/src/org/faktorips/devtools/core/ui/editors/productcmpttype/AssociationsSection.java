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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.AssociationsLabelProvider;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section that shows a product component type's associations in a viewer and allows to edit
 * association in a dialog, create new associations and delete associations.
 * 
 * @author Jan Ortmann
 */
public class AssociationsSection extends SimpleIpsPartsSection {

    public AssociationsSection(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
        super(pdObject, parent, Messages.AssociationsSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RelationsComposite(getIpsObject(), parent, toolkit);
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    /**
     * Action to open the selected target in a new editor window
     */
    private class OpenTargetProductCmptTypeInEditorAction extends IpsAction {

        public OpenTargetProductCmptTypeInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
            setText(Messages.AssociationsSection_menuOpenTargetInNewEditor);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IProductCmptTypeAssociation);
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IProductCmptTypeAssociation) {
                IProductCmptTypeAssociation productCmptTypeAssociation = (IProductCmptTypeAssociation)selected;
                try {
                    IType target = productCmptTypeAssociation.findTarget(getProductCmptType().getIpsProject());
                    IpsUIPlugin.getDefault().openEditor(target);
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
    }

    private class RelationsComposite extends IpsPartsComposite {

        protected OpenTargetProductCmptTypeInEditorAction openAction;

        RelationsComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, true, true, true, true, true, toolkit);
            openAction = new OpenTargetProductCmptTypeInEditorAction(getViewer());
            buildContextMenu();
        }

        private void buildContextMenu() {
            final MenuManager menuManager = new MenuManager();
            menuManager.setRemoveAllWhenShown(true);
            // display menu only if one element is selected
            menuManager.addMenuListener(new IMenuListener() {
                @Override
                public void menuAboutToShow(IMenuManager manager) {
                    ISelection selection = getViewer().getSelection();
                    if (selection.isEmpty()) {
                        return;
                    }
                    menuManager.add(openAction);
                }
            });

            Menu menu = menuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(menu);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                @Override
                public Object[] getElements(Object inputElement) {
                    return getProductCmptType().getAssociations();
                }

                @Override
                public void dispose() {
                    // Nothing to do
                }

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // Nothing to do
                }

            };
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new AssociationsLabelProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new AssociationEditDialog((IProductCmptTypeAssociation)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getProductCmptType().newProductCmptTypeAssociation();
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getProductCmptType().moveAssociations(indexes, up);
        }

        @Override
        protected void openLink() {
            openAction.run();
        }

    }

}
