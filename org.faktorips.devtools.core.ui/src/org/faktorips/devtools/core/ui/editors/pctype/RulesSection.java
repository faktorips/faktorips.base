/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.EnumSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * Section to display the validation rules.
 */
public class RulesSection extends SimpleIpsPartsSection {

    public RulesSection(IPolicyCmptType pcType, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(pcType, parent, site, ExpandableComposite.TITLE_BAR, Messages.RulesSection_title, toolkit);
    }

    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RulesComposite(getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows a policy component's rules in a viewer and allows to edit rules in a
     * dialog, create new rules and delete rules.
     */
    private class RulesComposite extends IpsPartsComposite {

        RulesComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, getSite(), EnumSet.of(Option.CAN_CREATE,
                    Option.CAN_EDIT, Option.CAN_DELETE, Option.CAN_MOVE,
                    Option.SHOW_EDIT_BUTTON, Option.JUMP_TO_SOURCE_CODE_SUPPORTED), toolkit);

        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getPcType().newRule();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            IValidationRule rule = (IValidationRule)part;
            if (rule.isCheckValueAgainstValueSetRule()) {
                String[] attrNames = rule.getValidatedAttributes();
                if (attrNames.length == 1) {
                    IPolicyCmptTypeAttribute attr = getPcType().getPolicyCmptTypeAttribute(attrNames[0]);
                    if (attr == null) {
                        String msg = NLS.bind(Messages.RulesSection_msgMissingAttribute, attrNames[0]);
                        MessageDialog.openInformation(getShell(), Messages.RulesSection_titleMissingAttribute, msg);
                        rule.delete();
                        return null;
                    }
                    AttributeEditDialog dialog = new AttributeEditDialog(attr, getShell());
                    dialog.showValidationRulePage();
                    dialog.setDataChangeable(isDataChangeable());
                    return dialog;
                }
            }
            return new RuleEditDialog((IValidationRule)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveRules(indexes, up);
        }

        private class ContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return getPcType().getValidationRules().toArray();
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
