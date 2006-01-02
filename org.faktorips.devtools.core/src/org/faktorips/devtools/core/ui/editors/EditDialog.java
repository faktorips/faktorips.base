package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 * @author Jan Ortmann
 */
public abstract class EditDialog extends TitleAreaDialog {

    private String windowTitle;
    private boolean tabFolderUsed;
    protected UIToolkit uiToolkit = new UIToolkit(null);

	public EditDialog(Shell parentShell, String windowTitle) {
	    this(parentShell, windowTitle, false);
	}
	
	public EditDialog(Shell parentShell, String windowTitle, boolean useTabFolder) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		this.windowTitle = windowTitle;
		tabFolderUsed = useTabFolder;
	}
	
	public void setWindowTitle(String newTitle) {
	    windowTitle = newTitle;
	    if (getShell()!=null) {
	        getShell().setText(newTitle);    
	    }
	}

	/**
	 * Overridden method.
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected final Control createDialogArea(Composite parent) {
	    getShell().setText(windowTitle);
		Composite composite = (Composite)super.createDialogArea(parent);
		updateTitleInTitleArea();
		Composite panel;
		if (tabFolderUsed) {
		    panel = new TabFolder(composite, SWT.TOP);
		} else {
			panel = new Composite(composite, SWT.NONE);
			panel.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout(1, false);
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			panel.setLayout(layout);
		}
		try {
			Composite workArea = createWorkArea(panel);
			if (workArea.getLayoutData()==null) {
			    workArea.setLayoutData(new GridData(GridData.FILL_BOTH));    
			}
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
		return composite;
	}
	
	protected abstract Composite createWorkArea(Composite parent) throws CoreException;
	
	protected void updateTitleInTitleArea() {
	}
	
	/**
	 * Creates a top level composite for a tab item with standardized margins
	 * and a grid layout with the given number of columns.
	 */
	protected final Composite createTabItemComposite(
	        TabFolder folder,
	        int numOfColumns,
	        boolean equalSize) {
	    Composite c = uiToolkit.createGridComposite(folder, numOfColumns, equalSize, true);
	    ((GridLayout)c.getLayout()).marginHeight = 12;
	    return c;
	}
	
	protected void showValidationResult(MessageList result) {
	    Message msg = result.getFirstMessage(Message.ERROR);
	    if (msg==null) {
	        setErrorMessage(null);
	    } else {
	        setErrorMessage(msg.getText());    
	    }
	}
	
}	

