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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.codegen.DatatypeHelper;
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
import org.faktorips.util.message.MessageList;



/**
 * Project to develop IPS objects. 
 */
public interface IIpsProject extends IIpsElement, IProjectNature {

    public final static String NATURE_ID = IpsPlugin.PLUGIN_ID + ".ipsnature"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the project's property file is missing.
     */
    public final static String MSGCODE_MISSING_PROPERTY_FILE = MSGCODE_PREFIX + "MissingPropertyFile"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the project's property file's contents is
     * not parsable.
     */
    public final static String MSGCODE_UNPARSABLE_PROPERTY_FILE = MSGCODE_PREFIX + "UnparsablePropertyFile"; //$NON-NLS-1$
    
    /**
     * Returns the corresponding platform project.
     */
    public IProject getProject();
    
    /**
     * Returns the corresponding Java project.
     */
    public IJavaProject getJavaProject();
    
    /**
     * Returns <code>true</code> if the project can be build / Java sourcecode can be generated.
     * Returns <code>false</code> otherwise. E. g. if the project's properties file is missing
     * the project can't be build.
     */
    public boolean canBeBuild();
    
    /**
     * Returns the project's properties. Note that the method returns a copy of the properties,
     * not a reference. In order to update the project's properties the modified properties
     * object has to be set in the project via setProperties().
     */
    public IIpsProjectProperties getProperties();
    
    /**
     * Sets the project's properties and stores the properties in the project's property file
     * (".ipsproject").
     * 
     * @throws CoreException if an error occurs while saving the properties to the file. 
     */
    public void setProperties(IIpsProjectProperties properties) throws CoreException;

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
     * Returns all output folders specified in the project's object path.
     */
    public IFolder[] getOutputFolders() throws CoreException;
    
    /**
     * Sets the id of the current artefact builder.
     * 
     * @deprecated use IIpsProjectProperties to change the project properties
     */
    public void setCurrentArtefactBuilderSet(String id) throws CoreException;
    
    /**
     * Sets the new object path.
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException;
    
    /**
     * Set the value datatypes allowed in the project.
     * 
     * @deprecated use IIpsProjectProperties to change the project properties
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
     * Returns the naming convention for changes over time used in the generated Java sourcecode.
     */
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode();
    
    /**
     * Returns <code>true</code> if this project contains a model defininition,
     * otherwise <code>false</code>.
     */
    public boolean isModelProject();

    /**
     * Returns <code>true</code> if this project contains a product defininition
     * (that means it contains product components), otherwise <code>false</code>.
     */
    public boolean isProductDefinitionProject();
    
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
     * Returns the product component with the given qualified name or <code>null</code> if no such
     * product component exists. If more than one product component with the given id exists, the
     * first one found is returned. 
     * 
     * @param qualifiedName the name to find the product component for
     * @throws CoreException if an error occurs during search.
     */
    public IProductCmpt findProductCmptByQualifiedName(String qualifiedName) throws CoreException;

    /**
     * Returns all product component generation that refer to the product component identified by the
     * given qualified name. Returns an empty array if none is found.
     * 
     * @throws CoreException if an exception occurs while searching.
     */
    public IProductCmptGeneration[] findReferencingProductCmptGenerations(String qualifiedProductCmptName) throws CoreException;

    /**
     * Returns the supertype of the given policy component type, and all policy component types 
     * that refer to the given policy component type. Returns an empty array if no references or 
     * supertypes are found.
     * 
     * @throws CoreException if an exception occurs while searching.
     */
    public IPolicyCmptType[] findReferencingPolicyCmptTypes(IPolicyCmptType pcType) throws CoreException;
    
    /**
     * Returns the datatypes representing values. If this project depends on other ips projects
     * the datatypes from the referenced projects are also returned, but each datatype is
     * returned only once.
     * 
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid);

    /**
     * Returns the datatypes representing values. If this project depends on other ips projects
     * the datatypes from the referenced projects are also returned, but each datatype is
     * returned only once.
     * 
     * @param includeVoid <code>true</code> if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives <code>true</code> if primitive datatypes should be returned.
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid, boolean includePrimitives);

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
     * Returns all datatypes accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value datatypes should be returned.
     * @param includeVoid    true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive Datatypes are included.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives) throws CoreException;

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
     * @throws NullPointerException if datatype is <code>null</code> or the datatype is not defined
     * in the project.
     */
    public ValueSetType[] getValueSetTypes(ValueDatatype datatype) throws CoreException;
    
    /**
     * Returns the <code>IpsArtefactBuilderSet</code> that is currently active for this project. If no
     * IpsArtefactBuilderSet is active for this project an <code>EmptyBuilderSet</code> is returned.
     */
    public IIpsArtefactBuilderSet getArtefactBuilderSet() throws CoreException;
    
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
     * Returns the stratgey used to name product components. This method never returns
     * <code>null</code>.
     */
    public IProductCmptNamingStrategy getProductCmptNamingStratgey() throws CoreException;
    
    /**
     * Adds a new DynamicValueDataType to the Project at runtime
     * 
     * @param newDatatype
     * @throws CoreException
     * 
     * @deprecated use IIpsProjectProperties to change the project properties
     */
    public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException;
    
    /**
     * Validates the project and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;
    
}
