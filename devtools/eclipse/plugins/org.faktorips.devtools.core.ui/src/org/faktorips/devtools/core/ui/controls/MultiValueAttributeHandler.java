/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controls.chooser.MultiValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueListExtractor;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;
import org.faktorips.devtools.core.ui.dialogs.MultiValueSubsetDialog;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IMultiValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;

public class MultiValueAttributeHandler {
    private final IAttributeValue attributeValue;
    private final Shell shell;
    private final ValueDatatype datatype;
    private final IProductCmptTypeAttribute productCmptTypeAttribute;

    public MultiValueAttributeHandler(Shell shell, IProductCmptTypeAttribute productCmptTypeAttribute,
            IAttributeValue attributeValue, ValueDatatype datatype) {
        this.shell = shell;
        this.productCmptTypeAttribute = productCmptTypeAttribute;
        this.attributeValue = attributeValue;
        this.datatype = datatype;
    }

    public void editValues() {
        IValueSet modelValueSet = productCmptTypeAttribute.getValueSet();
        if (modelValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            openMultiValueSubsetDialog((IEnumValueSet)modelValueSet, false);
        } else {
            if (datatype.isEnum()) {
                openMultiValueSubsetDialog((EnumDatatype)datatype, false);
            } else {
                openMultiValueDialog();
            }
        }
    }

    protected void openMultiValueSubsetDialog(EnumDatatype enumDatatype, boolean includeNull) {
        openMultiValueSubsetDialog(ValueListExtractor.extractValues(enumDatatype, includeNull));
    }

    protected void openMultiValueSubsetDialog(IEnumValueSet sourceValueSet, boolean includeNull) {
        openMultiValueSubsetDialog(ValueListExtractor.extractValues(sourceValueSet, includeNull));
    }

    protected void openMultiValueSubsetDialog(List<String> sourceValues) {
        MultiValueSubsetChooserModel model = new MultiValueSubsetChooserModel(sourceValues,
                (IMultiValueHolder)attributeValue.getValueHolder(), datatype, attributeValue);
        new MultiValueSubsetDialog(shell, attributeValue, model).open();
        // changes are applied directly to the provided target value set
    }

    protected void openMultiValueDialog() {
        MultiValueDialog multiValueDialog = new MultiValueDialog(shell, attributeValue, datatype);
        multiValueDialog.open();
        // values are applied in the dialog's okPressed() method
    }

}
