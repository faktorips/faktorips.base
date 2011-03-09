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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRenameHandler;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributesSection extends SimpleIpsPartsSection {

    public AttributesSection(IpsObjectEditorPage page, IProductCmptType type, Composite parent, UIToolkit toolkit) {
        super(type, parent, Messages.AttributesSection_title, toolkit);
        ArgumentCheck.notNull(page);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new AttributesComposite((IProductCmptType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    private class AttributesComposite extends IpsPartsComposite {

        public AttributesComposite(IProductCmptType type, Composite parent, UIToolkit toolkit) {
            super(type, parent, toolkit);
            createContextMenu();
        }

        private void createContextMenu() {
            MenuManager manager = new MenuManager();
            MenuManager refactorSubmenu = new MenuManager(Messages.AttributesSection_submenuRefactor);

            refactorSubmenu.add(IpsRefactoringHandler.getContributionItem(IpsRenameHandler.CONTRIBUTION_ID));

            manager.add(refactorSubmenu);
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

            @Override
            public Object[] getElements(Object inputElement) {
                return getProductCmptType().getProductCmptTypeAttributes().toArray();
            }

            @Override
            public void dispose() {
                // Nothing to do.
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do.
            }

        }

    }

}
