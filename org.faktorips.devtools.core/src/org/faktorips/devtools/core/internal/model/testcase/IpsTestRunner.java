/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.testcase.ITocTreeFromDependencyManagerLoader;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.testcase.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.testcase.IIpsTestRunListener;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.test.AbstractIpsTestRunner;
import org.faktorips.runtime.test.SocketIpsTestRunner;
import org.faktorips.util.StringUtil;

/**
 * Class to run ips test cases in a second VM.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunner implements IIpsTestRunner {

    public static final String ID_IPSTEST_LAUNCH_CONFIGURATION_TYPE = "org.faktorips.devtools.core.ipsTestLaunchConfigurationType"; //$NON-NLS-1$
    public static final String ATTR_PACKAGEFRAGMENTROOT = IpsPlugin.PLUGIN_ID + ".ATTR_PACKAGEFRAGMENTROOT"; //$NON-NLS-1$
    public static final String ATTR_PROJECT = IpsPlugin.PLUGIN_ID + ".ATTR_PROJECT"; //$NON-NLS-1$
    public static final String ATTR_TESTCASES = IpsPlugin.PLUGIN_ID + ".ATTR_TESTCASES"; //$NON-NLS-1$
    public static final String ATTR_MAX_HEAP_SIZE = IpsPlugin.PLUGIN_ID + ".ATTR_MAX_HEAP_SIZE"; //$NON-NLS-1$

    public static final String INVALID_NAME = IIpsProjectNamingConventions.INVALID_NAME;

    public static final boolean TRACE_IPS_TEST_RUNNER;

    private static final String BRACELEFT = "{"; //$NON-NLS-1$
    private static final String BRACERIGHT = "}"; //$NON-NLS-1$

    /**
     * Characters which are used within the test runner protocol and therefore forbidden to use
     * inside a test case name
     */
    private static final String FORBIDDEN_CHARACTERS_IN_TESTCASENAME = "\\[\\]{},:"; //$NON-NLS-1$

    private static final int ACCEPT_TIMEOUT = 5000;

    /**
     * time in ms to check for active test runner, if this time is reached then the test runner will
     * always started this avoids dead test runner (a state where the test runner didn't returned
     * from his running state)
     */
    private static final int MAX_START_TIME_INTERVAL = 5000;

    /** Shared instance of the test runner */
    private static final IpsTestRunner IPS_TEST_RUNNER = new IpsTestRunner();

    static {
        TRACE_IPS_TEST_RUNNER = Boolean.valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/testrunner")) //$NON-NLS-1$
                .booleanValue();
    }

    private int port;
    private IIpsProject ipsProject;
    private BufferedReader reader;

    private String testRunnerMaxHeapSize = ""; //$NON-NLS-1$

    private String classpathRepositories;
    private String testsuites;

    /** List storing the registered ips test run listeners */
    private List<IIpsTestRunListener> fIpsTestRunListeners = new ArrayList<>();

    /** Error details in case multiline errors */
    private ArrayList<String> errorDetailList;

    /**
     * The qualified test name in case of an error, necessary to store the name between two socket
     * receives the stack trace of the error will be send in several lines
     */
    private String qualifiedTestName;

    /** Laucher for the test runner process */
    private ILaunch launch;

    /** Indicates if the test runner was terminated */
    private boolean terminated;

    /** Dummy time to calculate the elapsed time which will be used if the test was terminated */
    private long testStartTime;

    /** Contains the progress monitor */
    private IProgressMonitor testRunnerMonitor;

    /** The job to run the test */
    private TestRunnerJob job;

    /**
     * timestamps to check an active test runner (either active delegate test runner state or active
     * test runner)
     */
    private long launchStartTime;

    private IpsTestRunner() {
        // avoid creating new instances (use getDefault() instead)
    }

    /**
     * Returns the shared instance.
     */
    public static IpsTestRunner getDefault() {
        return IPS_TEST_RUNNER;
    }

    /**
     * Gets the package name from the given ips package fragment root.
     */
    public static String getRepPckNameFromPckFrgmtRoot(IIpsPackageFragmentRoot root) {
        IIpsArtefactBuilderSet builderSet = root.getIpsProject().getIpsArtefactBuilderSet();
        return builderSet.getRuntimeRepositoryTocResourceName(root);
    }

    public static IIpsProject getIpsProject(String projectName) {
        IIpsProject[] projects = IIpsModel.get().getIpsProjects();
        for (IIpsProject project : projects) {
            if (project.getName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }

    public static IIpsProject getIpsProjectFromTocPath(String tocPaths) {
        List<String> reps = AbstractIpsTestRunner.extractListFromString(tocPaths);
        if (!(reps.size() > 0)) {
            return null;
        }
        String tocPath = reps.get(0);
        IIpsProject[] projects = IIpsModel.get().getIpsProjects();
        for (IIpsProject project : projects) {
            IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot root : roots) {
                if (tocPath.equals(getRepPckNameFromPckFrgmtRoot(root))) {
                    return project;
                }
            }
        }
        return null;
    }

    /**
     * Validate if the test case name is a valid name.
     */
    public static MessageList validateTestCaseName(String testCaseName) {
        MessageList ml = new MessageList();
        Pattern p = Pattern.compile("[" + FORBIDDEN_CHARACTERS_IN_TESTCASENAME + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        boolean matches = p.matcher(testCaseName).find();
        if (matches) {
            ml.add(new Message(INVALID_NAME, NLS.bind(Messages.IpsTestRunner_validationErrorInvalidName, testCaseName,
                    FORBIDDEN_CHARACTERS_IN_TESTCASENAME.replaceAll("\\\\", "")), Message.ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return ml;
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    /**
     * Run the test with the given jobLaunch.
     */
    private void run(String classpathRepositories, String testsuites, String mode, ILaunch launch)
            throws CoreException {
        trace("IpsTestRunner.run()"); //$NON-NLS-1$

        if (isRunningTestRunner()) {
            trace("Cancel test runner, because runner is running"); //$NON-NLS-1$
            return;
        }

        this.classpathRepositories = checkEnclosed(classpathRepositories);
        this.testsuites = checkEnclosed(testsuites);

        this.ipsProject = getCurrentIpsProject(this.ipsProject);

        if (this.ipsProject == null) {
            trace("Cancel test run, no project found."); //$NON-NLS-1$
            resetLaunchAndTestRun();
            return;
        }

        // store jobLaunch , so it can be terminated later
        this.launch = launch;
        // if no jobLaunch is given first create a new launch
        // the run method will be called later by using a new UI Job and a given jobLaunch
        if (this.launch == null) {
            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

            ILaunchConfiguration launchConfiguration = createConfiguration(this.classpathRepositories, this.testsuites,
                    manager);

            this.launch = new Launch(launchConfiguration, mode, null);
            if (launchConfiguration != null) {
                setDefaultSourceLocatorInternal(this.launch, launchConfiguration);
                lauchInUiThreadIfNecessary(launchConfiguration, mode);
            }
            return;
        }

        // sets the launch start time
        launchStartTime = System.currentTimeMillis();

        IVMInstall vmInstall = getVMInstall(ipsProject.getJavaProject());

        if (vmInstall == null) {
            trace("Cancel test run, VM not found."); //$NON-NLS-1$
            resetLaunchAndTestRun();
            return;
        }

        IVMRunner vmRunner = vmInstall.getVMRunner(mode);
        if (vmRunner == null) {
            trace("Cancel test run, VM Runner not found."); //$NON-NLS-1$
            resetLaunchAndTestRun();
            return;
        }

        String[] classPath = computeClasspath(this.ipsProject.getJavaProject());

        VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(SocketIpsTestRunner.class.getName(), classPath);
        String[] args = new String[4];

        port = SocketUtil.findFreePort();

        // sets the arguments for the socket test runner
        args[0] = Integer.toString(port);
        args[1] = this.classpathRepositories;
        args[2] = this.testsuites;
        args[3] = fillArgsRepositoryPackages(this.ipsProject);

        vmConfig.setProgramArguments(args);

        // Environment variables
        String[] envp = getEnvironment(this.launch.getLaunchConfiguration());
        vmConfig.setEnvironment(envp);

        // sets the max heap size of the test runner virtual machine
        if (StringUtils.isEmpty(testRunnerMaxHeapSize)) {
            // set the default size to 64
            testRunnerMaxHeapSize = "64"; //$NON-NLS-1$
        }
        setVmConfigMaxHeapSize(vmConfig, testRunnerMaxHeapSize);

        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

        ILaunchConfiguration launchConfiguration = this.launch.getLaunchConfiguration();
        // overwrite default max heap size if specified in the current launch
        String maxHeapSizeInConfiguration = launchConfiguration.getAttribute(ATTR_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(maxHeapSizeInConfiguration)) {
            setVmConfigMaxHeapSize(vmConfig, maxHeapSizeInConfiguration);
        }
        setDefaultSourceLocatorInternal(this.launch, launchConfiguration);

        // set additional vm arguments
        setAdditionalVmArguments(vmConfig, launchConfiguration);

        trace("Run VM Runner."); //$NON-NLS-1$
        testStartTime = System.currentTimeMillis();
        vmRunner.run(vmConfig, this.launch, new NullProgressMonitor());
        manager.addLaunch(this.launch);

        trace("Connect."); //$NON-NLS-1$
        connect();
        trace("Tester stream finished."); //$NON-NLS-1$

        if (testRunnerMonitor != null) {
            testRunnerMonitor.done();
        }

        resetLaunchAndTestRun();
    }

    /*
     * Create the string containing the additional repository packages
     */
    private String fillArgsRepositoryPackages(IIpsProject currentIpsProject) throws CoreException {
        StringBuilder argument = new StringBuilder();
        for (String repositoryPackages : getAllRepositoryPackagesAsString(currentIpsProject)) {
            argument.append(BRACELEFT + repositoryPackages + BRACERIGHT);
        }
        return argument.toString();
    }

    private IIpsProject getCurrentIpsProject(IIpsProject currentIpsProject) {
        if (currentIpsProject == null) {
            return getIpsProjectFromTocPath(this.classpathRepositories);
        }
        return currentIpsProject;
    }

    /*
     * if the classpathRepository or the testsuite are not enclosed in "{...}" then enclosed it,
     * therefore the strings will be correctly interpreted as one entry
     */
    private String checkEnclosed(String classpathRepositories) {
        if (!(classpathRepositories.indexOf(BRACELEFT) >= 0)) {
            return BRACELEFT + classpathRepositories + BRACERIGHT;
        }
        return classpathRepositories;
    }

    /**
     * Set the additional vm argurments specified in the jobLaunch configuration
     */
    private void setAdditionalVmArguments(VMRunnerConfiguration vmConfig, ILaunchConfiguration launchConfiguration)
            throws CoreException {

        String vmArgsInConfig = launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
                (String)null);
        if (StringUtils.isNotEmpty(vmArgsInConfig)) {
            String[] vmArguments = vmConfig.getVMArguments();
            String[] vmArgumentsFromLaunchConfig = new String[0];
            if (vmArgsInConfig != null) {
                vmArgumentsFromLaunchConfig = DebugPlugin.parseArguments(vmArgsInConfig);
            }
            String[] vmArgumentsNew = new String[vmArguments.length + vmArgumentsFromLaunchConfig.length];
            System.arraycopy(vmArguments, 0, vmArgumentsNew, 0, vmArguments.length);
            System.arraycopy(vmArgumentsFromLaunchConfig, 0, vmArgumentsNew, vmArguments.length,
                    vmArgumentsFromLaunchConfig.length);
            vmConfig.setVMArguments(vmArgumentsNew);
        }
    }

    private void resetLaunchAndTestRun() {
        trace("Reset launch and test run."); //$NON-NLS-1$
        launchStartTime = 0;
    }

    private void setVmConfigMaxHeapSize(VMRunnerConfiguration vmConfig, String maxHeapSize) {
        String testRunnerMaxHeapSizeArg = "-Xmx" + maxHeapSize + "m"; //$NON-NLS-1$ //$NON-NLS-2$
        vmConfig.setVMArguments(new String[] { testRunnerMaxHeapSizeArg });
    }

    /**
     * Launch the given configuration in an ui thread if no active workbench window is available,
     * otherwise jobLaunch in current thread.
     */
    private void lauchInUiThreadIfNecessary(final ILaunchConfiguration launchConfiguration, final String mode) {
        if (IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() == null) {
            UIJob uiJob = new UIJob("IPS Testrunner") { //$NON-NLS-1$
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    trace("Lauch configuration (" + launchConfiguration.getName() + ") in UI Job 'IPS Testrunner'"); //$NON-NLS-1$ //$NON-NLS-2$
                    try {
                        launchConfiguration.launch(mode, monitor);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                    return Job.ASYNC_FINISH;
                }

            };
            uiJob.setSystem(true);
            trace("Run UI Job..."); //$NON-NLS-1$
            uiJob.schedule();
        } else {
            trace("Lauch configuration: " + launchConfiguration.getName()); //$NON-NLS-1$
            DebugUITools.launch(launchConfiguration, mode);
        }
    }

    /**
     * Creates a dummy non persistent lauch configuration
     */
    private ILaunchConfiguration createConfiguration(String classpathRepositories,
            String testsuites,
            ILaunchManager manager) throws CoreException {

        String confName = Messages.IpsTestRunner_lauchConfigurationDefaultName;
        List<String> tests = AbstractIpsTestRunner.extractListFromString(testsuites);
        if (tests.size() == 1) {
            String testName = tests.get(0);
            if (StringUtils.isNotEmpty(testName)) {
                confName = StringUtil.unqualifiedName(testName);
            }
        }
        confName = manager.generateLaunchConfigurationName(confName);

        ILaunchConfigurationType configType = getLaunchConfigType();
        trace("Create jobLaunch configuration: " + confName); //$NON-NLS-1$
        ILaunchConfigurationWorkingCopy wc = createNewLaunchConfiguration(configType, confName, classpathRepositories,
                testsuites);
        ILaunchConfiguration[] confs = manager.getLaunchConfigurations(configType);
        for (ILaunchConfiguration conf : confs) {
            if (checkLaunchConfigurationSameAttributes(conf, wc)) {
                // reuse existing configuration
                trace("Existing jobLaunch configuration found, reuse: " + conf.getName()); //$NON-NLS-1$
                wc = createNewLaunchConfiguration(configType, conf.getName(), classpathRepositories, testsuites);
                wc.setAttributes(conf.getAttributes());
                break;
            }
        }
        return wc.doSave();
    }

    private boolean checkLaunchConfigurationSameAttributes(ILaunchConfiguration configuration,
            ILaunchConfigurationWorkingCopy wc) throws CoreException {

        if (configuration.getAttribute(ATTR_PACKAGEFRAGMENTROOT, "") //$NON-NLS-1$
                .equals(wc.getAttribute(ATTR_PACKAGEFRAGMENTROOT, "")) //$NON-NLS-1$
                && configuration.getAttribute(ATTR_TESTCASES, "").equals(wc.getAttribute(ATTR_TESTCASES, ""))) { //$NON-NLS-1$ //$NON-NLS-2$
            return true;
        }
        return false;
    }

    private ILaunchConfigurationWorkingCopy createNewLaunchConfiguration(ILaunchConfigurationType configType,
            String name,
            String classpathRepositories,
            String testsuites) throws CoreException {

        ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, name);
        wc.setAttribute(ATTR_PACKAGEFRAGMENTROOT, classpathRepositories);
        wc.setAttribute(ATTR_TESTCASES, testsuites);
        wc.setAttribute(ATTR_PROJECT, this.ipsProject.getName());
        wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
        wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
        return wc;
    }

    /**
     * Returns the config type of the lauch configuration
     */
    private ILaunchConfigurationType getLaunchConfigType() {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType launchConfigurationType = lm
                .getLaunchConfigurationType(ID_IPSTEST_LAUNCH_CONFIGURATION_TYPE);
        if (launchConfigurationType == null) {
            throw new RuntimeException("Lauch configuration type not found: " + ID_IPSTEST_LAUNCH_CONFIGURATION_TYPE); //$NON-NLS-1$
        }
        return launchConfigurationType;
    }

    /**
     * Sets the default source locator. The source container will evaluated as follows: a) all
     * source container from the given project b) all source container from the workspace c) all
     * sources attached to the libraries in the classpath
     */
    private void setDefaultSourceLocatorInternal(ILaunch launch, ILaunchConfiguration configuration)
            throws CoreException {

        ISourceLocator locator = getLaunchManager().newSourceLocator(configuration.getType().getSourceLocatorId());
        AbstractSourceLookupDirector sld = (AbstractSourceLookupDirector)locator;
        sld.initializeDefaults(configuration);

        // get source container from the project
        ISourceContainer sc = new ProjectSourceContainer(ipsProject.getProject(), true);
        List<ISourceContainer> sourceContainer = new ArrayList<>(
                Arrays.asList(sc.getSourceContainers()));

        // get source container from the workspace
        sc = new WorkspaceSourceContainer();
        sourceContainer.addAll(Arrays.asList(sc.getSourceContainers()));

        // get source container from the classpath
        List<IRuntimeClasspathEntry> classpaths = new ArrayList<>();
        classpaths.addAll(Arrays.asList(JavaRuntime.computeUnresolvedRuntimeClasspath(ipsProject.getJavaProject())));
        IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[classpaths.size()];
        classpaths.toArray(entries);
        IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
        ISourceContainer[] sourceContainers = JavaRuntime.getSourceContainers(resolved);

        sourceContainer.addAll(Arrays.asList(sourceContainers));
        sld.setSourceContainers(sourceContainer.toArray(new ISourceContainer[sourceContainer.size()]));

        launch.setSourceLocator(locator);
    }

    /**
     * Convenience method to get the jobLaunch manager.
     * 
     * @return the jobLaunch manager
     */
    protected ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    /**
     * Returns the max heap size for the test runner, should be passed as program argument
     * {@code -ipstestrunner.xmx <size>} (for example {@code"-ipstestrunner.xmx 512M"})
     */
    public String getMaxHEapSizeFromAppArgs() {
        String[] applicationArgs = Platform.getApplicationArgs();
        // do not process the last one as it will never have a parameter
        for (int i = 0; i < applicationArgs.length - 1; i++) {
            if ("-ipstestrunner.xmx".equalsIgnoreCase(applicationArgs[i])) { //$NON-NLS-1$
                return applicationArgs[i + 1];
            }
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public void terminate() throws CoreException {
        try {
            if (launch != null) {
                if (launch.canTerminate()) {
                    terminated = true;
                    launch.terminate();
                    notifyTestRunEnded("" + (System.currentTimeMillis() - testStartTime)); //$NON-NLS-1$
                    if (!testRunnerMonitor.isCanceled()) {
                        trace("Cancel test job."); //$NON-NLS-1$
                        job.cancel();
                    }
                }
            }
        } catch (DebugException e) {
            e.printStackTrace();
            throw new CoreException(new IpsStatus(e));
        }
    }

    /**
     * Establish socket connection to the socket test runner
     */
    private boolean connect() {
        boolean connected = true;
        try {
            ServerSocket server;
            server = new ServerSocket(port);
            server.setSoTimeout(ACCEPT_TIMEOUT);
            try {
                Socket socket = server.accept();
                try (socket) {
                    readMessage(socket);
                }
            } finally {
                server.close();
            }
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: illegalCatch
            /*
             * error durring socket listening notify the listener itself, because there is no
             * connection to a runner (connection failed)
             */
            if (!terminated) {
                IpsPlugin.log(e);
                notifyTestRunStarted(1, classpathRepositories, testsuites);
                notifyTestErrorOccured("", //$NON-NLS-1$
                        new String[] { Messages.IpsTestRunner_Error_CouldNotConnect + e.getLocalizedMessage() });
                connected = false;
            } else {
                // the test runner was terminated
                notifyTestRunEnded("" + (System.currentTimeMillis() - testStartTime)); //$NON-NLS-1$
            }
        }
        return connected;
    }

    private void readMessage(Socket socket) throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                parseMessage(line);
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Parse the incomming message and fire the messages events to the registered listener.
     */
    private void parseMessage(String line) {
        String newLine = line;
        trace(newLine);
        if (newLine.startsWith(SocketIpsTestRunner.ALL_TESTS_STARTED)) {
            // format:
            // SocketIpsTestRunner.ALL_TESTS_STARTED(<count>)
            // [<repositoryPackage>].[<testPackage>]:<testQualifiedName>{<testFullPath>},...
            int start = newLine.indexOf("(") + 1; //$NON-NLS-1$
            int count = Integer.parseInt(newLine.substring(start, newLine.indexOf(")"))); //$NON-NLS-1$
            String repositoryPackage = newLine.substring(newLine.indexOf("[") + 1, newLine.indexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
            newLine = newLine.substring(newLine.indexOf("].[") + 3); //$NON-NLS-1$
            String testSuitePackage = newLine.substring(0, newLine.indexOf("]")); //$NON-NLS-1$
            notifyTestRunStarted(count, repositoryPackage, testSuitePackage);
            newLine = newLine.substring(newLine.indexOf(":") + 1); //$NON-NLS-1$
            parseAllTestsStarted(newLine, count);
        } else if (newLine.startsWith(SocketIpsTestRunner.ALL_TESTS_FINISHED)) {
            String elapsedTime = newLine.substring(SocketIpsTestRunner.ALL_TESTS_FINISHED.length());
            notifyTestRunEnded(elapsedTime);
        } else if (newLine.startsWith(SocketIpsTestRunner.TEST_STARTED)) {
            // format: TEST_CASE_STARTED<qualifiedName>{<fullPath>}
            newLine = newLine.substring(SocketIpsTestRunner.TEST_STARTED.length());
            String testName = parseTestQualifiedName(newLine);
            String fullPath = parseTestFullPath(newLine);
            notifyTestEntry(testName, fullPath);
            notifyTestStarted(testName);
        } else if (newLine.startsWith(SocketIpsTestRunner.TEST_FINISHED)) {
            String testName = newLine.substring(SocketIpsTestRunner.TEST_FINISHED.length());
            notifyTestFinished(testName);
        } else if (newLine.startsWith(SocketIpsTestRunner.TEST_FAILED)) {
            // format: qualifiedName|testObject|testedAttribute|expectedValue|actualValue|message
            parseTestFailed(newLine);
        } else if (newLine.startsWith(SocketIpsTestRunner.TEST_ERROR)) {
            // format
            // qualifiedTestName{message}{StacktraceElem1}{StacktraceElem2}...{StacktraceElemN}
            errorDetailList = new ArrayList<>();
            String errorDetails = newLine.substring(SocketIpsTestRunner.TEST_ERROR.length());
            qualifiedTestName = errorDetails.substring(0, errorDetails.indexOf(BRACELEFT));
            parseErrorStack(errorDetailList, errorDetails);
        } else if (newLine.endsWith(SocketIpsTestRunner.TEST_ERROR_END)) {
            String errorDetails = newLine.substring(0, newLine.indexOf(SocketIpsTestRunner.TEST_ERROR_END));
            parseErrorStack(errorDetailList, errorDetails);
            notifyTestErrorOccured(qualifiedTestName,
                    errorDetailList == null ? new String[0] : errorDetailList.toArray(new String[0]));
            errorDetailList = null;
        } else if (errorDetailList != null) {
            // parse multi line stack elements
            parseErrorStack(errorDetailList, newLine);
        }
    }

    private void parseTestFailed(String newLine) {
        String failureDetailsLine = newLine.substring(SocketIpsTestRunner.TEST_FAILED.length());
        String qualifiedTest = failureDetailsLine.substring(0,
                failureDetailsLine.indexOf(SocketIpsTestRunner.TEST_FAILED_DELIMITERS));
        List<String> failureTokens = new ArrayList<>(6);
        while (failureDetailsLine.length() > 0) {
            String token = ""; //$NON-NLS-1$
            int end = failureDetailsLine.indexOf(SocketIpsTestRunner.TEST_FAILED_DELIMITERS);
            if (end == -1) {
                end = failureDetailsLine.length();
                token = failureDetailsLine;
                failureDetailsLine = ""; //$NON-NLS-1$
            } else {
                token = failureDetailsLine.substring(0, end);
                failureDetailsLine = failureDetailsLine.substring(end + 1);
            }
            failureTokens.add(token);
        }
        notifyTestFailureOccured(qualifiedTest, failureTokens.toArray(new String[failureTokens.size()]));
    }

    private void parseAllTestsStarted(String line, int count) {
        String newLine = line;
        // now parse all tests, format:
        // :[<testQualifiedName>{<testFullPath>},...]
        String[] testNames = new String[count];
        String[] testFullPaths = new String[count];
        int idx = 0;
        while (newLine.indexOf(",") >= 0) { //$NON-NLS-1$
            if (idx > count) {
                throw new RuntimeException(Messages.IpsTestRunner_Error_WrongTestProtocol);
            }
            testNames[idx] = parseTestQualifiedName(newLine);
            testFullPaths[idx] = parseTestFullPath(newLine);
            newLine = newLine.substring(newLine.indexOf(",") + 1); //$NON-NLS-1$
            idx++;
        }
        notifyTestEntries(testNames, testFullPaths);
    }

    private void trace(String line) {
        if (TRACE_IPS_TEST_RUNNER) {
            StringBuilder msg = new StringBuilder(line.length() + 40);
            msg.append("IpsTestRunner "); //$NON-NLS-1$
            msg.append(new Date());
            msg.append(line);
            System.out.println(msg.toString());
        }
    }

    private String parseTestQualifiedName(String line) {
        String testName = line.substring(0, line.indexOf(BRACELEFT));
        return testName;
    }

    private String parseTestFullPath(String line) {
        String fullPath = line.substring(line.indexOf(BRACELEFT) + 1, line.indexOf(BRACERIGHT));
        return fullPath;
    }

    private void parseErrorStack(ArrayList<String> errorDetailList, String errorDetails) {
        String newErrorDetails = errorDetails;
        if (newErrorDetails.length() == 0) {
            return;
        }

        newErrorDetails = newErrorDetails.replaceAll("\t", ""); //$NON-NLS-1$ //$NON-NLS-2$
        if (newErrorDetails.indexOf(BRACELEFT) == -1) {
            errorDetailList.add(newErrorDetails);
        } else {
            // fix brackets, in case of multiline stack elements
            if (newErrorDetails.indexOf(BRACERIGHT) == -1) {
                newErrorDetails += BRACERIGHT;
            } else if (newErrorDetails.indexOf(BRACELEFT) > newErrorDetails.indexOf(BRACERIGHT)) {
                newErrorDetails = BRACELEFT + newErrorDetails;
            }
            // parse stack elements
            while (newErrorDetails.indexOf(BRACERIGHT) >= 0) {
                String stackElem = newErrorDetails.substring(newErrorDetails.indexOf(BRACELEFT) + 1,
                        newErrorDetails.indexOf(BRACERIGHT));
                errorDetailList.add(stackElem);
                if (newErrorDetails.indexOf(BRACELEFT) >= 0) {
                    newErrorDetails = newErrorDetails.substring(newErrorDetails.indexOf(BRACERIGHT) + 1);
                }
            }
        }
    }

    private IVMInstall getVMInstall(IJavaProject project) throws CoreException {
        IVMInstall vmInstall = null;
        if (project != null) {
            vmInstall = JavaRuntime.getVMInstall(project);
        }

        if (vmInstall == null) {
            vmInstall = JavaRuntime.getDefaultVMInstall();
        }
        return vmInstall;
    }

    /**
     * Return all classpath enties from the given project
     */
    private String[] computeClasspath(IJavaProject project) throws CoreException {
        String[] defaultPath = JavaRuntime.computeDefaultRuntimeClassPath(project);
        String[] classPath = new String[defaultPath.length];
        System.arraycopy(defaultPath, 0, classPath, 0, defaultPath.length);

        return classPath;
    }

    /**
     * Returns a list off repository packages of the given ips project and its referenced projects
     * and referenced projects by the referenced projects ...
     */
    private List<String> getAllRepositoryPackagesAsString(IIpsProject ipsProject) throws CoreException {
        List<String> repositoryPackages = new ArrayList<>();
        getRepositoryPackages(ipsProject, repositoryPackages);
        List<IIpsProject> ipsProjects = ipsProject.getAllReferencedIpsProjects();
        for (IIpsProject ipsProject2 : ipsProjects) {
            getRepositoryPackages(ipsProject2, repositoryPackages);
        }
        for (ITocTreeFromDependencyManagerLoader extension : IpsPlugin.getDefault().getIpsCoreExtensions()
                .getTocTreeFromDependencyManagerLoader()) {
            if (extension.isResponsibleFor(ipsProject)) {
                extension.loadTocTreeFromDependencyManager(ipsProject, repositoryPackages);
            }
        }
        return repositoryPackages;
    }

    /**
     * Adds all repository packages of the given ips project to the given list. Add the repository
     * package only if the toc file exists.
     */
    private void getRepositoryPackages(IIpsProject ipsProject, List<String> repositoryPackages) throws CoreException {
        IIpsPackageFragmentRoot[] ipsRoots = ipsProject.getIpsPackageFragmentRoots();
        List<IIpsPackageFragmentRoot> ipsRootsList = Arrays.asList(ipsRoots);
        // need reverse sort order: see IpsTestAction#ipsProjectSelected
        Collections.reverse(ipsRootsList);
        for (IIpsPackageFragmentRoot ipsRoot : ipsRootsList) {
            IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
            IFile tocFile = builderSet.getRuntimeRepositoryTocFile(ipsRoot);
            if (tocFile != null && tocFile.exists()) {
                String repositoryPck = builderSet.getRuntimeRepositoryTocResourceName(ipsRoot);
                if (repositoryPck != null && !repositoryPackages.contains(repositoryPck)) {
                    repositoryPackages.add(repositoryPck);
                }
            }
        }
    }

    /**
     * Adds the given ips test run listener to the collection of listeners
     */
    @Override
    public void addIpsTestRunListener(IIpsTestRunListener newListener) {
        fIpsTestRunListeners.add(newListener);
    }

    /**
     * Removes the given ips test run listener from the collection of listeners
     */
    @Override
    public void removeIpsTestRunListener(IIpsTestRunListener listener) {
        if (fIpsTestRunListeners != null) {
            fIpsTestRunListeners.remove(listener);
        }
    }

    /**
     * Returns all registered ips test run listener.
     */
    @Override
    public List<IIpsTestRunListener> getIpsTestRunListener() {
        return fIpsTestRunListeners;
    }

    private void notifyTestEntry(String qualifiedName, String fullPath) {
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testTableEntry(qualifiedName, fullPath);
        }
    }

    private void notifyTestEntries(String[] qualifiedNames, String[] fullPaths) {
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testTableEntries(qualifiedNames, fullPaths);
        }
    }

    private void notifyTestStarted(String qualifiedTestName) {
        // check if the test runner is canceled
        if (testRunnerMonitor.isCanceled()) {
            try {
                terminate();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        testRunnerMonitor.subTask(qualifiedTestName);

        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testStarted(qualifiedTestName);
        }
    }

    private void notifyTestFinished(String qualifiedTestName) {
        testRunnerMonitor.worked(1);
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testFinished(qualifiedTestName);
        }
    }

    private void notifyTestFailureOccured(String testFailureOccured, String[] failureDetails) {
        // defensive copy to avoid concurrent modification exceptions
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testFailureOccured(testFailureOccured, failureDetails);
        }
    }

    private void notifyTestRunStarted(int count, String repositoryPackage, String testPackage) {
        testRunnerMonitor.beginTask(Messages.IpsTestRunner_Job_Name, count);
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testRunStarted(count, repositoryPackage, testPackage);
        }
    }

    private void notifyTestRunEnded(String elapsedTime) {
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testRunEnded(elapsedTime);
        }
    }

    private void notifyTestErrorOccured(String qualifiedTestName, String[] errorDetails) {
        List<IIpsTestRunListener> copy = new CopyOnWriteArrayList<>(fIpsTestRunListeners);
        for (IIpsTestRunListener listener : copy) {
            listener.testErrorOccured(qualifiedTestName, errorDetails);
        }
    }

    @Override
    public void startTestRunnerJob(String classpathRepository, String testsuite) throws CoreException {
        startTestRunnerJob(classpathRepository, testsuite, null, null);
    }

    /**
     * Starts the test runner.
     */
    @Override
    public synchronized void startTestRunnerJob(String classpathRepository,
            String testsuite,
            String mode,
            ILaunch launch) throws CoreException {

        trace("Start test runner Job"); //$NON-NLS-1$

        if (isRunningTestRunner()) {
            MessageDialog.openWarning(null, Messages.IpsTestRunner_InfoDialogTestCouldNotStarted_Title,
                    Messages.IpsTestRunner_InfoDialogTestAlreadyRunning_Text);
            trace("Cancel test runner start because a test run is already started."); //$NON-NLS-1$
            // terminate the given jobLaunch if possible
            terminateLaunch(launch);
            return;
        }

        if (ipsProject == null) {
            /*
             * if no project is given, try to extract and find the project from the given classpath
             * repository, this could be happen if the test case are started directly by using the
             * run history without further project information
             */
            ipsProject = getIpsProjectFromTocPath(classpathRepository);
        }

        if (ipsProject == null) {
            MessageDialog.openWarning(null, Messages.IpsTestRunner_InfoDialogTestCouldNotStarted_Title,
                    Messages.IpsTestRunner_Error_ProjectTheTestBelongsToNotFound);
            trace("Cancel test run, no project found."); //$NON-NLS-1$
            // terminate the given jobLaunch if possible
            terminateLaunch(launch);
            return;
        }

        Boolean javaProjectErrorFree = ipsProject.isJavaProjectErrorFree(true);
        // check if there are errors in the java project and referenced java projects
        if (Boolean.FALSE.equals(javaProjectErrorFree)) {
            MessageDialog.openWarning(null, Messages.IpsTestRunner_InfoDialogTestCouldNotStarted_Title,
                    Messages.IpsTestRunner_InfoDialogErrorsInProject_Text);
            trace("Cancel test runner start because the project contains errors."); //$NON-NLS-1$
            terminateLaunch(launch);
            return;
        }
        // check if the java project was build
        if (javaProjectErrorFree == null) {
            MessageDialog.openWarning(null, Messages.IpsTestRunner_InfoDialogTestCouldNotStarted_Title,
                    Messages.IpsTestRunner_InfoDialogProjectWasNotBuild_Text);
            trace("Cancel test runner start because the project wasn't build."); //$NON-NLS-1$
            terminateLaunch(launch);
            return;
        }

        /*
         * first check the heap size, to display an error if there is a wrong value, this is the
         * last chance to display the error, otherwise the error will only be logged in the
         * background
         */
        testRunnerMaxHeapSize = IpsPlugin.getDefault().getIpsPreferences().getIpsTestRunnerMaxHeapSize();
        if (!StringUtils.isNumeric(testRunnerMaxHeapSize)) {
            throw new CoreException(
                    new IpsStatus(NLS.bind(Messages.IpsTestRunner_Error_WrongHeapSize, testRunnerMaxHeapSize)));
        }

        job = new TestRunnerJob(this, classpathRepository, testsuite, mode, launch);

        job.setSystem(false);
        /*
         * we don't need to specify a rule here, because the ips test runner didn't depend on a
         * rule, there will be no blocking events (e.g. builder could be depend on job finishing or
         * something else) IWorkspace workspace = ResourcesPlugin.getWorkspace();
         * job.setRule(workspace.getRoot());
         */
        try {
            /*
             * wait until the build has finished, the join invocation will block until the
             * auto-build job completes, or until the join is interrupted or canceled.
             */
            Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
        } catch (OperationCanceledException ignored) {
            IpsPlugin.log(ignored);
        } catch (InterruptedException ignored) {
            IpsPlugin.log(ignored);
        }
        job.schedule();
    }

    private void terminateLaunch(ILaunch launch) throws DebugException {
        if (launch != null && launch.canTerminate()) {
            launch.terminate();
        }
    }

    @Override
    public boolean isRunningTestRunner() {
        trace("Check if a new test runner can start."); //$NON-NLS-1$
        if (isInsideTimeIntervall(launchStartTime)) {
            trace("Cannot start the test runner, a test runner is already running."); //$NON-NLS-1$
            return true;
        }
        return false;
    }

    /**
     * Check is the current system time is greater than the last start time plus a specific
     * intervall.
     */
    private boolean isInsideTimeIntervall(long timeToCheck) {
        return System.currentTimeMillis() < (timeToCheck + MAX_START_TIME_INTERVAL);
    }

    /**
     * Returns an array of environment variables to be used when launching the given configuration
     * or <code>null</code> if unspecified.
     */
    public String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
        return DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
    }

    /**
     * Job class to run the selected tests.
     */
    private class TestRunnerJob extends WorkspaceJob {

        private IpsTestRunner testRunner;
        private String classpathRepository;
        private String testsuite;
        private String mode;
        private ILaunch jobLaunch;

        public TestRunnerJob(IpsTestRunner testRunner, String classpathRepository, String testsuite, String mode,
                ILaunch launch) {

            super(Messages.IpsTestRunner_Job_Name);
            this.testRunner = testRunner;
            this.classpathRepository = classpathRepository;
            this.testsuite = testsuite;
            this.mode = mode;
            this.jobLaunch = launch;
        }

        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) {
            try {
                testRunnerMonitor = monitor;

                if (!monitor.isCanceled()) {
                    if (mode != null) {
                        testRunner.run(classpathRepository, testsuite, mode, jobLaunch);
                    } else {
                        testRunner.run(classpathRepository, testsuite, ILaunchManager.RUN_MODE, jobLaunch);
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return Status.OK_STATUS;
        }

    }

}
