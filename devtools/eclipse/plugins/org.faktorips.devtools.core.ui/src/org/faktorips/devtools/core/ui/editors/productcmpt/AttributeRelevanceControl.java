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

import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * Control for {@link AttributeRelevance}.
 */
public class AttributeRelevanceControl extends ControlComposite {

    private UIToolkit toolkit;
    private IPolicyCmptTypeAttribute attribute;
    private RadioButtonGroup<AttributeRelevance> radioButtonGroup;

    public AttributeRelevanceControl(Composite parent, UIToolkit toolkit, IPolicyCmptTypeAttribute attribute) {
        super(parent, SWT.NONE);
        this.toolkit = toolkit;
        this.attribute = attribute;
        initControls();
    }

    private void initControls() {
        ValueDatatype valueDatatype = attribute.findDatatype(attribute.getIpsProject());
        LinkedHashMap<AttributeRelevance, String> options = new LinkedHashMap<>();
        IValueSet valueSet = attribute.getValueSet();
        if (!valueSet.isEmpty()) {
            options.put(AttributeRelevance.Mandatory, Messages.AttributeRelevanceControl_Mandatory);
        }
        if (!valueDatatype.isPrimitive() && valueSet.isContainsNull()) {
            options.put(AttributeRelevance.Optional, Messages.AttributeRelevanceControl_Optional);
        }
        options.put(AttributeRelevance.Irrelevant, Messages.AttributeRelevanceControl_Irrelevant);
        radioButtonGroup = toolkit.createRadioButtonGroup(this, options);
        initLayout(options.size());
    }

    private void initLayout(int components) {
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        layout = new GridLayout(components, false);
        layout.horizontalSpacing = 10;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        radioButtonGroup.getComposite().setLayout(layout);
    }

    public RadioButtonGroup<AttributeRelevance> getRadioButtonGroup() {
        return radioButtonGroup;
    }

}
