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

package org.faktorips.devtools.core.internal.model;

import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.product.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An ips project's properties. The project can't keep the properties on it's own, as it is
 * a handle.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectProperties implements IIpsProjectProperties {

	public final static IpsProjectProperties createFromXml(IpsProject ipsProject, Element element) {
		IpsProjectProperties data = new IpsProjectProperties();
		data.initFromXml(ipsProject, element);
		return data;
	}
	
	final static String TAG_NAME = "IpsProject"; //$NON-NLS-1$
	final static String GENERATED_CODE_TAG_NAME = "GeneratedSourcecode";  //$NON-NLS-1$
		
	private boolean createdFromParsableFileContents = true;
	
	private boolean modelProject;
	private boolean productDefinitionProject;
	private Locale javaSrcLanguage = Locale.ENGLISH;
	private String changesInTimeConventionIdForGeneratedCode = IChangesOverTimeNamingConvention.VAA;
	private IProductCmptNamingStrategy productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
	private String builderSetId = ""; //$NON-NLS-1$
	private IIpsObjectPath path = new IpsObjectPath();
	private String[] predefinedDatatypesUsed = new String[0];
    private DynamicValueDatatype[] definedDatatypes = new DynamicValueDatatype[0]; 
    private String runtimeIdPrefix = ""; //$NON-NLS-1$


    /**
     * Default constructor.
     */
	public IpsProjectProperties() {
		super();
	}

    /**
     * Copy constructor.
     */
	public IpsProjectProperties(IIpsProject ipsProject, IpsProjectProperties props) {
		Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
		Element el = props.toXml(doc);
		initFromXml(ipsProject, el);
		this.createdFromParsableFileContents = props.createdFromParsableFileContents;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MessageList validate(IIpsProject ipsProject) throws CoreException {
		MessageList list = new MessageList();
		validateBuilderSetId(ipsProject, list);
		return list;
	}
	
	private void validateBuilderSetId(IIpsProject ipsProject, MessageList list) {
		IIpsArtefactBuilderSet[] sets = ipsProject.getIpsModel().getAvailableArtefactBuilderSets();
		for (int i = 0; i < sets.length; i++) {
			if (sets[i].getId().equals(builderSetId)) {
				return;
			}
		}
		String text = "Unknown builder set id " + builderSetId;
		Message msg = new Message(IIpsProjectProperties.MSGCODE_UNKNOWN_BUILDER_SET_ID, text, Message.ERROR, this, IIpsProjectProperties.PROPERTY_BUILDER_SET_ID);
		list.add(msg);
	}

	/**
	 * Returns <code>true</code> if this property object was created by reading a  
	 * .ipsproject file containg parsable xml data, otherwise <code>false</code>.
	 */
	public boolean isCreatedFromParsableFileContents() {
		return createdFromParsableFileContents;
	}

	/**
	 * Sets if if this property object was created by reading a .ipsproject file 
	 * containg parsable xml data, or not.
	 */
	public void setCreatedFromParsableFileContents(boolean flag) {
		this.createdFromParsableFileContents = flag;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBuilderSetId() {
		return builderSetId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setBuilderSetId(String id) {
		ArgumentCheck.notNull(id);
		builderSetId = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPath getIpsObjectPath() {
		return path;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isModelProject() {
		return modelProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setModelProject(boolean modelProject) {
		this.modelProject = modelProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isProductDefinitionProject() {
		return productDefinitionProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProductDefinitionProject(boolean productDefinitionProject) {
		this.productDefinitionProject = productDefinitionProject;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Locale getJavaSrcLanguage() {
		return javaSrcLanguage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJavaSrcLanguage(Locale javaSrcLanguage) {
		this.javaSrcLanguage = javaSrcLanguage;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
		return productCmptNamingStrategy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy) {
		ArgumentCheck.notNull(newStrategy);
		productCmptNamingStrategy = newStrategy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangesOverTimeNamingConventionIdForGeneratedCode(
			String changesInTimeConventionIdForGeneratedCode) {
		this.changesInTimeConventionIdForGeneratedCode = changesInTimeConventionIdForGeneratedCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getChangesOverTimeNamingConventionIdForGeneratedCode() {
		return changesInTimeConventionIdForGeneratedCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIpsObjectPath(IIpsObjectPath path) {
		ArgumentCheck.notNull(path);
		this.path = path;
	}
	
    /**
	 * {@inheritDoc}
	 */
	public String[] getPredefinedDatatypesUsed() {
		return predefinedDatatypesUsed;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPredefinedDatatypesUsed(String[] datatypes) {
		ArgumentCheck.notNull(datatypes);
		this.predefinedDatatypesUsed = datatypes;
	}
    
    /**
	 * {@inheritDoc}
	 */
    public DynamicValueDatatype[] getDefinedDatatypes() {
        return definedDatatypes;
    }
    
    /**
	 * {@inheritDoc}
	 */
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes) {
        definedDatatypes = datatypes;
    }

	public Element toXml(Document doc) {
		Element projectEl = doc.createElement(TAG_NAME);
		projectEl.setAttribute("modelProject", "" + modelProject); //$NON-NLS-1$ //$NON-NLS-2$
		projectEl.setAttribute("productDefinitionProject", "" + productDefinitionProject); //$NON-NLS-1$ //$NON-NLS-2$
		projectEl.setAttribute("runtimeIdPrefix", runtimeIdPrefix); //$NON-NLS-1$
		Element generatedCodeEl = doc.createElement(GENERATED_CODE_TAG_NAME);
		projectEl.appendChild(generatedCodeEl);
		generatedCodeEl.setAttribute("docLanguage", javaSrcLanguage.toString()); //$NON-NLS-1$
		generatedCodeEl.setAttribute("changesInTimeNamingConvention", changesInTimeConventionIdForGeneratedCode); //$NON-NLS-1$
		projectEl.appendChild(productCmptNamingStrategy.toXml(doc));
		Element builderSetEl = doc.createElement(IIpsArtefactBuilderSet.XML_ELEMENT);
		projectEl.appendChild(builderSetEl);
		builderSetEl.setAttribute("id", builderSetId); //$NON-NLS-1$
		
		// object path
		projectEl.appendChild(((IpsObjectPath)path).toXml(doc));
		
		// datatypes
		Element datatypesEl = doc.createElement("Datatypes"); //$NON-NLS-1$
		projectEl.appendChild(datatypesEl);
        Element predefinedTypesEl = doc.createElement("UsedPredefinedDatatypes"); //$NON-NLS-1$
        datatypesEl.appendChild(predefinedTypesEl);
		for (int i = 0; i < predefinedDatatypesUsed.length; i++) {
			Element datatypeEl = doc.createElement("Datatype"); //$NON-NLS-1$
			datatypeEl.setAttribute("id", predefinedDatatypesUsed[i]); //$NON-NLS-1$
            predefinedTypesEl.appendChild(datatypeEl);
		}
		Element definedDatatypesEl = doc.createElement("DatatypeDefinitions"); //$NON-NLS-1$
		datatypesEl.appendChild(definedDatatypesEl);
		writeDefinedDataTypesToXML(doc, definedDatatypesEl);
		
		return projectEl;
	}
	
	public void initFromXml(IIpsProject ipsProject, Element element) {
        modelProject = Boolean.valueOf(element.getAttribute("modelProject")).booleanValue(); //$NON-NLS-1$
        productDefinitionProject = Boolean.valueOf(element.getAttribute("productDefinitionProject")).booleanValue(); //$NON-NLS-1$
        runtimeIdPrefix = element.getAttribute("runtimeIdPrefix"); //$NON-NLS-1$
        
        Element generatedCodeEl = XmlUtil.getFirstElement(element, GENERATED_CODE_TAG_NAME);
        if (generatedCodeEl!=null) {
    	    javaSrcLanguage = getLocale(generatedCodeEl.getAttribute("docLanguage")); //$NON-NLS-1$
    	    changesInTimeConventionIdForGeneratedCode = generatedCodeEl.getAttribute("changesInTimeNamingConvention"); //$NON-NLS-1$
        } else {
        	javaSrcLanguage = Locale.ENGLISH;
        	changesInTimeConventionIdForGeneratedCode = IChangesOverTimeNamingConvention.VAA;
        }
        Element artefactEl = XmlUtil.getFirstElement(element, IIpsArtefactBuilderSet.XML_ELEMENT);
        if(artefactEl != null) {
            builderSetId = artefactEl.getAttribute("id"); //$NON-NLS-1$
        } else {
        	builderSetId = ""; //$NON-NLS-1$
        }
        initProductCmptNamingStrategyFromXml(ipsProject, XmlUtil.getFirstElement(element, IProductCmptNamingStrategy.XML_TAG_NAME));
        if(artefactEl != null) {
            builderSetId = artefactEl.getAttribute("id"); //$NON-NLS-1$
        } else {
        	builderSetId = ""; //$NON-NLS-1$
        }
        Element pathEl = XmlUtil.getFirstElement(element, IpsObjectPath.XML_TAG_NAME);
        if (pathEl != null) {
            path = IpsObjectPath.createFromXml(ipsProject, pathEl);
        } else {
        	path = new IpsObjectPath();
        }
        Element datatypesEl = XmlUtil.getFirstElement(element, "Datatypes"); //$NON-NLS-1$
        if (datatypesEl==null) {
        	predefinedDatatypesUsed = new String[0];
            definedDatatypes = new DynamicValueDatatype[0];
        	return;
        }
        initUsedPredefinedDatatypesFromXml(XmlUtil.getFirstElement(datatypesEl, "UsedPredefinedDatatypes")); //$NON-NLS-1$
        initDefinedDatatypesFromXml(ipsProject, XmlUtil.getFirstElement(datatypesEl, "DatatypeDefinitions")); //$NON-NLS-1$
        
        
	}
	
	private void initProductCmptNamingStrategyFromXml(IIpsProject ipsProject, Element el) {
		productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
		if (el!=null) {
        	String id = el.getAttribute("id"); //$NON-NLS-1$
        	if (id.equals(DateBasedProductCmptNamingStrategy.EXTENSION_ID)) {
        		productCmptNamingStrategy = new DateBasedProductCmptNamingStrategy();
        	}
        	productCmptNamingStrategy.setIpsProject(ipsProject);
    		productCmptNamingStrategy.initFromXml(el);
		}
	}

    private void initUsedPredefinedDatatypesFromXml(Element element) {
        if (element==null) {
            predefinedDatatypesUsed = new String[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype"); //$NON-NLS-1$
        predefinedDatatypesUsed = new String[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            predefinedDatatypesUsed[i] = ((Element)nl.item(i)).getAttribute("id"); //$NON-NLS-1$
        }
    }
    
    private void initDefinedDatatypesFromXml(IIpsProject ipsProject, Element element) {
        if (element==null) {
            definedDatatypes = new DynamicValueDatatype[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype"); //$NON-NLS-1$
        definedDatatypes = new DynamicValueDatatype[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            definedDatatypes[i] = DynamicValueDatatype.createFromXml(ipsProject, (Element)nl.item(i));
        }
    }
    
    private void writeDefinedDataTypesToXML(Document doc, Element parent) {
		for (int i=0; i<definedDatatypes.length; i++) {
			Element datatypeEl = doc.createElement("Datatype"); //$NON-NLS-1$
			definedDatatypes[i].writeToXml(datatypeEl);
			parent.appendChild(datatypeEl);
		}
	}

    static Locale getLocale(String s) {
    	StringTokenizer tokenzier = new StringTokenizer(s, "_"); //$NON-NLS-1$
    	if (!tokenzier.hasMoreTokens()) {
    		return Locale.ENGLISH;
    	}
    	String language = tokenzier.nextToken();
    	if (!tokenzier.hasMoreTokens()) {
    		return new Locale(language);
    	}
    	String country = tokenzier.nextToken();
    	if (!tokenzier.hasMoreTokens()) {
    		return new Locale(language, country);
    	}
    	String variant = tokenzier.nextToken();
    	return new Locale(language, country, variant);
    }

	public void addDefinedDataType(DynamicValueDatatype newDatatype) {
		DynamicValueDatatype [] oldValue = definedDatatypes;
		int i;
		/* replace, if Datatype already registered */
		for (i = 0; i < definedDatatypes.length; i++) {
			if(definedDatatypes[i].getAdaptedClassName().equals(newDatatype.getAdaptedClassName())) {
				definedDatatypes[i] = newDatatype;
				return;
			}
		}
		definedDatatypes = new DynamicValueDatatype [oldValue.length + 1];
		for (i = 0; i < oldValue.length; i++) {
			definedDatatypes[i]=oldValue[i];
		}
		definedDatatypes[i]=newDatatype;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeIdPrefix() {
		return this.runtimeIdPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRuntimeIdPrefix(String runtimeIdPrefix) {
		if (runtimeIdPrefix == null) {
			throw new NullPointerException("RuntimeIdPrefix can not be null"); //$NON-NLS-1$
		}
		this.runtimeIdPrefix = runtimeIdPrefix;
	}
}
