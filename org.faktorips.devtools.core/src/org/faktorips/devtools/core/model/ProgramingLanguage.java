/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * Typesafe enum for the programing languages.
 * 
 * @author Jan Ortmann
 */
public class ProgramingLanguage extends DefaultEnumValue {

    public final static ProgramingLanguage FAKTORIPS_FORMULA; 
    
    public final static ProgramingLanguage JAVA; 

    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ProgramingLanguage", ProgramingLanguage.class); //$NON-NLS-1$
        FAKTORIPS_FORMULA = new ProgramingLanguage(enumType, "fips-formula", "Formula language");
        JAVA = new ProgramingLanguage(enumType, "java", "Java");
    }
    
    public final static ProgramingLanguage getLanguage(String id) {
        return (ProgramingLanguage)enumType.getEnumValue(id);
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    private ProgramingLanguage(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

}
