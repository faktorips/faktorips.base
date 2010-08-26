/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Test runner failure pane shows all errors or failures in a table.
 * 
 * @author Joerg Ortmann
 */
public class FailurePane implements IMenuListener {
    private static final String TEST_ERROR_MESSAGE_INDICATOR = ">>>"; //$NON-NLS-1$
    private static final String TEST_ERROR_STACK_INDICATOR = "---"; //$NON-NLS-1$

    private final Clipboard clipboard;

    private Table table;

    // Action
    private Action showStackTraceAction;

    // Indicates if the stacktrace elemets will be shown or not
    private boolean showStackTrace = false;

    // Contains the last reported failures in this pane
    private String[] lastFailures = new String[0];
    private IpsTestRunnerViewPart viewPart;

    /*
     * Action to filter the stack trace elements
     */
    private class ShowStackTraceAction extends Action {
        public ShowStackTraceAction() {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$
            setText(Messages.IpsTestRunnerViewPart_Action_ShowStackTrace);
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_ShowStackTraceToolTip);
            setDisabledImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("dlcl16/cfilter.gif")); //$NON-NLS-1$
            ImageDescriptor cfilterDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "elcl16/cfilter.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(cfilterDescriptor);
            setImageDescriptor(cfilterDescriptor);
            setEnabled(showStackTrace);
        }

        @Override
        public void run() {
            showStackTrace = !showStackTrace;
            showStackTraceAction.setChecked(showStackTrace);
            showFailureDetails(lastFailures);
        }
    }

    /*
     * Action to copy the currently displayed failure details into the clipboard
     */
    private class IpsTestCopyAction extends SelectionListenerAction {
        private String failureDetails;

        protected IpsTestCopyAction(String failureDetails) {
            super(Messages.FailurePane_MenuLabel_CopyInClipboard);
            this.failureDetails = failureDetails;
        }

        @Override
        public void run() {
            TextTransfer plainTextTransfer = TextTransfer.getInstance();
            try {
                clipboard.setContents(new String[] { failureDetails }, new Transfer[] { plainTextTransfer });
            } catch (SWTError ignored) {
            }
        }
    }

    /*
     * Class to represent the corresponding object in the trace line of the stacktrace.
     */
    private class TraceLineElement {
        private String testName = ""; //$NON-NLS-1$
        private String fileName = ""; //$NON-NLS-1$
        private int line = 0;

        public TraceLineElement(String traceLine) {
            testName = traceLine;
            if (testName.lastIndexOf('(') == -1 || testName.lastIndexOf('.') == -1) {
                return;
            }
            testName = testName.substring(0, testName.lastIndexOf('(')).trim();
            testName = testName.substring(0, testName.lastIndexOf('.'));
            int innerSeparatorIndex = testName.indexOf('$');
            if (innerSeparatorIndex != -1) {
                testName = testName.substring(0, innerSeparatorIndex);
            }
            try {
                String lineNumber = traceLine;
                lineNumber = lineNumber.substring(lineNumber.indexOf(':') + 1, lineNumber.lastIndexOf(')'));
                line = Integer.valueOf(lineNumber).intValue();
                fileName = traceLine.substring(traceLine.lastIndexOf('(') + 1, traceLine.lastIndexOf(':'));
            } catch (NumberFormatException ignored) {
                // ignore number exception,
                // the results is an invalid element, see #isValidElement()
            }
        }

        public int getLine() {
            return line;
        }

        public String getTestName() {
            return testName;
        }

        /**
         * Returns <code>true</code> if the element is valid, means the element could be
         * successfully determined from the trace line.
         */
        public boolean isValidElement() {
            return testName.length() > 0 && fileName.length() > 0 && line > 0;
        }

    }

    public FailurePane(Composite parent, ToolBar toolBar, final IpsTestRunnerViewPart viewPart, Clipboard clipboard) {
        this.viewPart = viewPart;
        this.clipboard = clipboard;

        table = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewPart.setStatusBarMessage(getSelectedTableRowText());
            }
        });

        table.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Nothing to do
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (table.getSelectionCount() > 0) {
                    boolean navigate = false;
                    if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
                        // the user can modify to the model or sources, open the java class
                        navigate = openEditor(getSelectedTableRowText());
                    }
                    if (!navigate) {
                        // navigate to corresponding test case, if no source navigation is allowed
                        // or the source navigation failed (e.g. no stacktrace line, but failure
                        // line indicator)
                        new OpenTestInEditorAction(viewPart, viewPart.getSelectedTestFullPath(), viewPart
                                .getSelectedTestQualifiedName(), getSelectedTableRowText()).run();
                    }
                }
            }
        });

        // fill the failure trace viewer toolbar
        ToolBarManager failureToolBarmanager = new ToolBarManager(toolBar);
        showStackTraceAction = new ShowStackTraceAction();
        failureToolBarmanager.add(showStackTraceAction);
        failureToolBarmanager.update(true);

        initMenu();
    }

    private String getSelectedTableRowText() {
        TableItem[] items = table.getSelection();
        if (items.length > 0) {
            return items[0].getText();
        }
        return ""; //$NON-NLS-1$
    }

    private boolean containsTraceLineRelevantSourceFile() {
        return true;
        // the stacktrace will always be displayed
        // it doesn't matter if the source is inside the projects source folder or not
    }

    /*
     * Open the editor and mark corresponding line.
     */
    private boolean openEditor(String traceLine) {
        try {
            TraceLineElement tli = new TraceLineElement(traceLine);
            if (!tli.isValidElement()) {
                // no link to java source code
                return false;
            }

            IJavaElement file = findElement(viewPart.getLaunchedProject(), tli.getTestName());
            if (file == null) {
                MessageDialog.openError(viewPart.getShell(), Messages.FailurePane_DialogClassNotFound_Title, NLS.bind(
                        Messages.FailurePane_DialogClassNotFound_Description, tli.getTestName()));
                return false;
            }

            // try to get the editor input from the projects source folder
            IEditorInput editorInput = getEditorInput(file);
            if (editorInput == null) {
                // maybe this is a java class file in an jar archive
                IEditorPart part = JavaUI.openInEditor(file);
                if (part == null) {
                    MessageDialog
                            .openInformation(viewPart.getShell(),
                                    Messages.FailurePane_DialogClassNotFoundInSrcFolder_Title, NLS.bind(
                                            Messages.FailurePane_DialogClassNotFoundInSrcFolder_Description, tli
                                                    .getTestName()));
                    return false;
                }
                editorInput = part.getEditorInput();
            }
            // goto corresponding line in the editor
            IEditorDescriptor editor = IDE.getEditorDescriptor(editorInput.getName());
            ITextEditor textEditor = (ITextEditor)IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().openEditor(editorInput, editor.getId(), true);
            IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            if (document == null) {
                return false;
            }
            textEditor
                    .selectAndReveal(document.getLineOffset(tli.getLine() - 1), document.getLineLength(tli.getLine()));
        } catch (BadLocationException x) {
            // marker refers to invalid text position -> do nothing
        } catch (StringIndexOutOfBoundsException x) {
            // invalid text -> do nothing
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

    /*
     * Returns the editor input. Only files in the project source folder (compilation unit) are
     * supported. Class files in e.g. Jar's with source attachment are not supported.
     */
    private IEditorInput getEditorInput(IJavaElement element) {
        while (element != null) {
            if (element instanceof ICompilationUnit) {
                ICompilationUnit unit = (ICompilationUnit)element;
                IResource resource = unit.getResource();
                if (resource instanceof IFile) {
                    return new FileEditorInput((IFile)resource);
                }
            }
            element = element.getParent();
        }

        return null;
    }

    /*
     * Find java element in the given java project by the given class name.
     */
    private IJavaElement findElement(IJavaProject project, String className) throws CoreException {
        return project == null ? null : project.findType(className);
    }

    /**
     * Returns the composite used to present the failures.
     */
    public Composite getComposite() {
        return table;
    }

    /**
     * Inserts the given test case failure details in the table. One row for each given failure. If
     * showStackTrace is <code>false</code> and the given failure details contains stack trace
     * elements, then these elements will be hidden.
     */
    public void showFailureDetails(String[] testCaseFailures) {
        final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
        showStackTraceAction.setEnabled(false);
        lastFailures = testCaseFailures;
        table.removeAll();
        for (int i = 0; i < testCaseFailures.length; i++) {
            if (testCaseFailures[i].startsWith(TEST_ERROR_MESSAGE_INDICATOR)) {
                String text = testCaseFailures[i].substring(TEST_ERROR_MESSAGE_INDICATOR.length());
                if (text.trim().length() > 0) {
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(text);
                    ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                            "obj16/stkfrm_msg.gif");//$NON-NLS-1$
                    tableItem.setImage((Image)resourceManager.get(imageDescriptor));
                }
            } else if (testCaseFailures[i].startsWith(TEST_ERROR_STACK_INDICATOR)) {
                showStackTraceAction.setEnabled(true);
                if (showStackTrace) {
                    String traceLine = testCaseFailures[i].substring(TEST_ERROR_STACK_INDICATOR.length());
                    if (containsTraceLineRelevantSourceFile() || i == 0) {
                        TableItem tableItem = new TableItem(table, SWT.NONE);
                        // show the stacktrace line only if the element is inside the projects
                        // sources or this is the last stacktrace line,
                        // thus if the last stacktrace line is not inside the souce the error could
                        // be better determined with at least this trace line
                        tableItem.setText(traceLine);
                        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                                "obj16/stkfrm_obj.gif");//$NON-NLS-1$
                        tableItem.setImage((Image)resourceManager.get(imageDescriptor));
                    }
                }
            } else {
                if (testCaseFailures[i].trim().length() > 0) {
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(testCaseFailures[i]);
                    ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                            "obj16/testfail.gif");//$NON-NLS-1$
                    tableItem.setImage((Image)resourceManager.get(imageDescriptor));
                }
            }
        }
        table.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                resourceManager.dispose();
            }
        });
    }

    /**
     * A new test run will be started.
     */
    public void aboutToStart() {
        table.removeAll();
    }

    /**
     * Returns the index of the selected element.
     */
    int getSelectedTableIndex() {
        return table.getSelectionIndex();
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        if (table.getSelectionCount() > 0) {
            manager.add(new IpsTestCopyAction(getFailureDetailsAsString()));
        }
    }

    private String getFailureDetailsAsString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        TableItem[] tableItems = table.getItems();
        for (TableItem tableItem : tableItems) {
            printWriter.println(tableItem.getText());
        }
        return stringWriter.toString();
    }

    private void initMenu() {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(this);
        Menu menu = menuMgr.createContextMenu(table);
        table.setMenu(menu);
    }
}
