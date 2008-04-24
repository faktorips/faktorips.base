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
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;


/**
 * Enum class, to specify the target Java version for generated source files.
 * <ul>
 * <li>JAVA_1_4 - Java 1.4
 * <li>JAVA_5 - Java 5.0
 * </ul>
 * @author Daniel Hohenberger
 */
public class EnumTypeTargetJavaVersion extends DefaultEnumValue{
    
    public final static EnumTypeTargetJavaVersion JAVA_1_4;
    
    public final static EnumTypeTargetJavaVersion JAVA_5;
    
    public final static EnumTypeTargetJavaVersion DEFAULT;
    
    private final static DefaultEnumType enumType;
    
    private final int version;

    static {
        enumType = new DefaultEnumType("EnumTypeTargetJavaVersion", EnumTypeTargetJavaVersion.class); //$NON-NLS-1$
        JAVA_1_4 = new EnumTypeTargetJavaVersion(enumType, "1.4", 4); //$NON-NLS-1$ //$NON-NLS-2$
        JAVA_5 = new EnumTypeTargetJavaVersion(enumType, "5.0", 5); //$NON-NLS-1$ //$NON-NLS-2$
        
        DEFAULT = JAVA_1_4;
    }

    private EnumTypeTargetJavaVersion(DefaultEnumType type, String id, int version){
        super(type, id);
        this.version = version; 
    }
    
    public static EnumTypeTargetJavaVersion valueOf(final String value){
        if(JAVA_5.getId().equals(value)){
            return JAVA_5;
        }
        if(JAVA_1_4.getId().equals(value)){
            return JAVA_1_4;
        }
        return DEFAULT;
    }

    public boolean isAtLeast(EnumTypeTargetJavaVersion target) {
        return this.version >= target.version;
    }
}
