/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.FolderSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Composite for modifying IPS source folders
 * 
 * @author Roman Grutza
 */
public class ProductDefinitionComposite extends IpsSection {

    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private Table table;
    private IIpsObjectPath ipsObjectPath;
    private Button addSrcFolderButton;
    private Button removeSrcFolderButton;

    private FolderSelectionControl mergableSrcFolderControl;
    private TextButtonField mergableSrcFolderField;

    private FolderSelectionControl derivedSrcFolderControl;
    private TextButtonField derivedSrcFolderField;

    private IpsPckFragmentRefControl basePackageMergableControl;
    private TextButtonField basePackageMergableField;

    private IpsPckFragmentRefControl basePackageDerivedControl;
    private TextButtonField basePackageDerivedField;

    // separate output folders for model folders
    private CheckboxField multipleOutputCheckBoxField;
    private boolean dataChanged = false;
    private IIpsProjectProperties iIpsProjectProperties;
    private Set<String> resourcesPathExcludedFromTheProductDefiniton;

    public Set<String> getResourcesPathExcludedFromTheProductDefiniton() {
        return resourcesPathExcludedFromTheProductDefiniton;
    }

    /**
     * @param parent Composite
     */
    public ProductDefinitionComposite(Composite parent, IIpsProjectProperties iIpsProjectProperties, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        this.iIpsProjectProperties = iIpsProjectProperties;
        this.resourcesPathExcludedFromTheProductDefiniton = iIpsProjectProperties
                .getResourcesPathExcludedFromTheProductDefiniton();
        initControls();
        setText(Messages.ProductDefinitionComposite_title);
    }

    private TableViewer createViewer(Composite parent) {
        table = new Table(parent, SWT.BORDER | SWT.SINGLE);
        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(new ArrayContentProvider());
        resourcesPathExcludedFromTheProductDefiniton.add("aaa");
        tableViewer.setInput(resourcesPathExcludedFromTheProductDefiniton);
        // treeViewer.setLabelProvider();
        // IpsPlugin
        // .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
        return tableViewer;
    }

    private void createButtons(Composite buttons, UIToolkit toolkit) {
        addSrcFolderButton = toolkit.createButton(buttons, Messages.ProductDefinitionComposite_add_folder_text);
        addSrcFolderButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        addSrcFolderButton.addMouseListener(new AddMouseHandler());

        removeSrcFolderButton = toolkit.createButton(buttons, Messages.ProductDefinitionComposite_remove_folder_text);
        removeSrcFolderButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        removeSrcFolderButton.addMouseListener(new RemoveMouseHandler());
        // removeSrcFolderButton.addSelectionListener(srcFolderAdapter);
    }

    /**
     * IPS source path entries have been modified
     * 
     * @return true if source path entries have been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
    }

    private void updateWidgetEnabledStates() {
        // boolean multipleOutputFolders = (ipsObjectPath != null) &&
        // ipsObjectPath.isOutputDefinedPerSrcFolder();
        // multipleOutputCheckBoxField.getCheckbox().setChecked(multipleOutputFolders);
        //
        // if (treeViewer.getSelection().isEmpty()) {
        // removeSrcFolderButton.setEnabled(false);
        // editSelectionButton.setEnabled(false);
        //
        // return;
        // }
        //
        // TreeItem[] selection = treeViewer.getTree().getSelection();
        // if (selection.length > 0) {
        // Object selectedElement = selection[0].getData();
        // if (selectedElement instanceof IIpsSrcFolderEntry) {
        // removeSrcFolderButton.setEnabled(true);
        // editSelectionButton.setEnabled(false);
        // }
        //
        // if (selectedElement instanceof IIpsObjectPathEntryAttribute) {
        // removeSrcFolderButton.setEnabled(false);
        // editSelectionButton.setEnabled(true);
        // }
        // }
    }

    private void removeResourcesPathExcludedFromTheProductDefiniton() {
        ISelection selection = tableViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        if (selection instanceof StructuredSelection) {
            StructuredSelection structuredSelection = (StructuredSelection)selection;
            String element = (String)structuredSelection.getFirstElement();
            resourcesPathExcludedFromTheProductDefiniton.remove(element);
            tableViewer.setInput(resourcesPathExcludedFromTheProductDefiniton);
            tableViewer.refresh(false);
        }

    }

    private void addResourcesPathExcludedFromTheProductDefiniton() {
        // final ElementTreeSelectionDialog selectFolderDialog = createSelectFolderDialog();
        // if (selectFolderDialog.open() == Window.OK) {
        // // add new selected source folders to IPS object path
        // Object[] selectedFolders = selectFolderDialog.getResult();
        // for (Object selectedFolder : selectedFolders) {
        // IFolder folder = (IFolder)selectedFolder;
        // ipsObjectPath.newSourceFolderEntry(folder);
        // treeViewer.refresh(false);
        //
        // dataChanged = true;
        // }
        // }
    }

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        if (Display.getCurrent() != null) {
            tableViewer.setInput(ipsObjectPath);
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    tableViewer.setInput(ipsObjectPath);
                }
            });
        }
    }

    /**
     * Sets the default output folder for mergable/derived sources
     * 
     * @param mergable if true the mergable folder is changed, derived folder otherwise
     */
    private void handleDefaultOutputFolderChanged(boolean mergable) {
        String folderName;
        if (mergable) {
            folderName = (String)mergableSrcFolderField.getValue();
        } else {
            folderName = (String)derivedSrcFolderField.getValue();
        }
        if (folderName == null || "".equals(folderName)) { //$NON-NLS-1$
            return;
        }

        /*
         * Folder field text can temporarily contain invalid values, because it is updated on each
         * keystroke when typed in with keyboard. That's why exceptions are ignored here.
         */
        try {
            IPath path = new Path(IPath.SEPARATOR + folderName);
            IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
            if (mergable) {
                ipsObjectPath.setOutputFolderForMergableSources(folder);
            } else {
                ipsObjectPath.setOutputFolderForDerivedSources(folder);
            }
        } catch (Exception e) { /* ignore */
        }

        dataChanged = true;
    }

    private void handleOutputDefinedPerSrcFolderChanged(boolean multipleEnabled) {
        ipsObjectPath.setOutputDefinedPerSrcFolder(multipleEnabled);
        tableViewer.refresh();

        dataChanged = true;
    }

    /**
     * Sets the default package name for mergable/derived sources
     * 
     * @param mergable if true the mergable package name is changed, otherwise the derived package
     *            name
     */
    private void handleBasePackageNameChanged(boolean mergable) {
        if (mergable) {
            String packageName = basePackageMergableField.getText();
            ipsObjectPath.setBasePackageNameForMergableJavaClasses(packageName);
        } else {
            String packageName = basePackageDerivedField.getText();
            ipsObjectPath.setBasePackageNameForDerivedJavaClasses(packageName);
        }

        dataChanged = true;
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, true));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        Composite tableWithButtons = toolkit.createGridComposite(composite, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label treeViewerLabel = new Label(tableWithButtons, SWT.NONE);
        treeViewerLabel.setText(Messages.ProductDefinitionComposite_treeViewer_label);
        GridData gd = new GridData(SWT.BEGINNING);
        gd.horizontalSpan = 2;
        treeViewerLabel.setLayoutData(gd);

        tableViewer = createViewer(tableWithButtons);
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = toolkit.createComposite(tableWithButtons);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons, toolkit);

    }

    @Override
    protected void performRefresh() {
        // TODO Auto-generated method stub

    }

    private class RemoveMouseHandler implements MouseListener {

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseDown(MouseEvent e) {
            removeResourcesPathExcludedFromTheProductDefiniton();

        }

        @Override
        public void mouseUp(MouseEvent e) {
            // TODO Auto-generated method stub

        }

    }

    private class AddMouseHandler implements MouseListener {

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseDown(MouseEvent e) {
            addResourcesPathExcludedFromTheProductDefiniton();

        }

        @Override
        public void mouseUp(MouseEvent e) {
            // TODO Auto-generated method stub

        }

    }

}
