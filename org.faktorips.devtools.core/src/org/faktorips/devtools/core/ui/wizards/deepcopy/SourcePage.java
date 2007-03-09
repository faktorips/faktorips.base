/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.util.message.MessageList;

/**
 * Page to let the user select products related to each other.
 * 
 * @author Thorsten Guenther
 */
public class SourcePage extends WizardPage implements ICheckStateListener {
    private IProductCmptStructure structure;
    private CheckboxTreeViewer tree;
    private CheckStateListener checkStateListener;
    /**
     * Cotnrol for search pattern
     */
    private Text searchInput;

    /**
     * Control for replace text
     */
    private Text replaceInput;
    /**
     * The input field for the user to enter a version id to be used for all newly created product
     * components.
     */
    private Text versionId;

    /**
     * Control for target package.
     */
    private IpsPckFragmentRefControl targetInput;

    /**
     * The naming strategy which is to be used to find the correct new names of the product
     * components to create.
     */
    private IProductCmptNamingStrategy namingStrategy;

    private int type;

    static final String PAGE_ID = "deepCopyWizard.source"; //$NON-NLS-1$

    private static String getTitle(int type) {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return Messages.SourcePage_title;
        }
        else {
            return NLS.bind(Messages.SourcePage_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        }
    }

    /**
     * Creates a new page to select the objects to copy.
     */
    protected SourcePage(IProductCmptStructure structure, int type) {
        super(PAGE_ID, getTitle(type), null);
        this.structure = structure;
        this.type = type;

        setDescription(Messages.SourcePage_msgSelect);

        super.setDescription(Messages.SourcePage_description);
        
        try {
            this.namingStrategy = structure.getRoot().getProductCmpt().getIpsProject().getProductCmptNamingStrategy();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        if (structure == null) {
            Label errormsg = new Label(parent, SWT.WRAP);
            GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
            errormsg.setLayoutData(layoutData);
            errormsg.setText(Messages.SourcePage_msgCircleRelation);
            this.setControl(errormsg);
            return;
        }

        UIToolkit toolkit = new UIToolkit(null);
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        toolkit.createFormLabel(inputRoot, IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate());
        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelTargetPackage);
        targetInput = toolkit.createPdPackageFragmentRefControl(structure.getRoot().getProductCmpt()
                .getIpsPackageFragment().getRoot(), inputRoot);
        
        // set target default
        targetInput.setIpsPackageFragment(getPackage());
        
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelSearchPattern);
            searchInput = toolkit.createText(inputRoot);

            toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelReplacePattern);
            replaceInput = toolkit.createText(inputRoot);
        }

        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            String label = NLS.bind(Messages.ReferenceAndPreviewPage_labelVersionId, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
            toolkit.createFormLabel(inputRoot, label);
            versionId = toolkit.createText(inputRoot);
            versionId.setText(namingStrategy.getNextVersionId(structure.getRoot().getProductCmpt()));
            versionId.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    getWizard().getContainer().updateButtons();
                }
            });
        }

        tree = new CheckboxTreeViewer(root);
        tree.setUseHashlookup(true);
        tree.setLabelProvider(new DeepCopyLabelProvider());
        tree.setContentProvider(new DeepCopyContentProvider(true));
        tree.setInput(this.structure);
        tree.expandAll();
        setCheckedAll(tree.getTree().getItems(), true);
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tree.addCheckStateListener(this);
        checkStateListener = new CheckStateListener(null);
        tree.addCheckStateListener(checkStateListener);
        
        // add Listener to the target text control (must be done here after the default is set)
        targetInput.getTextControl().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getWizard().getContainer().updateButtons();
            }
        });        
    }

    private void setCheckedAll(TreeItem[] items, boolean checked) {
        for (int i = 0; i < items.length; i++) {
            items[i].setChecked(checked);
            setCheckedAll(items[i].getItems(), checked);
        }
    }

    /**
     * Calculate the number of <code>IPath</code>-segements which are equal for all product
     * components to copy.
     * 
     * @return 0 if no elements are contained in toCopy, number of all segments, if only one product
     *         component is contained in toCopy and the calculated value as described above for all
     *         other cases.
     */
    int getSegmentsToIgnore(IProductCmptReference[] toCopy) {
        if (toCopy.length == 0) {
            return 0;
        }

        IPath refPath = toCopy[0].getProductCmpt().getIpsPackageFragment().getRelativePath();
        if (toCopy.length == 1) {
            return refPath.segmentCount();
        }

        int ignore = Integer.MAX_VALUE;
        for (int i = 1; i < toCopy.length; i++) {
            int tmpIgnore;
            IPath nextPath = toCopy[i].getProductCmpt().getIpsPackageFragment().getRelativePath();
            tmpIgnore = nextPath.matchingFirstSegments(refPath);
            if (tmpIgnore < ignore) {
                ignore = tmpIgnore;
            }
        }

        return ignore;
    }
    
    IIpsPackageFragment getPackage() {
        int ignore = getSegmentsToIgnore((IProductCmptReference[])structure.toArray(true));
        IIpsPackageFragment pack = structure.getRoot().getProductCmpt().getIpsPackageFragment();
        int segments = pack.getRelativePath().segmentCount();
        if (segments - ignore > 0) {
            IPath path = pack.getRelativePath().removeLastSegments(segments - ignore);
            pack = pack.getRoot().getIpsPackageFragment(path.toString().replace('/', '.'));
        }
        return pack;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        boolean pageComplete = tree != null && tree.getCheckedElements().length > 0;
        setMessage(null);
        setErrorMessage(null);

        if (namingStrategy != null && namingStrategy.supportsVersionId() ) {
            MessageList ml = namingStrategy.validateVersionId(versionId.getText());
            if (!ml.isEmpty()) {
                setErrorMessage(ml.getMessage(0).getText());
                pageComplete = false;
                
            }
        }

        if (structure == null) {
            setErrorMessage(Messages.SourcePage_msgCircleRelationShort);
            pageComplete = false;
        }

        if (getTargetPackage() != null && !getTargetPackage().exists()) {
            setMessage(NLS.bind(Messages.SourcePage_msgWarningTargetWillBeCreated, getTargetPackage().getName()),
                    WARNING);
        } else if (getTargetPackage() == null){
            setErrorMessage(Messages.SourcePage_msgBadTargetPackage);
        }
        
        return pageComplete;
    }

    public IProductCmptStructureReference[] getCheckedNodes() {
        return (IProductCmptStructureReference[])Arrays.asList(tree.getCheckedElements()).toArray(
                new IProductCmptStructureReference[0]);
    }

    /**
     * Returns the pattern used to find the text to replace. This string is guaranteed to be either
     * empty or a valid pattern for java.util.regex.Pattern.
     */
    public String getSearchPattern() {
        String result = searchInput.getText();
        try {
            Pattern.compile(result);
        }
        catch (PatternSyntaxException e) {
            result = ""; //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Returns the text to replace the text found by the search pattern.
     */
    public String getReplaceText() {
        return replaceInput.getText();
    }

    /**
     * Returns the text to replace the text found by the search pattern.
     */
    public String getVersion() {
        return versionId.getText();
    }

    /**
     * Returns the text to replace the text found by the search pattern.
     */
    public IProductCmptNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    /**
     * Returns the package fragment which is to be used as target package for the copy.
     */
    public IIpsPackageFragment getTargetPackage() {
        return targetInput.getIpsPackageFragment();
    }

    public void checkStateChanged(CheckStateChangedEvent event) {

        // we have to check or uncheck all items which represent the same ipselement
        // because the decision of copy or not copy is global.
        IProductCmptStructureReference changed = (IProductCmptStructureReference)event.getElement();
        IProductCmptReference root = structure.getRoot();

        if (!(changed instanceof IProductCmptReference)) {
            IProductCmptTypeRelationReference[] children = structure
                    .getChildProductCmptTypeRelationReferences((IProductCmptStructureReference)event.getElement());
            for (int i = 0; i < children.length; i++) {
                setCheckState(children[i].getRelation(), new IProductCmptReference[] { root }, event.getChecked());
            }
        }
        else {
            setCheckState(((IProductCmptReference)changed).getProductCmpt(), new IProductCmptReference[] { root },
                    event.getChecked());
        }
    }

    private void setCheckState(IIpsElement toCompareWith, IProductCmptStructureReference[] nodes, boolean checked) {
        if (nodes instanceof IProductCmptReference[]) {
            for (int i = 0; i < nodes.length; i++) {
                setCheckState(toCompareWith, structure.getChildProductCmptTypeRelationReferences(nodes[i]), checked);
                if (((IProductCmptReference)nodes[i]).getProductCmpt().equals(toCompareWith)) {
                    tree.setChecked(nodes[i], checked);
                    checkStateListener.updateCheckState(tree, nodes[i], checked);
                }
            }
        }
        else {
            for (int i = 0; i < nodes.length; i++) {
                setCheckState(toCompareWith, structure.getChildProductCmptReferences(nodes[i]), checked);
                if (((IProductCmptTypeRelationReference)nodes[i]).getRelation().equals(toCompareWith)) {
                    tree.setChecked(nodes[i], checked);
                    checkStateListener.updateCheckState(tree, nodes[i], checked);
                }
            }
        }

    }

}
