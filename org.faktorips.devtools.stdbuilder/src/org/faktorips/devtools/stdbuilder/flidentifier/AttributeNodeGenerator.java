/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.GeneratorRuntimeException;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaGenerator for an {@link AttributeNode}. Example in formula language: "policy.premium" (get
 * the value of attribute "premium" from policy).
 * 
 * @author widmaier
 * @since 3.11.0
 */
public class AttributeNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    public AttributeNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> nodeBuilderFactory,
            StandardBuilderSet builderSet) {
        super(nodeBuilderFactory, builderSet);
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
        String attributGetterName = getAttributeGetterName(node.getAttribute(), node.isDefaultValueAccess());
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
        String parameterAttributGetterName = getAttributeGetterName(attribute, node.isDefaultValueAccess());

        JavaCodeFragment getTargetCode = new JavaCodeFragment("new "); //$NON-NLS-1$
        getTargetCode.appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.AttributeAccessorHelper.class);
        getTargetCode.append("<"); //$NON-NLS-1$
        getTargetCode.appendClassName(conextDatatype.getJavaClassName());
        getTargetCode.append(", "); //$NON-NLS-1$
        getTargetCode.appendClassName(attribute.getDatatype());
        getTargetCode.append(">(){@Override protected "); //$NON-NLS-1$
        getTargetCode.appendClassName(attribute.getDatatype());
        getTargetCode.append(" getValueInternal("); //$NON-NLS-1$
        getTargetCode.appendClassName(conextDatatype.getJavaClassName());
        getTargetCode.append(" sourceObject){return sourceObject." + parameterAttributGetterName); //$NON-NLS-1$
        getTargetCode.append("();}}.getAttributeValues("); //$NON-NLS-1$
        getTargetCode.append(contextCompilationResult.getCodeFragment());
        getTargetCode.append(")"); //$NON-NLS-1$

        return new CompilationResultImpl(getTargetCode, node.getDatatype());
    }

    private Datatype getBasicDatatype(CompilationResult<JavaCodeFragment> contextCompilationResult) {
        ListOfTypeDatatype contextListofTypeDatatype = (ListOfTypeDatatype)contextCompilationResult.getDatatype();
        Datatype conextDatatype = contextListofTypeDatatype.getBasicDatatype();
        return conextDatatype;
    }

    private JavaCodeFragment createCodeFragment(final String parameterAttributGetterName,
            JavaCodeFragment contextCodeFragment) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        javaCodeFragment.append(contextCodeFragment);
        javaCodeFragment.append('.' + parameterAttributGetterName + "()"); //$NON-NLS-1$
        return javaCodeFragment;
    }

    protected String getAttributeGetterName(IAttribute attribute, boolean isDefaultValueAccess) {
        String parameterAttributGetterName = isDefaultValueAccess ? getParameterAttributDefaultValueGetterName(attribute)
                : getParameterAttributGetterName(attribute);
        return parameterAttributGetterName;
    }

    private String getParameterAttributGetterName(IAttribute attribute) {
        if (attribute instanceof IPolicyCmptTypeAttribute) {
            XPolicyAttribute xPolicyAttribute = getModelNode(attribute, XPolicyAttribute.class);
            return xPolicyAttribute.getMethodNameGetter();
        } else if (attribute instanceof IProductCmptTypeAttribute) {
            XProductAttribute xProductAttribute = getModelNode(attribute, XProductAttribute.class);
            if (xProductAttribute.isChangingOverTime()) {
                return xProductAttribute.getMethodNameGetter();
            } else {
                XProductCmptClass xProductCmptClass = getModelNode(attribute.getType(), XProductCmptClass.class);
                return xProductCmptClass.getMethodNameGetProductCmpt() + "()." //$NON-NLS-1$
                        + xProductAttribute.getMethodNameGetter();
            }
        }
        throw new GeneratorRuntimeException("This type of attribute is not supported: " + attribute.getClass()); //$NON-NLS-1$
    }

    private String getParameterAttributDefaultValueGetterName(IAttribute attribute) {
        XPolicyCmptClass xPolicyCmptClass = getModelNode(attribute.getType(), XPolicyCmptClass.class);
        XPolicyAttribute xPolicyAttribute = getModelNode(attribute, XPolicyAttribute.class);
        return xPolicyCmptClass.getMethodNameGetProductCmptGeneration() + "()." //$NON-NLS-1$
                + xPolicyAttribute.getMethodNameGetDefaultValue();
    }
}
