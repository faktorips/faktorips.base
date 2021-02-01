/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.EnumSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;

/**
 * A section to display and edit a type's methods.
 */
public class MethodsSection extends SimpleIpsPartsSection {

    public MethodsSection(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(type, parent, site, ExpandableComposite.TITLE_BAR, Messages.MethodsSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new MethodsComposite(getType(), parent, getSite(), toolkit);
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
    }

    protected IType getType() {
        return (IType)getIpsObject();
    }

    /**
     * A composite that shows a type's methods in a viewer and allows to edit methods in a dialog,
     * create new methods and delete methods.
     */
    protected static class MethodsComposite extends IpsPartsComposite {

        protected MethodsComposite(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
            super(type, parent, site, EnumSet
                    .of(Option.CAN_CREATE, Option.CAN_DELETE, Option.CAN_EDIT, Option.CAN_MOVE, Option.CAN_OVERRIDE,
                            Option.JUMP_TO_SOURCE_CODE_SUPPORTED, Option.SHOW_EDIT_BUTTON),
                    toolkit);

        }

        @Override
        public void overrideClicked() {
            OverrideMethodDialog dialog = new OverrideMethodDialog(getType(), getShell());
            if (dialog.open() == Window.OK) {
                getType().overrideMethods(dialog.getSelectedParts());
            }
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getType().newMethod();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new MethodEditDialog((IMethod)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveMethods(indexes, up);
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new MethodContentProvider(getType());
        }

        private static class MethodContentProvider implements IStructuredContentProvider {

            private final IType type;

            public MethodContentProvider(IType type) {
                this.type = type;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return type.getMethods().toArray();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        }

    }
}
