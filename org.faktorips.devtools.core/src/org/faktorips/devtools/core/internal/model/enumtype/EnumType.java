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

package org.faktorips.devtools.core.internal.model.enumtype;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enumtype.EnumTypeValidations;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of IEnumType, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumType extends EnumValueContainer implements IEnumType {

    // Reference to the super enum type if any
    private String superEnumType;

    // Collection containing all enum attributes for this enum type
    private IpsObjectPartCollection enumAttributes;

    // Flag indicating whether the values for this enum type are defined in the model
    private boolean valuesArePartOfModel;

    // Flag indicating whether this enum type is abstract in means of the object oriented abstract
    // concept
    private boolean isAbstract;

    /**
     * Creates a new enum type.
     * 
     * @param file The ips source file in which this enum type will be stored in.
     */
    public EnumType(IIpsSrcFile file) {
        super(file);

        this.superEnumType = "";
        this.enumAttributes = new IpsObjectPartCollection(this, EnumAttribute.class, IEnumAttribute.class,
                IEnumAttribute.XML_TAG);
        this.valuesArePartOfModel = false;
        this.isAbstract = false;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * {@inheritDoc}
     */
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    /**
     * {@inheritDoc}
     */
    public boolean valuesArePartOfModel() {
        return valuesArePartOfModel;
    }

    /**
     * {@inheritDoc}
     */
    public void setValuesArePartOfModel(boolean valuesArePartOfModel) {
        this.valuesArePartOfModel = valuesArePartOfModel;
    }

    /**
     * {@inheritDoc}
     */
    public String getSuperEnumType() {
        return superEnumType;
    }

    /**
     * {@inheritDoc}
     */
    public void setSuperEnumType(String superEnumTypeQualifiedName) {
        ArgumentCheck.notNull(superEnumTypeQualifiedName);

        superEnumType = superEnumTypeQualifiedName;
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumAttribute> getEnumAttributes() {
        List<IEnumAttribute> attributesList = new ArrayList<IEnumAttribute>();
        IIpsObjectPart[] parts = enumAttributes.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            attributesList.add((IEnumAttribute)currentObjectPart);
        }

        return attributesList;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute newEnumAttribute() throws CoreException {
        IEnumAttribute newEnumAttribute = (IEnumAttribute)newPart(IEnumAttribute.class);

        // Create new enum attribute value objects on all referencing enum values
        for (IEnumContent currentEnumContent : findReferencingEnumContents()) {
            for (IEnumValue currentEnumValue : currentEnumContent.getEnumValues()) {
                currentEnumValue.newEnumAttributeValue();
            }
        }

        return newEnumAttribute;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumType findEnumType() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberEnumAttributes() {
        return enumAttributes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initFromXml(Element element, Integer id) {
        isAbstract = Boolean.parseBoolean(element.getAttribute(IEnumType.PROPERTY_ABSTRACT));
        valuesArePartOfModel = Boolean.parseBoolean(element.getAttribute(IEnumType.PROPERTY_VALUES_ARE_PART_OF_MODEL));
        superEnumType = element.getAttribute(IEnumType.PROPERTY_SUPERTYPE);

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_SUPERTYPE, superEnumType);
        element.setAttribute(PROPERTY_ABSTRACT, String.valueOf(isAbstract));
        element.setAttribute(PROPERTY_VALUES_ARE_PART_OF_MODEL, String.valueOf(valuesArePartOfModel));
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute getEnumAttribute(int id) {
        return (IEnumAttribute)enumAttributes.getPart(id);
    }

    /**
     * {@inheritDoc}
     */
    public void moveEnumAttributeDown(IEnumAttribute enumAttribute) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);

        if (enumAttribute == enumAttributes.getPart(enumAttributes.size() - 1)) {
            return;
        }

        moveEnumAttribute(enumAttribute, false);
    }

    /**
     * {@inheritDoc}
     */
    public void moveEnumAttributeUp(IEnumAttribute enumAttribute) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);

        if (enumAttribute == enumAttributes.getPart(0)) {
            return;
        }

        moveEnumAttribute(enumAttribute, true);
    }

    /*
     * Moves the given enum attribute up or down in the collection order by 1
     */
    @SuppressWarnings("unchecked")
    private void moveEnumAttribute(IEnumAttribute enumAttribute, boolean up) throws CoreException {
        List<IEnumAttribute> enumAttributesList = enumAttributes.getBackingList();
        for (int i = 0; i < enumAttributesList.size(); i++) {
            IEnumAttribute currentEnumAttribute = enumAttributesList.get(i);
            if (currentEnumAttribute == enumAttribute) {

                enumAttributes.moveParts(new int[] { i }, up);

                // Also move the refering enum attribute values
                if (!valuesArePartOfModel) {
                    for (IEnumContent currentEnumContent : findReferencingEnumContents()) {
                        moveEnumAttributeValues(currentEnumAttribute, currentEnumContent.getEnumValues(), up);
                    }
                } else {
                    moveEnumAttributeValues(currentEnumAttribute, getEnumValues(), up);
                }

                break;

            }
        }
    }

    /*
     * Moves the enum attribute value corresponding to the given enum attribute in each given enum
     * value up or down in the collection order by 1
     */
    private void moveEnumAttributeValues(IEnumAttribute enumAttribute, List<IEnumValue> enumValues, boolean up) {
        for (IEnumValue currentEnumValue : enumValues) {
            if (up) {
                currentEnumValue.moveEnumAttributeValueUp(enumAttribute);
            } else {
                currentEnumValue.moveEnumAttributeValueDown(enumAttribute);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumContent> findReferencingEnumContents() throws CoreException {
        List<IEnumContent> referencingEnumContents = new ArrayList<IEnumContent>();
        IIpsSrcFile[] enumContentsSrcFiles = getIpsProject().findIpsSrcFiles(IpsObjectType.ENUM_CONTENT);
        for (IIpsSrcFile currentIpsSrcFile : enumContentsSrcFiles) {
            IEnumContent currentEnumContent = (IEnumContent)currentIpsSrcFile.getIpsObject();
            if (currentEnumContent.getEnumType().equals(this.getQualifiedName())) {
                referencingEnumContents.add((IEnumContent)currentIpsSrcFile.getIpsObject());
            }
        }

        return referencingEnumContents;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteEnumAttributeWithValues(IEnumAttribute enumAttribute) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);

        deleteEnumAttributeWithValues(enumAttribute.getId());
    }

    /**
     * {@inheritDoc}
     */
    public void deleteEnumAttributeWithValues(int id) throws CoreException {
        deleteEnumAttributeValues(id, getEnumValues());
        for (IEnumContent currentEnumContent : findReferencingEnumContents()) {
            deleteEnumAttributeValues(id, currentEnumContent.getEnumValues());
        }

        IEnumAttribute enumAttributeToDelete = (IEnumAttribute)enumAttributes.getPartById(id);
        if (enumAttributeToDelete == null) {
            throw new NoSuchElementException();
        }

        enumAttributeToDelete.delete();
    }

    /*
     * Deletes all enum attribute values in the given enum values that refer to the enum attribute
     * identified by the given id
     */
    private void deleteEnumAttributeValues(int enumAttributeId, List<IEnumValue> enumValues) {
        for (IEnumValue currentEnumValue : enumValues) {
            for (IEnumAttributeValue currentEnumAttributeValue : currentEnumValue.getEnumAttributeValues()) {
                if (currentEnumAttributeValue.getEnumAttribute().getId() == enumAttributeId) {
                    currentEnumAttributeValue.delete();
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVoid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean enumAttributeExists(String name) {
        ArgumentCheck.notNull(name);

        for (IEnumAttribute currentEnumAttribute : getEnumAttributes()) {
            if (currentEnumAttribute.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute getEnumAttribute(String name) {
        ArgumentCheck.notNull(name);

        for (IEnumAttribute currentEnumAttribute : getEnumAttributes()) {
            if (currentEnumAttribute.getName().equals(name)) {
                return currentEnumAttribute;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        if (!(superEnumType.equals(""))) {
            Message validationMessage = EnumTypeValidations.validateSuperEnumType(this, superEnumType, ipsProject);
            if (validationMessage != null) {
                list.add(validationMessage);
            }
        }
    }

}
