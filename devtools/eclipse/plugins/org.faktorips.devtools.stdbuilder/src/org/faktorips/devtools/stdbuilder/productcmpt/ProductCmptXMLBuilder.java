/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.AbstractXmlFileBuilder;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.UUIDFilterStream;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.values.DateUtil;
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

    public static final String XML_ATTRIBUTE_VALID_FROM = "validFrom";

    private final ExpressionXMLBuilderHelper expressionXMLBuilderHelper;
    private MultiStatus buildStatus;

    public ProductCmptXMLBuilder(IpsObjectType type, StandardBuilderSet builderSet) {
        super(type, builderSet);
        expressionXMLBuilderHelper = new ExpressionXMLBuilderHelper(builderSet);
    }

    public StandardBuilderSet getStandardBuilderSet() {
        return (StandardBuilderSet)getBuilderSet();
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) {
        super.beforeBuild(ipsSrcFile, status);
        buildStatus = status;
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        Document document = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element root = productCmpt.toXml(document);

        root.removeAttribute(XmlUtil.XMLNS_ATTRIBUTE);
        root.removeAttributeNS(XmlUtil.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
        writeValidFrom(productCmpt, root);
        updateInternationalStringDefaultLocale(getDefaultLocale(productCmpt.getIpsProject()), root);
        updateLinks(productCmpt, root);
        updateGenerations(productCmpt, document, root);
        compileFormulas(productCmpt, document, root);
        build(ipsSrcFile, root);
    }

    private Locale getDefaultLocale(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
    }

    private void updateInternationalStringDefaultLocale(Locale defaultLocale, Element root) {
        NodeList internationalStrings = root.getElementsByTagName(IInternationalString.XML_TAG);
        for (int i = 0; i < internationalStrings.getLength(); i++) {
            Element internationalString = (Element)internationalStrings.item(i);
            internationalString.setAttribute(IInternationalString.XML_ATTR_DEFAULT_LOCALE, defaultLocale.getLanguage());
        }
    }

    private void writeValidFrom(IProductCmpt productCmpt, Element root) {
        root.setAttribute(XML_ATTRIBUTE_VALID_FROM,
                DateUtil.gregorianCalendarToIsoDateString(productCmpt.getValidFrom()));
    }

    private void updateGenerations(IProductCmpt productCmpt, Document document, Element root) {
        List<Element> generationNodes = getElements(root, IIpsObjectGeneration.TAG_NAME);
        if (productCmpt.allowGenerations()) {
            updateTargetAndCompileFormulaInGenerations(productCmpt, document, generationNodes);
        } else {
            removeDummyGeneration(root, generationNodes);
        }
    }

    private void updateTargetAndCompileFormulaInGenerations(IProductCmpt productCmpt,
            Document document,
            List<Element> generationNodes) {
        IIpsObjectGeneration[] generations = productCmpt.getGenerationsOrderedByValidDate();
        for (int i = 0; i < generations.length && i < generationNodes.size(); i++) {
            IProductCmptGeneration generation = (IProductCmptGeneration)generations[i];
            Element generationElement = generationNodes.get(i);

            updateLinks(generation, generationElement);
            compileFormulas(generation, document, generationElement);
        }
    }

    /**
     * Updates the link elements in the given parent element using the {@code IProductCmptLink}
     * objects from the given container.
     * <ul>
     * <li>link elements with the template value status {@code UNDEFINED} are removed from the
     * parent element</li>
     * <li>in the remaining link elements, the {@code targetRuntimeId} attribute is set to the
     * runtime id of the {@code IProductCmpt} in the corresponding {@code IProductCmptLink}</li>
     * </ul>
     * 
     * @param linkContainer the container with the product component links to obtain runtime ids
     *            from
     * @param linkContainerElement the parent element of which the link elements are children
     */
    private void updateLinks(IProductCmptLinkContainer linkContainer, Element linkContainerElement) {
        List<Element> linkElements = getElements(linkContainerElement, IProductCmptLink.TAG_NAME);
        List<IProductCmptLink> links = linkContainer.getLinksAsList();
        for (int i = 0; i < links.size(); i++) {
            IProductCmptLink link = links.get(i);
            Element linkElement = linkElements.get(i);
            if (link.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
                linkContainerElement.removeChild(linkElement);
            } else {
                linkElement.setAttribute(XML_ATTRIBUTE_TARGET_RUNTIME_ID, getTargetRuntimeId(links.get(i)));
            }
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
    private List<Element> getElements(Element prodCmptElement, String tagName) {
        List<Element> linkElements = new ArrayList<>();
        NodeList nodeList = prodCmptElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element element) {
                if (Objects.equals(element.getNodeName(), tagName)) {
                    linkElements.add(element);
                }
            }
        }
        return linkElements;
    }

    private String getTargetRuntimeId(IProductCmptLink link) {
        IProductCmpt productCmpt = link.findTarget(link.getIpsProject());
        if (productCmpt != null) {
            return productCmpt.getRuntimeId();
        }
        return "";
    }

    private void compileFormulas(IPropertyValueContainer propertyValueContainer, Document document, Element node) {
        if (GeneratorConfig.forIpsObject(propertyValueContainer.getIpsObject()).getFormulaCompiling()
                .isCompileToXml()) {
            List<IFormula> formulas = propertyValueContainer.getPropertyValues(IFormula.class);
            List<Element> formulaElements = getElements(node, IFormula.TAG_NAME);
            expressionXMLBuilderHelper.addCompiledFormulaExpressions(document, formulas, formulaElements, buildStatus);
        }
    }

    /**
     * For compatibility reasons we have still one generation also if the product component type
     * does not support generations. For the runtime XML we want to remove this dummy generation.
     */
    private void removeDummyGeneration(Element root, List<Element> generationNodes) {
        for (Element element : generationNodes) {
            root.removeChild(element);
        }
    }

    private void build(IIpsSrcFile ipsSrcFile, Element root) {
        try {

            IIpsProjectProperties properties = ipsSrcFile.getIpsProject().getReadOnlyProperties();
            String nodeToString = XmlUtil.nodeToString(root, ipsSrcFile.getIpsProject().getXmlFileCharset(),
                    properties.isEscapeNonStandardBlanks());
            build(ipsSrcFile, nodeToString);

        } catch (TransformerException e) {
            throw new IpsException(new CoreException(new IpsStatus(e)));
        }
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

    @Override
    public void writeToFile(AFile file, InputStream inputStream, boolean keepHistory) {
        super.writeToFile(file, new UUIDFilterStream(inputStream), keepHistory);
    }

}
