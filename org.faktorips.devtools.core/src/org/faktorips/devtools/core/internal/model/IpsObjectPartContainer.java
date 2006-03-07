package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A container for ips object parts.
 * 
 * @author Jan Ortmann
 */
public abstract class IpsObjectPartContainer extends IpsElement implements IIpsObjectPartContainer, IExtensionPropertyAccess {

    /**
     * Name of the xml element the containing the elements for the extension property values.
     */
    protected final static String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties"; //$NON-NLS-1$

    /**
     * Name of the xml element containing a property value.
     */
    protected final static String XML_VALUE_ELEMENT = "Value"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the property id.
     */
    protected final static String XML_ATTRIBUTE_EXTPROPERTYID = "id"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the information if the value is null or not.
     */
    protected final static String XML_ATTRIBUTE_ISNULL = "isNull"; //$NON-NLS-1$
    
    
    // map containing extension property ids as keys and their values.
    private HashMap extPropertyValues = null;

    public IpsObjectPartContainer(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * for testing purposes
     */
    public IpsObjectPartContainer() {
        super();
    }

    /**
     * Overridden IMethod. The IpsObjectPartContainer version does not throw an exception as no
     * resource access is neccessary.
     *
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public abstract IIpsElement[] getChildren();
    
    /**
     * Returns the id that can be used for a new part, so that it's id is unique.
     */
    protected int getNextPartId() {
        int maxId = -1;
        IIpsElement[] parts = getChildren();
        for (int i = 0; i < parts.length; i++) {
            IIpsObjectPart part = (IIpsObjectPart)parts[i];
            if (part.getId()>maxId) {
                maxId = part.getId();
            }
        }
        return ++maxId;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyAccess#getExtPropertyValue(java.lang.String)
     */
    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        return extPropertyValues.get(propertyId);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyAccess#setExtPropertyValue(java.lang.String, java.lang.Object)
     */
    public void setExtPropertyValue(String propertyId, Object value) {
        checkExtProperty(propertyId);
        IExtensionPropertyDefinition property = getIpsModel().getExtensionPropertyDefinition(getClass(), propertyId, true);
        if (!property.beforeSetValue(this, value)) {
            return; // veto to set the new value by the property definition
        }
        if (!ObjectUtils.equals(value, getExtPropertyValue(propertyId))) { 
            extPropertyValues.put(propertyId, value);
            updateSrcFile();
        }
        property.afterSetValue(this, value);
    }
    
    /**
     * Updates the source file with the object's state in xml format.
     */
    protected abstract void updateSrcFile();
    
    private void checkExtProperty(String propertyId) {
        initExtPropertiesIfNotDoneSoFar();
        if (!extPropertyValues.containsKey(propertyId)) {
            throw new IllegalArgumentException("Extension property " + propertyId + " is not defined for type " + getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private void initExtPropertiesIfNotDoneSoFar() {
        if (extPropertyValues==null) {
            extPropertyValues = new HashMap();
            IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
            for (int i = 0; i < properties.length; i++) {
                extPropertyValues.put(properties[i].getPropertyId(), properties[i].getDefaultValue());
            }
        }
    }
    
    /** 
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.XmlSupport#toXml(org.w3c.dom.Document)
     */
    public Element toXml(Document doc) {
        Element newElement = createElement(doc);
        propertiesToXml(newElement);
        extPropertiesToXml(newElement);
        partsToXml(doc, newElement);
        return newElement;
    }
    
    private void extPropertiesToXml(Element element) {
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        if (properties.length==0) {
            return;
        }
        initExtPropertiesIfNotDoneSoFar();        
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(XML_EXT_PROPERTIES_ELEMENT);
        element.appendChild(extPropertiesEl);
        for (int i = 0; i < properties.length; i++) {
            Element valueEl = doc.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
            extPropertiesEl.appendChild(valueEl);
            String propertyId = properties[i].getPropertyId();
            valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
            Object value = extPropertyValues.get(propertyId);
            valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value==null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
            if (value!=null) {
                properties[i].valueToXml(valueEl, value);
            }
        }
    }

    /**
     * Is called from the toXml() method to create the xml element for this container.
     */
    protected abstract Element createElement(Document doc);

    /*
     * Transforms the parts this container contains to xml elements and adds
     * them to the given xml element.
     * 
     * @param doc xml document used to create new element.
     * @param element the element to which the part elements should be added.
     */
    private void partsToXml(Document doc, Element element) {
        IIpsElement[] children = getChildren();
        for (int i = 0; i < children.length; i++) {
            IIpsObjectPart part = (IIpsObjectPart)children[i];
            Element newPartElement = part.toXml(doc);
            element.appendChild(newPartElement);
        }
    }

    /**
     * The method is called by the toXml() method, so that subclasses can store their properties in 
     * the xml element passed as parameter.
     * 
     * @param element
     */
    protected abstract void propertiesToXml(Element element);

    /** 
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.XmlSupport#initFromXml(org.w3c.dom.Element)
     */
    public void initFromXml(Element element) {
    	initFromXml(element, null);
    }
    
    protected void initFromXml(Element element, Integer id) {
        initPropertiesFromXml(element, id);
        initPartContainersFromXml(element);
        initExtPropertiesFromXml(element);
    }

    /**
     * The method is called by the initFromXml() method, so that subclasses can load their properties from
     * the xml element passed as parameter.
     * 
     * @param element 
     * @param id The value for the id-property of the ips object part or null, if the id should be generated 
     *           automatically (preferred).
     */
    protected abstract void initPropertiesFromXml(Element element, Integer id);

    /*
     * The method is called by the initFromXml() method to retrieve the values of the
     * extension properties.
     * 
     * @param element The &ltExtensionProperties&gt element.
     */
    private void initExtPropertiesFromXml(Element containerEl) {
        extPropertyValues = null;
        initExtPropertiesIfNotDoneSoFar(); // to make sure that new extension properties are initialized with their default.
        Element extPropertiesEl = XmlUtil.getFirstElement(containerEl, XML_EXT_PROPERTIES_ELEMENT);
        if (extPropertiesEl==null) {
            extPropertyValues = null;
            return;
        }
        HashMap extPropertyDefinitions = getExtensionProperties();
        NodeList nl = extPropertiesEl.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals(IpsObjectPartContainer.XML_VALUE_ELEMENT)) {
                initExtPropertyFromXml((Element)node, extPropertyDefinitions);
            }
        }
    }
    
    /*
     * The method is called by the initFromXml() method to retrieve the values of the
     * extension properties.
     * 
     * @param element The &ltExtensionProperties&gt element.
     */
    private void initExtPropertyFromXml(Element valueElement, HashMap extPropertyDefinitions) {
        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        Object value = null;
        String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
            IExtensionPropertyDefinition property = (IExtensionPropertyDefinition)extPropertyDefinitions.get(propertyId);
            if (property==null) {
                throw new RuntimeException("Extension property " + propertyId + " for " + this + " is unknown"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            value = property.getValueFromXml(valueElement);
        }
        extPropertyValues.put(propertyId, value);
    }

    private HashMap getExtensionProperties() {
        HashMap propMap = new HashMap();
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        for (int i = 0; i < properties.length; i++) {
            propMap.put(properties[i].getPropertyId(), properties[i]);
        }
        return propMap;
    }

    private void initPartContainersFromXml(Element element) {
        HashMap idPartMap = createIdPartMap();
        reinitPartCollections();
        NodeList nl = element.getChildNodes();
        int nextId = getMaxIdUsedInXml(element) + 1;
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType()!=Node.ELEMENT_NODE) {
                continue;
            }
            Element partEl = (Element)nl.item(i);
            if (partEl.getNodeName().equals(XML_EXT_PROPERTIES_ELEMENT)) {
            	continue;
            }
            String id = partEl.getAttribute("id").trim(); //$NON-NLS-1$
            IIpsObjectPart part = (IIpsObjectPart)idPartMap.get(id);
            if (part==null) {
                part = newPart(partEl, nextId++);
            } else {
                reAddPart(part);
            }
            if (part!=null) {
                // part may be null if the element does not represent a part!
                part.initFromXml(partEl);
            }
        }
        return;
    }

    private HashMap createIdPartMap() {
        HashMap map = new HashMap();
        IIpsElement[] parts = getChildren();
        for (int i = 0; i < parts.length; i++) {
            IIpsObjectPart part = (IIpsObjectPart)parts[i];
            map.put(""+part.getId(), part); //$NON-NLS-1$
        }
        return map;
    }

    private int getMaxIdUsedInXml(Element element) {
        int maxId = 0;
        NodeList nl = element.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType()==Node.ELEMENT_NODE) {
                Element partEl = (Element)nl.item(i);
                String id = partEl.getAttribute("id"); //$NON-NLS-1$
                if (!StringUtils.isEmpty(id)) {
                    int partId = Integer.parseInt(id);
                    if (partId > maxId) {
                        maxId = partId;
                    }
                }
            }
        }
        return maxId;
    }

    /**
     * This method is called during the initFromXml processing. Subclasses should clear all
     * collections that hold references to parts, e.g. for IPolicyCmptType: Collections for 
     * attributes, methods and so on have to be cleared.
     */
    protected abstract void reinitPartCollections();

    /**
     * This method is called during the initFromXml processing, when the part has been part
     * of the parent before the xml initialization and is still be found in the xml (the part's id
     * is found in the xml). Subclasses must override this method so that the part is added to
     * the right collection, e.g. for IPolicyCmptType: if the part is an IAttribute, the part must be
     * added to the <code>attributes</code> list.
     * 
     * @throws RuntimeException if the part can't be readded, e.g. because it's type is unknown.
     */
    protected abstract void reAddPart(IIpsObjectPart part);

    /**
     * This method is called during the initFromXml processing to create a new part object for 
     * the given element with the given id. Subclasses must create the right part based on
     * the xml element, e.g. for IPolicyCmptType: if the element name is <code>IAttribute</code>
     * an <code>IAttribute</code> is created.
     * <p>
     * Note: It is <strong>NOT</strong> neccessary to fully initialize the the part, this is 
     * done later by the caller calling initFromXml().
     * 
     * @return a new part with the given id, or <code>null</code> if the xml tag name is unknown.
     */
    protected abstract IIpsObjectPart newPart(Element xmlTag, int id);
    
    /**
     * Validates the extension property values.
     * 
     * @param ml The message list to which messages generated during the validation are added.
     * 
     * @throws CoreException if an error occurs while validation the extension properties.
     */
    protected void validateExtensionProperties(MessageList ml) throws CoreException {
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        for (int i = 0; i < properties.length; i++) {
        	Object value = getExtPropertyValue(properties[i].getPropertyId());
        	MessageList newList = properties[i].validate(this, value);
        	if (newList!=null) {
        		ml.add(newList);
        	}
        }
    }

}
