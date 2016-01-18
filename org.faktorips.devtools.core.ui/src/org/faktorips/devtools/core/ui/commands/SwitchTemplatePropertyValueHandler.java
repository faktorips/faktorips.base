/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.producttemplate.DefinedValuesContentProvider.ValueViewItem;
import org.faktorips.devtools.core.ui.views.producttemplate.SwitchTemplatePropertyValueOperation;

public class SwitchTemplatePropertyValueHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Collection<IPropertyValue> elements = getElements(currentSelection);
        if (elements != null) {
            switchTemplateValue(elements);
        } else {
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                    Messages.SwitchTemplatePropertyValueHandler_warning_title,
                    Messages.SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentElements);
        }
        return null;
    }

    protected Collection<IPropertyValue> getElements(ISelection currentSelection) {
        TypedSelection<IPropertyValue> propValueSelection = TypedSelection.createAnyCount(IPropertyValue.class,
                currentSelection);
        if (propValueSelection.isValid()) {
            return propValueSelection.getElements();
        } else {
            TypedSelection<ValueViewItem> viewItemSelection = TypedSelection.create(ValueViewItem.class,
                    currentSelection);
            if (viewItemSelection.isValid()) {
                ValueViewItem element = viewItemSelection.getElement();
                return element.getChildren();
            }
        }
        return null;
    }

    protected void switchTemplateValue(Collection<IPropertyValue> elements) {
        SwitchTemplatePropertyValueOperation switchPropertyValueOperation = SwitchTemplatePropertyValueOperation
                .create(elements);
        if (switchPropertyValueOperation != null) {
            IpsUIPlugin.getDefault().runWorkspaceModification(switchPropertyValueOperation);
        } else {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                    Messages.SwitchTemplatePropertyValueHandler_warning_title,
                    Messages.SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentValue);
        }
    }

}
