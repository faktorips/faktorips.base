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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.editors.type.MethodEditDialog;

public class ProductCmptTypeMethodEditDialog extends MethodEditDialog {

    public ProductCmptTypeMethodEditDialog(IMethod method, Shell parentShell) {
        super(method, parentShell);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        Composite c = super.createWorkArea(parent);
        bindingContext.bindEnabled(abstractCheckbox, method, IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION, false);
        
        
        nameText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(method.getName())) {
                    method.setName(((IProductCmptTypeMethod)method).getDefaultMethodName());
                }
            }
        });
        return c;
    }

    /**
     * {@inheritDoc}
     */
    protected void createAdditionalControlsOnGeneralPage(Composite parent, UIToolkit toolkit) {
        Composite group = toolkit.createGroup(parent, "Formula Signature Definition");
        AbstractCheckbox checkbox = toolkit.createCheckbox(group, "Is formula signature definition");
        bindingContext.bindContent(checkbox, method, IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);
        
        Composite area = uiToolkit.createLabelEditColumnComposite(group);
        toolkit.createLabel(area, "Formula name:");
        Text formulaNameText = toolkit.createText(area);
        bindingContext.bindContent(formulaNameText, method, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME);
    }
    
}