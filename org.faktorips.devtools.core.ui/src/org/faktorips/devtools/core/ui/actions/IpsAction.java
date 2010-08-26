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

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccess;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccessWithListenerSupport;
import org.faktorips.devtools.core.ui.IDataChangeableStateChangeListener;
import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;
import org.faktorips.util.StringUtil;

/**
 * Abstract base action for global actions.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public abstract class IpsAction extends Action {

    private static final String ARCHIVE_LINK = "ARCHIVE_LINK"; //$NON-NLS-1$

    private final static IIpsObject[] EMPTY_IPS_OBJECT_ARRAY = new IIpsObject[0];

    /** The source of objects to modify by this action. */
    protected ISelectionProvider selectionProvider;

    private ISelection selection = null;

    private IDataChangeableReadAccess ctrl;

    private ISelectionChangedListener adjustEnableStateListener;

    /**
     * Creates a new action. If the action is started, the given selection-provider is asked for its
     * selection and the modifications are done to the selection.
     */
    public IpsAction(ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
        if (selectionProvider != null) {
            adjustEnableStateListener = new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    updateEnabledProperty();
                }
            };
            selectionProvider.addSelectionChangedListener(adjustEnableStateListener);
        }
    }

    @Override
    public void run() {
        ISelection sel = selectionProvider.getSelection();
        if (sel != null) {
            if (sel instanceof IStructuredSelection) {
                run(new StructuredSelection(mapIpsSrcFilesToIpsObjects((IStructuredSelection)sel)));
            } else {
                throw new RuntimeException(Messages.IpsAction_msgUnsupportedSelection + selection.getClass().getName());
            }
        }
    }

    /**
     * Returns a list of selected objects, map all selected ips source files to the corresponding
     * ips object.
     */
    private List<Object> mapIpsSrcFilesToIpsObjects(IStructuredSelection selection) {
        List<Object> selectedIpsObjects = new ArrayList<Object>((selection).size());
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object select = iter.next();
            if (select instanceof IIpsSrcFile) {
                try {
                    selectedIpsObjects.add(((IIpsSrcFile)select).getIpsObject());
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            } else {
                selectedIpsObjects.add(select);
            }
        }

        return selectedIpsObjects;
    }

    abstract public void run(IStructuredSelection selection);

    /**
     * This method returns all <code>IIpsObject</code>s found in the given selection. Returns an
     * empty array if the selection is empty or does not contain <code>IpsObject</code>s.
     */
    protected IIpsObject[] getIpsObjectsForSelection(IStructuredSelection selection) {
        List<IIpsObject> ipsObjects = new ArrayList<IIpsObject>();
        for (Iterator<Object> i = getSelectionIterator(selection); i.hasNext();) {
            ipsObjects.add(getIpsObjectForSelection(i.next()));
        }

        return ipsObjects.toArray(new IIpsObject[ipsObjects.size()]);
    }

    /**
     * Returns all <code>IIpsSrcFile</code>s found in the given selection. Returns an empty array if
     * the selection is empty or does not contain <code>IIpsSrcFiles</code>s.
     */
    protected IIpsSrcFile[] getIpsSrcFilesForSelection(IStructuredSelection selection) {
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        for (Iterator<?> i = getSelectionIterator(selection); i.hasNext();) {
            IIpsSrcFile ipsSrcFile = getIpsSrcFileForSelection(i.next());
            if (ipsSrcFile != null) {
                ipsSrcFiles.add(ipsSrcFile);
            }
        }
        return ipsSrcFiles.toArray(new IIpsSrcFile[ipsSrcFiles.size()]);
    }

    /**
     * This method returns an <code>IIpsObject</code> for the first element of the given
     * <code>StructuredSelection</code>. Returns <code>null</code> if the selection is empty or does
     * not contain the expected types.
     * 
     * @see IpsAction#getIpsObjectForSelection(Object)
     */
    protected IIpsObject getIpsObjectForSelection(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        return getIpsObjectForSelection(selected);
    }

    /**
     * This method returns an <code>IIpsObject</code> referenced by or contained in the given
     * Object.
     * <p>
     * If the given Object is of type <code>IIpsObject</code> the object itself is returned. If it
     * is an <code>IIpsObjectPart</code> the corresponding IIpsObject is returned. If the given
     * object is an array, the first element of this array is returned. This object is a
     * <code>PolicyCmptType</code>, <code>ProductCmpt</code> or <code>TestCase</code> by convention,
     * <code>null</code> is returned if this convention is violated.
     * <p>
     * If the given object is an <code>IProductCmptReference</code> the contained
     * <code>IProductCmpt</code>, if it is an <code>IProductCmptTypeAssociationReference</code> the
     * contained <code>IRelation</code>'s <code>IIpsObject</code> is returned.
     * <p>
     * If the given object is an <code>IProductCmptRelation</code> the action searches for the
     * referenced <code>IProductCmpt</code> and returns it if existent, null otherwise.
     * <p>
     * Returns <code>null</code> if the object is not an instance of the expected types.
     */
    private IIpsObject getIpsObjectForSelection(Object selected) {
        if (selected == null) {
            return null;
        }
        Object selectedObject;
        if (selected instanceof Object[]) {
            selectedObject = ((Object[])selected)[0];
        } else {
            selectedObject = selected;
        }
        if (selectedObject instanceof IIpsObject) {
            return (IIpsObject)selectedObject;
        }
        if (selectedObject instanceof IIpsSrcFile) {
            try {
                return ((IIpsSrcFile)selectedObject).getIpsObject();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        if (selectedObject instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)selectedObject).getIpsObject();
        }
        if (selectedObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)selectedObject;
            IIpsSrcFile adaptedSrcFile = (IIpsSrcFile)adaptable.getAdapter(IIpsSrcFile.class);
            if (adaptedSrcFile != null) {
                try {
                    return adaptedSrcFile.getIpsObject();
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }
        return null;
    }

    /**
     * Returns the <code>IIpsSrcFile</code> for the given Object. If the given object is an
     * <code>IIpsSrcFile</code> it is returned. If the given object is an <code>IIpsObject</code>,
     * the corresponding <code>IIpsSrcFile</code> is returned.
     * <p>
     * Returns <code>null</code> if the selection is empty or does not contain the expected types.
     * 
     * @see IpsAction#getIpsObjectForSelection(IStructuredSelection)
     */
    private IIpsSrcFile getIpsSrcFileForSelection(Object selected) {
        if (selected instanceof IIpsSrcFile) {
            // avoid reading IpsSrcFile in getIpsObjectForSelection()
            return (IIpsSrcFile)selected;
        } else if (selected instanceof IpsSrcFileViewItem) {
            return ((IpsSrcFileViewItem)selected).getIpsSrcFile();
        }

        IIpsObject ipsObject = getIpsObjectForSelection(selected);
        if (ipsObject != null) {
            return ipsObject.getIpsSrcFile();
        }

        return null;
    }

    /**
     * This method returns the <code>IIpsSrcFile</code> for the given selection. The first element
     * of the <code>StructuredSelection</code> is processed. If an <code>IIpsSrcFile</code> is found
     * in the selection it is returned. If an <code>IIpsObject</code> is found in the selection, the
     * corresponding <code>IIpsSrcFile</code> is returned.
     * <p>
     * Returns <code>null</code> if the selection is empty or does not contain the expected types.
     * 
     * @see IpsAction#getIpsObjectForSelection(IStructuredSelection)
     */
    protected IIpsSrcFile getIpsSrcFileForSelection(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        return getIpsSrcFileForSelection(selected);
    }

    /**
     * Returns the apropriate Transfer for every item in the given lists in the same order as the
     * data is returend in getDataArray.
     */
    protected Transfer[] getTypeArray(List<String> stringItems,
            List<IResource> resourceItems,
            List<String> copiedResourceLinks) {

        List<Transfer> resultList = new ArrayList<Transfer>();
        if (resourceItems.size() > 0) {
            resultList.add(ResourceTransfer.getInstance());
        }
        for (int i = 0; i < stringItems.size(); i++) {
            resultList.add(TextTransfer.getInstance());
        }
        if (copiedResourceLinks != null && copiedResourceLinks.size() > 0) {
            // the links will be merged to one text inside the clipboard
            resultList.add(TextTransfer.getInstance());
        }
        Transfer[] result = new Transfer[resultList.size()];
        return resultList.toArray(result);
    }

    /**
     * Builds the data-array for clipboard operations (copy, drag,...).
     * 
     * @param stringItems The list of strings to put to the clipboard
     * @param resourceItems The list of resources to put to the clipboard
     * @param copiedResourceLinks The list of resource links to put in the clipboard
     */
    protected Object[] getDataArray(List<String> stringItems,
            List<IResource> resourceItems,
            List<String> copiedResourceLinks) {
        List<Object> result = new ArrayList<Object>();
        // add copied resources
        if (resourceItems.size() > 0) {
            IResource[] res = new IResource[resourceItems.size()];
            result.add(resourceItems.toArray(res));
        }
        // add copied text
        result.addAll(stringItems);
        // add copied resource links (e.g. links to an object inside an archiv)
        // all links will be merged to one text inside the clipboard
        if (copiedResourceLinks != null) {
            String strReferences = ""; //$NON-NLS-1$
            for (String element : copiedResourceLinks) {
                strReferences += strReferences.length() > 0 ? "," + element : element; //$NON-NLS-1$
            }
            if (StringUtils.isNotEmpty(strReferences)) {
                result.add(ARCHIVE_LINK + strReferences);
            }
        }
        return result.toArray();
    }

    public String getResourceLinkInArchive(IIpsObject ipsObject) {
        IIpsPackageFragmentRoot root = ipsObject.getIpsPackageFragment().getRoot();
        String srcFileName = ipsObject.getIpsSrcFile().getName();
        String content = root.getIpsProject().getName() + "#" + root.getName() + "#" + ipsObject.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "#" + StringUtil.getFileExtension(srcFileName); //$NON-NLS-1$
        return content;
    }

    public String getResourceLinkInArchive(IIpsPackageFragment fragment) {
        IIpsPackageFragmentRoot root = fragment.getRoot();
        String content = root.getIpsProject().getName() + "#" + root.getName() + "#" + fragment.getName(); //$NON-NLS-1$ //$NON-NLS-2$
        return content;
    }

    /**
     * Returns all objects which are represented by links inside the clipboard. If there are no
     * resource links inside the clipboard an empty array will ne returned. If the linked object
     * wasn't found then the object will be ignored (not returned).
     */
    public Object[] getObjectsFromResourceLinks(String resourceLinks) {
        if (resourceLinks == null || !resourceLinks.startsWith(ARCHIVE_LINK)) {
            // no resource links
            return EMPTY_IPS_OBJECT_ARRAY;
        }
        resourceLinks = resourceLinks.substring(ARCHIVE_LINK.length(), resourceLinks.length());

        StringTokenizer tokenizer = new StringTokenizer(resourceLinks, ","); //$NON-NLS-1$
        int count = tokenizer.countTokens();
        List<Object> result = new ArrayList<Object>(1);
        List<String> links = new ArrayList<String>(count);

        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            links.add(tokenizer.nextToken());
        }

        for (String resourceLink : links) {
            String[] copiedResource = StringUtils.split(resourceLink, "#"); //$NON-NLS-1$
            // 1. find the project
            IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(copiedResource[0]);
            try {
                // 2. find the root
                IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
                IIpsPackageFragmentRoot archive = null;
                for (IIpsPackageFragmentRoot root : roots) {
                    if (root.getName().equals(copiedResource[1])) {
                        archive = root;
                        break;
                    }
                }
                if (archive == null) {
                    continue;
                }
                // 3. find the object or package
                if (copiedResource.length >= 4) {
                    // the link represents an object (object [3] contains the type of the object)
                    // try to find the object
                    IIpsObject ipsObject = archive.findIpsObject(IpsObjectType.getTypeForExtension(copiedResource[3]),
                            copiedResource[2]);
                    if (ipsObject != null) {
                        result.add(ipsObject);
                    }
                } else {
                    // the link represents a package fragment
                    // try to obtain the package fragment
                    IIpsPackageFragment packageFrgmt = archive.getIpsPackageFragment(copiedResource[2]);
                    if (packageFrgmt != null) {
                        result.add(packageFrgmt);
                    }
                }
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
        return result.toArray();
    }

    public void setControlWithDataChangeableSupport(IDataChangeableReadAccessWithListenerSupport ctrl) {
        this.ctrl = ctrl;
        ctrl.addDataChangeableStateChangeListener(new IDataChangeableStateChangeListener() {

            @Override
            public void dataChangeableStateHasChanged(IDataChangeableReadAccess object) {
                updateEnabledProperty();
            }

        });
    }

    /**
     * Computes and sets the new value for the enabled property.
     */
    public void updateEnabledProperty() {
        setEnabled(computeEnabledProperty());
    }

    /**
     * Returns <code>true</code> if the action should be enabled, otherwise <code>false</code>.
     * Default implementation first checks if the enabled state depends on a control with switch
     * data changeable support. If this is not the case or the data is changeable, then the property
     * is computed based on the current selection (if a selection provider is available).
     */
    protected boolean computeEnabledProperty() {
        if (ctrl != null) {
            if (!ctrl.isDataChangeable()) {
                return false;
            }
        }
        if (selectionProvider != null) {
            if (selectionProvider.getSelection() instanceof IStructuredSelection) {
                return computeEnabledProperty((IStructuredSelection)selectionProvider.getSelection());
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the action is enabled based on the given selection, otherwise
     * <code>false</code>. The default implementation always returns <code>true</code>.
     * 
     * @param selection The user selection to check for enabled state of this action.
     */
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        return true;
    }

    public void dispose() {
        if (adjustEnableStateListener != null) {
            selectionProvider.removeSelectionChangedListener(adjustEnableStateListener);
        }
    }

    /**
     * Extracted as protected method to get along with one suppress warnings annotation.
     */
    @SuppressWarnings("unchecked")
    // Eclipse API uses unchecked iterator
    protected Iterator<Object> getSelectionIterator(IStructuredSelection selection) {
        return selection.iterator();
    }

}
