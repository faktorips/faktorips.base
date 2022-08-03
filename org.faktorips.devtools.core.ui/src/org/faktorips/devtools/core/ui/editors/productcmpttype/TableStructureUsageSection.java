/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.EnumSet;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class TableStructureUsageSection extends SimpleIpsPartsSection {

    private IProductCmptType productCmptType;

    public TableStructureUsageSection(IProductCmptType productCmptType, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(productCmptType, parent, site, ExpandableComposite.TITLE_BAR, Messages.TableStructureUsageSection_title,
                toolkit);
        this.productCmptType = productCmptType;
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new TblsStructureUsageComposite((IProductCmptType)getIpsObject(), parent, toolkit);
    }

    /**
     * Label provider for the table structure usages. Adds the first related table structure and ...
     * if more than one table structure is related.
     */
    private class TblStructureLabelProvider extends DefaultLabelProvider {

        @Override
        public String getText(Object element) {
            StringBuilder sb = new StringBuilder(super.getText(element));
            if (element instanceof ITableStructureUsage) {
                String[] tableStructures = ((ITableStructureUsage)element).getTableStructures();
                if (tableStructures.length > 0) {
                    sb.append(" : "); //$NON-NLS-1$
                    sb.append(tableStructures[0]);
                    if (tableStructures.length > 1) {
                        sb.append(", ..."); //$NON-NLS-1$
                    }
                }

            }
            return sb.toString();
        }
    }

    /**
     * A composite that shows the used table structures for a product component type in a viewer and
     * allows to edit, create, move and delete.
     */
    private class TblsStructureUsageComposite extends IpsPartsComposite {

        private OpenTableStructuresInEditorAction openAction;

        public TblsStructureUsageComposite(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
            super(productCmptType, parent, getSite(), EnumSet.of(Option.CAN_CREATE,
                    Option.CAN_EDIT, Option.CAN_DELETE, Option.CAN_MOVE,
                    Option.SHOW_EDIT_BUTTON, Option.JUMP_TO_SOURCE_CODE_SUPPORTED), toolkit);
        }

        @Override
        protected void createContextMenuThis(final MenuManager contextMenuManager) {
            openAction = new OpenTableStructuresInEditorAction(getViewer());
            contextMenuManager.add(new Separator());
            contextMenuManager.add(openAction);
            contextMenuManager.addMenuListener($ -> {
                if (!getSelection().isEmpty()) {
                    openAction.updateLabelFromSelection(getViewer().getSelection());
                }
            });
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new TblStructureLabelProvider();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new TblsStructureUsageContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            ArgumentCheck.isInstanceOf(part, ITableStructureUsage.class);
            return new TableStructureUsageEditDialog((ITableStructureUsage)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return productCmptType.moveTableStructureUsage(indexes, up);
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return productCmptType.newTableStructureUsage();
        }

        @Override
        protected void openLink() {
            openAction.run();
        }

        private class TblsStructureUsageContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return productCmptType.getTableStructureUsages().toArray();
            }

            @Override
            public void dispose() {
                // nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do
            }
        }

    }

    /**
     * Action to open the selected target in a new editor window
     */
    private class OpenTableStructuresInEditorAction extends IpsAction {
        String textSingular = Messages.TableStructureUsageSection_menuOpenTargetInNewEditorSingular;
        String textPlural = Messages.TableStructureUsageSection_menuOpenTargetInNewEditorPlural;

        public OpenTableStructuresInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
            setText(textSingular);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof ITableStructureUsage);
        }

        @Override
        public void run(IStructuredSelection selection) {
            ITableStructureUsage tableStructureUsage = getTableStructureFromSelection(selection);
            if (tableStructureUsage == null) {
                return;
            }
            try {
                String[] tableStructures = tableStructureUsage.getTableStructures();
                for (String tblStruct : tableStructures) {
                    IpsUIPlugin.getDefault().openEditor(
                            getProductCmptType().getIpsProject()
                                    .findIpsObject(IpsObjectType.TABLE_STRUCTURE, tblStruct));
                }
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

        private ITableStructureUsage getTableStructureFromSelection(ISelection selection) {
            if (!(selection instanceof IStructuredSelection)) {
                return null;
            }
            Object selected = ((IStructuredSelection)selection).getFirstElement();
            if (selected instanceof ITableStructureUsage) {
                return (ITableStructureUsage)selected;
            }
            return null;
        }

        public void updateLabelFromSelection(ISelection selection) {
            ITableStructureUsage tableStructureUsage = getTableStructureFromSelection(selection);
            if (tableStructureUsage.getTableStructures().length > 1) {
                setText(textPlural);
            } else {
                setText(textSingular);
            }
        }
    }

}
