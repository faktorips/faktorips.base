/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.controls.chooser.ListChooser;
import org.faktorips.devtools.core.ui.controls.chooser.ListChooserValue;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.ObjectUtil;

/**
 * A {@link ListChooser} that allowes to define an enum value set. The list of possible values in
 * the set that is defined in the control can be provided by another value set (source value set) or
 * by an {@link EnumDatatype}.
 * 
 * @author Thorsten Guenther, Stefan Widmaier
 */
public class EnumSubsetChooser extends ListChooser implements IValueSetEditControl {

    /**
     * The value set to modify
     */
    private IEnumValueSet targetValueSet;

    /**
     * The value set to get the values from. <code>null</code> if the possible values are defined by
     * an EnumDatatype.
     */
    private IEnumValueSet sourceValueSet;

    /**
     * The datatype all values in the set are instances of. If the sourceValueSet is
     * <code>null</code> then the value datatype MUST be an EnumDatatype!
     */
    private ValueDatatype valueDatatype;

    /**
     * Creates a new chooser where the allowed values are defined by the given EnumDatatype.
     * 
     * @param parent The parent control
     * @param toolkit The toolkit to make creation of UI easier.
     * @param target The target-valueset (the one to add the values to).
     * @param datatype The enum datatype that provides the allowed values.
     * @param uiController The controller to notify upon change
     */
    public EnumSubsetChooser(Composite parent, UIToolkit toolkit, IEnumValueSet target, EnumDatatype datatype,
            UIController uiController) {
        this(parent, toolkit, null, target, datatype, uiController);
    }

    /**
     * Creates a new chooser where the allowed values are defined by the given source value set.
     * 
     * @param parent The parent control
     * @param toolkit The toolkit to make creation of UI easier.
     * @param source The source-valueset. Can be <code>null</code> if no restriction is applied to
     *            the items of the enum datatype.
     * @param target The target-valueset (the one to add the values to).
     * @param uiController The controller to notify upon change
     */
    public EnumSubsetChooser(Composite parent, UIToolkit toolkit, IEnumValueSet source, IEnumValueSet target,
            ValueDatatype datatype, UIController uiController) {
        super(parent);
        targetValueSet = target;
        sourceValueSet = source;
        setValueDatatype(datatype);
        initControl(toolkit);
        init(getAdditionalSourceValues(), targetValueSet);
    }

    @Override
    protected void newTableColumns(Table parent, TableLayoutComposite parentLayouter) {
        parentLayouter.addColumnData(new ColumnPixelData(20, false)); // message image
        parentLayouter.addColumnData(new ColumnWeightData(100, true));
        new TableColumn(parent, SWT.NONE).setResizable(false);
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);
        new TableColumn(parent, ctrlFactory.getDefaultAlignment()).setResizable(false);
    }

    public boolean allowedValuesAreDefinedBySourceValueSet() {
        return sourceValueSet != null;
    }

    public boolean allowedValuesAreDefinedByEnumDatatype() {
        return sourceValueSet == null;
    }

    /**
     * If {link {@link #allowedValuesAreDefinedByEnumDatatype()} returns <code>true</code>, this
     * method returns the enum datatype providing the allowed values.
     */
    private EnumDatatype getEnumDatatype() {
        if (valueDatatype.isEnum()) {
            return (EnumDatatype)valueDatatype;
        }
        return null;
    }

    /**
     * Sets the new datatype. If the new datatype is an enum, the method creates a mapping from
     * enum-name to enum-id to provide a fast lookup.
     */
    private void setValueDatatype(ValueDatatype newDatatype) {
        if (ObjectUtil.equals(valueDatatype, newDatatype)) {
            return;
        }
        valueDatatype = newDatatype;
    }

    public IEnumValueSet getSourceValueSet() {
        return sourceValueSet;
    }

    public IEnumValueSet getTargetValueSet() {
        return targetValueSet;
    }

    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        if (valueDatatype == null) {
            return false;
        }
        return valueSet.isEnum() && (valueDatatype.isEnum());
    }

    @Override
    public IValueSet getValueSet() {
        return targetValueSet;
    }

    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.ENUM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        setEnumValueSet((IEnumValueSet)newSet, valueDatatype);
    }

    /**
     * Sets the new enum value set being edited in the control.
     * 
     * @param valueSet The new set of values.
     * @param newDatatype The datatype the values in the set are instances of.
     * 
     * @throws NullPointerException if newSet is <code>null</code>.
     */
    public void setEnumValueSet(IEnumValueSet valueSet, ValueDatatype newDatatype) {
        targetValueSet = valueSet;
        setValueDatatype(valueDatatype);
        init(getAdditionalSourceValues(), targetValueSet);
    }

    /**
     * Returns all values the user can add to the (target) value set he is editing. Values already
     * in the target set, are not returned.
     */
    private List<String> getAdditionalSourceValues() {
        List<String> values = new ArrayList<String>();
        if (allowedValuesAreDefinedBySourceValueSet()) {
            for (String value : (targetValueSet.getValuesNotContained(sourceValueSet))) {
                values.add(value);
            }
        } else {
            List<String> targetIds = Arrays.asList(targetValueSet.getValues());
            String[] allIds = getEnumDatatype().getAllValueIds(true);
            for (int i = 0; i < allIds.length; i++) {
                if (!targetIds.contains(allIds[i])) {
                    values.add(allIds[i]);
                }
            }
        }
        return values;
    }

    /**
     * Returns all values the user can add to the (target) value set whether the value is already in
     * the target set or not.
     */
    public List<String> getAllSourceValues() {
        if (allowedValuesAreDefinedByEnumDatatype()) {
            return Arrays.asList(getEnumDatatype().getAllValueIds(true));
        } else {
            return Arrays.asList(sourceValueSet.getValues());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList getMessagesFor(String value) {
        if (sourceValueSet == null) {
            return new MessageList();
        }
        return getMessagesForValue(value);
    }

    protected MessageList getMessagesForValue(String valueId) {
        MessageList result = new MessageList();
        try {
            sourceValueSet.containsValue(valueId, result, targetValueSet, null, sourceValueSet.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return result;
    }

    @Override
    protected ITableLabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ListChooserValue) {
                ListChooserValue value = (ListChooserValue)element;
                if (columnIndex != IMG_COLUMN) {
                    return null;
                }
                MessageList messages = getMessagesFor(value.getValue());
                if (!messages.isEmpty()) {
                    return IpsUIPlugin.getImageHandling().getImage(
                            IpsProblemOverlayIcon.getOverlay(messages.getSeverity()), false);
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == IMG_COLUMN) {
                return ""; //$NON-NLS-1$
            }
            String formattedString = IpsUIPlugin.getDefault().getDatatypeFormatter()
                    .formatValue(valueDatatype, element.toString());
            return formattedString;
        }

    }
}
