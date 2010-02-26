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
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.IMethodCallBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public abstract class MethodCallBFE extends BFElement implements IMethodCallBFE {

    private String executableMethodName = ""; //$NON-NLS-1$
    private String target = ""; //$NON-NLS-1$

    public MethodCallBFE(IIpsObject parent, String id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        target = element.getAttribute("target"); //$NON-NLS-1$
        executableMethodName = element.getAttribute("executableMethodName"); //$NON-NLS-1$
    }

    @Override
    public String getDisplayString() {
        return target + ':' + executableMethodName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("executableMethodName", executableMethodName); //$NON-NLS-1$
        element.setAttribute("target", target); //$NON-NLS-1$
    }

    public String getExecutableMethodName() {
        return executableMethodName;
    }

    public String getTarget() {
        return target;
    }

    public IParameterBFE getParameter() {
        return getBusinessFunction().getParameterBFE(getTarget());
    }

    public IMethod findMethod(IIpsProject ipsProject) throws CoreException {
        IParameterBFE param = getParameter();
        if (param != null) {
            Datatype datatype = param.findDatatype();
            if (datatype instanceof IType) {
                IType type = (IType)datatype;
                return type.findMethod(getExecutableMethodName(), new String[0], ipsProject);
            }
        }
        return null;
    }

    public void setExecutableMethodName(String name) {
        String old = executableMethodName;
        executableMethodName = name;
        valueChanged(old, name);
    }

    public void setTarget(String target) {
        String old = this.target;
        this.target = target;
        valueChanged(old, target);
    }

    protected void validateMethodCall(MessageList list, IIpsProject ipsProject) throws CoreException {
        // parameter has to be specified
        if (StringUtils.isEmpty(target)) {
            list.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED, Messages.MethodCallBFE_parameterNotSpecified, Message.ERROR,
                    this));
            return;
        }
        // parameter does not exist
        if (getParameter() == null) {
            list.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST,
                    Messages.MethodCallBFE_parameterMissing, Message.ERROR, this));
            return;
        }
        // method has to be specified
        if (StringUtils.isEmpty(getExecutableMethodName())) {
            list
                    .add(new Message(MSGCODE_METHOD_NOT_SPECIFIED, Messages.MethodCallBFE_methodMustBeSpecified, Message.ERROR,
                            this));
            return;
        }
        validateNotAllowedNames(getExecutableMethodName(), Messages.MethodCallBFE_methodName, list);
        Datatype datatype = getParameter().findDatatype();
        if (datatype == null) {
            // this case has to be handled in the parameter validation
            return;
        }
        // only parameters with IType datatypes are allowed
        if (!(datatype instanceof IType)) {
            list.add(new Message(MSGCODE_TARGET_NOT_VALID_TYPE,
                    Messages.MethodCallBFE_parameterNoType, Message.ERROR, this));
            return;
        }
        // method has to exist
        IType type = (IType)datatype;
        if (type.findMethod(getExecutableMethodName(), new String[0], ipsProject) == null) {
            String text = NLS
                    .bind(
                            Messages.MethodCallBFE_methodDoesNotExistOnParameter,
                            new String[] { getExecutableMethodName(), getTarget() });
            list.add(new Message(MSGCODE_METHOD_DOES_NOT_EXIST, text, Message.ERROR, this));
        }
    }

}
