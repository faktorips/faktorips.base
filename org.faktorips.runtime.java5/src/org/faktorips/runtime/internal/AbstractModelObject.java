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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectChangeListener;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Abstract base class for all policy component types.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractModelObject implements IModelObject {

    private List<IModelObjectChangeListener> changeListeners = null;
    
    public AbstractModelObject() {
        super();
    }
    
    /**
     * Removes the given child object from this object. If the given object is not a child of this
     * model object or the given object is <code>null</code>, the method does nothing.
     */
    public void removeChildModelObjectInternal(IModelObject child) {
        // default implementation does nothing
    }

    /**
     * @inheritDoc
     */
    public MessageList validate(String businessFunction) {
        MessageList list = new MessageList();
        validate(list, businessFunction);
        return list;
    }

    /**
     * Validates the policy component and adds any messages generated to the given list. Calls
     * validateSelf() and validateDependences().
     *
     * @param businessFunction a rule might check against the provided business function if it is to
     *          be applied
     * @throws NullPointerException
     *             if list is <code>null</code> and a message is generated.
     */
    public void validate(MessageList list, String businessFunction) {
        if(!validateSelf(list, businessFunction)){
            return;
        }
        validateDependants(list, businessFunction);
    }

    /**
     * Validates the policy component's dependant components and adds any message 
     * generated to the given list.
     * <p>
     * The default implementation does nothing. Should be overriden in subclasses.
     * 
     * @param businessFunction a rule might check against the provided business function if it is to
     *          be applied
     * @throws NullPointerException
     *             if list is <code>null</code> and a message is generated.
     */
    protected void validateDependants(MessageList list, String businessFunction) {
    }

    /**
     * Validates this policy component's state wihtout validating the dependant components. Adds any
     * message generated to the given list.
     * <p>
     * The default implementation does nothing. Should be overriden in subclasses.
     * 
     * @param businessFunction a rule might check against the provided business function if it is to
     *          be applied
     * @throws NullPointerException
     *             if list is <code>null</code> and a message is generated.
     */
    protected boolean validateSelf(MessageList list, String businessFunction) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void addChangeListener(IModelObjectChangeListener listener) {
        if (changeListeners==null) {
            changeListeners = new ArrayList<IModelObjectChangeListener>(1);
        }
        changeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeChangeListener(IModelObjectChangeListener listener) {
        if (changeListeners==null) {
            return;
        }
        changeListeners.remove(listener);
    }
    
    /**
     * Returns <code>true</code> if at least one change listener is registered in this policy
     * component or one of it's parents that needsto be informed about changes, 
     * otherwise <code>false</code>.
     */
    public boolean existsChangeListenerToBeInformed() {
        return changeListeners!=null && !changeListeners.isEmpty();
    }
    
    /**
     * Notifies all registered change listeners that this policy component has changed.
     * 
     * <strong>Note that it is the listener's responsibility to implement a proper exception handling!!!
     * This method will squeeze any exceptions thrown by any listener and go on to notify the others.</strong>
     * 
     * @param event The event to broadcast
     * 
     * @throws NullPointerException if event is <code>null</code>.
     */
    public void notifyChangeListeners(IModelObjectChangedEvent event) {
        if (event==null) {
            throw new NullPointerException();
        }
        if (changeListeners==null) {
            return;
        }
        List<IModelObjectChangeListener> listeners = new ArrayList<IModelObjectChangeListener>(changeListeners); // copy to avoid concurrent notification exceptions!
        for (Iterator<IModelObjectChangeListener> it=listeners.iterator(); it.hasNext(); ) {
            IModelObjectChangeListener listener = it.next();
            try {
                listener.modelObjectChanged(event);
            } catch (Exception e) {
                // squeeze excpetion, see javadoc!
            }
        }
    }

    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl 
     *      Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData
     *      <code>true</code> if the policy component should be initialized with the product defaults.
     * @param productRepository 
     *      The repository that contains the product components.
     * @param store
     *      The store where unresolved references are stored in (so that they can be resolved after all
     *      objects have been intialized from xml).
     */
    public void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, null, null);
    }
    
    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl 
     *      Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData
     *      <code>true</code> if the policy component should be initialized with the product defaults.
     * @param productRepository 
     *      The repository that contains the product components.
     * @param store
     *      The store where unresolved references are stored in (so that they can be resolved after all
     *      objects have been intialized from xml).
     * @param xmlCallback
     *      An XML callback class which could handle enhanced xml initialisation of the current element.
     */    
    public void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store,
            XmlCallback xmlCallback) {
        initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, "");
    }
    
    /**
     * Initializes the policy component's state with the data stored in the given xml element.
     * 
     * @param objectEl 
     *      Xml element containing the state data.
     * @param initWithProductDefaultsBeforeReadingXmlData
     *      <code>true</code> if the policy component should be initialized with the product defaults.
     * @param productRepository 
     *      The repository that contains the product components.
     * @param store
     *      The store where unresolved references are stored in (so that they can be resolved after all
     *      objects have been intialized from xml).
     * @param xmlCallback
     *      An XML callback class which could handle enhanced xml initialisation of the current element.
     * @param currPath
     *      The path inside the XML tree structure. Starting from the root element.
     */
    protected void initFromXml(
            Element objectEl, 
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository, 
            IObjectReferenceStore store,
            XmlCallback xmlCallback,
            String currPath) {
        String objectId = objectEl.getAttribute("objectId");
        if (store!=null && objectId!=null) {
            store.putObject(objectId, this);
        }
        HashMap<String, String> propMap = new HashMap<String, String>();
        NodeList nl = objectEl.getChildNodes();
        for (int i=0, max=nl.getLength(); i<max; i++) {
            if (!(nl.item(i) instanceof Element)) {
                continue;
            }
            Element el = (Element)nl.item(i);
            String type = el.getAttribute("type");
            if (type.equals("property")) {
                initPropertyFromXml(el, propMap);
            } 
        }
        initPropertiesFromXml(propMap);
        
        // if the a callback class is given then perform further initialisation using the property map
        String pathFromAggregateRoot = currPath == null ? null : currPath + "/" + objectEl.getNodeName();
        if (xmlCallback != null) {
            xmlCallback.initProperties(pathFromAggregateRoot, this, propMap);
        }
        
        // now init relations (including composites)
        for (int i=0, max=nl.getLength(); i<max; i++) {
            if (!(nl.item(i) instanceof Element)) {
                continue;
            }
            Element el = (Element)nl.item(i);
            String type = el.getAttribute("type");
            if (type.equals("property")) {
                // already handled
            } else if (type.equals("association")) {
                initAssociationFromXml(el, objectId, store);
            }  else if (type.equals("composite")) {
                AbstractModelObject newChild = createChildFromXml(el);
                if (newChild==null) {
                    throw new NullPointerException("Object: " + this + ", can't create child object, xml element: " + el);
                }
                newChild.initFromXml(el, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback, pathFromAggregateRoot);
            } else {
                throw new RuntimeException("Unknown type " + type);
            }
        }
    }
    
    private void initPropertyFromXml(Element el, HashMap<String, String> propMap) {
        String propName = el.getNodeName();
        String value = null;
        String isNull = el.getAttribute("isNull"); //$NON-NLS-1$
        if (!Boolean.valueOf(isNull).booleanValue()) {
            Text textNode = getTextNode(el);
            if (textNode!=null) {
                value = textNode.getNodeValue();
            }
        }
        propMap.put(propName, value);
    }
    
    protected void initPropertiesFromXml(Map<String, String> propMap) {
    }
    
    protected AbstractModelObject createChildFromXml(Element childEl) {
        return null;
    }
    
    protected void initAssociationFromXml(Element el, Object objectId, IObjectReferenceStore store) {
        String targetRole = el.getNodeName();
        String targetId = el.getAttribute("targetId");
        IUnresolvedReference reference;
        try {
            reference = createUnresolvedReference(objectId, targetRole, targetId);
            if (reference==null) {
                throw new NullPointerException();
            }
        }catch (Exception e) {
            throw new RuntimeException("Object: " + this + ", can't create unresolved reference for xml element " + el, e);
        }
        store.addUnresolvedReference(reference);
    }
    
    protected IUnresolvedReference createUnresolvedReference(
            Object objectId, 
            String targetRole, 
            String targetId) throws Exception {
        return null;
    }
    
    /*
     * Returns the element's first text node.
     */
    private Text getTextNode(Element valueElement) {
        NodeList nl = valueElement.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) instanceof Text) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }
    
}
