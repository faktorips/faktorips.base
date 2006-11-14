/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.IProgressService;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.ui.views.testrunner.IpsTestRunnerViewPart;
import org.faktorips.runtime.test.SocketIpsTestRunner;

/**
 * Class to run ips test cases in a second VM.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunner implements IIpsTestRunner { 
    
    private static final int ACCEPT_TIMEOUT = 5000;
    
	private int port;
    private IIpsProject ipsProject;    
    private BufferedReader reader;
    
    private String testRunnerMaxHeapSize = ""; //$NON-NLS-1$
    
    private String classpathRepositories;
    private String testsuites;
    
    // List storing the registered ips test run listeners
    private List fIpsTestRunListeners = new ArrayList();
	
    //  Shared instance of the test runner
    private static IpsTestRunner ipsTestRunner;
    
    // Error details in case multiline errors
    private ArrayList errorDetailList;
    
    // The qualified test name in case of an error, necessary to store the name between two socket receives
    // the stack trace of the error will be send in several lines
    private String qualifiedTestName;
    
    // Laucher for the test runner process
    private ILaunch launch;
    
    // Indicates if the test runner was terminated
    private boolean terminated;
    
    // Dummy time to calculate the elapsed time which will be used if the test was terminated
    private long testStartTime;
    
    // Contains the progress monitor 
    private IProgressMonitor testRunnerMonitor;
    
    // The job to run the test
    private TestRunnerJob job;
    
    private IpsTestRunner() {
    }

    /**
     * Returns the shared instance.
     */
    public static IpsTestRunner getDefault(){
    	if (ipsTestRunner == null){
    		ipsTestRunner = new IpsTestRunner();
    	}
    	return ipsTestRunner;
    }

    /*
     * Save all dirty editors in the workbench. Returns whether the operation succeeded.
     * @return whether all saving was completed
     */
    private static boolean saveAllEditors(boolean confirm) {
        if (IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() == null) {
            return false;
        }
        return IpsPlugin.getDefault().getWorkbench().saveAllEditors(confirm);
    }   
    
    /**
     * {@inheritDoc}
     */
    public void setIpsProject(IIpsProject ipsProject){
    	 this.ipsProject = ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject(){
    	 return ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public void run(String classpathRepositories, String testsuites) throws CoreException {
        run(classpathRepositories, testsuites, ILaunchManager.RUN_MODE);
    }

    /**
     * {@inheritDoc}
     */
    public void run(String classpathRepositories, String testsuites, String mode) throws CoreException {
    	this.classpathRepositories= classpathRepositories;
    	this.testsuites = testsuites;
    	
    	// if the classpathRepository or the testsuite are not enclosed in "{...}" then 
    	// enclosed it, therefore the strings will be correctly interpreted as one entry
    	if (! (classpathRepositories.indexOf("{")>=0)) //$NON-NLS-1$
    		classpathRepositories = "{" + classpathRepositories + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    	if (! (testsuites.indexOf("{")>=0)) //$NON-NLS-1$
    		testsuites = "{" + testsuites + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    	
    	IVMInstall vmInstall= getVMInstall(ipsProject.getJavaProject());
        
        if (vmInstall == null)
            return;
        IVMRunner vmRunner = vmInstall.getVMRunner(mode);
        if (vmRunner == null)
            return;

        String[] classPath = computeClasspath(ipsProject.getJavaProject());
        
        VMRunnerConfiguration vmConfig= new VMRunnerConfiguration(SocketIpsTestRunner.class.getName(), classPath);
        String[] args = new String[4];
        
        port= SocketUtil.findFreePort();  //$NON-NLS-1$        
        
        // sets the arguments for the socket test runner
        args[0]= Integer.toString(port);
        args[1] = classpathRepositories;
        args[2] = testsuites;
        args[3] = ""; //$NON-NLS-1$
        // create the string containing the additional repository packages
        for (Iterator iter = getAllRepositoryPackagesAsString(ipsProject).iterator(); iter.hasNext();) {
            args[3] += "{" + (String) iter.next() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        vmConfig.setProgramArguments(args);

        // sets the max heap size of the test runner virtual machine
        if (StringUtils.isEmpty(testRunnerMaxHeapSize)){
            // set the default size to 64
            testRunnerMaxHeapSize = "64"; //$NON-NLS-1$
        }
        if (testRunnerMaxHeapSize.length()>0){
            testRunnerMaxHeapSize = "-Xmx" + testRunnerMaxHeapSize + "m"; //$NON-NLS-1$ //$NON-NLS-2$
            vmConfig.setVMArguments(new String[]{testRunnerMaxHeapSize});
        } 
        
        ILaunchConfiguration launchConfiguration = null;
        if (mode != ILaunchManager.RUN_MODE){
            // create configuration in non run mode, e.g. debug mode
            launchConfiguration = createConfiguration();
        }
        
        launch= new Launch(launchConfiguration, mode, null);
        if (launchConfiguration != null){
            setDefaultSourceLocator(launch, launchConfiguration);
        }
        testStartTime = System.currentTimeMillis();
        vmRunner.run(vmConfig, launch, null);
        DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
        connect();
    }

    /*
     * Creates a dummy non persistent lauch configuration
     */
    private ILaunchConfiguration createConfiguration() throws CoreException {
        ILaunchConfigurationType configType = getWorkbenchLaunchConfigType();
        ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, "IpsTestRunner");  //$NON-NLS-1$
        return wc;
    }
    
    /*
     * Returns the config type of the lauch configuration
     */
    private ILaunchConfigurationType getWorkbenchLaunchConfigType() {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        return lm.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);  
    }   

    /*
     * Sets the default source locator.
     * The source container will evaluated as follows:
     * a) all source container from the given project
     * b) all source container from the workspace
     * c) all sources attached to the libraries in the classpath
     */
    private void setDefaultSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
        // set default source locator if none specified
        if (launch.getSourceLocator() == null) {
            
            ISourceLocator locator = getLaunchManager().newSourceLocator(configuration.getType().getSourceLocatorId());
            AbstractSourceLookupDirector sld = (AbstractSourceLookupDirector)locator;
            sld.initializeDefaults(configuration); 
            
            // get source container from the project
            ISourceContainer sc = new ProjectSourceContainer(ipsProject.getProject(), true);
            List sourceContainer = new ArrayList(Arrays.asList(sc.getSourceContainers()));
            
            // get source container from the workspace
            sc = new WorkspaceSourceContainer();
            sourceContainer.addAll(Arrays.asList(sc.getSourceContainers()));

            // get source container from the classpath
            List classpaths = new ArrayList();
            classpaths.addAll(Arrays.asList(JavaRuntime.computeUnresolvedRuntimeClasspath(ipsProject.getJavaProject())));
            IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[classpaths.size()];
            classpaths.toArray(entries);
            IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
            ISourceContainer[] sourceContainers = JavaRuntime.getSourceContainers(resolved);
            
            sourceContainer.addAll(Arrays.asList(sourceContainers));
            sld.setSourceContainers((ISourceContainer[]) sourceContainer.toArray(new ISourceContainer[sourceContainer.size()]));
            
            launch.setSourceLocator(locator);
        }
    }
    
    private ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }
    
    /**
     * Returns the max heap size for the test runner, should be passed as program argument
     * "-ipstestrunner.xmx <size>" (e.g. "-ipstestrunner.xmx 512M")
     */
    public String getMaxHEapSizeFromAppArgs(){
        String[] applicationArgs = Platform.getApplicationArgs();
        // do not process the last one as it will never have a parameter
        for (int i = 0; i < applicationArgs.length -1; i++) {
            if ("-ipstestrunner.xmx".equalsIgnoreCase(applicationArgs[i])) { //$NON-NLS-1$
                return applicationArgs[i+1];
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    /*
     * Ask for saving dirty editors, and wait for builders.
     */
    private boolean checkPrelauchConditions(final String classpathRepository, final String testsuite, final String mode) {
        try {
            // ask for saving dirty editors
            if (!saveAllEditors(true))
                return false;

            // show test runner view
            IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                    IpsTestRunnerViewPart.EXTENSION_ID);

            // wait until builder finished
            final IJobManager jobManager = Platform.getJobManager();
            boolean wait = (jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0)
                    || (jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD).length > 0)
                    || (jobManager.find(ResourcesPlugin.FAMILY_AUTO_REFRESH).length > 0)
                    || ! jobManager.isIdle();

            if (wait) {
                Job job = new Job(Messages.IpsTestRunner_LaunchingWaitJob_Name) {
                    public IStatus run(final IProgressMonitor monitor) {
                        IJobChangeListener listener = new IJobChangeListener() {
                            public void sleeping(IJobChangeEvent event) {
                            }
                            public void scheduled(IJobChangeEvent event) {
                            }
                            public void running(IJobChangeEvent event) {
                            }
                            public void done(IJobChangeEvent event) {
                                removeJobChangeListener(this);
                            }
                            public void awake(IJobChangeEvent event) {
                            }
                            public void aboutToRun(IJobChangeEvent event) {
                            }
                        };
                        addJobChangeListener(listener);
                        try {
                            jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
                            jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
                            jobManager.join(ResourcesPlugin.FAMILY_AUTO_REFRESH, monitor);
                        } catch (InterruptedException e) {
                            // just continue.
                        }

                        if (!monitor.isCanceled()) {
                            try {
                                startTestRunnerJob(classpathRepository, testsuite, true, mode);
                            } catch (CoreException e) {
                                IpsPlugin.log(e);
                            }                    
                            return Status.OK_STATUS;
                        }
                        return Status.CANCEL_STATUS;
                    }
                };
                IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
                IProgressService progressService = workbench.getProgressService();
                job.setPriority(Job.INTERACTIVE);
                job.setName(Messages.IpsTestRunner_LaunchingWaitJob_Name);
                if (wait) {
                    progressService.showInDialog(workbench.getActiveWorkbenchWindow().getShell(), job);
                }
                job.schedule();
                return false;
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void terminate() throws CoreException{
        try {
            if (launch != null){
                if (launch.canTerminate()){
                    terminated = true;
                    launch.terminate();
                    notifyTestRunEnded("" + (System.currentTimeMillis()- testStartTime)); //$NON-NLS-1$
                    if (!testRunnerMonitor.isCanceled())
                        job.cancel();
                }
            }
        } catch (DebugException e) {
            e.printStackTrace();
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    /*
     * Establish socket connection to the socket test runner
     */
    private boolean connect() {
    	boolean connected = true;
    	try {
            ServerSocket server;
            server= new ServerSocket(port);
            server.setSoTimeout(ACCEPT_TIMEOUT);
            try {
                Socket socket= server.accept();
                try {
                    readMessage(socket);
                } finally {
                    socket.close();
                }
            } finally {
                server.close();
            }
        } catch (Exception e) {
            // error durring socket listening
        	// notify the listener itself, because there is no connection to a runner (connection failed)
        	if (!terminated){
                IpsPlugin.log(e);
                notifyTestRunStarted(1, classpathRepositories, testsuites);
            	notifyTestErrorOccured("", new String[]{Messages.IpsTestRunner_Error_CouldNotConnect + e.getLocalizedMessage()}); //$NON-NLS-1$
            	connected = false;
            } else {
                // the test runner was terminated
                notifyTestRunEnded("" + (System.currentTimeMillis()- testStartTime)); //$NON-NLS-1$
            }
        }
        return connected;
    }
    
    private void readMessage(Socket socket) throws IOException {
    	reader= new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            String line=null;
            while ((line= reader.readLine()) != null) {
                parseMessage(line);
            }
        } finally {
            reader.close();
        }
    }
	
    /*
     * Parse the incomming message and fire the messages events to the registered listener.
     */
    private void parseMessage(String line) {
        if (line.startsWith(SocketIpsTestRunner.ALL_TESTS_STARTED)) {  
    		// format: 
            //   SocketIpsTestRunner.ALL_TESTS_STARTED(<count>) [<repositoryPackage>].[<testPackage>]:<testQualifiedName>{<testFullPath>},...
    		int start = line.indexOf("(") + 1; //$NON-NLS-1$
            int count = Integer.parseInt(line.substring(start, line.indexOf(")"))); //$NON-NLS-1$
            String repositoryPackage = line.substring(line.indexOf("[") +1, line.indexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
            line = line.substring(line.indexOf("].[") + 3); //$NON-NLS-1$
            String testSuitePackage = line.substring(0, line.indexOf("]")); //$NON-NLS-1$
            notifyTestRunStarted(count, repositoryPackage, testSuitePackage);
            line = line.substring(line.indexOf(":") +1); //$NON-NLS-1$
            // now parse all tests, format:
            //   :[<testQualifiedName>{<testFullPath>},...]
            String[] testNames = new String[count];
            String[] testFullPaths = new String[count];            
            int idx = 0;
            while (line.indexOf(",")>=0){ //$NON-NLS-1$
                if (idx > count){
                    throw new RuntimeException(Messages.IpsTestRunner_Error_WrongTestProtocol);
                }
                testNames[idx] = parseTestQualifiedName(line);
                testFullPaths[idx] = parseTestFullPath(line);
                line = line.substring(line.indexOf(",") + 1); //$NON-NLS-1$
                idx ++;
            }
            notifyTestEntries(testNames, testFullPaths);
        }else if (line.startsWith(SocketIpsTestRunner.ALL_TESTS_FINISHED)) { //$NON-NLS-1$
        	String elapsedTime = line.substring(SocketIpsTestRunner.ALL_TESTS_FINISHED.length());  //$NON-NLS-1$
            notifyTestRunEnded(elapsedTime);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_STARTED)) { //$NON-NLS-1$
        	// format: TEST_CASE_STARTED<qualifiedName>{<fullPath>}
            line = line.substring(SocketIpsTestRunner.TEST_STARTED.length());  //$NON-NLS-1$
            String testName = parseTestQualifiedName(line);
            String fullPath = parseTestFullPath(line);
            notifyTestEntry(testName, fullPath);
            notifyTestStarted(testName);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_FINISHED)) { //$NON-NLS-1$
            String testName = line.substring(SocketIpsTestRunner.TEST_FINISHED.length());  //$NON-NLS-1$
        	notifyTestFinished(testName);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_FAILED)) { //$NON-NLS-1$
        	// format: qualifiedName|testObject|testedAttribute|expectedValue|actualValue|message
            String failureDetailsLine = line.substring(SocketIpsTestRunner.TEST_FAILED.length());
            String qualifiedTestName = failureDetailsLine.substring(0, failureDetailsLine.indexOf(SocketIpsTestRunner.TEST_FAILED_DELIMITERS));
            ArrayList failureTokens = new ArrayList(5);
        	while(failureDetailsLine.length()>0){
        		String token = ""; //$NON-NLS-1$
        		int end = failureDetailsLine.indexOf(SocketIpsTestRunner.TEST_FAILED_DELIMITERS);
        		if (end ==-1){
        			end = failureDetailsLine.length();
        			token = failureDetailsLine;
        			failureDetailsLine = ""; //$NON-NLS-1$
        		}else{
        			token = failureDetailsLine.substring(0, end);
        			failureDetailsLine = failureDetailsLine.substring(end +1);
        		}
        		failureTokens.add(token);
        	}
        	notifyTestFailureOccured(qualifiedTestName, (String[])failureTokens.toArray(new String[0]));
        }else if(line.startsWith(SocketIpsTestRunner.TEST_ERROR)){
        	// format qualifiedTestName{message}{StacktraceElem1}{StacktraceElem2}...{StacktraceElemN}
            errorDetailList = new ArrayList();
        	String errorDetails = line.substring(SocketIpsTestRunner.TEST_ERROR.length());  //$NON-NLS-1$
        	qualifiedTestName = errorDetails.substring(0, errorDetails.indexOf("{")); //$NON-NLS-1$
            parseErrorStack(errorDetailList, errorDetails);
        } else if (line.endsWith(SocketIpsTestRunner.TEST_ERROR_END)){
            String errorDetails = line.substring(0, line.indexOf(SocketIpsTestRunner.TEST_ERROR_END));
            parseErrorStack(errorDetailList, errorDetails);
            notifyTestErrorOccured(qualifiedTestName, (String[]) errorDetailList.toArray(new String[0]));
            errorDetailList = null;
        } else if (errorDetailList != null){
            // parse multiline stack elements
            parseErrorStack(errorDetailList, line);
        }
    }

    private String parseTestQualifiedName(String line) {
        String testName = line.substring(0, line.indexOf("{"));  //$NON-NLS-1$
        return testName;
    }

    private String parseTestFullPath(String line) {
        String fullPath = line.substring(line.indexOf("{") +1, line.indexOf("}")); //$NON-NLS-1$ //$NON-NLS-2$
        return fullPath;
    }

    private void parseErrorStack(ArrayList errorDetailList, String errorDetails) {
        if (errorDetails.length() == 0)
            return;
        
        errorDetails = errorDetails.replaceAll("\t","");  //$NON-NLS-1$ //$NON-NLS-2$
        if (errorDetails.indexOf("{") == -1){ //$NON-NLS-1$
            errorDetailList.add(errorDetails);
        }else{
            // fix brackets, in case of multiline stack elements
            if (errorDetails.indexOf("}") == -1){ //$NON-NLS-1$
                errorDetails += "}"; //$NON-NLS-1$
            } else if (errorDetails.indexOf("{") > errorDetails.indexOf("}")){ //$NON-NLS-1$ //$NON-NLS-2$
                errorDetails = "{" + errorDetails; //$NON-NLS-1$
            }
            // parse stack elements
            while (errorDetails.indexOf("}") >= 0){ //$NON-NLS-1$
            	String stackElem = errorDetails.substring(errorDetails.indexOf("{") + 1, errorDetails.indexOf("}")); //$NON-NLS-1$ //$NON-NLS-2$
                errorDetailList.add(stackElem); //$NON-NLS-1$ //$NON-NLS-2$
            	if (errorDetails.indexOf("{") >=0 ) //$NON-NLS-1$
            		errorDetails = errorDetails.substring(errorDetails.indexOf("}") + 1); //$NON-NLS-1$
            }
        }
    }

	private IVMInstall getVMInstall(IJavaProject project) throws CoreException {
    	IVMInstall vmInstall = null;
    	if (project != null)
    		vmInstall = JavaRuntime.getVMInstall(project);
    	
        if (vmInstall == null)
            vmInstall = JavaRuntime.getDefaultVMInstall();
        return vmInstall;
    }
    
    /*
     * Return all classpath enties from the given project
     */
    private String[] computeClasspath(IJavaProject project) throws CoreException {
    	String[] defaultPath= JavaRuntime.computeDefaultRuntimeClassPath(project);
        String[] classPath= new String[defaultPath.length];
        System.arraycopy(defaultPath, 0, classPath, 0, defaultPath.length);
        
        return classPath;
    }
    
    /**
     * Returns a list off repository packages of the given ips project and its referenced projects
     * and referenced projects by the referenced projects ...
     */
     private List getAllRepositoryPackagesAsString(IIpsProject ipsProject) throws CoreException{
         List repositoryPackages = new ArrayList();
         getRepositoryPackages(ipsProject, repositoryPackages);
         IIpsProject[] ipsProjects = ipsProject.getReferencedIpsProjects();
         for (int i = 0; i < ipsProjects.length; i++) {
             getRepositoryPackages(ipsProjects[i], repositoryPackages);
         }
         return repositoryPackages;
     }
     
     /*
      * Adds all repository packages of the given ips project to the given list. Add the repository
      * package only if the toc file exists.
      */
     private void getRepositoryPackages(IIpsProject ipsProject, List repositoryPackages) throws CoreException {
         IIpsPackageFragmentRoot[] ipsRoots = ipsProject.getIpsPackageFragmentRoots();
         for (int i = 0; i < ipsRoots.length; i++) {
             IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
             IFile tocFile = builderSet.getRuntimeRepositoryTocFile(ipsRoots[i]);
             if (tocFile != null && tocFile.exists()){
                 String repositoryPck = builderSet.getRuntimeRepositoryTocResourceName(ipsRoots[i]);
                 if (repositoryPck != null && ! repositoryPackages.contains(repositoryPck))
                     repositoryPackages.add(repositoryPck);
             }
         }
         IIpsProject[] ipsProjects = ipsProject.getReferencedIpsProjects();
         for (int i = 0; i < ipsProjects.length; i++) {
             getRepositoryPackages(ipsProjects[i], repositoryPackages);
         }
     }
     
    /**
     * Adds the given ips test run listener to the collection of listeners
     */
    public void addIpsTestRunListener(IIpsTestRunListener newListener) {
    	fIpsTestRunListeners.add(newListener);
    }
    
	/**
	 * Removes the given ips test run listener from the collection of listeners
	 */
	public void removeIpsTestRunListener(IIpsTestRunListener listener) {
		if (fIpsTestRunListeners != null) 
			fIpsTestRunListeners.remove(listener);
	}
	
	/**
	 * Returns all registered ips test run listener.
	 */
	public List getIpsTestRunListener(){
		return fIpsTestRunListeners;
	}
	
    private void notifyTestEntry(String qualifiedName, String fullPath) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testTableEntry(qualifiedName, fullPath);
		}
    }
    
    private void notifyTestEntries(String[] qualifiedNames, String[] fullPaths) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
            IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
            listener.testTableEntries(qualifiedNames, fullPaths);
        }
    }    
    
    private void notifyTestStarted(String qualifiedTestName) {
        // check if the test runner is canceled
        if (testRunnerMonitor.isCanceled())
            try {
                terminate();
            } catch (CoreException e) {
                // ignore exception
            }
        testRunnerMonitor.subTask(qualifiedTestName);
            
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testStarted(qualifiedTestName);
		}
    }
    
    private void notifyTestFinished(String qualifiedTestName) {
        testRunnerMonitor.worked(1);
        
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testFinished(qualifiedTestName);
		}
    }
    
    private void notifyTestFailureOccured(String testFailureOccured, String[] failureDetails) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testFailureOccured(testFailureOccured, failureDetails);
		}
    }
    
	private void notifyTestRunStarted(int count, String repositoryPackage, String testPackage) {
        testRunnerMonitor.beginTask(Messages.IpsTestRunner_Job_Name, count);
        
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testRunStarted(count, repositoryPackage, testPackage);
		}		
	} 
	
	private void notifyTestRunEnded(String elapsedTime) {
        testRunnerMonitor.done();
        
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testRunEnded(elapsedTime);
		}		
	} 

	private void notifyTestErrorOccured(String qualifiedTestName, String[] errorDetails) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testErrorOccured(qualifiedTestName, errorDetails);
		}
	}
	
	/**
     * {@inheritDoc}
	 */
	public void startTestRunnerJob(String classpathRepository, String testsuite) throws CoreException{
        startTestRunnerJob(classpathRepository, testsuite, false, null);
	}

    /**
     * {@inheritDoc}
     */
    public void startTestRunnerJob(String classpathRepository, String testsuite, String mode) throws CoreException{
        startTestRunnerJob(classpathRepository, testsuite, false, mode);
    }
    
    /**
     * Starts the test runner.
     */
    private void startTestRunnerJob(String classpathRepository, String testsuite, boolean force, String mode) throws CoreException{
        // check for dirty editors and wait for builders
        if (!force && !checkPrelauchConditions(classpathRepository, testsuite, mode))
            return;
        
        // first check the heap size, to display an error if there is a wrong value,
        // this is the last chance to display the error, otherwise the error will only be logged in the background
        testRunnerMaxHeapSize = IpsPlugin.getDefault().getIpsPreferences().getIpsTestRunnerMaxHeapSize();
        if (!StringUtils.isNumeric(testRunnerMaxHeapSize)){
            throw new CoreException(new IpsStatus(NLS.bind(Messages.IpsTestRunner_Error_WrongHeapSize, testRunnerMaxHeapSize)));
        }
        
        job = new TestRunnerJob(this, classpathRepository, testsuite, mode);

        job.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent event) {
                // nothing to do
            }
        });
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        job.setRule(workspace.getRoot());
        job.schedule();
    }
    
	/*
	 * Job class to run the selected tests.
	 */
	private class TestRunnerJob extends WorkspaceJob {
		private IIpsTestRunner testRunner;
		private String classpathRepository;
		private String testsuite;
		private String mode;
        
        public TestRunnerJob(IIpsTestRunner testRunner, String classpathRepository, String testsuite, String mode) {
			super(Messages.IpsTestRunner_Job_Name);
			this.testRunner = testRunner;
			this.classpathRepository = classpathRepository;
			this.testsuite = testsuite;
            this.mode = mode;
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
                testRunnerMonitor = monitor;
                
                if (!monitor.isCanceled()) {
                    if (mode != null){
                        testRunner.run(classpathRepository, testsuite, mode);
                    } else {
                        testRunner.run(classpathRepository, testsuite);
                    }
                }
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			return Status.OK_STATUS;
		}
	}
}
