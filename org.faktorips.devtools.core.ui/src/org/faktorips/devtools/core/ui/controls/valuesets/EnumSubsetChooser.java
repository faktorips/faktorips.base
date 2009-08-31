/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controls.ListChooser;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.ObjectUtil;

/**
 * A {@link ListChooser} that allowed to define an enum value set. The list of possible values in
 * the set that is defined in the control can be provided by another value set (source value set) or
 * by an {@link EnumDatatype}.
 * 
 * @author Thorsten Guenther
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
     * The controller to notify of changes to the target value set
     */
    private UIController uiController;

    /**
     * Mapping of the formatted values to underlying values. For enums the formatted value is the
     * enum-name and the unformatted value is the enum-id.
     */
    private Map<String, String> formattedValue2Value = new HashMap<String, String>();

    /**
     * Creates a new chooser where the allowed values are defined by the given EnumDatatype.
     * 
     * @param parent
     * @param toolkit
     * @param target
     * @param datatype
     * @param uiController
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
        super(parent, toolkit);
        targetValueSet = target;
        sourceValueSet = source;
        setValueDatatype(datatype);
        this.uiController = uiController;
        setContents();
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
     * 
     * @return
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
        String[] values = getAllSourceValues();
        formattedValue2Value = new HashMap<String, String>();
        for (int i = 0; i < values.length; i++) {
            String formattedValue = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                    valueDatatype, values[i]);
            formattedValue2Value.put(formattedValue, values[i]);
        }
    }

    private void setContents() {
        super.setTargetContent(getTargetValues());
        super.setSourceContent(getSourceValues());
    }

    public IEnumValueSet getSourceValueSet() {
        return sourceValueSet;
    }

    public IEnumValueSet getTargetValueSet() {
        return targetValueSet;
    }

    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        return valueSet.isEnum() && (valueDatatype.isEnum());
    }

    public IValueSet getValueSet() {
        return targetValueSet;
    }

    public ValueSetType getValueSetType() {
        return ValueSetType.ENUM;
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        setEnumValueSet((IEnumValueSet)newSet, valueDatatype);
    }

    /**
     * Sets the new enum value set being edited in the control.
     * 
     * @param newSet The new set of values.
     * @param valueDatatype The datatype the values in the set are instances of.
     * 
     * @throws NullPointerException if newSet is <code>null</code>.
     */
    public void setEnumValueSet(IEnumValueSet valueSet, ValueDatatype newDatatype) {
        setValueDatatype(valueDatatype);
        targetValueSet = valueSet;
        setContents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void valuesAdded(String[] values) {
        for (int i = 0; i < values.length; i++) {
            targetValueSet.addValue(getValueForFormattedValue(values[i]));
        }
        uiController.updateUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void valuesRemoved(String[] values) {
        for (int i = 0; i < values.length; i++) {
            targetValueSet.removeValue(getValueForFormattedValue(values[i]));
        }
        uiController.updateUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void valueMoved(String textShown, int oldIndex, int newIndex, boolean up) {
        String tmp = targetValueSet.getValue(newIndex);
        targetValueSet.setValue(newIndex, getValueForFormattedValue(textShown));
        targetValueSet.setValue(oldIndex, tmp);
    }

    /**
     * Returns the value id for the given human readable name.
     */
    private String getValueForFormattedValue(String name) {
        return formattedValue2Value.get(name);
    }

    /**
     * Returns an array of human readable strings representing all values contained in the given
     * valueset. The names are requested from the given datatype. If the type is <code>null</code>
     * or does not suppert value names, the ids are returned as names.
     * 
     * @param valueSet The valueset to get the names for.
     * @param type The datatype to get the names from. Can be <code>null</code>.
     */
    private String[] getTargetValues() {
        String[] ids = targetValueSet.getValues();
        return formatValues(ids);
    }

    /**
     * Returns all values the user can add to the (target) value set he is editing. Value already in
     * the target set, are not returned. The values are formatted according to the preferences for
     * the given value datatype. For enum value for example, the name is returned instead of the id.
     */
    private String[] getSourceValues() {
        String[] values = new String[0];
        if (allowedValuesAreDefinedBySourceValueSet()) {
            values = targetValueSet.getValuesNotContained(sourceValueSet);
        } else {
            List<String> targetIds = Arrays.asList(targetValueSet.getValues());
            String[] allIds = getEnumDatatype().getAllValueIds(true);
            List<String> result = new ArrayList<String>();
            for (int i = 0; i < allIds.length; i++) {
                if (!targetIds.contains(allIds[i])) {
                    result.add(allIds[i]);
                }
            }
            values = result.toArray(new String[result.size()]);
        }
        return formatValues(values);
    }

    private String[] getAllSourceValues() {
        if (allowedValuesAreDefinedByEnumDatatype()) {
            return getEnumDatatype().getAllValueIds(true);
        } else {
            return sourceValueSet.getValues();
        }
    }

    /**
     * Formats the values accroding to the datatype specific formatter obtained by
     * {@link IpsPreferences#getDatatypeFormatter()}.
     */
    private String[] formatValues(String[] values) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            String name = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(valueDatatype,
                    values[i]);
            result.add(name);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList getMessagesFor(String value) {
        if (formattedValue2Value != null) {
            value = formattedValue2Value.get(value);
        }
        if (sourceValueSet == null) {
            return new MessageList();
        }
        return getMessagesForValue(value);
    }

    protected MessageList getMessagesForValue(String valueId) {
        MessageList result = new MessageList();
        sourceValueSet.containsValue(valueId, result, targetValueSet, null);
        return result;
    }
}
