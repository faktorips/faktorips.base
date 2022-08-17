/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public class ColumnsSection extends SimpleIpsPartsSection {

    public ColumnsSection(ITableStructure table, Composite parent, UIToolkit toolkit) {
        super(table, parent, null, ExpandableComposite.TITLE_BAR, Messages.ColumnsSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ColumnsComposite(getIpsObject(), parent, toolkit);
    }

    private class ColumnsComposite extends IpsPartsComposite {

        public ColumnsComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, getSite(), toolkit);
        }

        public ITableStructure getTable() {
            return (ITableStructure)getIpsObject();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new ColumnLabelProvider();
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getTable().newColumn();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new ColumnEditDialog((IColumn)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getTable().moveColumns(indexes, up);
        }

        private class ContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                return getTable().getColumns();
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
    }

    private class ColumnLabelProvider extends DefaultLabelProvider {
        @Override
        public String getText(Object element) {
            String text = super.getText(element);
            IColumn column = (IColumn)element;
            return text + " : " + column.getDatatype(); //$NON-NLS-1$
        }
    }
}
