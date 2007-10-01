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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
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

    public TableStructureUsageSection(
            IProductCmptType productCmptType,
            Composite parent,
            UIToolkit toolkit) {
        super(productCmptType, parent, "Table Structure Usages", toolkit);
        this.productCmptType = productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        tblsStructureUsageComposite = new TblsStructureUsageComposite((IProductCmptType)getIpsObject(), parent, toolkit);
        return tblsStructureUsageComposite;
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

            public Object[] getElements(Object inputElement) {
                return productCmptType.getTableStructureUsages();
            }
            
            public void dispose() {
                // nothing todo
            }
            
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }
        
        public TblsStructureUsageComposite(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
            super(productCmptType, parent, toolkit);
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
            return productCmptType.moveTableStructureUsage(indexes, up);
        }
        
        /**
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            ITableStructureUsage tsu = productCmptType.newTableStructureUsage();
            return tsu;
        }
    }
    
}
