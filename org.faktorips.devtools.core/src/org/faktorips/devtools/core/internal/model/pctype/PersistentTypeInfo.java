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
import org.eclipse.osgi.util.NLS;
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

    // the strategy which should be used to store the hierarchy objects
    // the strategy must only be defined on the root entity, the subclasses will adopt the
    // same strategy that is specified in the root entity superclass.
    // the JPA default is single table
    private InheritanceStrategy inheritanceStrategy = InheritanceStrategy.SINGLE_TABLE;

    // class indicator
    // the discriminator should used in single table and join subclass strategy
    // some vendors offer implementations of joined inheritance without the use of a discriminator
    // column but the discriminator columns should be used to ensure the portability
    private String discriminatorValue = "";
    private DiscriminatorDatatype discriminatorDatatype = DiscriminatorDatatype.STRING;
    private String discriminatorColumnName = "";

    // per default the persistent is disabled
    private boolean enabled = false;

    // specifies if the associate type defines the discriminator or not
    // note that only the root entity (root entity) can define the discriminator
    private boolean definesDiscriminatorColumn = false;

    // if true the table name of the supertype will be used
    private boolean useTableDefinedInSupertype = false;

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

    public void setUseTableDefinedInSupertype(boolean useTableDefinedInSupertype) {
        if (useTableDefinedInSupertype) {
            setTableName("");
        }
        boolean oldValue = this.useTableDefinedInSupertype;
        this.useTableDefinedInSupertype = useTableDefinedInSupertype;
        valueChanged(oldValue, useTableDefinedInSupertype);
    }

    public boolean isUseTableDefinedInSupertype() {
        return useTableDefinedInSupertype;
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
        ArgumentCheck.notNull(newStrategy);
        InheritanceStrategy oldValue = inheritanceStrategy;
        inheritanceStrategy = newStrategy;

        valueChanged(oldValue, newStrategy);
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
            RooEntityFinder rootEntityFinder = new RooEntityFinder();
            rootEntityFinder.start(getPolicyCmptType());

            validateInheritanceStrategy(msgList);
            validateTableName(msgList, rootEntityFinder.rooEntity);
            validateDisriminator(msgList, rootEntityFinder.rooEntity);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void validateInheritanceStrategy(final MessageList msgList) throws CoreException {
        IPolicyCmptType pcType = getPolicyCmptType();
        if (!pcType.hasSupertype()) {
            return;
        }
        new InheritanceStrategyMismatchVisitor(this, msgList).start(pcType);
    }

    private void validateTableName(MessageList msgList, IPolicyCmptType rootEntity) {
        if (isUseTableDefinedInSupertype()) {
            if (!StringUtils.isEmpty(tableName)) {
                msgList
                        .add(new Message(
                                MSGCODE_PERSISTENCE_TABLE_NAME_INVALID,
                                "The table name must be empty because the table name defined in the super type should be used.",
                                Message.ERROR, this, IPersistentTypeInfo.PROPERTY_TABLE_NAME));
                return;
            }
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
        if (getPolicyCmptType() == rootEntity) {
            validateTableNameValidIdentifier(msgList);
            return;
        }

        // in single table inheritance all not root entity table names must be empty
        if (!StringUtils.isEmpty(tableName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID,
                    "The table name must be empty because this is not the root entity and the inheritance strategy is: "
                            + inheritanceStrategy.toString(), Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }
    }

    private void validateTableNameValidIdentifier(MessageList msgList) {
        if (isUseTableDefinedInSupertype()) {
            return;
        }
        if (!PersistenceUtil.isValidDatabaseIdentifier(tableName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID, "The table name is invalid", Message.ERROR,
                    this, IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }
    }

    public IPolicyCmptType findRootEntity() throws CoreException {
        RooEntityFinder baseEntityFinder = new RooEntityFinder();
        baseEntityFinder.start(getPolicyCmptType());
        return baseEntityFinder.rooEntity;
    }

    private void validateDisriminator(MessageList msgList, IPolicyCmptType baseEntity) throws CoreException {
        // check if this type not defines the discriminator column
        // but the discriminator details are not empty
        if (!isDefinesDiscriminatorColumn()) {
            if (StringUtils.isNotEmpty(discriminatorColumnName)) {
                String text = "The discriminator definition is not allowed here because this type doesn't define the discriminator column.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME));
            }
        } else {
            if (isDefinesDiscriminatorColumn() && !PersistenceUtil.isValidDatabaseIdentifier(discriminatorColumnName)) {
                String text = "The discriminator column name is invalid.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME));
            }
        }

        if (baseEntity == null) {
            // there must always be a root entity, maybe an error
            return;
        }

        if (getPolicyCmptType().hasSupertype()
                && !baseEntity.getPersistenceTypeInfo().isDefinesDiscriminatorColumn()
                && (inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE || inheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS)
                && getPolicyCmptType().getAttributes().length > 0) {
            String text = NLS
                    .bind(
                            "The discriminator definition is missing, the discriminator must be defined in the root entity {0}.",
                            baseEntity.getUnqualifiedName());
            msgList.add(new Message(MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
            return;
        }

        // discriminator necessary if single table or joined table inheritance strategy
        // and the root entity defines a discriminator
        if (isDefinesDiscriminatorColumn() && baseEntity != getPolicyCmptType()) {
            String text = "The discriminator definition is not allowed here because this type is not the root entity.";
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
                String text = "The discriminator value must not be empty if the root entity defines the dicriminator column and the type is not abstract.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                        IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
                return;
            }
        } else {
            if (!StringUtils.isEmpty(discriminatorValue)) {
                String text = "The discriminator value must be empty if the root entity doesn't define a discriminator column or the type is abstract.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
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
        element.setAttribute(PROPERTY_ENABLED, "" //$NON-NLS-1$
                + Boolean.valueOf(enabled).toString());
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        tableName = element.getAttribute(PROPERTY_TABLE_NAME);
        inheritanceStrategy = InheritanceStrategy.valueOf(element.getAttribute(PROPERTY_INHERITANCE_STRATEGY));
        discriminatorColumnName = element.getAttribute(PROPERTY_DISCRIMINATOR_COLUMN_NAME);
        discriminatorDatatype = DiscriminatorDatatype.valueOf(element.getAttribute(PROPERTY_DISCRIMINATOR_DATATYPE));
        discriminatorValue = element.getAttribute(PROPERTY_DISCRIMINATOR_VALUE);
        enabled = Boolean.valueOf(element.getAttribute(PROPERTY_ENABLED));
        definesDiscriminatorColumn = Boolean.valueOf(element.getAttribute(PROPERTY_DEFINES_DISCRIMINATOR_COLUMN));
        useTableDefinedInSupertype = Boolean.valueOf(element.getAttribute(PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE));
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
                errorMessage = NLS.bind("The discriminator value \"{0}\" is already defined in the supertype {1}",
                        currentTypeInfo.getDiscriminatorValue(), currentType.getUnqualifiedName());
                errorProperty = IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE;
                return false;
            } else {
                discriminatorValues.add(currentTypeInfo.getDiscriminatorValue());
            }

            return true;
        }
    }

    /*
     * The root entity is the entity which can define the discriminator. A root entity has no
     * supertype and must be an JPA entity.
     */
    private class RooEntityFinder extends PolicyCmptTypeHierarchyVisitor {
        private IPolicyCmptType rooEntity = null;

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPersistentTypeInfo persistenceTypeInfo = currentType.getPersistenceTypeInfo();

            if (StringUtils.isEmpty(currentType.getSupertype())) {
                if (persistenceTypeInfo.isEnabled()) {
                    rooEntity = currentType;
                }
                return false;
            } else {
                if (persistenceTypeInfo.isEnabled()) {
                    rooEntity = currentType;
                }
            }
            return true;
        }
    }

}
