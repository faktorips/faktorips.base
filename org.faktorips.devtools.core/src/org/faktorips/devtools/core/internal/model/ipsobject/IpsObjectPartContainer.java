/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ValidationResultCache;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.memento.XmlMemento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link IIpsObjectPartContainer}.
 * 
 * @see IIpsObjectPartContainer
 * 
 * @author Jan Ortmann
 */
public abstract class IpsObjectPartContainer extends IpsElement implements IIpsObjectPartContainer,
        IExtensionPropertyAccess {

    /** Name of the XML element the containing the elements for the extension property values. */
    protected final static String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties"; //$NON-NLS-1$

    /** Name of the XML element containing a property value. */
    protected final static String XML_VALUE_ELEMENT = "Value"; //$NON-NLS-1$

    /** Name of the value element's attribute that stores the property id. */
    protected final static String XML_ATTRIBUTE_EXTPROPERTYID = "id"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the information if the value is
     * <code>null</code> or not.
     */
    protected final static String XML_ATTRIBUTE_ISNULL = "isNull"; //$NON-NLS-1$

    /** Set containing all labels attached to the object part container. */
    private final Set<ILabel> labels = new LinkedHashSet<ILabel>();

    /** Set containing all descriptions attached to the object part container. */
    private final Set<IDescription> descriptions = new LinkedHashSet<IDescription>();

    /** Map containing extension property IDs as keys and their values. */
    private HashMap<String, Object> extPropertyValues = null;

    /** Validation start time used for tracing in debug mode */
    private long validationStartTime;

    public IpsObjectPartContainer(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * Only for testing purposes.
     */
    public IpsObjectPartContainer() {
        super();
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        IIpsObject obj = getIpsObject();
        if (obj == null) {
            return null;
        }
        return obj.getIpsSrcFile();
    }

    /**
     * This implementation returns the labels and descriptions of the IPS object part container. It
     * might be extended by subclasses using the method {@link #getChildrenThis()}.
     */
    @Override
    public final IIpsElement[] getChildren() {
        List<IIpsElement> children = new ArrayList<IIpsElement>(labels.size() + descriptions.size());
        children.addAll(labels);
        children.addAll(descriptions);
        IIpsElement[] subclassChildren = getChildrenThis();
        children.addAll(Arrays.asList(subclassChildren));
        return children.toArray(new IIpsElement[children.size()]);
    }

    /**
     * Subclass implementation that can be used to extend the method {@link #getChildren()}. Must
     * not return <tt>null</tt>.
     */
    protected abstract IIpsElement[] getChildrenThis();

    /**
     * Returns the id that can be used for a new part, so that its id is unique.
     */
    protected String getNextPartId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        return extPropertyValues.get(propertyId);
    }

    @Override
    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        initExtPropertiesIfNotDoneSoFar();
        return extPropertyValues.containsKey(propertyId);
    }

    @Override
    public void setExtPropertyValue(String propertyId, Object value) {
        checkExtProperty(propertyId);
        IExtensionPropertyDefinition property = getIpsModel().getExtensionPropertyDefinition(getClass(), propertyId,
                true);
        if (!property.beforeSetValue(this, value)) {
            return; // veto to set the new value by the property definition
        }
        if (!ObjectUtils.equals(value, getExtPropertyValue(propertyId))) {
            extPropertyValues.put(propertyId, value);
            objectHasChanged();
        }
        property.afterSetValue(this, value);
    }

    /**
     * Informs the entire system that this model object has been changed. Subclasses should fire
     * this event if any property of the object changes.
     * 
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     * 
     * @return A flag indicating whether the valueChanged event has been fired successfully (it does
     *         not if the old value and new value are considered to be equal).
     */
    protected final boolean valueChanged(Object oldValue, Object newValue) {
        boolean changed = !ObjectUtils.equals(oldValue, newValue);
        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * @see #valueChanged(Object, Object)
     */
    protected final boolean valueChanged(boolean oldValue, boolean newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * @see #valueChanged(Object, Object)
     */
    protected final boolean valueChanged(int oldValue, int newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * Called when the object's state has changed to inform about this.
     */
    protected abstract void objectHasChanged();

    /**
     * Has to be called when a part was added to the container to trigger event notification.
     */
    protected void partWasAdded(IIpsObjectPart part) {
        objectHasChanged(ContentChangeEvent.newPartAddedEvent(part));
    }

    protected void partsMoved(IIpsObjectPart[] parts) {
        ContentChangeEvent event = ContentChangeEvent.newPartsChangedPositionsChangedEvent(getIpsSrcFile(), parts);
        objectHasChanged(event);
    }

    protected void objectHasChanged(ContentChangeEvent event) {
        IpsModel model = (IpsModel)getIpsModel();
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content != null) {
            content.ipsObjectChanged(event);
        } else {
            model.ipsSrcFileContentHasChanged(event);
        }

    }

    /**
     * This method executes the logic that is implemented in the provided
     * {@link SingleEventModification} and makes sure that only the {@link ContentChangeEvent} that
     * is provided by the {@link SingleEventModification} is fired. No events are fired during the
     * method execution.
     * 
     * @throws CoreException delegates the exceptions from the execute() method of the
     *             {@link SingleEventModification}
     */
    protected <T> T executeModificationsWithSingleEvent(SingleEventModification<T> modifications) throws CoreException {
        boolean successful = false;
        try {
            ((IpsModel)getIpsModel()).stopBroadcastingChangesMadeByCurrentThread();
            successful = modifications.execute();
        } catch (CoreException e) {
            throw e;
        } finally {
            ((IpsModel)getIpsModel()).resumeBroadcastingChangesMadeByCurrentThread();
        }
        if (successful) {
            IpsSrcFileContent content = ((IpsModel)getIpsModel()).getIpsSrcFileContent(getIpsSrcFile());
            if (content != null) {
                content.markAsUnmodified();
            }
            objectHasChanged(modifications.modificationEvent());
        }
        return modifications.getResult();
    }

    private void checkExtProperty(String propertyId) {
        if (!isExtPropertyDefinitionAvailable(propertyId)) {
            throw new IllegalArgumentException(
                    "Extension property " + propertyId + " is not defined for type " + getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private void initExtPropertiesIfNotDoneSoFar() {
        if (extPropertyValues == null) {
            extPropertyValues = new HashMap<String, Object>();
            IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
            for (IExtensionPropertyDefinition propertie : properties) {
                extPropertyValues.put(propertie.getPropertyId(), propertie.getDefaultValue());
            }
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element newElement = createElement(doc);
        propertiesToXml(newElement);
        extPropertiesToXml(newElement);
        partsToXml(doc, newElement);
        return newElement;
    }

    private void extPropertiesToXml(Element element) {
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        if (properties.length == 0) {
            return;
        }
        initExtPropertiesIfNotDoneSoFar();
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(XML_EXT_PROPERTIES_ELEMENT);
        element.appendChild(extPropertiesEl);
        for (IExtensionPropertyDefinition propertie : properties) {
            Element valueEl = doc.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
            extPropertiesEl.appendChild(valueEl);
            String propertyId = propertie.getPropertyId();
            valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
            Object value = extPropertyValues.get(propertyId);
            valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
            if (value != null) {
                propertie.valueToXml(valueEl, value);
            }
        }
    }

    /**
     * Is called from the toXml() method to create the xml element for this container.
     */
    protected abstract Element createElement(Document doc);

    /**
     * Transforms the parts this container contains to xml elements and adds them to the given xml
     * element.
     * 
     * @param doc xml document used to create new element.
     * 
     * @param element the element to which the part elements should be added.
     */
    private void partsToXml(Document doc, Element element) {
        IIpsElement[] children = getChildren();
        for (IIpsElement element2 : children) {
            IIpsObjectPart part = (IIpsObjectPart)element2;
            Element newPartElement = part.toXml(doc);
            element.appendChild(newPartElement);
        }
    }

    /**
     * The method is called by the toXml() method, so that subclasses can store their properties in
     * the xml element passed as parameter.
     */
    protected abstract void propertiesToXml(Element element);

    @Override
    public void initFromXml(Element element) {
        initFromXml(element, null);
    }

    protected void initFromXml(Element element, String id) {
        initPropertiesFromXml(element, id);
        initPartContainersFromXml(element);
        initExtPropertiesFromXml(element);
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property not exists as definitions then the property will be ignored.
     * 
     * @param propertyId id of the extension property
     * @param extPropertyValue extension property value
     */
    protected void addExtensionPropertyValue(String propertyId, String extPropertyValue) {
        initExtPropertiesIfNotDoneSoFar();
        Object value = null;
        if (extPropertyValue != null) {
            IExtensionPropertyDefinition property = findExtensionProperty(propertyId, getExtensionProperties());
            if (property == null) {
                return;
            }
            value = property.getValueFromString(extPropertyValue);
        }
        extPropertyValues.put(propertyId, value);
    }

    /**
     * The method is called by the initFromXml() method, so that subclasses can load their
     * properties from the xml element passed as parameter.
     * 
     * @param id The value for the id-property of the ips object part or null, if the id should be
     *            generated automatically (preferred).
     */
    protected abstract void initPropertiesFromXml(Element element, String id);

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     * 
     * @param containerEl The &lt;ExtensionProperties&gt; element.
     */
    private void initExtPropertiesFromXml(Element containerEl) {
        extPropertyValues = null;
        initExtPropertiesIfNotDoneSoFar(); // to make sure that new extension properties are
        // initialized with their default.
        Element extPropertiesEl = XmlUtil.getFirstElement(containerEl, XML_EXT_PROPERTIES_ELEMENT);
        if (extPropertiesEl == null) {
            extPropertyValues = null;
            return;
        }
        HashMap<String, IExtensionPropertyDefinition> extPropertyDefinitions = getExtensionProperties();
        NodeList nl = extPropertiesEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getNodeName().equals(IpsObjectPartContainer.XML_VALUE_ELEMENT)) {
                initExtPropertyFromXml((Element)node, extPropertyDefinitions);
            }
        }
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     */
    private void initExtPropertyFromXml(Element valueElement,
            HashMap<String, IExtensionPropertyDefinition> extPropertyDefinitions) {

        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        Object value = null;
        String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
            IExtensionPropertyDefinition property = findExtensionProperty(propertyId, extPropertyDefinitions);
            if (property == null) {
                return;
            }
            value = property.getValueFromXml(valueElement);
        }
        extPropertyValues.put(propertyId, value);
    }

    private HashMap<String, IExtensionPropertyDefinition> getExtensionProperties() {
        HashMap<String, IExtensionPropertyDefinition> propMap = new HashMap<String, IExtensionPropertyDefinition>();
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        for (IExtensionPropertyDefinition propertie : properties) {
            propMap.put(propertie.getPropertyId(), propertie);
        }
        return propMap;
    }

    /**
     * Searches an extension property using the given id. Returns null if no such extension property
     * exists.
     */
    private IExtensionPropertyDefinition findExtensionProperty(String propertyId,
            HashMap<String, IExtensionPropertyDefinition> extPropertyDefinitions) {

        IExtensionPropertyDefinition property = extPropertyDefinitions.get(propertyId);
        if (property == null) {
            IpsPlugin.log(new IpsStatus(IStatus.WARNING,
                    "Extension property " + propertyId + " for " + this + " is unknown")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        return property;
    }

    private void initPartContainersFromXml(Element element) {
        HashMap<String, IIpsObjectPart> idPartMap = createIdPartMap();
        Set<String> idSet = new HashSet<String>();
        reinitPartCollections();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element partEl = (Element)item;
            if (partEl.getNodeName().equals(XML_EXT_PROPERTIES_ELEMENT)) {
                continue;
            }
            String id = partEl.getAttribute("id").trim(); //$NON-NLS-1$
            IIpsObjectPart part = idPartMap.get(id);
            if (part == null) {
                part = newPart(partEl, getNextPartId());
            } else {
                boolean added = addPart(part);
                if (!(added)) {
                    throw new IllegalArgumentException("Could not re-add part " + part); //$NON-NLS-1$
                }
            }
            // part might be null if the element does not represent a part!
            if (part != null) {
                part.initFromXml(partEl);
                if (!idSet.add(part.getId())) {
                    throw new RuntimeException("Duplicated Part-ID in Object " + part.getParent().getName() + ", ID: " //$NON-NLS-1$ //$NON-NLS-2$
                            + part.getId());
                }
            }
        }
        return;
    }

    private HashMap<String, IIpsObjectPart> createIdPartMap() {
        HashMap<String, IIpsObjectPart> map = new HashMap<String, IIpsObjectPart>();
        IIpsElement[] parts = getChildren();
        for (IIpsElement part2 : parts) {
            IIpsObjectPart part = (IIpsObjectPart)part2;
            map.put(part.getId(), part);
        }
        return map;
    }

    /**
     * This method is called during the process of initialization from XML. All collections that
     * hold references to {@link IIpsObjectPart}s need to be cleared. It might be extended by
     * subclasses by overriding the method {@link #reinitPartCollectionsThis()}.
     */
    protected final void reinitPartCollections() {
        labels.clear();
        descriptions.clear();
        reinitPartCollectionsThis();
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #reinitPartCollections()}.
     */
    protected abstract void reinitPartCollectionsThis();

    /**
     * Adds the given part to the container. Returns <tt>true</tt> if successfully added,
     * <tt>false</tt> otherwise.
     * <p>
     * This method is called during the process of initialization from XML when the part has been
     * part of the parent before the XML initialization and is still found in the XML file (the
     * part's id is found in the XML file).
     * 
     * @param part The {@link IIpsObjectPart} to add to this container.
     */
    protected final boolean addPart(IIpsObjectPart part) {
        if (part instanceof ILabel && hasLabelSupport()) {
            labels.add((ILabel)part);
            return true;

        } else if (part instanceof IDescription && hasDescriptionSupport()) {
            descriptions.add((IDescription)part);
            return true;
        }
        return addPartThis(part);
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #addPart(IIpsObjectPart)}.
     * <p>
     * The given part must be added to the correct collection.
     * 
     * @param part The {@link IIpsObjectPart} to add to this container.
     */
    protected abstract boolean addPartThis(IIpsObjectPart part);

    /**
     * Removes the given part from the container. Returns <tt>true</tt> if removed, <tt>false</tt>
     * otherwise.
     * <p>
     * Subclasses may extend this method by using the method {@link #removePartThis(IIpsObjectPart)}.
     * 
     * @param part The {@link IIpsObjectPart} to remove from this container.
     */
    protected final boolean removePart(IIpsObjectPart part) {
        if (part instanceof ILabel && hasLabelSupport()) {
            labels.remove(part);
            return true;

        } else if (part instanceof IDescription && hasDescriptionSupport()) {
            descriptions.remove(part);
            return true;
        }
        return removePartThis(part);
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #removePart(IIpsObjectPart)}.
     * 
     * @param part The {@link IIpsObjectPart} to remove from this container.
     */
    protected abstract boolean removePartThis(IIpsObjectPart part);

    /**
     * This method is called during the initFromXml processing to create a new part object for the
     * given element with the given id. Subclasses must create the right part based on the XML
     * element, e.g. for IPolicyCmptType: if the element name is <code>Attribute</code> an
     * <code>IAttribute</code> is created.
     * <p>
     * Note: It is <strong>NOT</strong> necessary to fully initialize the part, this is done later
     * by the caller calling initFromXml().
     * <p>
     * Subclasses must not forget to call <tt>super.newPart(xmlTag, id)</tt>.
     * 
     * @return a new part with the given id, or <code>null</code> if the xml tag name is unknown.
     */
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        String nodeName = xmlTag.getNodeName();
        if (nodeName.equals(ILabel.XML_TAG_NAME) && hasLabelSupport()) {
            return newLabel(id);

        } else if (nodeName.equals(IDescription.XML_TAG_NAME) && hasDescriptionSupport()) {
            return newDescription(id);
        }

        return null;
    }

    /**
     * Creates a new {@link IIpsObjectPart} of the given type. If the type is not supported,
     * <tt>null</tt> is returned.
     * <p>
     * Subclasses must not forget to call <tt>super.newPart(partType)</tt>.
     * 
     * @param partType The published interface of the IPS object part that should be created.
     */
    public IIpsObjectPart newPart(Class<? extends IIpsObjectPart> partType) {
        if (partType == Label.class && hasLabelSupport()) {
            return newLabel();

        } else if (partType == Description.class && hasDescriptionSupport()) {
            return newDescription();
        }

        return null;
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        if (isHistoricPartContainer()) {
            return new MessageList();
        }
        MessageList result = beforeValidateThis();
        if (result != null) {
            return result;
        }

        result = new MessageList();
        validateThis(result, ipsProject);

        afterValidateThis(result, ipsProject);
        return result;
    }

    /**
     * Before validation method. Perform operations which will be executed before this object part
     * container will be validated. Returns the cached validation result for the given container or
     * <code>null</code> if the cache does not contain a result for the container.
     */
    protected MessageList beforeValidateThis() {
        MessageList result = getValidationCache().getResult(this);
        if (result != null) {
            if (IpsModel.TRACE_VALIDATION) {
                System.out.println("Validation of " + this + ": Got result from cache."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return result;
        }

        if (IpsModel.TRACE_VALIDATION) {
            validationStartTime = System.currentTimeMillis();
            System.out.println("Validation of " + this + ": Started."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

    /**
     * After validation method. Perform operations which will be executed after validation of this
     * object part container.
     */
    protected void afterValidateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        validateExtensionProperties(result);
        validateChildren(result, ipsProject);
        if (IpsModel.TRACE_VALIDATION) {
            System.out
                    .println("Validation of " + this + ": Finished, took " + (System.currentTimeMillis() - validationStartTime) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            validationStartTime = -1;
        }
        getValidationCache().putResult(this, result);
    }

    private ValidationResultCache getValidationCache() {
        IpsModel model = (IpsModel)getIpsModel();
        ValidationResultCache cache = model.getValidationResultCache();
        return cache;
    }

    /**
     * Validates part container's children.
     */
    protected void validateChildren(MessageList result, IIpsProject ipsProject) throws CoreException {
        IIpsElement[] children = getChildren();
        for (IIpsElement element : children) {
            MessageList childResult = ((IpsObjectPartContainer)element).validate(ipsProject);
            result.add(childResult);
        }
    }

    /**
     * Returns true if this <code>IpsObjectPartContainer</code> is part of an
     * <code>IIpsSrcFile</code> that is marked as historic. If no srcfile can be found, false is
     * returned.
     * 
     * @return True only if the parent srcfile is historic, false otherwise.
     */
    private boolean isHistoricPartContainer() {
        IIpsElement container = this;
        while (container != null) {
            if (container instanceof IIpsSrcFile && ((IIpsSrcFile)container).isHistoric()) {
                return true;
            }
            container = container.getParent();
        }
        return false;
    }

    /**
     * Validates the object and reports invalid states by adding validation messages to the list.
     * This is an application of the collecting parameter pattern.
     * 
     * @param list The message list containing all validation messages - if you overwrite this
     *            method you must add your validation messages to this list.
     * @param ipsProject The context IPS project. The validation might be called from a different
     *            IPS project than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the IPS project of the caller for finder-methods that are
     *            used within the implementation of this method.
     * 
     * @throws CoreException Subclasses may wrap any occurring exceptions into a CoreException and
     *             propagate it trough this method.
     * @throws NullPointerException if list is <code>null</code>.
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        return;
    }

    /**
     * Validates the extension property values.
     * 
     * @param ml The message list to which messages generated during the validation are added.
     * 
     * @throws CoreException if an error occurs while validation the extension properties.
     */
    protected void validateExtensionProperties(MessageList ml) throws CoreException {
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        for (IExtensionPropertyDefinition propertie : properties) {
            Object value = getExtPropertyValue(propertie.getPropertyId());
            MessageList newList = propertie.validate(this, value);
            if (newList != null) {
                ml.add(newList);
            }
        }
    }

    @Override
    public Memento newMemento() {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        return new XmlMemento(this, toXml(doc));
    }

    @Override
    public void setState(Memento memento) {
        if (!memento.getOriginator().equals(this)) {
            throw new IllegalArgumentException("Memento " + memento + " wasn't created by " + this); //$NON-NLS-1$ //$NON-NLS-2$
        }

        initFromXml(((XmlMemento)memento).getState());
        objectHasChanged();
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Helper to easily add details to the given map.
     * 
     * @param details The map of dependencies to the list of details.
     * @param dependency The dependency to add the details for
     * @param part The details part
     * @param propertyName The details property name
     */
    protected void addDetails(Map<IDependency, List<IDependencyDetail>> details,
            IDependency dependency,
            IIpsObjectPartContainer part,
            String propertyName) {

        if (details == null) {
            return;
        }
        List<IDependencyDetail> detailList = details.get(dependency);
        if (detailList == null) {
            detailList = new ArrayList<IDependencyDetail>();
            details.put(dependency, detailList);
        }
        detailList.add(new DependencyDetail(part, propertyName));
    }

    private ILabel newLabel(String id) {
        ILabel newLabel = new Label(this, id);
        labels.add(newLabel);
        return newLabel;
    }

    private IDescription newDescription(String id) {
        IDescription newDescription = new Description(this, id);
        descriptions.add(newDescription);
        return newDescription;
    }

    /**
     * The default implementation always returns <tt>false</tt>.
     */
    @Override
    public boolean hasDescriptionSupport() {
        return false;
    }

    /**
     * The default implementation always returns <tt>false</tt>.
     */
    @Override
    public boolean hasLabelSupport() {
        return false;
    }

    @Override
    public boolean isPluralLabelSupported() {
        return false;
    }

    @Override
    public ILabel getLabel(Locale locale) {
        ArgumentCheck.notNull(locale);
        for (ILabel label : labels) {
            Locale labelLocale = label.getLocale();
            if (labelLocale == null) {
                continue;
            }
            if (locale.getLanguage().equals(labelLocale.getLanguage())) {
                return label;
            }
        }
        return null;
    }

    @Override
    public Set<ILabel> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    @Override
    public ILabel getLabelForIpsModelLocale() {
        Locale ipsModelLocale = IpsPlugin.getDefault().getIpsModelLocale();
        return getLabel(ipsModelLocale);
    }

    @Override
    public ILabel getLabelForDefaultLocale() {
        ILabel defaultLabel = null;
        ISupportedLanguage defaultLanguage = getIpsProject().getProperties().getDefaultLanguage();
        if (defaultLanguage != null) {
            defaultLabel = getLabel(defaultLanguage.getLocale());
        }
        return defaultLabel;
    }

    @Override
    public ILabel newLabel() {
        if (!(hasLabelSupport())) {
            throw new UnsupportedOperationException("This IPS object part container does not support labels."); //$NON-NLS-1$
        }
        return newLabel(getNextPartId());
    }

    @Override
    public IDescription getDescription(Locale locale) {
        ArgumentCheck.notNull(locale);
        for (IDescription description : descriptions) {
            Locale descriptionLocale = description.getLocale();
            if (descriptionLocale == null) {
                continue;
            }
            if (locale.getLanguage().equals(descriptionLocale.getLanguage())) {
                return description;
            }
        }
        return null;
    }

    @Override
    public Set<IDescription> getDescriptions() {
        return Collections.unmodifiableSet(descriptions);
    }

    @Override
    public IDescription getDescriptionForIpsModelLocale() {
        Locale ipsModelLocale = IpsPlugin.getDefault().getIpsModelLocale();
        return getDescription(ipsModelLocale);
    }

    @Override
    public IDescription getDescriptionForDefaultLocale() {
        IDescription defaultDescription = null;
        ISupportedLanguage defaultLanguage = getIpsProject().getProperties().getDefaultLanguage();
        if (defaultLanguage != null) {
            defaultDescription = getDescription(defaultLanguage.getLocale());
        }
        return defaultDescription;
    }

    @Override
    public IDescription newDescription() {
        if (!(hasDescriptionSupport())) {
            throw new UnsupportedOperationException("This IPS object part container does not support descriptions."); //$NON-NLS-1$
        }
        return newDescription(getNextPartId());
    }

    @Deprecated
    @Override
    public String getDescription() {
        String description = ""; //$NON-NLS-1$
        Set<IDescription> descriptionSet = getDescriptions();
        if (descriptionSet.size() > 0) {
            IDescription firstDescription = descriptionSet.toArray(new IDescription[descriptionSet.size()])[0];
            description = firstDescription.getText();
        }
        return description;
    }

    @Deprecated
    @Override
    public void setDescription(String newDescription) {
        if (!(isDescriptionChangable())) {
            throw new UnsupportedOperationException();
        }
        if (newDescription == null) {
            // Throw exception as specified by the contract.
            throw new IllegalArgumentException();
        }

        Set<IDescription> descriptionSet = getDescriptions();
        IDescription firstDescription;
        if (descriptionSet.size() == 0) {
            firstDescription = newDescription();
        } else {
            firstDescription = descriptionSet.toArray(new IDescription[descriptionSet.size()])[0];
        }
        firstDescription.setText(newDescription);
    }

    @Deprecated
    @Override
    public final boolean isDescriptionChangable() {
        return hasDescriptionSupport();
    }

    /**
     * An internal abstract class that is used to execute modifications on
     * {@link IpsObjectPartContainer}s that would otherwise cause multiple
     * {@link ContentChangeEvent}s. To suppress the unwanted events and to fire a single event
     * instead when all the modifications are completed one needs to implement this interface and
     * execute it by means of the
     * {@link IpsObjectPartContainer#executeModificationsWithSingleEvent(SingleEventModification)}
     * method.
     * 
     * @author Peter Kuntz
     */
    public abstract class SingleEventModification<T> {

        /**
         * Is called by the framework. The modifications are supposed to be implemented within this
         * method.
         * 
         * @return true if the modifications have been successful and an event needs to be fired
         *         afterwards
         * 
         * @throws CoreException exceptions within this method
         */
        protected abstract boolean execute() throws CoreException;

        /**
         * Returns the {@link ContentChangeEvent} that is fired after the {@link #execute()} method
         * has been executed. By default a whole content change event is fired.
         */
        protected ContentChangeEvent modificationEvent() {
            return ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
        }

        /**
         * Returns the result of the execution if available. Returns <code>null</code> if the
         * execution doesn't have a result that needs to be returned.
         * 
         * @return the result of the execution or <code>null</code> if none needs to be returned
         */
        protected abstract T getResult();

    }

}
