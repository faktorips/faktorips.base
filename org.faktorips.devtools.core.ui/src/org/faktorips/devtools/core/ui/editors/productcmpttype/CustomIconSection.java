/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.Locale;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.internal.ProductCmptDecorator;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IType;

/**
 * This Section lets the user select icons that are used when instances of the edited type are
 * displayed in FIPS/Eclipse.
 * <p>
 * Note: This class can be easily extended for use with all ITypes (especially
 * {@link IPolicyCmptType}. The Methods in {@link IProductCmptType} as
 * {@link IProductCmptType#getInstancesIcon()} etc. then have to be pulled up to {@link IType}.
 */
public class CustomIconSection extends IpsSection {

    private IType type;
    private Text iconPathText;
    private Button iconBrowseButton;

    private Label iconPreview;

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
            createDescriptionLabel(client);
            createEnabledIconControls(client, toolkit);
            performRefresh();
        } else {
            Label unsupportedTypeLabel = new Label(client, SWT.NONE);
            unsupportedTypeLabel.setText(Messages.CustomIconSection_UnsupportedType_Label);
            GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1);
            unsupportedTypeLabel.setLayoutData(gd);
        }
    }

    private void createDescriptionLabel(Composite parent) {
        Label iconDescriptionLabel = new Label(parent, SWT.WRAP);
        iconDescriptionLabel.setText(Messages.CustomIconSection_ConfigurationDescription);
        GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 3, 1);
        gd.widthHint = 650;
        iconDescriptionLabel.setLayoutData(gd);
    }

    private void createEnabledIconControls(Composite parent, UIToolkit toolkit) {
        toolkit.createLabel(parent, Messages.CustomIconSection_IconPreviewLabel);

        iconPreview = new Label(parent, SWT.BORDER | SWT.CENTER);
        GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        gd.heightHint = 16;
        gd.widthHint = 16;
        gd.horizontalSpan = 2;
        iconPreview.setLayoutData(gd);

        toolkit.createLabel(parent, Messages.CustomIconSection_CustomPathText);
        iconPathText = toolkit.createText(parent);

        iconBrowseButton = toolkit.createButton(parent, Messages.CustomIconSection_BrowseButtonText);
        iconBrowseButton.addSelectionListener(new BrowseIconsListener());

        getBindingContext().bindContent(iconPathText, type, IProductCmptType.PROPERTY_ICON_FOR_INSTANCES);

        // Update Icon on path-change
        getBindingContext().add(new ControlPropertyBinding(iconPathText, type,
                IProductCmptType.PROPERTY_ICON_FOR_INSTANCES, String.class) {
            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                refreshIcon();
            }
        });

        getBindingContext().updateUI();
    }

    @Override
    protected void performRefresh() {
        if (!(type instanceof IProductCmptType)) {
            return;
        }
        refreshIcon();
    }

    private void refreshIcon() {
        if (!(type instanceof IProductCmptType)) {
            return;
        }
        IIpsElementDecorator decorator = IIpsDecorators.get(ProductCmpt.class);
        if (decorator instanceof ProductCmptDecorator) {
            ProductCmptDecorator adapter = (ProductCmptDecorator)decorator;
            iconPreview.setImage(IpsUIPlugin.getImageHandling()
                    .getImage(adapter.getImageDescriptorForInstancesOf((IProductCmptType)type)));
        }
    }

    public void setType(IType newType) {
        type = newType;
        performRefresh();
    }

    private class BrowseIconsListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
                    new WorkbenchContentProvider());

            dialog.setValidator(new FileValidator());
            dialog.setAllowMultiple(false);
            dialog.setTitle(Messages.CustomIconSection_SelectImageDialog_Title);
            dialog.setMessage(Messages.CustomIconSection_SelectImageDialog_Description);
            dialog.addFilter(new FileExtensionFilter(IProductCmptType.SUPPORTED_ICON_EXTENSIONS
                    .toArray(new String[IProductCmptType.SUPPORTED_ICON_EXTENSIONS.size()])));
            dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());

            if (dialog.open() == Window.OK) {
                // Validator ensures a file is returned as first element
                IFile file = (IFile)dialog.getFirstResult();
                /**
                 * Requirement by Jan: Images for Model-Classes must be accessible via the
                 * IpsObjectPath. Thus image-paths are be saved sourcefolder- or archive-relative
                 * respectively.
                 * <p>
                 * Note: This may cause problems when using Icons with the same name in different
                 * IPS SourceFolders (which is possible within the same project). Furthermore images
                 * will be cached by the IpsUIPlugin using their path, so problems may arise with
                 * caching as well.
                 */
                ((ProductCmptType)type).setInstancesIcon(file.getFullPath().removeFirstSegments(2).toString());
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

            if (element instanceof IFolder) {
                IFolder folder = (IFolder)element;
                if (!isIpsSrcFolder(folder)) {
                    return false;
                }
                try {
                    IResource[] resources = ((IContainer)element).members();
                    for (IResource resource : resources) {
                        if (select(viewer, parent, resource)) {
                            return true;
                        }
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }

            if (element instanceof IProject) {
                return selectIpsProjectsInPath((IProject)element);
            }

            // e.g. for WorkspaceRoot
            return false;
        }

        /**
         * Returns <code>true</code> if the given folder is an IPS SourceFolder
         * (PackageFragmentRoot) or a sub folder of an IPS SourceFolder (PackageFragment) and thus
         * part of the {@link IpsObjectPath}.
         */
        private boolean isIpsSrcFolder(IFolder folder) {
            IIpsProject ipsProject = IIpsModel.get()
                    .getIpsProject(Wrappers.wrap(folder.getProject()).as(AProject.class));
            IIpsPackageFragmentRoot root = ipsProject
                    .getIpsPackageFragmentRoot(folder.getProjectRelativePath().toOSString());
            if (root != null && root.exists()) {
                return true;
            } else {
                // Check if parentFolder is PackageFragmentRoot
                IContainer parent = folder.getParent();
                if (parent instanceof IFolder) {
                    return isIpsSrcFolder((IFolder)parent);
                } else {
                    return false;
                }
            }
        }

        /**
         * Returns <code>true</code> for all {@link IIpsProject}s from which the edited
         * {@link ProductCmptType} can load its custom icons, including the IpsProject it is
         * contained in. <code>false</code> for all other projects.
         */
        private boolean selectIpsProjectsInPath(IProject project) {
            try {
                if (project.isOpen() && project.hasNature(IIpsProject.NATURE_ID)) {
                    // the ipsProject itself
                    if (project.equals(type.getIpsProject().getProject().unwrap())) {
                        return true;
                    }
                    // referenced ipsProjects
                    IIpsProject ipsProject = IIpsModel.get().getIpsProject(Wrappers.wrap(project).as(AProject.class));
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

        @Override
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
