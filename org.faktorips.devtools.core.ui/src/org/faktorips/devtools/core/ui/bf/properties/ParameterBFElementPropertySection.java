/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A section that is displayed in the property view. The properties of a parameter element can be
 * edited with it.
 * 
 * @author Peter Erzberger
 */

public class ParameterBFElementPropertySection extends AbstractPropertySection {

    private ParametersEditControl control;
    private boolean initialized = false;

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        parent.setLayout(new GridLayout(1, false));
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true));
        UIToolkit toolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        control = new ParametersEditControl(parent, toolkit);
        control.setBackground(toolkit.getFormToolkit().getColors().getBackground());
        control.initControl();
        control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true));
    }

    private IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)((EditPart)((IStructuredSelection)getSelection()).getFirstElement()).getModel();
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        if (!initialized) {
            control.setInput(getBusinessFunction());
            initialized = true;
        }
    }

}
