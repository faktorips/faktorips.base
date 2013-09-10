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

package org.faktorips.devtools.core.builder.flidentifier;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

public class EnumParser extends AbstractIdentifierNodeParser {

    private final Map<String, EnumDatatype> enumDatatypes;

    public EnumParser(IExpression expression, IIpsProject ipsProject) {
        super(expression, ipsProject);
        enumDatatypes = createEnumMap();
    }

    private Map<String, EnumDatatype> createEnumMap() {
        EnumDatatype[] enumtypes = getExpression().getEnumDatatypesAllowedInFormula();
        Map<String, EnumDatatype> enumDatatypes = new HashMap<String, EnumDatatype>(enumtypes.length);
        for (EnumDatatype enumtype : enumtypes) {
            enumDatatypes.put(enumtype.getName(), enumtype);
        }
        return enumDatatypes;
    }

    @Override
    protected IdentifierNode parse() {
        if (isContextTypeFormulaType()) {
            return parseEnumClass();
        }
        if (getContextType() instanceof EnumClass) {
            return parseEnumDatatype();
        }
        return null;
    }

    private EnumClassNode parseEnumClass() {
        EnumDatatype enumType = enumDatatypes.get(getIdentifierPart());
        if (enumType != null) {
            return new EnumClassNode(new EnumClass(enumType));
        }
        return null;
    }

    private EnumValueNode parseEnumDatatype() {
        EnumDatatype enumType = ((EnumClass)getContextType()).getEnumDatatype();
        String[] valueIds = enumType.getAllValueIds(true);
        for (String enumValueName : valueIds) {
            if (ObjectUtils.equals(enumValueName, getIdentifierPart())) {
                return new EnumValueNode(enumValueName, enumType);
            }
        }
        return null;
    }

}
