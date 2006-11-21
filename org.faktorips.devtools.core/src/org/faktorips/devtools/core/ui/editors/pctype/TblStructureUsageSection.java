/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * A section to display and edit a product component type's used table structures.
 */
public class TblStructureUsageSection extends SimpleIpsPartsSection {

    private IProductCmptType productCmptType;

    private TblsStructureUsageComposite tblsStructureUsageComposite;

    private TblStructureUsageContentChangeListener changeListener;
    
    private class TblStructureUsageContentChangeListener extends ContentsChangeListenerForWidget{
        public TblStructureUsageContentChangeListener(Widget widget) {
            super(widget);
        }
        public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
            contentsHasChanged(event);
        }
    }
    /**
     * Label provider for the table structure usages. Adds the first related table structure and ... if 
     * more than one table structure is related.
     */
    private class TblStructureLabelProvider extends DefaultLabelProvider{
        /**
         * {@inheritDoc}
         */
        public String getText(Object element) {
            StringBuffer sb = new StringBuffer(super.getText(element));
            if (element instanceof ITableStructureUsage){
                String[] tableStructures = ((ITableStructureUsage)element).getTableStructures();
                if (tableStructures.length > 0){
                    sb.append(" : "); //$NON-NLS-1$
                    sb.append(tableStructures[0]);
                    if (tableStructures.length > 1){
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
        private class TblsStructureUsageContentProvider implements IStructuredContentProvider {
            private Object[] EMPTY_ARRAY = new Object[0];
            public Object[] getElements(Object inputElement) {
                if (productCmptType == null) {
                    return EMPTY_ARRAY;
                }
                return productCmptType.getTableStructureUsages();
            }
            public void dispose() {
                // nothing todo
            }
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }
        
        public TblsStructureUsageComposite(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
            super(policyCmptType, parent, toolkit);
        }
        
        private IPolicyCmptType getPcType() {
            return (IPolicyCmptType)getIpsObject();
        }
        
        /**
         * {@inheritDoc}
         */
        protected ILabelProvider createLabelProvider() {
            return new TblStructureLabelProvider();
        }

        /**
         * {@inheritDoc}
         */
        protected IStructuredContentProvider createContentProvider() {
            return new TblsStructureUsageContentProvider();
        }

        /**
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            ArgumentCheck.isInstanceOf(part, ITableStructureUsage.class);
            return new TblsStructureUsageEditDialog((ITableStructureUsage)part, shell);
        }

        /**
         * {@inheritDoc}
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveTableStructureUsage(indexes, up);
        }
        
        /**
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
            return tsu;
        }

        /**
         * {@inheritDoc}
         */
        protected void updateButtonEnabledStates() {
            super.updateButtonEnabledStates();
            if (productCmptType == null){
                newButton.setEnabled(false);
                editButton.setEnabled(false);
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            }
        }
    }
    
    public TblStructureUsageSection(
            IPolicyCmptType pcType,
            IProductCmptType productCmptType,
            Composite parent,
            UIToolkit toolkit) {
        super(pcType, parent, Messages.TblStructureUsageSection_Title, toolkit);
        this.productCmptType = productCmptType;
        changeListener = new TblStructureUsageContentChangeListener(this);
        pcType.getIpsModel().addChangeListener(changeListener);
    }

    private void contentsHasChanged(ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(getIpsObject().getIpsSrcFile())){
            return;
        }
        try {
            IProductCmptType productCmptTypeNew = ((IPolicyCmptType)getIpsObject()).findProductCmptType();
            // check if configured by product cmpt has changed, e.g. product cmpt was null before but now not null
            if ((productCmptType != null && productCmptTypeNew == null)
                    || (productCmptType == null && productCmptTypeNew != null)) {
                productCmptType = productCmptTypeNew;
                tblsStructureUsageComposite.refresh();
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        tblsStructureUsageComposite = new TblsStructureUsageComposite(
                (IPolicyCmptType)getIpsObject(), parent, toolkit);
        return tblsStructureUsageComposite;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        productCmptType.getIpsModel().removeChangeListener(changeListener);
        super.dispose();
    }
}
