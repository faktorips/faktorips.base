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

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenIpsObjectAction extends Action implements IWorkbenchWindowActionDelegate {

    private String perspective;
    private IIpsSrcFile[] ipsSrcFiles;

    public OpenIpsObjectAction() {
        super();
        setText(Messages.OpenIpsObjectAction_titleText);
        setDescription(Messages.OpenIpsObjectAction_description);
        setToolTipText(Messages.OpenIpsObjectAction_tooltip);
        setAccelerator(SWT.CTRL | SWT.SHIFT | 'I');
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("OpenIpsObject.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        Shell parent = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        String dialogTitle = Messages.OpenIpsObjectAction_dialogTitle;
        String dialogMessage = Messages.OpenIpsObjectAction_dialogMessage;
        perspective = getCurrentPerspective().getId();
        try {
            BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
                public void run() {
                    ipsSrcFiles = getIpsSrcFiles();
                }
            });
            OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(parent, dialogTitle, dialogMessage);
            dialog.setElements(ipsSrcFiles);
            dialog.setTypes(getIpsObjectTypes());
            dialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (dialog.open() == Window.OK) {
                IIpsElement object = dialog.getSelectedObject();
                if (object != null) {
                    IIpsObject ipsObject = null;
                    if (object instanceof IIpsSrcFile) {
                        ipsObject = ((IIpsSrcFile)object).getIpsObject();
                    } else if (object instanceof IIpsObject) {
                        ipsObject = (IIpsObject)object;
                    }
                    IpsUIPlugin.getDefault().openEditor(ipsObject);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

    }

    /*
     * @return all relevant IIpsObjects for the current perspective.
     */
    private IIpsSrcFile[] getIpsSrcFiles() {
        ipsSrcFiles = new IIpsSrcFile[0];
        if (perspective.equals("org.eclipse.team.ui.TeamSynchronizingPerspective")) { //$NON-NLS-1$
            return ipsSrcFiles;
        }
        try {
            ipsSrcFiles = getAllIpsSrcFiles();
        } catch (CoreException e) {
            // if we fail to get the objects, we won't be able to open them either.
        }
        if (perspective.equals(IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID)) {
            List<IIpsSrcFile> list = new ArrayList<IIpsSrcFile>();
            for (int i = 0; i < ipsSrcFiles.length; i++) {
                IIpsSrcFile object = ipsSrcFiles[i];
                if (object.getIpsObjectType().isProductDefinitionType()) {
                    list.add(object);
                }
            }
            ipsSrcFiles = list.toArray(new IIpsSrcFile[list.size()]);
        }
        return ipsSrcFiles;
    }

    public IPerspectiveDescriptor getCurrentPerspective() {
        return IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    }

    public IpsObjectType[] getIpsObjectTypes() {
        SortedMap<String, IpsObjectType> map = new TreeMap<String, IpsObjectType>();
        for (int i = 0; i < ipsSrcFiles.length; i++) {
            IIpsSrcFile srcFile = ipsSrcFiles[i];
            IpsObjectType type = srcFile.getIpsObjectType();
            map.put(type.getId(), type);
        }
        return map.values().toArray(new IpsObjectType[map.size()]);
    }

    /*
     * Calls <code>IIpsProject#findAllIpsSrcFiles()</code> on all projects in the workspace and
     * returns the collective list of <code>IIpsSrcFile</code>s.
     * 
     * @throws CoreException if getting objects from a <code>IIpsProject</code> fails.
     */
    private IIpsSrcFile[] getAllIpsSrcFiles() throws CoreException {
        List<IIpsSrcFile> list = new ArrayList<IIpsSrcFile>();
        IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
        for (int i = 0; i < projects.length; i++) {
            IIpsProject project = projects[i];
            project.findAllIpsSrcFiles(list);
        }
        Set<IIpsSrcFile> set = new HashSet<IIpsSrcFile>();
        for (Iterator<IIpsSrcFile> iter = list.iterator(); iter.hasNext();) {
            IIpsSrcFile object = iter.next();
            set.add(object);
        }
        return set.toArray(new IIpsSrcFile[set.size()]);
    }

    // ---- IWorkbenchWindowActionDelegate ------------------------------------------------

    public void run(IAction action) {
        run();
    }

    public void dispose() {
        // do nothing.
    }

    public void init(IWorkbenchWindow window) {
        // do nothing.
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }
}
