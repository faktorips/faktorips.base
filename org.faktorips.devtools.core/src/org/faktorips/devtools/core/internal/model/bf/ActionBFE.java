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

package org.faktorips.devtools.core.internal.model.bf;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ActionBFE extends BFElement implements IActionBFE {

    private String executableMethodName = ""; //$NON-NLS-1$
    private String target = ""; //$NON-NLS-1$

    public ActionBFE(IIpsObject parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        this.target = element.getAttribute("target"); //$NON-NLS-1$
        this.executableMethodName = element.getAttribute("executableMethodName"); //$NON-NLS-1$
    }

    public String getDisplayString() {
        if (BFElementType.ACTION_METHODCALL.equals(getType())) {
            return target + ':' + executableMethodName;
        }
        if (BFElementType.ACTION_BUSINESSFUNCTIONCALL.equals(getType())) {
            return target;
        }
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("executableMethodName", this.executableMethodName); //$NON-NLS-1$
        element.setAttribute("target", this.target); //$NON-NLS-1$
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IActionBFE.XML_TAG);
    }

    public String getExecutableMethodName() {
        return executableMethodName;
    }

    public String getTarget() {
        return target;
    }

    public String getReferencedBfQualifiedName() {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            return getTarget();
        }
        return null;
    }

    public String getReferencedBfUnqualifedName() {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            int index = StringUtils.lastIndexOf(getTarget(), '.');
            if (index == -1) {
                return getTarget();
            }
            return getTarget().substring(index + 1, getTarget().length());
        }
        return null;
    }

    public IParameterBFE getParameter() {
        return getBusinessFunction().getParameterBFE(getTarget());
    }

    public IBusinessFunction findReferencedBusinessFunction() throws CoreException {
        return (IBusinessFunction)getIpsProject().findIpsObject(BusinessFunctionIpsObjectType.getInstance(),
                getTarget());
    }

    public void setExecutableMethodName(String name) {
        String old = this.executableMethodName;
        this.executableMethodName = name;
        valueChanged(old, name);
    }

    public void setTarget(String target) {
        String old = this.target;
        this.target = target;
        valueChanged(old, target);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateMethodCallAction(list, ipsProject);
        validateBusinessFunctionCallAction(list);
    }

    private void validateBusinessFunctionCallAction(MessageList msgList) throws CoreException {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            // business function has to be specified
            if (StringUtils.isEmpty(target)) {
                msgList.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED,
                        Messages.getString("ActionBFE.bfMustBeSpecified"), //$NON-NLS-1$
                        Message.ERROR, this));
            }
            validateNotAllowedNames(target, Messages.getString("ActionBFE.bfName"), msgList); //$NON-NLS-1$
            // business function exists
            IBusinessFunction refBf = findReferencedBusinessFunction();
            if (refBf == null) {
                msgList.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST, Messages.getString("ActionBFE.bfDoesNotExist"), //$NON-NLS-1$
                        Message.ERROR, this));
            }
        }
    }

    private void validateMethodCallAction(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (getType().equals(BFElementType.ACTION_METHODCALL)) {
            // parameter has to be specified
            if (StringUtils.isEmpty(target)) {
                list.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED, Messages
                        .getString("ActionBFE.parameterNotSpecified"), //$NON-NLS-1$
                        Message.ERROR, this));
            }
            // method has to be specified
            if (StringUtils.isEmpty(getExecutableMethodName())) {
                list.add(new Message(MSGCODE_METHOD_NOT_SPECIFIED, Messages
                        .getString("ActionBFE.methodMustBeSpecified"), Message.ERROR, //$NON-NLS-1$
                        this));
            }
            validateNotAllowedNames(getExecutableMethodName(), Messages.getString("ActionBFE.methodName"), list); //$NON-NLS-1$
            // parameter has to exist
            if (getParameter() == null) {
                list.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST,
                        Messages.getString("ActionBFE.parameterMissing"), Message.ERROR, this)); //$NON-NLS-1$
                return;
            }
            Datatype datatype = getParameter().findDatatype();
            if (datatype == null) {
                // this case has to be handled in the parameter validation
                return;
            }
            // only parameters with IType datatypes are allowed
            if (!(datatype instanceof IType)) {
                list.add(new Message(MSGCODE_TARGET_NOT_VALID_TYPE,
                        Messages.getString("ActionBFE.parameterNoType"), Message.ERROR, this)); //$NON-NLS-1$
                return;
            }
            // method has to exist
            IType type = (IType)datatype;
            if (type.findMethod(getExecutableMethodName(), new String[0], ipsProject) == null) {
                String text = NLS
                        .bind(
                                Messages.getString("ActionBFE.methodDoesNotExistOnParameter"), new String[] { getExecutableMethodName(), getTarget() }); //$NON-NLS-1$
                list.add(new Message(MSGCODE_METHOD_DOES_NOT_EXIST, text, Message.ERROR, this));
            }
        }
    }

}
