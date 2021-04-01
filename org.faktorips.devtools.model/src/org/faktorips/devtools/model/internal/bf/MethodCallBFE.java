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
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.bf.IMethodCallBFE;
import org.faktorips.devtools.model.bf.IParameterBFE;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Element;

public abstract class MethodCallBFE extends BFElement implements IMethodCallBFE {

    private String executableMethodName = ""; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$

    public MethodCallBFE(IIpsObject parent, String id) {
        super(parent, id);
    }

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

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("executableMethodName", executableMethodName); //$NON-NLS-1$
        element.setAttribute("target", target); //$NON-NLS-1$
    }

    @Override
    public String getExecutableMethodName() {
        return executableMethodName;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public IParameterBFE getParameter() {
        return getBusinessFunction().getParameterBFE(getTarget());
    }

    @Override
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

    @Override
    public void setExecutableMethodName(String name) {
        String old = executableMethodName;
        executableMethodName = name;
        valueChanged(old, name);
    }

    @Override
    public void setTarget(String target) {
        String old = this.target;
        this.target = target;
        valueChanged(old, target);
    }

    protected void validateMethodCall(MessageList list, IIpsProject ipsProject) throws CoreException {
        // The parameter has to be specified.
        if (StringUtils.isEmpty(target)) {
            list.add(new Message(MSGCODE_TARGET_NOT_SPECIFIED, Messages.MethodCallBFE_parameterNotSpecified,
                    Message.ERROR, this));
            return;
        }

        // The parameter must exist.
        if (getParameter() == null) {
            list.add(new Message(MSGCODE_TARGET_DOES_NOT_EXIST, Messages.MethodCallBFE_parameterMissing, Message.ERROR,
                    this));
            return;
        }

        // The method has to be specified.
        if (StringUtils.isEmpty(getExecutableMethodName())) {
            list.add(new Message(MSGCODE_METHOD_NOT_SPECIFIED, Messages.MethodCallBFE_methodMustBeSpecified,
                    Message.ERROR, this));
            return;
        }

        validateNotAllowedNames(getExecutableMethodName(), Messages.MethodCallBFE_methodName, list);
        Datatype datatype = getParameter().findDatatype();
        if (datatype == null) {
            // This case has to be handled in the parameter validation.
            return;
        }

        // Only parameters with IType data types are allowed.
        if (!(datatype instanceof IType)) {
            list.add(new Message(MSGCODE_TARGET_NOT_VALID_TYPE, Messages.MethodCallBFE_parameterNoType, Message.ERROR,
                    this));
            return;
        }

        // The method has to exist.
        IType type = (IType)datatype;
        if (type.findMethod(getExecutableMethodName(), new String[0], ipsProject) == null) {
            String text = NLS.bind(Messages.MethodCallBFE_methodDoesNotExistOnParameter, new String[] {
                    getExecutableMethodName(), getTarget() });
            list.add(new Message(MSGCODE_METHOD_DOES_NOT_EXIST, text, Message.ERROR, this));
        }
    }

}
