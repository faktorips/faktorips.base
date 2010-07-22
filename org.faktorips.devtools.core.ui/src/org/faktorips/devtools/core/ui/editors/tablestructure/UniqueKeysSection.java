/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

public class UniqueKeysSection extends SimpleIpsPartsSection {

    public UniqueKeysSection(ITableStructure table, Composite parent, UIToolkit toolkit) {
        super(table, parent, Messages.UniqueKeysSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new UniqueKeysComposite(getIpsObject(), parent, toolkit);
    }

    private class UniqueKeysComposite extends IpsPartsComposite {

        public UniqueKeysComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
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
            return getTableStructure().newUniqueKey();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new KeyEditDialog((IUniqueKey)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getTableStructure().moveUniqueKeys(indexes, up);
        }

        private class ContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                return getTableStructure().getUniqueKeys();
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
