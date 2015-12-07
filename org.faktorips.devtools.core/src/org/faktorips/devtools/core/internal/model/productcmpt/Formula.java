/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.message.MessageList;
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
        this.templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
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
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findFormulaSignature(ipsProject);
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION;
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

    /**
     * Returns the generation this formula belongs to.
     * 
     * @deprecated As of 3.14 a {@link Formula} can be part of both {@link IProductCmpt product
     *             components} and {@link ProductCmptGeneration product component generations}. Use
     *             {@link #getPropertyValueContainer()} and the common interface
     *             {@link IPropertyValueContainer} instead.
     */
    @Deprecated
    @Override
    public IProductCmptGeneration getProductCmptGeneration() {
        if (getPropertyValueContainer() instanceof IProductCmptGeneration) {
            return (ProductCmptGeneration)getPropertyValueContainer();
        }
        return null;
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        try {
            return getPropertyValueContainer().findProductCmptType(ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<IAttribute> findMatchingProductCmptTypeAttributes() {
        List<IAttribute> allAttributes = super.findMatchingProductCmptTypeAttributes();
        if (!getPropertyValueContainer().isChangingOverTimeContainer()) {
            List<IAttribute> notChangingOverTimeAttributes = new ArrayList<IAttribute>();
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
    protected void collectEnumTypesFromAttributes(Map<String, EnumDatatype> enumTypes) {
        try {
            IIpsProject ipsProject = getIpsProject();
            IProductCmptType productCmptType = findProductCmptType(ipsProject);
            if (productCmptType != null) {
                List<IAttribute> attributes = productCmptType.findAllAttributes(ipsProject);
                for (IAttribute attribute : attributes) {
                    Datatype datatype = attribute.findDatatype(ipsProject);
                    if (datatype instanceof EnumDatatype) {
                        enumTypes.put(datatype.getName(), (EnumDatatype)datatype);
                    }
                }
            }
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject) {
        if (StringUtils.isEmpty(getFormulaSignature())) {
            return null;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        try {
            return type.findFormulaSignature(getFormulaSignature(), ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
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
        return TemplatePropertyFinder.findTemplatePropertyValue(this, IFormula.class);
    }

    @Override
    public boolean isConfiguringTemplateValueStatus() {
        return getPropertyValueContainer().isProductTemplate() || getPropertyValueContainer().isUsingTemplate();
    }

}
