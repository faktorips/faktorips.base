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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enums.EnumTypeValidations;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumType</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumType extends EnumValueContainer implements IEnumType {

    /** Reference to the super enum type if any. */
    private String superEnumType;

    /** Flag indicating whether the values for this enum type are defined in the model. */
    private boolean valuesArePartOfModel;

    /** Collection containing all enum attributes for this enum type. */
    private IpsObjectPartCollection enumAttributes;

    /**
     * Flag indicating whether this enum type is abstract in means of the object oriented abstract
     * concept.
     */
    private boolean isAbstract;

    /**
     * Creates a new <code>EnumType</code>.
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
        boolean oldIsAbstract = this.isAbstract;
        this.isAbstract = isAbstract;
        valueChanged(oldIsAbstract, isAbstract);
    }

    /**
     * {@inheritDoc}
     */
    public boolean getValuesArePartOfModel() {
        return valuesArePartOfModel;
    }

    /**
     * {@inheritDoc}
     */
    public void setValuesArePartOfModel(boolean valuesArePartOfModel) {
        boolean oldValuesArePartOfModel = this.valuesArePartOfModel;
        this.valuesArePartOfModel = valuesArePartOfModel;
        valueChanged(oldValuesArePartOfModel, valuesArePartOfModel);
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

        String oldSupertype = superEnumType;
        superEnumType = superEnumTypeQualifiedName;
        valueChanged(oldSupertype, superEnumType);
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumAttribute> getEnumAttributes() {
        return getEnumAttributes(false);
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumAttribute> findAllEnumAttributes() {
        return getEnumAttributes(true);
    }

    /**
     * Returns a list containing all enum attributes that belong to this enum type. It can be
     * specified whether to include inherited attributes or not.
     */
    private List<IEnumAttribute> getEnumAttributes(boolean includeInherited) {
        List<IEnumAttribute> attributesList = new ArrayList<IEnumAttribute>();
        IIpsObjectPart[] parts = enumAttributes.getParts();
        for (IIpsObjectPart currentIpsObjectPart : parts) {
            IEnumAttribute currentEnumAttribute = (IEnumAttribute)currentIpsObjectPart;
            if (!(currentEnumAttribute.isInherited()) || includeInherited) {
                attributesList.add(currentEnumAttribute);
            }
        }

        return attributesList;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute newEnumAttribute() throws CoreException {
        /*
         * The creation of a new enum attribute consists of multiple operations that need to be
         * batched.
         */
        NewEnumAttributeRunnable workspaceRunnable = new NewEnumAttributeRunnable();
        getIpsModel().runAndQueueChangeEvents(workspaceRunnable, null);

        return workspaceRunnable.newEnumAttribute;
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
    public int getEnumAttributesCount(boolean includeInherited) {
        if (includeInherited) {
            return findAllEnumAttributes().size();
        } else {
            return getEnumAttributes().size();
        }
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
    public int moveEnumAttribute(IEnumAttribute enumAttribute, boolean up) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);
        if (enumAttribute.getEnumType() != this) {
            throw new NoSuchElementException();
        }

        if (up) {
            // Can't move further up any more
            if (enumAttribute == enumAttributes.getPart(0)) {
                return getIndexOfEnumAttribute(enumAttribute);
            }
        } else {
            // Can't move further down any more
            if (enumAttribute == enumAttributes.getPart(enumAttributes.size() - 1)) {
                return getIndexOfEnumAttribute(enumAttribute);
            }
        }

        int indexToMove = getIndexOfEnumAttribute(enumAttribute);

        // Moving an enum attribute consists of multiple operations that need to be batched.
        MoveEnumAttributeRunnable workspaceRunnable = new MoveEnumAttributeRunnable(indexToMove, up);
        getIpsModel().runAndQueueChangeEvents(workspaceRunnable, null);

        return workspaceRunnable.newIndex;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute) {
        ArgumentCheck.notNull(enumAttribute);

        List<IEnumAttribute> enumAttributesList = enumAttributes.getBackingList();
        for (int i = 0; i < enumAttributesList.size(); i++) {
            IEnumAttribute currentEnumAttribute = enumAttributesList.get(i);
            if (currentEnumAttribute == enumAttribute) {
                return i;
            }
        }

        throw new NoSuchElementException();
    }

    /**
     * Moves the enum attribute value corresponding to the given enum attribute identified by its
     * index in each given enum value up or down in the containing list by 1.
     */
    private void moveEnumAttributeValues(int enumAttributeIndex, List<IEnumValue> enumValues, boolean up)
            throws CoreException {

        for (IEnumValue currentEnumValue : enumValues) {
            currentEnumValue.moveEnumAttributeValue(currentEnumValue.getEnumAttributeValues().get(enumAttributeIndex),
                    up);
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

        return getEnumAttribute(name, false);
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute findEnumAttribute(String name) {
        ArgumentCheck.notNull(name);

        return getEnumAttribute(name, true);
    }

    /**
     * Searches and returns the enum attribute with the given name or <code>null</code> if none
     * exists. It can be specified whether to include inherited enum attributes.
     */
    private IEnumAttribute getEnumAttribute(String name, boolean includeInherited) {
        List<IEnumAttribute> enumAttributesToSearch;
        if (includeInherited) {
            enumAttributesToSearch = findAllEnumAttributes();
        } else {
            enumAttributesToSearch = getEnumAttributes();
        }

        for (IEnumAttribute currentEnumAttribute : enumAttributesToSearch) {
            if (currentEnumAttribute.getName().equals(name)) {
                return currentEnumAttribute;
            }
        }

        // No enum attribute with the given name found
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        Message validationMessage = null;

        // Validate super enum type
        if (!(superEnumType.equals(""))) {
            EnumTypeValidations.validateSuperEnumType(list, this, superEnumType, ipsProject);
        }

        // Validate inherited attributes
        if (!(superEnumType.equals(""))) {
            if (validationMessage == null) {
                EnumTypeValidations.validateInheritedAttributes(list, this);
            }
        }

        // Validate identifier attribute
        EnumTypeValidations.validateIdentifierAttribute(list, this);
    }

    /**
     * {@inheritDoc}
     */
    public IEnumType findSuperEnumType() throws CoreException {
        IIpsSrcFile[] enumTypeSrcFiles = getIpsProject().findIpsSrcFiles(IpsObjectType.ENUM_TYPE);
        for (IIpsSrcFile currentIpsSrcFile : enumTypeSrcFiles) {
            IEnumType currentEnumType = (IEnumType)currentIpsSrcFile.getIpsObject();
            if (currentEnumType.getQualifiedName().equals(superEnumType)) {
                return currentEnumType;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteEnumAttributeWithValues(final IEnumAttribute enumAttribute) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);
        ArgumentCheck.isTrue(enumAttributes.getBackingList().contains(enumAttribute));

        // Deleting an enum attribute consists of multiple operations that need to be batched.
        IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
            /**
             * {@inheritDoc}
             */
            public void run(IProgressMonitor monitor) throws CoreException {
                deleteEnumAttributeValues(enumAttribute, getEnumValues());
                enumAttribute.delete();
            }
        };
        getIpsModel().runAndQueueChangeEvents(workspaceRunnable, null);
    }

    /**
     * Deletes all enum attribute values in the given enum values that refer to the given enum
     * attribute.
     */
    private void deleteEnumAttributeValues(IEnumAttribute enumAttribute, List<IEnumValue> enumValues)
            throws CoreException {

        for (IEnumValue currentEnumValue : enumValues) {
            for (IEnumAttributeValue currentEnumAttributeValue : currentEnumValue.getEnumAttributeValues()) {
                if (currentEnumAttributeValue.findEnumAttribute() == enumAttribute) {
                    currentEnumAttributeValue.delete();
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSuperEnumType() {
        return (!(superEnumType.equals("")));
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumType> findAllSuperEnumTypes() throws CoreException {
        List<IEnumType> enumTypes = new ArrayList<IEnumType>();

        IEnumType currentEnumType = this;
        while (currentEnumType != null) {
            IEnumType superEnumType = currentEnumType.findSuperEnumType();
            currentEnumType = superEnumType;
            if (superEnumType != null) {
                enumTypes.add(superEnumType);
            }
        }

        return enumTypes;
    }

    /**
     * Creates a new enum attribute. On every enum value that is contained in this enum type new
     * enum attribute value objects need to be created for the new enum attribute.
     * <p>
     * These operations are atomic and therefore need to be batched into a runanble.
     */
    private class NewEnumAttributeRunnable implements IWorkspaceRunnable {

        /** Handle to the enum attribute to be created. */
        private IEnumAttribute newEnumAttribute;

        /** Creates the <code>NewEnumAttributeRunnable</code>. */
        public NewEnumAttributeRunnable() {
            this.newEnumAttribute = null;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) throws CoreException {
            // Create new enum attribute
            newEnumAttribute = (IEnumAttribute)newPart(IEnumAttribute.class);

            // Create new enum attribute value objects on the enum values of this enum type
            for (IEnumValue currentEnumValue : getEnumValues()) {
                currentEnumValue.newEnumAttributeValue();
            }
        }

    }

    /**
     * Moves an enum attribute. On every enum value that is contained in this enum type the enum
     * attribute values refering to this enum attribute need to be moved, too.
     * <p>
     * These operations are atomic and therefore need to be batched into a runanble.
     */
    private class MoveEnumAttributeRunnable implements IWorkspaceRunnable {

        /** The index of the enum attribute to be moved. */
        private int indexToMove;

        /** The new index of the enum attriubute that has been moved. */
        private int newIndex;

        /** Flag indicating whether to move up or down. */
        private boolean up;

        /** Creates the <code>MoveEnumAttributeRunnable</code>. */
        public MoveEnumAttributeRunnable(int indexToMove, boolean up) {
            this.indexToMove = indexToMove;
            this.newIndex = -1;
            this.up = up;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) throws CoreException {
            // Move the enum attribute
            int[] newIndex = enumAttributes.moveParts(new int[] { indexToMove }, up);

            // Move the enum attribute values of the enum values of this enum type
            moveEnumAttributeValues(indexToMove, getEnumValues(), up);

            this.newIndex = newIndex[0];
        }

    }

}
