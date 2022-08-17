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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;

/**
 * A section to display and edit a type's attributes.
 */
public abstract class AttributesSection extends SimpleIpsPartsSection {

    protected AttributesSection(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(type, parent, site, ExpandableComposite.TITLE_BAR, Messages.AttributesSection_title, toolkit);
    }

    protected IType getType() {
        return (IType)getIpsObject();
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    protected abstract static class AttributesComposite extends IpsPartsComposite {

        private IpsAction openEnumTypeAction;

        protected AttributesComposite(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
            super(type, parent, site,
                    EnumSet.of(Option.CAN_CREATE, Option.CAN_DELETE, Option.CAN_EDIT, Option.CAN_MOVE,
                            Option.CAN_OVERRIDE, Option.JUMP_TO_SOURCE_CODE_SUPPORTED,
                            Option.PULL_UP_REFACTORING_SUPPORTED, Option.RENAME_REFACTORING_SUPPORTED,
                            Option.SHOW_EDIT_BUTTON),
                    toolkit);
            openEnumTypeAction = new OpenEnumerationTypeInNewEditor(getViewer());
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getType().newAttribute();
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveAttributes(indexes, up);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider(getType());
        }

        @Override
        protected void createContextMenuThis(MenuManager contextMenuManager) {
            contextMenuManager.add(new Separator());
            contextMenuManager.add(openEnumTypeAction);
        }

        @Override
        protected void openLink() {
            openEnumTypeAction.run();
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
        }

        @Override
        public void overrideClicked() {
            OverrideAttributeDialog dialog = new OverrideAttributeDialog(getType(), getShell());
            if (dialog.open() == Window.OK) {
                getType().overrideAttributes(dialog.getSelectedParts());
                refresh();
            }
        }

        private static class AttributeContentProvider implements IStructuredContentProvider {

            private final IType type;

            public AttributeContentProvider(IType type) {
                this.type = type;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return type.getAttributes().toArray();
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

        private static class OpenEnumerationTypeInNewEditor extends IpsAction {

            public OpenEnumerationTypeInNewEditor(ISelectionProvider selectionProvider) {
                super(selectionProvider);
                setText(Messages.AttributesSection_openEnumContentInNewEditor);
            }

            @Override
            protected boolean computeEnabledProperty(IStructuredSelection selection) {
                Object selected = selection.getFirstElement();
                if (!(selected instanceof IAttribute)) {
                    return false;
                }

                IAttribute attribute = (IAttribute)selected;
                Datatype datatype = attribute.findDatatype(attribute.getIpsProject());
                return datatype instanceof EnumTypeDatatypeAdapter;
            }

            @Override
            public void run(IStructuredSelection selection) {
                Object selected = selection.getFirstElement();
                if (!(selected instanceof IAttribute)) {
                    return;
                }

                IAttribute attribute = (IAttribute)selected;
                Datatype datatype = attribute.findDatatype(attribute.getIpsProject());
                if (datatype instanceof EnumTypeDatatypeAdapter) {
                    EnumTypeDatatypeAdapter enumDatatype = (EnumTypeDatatypeAdapter)datatype;
                    IpsUIPlugin.getDefault().openEditor(enumDatatype.getEnumValueContainer());
                }
            }

        }

    }
}
