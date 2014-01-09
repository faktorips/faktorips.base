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

package org.faktorips.devtools.core.ui.bf.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;

/**
 * A section that is displayed in the property view. The properties of a control flow object can be
 * edited with it.
 * 
 * @author Peter Erzberger
 */
public class DecisionPropertySection extends NamedOnlyBFElementsPropertySection {

    private DatatypeRefControl datatypeField;

    @Override
    protected void createControlsInternal(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        uiToolkit.createLabel(parent, Messages.DecisionPropertySection_datatypeLabel);
        datatypeField = uiToolkit.createDatatypeRefEdit(null, parent);
        datatypeField.setOnlyValueDatatypesAllowed(true);
        datatypeField.setPrimitivesAllowed(false);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = false;
        data.widthHint = 300;
        datatypeField.setLayoutData(data);
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        IDecisionBFE decision = (IDecisionBFE)getBFElement();
        datatypeField.setIpsProject(decision.getIpsProject());
        bindingContext.removeBindings(datatypeField);
        bindingContext.bindContent(datatypeField, decision, IDecisionBFE.PROPERTY_DATATYPE);
    }
}
