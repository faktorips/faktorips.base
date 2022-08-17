/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * {@link FormattedCellEditingSupport} that creates its cell editors based on a specific
 * {@link ValueDatatype}.
 * 
 * @author Stefan Widmaier
 */
public class DatatypeEditingSupport<T> extends FormattedCellEditingSupport<T, String> {

    private final UIToolkit toolkit;
    private final TableViewer tableViewer;
    private final ValueDatatype datatype;
    private ValueDatatypeControlFactory controlFactory;
    private final IIpsProject ipsProject;

    public DatatypeEditingSupport(UIToolkit toolkit, TableViewer tableViewer, IIpsProject ipsProject,
            ValueDatatype datatype, IElementModifier<T, String> elementModifier) {
        this(toolkit, tableViewer, ipsProject, datatype, elementModifier, DEFAULT_EDIT_CONDITION);
    }

    public DatatypeEditingSupport(UIToolkit toolkit, TableViewer tableViewer, IIpsProject ipsProject,
            ValueDatatype datatype, IElementModifier<T, String> elementModifier, EditCondition editCondition) {
        super(tableViewer, elementModifier, editCondition);
        this.toolkit = toolkit;
        this.tableViewer = tableViewer;
        this.ipsProject = ipsProject;
        this.datatype = datatype;
        controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(T element) {
        return controlFactory.createTableCellEditor(toolkit, datatype, null, tableViewer, 0, ipsProject);
    }

    /**
     * Returns the string representing the given element. The string is formatted depending on the
     * datatype and the current locale.
     * 
     * @param element the element to return a formatted string for
     */
    @Override
    public String getFormattedValue(T element) {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, getValue(element));
    }

}
