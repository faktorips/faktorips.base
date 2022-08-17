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

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TableContentsUsageRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;

/**
 * Provides controls that allow the user to edit an {@link ITableContentUsage}.
 * 
 * @since 3.6
 * 
 * @see ITableContentUsage
 */
public class TableContentUsageEditComposite
        extends EditPropertyValueComposite<ITableStructureUsage, ITableContentUsage> {

    public TableContentUsageEditComposite(ITableStructureUsage property, ITableContentUsage propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        TextButtonField tcuField = createTableContentEditField();
        createTemplateStatusButton(tcuField);
        addMovingChangingOverTimeDecorationIfRequired(tcuField);
        editFields.add(tcuField);
    }

    private TextButtonField createTableContentEditField() {
        TableContentsUsageRefControl tcuControl = new TableContentsUsageRefControl(getPropertyValue().getIpsProject(),
                this, getToolkit(), getPropertyValue());

        TextButtonField editField = new TextButtonField(tcuControl);
        editField.setSupportsNullStringRepresentation(false);
        getBindingContext().bindContent(editField, getPropertyValue(), ITableContentUsage.PROPERTY_TABLE_CONTENT);
        return editField;
    }

    @Override
    protected Function<ITableContentUsage, String> getToolTipFormatter() {
        return PropertyValueFormatter.TABLE_CONTENT_USAGE;
    }
}
