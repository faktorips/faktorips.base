/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.bf.edit.NodeEditPart;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.bf.IBFElement;

/**
 * A section that is displayed in the property view. The name property of a business function
 * element can be edited with it.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
public class NamedOnlyBFElementsPropertySection extends AbstractPropertySection {

    private Text nameField;
    protected BindingContext bindingContext;
    protected UIToolkit uiToolkit;

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        parent.setLayout(new GridLayout(1, true));
        uiToolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        Composite panel = uiToolkit.createGridComposite(parent, 1, true, true);
        bindingContext = new BindingContext();
        Composite content = uiToolkit.createLabelEditColumnComposite(panel);
        uiToolkit.createLabel(content, Messages.NamedOnlyBFElementsPropertySection_nameLabel);
        nameField = uiToolkit.createText(content);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = false;
        data.widthHint = 300;
        nameField.setLayoutData(data);
        createControlsInternal(content, tabbedPropertySheetPage);
    }

    /**
     * This method can be overridden by subclasses to add additional editing field to this section.
     * 
     * @param parent The composite to extend
     * @param tabbedPropertySheetPage The tabbed property sheet page
     */
    protected void createControlsInternal(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        // Empty default implementation
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bindingContext != null) {
            bindingContext.dispose();
        }
    }

    public IBFElement getBFElement() {
        return ((NodeEditPart)((IStructuredSelection)getSelection()).getFirstElement()).getBFElement();
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        bindingContext.removeBindings(nameField);
        bindingContext.bindContent(nameField, getBFElement(), IIpsElement.PROPERTY_NAME);
        bindingContext.updateUI();
    }

}
