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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class TableStructureUsageSection extends SimpleIpsPartsSection {

    private IProductCmptType productCmptType;

    private TblsStructureUsageComposite tblsStructureUsageComposite;

    public TableStructureUsageSection(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
        super(productCmptType, parent, null, ExpandableComposite.TITLE_BAR, Messages.TableStructureUsageSection_title,
                toolkit);
        this.productCmptType = productCmptType;
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

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        tblsStructureUsageComposite = new TblsStructureUsageComposite((IProductCmptType)getIpsObject(), parent, toolkit);
        tblsStructureUsageComposite.initContextMenu();
        return tblsStructureUsageComposite;
    }

    /**
     * Label provider for the table structure usages. Adds the first related table structure and ...
     * if more than one table structure is related.
     */
    private class TblStructureLabelProvider extends DefaultLabelProvider {

        @Override
        public String getText(Object element) {
            StringBuffer sb = new StringBuffer(super.getText(element));
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

        public void initContextMenu() {
            openAction = new OpenTableStructuresInEditorAction(getViewer());
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
                    openAction.updateLabelFromSelection(selection);
                    menuManager.add(openAction);
                }
            });

            Menu menu = menuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(menu);
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

        public TblsStructureUsageComposite(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
            super(productCmptType, parent, getSite(), toolkit);
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
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            ArgumentCheck.isInstanceOf(part, ITableStructureUsage.class);
            return new TblsStructureUsageEditDialog((ITableStructureUsage)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return productCmptType.moveTableStructureUsage(indexes, up);
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
            return tsu;
        }

        @Override
        protected void openLink() {
            openAction.run();
        }

    }

}
