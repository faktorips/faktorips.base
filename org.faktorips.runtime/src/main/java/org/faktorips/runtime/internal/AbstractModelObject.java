/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for all policy component types.
 */
public abstract class AbstractModelObject implements IModelObject {

    /**
     * Removes the given child object from this object. If the given object is not a child of this
     * model object or the given object is <code>null</code>, the method does nothing.
     * 
     * @param child The child you want to remove
     */
    public void removeChildModelObjectInternal(IModelObject child) {
        // empty default implementation
    }

    @Override
    public MessageList validate(IValidationContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        MessageList list = new MessageList();
        validate(list, context);
        return list;
    }

    /**
     * Validates the policy component and adds any messages generated to the given list. Calls
     * validateSelf() and validateDependences().
     * 
     * @param context provides additional external information that might be necessary to execute
     *            the validation. E.g. the business context, the locale to provide locale specific
     *            message texts, user information
     * 
     * @throws NullPointerException if the list is <code>null</code> and a message is generated.
     * 
     *             This method is model internal, it is not part of the published interface and
     *             should not be used by clients
     */
    public void validate(MessageList list, IValidationContext context) {
        if (!validateSelf(list, context)) {
            return;
        }
        validateDependants(list, context);
    }

    /**
     * Validates the policy component's dependant components and adds any message generated to the
     * given list.
     * <p>
     * The default implementation does nothing. Should be overridden in subclasses.
     * 
     * @param list The message list
     * 
     * @param context provides additional external information that might be necessary to execute
     *            the validation. E.g. the business context, the locale to provide locale specific
     *            message texts, user information
     * @throws NullPointerException if list is <code>null</code> and a message is generated.
     */
    protected void validateDependants(MessageList list, IValidationContext context) {
        // empty default implementation
    }

    /**
     * Validates this policy component's state without validating the dependant components. Adds any
     * message generated to the given list.
     * <p>
     * The default implementation does nothing. Should be overridden in subclasses.
     * 
     * @param list the message list
     * @param context provides additional external information that might be necessary to execute
     *            the validation. E.g. the business context, the locale to provide locale specific
     *            message texts, user information
     * @return <code>true</code> if this model object should continue validating, <code>false</code>
     *         else.
     * @throws NullPointerException if list is <code>null</code> and a message is generated.
     */
    protected boolean validateSelf(MessageList list, IValidationContext context) {
        return true;
    }

    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData <code>true</code> if the policy component
     *            should be initialized with the product defaults.
     * @param productRepository The repository that contains the product components.
     * @param store The store where unresolved references are stored in (so that they can be
     *            resolved after all objects have been initialized from xml).
     */
    public void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, null, null);
    }

    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData <code>true</code> if the policy component
     *            should be initialized with the product defaults.
     * @param productRepository The repository that contains the product components.
     * @param store The store where unresolved references are stored in (so that they can be
     *            resolved after all objects have been initialized from xml).
     * @param xmlCallback An XML callback class which could handle enhanced xml initialization of
     *            the current element.
     */
    public void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store,
            XmlCallback xmlCallback) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, "");
    }

    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData <code>true</code> if the policy component
     *            should be initialized with the product defaults.
     * @param productRepository The repository that contains the product components.
     * @param store The store where unresolved references are stored in (so that they can be
     *            resolved after all objects have been initialized from xml).
     * @param xmlCallback An XML callback class which could handle enhanced xml initialization of
     *            the current element.
     * @param currPath The path inside the XML tree structure. Starting from the root element.
     */
    protected void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store,
            XmlCallback xmlCallback,
            String currPath) {
        String objectId = objectEl.getAttribute("objectId");
        if (store != null && objectId != null) {
            store.putObject(objectId, this);
        }
        NodeList nl = objectEl.getChildNodes();
        HashMap<String, String> propMap = createPropertiesMap(nl);
        initPropertiesFromXml(propMap, productRepository);

        // if the a callback class is given then perform further initialization using the property
        // map
        String pathFromAggregateRoot = currPath == null ? null : currPath + "/" + objectEl.getNodeName();
        if (xmlCallback != null) {
            xmlCallback.initProperties(pathFromAggregateRoot, this, propMap);
        }
        initAssociations(initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, objectId,
                nl, pathFromAggregateRoot);
    }

    private void initAssociations(boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store,
            XmlCallback xmlCallback,
            String objectId,
            NodeList nl,
            String pathFromAggregateRoot) {
        for (int i = 0, max = nl.getLength(); i < max; i++) {
            if (!(nl.item(i) instanceof Element)) {
                continue;
            }
            Element el = (Element)nl.item(i);
            String type = el.getAttribute("type");
            if ("association".equals(type)) {
                initAssociationFromXml(el, objectId, store);
            } else if ("composite".equals(type)) {
                AbstractModelObject newChild = createChildFromXml(el);
                if (newChild == null) {
                    throw new NullPointerException(
                            "Object: " + this + ", can't create child object, xml element: " + el);
                }
                newChild.initFromXml(el, initWithProductDefaultsBeforeReadingXmlData, productRepository, store,
                        xmlCallback, pathFromAggregateRoot);
            } else if (!("property".equals(type))) {
                throw new RuntimeException("Unknown type " + type);
            }
        }
    }

    private HashMap<String, String> createPropertiesMap(NodeList nl) {
        HashMap<String, String> propMap = new HashMap<>();
        for (int i = 0, max = nl.getLength(); i < max; i++) {
            if (!(nl.item(i) instanceof Element)) {
                continue;
            }
            Element el = (Element)nl.item(i);
            String type = el.getAttribute("type");
            if ("property".equals(type)) {
                putPropertyFromXml(el, propMap);
            }
        }
        return propMap;
    }

    private void putPropertyFromXml(Element el, HashMap<String, String> propMap) {
        String propName = el.getNodeName();
        propMap.put(propName, ValueToXmlHelper.getValueFromElement(el));
    }

    /**
     * @param propMap The property map
     * @param productRepository the runtime repository
     */
    protected void initPropertiesFromXml(Map<String, String> propMap, IRuntimeRepository productRepository) {
        // default implementation does nothing
    }

    /**
     * @param childEl The child element
     */
    protected AbstractModelObject createChildFromXml(Element childEl) {
        return null;
    }

    protected void initAssociationFromXml(Element el, Object objectId, IObjectReferenceStore store) {
        String targetRole = el.getNodeName();
        String targetId = el.getAttribute("targetId");
        IUnresolvedReference reference;
        try {
            reference = createUnresolvedReference(objectId, targetRole, targetId);
            if (reference == null) {
                throw new NullPointerException();
            }
            // CSOFF: IllegalCatch
            // Any exception should be catched and wrapped in a new RuntimeException
        } catch (Exception e) {
            throw new RuntimeException("Object: " + this + ", cannot create unresolved reference for xml element " + el,
                    e);
            // CSON: IllegalCatch
        }
        store.addUnresolvedReference(reference);
    }

    /**
     * 
     * @param objectId object id
     * @param targetRole target role
     * @param targetId target id
     */
    protected IUnresolvedReference createUnresolvedReference(Object objectId, String targetRole, String targetId)
            throws Exception {
        return null;
    }

}
