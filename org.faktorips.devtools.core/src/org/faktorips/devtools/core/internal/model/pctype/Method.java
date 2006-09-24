/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.List;

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
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IParameter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class Method extends IpsObjectPart implements IMethod {
    
    final static String TAG_NAME = "Method"; //$NON-NLS-1$

    private String datatype = "void"; //$NON-NLS-1$
    private Modifier modifier = Modifier.PUBLISHED;
    private boolean abstractFlag = false;
    private List parameters = new ArrayList();
    private boolean deleted = false;

    
    /**
     * Creates a new method.
     * 
     * @param type The type the method belongs to.
     * @param id The method's id.
     */
    public Method(IPolicyCmptType type, int id) {
        super(type, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public Method() {
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
    public void delete() {
        ((PolicyCmptType)getIpsObject()).removeMethod(this);
        objectHasChanged();
        deleted = true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
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
        this.datatype = newDatatype;
        objectHasChanged();
    }

    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
        Image image;
        if (modifier==Modifier.PRIVATE) {
            image = IpsPlugin.getDefault().getImage("MethodPrivate.gif");     //$NON-NLS-1$
        } else {
            image = IpsPlugin.getDefault().getImage("MethodPublic.gif"); //$NON-NLS-1$
        }
        if (!isAbstract()) {
            return image;
        }
        return new AbstractPropertyImageDescriptor(image).createImage();
    }
    
    /**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		return getParameters();
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
	public IParameter newParameter() {
		Parameter p = newParameterInternal(getNextPartId());
		objectHasChanged();
		return p;
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

	/*
	 * Creates a new parameter without updating the src file.
	 */
	private Parameter newParameterInternal(int id) {
		Parameter p = new Parameter(this, id);
		parameters.add(p);
		return p;
	}

	private IParameter getParameter(int index) {
    	return (IParameter)parameters.get(index);
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getParameterNames() {
        String[] names = new String[parameters.size()];
        for (int i=0; i<names.length; i++) {
            names[i] = getParameter(i).getName();
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getParameterTypes() {
        String[] types = new String[parameters.size()];
        for (int i=0; i<types.length; i++) {
            types[i] = getParameter(i).getDatatype();
        }
        return types;
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
	public void removeParameter(IParameter param) {
		if (parameters.remove(param)) {
			objectHasChanged();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int[] moveParameters(int[] indexes, boolean up) {
		ListElementMover mover = new ListElementMover(parameters);
		return mover.move(indexes, up);
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
        if (isAbstract() && !getPolicyCmptType().isAbstract()) {
            result.add(new Message("", NLS.bind(Messages.Method_abstractMethodError, getName()), Message.ERROR, this, PROPERTY_ABSTRACT)); //$NON-NLS-1$
        }
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
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getParent();
    }
    
    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
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
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(Parameter.TAG_NAME)) {
			return newParameterInternal(id);
		}
		if (xmlTagName.equals("Body")) { //$NON-NLS-1$
			return null; // migration for old files
		}
		throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		if (IParameter.class.isAssignableFrom(partType)) {
			return newParameter();
		}
		throw new IllegalArgumentException("Could not create part for class " + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof IParameter) {
			parameters.add(part);
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		super.reinitPartCollections();
		parameters.clear();
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