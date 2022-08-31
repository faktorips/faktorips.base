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

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.util.StringBuilderJoiner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IBaseMethod}
 * 
 * @author frank
 */
public class BaseMethod extends BaseIpsObjectPart implements IBaseMethod {

    public static final String XML_ELEMENT_NAME = "Method"; //$NON-NLS-1$

    private IpsObjectPartCollection<IParameter> parameters = new IpsObjectPartCollection<>(this,
            Parameter.class, IParameter.class, Parameter.TAG_NAME);

    private String datatype = "void"; //$NON-NLS-1$

    public BaseMethod(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, name);
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String newDatatype) {
        String oldDatatype = getDatatype();
        datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype, PROPERTY_DATATYPE);
    }

    @Override
    public IParameter newParameter() {
        return parameters.newPart();
    }

    @Override
    public IParameter newParameter(String datatype, String name) {
        IParameter param = newParameter();
        param.setDatatype(datatype);
        param.setName(name);
        return param;
    }

    @Override
    public int getNumOfParameters() {
        return parameters.size();
    }

    @Override
    public IParameter[] getParameters() {
        return parameters.toArray(new IParameter[parameters.size()]);
    }

    @Override
    public String[] getParameterNames() {
        String[] names = new String[parameters.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = (parameters.getPart(i)).getName();
        }
        return names;
    }

    @Override
    public List<Datatype> getParameterDatatypes() {
        List<Datatype> parameterDatatypes = new ArrayList<>();
        for (IParameter parameter : getParameters()) {
            Datatype parameterDatatype = parameter.findDatatype(getIpsProject());
            if (parameterDatatype != null) {
                parameterDatatypes.add(parameterDatatype);
            }
        }
        return parameterDatatypes;
    }

    @Override
    public int[] moveParameters(int[] indexes, boolean up) {
        return parameters.moveParts(indexes, up);
    }

    public IParameter getParameter(int i) {
        return parameters.getPart(i);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(BaseMethod.XML_ELEMENT_NAME);
    }

    @Override
    public String getSignatureString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append('(');
        StringBuilderJoiner.join(builder, getParameters(), p -> builder.append(p.getDatatype()));
        builder.append(')');
        return builder.toString();
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) {
        super.validateThis(result, ipsProject);
        if (IpsStringUtils.isEmpty(getName())) {
            result.add(new Message(IBaseMethod.MSGCODE_NO_NAME, Messages.Method_msg_NameEmpty, Message.ERROR, this,
                    PROPERTY_NAME));
        } else {
            Runtime.Version sourceVersion = ipsProject.getJavaProject().getSourceVersion();
            IStatus status = JavaConventions.validateMethodName(getName(), sourceVersion.toString(),
                    sourceVersion.toString());
            if (!status.isOK()) {
                result.add(new Message(IBaseMethod.MSGCODE_INVALID_METHODNAME, Messages.Method_msg_InvalidMethodname,
                        Message.ERROR, this, PROPERTY_NAME));
            }
        }
        ValidationUtils.checkDatatypeReference(getDatatype(), true, this, PROPERTY_DATATYPE, "", result, ipsProject); //$NON-NLS-1$

        validateMultipleParameterNames(result);
        // description same locale
    }

    private void validateMultipleParameterNames(MessageList msgList) {
        List<String> parameterNames = new ArrayList<>();
        Set<String> multipleNames = new HashSet<>();
        for (IParameter p : getParameters()) {
            if (parameterNames.contains(p.getName())) {
                multipleNames.add(p.getName());
            }
            parameterNames.add(p.getName());
        }
        if (multipleNames.isEmpty()) {
            return;
        }
        for (String paramName : multipleNames) {
            ArrayList<ObjectProperty> objProps = new ArrayList<>();
            for (int j = 0; j < parameterNames.size(); j++) {
                if (parameterNames.get(j).equals(paramName)) {
                    objProps.add(new ObjectProperty(getParameter(j), IBaseMethod.PROPERTY_PARAMETERS, j));
                }
            }
            ObjectProperty[] objectProperties = objProps.toArray(new ObjectProperty[objProps.size()]);
            String text = MessageFormat.format(Messages.Method_duplicateParamName, paramName);
            msgList.add(new Message(IBaseMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME, text, Message.ERROR,
                    objectProperties));
        }
    }

    @Override
    public void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        initDatatype(element);
    }

    private void initDatatype(Element element) {
        String datatypeElement = element.getAttribute(PROPERTY_DATATYPE);
        if (datatypeElement != null) {
            datatype = datatypeElement;
        }
    }

    @Override
    public Datatype findDatatype(IIpsProject ipsProject) {
        return ipsProject.findDatatype(getDatatype());
    }

    @Override
    public boolean isSameSignature(IBaseMethod other) {
        if (!getName().equals(other.getName()) || (getNumOfParameters() != other.getNumOfParameters())) {
            return false;
        }
        IParameter[] otherParams = other.getParameters();
        for (int i = 0; i < getNumOfParameters(); i++) {
            if (!getParameter(i).getDatatype().equals(otherParams[i].getDatatype())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_DATATYPE, getDatatype());
        element.setAttribute(PROPERTY_NAME, getName());
    }

    /**
     * Sets the name with the parent's name
     */
    public void synchronizeName() {
        name = getParent().getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation always returns {@link Modifier#PUBLIC}.
     */
    @Override
    public int getJavaModifier() {
        return Modifier.PUBLIC;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("BaseMethod: "); //$NON-NLS-1$
        buffer.append(getName());
        buffer.append(": "); //$NON-NLS-1$
        buffer.append(getDatatype());
        buffer.append(' ');
        buffer.append(getName());
        buffer.append('(');
        StringBuilderJoiner.join(buffer, getParameters(), p -> {
            buffer.append(p.getDatatype());
            buffer.append(' ');
            buffer.append(p.getName());
        });
        buffer.append(')');
        return buffer.toString();
    }

}
