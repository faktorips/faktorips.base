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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IPersistentTypeInfo}.
 * 
 * @author Roman Grutza
 */
public class PersistentTypeInfo extends AtomicIpsObjectPart implements IPersistentTypeInfo {

    private String tableName = ""; //$NON-NLS-1$

    /**
     * the strategy which should be used to store the hierarchy objects the strategy must only be
     * defined on the root entity, the subclasses will adopt the same strategy that is specified in
     * the root entity superclass. the JPA default is single table
     */
    private InheritanceStrategy inheritanceStrategy = InheritanceStrategy.SINGLE_TABLE;

    /**
     * class indicator the discriminator should used in single table and join subclass strategy some
     * vendors offer implementations of joined inheritance without the use of a discriminator column
     * but the discriminator columns should be used to ensure the portability
     */
    private String discriminatorValue = ""; //$NON-NLS-1$

    private DiscriminatorDatatype discriminatorDatatype = DiscriminatorDatatype.STRING;

    private String discriminatorColumnName = ""; //$NON-NLS-1$

    /** per default the persistent is disabled */
    private PersistentType persistentType = PersistentType.NONE;

    /**
     * specifies if the associate type defines the discriminator or not note that only the root
     * entity (root entity) can define the discriminator
     */
    private boolean definesDiscriminatorColumn = false;

    /** if true the table name of the supertype will be used */
    private boolean useTableDefinedInSupertype = false;

    public PersistentTypeInfo(IPolicyCmptType pcType, String id) {
        super(pcType, id);
    }

    @Override
    public PersistentType getPersistentType() {
        return persistentType;
    }

    @Override
    public boolean isEnabled() {
        return persistentType == PersistentType.ENTITY || persistentType == PersistentType.MAPPED_SUPERCLASS;
    }

    @Override
    public boolean isDefinesDiscriminatorColumn() {
        return definesDiscriminatorColumn;
    }

    @Override
    public void setPersistentType(PersistentType persistentType) {
        if (persistentType != PersistentType.ENTITY) {
            setTableName(""); //$NON-NLS-1$
            setDefinesDiscriminatorColumn(false);
            setDiscriminatorValue(""); //$NON-NLS-1$
        }
        PersistentType oldValue = this.persistentType;
        this.persistentType = persistentType;
        valueChanged(oldValue, persistentType);
    }

    @Override
    public void setDefinesDiscriminatorColumn(boolean definesDiscriminatorColumn) {
        if (!definesDiscriminatorColumn) {
            setDiscriminatorColumnName(""); //$NON-NLS-1$
        }
        boolean oldValue = this.definesDiscriminatorColumn;
        this.definesDiscriminatorColumn = definesDiscriminatorColumn;
        valueChanged(oldValue, definesDiscriminatorColumn);
    }

    @Override
    public void setUseTableDefinedInSupertype(boolean useTableDefinedInSupertype) {
        setTableName(""); //$NON-NLS-1$
        boolean oldValue = this.useTableDefinedInSupertype;
        this.useTableDefinedInSupertype = useTableDefinedInSupertype;
        valueChanged(oldValue, useTableDefinedInSupertype);
    }

    @Override
    public boolean isUseTableDefinedInSupertype() {
        return useTableDefinedInSupertype;
    }

    @Override
    public String getDiscriminatorColumnName() {
        return discriminatorColumnName;
    }

    @Override
    public DiscriminatorDatatype getDiscriminatorDatatype() {
        return discriminatorDatatype;
    }

    @Override
    public String getDiscriminatorValue() {
        return discriminatorValue;
    }

    @Override
    public InheritanceStrategy getInheritanceStrategy() {
        return inheritanceStrategy;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setDiscriminatorColumnName(String newDiscriminatorColumnName) {
        ArgumentCheck.notNull(newDiscriminatorColumnName);
        String oldValue = discriminatorColumnName;
        discriminatorColumnName = newDiscriminatorColumnName;

        valueChanged(oldValue, newDiscriminatorColumnName);
    }

    @Override
    public void setDiscriminatorDatatype(DiscriminatorDatatype newDescriminatorDatatype) {
        ArgumentCheck.notNull(newDescriminatorDatatype);
        DiscriminatorDatatype oldValue = discriminatorDatatype;
        discriminatorDatatype = newDescriminatorDatatype;

        valueChanged(oldValue, newDescriminatorDatatype);
    }

    @Override
    public void setDiscriminatorValue(String newDescriminatorValue) {
        ArgumentCheck.notNull(newDescriminatorValue);
        String oldValue = discriminatorValue;
        discriminatorValue = newDescriminatorValue;

        valueChanged(oldValue, newDescriminatorValue);
    }

    @Override
    public void setInheritanceStrategy(InheritanceStrategy newStrategy) {
        ArgumentCheck.notNull(newStrategy);
        InheritanceStrategy oldValue = inheritanceStrategy;

        try {
            setInheritanceStrategyInternal(newStrategy);
        } catch (CoreException e) {
            // maybe there was an error searching the root entity
            // this error should be reported before, thus just ignore it by throwing a new runtime
            // exception
            throw new RuntimeException(e);
        }

        valueChanged(oldValue, newStrategy);
    }

    public void setInheritanceStrategyInternal(InheritanceStrategy newStrategy) throws CoreException {
        if (InheritanceStrategy.SINGLE_TABLE.equals(newStrategy) && !isRootEntity()) {
            // initialize defaults for single table inheritance strategy if this is not the roor
            // entity
            useTableDefinedInSupertype = true;
            tableName = ""; //$NON-NLS-1$
        }
        inheritanceStrategy = newStrategy;
    }

    @Override
    public void setTableName(String newTableName) {
        ArgumentCheck.notNull(newTableName);
        String oldValue = tableName;
        tableName = newTableName;

        valueChanged(oldValue, tableName);
    }

    public boolean isSecondaryTableNameRequired() {
        return false;
    }

    @Override
    public void validateThis(MessageList msgList, IIpsProject ipsProject) {
        if (persistentType == PersistentType.NONE) {
            return;
        }
        try {
            RooEntityFinder rootEntityFinder = new RooEntityFinder();
            rootEntityFinder.start(getPolicyCmptType());

            validateInheritanceStrategy(msgList, rootEntityFinder.rooEntity);
            validateTableName(msgList, rootEntityFinder.rooEntity);
            validateDisriminator(msgList, rootEntityFinder.rooEntity);
            validateUniqueColumnNameInHierarchy(msgList);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void validateInheritanceStrategy(final MessageList msgList, IPolicyCmptType rooEntity) throws CoreException {
        IPolicyCmptType pcType = getPolicyCmptType();

        if (!pcType.hasSupertype()) {
            return;
        }

        if (!isRootEntity(rooEntity) && InheritanceStrategy.SINGLE_TABLE.equals(inheritanceStrategy)
                && !useTableDefinedInSupertype) {
            String text = NLS.bind(Messages.PersistentTypeInfo_msgTableNameOfRootEntityMustBeUsed,
                    inheritanceStrategy.toString());
            msgList.add(new Message(MSGCODE_MUST_USE_TABLE_FROM_ROOT_ENTITY, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE));
        }

        new InheritanceStrategyMismatchVisitor(this, msgList).start(pcType);
    }

    private void validateTableName(MessageList msgList, IPolicyCmptType rootEntity) {
        if (getPersistentType() == PersistentType.MAPPED_SUPERCLASS) {
            // in case of mapped superclass the table names must be empty
            if (!StringUtils.isEmpty(tableName)) {
                msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID,
                        Messages.PersistentTypeInfo_msgTableNameMustBeEmptyMappedSuperclass, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_TABLE_NAME));
            }
            return;
        }

        // validate if the table name defined in the supertype should be used
        if (isUseTableDefinedInSupertype()) {
            if (StringUtils.isEmpty(getPolicyCmptType().getSupertype())) {
                msgList.add(new Message(MSGCODE_USE_TABLE_DEFINED_IN_SUPERTYPE_NOT_ALLOWED,
                        Messages.PersistentTypeInfo_msgUseTableDefInSupertypIsNotAllowed, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE));
                return;
            }
            if (!StringUtils.isEmpty(tableName)) {
                msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID,
                        Messages.PersistentTypeInfo_msgTableNameMustBeEmptyBecauseNameDefinedInSupertypeShouldBeUsed,
                        Message.ERROR, this, IPersistentTypeInfo.PROPERTY_TABLE_NAME));
                return;
            }
        }

        // validate max table name length
        int maxTableNameLenght = getIpsProject().getProperties().getPersistenceOptions().getMaxTableNameLength();
        if (StringUtils.isNotBlank(tableName) && tableName.length() > maxTableNameLenght) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID, NLS.bind(
                    Messages.PersistentTypeInfo_msgTableNameExceedsMaximumLength, tableName.length(),
                    maxTableNameLenght), Message.ERROR, this, IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }

        // validate none single table strategy
        if (inheritanceStrategy != InheritanceStrategy.SINGLE_TABLE) {
            // if a subtype type has no attributes then we didn't need an own table
            if (StringUtils.isNotEmpty(getPolicyCmptType().getSupertype())
                    && getPolicyCmptType().getAttributes().length == 0) {
                return;
            }
            validateTableNameValidIdentifier(msgList);
            return;
        }

        // in single table inheritance only the root entity table name must not be empty
        if (isRootEntity(rootEntity)) {
            validateTableNameValidIdentifier(msgList);
            return;
        }

        // in single table inheritance all not root entity table names must be empty
        if (!StringUtils.isEmpty(tableName)) {
            String text = NLS.bind(Messages.PersistentTypeInfo_msgTableNameMustBeEmptyNotRootEntityAndInhStrategyIs,
                    inheritanceStrategy.toString());
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }
    }

    private void validateTableNameValidIdentifier(MessageList msgList) {
        if (isUseTableDefinedInSupertype()) {
            return;
        }

        if (!PersistenceUtil.isValidDatabaseIdentifier(tableName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID,
                    Messages.PersistentTypeInfo_msgTableNameInvalid, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }
    }

    @Override
    public IPolicyCmptType findRootEntity() throws CoreException {
        RooEntityFinder rooEntityFinder = new RooEntityFinder();
        rooEntityFinder.start(getPolicyCmptType());
        return rooEntityFinder.rooEntity;
    }

    private void validateDisriminator(MessageList msgList, IPolicyCmptType rootEntity) throws CoreException {
        if (getPersistentType() == PersistentType.MAPPED_SUPERCLASS) {
            if (isDefinesDiscriminatorColumn()) {
                String text = Messages.PersistentTypeInfo_msgDiscriminatorDefinitionNotAllowedBecauseMappedSuperclass;
                msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            }
            if (StringUtils.isNotEmpty(discriminatorValue)) {
                String text = Messages.PersistentTypeInfo_msgDiscriminatorValueMustBeEmptyBecauseMappedSuperclass;
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            }
        }

        // check if this type not defines the discriminator column
        // but the discriminator details are not empty
        if (!isDefinesDiscriminatorColumn()) {
            if (StringUtils.isNotEmpty(discriminatorColumnName)) {
                String text = Messages.PersistentTypeInfo_msgDiscriminatorDefinitionNotAllowedTypeNotDefDiscrColumn;
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME));
            }
        } else {
            if (isDefinesDiscriminatorColumn() && !PersistenceUtil.isValidDatabaseIdentifier(discriminatorColumnName)) {
                String text = Messages.PersistentTypeInfo_msgDiscriminatorColumnNameIsInvalid;
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME));
            }
        }

        if (rootEntity == null) {
            // there must always be a root entity, maybe an error
            return;
        }

        if (rootEntity != getPolicyCmptType()
                && !rootEntity.getPersistenceTypeInfo().isDefinesDiscriminatorColumn()
                && (inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE || inheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS)
                && getPolicyCmptType().getAttributes().length > 0) {
            // TODO JPA Joerg wenn das mit getPolicyCmptType().getAttributes().length > 0 stimmt
            // zus. noch transiente attribute ausschliessen
            String text = NLS.bind(Messages.PersistentTypeInfo_msgDiscriminatorMustBeDefinedInTheRootEntity,
                    rootEntity.getUnqualifiedName());
            msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            return;
        }

        // discriminator necessary if single table or joined table inheritance strategy
        // and the root entity defines a discriminator
        if (isDefinesDiscriminatorColumn() && rootEntity != getPolicyCmptType()) {
            String text = Messages.PersistentTypeInfo_msgDiscriminatorDefinitionNotAllowedNotRootEntity;
            msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            return;
        }

        boolean discrValueMustBeEmpty = false;
        if (getPolicyCmptType().isAbstract()) {
            discrValueMustBeEmpty = true;
        } else {
            if (rootEntity.getPersistenceTypeInfo().isDefinesDiscriminatorColumn()) {
                discrValueMustBeEmpty = false;
            } else {
                discrValueMustBeEmpty = true;
            }
        }

        if (!discrValueMustBeEmpty) {
            if (StringUtils.isEmpty(discriminatorValue)) {
                String text = Messages.PersistentTypeInfo_msgDiscriminatorValueMustNotBeEmpty;
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
                return;
            }
        } else {
            if (!StringUtils.isEmpty(discriminatorValue)) {
                String text = Messages.PersistentTypeInfo_msgDiscriminatorValueMustBeEmpty;
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
                return;
            }
        }

        if (!rootEntity.getPersistenceTypeInfo().isDefinesDiscriminatorColumn()) {
            // discriminator not defined in hierarchy,
            // skip next validation steps
            return;
        }

        if (!discrValueMustBeEmpty
                && !rootEntity.getPersistenceTypeInfo().getDiscriminatorDatatype()
                        .isParsableToDiscriminatorDatatype(discriminatorValue)) {
            String text = Messages.PersistentTypeInfo_msgDiscriminatorValueNotConform;
            msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
            return;
        }

        IPolicyCmptType pcType = getPolicyCmptType();
        if (!pcType.isAbstract()) {
            DiscriminatorValidator dValidator = new DiscriminatorValidator(this);
            dValidator.start(pcType);
            if (dValidator.conflictingTypeInfo != null) {
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, dValidator.errorMessage,
                        Message.ERROR, this, dValidator.errorProperty));
            }
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TABLE_NAME, "" + tableName); //$NON-NLS-1$
        element.setAttribute(PROPERTY_INHERITANCE_STRATEGY, "" + inheritanceStrategy); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DISCRIMINATOR_COLUMN_NAME, "" + discriminatorColumnName); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DISCRIMINATOR_DATATYPE, "" + discriminatorDatatype); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DISCRIMINATOR_VALUE, "" + discriminatorValue); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DEFINES_DISCRIMINATOR_COLUMN, "" //$NON-NLS-1$
                + Boolean.valueOf(definesDiscriminatorColumn).toString());
        element.setAttribute(PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE, "" //$NON-NLS-1$
                + Boolean.valueOf(useTableDefinedInSupertype).toString());
        element.setAttribute(PROPERTY_PERSISTENT_TYPE, "" + persistentType); //$NON-NLS-1$
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        tableName = element.getAttribute(PROPERTY_TABLE_NAME);
        inheritanceStrategy = InheritanceStrategy.valueOf(element.getAttribute(PROPERTY_INHERITANCE_STRATEGY));
        discriminatorColumnName = element.getAttribute(PROPERTY_DISCRIMINATOR_COLUMN_NAME);
        discriminatorDatatype = DiscriminatorDatatype.valueOf(element.getAttribute(PROPERTY_DISCRIMINATOR_DATATYPE));
        discriminatorValue = element.getAttribute(PROPERTY_DISCRIMINATOR_VALUE);
        initPersistentTypeWithWorkaround(element);
        definesDiscriminatorColumn = Boolean.valueOf(element.getAttribute(PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
        useTableDefinedInSupertype = Boolean.valueOf(element.getAttribute(PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE));
    }

    /**
     * Read persistent type, note that this method is downwardly compatible to v 3.0.0.ms1. In the
     * older version the persistent could only be enabled or disabled using the 'enabled' attribute.
     */
    private void initPersistentTypeWithWorkaround(Element element) {
        String attrPersistentType = element.getAttribute(PROPERTY_PERSISTENT_TYPE);
        if (StringUtils.isNotEmpty(attrPersistentType)) {
            persistentType = PersistentType.valueOf(attrPersistentType);
        } else {
            if (Boolean.valueOf(element.getAttribute("enabled"))) {
                persistentType = PersistentType.ENTITY;
            } else {
                persistentType = PersistentType.NONE;
            }
        }
    }

    /**
     * Adds error messages to MessageList if the type hierarchy is inconsistent relating to the
     * inheritance strategy.
     */
    private final class InheritanceStrategyMismatchVisitor extends PolicyCmptTypeHierarchyVisitor {

        private final MessageList msgList;

        private final PersistentTypeInfo persistentTypeInfo;

        private InheritanceStrategyMismatchVisitor(PersistentTypeInfo persistentTypeInfo, MessageList msgList) {
            this.persistentTypeInfo = persistentTypeInfo;
            this.msgList = msgList;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            InheritanceStrategy supertypeStrategy = currentType.getPersistenceTypeInfo().getInheritanceStrategy();
            if (supertypeStrategy == inheritanceStrategy
                    || (currentType.getPersistenceTypeInfo().getPersistentType() != PersistentType.ENTITY)) {
                return true;
            }

            String text = NLS.bind(Messages.PersistentTypeInfo_msgInvalidInheritanceStratedyCombination, new Object[] {
                    currentType.getUnqualifiedName(), supertypeStrategy,
                    persistentTypeInfo.getIpsObject().getUnqualifiedName(), inheritanceStrategy });
            msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text, Message.ERROR,
                    persistentTypeInfo, IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
            return false;
        }
    }

    @Override
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }

    private final static class DiscriminatorValidator extends PolicyCmptTypeHierarchyVisitor {

        private final InheritanceStrategy inheritanceStrategy;
        private final List<String> discriminatorValues = new ArrayList<String>();

        // If these fields are not null errors exist in the naming of the tables
        private IPersistentTypeInfo conflictingTypeInfo;
        private String errorMessage;
        public String errorProperty;

        public DiscriminatorValidator(IPersistentTypeInfo typeInfo) {
            inheritanceStrategy = typeInfo.getInheritanceStrategy();
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            IPersistentTypeInfo currentTypeInfo = currentType.getPersistenceTypeInfo();
            if (currentType.isAbstract()) {
                return true;
            }
            if (!(inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE || inheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS)) {
                // wrong inheritance strategy abort
                return false;
            }

            // Invariants:
            // - discriminator values must be unique
            if (discriminatorValues.contains(currentTypeInfo.getDiscriminatorValue())) {
                conflictingTypeInfo = currentTypeInfo;
                errorMessage = NLS.bind(Messages.PersistentTypeInfo_msgDiscriminatorAlreadyDefined,
                        currentTypeInfo.getDiscriminatorValue(), currentType.getUnqualifiedName());
                errorProperty = IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE;
                return false;
            } else {
                discriminatorValues.add(currentTypeInfo.getDiscriminatorValue());
            }

            return true;
        }
    }

    /**
     * The root entity is the entity which can define the discriminator. A root entity has no
     * supertype and must be an JPA entity.
     */
    private class RooEntityFinder extends PolicyCmptTypeHierarchyVisitor {

        private IPolicyCmptType rooEntity = null;

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPersistentTypeInfo persistenceTypeInfo = currentType.getPersistenceTypeInfo();

            if (StringUtils.isEmpty(currentType.getSupertype())) {
                if (persistenceTypeInfo.getPersistentType() == PersistentType.ENTITY) {
                    rooEntity = currentType;
                }
                return false;
            } else {
                if (persistenceTypeInfo.getPersistentType() == PersistentType.ENTITY) {
                    rooEntity = currentType;
                }
            }
            return true;
        }
    }

    private void validateUniqueColumnNameInHierarchy(MessageList msgList) throws CoreException {
        ColumnNameCollector columnNameCollector = new ColumnNameCollector();
        columnNameCollector.start(getPolicyCmptType());

        Map<String, Object> persistentObjectesBySameColumnName = columnNameCollector
                .getPersistentObjectesBySameColumnName();
        for (String columnName : persistentObjectesBySameColumnName.keySet()) {
            addMessagesDuplicateColumnName(msgList, columnName, persistentObjectesBySameColumnName.get(columnName));
        }
    }

    @SuppressWarnings("unchecked")
    // suppressed because we use either a object which holds the column name or a list of objects
    // with the same column name
    private void addMessagesDuplicateColumnName(MessageList msgList,
            String columnName,
            Object objectOrObjectsUseSameColumnName) {

        if (objectOrObjectsUseSameColumnName instanceof List<?>) {
            List<ObjectProperty> objectsUseSameColumnName = (List<ObjectProperty>)objectOrObjectsUseSameColumnName;
            String objectsAsString = ""; //$NON-NLS-1$
            for (ObjectProperty objectProperty : objectsUseSameColumnName) {
                objectsAsString += objectPropertyAsString(objectProperty);
            }
            for (ObjectProperty objectProperty : objectsUseSameColumnName) {
                if (getPolicyCmptTypeFromObjectProperty(objectProperty).getPersistenceTypeInfo() == this) {
                    // append the other object property to the message text
                    String objAsString = StringUtils.strip(
                            objectsAsString.replace(objectPropertyAsString(objectProperty), ""), ", "); //$NON-NLS-1$ //$NON-NLS-2$
                    String message = objAsString.length() == 0 ? "" : Messages.PersistentTypeInfo_msgFoundDuplicateColumnNameIn; //$NON-NLS-1$
                    addMessageDuplicateColumnName(msgList, objectProperty, columnName + NLS.bind(message, objAsString));
                }
            }
        }
    }

    private IPolicyCmptType getPolicyCmptTypeFromObjectProperty(ObjectProperty objectProperty) {
        return (IPolicyCmptType)((IIpsObjectPart)objectProperty.getObject()).getIpsObject();
    }

    private String objectPropertyAsString(ObjectProperty objectProperty) {
        return NLS.bind(" {0}#{1}, ", getPolicyCmptTypeFromObjectProperty(objectProperty).getUnqualifiedName(), //$NON-NLS-1$
                objectProperty.getProperty());
    }

    private void addMessageDuplicateColumnName(MessageList msgList, ObjectProperty objectProperty, String detailText) {
        msgList.add(new Message(MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME, NLS.bind(
                Messages.PersistentTypeInfo_msgDuplicateColumnName, detailText), Message.ERROR, objectProperty));
    }

    private static class ColumnNameCollector extends PolicyCmptTypeHierarchyVisitor {
        private Map<String, Object> persistentObjectesBySameColumnName = new HashMap<String, Object>();

        public Map<String, Object> getPersistentObjectesBySameColumnName() {
            return persistentObjectesBySameColumnName;
        }

        private boolean isPersistentAttribute(IPolicyCmptTypeAttribute attribute) {
            return attribute.getPersistenceAttributeInfo().isPersistentAttribute();
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            InheritanceStrategy currentInheritanceStrategy = currentType.getPersistenceTypeInfo()
                    .getInheritanceStrategy();

            IPolicyCmptTypeAttribute[] policyCmptTypeAttributes = currentType.getPolicyCmptTypeAttributes();
            for (IPolicyCmptTypeAttribute currentAttribute : policyCmptTypeAttributes) {
                if (isPersistentAttribute(currentAttribute)) {
                    addIfNotEmpty(currentAttribute.getPersistenceAttributeInfo().getTableColumnName(),
                            new ObjectProperty(currentAttribute.getPersistenceAttributeInfo(),
                                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
                }
            }

            collectAssociationColumnsIfExists(currentType);

            if (currentInheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS) {
                // do not collect supertype attributes, since each table of a JOINED_SUBCLASS
                // hierarchy can have the same column names
                return false;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        // suppressed because we use either a object which holds the column name or a list of
        // objects with the same column name
        private void addColumnName(String columnName, ObjectProperty objectProperty) {
            if (StringUtils.isEmpty(columnName)) {
                return;
            }
            Object objectOrObjectsSameColumnName = persistentObjectesBySameColumnName.get(columnName);
            if (objectOrObjectsSameColumnName == null) {
                persistentObjectesBySameColumnName.put(columnName, objectProperty);
            } else {
                List<ObjectProperty> objectsUseSameColumnName = null;
                if (objectOrObjectsSameColumnName instanceof ObjectProperty) {
                    objectsUseSameColumnName = new ArrayList<ObjectProperty>();
                    persistentObjectesBySameColumnName.put(columnName, objectsUseSameColumnName);
                    objectsUseSameColumnName.add((ObjectProperty)objectOrObjectsSameColumnName);
                } else {
                    objectsUseSameColumnName = (List<ObjectProperty>)objectOrObjectsSameColumnName;
                }
                objectsUseSameColumnName.add(objectProperty);
            }
        }

        private void collectAssociationColumnsIfExists(IPolicyCmptType currentType) {
            IPolicyCmptTypeAssociation[] policyCmptTypeAssociations = currentType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyCmptTypeAssociations) {
                IPersistentAssociationInfo pAssInfo = policyCmptTypeAssociation.getPersistenceAssociatonInfo();
                addIfNotEmpty(pAssInfo.getJoinColumnName(), new ObjectProperty(pAssInfo,
                        IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME));
                addIfNotEmpty(pAssInfo.getSourceColumnName(), new ObjectProperty(pAssInfo,
                        IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME));
                addIfNotEmpty(pAssInfo.getTargetColumnName(), new ObjectProperty(pAssInfo,
                        IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME));
            }
        }

        private void addIfNotEmpty(String columnName, ObjectProperty objectProperty) {
            if (StringUtils.isNotEmpty(columnName)) {
                addColumnName(columnName, objectProperty);
            }
        }
    }

    private boolean isRootEntity() throws CoreException {
        RooEntityFinder rootEntityFinder = new RooEntityFinder();
        rootEntityFinder.start(getPolicyCmptType());
        return isRootEntity(rootEntityFinder.rooEntity);
    }

    private boolean isRootEntity(IPolicyCmptType rootEntity) {
        return getPolicyCmptType() == rootEntity;
    }
}
