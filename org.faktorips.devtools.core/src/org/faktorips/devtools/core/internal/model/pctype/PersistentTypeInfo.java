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

    private String tableName = "TABLE_NAME";
    private String secondaryTableName = "";
    private InheritanceStrategy inheritanceStrategy = InheritanceStrategy.SINGLE_TABLE;
    private String discriminatorValue = "DISCRIMINATOR_VALUE";
    private DiscriminatorDatatype discriminatorDatatype = DiscriminatorDatatype.STRING;
    private String discriminatorColumnName = "DTYPE";

    private boolean notJoinedSubclass = true;

    // per default the persistent is disabled
    private boolean enabled = false;

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

    public void setEnabled(boolean enabled) {
        boolean oldValue = this.enabled;
        this.enabled = enabled;
        valueChanged(oldValue, enabled);
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

    public boolean isDiscriminatorRequired() {
        return inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE;
    }

    public boolean isSecondaryTableNameRequired() {
        return inheritanceStrategy == InheritanceStrategy.MIXED;
    }

    @Override
    public void validateThis(MessageList msgList, IIpsProject ipsProject) {
        if (!enabled) {
            return;
        }
        try {
            validateInheritanceStrategy(msgList);
            validateTableNames(msgList);
            validateDisriminator(msgList);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void validateInheritanceStrategy(final MessageList msgList) throws CoreException {
        IPolicyCmptType pcType = (IPolicyCmptType)getIpsObject();
        if (!pcType.hasSupertype()) {
            if (inheritanceStrategy == InheritanceStrategy.MIXED) {
                // MIXED strategy only makes sense with at least 2 hierarchy (inheritance) levels
                String text = "Use of MIXED inheritance strategy is discouraged for types denoting a hierarchy root.";
                msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text, Message.WARNING, this,
                        IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
            }
            return;
        }
        new InheritanceStrategyMismatchVisitor(this, msgList).start(pcType);
    }

    private void validateTableNames(MessageList msgList) throws CoreException {
        IPolicyCmptType pcType = (IPolicyCmptType)getIpsObject();

        TableNameValidator tableNameValidator = new TableNameValidator(this, tableName);
        tableNameValidator.start(pcType);
        if (tableNameValidator.conflictingTypeInfo != null) {
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID, tableNameValidator.errorMessage,
                    Message.ERROR, this, IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }

        if (isSecondaryTableNameRequired() && !PersistenceUtil.isValidDatabaseIdentifier(secondaryTableName)) {
            String text = "The secondary table name is invalid.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME));
        }

        if (isSecondaryTableNameRequired() && tableName.equals(secondaryTableName)) {
            String text = "Primary and secondary table names must not be equal.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME));
        }
    }

    private void validateDisriminator(MessageList msgList) throws CoreException {
        if (!isDiscriminatorRequired()) {
            return;
        }

        if (!discriminatorDatatype.isParsableToDiscriminatorDatatype(discriminatorValue)) {
            String text = "The discriminator value does not conform to the specified descriminator datatype.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_VALUE));
            return;
        }

        IPolicyCmptType pcType = (IPolicyCmptType)getIpsObject();
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
        element.setAttribute(PROPERTY_ENABLED, Boolean.valueOf(enabled).toString());
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
            if (supertypeStrategy == InheritanceStrategy.SINGLE_TABLE
                    && inheritanceStrategy == InheritanceStrategy.MIXED) {
                return true;
            }

            String text = "Invalid combination of inheritance strategies. Resolve by changing strategy for either "
                    + currentType.getUnqualifiedName() + "(" + supertypeStrategy + ") or "
                    + persistentTypeInfo.getIpsObject().getUnqualifiedName() + "(" + inheritanceStrategy + ")";
            msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text, Message.ERROR,
                    persistentTypeInfo, IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
            return false;
        }
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
            if (inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE
                    || inheritanceStrategy == InheritanceStrategy.MIXED) {
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
        private final String discriminatorColumnName;
        private final DiscriminatorDatatype discriminatorDatatype;
        private final List<String> discriminatorValues = new ArrayList<String>();

        // If these fields are not null errors exist in the naming of the tables
        private IPersistentTypeInfo conflictingTypeInfo;
        private String errorMessage;
        public String errorProperty;

        public DiscriminatorValidator(IPersistentTypeInfo typeInfo) {
            inheritanceStrategy = typeInfo.getInheritanceStrategy();
            discriminatorDatatype = typeInfo.getDiscriminatorDatatype();
            discriminatorColumnName = typeInfo.getDiscriminatorColumnName();

            if (!PersistenceUtil.isValidDatabaseIdentifier(discriminatorColumnName)) {
                conflictingTypeInfo = typeInfo;
            }
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            if (conflictingTypeInfo != null) {
                errorMessage = "The discriminator column name is invalid.";
                errorProperty = IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME;
                return false;
            }

            IPersistentTypeInfo currentTypeInfo = currentType.getPersistenceTypeInfo();

            if (inheritanceStrategy != InheritanceStrategy.SINGLE_TABLE) {
                return false;
            }

            // Invariants:
            // - discriminator columns and datatypes must be equal in whole hierarchy
            // - discriminator values must be unique
            if (!discriminatorColumnName.equals(currentTypeInfo.getDiscriminatorColumnName())) {
                conflictingTypeInfo = currentTypeInfo;
                errorMessage = "The discriminator column name does not match the discriminator column name in the supertype "
                        + currentType.getUnqualifiedName() + "(" + currentTypeInfo.getDiscriminatorColumnName() + ")";
                errorProperty = IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_COLUMN_NAME;
                return false;
            }

            if (discriminatorDatatype != currentTypeInfo.getDiscriminatorDatatype()) {
                conflictingTypeInfo = currentTypeInfo;
                errorMessage = "The discriminator datatype does not match the datataype in the supertype "
                        + currentType.getUnqualifiedName() + "(" + currentTypeInfo.getDiscriminatorDatatype() + ")";
                errorProperty = IPersistentTypeInfo.PROPERTY_DISCRIMINATOR_DATATYPE;
                return false;
            }

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
}
