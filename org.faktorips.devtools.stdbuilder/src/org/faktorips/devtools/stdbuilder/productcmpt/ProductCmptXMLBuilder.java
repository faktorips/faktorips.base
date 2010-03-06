/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpt;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.AbstractXmlFileBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Copies the product cmpt xml file to the output location (=java source folder). For associations
 * the target runtime id is added to the xml.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptXMLBuilder extends AbstractXmlFileBuilder {

    public ProductCmptXMLBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet, String kind) {
        super(type, builderSet, kind);
    }

    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        Element root = productCmpt.toXml(IpsPlugin.getDefault().newDocumentBuilder().newDocument());

        IIpsObjectGeneration[] generations = productCmpt.getGenerationsOrderedByValidDate();
        NodeList generationNodes = root.getElementsByTagName(IIpsObjectGeneration.TAG_NAME);
        for (int i = 0; i < generations.length; i++) {
            updateTargetRuntimeId((IProductCmptGeneration)generations[i], (Element)generationNodes.item(i));
        }
        try {
            super.build(ipsSrcFile, XmlUtil.nodeToString(root, ipsSrcFile.getIpsProject().getXmlFileCharset()));
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus(e));
        }
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
