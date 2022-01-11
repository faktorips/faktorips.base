/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType;

public class AllowedValuesForAttributeRule {

    private static final Map<ValueSetMethods, Map<ValueSetMethods, List<GenerateValueSetTypeRule>>> RULES = new HashMap<>();
    // @formatter:off
    static {
        RULES.put(ValueSetMethods.Unified,
                Map.of(ValueSetMethods.Unified,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, true, false, false)),
                        ValueSetMethods.ByValueSetType,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, false, false, false),
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, true, false, true)),
                        ValueSetMethods.Both,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, true, false, false),
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, true, true, true))));

        RULES.put(ValueSetMethods.ByValueSetType,
                Map.of(
                        ValueSetMethods.Unified,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, false, false, false),
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, true, false, true)),
                        ValueSetMethods.ByValueSetType,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, true, false, false)),
                        ValueSetMethods.Both,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, true, false, false))));

        RULES.put(ValueSetMethods.Both,
                Map.of(ValueSetMethods.Unified,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, true, false, true),
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, false, true, false)),
                        ValueSetMethods.ByValueSetType,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, false, false, true),
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, true, true, false)),
                        ValueSetMethods.Both,
                        List.of(
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_UNIFIED, true, false, true),
                                new GenerateValueSetTypeRule(GenerateValueSetType.GENERATE_BY_TYPE, true, true, false))));
    }
    // @formatter:on

    private AllowedValuesForAttributeRule() {
        // Util Class
    }

    public static List<GenerateValueSetTypeRule> getGenerateValueSetTypeRulesFor(XPolicyAttribute attribute) {

        ValueSetMethods thisSetting = attribute.getContext().getBaseGeneratorConfig().getValueSetMethods();
        ValueSetMethods superSetting;

        if (!attribute.isOverwrite()) {
            superSetting = thisSetting;
        } else {
            superSetting = getGeneratorConfigFromSuperType(attribute);
        }

        return RULES.get(thisSetting).get(superSetting);
    }

    private static ValueSetMethods getGeneratorConfigFromSuperType(XPolicyAttribute attribute) {
        return attribute.getContext()
                .getGeneratorConfig(attribute.getOverwrittenAttribute().getAttribute().getIpsObject())
                .getValueSetMethods();
    }
}
