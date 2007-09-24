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

package org.faktorips.devtools.core;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.devtools.core.internal.model.DynamicEnumDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.IpsProjectProperties;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype2.ProductCmptType;
import org.faktorips.devtools.core.internal.model.testcase.TestObject;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.test.XmlAbstractTestCase;
import org.faktorips.devtools.core.ui.binding.BeanUtil;
import org.faktorips.devtools.core.ui.editors.pctype.ContentsChangeListenerForWidget;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.StringUtil;

/**
 * Base class for all plugin test cases. Has a factory method to create an ips
 * project including the underlying platform project.
 *
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPluginTest extends XmlAbstractTestCase {

	/**
	 *
	 */
	public AbstractIpsPluginTest() {
		super();
	}

	/**
	 * @param name
	 */
	public AbstractIpsPluginTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
	    ((IpsModel)IpsPlugin.getDefault().getIpsModel()).stopListeningToResourceChanges();
        IpsPlugin.getDefault().setFeatureVersionManagers(new IIpsFeatureVersionManager[]{new TestIpsFeatureVersionManager()});
        setAutoBuild(false);

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Start deleting projects.");
                }
                waitForIndexer();
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                        .getProjects();
                for (int i = 0; i < projects.length; i++) {
                    projects[i].close(null);
                    projects[i].delete(true, true, null);
                }
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Projects deleted.");
                }
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
			}
		};
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE,
				null);

	}

    protected void createArchive(IIpsProject projectToArchive, IFile archiveFile) throws Exception {
        File file = createFileIfNecessary(archiveFile);

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(projectToArchive, file);
        ResourcesPlugin.getWorkspace().run(op, null);

        createLinkIfNecessary(archiveFile, file);
    }

    protected void createArchive(IIpsPackageFragmentRoot rootToArchive, IFile archiveFile) throws Exception {
        File file = createFileIfNecessary(archiveFile);

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(rootToArchive, file);
        ResourcesPlugin.getWorkspace().run(op, null);

        createLinkIfNecessary(archiveFile, file);
    }

    /*
     * Creates a links to the given file in the workspace.
     */
    protected void createLinkIfNecessary(IFile archiveFile, File file) throws CoreException {
        if (! archiveFile.isLinked() && ! archiveFile.exists()){
            archiveFile.createLink(new Path(file.getAbsolutePath()), 0, new NullProgressMonitor());
        }
    }

    protected File createFileIfNecessary(IFile archiveFile) {
        File file = null;
        if (archiveFile.exists()){
            file = archiveFile.getLocation().toFile();
        } else {
            file = archiveFile.getLocation().toFile();
        }
        return file;
    }

    /**
     * Creates a new IpsProject.
     */
    protected IIpsProject newIpsProject() throws CoreException {
        return newIpsProject("TestProject");
    }

	/**
	 * Creates a new IpsProject.
	 */
	protected IIpsProject newIpsProject(final String name) throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProject project = newPlatformProject(name);
				addJavaCapabilities(project);
				addIpsCapabilities(project);
			}
		};
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE,
				null);

		return IpsPlugin.getDefault().getIpsModel().getIpsProject(name);
	}

	/**
	 * Creates a new platfrom project with the given name and opens it.
	 *
	 * @throws CoreException
	 */
	protected IProject newPlatformProject(final String name)
			throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				internalNewPlatformProject(name);
			}
		};
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE,
				null);
		return workspace.getRoot().getProject(name);
	}

	/**
	 * Creates a new platfrom project with the given name and opens it.
	 *
	 * @throws CoreException
	 */
	private IProject internalNewPlatformProject(final String name)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		project.create(null);
		project.open(null);
		return project;
	}

	/**
	 * Creates a new Java Project for the given platform project.
	 */
	protected IJavaProject addJavaCapabilities(IProject project)
			throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		// add Java nature
		Util.addNature(project, JavaCore.NATURE_ID);
		// create bin folder and set as output folder.
		IFolder binFolder = project.getFolder("bin");
		if (!binFolder.exists()) {
			binFolder.create(true, true, null);
		}
		IFolder srcFolder = project.getFolder("src");
		javaProject.setOutputLocation(binFolder.getFullPath(), null);
		if (!srcFolder.exists()) {
			srcFolder.create(true, true, null);
		}
		IFolder extFolder = project.getFolder("extension");
		if (!extFolder.exists()) {
			extFolder.create(true, true, null);
		}
		IPackageFragmentRoot srcRoot = javaProject.getPackageFragmentRoot(srcFolder);
		IPackageFragmentRoot extRoot = javaProject.getPackageFragmentRoot(extFolder);
		IClasspathEntry[] entries = new IClasspathEntry[2];
		entries[0] = JavaCore.newSourceEntry(srcRoot.getPath());
		entries[1] = JavaCore.newSourceEntry(extRoot.getPath());
		// set classpath
		javaProject.setRawClasspath(entries, null);
		addSystemLibraries(javaProject);
		return javaProject;
	}

    protected void addClasspathEntry(IJavaProject project, IClasspathEntry entry) throws JavaModelException {
        IClasspathEntry[] entries = project.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length+1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[entries.length] = entry;
        project.setRawClasspath(newEntries, null);
    }

	protected void addIpsCapabilities(IProject project) throws CoreException {
		Util.addNature(project, IIpsProject.NATURE_ID);
		IFolder rootFolder = project.getFolder("productdef");
		rootFolder.create(true, true, null);
		IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel()
				.getIpsProject(project.getName());
		IIpsObjectPath path = ipsProject.getIpsObjectPath();
		path.setOutputDefinedPerSrcFolder(true);
		IIpsSrcFolderEntry entry = path.newSourceFolderEntry(rootFolder);
		entry
				.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.model");
		entry.setSpecificOutputFolderForMergableJavaFiles(project
				.getFolder("src"));
		entry
				.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.model");
		entry.setSpecificOutputFolderForDerivedJavaFiles(project
				.getFolder("extension"));
		ipsProject.setIpsObjectPath(path);

		// TODO: wichtig dies erzeugt eine Abhaengigkeit vom StdBuilder Projekt.
		// Dies muss ueberarbeitet werden
		IIpsProjectProperties props = ipsProject.getProperties();
		props.setBuilderSetId("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
        props.setLoggingFrameworkConnectorId("org.faktorips.devtools.core.javaUtilLoggingConnector");
		props.setPredefinedDatatypesUsed(new String[] { "Decimal", "Money", "Integer",
				"String", "Boolean" });
		props.setJavaSrcLanguage(Locale.GERMAN);

        props.setMinRequiredVersionNumber("org.faktorips.feature", (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ipsProject.setProperties(props);

		ipsProject.setProperties(props);
	}

	private void addSystemLibraries(IJavaProject javaProject)
			throws JavaModelException {
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaRuntime
				.getDefaultJREContainerEntry();
		javaProject.setRawClasspath(newEntries, null);
	}

	private void waitForIndexer() throws JavaModelException {
	    SearchEngine engine = new SearchEngine();
	    engine.searchAllTypeNames(
	            new char[] {},
	            new char[] {},
	            SearchPattern.R_EXACT_MATCH,
	            IJavaSearchConstants.CLASS,
	            SearchEngine.createJavaSearchScope(new IJavaElement[0]),
				new TypeNameRequestor() {},
	    		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
	}

	protected void setAutoBuild(boolean autoBuild) throws CoreException {
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace()
				.getDescription();
		if (autoBuild != ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			description.setAutoBuilding(autoBuild);
			ResourcesPlugin.getWorkspace().setDescription(description);
		}
	}

	/**
	 * Creates a new ipsobject in the indicated project's first source folder.
	 * If the qualifiedName includes a package name, the package is created if
	 * it does not already exists.
	 *
	 * @throws CoreException
	 */
	protected IIpsObject newIpsObject(IIpsProject project, IpsObjectType type,
			String qualifiedName) throws CoreException {
		IIpsPackageFragmentRoot root = project
				.getSourceIpsPackageFragmentRoots()[0];
		return newIpsObject(root, type, qualifiedName);
	}

	/**
	 * Creates a new ipsobject in the indicated package fragment root. If the
	 * qualifiedName includes a package name, the package is created if it does
	 * not already exists.
	 *
	 * @throws CoreException
	 */
	protected IIpsObject newIpsObject(
            final IIpsPackageFragmentRoot root,
			final IpsObjectType type,
            final String qualifiedName) throws CoreException {

	    return newIpsObject(root, type, qualifiedName, true);
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does
     * not already exists.
     *
     * @throws CoreException
     */
    private IIpsObject newIpsObject(
            final IIpsPackageFragmentRoot root,
            final IpsObjectType type,
            final String qualifiedName, final boolean createAutoProductCmptType) throws CoreException {

        final String packName = StringUtil.getPackageName(qualifiedName);
        final String unqualifiedName = StringUtil.unqualifiedName(qualifiedName);
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
                if (!pack.exists()) {
                    pack = root.createPackageFragment(packName, true, null);
                }
                IIpsSrcFile file = pack
                        .createIpsFile(type, unqualifiedName, true, null);
                IIpsObject ipsObject = file.getIpsObject();
                if (createAutoProductCmptType && ipsObject instanceof IPolicyCmptType) {
                    ((IPolicyCmptType) ipsObject)
                            .setConfigurableByProductCmptType(true);
                    ((IPolicyCmptType) ipsObject)
                            .setUnqualifiedProductCmptType(unqualifiedName
                                    + "ProductCmpt");
                    newProductCmptType(root, qualifiedName + "ProductCmpt");
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
        IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
        return pack.getIpsSrcFile(type.getFileName(unqualifiedName)).getIpsObject();
    }

	/**
	 * Creates a new policy component type in the indicated package fragment root. If the qualifiedName includes a
	 * package name, the package is created if it does not already exists.
	 *
	 * @throws CoreException
	 */
	protected PolicyCmptType newPolicyCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName) throws CoreException {
	    return (PolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
	}

	/**
	 * Creates a new policy component type in the project's first package fragment root.
	 * If the qualifiedName includes a package name, the package is created if it does not already exists.
	 *
	 * @throws CoreException
	 */
	protected PolicyCmptType newPolicyCmptType(IIpsProject project, String qualifiedName) throws CoreException {
		return (PolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
	}

    /**
     * Creates a new policy component type in the project's first package fragment root. Does not
     * create a product component type.
     * If the qualifiedName includes a package name, the package is created if it does not already exists.
     *
     * @throws CoreException
     */
    protected PolicyCmptType newPolicyCmptTypeWithoutProductCmptType(IIpsProject project, String qualifiedName) throws CoreException {
        return (PolicyCmptType)newIpsObject(project.getIpsPackageFragmentRoots()[0], IpsObjectType.POLICY_CMPT_TYPE, qualifiedName, false);
    }

    /**
     * Creates a new product component type in the project's first package fragment root.
     * If the qualifiedName includes a package name, the package is created if it does not already exists.
     *
     * @throws CoreException
     */
    protected ProductCmptType newProductCmptType(IIpsProject project, String qualifiedName) throws CoreException {
        return (ProductCmptType)newIpsObject(project, IpsObjectType.PRODUCT_CMPT_TYPE_V2, qualifiedName);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the qualifiedName includes a
     * package name, the package is created if it does not already exists.
     *
     * @throws CoreException
     */
    protected ProductCmptType newProductCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName) throws CoreException {
        return (ProductCmptType)newIpsObject(root, IpsObjectType.PRODUCT_CMPT_TYPE_V2, qualifiedName);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the qualifiedName includes a
     * package name, the package is created if it does not already exists.
     *
     * @throws CoreException
     */
    protected PolicyCmptType newPolicyAndProductCmptType(IIpsProject project, String policyCmptTypeName, String productCmptTypeName) throws CoreException {
        IPolicyCmptType policyCmptType = (IPolicyCmptType)newIpsObject(project.getIpsPackageFragmentRoots()[0], IpsObjectType.POLICY_CMPT_TYPE, policyCmptTypeName, false);
        ProductCmptType productCmptType = newProductCmptType(project, productCmptTypeName);
        productCmptType.setPolicyCmptType(policyCmptTypeName);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setUnqualifiedProductCmptType(QNameUtil.getUnqualifiedName(productCmptTypeName));
        policyCmptType.getIpsSrcFile().save(true, null);
        productCmptType.getIpsSrcFile().save(true, null);
        return (PolicyCmptType)policyCmptType;
    }

    /**
     * Creates a new product component that is based on the given product component type and has one generation
     * with it's valid from date set to the current working date.
     * The product component is stored in the same package fragment root as the type. 
     * If the qualifiedName includes a package name, the package is created if it does not already exists.
     *
     * @throws CoreException
     */
    protected ProductCmpt newProductCmpt(IProductCmptType type, String qualifiedName) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)newIpsObject(type.getIpsPackageFragment().getRoot(), IpsObjectType.PRODUCT_CMPT, qualifiedName);
        productCmpt.setProductCmptType(type.getQualifiedName());
        productCmpt.newGeneration(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        productCmpt.getIpsSrcFile().save(true, null);
        return (ProductCmpt)productCmpt;
    }

	/**
	 * Creates a new product component in the indicated package fragment root. If the qualifiedName includes a
	 * package name, the package is created if it does not already exists.
	 *
	 * @throws CoreException
	 */
	protected ProductCmpt newProductCmpt(IIpsPackageFragmentRoot root, String qualifiedName) throws CoreException {
	    return (ProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, qualifiedName);
	}

	/**
	 * Creates a new product component in the project's first package fragment root.
	 * If the qualifiedName includes a package name, the package is created if it does not already exists.
	 *
	 * @throws CoreException
	 */
	protected ProductCmpt newProductCmpt(IIpsProject project, String qualifiedName) throws CoreException {
		return (ProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, qualifiedName);
	}

	/**
	 * Creates a new ipsobject in the indicated package fragment root. If the
	 * qualifiedName includes a package name, the package is created if it does
	 * not already exists.
	 *
	 * @throws CoreException
	 */
	protected IIpsObject newIpsObject(IIpsPackageFragment pack,
			IpsObjectType type, String unqualifiedName) throws CoreException {
		IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
		return file.getIpsObject();
	}

    /**
     * Triggers a full build of the workspace.
     */
    protected void fullBuild() throws CoreException {
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    /**
     * Triggers an incremental build of the workspace.
     */
    protected void incrementalBuild() throws CoreException {
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

    /**
	 * Expects an array of classes that comply to the enum type pattern. The
	 * enum types are registered to the provided IpsProject as definded
	 * datatypes. The qualifiedName of a registered datatype is the unqualified
	 * class name of the enum type class. The enum type class must have the
	 * folloing methods:
	 * </p>
	 * <ol>
	 * <li>public final static &lt;EnumValue &gt; getAllValues()</li>
	 * <li>public String getId()</li>
	 * <li>public String getName()</li>
	 * <li>public boolean isValueOf(String)</li>
	 * <li>public String toString(), must return the id of the enum value</li>
	 * <li>public final static &lt;EnumValue &gt; valueOf(String), the id is
	 * provided to this method and an enum values is supposed to be returned by
	 * this method</li>
	 * </ol>
	 */
	protected DynamicEnumDatatype[] newDefinedEnumDatatype(IIpsProject project,
			Class[] adaptedClass) {

		ArrayList dataTypes = new ArrayList(adaptedClass.length);
		for (int i = 0; i < adaptedClass.length; i++) {
			DynamicEnumDatatype dataType = new DynamicEnumDatatype(project);
			dataType.setAdaptedClass(adaptedClass[i]);
			dataType.setAllValuesMethodName("getAllValues");
			dataType.setGetNameMethodName("getName");
			dataType.setIsParsableMethodName("isValueOf");
			dataType.setIsSupportingNames(true);
			dataType.setQualifiedName(StringUtil
					.unqualifiedName(adaptedClass[i].getName()));
			dataType.setToStringMethodName("toString");
			dataType.setValueOfMethodName("valueOf");
			dataTypes.add(dataType);
		}

		IpsProjectProperties properties = ((IpsModel) project.getIpsModel())
				.getIpsProjectProperties((IpsProject)project);
		DynamicEnumDatatype[] returnValue = (DynamicEnumDatatype[]) dataTypes
				.toArray(new DynamicEnumDatatype[adaptedClass.length]);
		properties.setDefinedDatatypes(returnValue);
		return returnValue;
	}

	/**
	 * Copies the given project properties file and the given classes to the
	 * given project. The classes are added to the classpath of the project.
	 *
	 * @throws CoreException
	 *
	 */
	protected void configureProject(IIpsProject project,
			String ipsProjectFileName, Class[] dependencies)
			throws CoreException {
		IPath outputPath = project.getJavaProject().getOutputLocation();
		IFolder output = project.getProject().getFolder(outputPath);
		for (int i = 0; i < dependencies.length; i++) {
			String name = dependencies[i].getName() + ".class";
			output.getFile(name).create(
					dependencies[i].getResourceAsStream(name), true, null);
		}
		IFile ipsproject = project.getProject().getFile(".ipsproject");
		if (ipsproject.exists()) {
			ipsproject.setContents(getClass().getResourceAsStream(
					ipsProjectFileName), true, false, null);
		} else {
			ipsproject.create(getClass()
					.getResourceAsStream(ipsProjectFileName), true, null);
		}
	}

    /**
     * Sets the builderset as the one to be used by the indicated project.
     * This method modifies the project's properties and also registers the builderset in the model.
     */
    protected void setArtefactBuildset(IIpsProject project, IIpsArtefactBuilderSet builderset) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(builderset.getId());
        project.setProperties(props);
        IpsModel model = ((IpsModel)project.getIpsModel());
        model.setIpsArtefactBuilderSet(project, builderset);
        model.clearValidationCache();
    }

	/**
     * Subclasses can use this method to print out the MultiStatus of a CoreException. This is expecially interesting for
     * testcases that start a build because normally all you get is a ResourceException with no detailed information.
     *
     * @param status the status of a CoreException
	 */
    protected void printOriginalStatus(IStatus status){
        System.out.println(status.getMessage());
        if(status.getChildren().length == 0){
            return;
        }
        IStatus[] statuus = status.getChildren();
        for (int i = 0; i < statuus.length; i++) {
            printOriginalStatus(statuus[i]);
        }
    }
    
    protected IpsModel getIpsModel() {
        return (IpsModel)IpsPlugin.getDefault().getIpsModel();
    }

    protected void testPropertyAccessReadWrite(Class clazz, String propertyName) {
        testPropertyAccessReadOnly(clazz, propertyName);
        testPropertyAccessWriteOnly(clazz, propertyName);
    }

    protected void testPropertyAccessReadWrite(Class clazz, String propertyName, Object object, Object testValueToSet)  {
        testPropertyAccessReadWrite(clazz, propertyName);
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        ModelChangeListener listener = new ModelChangeListener();
        boolean writeOk = false;
        try {
            getIpsModel().addChangeListener(listener);
            prop.getWriteMethod().invoke(object, new Object[]{testValueToSet});
            writeOk = true;
            Object retValue = prop.getReadMethod().invoke(object, new Object[0]);
            assertEquals("Getter method for property " + propertyName + " of class " + clazz.getName() + " does not return the expected value", testValueToSet, retValue);
            assertNotNull("Setter method for property " + propertyName + " of class " + clazz.getName() + " hasn't triggered a change event", listener.lastEvent);
        } catch (Exception e) {
            if (writeOk) {
                fail("An exception occured while reading property " + propertyName + " of class " + clazz.getName());
            } else {
                fail("An exception occured while setting property " + propertyName + " of class " + clazz.getName());
            }
        } finally {
            getIpsModel().removeChangeListener(listener);
        }
    }

    protected void testPropertyAccessWriteOnly(Class clazz, String propertyName) {
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        Method writeMethod = prop.getWriteMethod();
        assertNotNull("Class " + clazz.getName() + " hasn't got a write method for property " + propertyName, writeMethod);
        assertEquals("Class " + clazz.getName() + ": Write method for property " + propertyName + " must have exactly 1 argument", 1, writeMethod.getParameterTypes().length);
    }

    protected void testPropertyAccessReadOnly(Class clazz, String propertyName) {
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        Method readMethod = prop.getReadMethod();
        assertNotNull("Class " + clazz.getName() + " hasn't got a read method for property " + propertyName, readMethod);
        assertEquals(0, readMethod.getParameterTypes().length);
    }

    /**
     * Create the sort order file for {@link IpsPackageFragment}s from a List of Strings.
     *
     * @param folder Handle to the parent folder (IpsPackageFragment or IpsPackageFragmentRoot)
     * @param strings IpsPackageFragment names ikn sort order.
     *
     * @throws IOException
     * @throws CoreException
     */
    protected void createPackageOrderFile(IFolder folder, List strings) throws IOException, CoreException {

        IFile file = folder.getFile(IpsPackageFragment.SORT_ORDER_FILE);

        if (file.exists()) {
            file.delete(true, null);
        }

        String print = new String();
        String lineSeparator = StringUtil.getSystemLineSeparator();

        print = print.concat("# comment" + lineSeparator);

        for (Iterator iter = strings.iterator(); iter.hasNext();) {
            String element = (String)iter.next();

            print = print.concat(element + lineSeparator );
        }

        byte[] bytes = print.getBytes(StringUtil.CHARSET_UTF8);

        ByteArrayInputStream is= new ByteArrayInputStream(bytes);
        file.create(is, true, null);
    }
    
    class ModelChangeListener implements ContentsChangeListener {

        ContentChangeEvent lastEvent;
        
        /**
         * {@inheritDoc}
         */
        public void contentsChanged(ContentChangeEvent event) {
            lastEvent = event;
        }
        
    }
}
