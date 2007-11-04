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

import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;

public class DeepCopyOperation implements IWorkspaceRunnable{

	private IProductCmptReference[] toCopy;
	private IProductCmptReference[] toRefer;
	private Map handleMap;
	private IProductCmpt copiedRoot;
	
	/**
	 * Creates a new operation to copy the given product components.
	 * 
	 * @param toCopy All product components that should be copied.
	 * @param toRefer All product components which should be referred from the copied ones.
	 * @param handleMap All <code>IIpsSrcFiles</code> (which are all handles to non-existing resources!). Keys are the
	 * nodes given in <code>toCopy</code>.
	 */
	public DeepCopyOperation(IProductCmptReference[] toCopy, IProductCmptReference[] toRefer, Map handleMap) {
		this.toCopy = toCopy;
		this.toRefer = toRefer;
		this.handleMap = handleMap;
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
			referMap.put((toRefer[i].getProductCmpt()).getQualifiedName(), toRefer[i]);
		}
		
		monitor.worked(1);

		GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
		IProductCmpt[] products = new IProductCmpt[toCopy.length];
		Hashtable copied = new Hashtable();
		for (int i = 0; i < toCopy.length; i++) {
			IIpsSrcFile file = (IIpsSrcFile)handleMap.get(toCopy[i]);
			// if the file allready exists, we can do nothing because the file was created allready
			// caused by another reference to the same product component.
			if (!file.exists()) {
				IIpsPackageFragment targetPackage = createTargetPackage(file, monitor);
				String newName = file.getName().substring(0, file.getName().lastIndexOf('.'));
				try {
					file = targetPackage.createIpsFileFromTemplate(newName, toCopy[i].getProductCmpt(), date, false, monitor);
				} catch (CoreException e) {
					// the file could not be created from template, so create an empty file
					file = targetPackage.createIpsFile(IpsObjectType.PRODUCT_CMPT, newName, false, monitor);
					IProductCmpt cmpt = (IProductCmpt)file.getIpsObject();
			        IProductCmptGeneration generation = (IProductCmptGeneration)cmpt.newGeneration();
			        generation.setValidFrom(date);
				}
				monitor.worked(1);
				IProductCmpt product = (IProductCmpt)file.getIpsObject();
				products[i] = product;
				copied.put(product, toCopy[i]);
			}
		}

		Hashtable nameMap = new Hashtable();
		for (int i = 0; i < products.length; i++) {
			if (products[i] != null) {
				nameMap.put(toCopy[i].getProductCmpt().getQualifiedName(), products[i].getQualifiedName());
			}
		}

		for (int i = 0; i < products.length; i++) {
			if (products[i] != null) {
				fixRelations(products[i], (IProductCmptReference)copied.get(products[i]), nameMap, referMap);
				products[i].getIpsSrcFile().save(true, monitor);
				monitor.worked(1);
			}
		}
		copiedRoot = products[0];
		monitor.done();
	}
	
	private void fixRelations(IProductCmpt product, IProductCmptReference source, Hashtable nameMap, Hashtable referMap) {
		IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerations()[0];
		IProductCmptLink[] relations = generation.getLinks();
		
		for (int i = 0; i < relations.length; i++) {
			String target = relations[i].getTarget();
			IProductCmptReference node = (IProductCmptReference)referMap.get(target);
			while (node != null && node != source) {
				node = node.getStructure().getParentProductCmptReference(node);
			}

			if (referMap.containsKey(target) && node != null) {
				// do nothing, the old relation has to be kept.
			} else if (nameMap.containsKey(target)) {
				relations[i].setTarget((String)nameMap.get(target));
			} else if (!referMap.containsKey(target) || node == null) {
				relations[i].delete();
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
