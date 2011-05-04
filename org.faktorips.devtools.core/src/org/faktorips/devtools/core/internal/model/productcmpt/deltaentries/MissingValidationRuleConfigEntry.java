/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;

/**
 * DeltaEntry for an {@link IValidationRule} that is not configured by an
 * {@link IValidationRuleConfig} on product-side.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class MissingValidationRuleConfigEntry extends AbstractDeltaEntry {

    private final IValidationRule validationRule;

    public MissingValidationRuleConfigEntry(GenerationToTypeDelta delta, IValidationRule rule) {
        super(delta);
        this.validationRule = rule;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_VALIDATION_RULE_CONFIG;
    }

    @Override
    public String getDescription() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(validationRule);
    }

    @Override
    public void fix() {
        getGeneration().newValidationRuleConfig(validationRule);
    }

    /**
     * @return the validation rule referenced by this delta entry.
     */
    public IValidationRule getValidationRule() {
        return validationRule;
    }

    @Override
    public String toString() {
        return getDeltaType().getDescription() + " (" + validationRule.getName() + ")"; //$NON-NLS-1$//$NON-NLS-2$
    }

}
