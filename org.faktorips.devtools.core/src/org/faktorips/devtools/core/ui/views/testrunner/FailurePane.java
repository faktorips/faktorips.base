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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Test runner failure pane shows all errors or failures in a table.
 * 
 * @author Joerg Ortmann
 */
public class FailurePane {
    private static final String TEST_ERROR_MESSAGE_INDICATOR = ">>>"; //$NON-NLS-1$
    private static final String TEST_ERROR_STACK_INDICATOR = "---"; //$NON-NLS-1$
    
	private Table fTable;
	
    // Action
    private Action fShowStackTraceAction;

    // Indicates if the stacktrace elemets will be shown or not
    private boolean fShowStackTrace = false;
    
    // Contains the last reported failures in this pane
    private String[] fLastFailures = new String[0];
    private IpsTestRunnerViewPart viewPart;
    
    /*
     * Action class to filter the stack trace elements
     */
    private class ShowStackTraceAction extends Action {
        public ShowStackTraceAction() {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$
            setText(Messages.IpsTestRunnerViewPart_Action_ShowStackTrace); 
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_ShowStackTraceToolTip); 
            setDisabledImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("dlcl16/cfilter.gif")); //$NON-NLS-1$
            setHoverImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
            setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
            setEnabled(fShowStackTrace);
        }
        public void run(){
            fShowStackTrace = ! fShowStackTrace;
            fShowStackTraceAction.setChecked(fShowStackTrace);
            showFailureDetails(fLastFailures);
        }
    }
    
    /*
     * Class to represent the corresponding object in the trace line of the stacktrace.
     */
    private class TraceLineElement{
        private String testName = ""; //$NON-NLS-1$
        private String fileName = ""; //$NON-NLS-1$
        private int line = 0;
        
        public TraceLineElement(String traceLine) {
            testName= traceLine;
            if (testName.lastIndexOf('(') == -1 || testName.lastIndexOf('.') == -1){
                return;
            }
            testName= testName.substring(0, testName.lastIndexOf('(')).trim();
            testName= testName.substring(0, testName.lastIndexOf('.'));
            int innerSeparatorIndex= testName.indexOf('$');
            if (innerSeparatorIndex != -1){
                testName= testName.substring(0, innerSeparatorIndex);
            }
            String lineNumber= traceLine;
            lineNumber= lineNumber.substring(lineNumber.indexOf(':') + 1, lineNumber.lastIndexOf(')'));
            line = Integer.valueOf(lineNumber).intValue();
            fileName= traceLine.substring(traceLine.lastIndexOf('(') + 1, traceLine.lastIndexOf(':'));
        }
        
        public String getFileName() {
            return fileName;
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
        public boolean isValidElement(){
            return testName.length()>0 && fileName.length()>0 && line >0;
        }
        
        /**
         * Returns <code>true</code> if the element is an valid element in the java projects source folder.
         */
        public boolean isValidProjectSourceElement(){
            if (!isValidElement()){
                return false;
            }
            try {
                // find the java element, all elements in the whole classpath are searched
                IJavaElement file = findElement(viewPart.getLaunchedProject(), testName);
                // check if there is a valid editor input for the found element,
                // see method getEditorInput for further details. The method returns only an valid input
                // if the element is a compilation unit, means a source file (but no classfile),
                // therefore only elements in the source folder are valid and no class elements in jars
                // or binary folders. 
                // This is ok, because only elements which are visible and editable by the user
                // should be displayed in the stacktrace.
                IEditorInput editorInput = getEditorInput(file, fileName);
                if (editorInput != null) {
                    return true;
                }
            } catch (Exception e) {
                // ignore exception
            }
            return false;
        }
    }
    
	public FailurePane(Composite parent, ToolBar toolBar, final IpsTestRunnerViewPart viewPart) {
		this.viewPart = viewPart;
        fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        
        fTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                viewPart.setStatusBarMessage(getSelectedTableRowText());
            }
        });
        
        fTable.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                if (fTable.getSelectionCount() > 0) {
                    boolean navigate = false;
                    if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
                        // the user can modify to the model or sources, open the java class
                        navigate = openEditor(getSelectedTableRowText());
                    }
                    if (!navigate) {
                        // navigate to corresponding test case
                        new OpenTestInEditorAction(viewPart, viewPart.getSelectedTestFullPath(), getSelectedTableRowText()).run();
                    }
                }
            }
        });
        
        // fill the failure trace viewer toolbar
        ToolBarManager failureToolBarmanager= new ToolBarManager(toolBar);
        fShowStackTraceAction = new ShowStackTraceAction();
        failureToolBarmanager.add(fShowStackTraceAction);    
        failureToolBarmanager.update(true);
	}
    
    private String getSelectedTableRowText(){
        TableItem[] items = fTable.getSelection();
        if(items.length>0){
            return items[0].getText();
        }
        return ""; //$NON-NLS-1$
    }
    
    private boolean containsTraceLineRelevantSourceFile(String traceLine){
        TraceLineElement tli = new TraceLineElement(traceLine);
        return tli.isValidProjectSourceElement();
    }
    
    /*
     * Open the editor and mark corresponding line.
     */
    private boolean openEditor(String traceLine) {
        try { 
            TraceLineElement tli = new TraceLineElement(traceLine);
            if (! tli.isValidElement()){
                //  no link to java source code
                return false;
            }
            
            IJavaElement file= findElement(viewPart.getLaunchedProject(), tli.getTestName());
            if (file == null){
                MessageDialog.openError(viewPart.getShell(), 
                        Messages.FailurePane_DialogClassNotFound_Title, NLS.bind(Messages.FailurePane_DialogClassNotFound_Description, tli.getTestName())); 
                    return false;
            }

            // don't support full JUnint class file editor support, only edit files which are in the source folder
            // ITextEditor textEditor= (ITextEditor)EditorUtility.openInEditor(file, true);
            
            IEditorInput editorInput = getEditorInput(file, tli.getFileName());
            if (editorInput == null){
                MessageDialog.openInformation(viewPart.getShell(), 
                        Messages.FailurePane_DialogClassNotFoundInSrcFolder_Title, NLS.bind(Messages.FailurePane_DialogClassNotFoundInSrcFolder_Description, tli.getTestName())); 
                    return false;
            }
            IEditorDescriptor editor = IDE.getEditorDescriptor(editorInput.getName());
            ITextEditor textEditor = (ITextEditor)IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editor.getId(), true);

            // goto corresponding line in the editor
            IDocument document= textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            if (document == null)
                return false;
            textEditor.selectAndReveal(document.getLineOffset(tli.getLine()-1), document.getLineLength(tli.getLine()));
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
     * Returns the editor input. Only files in the project source folder (compilation init) are supported.
     * Class files in e.g. Jar's with source attachment are not supported.
     */
    private IEditorInput getEditorInput(IJavaElement element, String name) throws JavaModelException {
        while (element != null){
            if (element instanceof ICompilationUnit) {
                ICompilationUnit unit = (ICompilationUnit)element;
                IResource resource = unit.getResource();
                if (resource instanceof IFile)
                    return new FileEditorInput((IFile)resource);
            }
            element = element.getParent();
        }

        return null;
    }

    /*
     * Find java element in the given java project by the given class name.
     */
    private IJavaElement findElement(IJavaProject project, String className) throws CoreException {
        return project==null?null:project.findType(className);
    }
    
	/**
	 * Returns the composite used to present the failures.
	 */
	public Composite getComposite(){
		return fTable;
	}
    
	/**
	 * Inserts the given test case failure details in the table. One row for each given failure.
     * If showStackTrace is <code>false</code> and the given failure details contains stack trace elements, then
     * these elements will be hidden.
	 */
	public void showFailureDetails(String[] testCaseFailures) {
        fShowStackTraceAction.setEnabled(false);
        fLastFailures = testCaseFailures;
        fTable.removeAll();
		for (int i = 0; i < testCaseFailures.length; i++) {
            if (testCaseFailures[i].startsWith(TEST_ERROR_MESSAGE_INDICATOR)){
                String text = testCaseFailures[i].substring(TEST_ERROR_MESSAGE_INDICATOR.length());
                if (text.trim().length()>0){
                    TableItem tableItem = new TableItem(fTable, SWT.NONE);
                    tableItem.setText(text);
                    tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/stkfrm_msg.gif")); //$NON-NLS-1$
                }
            } else if (testCaseFailures[i].startsWith(TEST_ERROR_STACK_INDICATOR)) {
                fShowStackTraceAction.setEnabled(true);
                if (fShowStackTrace){
                    String traceLine = testCaseFailures[i].substring(TEST_ERROR_STACK_INDICATOR.length());
                    if (containsTraceLineRelevantSourceFile(traceLine) || i == 0){
                        TableItem tableItem = new TableItem(fTable, SWT.NONE);
                        // show the stacktrace line only if the element is inside the projects
                        // sources or this is the last stacktrace line,
                        // thus if the last stacktrace line is not inside the souce the error could
                        // be better determined with at least this trace line
                        tableItem.setText(traceLine);
                        tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/stkfrm_obj.gif")); //$NON-NLS-1$
                    }
                }
            } else {
                if (testCaseFailures[i].trim().length()>0){
                    TableItem tableItem = new TableItem(fTable, SWT.NONE);
                    tableItem.setText(testCaseFailures[i]);
                    tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testfail.gif")); //$NON-NLS-1$
                }
            }
		}
	}

	/**
	 * A new test run will be started.
	 */
	public void aboutToStart() {
		fTable.removeAll();
	}

    /**
     * Returns the index of the selected element.
     */
    int getSelectedTableIndex() {
        return fTable.getSelectionIndex();
    }    
}
