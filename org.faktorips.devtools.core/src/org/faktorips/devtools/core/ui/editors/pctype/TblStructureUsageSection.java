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

    /**
     * A composite that shows the used table structures for a product component type in a viewer and 
     * allows to edit, create, move and delete.
     */
    private class TblsStructureUsageComposite extends IpsPartsComposite{
        private final Object[] EMPTY_ARRAY = new Object[0];
        
        private class TblsStructureUsageContentProvider implements IStructuredContentProvider {
            public Object[] getElements(Object inputElement) {
                if (productCmptType == null){
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
            return new DefaultLabelProvider();
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
    }
    
    public TblStructureUsageSection(
            IPolicyCmptType pcType,
            IProductCmptType productCmptType,
            Composite parent,
            UIToolkit toolkit) {
        super(pcType, parent, Messages.TblStructureUsageSection_Title, toolkit);
        this.productCmptType = productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new TblsStructureUsageComposite((IPolicyCmptType)getIpsObject(), parent, toolkit);
    }
}
