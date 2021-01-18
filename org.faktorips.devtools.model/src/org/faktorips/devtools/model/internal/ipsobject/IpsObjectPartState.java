/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.util.XmlUtil;
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
    private Class<? extends IIpsObjectPart> type;

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
            state = XmlUtil.parseDocument(new ByteArrayInputStream(part.getBytes()));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        NodeList nodes = state.getDocumentElement().getElementsByTagName(ELEMENT_TYPE);
        if (nodes.getLength() != 1) {
            throw new RuntimeException(
                    "Illegal String - expected exactly ONE node with tagname " + ELEMENT_TYPE + ", but found " //$NON-NLS-1$ //$NON-NLS-2$
                            + nodes.getLength());
        }

        Attr typeAttr = (Attr)nodes.item(0).getAttributes().getNamedItem("type"); //$NON-NLS-1$
        String typeName = typeAttr.getValue();

        try {
            type = Class.forName(typeName).asSubclass(IIpsObjectPart.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new IpsObjectPartState parsed out of the given string using the given ClassLoader.
     */
    @SuppressWarnings("unchecked")
    public IpsObjectPartState(String part, ClassLoader cl) {
        try {
            state = XmlUtil.parseDocument(new ByteArrayInputStream(part.getBytes()));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        NodeList nodes = state.getDocumentElement().getElementsByTagName(ELEMENT_TYPE);
        if (nodes.getLength() != 1) {
            throw new RuntimeException(
                    "Illegal String - expected exactly ONE node with tagname " + ELEMENT_TYPE + ", but found " //$NON-NLS-1$ //$NON-NLS-2$
                            + nodes.getLength());
        }

        Attr typeAttr = (Attr)nodes.item(0).getAttributes().getNamedItem("type"); //$NON-NLS-1$
        String typeName = typeAttr.getValue();

        try {
            type = (Class<? extends IIpsObjectPart>)cl.loadClass(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the string-representation of this state. This string can be parsed back to an object
     * by using the constructor taking a string.
     */
    @Override
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
    public IIpsObjectPart newPart(IpsObjectPartContainer parent) {
        IIpsObjectPart part = parent.newPart(type);
        NodeList nodes = state.getDocumentElement().getElementsByTagName(ELEMENT_DATA);

        if (nodes.getLength() != 1) {
            throw new RuntimeException(
                    "Illegal String - expected exactly ONE node with tagname " + ELEMENT_DATA + ", but found " //$NON-NLS-1$ //$NON-NLS-2$
                            + nodes.getLength());
        }

        Element el = (Element)nodes.item(0);
        ((IpsObjectPart)part).initFromXml((Element)el.getElementsByTagName("*").item(0), part.getId()); //$NON-NLS-1$

        parent.partWasAdded(part);
        return part;
    }

    /**
     * Returns the type this {@link IpsObjectPartState} stores.
     * 
     * @return the type this {@link IpsObjectPartState} stores
     */
    public Class<? extends IIpsObjectPart> getType() {
        return type;
    }

}
