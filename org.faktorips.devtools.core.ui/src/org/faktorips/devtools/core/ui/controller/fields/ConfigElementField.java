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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.productcmpt.AnyValueSetControl;
import org.faktorips.devtools.core.ui.inputformat.AnyValueSetFormat;

public class ConfigElementField extends FormattingTextField<IValueSet> {

    private final AnyValueSetControl valueSetControl;
    private final IConfigElement configElement;

    public ConfigElementField(IConfigElement configElement, AnyValueSetControl valueSetControl) {
        this(configElement, valueSetControl, true);
    }

    public ConfigElementField(IConfigElement configElement, AnyValueSetControl valueSetControl,
            boolean formatOnFocusLost) {
        super(valueSetControl.getTextControl(), AnyValueSetFormat.newInstance(configElement), formatOnFocusLost);
        // important: the whole value set must never be null but may contains null values
        setSupportsNullStringRepresentation(false);
        this.valueSetControl = valueSetControl;
        this.configElement = configElement;
        initContentAssistent();
    }

    private void initContentAssistent() {
        if (isContentAssistAvailable()) {
            ConfigElementProposalProvider proposalProvider = new ConfigElementProposalProvider(configElement,
                    IpsUIPlugin.getDefault().getDatatypeFormatter());
            new UIToolkit(null).attachContentProposalAdapter(getTextControl(), proposalProvider,
                    ContentProposalAdapter.PROPOSAL_INSERT, null);
        }
    }

    private boolean isContentAssistAvailable() {
        try {
            boolean enumValueSetAllowed = configElement.getAllowedValueSetTypes(getIpsProject()).contains(
                    ValueSetType.ENUM);
            if (enumValueSetAllowed) {
                ValueDatatype valueDatatype = configElement.findValueDatatype(getIpsProject());
                if (valueDatatype.isEnum()) {
                    return true;
                }
                IValueSet modelValueSet = configElement.findPcTypeAttribute(getIpsProject()).getValueSet();
                return modelValueSet.isEnum();
            } else {
                return false;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IIpsProject getIpsProject() {
        return configElement.getIpsProject();
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
