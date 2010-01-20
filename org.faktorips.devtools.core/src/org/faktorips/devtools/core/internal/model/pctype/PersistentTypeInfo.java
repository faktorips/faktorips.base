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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.util.PersistenceUtil;
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
    private String descriminatorValue;
    private DiscriminatorDatatype descriminatorDatatype = DiscriminatorDatatype.STRING;
    private String descriminatorColumnName = "DTYPE";

    private boolean notJoinedSubclass = true;

    public boolean isNotJoinedSubclass() {
        return notJoinedSubclass;
    }

    public void setNotJoinedSubclass(boolean notJoinedSubclass) {
        this.notJoinedSubclass = notJoinedSubclass;
    }

    /**
     * @param policyComponentType
     */
    public PersistentTypeInfo(IIpsObject ipsObject, int id) {
        super(ipsObject, id);

        ITableNamingStrategy tableNamingStrategy = getIpsProject().getTableNamingStrategy();

        tableName = tableNamingStrategy.getTableName(ipsObject.getName());
        descriminatorValue = ipsObject.getName();
    }

    public String getDiscriminatorColumnName() {
        return descriminatorColumnName;
    }

    public DiscriminatorDatatype getDiscriminatorDatatype() {
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
        String oldValue = descriminatorColumnName;
        descriminatorColumnName = newDescriminatorColumnName;

        valueChanged(oldValue, newDescriminatorColumnName);
    }

    public void setDescriminatorDatatype(DiscriminatorDatatype newDescriminatorDatatype) {
        DiscriminatorDatatype oldValue = descriminatorDatatype;
        descriminatorDatatype = newDescriminatorDatatype;

        valueChanged(oldValue, newDescriminatorDatatype);
    }

    public void setDescriminatorValue(String newDescriminatorValue) {
        String oldValue = descriminatorValue;
        descriminatorValue = newDescriminatorValue;

        valueChanged(oldValue, newDescriminatorValue);
    }

    public void setInheritanceStrategy(InheritanceStrategy newStrategy) {
        InheritanceStrategy oldValue = inheritanceStrategy;
        inheritanceStrategy = newStrategy;
        notJoinedSubclass = (newStrategy != InheritanceStrategy.JOINED_SUBCLASS);

        valueChanged(oldValue, newStrategy);
    }

    public void setSecondaryTableName(String newSecondaryTableName) {
        String oldValue = secondaryTableName;
        secondaryTableName = newSecondaryTableName;

        valueChanged(oldValue, newSecondaryTableName);
    }

    public void setTableName(String newTableName) {
        String oldValue = tableName;
        tableName = newTableName;

        valueChanged(oldValue, newTableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateThis(MessageList msgList, IIpsProject ipsProject) {
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
                && descriminatorDatatype != DiscriminatorDatatype.VOID;
        invalidInheritanceStrategy = (invalidInheritanceStrategy || inheritanceStrategy == InheritanceStrategy.SINGLE_TABLE)
                && descriminatorDatatype == DiscriminatorDatatype.VOID;
        if (invalidInheritanceStrategy) {
            String text = "Descriminator columns are used only for the inheritance strategy SINGLE_TABLE. Either use this strategy or set the descriminator datatype to VOID.";
            msgList.add(new Message(MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID, text, Message.ERROR, this,
                    IPersistentTypeInfo.PROPERTY_INHERITANCE_STRATEGY));
        }

        // FIXME: add additional validation rules to check the type hierarchy for consistence, e.g:
        // - A subtype cannot have an InheritanceStrategy of JOINED_SUBCLASS if secondary tables are
        // used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TABLE_NAME, "" + tableName);//$NON-NLS-1$
        element.setAttribute(PROPERTY_SECONDARY_TABLE_NAME, "" + secondaryTableName);
        element.setAttribute(PROPERTY_INHERITANCE_STRATEGY, "" + inheritanceStrategy);
        element.setAttribute(PROPERTY_DESCRIMINATOR_COLUMN_NAME, "" + descriminatorColumnName);
        element.setAttribute(PROPERTY_DESCRIMINATOR_DATATYPE, "" + descriminatorDatatype);
        element.setAttribute(PROPERTY_DESCRIMINATOR_VALUE, "" + descriminatorValue);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        tableName = element.getAttribute(PROPERTY_TABLE_NAME);
        secondaryTableName = element.getAttribute(PROPERTY_SECONDARY_TABLE_NAME);
        inheritanceStrategy = InheritanceStrategy.valueOf(element.getAttribute(PROPERTY_INHERITANCE_STRATEGY));
        descriminatorColumnName = element.getAttribute(PROPERTY_DESCRIMINATOR_COLUMN_NAME);
        descriminatorDatatype = DiscriminatorDatatype.valueOf(element.getAttribute(PROPERTY_DESCRIMINATOR_DATATYPE));
        descriminatorValue = element.getAttribute(PROPERTY_DESCRIMINATOR_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return getParent().getImage();
    }
}
