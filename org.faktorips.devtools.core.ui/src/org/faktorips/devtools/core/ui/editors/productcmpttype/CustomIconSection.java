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
import org.eclipse.ui.model.IWorkbenchAdapter;
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
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter;

/**
 * This Section lets the user select icons that are used when instances of the edited type are
 * displayed in FIPS/Eclipse. The user can define an enabled and a disabled Icon separately.
 * <p>
 * Note: This class can be easily extended for use with all ITypes (especially
 * {@link IPolicyCmptType}. The Methods in {@link IProductCmptType} as
 * {@link IProductCmptType#getInstancesEnabledIcon()} etc. then have to be pulled up to
 * {@link IType}.
 * 
 * @author Stefan Widmaier, Faktor Zehn AG
 */
public class CustomIconSection extends IpsSection {

    private IType type;
    private Label disabledIconTextLabel;
    private Text disabledIconPathText;
    private Button disabledIconBrowseButton;
    private Label enabledIconTextLabel;
    private Text enabledIconPathText;
    private Button enabledIconBrowseButton;
    private Checkbox checkBox;
    private Label enabledIconLabel;
    private Label disabledIconLabel;

    /*
     * TODO: rewrite constructor to expect an IType when IPolicyCmptType also requires an Icon.
     */
    public CustomIconSection(IProductCmptType type, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.type = type;
        initControls();
        setText("Instance Icon Settings");
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);
        createInheritedIconLabel(client, toolkit);
        createUseCustomIconCheckBox(client, toolkit);
        createEnabledIconControls(client, toolkit);
        createDisabledIconControls(client, toolkit);
        performRefresh();
    }

    private void createInheritedIconLabel(Composite parent, UIToolkit toolkit) {
        Composite inheritedIconComposite = toolkit.createComposite(parent);
        GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        inheritedIconComposite.setLayoutData(gd);
        inheritedIconComposite.setLayout(layout);

        enabledIconLabel = new Label(inheritedIconComposite, SWT.BORDER | SWT.CENTER);
        gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
        gd.heightHint = 16;
        gd.widthHint = 16;
        enabledIconLabel.setLayoutData(gd);

        disabledIconLabel = new Label(inheritedIconComposite, SWT.BORDER | SWT.CENTER);
        gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
        disabledIconLabel.setLayoutData(gd);
        gd.heightHint = 16;
        gd.widthHint = 16;

        Label iconDescriptionLabel = new Label(inheritedIconComposite, SWT.NONE);
        iconDescriptionLabel.setText("These icons will be used for all instances of this type.");
        gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 1, 1);
        iconDescriptionLabel.setLayoutData(gd);
    }

    private void createUseCustomIconCheckBox(Composite client, UIToolkit toolkit) {
        checkBox = toolkit.createCheckbox(client, "Use Custom Icons for Instances");
        GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1);
        checkBox.setLayoutData(gd);
        checkBox.getButton().addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                IProductCmptType prodType = (IProductCmptType)type;
                prodType.setUseCustomInstanceIcon(checkBox.isChecked());
                performRefresh();
            }
        });
    }

    private void createEnabledIconControls(Composite parent, UIToolkit toolkit) {
        enabledIconTextLabel = toolkit.createLabel(parent, "Icon for enabled Instances:");
        enabledIconPathText = toolkit.createText(parent);
        enabledIconBrowseButton = toolkit.createButton(parent, "Browse");
        enabledIconBrowseButton.addSelectionListener(new BrowseIconsListener(enabledIconPathText));

        GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        enabledIconTextLabel.setLayoutData(gd);
        gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1);
        enabledIconPathText.setLayoutData(gd);

        enabledIconPathText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (isRefreshing()) {
                    return;
                }
                IProductCmptType prodType = (IProductCmptType)type;
                if (!ObjectUtils.equals(prodType.getInstancesEnabledIcon(), enabledIconPathText.getText())) {
                    prodType.setInstancesEnabledIcon(enabledIconPathText.getText());
                }
                refreshIcons();
            }

        });
    }

    private void createDisabledIconControls(Composite parent, UIToolkit toolkit) {
        disabledIconTextLabel = toolkit.createLabel(parent, "Icon for disabled Instances:");
        disabledIconPathText = toolkit.createText(parent);
        disabledIconBrowseButton = toolkit.createButton(parent, "Browse");
        disabledIconBrowseButton.addSelectionListener(new BrowseIconsListener(disabledIconPathText));

        GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        disabledIconTextLabel.setLayoutData(gd);
        gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1);
        disabledIconPathText.setLayoutData(gd);

        disabledIconPathText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (isRefreshing()) {
                    return;
                }
                IProductCmptType prodType = (IProductCmptType)type;
                if (!ObjectUtils.equals(prodType.getInstancesDisabledIcon(), disabledIconPathText.getText())) {
                    prodType.setInstancesDisabledIcon(disabledIconPathText.getText());
                }
                refreshIcons();
            }

        });
    }

    private void enableIconEditControls(boolean enable) {
        enabledIconBrowseButton.setEnabled(enable);
        enabledIconPathText.setEnabled(enable);
        enabledIconTextLabel.setEnabled(enable);
        disabledIconBrowseButton.setEnabled(enable);
        disabledIconPathText.setEnabled(enable);
        disabledIconTextLabel.setEnabled(enable);
    }

    @Override
    protected void performRefresh() {
        if (type == null) {
            return;
        }
        IProductCmptType prodCmptType = (IProductCmptType)type;
        boolean hasCustomIcon = prodCmptType.isUseCustomInstanceIcons();
        if (checkBox.isChecked() != hasCustomIcon) {
            // optimization
            checkBox.setChecked(hasCustomIcon);
        }
        enableIconEditControls(hasCustomIcon);
        enabledIconPathText.setText(prodCmptType.getInstancesEnabledIcon());
        disabledIconPathText.setText(prodCmptType.getInstancesDisabledIcon());
        refreshIcons();
    }

    private void refreshIcons() {
        ProductCmptWorkbenchAdapter adapter = (ProductCmptWorkbenchAdapter)IpsUIPlugin.getDefault().getAdapterFactory()
                .getAdapter(ProductCmpt.class, IWorkbenchAdapter.class);
        // try {
        enabledIconLabel.setImage(IpsUIPlugin.getDefault().getImage(
                adapter.getImageDescriptorForInstancesOf((IProductCmptType)type)));
        // FIXME DiabledIcon aktualisieren
        // Image disabledIconImage =
        // type.getIpsProject().getCustomInstanceDisabledIconOrDefault((IProductCmptType)type);
        // disabledIconLabel.setImage(disabledIconImage);
        // } catch (CoreException e) {
        // IpsPlugin.log(e);
        // }
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
            dialog.setTitle("Icon auswählen");
            dialog.setMessage("Bitte wählen sie ein Icon aus");
            dialog.addFilter(new FileExtensionFilter(new String[] { "gif", "png" }));
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
                if (file.getName().toLowerCase(Locale.ENGLISH).endsWith("." + extension)) {
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
            return new Status(IStatus.ERROR, IpsUIPlugin.PLUGIN_ID, IStatus.ERROR, "", //$NON-NLS-1$
                    null);
        }

    }
}
