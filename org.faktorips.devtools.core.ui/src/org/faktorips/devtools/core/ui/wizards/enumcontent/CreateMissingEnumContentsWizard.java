/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.util.StringUtil;

/**
 * The <tt>CreateMissingEnumContentsWizard</tt> that searches for <tt>IEnumContent</tt>s that should
 * be existent in <tt>IIpsProject</tt>s (the information is derived from the <tt>IIpsProject</tt>'s
 * <tt>IEnumType</tt>s). The wizard enables the user to comfortably select the missing
 * <tt>IEnumContent</tt>s that shall be created.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class CreateMissingEnumContentsWizard extends Wizard {

    /** The one and only wizard page. */
    private SelectEnumContentsPage selectEnumContentsPage;

    /** The <tt>WizardDialog</tt> this wizard will be started with. */
    private WizardDialog wizardDialog;

    /** The preselected <tt>IIpsElement</tt>. */
    private IIpsElement preselectedIpsElement;

    /**
     * Creates the <tt>CreateMissingEnumContentsWizard</tt>.
     * 
     * @param preselectedIpsElement The preselected <tt>IIpsElement</tt>.
     */
    public CreateMissingEnumContentsWizard(IIpsElement preselectedIpsElement) {
        this.preselectedIpsElement = preselectedIpsElement;
        setWindowTitle(Messages.CreateMissingEnumContentsWizard_title);
        setNeedsProgressMonitor(true);
    }

    /** Opens the wizard in a new <tt>WizardDialog</tt>. */
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
     * The action will run in the <tt>IRunnableContext</tt> of the wizard dialog so that a progress
     * monitor can be shown directly in the wizard.
     */
    @Override
    public boolean performFinish() {
        try {
            Object[] checkedElements = selectEnumContentsPage.viewer.getCheckedElements();
            IIpsPackageFragmentRoot targetRoot = selectEnumContentsPage.getTargetIpsProject()
                    .getIpsPackageFragmentRoot(selectEnumContentsPage.getTargetSourceFolderName());
            wizardDialog.run(true, true, new CreateIpsSrcFilesRunnable(checkedElements, targetRoot));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /** This page enables the user to select the missing <tt>IEnumContent</tt>s to create. */
    private class SelectEnumContentsPage extends WizardPage implements ValueChangeListener {

        /** The tree viewer widget. */
        private ContainerCheckedTreeViewer viewer;

        /** The content provider for the tree viewer. */
        private ITreeContentProvider contentProvider;

        /** The text field on the top of the wizard to select the target source folder with. */
        private IpsPckFragmentRootRefControl targetSourceFolderControl;

        /** Creates the <tt>SelectEnumContentsPage</tt>. */
        protected SelectEnumContentsPage() {
            super(Messages.SelectEnumContentsPage_title);
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "wizards/CreateMissingEnumContentsWizard.png")); //$NON-NLS-1$
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
         * Creates the section on top of the wizard page containing a <tt>TextButtonField</tt> to
         * select the target source folder with.
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
            viewer.setInput(this); // input element can be anything (but an existing package name
            // including the default package name ""
            viewer.addCheckStateListener(new ICheckStateListener() {
                @Override
                public void checkStateChanged(CheckStateChangedEvent event) {
                    setPageComplete(checkPageCompleteCondition());
                }
            });
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
            selectAllButton.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    setAllChecked(true);
                }
            });
            Button deselectAllButton = new Button(selectionButtonsComposite, SWT.NONE);
            deselectAllButton.setText(Messages.SelectEnumContentsPage_buttonDeselectAll);
            deselectAllButton.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    setAllChecked(false);
                }
            });
        }

        /**
         * Initializes the target folder <tt>TextButtonControl</tt> based on the user selection
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
         * Checks whether this page is complete. Returns <tt>true</tt> if at least one element is
         * checked in the tree viewer widget and the page is valid, <tt>false</tt> otherwise.
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
                        root = targetIpsProject.getIpsPackageFragmentRoot(targetRoot);
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
         * <tt>TextButtonControl</tt>. If this is not possible <tt>null</tt> will be returned.
         */
        private IIpsProject getTargetIpsProject() {
            String targetFolder = targetSourceFolderControl.getText();
            if (!(targetFolder.contains("/"))) { //$NON-NLS-1$
                return null;
            }
            String targetProject = targetFolder.substring(0, targetFolder.indexOf('/'));
            return IpsPlugin.getDefault().getIpsModel().getIpsProject(targetProject);
        }

        /**
         * Resolves the name of the target source folder specified by the user from the target
         * source folder <tt>TextButtonControl</tt>.
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

        /** The <tt>LabelProvider</tt> for the tree viewer widget. */
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
                return IpsUIPlugin.getImageHandling().getSharedImage("IpsPackageFragment.gif", true); //$NON-NLS-1$
            }

        }

        /** The <tt>ContentProvider</tt> for the tree viewer widget. */
        private class EnumContentsContentProvider implements ITreeContentProvider {

            /** A map that is used to speed up the <tt>getChildren(Object)</tt> operation. */
            private Map<IIpsPackageFragment, List<IEnumType>> treeStructure;

            /** Creates the <tt>EnumContentsContentProvider</tt>. */
            public EnumContentsContentProvider() {
                treeStructure = new HashMap<IIpsPackageFragment, List<IEnumType>>();
            }

            @Override
            public Object[] getElements(Object inputElement) {
                treeStructure.clear();
                List<IIpsPackageFragment> elements = new ArrayList<IIpsPackageFragment>();
                try {
                    IIpsProject targetProject = getTargetIpsProject();
                    IIpsPackageFragmentRoot targetRoot = getTargetPackageFragmentRoot();
                    if (targetProject != null) {
                        for (IEnumType currentEnumType : findEnumTypesNeedingEnumContent(targetProject)) {
                            String enumContentName = currentEnumType.getEnumContentName();
                            String currentPackName = StringUtil.getPackageName(enumContentName);
                            IIpsPackageFragment pack = targetRoot.getIpsPackageFragment(currentPackName);
                            List<IEnumType> list = treeStructure.get(pack);
                            if (list == null) {
                                list = new ArrayList<IEnumType>();
                            }
                            list.add(currentEnumType);
                            treeStructure.put(pack, list);
                            if (!(elements.contains(pack))) {
                                elements.add(pack);
                            }
                        }
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
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
                List<Object> children = new LinkedList<Object>();
                if (parentElement instanceof IIpsPackageFragment) {
                    children.addAll(treeStructure.get(parentElement));
                }
                return children.toArray();
            }

            /**
             * Searches for <tt>IEnumType</tt>s currently lacking of <tt>IEnumContent</tt>s while
             * needing them. Returns all found <tt>IEnumType</tt>s in a list.
             * <p>
             * Never returns <tt>null</tt>; An empty list is returned if no <tt>IEnumType</tt>s were
             * found.
             * 
             * @param ipsProject The IPS project (starting point) where <tt>IEnumType</tt>s needing
             *            <tt>IEnumContent</tt>s shall be searched.
             * 
             * @throws CoreException If an error occurs while searching the given IPS project for
             *             <tt>IEnumType</tt>s.
             */
            private List<IEnumType> findEnumTypesNeedingEnumContent(IIpsProject ipsProject) throws CoreException {
                List<IEnumType> enumTypesInProject = ipsProject.findEnumTypes(false, true);
                List<IEnumType> retEnumTypes = new ArrayList<IEnumType>(enumTypesInProject.size() / 2);
                for (IEnumType currentEnumType : enumTypesInProject) {
                    String enumContentName = currentEnumType.getEnumContentName();
                    if (currentEnumType.isContainingValues() || enumContentName.length() == 0) {
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
     * This <tt>IRunnableWithProgress</tt> will be executed when the user clicks the 'Finish' -
     * button. All selected missing <tt>IEnumContent</tt>s will be created by the operation.
     */
    private class CreateIpsSrcFilesRunnable implements IRunnableWithProgress {

        /**
         * The elements that user selected in the tree viewer widget of the
         * <tt>SelectEnumContentsPage</tt>.
         */
        private Object[] checkedElements;

        /**
         * The target <tt>IIpsPackageFragmentRoot</tt> the user selected the missing
         * <tt>IEnumContent</tt>s to be created into.
         */
        private IIpsPackageFragmentRoot targetRoot;

        /**
         * Creates the <tt>CreateIpsSrcFilesRunnable</tt>.
         * 
         * @param checkedElements The elements the user selected in the tree viewer widget of the
         *            <tt>SelectEnumContentsPage</tt>.
         * @param targetRoot The target <tt>IIpsPackageFragmentRoot</tt> missing
         *            <tt>IEnumContent</tt>s shall be created into.
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
                    try {
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
                        file.save(true, null);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
                monitor.worked(1);
            }
            monitor.done();
        }
    }

}
