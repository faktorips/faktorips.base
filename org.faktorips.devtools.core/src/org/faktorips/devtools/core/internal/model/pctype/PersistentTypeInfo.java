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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IPersistentTypeInfo}.
 * 
 * @author Roman Grutza
 */
public class PersistentTypeInfo extends AtomicIpsObjectPart implements IPersistentTypeInfo {

    private String tableName = "";
    private String secondaryTableName = "";
    private InheritanceStrategy inheritanceStrategy = InheritanceStrategy.SINGLE_TABLE;
    private String discriminatorValue = "";
    private DiscriminatorDatatype discriminatorDatatype = DiscriminatorDatatype.STRING;
    private String discriminatorColumnName = "";

    private boolean notJoinedSubclass = true;

    // per default the persistent is disabled
    private boolean enabled = false;

    private boolean definesDiscriminatorColumn = false;

    public boolean isNotJoinedSubclass() {
        return notJoinedSubclass;
    }

    public void setNotJoinedSubclass(boolean notJoinedSubclass) {
        this.notJoinedSubclass = notJoinedSubclass;
    }

    public PersistentTypeInfo(IPolicyCmptType pcType, String id) {
        super(pcType, id);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDefinesDiscriminatorColumn() {
        return definesDiscriminatorColumn;
    }

    public void setEnabled(boolean enabled) {
        boolean oldValue = this.enabled;
        this.enabled = enabled;
        valueChanged(oldValue, enabled);
    }

    public void setDefinesDiscriminatorColumn(boolean definesDiscriminatorColumn) {
        if (!definesDiscriminatorColumn) {
            setDiscriminatorColumnName("");
        }
        boolean oldValue = this.definesDiscriminatorColumn;
        this.definesDiscriminatorColumn = definesDiscriminatorColumn;
        valueChanged(oldValue, definesDiscriminatorColumn);
    }

    public String getDiscriminatorColumnName() {
        return discriminatorColumnName;
    }

    public DiscriminatorDatatype getDiscriminatorDatatype() {
        return discriminatorDatatype;
    }

    public String getDiscriminatorValue() {
        return discriminatorValue;
    }

    public InheritanceStrategy getInheritanceStrategy() {
        return inheritanceStrategy;
    }

    public String getSecondaryTableName() {
        return secondaryTableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setDiscriminatorColumnName(String newDiscriminatorColumnName) {
        ArgumentCheck.notNull(newDiscriminatorColumnName);
        String oldValue = discriminatorColumnName;
        discriminatorColumnName = newDiscriminatorColumnName;

        valueChanged(oldValue, newDiscriminatorColumnName);
    }

    public void setDiscriminatorDatatype(DiscriminatorDatatype newDescriminatorDatatype) {
        ArgumentCheck.notNull(newDescriminatorDatatype);
        DiscriminatorDatatype oldValue = discriminatorDatatype;
        discriminatorDatatype = newDescriminatorDatatype;

        valueChanged(oldValue, newDescriminatorDatatype);
    }

    public void setDiscriminatorValue(String newDescriminatorValue) {
        ArgumentCheck.notNull(newDescriminatorValue);
        String oldValue = discriminatorValue;
        discriminatorValue = newDescriminatorValue;

        valueChanged(oldValue, newDescriminatorValue);
    }

    public void setInheritanceStrategy(InheritanceStrategy newStrategy) {
        InheritanceStrategy oldValue = inheritanceStrategy;
        inheritanceStrategy = newStrategy;
        notJoinedSubclass = (newStrategy != InheritanceStrategy.JOINED_SUBCLASS);

        valueChanged(oldValue, newStrategy);
    }

    public void setSecondaryTableName(String newSecondaryTableName) {
        ArgumentCheck.notNull(newSecondaryTableName);
        String oldValue = secondaryTableName;
        secondaryTableName = newSecondaryTableName;

        valueChanged(oldValue, newSecondaryTableName);
    }

    public void setTableName(String newTableName) {
        ArgumentCheck.notNull(newTableName);
        String oldValue = tableName;
        tableName = newTableName;

        valueChanged(oldValue, tableName);
    }

    public boolean isSecondaryTableNameRequired() {
        return false;
        // return inheritanceStrategy == InheritanceStrategy.MIXED;
    }

    @Override
    public void validateThis(MessageList msgList, IIpsProject ipsProject) {
        if (!enabled) {
            return;
        }
        try {
            BaseEntityFinder baseEntityFinder = new BaseEntityFinder();
            baseEntityFinder.start(getPolicyCmptType());

            validateInheritanceStrategy(msgList, baseEntityFinder.baseEntity);
            validateTableName(msgList, baseEntityFinder.baseEntity);
            validateDisriminator(msgList, baseEntityFinder.baseEntity);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void validateInheritanceStrategy(final MessageList msgList, IPolicyCmptType baseEntity)
            throws CoreException {
        IPolicyCmptType pcType = getPolicyCmptType();
        if (!pcType.hasSupertype()) {
            // if (inheritanceStrategy == InheritanceStrategy.MIXED) {
            // // MIXED strategy only makes sense with at least 2 hierarchy (inheritance) levels
            // String text =
            // "Use of MIXED inheritance strategy is discouraged for types denoting a hierarchy root.";
            // msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text,
            // Message.WARNING, this,
            // IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
            // }
            return;
        }
        new InheritanceStrategyMismatchVisitor(this, msgList).start(pcType);
    }

    private void validateTableName(MessageList msgList, IPolicyCmptType baseEntity) throws CoreException {
        if (inheritanceStrategy != InheritanceStrategy.SINGLE_TABLE) {
            validateThisTableName(msgList);
            return;
        }

        // single table inheritance
        if (getPolicyCmptType() == baseEntity) {
            validateThisTableName(msgList);
            return;
        }

        // table name must be empty
        if (!StringUtils.isEmpty(tableName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID,
                    "The table name must be empty because this is not the base entity and the inheritance strategy is: "
                            + inheritanceStrategy.toString(), Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }

        // if (isSecondaryTableNameRequired() &&
        // !PersistenceUtil.isValidDatabaseIdentifier(secondaryTableName)) {
        // String text = "The secondary table name is invalid.";
        // msgList.add(new Message(MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID, text,
        // Message.ERROR, this,
        // IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME));
        // }
        //
        // if (isSecondaryTableNameRequired() && tableName.equals(secondaryTableName)) {
        // String text = "Primary and secondary table names must not be equal.";
        // msgList.add(new Message(MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID, text,
        // Message.ERROR, this,
        // IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME));
        // }
    }

    private void validateThisTableName(MessageList msgList) {
        if (!PersistenceUtil.isValidDatabaseIdentifier(tableName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID, "The table name is invalid", Message.ERROR,
                    this, IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }
    }

    public IPolicyCmptType findBaseEntity() throws CoreException {
        BaseEntityFinder baseEntityFinder = new BaseEntityFinder();
        baseEntityFinder.start(getPolicyCmptType());
        return baseEntityFinder.baseEntity;
    }

    private void validateDisriminator(MessageList msgList, IPolicyCmptType baseEntity) throws CoreException {
        // check if this type not defines the discriminator column
        // but the discriminator details are not empty
        if (!isDefinesDiscriminatorColumn()) {
            if (StringUtils.isNotEmpty(discriminatorColumnName)) {
                String text = "The discriminator definition is not allowed here because this type doesn't define the discriminator column.";
                msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME));
            }
        } else {
            if (!PersistenceUtil.isValidDatabaseIdentifier(discriminatorColumnName) && isDefinesDiscriminatorColumn()) {
                String text = "The discriminator column name is invalid.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME));
            }
        }

        if (baseEntity == null) {
            // there must be a base entity, maybe an error
            return;
        }

        if (!isDefinesDiscriminatorColumn()
                && baseEntity == getPolicyCmptType()
                && (inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE || inheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS)) {
            String text = "The discriminator definition must be defined because this is the base entity of the inheritance hierarchy.";
            msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            return;
        }

        // discriminator necessary if single table or joined table inheritance strategy
        // and the base entity defines a discriminator
        if (isDefinesDiscriminatorColumn() && baseEntity != getPolicyCmptType()) {
            String text = "The discriminator definition is not allowed here because this type is not the base entity.";
            msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            return;
        }

        boolean discrValueMustBeEmpty = false;
        if (getPolicyCmptType().isAbstract()) {
            discrValueMustBeEmpty = true;
        } else {
            if (baseEntity.getPersistenceTypeInfo().isDefinesDiscriminatorColumn()) {
                discrValueMustBeEmpty = false;
            } else {
                discrValueMustBeEmpty = true;
            }
        }

        if (!discrValueMustBeEmpty) {
            if (StringUtils.isEmpty(discriminatorValue)) {
                String text = "The discriminator value must not be empty if the base entity defines the dicriminator column.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
                return;
            }
        } else {
            if (!StringUtils.isEmpty(discriminatorValue)) {
                String text = "The discriminator value must be empty if the base entity doesn't define a discriminator column or the type is abstract.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
                return;
            }
        }

        if (!baseEntity.getPersistenceTypeInfo().isDefinesDiscriminatorColumn()) {
            // discriminator not defined in hierarchy,
            // skip next validation steps
            return;
        }

        if (!discrValueMustBeEmpty
                && !baseEntity.getPersistenceTypeInfo().getDiscriminatorDatatype().isParsableToDiscriminatorDatatype(
                        discriminatorValue)) {
            String text = "The discriminator value does not conform to the specified descriminator datatype.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
            return;
        }

        IPolicyCmptType pcType = getPolicyCmptType();
        DiscriminatorValidator dValidator = new DiscriminatorValidator(this);
        dValidator.start(pcType);
        if (dValidator.conflictingTypeInfo != null) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID, dValidator.errorMessage, Message.ERROR,
                    this, dValidator.errorProperty));
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
        element.setAttribute(PROPERTY_SECONDARY_TABLE_NAME, "" + secondaryTableName); //$NON-NLS-1$
        element.setAttribute(PROPERTY_INHERITANCE_STRATEGY, "" + inheritanceStrategy); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DISCRIMINATOR_COLUMN_NAME, "" + discriminatorColumnName); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DISCRIMINATOR_DATATYPE, "" + discriminatorDatatype); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DISCRIMINATOR_VALUE, "" + discriminatorValue); //$NON-NLS-1$
        element.setAttribute(PROPERTY_ENABLED, "" + Boolean.valueOf(enabled).toString()); //$NON-NLS-1$
        element.setAttribute(PROPERTY_DEFINES_DISCRIMINATOR_COLUMN, "" //$NON-NLS-1$
                + Boolean.valueOf(definesDiscriminatorColumn).toString());
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        tableName = element.getAttribute(PROPERTY_TABLE_NAME);
        secondaryTableName = element.getAttribute(PROPERTY_SECONDARY_TABLE_NAME);
        inheritanceStrategy = InheritanceStrategy.valueOf(element.getAttribute(PROPERTY_INHERITANCE_STRATEGY));
        discriminatorColumnName = element.getAttribute(PROPERTY_DISCRIMINATOR_COLUMN_NAME);
        discriminatorDatatype = DiscriminatorDatatype.valueOf(element.getAttribute(PROPERTY_DISCRIMINATOR_DATATYPE));
        discriminatorValue = element.getAttribute(PROPERTY_DISCRIMINATOR_VALUE);
        enabled = Boolean.valueOf(element.getAttribute(PROPERTY_ENABLED));
        definesDiscriminatorColumn = Boolean.valueOf(element.getAttribute(PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
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
            if (supertypeStrategy == inheritanceStrategy) {
                return true;
            }
            // the only case where one can combine different inheritance strategies:
            // subclass is MIXED, superclass is SINGLE_TABLE
            // if (supertypeStrategy == InheritanceStrategy.SINGLE_TABLE
            // && inheritanceStrategy == InheritanceStrategy.MIXED) {
            // return true;
            // }

            String text = "Invalid combination of inheritance strategies. Resolve by changing strategy for either "
                    + currentType.getUnqualifiedName() + "(" + supertypeStrategy + ") or "
                    + persistentTypeInfo.getIpsObject().getUnqualifiedName() + "(" + inheritanceStrategy + ")";
            msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text, Message.ERROR,
                    persistentTypeInfo, IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
            return false;
        }
    }

    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }

    private final static class TableNameValidator extends PolicyCmptTypeHierarchyVisitor {

        private final InheritanceStrategy inheritanceStrategy;
        private final String primaryTableName;
        private final List<String> tableNames = new ArrayList<String>();

        // If these fields are not null errors exist in the naming of the tables
        private IPersistentTypeInfo conflictingTypeInfo;
        private String errorMessage;

        public TableNameValidator(IPersistentTypeInfo typeInfo, String tableName) {
            inheritanceStrategy = typeInfo.getInheritanceStrategy();
            primaryTableName = typeInfo.getTableName();
            if (!PersistenceUtil.isValidDatabaseIdentifier(tableName)) {
                conflictingTypeInfo = typeInfo;
            }
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            if (conflictingTypeInfo != null) {
                errorMessage = "The table name is invalid.";
                return false;
            }

            String currentTableName = currentType.getPersistenceTypeInfo().getTableName();
            if (inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE) {
                // || inheritanceStrategy == InheritanceStrategy.MIXED) {
                // table names must be equal in whole supertype hierarchy
                if (!primaryTableName.equals(currentType.getPersistenceTypeInfo().getTableName())) {
                    conflictingTypeInfo = currentType.getPersistenceTypeInfo();
                    errorMessage = "The table name does not match the table name in the supertype "
                            + currentType.getUnqualifiedName() + "(" + currentTableName + ")";
                    return false;
                }
            }

            if (inheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS) {
                // each table name must be unique in whole supertype hierarchy
                if (tableNames.contains(currentTableName)) {
                    conflictingTypeInfo = currentType.getPersistenceTypeInfo();
                    errorMessage = "The table name is already defined in the supertype "
                            + currentType.getUnqualifiedName();
                    return false;
                }
                tableNames.add(currentTableName);
            }

            return true;
        }
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

            if (inheritanceStrategy != InheritanceStrategy.SINGLE_TABLE) {
                return false;
            }

            // Invariants:
            // - discriminator values must be unique
            if (discriminatorValues.contains(currentTypeInfo.getDiscriminatorValue())) {
                conflictingTypeInfo = currentTypeInfo;
                errorMessage = "The discriminator value is already defined in the supertype "
                        + currentType.getUnqualifiedName();
                errorProperty = IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE;
                return false;
            } else {
                discriminatorValues.add(currentTypeInfo.getDiscriminatorValue());
            }

            return true;
        }
    }

    /*
     * The base entity is the entity which can define the discriminator. A base entity has no
     * supertype and must be an JPA entity.
     */
    private class BaseEntityFinder extends PolicyCmptTypeHierarchyVisitor {
        private IPolicyCmptType baseEntity = null;

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPersistentTypeInfo persistenceTypeInfo = currentType.getPersistenceTypeInfo();

            if (StringUtils.isEmpty(currentType.getSupertype())) {
                if (persistenceTypeInfo.isEnabled()) {
                    baseEntity = currentType;
                }
                return false;
            } else {
                if (persistenceTypeInfo.isEnabled()) {
                    baseEntity = currentType;
                }
            }
            return true;
        }
    }

}
