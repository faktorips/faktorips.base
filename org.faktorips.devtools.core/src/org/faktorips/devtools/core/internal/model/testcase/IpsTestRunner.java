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
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.runtime.test.SocketIpsTestRunner;

/**
 * Class to run ips test cases in a second VM.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunner implements IIpsTestRunner { 
    
    private int port;
    private IJavaProject project;    
    private BufferedReader reader;
    
    // List storing the registered ips test run listeners
    private List fIpsTestRunListeners = new ArrayList();
	
    public IpsTestRunner() {
    }

    /**
     * Sets the java project to compute the default runtime classpath
     */
    public void setJavaProject(IJavaProject project){
    	 this.project = project;
    }

    /**
     * Run the given ips test in a new VM.
     * 
     * @param classpathRepository The name of the repository in the classpath which contains 
     *                            the to be tested testsuite.
     * @param testsuite The name of the testsuite which will be executed.
     * @throws CoreException If an error occured.
     */
    public void run(String classpathRepository, String testsuite) throws CoreException {
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
        args[1] = classpathRepository;
        args[2] = testsuite;
        
        vmConfig.setProgramArguments(args);
        ILaunch launch= new Launch(null, ILaunchManager.RUN_MODE, null);
        vmRunner.run(vmConfig, launch, null);
        DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
        connect();
    }

    private void connect() throws CoreException {
        try {
            ServerSocket server;
            server= new ServerSocket(port);
            server.setSoTimeout(500);
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
        } catch (IOException e) {
            IStatus status= new IpsStatus(IStatus.ERROR, "Could not connect", e); //$NON-NLS-1$
            throw new CoreException(status);
        }
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
     * 
     */
    private void parseMessage(String line) {
    	if (line.startsWith(SocketIpsTestRunner.ALL_TESTS_STARTED)) {  
            int count = Integer.parseInt(line.substring(SocketIpsTestRunner.ALL_TESTS_STARTED.length()));
            notifyTestRunStarted(count);
        }else if (line.startsWith(SocketIpsTestRunner.ALL_TESTS_FINISHED)) { //$NON-NLS-1$
        	notifyTestRunEnded();
        }else if (line.startsWith(SocketIpsTestRunner.TEST_STARTED)) { //$NON-NLS-1$
            String testName = line.substring(SocketIpsTestRunner.TEST_STARTED.length());  //$NON-NLS-1$
            notifyTestTreeEntry(testName);
            notifyTestStarted(testName);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_FINISHED)) { //$NON-NLS-1$
            String testName = line.substring(SocketIpsTestRunner.TEST_FINISHED.length());  //$NON-NLS-1$
        	notifyTestFinished(testName);
        }else if (line.startsWith(SocketIpsTestRunner.TEST_FAILED)) { //$NON-NLS-1$
        	String failureDetailsLine = line.substring(SocketIpsTestRunner.TEST_FAILED.length());
        	StringTokenizer stringTokenizer = new StringTokenizer(failureDetailsLine,SocketIpsTestRunner.TEST_FAILED_DELIMITERS);
            String[] failureDeatils = new String[stringTokenizer.countTokens()];
        	int idx = 0;
        	while (stringTokenizer.hasMoreElements()) {
        		failureDeatils[idx++] = (String) stringTokenizer.nextElement();
			}
        	notifyTestFailureOccured(failureDeatils);
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
    	if (project == null){
    		// TODO Joerg: eval and add default classpath
    		// Workaround: to run this test runner copy the jar in the eclipse application directory
    		return new String[]{"faktorips-runtime.jar"};
    	}
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
	
    private void notifyTestTreeEntry(String tableEntry) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testTableEntry(tableEntry);
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
    
	private void notifyTestRunStarted(int count) {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testRunStarted(count);
		}		
	} 
	
	private void notifyTestRunEnded() {
        for (Iterator iter = fIpsTestRunListeners.iterator(); iter.hasNext();) {
			IIpsTestRunListener listener = (IIpsTestRunListener) iter.next();
			listener.testRunEnded();
		}		
	}   	
}
