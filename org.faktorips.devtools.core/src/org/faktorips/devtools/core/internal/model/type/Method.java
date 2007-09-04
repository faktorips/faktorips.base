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
import org.faktorips.devtools.core.internal.model.pctype.Messages;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
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

    private final static String XML_ELEMENT_NAME = "Method";
    
    private String datatype = "void"; //$NON-NLS-1$
    private Modifier modifier = Modifier.PUBLISHED;
    private boolean abstractFlag = false;
    
    private IpsObjectPartCollection parameters = new IpsObjectPartCollection(this, Parameter.class, Parameter.TAG_NAME);
    
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
    public void setDatatype(String newDatatype) {
        String oldDatatype = newDatatype;
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
    public int[] moveParameters(int[] indexes, boolean up) {
        return parameters.moveParts(indexes, up);
    }
    
    public IParameter getParameter(int i) {
        return (IParameter)parameters.getPart(i);
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isSame(IMethod other) {
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
        if (StringUtils.isEmpty(name)) {
            result.add(new Message("", Messages.Method_msgNameEmpty, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
        } else {
            IStatus status = JavaConventions.validateMethodName(name);
            if (!status.isOK()) {
                result.add(new Message("", Messages.Method_msgInvalidMethodname, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
            }
        }
        if (StringUtils.isEmpty(datatype)) {
            result.add(new Message("", Messages.Method_msgTypeEmpty, Message.ERROR, this, PROPERTY_DATATYPE)); //$NON-NLS-1$
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(datatype);
            if (datatypeObject==null) {
                result.add(new Message("", NLS.bind(Messages.Method_msgDatatypeNotFound, datatype), Message.ERROR, this, PROPERTY_DATATYPE)); //$NON-NLS-1$
            }
        }
        if (isAbstract() && !getType().isAbstract()) {
            result.add(new Message("", NLS.bind(Messages.Method_abstractMethodError, getName()), Message.ERROR, this, PROPERTY_ABSTRACT)); //$NON-NLS-1$
        }
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


}
