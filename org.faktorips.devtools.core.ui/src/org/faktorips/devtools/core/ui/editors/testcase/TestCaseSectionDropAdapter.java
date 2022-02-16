/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.util.NestedProjectFileUtil;

/**
 * Provides drop support for {@linkplain TestCaseSection}.
 * <p>
 * <strong>Supported transfers:</strong>
 * <ul>
 * <li>{@linkplain LocalSelectionTransfer}
 * <li>{@linkplain FileTransfer}
 * </ul>
 * <p>
 * <strong>Supported operations:</strong>
 * <ul>
 * <li>{@linkplain DND#DROP_MOVE}:
 * <ul>
 * <li>The DND operation is set to this value if the drag was initiated from within
 * {@linkplain TestCaseSection} itself.
 * <li>The intention is to interchange the positions of two {@linkplain ITestPolicyCmpt test policy
 * components}.
 * <li>The drop is not possible if no {@linkplain LocalSelectionTransfer} is used, or the selected
 * element is not a test policy component.
 * </ul>
 * <li>{@linkplain DND#DROP_LINK}:
 * <ul>
 * <li>The DND operation is set to this value if the drag was initiated from any other source than
 * the {@linkplain TestCaseSection} itself.
 * <li>The intention is to drop a {@linkplain IProductCmpt product component} onto a
 * {@linkplain ITestPolicyCmpt test policy component} or onto a {@linkplain TestCaseTypeAssociation}
 * , in order to add a new {@linkplain ITestPolicyCmptLink test policy component link} to the
 * {@linkplain ITestCase test case} that is being edited.
 * <li>The drop is not possible if the dragged element does not adapt to {@link IProductCmpt}, or
 * that product component is not allowed for the target.</li>
 * </ul>
 * </ul>
 */
class TestCaseSectionDropAdapter extends ViewerDropAdapter {

    final TestCaseSection testCaseSection;

    private final DropToMoveHelper dropToMoveHelper;

    private final DropToLinkHelper dropToLinkHelper;

    TestCaseSectionDropAdapter(Viewer viewer, TestCaseSection testCaseSection) {
        super(viewer);
        this.testCaseSection = testCaseSection;
        dropToMoveHelper = new DropToMoveHelper(this);
        dropToLinkHelper = new DropToLinkHelper(this);
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        /*
         * Some operating systems do not fill file transfer data during drop validation. In this
         * case, we have to simply allow the drop. Note that validation is called once again during
         * performDrop(...).
         */
        if (isFileTransferWithoutValidationSupport(transferType)) {
            return true;
        }

        if (testCaseSection.isLocalDragAndDrop()) {
            return dropToMoveHelper.validateDrop(target, transferType);
        } else {
            return dropToLinkHelper.validateDrop(target, transferType);
        }
    }

    private boolean isFileTransferWithoutValidationSupport(TransferData transferType) {
        if (!FileTransfer.getInstance().isSupportedType(transferType)) {
            return false;
        }
        return FileTransfer.getInstance().nativeToJava(transferType) == null;
    }

    @Override
    public boolean performDrop(Object data) {
        if (testCaseSection.isLocalDragAndDrop()) {
            return dropToMoveHelper.performDrop(data, (ITestPolicyCmpt)getInsertAt());
        } else {
            if (dropToLinkHelper.validateDrop(getCurrentTarget(), getCurrentEvent().currentDataType)) {
                return dropToLinkHelper.performDrop(data);
            }
            return false;
        }
    }

    private Object getInsertAt() {
        if (getCurrentEvent().item != null && getCurrentEvent().item.getData() != null) {
            return getCurrentEvent().item.getData();
        }

        // Event happened on the tree viewer, but not targeted at an entry
        TreeItem[] items = ((TreeViewer)getViewer()).getTree().getItems();
        return items.length > 0 ? items[items.length - 1].getData() : null;
    }

    @Override
    public Viewer getViewer() {
        return super.getViewer();
    }

    @Override
    public DropTargetEvent getCurrentEvent() {
        return super.getCurrentEvent();
    }

    @Override
    public Object getCurrentTarget() {
        return super.getCurrentTarget();
    }

}

class DropToMoveHelper {

    private final TestCaseSectionDropAdapter viewerDropAdapter;

    DropToMoveHelper(TestCaseSectionDropAdapter viewerDropAdapter) {
        this.viewerDropAdapter = viewerDropAdapter;
    }

    public boolean validateDrop(Object target, TransferData transferType) {
        if (!LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
            return false;
        }

        ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
        TypedSelection<ITestPolicyCmpt> typedSelection = TypedSelection.create(ITestPolicyCmpt.class, selection, 1, 1);
        return typedSelection.isValid() ? isValidToMove(target, typedSelection.getElement()) : false;
    }

    private boolean isValidToMove(Object target, ITestPolicyCmpt source) {
        if (source == null) {
            return false;
        }
        if (!(target instanceof ITestPolicyCmpt)) {
            return false;
        }
        return source.getParentTestPolicyCmpt().equals(((ITestPolicyCmpt)target).getParentTestPolicyCmpt());
    }

    public boolean performDrop(Object data, ITestPolicyCmpt targetTestPolicyCmpt) {
        viewerDropAdapter.getCurrentEvent().detail = DND.DROP_MOVE;

        ITestPolicyCmpt droppedTestPolicyCmpt = getDroppedTestPolicyCmpt(data);
        final ITestPolicyCmpt parentTestPolicyCmpt = droppedTestPolicyCmpt.getParentTestPolicyCmpt();
        int posTarget = parentTestPolicyCmpt.getIndexOfChildTestPolicyCmpt(targetTestPolicyCmpt);
        final int posSource = parentTestPolicyCmpt.getIndexOfChildTestPolicyCmpt(droppedTestPolicyCmpt);

        int steps = posSource - posTarget;
        final boolean up = (steps >= 0);
        final int stepsToMove = Math.abs(steps);
        ICoreRunnable moveRunnable = $ -> {
            int currPos = posSource;
            for (int i = 0; i < stepsToMove; i++) {
                parentTestPolicyCmpt.moveTestPolicyCmptLink(new int[] { currPos }, up);
                currPos += (up ? -1 : 1);
            }
        };
        IIpsModel.get().runAndQueueChangeEvents(moveRunnable, null);

        ISelection selection = getViewer().getSelection();
        getTestCaseSection().refreshTreeAndDetailArea();
        getViewer().setSelection(selection);

        return true;
    }

    private ITestPolicyCmpt getDroppedTestPolicyCmpt(Object data) {
        if (!(data instanceof IStructuredSelection)) {
            return null;
        }
        return (ITestPolicyCmpt)(((IStructuredSelection)data).getFirstElement());
    }

    private Viewer getViewer() {
        return viewerDropAdapter.getViewer();
    }

    private TestCaseSection getTestCaseSection() {
        return viewerDropAdapter.testCaseSection;
    }

}

class DropToLinkHelper {

    private final TestCaseSectionDropAdapter viewerDropAdapter;

    DropToLinkHelper(TestCaseSectionDropAdapter viewerDropAdapter) {
        this.viewerDropAdapter = viewerDropAdapter;
    }

    public boolean validateDrop(Object target, TransferData transferType) {
        IProductCmpt productCmpt = getProductCmpt(transferType);
        if (productCmpt == null) {
            return false;
        }

        if (target instanceof TestCaseTypeAssociation) {
            TestCaseTypeAssociation testCaseTypeAssociation = (TestCaseTypeAssociation)target;

            ITestPolicyCmpt parentTestPolicyCmpt = testCaseTypeAssociation.getParentTestPolicyCmpt();
            if (!testCaseTypeAssociation.isRoot()) {
                // Will be null if drag over root test policy component
                if (parentTestPolicyCmpt == null) {
                    return false;
                }

                // Will be null if model is invalid
                ITestPolicyCmptTypeParameter targetToChildParam = getTargetToChildParameter(productCmpt,
                        parentTestPolicyCmpt);
                if (targetToChildParam == null) {
                    return false;
                }
            }

            return isValidTarget(testCaseTypeAssociation.getTestPolicyCmptTypeParam(), productCmpt,
                    parentTestPolicyCmpt);

        } else if (target instanceof ITestPolicyCmpt) {
            // Will be null if model is invalid
            ITestPolicyCmptTypeParameter targetToChildParam = getTargetToChildParameter(productCmpt,
                    (ITestPolicyCmpt)target);
            if (targetToChildParam == null) {
                return false;
            }

            return isValidTarget(targetToChildParam, productCmpt, (ITestPolicyCmpt)target);
        }

        return false;
    }

    private boolean isValidTarget(ITestPolicyCmptTypeParameter targetToParameter,
            IProductCmpt productCmpt,
            ITestPolicyCmpt targetToTestPolicyCmpt) {

        IIpsSrcFile[] srcFiles = getTestCaseSection().getProductCmptSrcFiles(targetToParameter, targetToTestPolicyCmpt);
        return Arrays.asList(srcFiles).contains(productCmpt.getIpsSrcFile());
    }

    public boolean performDrop(Object data) {
        viewerDropAdapter.getCurrentEvent().detail = DND.DROP_LINK;

        class DropOnLinkRunnable implements Runnable {
            private IProductCmpt productCmpt;

            private DropOnLinkRunnable(IProductCmpt productCmpt) {
                this.productCmpt = productCmpt;
            }

            @Override
            public void run() {
                ITestPolicyCmpt newTestPolicyCmpt;
                if (getCurrentTarget() instanceof ITestPolicyCmpt) {
                    newTestPolicyCmpt = dropOnTestPolicyCmpt((ITestPolicyCmpt)getCurrentTarget());
                } else if (getCurrentTarget() instanceof TestCaseTypeAssociation) {
                    newTestPolicyCmpt = dropOnTestCaseTypeAssociation((TestCaseTypeAssociation)getCurrentTarget());
                } else {
                    throw new RuntimeException();
                }

                getTestCaseSection().refreshTreeAndDetailArea();
                getTestCaseSection().expandTreeAfterAdd(getCurrentTarget(), newTestPolicyCmpt);
                getTestCaseSection().selectInTreeByObject(newTestPolicyCmpt, true);
            }

            private ITestPolicyCmpt dropOnTestCaseTypeAssociation(TestCaseTypeAssociation testCaseTypeAssociation)
                    {

                if (testCaseTypeAssociation.isRoot()) {
                    return dropOnRootTestCaseTypeAssociation(testCaseTypeAssociation);
                } else {
                    return dropOnTestPolicyCmpt(testCaseTypeAssociation.getParentTestPolicyCmpt());
                }
            }

            private ITestPolicyCmpt dropOnRootTestCaseTypeAssociation(TestCaseTypeAssociation testCaseTypeAssociation)
                    {

                ITestPolicyCmpt rootTestPolicyCmpt = ((TestCase)getTestCaseSection().getTestCase())
                        .addRootTestPolicyCmpt((testCaseTypeAssociation).getTestPolicyCmptTypeParam());
                rootTestPolicyCmpt.setProductCmptAndNameAfterIfApplicable(productCmpt.getQualifiedName());
                rootTestPolicyCmpt.addRequiredLinks(productCmpt.getIpsProject());
                return rootTestPolicyCmpt;
            }

            private ITestPolicyCmpt dropOnTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
                ITestPolicyCmptTypeParameter targetToChildParameter = getTargetToChildParameter(productCmpt,
                        testPolicyCmpt);
                ITestPolicyCmptLink testPolicyCmptLink = testPolicyCmpt.addTestPcTypeLink(targetToChildParameter,
                        productCmpt.getQualifiedName(), null, null, true);
                return testPolicyCmptLink.findTarget();
            }

        }

        IProductCmpt productCmpt = getProductCmpt(data);
        DropOnLinkRunnable dropOnLinkRunnable = new DropOnLinkRunnable(productCmpt);
        BusyIndicator.showWhile(Display.getDefault(), dropOnLinkRunnable);

        return true;
    }

    private ITestPolicyCmptTypeParameter getTargetToChildParameter(IProductCmpt productCmpt,
            ITestPolicyCmpt testPolicyCmptTarget) {

        ITestPolicyCmptTypeParameter testTypeParam = testPolicyCmptTarget
                .findTestPolicyCmptTypeParameter(testPolicyCmptTarget.getIpsProject());
        if (testTypeParam == null) {
            return null;
        }
        IProductCmptType productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        if (productCmptType == null) {
            return null;
        }
        IPolicyCmptType policyCmptType = productCmptType.findPolicyCmptType(productCmpt.getIpsProject());
        if (policyCmptType == null) {
            return null;
        }
        ITestPolicyCmptTypeParameter targetToChildParam = null;
        for (ITestPolicyCmptTypeParameter potentialTargetToChildParam : testTypeParam
                .getTestPolicyCmptTypeParamChilds()) {
            IPolicyCmptType policyTypeOfParameter = potentialTargetToChildParam.findPolicyCmptType(productCmpt
                    .getIpsProject());
            if (policyCmptType.isSubtypeOrSameType(policyTypeOfParameter, productCmpt.getIpsProject())) {
                targetToChildParam = potentialTargetToChildParam;
                break;
            }
        }
        return targetToChildParam;
    }

    private IProductCmpt getProductCmpt(Object data) {
        if (data instanceof ISelection) {
            return getProductCmpt((ISelection)data);
        }
        if (data instanceof String[]) {
            return getProductCmpt(((String[])data)[0]);
        }
        return null;
    }

    private IProductCmpt getProductCmpt(TransferData transferData) {
        if (LocalSelectionTransfer.getTransfer().isSupportedType(transferData)) {
            ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
            return getProductCmpt(selection);
        }

        if (FileTransfer.getInstance().isSupportedType(transferData)) {
            Object transferedObject = FileTransfer.getInstance().nativeToJava(transferData);
            if (transferedObject != null) {
                String[] filename = (String[])transferedObject;
                return getProductCmpt(filename[0]);
            }
        }

        return null;
    }

    private IProductCmpt getProductCmpt(ISelection selection) {
        TypedSelection<IAdaptable> typedSelection = TypedSelection.create(IAdaptable.class, selection, 1, 1);
        if (typedSelection.isValid()) {
            return typedSelection.getFirstElement().getAdapter(IProductCmpt.class);
        } else {
            return null;
        }
    }

    private IProductCmpt getProductCmpt(String filename) {
        IFile file = NestedProjectFileUtil.getFile(filename);
        return file.getAdapter(IProductCmpt.class);
    }

    private TestCaseSection getTestCaseSection() {
        return viewerDropAdapter.testCaseSection;
    }

    private Object getCurrentTarget() {
        return viewerDropAdapter.getCurrentTarget();
    }

}