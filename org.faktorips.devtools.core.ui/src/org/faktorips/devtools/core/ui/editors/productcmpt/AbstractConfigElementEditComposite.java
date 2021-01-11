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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.forms.IpsSection;

public abstract class AbstractConfigElementEditComposite<V extends IConfigElement> extends
        EditPropertyValueComposite<IPolicyCmptTypeAttribute, V> {

    public AbstractConfigElementEditComposite(IPolicyCmptTypeAttribute property, V propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {
        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
    }

    @Override
    protected void setLayout() {
        super.setLayout();
        GridLayout clientLayout = (GridLayout)getLayout();
        clientLayout.numColumns = 2;
        // Space between Labels and fields. Also allows error markers to be drawn.
        clientLayout.horizontalSpacing = 7;
    }

    /**
     * Creates a {@link Label} whose width corresponds to the width of the broadest label of this
     * section.
     * 
     * @param text The text for the label
     */
    protected void createLabel(String text) {
        Label label = getToolkit().createLabel(this, text);
        ((GridData)label.getLayoutData()).widthHint = calcWidth();
    }

    private int calcWidth() {
        GC gc = new GC(this);
        int widthDefault = gc.stringExtent(Messages.ConfigElementEditComposite_defaultValue).x;
        int widthValueSet = gc.stringExtent(Messages.ConfigElementEditComposite_valueSet).x;
        return Math.max(widthDefault, widthValueSet);
    }

}