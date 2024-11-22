/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.model.internal.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Thorsten Guenther
 */
public class TableContentUsage extends AbstractSimplePropertyValue implements ITableContentUsage {

    public static final String TAG_NAME = ValueToXmlHelper.XML_TAG_TABLE_CONTENT_USAGE;
    /**
     * The full qualified name of the table content this usage defines.
     */
    private String tableContentName = ""; //$NON-NLS-1$

    /**
     * The role-name of the structure usage this content usage is based on.
     */
    private String structureUsage = ""; //$NON-NLS-1$

    private final TemplateValueSettings templateValueSettings;

    public TableContentUsage(IPropertyValueContainer parent, String id) {
        this(parent, id, ""); //$NON-NLS-1$
    }

    public TableContentUsage(IPropertyValueContainer parent, String id, String structureUsage) {
        super(parent, id);
        this.structureUsage = structureUsage;
        templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public String getPropertyName() {
        return structureUsage;
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) {
        return findTableStructureUsage(ipsProject);
    }

    @Override
    public PropertyValueType getPropertyValueType() {
        return PropertyValueType.TABLE_CONTENT_USAGE;
    }

    @Override
    public String getPropertyValue() {
        return getTableContentName();
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return getPropertyValueContainer().getProductCmpt();
    }

    private IProductCmptType getProductCmptType(IIpsProject ipsProject) {
        return getPropertyValueContainer().findProductCmptType(ipsProject);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public void setStructureUsage(String structureUsage) {
        this.structureUsage = structureUsage;
        objectHasChanged();
    }

    @Override
    public String getStructureUsage() {
        return structureUsage;
    }

    @Override
    public void setTableContentName(String tableContentName) {
        this.tableContentName = tableContentName;
        objectHasChanged();
    }

    @Override
    public String getName() {
        return getTableContentName();
    }

    @Override
    public String getTableContentName() {
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateTableContentName();
        }

        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return ""; //$NON-NLS-1$
        }

        return tableContentName;
    }

    public String getInternalTableContentName() {
        return tableContentName;
    }

    private String findTemplateTableContentName() {
        ITableContentUsage templateContentUsage = findTemplateProperty(getIpsProject());
        if (templateContentUsage == null) {
            // Template should exist but does not. Use the "last known" value as a more or less
            // helpful fallback while some validation hopefully addresses the missing template...
            return tableContentName;
        }
        return templateContentUsage.getTableContentName();
    }

    @Override
    public ITableContents findTableContents(IIpsProject ipsProject) {
        return (ITableContents)ipsProject.findIpsObject(IpsObjectType.TABLE_CONTENTS, getTableContentName());
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        String tableContentNameToValidate = getTableContentName();
        IProductCmptType type = getProductCmptType(ipsProject);
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TYPE, Messages.TableContentUsage_msgNoType, Message.WARNING, this));
            return;
        }

        ITableStructureUsage tsu = type.findTableStructureUsage(structureUsage, ipsProject);
        if (tsu == null) {
            String text = MessageFormat.format(Messages.TableContentUsage_msgUnknownStructureUsage, structureUsage);
            list.add(new Message(MSGCODE_UNKNOWN_STRUCTURE_USAGE, text, Message.ERROR, this, PROPERTY_STRUCTURE_USAGE));
            return;
        }
        list.add(templateValueSettings.validate(this, ipsProject));
        ITableContents content = null;
        if (tableContentNameToValidate != null) {
            content = findTableContents(ipsProject);
        }
        if (content == null) {
            if (!isNullContentAllowed(tsu)) {
                String text = MessageFormat.format(Messages.TableContentUsage_msgUnknownTableContent,
                        tableContentNameToValidate);
                list.add(new Message(MSGCODE_UNKNOWN_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
            }
            return;
        }
        String usedStructure = content.getTableStructure();
        if (!tsu.isUsed(usedStructure)) {
            String text = MessageFormat.format(Messages.TableContentUsage_msgInvalidTableContent,
                    tableContentNameToValidate, usedStructure, structureUsage);
            list.add(new Message(MSGCODE_INVALID_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
        }
    }

    private boolean isNullContentAllowed(ITableStructureUsage tsu) {
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return true;
        } else {
            return IpsStringUtils.isEmpty(getTableContentName()) && !tsu.isMandatoryTableContent();
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        structureUsage = element.getAttribute(PROPERTY_STRUCTURE_USAGE);
        tableContentName = ValueToXmlHelper.getValueFromElement(element, "TableContentName"); //$NON-NLS-1$
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_STRUCTURE_USAGE, structureUsage);
        ValueToXmlHelper.addValueToElement(getTableContentName(), element, "TableContentName"); //$NON-NLS-1$
        templateValueSettings.propertiesToXml(element);
    }

    @Override
    public ITableStructureUsage findTableStructureUsage(IIpsProject ipsProject) {
        IProductCmptType type = getProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findTableStructureUsage(structureUsage, ipsProject);
    }

    @Override
    public String getCaption(Locale locale) {
        ArgumentCheck.notNull(locale);

        ITableStructureUsage currentStructureUsage = findTableStructureUsage(getIpsProject());
        if (currentStructureUsage != null) {
            return currentStructureUsage.getLabelValue(locale);
        } else {
            return null;
        }
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(structureUsage);
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        if (newStatus == TemplateValueStatus.DEFINED) {
            // Copy table name values from template (if present)
            tableContentName = getTableContentName();
        }
        TemplateValueStatus oldStatus = templateValueSettings.getStatus();
        templateValueSettings.setStatus(newStatus);
        objectHasChanged(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldStatus, newStatus));

    }

    @Override
    public TemplateValueStatus getTemplateValueStatus() {
        return templateValueSettings.getStatus();
    }

    @Override
    public ITableContentUsage findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, ITableContentUsage.class);
    }

    @Override
    public boolean hasTemplateForProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.hasTemplateForValue(this, ITableContentUsage.class);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Objects.hash(structureUsage, tableContentName, templateValueSettings.getStatus());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || (getClass() != obj.getClass())) {
            return false;
        }
        TableContentUsage other = (TableContentUsage)obj;
        return Objects.equals(structureUsage, other.structureUsage)
                && Objects.equals(tableContentName, other.tableContentName)
                && Objects.equals(templateValueSettings.getStatus(), other.templateValueSettings.getStatus());
    }

}
