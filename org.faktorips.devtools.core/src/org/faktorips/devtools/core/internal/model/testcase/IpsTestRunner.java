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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.runtime.test.SocketIpsTestRunner;

/**
 * Class to run ips test cases in a second VM.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunner implements IIpsTestRunner { 
    
    private static final int ACCEPT_TIMEOUT = 1500;
    
	private int port;
    private IJavaProject project;    
    private BufferedReader reader;
    
    private String classpathRepositories;
    private String testsuites;
    
    // List storing the registered ips test run listeners
    private List fIpsTestRunListeners = new ArrayList();
	
    //  Shared instance of the test runner
    private static IpsTestRunner ipsTestRunner;
    
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

    /**
     * {@inheritDoc}
     */
    public void setJavaProject(IJavaProject project){
    	 this.project = project;
    }

    /**
     * {@inheritDoc}
     */
    public IJavaProject getJavaProject(){
    	 return project;
    }
    
    /**
     * Run the given ips test in a new VM.
     * 
     * @param classpathRepository The name of the repository in the classpath which contains 
     *                            the to be tested testsuite.
     * @param testsuite The name of the testsuite which will be executed.
     * @throws CoreException If an error occured.
     */
    public void run(String classpathRepositories, String testsuites) throws CoreException {
    	this.classpathRepositories= classpathRepositories;
    	this.testsuites = testsuites;
    	
    	// if the classpathRepository or the testsuite are not enclosed in "{...}" then 
    	// enclosed it, therefore the strings will be correctly interpreted as one entry
    	if (! (classpathRepositories.indexOf("{")>=0))
    		classpathRepositories = "{" + classpathRepositories + "}";
    	if (! (testsuites.indexOf("{")>=0))
    		testsuites = "{" + testsuites + "}";
    	
    	IVMInstall vmInstall= getVMInstall(project);
        
        if (vmInstall == null)
            return;
        IVMRunner vmRunner = vmInstall.getVMRunner(ILaunchManager.RUN_MODE);
        if (vmRunner == null)
            return;

        String[] classPath = computeClasspath(project);
        
        VMRunnerConfiguration vmConfig= new VMRunnerConfiguration(SocketIpsTestRunner.class.getName(), classPath);
        String[] args = new String[3];
        
        port= SocketUtil.findFreePort();  //$NON-NLS-1$        
        
        args[0]= Integer.toString(port);
        args[1] = classpathRepositories;
        args[2] = testsuites;
        
        vmConfig.setProgramArguments(args);
        ILaunch launch= new Launch(null, ILaunchManager.RUN_MODE, null);
        vmRunner.run(vmConfig, launch, null);
        DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
        connect();
    }

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
        	notifyTestRunStarted(1, classpathRepositories, testsuites);
        	notifyTestErrorOccured("", new String[]{"Could not connect to the test runner: " + e.getLocalizedMessage()});
        	connected = false;
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
    		// format: SocketIpsTestRunner.ALL_TESTS_STARTED(<count>) [<repositoryPackage>].[<testPackage>]
    		int start = line.indexOf("(") + 1;
            int count = Integer.parseInt(line.substring(start, line.indexOf(")")));
            String repositoryPackage = line.substring(line.indexOf("[") +1, line.indexOf("]"));
            line = line.substring(line.indexOf("].[") + 3);
            String testSuitePackage = line.substring(0, line.length() -1);
            notifyTestRunStarted(count, repositoryPackage, testSuitePackage);
        }else if (line.startsWith(SocketIpsTestRunner.ALL_TESTS_FINISHED)) { //$NON-NLS-1$
        	notifyTestRunEnded();
        }else if (line.startsWith(SocketIpsTestRunner.TEST_STARTED)) { //$NON-NLS-1$
        	// format: TEST_CASE_STARTED<qualifiedName>{<fullPath>}
            String testName = line.substring(SocketIpsTestRunner.TEST_STARTED.length(), line.indexOf("{"));  //$NON-NLS-1$
            String fullPath = line.substring(line.indexOf("{") +1, line.indexOf("}"));
            notifyTestEntry(testName, fullPath);
            notifyTestStarted(testName);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_FINISHED)) { //$NON-NLS-1$
            String testName = line.substring(SocketIpsTestRunner.TEST_FINISHED.length());  //$NON-NLS-1$
        	notifyTestFinished(testName);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_FAILED)) { //$NON-NLS-1$
        	// format: qualifiedName|testObject|testedAttribute|expectedValue|actualValue
        	String failureDetailsLine = line.substring(SocketIpsTestRunner.TEST_FAILED.length());
        	ArrayList failureTokens = new ArrayList(5);
        	while(failureDetailsLine.length()>0){
        		String token = "";
        		int end = failureDetailsLine.indexOf(SocketIpsTestRunner.TEST_FAILED_DELIMITERS);
        		if (end ==-1){
        			end = failureDetailsLine.length();
        			token = failureDetailsLine;
        			failureDetailsLine = "";
        		}else{
        			token = failureDetailsLine.substring(0, end);
        			failureDetailsLine = failureDetailsLine.substring(end +1);
        		}
        		failureTokens.add(token);
        	}
        	notifyTestFailureOccured((String[])failureTokens.toArray(new String[0]));
        }else if(line.startsWith(SocketIpsTestRunner.TEST_ERROR)){
        	// format qualifiedTestName{message}{StacktraceElem1}{StacktraceElem2}...{StacktraceElemN}
        	ArrayList errorDetailList = new ArrayList();
        	String errorDetails = line.substring(SocketIpsTestRunner.TEST_ERROR.length());  //$NON-NLS-1$
        	String qualifiedTestName = errorDetails.substring(0, errorDetails.indexOf("{"));
        	while (errorDetails.indexOf("}") >= 0){
        		errorDetailList.add(errorDetails.substring(errorDetails.indexOf("{") + 1, errorDetails.indexOf("}")));
        		if (errorDetails.indexOf("{") >=0 )
        			errorDetails = errorDetails.substring(errorDetails.indexOf("}") + 1);
        	}
        	notifyTestErrorOccured(qualifiedTestName, (String[]) errorDetailList.toArray(new String[0]));
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
    
    private void notifyTestStarted(String qualifiedTestName) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testStarted(qualifiedTestName);
		}
    }

    private void notifyTestFinished(String qualifiedTestName) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testFinished(qualifiedTestName);
		}
    }
    
    private void notifyTestFailureOccured(String[] failureDetails) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testFailureOccured(failureDetails);
		}
    }  
    
	private void notifyTestRunStarted(int count, String repositoryPackage, String testPackage) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testRunStarted(count, repositoryPackage, testPackage);
		}		
	} 
	
	private void notifyTestRunEnded() {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testRunEnded();
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
	public void startTestRunnerJob(String classpathRepository, String testsuite){
		TestRunnerJob job = new TestRunnerJob(this, classpathRepository, testsuite);
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
		
		public TestRunnerJob(IIpsTestRunner testRunner, String classpathRepository, String testsuite) {
			super("FaktorIps Test Job");
			this.testRunner = testRunner;
			this.classpathRepository = classpathRepository;
			this.testsuite = testsuite;
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
				testRunner.run(classpathRepository, testsuite);
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			return Status.OK_STATUS;
		}
	}
}
