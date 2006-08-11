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

package org.faktorips.devtools.core.ui.views.testrunner;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;

/**
 * A ViewPart that shows the results of a ips test run.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunnerViewPart extends ViewPart implements IIpsTestRunListener {
	public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.testRunner"; //$NON-NLS-1$
	
	static final int REFRESH_INTERVAL = 200;
	
	/* Ui components */
	private Composite fCounterComposite;
	private IpsTestCounterPanel fCounterPanel;
	private IpsTestProgressBar fProgressBar;
	private SashForm fSashForm;
	private FailurePane fFailurePane;
	private TestRunPane fTestRunPane;
	private Composite fParent;
	
	protected volatile String fStatus = "";
	
	/*
	 * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
	 * <code>VIEW_ORIENTATION_VERTICAL</code>, or <code>VIEW_ORIENTATION_AUTOMATIC</code>.
	 */
	private int fOrientation= VIEW_ORIENTATION_AUTOMATIC;
	private int fCurrentOrientation;
	
	/* Actions */
	private Action fRerunLastTestAction;
	private ToggleOrientationAction[] fToggleOrientationActions;
	
	/* Sash form orientations */
	static final int VIEW_ORIENTATION_VERTICAL = 0;
	static final int VIEW_ORIENTATION_HORIZONTAL = 1;
	static final int VIEW_ORIENTATION_AUTOMATIC = 2;
	
	/* Queue used for processing Tree Entries */
	private List fTableEntryQueue = new ArrayList();
	
	/* Is the UI disposed */
	private boolean fIsDisposed= false;
	
	/* Indicates an instance of TreeEntryQueueDrainer is already running, or scheduled to */
	private boolean fQueueDrainRequestOutstanding;
	
 	/*
 	 * Number of executed tests during a test run
 	 */
	private volatile int fExecutedTests;
	/*
	 * Number of errors during this test run
	 */
	private volatile int fErrorCount;
	/*
	 * Number of failures during this test run
	 */
	private volatile int fFailureCount;
	
	private UpdateUIJob fUpdateJob;
	
	private int testRuns=0;
	private int testId;
	
	// Test last test run context
	private String repositoryPackage;
	private String testPackage;
	
	// The project which contains the runned tests 
	private IJavaProject fTestProject;
	
	/*
	 * Action class to rerun a test.
	 */
	private class RerunLastAction extends Action {
		public RerunLastAction() {
			setText("Rerun Last Test"); 
			setToolTipText("Rerun Last Test"); 
			setDisabledImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
			setHoverImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
			setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
			setEnabled(false);
		}
		
		public void run(){
			rerunTestRun();
		}
	}
	
	/*
	 * Job class to run the test runner.
	 */
	private class TestRunnerJob extends WorkspaceJob {
		private String classpathRepository;
		private String testsuite;
		private IIpsTestRunner testRunner;
		
		public TestRunnerJob(String classpathRepository, String testsuite) {
			super("FaktorIps Test Job");
			this.classpathRepository = 
			this.classpathRepository = classpathRepository;
			this.testsuite = testsuite;
			this.testRunner = IpsPlugin.getDefault().getIpsTestRunner();
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			
			try {

				testRunner.run(classpathRepository, testsuite);
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			return Status.OK_STATUS;
		}
		
		public IIpsTestRunner getTestRunner(){
			return testRunner;
		}
	}	
	
	/*
	 * Runs the last runned test.
	 */
	private void rerunTestRun()  {
		TestRunnerJob job = new TestRunnerJob(repositoryPackage, testPackage);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		job.setRule(workspace.getRoot());
		job.schedule();
	}
	
	/*
	 * UIJob to refresh the counter in th user interface.
	 */
	class UpdateUIJob extends UIJob {
		private boolean fRunning= true; 
		
		public UpdateUIJob(String name) {
			super(name);
			setSystem(true);
		}
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!isDisposed()) { 
				doShowStatus();
				refreshCounters();
			}
			schedule(REFRESH_INTERVAL);
			return Status.OK_STATUS;
		}
		
		public void stop() {
			fRunning= false;
		}
		public boolean shouldSchedule() {
			return fRunning;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	 public void setFocus() {
	 }
	 
	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent) {
		fParent = parent;
		
		GridLayout gridLayout= new GridLayout(); 
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		parent.setLayout(gridLayout);

		configureToolBar();
		
		fCounterComposite = createProgressCountPanel(parent);
		fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		SashForm sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		registerAsIpsTestRunListener();
	}

	/**
	 * Returns the failure pane.
	 */
	public FailurePane getFailurePane() {
		return fFailurePane;
	}

	/**
	 * Returns the test run pane.
	 */
	public TestRunPane getTestRunPane() {
		return fTestRunPane;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
	}
	
	private Composite createProgressCountPanel(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 1;
		
		fCounterPanel = new IpsTestCounterPanel(composite);
		fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		fProgressBar = new IpsTestProgressBar(composite);
		fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}
	
	private SashForm createSashForm(Composite parent) {
		fSashForm= new SashForm(parent, SWT.VERTICAL);
		
		ViewForm top= new ViewForm(fSashForm, SWT.NONE);		
		CLabel label= new CLabel(top, SWT.NONE);
		label.setText("Test Runs"); 
		label.setImage(IpsPlugin.getDefault().getImage("TestCaseRun.gif"));		
		top.setTopLeft(label);
		fTestRunPane = new TestRunPane(top, this);
		top.setContent(fTestRunPane.getComposite()); 
		
		ViewForm bottom= new ViewForm(fSashForm, SWT.NONE);
		label= new CLabel(bottom, SWT.NONE);
		label.setText("Failure Details"); 
		label.setImage(IpsPlugin.getDefault().getImage("failures.gif"));
		bottom.setTopLeft(label);

		fFailurePane = new FailurePane(bottom);
		bottom.setContent(fFailurePane.getComposite()); 
		
		fSashForm.setWeights(new int[]{50, 50});
		return fSashForm;
	}

	private void configureToolBar() {
		IActionBars actionBars= getViewSite().getActionBars();
		IToolBarManager toolBar= actionBars.getToolBarManager();
		IMenuManager viewMenu = actionBars.getMenuManager();
		
		fRerunLastTestAction= new RerunLastAction();
		
		fToggleOrientationActions =
			new ToggleOrientationAction[] {
				new ToggleOrientationAction(this, VIEW_ORIENTATION_VERTICAL),
				new ToggleOrientationAction(this, VIEW_ORIENTATION_HORIZONTAL),
				new ToggleOrientationAction(this, VIEW_ORIENTATION_AUTOMATIC)};
		

		toolBar.add(fRerunLastTestAction);
		
		for (int i = 0; i < fToggleOrientationActions.length; ++i)
			viewMenu.add(fToggleOrientationActions[i]);		

		actionBars.updateActionBars();
		
		fRerunLastTestAction.setEnabled(false);
	}
	
	private class ToggleOrientationAction extends Action {
		private final int fActionOrientation;
		
		public ToggleOrientationAction(IpsTestRunnerViewPart v, int orientation) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			if (orientation == IpsTestRunnerViewPart.VIEW_ORIENTATION_HORIZONTAL) {
				setText("Horizontal View Orientation"); 
				setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/th_horizontal.gif")); //$NON-NLS-1$				
			} else if (orientation == IpsTestRunnerViewPart.VIEW_ORIENTATION_VERTICAL) {
				setText("Vertical View Orientation"); 
				setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/th_vertical.gif")); //$NON-NLS-1$				
			} else if (orientation == IpsTestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC) {
				setText("Automatic View Orientation");  
				setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/th_automatic.gif")); //$NON-NLS-1$				
			}
			fActionOrientation = orientation;
		}
		
		public int getOrientation() {
			return fActionOrientation;
		}
		
		public void run() {
			if (isChecked()) {
				fOrientation = fActionOrientation;
				computeOrientation();
			}
		}		
	}

	private void computeOrientation() {
		if (fOrientation != VIEW_ORIENTATION_AUTOMATIC) {
			fCurrentOrientation = fOrientation;
			setOrientation(fCurrentOrientation);
		}
		else {
			Point size= fParent.getSize();
			if (size.x != 0 && size.y != 0) {
				if (size.x > size.y) 
					setOrientation(VIEW_ORIENTATION_HORIZONTAL);
				else 
					setOrientation(VIEW_ORIENTATION_VERTICAL);
			}
		}
	}
	
	private void setOrientation(int orientation) {
		if ((fSashForm == null) || fSashForm.isDisposed())
			return;
		boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
		fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
		for (int i = 0; i < fToggleOrientationActions.length; ++i)
			fToggleOrientationActions[i].setChecked(fOrientation == fToggleOrientationActions[i].getOrientation());
		fCurrentOrientation = orientation;
		GridLayout layout= (GridLayout) fCounterComposite.getLayout();
		setCounterColumns(layout); 
		fParent.layout();
	}
	
	private void setCounterColumns(GridLayout layout) {
		if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL)
			layout.numColumns= 2; 
		else
			layout.numColumns= 1;
	}

	public synchronized void dispose(){
		fIsDisposed = true;
		IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
		testRunner.removeIpsTestRunListener(this);
	}
	
	private boolean isDisposed() {
		return fIsDisposed || fCounterPanel.isDisposed();
	}
	
	private void doShowStatus() {
		setContentDescription(fStatus);
	}

	public void setInfoMessage(final String message) {
		fStatus= message;
	}
	
	private class TableEntryQueueDrainer implements Runnable {
		private String testId;
		TableEntryQueueDrainer(String testId){
			this.testId = testId;
		}
		public void run() {
			while (true) {
				TestCaseEntry testCaseEntry;
				synchronized (fTableEntryQueue) {
					if (fTableEntryQueue.isEmpty() || isDisposed()) {
						fQueueDrainRequestOutstanding = false;
						return;
					}
					testCaseEntry = (TestCaseEntry) fTableEntryQueue.remove(0);
				}
				fTestRunPane.newTableEntry(testId, testCaseEntry.getQualifiedName(), testCaseEntry.fullPath);
			}
		}
	}
	
	// inner class to represent an ips test case
	private class TestCaseEntry {
		private String qualifiedName;
		private String fullPath;
		public TestCaseEntry(String qualifiedName, String fullPath){
			this.qualifiedName = qualifiedName;
			this.fullPath = fullPath;
		}
		public String getFullPath() {
			return fullPath;
		}
		public String getQualifiedName() {
			return qualifiedName;
		}
		
	}
	
	public Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}
	 
	private void stopUpdateJobs() {
		if (fUpdateJob != null) {
			fUpdateJob.stop();
			fUpdateJob= null;
		}
	}
	
	private void reset(final int testCount) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) 
					return;
				fCounterPanel.reset();
				fProgressBar.reset();
				start(testCount);
			}
		});
		fExecutedTests= 0;
		fFailureCount= 0;
		fErrorCount= 0;
		resetTestId();
		aboutToStart();
	}
	
	private void resetProgressBar(final int total) {
		fProgressBar.reset();
		fProgressBar.setMaximum(total);
	}

	private void handleEndTest() {
		fProgressBar.step(fFailureCount + fErrorCount);
	}
	
	private void refreshCounters() {
		fCounterPanel.setErrorValue(fErrorCount);
		fCounterPanel.setFailureValue(fFailureCount);
		fCounterPanel.setRunValue(fExecutedTests);
		fProgressBar.refresh(fErrorCount+fFailureCount > 0);
	}	
	
	private void start(final int total) {
		resetProgressBar(total);
		fCounterPanel.setTotal(total);
		fCounterPanel.setRunValue(0);	
	}
	
	/*
	 * Register self as ips test run listener. 
	 */
	private void registerAsIpsTestRunListener() {
		IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
		testRunner.addIpsTestRunListener(this);
	}

	//
	// Inform ui panes about test runs
	//
	
	private void aboutToStart() {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (!isDisposed())
					fTestRunPane.aboutToStart();
					fFailurePane.aboutToStart();
				}
		});
	}
	
	private void postSyncRunnable(Runnable r) {
		if (!isDisposed())
			getDisplay().syncExec(r);
	}

	private void postStartTest(final String testId, final String qualifiedTestName) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				fTestRunPane.startTest(testId, qualifiedTestName);
			}
		});
	}
	
	private void postErrorInTest(final String testId, final String qualifiedTestName, final String[] errorDetails) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				fTestRunPane.errorInTest(testId, qualifiedTestName, errorDetails);
			}
		});
	}
	
	private void postEndTest(final String testId, final String qualifiedTestName) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				handleEndTest();
				fTestRunPane.endTest(testId, qualifiedTestName);
			}
		});	
	}

	private void postFailureTest(final String testId, final String[] failureDetails) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				fTestRunPane.failureTest(testId, failureDetailsToString(failureDetails));
			}
		});
	}
	
	/*
	 * Converts the given failure details to one failure detail row.
	 */
	private String failureDetailsToString(String[] failureDetails){
		String failureFormat= "Failure in \"{0}\":";
		String failureActual = " but was: \"{4}\"";
		String failureExpected = " expected: \"{3}\"";
		String failureFormatAttribute= ". Attribute: \"{2}\"";
		String failureFormatObject= ". Object: \"{1}\"";
		if (failureDetails.length>3)
			failureFormat= failureFormat + (failureDetails[3]!=null?failureExpected:"");
		if (failureDetails.length>4)
			failureFormat= failureFormat + (failureDetails[4]!=null?failureActual:"");		
		if (failureDetails.length>1)
			failureFormat= failureFormat + (failureDetails[1]!=null?failureFormatObject:"");
		if (failureDetails.length>2)
			failureFormat= failureFormat + (failureDetails[2]!=null?failureFormatAttribute:"");		
		return MessageFormat.format(failureFormat, failureDetails); 
	}
	
	//
	// Helper functions to generate unique test id's to identify the test in the test run ui control.
	//
	private void resetTestId(){
		testRuns++;
		testId = 0;
	}
	private String nextTestId(){
		return "" + testRuns + "." + ++testId;
	}
	private String getTestId(){
		return "" + testRuns + "." + testId;
	}
	
	//
	// Listener methods
	//

	/**
	 * {@inheritDoc}
	 */
	public void testFailureOccured(String[] failureDetails) {
	    fFailureCount++;
	    postFailureTest(getTestId(), failureDetails);
	}

	/**
	 * {@inheritDoc}
	 */
	public void testFinished(String qualifiedTestName) {
		fExecutedTests++;
		postEndTest(getTestId(), qualifiedTestName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void testStarted(String qualifiedTestName) {
		fRerunLastTestAction.setEnabled(true);
		postStartTest(getTestId(), qualifiedTestName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void testTableEntry(final String qualifiedName, final String fullPath) {
		String testId = nextTestId();
		synchronized(fTableEntryQueue) {
			fTableEntryQueue.add(new TestCaseEntry(qualifiedName, fullPath));
			if (!fQueueDrainRequestOutstanding) {
				fQueueDrainRequestOutstanding = true;
				if (!isDisposed())
					getDisplay().asyncExec(new TableEntryQueueDrainer(testId));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void testRunStarted(int testCount, String repositoryPackage, String testPackage){
		this.repositoryPackage = repositoryPackage;
		this.testPackage = testPackage;
		
		reset(testCount);
		fExecutedTests++;
		fRerunLastTestAction.setEnabled(true);
		
		stopUpdateJobs();
		fUpdateJob = new UpdateUIJob("FaktorIps Test Starter Job"); 
		fUpdateJob.schedule(0);
		
		// store the project which contains the tests will be used to open the test in the editor
		fTestProject = IpsPlugin.getDefault().getIpsTestRunner().getJavaProject();
	}

	/**
	 * {@inheritDoc}
	 */
	public void testRunEnded(){
		fExecutedTests--;
		stopUpdateJobs();
	}

	/**
	 * {@inheritDoc}
	 */
    public void testErrorOccured(String qualifiedTestName, String[] errorDetails){
    	fErrorCount ++;
    	postErrorInTest(getTestId(), qualifiedTestName, errorDetails);
    }
    
	/**
	 * Informs that the selection of a test result changed.
	 * 
	 * @param testCaseDetails contains the details of the selcted test result.
	 */
	public void selectionOfTestCaseChanged(String[] testCaseFailures) {
		fFailurePane.showFailureDetails(testCaseFailures);
	}

	public IJavaProject getLaunchedProject() {
		return fTestProject;
	}
}
