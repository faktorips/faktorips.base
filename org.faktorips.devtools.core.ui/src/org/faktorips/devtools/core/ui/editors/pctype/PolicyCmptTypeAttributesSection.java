/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AttributesSection;

public class PolicyCmptTypeAttributesSection extends AttributesSection {

    private PolicyCmptTypeAttributesComposite attributesComposite;

    public PolicyCmptTypeAttributesSection(IPolicyCmptType policyCmptType, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(policyCmptType, parent, site, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        attributesComposite = new PolicyCmptTypeAttributesComposite(getPolicyCmptType(), parent, toolkit);
        return attributesComposite;
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
        attributesComposite.updateOverrideButtonEnabledState();
    }

    private IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private class PolicyCmptTypeAttributesComposite extends AttributesComposite {

        private Button overrideButton;

        public PolicyCmptTypeAttributesComposite(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
            super(policyCmptType, parent, toolkit);
            addDeleteListener();
        }

        private void addDeleteListener() {
            super.addDeleteListener(new IDeleteListener() {
                @Override
                public boolean aboutToDelete(IIpsObjectPart part) {
                    IValidationRule rule = findValidationRule(part);
                    if (rule == null) {
                        // Nothing to do if no special rule is defined.
                        return true;
                    }
                    String msg = Messages.AttributesSection_deleteMessage;
                    boolean delete = MessageDialog
                            .openQuestion(getShell(), Messages.AttributesSection_deleteTitle, msg);
                    if (delete) {
                        rule.delete();
                    } else if (!delete) {
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
            });
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            overrideButton.setEnabled(flag);
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            overrideButton = toolkit.createButton(buttons, Messages.AttributesSection_OverrideButton);
            overrideButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            overrideButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    overrideClicked();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Nothing to do
                }
            });
            updateOverrideButtonEnabledState();

            return true;
        }

        private void updateOverrideButtonEnabledState() {
            try {
                boolean supertypeExisting = getPolicyCmptType().hasExistingSupertype(
                        getPolicyCmptType().getIpsProject());
                overrideButton.setEnabled(supertypeExisting);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        private void overrideClicked() {
            OverrideAttributeDialog dialog = new OverrideAttributeDialog(getPolicyCmptType(), getShell());
            if (dialog.open() == Window.OK) {
                getPolicyCmptType().overrideAttributes(dialog.getSelectedParts());
                refresh();
            }
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);
        }

    }

}
