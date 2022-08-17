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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.tablestructure.IForeignKey;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public class ForeignKeysSection extends SimpleIpsPartsSection {

    public ForeignKeysSection(ITableStructure table, Composite parent, UIToolkit toolkit) {
        super(table, parent, null, ExpandableComposite.TITLE_BAR, Messages.ForeignKeysSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ForeignKeysComposite(getIpsObject(), parent, toolkit);
    }

    private class ForeignKeysComposite extends IpsPartsComposite {

        public ForeignKeysComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, getSite(), toolkit);
        }

        public ITableStructure getTableStructure() {
            return (ITableStructure)getIpsObject();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getTableStructure().newForeignKey();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new ForeignKeyEditDialog((IForeignKey)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getTableStructure().moveForeignKeys(indexes, up);
        }

        private class ContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                return getTableStructure().getForeignKeys();
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

}
