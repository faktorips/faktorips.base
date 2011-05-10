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

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class Method extends TypePart implements IMethod {

    public final static String XML_ELEMENT_NAME = "Method"; //$NON-NLS-1$

    private String datatype = "void"; //$NON-NLS-1$

    private boolean abstractFlag = false;

    private IpsObjectPartCollection<IParameter> parameters = new IpsObjectPartCollection<IParameter>(this,
            Parameter.class, IParameter.class, Parameter.TAG_NAME);

    public Method(IType parent, String id) {
        super(parent, id);
    }

    @Override
    public IType getType() {
        return (IType)getParent();
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
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findDatatype(datatype);
    }

    @Override
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }

    @Override
    public boolean isAbstract() {
        return abstractFlag;
    }

    @Override
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public int getJavaModifier() {
        return getModifier().getJavaModifier() | (abstractFlag ? java.lang.reflect.Modifier.ABSTRACT : 0);
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
    public int[] moveParameters(int[] indexes, boolean up) {
        return parameters.moveParts(indexes, up);
    }

    public IParameter getParameter(int i) {
        return parameters.getPart(i);
    }

    @Override
    public IMethod findOverridingMethod(IType typeToSearchFrom, IIpsProject ipsProject) throws CoreException {
        if (!typeToSearchFrom.isSubtypeOf(getType(), ipsProject)) {
            return null;
        }
        OverridingMethodFinder finder = new OverridingMethodFinder(ipsProject);
        finder.start(typeToSearchFrom);
        return finder.overridingMethod;
    }

    @Override
    public IMethod findOverriddenMethod(IIpsProject ipsProject) throws CoreException {
        OverridingMethodFinder finder = new OverridingMethodFinder(ipsProject);
        finder.start(getType());
        return finder.overridingMethod;
    }

    @Override
    public boolean isSameSignature(IMethod other) {
        if (!getName().equals(other.getName())) {
            return false;
        }
        if (getNumOfParameters() != other.getNumOfParameters()) {
            return false;
        }
        IParameter[] otherParams = other.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (!getParameter(i).getDatatype().equals(otherParams[i].getDatatype())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean overrides(IMethod other) throws CoreException {
        if (!isSameSignature(other)) {
            return false;
        }
        if (!getType().isSubtypeOf(other.getType(), other.getIpsProject())) {
            return false;
        }
        return true;
    }

    @Override
    public String getSignatureString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName());
        buffer.append('(');
        IParameter[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(params[i].getDatatype());
        }
        buffer.append(')');
        return buffer.toString();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_ELEMENT_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_NAME, name);
        newElement.setAttribute(PROPERTY_DATATYPE, datatype);
        newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (StringUtils.isEmpty(name)) {
            result.add(new Message("", Messages.Method_msg_NameEmpty, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
        } else {
            String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
            String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
            IStatus status = JavaConventions.validateMethodName(name, sourceLevel, complianceLevel);
            if (!status.isOK()) {
                result.add(new Message("", Messages.Method_msg_InvalidMethodname, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
            }
        }
        ValidationUtils.checkDatatypeReference(datatype, true, this, PROPERTY_DATATYPE, "", result, ipsProject); //$NON-NLS-1$
        if (isAbstract() && !getType().isAbstract()) {
            result.add(new Message(
                    "", NLS.bind(Messages.Method_msg_abstractMethodError, getName()), Message.ERROR, this, PROPERTY_ABSTRACT)); //$NON-NLS-1$
        }
        validateMultipleParameterNames(result);
        if (validateDuplicateMethodInSameType(result)) {
            validateReturnTypeOfOverriddenMethod(result, ipsProject);
        }
    }

    private void validateMultipleParameterNames(MessageList msgList) {
        List<String> parameterNames = new ArrayList<String>();
        Set<String> multipleNames = new HashSet<String>();
        for (IParameter p : parameters) {
            if (parameterNames.contains(p.getName())) {
                multipleNames.add(p.getName());
            }
            parameterNames.add(p.getName());
        }
        if (multipleNames.isEmpty()) {
            return;
        }
        for (String paramName : multipleNames) {
            ArrayList<ObjectProperty> objProps = new ArrayList<ObjectProperty>();
            for (int j = 0; j < parameterNames.size(); j++) {
                if (parameterNames.get(j).equals(paramName)) {
                    objProps.add(new ObjectProperty(getParameter(j), PROPERTY_PARAMETERS, j));
                }
            }
            ObjectProperty[] objectProperties = objProps.toArray(new ObjectProperty[objProps.size()]);
            String text = NLS.bind(Messages.Method_duplicateParamName, paramName);
            msgList.add(new Message(MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME, text, Message.ERROR, objectProperties));
        }
    }

    private void validateReturnTypeOfOverriddenMethod(MessageList list, IIpsProject ipsProject) throws CoreException {
        Datatype returnType = findDatatype(ipsProject);
        if (returnType == null) {
            return;
        }
        IMethod overridden = findOverriddenMethod(ipsProject);
        if (overridden == null) {
            return;
        }
        Datatype overriddenReturnType = overridden.findDatatype(ipsProject);
        if (!returnType.equals(overriddenReturnType)) {
            String text = NLS.bind(Messages.Method_incompatbileReturnType, overridden.getType().getUnqualifiedName(),
                    overridden.getSignatureString());
            Message msg = Message.newError(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE, text, this,
                    IMethod.PROPERTY_DATATYPE);
            list.add(msg);
        }
    }

    private boolean validateDuplicateMethodInSameType(MessageList msgList) {
        List<IMethod> methods = getType().getMethods();
        String thisSignature = getSignatureString();
        for (IMethod method : methods) {
            if (method.equals(this)) {
                continue;
            }
            if (method.getSignatureString().equals(thisSignature)) {
                msgList.add(new Message(MSGCODE_DUBLICATE_SIGNATURE, Messages.Method_duplicateSignature, Message.ERROR,
                        this));
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getType().getQualifiedName());
        buffer.append(": "); //$NON-NLS-1$
        buffer.append(datatype);
        buffer.append(' ');
        buffer.append(getName());
        buffer.append('(');
        IParameter[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(params[i].getDatatype());
            buffer.append(' ');
            buffer.append(params[i].getName());
        }
        buffer.append(')');
        return buffer.toString();
    }

    public void dependsOn(Set<IDependency> dependencies, Map<IDependency, List<IDependencyDetail>> details) {
        IDependency dependency = new DatatypeDependency(getType().getQualifiedNameType(), getDatatype());
        dependencies.add(dependency);
        addDetails(details, dependency, this, PROPERTY_DATATYPE);
        for (IParameter parameter : parameters) {
            dependency = new DatatypeDependency(getType().getQualifiedNameType(), parameter.getDatatype());
            dependencies.add(dependency);
            addDetails(details, dependency, parameter, IParameter.PROPERTY_DATATYPE);
        }
    }

    class OverridingMethodFinder extends TypeHierarchyVisitor<IType> {

        private IMethod overridingMethod;

        public OverridingMethodFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            IMethod match = currentType.getMatchingMethod(Method.this);
            if (match != null && match != Method.this) {
                overridingMethod = match;
                return false;
            }
            return true;
        }

    }

}
