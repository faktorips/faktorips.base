package org.faktorips.devtools.core.ui.editors.testcase;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;
import org.faktorips.devtools.core.ui.IIpsDropAdapterProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsViewerDropAdapter;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.util.StringUtil;

public class TestCaseDropAdapter extends IpsViewerDropAdapter implements IIpsDropAdapterProvider {

    protected TestCaseDropAdapter(Viewer viewer) {
        super(viewer);
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        event.detail = DND.DROP_LINK;
        super.dragEnter(event);
    }

    private List<IProductCmpt> insertNewProductCmpts(IProductCmptTypeAssociation association,
            Object data,
            IProductCmpt parent,
            IProductCmptLink insertBefore) {

        ProcessDropRunnable runnable = new ProcessDropRunnable(parent, association, insertBefore);
        IRunnableWithProgress runnableAdapter = getRunnableAdapter(runnable);
        executeRunnable(runnableAdapter);

        return runnable.getImportedCmpts();
    }

    private void executeRunnable(IRunnableWithProgress runnableAdapter) {
        Shell shell = IpsUIPlugin.getDefault().getWorkbench().getDisplay().getActiveShell();
        ProgressMonitorDialog dlg = new ProgressMonitorDialog(shell);
        try {
            dlg.run(false, true, runnableAdapter);
        } catch (InvocationTargetException e) {
            IpsPlugin.log(e);
        } catch (InterruptedException e) {
            IpsPlugin.log(e);
        }
    }

    private IRunnableWithProgress getRunnableAdapter(IWorkspaceRunnable runnable) {
        ISchedulingRule rule = null;
        Job job = Job.getJobManager().currentJob();
        if (job != null) {
            rule = job.getRule();
        }
        if (rule != null) {
            return new WorkbenchRunnableAdapter(runnable, rule);
        } else {
            return new WorkbenchRunnableAdapter(runnable, ResourcesPlugin.getWorkspace().getRoot());
        }
    }

    // private boolean isDataAccepted(TransferData data) {
    // return PsyProductTransfer.getInstance().isSupportedType(data);
    // }

    private boolean isTargetValid(Object target) {
        return target instanceof ITestCase;
    }

    @Override
    public boolean validateDropSingle(Object target, int operation, TransferData data) {
        return isTargetValid(target);// && isDataAccepted(data);
    }

    @Override
    public boolean performDropSingle(Object data) {
        IProductCmptStructureReference ref = (IProductCmptStructureReference)getCurrentTarget();
        IIpsObjectPart part = ref.getWrapped();

        List<IProductCmpt> result;
        if (part instanceof IProductCmptTypeAssociation) {
            IProductCmpt parent = (IProductCmpt)ref.getParent().getWrappedIpsObject();
            result = insertNewProductCmpts((IProductCmptTypeAssociation)part, data, parent, null);
        } else if (part instanceof IProductCmptLink) {
            if (getCurrentLocation() != LOCATION_ON) {
                result = insertNewProductCmptsBetween(ref, data);
            } else {
                return true;
            }
        } else {
            //            IpsUIPlugin.log("Unknown type of drop-target: " + (part == null ? "null" : part.getClass())); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        return !result.isEmpty();
    }

    private List<IProductCmpt> insertNewProductCmptsBetween(IProductCmptStructureReference dropTarget, Object data) {
        IProductCmptStructureReference associationRef = dropTarget.getParent();
        IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)associationRef.getWrapped();
        IProductCmpt parent = (IProductCmpt)associationRef.getParent().getWrappedIpsObject();

        return insertNewProductCmpts(association, data, parent, null);
    }

    /**
     * Find the appropriate {@link IProductCmptType} for the given association. That means to take
     * the {@link IProductCmptTypeAssociation#getTarget()} and look for a package in the given
     * project matching this (unqualified) type name. This search is done up the type hierarchy till
     * a package is found.
     * 
     * @param association The association the target type is needed for.
     * @param project Searched for the matching package
     * @return The found {@link IProductCmptType} or <code>null</code> if no matching type/package
     *         combination was found.
     */
    public static IProductCmptType findProductCmptType(IProductCmptTypeAssociation association, IIpsProject project) {
        IProductCmptType type;
        try {
            type = getTargetType(project, association.findTargetProductCmptType(project));
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
        return type;
    }

    private static IProductCmptType getTargetType(IIpsProject project, IProductCmptType type) {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        String qTypeName = type.getQualifiedName();
        IIpsPackageFragment pack = null;
        for (IIpsPackageFragmentRoot root : roots) {
            pack = root.getIpsPackageFragment(StringUtil.unqualifiedName(qTypeName));
            if (pack.exists()) {
                return type;
            }
        }

        return getTargetTypeDeepSearch(project, type);
    }

    private static IProductCmptType getTargetTypeDeepSearch(IIpsProject project, IProductCmptType type) {
        IPolicyCmptType policyCmptType;
        ITypeHierarchy hierarchy;
        try {
            policyCmptType = type.findPolicyCmptType(project);
            hierarchy = policyCmptType.getSubtypeHierarchy();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }

        return getTargetTypeDeepSearch(project, hierarchy.getAllSubtypes(policyCmptType));
    }

    private static IProductCmptType getTargetTypeDeepSearch(IIpsProject project, List<IType> subTypes) {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        IIpsPackageFragment pack = null;
        for (IType subPolicyCmptType : subTypes) {
            String qTypeName = ((IPolicyCmptType)subPolicyCmptType).getProductCmptType();
            String unqualifiedName = StringUtil.unqualifiedName(qTypeName);
            for (IIpsPackageFragmentRoot root : roots) {
                pack = root.getIpsPackageFragment(unqualifiedName);
                if (pack.exists()) {
                    try {
                        return project.findProductCmptType(qTypeName);
                    } catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    private static final class ProcessDropRunnable implements IWorkspaceRunnable {

        private IProductCmpt parent;
        private IProductCmptTypeAssociation association;
        private IProductCmptLink insertBefore;
        private List<IProductCmpt> result;

        public ProcessDropRunnable(IProductCmpt parent, IProductCmptTypeAssociation association,
                IProductCmptLink insertBefore) {

            this.parent = parent;
            this.association = association;
            this.insertBefore = insertBefore;
        }

        @Override
        public void run(IProgressMonitor monitor) {
            result = new ArrayList<IProductCmpt>();
            IIpsProject targetProject = parent.getIpsProject();

            // UnspecificImporter importer = new UnspecificImporter(targetProject,
            // ImportPolicy.DEFAULT, monitor);
            // VSUImporter vsuImporter = new VSUImporter(targetProject, ImportPolicy.DEFAULT,
            // monitor);
            // importer.setTypeName(findProductCmptType(association,
            // targetProject).getQualifiedName());
            //
            // for (PsyProductTransferInfo transferInfo : transferInfos) {
            // AbstractProductPart psyNode = findPsyNode(transferInfo);
            // importer.processImport(psyNode);
            // result.addAll(importer.getProducts());
            // importVSU(vsuImporter, psyNode.getChildren());
            // Cardinality cardinality = null;
            // if (psyNode instanceof ProductPart) {
            // cardinality = ((ProductPart)psyNode).getCardinality();
            // }
            //
            // for (IProductCmpt usedCmpt : importer.getProducts()) {
            // psyNode.getPsyImage().registerPsyMapping(psyNode, usedCmpt, false);
            // }
            // }
        }

        /**
         * Returns the list of all created {@link IProductCmpt components} during this drop.
         * 
         * @since 1.0.0
         */
        public List<IProductCmpt> getImportedCmpts() {
            return Collections.unmodifiableList(result);
        }

    }

    @Override
    public List<Transfer> getSupportedTransferTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSupportedOperations() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IpsViewerDropAdapter getDropAdapter(Viewer viewer) {
        return new TestCaseDropAdapter(viewer);
    }

}
