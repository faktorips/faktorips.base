/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.method;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.method.IParameterContainer;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class Parameter extends AtomicIpsObjectPart implements IParameter {

    static final String TAG_NAME = "Parameter"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    public Parameter(IParameterContainer container, String id) {
        super(container, id);
    }

    @Override
    public IParameterContainer getParameterContainer() {
        return (IParameterContainer)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, name);
    }

    @Override
    public void setDatatype(String type) {
        String oldType = datatype;
        datatype = type;
        valueChanged(oldType, datatype);
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public Datatype findDatatype(IIpsProject ipsProject) {
        return ipsProject.findDatatype(datatype);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) {
        super.validateThis(result, ipsProject);
        if (IpsStringUtils.isEmpty(name)) {
            result.add(new Message("", Messages.Parameter_msg_NameEmpty, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
        } else {
            Message msg = ipsProject.getNamingConventions().validateIfValidJavaIdentifier(getName(),
                    Messages.Parameter_msg_InvalidParameterName, this, ipsProject);
            if (msg == null) {
                if (!ExprCompiler.isValidIdentifier(getName())) {
                    msg = new Message(
                            "", Messages.Parameter_msg_InvalidParameterName, Message.ERROR, this, PROPERTY_NAME); //$NON-NLS-1$
                }
            }
            result.add(msg);
        }
        ValidationUtils.checkDatatypeReference(datatype, false, this, PROPERTY_DATATYPE, "", result, ipsProject); //$NON-NLS-1$
    }

}
