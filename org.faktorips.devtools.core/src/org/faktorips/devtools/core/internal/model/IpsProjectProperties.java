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

import org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.product.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An ips project's properties. The project can't keep the properties on it's own, as it is
 * a handle.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectProperties {

	public final static IpsProjectProperties createFromXml(IpsProject ipsProject, Element element) {
		IpsProjectProperties data = new IpsProjectProperties();
		data.initFromXml(ipsProject, element);
		return data;
	}
	
	final static String TAG_NAME = "IpsProject"; //$NON-NLS-1$
	final static String GENERATED_CODE_TAG_NAME = "GeneratedSourcecode";  //$NON-NLS-1$
		
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

	public String getBuilderSetId() {
		return builderSetId;
	}
	
	public void setBuilderSetId(String id) {
		ArgumentCheck.notNull(id);
		builderSetId = id;
	}
	
	public IIpsObjectPath getIpsObjectPath() {
		return path;
	}
	
	public boolean isModelProject() {
		return modelProject;
	}

	public void setModelProject(boolean modelProject) {
		this.modelProject = modelProject;
	}

	public boolean isProductDefinitionProject() {
		return productDefinitionProject;
	}

	public void setProductDefinitionProject(boolean productDefinitionProject) {
		this.productDefinitionProject = productDefinitionProject;
	}
	
	public Locale getJavaSrcLanguage() {
		return javaSrcLanguage;
	}

	public void setJavaSrcLanguage(Locale javaSrcLanguage) {
		this.javaSrcLanguage = javaSrcLanguage;
	}
	
	public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
		return productCmptNamingStrategy;
	}
	
	public void setChangesInTimeConventionIdForGeneratedCode(
			String changesInTimeConventionIdForGeneratedCode) {
		this.changesInTimeConventionIdForGeneratedCode = changesInTimeConventionIdForGeneratedCode;
	}

	public String getChangesInTimeConventionIdForGeneratedCode() {
		return changesInTimeConventionIdForGeneratedCode;
	}

	public void setIpsObjectPath(IIpsObjectPath path) {
		ArgumentCheck.notNull(path);
		this.path = path;
	}
	
    /**
     * Returns the ids of the predefined datatypes used by the project.
     */
	public String[] getPredefinedDatatypesUsed() {
		return predefinedDatatypesUsed;
	}

	public void setPredefinedDatatypesUsed(String[] datatypes) {
		ArgumentCheck.notNull(datatypes);
		this.predefinedDatatypesUsed = datatypes;
	}
    
    public DynamicValueDatatype[] getDefinedDatatypes() {
        return definedDatatypes;
    }
    
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes) {
        definedDatatypes = datatypes;
    }

	public Element toXml(Document doc) {
		Element projectEl = doc.createElement(TAG_NAME);
		projectEl.setAttribute("modelProject", "" + modelProject); //$NON-NLS-1$ //$NON-NLS-2$
		projectEl.setAttribute("productDefinitionProject", "" + productDefinitionProject); //$NON-NLS-1$ //$NON-NLS-2$
		Element generatedCodeEl = doc.createElement(GENERATED_CODE_TAG_NAME);
		projectEl.appendChild(generatedCodeEl);
		generatedCodeEl.setAttribute("docLanguage", javaSrcLanguage.toString()); //$NON-NLS-1$
		generatedCodeEl.setAttribute("changesInTimeNamingConvention", changesInTimeConventionIdForGeneratedCode); //$NON-NLS-1$
		Element builderSetEl = doc.createElement(IIpsArtefactBuilderSet.XML_ELEMENT);
		projectEl.appendChild(builderSetEl);
		builderSetEl.setAttribute("id", builderSetId); //$NON-NLS-1$
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
        runtimeIdPrefix = element.getAttribute("runtimeIDPrefix"); //$NON-NLS-1$
        
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
        initProductCmptNamingStrategyFromXml(XmlUtil.getFirstElement(element, IProductCmptNamingStrategy.XML_TAG_NAME));
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
	
	private void initProductCmptNamingStrategyFromXml(Element el) {
		productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
		if (el!=null) {
        	String id = el.getAttribute("id"); //$NON-NLS-1$
        	if (id.equals(DateBasedProductCmptNamingStrategy.EXTENSION_ID)) {
        		productCmptNamingStrategy = new DateBasedProductCmptNamingStrategy();
        	}
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
	
	public String getRuntimeIdPrefix() {
		return this.runtimeIdPrefix;
	}
}
