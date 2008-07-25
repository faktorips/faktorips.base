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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

public class DeepCopyOperation implements IWorkspaceRunnable{

	private IProductCmptStructureReference[] toCopy;
	private IProductCmptStructureReference[] toRefer;
	private Map handleMap;
	private IProductCmpt copiedRoot;
    private boolean createEmptyTableContents = false;
	
	/**
	 * Creates a new operation to copy the given product components.
	 * 
	 * @param toCopy All product components and table contents that should be copied.
	 * @param toRefer All product components and table contents which should be referred from the copied ones.
	 * @param handleMap All <code>IIpsSrcFiles</code> (which are all handles to non-existing resources!). Keys are the
	 * nodes given in <code>toCopy</code>.
	 */
	public DeepCopyOperation(IProductCmptStructureReference[] toCopy, IProductCmptStructureReference[] toRefer, Map handleMap) {
		this.toCopy = toCopy;
		this.toRefer = toRefer;
		this.handleMap = handleMap;
	}

	/**
     * If <code>true</code> table contents will be created as empty files, otherwise the table
     * contents will be copied.
     */
    public void setCreateEmptyTableContents(boolean createEmptyTableContents) {
        this.createEmptyTableContents = createEmptyTableContents;
    }

    /**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.DeepCopyOperation_taskTitle, 2 + toCopy.length*2 + toRefer.length);
		
		monitor.worked(1);
		
		Hashtable referMap = new Hashtable();
		for (int i = 0; i < toRefer.length; i++) {
		    String key = toRefer[i].getWrappedIpsObject().getQualifiedName();
		    List refers = (List)referMap.get(key);
		    if (refers == null){
		        refers = new ArrayList();
		    }
		    refers.add(toRefer[i]);
		    referMap.put(key, refers);
		}
		
		monitor.worked(1);

		GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
		IIpsObject[] ipsObjects = new IIpsObject[toCopy.length];
		Hashtable copied = new Hashtable();
		for (int i = 0; i < toCopy.length; i++) {
		    IIpsObject templateObject = toCopy[i].getWrappedIpsObject();
			IIpsSrcFile file = (IIpsSrcFile)handleMap.get(toCopy[i]);

			// if the file already exists, we can do nothing because the file was created already
			// caused by another reference to the same product component.
			if (!file.exists()) {
				IIpsPackageFragment targetPackage = createTargetPackage(file, monitor);
				String newName = file.getName().substring(0, file.getName().lastIndexOf('.'));
				
				boolean createEmptyFile = false;
                
				if (createEmptyTableContents && IpsObjectType.TABLE_CONTENTS.equals(templateObject.getIpsObjectType())){
				    createEmptyFile = true;
				}
				
				if (!createEmptyFile){
    				// try to create the file as copy
				    try {
    					file = targetPackage.createIpsFileFromTemplate(newName, templateObject, date, false, monitor);
    				} catch (CoreException e) {
    				    // exception occurred thus create empty file below
    				    createEmptyFile = true;
    				}
				}
				
				if (createEmptyFile){
				    // if table contents should be created empty or
				    // if the file could not be created from template then create an empty file
    				file = targetPackage.createIpsFile(templateObject.getIpsObjectType(), newName, false, monitor);
    				TimedIpsObject ipsObject = (TimedIpsObject)file.getIpsObject();
    				IIpsObjectGeneration generation = (IIpsObjectGeneration)ipsObject.newGeneration();
    				generation.setValidFrom(date);
    				setPropertiesFromTemplate(templateObject, ipsObject);
				}
				
				monitor.worked(1);
				IIpsObject ipsObject = (IIpsObject)file.getIpsObject();
				ipsObjects[i] = ipsObject;
				copied.put(ipsObject, toCopy[i]);
			}
		}

		Hashtable nameMap = new Hashtable();
		for (int i = 0; i < ipsObjects.length; i++) {
			if (ipsObjects[i] != null) {
				nameMap.put(toCopy[i].getWrappedIpsObject().getQualifiedName(), ipsObjects[i].getQualifiedName());
			}
		}

		for (int i = 0; i < ipsObjects.length; i++) {
			if (ipsObjects[i] != null) {
				fixRelations(ipsObjects[i], (IProductCmptStructureReference)copied.get(ipsObjects[i]), nameMap, referMap);
				ipsObjects[i].getIpsSrcFile().save(true, monitor);
				monitor.worked(1);
			}
		}
		copiedRoot = (IProductCmpt)ipsObjects[0];
		monitor.done();
	}
	
	private void setPropertiesFromTemplate(IIpsObject template, IIpsObject newObject) {
        if (template instanceof IProductCmpt) {
            ((IProductCmpt)newObject).setProductCmptType(((IProductCmpt)template).getProductCmptType());
        } else if (template instanceof ITableContents) {
            ((ITableContents)newObject).setTableStructure(((ITableContents)template).getTableStructure());
            ((TableContents)newObject).setNumOfColumnsInternal(((ITableContents)template).getNumOfColumns());
        }
    }
	
	private void fixRelations(IIpsObject ipsObject, IProductCmptStructureReference source, Hashtable nameMap, Hashtable referMap) {
	    if (! (ipsObject instanceof IProductCmpt)){
	        return;
	    }
	    IProductCmpt product = (IProductCmpt) ipsObject;
		IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerationsOrderedByValidDate()[0];
		IProductCmptLink[] links = generation.getLinks();
		ITableContentUsage[] tableContentUsages = generation.getTableContentUsages();
		
        // fix links
		for (int i = 0; i < links.length; i++) {
			String target = links[i].getTarget();
			List refers = (List)referMap.get(target);
			IProductCmptReference node = null;
			
			if (refers != null){
    			for (Iterator iterator = refers.iterator(); iterator.hasNext();) {
    			    IProductCmptReference currNode = (IProductCmptReference)iterator.next();
    			    if (currNode == source) {
    			        node = currNode;
    			        break;
    			    } else {
    			        currNode = currNode.getStructure().getParentProductCmptReference(currNode);
    			        if (currNode == source) {
                            node = currNode;
                            break;
    			        }
    			    }
                }
			}

			if (referMap.containsKey(target) && node != null) {
				// do nothing, the old relation has to be kept.
			} else if (nameMap.containsKey(target)) {
				links[i].setTarget((String)nameMap.get(target));
			} else if (!referMap.containsKey(target) || node == null) {
				links[i].delete();
			}
		}

          // fix table usages
	      for (int i = 0; i < tableContentUsages.length; i++) {
	            String target = tableContentUsages[i].getTableContentName();
	            List refers = (List)referMap.get(target);
	            IProductCmptStructureReference node = null;
	            
	            if (refers != null){
	                for (Iterator iterator = refers.iterator(); iterator.hasNext();) {
	                    IProductCmptStructureReference currNode = (IProductCmptStructureReference)iterator.next();
	                    currNode = currNode.getStructure().getParentProductCmptReference(currNode);
	                    if (currNode == source) {
	                        node = currNode;
	                        break;
	                    }
	                }
	            }

	            if (referMap.containsKey(target) && node != null) {
	                // do nothing, the old relation has to be kept.
	            } else if (nameMap.containsKey(target)) {
	                tableContentUsages[i].setTableContentName((String)nameMap.get(target));
	            } else if (!referMap.containsKey(target) || node == null) {
	                tableContentUsages[i].setTableContentName(""); //$NON-NLS-1$
	            }
	        }
	}
	
	/**
	 * Creates a new package, based on the target package. To this base package, the path of the source
	 * is appended, after the given number of segments to ignore is cut off.
	 */
	private IIpsPackageFragment createTargetPackage(IIpsSrcFile file, IProgressMonitor monitor) throws CoreException {
		IIpsPackageFragment result;
		IIpsPackageFragmentRoot root = file.getIpsPackageFragment().getRoot();
		String path = file.getIpsPackageFragment().getRelativePath().toString().replace('/', '.');
		result = root.createPackageFragment(path, false, monitor);
		return result;
	}	
	
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}
}
