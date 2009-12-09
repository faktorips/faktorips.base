/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter;

/**
 * This Section lets the user select icons that are used when instances of the edited type are
 * displayed in FIPS/Eclipse. The user can define an enabled and a disabled Icon separately.
 * <p>
 * Note: This class can be easily extended for use with all ITypes (especially
 * {@link IPolicyCmptType}. The Methods in {@link IProductCmptType} as
 * {@link IProductCmptType#getInstancesIcon()} etc. then have to be pulled up to {@link IType}.
 * 
 * @author Stefan Widmaier, Faktor Zehn AG
 */
public class CustomIconSection extends IpsSection {

    private IType type;
    private Label enabledIconTextLabel;
    private Text enabledIconPathText;
    private Button enabledIconBrowseButton;

    private Label enabledIconPreview;

    public CustomIconSection(IType type, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.type = type;
        initControls();
        setText(Messages.CustomIconSection_SectionTitle);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);
        if (type instanceof IProductCmptType) {
            createDescriptionLabel(client, toolkit);
            createEnabledIconControls(client, toolkit);
            performRefresh();
        } else {
            Label unsupportedTypeLabel = new Label(client, SWT.NONE);
            unsupportedTypeLabel.setText(Messages.CustomIconSection_UnsupportedType_Label);
            GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1);
            unsupportedTypeLabel.setLayoutData(gd);
        }
    }

    private void createDescriptionLabel(Composite parent, UIToolkit toolkit) {
        Label iconDescriptionLabel = new Label(parent, SWT.WRAP);
        iconDescriptionLabel.setText(Messages.CustomIconSection_ConfigurationDescription);
        GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 3, 1);
        gd.widthHint = 600;
        iconDescriptionLabel.setLayoutData(gd);
    }

    private void createEnabledIconControls(Composite parent, UIToolkit toolkit) {
        toolkit.createLabel(parent, Messages.CustomIconSection_IconPreviewLabel);

        enabledIconPreview = new Label(parent, SWT.BORDER | SWT.CENTER);
        GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        gd.heightHint = 16;
        gd.widthHint = 16;
        gd.horizontalSpan = 2;
        enabledIconPreview.setLayoutData(gd);

        enabledIconTextLabel = toolkit.createLabel(parent, Messages.CustomIconSection_CustomPathText);
        enabledIconPathText = toolkit.createText(parent);
        enabledIconPathText.setEnabled(true);

        enabledIconBrowseButton = toolkit.createButton(parent, Messages.CustomIconSection_BrowseButtonText);
        enabledIconBrowseButton.addSelectionListener(new BrowseIconsListener(enabledIconPathText));

        enabledIconPathText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (isRefreshing()) {
                    return;
                }
                IProductCmptType prodType = (IProductCmptType)type;
                if (!ObjectUtils.equals(prodType.getInstancesIcon(), enabledIconPathText.getText())) {
                    prodType.setInstancesIcon(enabledIconPathText.getText());
                }
                refreshIcons();
            }

        });
    }

    @Override
    protected void performRefresh() {
        if (type == null) {
            return;
        }
        IProductCmptType prodCmptType = (IProductCmptType)type;
        enabledIconPathText.setText(prodCmptType.getInstancesIcon());
        refreshIcons();
    }

    private void refreshIcons() {
        if (type instanceof IProductCmptType) {
            ProductCmptWorkbenchAdapter adapter = (ProductCmptWorkbenchAdapter)IpsUIPlugin.getDefault()
                    .getWorkbenchAdapterFor(ProductCmpt.class);
            enabledIconPreview.setImage(IpsUIPlugin.getDefault().getImage(
                    adapter.getImageDescriptorForInstancesOf((IProductCmptType)type)));
        }
    }

    public void setType(IType newType) {
        type = newType;
        performRefresh();
    }

    private class BrowseIconsListener implements SelectionListener {
        private Text text;

        public BrowseIconsListener(Text textControl) {
            text = textControl;
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void widgetSelected(SelectionEvent e) {
            ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                    new WorkbenchLabelProvider(), new WorkbenchContentProvider());

            dialog.setValidator(new FileValidator());
            dialog.setAllowMultiple(false);
            dialog.setTitle(Messages.CustomIconSection_SelectImageDialog_Title);
            dialog.setMessage(Messages.CustomIconSection_SelectImageDialog_Description);
            dialog.addFilter(new FileExtensionFilter(new String[] { "gif", "png" })); //$NON-NLS-1$ //$NON-NLS-2$
            dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());

            if (dialog.open() == Window.OK) {
                // Validator ensures a file is returned as first element
                IFile file = (IFile)dialog.getFirstResult();
                // remove project from file-path. Using the ipsObjectPath Images must be addressed
                // project- or archive-relative respectively.
                text.setText(file.getFullPath().removeFirstSegments(1).toString());
                performRefresh();
            }
        }

    }

    /**
     * Selects files with the given file-extension that are contained in IpsProjects. Folders that
     * contain such files are also selected.
     * <p>
     * Note: this class was copied from org.eclipse.pde.internal.ui.util and extended by a check for
     * IpsNature and IpsObjectPath.
     */
    private class FileExtensionFilter extends ViewerFilter {

        private String[] targetExtensions;

        public FileExtensionFilter(String[] fileExtensions) {
            targetExtensions = fileExtensions;
        }

        @Override
        public boolean select(Viewer viewer, Object parent, Object element) {
            if (element instanceof IFile) {
                return selectFile((IFile)element);
            }

            if (element instanceof IProject) {
                return selectIpsProjectsInPath((IProject)element);
            }

            if (element instanceof IContainer) { // i.e. IProject, IFolder
                try {
                    IResource[] resources = ((IContainer)element).members();
                    for (int i = 0; i < resources.length; i++) {
                        if (select(viewer, parent, resources[i])) {
                            return true;
                        }
                    }
                } catch (CoreException e) {
                }
            }
            return false;
        }

        /**
         * Returns <code>true</code> for all {@link IIpsProject}s from which the edited
         * {@link ProductCmptType} can load its custom icons, including the IpsProject it is
         * contained in. <code>false</code> for all other projects.
         * 
         * @param project
         * @return
         */
        private boolean selectIpsProjectsInPath(IProject project) {
            try {
                if (project.isOpen() && project.hasNature(IIpsProject.NATURE_ID)) {
                    // the ipsProject itself
                    if (project.equals(type.getIpsProject().getProject())) {
                        return true;
                    }
                    // referenced ipsProjects
                    IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
                    IIpsObjectPath ioPath = type.getIpsProject().getIpsObjectPath();
                    if (ioPath.containsProjectRefEntry(ipsProject)) {
                        return true;
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return false;
        }

        /**
         * Returns <code>true</code> for files with one of the defined Extensions,
         * <code>false</code> otherwise.
         * 
         * @param file
         * @return
         */
        private boolean selectFile(IFile file) {
            for (String extension : targetExtensions) {
                if (file.getName().toLowerCase(Locale.ENGLISH).endsWith("." + extension)) { //$NON-NLS-1$
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Sends an Error-Status if no file is selected in the IconSelectionDialog.
     * <p>
     * Note: this class was copied from org.eclipse.pde.internal.ui.util and extended by the
     * IpsUIPlugin-ID.
     */
    private class FileValidator implements ISelectionStatusValidator {

        public IStatus validate(Object[] selection) {
            if (selection.length > 0 && selection[0] instanceof IFile) {
                return new Status(IStatus.OK, IpsUIPlugin.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
                        null);
            }
            return new Status(IStatus.ERROR, IpsUIPlugin.PLUGIN_ID, IStatus.ERROR,
                    Messages.CustomIconSection_SelectImageDialog_Error_NoFileSelected, null);
        }

    }
}
