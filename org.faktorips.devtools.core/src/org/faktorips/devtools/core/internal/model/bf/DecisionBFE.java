/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DecisionBFE extends BFElement implements IDecisionBFE {

    private String datatype = ""; //$NON-NLS-1$

    public DecisionBFE(IIpsObject parent, int id) {
        super(parent, id);
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        String old = this.datatype;
        this.datatype = datatype;
        valueChanged(old, datatype);
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findValueDatatype(datatype);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
    }

    /**
     * {@inheritDoc}
     */
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
        super.validateThis(list, ipsProject);
        // datatype specified
        if (StringUtils.isEmpty(datatype)) {
            list.add(new Message(MSGCODE_DATATYPE_NOT_SPECIFIED, Messages.getString("DecisionBFE.datatypeNotSpecified"), Message.ERROR, //$NON-NLS-1$
                    this));
            return;
        }
        // datatype exists
        Datatype datatype = findDatatype(ipsProject);
        if (datatype == null) {
            list.add(new Message(MSGCODE_DATATYPE_DOES_NOT_EXIST, Messages.getString("DecisionBFE.datatypeDoesNotExist"), //$NON-NLS-1$
                    Message.ERROR, this));
            return;
        }
        // datatype only none primitive valuedatatype
        if (!datatype.isValueDatatype() || datatype.isPrimitive()) {
            list.add(new Message(MSGCODE_DATATYPE_ONLY_NONE_PRIM_VALUEDATATYPE,
                    Messages.getString("DecisionBFE.DatatypeMustBeNotPrimitive"), Message.ERROR, this)); //$NON-NLS-1$
        }
    }

}
