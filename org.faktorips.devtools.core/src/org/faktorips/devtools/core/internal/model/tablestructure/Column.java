/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Column extends AtomicIpsObjectPart implements IColumn {

    static final String TAG_NAME = "Column"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    Column(TableStructure table, String id) {
        super(table, id);
    }

    @Override
    public void setName(String newName) {
        name = newName;
        objectHasChanged();
    }

    @Override
    public boolean isRange() {
        return false;
    }

    @Override
    public String getAccessParameterName() {
        return name;
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String newDatatype) {
        datatype = newDatatype;
        objectHasChanged();
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ValidationUtils.checkStringPropertyNotEmpty(name, "name", this, PROPERTY_NAME, "", list); //$NON-NLS-1$ //$NON-NLS-2$
        Datatype type = ValidationUtils.checkValueDatatypeReference(datatype, false, this, PROPERTY_DATATYPE, "", list); //$NON-NLS-1$
        if (type == null) {
            return;
        }
        if (type.isPrimitive()) {
            String text = Messages.Column_msgPrimitvesArentSupported;
            list.add(new Message(MSGCODE_DATATYPE_IS_A_PRIMITTVE, text, Message.ERROR, this, PROPERTY_DATATYPE));
        }

        IStatus status = ValidationUtils.validateJavaIdentifier(StringUtils.uncapitalize(name), ipsProject);
        if (!status.isOK()) {
            String text = Messages.Column_msgInvalidName;
            list.add(new Message(MSGCODE_INVALID_NAME, text, Message.ERROR, this, PROPERTY_NAME));
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute("name"); //$NON-NLS-1$
        datatype = element.getAttribute("datatype"); //$NON-NLS-1$
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("name", name); //$NON-NLS-1$
        element.setAttribute("datatype", datatype); //$NON-NLS-1$
    }

    @Override
    public IColumn[] getColumns() {
        return new IColumn[] { this };
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findValueDatatype(datatype);
    }

}
