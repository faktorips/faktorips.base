/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.type.TypePart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.util.ListElementMover;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of ITableStructureUsage.
 *
 * @author Joerg Ortmann
 */
public class TableStructureUsage extends TypePart implements ITableStructureUsage {

    private boolean mandatoryTableContent = false;

    /** Contains the related table structures identified by the full qualified name */
    private List<TableStructureReference> tableStructures = new ArrayList<>();

    /** Flag indicating if this {@link TableStructureUsage} is static */
    private boolean changingOverTime = getProductCmptType().isChangingOverTime();

    public TableStructureUsage(IProductCmptType pcType, String id) {
        super(pcType, id);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return tableStructures.toArray(new IIpsElement[tableStructures.size()]);
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType.equals(TableStructureReference.class)) {
            return newTableStructureReference();
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(TAG_NAME_TABLE_STRUCTURE)) {
            return newTableStructureReferenceInternal(id);
        }
        return null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof TableStructureReference) {
            tableStructures.add((TableStructureReference)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof TableStructureReference) {
            tableStructures.remove(part);
            return true;
        }
        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        tableStructures.clear();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_ROLENAME);
        mandatoryTableContent = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_MANDATORY_TABLE_CONTENT);
        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            changingOverTime = Boolean.parseBoolean(element.getAttribute(PROPERTY_CHANGING_OVER_TIME));
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ROLENAME, name);
        if (mandatoryTableContent) {
            element.setAttribute(PROPERTY_MANDATORY_TABLE_CONTENT, "" + mandatoryTableContent); //$NON-NLS-1$
        }
        element.setAttribute(PROPERTY_CHANGING_OVER_TIME, "" + changingOverTime); //$NON-NLS-1$
    }

    @Override
    public String getRoleName() {
        return name;
    }

    @Override
    public void setRoleName(String newRoleName) {
        String oldRoleName = name;
        name = newRoleName;
        valueChanged(oldRoleName, newRoleName);
    }

    @Override
    public boolean isMandatoryTableContent() {
        return mandatoryTableContent;
    }

    @Override
    public void setMandatoryTableContent(boolean mandatoryTableContent) {
        boolean oldIsMandatory = this.mandatoryTableContent;
        this.mandatoryTableContent = mandatoryTableContent;
        valueChanged(oldIsMandatory, mandatoryTableContent);
    }

    @Override
    public String[] getTableStructures() {
        String[] result = new String[tableStructures.size()];
        for (int i = 0; i < result.length; i++) {
            TableStructureReference tsr = tableStructures.get(i);
            result[i] = tsr.getTableStructure();
        }
        return result;
    }

    @Override
    public void addTableStructure(String tableStructure) {
        if (getTableStructureReference(tableStructure) != null) {
            // the table structure is already assign, do nothing
            return;
        }
        TableStructureReference tsr = newTableStructureReferenceInternal(getNextPartId());
        tsr.setTableStructure(tableStructure);
        objectHasChanged();
    }

    @Override
    public boolean isUsed(String tableStructure) {
        if (tableStructure == null) {
            return false;
        }
        for (TableStructureReference tsr : tableStructures) {
            if (tableStructure.equals(tsr.tableStructure)) {
                return true;
            }
        }
        return false;
    }

    private TableStructureReference newTableStructureReference() {
        TableStructureReference tsr = newTableStructureReferenceInternal(getNextPartId());
        objectHasChanged();
        return tsr;
    }

    /**
     * Creates a new table structure usage without updating the src file.
     */
    private TableStructureReference newTableStructureReferenceInternal(String id) {
        TableStructureReference tsr = new TableStructureReference(this, id);
        tableStructures.add(tsr);
        return tsr;
    }

    @Override
    public void removeTableStructure(String tableStructure) {
        TableStructureReference toBeDeleted = getTableStructureReference(tableStructure);
        if (toBeDeleted != null) {
            tableStructures.remove(toBeDeleted);
            objectHasChanged();
        }
    }

    /**
     * Returns the table structure assignment object by the given name, if there is not table
     * structure assignet return <code>null</code>
     */
    protected TableStructureReference getTableStructureReference(String tableStructure) {
        for (TableStructureReference tsr : tableStructures) {
            if (tsr.getTableStructure() != null && tsr.getTableStructure().equals(tableStructure)) {
                return tsr;
            }
        }
        return null;
    }

    public void removeTableStructure(TableStructureReference tableStructureAssignment) {
        if (tableStructures.remove(tableStructureAssignment)) {
            objectHasChanged();
        }
    }

    @Override
    public int[] moveTableStructure(int[] indexes, boolean up) {
        ListElementMover<TableStructureReference> mover = new ListElementMover<>(
                tableStructures);
        return mover.move(indexes, up);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        // check the correct name format
        if (!ValidationUtils.validateFieldName(name, ipsProject)) {
            String text = MessageFormat.format(Messages.TableStructureUsage_msgInvalidRoleName, name);
            Message msg = new Message(MSGCODE_INVALID_ROLE_NAME, text, Message.ERROR, this, PROPERTY_ROLENAME);
            list.add(msg);
        }

        // check that at least one table structure is referenced
        if (tableStructures.size() == 0) {
            String text = Messages.TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;
            Message msg = new Message(MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE, text, Message.ERROR, this,
                    PROPERTY_TABLESTRUCTURE);
            list.add(msg);
        }

        validateRoleNameInSupertypeHierarchy(list);
        validateChangingOverTime(list);
    }

    private void validateRoleNameInSupertypeHierarchy(MessageList msgList) {
        IProductCmptType supertype = getProductCmptType().findSuperProductCmptType(getIpsProject());
        if (supertype == null) {
            return;
        }
        ITableStructureUsage tsu = supertype.findTableStructureUsage(name, getIpsProject());
        if (tsu != null) {
            String msg = MessageFormat.format(Messages.TableStructureUsage_msgRoleNameAlreadyInSupertype,
                    getRoleName());
            msgList.add(new Message(MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE, msg, Message.ERROR));
        }
    }

    private void validateChangingOverTime(MessageList list) {
        ChangingOverTimePropertyValidator propertyValidator = new ChangingOverTimePropertyValidator(this);
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(list);
    }

    @Override
    public String getPropertyName() {
        return name;
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return ProductCmptPropertyType.TABLE_STRUCTURE_USAGE;
    }

    @Override
    public List<PropertyValueType> getPropertyValueTypes() {
        return Arrays.asList(PropertyValueType.TABLE_CONTENT_USAGE);
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    @Override
    public void setChangingOverTime(boolean changingOverTime) {
        boolean oldValue = this.changingOverTime;
        this.changingOverTime = changingOverTime;
        valueChanged(oldValue, changingOverTime, ITableStructureUsage.PROPERTY_CHANGING_OVER_TIME);
    }

    @Override
    public String getPropertyDatatype() {
        return IpsStringUtils.EMPTY;
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return getProductCmptType();
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return false;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().isMatchingPropertyValue(getPropertyName(), propertyValue);
    }

    public static class TableStructureReference extends AtomicIpsObjectPart {

        private String tableStructure = IpsStringUtils.EMPTY;

        public TableStructureReference(ITableStructureUsage tableStructureUsage, String id) {
            super(tableStructureUsage, id);
        }

        @Override
        protected Element createElement(Document doc) {
            return doc.createElement(TAG_NAME_TABLE_STRUCTURE);
        }

        @Override
        protected void initPropertiesFromXml(Element element, String id) {
            super.initPropertiesFromXml(element, id);
            tableStructure = element.getAttribute(PROPERTY_TABLESTRUCTURE);
        }

        @Override
        protected void propertiesToXml(Element element) {
            super.propertiesToXml(element);
            element.setAttribute(PROPERTY_TABLESTRUCTURE, tableStructure);
        }

        /**
         * Returns the table structure.
         */
        public String getTableStructure() {
            return tableStructure;
        }

        /**
         * Sets the table structure.
         */
        public void setTableStructure(String tableStructure) {
            String oldStructure = this.tableStructure;
            this.tableStructure = tableStructure;
            valueChanged(oldStructure, tableStructure);
        }

        @Override
        protected void validateThis(MessageList list, IIpsProject ipsProject) {
            super.validateThis(list, ipsProject);
            if (getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, getTableStructure()) == null) {
                String text = MessageFormat.format(Messages.TableStructureUsage_msgTableStructureNotExists,
                        getTableStructure());
                Message msg = new Message(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND, text, Message.ERROR,
                        this);
                list.add(msg);
            }
        }

    }

}
