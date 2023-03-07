/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.GeneratorRuntimeException;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * JavaGenerator for an {@link AttributeNode}. Supports both policy- and product-attributes.
 * Examples in the formula language: "policy.premium" (gets the value of attribute "premium" from
 * policy) and "policy.paymentMode" (gets the value "paymentMode" from the configuring product
 * component).
 * 
 * @author widmaier
 * @since 3.11.0
 */
public class AttributeNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    private IExpression expression;

    public AttributeNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> nodeBuilderFactory,
            IExpression expression, StandardBuilderSet builderSet) {
        super(nodeBuilderFactory, builderSet);
        this.expression = expression;
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        AttributeNode node = (AttributeNode)identifierNode;
        if (isListOfTypeDatatype(contextCompilationResult)) {
            return createListCompilationResult(node, contextCompilationResult);
        } else {
            return createNormalCompilationResult(node, contextCompilationResult);
        }
    }

    private CompilationResult<JavaCodeFragment> createNormalCompilationResult(final AttributeNode node,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        Datatype contextDatatype = contextCompilationResult.getDatatype();
        String attributGetterName = getAttributeGetterName(node, contextDatatype);
        JavaCodeFragment attributeFragment = createCodeFragment(attributGetterName,
                contextCompilationResult.getCodeFragment());
        return new CompilationResultImpl(attributeFragment, node.getDatatype());
    }

    private boolean isListOfTypeDatatype(CompilationResult<JavaCodeFragment> compilationResult) {
        return compilationResult.getDatatype() instanceof ListOfTypeDatatype;
    }

    private CompilationResult<JavaCodeFragment> createListCompilationResult(AttributeNode node,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {

        if (!node.isListOfTypeDatatype()) {
            throw new GeneratorRuntimeException("The datatype of this node is not a ListOfTypeDatatype: " + node); //$NON-NLS-1$
        }

        Datatype conextDatatype = getBasicDatatype(contextCompilationResult);
        IAttribute attribute = node.getAttribute();
        String attributeDatatypeClassName = getDatatypeClassname(attribute);
        String parameterAttributGetterName = getAttributeGetterName(node, conextDatatype);

        JavaCodeFragment getTargetCode = new JavaCodeFragment("new "); //$NON-NLS-1$
        getTargetCode.appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.AttributeAccessorHelper.class);
        getTargetCode.append("<"); //$NON-NLS-1$
        getTargetCode.appendClassName(getJavaClassName(conextDatatype));
        getTargetCode.append(", "); //$NON-NLS-1$
        getTargetCode.appendClassName(attributeDatatypeClassName);
        getTargetCode.append(">(){\n@Override protected "); //$NON-NLS-1$
        getTargetCode.appendClassName(attributeDatatypeClassName);
        getTargetCode.append(" getValueInternal("); //$NON-NLS-1$
        getTargetCode.appendClassName(getJavaClassName(conextDatatype));
        getTargetCode.append(" sourceObject){return sourceObject." + parameterAttributGetterName); //$NON-NLS-1$
        getTargetCode.append("();}}.getAttributeValues("); //$NON-NLS-1$
        getTargetCode.append(contextCompilationResult.getCodeFragment());
        getTargetCode.append(")"); //$NON-NLS-1$

        return new CompilationResultImpl(getTargetCode, node.getDatatype());
    }

    private String getDatatypeClassname(IAttribute attribute) {
        ValueDatatype datatype = attribute.findDatatype(getIpsProject());
        if (datatype.isPrimitive()) {
            datatype = datatype.getWrapperType();
        }
        return getJavaClassName(datatype);
    }

    private Datatype getBasicDatatype(CompilationResult<JavaCodeFragment> contextCompilationResult) {
        ListOfTypeDatatype contextListofTypeDatatype = (ListOfTypeDatatype)contextCompilationResult.getDatatype();
        return contextListofTypeDatatype.getBasicDatatype();
    }

    private JavaCodeFragment createCodeFragment(final String parameterAttributGetterName,
            JavaCodeFragment contextCodeFragment) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        javaCodeFragment.append(contextCodeFragment);
        javaCodeFragment.append('.' + parameterAttributGetterName + "()"); //$NON-NLS-1$
        return javaCodeFragment;
    }

    protected String getAttributeGetterName(final AttributeNode node, Datatype contextDatatype) {
        return node.isDefaultValueAccess()
                ? getParameterAttributDefaultValueGetterName(node
                        .getAttribute())
                : getParameterAttributGetterName(node, contextDatatype);
    }

    private String getParameterAttributGetterName(final AttributeNode node, Datatype contextDatatype) {
        IAttribute attribute = node.getAttribute();
        if (attribute instanceof IPolicyCmptTypeAttribute) {
            return getPolicyAttributeGetterName((IPolicyCmptTypeAttribute)attribute);
        } else if (attribute instanceof IProductCmptTypeAttribute) {
            return getProductAttributeAccessCode(node, contextDatatype);
        }
        throw new GeneratorRuntimeException("This type of attribute is not supported: " + attribute.getClass()); //$NON-NLS-1$
    }

    private String getPolicyAttributeGetterName(IPolicyCmptTypeAttribute attribute) {
        XPolicyAttribute xPolicyAttribute = getModelNode(attribute, XPolicyAttribute.class);
        return xPolicyAttribute.getMethodNameGetter();
    }

    private String getProductAttributeAccessCode(final AttributeNode node, Datatype contextDatatype) {
        StringBuilder contextAccessCode = new StringBuilder();
        contextAccessCode.append(getProductCmptContextCode(node, contextDatatype));
        contextAccessCode.append(getProductAttributeGetterName((IProductCmptTypeAttribute)node.getAttribute()));
        return contextAccessCode.toString();
    }

    private String getProductCmptContextCode(final AttributeNode node, Datatype contextDatatype) {
        if (contextDatatype instanceof IPolicyCmptType) {
            return getProductCmptOrGenerationGetterCode((IProductCmptTypeAttribute)node.getAttribute(),
                    (IPolicyCmptType)contextDatatype);
        } else {
            return getProductCmptGetterCodeIfRequired(node, contextDatatype);
        }
    }

    /**
     * Based on a policy component, returns the code for getting the configuring product component
     * generation if the requested attribute is changing over time or the product component if the
     * attribute is static.
     */
    private String getProductCmptOrGenerationGetterCode(IProductCmptTypeAttribute attribute,
            IPolicyCmptType policyType) {
        XPolicyCmptClass xPolicyCmptClass = getModelNode(policyType, XPolicyCmptClass.class);
        if (attribute.isChangingOverTime()) {
            return xPolicyCmptClass.getMethodNameGetProductCmptGeneration() + "().";
        } else {
            return xPolicyCmptClass.getMethodNameGetProductCmpt() + "().";
        }
    }

    /**
     * Based on a product component generation, returns an empty string if the requested attribute
     * is changing over time or the code for getting the product component if the attribute is
     * static.
     */
    private String getProductCmptGetterCodeIfRequired(final AttributeNode node, Datatype contextDatatype) {
        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)node.getAttribute();
        if (!attribute.isChangingOverTime() && isChanginOverTimeContext(contextDatatype)) {
            XProductCmptClass xProductCmptClass = getModelNode(attribute.getType(), XProductCmptClass.class);
            return xProductCmptClass.getMethodNameGetProductCmpt() + "().";
        } else {
            return IpsStringUtils.EMPTY;
        }
    }

    private boolean isChanginOverTimeContext(Datatype contextDatatype) {
        if (expression instanceof IFormula formula) {
            IProductCmptType productCmptType = formula.findProductCmptType(getIpsProject());
            if (productCmptType != null && productCmptType.equals(contextDatatype)) {
                return formula.getPropertyValueContainer().isChangingOverTimeContainer();
            } else {
                // the contextDatatype seems to be the type of a parameter and hence it would always
                // be a generation.
                return true;
            }
        } else {
            return false;
        }
    }

    private String getProductAttributeGetterName(IProductCmptTypeAttribute attribute) {
        XProductAttribute xProductAttribute = getModelNode(attribute, XProductAttribute.class);
        return xProductAttribute.getMethodNameGetter();
    }

    private String getParameterAttributDefaultValueGetterName(IAttribute attribute) {
        XPolicyCmptClass xPolicyCmptClass = getModelNode(attribute.getType(), XPolicyCmptClass.class);
        XPolicyAttribute xPolicyAttribute = getModelNode(attribute, XPolicyAttribute.class);
        return xPolicyCmptClass.getMethodNameGetProductCmptGeneration() + "()." //$NON-NLS-1$
                + xPolicyAttribute.getMethodNameGetDefaultValue();
    }
}
