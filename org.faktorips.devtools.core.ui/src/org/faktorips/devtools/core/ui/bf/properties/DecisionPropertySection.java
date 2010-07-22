/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
