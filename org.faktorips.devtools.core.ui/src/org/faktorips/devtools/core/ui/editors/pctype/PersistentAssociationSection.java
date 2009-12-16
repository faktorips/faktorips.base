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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

public class PersistentAssociationSection extends SimpleIpsPartsSection {

    public PersistentAssociationSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, "Associations", toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PersistenceAssociationsComposite(getIpsObject(), parent, toolkit);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    public class PersistenceAssociationsComposite extends PersistenceComposite {

        // private final String[] columnHeaders = { "Join Table Name", "Source Column Name",
        // "Target Column Name",
        // "Fetch Type" };

        public PersistenceAssociationsComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, toolkit);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new PersistentAttributeContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new PersistentAssociationEditDialog((IPolicyCmptTypeAttribute)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return ((IPolicyCmptType)getIpsObject()).newPolicyCmptTypeAttribute();
        }

        @Override
        public String[] getColumnHeaders() {
            // return columnHeaders;
            return new String[] { "Join Table Name", "Source Column Name", "Target Column Name", "Fetch Type" };
        }

        private class PersistentAttributeContentProvider implements IStructuredContentProvider {
            public Object[] getElements(Object inputElement) {
                return ((IPolicyCmptType)getIpsObject()).getPolicyCmptTypeAttributes();
            }

            public void dispose() {
                // nothing todo
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }

        private class PersistentAssociationLabelProvider extends LabelProvider implements ITableLabelProvider {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)element;
                IPersistentAttributeInfo jpaAttributeInfo = attribute.getPersistenceAttributeInfo();

                String result = "";
                switch (columnIndex) {
                    case 0:
                        result = jpaAttributeInfo.getTableColumnName();
                        break;

                    default:
                        result = "";
                }
                return (result == null ? "" : result);
            }
        }

        @Override
        public ILabelProvider createLabelProvider() {
            return new PersistentAssociationLabelProvider();
        }

    }

    public class PersistentAssociationEditDialog extends EditDialog {

        public PersistentAssociationEditDialog(IPolicyCmptTypeAttribute part, Shell shell) {
            super(shell, "Edit Association", true);
        }

        @Override
        protected Composite createWorkArea(Composite parent) throws CoreException {
            TabFolder folder = (TabFolder)parent;

            TabItem page = new TabItem(folder, SWT.NONE);
            page.setText("Persistence");
            Label control = new Label(parent, SWT.NONE);
            control.setText("Sample Content");
            page.setControl(control);

            return folder;
        }
    }

}
