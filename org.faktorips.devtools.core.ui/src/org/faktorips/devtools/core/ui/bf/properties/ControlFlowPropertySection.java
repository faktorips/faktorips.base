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
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.bf.edit.ControlFlowEditPart;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * A section that is displayed in the property view. The properties of a control flow object can be
 * edited with it.
 * 
 * @author Peter Erzberger
 */
public class ControlFlowPropertySection extends AbstractPropertySection {

    private Composite contentPanel;
    private Text conditionValueField;
    protected BindingContext bindingContext;
    protected UIToolkit uiToolkit;

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        parent.setLayout(new GridLayout(1, true));
        uiToolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        Composite panel = uiToolkit.createGridComposite(parent, 1, true, true);
        bindingContext = new BindingContext();
        contentPanel = uiToolkit.createLabelEditColumnComposite(panel);
        uiToolkit.createLabel(contentPanel, Messages.ControlFlowPropertySection_valueLabel);
        conditionValueField = uiToolkit.createText(contentPanel);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = false;
        data.widthHint = 300;
        conditionValueField.setLayoutData(data);
    }

    public IControlFlow getControlFlow() {
        return ((ControlFlowEditPart)((IStructuredSelection)getSelection()).getFirstElement()).getControlFlow();
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        bindingContext.removeBindings(conditionValueField);
        IControlFlow cf = getControlFlow();
        IBFElement source = cf.getSource();
        if (source instanceof IDecisionBFE) {
            bindingContext.bindContent(conditionValueField, getControlFlow(), IControlFlow.PROPERTY_CONDITION_VALUE);
            contentPanel.setVisible(true);
        } else {
            contentPanel.setVisible(false);
        }
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
