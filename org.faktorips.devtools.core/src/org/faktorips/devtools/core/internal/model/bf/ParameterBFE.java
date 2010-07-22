/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.bf;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParameterBFE extends BFElement implements IParameterBFE {

    private String datatype = ""; //$NON-NLS-1$

    public ParameterBFE(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public String getDisplayString() {
        return getDatatype() + ":" + getName(); //$NON-NLS-1$
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype, this);
        String old = this.datatype;
        this.datatype = datatype;
        valueChanged(old, datatype);
    }

    @Override
    public Datatype findDatatype() throws CoreException {
        return getIpsProject().findDatatype(datatype);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        setType(BFElementType.getType(element.getAttribute(PROPERTY_TYPE)));
        datatype = element.getAttribute(PROPERTY_DATATYPE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TYPE, getType().getId());
        element.setAttribute(PROPERTY_DATATYPE, datatype);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IParameterBFE.XML_TAG);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        validateName(list, ipsProject);
        validateDuplicateName(list);
        if (StringUtils.isEmpty(getDatatype())) {
            String text = Messages.ParameterBFE_datatypeNotSpecified;
            list.add(new Message(MSGCODE_DATATYPE_NOT_SPECIFIED, text, Message.ERROR, this));
            return;
        }
        Datatype datatype = findDatatype();
        if (datatype == null) {
            String text = NLS.bind(Messages.ParameterBFE_parameterOrDatatypeMissing, new String[] { getDatatype(),
                    getName() });
            list.add(new Message(MSGCODE_DATATYPE_DOES_NOT_EXISIT, text, Message.ERROR, this));
        }
    }

    private void validateDuplicateName(MessageList msgList) {
        List<IParameterBFE> params = getBusinessFunction().getParameterBFEs();
        for (IParameterBFE parameter : params) {
            if (parameter == this) {
                continue;
            }
            if (parameter.getName().equals(getName())) {
                msgList.add(new Message(MSGCODE_NAME_DUBLICATE, Messages.ParameterBFE_duplicateParameter,
                        Message.ERROR, parameter));
            }
        }
    }

}
