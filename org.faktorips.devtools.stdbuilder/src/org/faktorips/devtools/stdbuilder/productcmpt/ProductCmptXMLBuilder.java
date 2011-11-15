/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.AbstractXmlFileBuilder;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
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

    private final ExpressionXMLBuilderHelper expressionXMLBuilderHelper;
    private MultiStatus buildStatus;

    public ProductCmptXMLBuilder(IpsObjectType type, StandardBuilderSet builderSet) {
        super(type, builderSet);
        this.expressionXMLBuilderHelper = new ExpressionXMLBuilderHelper(builderSet);
    }

    public StandardBuilderSet getStandardBuilderSet() {
        return (StandardBuilderSet)getBuilderSet();
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) {
        try {
            super.beforeBuild(ipsSrcFile, status);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        buildStatus = status;
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        try {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
            Document document = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
            Element root = productCmpt.toXml(document);

            IIpsObjectGeneration[] generations = productCmpt.getGenerationsOrderedByValidDate();
            NodeList generationNodes = root.getElementsByTagName(IIpsObjectGeneration.TAG_NAME);
            for (int i = 0; i < generations.length; i++) {
                updateTargetRuntimeId((IProductCmptGeneration)generations[i], (Element)generationNodes.item(i));

                // creating compiled formula expressions
                if (getStandardBuilderSet().getFormulaCompiling().isCompileToXml()) {
                    IFormula[] formulas = ((IProductCmptGeneration)generations[i]).getFormulas();
                    NodeList formulaElements = ((Element)generationNodes.item(i))
                            .getElementsByTagName(Formula.TAG_NAME);
                    expressionXMLBuilderHelper.addCompiledFormulaExpressions(document, formulas, formulaElements,
                            buildStatus);
                }
            }
            try {
                super.build(ipsSrcFile, XmlUtil.nodeToString(root, ipsSrcFile.getIpsProject().getXmlFileCharset()));
            } catch (TransformerException e) {
                throw new CoreRuntimeException(new CoreException(new IpsStatus(e)));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    private void updateTargetRuntimeId(IProductCmptGeneration generation, Element generationElement)
            throws DOMException {
        NodeList associationNodes = generationElement.getElementsByTagName(IProductCmptLink.TAG_NAME);
        IProductCmptLink[] associations = generation.getLinks();
        for (int i = 0; i < associations.length; i++) {
            Element association = (Element)associationNodes.item(i);
            association.setAttribute(ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID,
                    getTargetRuntimeId(associations[i]));
        }
    }

    private String getTargetRuntimeId(IProductCmptLink link) {
        IProductCmpt productCmpt;
        try {
            productCmpt = link.findTarget(link.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
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
