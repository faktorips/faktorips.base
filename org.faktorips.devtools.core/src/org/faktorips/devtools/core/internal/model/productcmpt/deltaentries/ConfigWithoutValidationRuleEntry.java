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
 * 
 * DeltaEntry for an {@link IValidationRuleConfig} that does not configure an
 * {@link IValidationRule} from policy-side.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ConfigWithoutValidationRuleEntry extends AbstractDeltaEntry {

    private final IValidationRuleConfig vRuleConfig;

    public ConfigWithoutValidationRuleEntry(GenerationToTypeDelta delta, IValidationRuleConfig config) {
        super(delta);
        this.vRuleConfig = config;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.CONFIG_WITHOUT_VALIDATION_RULE;
    }

    @Override
    public String getDescription() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(vRuleConfig);
    }

    @Override
    public void fix() {
        vRuleConfig.delete();
    }

    /**
     * @return the validation rule config referenced by this delta entry.
     */
    public IValidationRuleConfig getValidationRuleConfig() {
        return vRuleConfig;
    }

    @Override
    public String toString() {
        return getDeltaType().getDescription() + " (" + vRuleConfig.getName() + ")"; //$NON-NLS-1$//$NON-NLS-2$
    }
}
