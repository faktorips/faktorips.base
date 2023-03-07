/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.producttemplate.DefinedValuesContentProvider;
import org.faktorips.devtools.core.ui.views.producttemplate.SwitchTemplatedValueOperation;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

public class SwitchTemplatePropertyValueHandler extends AbstractHandler {

    private boolean enabled;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Collection<ITemplatedValue> elements = DefinedValuesContentProvider
                .getSelectedTemplatedValues(currentSelection);
        if (elements.isEmpty()) {
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                    Messages.SwitchTemplatePropertyValueHandler_warning_title,
                    Messages.SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentElements);
        } else {
            switchTemplateValue(elements);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext context) {
            Object variable = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
            if (variable instanceof ISelection selection) {
                Collection<ITemplatedValue> selectedValues = DefinedValuesContentProvider
                        .getSelectedTemplatedValues(selection);
                enabled = SwitchTemplatedValueOperation.isValidSelection(selectedValues);
            } else {
                enabled = false;
            }
        } else {
            enabled = false;
        }
    }

    private void switchTemplateValue(Collection<ITemplatedValue> elements) {
        SwitchTemplatedValueOperation switchPropertyValueOperation = SwitchTemplatedValueOperation.create(elements);
        if (switchPropertyValueOperation != null) {
            IpsUIPlugin.getDefault().runWorkspaceModification(switchPropertyValueOperation);
        } else {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                    Messages.SwitchTemplatePropertyValueHandler_warning_title,
                    Messages.SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentValue);
        }
    }

}
