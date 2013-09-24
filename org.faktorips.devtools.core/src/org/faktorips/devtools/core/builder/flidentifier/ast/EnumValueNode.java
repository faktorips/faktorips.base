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

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;

/**
 * The enum value node represents the value part of an enum access. It have to follow an
 * {@link EnumClassNode}. The resulting {@link Datatype} will be a {@link EnumDatatype}.
 * 
 * @author dirmeier
 */
public class EnumValueNode extends IdentifierNode {

    private final String enumValueName;

    EnumValueNode(String enumValueName, EnumDatatype datatype) {
        super(datatype);
        this.enumValueName = enumValueName;
    }

    public String getEnumValueName() {
        return enumValueName;
    }

    @Override
    public EnumDatatype getDatatype() {
        return (EnumDatatype)super.getDatatype();
    }

}
