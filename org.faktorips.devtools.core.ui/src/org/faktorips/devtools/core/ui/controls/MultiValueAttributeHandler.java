/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.controls.chooser.MultiValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueListExtractor;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;
import org.faktorips.devtools.core.ui.dialogs.MultiValueSubsetDialog;

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
            openMultiValueSubsetDialog((IEnumValueSet)modelValueSet);
        } else {
            if (datatype.isEnum()) {
                openMultiValueSubsetDialog((EnumDatatype)datatype);
            } else {
                openMultiValueDialog();
            }
        }
    }

    protected void openMultiValueSubsetDialog(EnumDatatype enumDatatype) {
        openMultiValueSubsetDialog(ValueListExtractor.extractValues(enumDatatype));
    }

    protected void openMultiValueSubsetDialog(IEnumValueSet sourceValueSet) {
        openMultiValueSubsetDialog(sourceValueSet.getValuesAsList());
    }

    protected void openMultiValueSubsetDialog(List<String> sourceValues) {
        MultiValueSubsetChooserModel model = new MultiValueSubsetChooserModel(sourceValues,
                (MultiValueHolder)attributeValue.getValueHolder(), datatype);
        new MultiValueSubsetDialog(shell, attributeValue, model).open();
        // changes are applied directly to the provided target value set
    }

    protected void openMultiValueDialog() {
        MultiValueDialog multiValueDialog = new MultiValueDialog(shell, attributeValue, datatype);
        multiValueDialog.open();
        // values are applied in the dialog's okPressed() method
    }

}