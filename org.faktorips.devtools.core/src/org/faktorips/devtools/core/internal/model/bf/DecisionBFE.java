/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
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
