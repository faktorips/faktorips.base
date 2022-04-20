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

import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.CustomValidationsResolver;
import org.faktorips.devtools.model.internal.IpsElement;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ValidationResultCache;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.memento.XmlMemento;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for easy implementation of {@link IIpsObjectPartContainer}.
 * <p>
 * <strong>Subclassing:</strong><br>
 * This base class provides the implementations required by the interfaces {@link IDescribedElement}
 * and {@link ILabeledElement}. Therefore, subclasses are able to receive multi-language support
 * just by implementing the corresponding interfaces.
 * 
 * @see IIpsObjectPartContainer
 * 
 * @author Jan Ortmann
 */
public abstract class IpsObjectPartContainer extends IpsElement implements IIpsObjectPartContainer {

    /** Name of the XML element the containing the elements for the extension property values. */
    public static final String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties"; //$NON-NLS-1$

    public static final String PROPERTY_DEPRECATED = "deprecated"; //$NON-NLS-1$

    /** Name of the XML element containing a property value. */
    protected static final String XML_VALUE_ELEMENT = ValueToXmlHelper.XML_TAG_VALUE;

    /** Name of the value element's attribute that stores the property id. */
    protected static final String XML_ATTRIBUTE_EXTPROPERTYID = "id"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the information if the value is
     * <code>null</code> or not.
     */
    protected static final String XML_ATTRIBUTE_ISNULL = ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL;

    /** Name of the object part container's version */
    protected static final String XML_ATTRIBUTE_VERSION = "since"; //$NON-NLS-1$

    /** List containing all labels attached to the object part container. */
    private final List<ILabel> labels = new ArrayList<>(2);

    /** List containing all descriptions attached to the object part container. */
    private final List<IDescription> descriptions = new ArrayList<>(2);

    private final ExtensionPropertyHandler extensionProperties = new ExtensionPropertyHandler(this);

    /** The version since which this part is available. May be <code>null</code>. */
    private String sinceVersion;

    /** Validation start time used for tracing in debug mode */
    private long validationStartTime;

    private IDeprecation deprecation;

    private boolean deprecated = false;

    public IpsObjectPartContainer(IIpsElement parent, String name) {
        super(parent, name);
        if (this instanceof ILabeledElement || this instanceof IDescribedElement) {
            initLabelsAndDescriptions();
        }
        initDefaultVersion();
    }

    /**
     * Only for testing purposes.
     */
    public IpsObjectPartContainer() {
        this(null, null);
    }

    private void initLabelsAndDescriptions() {
        IIpsProject ipsProject = getIpsProject();
        if (ipsProject != null) {
            for (ISupportedLanguage language : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
                Locale locale = language.getLocale();
                if (this instanceof ILabeledElement) {
                    Label label = (Label)newLabel();
                    label.setLocaleWithoutChangeEvent(locale);
                }
                if (this instanceof IDescribedElement) {
                    Description description = (Description)newDescription();
                    description.setLocaleWithoutChangeEvent(locale);
                }
            }
        }
    }

    /* private */void initDefaultVersion() {
        sinceVersion = getDefaultVersion();
    }

    String getDefaultVersion() {
        if (this instanceof IVersionControlledElement && getIpsProject() != null) {
            IVersionProvider<?> versionProvider = getIpsProject().getVersionProvider();
            IVersion<?> version = versionProvider.getProjectVersion();
            if (version.isNotEmptyVersion()) {
                return version.getUnqualifiedVersion();
            }
        }
        return StringUtils.EMPTY;

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
        List<IIpsElement> children = new ArrayList<>(labels.size() + descriptions.size());
        children.addAll(labels);
        children.addAll(descriptions);
        IIpsElement[] subclassChildren = getChildrenThis();
        children.addAll(Arrays.asList(subclassChildren));
        return children.toArray(new IIpsElement[children.size()]);
    }

    /**
     * Subclass implementation that can be used to extend the method {@link #getChildren()}. Must
     * not return <code>null</code>.
     */
    protected abstract IIpsElement[] getChildrenThis();

    /**
     * Returns the id that can be used for a new part, so that its id is unique.
     */
    protected String getNextPartId() {
        return getIpsModel().getNextPartId(this);
    }

    @Override
    public Object getExtPropertyValue(String propertyId) {
        return extensionProperties.getExtPropertyValue(propertyId);
    }

    @Override
    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        return extensionProperties.isExtPropertyDefinitionAvailable(propertyId);
    }

    @Override
    public void setExtPropertyValue(String propertyId, Object value) {
        extensionProperties.setExtPropertyValue(propertyId, value);
    }

    @Override
    public void removeObsoleteExtensionProperties() {
        extensionProperties.removeObsoleteExtensionProperties();
        removeObsoleteExtensionPropertiesOfChilden();
    }

    private void removeObsoleteExtensionPropertiesOfChilden() {
        IIpsElement[] children = getChildren();
        for (IIpsElement child : children) {
            ((IIpsObjectPartContainer)child).removeObsoleteExtensionProperties();
        }
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
        boolean changed = !Objects.equals(oldValue, newValue);
        if (changed) {
            objectHasChanged();
        }

        return changed;
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
    protected final boolean valueChanged(Object oldValue, Object newValue, String propertyName) {
        boolean changed = !Objects.equals(oldValue, newValue);
        if (changed) {
            objectHasChanged(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
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
     * Called when the object's state has changed to inform about this.
     * 
     * @param propertyChangeEvent the propertyChangeEvent details the change.
     */
    protected abstract void objectHasChanged(PropertyChangeEvent propertyChangeEvent);

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

    @Override
    public Element toXml(Document doc) {
        Element newElement = createElement(doc);
        propertiesToXml(newElement);
        extensionProperties.toXml(newElement);
        partsToXml(doc, newElement);
        versionToXML(newElement);
        deprecationInfoToXML(doc, newElement);
        return newElement;
    }

    /**
     * Is called from the {@link #toXml(Document)} method to create the XML element for this
     * container.
     */
    protected abstract Element createElement(Document doc);

    /**
     * Transforms the parts this container contains to XML elements and adds them to the given XML
     * element.
     * 
     * @param doc XML document used to create new elements
     * 
     * @param element the element to which the part elements should be added
     */
    protected void partsToXml(Document doc, Element element) {
        IIpsElement[] children = getChildren();
        for (IIpsElement element2 : children) {
            IIpsObjectPart part = (IIpsObjectPart)element2;
            if (isPartSavedToXml(part)) {
                Element newPartElement = part.toXml(doc);
                element.appendChild(newPartElement);
            }
        }
    }

    /**
     * Returns whether the provided {@link IIpsObjectPart} should be persisted to XML.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The default implementation always returns true.
     * 
     * @param part the {@link IIpsObjectPart} in question to save
     */
    protected boolean isPartSavedToXml(IIpsObjectPart part) {
        return true;
    }

    /**
     * Writes the version to an element
     */
    public void versionToXML(Element element) {
        if (this instanceof IVersionControlledElement && StringUtils.isNotEmpty(sinceVersion)) {
            element.setAttribute(XML_ATTRIBUTE_VERSION, sinceVersion);
        }
    }

    private void deprecationInfoToXML(Document doc, Element element) {
        if (deprecated) {
            Element deprecationElement = ((Deprecation)deprecation).toXml(doc);
            element.appendChild(deprecationElement);
        }
    }

    /**
     * The method is called by the toXml() method, so that subclasses can store their properties in
     * the XML element passed as parameter.
     */
    protected abstract void propertiesToXml(Element element);

    @Override
    public void initFromXml(Element element) {
        initFromXml(element, null);
    }

    protected void initFromXml(Element element, String id) {
        initPropertiesFromXml(element, id);
        initPartContainersFromXml(element);
        initVersionFromXML(element);
        initDeprecationFromXML(element);
        extensionProperties.initFromXml(element);
    }

    private void initVersionFromXML(Element element) {
        if (this instanceof IVersionControlledElement) {
            sinceVersion = element.getAttribute(XML_ATTRIBUTE_VERSION);
        }
    }

    private void initDeprecationFromXML(Element element) {
        NodeList elementsByTagName = element.getElementsByTagName(IDeprecation.XML_TAG);
        Node deprecationNode = elementsByTagName.item(0);
        if (deprecationNode != null) {
            deprecation = newDeprecation();
            deprecation.initFromXml((Element)deprecationNode);
            deprecated = true;
        } else {
            deprecated = false;
        }
    }

    /**
     * The method is called by the initFromXml() method, so that subclasses can load their
     * properties from the XML element passed as parameter.
     * 
     * @param id The value for the id-property of the IPS object part or null, if the id should be
     *            generated automatically (preferred).
     */
    protected abstract void initPropertiesFromXml(Element element, String id);

    @Override
    public Collection<IExtensionPropertyDefinition> getExtensionPropertyDefinitions() {
        Map<String, IExtensionPropertyDefinition> properties = getIpsModel().getExtensionPropertyDefinitions(this);
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(String propertyId) {
        return getIpsModel().getExtensionPropertyDefinitions(this).get(propertyId);
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property not exists as definitions then the property will be ignored.
     * <p>
     * Note: Better do not use this method. The extension property should be initialized by
     * {@link ExtensionPropertyHandler#initFromXml(Element)}.
     * 
     * @param propertyId id of the extension property
     * @param extPropertyValue extension property value
     */
    protected void addExtensionPropertyValue(String propertyId, String extPropertyValue) {
        extensionProperties.addExtensionPropertyValue(propertyId, extPropertyValue);
    }

    /**
     * This method initializes the parts that are nested in the given element. If this
     * {@link IpsObjectPartContainer} was already initialized by previous initialization it verifies
     * that existing instances are used. This is important to have the same instance references for
     * example after resetting the part using the memento mechanism.
     * <p>
     * If the object was not previously initialized or a new part was found in the XML element, this
     * method creates a new part with a new ID. During initialization of the part itself it would
     * overwrite the new ID with an existing ID read from the XML element. This is done to
     * automatically migrate older parts that may have no ID attribute yet.
     * <p>
     * The initialization also validates that there is no duplicate ID within any
     * {@link IpsObjectPartContainer}. If a duplicated ID is recognized it throws a
     * {@link RuntimeException}.
     * 
     */
    protected void initPartContainersFromXml(Element element) {
        Map<String, IIpsObjectPart> idPartMap = createIdPartMap();
        reinitPartCollections();
        Map<String, IIpsObjectPart> newIdPartMap = initPartContainersFromXml(element, idPartMap);
        deleteOldParts(idPartMap, newIdPartMap);
    }

    private void deleteOldParts(Map<String, IIpsObjectPart> oldIdPartMap, Map<String, IIpsObjectPart> newIdPartMap) {
        for (Iterator<Entry<String, IIpsObjectPart>> iterator = oldIdPartMap.entrySet().iterator(); iterator
                .hasNext();) {
            Entry<String, IIpsObjectPart> entry = iterator.next();
            if (!newIdPartMap.containsKey(entry.getKey())) {
                ((IpsObjectPart)entry.getValue()).markAsDeleted();
            }
        }
    }

    protected Map<String, IIpsObjectPart> initPartContainersFromXml(Element element,
            Map<String, IIpsObjectPart> idPartMap) {
        Map<String, IIpsObjectPart> newIdPartMap = new HashMap<>();

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
            String id = partEl.getAttribute(IIpsObjectPart.PROPERTY_ID).trim();
            IIpsObjectPart part = null;
            if (IpsStringUtils.isNotEmpty(id)) {
                part = idPartMap.get(id);
            }
            if (part == null) {
                part = newPart(partEl, getNextPartId());
            } else {
                boolean added = addPart(part);
                if (!(added)) {
                    throw new IllegalArgumentException("Could not re-add part " + part); //$NON-NLS-1$
                }
            }
            // part might be null if the partEl does not represent a IpsObjectPart!
            if (part != null) {
                part.initFromXml(partEl);
                if (newIdPartMap.put(part.getId(), part) != null) {
                    throw new RuntimeException("Duplicated Part-ID in Object " + part.getParent().getName() + ", ID: " //$NON-NLS-1$ //$NON-NLS-2$
                            + part.getId());
                }
            }
        }
        return newIdPartMap;
    }

    private HashMap<String, IIpsObjectPart> createIdPartMap() {
        HashMap<String, IIpsObjectPart> map = new HashMap<>();
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
        extensionProperties.clear();
        reinitPartCollectionsThis();
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #reinitPartCollections()}.
     */
    protected abstract void reinitPartCollectionsThis();

    /**
     * Adds the given part to the container. Returns <code>true</code> if successfully added,
     * <code>false</code> otherwise.
     * <p>
     * This method is called during the process of initialization from XML when the part has been
     * part of the parent before the XML initialization and is still found in the XML file (the
     * part's id is found in the XML file). To avoid a StackOverflow it is important NOT to call the
     * {@link #objectHasChanged()} method after adding a part!
     * 
     * @param part The {@link IIpsObjectPart} to add to this container.
     */
    protected final boolean addPart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.add((ILabel)part);
            return true;

        } else if (part instanceof IDescription) {
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
     * <p>
     * This method is called by {@link #addPart(IIpsObjectPart)} which is called during XML
     * initialization. It is important NOT to call the {@link #objectHasChanged()} method after
     * adding a part!
     * 
     * @param part The {@link IIpsObjectPart} to add to this container.
     */
    protected abstract boolean addPartThis(IIpsObjectPart part);

    /**
     * Removes the given part from the container. Returns <code>true</code> if removed,
     * <code>false</code> otherwise.
     * <p>
     * Subclasses may extend this method by using the method
     * {@link #removePartThis(IIpsObjectPart)}.
     * 
     * @param part The {@link IIpsObjectPart} to remove from this container.
     */
    protected final boolean removePart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.remove(part);
            return true;

        } else if (part instanceof IDescription) {
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
     * This method is called during the process of initialization from XML to create a new part
     * object for the given element with the given id.
     * 
     * @param xmlTag The XML tag that describes the part to create.
     * @param id The unique id for the new part.
     */
    protected final IIpsObjectPart newPart(Element xmlTag, String id) {
        String nodeName = xmlTag.getNodeName();
        if (nodeName.equals(ILabel.XML_TAG_NAME)) {
            if (this instanceof ILabeledElement) {
                return newLabel(id);
            } else {
                return null;
            }
        } else if (nodeName.equals(IDescription.XML_TAG_NAME)) {
            if (this instanceof IDescribedElement) {
                return newDescription(id);
            } else {
                return null;
            }
        }
        return newPartThis(xmlTag, id);
    }

    /**
     * Subclass implementation that must create and return the right part based on the XML element.
     * <p>
     * <strong>Note:</strong> It is <strong>NOT</strong> necessary to fully initialize the part,
     * this is done later by the caller calling initFromXml(). Also it is NOT <strong>NOT</strong>
     * necessary to notify any change listener about the newly added part!
     * <p>
     * Should return <code>null</code> if the XML tag is unknown.
     * 
     * @param xmlTag The XML tag that describes the part to create.
     * @param id The unique id for the new part.
     */
    protected abstract IIpsObjectPart newPartThis(Element xmlTag, String id);

    /**
     * Creates a new {@link IIpsObjectPart} of the given type. If the type is not supported,
     * <code>null</code> is returned.
     * 
     * @param partType The published interface of the IPS object part that should be created.
     */
    @SuppressWarnings("unchecked")
    // not type safe because newPartThis is not type safe
    public final <T extends IIpsObjectPart> T newPart(Class<T> partType) {
        if (partType == Label.class) {
            T newLabel = (T)newLabel();
            return newLabel;

        } else if (partType == Description.class) {
            T newDescription = (T)newDescription();
            return newDescription;
        }
        T newPartThis = (T)newPartThis(partType);
        return newPartThis;
    }

    /**
     * Subclass implementation that must create and return the right part based on the given
     * published interface class.
     * <p>
     * Should return <code>null</code> if the type is unknown.
     * <p>
     * This class is not type safe because it was to late to implement generics correctly. However
     * the implementer needs to ensure that the returned object could safely casted to
     * <code>partType</code>.
     * 
     * @param partType The published interface of the IPS object part that should be created.
     */
    protected abstract IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType);

    @Override
    public MessageList validate(IIpsProject ipsProject) {
        if (isNotInIpsRoot()) {
            return new MessageList();
        }
        MessageList result = beforeValidateThis();
        if (result != null) {
            return result;
        }

        result = new MessageList();

        int languageCount = getLanguageCount();
        if (this instanceof IDescribedElement) {
            validateDescriptionCount(result, languageCount);
        }
        if (this instanceof ILabeledElement) {
            validateLabelCount(result, languageCount);
        }

        validateThis(result, ipsProject);
        execCustomValidations(result, ipsProject);

        validateSinceVersionFormat(result);

        afterValidateThis(result, ipsProject);
        return result;
    }

    /**
     * This method returns the number of supported languages which the <em>IpsProject</em> of this
     * {@link IpsObjectPartContainer} supports.
     * 
     * @return the number of supported languages.
     */
    private int getLanguageCount() {
        IIpsProjectProperties properties = getIpsProject().getReadOnlyProperties();
        int languageCount = properties.getSupportedLanguages().size();
        return languageCount;
    }

    private void validateSinceVersionFormat(MessageList result) {
        if (getIpsProject() != null && StringUtils.isNotEmpty(sinceVersion)) {
            IVersionProvider<?> versionProvider = getIpsProject().getVersionProvider();
            boolean isCorrectFormat = versionProvider.isCorrectVersionFormat(sinceVersion);
            if (!isCorrectFormat) {
                String text = MessageFormat.format(Messages.IpsObjectPartContainer_msgInvalidVersionFormat,
                        versionProvider.getVersionFormat());
                Message message = Message.newError(IIpsObjectPartContainer.MSGCODE_INVALID_VERSION_FORMAT, text, this,
                        IVersionControlledElement.PROPERTY_SINCE_VERSION_STRING);
                result.add(message);
            }
        }
    }

    private void validateDescriptionCount(MessageList result, int languageCount) {
        int descriptionCount = descriptions.size();
        if (descriptionCount != languageCount) {
            String text = MessageFormat.format(Messages.IpsObjectPartContainer_msgInvalidDescriptionCount,
                    descriptionCount,
                    languageCount);
            Message message = Message.newWarning(IIpsObjectPartContainer.MSGCODE_INVALID_DESCRIPTION_COUNT, text, this);
            result.add(message);
        }
    }

    private void validateLabelCount(MessageList result, int languageCount) {
        int labelCount = labels.size();
        if (labelCount != languageCount) {
            String text = MessageFormat.format(Messages.IpsObjectPartContainer_msgInvalidLabelCount, labelCount,
                    languageCount);
            Message message = Message.newWarning(IIpsObjectPartContainer.MSGCODE_INVALID_LABEL_COUNT, text, this);
            result.add(message);
        }
    }

    private void execCustomValidations(MessageList result, IIpsProject ipsProject) {
        Class<? extends IpsObjectPartContainer> thisClass = getClass();
        Set<ICustomValidation<? extends IIpsObjectPartContainer>> customValidations = getIpsModel()
                .getCustomModelExtensions().getCustomValidations(thisClass);
        for (ICustomValidation<? extends IIpsObjectPartContainer> validation : customValidations) {
            MessageList msgList = getValidationMessages(ipsProject, validation);
            result.add(msgList);
        }
    }

    /**
     * Unsafe cast required. We only use the casted object for calling
     * {@link ICustomValidation#validate(IIpsObjectPartContainer, IIpsProject)}.
     * 
     * The {@link CustomValidationsResolver} can only provide custom validations of arbitrary type
     * ("?"). This is due to the fact, that it would require a type parameter that is both a
     * subclass of IIpsObjectPartContainer and at the same time a superclass of the requested class.
     * This is not possible. We'll have to content ourselves with the unsafe cast.
     */
    private MessageList getValidationMessages(IIpsProject ipsProject,
            ICustomValidation<? extends IIpsObjectPartContainer> validation) {
        @SuppressWarnings("unchecked")
        MessageList msgList = ((ICustomValidation<IIpsObjectPartContainer>)validation).validate(this, ipsProject);
        return msgList;
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
            System.out.println("Validation of " + this + ": Started."); //$NON-NLS-1$//$NON-NLS-2$
        }
        return result;
    }

    /**
     * After validation method. Perform operations which will be executed after validation of this
     * object part container.
     */
    protected void afterValidateThis(MessageList result, IIpsProject ipsProject) {
        result.add(extensionProperties.validate());
        validateChildren(result, ipsProject);
        if (IpsModel.TRACE_VALIDATION) {
            System.out.println("Validation of " + this + ": Finished, took " //$NON-NLS-1$ //$NON-NLS-2$
                    + (System.currentTimeMillis() - validationStartTime) + "ms."); //$NON-NLS-1$
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
    protected void validateChildren(MessageList result, IIpsProject ipsProject) {
        IIpsElement[] children = getChildren();
        for (IIpsElement element : children) {
            MessageList childResult = ((IpsObjectPartContainer)element).validate(ipsProject);
            result.add(childResult);
        }
    }

    /**
     * Returns true if this <code>IpsObjectPartContainer</code> is part of an
     * <code>IIpsSrcFile</code> that is marked as historic. If no {@link IIpsSrcFile} can be found,
     * false is returned.
     * 
     * @return True only if the corresponding {@link IIpsSrcFile} is located in an existing IPS
     *         Root, false otherwise.
     */
    private boolean isNotInIpsRoot() {
        IIpsElement container = this;
        while (container != null) {
            if (container instanceof IIpsSrcFile && !((IIpsSrcFile)container).isContainedInIpsRoot()) {
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
     * @throws IpsException Subclasses may wrap any occurring exceptions into a CoreException and
     *             propagate it trough this method.
     * @throws NullPointerException if list is <code>null</code>.
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        // empty default method
    }

    @Override
    public Memento newMemento() {
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        return new XmlMemento(this, toXml(doc));
    }

    @Override
    public void setState(Memento memento) {
        if (!equals(memento.getOriginator())) {
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
        DependencyDetail dependencyDetail = new DependencyDetail(part, propertyName);
        addDependencyDetail(details, dependency, dependencyDetail);
    }

    /**
     * Helper to easily add details to the given map.
     * 
     * @param details The map of dependencies to the list of details.
     * @param dependency The dependency to add the details for
     * @param newDependencyDetail The new dependency detail that should be added
     */
    protected void addDependencyDetail(Map<IDependency, List<IDependencyDetail>> details,
            IDependency dependency,
            IDependencyDetail newDependencyDetail) {
        if (details == null) {
            return;
        }
        List<IDependencyDetail> detailList = details.computeIfAbsent(dependency,
                $ -> new ArrayList<>());
        detailList.add(newDependencyDetail);
    }

    private ILabel newLabel(String id) {
        ILabel newLabel = new Label(this, id);
        labels.add(newLabel);
        return newLabel;
    }

    protected IDescription newDescription(String id) {
        IDescription newDescription = new Description(this, id);
        descriptions.add(newDescription);
        return newDescription;
    }

    /**
     * @see ILabeledElement#getLabel(Locale)
     */
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

    /**
     * @see ILabeledElement#getLabels()
     */
    public List<ILabel> getLabels() {
        List<ILabel> orderedLabels = new ArrayList<>(labels.size());
        for (ISupportedLanguage language : getIpsProject().getReadOnlyProperties().getSupportedLanguages()) {
            Locale locale = language.getLocale();
            if (locale == null) {
                continue;
            }
            ILabel label = getLabel(locale);
            if (label != null) {
                orderedLabels.add(label);
            }
        }
        for (ILabel label : labels) {
            if (!(orderedLabels.contains(label))) {
                orderedLabels.add(label);
            }
        }
        return orderedLabels;
    }

    /**
     * @see ILabeledElement#newLabel()
     */
    public ILabel newLabel() {
        return newLabel(getNextPartId());
    }

    /**
     * @see ILabeledElement#isPluralLabelSupported()
     */
    public boolean isPluralLabelSupported() {
        return false;
    }

    /**
     * @see ILabeledElement#getLabelValue(Locale)
     */
    public String getLabelValue(Locale locale) {
        ILabel label = getLabel(locale);
        return label == null ? null : label.getValue();
    }

    /**
     * @see ILabeledElement#getPluralLabelValue(Locale)
     */
    public String getPluralLabelValue(Locale locale) {
        ILabel label = getLabel(locale);
        return label == null ? null : label.getPluralValue();
    }

    /**
     * @see ILabeledElement#setLabelValue(Locale, String)
     */
    public void setLabelValue(Locale locale, String value) {
        ILabel label = getLabel(locale);
        if (label == null) {
            throw new IllegalArgumentException("There is no label with the locale '" + locale + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
        label.setValue(value);
    }

    /**
     * @see ILabeledElement#setPluralLabelValue(Locale, String)
     */
    public void setPluralLabelValue(Locale locale, String pluralValue) {
        ILabel label = getLabel(locale);
        if (label == null) {
            throw new IllegalArgumentException("There is no label with the locale '" + locale + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
        label.setPluralValue(pluralValue);
    }

    /**
     * @see IDescribedElement#getDescription(Locale)
     */
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

    /**
     * @see IDescribedElement#getDescriptionText(Locale)
     */
    public String getDescriptionText(Locale locale) {
        IDescription description = getDescription(locale);
        return description == null ? "" : description.getText(); //$NON-NLS-1$
    }

    /**
     * @see IDescribedElement#getDescriptions()
     */
    public List<IDescription> getDescriptions() {
        List<IDescription> orderedDescriptions = new ArrayList<>(descriptions.size());
        for (ISupportedLanguage language : getIpsProject().getReadOnlyProperties().getSupportedLanguages()) {
            Locale locale = language.getLocale();
            if (locale == null) {
                continue;
            }
            IDescription description = getDescription(locale);
            if (description != null) {
                orderedDescriptions.add(description);
            }
        }
        for (IDescription description : descriptions) {
            if (!(orderedDescriptions.contains(description))) {
                orderedDescriptions.add(description);
            }
        }
        return orderedDescriptions;
    }

    /**
     * @see IDescribedElement#newDescription()
     */
    public IDescription newDescription() {
        return newDescription(getNextPartId());
    }

    /**
     * @see IVersionControlledElement#newDeprecation()
     */
    public IDeprecation newDeprecation() {
        deprecation = new Deprecation(this, getNextPartId());
        return deprecation;
    }

    /**
     * @see IDescribedElement#setDescriptionText(Locale, String)
     */
    public void setDescriptionText(Locale locale, String text) {
        IDescription description = getDescription(locale);
        if (description == null) {
            throw new IllegalArgumentException("There is no description with the locale '" + locale + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        description.setText(text);
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses to
     * provide the correct caption.
     */
    @Override
    public String getCaption(Locale locale) {
        ArgumentCheck.notNull(locale);
        return ""; //$NON-NLS-1$
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses to
     * provide the correct plural caption.
     */
    @Override
    public String getPluralCaption(Locale locale) {
        ArgumentCheck.notNull(locale);
        return ""; //$NON-NLS-1$
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses if
     * they are able to provide a reasonable last resort caption.
     */
    @Override
    public String getLastResortCaption() {
        return ""; //$NON-NLS-1$
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses if
     * they are able to provide a reasonable last resort plural caption.
     */
    @Override
    public String getLastResortPluralCaption() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public void copyFrom(IIpsObjectPartContainer source) {
        ArgumentCheck.isTrue(getClass().equals(source.getClass()));
        Element xmlElement = source.toXml(XmlUtil.getDefaultDocumentBuilder().newDocument());
        initFromXml(xmlElement);
    }

    /**
     * Sets the Version since which this part is available in the model using a version string
     * representation.
     * 
     * @param version The version-string that should be set as since-version
     * 
     * @see IVersionControlledElement#setSinceVersionString(String)
     */
    public void setSinceVersionString(String version) {
        String oldValue = this.sinceVersion;
        this.sinceVersion = version;
        valueChanged(oldValue, version, IVersionControlledElement.PROPERTY_SINCE_VERSION_STRING);
    }

    /**
     * Returns the version since which this part is available as a string. The version was set by
     * {@link #setSinceVersionString(String)}.
     * 
     * @return the version since which this element is available
     * @see #getSinceVersion()
     * 
     * @see IVersionControlledElement#getSinceVersionString()
     */
    public String getSinceVersionString() {
        return sinceVersion;
    }

    /**
     * Returns <code>true</code> if the version set by {@link #setSinceVersionString(String)} is a
     * valid version according to the configured {@link IVersionProvider}.
     * 
     * @return <code>true</code> if the version is correct and {@link #getSinceVersion()} would
     *         return a valid version. Otherwise <code>false</code>.
     * 
     * @see IVersionControlledElement#isValidSinceVersion()
     */
    public boolean isValidSinceVersion() {
        if (this instanceof IVersionControlledElement && StringUtils.isNotBlank(sinceVersion)) {
            IVersionProvider<?> versionProvider = getIpsProject().getVersionProvider();
            return versionProvider.isCorrectVersionFormat(sinceVersion);
        } else {
            return false;
        }
    }

    /**
     * Returns the version since which this part is available. The version was set by
     * {@link #setSinceVersionString(String)}. Returns <code>null</code> if no since version is set.
     * 
     * @return the version since which this element is available
     * @throws IllegalArgumentException if the current since version is no valid version according
     *             to the configured {@link IVersionProvider}
     * 
     * @see #isValidSinceVersion()
     * @see IVersionControlledElement#getSinceVersion()
     */
    public IVersion<?> getSinceVersion() {
        if (StringUtils.isBlank(sinceVersion)) {
            return null;
        }
        IVersionProvider<?> versionProvider = getIpsProject().getVersionProvider();
        return versionProvider.getVersion(sinceVersion);
    }

    /**
     * Returns the deprecation information for this part, if it is deprecated or {@code null}
     * otherwise.
     *
     * @see IVersionControlledElement#getDeprecation()
     */
    public IDeprecation getDeprecation() {
        return deprecation;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean isDeprecated) {
        boolean oldValue = this.deprecated;
        this.deprecated = isDeprecated;
        valueChanged(oldValue, isDeprecated);
        if (isDeprecated && deprecation == null) {
            newDeprecation();
        }
    }

}
