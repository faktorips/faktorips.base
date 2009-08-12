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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Storage for a state-snapshot of an IpsObject.
 * 
 * @author Thorsten Guenther
 */
public class IpsObjectPartState {

    /**
     * Type information to create a new IIpsObjectPart.
     */
    private Class type;

    /**
     * The complete state-information to create a copy of an IIpsObjectPart.
     */
    private Document state;

    /**
     * Name of the node to store the type-information.
     */
    private static final String ELEMENT_TYPE = "ipsObjectPartStateTypeInformation"; //$NON-NLS-1$

    /**
     * Name of the node to store the object-representation.
     */
    private static final String ELEMENT_DATA = "ipsObjectPartStateData"; //$NON-NLS-1$

    /**
     * Creates a new state-snapshot from the given part.
     */
    public IpsObjectPartState(IIpsObjectPart part) {
        type = part.getClass();
        /*
         * TODO AW: Not sure if this is code is vital for this to function, delete later if all is
         * well.
         */
        /*
         * if (!type.isInterface()) { Class[] interfaces = type.getInterfaces(); for (int i = 0; i <
         * interfaces.length; i ++) { if (IIpsObjectPart.class.isAssignableFrom(interfaces[i])) {
         * type = interfaces[i]; break; } } }
         */
        state = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element root = state.createElement("root"); //$NON-NLS-1$
        state.appendChild(root);
        Element data = state.createElement(ELEMENT_DATA);
        root.appendChild(data);
        data.appendChild(part.toXml(state));
        Element typeInfo = state.createElement(ELEMENT_TYPE);

        typeInfo.setAttribute("type", type.getName()); //$NON-NLS-1$
        root.appendChild(typeInfo);
    }

    /**
     * Creates a new IpsObjectPartState parsed out of the given string.
     */
    public IpsObjectPartState(String part) {
        try {
            state = XmlUtil.getDocument(new ByteArrayInputStream(part.getBytes()));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        NodeList nodes = state.getDocumentElement().getElementsByTagName(ELEMENT_TYPE);

        if (nodes.getLength() != 1) {
            throw new RuntimeException(
                    "Illegal String - expected exactly ONE node with tagname " + ELEMENT_TYPE + ", but found " + nodes.getLength()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Attr typeAttr = (Attr)nodes.item(0).getAttributes().getNamedItem("type"); //$NON-NLS-1$
        String typeName = typeAttr.getValue();
        try {
            type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the string-representation of this state. This string can be parsed back to an object
     * by using the constructor taking a string.
     */
    public String toString() {
        try {
            return XmlUtil.nodeToString(state, "UTF-8"); //$NON-NLS-1$
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new IIpsObjectPart from the currently stored state (if modifications took place in
     * the source of this state object, these are NOT reflected here!).
     * 
     * @param parent The parent to create the part in.
     * @return The newly created IIpsObjectPart, initialized to match the stored state.
     */
    public IIpsObjectPart newPart(IIpsObjectPartContainer parent) {
        IIpsObjectPart part = parent.newPart(type);
        NodeList nodes = state.getDocumentElement().getElementsByTagName(ELEMENT_DATA);

        if (nodes.getLength() != 1) {
            throw new RuntimeException(
                    "Illegal String - expected exactly ONE node with tagname " + ELEMENT_DATA + ", but found " + nodes.getLength()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Element el = (Element)nodes.item(0);
        part.initFromXml((Element)el.getElementsByTagName("*").item(0)); //$NON-NLS-1$
        return part;
    }

}
