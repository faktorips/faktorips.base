/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section that shows a product component type's associations in a viewer and 
 * allows to edit association in a dialog, create new associations and delete associations.
 * 
 * @author Jan Ortmann
 */
public class AssociationsSection extends SimpleIpsPartsSection {

    public AssociationsSection(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
        super(pdObject, parent, Messages.AssociationsSection_title, toolkit);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RelationsComposite(getIpsObject(), parent, toolkit);
    }
    
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    /*
     * Action to open the selected target in a new editor window
     */
    private class OpenTargetProductCmptTypeInEditorAction extends IpsAction {
        public OpenTargetProductCmptTypeInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
            setText(Messages.AssociationsSection_menuOpenTargetInNewEditor);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IProductCmptTypeAssociation);
        }

        /** 
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IProductCmptTypeAssociation) {
                IProductCmptTypeAssociation productCmptTypeAssociation = (IProductCmptTypeAssociation)selected;
                try {
                    IType target = productCmptTypeAssociation.findTarget(getProductCmptType().getIpsProject());
                    IpsPlugin.getDefault().openEditor(target);
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
    }
    
    private class RelationsComposite extends IpsPartsComposite {

        RelationsComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, true, true, true, true, true, toolkit);
            buildContextMenu();
        }

        private void buildContextMenu() {
            final MenuManager menuManager = new MenuManager();
            menuManager.setRemoveAllWhenShown(true);
            // display menu only if one element is selected
            menuManager.addMenuListener(new IMenuListener(){
                public void menuAboutToShow(IMenuManager manager) {
                    ISelection selection = getViewer().getSelection();
                    if (selection.isEmpty()){
                        return;
                    }
                    OpenTargetProductCmptTypeInEditorAction openAction = new OpenTargetProductCmptTypeInEditorAction(getViewer());
                    menuManager.add(openAction);
                }
            });
            
            Menu menu = menuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(menu);
        }

        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                public Object[] getElements(Object inputElement) {
                    return getProductCmptType().getAssociations();
                }

                public void dispose() {
                    
                }

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                }
                
            };
        }

        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new AssociationEditDialog((IProductCmptTypeAssociation)part, shell);
        }

        protected IIpsObjectPart newIpsPart() {
            return getProductCmptType().newProductCmptTypeAssociation();
        }
        
    }

}
