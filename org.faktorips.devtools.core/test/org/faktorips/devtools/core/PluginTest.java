package org.faktorips.devtools.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlTestCase;



/**
 * Base class for all plugin test cases. Has a factory method to create an ips project
 * including the underlying platform project. 
 * 
 * @author Jan Ortmann
 */
public abstract class PluginTest extends XmlTestCase {

    /**
     * 
     */
    public PluginTest() {
        super();
    }

    /**
     * @param name
     */
    public PluginTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        setAutoBuild(false);
        IpsPlugin.getDefault().reinitModel();
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (int i=0; i<projects.length; i++) {
                    projects[i].close(null);
                    projects[i].delete(true, true, null);
                }               
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
    }

	/**
	 * Creates a new IpsProject.
	 */
	protected IIpsProject newIpsProject(final String name) throws CoreException
	{
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
        	    IProject project = newPlatformProject(name);
        		addJavaCapabilities(project);
        		addIpsCapabilities(project);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
	    
		return IpsPlugin.getDefault().getIpsModel().getIpsProject(name);
	}
	
	/**
	 * Creates a new platfrom project with the given name and opens it.  
	 * @throws CoreException
	 */
	protected IProject newPlatformProject(final String name) throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                internalNewPlatformProject(name);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
        return workspace.getRoot().getProject(name);
	}

	/**
	 * Creates a new platfrom project with the given name and opens it.  
	 * @throws CoreException
	 */
	private IProject internalNewPlatformProject(final String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		project.create(null);
		project.open(null);
		return project;
	}
	
	/**
	 * Creates a new Java Project for the given platform project. 
	 */
	private IJavaProject addJavaCapabilities(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		// add Java nature
		Util.addNature(project, JavaCore.NATURE_ID);
		// create bin folder and set as output folder.
		IFolder binFolder= project.getFolder("bin");
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
	
	protected void addIpsCapabilities(IProject project) throws CoreException {
	    Util.addNature(project, IIpsProject.NATURE_ID);
	    IFolder rootFolder = project.getFolder("productdef");
	    rootFolder.create(true, true, null);
	    IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
	    IIpsObjectPath path = ipsProject.getIpsObjectPath();
	    path.setOutputDefinedPerSrcFolder(true);
	    IIpsSrcFolderEntry entry = path.newSourceFolderEntry(rootFolder);
	    entry.setSpecificBasePackageNameForGeneratedJavaClasses("org.faktorips.sample.model");
	    entry.setSpecificOutputFolderForGeneratedJavaFiles(project.getFolder("src"));
	    entry.setSpecificBasePackageNameForExtensionJavaClasses("org.faktorips.sample.model");
	    entry.setSpecificOutputFolderForExtensionJavaFiles(project.getFolder("extension"));
	    ipsProject.setIpsObjectPath(path);

	    //TODO: wichtig dies erzeugt eine Abhängigkeit vom StdBuilder Projekt. Dies muss dringend überarbeitet
        //werden
        ipsProject.setCurrentArtefactBuilderSet("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
	    ipsProject.setValueDatatypes(new String[]{"Decimal", "Money", "String", "Boolean"});
	}

	private void addSystemLibraries(IJavaProject javaProject) throws JavaModelException 
	{
		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaRuntime.getDefaultJREContainerEntry();
		javaProject.setRawClasspath(newEntries, null);
	}
	
	private void waitForIndexer() throws JavaModelException 
	{
	    SearchEngine engine = new SearchEngine();
	    engine.searchAllTypeNames(
	            new char[] {}, 
	            new char[] {}, 
	            SearchPattern.R_EXACT_MATCH,
	            IJavaSearchConstants.CLASS,
	            SearchEngine.createJavaSearchScope(new IJavaElement[0]),
				new ITypeNameRequestor() {
	    			public void acceptClass(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
	    			}
	    			public void acceptInterface(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
	    			}
	    		}, 
	    		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
	}
	
	protected void setAutoBuild(boolean autoBuild) throws CoreException {
        IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
        if (autoBuild != ResourcesPlugin.getWorkspace().isAutoBuilding()) {
            description.setAutoBuilding(autoBuild);
            ResourcesPlugin.getWorkspace().setDescription(description);
        }
    }
	
	/**
	 * Creates a new ipsobject in the indicated project's first source folder. If the qualifiedName includes a 
	 * package name, the package is created if it does not already exists.
	 * 
	 * @throws CoreException
	 */
	protected IIpsObject newIpsObject(IIpsProject project, IpsObjectType type, String qualifiedName) throws CoreException {
	    IIpsPackageFragmentRoot root = project.getSourceIpsPackageFragmentRoots()[0];
	    return newIpsObject(root, type, qualifiedName);
	}
	
	/**
	 * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes a 
	 * package name, the package is created if it does not already exists.
	 * 
	 * @throws CoreException
	 */
	protected IIpsObject newIpsObject(IIpsPackageFragmentRoot root, IpsObjectType type, String qualifiedName) throws CoreException {
	    String packName = StringUtil.getPackageName(qualifiedName);
	    String unqualifiedName = StringUtil.unqualifiedName(qualifiedName);
	    IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
	    if (!pack.exists()) {
	        pack = root.createPackageFragment(packName, true, null);
	    }
	    IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
	    return file.getIpsObject();
	}
	
	/**
	 * Creates a new policy component type in the indicated package fragment root. If the qualifiedName includes a 
	 * package name, the package is created if it does not already exists.
	 * 
	 * @throws CoreException
	 */
	protected PolicyCmptType newPolicyCmptType(IIpsPackageFragmentRoot root, String qualifiedName) throws CoreException {
	    return (PolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
	}
	
	/**
	 * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes a 
	 * package name, the package is created if it does not already exists.
	 * 
	 * @throws CoreException
	 */
	protected IIpsObject newIpsObject(IIpsPackageFragment pack, IpsObjectType type, String unqualifiedName) throws CoreException {
	    IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
	    return file.getIpsObject();
	}
}
