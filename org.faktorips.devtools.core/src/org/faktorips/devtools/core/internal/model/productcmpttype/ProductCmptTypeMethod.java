/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    public IProductCmptTypeMethod findOverloadedFormulaMethod(IIpsProject ipsProject) throws CoreException {
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
        formulaSignatureDefinition = Boolean.valueOf(element.getAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION))
                .booleanValue();
        overloadsFormula = Boolean.valueOf(element.getAttribute(PROPERTY_OVERLOADS_FORMULA));
        String mandatoryXml = element.getAttribute(XML_FORMULA_MANDATORY);
        formulaMandatory = StringUtils.isEmpty(mandatoryXml) ? true : Boolean.valueOf(mandatoryXml);
        formulaName = element.getAttribute(PROPERTY_FORMULA_NAME);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION, String.valueOf(formulaSignatureDefinition));
        element.setAttribute(PROPERTY_OVERLOADS_FORMULA, String.valueOf(overloadsFormula));
        element.setAttribute(XML_FORMULA_MANDATORY, String.valueOf(formulaMandatory));
        element.setAttribute(PROPERTY_FORMULA_NAME, String.valueOf(formulaName));
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
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
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

    private void validateOverloadedFormulaSignature(MessageList result, IIpsProject ipsProject) throws CoreException {
        if (isFormulaSignatureDefinition() && isOverloadsFormula()) {
            FormulaNameFinder finder = new FormulaNameFinder(ipsProject);
            finder.start(getProductCmptType().findSuperProductCmptType(ipsProject));
            if (!StringUtils.isEmpty(getFormulaName()) && !finder.formulaNameFound()) {
                result.add(new Message(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY,
                        Messages.ProductCmptTypeMethod_msgNoOverloadableFormulaInSupertypeHierarchy, Message.ERROR,
                        this, IProductCmptTypeMethod.PROPERTY_OVERLOADS_FORMULA));
            }

            if (!isFormulaMandatory() && finder.getMethod().isFormulaMandatory()) {
                result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTBE_MANDATORY,
                        Messages.ProductCmptTypeMethod_msgOptionalNotAllowedBecauseNotOptionalInSupertypeHierarchy,
                        Message.ERROR, this, IProductCmptTypeMethod.PROPERTY_FORMULA_MANDATORY));
            }
        }
    }

    @Override
    public boolean isChangingOverTime() {
        return true;
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmptType();
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return false;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().equals(propertyValue.getPropertyType())
                && getPropertyName().equals(propertyValue.getPropertyName());
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

        private boolean formulaNameFound() {
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
