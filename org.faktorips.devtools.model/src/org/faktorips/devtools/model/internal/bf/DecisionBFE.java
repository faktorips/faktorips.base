/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.bf;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.bf.BFElementType;
import org.faktorips.devtools.model.bf.IDecisionBFE;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DecisionBFE extends MethodCallBFE implements IDecisionBFE {

    private String datatype = Datatype.BOOLEAN.getQualifiedName();

    public DecisionBFE(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String datatype) {
        String old = this.datatype;
        this.datatype = datatype;
        valueChanged(old, datatype);
    }

    @Override
    public String getDisplayString() {
        if (getType().equals(BFElementType.DECISION)) {
            return getName();
        }
        return super.getDisplayString();
    }

    @Override
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException {
        if (getType().equals(BFElementType.DECISION_METHODCALL)) {
            IMethod method = findMethod(ipsProject);
            if (method == null) {
                return null;
            }
            Datatype datatype = method.findDatatype(ipsProject);
            if (datatype.isValueDatatype()) {
                return (ValueDatatype)datatype;
            }
            return null;
        }
        return ipsProject.findValueDatatype(datatype);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IDecisionBFE.XML_TAG);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (getType().equals(BFElementType.DECISION_METHODCALL)) {
            validateMethodCall(list, ipsProject);
            return;
        }

        super.validateThis(list, ipsProject);

        // Data type specified
        if (StringUtils.isEmpty(datatype)) {
            list.add(new Message(MSGCODE_DATATYPE_NOT_SPECIFIED, Messages.DecisionBFE_datatypeNotSpecified,
                    Message.ERROR, this));
            return;
        }

        // Data type exists
        Datatype datatype = findDatatype(ipsProject);
        if (datatype == null) {
            list.add(new Message(MSGCODE_DATATYPE_DOES_NOT_EXIST, Messages.DecisionBFE_datatypeDoesNotExist,
                    Message.ERROR, this));
            return;
        }

        // Data type only none primitive value data type
        if (!datatype.isValueDatatype() || datatype.isPrimitive()) {
            list.add(new Message(MSGCODE_DATATYPE_ONLY_NONE_PRIM_VALUEDATATYPE,
                    Messages.DecisionBFE_DatatypeMustBeNotPrimitive, Message.ERROR, this));
        }
    }

}
