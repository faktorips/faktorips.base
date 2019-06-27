/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.editors.DescriptionEditComposite;

/**
 * A section that is displayed in the property view. The description property of business function
 * elements can be edited with it.
 * 
 * @author Peter Erzberger
 */
public class DescriptionPropertySection extends AbstractPropertySection {

    private BindingContext bindingContext;
    private UIToolkit uiToolkit;
    private DescriptionEditComposite descriptionField;

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        bindingContext = new BindingContext();
        parent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        super.createControls(parent, tabbedPropertySheetPage);
        parent.setLayout(new GridLayout(1, true));
        uiToolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        Composite panel = uiToolkit.createGridComposite(parent, 1, true, true);
        descriptionField = new DescriptionEditComposite(panel, null, uiToolkit, bindingContext);
        GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
        descriptionField.setLayoutData(data);
    }

    private IIpsElement getIpsElement() {
        return (IIpsElement)((EditPart)((IStructuredSelection)getSelection()).getFirstElement()).getModel();
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        if (getIpsElement() instanceof IDescribedElement) {
            descriptionField.setDescribedElement((IDescribedElement)getIpsElement());
            descriptionField.setEnabled(true);
        } else {
            descriptionField.setEnabled(false);
        }
        descriptionField.refresh();
        bindingContext.updateUI();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bindingContext != null) {
            bindingContext.dispose();
        }
    }

}
