/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AttributesSection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;

public class PolicyCmptTypeAttributesSection extends AttributesSection {

    private PolicyCmptTypeAttributesComposite attributesComposite;

    public PolicyCmptTypeAttributesSection(IPolicyCmptType policyCmptType, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(policyCmptType, parent, site, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        attributesComposite = new PolicyCmptTypeAttributesComposite(getPolicyCmptType(), parent, getSite(), toolkit);
        return attributesComposite;
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
    }

    private IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private class PolicyCmptTypeAttributesComposite extends AttributesComposite {

        public PolicyCmptTypeAttributesComposite(IPolicyCmptType policyCmptType, Composite parent,
                IWorkbenchPartSite site, UIToolkit toolkit) {
            super(policyCmptType, parent, site, toolkit);
            addDeleteListener();
        }

        private void addDeleteListener() {
            super.addDeleteListener(new DeleteListenerImplementation());
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);
        }

        private final class DeleteListenerImplementation implements IDeleteListener {
            @Override
            public boolean aboutToDelete(IIpsObjectPart part) {
                IValidationRule rule = findValidationRule(part);
                if (rule == null) {
                    // Nothing to do if no special rule is defined.
                    return true;
                }
                String msg = Messages.AttributesSection_deleteMessage;
                boolean delete = MessageDialog.openQuestion(getShell(), Messages.AttributesSection_deleteTitle, msg);
                if (delete) {
                    rule.delete();
                } else {
                    rule.setCheckValueAgainstValueSetRule(false);
                }
                return true;
            }

            private IValidationRule findValidationRule(IIpsObjectPart part) {
                String name = part.getName();
                List<IValidationRule> rules = getPolicyCmptType().getValidationRules();
                for (IValidationRule rule : rules) {
                    if (!rule.isCheckValueAgainstValueSetRule()) {
                        continue;
                    }
                    String[] attributes = rule.getValidatedAttributes();
                    if (attributes.length == 1 && attributes[0].equals(name)) {
                        return rule;
                    }
                }
                return null;
            }

            @Override
            public void deleted(IIpsObjectPart part) {
                // Nothing to do.
            }
        }
    }

}
