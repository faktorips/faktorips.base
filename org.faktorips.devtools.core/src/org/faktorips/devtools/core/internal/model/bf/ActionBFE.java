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

    private String executableMethodName = "";
    private String target = "";

    public ActionBFE(IIpsObject parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        this.target = element.getAttribute("target");
        this.executableMethodName = element.getAttribute("executableMethodName");
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
        element.setAttribute("executableMethodName", this.executableMethodName);
        element.setAttribute("target", this.target);
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

    // TODO test missing
    public String getReferencedBfQualifiedName() {
        if (getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
            return getTarget();
        }
        return null;
    }

    // TODO test missing
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
        validateMethodCallAction(list);
        validateBusinessFunctionCallAction(list);
    }
    
    private void validateBusinessFunctionCallAction(MessageList msgList) throws CoreException{
        if(getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)){
            // business function has to be specified
            if (StringUtils.isEmpty(target)) {
                msgList.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED, "The business function needs to be specified.",
                        Message.ERROR, this));
            }
            validateNotAllowedNames(target, "business function name", msgList);
            //business function exists
            IBusinessFunction refBf = findReferencedBusinessFunction();
            if(refBf == null){
                msgList.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST, "The specified business function does not exist.",
                        Message.ERROR, this));
            }
        }
    }
    
    private void validateMethodCallAction(MessageList list) throws CoreException {
        if (getType().equals(BFElementType.ACTION_METHODCALL)) {
            // parameter has to be specified
            if (StringUtils.isEmpty(target)) {
                list.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED, "The parameter needs to be specified.",
                        Message.ERROR, this));
            }
            // method has to be specified
            if (StringUtils.isEmpty(getExecutableMethodName())) {
                list.add(new Message(MSGCODE_METHOD_NOT_SPECIFIED, "The method needs to be specified.", Message.ERROR,
                        this));
            }
            validateNotAllowedNames(getExecutableMethodName(), "method name", list);
            // parameter has to exist
            if (getParameter() == null) {
                list.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST,
                        "The specified parameter does not exist within this business function.", Message.ERROR, this));
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
                        "The specified datatype is not a policy oder product component type", Message.ERROR, this));
                return;
            }
            // method has to exist
            IType type = (IType)datatype;
            if (type.getMethod(getExecutableMethodName(), new String[0]) == null) {
                list.add(new Message(MSGCODE_METHOD_DOES_NOT_EXIST,
                        "This method doesn't exist on the parameter. Only methods without parameters are considered.",
                        Message.ERROR, this));
            }
        }
    }

}
