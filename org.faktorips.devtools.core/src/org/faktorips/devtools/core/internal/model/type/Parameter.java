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

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IParameterContainer;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class Parameter extends AtomicIpsObjectPart implements IParameter {

    final static String TAG_NAME = "Parameter"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    public Parameter(IParameterContainer container, int id) {
        super(container, id);
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

    public void setDatatype(String type) {
        String oldType = datatype;
        datatype = type;
        valueChanged(oldType, datatype);
    }

    public String getDatatype() {
        return datatype;
    }

    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findDatatype(datatype);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
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
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (StringUtils.isEmpty(name)) {
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
