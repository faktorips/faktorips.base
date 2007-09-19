/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.AbstractXmlFileBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Copies the product cmpt xml file to the output location (=java source folder).
 * For relations the target runtime id is added to the xml.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptXMLBuilder extends AbstractXmlFileBuilder {

    public ProductCmptXMLBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet, String kind) {
        super(type, builderSet, kind);
    }

    /**
     * {@inheritDoc}
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        Element root = productCmpt.toXml(IpsPlugin.getDefault().newDocumentBuilder().newDocument());
        
        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
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

    private void updateTargetRuntimeId(IProductCmptGeneration generation, Element generationElement) throws DOMException, CoreException {
        NodeList relationNodes = generationElement.getElementsByTagName(IProductCmptLink.TAG_NAME);
        IProductCmptLink[] relations = generation.getLinks();
        for (int i = 0; i < relations.length; i++) {
            Element relation = (Element)relationNodes.item(i);
            relation.setAttribute(ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID, getTargetRuntimeId(relations[i]));
        }
    }

    private String getTargetRuntimeId(IProductCmptLink link) throws CoreException {
        IProductCmpt productCmpt = link.findTarget(link.getIpsProject());
        if(productCmpt != null){
            return productCmpt.getRuntimeId();
        }
        return "";
    }
    
    /**
     * {@inheritDoc}
     * 
     * Returns true.
     */
    public boolean buildsDerivedArtefacts() {
        return true;
    }

}
