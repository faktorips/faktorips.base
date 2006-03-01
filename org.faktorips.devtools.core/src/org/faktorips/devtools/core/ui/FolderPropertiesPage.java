package org.faktorips.devtools.core.ui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Page to display and modify properties for FaktorIPS applied on folders.
 * 
 * @author Thorsten Guenther
 */
public class FolderPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage, ModifyListener {

	public static final QualifiedName SORTING_ORDER_PROPERTY = new QualifiedName(IpsPlugin.PLUGIN_ID, "FolderOrderNumber"); //$NON-NLS-1$
	
	/**
	 * The input field for the order value
	 */
	private Text orderValue; 
	
	/**
	 * {@inheritDoc}
	 */
	protected Control createContents(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		
		root.setLayout(new GridLayout(2, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label label = new Label(root, SWT.LEFT);
		label.setText(Messages.FolderPropertiesPage_labelSortNumber);
		orderValue = new Text(root, SWT.BORDER);
		orderValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		String value = null;;
		try {
			value = ((IFolder)super.getElement()).getPersistentProperty(SORTING_ORDER_PROPERTY);
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
		
		if (value == null) {
			value = "0"; //$NON-NLS-1$
		}
		
		orderValue.setText(value);
		
		orderValue.addModifyListener(this);
		
		return root;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyText(ModifyEvent e) {
		try {
			Integer.valueOf(orderValue.getText());
			super.setMessage(null);
		} catch (NumberFormatException e1) {
			super.setMessage(Messages.FolderPropertiesPage_msgSortNumberInvalid, ERROR);
		}
		
	}

	protected void performDefaults() {
		orderValue.setText("0"); //$NON-NLS-1$
		super.performDefaults();
	}

	public boolean okToLeave() {
		return super.getMessage() == null;
	}

	public boolean performOk() {
		try {
			((IFolder)super.getElement()).setPersistentProperty(SORTING_ORDER_PROPERTY, orderValue.getText());
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
		return true;
	}
	
	

}
