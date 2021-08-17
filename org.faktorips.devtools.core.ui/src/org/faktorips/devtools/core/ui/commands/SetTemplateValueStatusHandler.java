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
import org.faktorips.devtools.core.ui.views.producttemplate.SetTemplateValueStatusOperation;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

/** A handler to change the template value status of property values. */
public class SetTemplateValueStatusHandler extends AbstractHandler {

    private final TemplateValueStatus status;
    private boolean enabled;

    public SetTemplateValueStatusHandler(TemplateValueStatus status) {
        super();
        this.status = status;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            IEvaluationContext context = (IEvaluationContext)evaluationContext;
            Object variable = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
            if (variable instanceof ISelection) {
                ISelection selection = (ISelection)variable;
                Collection<ITemplatedValue> selectedPropertyValues = DefinedValuesContentProvider
                        .getSelectedTemplatedValues(selection);
                enabled = SetTemplateValueStatusOperation.isValid(selectedPropertyValues);
            } else {
                enabled = false;
            }
        } else {
            enabled = false;
        }
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Collection<ITemplatedValue> elements = DefinedValuesContentProvider
                .getSelectedTemplatedValues(currentSelection);
        if (SetTemplateValueStatusOperation.isValid(elements)) {
            setTemplateValueStatus(elements);
        } else {
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                    Messages.SetTemplateValueStatus_warning_title,
                    Messages.SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentElements);
        }
        return null;
    }

    private void setTemplateValueStatus(Collection<ITemplatedValue> elements) {
        IpsUIPlugin.getDefault().runWorkspaceModification(new SetTemplateValueStatusOperation(elements, status));
    }

}
