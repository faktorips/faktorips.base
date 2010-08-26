/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IMethodCallBFE;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.bf.edit.NodeEditPart;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * A section that is displayed in the property view. The properties of a method call action can be
 * edited with it.
 * 
 * @author Peter Erzberger
 */
public class CallMethodPropertySection extends AbstractPropertySection implements ContentsChangeListener {

    protected Text parameterSelectionControl;
    protected ParameterMethodRefControl methodSelectionField;
    protected BindingContext bindingContext;
    protected UIToolkit uiToolkit;
    private ParameterCompletionProcessor processor;

    @Override
    public void aboutToBeHidden() {
        getBFElement().getIpsModel().removeChangeListener(this);
    }

    @Override
    public void aboutToBeShown() {
        getBFElement().getIpsModel().addChangeListener(this);
    }

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        parent.setLayout(new GridLayout(1, true));
        uiToolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        Composite panel = uiToolkit.createGridComposite(parent, 1, true, true);
        bindingContext = new BindingContext();
        Composite content = uiToolkit.createLabelEditColumnComposite(panel);
        uiToolkit.createLabel(content, Messages.CallMethodPropertySection_parameterLabel);
        parameterSelectionControl = uiToolkit.createText(content);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = false;
        data.widthHint = 300;
        parameterSelectionControl.setLayoutData(data);
        uiToolkit.createLabel(content, Messages.CallMethodPropertySection_MethodLabel);
        methodSelectionField = new ParameterMethodRefControl(content, uiToolkit);
        data = new GridData();
        data.grabExcessHorizontalSpace = false;
        data.widthHint = 300;
        methodSelectionField.setLayoutData(data);
        extendControlArea(content);
        processor = new ParameterCompletionProcessor();
        processor.setComputeProposalForEmptyPrefix(true);
        SubjectControlContentAssistant assistant = CompletionUtil.createContentAssistant(processor);
        ContentAssistHandler.createHandlerForText(parameterSelectionControl, assistant);
    }

    /**
     * Template method to allow subclasses extending the control area
     * 
     * @param area The control area to be extended
     */
    protected void extendControlArea(Composite area) {
        // Empty default implementation
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public IBFElement getBFElement() {
        return ((NodeEditPart)((IStructuredSelection)getSelection()).getFirstElement()).getBFElement();
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        IMethodCallBFE methodCallBFE = (IMethodCallBFE)getBFElement();
        processor.setBusinessFunction(methodCallBFE.getBusinessFunction());
        processor.setIpsProject(methodCallBFE.getIpsProject());
        methodSelectionField.setIpsProject(methodCallBFE.getIpsProject());
        bindingContext.removeBindings(parameterSelectionControl);
        bindingContext.removeBindings(methodSelectionField);
        bindingContext.bindContent(parameterSelectionControl, getBFElement(), IMethodCallBFE.PROPERTY_TARGET);
        bindingContext
                .bindContent(methodSelectionField, getBFElement(), IMethodCallBFE.PROPERTY_EXECUTABLE_METHOD_NAME);
        updateMethodSelectionControl();
        bindingContext.updateUI();
    }

    private void updateMethodSelectionControl() {
        try {
            IMethodCallBFE methodCallBFE = (IMethodCallBFE)getBFElement();
            if (methodCallBFE.getParameter() == null) {
                methodSelectionField.setParameterType(null);
                return;
            }
            Datatype datatype = methodCallBFE.getParameter().findDatatype();
            if (datatype instanceof IType) {
                methodSelectionField.setParameterType((IType)datatype);
            } else {
                methodSelectionField.setParameterType(null);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

    }

    protected void updateFromModel(ContentChangeEvent event) {
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(getBFElement().getIpsSrcFile())) {
            return;
        }
        updateMethodSelectionControl();
        updateFromModel(event);
    }

}
