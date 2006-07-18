package org.faktorips.devtools.core.ui.search;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * 
 * 
 * @author Stefan Widmaier
 */
public class SearchResultContentProviderTest extends AbstractIpsPluginTest {
	
	private IIpsProject proj;
	private IIpsPackageFragmentRoot root;
	private PolicyCmptType pcType;
    private IPolicyCmptType pcType2;
	private IProductCmpt prodCmpt;
	private IProductCmptGeneration generation;
	private SearchResultContentProvider provider;
    private Object[] parentElement2;
    private Object[] parentElement;

	protected void setUp() throws Exception {
		super.setUp();
		provider = new SearchResultContentProvider();
		proj = newIpsProject("TestProjekt");
		root = (IpsPackageFragmentRoot) proj.getIpsPackageFragmentRoots()[0];
        pcType = newPolicyCmptType(root, "TestPCType");
        pcType2 = newPolicyCmptType(root, "TestPCType2");
        pcType2.newAttribute();
        pcType2.newAttribute();
        pcType2.newAttribute();
		prodCmpt = newProductCmpt(root, "TestProdType");
		generation = (IProductCmptGeneration) prodCmpt.newGeneration();
        // Setup Arrays with element and children
        IIpsElement[] pcChildren= pcType.getChildren();
        parentElement = new Object[pcChildren.length+1];
        System.arraycopy(pcChildren, 0, parentElement, 1, pcChildren.length);
        IIpsElement[] pcChildren2= pcType2.getChildren();
        parentElement2 = new Object[pcChildren2.length+1];
        System.arraycopy(pcChildren2, 0, parentElement2, 1, pcChildren2.length);
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.ui.search.SearchResultContentProvider.getChildren(Object)'
	 */
	public void testGetChildren() throws CoreException {
		// The first element in the Object[] is the search result, the others are its children.
		
		IIpsElement[] children= (IIpsElement[]) provider.getChildren(parentElement);
        assertEquals(0, children.length);
		assertTrue(Arrays.equals(pcType.getChildren(), children));

        children= (IIpsElement[]) provider.getChildren(parentElement2);
        assertEquals(3, children.length);
        assertTrue(Arrays.equals(pcType2.getChildren(), children));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.ui.search.SearchResultContentProvider.getParent(Object)'
	 */
	public void testGetParent() {
		Object parent= provider.getParent(generation);
        assertEquals(generation.getProductCmpt(), parent);
		parent= provider.getParent(pcType);
		assertEquals(pcType.getParent(), parent);
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.ui.search.SearchResultContentProvider.hasChildren(Object)'
	 */
	public void testHasChildren() throws CoreException {
        assertFalse(provider.hasChildren(parentElement));
	    assertTrue(provider.hasChildren(parentElement2));
	}

}
