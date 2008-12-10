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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;

/**
 * 
 * @author Thorsten Waertel
 */
public class SelectContentsPage extends WizardPage implements ValueChangeListener {
    public static final String PAGE_NAME= "SelectContentsPage"; //$NON-NLS-1$

    // Stored widget contents
	
    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    // edit controls
    private IpsProjectRefControl projectControl;
	private TableContentsRefControl contentsControl;
    
    // edit fields
    private TextButtonField projectField;
    private TextButtonField contentsField;
    
    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;
    
    // page control as defined by the wizard page class
    private Composite pageControl;
    
	/**
	 * @param pageName
     * @param selection
     * @throws JavaModelException
	 */
	public SelectContentsPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.SelectContentsPage_title);
        
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();                
        } else if (selection.getFirstElement() instanceof IIpsElement) {
        	selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
        setPageComplete(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.SelectContentsPage_title);
        
        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);
        
        Composite locationComposite  = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.SelectContentsPage_labelProject);
        projectControl = toolkit.createIpsProjectRefControl(locationComposite);
        projectField = new TextButtonField(projectControl);
        projectField.addChangeListener(this);
        
        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Composite lowerComposite = toolkit.createLabelEditColumnComposite(pageControl); 
        toolkit.createFormLabel(lowerComposite, Messages.SelectContentsPage_labelContents);
        contentsControl = toolkit.createTableContentsRefControl(null, lowerComposite);
        contentsField = new TextButtonField(contentsControl);
        contentsField.addChangeListener(this);

        setDefaults(selectedResource);

        validateInput = true;
	}

    /**
     * Derives the default values for source folder and package from
     * the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when
     * the wizard was opened.
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
    
    public ITableContents getTableContents() throws CoreException {
        return contentsControl.findTableContents();
    }
    
    protected void contentsChanged() {
    }
    
    protected void projectChanged() {
        if ("".equals(projectField.getText())) { //$NON-NLS-1$
            contentsControl.setIpsProject(null);
            return;
        }
        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
        if (project.exists()) {
            contentsControl.setIpsProject(project);
            return;
        }
        contentsControl.setIpsProject(null);
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
        contentsControl.setIpsProject(project);
    }
    
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (e.field == contentsField) {
            contentsChanged();
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
        updatePageComplete();
    }
    
    protected void validateProject() {
        IIpsProject project = getIpsProject();
        if (project == null) {
            setErrorMessage(Messages.SelectContentsPage_msgInvalidProject);
            return;
        }
        if (!project.exists()) {
            setErrorMessage(Messages.SelectContentsPage_msgNonExistingProject);
            return;
        }
    }
    
    protected void validateContents() {
        try {
            ITableContents contents = getTableContents();
            if (contents == null) {
                setErrorMessage(NLS.bind(Messages.SelectContentsPage_msgMissingContent, contentsControl.getText()));
                return;
            }
            if (!contents.exists()) {
                setErrorMessage(NLS.bind(Messages.SelectContentsPage_msgMissingContent, contentsControl.getText()));
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            setErrorMessage(Messages.SelectContentsPage_msgMissingContent + e);
            return;
        }
        
    }
    
    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(projectField.getText()) //$NON-NLS-1$
        && !"".equals(contentsField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    /**
     * {@inheritDoc}
     */
    public void saveWidgetValues() {
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean allowNewContainerName() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
    }
}
