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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IpsBuilder;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class IpsProject extends IpsElement implements IIpsProject {

    /**
     * Constructor needed for <code>IProject.getNature()</code> and
     * <code>IProject.addNature()</code>.
     * 
     * @see #setProject(IProject)
     */
    public IpsProject() {
    }

    IpsProject(IIpsModel model, String name) {
        super(model, name);
    }

    public IProject getProject() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
    
    /**
     * Returns the file that stores the project's properties. Note that the file need not exist.
     */
    public IFile getIpsProjectPropertiesFile() {
    	return getProject().getFile(".ipsproject"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public IJavaProject getJavaProject() {
        return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(getName());
    }

    /**
     * Overridden.
     */
    public String getXmlFileCharset() {
        return "UTF-8"; //$NON-NLS-1$
    }
    
    /**
     * Overridden.
     */
    public boolean isModelProject() {
		return getProperties().isModelProject();
	}

    /**
     * Overridden
     * @throws CoreException 
     */
	public void setModelProject(boolean flag) throws CoreException {
    	IpsProjectProperties properties = getProperties();
    	properties.setModelProject(flag);
    	saveProjectProperties(properties);
	}

    /**
     * Overridden
     */
	public boolean isProductDefinitionProject() {
		return getProperties().isProductDefinitionProject();
	}

	/**
	 * Overridden
	 */
	public void setProductDefinitionProject(boolean flag) throws CoreException {
    	IpsProjectProperties properties = getProperties();
    	properties.setProductDefinitionProject(flag);
    	saveProjectProperties(properties);
	}

	/**
     * Overridden
     */
    public IIpsObjectPath getIpsObjectPath() throws CoreException {
    	return getProperties().getIpsObjectPath();
    }

    /**
     * Overridden.
     */
    public void setCurrentArtefactBuilderSet(String id) throws CoreException {
    	IpsProjectProperties properties = getProperties();
    	properties.setBuilderSetId(id);
    	saveProjectProperties(properties);
    }
    
    /**
     * Overridden.
     */
    public void setValueDatatypes(String[] ids) throws CoreException {
        IpsProjectProperties properties = getProperties();
        properties.setPredefinedDatatypesUsed(ids);
        saveProjectProperties(properties);
    }
    
    /**
     * Overridden.
     */
    public void setValueDatatypes(ValueDatatype[] types) throws CoreException {
    	String[] ids = new String[types.length];
    	for (int i = 0; i < types.length; i++) {
			ids[i] = types[i].getQualifiedName();
		}
		setValueDatatypes(ids);
	}
    
	/**
     * Saves the project properties to the .ipsproject file.
     * 
     * @throws CoreException if an error occurs while saving the data.
     */
    private void saveProjectProperties(IpsProjectProperties properties) throws CoreException {
    	Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
    	Element propertiesEl = properties.toXml(doc);
        IFile file = getIpsProjectPropertiesFile();
        String charset = getXmlFileCharset();
        String contents;
        try {
            contents = XmlUtil.nodeToString(propertiesEl, charset);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Error tranforming project data to xml string", e)); //$NON-NLS-1$
        }
        ByteArrayInputStream is;
        try {
            is = new ByteArrayInputStream(contents.getBytes(charset));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("Error creating byte stream", e)); //$NON-NLS-1$
        }
        if (file.exists()) {
            file.setContents(is, true, true, null);
        } else {
            file.create(is, true, null);
        }
    }
    
    /**
     * Overridden.
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException {
    	IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    	properties.setIpsObjectPath(newPath);
    	saveProjectProperties(properties);
    }

    /**
     * Overridden.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name) {
        return new IpsPackageFragmentRoot(this, name);
    }

    /**
     * Overridden.
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException {
        List roots = new ArrayList();
        IIpsObjectPathEntry[] entries = getIpsObjectPath().getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] instanceof IIpsSrcFolderEntry) {
                roots.add(((IIpsSrcFolderEntry)entries[i]).getIpsPackageFragmentRoot(this));
            }
        }
        return (IIpsPackageFragmentRoot[])roots.toArray(new IIpsPackageFragmentRoot[roots.size()]);
    }

    /**
     * Overridden.
     */
    public boolean exists() {
        return getCorrespondingResource().exists();
    }

    /**
     * Overridden.
     */
    public Locale getExpressionLanguageFunctionsLanguage() {
        return Locale.GERMAN;
    }

    /**
     * Overridden.
     */
    public Locale getGeneratedJavaSourcecodeDocumentationLanguage() {
    	IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    	return properties.getJavaSrcLanguage();
    }

    /**
     * Sets the generated java source code documentation language. 
     */
    public void setGeneratedJavaSourcecodeDocumentationLanguage(Locale locale) {
    	IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    	properties.setJavaSrcLanguage(locale);
    }

    /**
     * {@inheritDoc}
     */
	public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode() {
    	IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    	return getIpsModel().getChangesOverTimeNamingConvention(properties.getChangesInTimeConventionIdForGeneratedCode());
	}

	/**
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsProject.gif"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public IResource getCorrespondingResource() {
        return getProject();
    }

    /**
     * Overridden.
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragmentRoots();
    }

    /**
     * Overridden.
     */
    public IIpsProject getIpsProject() {
        return this;
    }

    /**
     * Overridden.
     * 
     * @see org.eclipse.core.resources.IProjectNature#configure()
     */
    public void configure() throws CoreException {
        IProjectDescription description = getProject().getDescription();
        ICommand command = getPdBuildCommand();
        if (command == null) {
            // Add a product definition build command to the build spec
            ICommand newBuildCommand = description.newCommand();
            newBuildCommand.setBuilderName(IpsBuilder.BUILDER_ID);
            addCommandAtFirstPosition(description, newBuildCommand);
        }
    }

    /**
     * Overridden.
     * 
     * @see org.eclipse.core.resources.IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException {
    }

    /**
     * Overridden.
     * 
     * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
        this.name = project.getName();
    }

    /**
     * Finds the specific command for product definition builder.
     */
    private ICommand getPdBuildCommand() throws CoreException {
        ICommand[] commands = getProject().getDescription().getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(IpsBuilder.BUILDER_ID)) {
                return commands[i];
            }
        }
        return null;
    }

    /*
     * Adds the command to the build spec
     */
    private void addCommandAtFirstPosition(IProjectDescription description, ICommand newCommand)
            throws CoreException {

        ICommand[] oldCommands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[oldCommands.length + 1];
        System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
        newCommands[0] = newCommand;
        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
    }

    /**
     * Overridden.
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPath()).findIpsObject(this, nameType);
    }

    /**
     * Overridden.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPath()).findIpsObject(this, type, qualifiedName);
    }

    /**
     * Overridden.
     */
    public IIpsObject[] findIpsObjectsStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase) throws CoreException {
        ArrayList result = new ArrayList();
        findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }

    /**
     * Searches all objects of the given type starting with the given prefix found on the project's
     * path and adds them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the objects.
     */
    public void findIpsObjectsStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result) throws CoreException {
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjectsStartingWith(this, type, prefix,
            ignoreCase, result);
    }

    /**
     * Overridden.
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException {
        return (IPolicyCmptType)findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }
    
    /**
	 * Overridden.
	 */
	public IProductCmptType findProductCmptType(String qualifiedName) throws CoreException {
		return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
	}

	/**
     * Overridden.
     */
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, type);
    }

    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     * @throws CoreException
     */
    public void findAllIpsObjects(List result) throws CoreException{
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, IpsObjectType.POLICY_CMPT_TYPE, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, IpsObjectType.PRODUCT_CMPT, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, IpsObjectType.TABLE_STRUCTURE, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, IpsObjectType.TABLE_CONTENTS, result);
        ((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, IpsObjectType.BUSINESS_FUNCTION, result);
    }
    
    /**
     * Finds all ips objects of the given type in the project and adds them to the result.
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
    	((IpsObjectPath)getIpsObjectPath()).findIpsObjects(this, type, result);
    }

    /**
     * Overridden.
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid) {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, result);
        return (ValueDatatype[])result.toArray(new ValueDatatype[result.size()]);
    }

    private void getValueDatatypes(boolean includeVoid, Set result) {
        if (includeVoid) {
            result.add(Datatype.VOID);
        }
        getValueDatatypes(this, result, new HashSet());
    }
    
    private void getValueDatatypes(IIpsProject ipsProject, Set result, Set visitedProjects){
        try {
            ((IpsModel)getIpsModel()).getValueDatatypes(ipsProject, result);
            IIpsProject[] projects = ipsProject.getIpsObjectPath().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                if(!visitedProjects.contains(projects[i])){
                	visitedProjects.add(projects[i]);
                	getValueDatatypes(projects[i], result, visitedProjects);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Overridden.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid)
            throws CoreException {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, result);
        if (!valuetypesOnly) {
            List refDatatypes = new ArrayList();
            findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, refDatatypes);
            result.addAll(refDatatypes);
        }
        Datatype[] array = new Datatype[result.size()];
        result.toArray(array);
        return array;
    }

    /**
     * {@inheritDoc}
     */
    public EnumDatatype[] findEnumDatatypes() throws CoreException{
    	//TODO this implementation can be improved and instanceof can be avoided. Therefore the storage of EnumDatatypes an Datatypes
    	//has to be separated within the IpsModel class
    	Datatype[] datatypes = findDatatypes(true, false);
    	ArrayList enumDatatypeList = new ArrayList();
    	for (int i = 0; i < datatypes.length; i++) {
			if(datatypes[i] instanceof EnumDatatype){
				enumDatatypeList.add(datatypes[i]);
			}
		}
    	return (EnumDatatype[])enumDatatypeList.toArray(new EnumDatatype[enumDatatypeList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public EnumDatatype findEnumDatatype(String qualifiedName) throws CoreException{
    	EnumDatatype[] allEnums = findEnumDatatypes();
    	for (int i = 0; i < allEnums.length; i++) {
			if(allEnums[i].getQualifiedName().equals(qualifiedName)){
				return allEnums[i];
			}
		}
    	return null;
    }
    
    /**
     * Overridden.
     */
    public Datatype findDatatype(String qualifiedName) throws CoreException {
        Datatype[] datatypes = findDatatypes(false, true);
        for (int i = 0; i < datatypes.length; i++) {
            if (datatypes[i].getQualifiedName().equals(qualifiedName)) {
                return datatypes[i];
            }
        }
        return null;
    }

    /**
     * Overridden.
     */
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException {
        // TODO Jan. Testfall, noch mal pr?fen.
        return (ValueDatatype)findDatatype(qualifiedName);
    }

    /**
     * Overridden.
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (!(datatype instanceof ValueDatatype)) {
            return null;
        }
        DatatypeHelper helper = ((IpsModel)getIpsModel()).getDatatypeHelper(this,
            (ValueDatatype)datatype);
        if (helper != null) {
            return helper;
        }
        try {
            IIpsProject[] projects = getIpsObjectPath().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                helper = ((IpsModel)getIpsModel()).getDatatypeHelper(projects[i],
                    (ValueDatatype)datatype);
                if (helper != null) {
                    return helper;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public DatatypeHelper findDatatypeHelper(String qName) throws CoreException {
		Datatype datatype = findDatatype(qName);
		return getDatatypeHelper(datatype);
	}

	/**
     * Overridden.
     */
    public ValueSetType[] getValueSetTypes(ValueDatatype datatype) throws CoreException {
        ArgumentCheck.notNull(datatype);
        if (datatype instanceof EnumDatatype) {
            return new ValueSetType[] { ValueSetType.ALL_VALUES, ValueSetType.ENUM };
        }
        return ValueSetType.getValueSetTypes();
    }

    /**
     * Overridden.
     */
    public IProductCmpt[] findProductCmpts(String qualifiedTypeName, boolean includeSubytpes)
            throws CoreException {
        List result = new ArrayList();
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            ((IpsPackageFragmentRoot)roots[i]).findProductCmpts(qualifiedTypeName,
                includeSubytpes, result);
        }
        IProductCmpt[] array = new IProductCmpt[result.size()];
        result.toArray(array);
        return array;
    }

    /**
     * Overridden.
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException {
        List result = new ArrayList();
        getSourcePdPckFragmentRoots(result);
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    void getSourcePdPckFragmentRoots(List result) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].containsSourceFiles()) {
                result.add(roots[i]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSet getCurrentArtefactBuilderSet() throws CoreException {
        return ((IpsModel)getIpsModel()).getCurrentIpsArtefactBuilderSet(this);
    }

    /**
     * Overridden.
     */
    public JavaCodeFragment getCodeToGetTheRuntimeRepository() throws CoreException {
        return new JavaCodeFragment("null"); // TODO must read from ipsproject file. //$NON-NLS-1$
    }
    
    private IpsProjectProperties getProperties() {
    	return ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    }

    /**
     * Overridden
     */
	public IProductCmptGeneration[] findReferencingProductCmptGenerations(String qualifiedProductCmptName) throws CoreException {
		ArrayList result = new ArrayList();
		IIpsObject[] allProductCmpts = this.findIpsObjects(IpsObjectType.PRODUCT_CMPT);
		
		for (int i = 0; i < allProductCmpts.length; i++) {
			IProductCmptGeneration generation = (IProductCmptGeneration)((IProductCmpt)allProductCmpts[i]).findGenerationEffectiveOn(IpsPreferences.getWorkingDate());
			if (generation == null) {
				// it is possible have the working date set to a date in the past
				// where no generation exists for a product cmpt. In this case,
				// we ignore this product cmpt.
				continue;
			}
			IProductCmptRelation[] relations = generation.getRelations();
			for (int j = 0; j < relations.length; j++) {
				if (relations[j].getTarget().equals(qualifiedProductCmptName)) {
					result.add(generation);
					break;
				}
			}
		}
		IProductCmptGeneration[] resultArray = new IProductCmptGeneration[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}

	public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException  {
		getProperties().addDefinedDataType(newDatatype);
		saveProjectProperties(getProperties());
	}
    
    
}
