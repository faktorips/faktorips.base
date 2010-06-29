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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
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

    public TableStructureUsageSection(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
        super(productCmptType, parent, Messages.TableStructureUsageSection_title, toolkit);
        this.productCmptType = productCmptType;
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        tblsStructureUsageComposite = new TblsStructureUsageComposite((IProductCmptType)getIpsObject(), parent, toolkit);
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

        private class TblsStructureUsageContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return productCmptType.getTableStructureUsages();
            }

            @Override
            public void dispose() {
                // nothing todo
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }

        public TblsStructureUsageComposite(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
            super(productCmptType, parent, toolkit);
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
    }

}
