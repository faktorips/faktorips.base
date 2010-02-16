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
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.EnumTypeValidations;
import org.faktorips.devtools.core.model.enums.EnumUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.TreeSetHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumType</tt>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumType extends EnumValueContainer implements IEnumType {

    /** Qualified name of the super <tt>IEnumType</tt> if any. */
    private String superEnumType;

    /** Flag indicating whether the values for this <tt>IEnumType</tt> are defined in the model. */
    private boolean containingValues;

    /**
     * Qualified name of the package fragment a referencing <tt>IEnumContent</tt> must be stored in.
     */
    private String enumContentPackageFragment;

    /** Collection containing all <tt>IEnumAttribute</tt>s for this <tt>IEnumType</tt>. */
    private IpsObjectPartCollection<IEnumAttribute> enumAttributes;

    /**
     * Flag indicating whether this <tt>IEnumType</tt> is abstract in means of the object oriented
     * abstract concept.
     */
    private boolean isAbstract;

    /**
     * Creates a new <tt>IEnumType</tt>.
     * 
     * @param file The IPS source file in which this <tt>IEnumType</tt> will be stored in.
     */
    public EnumType(IIpsSrcFile file) {
        super(file);

        superEnumType = "";
        containingValues = false;
        isAbstract = false;
        enumContentPackageFragment = "";
        enumAttributes = new IpsObjectPartCollection<IEnumAttribute>(this, EnumAttribute.class, IEnumAttribute.class,
                IEnumAttribute.XML_TAG);
    }

    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_TYPE;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        boolean oldIsAbstract = this.isAbstract;
        this.isAbstract = isAbstract;
        valueChanged(oldIsAbstract, isAbstract);
    }

    public boolean isContainingValues() {
        return containingValues;
    }

    public void setContainingValues(boolean containingValues) {
        boolean oldContainingValues = this.containingValues;
        this.containingValues = containingValues;
        valueChanged(oldContainingValues, containingValues);
    }

    public String getSuperEnumType() {
        return superEnumType;
    }

    public void setSuperEnumType(String superEnumTypeQualifiedName) {
        ArgumentCheck.notNull(superEnumTypeQualifiedName);

        String oldSupertype = superEnumType;
        superEnumType = superEnumTypeQualifiedName;
        valueChanged(oldSupertype, superEnumType);
    }

    public boolean isSubEnumTypeOf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject) throws CoreException {
        if (superEnumTypeCandidate == null) {
            return false;
        }
        IEnumType superEnumType = findSuperEnumType(ipsProject);
        if (superEnumType == null) {
            return false;
        }
        if (superEnumTypeCandidate.equals(superEnumType)) {
            return true;
        }
        IsSubEnumTypeOfVisitor visitor = new IsSubEnumTypeOfVisitor(ipsProject, superEnumTypeCandidate);
        visitor.start(superEnumType);
        return visitor.isSubtype();
    }

    public boolean isSubEnumTypeOrSelf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject) throws CoreException {
        if (equals(superEnumTypeCandidate)) {
            return true;
        }
        return isSubEnumTypeOf(superEnumTypeCandidate, ipsProject);
    }

    public List<IEnumAttribute> getEnumAttributes(boolean includeLiteralName) {
        return getEnumAttributesInternal(false, includeLiteralName);
    }

    public List<IEnumAttribute> getEnumAttributesIncludeSupertypeCopies(boolean includeLiteralName) {
        return getEnumAttributesInternal(true, includeLiteralName);
    }

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s that belong to this <tt>IEnumType</tt>
     * . It can be specified whether to include copied inherited attributes or not and whether to
     * include literal name attributes or not.
     */
    private List<IEnumAttribute> getEnumAttributesInternal(boolean includeInheritedCopies,
            boolean includeLiteralNameAttributes) {

        List<IEnumAttribute> attributesList = new ArrayList<IEnumAttribute>();
        IIpsObjectPart[] parts = enumAttributes.getParts();
        for (IIpsObjectPart currentIpsObjectPart : parts) {
            IEnumAttribute currentEnumAttribute = (IEnumAttribute)currentIpsObjectPart;
            if (!(currentEnumAttribute.isInherited()) || includeInheritedCopies) {
                boolean literalNameAttribute = currentEnumAttribute instanceof IEnumLiteralNameAttribute;
                if ((literalNameAttribute && includeLiteralNameAttributes) || !literalNameAttribute) {
                    attributesList.add(currentEnumAttribute);
                }
            }
        }

        return attributesList;
    }

    public List<IEnumAttribute> findAllEnumAttributesIncludeSupertypeOriginals(boolean includeLiteralName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(ipsProject);

        List<IEnumType> superEnumTypes = findAllSuperEnumTypes(ipsProject);
        List<IEnumType> completeHierarchy = new ArrayList<IEnumType>(superEnumTypes.size() + 1);
        completeHierarchy.add(this);
        completeHierarchy.addAll(superEnumTypes);

        List<IEnumAttribute> attributesList = new ArrayList<IEnumAttribute>();
        for (IEnumType currentEnumType : completeHierarchy) {
            attributesList.addAll(currentEnumType.getEnumAttributes(includeLiteralName));
        }
        return attributesList;
    }

    public IEnumAttribute newEnumAttribute() throws CoreException {
        return createNewEnumAttribute(EnumAttribute.class);
    }

    public IEnumLiteralNameAttribute newEnumLiteralNameAttribute() throws CoreException {
        IEnumLiteralNameAttribute literalNameAttribute = (IEnumLiteralNameAttribute)createNewEnumAttribute(EnumLiteralNameAttribute.class);
        literalNameAttribute.setName(IEnumLiteralNameAttribute.DEFAULT_NAME);
        literalNameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        literalNameAttribute.setUnique(true);
        return literalNameAttribute;
    }

    private IEnumAttribute createNewEnumAttribute(final Class<? extends IEnumAttribute> attributeClass)
            throws CoreException {
        return executeModificationsWithSingleEvent(new SingleEventModification<IEnumAttribute>() {

            IEnumAttribute newEnumAttribute;

            @Override
            public boolean execute() throws CoreException {
                newEnumAttribute = (IEnumAttribute)newPart(attributeClass);

                // Create new EnumAttributeValue objects on the EnumValues of this EnumType.
                for (IEnumValue currentEnumValue : getEnumValues()) {
                    currentEnumValue.newEnumAttributeValue();
                }
                return true;
            }

            @Override
            public IEnumAttribute getResult() {
                return newEnumAttribute;
            }
        });
    }

    public IEnumType findEnumType(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        return this;
    }

    public int getEnumAttributesCount(boolean includeLiteralName) {
        return getEnumAttributes(includeLiteralName).size();
    }

    public int getEnumAttributesCountIncludeSupertypeCopies(boolean includeLiteralName) {
        return getEnumAttributesIncludeSupertypeCopies(includeLiteralName).size();
    }

    public List<IEnumAttribute> findUniqueEnumAttributes(boolean includeLiteralName, IIpsProject ipsProject)
            throws CoreException {

        List<IEnumAttribute> uniqueEnumAttributes = new ArrayList<IEnumAttribute>(2);
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(includeLiteralName)) {
            if (EnumUtil.findEnumAttributeIsUnique(currentEnumAttribute, ipsProject)) {
                uniqueEnumAttributes.add(currentEnumAttribute);
            }
        }
        return uniqueEnumAttributes;
    }

    @Override
    public boolean initUniqueIdentifierValidationCacheImpl() throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        List<IEnumAttribute> uniqueEnumAttributes = findUniqueEnumAttributes(true, ipsProject);
        for (IEnumAttribute currentUniqueAttribute : uniqueEnumAttributes) {
            addUniqueIdentifierToValidationCache(getIndexOfEnumAttribute(currentUniqueAttribute));
        }
        initValidationCacheUniqueIdentifierEntries(uniqueEnumAttributes, this);
        return true;
    }

    @Override
    protected void initFromXml(Element element, String id) {
        isAbstract = Boolean.parseBoolean(element.getAttribute(PROPERTY_ABSTRACT));
        containingValues = Boolean.parseBoolean(element.getAttribute(PROPERTY_CONTAINING_VALUES));
        superEnumType = element.getAttribute(PROPERTY_SUPERTYPE);
        enumContentPackageFragment = element.getAttribute(PROPERTY_ENUM_CONTENT_NAME);

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_SUPERTYPE, superEnumType);
        element.setAttribute(PROPERTY_ABSTRACT, String.valueOf(isAbstract));
        element.setAttribute(PROPERTY_CONTAINING_VALUES, String.valueOf(containingValues));
        element.setAttribute(PROPERTY_ENUM_CONTENT_NAME, enumContentPackageFragment);
    }

    public int moveEnumAttribute(final IEnumAttribute enumAttribute, final boolean up) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);
        if (enumAttribute.getEnumType() != this) {
            throw new NoSuchElementException();
        }
        if (up) {
            // Can't move further up any more.
            if (enumAttribute == enumAttributes.getPart(0)) {
                return getIndexOfEnumAttribute(enumAttribute);
            }
        } else {
            // Can't move further down any more.
            if (enumAttribute == enumAttributes.getPart(enumAttributes.size() - 1)) {
                return getIndexOfEnumAttribute(enumAttribute);
            }
        }

        return executeModificationsWithSingleEvent(new SingleEventModification<Integer>() {
            int[] newIndex = {};

            @Override
            public boolean execute() throws CoreException {
                int indexToMove = getIndexOfEnumAttribute(enumAttribute);

                // Move the EnumAttribute.
                newIndex = enumAttributes.moveParts(new int[] { indexToMove }, up);

                // Move the EnumAttributeValues of the EnumValues of this EnumType.
                if (newIndex[0] != indexToMove) {
                    moveEnumAttributeValues(indexToMove, getEnumValues(), up);
                }

                // Update unique identifier validation cache.
                if (isUniqueIdentifierValidationCacheInitialized()) {
                    handleMoveEnumAttributeForUniqueIdentifierValidationCache(indexToMove, up);
                }
                if (newIndex[0] != indexToMove) {
                    return true;
                }
                return false;
            }

            @Override
            public Integer getResult() {
                return newIndex[0];
            }
        });
    }

    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute) {
        ArgumentCheck.notNull(enumAttribute);
        return enumAttributes.indexOf(enumAttribute);
    }

    public int getIndexOfEnumLiteralNameAttribute() {
        if (getEnumLiteralNameAttribute() == null) {
            return -1;
        }
        return enumAttributes.indexOf(getEnumLiteralNameAttribute());
    }

    /**
     * Moves the <tt>IEnumAttributeValue</tt> corresponding to the given <tt>IEnumAttribute</tt>
     * identified by its index in each given <tt>IEnumValue</tt> up or down in the containing list
     * by 1.
     */
    private void moveEnumAttributeValues(int enumAttributeIndex, List<IEnumValue> enumValues, boolean up)
            throws CoreException {

        for (IEnumValue currentEnumValue : enumValues) {
            currentEnumValue.moveEnumAttributeValue(currentEnumValue.getEnumAttributeValues().get(enumAttributeIndex),
                    up);
        }
    }

    public IEnumAttribute getEnumAttribute(String name) {
        ArgumentCheck.notNull(name);
        return getEnumAttribute(name, false);
    }

    public IEnumAttribute getEnumAttributeIncludeSupertypeCopies(String name) {
        ArgumentCheck.notNull(name);
        return getEnumAttribute(name, true);
    }

    /**
     * Searches and returns the <tt>IEnumAttribute</tt> with the given name or <tt>null</tt> if none
     * exists. It can be specified whether to include inherited <tt>IEnumAttribute</tt>s.
     */
    private IEnumAttribute getEnumAttribute(String name, boolean includeInherited) {
        List<IEnumAttribute> enumAttributesToSearch;
        if (includeInherited) {
            enumAttributesToSearch = getEnumAttributesIncludeSupertypeCopies(true);
        } else {
            enumAttributesToSearch = getEnumAttributes(true);
        }

        for (IEnumAttribute currentEnumAttribute : enumAttributesToSearch) {
            if (currentEnumAttribute.getName().equals(name)) {
                return currentEnumAttribute;
            }
        }

        // No IEnumAttribute with the given name found.
        return null;
    }

    public IEnumAttribute findEnumAttributeIncludeSupertypeOriginals(IIpsProject ipsProject, String name)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { ipsProject, name });

        List<IEnumType> superEnumTypes = findAllSuperEnumTypes(ipsProject);
        List<IEnumType> completeHierarchy = new ArrayList<IEnumType>(superEnumTypes.size() + 1);
        completeHierarchy.add(this);
        completeHierarchy.addAll(superEnumTypes);

        for (IEnumType currentEnumType : completeHierarchy) {
            IEnumAttribute enumAttribute = currentEnumType.getEnumAttribute(name);
            if (enumAttribute != null) {
                return enumAttribute;
            }
        }

        // No IEnumAttribute with the given name found.
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        if (hasSuperEnumType()) {
            EnumTypeValidations.validateSuperEnumType(list, this, superEnumType, ipsProject);
        }
        EnumTypeValidations.validateSuperTypeHierarchy(list, this, ipsProject);

        if (hasSuperEnumType()) {
            if (list.getNoOfMessages() == 0) {
                validateInheritedAttributes(list, ipsProject);
            }
        }

        validateLiteralNameAttribute(list);
        validateIdentifierAttribute(list, ipsProject);
        validateUsedAsNameInFaktorIpsUiAttribute(list, ipsProject);

        EnumTypeValidations.validateEnumContentName(list, this, isAbstract(), !isContainingValues(),
                enumContentPackageFragment);

        // Validate possible obsolete enumeration values.
        if (getEnumValuesCount() > 0) {
            if (!containingValues || isAbstract) {
                String text = Messages.EnumType_EnumValuesObsolete;
                Message validationMessage = new Message(MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE, text, Message.WARNING,
                        this);
                list.add(validationMessage);
            }
        }
    }

    /**
     * Validates whether this <tt>IEnumType</tt> inherits all <tt>IEnumAttribute</tt>s defined in
     * its supertype hierarchy.
     * <p>
     * Adds validation messages to the given message list. The validation will pass immediately if
     * the <tt>IEnumType</tt> is abstract.
     */
    private void validateInheritedAttributes(MessageList validationMessageList, IIpsProject ipsProject)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, ipsProject });

        // Pass validation on abstract EnumType.
        if (isAbstract) {
            return;
        }

        // All attributes from supertype hierarchy inherited?
        List<IEnumAttribute> notInheritedAttributes = findInheritEnumAttributeCandidates(ipsProject);
        int notInheritedAttributesCount = notInheritedAttributes.size();
        if (notInheritedAttributesCount > 0) {
            IEnumAttribute firstNotInheritedAttribute = notInheritedAttributes.get(0);
            String showFirst = firstNotInheritedAttribute.getName() + " (" + firstNotInheritedAttribute.getDatatype() //$NON-NLS-1$
                    + ')';
            String text = (notInheritedAttributesCount > 1) ? NLS.bind(
                    Messages.EnumType_NotInheritedAttributesInSupertypeHierarchyPlural, notInheritedAttributesCount,
                    showFirst) : NLS.bind(Messages.EnumType_NotInheritedAttributesInSupertypeHierarchySingular,
                    showFirst);
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY,
                    text, Message.ERROR, this);
            validationMessageList.add(message);
        }
    }

    /**
     * Validates whether this <tt>IEnumType</tt> contains at least one
     * <tt>IEnumLiteralNameAttribute</tt>.
     * <p>
     * If the this <tt>IEnumType</tt> is abstract or does not contain values the validation will
     * succeed even if there is no <tt>IEnumLiteralNameAttribute</tt>.
     */
    private void validateLiteralNameAttribute(MessageList validationMessageList) throws CoreException {
        // Pass validation if the EnumType is abstract or does not contain values.
        if (isAbstract || !isContainingValues()) {
            return;
        }

        String text;
        Message message;

        if (!(containsEnumLiteralNameAttribute())) {
            text = Messages.EnumType_NoLiteralNameAttribute;
            message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(this, null) });
            validationMessageList.add(message);
            return;
        }

        int literalNameAttributesCount = getEnumLiteralNameAttributesCount();
        if (literalNameAttributesCount > 1) {
            text = NLS.bind(Messages.EnumType_MultipleLiteralNameAttributes, literalNameAttributesCount);
            message = new Message(IEnumType.MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES, text, Message.ERROR,
                    this);
            validationMessageList.add(message);
        }
    }

    public int getEnumLiteralNameAttributesCount() {
        int count = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributes.getBackingList()) {
            if (currentEnumAttribute instanceof IEnumLiteralNameAttribute) {
                count++;
            }
        }
        return count;
    }

    /**
     * Validates whether this <tt>IEnumType</tt> has at least one <tt>IEnumAttribute</tt> being
     * marked to be used as ID in the Faktor-IPS UI.
     * <p>
     * If the <tt>IEnumType</tt> is abstract the validation will succeed even if there is no such
     * <tt>IEnumAttribute</tt>.
     */
    private void validateIdentifierAttribute(MessageList validationMessageList, IIpsProject ipsProject)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { ipsProject });

        // Pass validation if the EnumType is abstract.
        if (isAbstract) {
            return;
        }

        if (findIdentiferAttribute(ipsProject) == null) {
            String text = Messages.EnumType_NoUsedAsIdInFaktorIpsUiAttribute;
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE, text,
                    Message.ERROR, new ObjectProperty[] { new ObjectProperty(this, null) });
            validationMessageList.add(message);
        }
    }

    /**
     * Validates whether this <tt>IEnumType</tt> has at least one <tt>IEnumAttribute</tt> being
     * marked to be used as name in the Faktor-IPS UI.
     * <p>
     * If the <tt>IEnumType</tt> is abstract the validation will succeed even if there is no such
     * <tt>IEnumAttribute</tt>.
     */
    private void validateUsedAsNameInFaktorIpsUiAttribute(MessageList validationMessageList, IIpsProject ipsProject)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, ipsProject });

        // Pass validation if the EnumType is abstract.
        if (isAbstract) {
            return;
        }

        if (findUsedAsNameInFaktorIpsUiAttribute(ipsProject) == null) {
            String text = Messages.EnumType_NoUsedAsNameInFaktorIpsUiAttribute;
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE, text,
                    Message.ERROR, new ObjectProperty[] { new ObjectProperty(this, null) });
            validationMessageList.add(message);
        }
    }

    /**
     * Returns all <tt>IEnumAttribute</tt>s that are defined in the supertype hierarchy of this
     * <tt>IEnumType</tt>.
     */
    private List<IEnumAttribute> findAllAttributesInSupertypeHierarchy(IIpsProject ipsProject) throws CoreException {
        List<IEnumAttribute> returnAttributesList = new ArrayList<IEnumAttribute>();

        /*
         * Go over all EnumAttributes of every EnumType in the supertype hierarchy. Do this
         * backwards so the attributes of the EnumType up highest in the hierarchy will be showed
         * first.
         */
        List<IEnumType> superEnumTypes = findAllSuperEnumTypes(ipsProject);
        for (int i = superEnumTypes.size() - 1; i >= 0; i--) {
            IEnumType currentSuperEnumType = superEnumTypes.get(i);
            for (IEnumAttribute currentEnumAttribute : currentSuperEnumType.getEnumAttributes(false)) {
                returnAttributesList.add(currentEnumAttribute);
            }
        }
        return returnAttributesList;
    }

    public IEnumType findSuperEnumType(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        IIpsSrcFile enumTypeSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, superEnumType);
        if (enumTypeSrcFile != null && enumTypeSrcFile.exists()) {
            return (IEnumType)enumTypeSrcFile.getIpsObject();
        }
        return null;
    }

    public IEnumAttribute findIdentiferAttribute(IIpsProject ipsProject) throws CoreException {
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(false)) {
            if (EnumUtil.findEnumAttributeIsIdentifier(currentEnumAttribute, ipsProject)) {
                return currentEnumAttribute;
            }
        }
        return null;
    }

    public IEnumAttribute findUsedAsNameInFaktorIpsUiAttribute(IIpsProject ipsProject) throws CoreException {
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(false)) {
            if (EnumUtil.findEnumAttributeIsUsedAsNameInFaktorIpsUi(currentEnumAttribute, ipsProject)) {
                return currentEnumAttribute;
            }
        }
        return null;
    }

    public boolean deleteEnumAttributeWithValues(final IEnumAttribute enumAttribute) throws CoreException {
        if (enumAttribute == null) {
            return false;
        }
        if (!(enumAttributes.contains(enumAttribute))) {
            return false;
        }

        // Update unique identifier validation cache if necessary.
        if (isUniqueIdentifierValidationCacheInitialized()) {
            int index = getIndexOfEnumAttribute(enumAttribute);
            removeUniqueIdentifierFromValidationCache(index);
            handleEnumAttributeDeletion(index);
        }

        executeModificationsWithSingleEvent(new SingleEventModification<Object>() {

            @Override
            public boolean execute() throws CoreException {
                deleteEnumAttributeValues(enumAttribute, getEnumValues());
                enumAttribute.delete();
                return true;
            }

            @Override
            public Object getResult() {
                return null;
            }
        });
        return true;
    }

    /**
     * Deletes all <tt>IEnumAttributeValue</tt>s in the given <tt>IEnumValue</tt>s that refer to the
     * given <tt>IEnumAttribute</tt>.
     * <p>
     * If no <tt>IEnumAttributeValue</tt>s remain in the given <tt>IEnumValue</tt>s they will be
     * deleted, too.
     */
    private void deleteEnumAttributeValues(IEnumAttribute enumAttribute, List<IEnumValue> enumValues) {
        boolean deleteEnumValues = false;
        for (IEnumValue currentEnumValue : enumValues) {
            int index = getIndexOfEnumAttribute(enumAttribute);
            currentEnumValue.getEnumAttributeValues().get(index).delete();
            deleteEnumValues = currentEnumValue.getEnumAttributeValuesCount() == 0;
        }
        if (deleteEnumValues) {
            for (int i = 0; i < enumValues.size(); i++) {
                enumValues.get(i).delete();
            }
        }
    }

    public boolean hasSuperEnumType() {
        return !("".equals(superEnumType));
    }

    public List<IEnumType> findAllSuperEnumTypes(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        final List<IEnumType> superEnumTypes = new ArrayList<IEnumType>();
        IEnumType directSuperEnumType = findSuperEnumType(ipsProject);
        if (directSuperEnumType != null) {
            EnumTypeHierachyVisitor collector = new EnumTypeHierachyVisitor(getIpsProject()) {
                @Override
                protected boolean visit(IEnumType currentType) throws CoreException {
                    superEnumTypes.add(currentType);
                    return true;
                }
            };
            collector.start(directSuperEnumType);
        }

        return superEnumTypes;
    }

    public List<IEnumAttribute> findInheritEnumAttributeCandidates(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        List<IEnumAttribute> inheritedEnumAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(false)) {
            if (currentEnumAttribute.isInherited()) {
                inheritedEnumAttributes.add(currentEnumAttribute);
            }
        }
        List<IEnumAttribute> supertypeHierarchyAttributes = findAllAttributesInSupertypeHierarchy(ipsProject);
        List<IEnumAttribute> notInheritedEnumAttributes = new ArrayList<IEnumAttribute>();

        for (IEnumAttribute currentSupertypeHierarchyAttribute : supertypeHierarchyAttributes) {
            if (!(containsEqualEnumAttribute(inheritedEnumAttributes, currentSupertypeHierarchyAttribute))) {
                notInheritedEnumAttributes.add(currentSupertypeHierarchyAttribute);
            }
        }

        return notInheritedEnumAttributes;
    }

    /**
     * Checks whether the given <tt>IEnumAttribute</tt> is contained in the given list of
     * <tt>IEnumAttribute</tt>s in means of an equal <tt>IEnumAttribute</tt>.
     */
    private boolean containsEqualEnumAttribute(List<IEnumAttribute> listOfEnumAttributes, IEnumAttribute enumAttribute) {
        for (IEnumAttribute currentEnumAttribute : listOfEnumAttributes) {
            if (currentEnumAttribute.getName().equals(enumAttribute.getName())) {
                return true;
            }
        }

        return false;
    }

    public List<IEnumAttribute> inheritEnumAttributes(List<IEnumAttribute> superEnumAttributes) throws CoreException {
        List<IEnumAttribute> newEnumAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentSuperEnumAttribute : superEnumAttributes) {
            String currentSuperEnumAttributeName = currentSuperEnumAttribute.getName();

            // Continue if already inherited.
            IEnumAttribute searchedEnumAttribute = getEnumAttributeIncludeSupertypeCopies(currentSuperEnumAttributeName);
            if (searchedEnumAttribute != null) {
                if (searchedEnumAttribute.isInherited()) {
                    continue;
                }
            }

            // Throw exception if not part of supertype hierarchy.
            searchedEnumAttribute = findEnumAttributeIncludeSupertypeOriginals(getIpsProject(),
                    currentSuperEnumAttributeName);
            boolean partOfSupertypeHierarchy = false;
            if (searchedEnumAttribute != null) {
                if (searchedEnumAttribute.getEnumType() != this) {
                    partOfSupertypeHierarchy = true;
                }
            }
            if (!partOfSupertypeHierarchy) {
                throw new IllegalArgumentException("The given enum attribute " + currentSuperEnumAttributeName
                        + " is not part of the supertype hierarchy.");
            }

            // Every check passed, inherit the EnumAttribute.
            IEnumAttribute newEnumAttribute = newEnumAttribute();
            newEnumAttribute.setName(currentSuperEnumAttributeName);
            newEnumAttribute.setInherited(true);
            newEnumAttributes.add(newEnumAttribute);
        }

        return newEnumAttributes;
    }

    public String getEnumContentName() {
        return enumContentPackageFragment;
    }

    public void setEnumContentName(String packageFragmentQualifiedName) {
        ArgumentCheck.notNull(packageFragmentQualifiedName);

        String oldEnumContentPackageFragment = enumContentPackageFragment;
        enumContentPackageFragment = packageFragmentQualifiedName;
        valueChanged(oldEnumContentPackageFragment, packageFragmentQualifiedName);
    }

    public IEnumContent findEnumContent(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findEnumContent(this);
    }

    @Override
    public IDependency[] dependsOn() throws CoreException {
        if (hasSuperEnumType()) {
            IDependency superEnumTypeDependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                    this, PROPERTY_SUPERTYPE, new QualifiedNameType(superEnumType, IpsObjectType.ENUM_TYPE));
            return new IDependency[] { superEnumTypeDependency };
        } else {
            return new IDependency[0];
        }
    }

    public IIpsSrcFile[] searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {

        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllEnumContentSrcFiles(this, includeSubtypes)));
        }

        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    public boolean containsEnumAttribute(String attributeName) {
        return containsEnumAttribute(attributeName, false);
    }

    public boolean containsEnumAttributeIncludeSupertypeCopies(String attributeName) {
        return containsEnumAttribute(attributeName, true);
    }

    public boolean containsEnumLiteralNameAttribute() {
        return getEnumLiteralNameAttribute() != null;
    }

    /**
     * Returns whether an <tt>IEnumAttribute</tt> with the given name exists in this
     * <tt>IEnumType</tt>. Depending on the boolean flag the supertype copies are included in the
     * check.
     */
    private boolean containsEnumAttribute(String attributeName, boolean includeSupertypeCopies) {
        List<IEnumAttribute> enumAttributesToCheck = includeSupertypeCopies ? getEnumAttributesIncludeSupertypeCopies(true)
                : getEnumAttributes(true);
        for (IEnumAttribute currentEnumAttribute : enumAttributesToCheck) {
            if (currentEnumAttribute.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        ArgumentCheck.notNull(xmlTag);
        if (xmlTag.getTagName().equals(IEnumLiteralNameAttribute.XML_TAG)) {
            return newPart(EnumLiteralNameAttribute.class);
        }
        return super.newPart(xmlTag, id);
    }

    public IEnumLiteralNameAttribute getEnumLiteralNameAttribute() {
        for (IEnumAttribute currentAttribute : enumAttributes.getBackingList()) {
            if (currentAttribute instanceof IEnumLiteralNameAttribute) {
                return (IEnumLiteralNameAttribute)currentAttribute;
            }
        }
        return null;
    }

    public boolean hasEnumLiteralNameAttribute() {
        return getEnumLiteralNameAttribute() != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt> if this <tt>IEnumType</tt> is not abstract and is configured to contain
     * values.
     */
    public boolean isCapableOfContainingValues() {
        return !isAbstract && containingValues;
    }

    private static class IsSubEnumTypeOfVisitor extends EnumTypeHierachyVisitor {

        private IEnumType superEnumTypeCandidate;
        private boolean subEnumType = false;

        public IsSubEnumTypeOfVisitor(IIpsProject ipsProject, IEnumType superEnumTypeCandidate) {
            super(ipsProject);
            ArgumentCheck.notNull(superEnumTypeCandidate);
            this.superEnumTypeCandidate = superEnumTypeCandidate;
        }

        boolean isSubtype() {
            return subEnumType;
        }

        @Override
        protected boolean visit(IEnumType currentEnumType) throws CoreException {
            if (currentEnumType == superEnumTypeCandidate) {
                subEnumType = true;
                return false;
            }
            return true;
        }

    }

}
