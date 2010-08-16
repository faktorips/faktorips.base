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

package org.faktorips.devtools.core.model.valueset;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.TimedEnumDatatype;
import org.faktorips.datatype.TimedEnumDatatypeUtil;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides methods that filters invalid values in a given period, if a value set is based on a
 * {@link TimedEnumDatatype}.
 * 
 * @author Jan Ortmann
 */
public class ValueSetFilter {

    /**
     * If the given datatype is a time based enum datatype and the given value set is either
     * <code>null</code> or an enum value set, then this method creates a new enum value set that
     * contains only those values that are valid in the given period, see {link
     * {@link #createEnumValueSet(TimedEnumDatatype, GregorianCalendar, GregorianCalendar, org.faktorips.datatype.TimedEnumDatatypeUtil.ValidityCheck)}
     * for details. In all other cases, no new set is created, the sourceSet is returned unmodified.
     * 
     * @param sourceSet The value set that contains the possible values.
     * @param datatype The datatype.
     * @param begin Begin of the period.
     * @param end End of the period.
     * @param check Flag whether value are included if they are valid in the whole period or some
     *            part of the period.
     */
    public static final IValueSet filterValueSet(IValueSet sourceSet,
            ValueDatatype datatype,
            GregorianCalendar begin,
            GregorianCalendar end,
            TimedEnumDatatypeUtil.ValidityCheck check) {

        if (datatype instanceof TimedEnumDatatype) {
            if (sourceSet == null || sourceSet.isUnrestricted()) {
                return createEnumValueSet((TimedEnumDatatype)datatype, begin, end, check);
            }
            if (sourceSet.isEnum()) {
                return createEnumValueSet((IEnumValueSet)sourceSet, (TimedEnumDatatype)datatype, begin, end, check);
            }
        }
        return sourceSet;
    }

    /**
     * Creates an enum value set based on a given enum value set or, if the set is null, the given
     * timed enum datatype. The returned set contains all values that are valid in the given period
     * (defined by beginn, end).
     * 
     * @param sourceSet The enum value set that contains the possible values.
     * @param datatype The timed enum datatype.
     * @param begin Begin of the period.
     * @param end End of the period.
     * @param check Flag whether value are included if they are valid in the whole period or some
     *            part of the period.
     */
    public static final IEnumValueSet createEnumValueSet(IEnumValueSet sourceSet,
            TimedEnumDatatype datatype,
            GregorianCalendar begin,
            GregorianCalendar end,
            TimedEnumDatatypeUtil.ValidityCheck check) {

        if (sourceSet == null) {
            return createEnumValueSet(datatype, begin, end, check);
        }
        EnumValueSet newSet = new EnumValueSet(null, "TemporaryValueSet"); //$NON-NLS-1$
        String[] values = sourceSet.getValues();
        for (int i = 0; i < values.length; i++) {
            if (TimedEnumDatatypeUtil.isValid(datatype, values[i], begin, end, check)) {
                newSet.addValueWithoutTriggeringChangeEvent(values[i]);
            }
        }
        return newSet;
    }

    /**
     * Creates an enum value set based on a given timed enum datatype. The returned set contains all
     * values that are valid in the given period (defined by beginn, end).
     * 
     * @param datatype The timed enum datatype.
     * @param begin Begin of the period.
     * @param end End of the period.
     * @param check Flag whether value are included if they are valid in the whole period or some
     *            part of the period.
     */
    public static final IEnumValueSet createEnumValueSet(TimedEnumDatatype datatype,
            GregorianCalendar begin,
            GregorianCalendar end,
            TimedEnumDatatypeUtil.ValidityCheck check) {

        InternalValueSetOwner owner = new InternalValueSetOwner(datatype);
        EnumValueSet newSet = new EnumValueSet(owner, "TemporaryValueSet"); //$NON-NLS-1$
        owner.valueSet = newSet;
        String[] values = datatype.getAllValueIds(true);
        for (int i = 0; i < values.length; i++) {
            if (TimedEnumDatatypeUtil.isValid(datatype, values[i], begin, end, check)) {
                newSet.addValueWithoutTriggeringChangeEvent(values[i]);
            }
        }
        return newSet;
    }

    private static class InternalValueSetOwner extends IpsObjectPart implements IValueSetOwner {

        private ValueDatatype datatype;
        private IValueSet valueSet;

        public InternalValueSetOwner(ValueDatatype datatype) {
            super();
            this.datatype = datatype;
        }

        @Override
        public IValueSet changeValueSetType(ValueSetType newType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException {
            return datatype;
        }

        @Override
        public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
            List<ValueSetType> types = new ArrayList<ValueSetType>(1);
            types.add(ValueSetType.ENUM);
            return types;
        }

        @Override
        public IValueSet getValueSet() {
            return valueSet;
        }

        @Override
        public boolean isValueSetUpdateable() {
            return false;
        }

        @Override
        public void setValueSetType(ValueSetType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Element createElement(Document doc) {
            throw new UnsupportedOperationException();
        }

    }
}
