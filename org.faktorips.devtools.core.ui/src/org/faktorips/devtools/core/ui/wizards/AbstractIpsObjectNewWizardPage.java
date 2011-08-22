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

package org.faktorips.devtools.core.ui.wizards;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;

/**
 * This is the base class for pages used with the {@link NewIpsObjectWizard}. The wizard uses the
 * interface of this class as follows:
 * <ul>
 * <li>The wizard requests the IPS package fragment through the {@link #getIpsPackageFragment()}
 * method. The wizard delegates the selection to this page to set a default for the package fragment
 * by means of the {@link #setIpsPackageFragment(IIpsPackageFragment)} method.</li>
 * <li>Same for the IPS package fragment root</li>
 * <li>The wizard asks the page if it can provide the {@link IIpsSrcFile} by means of the
 * {@link #canCreateIpsSrcFile()} method and if <code>true</code> calls the
 * {@link #createIpsSrcFile(IProgressMonitor)} to create it.</li>
 * <li>The wizard calls the {@link #getIpsObjectName()} and {@link #getIpsObjectType()} to display
 * these in the title area.</li>
 * <li>The wizard calls the {@link #finishIpsObjects(IIpsObject, Set)} after the creation of the
 * {@link IIpsSrcFile} where it expects the {@link IIpsObject} to be set into a valid state that can
 * be used for further editing.</li>
 * <li>The wizard calls {@link #pageEntered()} when this page is entered. By default this method
 * calls {@link #setDefaultFocus()} to give implementations the chance to set the focus on the
 * preferred widget when the page is entered.</li>
 * </ul>
 * <p>
 * Subclasses need to implement the {@link #createControlInternal(Composite)} to provide the user
 * interface of this page.
 * 
 * @author Peter Kuntz
 */
public abstract class AbstractIpsObjectNewWizardPage extends WizardPage {

    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    protected AbstractIpsObjectNewWizardPage(IStructuredSelection selection, String pageName) {
        super(pageName);
        selectedResource = getSelectedResourceFromSelection(selection);
    }

    protected IResource getSelectedResourceFromSelection(IStructuredSelection selection) {
        if (selection == null) {
            return null;
        }
        try {
            Object element = selection.getFirstElement();
            if (element instanceof IResource) {
                return (IResource)element;
            } else if (element instanceof IJavaElement) {
                return ((IJavaElement)element).getCorrespondingResource();
            } else if (element instanceof IIpsElement) {
                return ((IIpsElement)element).getEnclosingResource();
            } else if (element instanceof IProductCmptReference) {
                return ((IProductCmptReference)element).getProductCmpt().getEnclosingResource();
            }
        } catch (JavaModelException e) {
            /*
             * If we can't get the selected resource, we can't put default in the controls but no
             * need to bother the user with this, so we just log the exception.
             */
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Derives the default values for source folder and package from the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) throws CoreException {
        setDefaultsExtension(selectedResource);
        if (selectedResource == null) {
            setIpsPackageFragmentRoot(null);
            return;
        }

        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots;
            roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
            if (roots.length > 0) {
                setIpsPackageFragmentRoot(roots[0]);
            }
        } else if (element instanceof IIpsPackageFragmentRoot) {
            setIpsPackageFragmentRoot((IIpsPackageFragmentRoot)element);
        } else if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element;
            setIpsPackageFragment(pack);
            setIpsPackageFragmentRoot(pack.getRoot());
        } else if (element instanceof IIpsSrcFile) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element.getParent();
            setIpsPackageFragment(pack);
            setIpsPackageFragmentRoot(pack.getRoot());
        } else {
            setIpsPackageFragmentRoot(null);
        }
    }

    /**
     * 
     * Subclasses may override to set default values based upon the user selection.
     * 
     * @param selectedResource the selected resource
     * @throws CoreException in any case of exception
     * */
    protected void setDefaultsExtension(IResource selectedResource) throws CoreException {
        // subclasses may override
    }

    /**
     * Returns the ips object that is stored in the resource that was selected when the wizard was
     * opened or <code>null</code> if none is selected.
     * 
     * @throws CoreException if the contents of the resource can't be parsed.
     */
    public IIpsObject getSelectedIpsObject() throws CoreException {
        if (selectedResource == null) {
            return null;
        }

        IIpsElement el = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (el instanceof IIpsSrcFile) {
            return ((IIpsSrcFile)el).getIpsObject();
        }

        return null;
    }

    @Override
    public final void createControl(Composite parent) {
        Control control = createControlInternal(parent);
        try {
            setDefaults(selectedResource);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        setControl(control);
    }

    /**
     * Returns <code>false</code> by default. Subclasses can override this method to indicate that
     * the Wizard can finish without stepping to the next page. It only makes sense to use this
     * method if this page is added to a NewIpsObjectWizard.
     */
    protected boolean finishWhenThisPageIsComplete() {
        return false;
    }

    /**
     * This method is called when the page is entered. By default it calls the setDefaultFocus()
     * method.
     * 
     * @throws CoreException in any case of exception
     */
    protected void pageEntered() throws CoreException {
        setDefaultFocus();
    }

    /**
     * Can be used by subclasses to update the completion state of this page after validation.
     */
    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        setPageComplete(true);
    }

    /**
     * The wizard asks if it is possible to create the {@link IIpsSrcFile} in the current state.
     * Returns <code>true</code> by default.
     */
    protected boolean canCreateIpsSrcFile() {
        return true;
    }

    /**
     * Implementation can set the default focus by this method. This method is called when the page
     * is entered by the wizard.
     */
    protected abstract void setDefaultFocus();

    /**
     * Implementations need to create a new {@link IIpsSrcFile} by means of this method. The method
     * should always be able to return a valid {@link IIpsSrcFile} when called, if it returns
     * <code>null</code> it is considered to be an error of the implementation an will be logged as
     * such.
     * 
     * @param monitor the wizards progress monitor
     * @return the new {@link IIpsSrcFile}
     * @throws CoreException when an exception ocurrs during creation
     */
    protected abstract IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException;

    /**
     * Implementations must provide an {@link IIpsPackageFragment} for the wizard by this method.
     */
    protected abstract IIpsPackageFragment getIpsPackageFragment();

    /**
     * The wizard calls this method after the {@link IIpsSrcFile} is created to give the page the
     * chance to state the {@link IIpsObject} in a valid state before leaving the wizard.
     * <p>
     * This implementation creates an {@link IDescription} and an {@link ILabel} for every language
     * the IPS project supports if the IPS object in question has description or label support
     * respectively.
     * 
     * @param newIpsObject the {@link IIpsObject} to that is created by this page
     * @param modifiedIpsObjects {@link IIpsObject}s that have being modified during the finishing
     *            of the new {@link IIpsObject} need to be registered in this set so that the wizard
     *            can also save the changed state of these objects.
     * 
     * @throws CoreException if an exception occurs during finishing
     */
    public final void finishIpsObjects(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        finishIpsObjectsExtension(newIpsObject, modifiedIpsObjects);
    }

    /**
     * This method may be overridden by subclasses to extend the method
     * {@link #finishIpsObjects(IIpsObject, Set)}.
     * 
     * @param newIpsObject the {@link IIpsObject} to that is created by this page
     * @param modifiedIpsObjects {@link IIpsObject}s that have being modified during the finishing
     *            of the new {@link IIpsObject} need to be registered in this set so that the wizard
     *            can also save the changed state of these objects.
     * 
     * @throws CoreException if an exception occurs during finishing
     */
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        // Empty default implementation, subclasses may override
    }

    /**
     * The wizard asks the unqualifed name of the created {@link IIpsObject}
     */
    protected abstract String getIpsObjectName();

    /**
     * The wizard asks the type of the created {@link IIpsObject}
     */
    protected abstract IpsObjectType getIpsObjectType();

    /**
     * Called when the selection provided by the wizard suggests an {@link IIpsPackageFragmentRoot}.
     */
    protected abstract void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot root);

    /**
     * Called when the selection provided by the wizard suggests an {@link IIpsPackageFragment}.
     */
    protected abstract void setIpsPackageFragment(IIpsPackageFragment pack);

    /**
     * Subclasses need to provide the user interface by means of this method.
     */
    protected abstract Control createControlInternal(Composite parent);
}
