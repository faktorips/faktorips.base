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

package org.faktorips.devtools.core.model;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;



/**
 * Project to edit IPS objects. 
 */
public interface IIpsProject extends IIpsElement, IProjectNature {

    public final static String NATURE_ID = IpsPlugin.PLUGIN_ID + ".ipsnature"; //$NON-NLS-1$
    
    /**
     * Returns the corresponding platform project.
     */
    public IProject getProject();
    
    /**
     * Returns the corresponding Java project.
     */
    public IJavaProject getJavaProject();
    
    /**
     * Returns the charset/encoding in that the IIpsSrcFile contents is stored.
     */
    public String getXmlFileCharset();
    
    /**
     * Returns a copy of the project's object path. Note that a copy and not a reference is returned. If you want
     * to update the project's path, the updated object path has to b e explicitly set on the project via the
     * <code>setIpsObjectPath()</code> method.
     */
    public IIpsObjectPath getIpsObjectPath() throws CoreException;
    
    /**
     * Sets the id of the current artefact builder.
     */
    public void setCurrentArtefactBuilderSet(String id) throws CoreException;
    
    /**
     * Sets the new object path.
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException;
    
    /**
     * Set the value datatypes allowed in the project.
     */
    public void setValueDatatypes(String[] ids) throws CoreException;
    
    /**
     * Set the value datatypes allowed in the project.
     */
    public void setValueDatatypes(ValueDatatype[] types) throws CoreException;

    /**
     * Returns the language in that the expression language's functions are used.
     * E.g. the <code>if</code> function is called IF in english, but WENN in german.
     */
    public Locale getExpressionLanguageFunctionsLanguage();
    
    /**
     * Returns the language (as a locale) in that the generated Java sourcecode
     * is documented. 
     * <p>
     * E.g. in English we could generate getNumOfCoverages(), in German we could 
     * generate getAnzahlCoverages().
     */
    public Locale getGeneratedJavaSourcecodeDocumentationLanguage();
    
    /**
     * Sets the language (as a locale) in that the generated Java sourcecode
     * is documented. 
     */
    public void setGeneratedJavaSourcecodeDocumentationLanguage(Locale locale);
    
    /**
     * Returns the naming convention for changes over time used in the generated Java sourcecode.
     */
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode();
    
    /**
     * Returns <code>true</code> if this project contains a model defininition,
     * otherwise <code>false</code>.
     */
    public boolean isModelProject();

    /**
     * Sets if this is project contains a model defininition,
     * @throws CoreException 
     */
    public void setModelProject(boolean flag) throws CoreException;

    /**
     * Returns <code>true</code> if this project contains a product defininition
     * (that means it contains product components), otherwise <code>false</code>.
     */
    public boolean isProductDefinitionProject();
    
    /**
     * Sets if this is project contains a product defininition,
     * @throws CoreException 
     */
    public void setProductDefinitionProject(boolean flag) throws CoreException;

    /**
     * Returns the root folder with the indicated name.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name);
    
    /**
     * Returns the project's package fragment roots or an empty array 
     * if none is found.
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException;
    
    /**
     * Returns the project's package fragment roots contains source code
     * or an empty array if none is found.
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException;
    
    /**
     * Returns the first object with the indicated type and qualified name found
     * on the objectpath. 
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException;

    /**
     * Returns the first object with the indicated qualified name type found
     * on the objectpath. 
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException;

    /**
     * Returns the first policy component type the qualified name found
     * on the path. 
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException;
    
    /**
     * Returns the first policy component type the qualified name found
     * on the path. 
     */
    public IProductCmptType findProductCmptType(String qualifiedName) throws CoreException;

    /**
     * Returns all objects of the given type found on the classpath. 
     */
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException;
    
    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     */
    public void findAllIpsObjects(List result) throws CoreException;

    /**
     * Returns all objects of the given type starting with the given prefix found on the ipsobject path.
     */
    public IIpsObject[] findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase) throws CoreException;
    
    /**
     * Returns all product components that are based on the given policy component type
     * (either directly or because they are based on a subtype of the given
     * type). If qualifiedTypeName is null, the method returns all product
     * components found on the classpath.
     * 
     * @param pcTypeName The qualified name of the policy component type, product components are searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based on subtypes
     * of the given policy component are returned, otherwise only product components that are directly based
     * on the given type are returned.
     */
    public IProductCmpt[] findProductCmpts(String qualifiedPcTypeName, boolean includeSubtypes) throws CoreException;
    
    /**
     * Returns the product component with the given runtime id or <code>null</code> if no such
     * product component exists. If more than one product component with the given id exists, the
     * first one found is returned. 
     * 
     * @param runtimeId The runtime-id to find the product component for.
     * @throws CoreException if an error occurs during search.
     */
    public IProductCmpt findProductCmpt(String runtimeId) throws CoreException;
    
    /**
     * Returns all product component generation that refer to the product component identified by the
     * given qualified name. Returns an empty array if none is found.
     * 
     * @throws CoreException if an exception occurs while searching.
     */
    public IProductCmptGeneration[] findReferencingProductCmptGenerations(String qualifiedProductCmptName) throws CoreException;
    
    /**
     * Returns the datatypes representing values. If this project depends on other ips projects
     * the datatypes from the referenced projects are also returned, but each datatype is
     * returned only once.
     * 
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid);

    /**
     * Returns all datatypes accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value datatypes should be returned.
     * @param includeVoid    true if <code>Datatype.VOID</code> should be included.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid) throws CoreException;
    
    /**
     * Returns all enumeration datatypes accessible on the project's path.
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public EnumDatatype[] findEnumDatatypes() throws CoreException;
    
    /**
     * Returns the enumeration datatype accessible on the projects's path with the specified qualifiedName. 
     * If none is found null will be returned.
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public EnumDatatype findEnumDatatype(String qualifiedName) throws CoreException;
    
    /**
     * Returns the first datatype found on the path with the given qualified name.
     * Returns <code>null</code> if no datatype with the given name is found.
     *
     * @throws CoreException if an exception occurs while searching for the datatype. 
     */
    public Datatype findDatatype(String qualifiedName) throws CoreException;

    /**
     * Returns the first value datatype found on the path with the given qualified name.
     * Returns <code>null</code> if no value datatype with the given name is found.
     *
     * @throws CoreException if an exception occurs while searching for the datatype. 
     */
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException;
    
    /**
     * Returns the code generation helper for the given datatype or <code>null</code> if no helper is
     * available for the given datatype. 
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype);
    
    /**
     * Returns the code generation helper for the given datatype or <code>null</code> if no helper is
     * available for the given datatype.
     * 
     *  @param qName The qualified datatype name.
     *  
     *  @throws CoreException if an error occurs while searching for the datatype.
     */
    public DatatypeHelper findDatatypeHelper(String qName) throws CoreException;

    /**
     * Returns the possible value set types that are defined for the given datatype.
     * The type <code>ALL_VALUES</code> is always returned and is the first element in the array.
     * 
     * @throws CoreException if an error occurs while retrieving the value set types, possible reasons
     * are that the datatypes files can't be read or the xml can't be parsed.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code> or the datatype is not defined
     * in the project.
     */
    public ValueSetType[] getValueSetTypes(ValueDatatype datatype) throws CoreException;
    
    /**
     * Returns the <code>IpsArtefactBuilderSet</code> that is currently active for this project. If no
     * IpsArtefactBuilderSet is active for this project an <code>EmptyBuilderSet</code> is returned.
     */
    public IIpsArtefactBuilderSet getCurrentArtefactBuilderSet() throws CoreException;
    
    /**
     * Returns a Java code fragment that gives access to the FaktorIPS runtime repository.
     */
    public JavaCodeFragment getCodeToGetTheRuntimeRepository() throws CoreException;

    /**
     * Returns the runtime id prefix configured for this project.
     */
    public String getRuntimeIdPrefix();
    
    /**
     * Returns the runtime id evaluated for the given product component. It is <strong>
     * not</strong> ensured that the same id is returend for different calls.
     * 
     * @param productCmpt The product component to find the runtime id for.
     * @throws CoreException if an error occurs during evaluation.
     */
    public String getRuntimeId(IProductCmpt productCmpt) throws CoreException;
    
    /**
     * Returns the stratgey used to name product components.
     */
    public IProductCmptNamingStrategy getProductCmptNamingStratgey() throws CoreException;
    
	public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException;
    
}
