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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ValueSetFormat extends AbstractInputFormat<IValueSet> {

    private static final String VALUESET_SEPARATOR = "|"; //$NON-NLS-1$

    private final IConfigElement configElement;

    public static ValueSetFormat newInstance(IConfigElement configElement) {
        ValueSetFormat format = new ValueSetFormat(configElement);
        format.initFormat();
        return format;
    }

    public ValueSetFormat(IConfigElement configElement) {
        this.configElement = configElement;
    }

    private IValueSet getValueSet() {
        return this.configElement.getValueSet();
    }

    private ValueDatatype getDatatype() {
        try {
            return this.configElement.findValueDatatype(this.configElement.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isValueSetUnrestricted() {
        try {
            List<ValueSetType> allowedValueSetTypes = this.configElement.getAllowedValueSetTypes(this.configElement
                    .getIpsProject());
            return allowedValueSetTypes.contains(ValueSetType.UNRESTRICTED);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected IValueSet parseInternal(String stringToBeParsed) {
        if (getValueSet() instanceof EnumValueSet) {
            if (stringToBeParsed.isEmpty()) {
                if (isValueSetUnrestricted()) {
                    return createNewUnrestrictedValueSet();
                } else {
                    return createNewEmptyEnumValueSet();
                }
            } else if (stringToBeParsed.contains("<unrestriced>")) { //$NON-NLS-1$
                return createNewUnrestrictedValueSet();
            }
            String[] split = stringToBeParsed.trim().split("\\" + VALUESET_SEPARATOR); //$NON-NLS-1$
            // neues ValueSet anlegen
            EnumValueSet enumValueSet = createNewEmptyEnumValueSet();
            for (String value : split) {
                // Formater holen und parse aufrufen
                // rückgabewert ist neues Value
                enumValueSet.addValueWithoutTriggeringChangeEvent(parseWithFormater(value));
            }
            return enumValueSet;
        }
        return getValueSet();
    }

    private String parseWithFormater(String value) {
        // Datatype and value an Formater
        return value.trim();
    }

    private EnumValueSet createNewEmptyEnumValueSet() {
        EnumValueSet valueSet = new EnumValueSet((IIpsObjectPart)getValueSet().getParent(), getValueSet().getId());
        return valueSet;
    }

    private UnrestrictedValueSet createNewUnrestrictedValueSet() {
        UnrestrictedValueSet newValueSet = new UnrestrictedValueSet((IIpsObjectPart)getValueSet().getParent(),
                getValueSet().getId());
        return newValueSet;
    }

    @Override
    protected String formatInternal(IValueSet value) {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValueSet(value);
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {

    }

    @Override
    protected void initFormat(Locale locale) {
        // TODO Auto-generated method stub

    }

}
