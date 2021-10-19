/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.enums.EnumTypeHierarchyVisitor;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.internal.util.TreeSetHelper;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumType</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumType extends EnumValueContainer implements IEnumType {

    /** Qualified name of the super <code>IEnumType</code> if any. */
    private String superEnumType;

    /** Flag indicating whether this <code>IEnumType</code> is extensible. */
    private boolean extensible;

    /** Boundary for the identifier attribute of this {@link IEnumType}. */
    private String identifierBoundary;

    /**
     * Qualified name of the package fragment a referencing <code>IEnumContent</code> must be stored
     * in.
     */
    private String enumContentPackageFragment;

    /** Collection containing all <code>IEnumAttribute</code>s for this <code>IEnumType</code>. */
    private IpsObjectPartCollection<IEnumAttribute> enumAttributes;

    /**
     * Flag indicating whether this <code>IEnumType</code> is abstract in means of the object
     * oriented abstract concept.
     */
    private boolean isAbstract;

    /**
     * Creates a new <code>IEnumType</code>.
     * 
     * @param file The IPS source file in which this <code>IEnumType</code> will be stored in.
     */
    public EnumType(IIpsSrcFile file) {
        super(file);
        superEnumType = StringUtils.EMPTY;
        extensible = false;
        isAbstract = false;
        enumContentPackageFragment = StringUtils.EMPTY;
        enumAttributes = new IpsObjectPartCollection<>(this, EnumAttribute.class, IEnumAttribute.class,
                IEnumAttribute.XML_TAG);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_TYPE;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public void setAbstract(boolean isAbstract) {
        boolean oldIsAbstract = this.isAbstract;
        this.isAbstract = isAbstract;

        if (isAbstract) {
            removePartThis(getEnumLiteralNameAttribute());
        }

        valueChanged(oldIsAbstract, isAbstract);
    }

    @Override
    public boolean containsValues() {
        return !getEnumValues().isEmpty();
    }

    @Override
    public boolean isExtensible() {
        return extensible;
    }

    @Override
    public void setExtensible(boolean extensible) {
        boolean oldExtensible = this.extensible;
        this.extensible = extensible;
        valueChanged(oldExtensible, extensible);
    }

    @Override
    public String getIdentifierBoundary() {
        return identifierBoundary;
    }

    @Override
    public void setIdentifierBoundary(String identifierBoundary) {
        String oldIdentifierBoundary = this.identifierBoundary;
        this.identifierBoundary = identifierBoundary;
        valueChanged(oldIdentifierBoundary, identifierBoundary);
    }

    @Override
    public String getSuperEnumType() {
        return superEnumType;
    }

    @Override
    public void setSuperEnumType(String superEnumTypeQualifiedName) {
        ArgumentCheck.notNull(superEnumTypeQualifiedName);

        String oldSupertype = superEnumType;
        superEnumType = superEnumTypeQualifiedName;
        valueChanged(oldSupertype, superEnumType);
    }

    @Override
    public boolean isSubEnumTypeOf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject) {
        if (superEnumTypeCandidate == null) {
            return false;
        }
        IEnumType foundSuperEnumType = findSuperEnumType(ipsProject);
        if (foundSuperEnumType == null) {
            return false;
        }
        if (superEnumTypeCandidate.equals(foundSuperEnumType)) {
            return true;
        }
        IsSubEnumTypeOfVisitor visitor = new IsSubEnumTypeOfVisitor(ipsProject, superEnumTypeCandidate);
        visitor.start(foundSuperEnumType);
        return visitor.isSubtype();
    }

    @Override
    public boolean isSubEnumTypeOrSelf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject) {
        if (equals(superEnumTypeCandidate)) {
            return true;
        }
        return isSubEnumTypeOf(superEnumTypeCandidate, ipsProject);
    }

    @Override
    public List<IEnumAttribute> getEnumAttributes(boolean includeLiteralName) {
        return getEnumAttributesInternal(false, includeLiteralName);
    }

    @Override
    public List<IEnumAttribute> getEnumAttributesIncludeSupertypeCopies(boolean includeLiteralName) {
        return getEnumAttributesInternal(true, includeLiteralName);
    }

    /**
     * Returns a list containing all <code>IEnumAttribute</code>s that belong to this
     * <code>IEnumType</code> . It can be specified whether to include copied inherited attributes
     * or not and whether to include literal name attributes or not.
     */
    private List<IEnumAttribute> getEnumAttributesInternal(boolean includeInheritedCopies,
            boolean includeLiteralNameAttributes) {

        List<IEnumAttribute> attributesList = new ArrayList<>();
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

    @Override
    public List<IEnumAttribute> findAllEnumAttributes(final boolean includeLiteralName, IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        final LinkedList<IEnumAttribute> allAttributes = new LinkedList<>();
        EnumTypeHierarchyVisitor collector = new AttributeFinder(getIpsProject(), allAttributes, includeLiteralName);
        collector.start(this);
        return allAttributes;
    }

    @Override
    public IEnumAttribute newEnumAttribute() throws CoreException {
        return createNewEnumAttribute(EnumAttribute.class);
    }

    @Override
    public IEnumLiteralNameAttribute newEnumLiteralNameAttribute() throws CoreException {
        IEnumLiteralNameAttribute literalNameAttribute = (IEnumLiteralNameAttribute)createNewEnumAttribute(
                EnumLiteralNameAttribute.class);
        literalNameAttribute.setName(IEnumLiteralNameAttribute.DEFAULT_NAME);
        literalNameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        literalNameAttribute.setUnique(true);
        return literalNameAttribute;
    }

    private IEnumAttribute createNewEnumAttribute(final Class<? extends IEnumAttribute> attributeClass)
            throws CoreException {

        return ((IpsModel)getIpsModel())
                .executeModificationsWithSingleEvent(new SingleEventModification<IEnumAttribute>(getIpsSrcFile()) {

                    private IEnumAttribute newEnumAttribute;

                    @Override
                    public boolean execute() throws CoreException {
                        newEnumAttribute = createNewEnumAttributeSingleEvent(attributeClass);
                        return newEnumAttribute != null;
                    }

                    @Override
                    public IEnumAttribute getResult() {
                        return newEnumAttribute;
                    }
                });
    }

    private IEnumAttribute createNewEnumAttributeSingleEvent(final Class<? extends IEnumAttribute> attributeClass)
            throws CoreException {
        IEnumAttribute newEnumAttribute = newPart(attributeClass);

        // Create new EnumAttributeValue objects on the EnumValues of this EnumType.
        for (IEnumValue currentEnumValue : getEnumValues()) {
            if (attributeClass.equals(EnumLiteralNameAttribute.class)) {
                currentEnumValue.newEnumLiteralNameAttributeValue();
            } else {
                currentEnumValue.newEnumAttributeValue();
            }
        }
        return newEnumAttribute;
    }

    @Override
    public IEnumType findEnumType(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        return this;
    }

    @Override
    public int getEnumAttributesCount(boolean includeLiteralName) {
        return getEnumAttributes(includeLiteralName).size();
    }

    @Override
    public int getEnumAttributesCountIncludeSupertypeCopies(boolean includeLiteralName) {
        return getEnumAttributesIncludeSupertypeCopies(includeLiteralName).size();
    }

    @Override
    public List<IEnumAttribute> findUniqueEnumAttributes(boolean includeLiteralName, IIpsProject ipsProject)
            throws CoreException {

        List<IEnumAttribute> uniqueEnumAttributes = new ArrayList<>(2);
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(includeLiteralName)) {
            if (currentEnumAttribute.findIsUnique(ipsProject)) {
                uniqueEnumAttributes.add(currentEnumAttribute);
            }
        }
        return uniqueEnumAttributes;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        isAbstract = Boolean.parseBoolean(element.getAttribute(PROPERTY_ABSTRACT));
        extensible = Boolean.parseBoolean(element.getAttribute(PROPERTY_EXTENSIBLE));
        if (element.hasAttribute(PROPERTY_IDENTIFIER_BOUNDARY)) {
            identifierBoundary = element.getAttribute(PROPERTY_IDENTIFIER_BOUNDARY);
        }
        superEnumType = element.getAttribute(PROPERTY_SUPERTYPE);
        enumContentPackageFragment = element.getAttribute(PROPERTY_ENUM_CONTENT_NAME);
        initDeprecatedProperties(element);
        super.initPropertiesFromXml(element, id);
    }

    private void initDeprecatedProperties(Element element) {
        String containsValuesAttribute = element.getAttribute("containingValues"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(containsValuesAttribute)) {
            extensible = !Boolean.parseBoolean(containsValuesAttribute);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_SUPERTYPE, superEnumType);
        element.setAttribute(PROPERTY_ABSTRACT, String.valueOf(isAbstract));
        element.setAttribute(PROPERTY_EXTENSIBLE, String.valueOf(extensible));
        if (identifierBoundary != null) {
            element.setAttribute(PROPERTY_IDENTIFIER_BOUNDARY, identifierBoundary);
        }
        element.setAttribute(PROPERTY_ENUM_CONTENT_NAME, enumContentPackageFragment);
    }

    @Override
    public int moveEnumAttribute(final IEnumAttribute enumAttribute, final boolean up) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);
        if (enumAttribute.getEnumType() != this) {
            throw new NoSuchElementException();
        }
        if (up) {
            // Can't move further up any more.
            if (enumAttribute == enumAttributes.getPart(0)) {
                return getIndexOfEnumAttribute(enumAttribute, true);
            }
        } else {
            // Can't move further down any more.
            if (enumAttribute == enumAttributes.getPart(enumAttributes.size() - 1)) {
                return getIndexOfEnumAttribute(enumAttribute, true);
            }
        }

        return ((IpsModel)getIpsModel())
                .executeModificationsWithSingleEvent(new SingleEventModification<Integer>(getIpsSrcFile()) {
                    private int[] newIndex = {};

                    @Override
                    public boolean execute() throws CoreException {
                        int indexToMove = getIndexOfEnumAttribute(enumAttribute, true);
                        // Move the EnumAttribute.
                        newIndex = enumAttributes.moveParts(new int[] { indexToMove }, up);
                        // Move the EnumAttributeValues of the EnumValues of this EnumType.
                        if (newIndex[0] != indexToMove) {
                            moveEnumAttributeValues(indexToMove, getEnumValues(), up);
                        }
                        return newIndex[0] != indexToMove;
                    }

                    @Override
                    public Integer getResult() {
                        return newIndex[0];
                    }
                });
    }

    @Override
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute, boolean considerLiteralName) {
        ArgumentCheck.notNull(enumAttribute);
        int indexOf = enumAttributes.indexOf(enumAttribute);
        if (!considerLiteralName) {
            int indexOfEnumLiteralNameAttribute = getIndexOfEnumLiteralNameAttribute();
            if (indexOfEnumLiteralNameAttribute >= 0 && indexOfEnumLiteralNameAttribute < indexOf) {
                return indexOf - 1;
            }
        }
        return indexOf;
    }

    @Override
    public int getIndexOfEnumLiteralNameAttribute() {
        if (getEnumLiteralNameAttribute() == null) {
            return -1;
        }
        return enumAttributes.indexOf(getEnumLiteralNameAttribute());
    }

    /**
     * Moves the <code>IEnumAttributeValue</code> corresponding to the given
     * <code>IEnumAttribute</code> identified by its index in each given <code>IEnumValue</code> up
     * or down in the containing list by 1.
     */
    private void moveEnumAttributeValues(int enumAttributeIndex, List<IEnumValue> enumValues, boolean up) {
        for (IEnumValue currentEnumValue : enumValues) {
            currentEnumValue.moveEnumAttributeValue(currentEnumValue.getEnumAttributeValues().get(enumAttributeIndex),
                    up);
        }
    }

    @Override
    public IEnumAttribute getEnumAttribute(String name) {
        ArgumentCheck.notNull(name);
        return getEnumAttribute(name, false);
    }

    @Override
    public IEnumAttribute getEnumAttributeIncludeSupertypeCopies(String name) {
        ArgumentCheck.notNull(name);
        return getEnumAttribute(name, true);
    }

    /**
     * Searches and returns the <code>IEnumAttribute</code> with the given name or <code>null</code>
     * if none exists. It can be specified whether to include inherited
     * <code>IEnumAttribute</code>s.
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

    @Override
    public IEnumAttribute findEnumAttributeIncludeSupertypeOriginals(IIpsProject ipsProject, final String name) {

        ArgumentCheck.notNull(new Object[] { ipsProject, name });

        final List<IEnumAttribute> result = new ArrayList<>(1);
        EnumTypeHierarchyVisitor visitor = new EnumTypeHierarchyVisitor(ipsProject) {
            @Override
            protected boolean visit(IEnumType currentType) {
                IEnumAttribute enumAttribute = currentType.getEnumAttribute(name);
                if (enumAttribute != null) {
                    result.add(enumAttribute);
                    return false;
                }
                return true;
            }
        };
        visitor.start(this);

        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        if (hasSuperEnumType()) {
            EnumTypeValidations.validateSuperEnumType(list, this, superEnumType, ipsProject);
        }
        EnumTypeValidations.validateSuperTypeHierarchy(list, this, ipsProject);

        if (hasSuperEnumType()) {
            if (list.containsErrorMsg()) {
                return;
            }
            validateInheritedAttributes(list, ipsProject);
        }

        validateLiteralNameAttribute(list);
        validateIdentifierAttribute(list, ipsProject);
        validateUsedAsNameInFaktorIpsUiAttribute(list, ipsProject);
        validateEnumContentAlreadyUsed(list, ipsProject);
        if (isValidateIdentifierBoundaryOnDatatypeNecessary(getIdentifierBoundary())) {
            validateIdentifierBoundaryOnDatatype(list);
        }

        EnumTypeValidations.validateEnumContentName(list, this, isAbstract(), isExtensible(),
                enumContentPackageFragment);

        // Validate possible obsolete enumeration values.
        if (getEnumValuesCount() > 0) {
            if (isAbstract) {
                String text = Messages.EnumType_EnumValuesObsolete;
                Message validationMessage = new Message(MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE, text, Message.WARNING,
                        this);
                list.add(validationMessage);
            }
        }
    }

    /**
     * Validates whether this <code>IEnumType</code> contains at least one
     * <code>IEnumLiteralNameAttribute</code>.
     * <p>
     * If this <code>IEnumType</code> is abstract or is extensible the validation will succeed even
     * if there is no <code>IEnumLiteralNameAttribute</code>.
     */
    private void validateLiteralNameAttribute(MessageList validationMessageList) {
        if (isAbstract()) {
            return;
        }
        validateLiteralNameAttributeExists(validationMessageList);
        validateLiteralNameAttributeCount(validationMessageList);
    }

    /**
     * Validates whether this <code>IEnumType</code> has at least one <code>IEnumAttribute</code>
     * being marked to be used as ID in the Faktor-IPS UI.
     * <p>
     * If the <code>IEnumType</code> is abstract the validation will succeed even if there is no
     * such <code>IEnumAttribute</code>.
     */
    private void validateIdentifierAttribute(MessageList validationMessageList, IIpsProject ipsProject) {
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
     * Validates whether this <code>IEnumType</code> has at least one <code>IEnumAttribute</code>
     * being marked to be used as name in the Faktor-IPS UI.
     * <p>
     * If the <code>IEnumType</code> is abstract the validation will succeed even if there is no
     * such <code>IEnumAttribute</code>.
     */
    private void validateUsedAsNameInFaktorIpsUiAttribute(MessageList validationMessageList, IIpsProject ipsProject) {

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
     * Validates whether the <code>IEnumContent</code> specified by this <code>IEnumType</code> is
     * already used by another <code>IEnumType</code>.
     */
    private void validateEnumContentAlreadyUsed(MessageList validationMessageList, IIpsProject ipsProject) {

        IEnumContent enumContent = findEnumContent(ipsProject);
        if (enumContent != null && !enumContent.getEnumType().equals(getQualifiedName())) {
            String text = NLS.bind(Messages.EnumType_EnumContentAlreadyUsedByAnotherEnumType,
                    enumContentPackageFragment, enumContent.getEnumType());
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_ALREADY_USED, text, Message.ERROR,
                    this, IEnumType.PROPERTY_ENUM_CONTENT_NAME);
            validationMessageList.add(message);
        }
    }

    protected boolean isValidateIdentifierBoundaryOnDatatypeNecessary(String identifierBoundaryString) {
        return !isAbstract && isExtensible() && isIdentifierAttributeComparable()
                && isIdentifierBoundaryValueValid(identifierBoundaryString);
    }

    private boolean isIdentifierAttributeComparable() {
        IEnumAttribute identiferAttribute = findIdentiferAttribute(getIpsProject());
        if (identiferAttribute != null) {
            try {
                ValueDatatype datatype = identiferAttribute.findDatatype(getIpsProject());
                if (datatype != null) {
                    return datatype.supportsCompare();
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return false;
    }

    private boolean isIdentifierBoundaryValueValid(String identifierBoundaryString) {
        return identifierBoundaryString != null && !(identifierBoundaryString.isEmpty());
    }

    private void validateIdentifierBoundaryOnDatatype(MessageList validationMessageList) throws CoreException {
        IEnumAttribute identifierAttribute = getIdentifierAttribute();
        if (identifierAttribute != null) {
            ValueDatatype identifierAttributeDatatype = identifierAttribute.findDatatype(getIpsProject());
            String identifierBoundaryString = getIdentifierBoundary();
            ValidationUtils.checkValue(identifierAttributeDatatype, identifierAttribute.getDatatype(),
                    identifierBoundaryString, this, PROPERTY_IDENTIFIER_BOUNDARY, validationMessageList);
        }
    }

    private IEnumAttribute getIdentifierAttribute() {
        IEnumAttribute identifierAttribute;
        if (hasSuperEnumType()) {
            identifierAttribute = findSuperEnumType(getIpsProject()).findIdentiferAttribute(getIpsProject());
        } else {
            identifierAttribute = findIdentiferAttribute(getIpsProject());
        }
        return identifierAttribute;
    }

    private void validateLiteralNameAttributeExists(MessageList validationMessageList) {
        if (isMissingLiteralNameAttribute()) {
            String text = Messages.EnumType_NoLiteralNameAttribute;
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(this, null) });
            validationMessageList.add(message);
        }
    }

    private void validateLiteralNameAttributeCount(MessageList validationMessageList) {
        int literalNameAttributesCount = getEnumLiteralNameAttributesCount();
        if (literalNameAttributesCount > 1) {
            String text = NLS.bind(Messages.EnumType_MultipleLiteralNameAttributes, literalNameAttributesCount);
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES, text,
                    Message.ERROR, this);
            validationMessageList.add(message);
        }
    }

    /**
     * Validates whether this <code>IEnumType</code> inherits all <code>IEnumAttribute</code>s
     * defined in its supertype hierarchy.
     * <p>
     * Adds validation messages to the given message list. The validation will pass immediately if
     * the <code>IEnumType</code> is abstract.
     */
    private void validateInheritedAttributes(MessageList validationMessageList, IIpsProject ipsProject)
            throws CoreException {
        ArgumentCheck.notNull(new Object[] { validationMessageList, ipsProject });

        // Pass validation on abstract EnumType.
        if (isAbstract()) {
            return;
        }
        validateInheritedAttributesNonAbstract(validationMessageList, ipsProject);
    }

    private void validateInheritedAttributesNonAbstract(MessageList validationMessageList, IIpsProject ipsProject)
            throws CoreException {
        List<IEnumAttribute> notInheritedAttributes = findInheritEnumAttributeCandidates(ipsProject);
        int notInheritedAttributesCount = notInheritedAttributes.size();
        if (notInheritedAttributesCount > 0) {
            IEnumAttribute firstNotInheritedAttribute = notInheritedAttributes.get(0);
            String showFirst = firstNotInheritedAttribute.getName() + " (" + firstNotInheritedAttribute.getDatatype() //$NON-NLS-1$
                    + ')';
            String text = (notInheritedAttributesCount > 1)
                    ? NLS.bind(Messages.EnumType_NotInheritedAttributesInSupertypeHierarchyPlural,
                            notInheritedAttributesCount, showFirst)
                    : NLS.bind(Messages.EnumType_NotInheritedAttributesInSupertypeHierarchySingular, showFirst);
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY,
                    text, Message.ERROR, this);
            validationMessageList.add(message);
        }
    }

    public boolean isMissingLiteralNameAttribute() {
        return !containsEnumLiteralNameAttribute();
    }

    @Override
    public boolean isInextensibleEnum() {
        return !isAbstract() && !isExtensible();
    }

    public int getEnumLiteralNameAttributesCount() {
        int count = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute instanceof IEnumLiteralNameAttribute) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns all <code>IEnumAttribute</code>s that are defined in the supertype hierarchy of this
     * <code>IEnumType</code>.
     */
    private List<IEnumAttribute> findAllAttributesInSupertypeHierarchy(IIpsProject ipsProject) {
        List<IEnumAttribute> returnAttributesList = new ArrayList<>();

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

    @Override
    public IEnumType findSuperEnumType(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        if (!hasSuperEnumType()) {
            return null;
        }

        IIpsSrcFile enumTypeSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, superEnumType);
        if (enumTypeSrcFile != null && enumTypeSrcFile.exists()) {
            return (IEnumType)enumTypeSrcFile.getIpsObject();
        }
        return null;
    }

    @Override
    public Set<IEnumType> searchSubclassingEnumTypes() throws CoreException {
        Set<IEnumType> collectedEnumTypes = new HashSet<>(25);
        IIpsProject[] ipsProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject ipsProject : ipsProjects) {
            IIpsSrcFile[] srcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.ENUM_TYPE);
            for (IIpsSrcFile ipsSrcFile : srcFiles) {
                IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
                if (enumType.isSubEnumTypeOf(this, ipsProject)) {
                    collectedEnumTypes.add(enumType);
                }
            }
        }
        return collectedEnumTypes;
    }

    @Override
    public IEnumAttribute findIdentiferAttribute(IIpsProject ipsProject) {
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(false)) {
            if (currentEnumAttribute.findIsIdentifier(ipsProject)) {
                return currentEnumAttribute;
            }
        }
        return null;
    }

    @Override
    public IEnumAttribute findUsedAsNameInFaktorIpsUiAttribute(IIpsProject ipsProject) {
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(false)) {
            if (currentEnumAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject)) {
                return currentEnumAttribute;
            }
        }
        return null;
    }

    @Override
    protected boolean removePartThis(final IIpsObjectPart part) {
        if (part instanceof IEnumAttribute) {
            final IEnumAttribute enumAttributeToDelete = (IEnumAttribute)part;
            try {
                ((IpsModel)getIpsModel())
                        .executeModificationsWithSingleEvent(new SingleEventModification<Void>(getIpsSrcFile()) {
                            @Override
                            public boolean execute() throws CoreException {
                                deleteEnumAttributeValues(enumAttributeToDelete, getEnumValues());
                                return EnumType.super.removePartThis(part);
                            }
                        });
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            return part.isDeleted();
        } else {
            return super.removePartThis(part);
        }
    }

    /**
     * Deletes all <code>IEnumAttributeValue</code>s in the given <code>IEnumValue</code>s that
     * refer to the given <code>IEnumAttribute</code>.
     * <p>
     * If no <code>IEnumAttributeValue</code>s remain in the given <code>IEnumValue</code>s they
     * will be deleted, too.
     */
    private void deleteEnumAttributeValues(IEnumAttribute enumAttribute, List<IEnumValue> enumValues) {
        boolean deleteEnumValues = false;
        for (IEnumValue currentEnumValue : enumValues) {
            int index = getIndexOfEnumAttribute(enumAttribute, true);
            currentEnumValue.getEnumAttributeValues().get(index).delete();
            deleteEnumValues = currentEnumValue.getEnumAttributeValuesCount() == 0;
        }
        if (deleteEnumValues) {
            for (IEnumValue enumValue : enumValues) {
                enumValue.delete();
            }
        }
    }

    @Override
    public boolean hasSuperEnumType() {
        return StringUtils.isNotEmpty(superEnumType);
    }

    @Override
    public boolean hasExistingSuperEnumType(IIpsProject ipsProject) {
        return findSuperEnumType(ipsProject) != null;
    }

    @Override
    public List<IEnumType> findAllSuperEnumTypes(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        final List<IEnumType> superEnumTypes = new ArrayList<>();
        IEnumType directSuperEnumType = findSuperEnumType(ipsProject);
        if (directSuperEnumType != null) {
            EnumTypeHierarchyVisitor collector = new EnumTypeHierarchyVisitor(getIpsProject()) {
                @Override
                protected boolean visit(IEnumType currentType) {
                    superEnumTypes.add(currentType);
                    return true;
                }
            };
            collector.start(directSuperEnumType);
        }

        return superEnumTypes;
    }

    @Override
    public List<IEnumAttribute> findInheritEnumAttributeCandidates(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        List<IEnumAttribute> inheritedEnumAttributes = new ArrayList<>();
        for (IEnumAttribute currentEnumAttribute : getEnumAttributesIncludeSupertypeCopies(false)) {
            if (currentEnumAttribute.isInherited()) {
                inheritedEnumAttributes.add(currentEnumAttribute);
            }
        }
        List<IEnumAttribute> supertypeHierarchyAttributes = findAllAttributesInSupertypeHierarchy(ipsProject);
        List<IEnumAttribute> notInheritedEnumAttributes = new ArrayList<>();

        for (IEnumAttribute currentSupertypeHierarchyAttribute : supertypeHierarchyAttributes) {
            if (!(containsEqualEnumAttribute(inheritedEnumAttributes, currentSupertypeHierarchyAttribute))) {
                notInheritedEnumAttributes.add(currentSupertypeHierarchyAttribute);
            }
        }

        return notInheritedEnumAttributes;
    }

    /**
     * Checks whether the given <code>IEnumAttribute</code> is contained in the given list of
     * <code>IEnumAttribute</code>s in means of an equal <code>IEnumAttribute</code>.
     */
    private boolean containsEqualEnumAttribute(List<IEnumAttribute> listOfEnumAttributes,
            IEnumAttribute enumAttribute) {
        for (IEnumAttribute currentEnumAttribute : listOfEnumAttributes) {
            if (currentEnumAttribute.getName().equals(enumAttribute.getName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<IEnumAttribute> inheritEnumAttributes(List<IEnumAttribute> superEnumAttributes) throws CoreException {
        List<IEnumAttribute> newEnumAttributes = new ArrayList<>();
        for (IEnumAttribute currentSuperEnumAttribute : superEnumAttributes) {
            String currentSuperEnumAttributeName = currentSuperEnumAttribute.getName();

            // Continue if already inherited.
            IEnumAttribute searchedEnumAttribute = getEnumAttributeIncludeSupertypeCopies(
                    currentSuperEnumAttributeName);
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
                throw new IllegalArgumentException("The given enum attribute " + currentSuperEnumAttributeName //$NON-NLS-1$
                        + " is not part of the supertype hierarchy."); //$NON-NLS-1$
            }

            // Every check passed, inherit the EnumAttribute.
            IEnumAttribute newEnumAttribute = newEnumAttribute();
            newEnumAttribute.setName(currentSuperEnumAttributeName);
            newEnumAttribute.setInherited(true);
            newEnumAttributes.add(newEnumAttribute);
        }

        return newEnumAttributes;
    }

    @Override
    public String getEnumContentName() {
        return enumContentPackageFragment;
    }

    @Override
    public void setEnumContentName(String packageFragmentQualifiedName) {
        ArgumentCheck.notNull(packageFragmentQualifiedName);

        String oldEnumContentPackageFragment = enumContentPackageFragment;
        enumContentPackageFragment = packageFragmentQualifiedName;
        valueChanged(oldEnumContentPackageFragment, packageFragmentQualifiedName);
    }

    @Override
    public IEnumContent findEnumContent(IIpsProject ipsProject) {
        return ipsProject.findEnumContent(this);
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        ArrayList<IDependency> dependencies = new ArrayList<>();
        if (hasSuperEnumType()) {
            IDependency superEnumTypeDependency = IpsObjectDependency.createSubtypeDependency(getQualifiedNameType(),
                    new QualifiedNameType(superEnumType, IpsObjectType.ENUM_TYPE));
            addDetails(details, superEnumTypeDependency, this, PROPERTY_SUPERTYPE);
            dependencies.add(superEnumTypeDependency);
        }
        for (IEnumAttribute enumAttribute : enumAttributes) {
            String datatype = enumAttribute.getDatatype();
            IDependency dependency = new DatatypeDependency(getQualifiedNameType(), datatype);
            dependencies.add(dependency);
            addDetails(details, dependency, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);
        }
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    @Override
    public Collection<IIpsSrcFile> searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {

        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllEnumContentSrcFiles(this, includeSubtypes)));
        }

        return result;
    }

    @Override
    public boolean containsEnumAttribute(String attributeName) {
        return containsEnumAttribute(attributeName, false);
    }

    @Override
    public boolean containsEnumAttributeIncludeSupertypeCopies(String attributeName) {
        return containsEnumAttribute(attributeName, true);
    }

    @Override
    public boolean containsEnumLiteralNameAttribute() {
        return getEnumLiteralNameAttribute() != null;
    }

    /**
     * Returns whether an <code>IEnumAttribute</code> with the given name exists in this
     * <code>IEnumType</code>. Depending on the boolean flag the supertype copies are included in
     * the check.
     */
    private boolean containsEnumAttribute(String attributeName, boolean includeSupertypeCopies) {
        List<IEnumAttribute> enumAttributesToCheck = includeSupertypeCopies
                ? getEnumAttributesIncludeSupertypeCopies(true)
                : getEnumAttributes(true);
        for (IEnumAttribute currentEnumAttribute : enumAttributesToCheck) {
            if (currentEnumAttribute.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getTagName().equals(IEnumLiteralNameAttribute.XML_TAG)) {
            return newPart(EnumLiteralNameAttribute.class);
        }
        return super.newPartThis(xmlTag, id);
    }

    @Override
    public IEnumLiteralNameAttribute getEnumLiteralNameAttribute() {
        for (IEnumAttribute currentAttribute : enumAttributes) {
            if (currentAttribute instanceof IEnumLiteralNameAttribute) {
                return (IEnumLiteralNameAttribute)currentAttribute;
            }
        }
        return null;
    }

    @Override
    public boolean hasEnumLiteralNameAttribute() {
        return getEnumLiteralNameAttribute() != null;
    }

    /**
     * Returns <code>true</code> if this <code>IEnumType</code> is not abstract.
     */
    @Override
    public boolean isCapableOfContainingValues() {
        return !isAbstract;
    }

    @Override
    public List<IEnumValue> findAggregatedEnumValues() {
        return getEnumValues();
    }

    @Override
    public boolean isIdentifierNamespaceBelowBoundary() {
        return true;
    }

    private final class AttributeFinder extends EnumTypeHierarchyVisitor {
        private final LinkedList<IEnumAttribute> allAttributes;
        private final boolean includeLiteralName;

        private AttributeFinder(IIpsProject ipsProject, LinkedList<IEnumAttribute> allAttributes,
                boolean includeLiteralName) {
            super(ipsProject);
            this.allAttributes = allAttributes;
            this.includeLiteralName = includeLiteralName;
        }

        @Override
        protected boolean visit(IEnumType currentType) {
            LinkedList<IEnumAttribute> attributesToPrepend = new LinkedList<>();
            for (IEnumAttribute localAttribute : ((EnumType)currentType).getEnumAttributesInternal(true,
                    includeLiteralName)) {
                if (!contains(localAttribute)) {
                    attributesToPrepend.addFirst(localAttribute);
                }
            }
            for (IEnumAttribute attributeToPrepend : attributesToPrepend) {
                allAttributes.addFirst(attributeToPrepend);
            }
            return true;
        }

        /**
         * If there are multiple attributes with the same name and none of them is marked as
         * inherited, add all those attributes (or "duplicates") to the list. This case is necessary
         * to detect errors during object validation.
         */
        private final boolean contains(IEnumAttribute attribute) {
            for (IEnumAttribute enumAttribute : allAttributes) {
                if (enumAttribute.getName().equals(attribute.getName()) && enumAttribute.isInherited()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class IsSubEnumTypeOfVisitor extends EnumTypeHierarchyVisitor {

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
        protected boolean visit(IEnumType currentEnumType) {
            if (currentEnumType == superEnumTypeCandidate) {
                subEnumType = true;
                return false;
            }
            return true;
        }

    }

}
