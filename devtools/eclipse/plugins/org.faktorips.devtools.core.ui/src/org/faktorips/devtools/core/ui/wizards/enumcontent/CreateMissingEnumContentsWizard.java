/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.StringUtil;

/**
 * The <code>CreateMissingEnumContentsWizard</code> that searches for <code>IEnumContent</code>s
 * that should be existent in <code>IIpsProject</code>s (the information is derived from the
 * <code>IIpsProject</code>'s <code>IEnumType</code>s). The wizard enables the user to comfortably
 * select the missing <code>IEnumContent</code>s that shall be created.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class CreateMissingEnumContentsWizard extends Wizard {

    /** The one and only wizard page. */
    private SelectEnumContentsPage selectEnumContentsPage;

    /** The <code>WizardDialog</code> this wizard will be started with. */
    private WizardDialog wizardDialog;

    /** The preselected <code>IIpsElement</code>. */
    private IIpsElement preselectedIpsElement;

    /**
     * Creates the <code>CreateMissingEnumContentsWizard</code>.
     * 
     * @param preselectedIpsElement The preselected <code>IIpsElement</code>.
     */
    public CreateMissingEnumContentsWizard(IIpsElement preselectedIpsElement) {
        this.preselectedIpsElement = preselectedIpsElement;
        setWindowTitle(Messages.CreateMissingEnumContentsWizard_title);
        setNeedsProgressMonitor(true);
    }

    /** Opens the wizard in a new <code>WizardDialog</code>. */
    public void open(Shell parentShell) {
        if (wizardDialog != null) {
            return;
        }
        wizardDialog = new WizardDialog(parentShell, this);
        wizardDialog.open();
    }

    @Override
    public void addPages() {
        selectEnumContentsPage = new SelectEnumContentsPage();
        addPage(selectEnumContentsPage);
    }

    /**
     * The action will run in the <code>IRunnableContext</code> of the wizard dialog so that a
     * progress monitor can be shown directly in the wizard.
     */
    @Override
    public boolean performFinish() {
        try {
            Object[] checkedElements = selectEnumContentsPage.viewer.getCheckedElements();
            IIpsPackageFragmentRoot targetRoot = selectEnumContentsPage.getTargetIpsProject()
                    .getIpsPackageFragmentRoot(selectEnumContentsPage.getTargetSourceFolderName());
            wizardDialog.run(true, true, new CreateIpsSrcFilesRunnable(checkedElements, targetRoot));
        } catch (InvocationTargetException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /** This page enables the user to select the missing <code>IEnumContent</code>s to create. */
    private class SelectEnumContentsPage extends WizardPage implements ValueChangeListener {

        /** The tree viewer widget. */
        private ContainerCheckedTreeViewer viewer;

        /** The content provider for the tree viewer. */
        private ITreeContentProvider contentProvider;

        /** The text field on the top of the wizard to select the target source folder with. */
        private IpsPckFragmentRootRefControl targetSourceFolderControl;

        /** Creates the <code>SelectEnumContentsPage</code>. */
        protected SelectEnumContentsPage() {
            super(Messages.SelectEnumContentsPage_title);
            setImageDescriptor(IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("wizards/CreateMissingEnumContentsWizard.png")); //$NON-NLS-1$
            setMessage(Messages.SelectEnumContentsPage_prompt);
            setTitle(Messages.SelectEnumContentsPage_title);
        }

        @Override
        public void createControl(Composite parent) {
            Composite pageControl = new Composite(parent, SWT.NONE);
            pageControl.setLayout(new GridLayout(1, true));
            pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            createTopSection(pageControl);
            createTreeViewer(pageControl);
            createSelectionButtons(pageControl);

            refresh();
            pageControl.pack();
            setControl(pageControl);
        }

        /**
         * Creates the section on top of the wizard page containing a <code>TextButtonField</code>
         * to select the target source folder with.
         */
        private void createTopSection(Composite pageControl) {
            UIToolkit toolkit = new UIToolkit(null);
            Composite twoColumnComposite = toolkit.createLabelEditColumnComposite(pageControl);
            toolkit.createFormLabel(twoColumnComposite, Messages.SelectEnumContentsPage_labelTargetSourceFolder);
            targetSourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(twoColumnComposite, true);
            TextButtonField sourceFolderField = new TextButtonField(targetSourceFolderControl);
            setTargetFolderBasedOnSelection(sourceFolderField);
            sourceFolderField.addChangeListener(this);
            targetSourceFolderControl.setFocus();
            toolkit.createVerticalSpacer(pageControl, 1);
            toolkit.createHorizonzalLine(pageControl);
            toolkit.createVerticalSpacer(pageControl, 2);
        }

        /** Creates and configures the tree viewer widget in the center of the wizard page. */
        private void createTreeViewer(Composite pageControl) {
            viewer = new ContainerCheckedTreeViewer(pageControl, SWT.FILL | SWT.BORDER);
            viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            contentProvider = new EnumContentsContentProvider();
            viewer.setContentProvider(contentProvider);
            viewer.setLabelProvider(new EnumContentsLabelProvider());
            viewer.setInput(this);
            // input element can be anything (but an existing package name including the default
            // package name ""
            viewer.addCheckStateListener($ -> setPageComplete(checkPageCompleteCondition()));
            viewer.setComparator(new ViewerComparator());
            viewer.expandAll();
            setAllChecked(true);
        }

        /**
         * Creates and configures the 'Select all' and 'Deselect all' buttons below the tree viewer
         * widget.
         */
        private void createSelectionButtons(Composite pageControl) {
            Composite selectionButtonsComposite = new Composite(pageControl, SWT.NONE);
            FillLayout buttonLayout = new FillLayout();
            buttonLayout.marginHeight = 1;
            buttonLayout.spacing = 5;
            selectionButtonsComposite.setLayout(buttonLayout);
            Button selectAllButton = new Button(selectionButtonsComposite, SWT.NONE);
            selectAllButton.setText(Messages.SelectEnumContentsPage_buttonSelectAll);
            selectAllButton.addListener(SWT.Selection, $ -> setAllChecked(true));
            Button deselectAllButton = new Button(selectionButtonsComposite, SWT.NONE);
            deselectAllButton.setText(Messages.SelectEnumContentsPage_buttonDeselectAll);
            deselectAllButton.addListener(SWT.Selection, $ -> setAllChecked(false));
        }

        /**
         * Initializes the target folder <code>TextButtonControl</code> based on the user selection
         * input.
         */
        private void setTargetFolderBasedOnSelection(TextButtonField targetFolderField) {
            String sourceFolderName = null;
            if (preselectedIpsElement instanceof IIpsPackageFragmentRoot) {
                IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot)preselectedIpsElement;
                sourceFolderName = root.getName();
            }
            if (preselectedIpsElement instanceof IIpsPackageFragment) {
                IIpsPackageFragment fragment = (IIpsPackageFragment)preselectedIpsElement;
                sourceFolderName = fragment.getRoot().getName();
            }
            if (preselectedIpsElement instanceof IIpsObject) {
                IIpsObject ipsObject = (IIpsObject)preselectedIpsElement;
                sourceFolderName = ipsObject.getIpsPackageFragment().getRoot().getName();
            }
            if (preselectedIpsElement instanceof IIpsObjectPart) {
                IIpsObjectPart ipsObjectPart = (IIpsObjectPart)preselectedIpsElement;
                sourceFolderName = ipsObjectPart.getIpsObject().getIpsPackageFragment().getRoot().getName();
            }
            if (preselectedIpsElement instanceof IIpsProject) {
                IIpsProject ipsProject = (IIpsProject)preselectedIpsElement;
                IIpsPackageFragmentRoot[] rootsInProject = ipsProject.getIpsPackageFragmentRoots();
                if (rootsInProject.length > 0) {
                    sourceFolderName = rootsInProject[0].getName();
                }
            }
            if (sourceFolderName != null) {
                IIpsProject ipsProject = preselectedIpsElement.getIpsProject();
                targetFolderField.setText(ipsProject.getName() + '/' + sourceFolderName);
            }
        }

        /**
         * Checks whether this page is complete. Returns <code>true</code> if at least one element
         * is checked in the tree viewer widget and the page is valid, <code>false</code> otherwise.
         */
        private boolean checkPageCompleteCondition() {
            return viewer.getCheckedElements().length > 0 && validatePage();
        }

        /** Sets all elements in the tree viewer widget to the checked state. */
        private void setAllChecked(boolean checked) {
            Object[] checkedElements = checked ? contentProvider.getElements(new Object()) : new Object[0];
            viewer.setCheckedElements(checkedElements);
        }

        /**
         * Refreshes the wizard page, if a valid IPS project is selected all entries of the tree
         * viewer widget will be expanded and checked.
         */
        @Override
        public void valueChanged(FieldValueChangedEvent event) {
            refresh();
            if (getTargetIpsProject() != null && validatePage()) {
                viewer.expandAll();
                setAllChecked(true);
            }
        }

        /**
         * Refreshes this wizard page by refreshing the tree viewer widget and checking the page
         * complete condition.
         */
        private void refresh() {
            viewer.refresh();
            setPageComplete(checkPageCompleteCondition());
        }

        /**
         * Validates the user input for this wizard page. If a validation error occurs a
         * corresponding error message will be displayed in the wizard.
         */
        private boolean validatePage() {
            String errorMessage = null;

            String targetFolder = targetSourceFolderControl.getText();
            if (targetFolder.length() == 0) {
                errorMessage = Messages.SelectEnumContentsPage_msgTargetSourceFolderNotSpecified;
            } else {
                IIpsProject targetIpsProject = getTargetIpsProject();
                if (targetIpsProject == null) {
                    errorMessage = Messages.SelectEnumContentsPage_msgTargetSourceFolderDoesNotExist;
                }
                boolean targetProjectDoesNotExist = (targetIpsProject == null) ? true : !(targetIpsProject.exists());
                if (targetProjectDoesNotExist) {
                    errorMessage = Messages.SelectEnumContentsPage_msgTargetSourceFolderDoesNotExist;
                } else {
                    String targetRoot = getTargetSourceFolderName();
                    IIpsPackageFragmentRoot root = null;
                    if (targetRoot.length() > 0) {
                        root = targetIpsProject == null ? null : targetIpsProject.getIpsPackageFragmentRoot(targetRoot);
                    }
                    boolean rootDoesNotExist = (root == null) ? true : !(root.exists());
                    if (rootDoesNotExist) {
                        errorMessage = Messages.SelectEnumContentsPage_msgTargetSourceFolderDoesNotExist;
                    }
                }
            }

            setErrorMessage(errorMessage);
            return errorMessage == null;
        }

        /**
         * Resolves the target IPS project from the user input in the target source folder
         * <code>TextButtonControl</code>. If this is not possible <code>null</code> will be
         * returned.
         */
        private IIpsProject getTargetIpsProject() {
            String targetFolder = targetSourceFolderControl.getText();
            if (!(targetFolder.contains("/"))) { //$NON-NLS-1$
                return null;
            }
            String targetProject = targetFolder.substring(0, targetFolder.indexOf('/'));
            return IIpsModel.get().getIpsProject(targetProject);
        }

        /**
         * Resolves the name of the target source folder specified by the user from the target
         * source folder <code>TextButtonControl</code>.
         */
        private String getTargetSourceFolderName() {
            String targetFolder = targetSourceFolderControl.getText();
            return targetFolder.substring(targetFolder.indexOf('/') + 1);
        }

        private IIpsPackageFragmentRoot getTargetPackageFragmentRoot() {
            IIpsProject targetProject = getTargetIpsProject();
            IIpsPackageFragmentRoot root = targetProject.getIpsPackageFragmentRoot(getTargetSourceFolderName());
            if (root == null) {
                root = targetProject.getIpsPackageFragmentRoot("VirtualRoot"); //$NON-NLS-1$
            }
            return root;
        }

        /** The <code>LabelProvider</code> for the tree viewer widget. */
        private class EnumContentsLabelProvider extends DefaultLabelProvider {

            @Override
            public String getText(Object element) {
                if (element instanceof IEnumType) {
                    IEnumType enumType = (IEnumType)element;
                    String enumContentName = enumType.getEnumContentName();
                    return StringUtil.unqualifiedName(enumContentName);
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof IEnumType) {
                    return IpsUIPlugin.getImageHandling().getSharedImage("NewEnumContent.gif", true); //$NON-NLS-1$
                }
                IIpsPackageFragment pack = (IIpsPackageFragment)element;
                if (pack == null || !(pack.exists())) {
                    return IpsUIPlugin.getImageHandling().getSharedImage("NewIpsPackageFragment.gif", true); //$NON-NLS-1$
                }
                return IIpsDecorators.getImageHandling().getSharedImage("IpsPackageFragment.gif", true); //$NON-NLS-1$
            }

        }

        /** The <code>ContentProvider</code> for the tree viewer widget. */
        private class EnumContentsContentProvider implements ITreeContentProvider {

            /** A map that is used to speed up the <code>getChildren(Object)</code> operation. */
            private Map<IIpsPackageFragment, List<IEnumType>> treeStructure;

            /** Creates the <code>EnumContentsContentProvider</code>. */
            public EnumContentsContentProvider() {
                treeStructure = new HashMap<>();
            }

            @Override
            public Object[] getElements(Object inputElement) {
                treeStructure.clear();
                List<IIpsPackageFragment> elements = new ArrayList<>();
                IIpsProject targetProject = getTargetIpsProject();
                IIpsPackageFragmentRoot targetRoot = getTargetPackageFragmentRoot();
                if (targetProject != null) {
                    for (IEnumType currentEnumType : findEnumTypesNeedingEnumContent(targetProject)) {
                        String enumContentName = currentEnumType.getEnumContentName();
                        String currentPackName = StringUtil.getPackageName(enumContentName);
                        IIpsPackageFragment pack = targetRoot.getIpsPackageFragment(currentPackName);
                        List<IEnumType> list = treeStructure.computeIfAbsent(pack, $ -> new ArrayList<>());
                        if (!list.contains(currentEnumType)) {
                            list.add(currentEnumType);
                        }
                        if (!(elements.contains(pack))) {
                            elements.add(pack);
                        }
                    }
                }
                return elements.toArray();
            }

            @Override
            public void dispose() {
                treeStructure = null;
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                List<Object> children = new LinkedList<>();
                if (parentElement instanceof IIpsPackageFragment) {
                    children.addAll(treeStructure.get(parentElement));
                }
                return children.toArray();
            }

            /**
             * Searches for <code>IEnumType</code>s currently lacking of <code>IEnumContent</code>s
             * while needing them. Returns all found <code>IEnumType</code>s in a list.
             * <p>
             * Never returns <code>null</code>; An empty list is returned if no
             * <code>IEnumType</code>s were found.
             * 
             * @param ipsProject The IPS project (starting point) where <code>IEnumType</code>s
             *            needing <code>IEnumContent</code>s shall be searched.
             */
            private List<IEnumType> findEnumTypesNeedingEnumContent(IIpsProject ipsProject) {
                List<IEnumType> enumTypesInProject = ipsProject.findEnumTypes(false, true);
                List<IEnumType> retEnumTypes = new ArrayList<>(enumTypesInProject.size() / 2);
                for (IEnumType currentEnumType : enumTypesInProject) {
                    String enumContentName = currentEnumType.getEnumContentName();
                    if (currentEnumType.isInextensibleEnum() || enumContentName.length() == 0) {
                        continue;
                    }
                    if (ipsProject.findEnumContent(currentEnumType) == null) {
                        retEnumTypes.add(currentEnumType);
                    }
                }
                return retEnumTypes;
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof IEnumType) {
                    IEnumType enumType = (IEnumType)element;
                    String enumContentName = enumType.getEnumContentName();
                    return StringUtil.getPackageName(enumContentName);
                }
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                return element instanceof IIpsPackageFragment;
            }

        }

    }

    /**
     * This <code>IRunnableWithProgress</code> will be executed when the user clicks the 'Finish' -
     * button. All selected missing <code>IEnumContent</code>s will be created by the operation.
     */
    private static class CreateIpsSrcFilesRunnable implements IRunnableWithProgress {

        /**
         * The elements that user selected in the tree viewer widget of the
         * <code>SelectEnumContentsPage</code>.
         */
        private Object[] checkedElements;

        /**
         * The target <code>IIpsPackageFragmentRoot</code> the user selected the missing
         * <code>IEnumContent</code>s to be created into.
         */
        private IIpsPackageFragmentRoot targetRoot;

        /**
         * Creates the <code>CreateIpsSrcFilesRunnable</code>.
         * 
         * @param checkedElements The elements the user selected in the tree viewer widget of the
         *            <code>SelectEnumContentsPage</code>.
         * @param targetRoot The target <code>IIpsPackageFragmentRoot</code> missing
         *            <code>IEnumContent</code>s shall be created into.
         */
        public CreateIpsSrcFilesRunnable(Object[] checkedElements, IIpsPackageFragmentRoot targetRoot) {
            this.checkedElements = checkedElements;
            this.targetRoot = targetRoot;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask(Messages.CreateMissingEnumContentsWizard_labelOperation, checkedElements.length);
            for (Object checkedObj : checkedElements) {
                if (monitor.isCanceled()) {
                    return;
                }
                if (checkedObj instanceof IEnumType) {
                    IEnumType currentEnumType = (IEnumType)checkedObj;
                    String enumContentQualifiedName = currentEnumType.getEnumContentName();
                    String enumContentPackageName = StringUtil.getPackageName(enumContentQualifiedName);
                    String enumContentName = StringUtil.unqualifiedName(enumContentQualifiedName);
                    IIpsPackageFragment pack = targetRoot.getIpsPackageFragment(enumContentPackageName);
                    if (!pack.exists()) {
                        pack = targetRoot.createPackageFragment(enumContentPackageName, true, null);
                    }
                    if (monitor.isCanceled()) {
                        return;
                    }
                    IIpsSrcFile file = pack.createIpsFile(IpsObjectType.ENUM_CONTENT, enumContentName, true, null);
                    IEnumContent enumContent = (IEnumContent)file.getIpsObject();
                    enumContent.setEnumType(currentEnumType.getQualifiedName());
                    file.save(null);
                }
                monitor.worked(1);
            }
            monitor.done();
        }
    }

}
