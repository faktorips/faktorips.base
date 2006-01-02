package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 *
 */
public class Method extends Member implements IMethod {
    
    final static String TAG_NAME = "Method";
    final static String PARAMETER_TAG_NAME = "Parameter";
    final static String BODY_TAG_NAME = "Body";

    private String datatype = "void";
    private Modifier modifier = Modifier.PUBLISHED;
    private boolean abstractFlag = false;
    private Parameter[] parameters = new Parameter[0];
    private String body = "";
    
    /**
     * Creates a new method.
     * 
     * @param pcType The type the method belongs to.
     * @param id The method's unique id within the type.
     */
     Method(IIpsObject pdObject, int id) {
        super(pdObject, id);
    }

    /**
     * Constructor for testing purposes.
     */
    Method() {
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((PolicyCmptType)getIpsObject()).removeMethod(this);
        updateSrcFile();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getDatatype()
     */
    public String getDatatype() {
        return datatype;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#setDatatype(java.lang.String)
     */
    public void setDatatype(String newDatatype) {
        this.datatype = newDatatype;
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        Image image;
        if (modifier==Modifier.PRIVATE) {
            image = IpsPlugin.getDefault().getImage("MethodPrivate.gif");    
        } else {
            image = IpsPlugin.getDefault().getImage("MethodPublic.gif");
        }
        if (!isAbstract()) {
            return image;
        }
        return new AbstractPropertyImageDescriptor(image).createImage();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getParameters()
     */
    public Parameter[] getParameters() {
        Parameter[] copy = new Parameter[parameters.length];
        System.arraycopy(parameters, 0, copy, 0, parameters.length);
        return copy;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getParameterNames()
     */
    public String[] getParameterNames() {
        String[] names = new String[parameters.length];
        for (int i=0; i<names.length; i++) {
            names[i] = parameters[i].getName();
        }
        return names;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getParameterTypes()
     */
    public String[] getParameterTypes() {
        String[] types = new String[parameters.length];
        for (int i=0; i<types.length; i++) {
            types[i] = parameters[i].getDatatype();
        }
        return types;
    }
    
    /**
     * Overridden method.
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getParameterTypeSignatures()
     */
    public String[] getParameterTypeSignatures() throws CoreException {
        Parameter[] params = getParameters();
        String[] signatures = new String[params.length];
        for (int i=0; i<signatures.length; i++) {
            String datatypeName = params[i].getDatatype();
            Datatype datatype = getIpsProject().findDatatype(datatypeName);
            if (datatype==null) {
                throw new CoreException(new IpsStatus("Datatype " + datatypeName + " not found for method " + this + ", parameter " + i));
            }
            String classname = StringUtil.unqualifiedName(datatype.getJavaClassName());
            signatures[i] = Signature.createTypeSignature(classname, false);    
        }
        return signatures;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getNumOfParameters()
     */
    public int getNumOfParameters() {
        return parameters.length;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#setParameters(org.faktorips.devtools.core.model.pctype.Parameter[])
     */
    public void setParameters(Parameter[] params) {
        parameters = new Parameter[params.length];
        System.arraycopy(params, 0, parameters, 0, params.length);
        updateSrcFile();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getBody()
     */
    public String getBody() {
        return body;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#setBody(java.lang.String)
     */
    public void setBody(String sourcecode) {
        String oldBody = body;
        body = sourcecode;
        valueChanged(oldBody, body);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPart#validate(org.faktorips.util.message.MessageList)
     */
    protected void validate(MessageList result) throws CoreException {
    	super.validate(result);
        if (StringUtils.isEmpty(name)) {
            result.add(new Message("", "The name is empty!", Message.ERROR, this, PROPERTY_NAME));
        } else {
	        IStatus status = JavaConventions.validateMethodName(name);
	        if (!status.isOK()) {
	            result.add(new Message("", "Invalid method name.", Message.ERROR, this, PROPERTY_NAME));
	        }
        }
        if (StringUtils.isEmpty(datatype)) {
            result.add(new Message("", "The type is empty!", Message.ERROR, this, PROPERTY_DATATYPE));
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(datatype);
            if (datatypeObject==null) {
                result.add(new Message("", "The datatype " + datatype + " does not exists on the object path!", Message.ERROR, this, PROPERTY_DATATYPE));
            }
        }
        if (isAbstract() && !getPolicyCmptType().isAbstract()) {
            result.add(new Message("", "The abstract method " + getName() + " can only be defined in an abstract class!", Message.ERROR, this, PROPERTY_ABSTRACT));
        }
        for (int i=0; i<parameters.length; i++) {
            validate(parameters[i], result);
        }
    }
    
    private void validate(Parameter param, MessageList result) throws CoreException {
        if (StringUtils.isEmpty(param.getName())) {
            result.add(new Message("", "The name is empty!", Message.ERROR, param, PROPERTY_PARAM_NAME));
        } else {
	        IStatus status = JavaConventions.validateIdentifier(param.getName());
	        if (!status.isOK()) {
	            result.add(new Message("", "Invalid parameter name.", Message.ERROR, param, PROPERTY_PARAM_NAME));
	        }
        }
        if (StringUtils.isEmpty(param.getDatatype())) {
            result.add(new Message("", "The datatype is empty!", Message.ERROR, param, PROPERTY_PARAM_DATATYPE));
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(param.getDatatype());
            if (datatypeObject==null) {
                result.add(new Message("", "The datatype " + param.getDatatype() + " does not exists on the object path!", Message.ERROR, param, PROPERTY_PARAM_DATATYPE));
            }
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getModifier()
     */
    public Modifier getModifier() {
        return modifier;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#setModifier(org.faktorips.devtools.core.model.pctype.Modifier)
     */
    public void setModifier(Modifier newModifier) {
        Modifier oldModifier = modifier;
        modifier = newModifier;
        valueChanged(oldModifier, newModifier);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#isAbstract()
     */
    public boolean isAbstract() {
        return abstractFlag;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#setAbstract(boolean)
     */
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);        
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IMethod#isSame(org.faktorips.devtools.core.model.pctype.IMethod)
     */
    public boolean isSame(IMethod other) {
        if (!getName().equals(other.getName())) {
            return false;
        }
        if (getNumOfParameters()!=other.getNumOfParameters()) {
            return false;
        }
        Parameter[] otherParams = other.getParameters();
        for (int i=0; i<parameters.length; i++) {
            if (!parameters[i].getDatatype().equals(otherParams[i].getDatatype())) {
                return false;
            }
        }
        return true;
    }

    /** 
     * Overridden.
     */
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getParent();
    }
    
    /**
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
        NodeList nl = element.getChildNodes();
        List params = new ArrayList();
        int paramIndex = 0;
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element paramElement = (Element)nl.item(i);
                if (paramElement.getTagName().equals(PARAMETER_TAG_NAME)) {
                    Parameter newParam = new Parameter(paramIndex);
                    newParam.setName(paramElement.getAttribute("name"));
                    newParam.setDatatype(paramElement.getAttribute("datatype"));
                    params.add(newParam);
                    paramIndex++;
                }
            }
        }
        parameters = (Parameter[])params.toArray(new Parameter[0]);
        Element bodyElement = XmlUtil.getFirstElement(element, BODY_TAG_NAME);
        if (bodyElement!=null) {
            Text bodyText = XmlUtil.getTextNode(bodyElement);
            if (bodyText!=null) {
                body = bodyText.getData();
            }
        } else {
            body = "";
        }
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_DATATYPE, datatype);
        newElement.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag);
        Document doc = newElement.getOwnerDocument();
        for (int i=0; i<parameters.length; i++) {
            Element newParamElement = doc.createElement(PARAMETER_TAG_NAME);
            newParamElement.setAttribute("name", parameters[i].getName());
            newParamElement.setAttribute("datatype", parameters[i].getDatatype());
            newElement.appendChild(newParamElement);
        }
        Element bodyElement = doc.createElement(BODY_TAG_NAME);
        bodyElement.appendChild(doc.createTextNode(body));
        newElement.appendChild(bodyElement);
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
         * Overridden method.
         * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
         */
        protected void drawCompositeImage(int width, int height) {
    		drawImage(baseImage.getImageData(), 0, 0);
    		drawImage(IpsPlugin.getDefault().getImage("AbstractIndicator.gif").getImageData(), 8, 0);
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
         */
        protected Point getSize() {
            return size;
        }
    }

    
    
}