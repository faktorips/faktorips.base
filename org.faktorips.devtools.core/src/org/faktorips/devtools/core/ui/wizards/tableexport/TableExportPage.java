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

package org.faktorips.devtools.core.ui.wizards.tableexport;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Thorsten Waertel
 */
public class TableExportPage extends WizardPage implements ValueChangeListener {
	
    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    // edit controls
    private IpsProjectRefControl projectControl;
	private TableContentsRefControl contentsControl;
    private Combo fileFormatControl;
    private Text nullRepresentation;
    
    // edit fields
    private TextButtonField filenameField;
    private TextButtonField projectField;
    private TextButtonField contentsField;
    
    private AbstractExternalTableFormat[] formats;
    
    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;
    
    // page control as defined by the wizard page class
    private Composite pageControl;
    
	/**
	 * @param pageName
     * @param selection
     * @throws JavaModelException
	 */
	public TableExportPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.TableExportPage_title);
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();                
        } else if (selection.getFirstElement() instanceof IIpsElement) {
        	selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
	}

	/**
	 * {@inheritDoc}
	 * @throws CoreException 
	 */
	public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.TableExportPage_title);
        
        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);
        
        Composite locationComposite  = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.TableExportPage_labelProject);
        projectControl = toolkit.createIpsProjectRefControl(locationComposite);
        projectField = new TextButtonField(projectControl);
        projectField.addChangeListener(this);
        
        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Composite lowerComposite = toolkit.createLabelEditColumnComposite(pageControl); 
        toolkit.createFormLabel(lowerComposite, Messages.TableExportPage_labelContents);
        contentsControl = toolkit.createTableContentsRefControl(null, lowerComposite);
        contentsField = new TextButtonField(contentsControl);
        contentsField.addChangeListener(this);
        
        toolkit.createFormLabel(lowerComposite, Messages.TableExportPage_labelFileFormat);
        fileFormatControl = toolkit.createCombo(lowerComposite);
        
        formats = IpsPlugin.getDefault().getExternalTableFormats();
        for (int i = 0; i < formats.length; i++) {
        	fileFormatControl.add(formats[i].getName());
		}
        fileFormatControl.select(0);

        toolkit.createFormLabel(lowerComposite, Messages.TableExportPage_labelName); 
        filenameField = new TextButtonField(new FileSelectionControl(lowerComposite, toolkit));
        filenameField.addChangeListener(this);

        toolkit.createFormLabel(lowerComposite, Messages.TableExportPage_labelNullRepresentation);
        nullRepresentation = toolkit.createText(lowerComposite);
        nullRepresentation.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        
        setDefaults(selectedResource);

        validateInput = true;
	}

    /**
     * Derives the default values for source folder and package from
     * the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when
     * the wizard was opened.
     * @throws CoreException 
     */
    protected void setDefaults(IResource selectedResource) {
        try {
	    	if (selectedResource==null) {
	            setTableContents(null);
	            return;
	        }
	        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile) element;
                if (src.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
                    ITableContents contents = (ITableContents) src.getIpsObject();
                    setTableContents(contents);
                } 
            } else if (element != null) {
                setIpsProject(element.getIpsProject());
	        } else {
	            setTableContents(null);    
	        }
        } catch (CoreException e) {
        	IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    public String getFilename() {
        return filenameField.getText();
    }
    
    public AbstractExternalTableFormat getFormat() {
    	if (fileFormatControl.getSelectionIndex() == -1) {
    		return null;
    	}
        return formats[fileFormatControl.getSelectionIndex()];
    }
    
    public void setFilename(String newName) {
        filenameField.setText(newName);
    }
    
    public ITableContents getTableContents() throws CoreException {
        return contentsControl.findTableContents();
    }
    
    public String getNullRepresentation() {
    	return nullRepresentation.getText();
    }
    
    protected void filenameChanged() {
        
    }
    
    protected void formatChanged() {
        
    }
    
    protected void contentsChanged() {
        if (getFilename().equals("")) { //$NON-NLS-1$
            String contentsName = contentsField.getText();
            AbstractExternalTableFormat format = getFormat();
            String extension = ""; //$NON-NLS-1$
            if (format != null) {
            	extension = format.getDefaultExtension();
            }
            
            setFilename(new File(
                    System.getProperty("user.home") +  //$NON-NLS-1$
                    File.separator + StringUtil.unqualifiedName(contentsName) + extension).getAbsolutePath());
        }
    }
    
    protected void projectChanged() {
        if ("".equals(projectField.getText())) { //$NON-NLS-1$
            contentsControl.setPdProject(null);
            return;
        }
        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
        if (project.exists()) {
            contentsControl.setPdProject(project);
            return;
        }
        contentsControl.setPdProject(null);
    }
    
    private void setTableContents(ITableContents contents) {
    	if (contents == null) {
            contentsControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
    	}
        contentsControl.setText(contents.getQualifiedName());
        setIpsProject(contents.getIpsProject());
    }
    
    public IIpsProject getIpsProject() {
    	return "".equals(projectField.getText()) ? null : //$NON-NLS-1$
            IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
    }
    
    public void setIpsProject(IIpsProject project) {
        projectControl.setIpsProject(project);
        contentsControl.setPdProject(project);
    }
    
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (e.field == contentsField) {
        	contentsChanged();
        }
        if (e.field==filenameField) {
            filenameChanged();
        }
        if (validateInput) { // don't validate during control creating!
            validatePage();    
        }
        updatePageComplete();
    }
    
    /**
     * Validates the page and generates error messages if needed. 
     * Can be overridden in subclasses to add specific validation logic.s 
     */
    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
		setErrorMessage(null);
        validateProject();
        if (getErrorMessage()!=null) {
            return;
        }
        validateContents();
        if (getErrorMessage()!=null) {
            return;
        }
        validateName();
        if (getErrorMessage()!=null) {
            return;
        }
        validateFormat();
        if (getErrorMessage()!=null) {
            return;
        }
        updatePageComplete();
    }
    
    protected void validateProject() {
        IIpsProject project = getIpsProject();
        if (project == null) {
            setErrorMessage(Messages.TableExportPage_msgInvalidProject);
            return;
        }
        if (!project.exists()) {
            setErrorMessage(Messages.TableExportPage_msgNonExistingProject);
            return;
        }
    }
    
    protected void validateContents() {
        try {
            ITableContents contents = getTableContents();
            if (contents == null) {
                setErrorMessage(Messages.TableExportPage_msgInvalidContents);
                return;
            }
            if (!contents.exists()) {
                setErrorMessage(Messages.TableExportPage_msgNonExisitingContents);
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            setErrorMessage(Messages.TableExportPage_msgValidateContentsError + e);
            return;
        }
        
    }
    
	/**
	 * The method validates the name.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 */
	protected void validateName() {
		String name=filenameField.getText(); 
		// must not be empty
		if (name.length() == 0) {
			setErrorMessage(Messages.TableExportPage_msgEmptyName);
			return;
		}
	}
    
    protected void validateFormat() {
        // must not be empty
        if (fileFormatControl.getSelectionIndex() == -1) {
            setErrorMessage(Messages.TableExportPage_msgMissingFileFormat);
            return;
        }
    }
    
    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(projectField.getText()) //$NON-NLS-1$
        && !"".equals(filenameField.getText()) //$NON-NLS-1$
        && !"".equals(contentsField.getText()) //$NON-NLS-1$
        && fileFormatControl.getSelectionIndex() != -1; 
        setPageComplete(complete);
    }
    
}
