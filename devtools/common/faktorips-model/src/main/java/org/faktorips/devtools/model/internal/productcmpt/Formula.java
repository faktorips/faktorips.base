/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class Formula extends Expression implements IFormula {

    private final TemplateValueSettings templateValueSettings;

    public Formula(IPropertyValueContainer parent, String id) {
        this(parent, id, ""); //$NON-NLS-1$
    }

    public Formula(IPropertyValueContainer parent, String id, String formulaSignature) {
        super(parent, id, formulaSignature);
        templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    public IPropertyValueContainer getTemplatedValueContainer() {
        return getPropertyValueContainer();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(Expression.TAG_NAME);
    }

    @Override
    public String getName() {
        return getFormulaSignature();
    }

    @Override
    public String getPropertyName() {
        return getFormulaSignature();
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) {
        return findFormulaSignature(ipsProject);
    }

    @Override
    public PropertyValueType getPropertyValueType() {
        return PropertyValueType.FORMULA;
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return getPropertyValueType().getCorrespondingPropertyType();
    }

    @Override
    public String getPropertyValue() {
        return getExpression();
    }

    /**
     * Returns the formula expression. Note that this overrides the method
     * {@link Expression#getExpression()} and returns the formula from this formula's template if
     * applicable.
     */
    @Override
    public String getExpression() {
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateExpression();
        }

        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return ""; //$NON-NLS-1$
        }

        return super.getExpression();
    }

    private String findTemplateExpression() {
        IFormula templateFormula = findTemplateProperty(getIpsProject());
        if (templateFormula == null) {
            // Template should exist but does not. Use the "last known" value as a more or less
            // helpful fallback while some validation hopefully addresses the missing template...
            return super.getExpression();
        }
        return templateFormula.getExpression();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return getPropertyValueContainer().findProductCmptType(ipsProject);
    }

    @Override
    public List<IAttribute> findMatchingProductCmptTypeAttributes() {
        List<IAttribute> allAttributes = super.findMatchingProductCmptTypeAttributes();
        if (!getPropertyValueContainer().isChangingOverTimeContainer()) {
            List<IAttribute> notChangingOverTimeAttributes = new ArrayList<>();
            for (IAttribute attribute : allAttributes) {
                if (!((ProductCmptTypeAttribute)attribute).isChangingOverTime()) {
                    notChangingOverTimeAttributes.add(attribute);
                }
            }
            return notChangingOverTimeAttributes;
        } else {
            return allAttributes;
        }
    }

    @Override
    protected ITableContentUsage[] getTableContentUsages() {
        List<ITableContentUsage> usages = getPropertyValueContainer().getPropertyValues(ITableContentUsage.class);
        if (getPropertyValueContainer() instanceof IProductCmptGeneration) {
            usages.addAll(getPropertyValueContainer().getProductCmpt().getPropertyValues(ITableContentUsage.class));
        }
        return usages.toArray(new ITableContentUsage[usages.size()]);
    }

    @Override
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject) {
        if (IpsStringUtils.isEmpty(getFormulaSignature())) {
            return null;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findFormulaSignature(getFormulaSignature(), ipsProject);
    }

    @Override
    public boolean isFormulaMandatory() {
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return false;
        }
        IProductCmptTypeMethod formulaSignature = findFormulaSignature(getIpsProject());
        if (formulaSignature != null) {
            return formulaSignature.isFormulaMandatory();
        }
        return true;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        templateValueSettings.propertiesToXml(element);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        list.add(templateValueSettings.validate(this, ipsProject));
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        if (newStatus == TemplateValueStatus.DEFINED) {
            // Copy current expression from template (if present)
            setExpressionInternal(getExpression());
        }
        TemplateValueStatus oldStatus = templateValueSettings.getStatus();
        templateValueSettings.setStatus(newStatus);
        objectHasChanged(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldStatus, newStatus));
    }

    @Override
    public TemplateValueStatus getTemplateValueStatus() {
        return templateValueSettings.getStatus();
    }

    @Override
    public void switchTemplateValueStatus() {
        setTemplateValueStatus(getTemplateValueStatus().getNextStatus(this));
    }

    @Override
    public IFormula findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, IFormula.class);
    }

    @Override
    public boolean hasTemplateForProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.hasTemplateForValue(this, IFormula.class);
    }

    @Override
    public boolean isPartOfTemplateHierarchy() {
        return (getTemplatedValueContainer().isUsingTemplate() && findTemplateProperty(getIpsProject()) != null)
                || getTemplatedValueContainer().isProductTemplate();
    }

    @Override
    public Comparator<Object> getValueComparator() {
        return getPropertyValueType().getValueComparator();
    }

    @Override
    public Function<IPropertyValue, Object> getValueGetter() {
        return getPropertyValueType().getValueGetter();
    }

    @Override
    public BiConsumer<IPropertyValue, Object> getValueSetter() {
        return getPropertyValueType().getValueSetter();
    }

    @Override
    public ITemplatedValueIdentifier getIdentifier() {
        return new PropertyValueIdentifier(this);
    }

    @Override
    public boolean isConcreteValue() {
        return getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }
}
