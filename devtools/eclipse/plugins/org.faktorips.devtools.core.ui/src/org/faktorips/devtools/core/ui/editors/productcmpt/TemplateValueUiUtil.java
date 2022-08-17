/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

public class TemplateValueUiUtil {

    private TemplateValueUiUtil() {
    }

    public static <T extends ITemplatedValue> ToolItem setUpStatusToolItem(ToolBar toolBar,
            BindingContext bindingContext,
            AbstractTemplateValuePmo<T> pmo) {
        final ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        // set any default icon to force correct button size. Correct icon will be set when UI
        // gets updated
        toolItem.setImage(TemplateValueUiStatus.OVERWRITE_EQUAL.getIcon());
        bindTemplateStatusButton(bindingContext, toolBar, toolItem, pmo);

        listenToTemplateStatusClick(toolItem, pmo);

        return toolItem;
    }

    private static <T extends ITemplatedValue> void listenToTemplateStatusClick(final ToolItem toolItem,
            final AbstractTemplateValuePmo<T> pmo) {
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                pmo.onClick();
            }
        });
    }

    public static <T extends ITemplatedValue> void bindTemplateStatusButton(final BindingContext bindingContext,
            final ToolBar toolBar,
            final ToolItem toolItem,
            final AbstractTemplateValuePmo<T> pmo) {
        bindingContext.add(new PropertyChangeBinding<>(toolBar, pmo,
                AbstractTemplateValuePmo.PROPERTY_TEMPLATE_VALUE_STATUS, TemplateValueUiStatus.class) {

            @Override
            protected void propertyChanged(TemplateValueUiStatus oldValue, TemplateValueUiStatus newValue) {
                toolItem.setImage(newValue.getIcon());
            }
        });
        new DefaultToolTip(toolBar) {

            @Override
            protected String getText(Event event) {
                return pmo.getToolTipText();
            }

        };
    }

}
