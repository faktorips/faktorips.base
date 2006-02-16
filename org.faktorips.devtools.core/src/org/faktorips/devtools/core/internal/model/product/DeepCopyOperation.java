package org.faktorips.devtools.core.internal.model.product;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

public class DeepCopyOperation implements IRunnableWithProgress {

	private IProductCmpt[] toCopy;
	private IProductCmpt[] toRefer;
	private Hashtable handleMap;
	private IProductCmpt copiedRoot;
	
	/**
	 * Creates a new operation to copy the given product components.
	 * 
	 * @param toCopy All product components that should be copied.
	 * @param toRefer All product components which should be referred from the copied ones.
	 * @param handleMap All <code>IIpsSrcFiles</code> (which are all handles to non-existing resources!). Keys are the
	 * product components given in <code>toCopy</code>.
	 */
	public DeepCopyOperation(IProductCmpt[] toCopy, IProductCmpt[] toRefer, Hashtable handleMap) {
		this.toCopy = toCopy;
		this.toRefer = toRefer;
		this.handleMap = handleMap;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Deep Copy", 2 + toCopy.length*2 + toRefer.length);
		
		monitor.worked(1);
		
		Hashtable referMap = new Hashtable();
		for (int i = 0; i < toRefer.length; i++) {
			referMap.put(toRefer[i].getQualifiedName(), toRefer[i].getName());
		}
		
		monitor.worked(1);

		GregorianCalendar date = IpsPreferences.getWorkingDate();
		IProductCmpt[] products = new IProductCmpt[toCopy.length];
		for (int i = 0; i < toCopy.length; i++) {
			try {
				IIpsSrcFile file = (IIpsSrcFile)handleMap.get(toCopy[i]);
				IIpsPackageFragment targetPackage = createTargetPackage(file, monitor);
				String newName = file.getName().substring(0, file.getName().lastIndexOf('.'));
				file = targetPackage.createIpsFileFromTemplate(newName, toCopy[i], date, false, monitor);
				monitor.worked(1);
				IProductCmpt product = (IProductCmpt)file.getIpsObject();
				products[i] = product;
			} catch (CoreException e) {
				IpsPlugin.logAndShowErrorDialog(e);
				return;
			}
		}

		Hashtable nameMap = new Hashtable();
		for (int i = 0; i < products.length; i++) {
			nameMap.put(toCopy[i].getQualifiedName(), products[i].getQualifiedName());
		}

		for (int i = 0; i < products.length; i++) {
			fixRelations(products[i], nameMap, referMap);
			try {
				products[i].getIpsSrcFile().save(true, monitor);
			} catch (CoreException e) {
				IpsPlugin.logAndShowErrorDialog(e);
				return;
			}
			monitor.worked(1);
		}
		copiedRoot = products[0];
		monitor.done();
	}
	
	private void fixRelations(IProductCmpt product, Hashtable nameMap, Hashtable referMap) {
		IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerations()[0];
		IProductCmptRelation[] relations = generation.getRelations();
		
		for (int i = 0; i < relations.length; i++) {
			String target = relations[i].getTarget();
			if (nameMap.containsKey(target)) {
				relations[i].setTarget((String)nameMap.get(target));
			}
			else if (!referMap.containsKey(target)) {
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
