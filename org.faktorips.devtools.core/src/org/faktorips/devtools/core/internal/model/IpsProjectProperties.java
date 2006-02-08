package org.faktorips.devtools.core.internal.model;

import java.util.Locale;
import java.util.StringTokenizer;

import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IpsProjectProperties {

	public final static IpsProjectProperties createFromXml(IpsProject ipsProject, Element element) {
		IpsProjectProperties data = new IpsProjectProperties();
		data.initFromXml(ipsProject, element);
		return data;
	}
	
	final static String TAG_NAME = "IpsProject"; 
		
	private boolean modelProject;
	private boolean productDefinitionProject;
	private Locale javaSrcLanguage = Locale.ENGLISH;
	private String builderSetId = "";
	private IIpsObjectPath path = new IpsObjectPath();
	private String[] predefinedDatatypesUsed = new String[0];
    private DynamicValueDatatype[] definedDatatypes = new DynamicValueDatatype[0]; 

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
		projectEl.setAttribute("modelProject", "" + modelProject);
		projectEl.setAttribute("productDefinitionProject", "" + productDefinitionProject);
		projectEl.setAttribute("javaSrcLanguage", javaSrcLanguage.toString());
		Element builderSetEl = doc.createElement(IIpsArtefactBuilderSet.XML_ELEMENT);
		projectEl.appendChild(builderSetEl);
		builderSetEl.setAttribute("id", builderSetId);
		projectEl.appendChild(((IpsObjectPath)path).toXml(doc));
		
		// datatypes
		Element datatypesEl = doc.createElement("Datatypes");
		projectEl.appendChild(datatypesEl);
        Element predefinedTypesEl = doc.createElement("UsedPredefinedDatatypes");
        datatypesEl.appendChild(predefinedTypesEl);
		for (int i = 0; i < predefinedDatatypesUsed.length; i++) {
			Element datatypeEl = doc.createElement("Datatype");
			datatypeEl.setAttribute("id", predefinedDatatypesUsed[i]);
            predefinedTypesEl.appendChild(datatypeEl);
		}
		
		return projectEl;
	}
	
	public void initFromXml(IIpsProject ipsProject, Element element) {
        Element artefactEl = XmlUtil.getFirstElement(element, IIpsArtefactBuilderSet.XML_ELEMENT);
        modelProject = Boolean.valueOf(element.getAttribute("modelProject")).booleanValue();
        productDefinitionProject = Boolean.valueOf(element.getAttribute("productDefinitionProject")).booleanValue();
	    javaSrcLanguage = getLocale(element.getAttribute("javaSrcLanguage"));
        if(artefactEl != null) {
            builderSetId = artefactEl.getAttribute("id");
        } else {
        	builderSetId = "";
        }
        Element pathEl = XmlUtil.getFirstElement(element, IpsObjectPath.XML_TAG_NAME);
        if (pathEl != null) {
            path = IpsObjectPath.createFromXml(ipsProject, pathEl);
        } else {
        	path = new IpsObjectPath();
        }
        Element datatypesEl = XmlUtil.getFirstElement(element, "Datatypes");
        if (datatypesEl==null) {
        	predefinedDatatypesUsed = new String[0];
            definedDatatypes = new DynamicValueDatatype[0];
        	return;
        }
        initUsedPredefinedDatatypesFromXml(XmlUtil.getFirstElement(datatypesEl, "UsedPredefinedDatatypes"));
        initDefinedDatatypesFromXml(ipsProject, XmlUtil.getFirstElement(datatypesEl, "DatatypeDefinitions"));
	}

    private void initUsedPredefinedDatatypesFromXml(Element element) {
        if (element==null) {
            predefinedDatatypesUsed = new String[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype");
        predefinedDatatypesUsed = new String[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            predefinedDatatypesUsed[i] = ((Element)nl.item(i)).getAttribute("id");
        }
    }
    
    private void initDefinedDatatypesFromXml(IIpsProject ipsProject, Element element) {
        if (element==null) {
            definedDatatypes = new DynamicValueDatatype[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype");
        definedDatatypes = new DynamicValueDatatype[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            definedDatatypes[i] = DynamicValueDatatype.createFromXml(ipsProject, (Element)nl.item(i));
        }
    }

    static Locale getLocale(String s) {
    	StringTokenizer tokenzier = new StringTokenizer(s, "_");
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
}
