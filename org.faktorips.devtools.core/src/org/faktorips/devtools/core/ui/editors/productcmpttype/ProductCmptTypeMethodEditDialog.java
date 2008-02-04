/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.editors.type.MethodEditDialog;

public class ProductCmptTypeMethodEditDialog extends MethodEditDialog {

    public ProductCmptTypeMethodEditDialog(IProductCmptTypeMethod method, Shell parentShell) {
        super(method, parentShell);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        
        Composite c = super.createWorkArea(parent);
        
        nameText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(method.getName())) {
                    method.setName(getProductCmptTypeMethod().getDefaultMethodName());
                }
            }
        });
        
        return c;
    }

    private IProductCmptTypeMethod getProductCmptTypeMethod(){
        return (IProductCmptTypeMethod)method;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createAdditionalControlsOnGeneralPage(Composite parent, UIToolkit toolkit) {
        Composite group = toolkit.createGroup(parent, Messages.ProductCmptTypeMethodEditDialog_formulaGroup);
        AbstractCheckbox checkbox = toolkit.createCheckbox(group, Messages.ProductCmptTypeMethodEditDialog_formulaCheckbox);
        bindingContext.bindContent(checkbox, method, IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        Composite area = uiToolkit.createLabelEditColumnComposite(group);
        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_formulaNameLabel);
        Text formulaNameText = toolkit.createText(area);
        bindingContext.bindContent(formulaNameText, method, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME);

        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_labelOverloadedFormulaMethod);
        Text overloadedFormulaText = toolkit.createText(area);
        IContentAssistProcessor processor = 
            new OverloadedFormulaMethodSignatureCompletionProcessor(getProductCmptTypeMethod().getProductCmptType());
        CompletionUtil.createContentAssistant(processor);
        ContentAssistHandler.createHandlerForText(overloadedFormulaText, CompletionUtil.createContentAssistant(processor));
        
        bindingContext.bindContent(overloadedFormulaText, method, IProductCmptTypeMethod.PROPERTY_OVERLOADED_FORMULA_SIGNATURE);
    }
    
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        if (event.getIpsSrcFile().equals(getIpsPart().getIpsSrcFile())) {
            IProductCmptTypeMethod tMethod = (IProductCmptTypeMethod)method;
            datatypeControl.setVoidAllowed(!tMethod.isFormulaSignatureDefinition());
            datatypeControl.setOnlyValueDatatypesAllowed(tMethod.isFormulaSignatureDefinition());
        }
    }
    
    class OverloadedFormulaMethodSignatureCompletionProcessor extends AbstractCompletionProcessor {

        private IProductCmptType type;
        
        public OverloadedFormulaMethodSignatureCompletionProcessor(IProductCmptType type) {
            super(type==null ? null : type.getIpsProject());
            this.type = type;
            setComputeProposalForEmptyPrefix(true);
        }

        /**
         * {@inheritDoc}
         */
        protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
            if (type==null || !type.hasSupertype()) {
                return;
            }
            FormulaSignatureFinder finder = new FormulaSignatureFinder(method.getIpsProject(), getProductCmptTypeMethod().getFormulaName(), false);
            finder.start(type.findSupertype(method.getIpsProject()));

            List methods = finder.getMethods();
            for (Iterator it = methods.iterator(); it.hasNext();) {
                IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
                if (method.getSignatureString().startsWith(prefix)) {
                    addToResult(result, method, documentOffset);
                }
            }
        }
        
        private void addToResult(List result, IMethod method, int documentOffset) {
            String name = method.getSignatureString();
            CompletionProposal proposal = new CompletionProposal(
                    name, 0, documentOffset, name.length(),  
                    method.getImage(), name, null, method.getDescription());
            result.add(proposal);
        }
        
    }

}