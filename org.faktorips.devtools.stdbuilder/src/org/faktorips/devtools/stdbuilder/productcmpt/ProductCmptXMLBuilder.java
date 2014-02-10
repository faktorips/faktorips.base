/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Copies the product cmpt xml file to the output location (=java source folder). For associations
 * the target runtime id is added to the xml.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptXMLBuilder extends AbstractXmlFileBuilder {

    public static final String XML_ATTRIBUTE_TARGET_RUNTIME_ID = "targetRuntimeId"; //$NON-NLS-1$

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

            updateTargetRuntimeId(productCmpt, root);

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

    private void updateTargetRuntimeId(IProductCmptLinkContainer linkContainer, Element linkContainerElement)
            throws DOMException {
        List<Element> linkElements = getLinkElements(linkContainerElement);
        List<IProductCmptLink> links = linkContainer.getLinksAsList();
        for (int i = 0; i < links.size(); i++) {
            Element linkXmlElement = linkElements.get(i);
            linkXmlElement.setAttribute(XML_ATTRIBUTE_TARGET_RUNTIME_ID, getTargetRuntimeId(links.get(i)));
        }
    }

    /**
     * Returns all (direct) children of the given element that have the name "Link".
     * {@link Element#getElementsByTagName(String)} is not used deliberately as it returns all
     * descendants with a specific name. For product component elements this is undesired as it
     * would also return the link elements of all generations in addition to the component's own
     * link elements.
     * 
     * @param prodCmptElement the element to retrieve link elements from
     * @return the list of all found link elements
     * @since 3.8 with the introduction of static associations.
     */
    private List<Element> getLinkElements(Element prodCmptElement) {
        List<Element> linkElements = new ArrayList<Element>();
        NodeList nodeList = prodCmptElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && ((Element)node).getNodeName().equals(IProductCmptLink.TAG_NAME)) {
                linkElements.add((Element)nodeList.item(i));
            }
        }
        return linkElements;
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
