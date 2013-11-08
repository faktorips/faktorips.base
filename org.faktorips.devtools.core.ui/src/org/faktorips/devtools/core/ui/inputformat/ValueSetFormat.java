/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.inputformat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;

public class ValueSetFormat extends AbstractInputFormat<IValueSet> {

    public static final String VALUESET_SEPARATOR = "|"; //$NON-NLS-1$

    private final IConfigElement configElement;

    private final IpsUIPlugin uiPlugin;

    public ValueSetFormat(IConfigElement configElement, IpsUIPlugin uiPlugin) {
        this.configElement = configElement;
        this.uiPlugin = uiPlugin;
    }

    public static ValueSetFormat newInstance(IConfigElement configElement) {
        ValueSetFormat format = new ValueSetFormat(configElement, IpsUIPlugin.getDefault());
        format.initFormat();
        return format;
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        if (isEnumValueSetAllowed()) {
            if (isRangeAllowed()) {
                getValueSet();
            }
            if (stringToBeParsed.isEmpty()) {
                if (isUnrestrictedAllowed()) {
                    return getUnrestrictedValueSet();
                } else {
                    return getEmptyValueSet();
                }
            } else if (stringToBeParsed.equals(Messages.ValueSetFormat_unrestricted)) {
                return getUnrestrictedValueSet();
            }
            String[] split = stringToBeParsed.split("\\" + VALUESET_SEPARATOR); //$NON-NLS-1$
            List<String> parsedValues = parseValues(split);
            if (!isEqualContent(parsedValues)) {
                EnumValueSet enumValueSet = createNewEnumValueSet(parsedValues);
                return enumValueSet;
            }
        }
        return getValueSet();
    }

    private boolean isRangeAllowed() {
        return isAllowedValueSetType(ValueSetType.RANGE);
    }

    private boolean isUnrestrictedAllowed() {
        return isAllowedValueSetType(ValueSetType.UNRESTRICTED);
    }

    private boolean isEnumValueSetAllowed() {
        return isAllowedValueSetType(ValueSetType.ENUM);
    }

    private boolean isAllowedValueSetType(ValueSetType valueSetType) {
        try {
            List<ValueSetType> allowedValueSetTypes = this.configElement.getAllowedValueSetTypes(this.configElement
                    .getIpsProject());
            return allowedValueSetTypes.contains(valueSetType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IValueSet getUnrestrictedValueSet() {
        final IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            return valueSet;
        } else {
            UnrestrictedValueSet newValueSet = new UnrestrictedValueSet(configElement, getNextPartId(configElement));
            return newValueSet;
        }
    }

    private IValueSet getEmptyValueSet() {
        IValueSet valueSet = getValueSet();
        if (valueSet.isEnum() && ((IEnumValueSet)valueSet).getValuesAsList().isEmpty()) {
            return valueSet;
        } else {
            return createNewEnumValueSet(new ArrayList<String>());
        }
    }

    private List<String> parseValues(String[] split) {
        List<String> parseValues = new ArrayList<String>();
        for (String value : split) {
            parseValues.add(parseWithFormater(value.trim()));
        }
        return parseValues;
    }

    protected String parseWithFormater(String value) {
        IInputFormat<String> inputFormat = uiPlugin.getInputFormat(getDatatype());
        if (inputFormat == null) {
            return value;
        }
        return inputFormat.parse(value);
    }

    private ValueDatatype getDatatype() {
        try {
            return this.configElement.findValueDatatype(this.configElement.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isEqualContent(List<String> parsedValues) {
        return getValueSet() instanceof IEnumValueSet && getValuesAsList().equals(parsedValues);
    }

    private List<String> getValuesAsList() {
        return ((IEnumValueSet)getValueSet()).getValuesAsList();
    }

    private IValueSet getValueSet() {
        return this.configElement.getValueSet();
    }

    private EnumValueSet createNewEnumValueSet(List<String> values) {
        EnumValueSet valueSet = new EnumValueSet(configElement, values, getNextPartId(configElement));
        return valueSet;
    }

    private String getNextPartId(IIpsObjectPartContainer parent) {
        return parent.getIpsModel().getNextPartId(parent);
    }

    @Override
    protected String formatInternal(IValueSet valueSet) {
        return formatValueSet(valueSet);
    }

    /**
     * Utility method for formatting value sets without the corresponding config-element. Made
     * static so the {@link UIDatatypeFormatter} can use this method.
     * 
     * @param valueSet the valueset to format
     * @return a string representation of the value set.
     */
    public static String formatValueSet(IValueSet valueSet) {
        if (valueSet instanceof EnumValueSet) {
            EnumValueSet enumValueSet = (EnumValueSet)valueSet;
            ValueDatatype type = enumValueSet.getValueDatatype();
            StringBuffer buffer = new StringBuffer();
            for (String id : enumValueSet.getValues()) {
                String formatedEnumText = IpsUIPlugin.getDefault().getInputFormat(type).format(id);
                buffer.append(formatedEnumText);
                buffer.append(" " + ValueSetFormat.VALUESET_SEPARATOR + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (buffer.length() > 3) {
                /*
                 * Remove the separator after the last value (" | ")
                 */
                buffer.delete(buffer.length() - 3, buffer.length());
            }
            return buffer.toString();
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // do nothing
    }

    @Override
    protected void initFormat(Locale locale) {
        // do nothing
    }

}
