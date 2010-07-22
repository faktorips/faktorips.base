/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpt;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.AbstractXmlFileBuilder;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.runtime.formula.AbstractFormulaEvaluator;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Copies the product cmpt xml file to the output location (=java source folder). For associations
 * the target runtime id is added to the xml.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptXMLBuilder extends AbstractXmlFileBuilder {

    private final ProductCmptBuilder productCmptGenerationImplBuilder;

    public ProductCmptXMLBuilder(IpsObjectType type, StandardBuilderSet builderSet, String kind,
            ProductCmptBuilder productCmptGenerationImplBuilder) {
        super(type, builderSet, kind);
        this.productCmptGenerationImplBuilder = productCmptGenerationImplBuilder;
    }

    public StandardBuilderSet getStandardBuilderSet() {
        return (StandardBuilderSet)getBuilderSet();
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        Document document = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element root = productCmpt.toXml(document);

        IIpsObjectGeneration[] generations = productCmpt.getGenerationsOrderedByValidDate();
        NodeList generationNodes = root.getElementsByTagName(IIpsObjectGeneration.TAG_NAME);
        for (int i = 0; i < generations.length; i++) {
            updateTargetRuntimeId((IProductCmptGeneration)generations[i], (Element)generationNodes.item(i));

            // creating compiled formula expressions
            if (getStandardBuilderSet().getFormulaCompiling().compileToXml()) {
                IFormula[] formulas = ((IProductCmptGeneration)generations[i]).getFormulas();
                NodeList formulaElements = ((Element)generationNodes.item(i)).getElementsByTagName(Formula.TAG_NAME);
                addCompiledFormulaExpressions(document, formulas, formulaElements);
            }
        }
        try {
            super.build(ipsSrcFile, XmlUtil.nodeToString(root, ipsSrcFile.getIpsProject().getXmlFileCharset()));
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    private void addCompiledFormulaExpressions(Document document, IFormula[] formulas, NodeList formulaElements)
            throws CoreException {
        for (int formulaIndex = 0; formulaIndex < formulas.length; formulaIndex++) {
            IFormula formula = formulas[formulaIndex];
            IProductCmptTypeMethod method = formula.findFormulaSignature(getIpsProject());
            if (method != null) {
                Element formulaElement = (Element)formulaElements.item(formulaIndex);
                Element javaExpression = document.createElement(AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
                JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder().appendln();
                IProductCmptTypeMethod formulaSignature = formula.findFormulaSignature(getIpsProject());
                JavaCodeFragment formulaFragment = productCmptGenerationImplBuilder.getGenerationBuilder()
                        .compileFormulaToJava(formula, formulaSignature, false);

                generateFormulaMethodSignature(method, builder);

                builder.openBracket();
                builder.append("return ").append(formulaFragment).append(';');
                builder.closeBracket().appendln();
                String sourceCode = builder.getFragment().getImportDeclaration().toString() + '\n'
                        + builder.getFragment().getSourcecode();
                CDATASection javaCode = document.createCDATASection(sourceCode);
                javaExpression.appendChild(javaCode);
                formulaElement.appendChild(javaExpression);
            }
        }
    }

    private void generateFormulaMethodSignature(IMethod method, JavaCodeFragmentBuilder builder) throws CoreException {
        IParameter[] parameters = method.getParameters();
        int modifier = method.getJavaModifier();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(method.getDatatype(), false,
                getStandardBuilderSet(), getIpsProject());

        String[] parameterNames = BuilderHelper.extractParameterNames(parameters);
        String[] parameterTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, false,
                getStandardBuilderSet(), getIpsProject());
        String[] parameterInSignatur = parameterNames;
        String[] parameterTypesInSignatur = parameterTypes;

        String methodName = method.getName();
        // extend the method signature with the given parameter names
        builder.signature(modifier, returnClass, methodName, parameterInSignatur, parameterTypesInSignatur, false);
    }

    private void updateTargetRuntimeId(IProductCmptGeneration generation, Element generationElement)
            throws DOMException, CoreException {
        NodeList associationNodes = generationElement.getElementsByTagName(IProductCmptLink.TAG_NAME);
        IProductCmptLink[] associations = generation.getLinks();
        for (int i = 0; i < associations.length; i++) {
            Element association = (Element)associationNodes.item(i);
            association.setAttribute(ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID,
                    getTargetRuntimeId(associations[i]));
        }
    }

    private String getTargetRuntimeId(IProductCmptLink link) throws CoreException {
        IProductCmpt productCmpt = link.findTarget(link.getIpsProject());
        if (productCmpt != null) {
            return productCmpt.getRuntimeId();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     * 
     * Returns true.
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

}
