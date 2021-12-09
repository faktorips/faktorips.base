/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.method.Method;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeMethod extends Method implements IProductCmptTypeMethod {

    private static final String XML_FORMULA_MANDATORY = "mandatory"; //$NON-NLS-1$

    private boolean formulaSignatureDefinition = true;

    private boolean overloadsFormula = false;

    private boolean formulaMandatory = true;

    private String formulaName = StringUtils.EMPTY;

    /** Flag indicating if this is static */
    private boolean changingOverTime = getProductCmptType().isChangingOverTime();

    public ProductCmptTypeMethod(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public boolean isFormulaSignatureDefinition() {
        return formulaSignatureDefinition;
    }

    @Override
    public void setFormulaSignatureDefinition(boolean newValue) {
        boolean oldValue = formulaSignatureDefinition;
        formulaSignatureDefinition = newValue;
        if (!formulaSignatureDefinition) {
            setFormulaName(StringUtils.EMPTY);
            overloadsFormula = false;
        } else {
            setAbstract(false);
        }
        valueChanged(oldValue, newValue);
    }

    @Override
    public void setOverloadsFormula(boolean enabled) {
        boolean oldValue = overloadsFormula;
        overloadsFormula = enabled;
        valueChanged(oldValue, enabled);
    }

    @Override
    public boolean isOverloadsFormula() {
        return overloadsFormula;
    }

    @Override
    public IProductCmptTypeMethod findOverloadedFormulaMethod(IIpsProject ipsProject) {
        if (!isOverloadsFormula()) {
            return null;
        }
        FormulaNameFinder finder = new FormulaNameFinder(ipsProject);
        finder.start((IProductCmptType)getProductCmptType().findSupertype(ipsProject));
        return finder.method;
    }

    @Override
    public String getDefaultMethodName() {
        if (isFormulaSignatureDefinition()) {
            return "compute" + StringUtils.capitalize(getFormulaName()); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        if (element.hasAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION)) {
            formulaSignatureDefinition = Boolean.valueOf(element.getAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION))
                    .booleanValue();
        }
        overloadsFormula = Boolean.valueOf(element.getAttribute(PROPERTY_OVERLOADS_FORMULA));
        String mandatoryXml = element.getAttribute(XML_FORMULA_MANDATORY);
        formulaMandatory = StringUtils.isEmpty(mandatoryXml) ? true : Boolean.valueOf(mandatoryXml);
        formulaName = element.getAttribute(PROPERTY_FORMULA_NAME);
        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            changingOverTime = ValueToXmlHelper.isAttributeTrue(element, PROPERTY_CHANGING_OVER_TIME);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION, String.valueOf(formulaSignatureDefinition));
        element.setAttribute(PROPERTY_OVERLOADS_FORMULA, String.valueOf(overloadsFormula));
        element.setAttribute(XML_FORMULA_MANDATORY, String.valueOf(formulaMandatory));
        element.setAttribute(PROPERTY_FORMULA_NAME, String.valueOf(formulaName));
        element.setAttribute(PROPERTY_CHANGING_OVER_TIME, String.valueOf(changingOverTime));
    }

    @Override
    public String getPropertyName() {
        if (formulaSignatureDefinition) {
            return getFormulaName();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        if (formulaSignatureDefinition) {
            return ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION;
        }
        return null;
    }

    @Override
    public List<PropertyValueType> getPropertyValueTypes() {
        return Arrays.asList(PropertyValueType.FORMULA);
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreRuntimeException {
        super.validateThis(result, ipsProject);
        validateChangingOverTime(result, ipsProject);
        if (!isFormulaSignatureDefinition()) {
            return;
        }
        if (StringUtils.isEmpty(getFormulaName())) {
            String text = Messages.ProductCmptTypeMethod_FormulaNameIsMissing;
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY, text, Message.ERROR, this,
                    IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
        }
        Datatype datatype = findDatatype(ipsProject);
        if (datatype != null) {
            if (datatype.isVoid() || !datatype.isValueDatatype()) {
                String text = Messages.ProductCmptTypeMethod_FormulaSignatureDatatypeMustBeAValueDatatype;
                result.add(new Message(
                        IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES, text,
                        Message.ERROR, this, IMethod.PROPERTY_DATATYPE));
            }
        }
        if (isAbstract()) {
            String text = Messages.ProductCmptTypeMethod_FormulaSignatureMustntBeAbstract;
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT, text, Message.ERROR,
                    this, IMethod.PROPERTY_ABSTRACT));
        }
        validateOverloadedFormulaSignature(result, ipsProject);
    }

    protected void validateOverloadedFormulaSignature(MessageList result, IIpsProject ipsProject) {
        if (isFormulaSignatureDefinition() && isOverloadsFormula() && !StringUtils.isEmpty(getFormulaName())) {
            FormulaNameFinder finder = new FormulaNameFinder(ipsProject);
            finder.start(getProductCmptType().findSuperProductCmptType(ipsProject));
            if (!finder.isOverloadedMethodAvailable()) {
                result.add(new Message(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY,
                        Messages.ProductCmptTypeMethod_msgNoOverloadableFormulaInSupertypeHierarchy, Message.ERROR,
                        this, IProductCmptTypeMethod.PROPERTY_OVERLOADS_FORMULA));
            } else {
                validateFormulaMandatoryOverloaded(result, finder);
            }
        }
    }

    private void validateFormulaMandatoryOverloaded(MessageList result, FormulaNameFinder finder) {
        if (!isFormulaMandatory() && finder.getMethod().isFormulaMandatory()) {
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTBE_MANDATORY,
                    Messages.ProductCmptTypeMethod_msgOptionalNotAllowedBecauseNotOptionalInSupertypeHierarchy,
                    Message.ERROR, this, IProductCmptTypeMethod.PROPERTY_FORMULA_MANDATORY));
        }
    }

    protected void validateChangingOverTime(MessageList result, IIpsProject ipsProject) throws CoreRuntimeException {
        if (!StringUtils.isEmpty(getName())) {
            ChangingOverTimePropertyValidator propertyValidator = new ChangingOverTimePropertyValidator(this);
            propertyValidator.validateTypeDoesNotAcceptChangingOverTime(result);

            IMethod superMethod = findOverriddenMethod(ipsProject);
            if (superMethod == null) {
                superMethod = findOverloadedFormulaMethod(ipsProject);
            }
            if (superMethod == null) {
                return;
            }
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)superMethod;
            if (method.isChangingOverTime() && !isChangingOverTime()) {
                result.add(new Message(
                        IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTBE_CHANGING_OVER_TIME,
                        Messages.ProductCmptTypeMethod_msgNotChangingOverTimeNotAllowedBecauseChangingOverTimeInSupertypeHierarchy,
                        Message.ERROR, this, IProductCmptTypeMethod.PROPERTY_CHANGING_OVER_TIME));
            }
            if (isChangingOverTime() && !method.isChangingOverTime()) {
                result.add(new Message(
                        IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTBE_NOT_CHANGING_OVER_TIME,
                        Messages.ProductCmptTypeMethod_msgChangingOverTimeNotAllowedBecauseNotChangingOverTimeInSupertypeHierarchy,
                        Message.ERROR, this, IProductCmptTypeMethod.PROPERTY_CHANGING_OVER_TIME));
            }
        }
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreRuntimeException {
        return getProductCmptType();
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return false;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().isMatchingPropertyValue(getPropertyName(), propertyValue);
    }

    @Override
    public boolean isFormulaMandatory() {
        return formulaMandatory || !isFormulaOptionalSupported();
    }

    @Override
    public void setFormulaMandatory(boolean formulaMandatory) {
        boolean oldValue = this.formulaMandatory;
        this.formulaMandatory = formulaMandatory;
        valueChanged(oldValue, formulaMandatory);
    }

    @Override
    public boolean isFormulaOptionalSupported() {
        return isFormulaSignatureDefinition();
    }

    @Override
    public String getFormulaName() {
        return formulaName;
    }

    @Override
    public void setFormulaName(String newFormulaName) {
        String oldFormulaName = getFormulaName();
        this.formulaName = newFormulaName;
        valueChanged(oldFormulaName, formulaName, PROPERTY_FORMULA_NAME);
    }

    @Override
    public void setChangingOverTime(boolean changingOverTime) {
        boolean oldValue = this.changingOverTime;
        this.changingOverTime = changingOverTime;
        valueChanged(oldValue, changingOverTime, PROPERTY_CHANGING_OVER_TIME);
    }

    /**
     * Searches for a formula in the supertype hierarchy with the same name than the formula name of
     * this formula. Stops searching when the first formula method is found that meets this
     * condition or if the super type hierarchy ends.
     */
    private class FormulaNameFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private IProductCmptTypeMethod method;

        public FormulaNameFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        private boolean isOverloadedMethodAvailable() {
            return method != null;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            if (StringUtils.isEmpty(getFormulaName()) || currentType == null) {
                return false;
            }
            method = currentType.getFormulaSignature(getFormulaName());
            return method == null;
        }

        private IProductCmptTypeMethod getMethod() {
            return method;
        }

    }
}
