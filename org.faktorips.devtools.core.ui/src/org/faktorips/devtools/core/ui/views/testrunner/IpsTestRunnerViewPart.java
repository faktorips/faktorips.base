/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.testrunner;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.StringUtil;

/**
 * A ViewPart that shows the results of a ips test run.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestRunnerViewPart extends ViewPart implements IIpsTestRunListener {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.testRunner"; //$NON-NLS-1$

    // Ui refresh intervall
    static final int REFRESH_INTERVAL = 200;

    /* Ui components */
    private Composite fCounterComposite;
    private IpsTestCounterPanel fCounterPanel;
    private IpsTestProgressBar fProgressBar;
    private SashForm fSashForm;
    private FailurePane fFailurePane;
    private TestRunPane fTestRunPane;
    private Composite fParent;

    // Contains the status message
    protected volatile String fStatus = ""; //$NON-NLS-1$

    /*
     * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
     * <code>VIEW_ORIENTATION_VERTICAL</code>, or <code>VIEW_ORIENTATION_AUTOMATIC</code>.
     */
    private int fOrientation = VIEW_ORIENTATION_AUTOMATIC;
    private int fCurrentOrientation;

    /* Indicates if the scroll is locked or not locked */
    private boolean scrollLocked;

    /* Actions */
    private Action fStopTestRunAction;
    private Action fRerunLastTestAction;
    private Action fNextAction;
    private Action fPreviousAction;
    private Action fShowErrorsOrFailuresOnlyAction;
    private Action fLockScrollAction;
    private ToggleOrientationAction[] fToggleOrientationActions;

    /* Sash form orientations */
    static final int VIEW_ORIENTATION_VERTICAL = 0;
    static final int VIEW_ORIENTATION_HORIZONTAL = 1;
    static final int VIEW_ORIENTATION_AUTOMATIC = 2;

    // Persistence tags.
    static final String LAYOUT_MEMENTO = "layout"; //$NON-NLS-1$
    static final String TAG_RATIO = "ratio"; //$NON-NLS-1$
    static final String TAG_ORIENTATION = "orientation"; //$NON-NLS-1$
    static final String TAG_SCROLL = "scroll"; //$NON-NLS-1$

    private IMemento fMemento;

    /* Queue used for processing Tree Entries */
    private List<TestCaseEntry> fTableEntryQueue = new ArrayList<TestCaseEntry>();

    /* Is the UI disposed */
    private boolean fIsDisposed = false;

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

    /* Indicates that there was an failure */
    private boolean isFailure;

    private UpdateUIJob fUpdateJob;

    private Clipboard clipboard;

    /*
     * A Job that runs as long as a test run is running. It is used to get the progress feedback for
     * running jobs in the view.
     */
    private IpsTestIsRunningJob ipsTestIsRunningJob;
    private ILock ipsTestIsRunningLock;

    public static final Object FAMILY_IPT_TEST_RUN = new Object();

    private int testRuns = 0;
    private int testId;

    // Test last test run context
    private String classpathRepository;
    private String testPackage;

    // The project which contains the runned tests
    private IJavaProject fTestProject;

    // Contains the map to do the mapping between the test case ids (unique id in the table run pane
    // table) and the test case qualified name
    private Map<String, String> testId2TestQualifiedNameMap = new HashMap<String, String>();

    private ResourceManager resourceManager;

    public IpsTestRunnerViewPart() {
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    /*
     * Action to lock the scroll
     */
    private class LockScrollAction extends Action {
        public LockScrollAction() {
            super(Messages.IpsTestRunnerViewPart_Menu_ScrollLock, IAction.AS_CHECK_BOX);
            setToolTipText(Messages.IpsTestRunnerViewPart_Menu_ScrollLockTooltip);
            setDisabledImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("dlcl16/lock.gif")); //$NON-NLS-1$
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/lock.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(imageDescriptor);
            setImageDescriptor(imageDescriptor);
        }

        @Override
        public void run() {
            scrollLocked = !scrollLocked;
        }
    }

    /*
     * Action to stop the currently running test.
     */
    private class StopTestRunAction extends Action {
        public StopTestRunAction() {
            setText(Messages.IpsTestRunnerViewPart_Action_StopTest);
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_StopTest_ToolTip);
            setDisabledImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("dlcl16/stop.gif")); //$NON-NLS-1$
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/stop.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(imageDescriptor);
            setImageDescriptor(imageDescriptor);
            setEnabled(false);
        }

        @Override
        public void run() {
            try {
                IpsPlugin.getDefault().getIpsTestRunner().terminate();
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /*
     * Action to rerun a test.
     */
    private class RerunLastAction extends Action {
        public RerunLastAction() {
            setText(Messages.IpsTestRunnerViewPart_Action_RerunLastTest_Text);
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_RerunLastTest_ToolTip);
            setDisabledImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "elcl16/relaunch.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(imageDescriptor);
            setImageDescriptor(imageDescriptor);
            setEnabled(false);
        }

        @Override
        public void run() {
            try {
                rerunTestRun();
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /*
     * Action to select the next error or failure
     */
    private class ShowNextErrorAction extends Action {
        public ShowNextErrorAction() {
            setText(Messages.IpsTestRunnerViewPart_Action_NextFailure);
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_NextFailureToolTip);
            setDisabledImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("dlcl16/select_next.gif")); //$NON-NLS-1$
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "elcl16/select_next.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(imageDescriptor);
            setImageDescriptor(imageDescriptor);
            setEnabled(false);
        }

        @Override
        public void run() {
            fTestRunPane.selectNextFailureOrError();
        }
    }

    /*
     * Action to select the previous error or failure
     */
    private class ShowPreviousErrorAction extends Action {
        public ShowPreviousErrorAction() {
            setText(Messages.IpsTestRunnerViewPart_Action_PrevFailure);
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_PrevFailureToolTip);
            setDisabledImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("dlcl16/select_prev.gif")); //$NON-NLS-1$
            ImageDescriptor createImageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "elcl16/select_prev.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(createImageDescriptor);
            setImageDescriptor(createImageDescriptor);
            setEnabled(false);
        }

        @Override
        public void run() {
            fTestRunPane.selectPreviousFailureOrError();
        }
    }

    /*
     * Action to show only failures or errors
     */
    private class ShowErrorsFailureOnlyAction extends Action {
        public ShowErrorsFailureOnlyAction() {
            super(Messages.IpsTestRunnerViewPart_Action_ShowFailuresOnly, IAction.AS_CHECK_BOX);
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_ShowFailuresOnly_ToolTip);
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("failures.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(imageDescriptor);
            setImageDescriptor(imageDescriptor);
        }

        @Override
        public void run() {
            fTestRunPane.toggleShowErrorsOrFailuresOnly();
        }
    }

    /*
     * ShowErrorsFailureOnlyAction Action to toggle the orientation of the view
     */
    private class ToggleOrientationAction extends Action {
        private final int fActionOrientation;

        public ToggleOrientationAction(int orientation) {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$
            if (orientation == IpsTestRunnerViewPart.VIEW_ORIENTATION_HORIZONTAL) {
                setText(Messages.IpsTestRunnerViewPart_Menu_HorizontalOrientation);
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/th_horizontal.gif")); //$NON-NLS-1$                
            } else if (orientation == IpsTestRunnerViewPart.VIEW_ORIENTATION_VERTICAL) {
                setText(Messages.IpsTestRunnerViewPart_Menu_VerticalOrientation);
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/th_vertical.gif")); //$NON-NLS-1$              
            } else if (orientation == IpsTestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC) {
                setText(Messages.IpsTestRunnerViewPart_Menu_AutomaticOrientation);
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/th_automatic.gif")); //$NON-NLS-1$             
            }
            fActionOrientation = orientation;
        }

        public int getOrientation() {
            return fActionOrientation;
        }

        @Override
        public void run() {
            if (isChecked()) {
                fOrientation = fActionOrientation;
                computeOrientation();
            }
        }
    }

    /*
     * Runs the last runned test.
     */
    private void rerunTestRun() throws CoreException {
        IpsPlugin.getDefault().getIpsTestRunner().startTestRunnerJob(classpathRepository, testPackage);
    }

    /*
     * UIJob to refresh the counter in th user interface.
     */
    class UpdateUIJob extends UIJob {
        private boolean fRunning = true;

        public UpdateUIJob(String name) {
            super(name);
            setSystem(true);
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (!isDisposed()) {
                doShowStatus();
                refreshCounters();
            }
            schedule(REFRESH_INTERVAL);
            return Status.OK_STATUS;
        }

        public void stop() {
            fRunning = false;
        }

        @Override
        public boolean shouldSchedule() {
            return fRunning;
        }
    }

    @Override
    public void setFocus() {
        // Nothing to do
    }

    @Override
    public void createPartControl(Composite parent) {
        fParent = parent;
        addResizeListener(parent);

        clipboard = new Clipboard(parent.getDisplay());

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        parent.setLayout(gridLayout);

        configureToolBar();

        fCounterComposite = createProgressCountPanel(parent);
        fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

        SashForm sashForm = createSashForm(parent);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        registerAsIpsTestRunListener();

        if (fMemento != null) {
            restoreLayoutState(fMemento.getChild(LAYOUT_MEMENTO));
        }
        fMemento = null;
    }

    private void restoreLayoutState(IMemento memento) {
        restoreOrientationAndRatio(memento);
        restoreLockScroll(memento);
    }

    private void restoreLockScroll(IMemento memento) {
        if (memento == null) {
            return;
        }

        Integer scroll = memento.getInteger(TAG_SCROLL);
        if (scroll != null) {
            scrollLocked = (scroll.intValue() == 1) ? true : false;
        }
        if (fLockScrollAction != null) {
            fLockScrollAction.setChecked(scrollLocked);
        }
    }

    private void restoreOrientationAndRatio(IMemento memento) {
        if (memento == null) {
            return;
        }

        Integer ratio = memento.getInteger(TAG_RATIO);
        if (ratio != null) {
            fSashForm.setWeights(new int[] { ratio.intValue(), 1000 - ratio.intValue() });
        }
        Integer orientation = memento.getInteger(TAG_ORIENTATION);
        if (orientation != null) {
            fOrientation = orientation.intValue();
        }
        computeOrientation();
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
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        fMemento = memento;

        IWorkbenchSiteProgressService progressService = getProgressService();
        if (progressService != null) {
            progressService.showBusyForFamily(FAMILY_IPT_TEST_RUN);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(LAYOUT_MEMENTO);
        layout.putInteger(TAG_ORIENTATION, fCurrentOrientation);
        layout.putInteger(TAG_RATIO, fSashForm.getWeights()[0]);
        layout.putInteger(TAG_SCROLL, scrollLocked ? 1 : 0);
    }

    private IWorkbenchSiteProgressService getProgressService() {
        Object siteService = getSite().getAdapter(IWorkbenchSiteProgressService.class);
        if (siteService != null) {
            return (IWorkbenchSiteProgressService)siteService;
        }
        return null;
    }

    private Composite createProgressCountPanel(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        layout.numColumns = 1;

        fCounterPanel = new IpsTestCounterPanel(composite);
        fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

        fProgressBar = new IpsTestProgressBar(composite);
        fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        return composite;
    }

    private SashForm createSashForm(Composite parent) {
        fSashForm = new SashForm(parent, SWT.VERTICAL);

        ViewForm top = new ViewForm(fSashForm, SWT.NONE);
        CLabel label = new CLabel(top, SWT.NONE);
        label.setText(Messages.IpsTestRunnerViewPart_TestRunPane_Text);
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseRun.gif");//$NON-NLS-1$
        label.setImage((Image)resourceManager.get(imageDescriptor));
        top.setTopLeft(label);
        fTestRunPane = new TestRunPane(top, this);
        top.setContent(fTestRunPane.getComposite());

        ViewForm bottom = new ViewForm(fSashForm, SWT.NONE);
        label = new CLabel(bottom, SWT.NONE);
        label.setText(Messages.IpsTestRunnerViewPart_TestFailurePane_Text);
        ImageDescriptor failureImageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("failures.gif");//$NON-NLS-1$
        label.setImage((Image)resourceManager.get(failureImageDescriptor));
        bottom.setTopLeft(label);

        ToolBar failureToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
        bottom.setTopCenter(failureToolBar);
        fFailurePane = new FailurePane(bottom, failureToolBar, this, clipboard);
        bottom.setContent(fFailurePane.getComposite());

        fSashForm.setWeights(new int[] { 50, 50 });
        return fSashForm;
    }

    private void configureToolBar() {
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBar = actionBars.getToolBarManager();
        IMenuManager viewMenu = actionBars.getMenuManager();

        fStopTestRunAction = new StopTestRunAction();
        fRerunLastTestAction = new RerunLastAction();
        fNextAction = new ShowNextErrorAction();
        fPreviousAction = new ShowPreviousErrorAction();
        fShowErrorsOrFailuresOnlyAction = new ShowErrorsFailureOnlyAction();
        fLockScrollAction = new LockScrollAction();

        fToggleOrientationActions = new ToggleOrientationAction[] {
                new ToggleOrientationAction(VIEW_ORIENTATION_VERTICAL),
                new ToggleOrientationAction(VIEW_ORIENTATION_HORIZONTAL),
                new ToggleOrientationAction(VIEW_ORIENTATION_AUTOMATIC) };

        fToggleOrientationActions[2].setChecked(true);

        toolBar.add(fNextAction);
        toolBar.add(fPreviousAction);
        toolBar.add(fShowErrorsOrFailuresOnlyAction);
        toolBar.add(fLockScrollAction);
        toolBar.add(new Separator());
        toolBar.add(fStopTestRunAction);
        toolBar.add(fRerunLastTestAction);

        for (int i = 0; i < fToggleOrientationActions.length; ++i) {
            viewMenu.add(fToggleOrientationActions[i]);
        }

        actionBars.updateActionBars();

        setRunToolBarButtonsStatus(false);
        setNextPrevToolBarButtonsStatus(false);
    }

    private void addResizeListener(Composite parent) {
        parent.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {
                // Nothing to do
            }

            @Override
            public void controlResized(ControlEvent e) {
                computeOrientation();
            }
        });
    }

    private void computeOrientation() {
        if (fOrientation != VIEW_ORIENTATION_AUTOMATIC) {
            fCurrentOrientation = fOrientation;
            setOrientation(fCurrentOrientation);
        } else {
            Point size = fParent.getSize();
            if (size.x != 0 && size.y != 0) {
                if (size.x > size.y) {
                    setOrientation(VIEW_ORIENTATION_HORIZONTAL);
                } else {
                    setOrientation(VIEW_ORIENTATION_VERTICAL);
                }
            }
        }
    }

    private void setOrientation(int orientation) {
        if ((fSashForm == null) || fSashForm.isDisposed()) {
            return;
        }
        boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
        fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
        for (int i = 0; i < fToggleOrientationActions.length; ++i) {
            fToggleOrientationActions[i].setChecked(fOrientation == fToggleOrientationActions[i].getOrientation());
        }
        fCurrentOrientation = orientation;
        GridLayout layout = (GridLayout)fCounterComposite.getLayout();
        setCounterColumns(layout);
        fParent.layout();
    }

    private void setCounterColumns(GridLayout layout) {
        if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL) {
            layout.numColumns = 2;
        } else {
            layout.numColumns = 1;
        }
    }

    @Override
    public synchronized void dispose() {
        fIsDisposed = true;
        resourceManager.dispose();
        IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
        testRunner.removeIpsTestRunListener(this);

        if (clipboard != null) {
            clipboard.dispose();
        }
    }

    private boolean isDisposed() {
        return fIsDisposed || fCounterPanel.isDisposed();
    }

    private void doShowStatus() {
        setContentDescription(fStatus);
    }

    public void setInfoMessage(final String message) {
        fStatus = message;
    }

    public void setStatusBarMessage(String message) {
        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        if (statusLineManager != null) {
            statusLineManager.setMessage(message);
        }
    }

    private class TableEntryQueueDrainer implements Runnable {
        @Override
        public void run() {
            while (true) {
                TestCaseEntry testCaseEntry;
                synchronized (fTableEntryQueue) {
                    if (fTableEntryQueue.isEmpty() || isDisposed()) {
                        fQueueDrainRequestOutstanding = false;
                        return;
                    }
                    testCaseEntry = fTableEntryQueue.remove(0);
                }
                fTestRunPane.newTableEntry(testCaseEntry.getTestId(), testCaseEntry.getQualifiedName(),
                        testCaseEntry.fullPath);
                fTestRunPane.checkMissingEntries();
            }
        }
    }

    // inner class to represent an ips test case
    private class TestCaseEntry {
        private String qualifiedName;
        private String fullPath;
        private String testId;

        public TestCaseEntry(String testId, String qualifiedName, String fullPath) {
            this.testId = testId;
            this.qualifiedName = qualifiedName;
            this.fullPath = fullPath;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public String getTestId() {
            return testId;
        }
    }

    class IpsTestIsRunningJob extends Job {
        public IpsTestIsRunningJob(String name) {
            super(name);
            setSystem(true);
        }

        @Override
        public IStatus run(IProgressMonitor monitor) {
            // wait until the test run terminates
            ipsTestIsRunningLock.acquire();
            return Status.OK_STATUS;
        }

        @Override
        public boolean belongsTo(Object family) {
            return family == FAMILY_IPT_TEST_RUN;
        }
    }

    public Display getDisplay() {
        return getViewSite().getShell().getDisplay();
    }

    public Shell getShell() {
        return getViewSite().getShell();
    }

    private void stopUpdateJobs() {
        if (fUpdateJob != null) {
            fUpdateJob.stop();
            fUpdateJob = null;
        }
        if (ipsTestIsRunningJob != null && ipsTestIsRunningLock != null) {
            ipsTestIsRunningLock.release();
            ipsTestIsRunningJob = null;
        }
    }

    private void reset(final int testCount) {
        postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                setStatusBarMessage(""); //$NON-NLS-1$
                fCounterPanel.reset();
                fProgressBar.reset();
                start(testCount);
            }
        });

        fExecutedTests = 0;
        fFailureCount = 0;
        fErrorCount = 0;
        fStatus = ""; //$NON-NLS-1$
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
        fProgressBar.refresh(fErrorCount + fFailureCount > 0);
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
            @Override
            public void run() {
                if (!isDisposed()) {
                    fTestRunPane.aboutToStart();
                }
                fFailurePane.aboutToStart();
            }
        });
    }

    private void postSyncRunnable(Runnable r) {
        if (!isDisposed()) {
            getDisplay().syncExec(r);
        }
    }

    private void postStartTest(final String testId) {
        postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                fTestRunPane.startTest(testId, scrollLocked);
            }
        });
    }

    private void postErrorInTest(final String testId, final String[] errorDetails) {
        postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                fTestRunPane.errorInTest(testId, errorDetails);
            }
        });
    }

    private void postEndTest(final String testId) {
        postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                handleEndTest();
                fTestRunPane.endTest(testId);
            }
        });
    }

    private void postFailureTest(final String testId, final String[] failureDetails) {
        postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                fTestRunPane.failureTest(testId, failureDetailsToString(failureDetails), failureDetails);
            }
        });
    }

    private void postEndTestRun() {
        postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                fTestRunPane.selectFirstFailureOrError();
            }
        });
    }

    /*
     * Converts the given failure details to one failure detail row.
     */
    private String failureDetailsToString(String[] failureDetails) {
        String[] failureDetailsCopy = new String[failureDetails.length];
        System.arraycopy(failureDetails, 0, failureDetailsCopy, 0, failureDetails.length);

        String failureFormat = Messages.IpsTestRunnerViewPart_FailureFormat_FailureIn;
        String failureActual = Messages.IpsTestRunnerViewPart_FailureFormat_Actual;
        String failureExpected = Messages.IpsTestRunnerViewPart_FailureFormat_Expected;
        String failureFormatAttribute = Messages.IpsTestRunnerViewPart_FailureFormat_Attribute;
        String failureFormatObject = Messages.IpsTestRunnerViewPart_FailureFormat_Object;
        String failureFormatMessage = Messages.IpsTestRunnerViewPart_FailureFormat_Message;

        failureDetailsCopy[3] = TestRuleViolationType.mapRuleValueTest(failureDetailsCopy[3]);
        failureDetailsCopy[4] = TestRuleViolationType.mapRuleValueTest(failureDetailsCopy[4]);

        if (failureDetailsCopy.length > 3) {
            failureFormat = failureFormat + (failureExpected);
        }
        if (failureDetailsCopy.length > 4) {
            failureFormat = failureFormat + (failureActual);
        }
        if (failureDetailsCopy.length > 2) {
            failureFormat = failureFormat + (!"<null>".equals(failureDetailsCopy[2]) ? failureFormatAttribute : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (failureDetailsCopy.length > 1) {
            failureFormat = failureFormat + (!"<null>".equals(failureDetailsCopy[1]) ? failureFormatObject : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (failureDetailsCopy.length > 5) {
            failureFormat = failureFormat + (!"<null>".equals(failureDetailsCopy[5]) ? failureFormatMessage : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return MessageFormat.format(failureFormat, (Object[])failureDetailsCopy);
    }

    //
    // Helper functions to generate unique test id's to identify the test in the test run ui
    // control.
    //

    private void resetTestId() {
        testRuns++;
        testId = 0;
    }

    private String nextTestId() {
        return "" + testRuns + "." + ++testId; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getTestId(String qualifiedTestName) {
        return testId2TestQualifiedNameMap.get(qualifiedTestName);
    }

    //
    // Listener methods
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public void testFailureOccured(String qualifiedTestName, String[] failureDetails) {
        isFailure = true;
        postFailureTest(getTestId(qualifiedTestName), failureDetails);
        setNextPrevToolBarButtonsStatus(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testFinished(String qualifiedTestName) {
        if (fExecutedTests > fCounterPanel.getTotal()) {
            // set correct total size
            // if there are more test case executed as previously expected
            // e.g. if an ips test case starts itself several ips tests
            postSyncRunnable(new Runnable() {
                @Override
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    fCounterPanel.setTotal(fCounterPanel.getTotal() + 1);
                }
            });
        }
        fExecutedTests++;
        if (isFailure) {
            fFailureCount++;
        }
        postEndTest(getTestId(qualifiedTestName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String qualifiedTestName) {
        setInfoMessage(StringUtil.unqualifiedName(qualifiedTestName));
        isFailure = false;
        setRunToolBarButtonsStatus(true);
        postStartTest(getTestId(qualifiedTestName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testTableEntry(final String qualifiedName, final String fullPath) {
        // get a new or a cached test id
        String testId = getTestId(qualifiedName);
        if (testId == null) {
            testId = nextTestId();
            testId2TestQualifiedNameMap.put(qualifiedName, testId);
        }

        synchronized (fTableEntryQueue) {
            fTableEntryQueue.add(new TestCaseEntry(testId, qualifiedName, fullPath));
            if (!fQueueDrainRequestOutstanding) {
                fQueueDrainRequestOutstanding = true;
                if (!isDisposed()) {
                    getDisplay().asyncExec(new TableEntryQueueDrainer());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testTableEntries(final String[] qualifiedNames, final String[] fullPaths) {
        // get a new or a cached test id

        synchronized (fTableEntryQueue) {
            for (int i = 0; i < fullPaths.length; i++) {
                String testId = getTestId(qualifiedNames[i]);
                if (testId == null) {
                    testId = nextTestId();
                    testId2TestQualifiedNameMap.put(qualifiedNames[i], testId);
                }
                fTableEntryQueue.add(new TestCaseEntry(testId, qualifiedNames[i], fullPaths[i]));
            }
            if (!fQueueDrainRequestOutstanding) {
                fQueueDrainRequestOutstanding = true;
                if (!isDisposed()) {
                    getDisplay().asyncExec(new TableEntryQueueDrainer());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testRunStarted(int testCount, String classpathRepository, String testPackage) {
        this.classpathRepository = classpathRepository;
        this.testPackage = testPackage;

        testId2TestQualifiedNameMap.clear();

        reset(testCount);
        fExecutedTests++;
        setRunToolBarButtonsStatus(true);
        setNextPrevToolBarButtonsStatus(false);

        stopUpdateJobs();
        fUpdateJob = new UpdateUIJob(Messages.IpsTestRunnerViewPart_Job_UpdateUiTitle);
        ipsTestIsRunningJob = new IpsTestIsRunningJob("IPSTest Starter Job"); //$NON-NLS-1$
        ipsTestIsRunningLock = Job.getJobManager().newLock();
        // acquire lock while a test run is running
        // the lock is released when the test run terminates
        // the wrapper job will wait on this lock.
        ipsTestIsRunningLock.acquire();
        getProgressService().schedule(ipsTestIsRunningJob);

        fUpdateJob.schedule(0);

        // store the project which contains the tests, will be used to open the test in the editor
        fTestProject = IpsPlugin.getDefault().getIpsTestRunner().getIpsProject().getJavaProject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testRunEnded(String elapsedTime) {
        fExecutedTests--;
        stopUpdateJobs();
        postEndTestRun();
        long elapsedTimeLong = 0;
        try {
            elapsedTimeLong = Long.parseLong(elapsedTime);
        } catch (NumberFormatException e) {
            // ignore exception of wrong number format
        }
        fStatus = NLS.bind(Messages.IpsTestRunnerViewPart_Message_TestFinishedAfterNSeconds,
                elapsedTimeAsString(elapsedTimeLong));

        fStopTestRunAction.setEnabled(false);
        if (fErrorCount + fFailureCount > 0) {
            setNextPrevToolBarButtonsStatus(true);
        }
    }

    /*
     * Returns the string representation in second of the given time in milliseconds
     */
    private String elapsedTimeAsString(long elapsedTime) {
        return NumberFormat.getInstance().format((double)elapsedTime / 1000);
    }

    @Override
    public void testErrorOccured(String qualifiedTestName, String[] errorDetails) {
        fErrorCount++;
        postErrorInTest(getTestId(qualifiedTestName), errorDetails);
    }

    /**
     * Informs that the selection of a test result changed.
     */
    public void selectionOfTestCaseChanged(String[] testCaseFailures) {
        fFailurePane.showFailureDetails(testCaseFailures);
    }

    public IJavaProject getLaunchedProject() {
        return fTestProject;
    }

    /*
     * Enables or disables the run and stop toolbar actions
     */
    private void setRunToolBarButtonsStatus(boolean enabled) {
        fRerunLastTestAction.setEnabled(enabled);
        fStopTestRunAction.setEnabled(enabled);
    }

    /*
     * Enables or disables the next and previous toolbar actions
     */
    private void setNextPrevToolBarButtonsStatus(boolean enabled) {
        fNextAction.setEnabled(enabled);
        fPreviousAction.setEnabled(enabled);
    }

    public String getSelectedTestFullPath() {
        return fTestRunPane.getSelectedTestFullPath();
    }

    public String getSelectedTestQualifiedName() {
        return fTestRunPane.getSelectedTestQualifiedName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canNavigateToFailure() {
        return false;
    }

    String[] getFailureDetailsOfSelectedTestCase() {
        return fTestRunPane.getFailureDetailsOfSelectedTestCase(fFailurePane.getSelectedTableIndex());
    }

    /**
     * @param resourceManager The resourceManager to set.
     */
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * @return Returns the resourceManager.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
