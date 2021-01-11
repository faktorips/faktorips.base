/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
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
        for (String enumValueId : valueIds) {
            if (ObjectUtils.equals(enumValueId, getIdentifierPart())) {
                return nodeFactory().createEnumValueNode(enumValueId, enumType);
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
    public List<IdentifierProposal> getProposals(String prefix) {
        IdentifierProposalCollector collector = new IdentifierProposalCollector();
        if (isContextTypeFormulaType()) {
            addEnumClassProposals(prefix, collector);
        } else if (getContextType() instanceof EnumClass) {
            addEnumValueProposals(prefix, collector);
        }
        return collector.getProposals();
    }

    private void addEnumClassProposals(String prefix, IdentifierProposalCollector collector) {
        EnumDatatype[] enumDatatypesAllowedInFormula = getExpression().getEnumDatatypesAllowedInFormula();
        for (EnumDatatype enumDatatype : enumDatatypesAllowedInFormula) {
            collector.addMatchingNode(getText(enumDatatype), getDescription(enumDatatype), prefix,
                    IdentifierNodeType.ENUM_CLASS);
        }
    }

    private String getText(EnumDatatype enumDatatype) {
        return enumDatatype.getName();
    }

    protected String getDescription(EnumDatatype enumDatatype) {
        if (enumDatatype instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter enumTypeDatatypeAdapter = (EnumTypeDatatypeAdapter)enumDatatype;
            MultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
            return getNameAndDescription(enumTypeDatatypeAdapter.getEnumType(), multiLanguageSupport);
        }
        return NLS.bind(Messages.EnumParser_description, getText(enumDatatype));
    }

    private void addEnumValueProposals(String prefix, IdentifierProposalCollector collector) {
        EnumDatatype enumDatatype = ((EnumClass)getContextType()).getEnumDatatype();
        String[] valueIds = enumDatatype.getAllValueIds(false);
        for (String enumValueId : valueIds) {
            collector.addMatchingNode(enumValueId, getLabel(enumValueId, enumDatatype), StringUtils.EMPTY, prefix,
                    IdentifierNodeType.ENUM_VALUE);
        }
    }

    private String getLabel(String enumValueId, EnumDatatype enumDatatype) {
        return enumValueId + "(" + enumDatatype.getValueName(enumValueId) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
