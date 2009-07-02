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

package org.faktorips.devtools.core;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
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
     * @throws CoreException 
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
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            return formatValue((EnumTypeDatatypeAdapter)datatype, value);
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
     * Formats the provided id according to the user preferences. If the id isn't an id that identifies
     * a value of the provided enum type or content the id will be returned unformatted. Also if the
     * enum type is not defined properly in the model the provided id value will be returned unformatted.
     *  
     * @param enumType the enum type that defines the enumeration in which the provided id identifies a value 
     * @param enumContent can be <code>null</code> and is only necessary if the enum type is one that doesn't contain values
     * @param id the identifies a value of the provided enum type 
     * @return the formatted value
     */
    private String formatValue(EnumTypeDatatypeAdapter datatypeAdapter, String id) {
        try {
            String idDisplayValue = getIdDisplayValue(datatypeAdapter, id);
            EnumTypeDisplay enumTypeDisplay = preferences.getEnumTypeDisplay();
            if (enumTypeDisplay.equals(EnumTypeDisplay.ID) && idDisplayValue == null) {
                return id;
            } else if (enumTypeDisplay.equals(EnumTypeDisplay.ID)) {
                return idDisplayValue;
            }
            String nameDisplayValue = getNameDisplayValue(datatypeAdapter, id);
            if (enumTypeDisplay.equals(EnumTypeDisplay.NAME) && nameDisplayValue == null) {
                return id;
            } else if (enumTypeDisplay.equals(EnumTypeDisplay.NAME)) {
                return nameDisplayValue;
            }
            if (enumTypeDisplay.equals(EnumTypeDisplay.NAME_AND_ID)
                    && (idDisplayValue != null || nameDisplayValue != null)) {
                return nameDisplayValue + " (" + idDisplayValue + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                return id;
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getIdDisplayValue(EnumTypeDatatypeAdapter datatypeAdapter, String id) throws CoreException{
        
        IEnumValue enumValue = getEnumValue(datatypeAdapter, id);
        if(enumValue == null){
            return null;
        }
        IEnumType enumType = datatypeAdapter.getEnumType();
        IEnumAttribute enumAttribute = enumType.findIsUsedAsIdInFaktorIpsUiAttribute(enumType.getIpsProject());
        if(enumAttribute == null){
            return null;
        }
        IEnumAttributeValue enumAttributeValue = enumValue.findEnumAttributeValue(enumType.getIpsProject(), enumAttribute);
        return enumAttributeValue.getValue();
    }

    private String getNameDisplayValue(EnumTypeDatatypeAdapter datatypeAdapter, String id) throws CoreException{
        
        IEnumValue enumValue = getEnumValue(datatypeAdapter, id);
        if(enumValue == null){
            return null;
        }
        IEnumType enumType = datatypeAdapter.getEnumType();
        IEnumAttribute enumAttribute = enumType.findIsUsedAsNameInFaktorIpsUiAttribute(enumType.getIpsProject());
        if(enumAttribute == null){
            return null;
        }
        IEnumAttributeValue enumAttributeValue = enumValue.findEnumAttributeValue(enumType.getIpsProject(), enumAttribute);
        return enumAttributeValue.getValue();
    }
    
    private IEnumValue getEnumValue(EnumTypeDatatypeAdapter datatypeAdapter, String id) throws CoreException {
        IEnumValueContainer valueContainer = datatypeAdapter.getEnumValueContainer();
        return valueContainer.findEnumValue(id, valueContainer.getIpsProject());
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
    private String formatValue(EnumDatatype datatype, String id) {
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
