/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.Modifier;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class Method extends BaseIpsObjectPart implements IMethod {

    public final static String XML_ELEMENT_NAME = "Method";
    
    private String datatype = "void"; //$NON-NLS-1$
    private Modifier modifier = Modifier.PUBLISHED;
    private boolean abstractFlag = false;
    
    private IpsObjectPartCollection parameters = new IpsObjectPartCollection(this, Parameter.class, IParameter.class, Parameter.TAG_NAME);
    
    public Method(IType parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    public IType getType() {
        return (IType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String newName) {
        String oldName = name;
        this.name = newName;
        valueChanged(oldName, name);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }
    
    /**
     * {@inheritDoc}
     */
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findDatatype(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        this.datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return abstractFlag;
    }

    /** 
     * {@inheritDoc}
     */
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);        
    }

    /** 
     * {@inheritDoc}
     */
    public Modifier getModifier() {
        return modifier;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getJavaModifier() {
        return modifier.getJavaModifier() | (abstractFlag ? java.lang.reflect.Modifier.ABSTRACT : 0);
    }

    /** 
     * {@inheritDoc}
     */
    public void setModifier(Modifier newModifier) {
        Modifier oldModifier = modifier;
        modifier = newModifier;
        valueChanged(oldModifier, newModifier);
    }

    /**
     * {@inheritDoc}
     */
    public IParameter newParameter() {
        return (IParameter)parameters.newPart();
    }
    
    /**
     * {@inheritDoc}
     */
    public IParameter newParameter(String datatype, String name) {
        IParameter param = newParameter();
        param.setDatatype(datatype);
        param.setName(name);
        return param;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNumOfParameters() {
        return parameters.size();
    }

    /**
     * {@inheritDoc}
     */
    public IParameter[] getParameters() {
        return (IParameter[])parameters.toArray(new IParameter[parameters.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getParameterNames() {
        String[] names = new String[parameters.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = ((IParameter)parameters.getPart(i)).getName();
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveParameters(int[] indexes, boolean up) {
        return parameters.moveParts(indexes, up);
    }
    
    public IParameter getParameter(int i) {
        return (IParameter)parameters.getPart(i);
    }
    
    /**
     * {@inheritDoc}
     */
    public IMethod findOverridingMethod(IType typeToSearchFrom, IIpsProject ipsProject) throws CoreException {
        OverridingMethodFinder finder = new OverridingMethodFinder(ipsProject);
        finder.start(typeToSearchFrom);
        return finder.overridingMethod;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean overrides(IMethod other) {
        if (!getName().equals(other.getName())) {
            return false;
        }
        if (getNumOfParameters()!=other.getNumOfParameters()) {
            return false;
        }
        IParameter[] otherParams = other.getParameters();
        for (int i=0; i<parameters.size(); i++) {
            if (!getParameter(i).getDatatype().equals(otherParams[i].getDatatype())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getSignatureString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName());
        buffer.append('(');
        IParameter[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(params[i].getDatatype());
        }
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(XML_ELEMENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        Image image = IpsPlugin.getDefault().getImage("MethodPublic.gif"); //$NON-NLS-1$
        if (!isAbstract()) {
            return image;
        }
        return new AbstractPropertyImageDescriptor(image).createImage();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_NAME, name);
        newElement.setAttribute(PROPERTY_DATATYPE, datatype);
        newElement.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
    }
 
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList result) throws CoreException {
        super.validateThis(result);
        IIpsProject ipsProject = getIpsProject();
        if (StringUtils.isEmpty(name)) {
            result.add(new Message("", Messages.Method_msg_NameEmpty, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
        } else {
            IStatus status = JavaConventions.validateMethodName(name);
            if (!status.isOK()) {
                result.add(new Message("", Messages.Method_msg_InvalidMethodname, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
            }
        }
        ValidationUtils.checkDatatypeReference(datatype, true, this, PROPERTY_DATATYPE, "", result, ipsProject);
        if (isAbstract() && !getType().isAbstract()) {
            result.add(new Message("", NLS.bind(Messages.Method_msg_abstractMethodError, getName()), Message.ERROR, this, PROPERTY_ABSTRACT)); //$NON-NLS-1$
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getType().getQualifiedName());
        buffer.append(": ");
        buffer.append(datatype);
        buffer.append(' ');
        buffer.append(getName());
        buffer.append('(');
        IParameter[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(params[i].getDatatype());
            buffer.append(' ');
            buffer.append(params[i].getName());
        }
        buffer.append(')');
        return buffer.toString();
    }
    
    private static class AbstractPropertyImageDescriptor extends CompositeImageDescriptor {

        private final static Point DEFAULT_SIZE = new Point(16, 16);
        
        private Image baseImage;
        private Point size = DEFAULT_SIZE;
        
        public AbstractPropertyImageDescriptor(Image image) {
            ArgumentCheck.notNull(image);
            baseImage = image;
        }

        /** 
         * {@inheritDoc}
         */
        protected void drawCompositeImage(int width, int height) {
            drawImage(baseImage.getImageData(), 0, 0);
            drawImage(IpsPlugin.getDefault().getImage("AbstractIndicator.gif").getImageData(), 8, 0); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        protected Point getSize() {
            return size;
        }
    }

    class OverridingMethodFinder extends TypeHierarchyVisitor {

        private IMethod overridingMethod;
        
        public OverridingMethodFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IMethod match = currentType.getMatchingMethod(Method.this);
            if (match != null && match!=Method.this) {
                overridingMethod = match;
                return false;
            }
            return true;
        }
        
    }
}
