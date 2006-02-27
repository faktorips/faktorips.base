package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;


/**
 *
 */
public abstract class IpsPartEditDialog extends EditDialog {
    
    protected IpsPartUIController uiController;
    private TextField descriptionField;

    public IpsPartEditDialog(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle) {
        this(part, parentShell, windowTitle, false);
    }
    
    public IpsPartEditDialog(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle,
            boolean useTabFolder) {
        super(parentShell, windowTitle, useTabFolder);
        uiController = createUIController(part);
    }
    
	protected Control createContents(Composite parent) {
	    Control control = super.createContents(parent);
        connectToModel();
        uiController.updateUI();
        setTitle(buildTitle());
        return control;
	}
    
	protected TabItem createDescriptionTabItem(TabFolder folder) {
	    Composite c = createTabItemComposite(folder, 1, false);
	    Text text = uiToolkit.createMultilineText(c);
	    descriptionField = new TextField(text);
	    TabItem item = new TabItem(folder, SWT.NONE);
	    item.setText(Messages.IpsPartEditDialog_description);
	    item.setControl(c);
	    return item;
	}
    
    protected IpsPartUIController createUIController(IIpsObjectPart part) {
        IpsPartUIController controller = new IpsPartUIController(part) {
            public void valueChanged(FieldValueChangedEvent e) {
                try {
                    super.valueChanged(e);
                    setTitle(buildTitle());
                    // showValidationResult(getPdPart().validate());
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        };
        return controller;
    }

    /**
     * Returns the part being edited.
     */
    public IIpsObjectPart getIpsPart() {
        return uiController.getIpsObjectPart();
    }
    
    protected String buildTitle() {
        IIpsObjectPart part = getIpsPart();
        if (part.getParent() instanceof IIpsObjectGeneration) {
            return part.getIpsObject().getName() + " "  //$NON-NLS-1$
            	+ part.getParent().getName() + "." + part.getName(); //$NON-NLS-1$
        }
        return part.getIpsObject().getName() + "." + part.getName(); //$NON-NLS-1$
    }
    
    protected void connectToModel() {
        if (descriptionField!=null) {
            uiController.add(descriptionField, getIpsPart(), IIpsObjectPart.PROPERTY_DESCRIPTION);
        }
    }

    protected void setEnabledDescription(boolean enabled) {
    	if (descriptionField != null) {
    		descriptionField.getControl().setEnabled(enabled);
    	}
    }
}
