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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ValidationRuleConfig extends AtomicIpsObjectPart implements IValidationRuleConfig {

    public static final String TAG_NAME = "ValidationRuleConfig"; //$NON-NLS-1$

    public static final String TAG_NAME_ACTIVE = "active"; //$NON-NLS-1$
    public static final String TAG_NAME_RULE_NAME = "ruleName"; //$NON-NLS-1$

    private boolean isActive = false;

    private String ruleName;

    public ValidationRuleConfig(IPropertyValueContainer parent, String id, String ruleName) {
        super(parent, id);
        this.ruleName = ruleName;
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        ruleName = element.getAttribute(TAG_NAME_RULE_NAME);
        isActive = Boolean.valueOf(element.getAttribute(TAG_NAME_ACTIVE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(TAG_NAME_RULE_NAME, ruleName);
        element.setAttribute(TAG_NAME_ACTIVE, Boolean.toString(isActive));
    }

    @Override
    public IValidationRule findValidationRule(IIpsProject ipsProject) throws CoreException {
        IProductCmptGeneration generation = (IProductCmptGeneration)getParent();
        IProductCmpt component = (IProductCmpt)generation.getParent();
        IPolicyCmptType pcType = component.findPolicyCmptType(ipsProject);
        if (pcType != null) {
            IValidationRule rule = pcType.findValidationRule(ruleName, ipsProject);
            return rule;
        }
        return null;
    }

    /**
     * @return the name of this {@link IValidationRuleConfig}. That name is also the name of the
     *         configured {@link IValidationRule}.
     */
    @Override
    public String getName() {
        return ruleName;
    }

    @Override
    public void setActive(boolean active) {
        boolean oldValue = isActive;
        isActive = active;
        valueChanged(oldValue, active);
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IValidationRule rule = findValidationRule(getIpsProject());
        if (rule != null) {
            caption = rule.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(ruleName);
    }

    @Override
    public String getPropertyName() {
        return getName();
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findValidationRule(ipsProject);
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return ProductCmptPropertyType.VALIDATION_RULE;
    }

    @Override
    public String getPropertyValue() {
        return Boolean.toString(isActive());
    }
}
