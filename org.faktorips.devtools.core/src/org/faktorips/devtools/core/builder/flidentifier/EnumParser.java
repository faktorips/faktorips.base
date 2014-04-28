/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * The parser for enum value identifiers.
 * <p>
 * An enum identifier always consists of two parts. First the enum class name and second the id of
 * the enum value. This parser is able to handle both parts, it depends on the context type which
 * part it tries to parse.
 * 
 * @author dirmeier
 */
public class EnumParser extends AbstractIdentifierNodeParser {

    private final Map<String, EnumDatatype> enumDatatypes;

    /**
     * Creates a new {@link EnumParser} for the specified expression and project
     */
    public EnumParser(ParsingContext parsingContext) {
        super(parsingContext);
        enumDatatypes = createEnumMap();
    }

    private Map<String, EnumDatatype> createEnumMap() {
        EnumDatatype[] enumtypes = getExpression().getEnumDatatypesAllowedInFormula();
        Map<String, EnumDatatype> newEnumDatatypes = new HashMap<String, EnumDatatype>(enumtypes.length);
        for (EnumDatatype enumtype : enumtypes) {
            newEnumDatatypes.put(enumtype.getName(), enumtype);
        }
        return newEnumDatatypes;
    }

    @Override
    protected IdentifierNode parse() {
        if (isContextTypeFormulaType()) {
            return parseEnumClass();
        }
        if (isAllowedType() && getContextType() instanceof EnumClass) {
            return parseEnumDatatype();
        }
        return null;
    }

    private IdentifierNode parseEnumClass() {
        EnumDatatype enumType = enumDatatypes.get(getIdentifierPart());
        if (enumType != null) {
            return nodeFactory().createEnumClassNode(new EnumClass(enumType));
        }
        return null;
    }

    private IdentifierNode parseEnumDatatype() {
        EnumDatatype enumType = ((EnumClass)getContextType()).getEnumDatatype();
        String[] valueIds = enumType.getAllValueIds(true);
        for (String enumValueName : valueIds) {
            if (ObjectUtils.equals(enumValueName, getIdentifierPart())) {
                return nodeFactory().createEnumValueNode(enumValueName, enumType);
            }
        }
        return nodeFactory()
                .createInvalidIdentifier(
                        Message.newError(
                                ExprCompiler.UNDEFINED_IDENTIFIER,
                                NLS.bind(Messages.EnumParser_msgErrorInvalidEnumValue, getIdentifierPart(),
                                        enumType.getName())));
    }

    @Override
    public List<IdentifierNode> getProposals(String prefix) {
        ArrayList<IdentifierNode> result = new ArrayList<IdentifierNode>();

        EnumDatatype[] enumDatatypesAllowedInFormula = getExpression().getEnumDatatypesAllowedInFormula();
        for (EnumDatatype enumDatatype : enumDatatypesAllowedInFormula) {
            addNodeIfPrefixEnumValueOrType(prefix, result, enumDatatype);
        }
        return result;
    }

    private void addNodeIfPrefixEnumValueOrType(String prefix,
            ArrayList<IdentifierNode> result,
            EnumDatatype enumDatatype) {
        if (enumDatatype instanceof EnumTypeDatatypeAdapter) {
            IEnumType enumType = ((EnumTypeDatatypeAdapter)enumDatatype).getEnumType();
            if (StringUtils.startsWithIgnoreCase(enumType.getName(), prefix)) {
                result.add(nodeFactory().createEnumClassNode(new EnumClass(enumDatatype)));
            } else {
                addNodeIfPrefixIsEnumValue(prefix, result, enumDatatype, enumType);
            }
        }
    }

    private void addNodeIfPrefixIsEnumValue(String prefix,
            ArrayList<IdentifierNode> result,
            EnumDatatype enumDatatype,
            IEnumType enumType) {
        List<IEnumValue> enumValues = enumType.getEnumValues();
        for (IEnumValue enumValue : enumValues) {
            if (StringUtils.startsWithIgnoreCase(enumValue.getName(), prefix)) {
                result.add(nodeFactory().createEnumValueNode(enumValue.getName(), enumDatatype));
            }
        }
    }
}
