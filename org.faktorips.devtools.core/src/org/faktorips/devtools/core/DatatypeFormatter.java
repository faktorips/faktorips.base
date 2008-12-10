/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.util.ArgumentCheck;

public class DatatypeFormatter {
    
    private IpsPreferences preferences;
    
    public DatatypeFormatter(IpsPreferences ipsPreferences){
        ArgumentCheck.notNull(ipsPreferences, this);
        this.preferences = ipsPreferences;
    }
    
    /**
     * Formats the given value according to the user preferences.
     *  
     * @param datatype The datatype the value is a value of.
     * @param value The value as string
     * @return
     * 
     * @see #ENUM_TYPE_DISPLAY
     * @see #NULL_REPRESENTATION_STRING
     */
    public String formatValue(ValueDatatype datatype, String value) {
        if (value==null) {
            return preferences.getNullPresentation();
        }
        if (datatype==null) {
            return value;
        }
        if (datatype instanceof EnumDatatype) {
            return formatValue((EnumDatatype)datatype, value);
        }
        if (datatype instanceof BooleanDatatype || datatype instanceof PrimitiveBooleanDatatype) {
            if (Boolean.valueOf(value).booleanValue()) {
                return Messages.DatatypeFormatter_booleanTrue;
            }
            return Messages.DatatypeFormatter_booleanFalse;
        }
        return value;
    }
    
    /**
     * Formats the given value according to the user preferences.
     *  
     * @param datatype The datatype the value is a value of.
     * @param value The value as string
     * @return
     * 
     * @see #ENUM_TYPE_DISPLAY
     * @see #NULL_REPRESENTATION_STRING
     */
    public String formatValue(EnumDatatype datatype, String id) {
        if (id==null) {
            return preferences.getNullPresentation();
        }
        if (datatype==null) {
            return id;
        }
        if (!datatype.isSupportingNames()) {
            return id;
        }
        EnumTypeDisplay enumTypeDisplay = preferences.getEnumTypeDisplay();
        if (enumTypeDisplay.equals(EnumTypeDisplay.ID)) {
            return id;
        }
        if (!datatype.isParsable(id)) {
            return id;
        }
        String name = datatype.getValueName(id);
        if (enumTypeDisplay.equals(EnumTypeDisplay.NAME_AND_ID)){
            return name + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return name;
        } 
    }
    
    /**
     * Returns the to be displayed text of an enumeration. 
     * The property ENUM_TYPE_DISPLAY specifies how the name and id will be formated.
     * E.g. display only id or only name, or display both.
     */
    public String getFormatedEnumText(String id, String name){
        EnumTypeDisplay enumTypeDisplay = preferences.getEnumTypeDisplay();
        if (enumTypeDisplay.equals(EnumTypeDisplay.NAME_AND_ID)){
            return name + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (enumTypeDisplay.equals(EnumTypeDisplay.NAME)){
            return name;
        } else {
            return id;
        }
    }
    
    public String getBooleanTrueDisplay(){
        return Messages.DatatypeFormatter_booleanTrue;
    }

    public String getBooleanFalseDisplay(){
        return Messages.DatatypeFormatter_booleanFalse;
    }
}
