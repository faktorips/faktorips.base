/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationFieldPainter;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationProposalAdapter;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

/**
 * A factory to create controls and edit fields that allow to edit values for one or more value
 * datatypes.
 * 
 * @author Joerg Ortmann
 */
public abstract class ValueDatatypeControlFactory {

    /**
     * Returns <code>true</code> if this factory can create controls for the given datatype,
     * otherwise <code>false</code>.
     * 
     * @param datatype Datatype controls are needed for - might be <code>null</code>.
     */
    public abstract boolean isFactoryFor(ValueDatatype datatype);

    /**
     * Creates a control and edit field that allows to edit a value of one of the value datatypes
     * this is a factory for.
     * 
     * @param toolkit The toolkit used to create the control.
     * @param parent The parent composite to which the control is added.
     * @param datatype The value datatype a control should be created for.
     * @param valueSet An optional @Deprecated valueset. Future Implementations should use
     *            ValueSetOwner instead.
     * 
     */
    public abstract EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject);

    protected void adaptEnumValueSetProposal(Text textControl, IValueSet valueSet, ValueDatatype datatype) {
        if (valueSet != null) {
            IValueSetOwner valueSetOwner = valueSet.getValueSetOwner();
            EnumerationFieldPainter.addPainterTo(textControl, datatype, valueSetOwner);
            IInputFormat<String> inputFormat = IpsUIPlugin.getDefault().getInputFormat(datatype,
                    valueSetOwner.getIpsProject());
            EnumerationProposalAdapter.createAndActivateOnAnyKey(textControl, datatype, valueSetOwner, inputFormat);
        }
    }

    /**
     * Creates a control that allows to edit a value of the value datatype this is a factory for.
     * 
     * @param toolkit The toolkit used to create the control.
     * @param parent The parent composite to which the control is added.
     * @param datatype The value datatype a control should be created for.
     * @param valueSet An optional @Deprecated valueset.Future Implementations should use
     *            ValueSetOwner instead.
     */
    public abstract Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject);

    /**
     * Creates a cell editor that allows to edit a value of the value datatype this is a factory
     * for.
     * 
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    public abstract IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject);

    /**
     * Creates a cell editor that allows to edit a value of the value datatype this is a factory
     * for.
     * 
     * @param toolkit The ui toolkit to use for creating ui elements.
     * @param datatype The <code>ValueDatatype</code> to create a cell editor for.
     * @param valueSet An optional valueset.
     * @param tableViewer The viewer
     * @param columnIndex The index of the column.
     */
    public abstract IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject);

    public abstract int getDefaultAlignment();

    protected boolean isControlForDefaultValue(IValueSet valueSet) {
        return valueSet != null && valueSet.getValueSetOwner() instanceof IConfigElement;
    }

    protected String getNullStringRepresentation(IValueSet valueSet) {
        if (isControlForDefaultValue(valueSet)) {
            return Messages.DefaultValueRepresentation_Combobox;
        } else {
            return StringUtils.EMPTY;
        }
    }

}
