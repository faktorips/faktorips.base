/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.AbstractProposalProvider;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.ConfigElementProposalProvider;
import org.faktorips.devtools.core.ui.editors.productcmpt.AnyValueSetControl;
import org.faktorips.devtools.core.ui.editors.productcmpt.ContentProposalListener;
import org.faktorips.devtools.core.ui.inputformat.AnyValueSetFormat;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

public class ConfiguredValueSetField extends FormattingTextField<IValueSet> {

    private final AnyValueSetControl valueSetControl;
    private final IConfiguredValueSet configuredValueSet;

    public ConfiguredValueSetField(IConfiguredValueSet configuredValueSet, AnyValueSetControl valueSetControl) {
        this(configuredValueSet, valueSetControl, true);
    }

    public ConfiguredValueSetField(IConfiguredValueSet configuredValueSet, AnyValueSetControl valueSetControl,
            boolean formatOnFocusLost) {
        super(valueSetControl.getTextControl(), AnyValueSetFormat.newInstance(configuredValueSet), formatOnFocusLost);
        // important: the whole value set must never be null but may contains null values
        setSupportsNullStringRepresentation(false);
        this.valueSetControl = valueSetControl;
        this.configuredValueSet = configuredValueSet;
        checkSpecialConstraints();
        initContentAssistent();
    }

    private void checkSpecialConstraints() {
        valueSetControl.getTextControl().setEnabled(!configuredValueSet.getValueSet().isStringLength());
    }

    private void initContentAssistent() {
        if (isContentAssistAvailable()) {
            ValueDatatype valueDatatype = configuredValueSet.findValueDatatype(getIpsProject());
            IInputFormat<String> inputFormat = IpsUIPlugin.getDefault().getInputFormat(valueDatatype, getIpsProject());
            AbstractProposalProvider proposalProvider = new ConfigElementProposalProvider(configuredValueSet,
                    valueDatatype, inputFormat);
            ContentProposalAdapter contentProposalAdapter = new UIToolkit(null).attachContentProposalAdapter(
                    getTextControl(), proposalProvider, ContentProposalAdapter.PROPOSAL_IGNORE, null);
            ContentProposalListener contentProposalListener = new ContentProposalListener(contentProposalAdapter);
            contentProposalAdapter.addContentProposalListener(contentProposalListener);
        }
    }

    private boolean isContentAssistAvailable() {
        boolean enumValueSetAllowed = configuredValueSet.getAllowedValueSetTypes(getIpsProject()).contains(
                ValueSetType.ENUM);
        if (enumValueSetAllowed) {
            ValueDatatype valueDatatype = configuredValueSet.findValueDatatype(getIpsProject());
            if (valueDatatype == null) {
                return false;
            } else if (valueDatatype.isEnum()) {
                return true;
            }
            IValueSet modelValueSet = configuredValueSet.findPcTypeAttribute(getIpsProject()).getValueSet();
            return modelValueSet.isEnum();
        } else {
            return false;
        }
    }

    private IIpsProject getIpsProject() {
        return configuredValueSet.getIpsProject();
    }

    @Override
    public Control getControl() {
        return valueSetControl;
    }

    @Override
    public boolean isTextContentParsable() {
        return true;
    }
}
