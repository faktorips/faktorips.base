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

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Default implementation of {@link IPersistentTypeInfo}.
 * 
 * @author Roman Grutza
 */
public class PersistentTypeInfo implements IPersistentTypeInfo {

    private String tableName;
    private String secondaryTableName;
    private InheritanceStrategy inheritanceStrategy;
    private String descriminatorValue;
    private DescriminatorDatatype descriminatorDatatype;
    private String descriminatorColumnName;

    public String getDescriminatorColumnName() {
        return descriminatorColumnName;
    }

    public DescriminatorDatatype getDescriminatorDatatype() {
        return descriminatorDatatype;
    }

    public String getDescriminatorValue() {
        return descriminatorValue;
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

    public void setDescriminatorColumnName(String newDescriminatorColumnName) {
        if (StringUtils.isEmpty(newDescriminatorColumnName)) {
            throw new RuntimeException("Descriminator column name must not be null or empty.");
        }
        descriminatorColumnName = newDescriminatorColumnName;
    }

    public void setDescriminatorDatatype(DescriminatorDatatype newDescriminatorDatatype) {
        descriminatorDatatype = newDescriminatorDatatype;
    }

    public void setDescriminatorValue(String newDescriminatorValue) {
        if (StringUtils.isEmpty(newDescriminatorValue)) {
            throw new RuntimeException("Descriminator value name must not be null or empty.");
        }
        descriminatorValue = newDescriminatorValue;
    }

    public void setInheritanceStrategy(InheritanceStrategy newStrategy) {
        inheritanceStrategy = newStrategy;
    }

    public void setSecondaryTableName(String newSecondaryTableName) {
        if (StringUtils.isEmpty(newSecondaryTableName)) {
            throw new RuntimeException("Secondary table name must not be null or empty.");
        }
        secondaryTableName = newSecondaryTableName;
    }

    public void setTableName(String newTableName) {
        if (StringUtils.isEmpty(newTableName)) {
            throw new RuntimeException("Table name must not be null or empty.");
        }
        tableName = newTableName;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(MessageList msgList, IIpsProject ipsProject) {
        if (!PersistenceUtil.isValidDatabaseIdentifier(tableName)) {
            String text = "The table name is invalid.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_TABLE_NAME_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_TABLE_NAME));
        }
        if (!PersistenceUtil.isValidDatabaseIdentifier(secondaryTableName)) {
            String text = "The secondary table name is invalid.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_SECONDARY_TABLE_NAME));
        }
        if (!PersistenceUtil.isValidDatabaseIdentifier(descriminatorColumnName)) {
            String text = "The descriminator column name is invalid.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_DESCRIMINATOR_COLUMN_NAME_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_COLUMN_NAME));
        }
        if (descriminatorDatatype == null || !descriminatorDatatype.isParsableToDatatype(descriminatorValue)) {
            String text = "The descriminator value does not conform to the specified descriminator datatype.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_DESCRIMINATOR_VALUE_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_DESCRIMINATOR_VALUE));
        }

        // Descriminator columns are used only for the strategy SINGLE_TABLE
        boolean invalidInheritanceStrategy = (inheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS)
                && descriminatorDatatype != DescriminatorDatatype.VOID;
        invalidInheritanceStrategy = (invalidInheritanceStrategy || inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE)
                && descriminatorDatatype == DescriminatorDatatype.VOID;
        if (invalidInheritanceStrategy) {
            String text = "Descriminator columns are used only for the inheritance strategy SINGLE_TABLE. Either use this strategy or set the descriminator datatype to VOID.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
        }

        // FIXME: add additional validation rules to check the type hierarchy for consistence, e.g:
        // - A subtype cannot have an InheritanceStrategy of JOINED_SUBCLASS if secondary tables are
        // used
    }
}
