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

package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

public class ReferenceSearchResult extends AbstractTextSearchResult {

    private ReferenceSearchQuery query;
    ReferenceSearchResultPage page;

    // only in Eclipse 3.3
    // private MatchFilter testCaseMatchFilter = new TestCaseMatchFilter();
    // private MatchFilter productCmptMatchFilter = new ProductCmptMatchFilter();
    //
    // protected class TestCaseMatchFilter extends MatchFilter {
    // private static final String ID = "TestCaseFilter";
    //
    // /**
    // * {@inheritDoc}
    // */
    // public boolean filters(Match match) {
    // boolean filter =
    // IpsObjectType.TEST_CASE.equals(getCorrespondingIpsSrcFile(match.getElement()).getIpsObjectType());
    // return filter;
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public String getActionLabel() {
    // return "Filter Testfälle";
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public String getDescription() {
    // return "Filter Testfälle";
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public String getID() {
    // return ID;
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public String getName() {
    // return "Testfall-Filter";
    // }
    // }
    //    
    // protected class ProductCmptMatchFilter extends MatchFilter {
    // private static final String ID = "ProductCmptFilter";
    // /**
    // * {@inheritDoc}
    // */
    // public boolean filters(Match match) {
    // boolean filter =
    // IpsObjectType.PRODUCT_CMPT.equals(getCorrespondingIpsSrcFile(match.getElement()).getIpsObjectType());
    // return filter;
    // }
    //        
    // /**
    // * {@inheritDoc}
    // */
    // public String getActionLabel() {
    // return "Filter Produktbausteine";
    // }
    //        
    // /**
    // * {@inheritDoc}
    // */
    // public String getDescription() {
    // return "Filter Produktbausteine";
    // }
    //        
    // /**
    // * {@inheritDoc}
    // */
    // public String getID() {
    // return ID;
    // }
    //        
    // /**
    // * {@inheritDoc}
    // */
    // public String getName() {
    // return "Produktbaustein-Filter";
    // }
    // }
    // private IIpsSrcFile getCorrespondingIpsSrcFile(Object selection) {
    // if (selection instanceof Object[]) {
    // selection= ((Object[])selection)[0];
    // }
    // if(selection instanceof IIpsObjectPart){
    // return ((IIpsObjectPart)selection).getIpsObject().getIpsSrcFile();
    // }
    // if(selection instanceof IIpsObject){
    // return ((IIpsObject)selection).getIpsSrcFile();
    // }
    // return null;
    // }

    public ReferenceSearchResult(ReferenceSearchQuery query) {
        this.query = query;
    }

    @Override
    public String getLabel() {
        return "" + super.getMatchCount() + Messages.ReferenceSearchResult_label + this.query.getReferencedName(); //$NON-NLS-1$
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public ISearchQuery getQuery() {
        return this.query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return null;
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }

    public void setPage(ReferenceSearchResultPage page) {
        this.page = page;
    }

    // Eclipse 3.3
    // /**
    // * {@inheritDoc}
    // */
    // public MatchFilter[] getAllMatchFilters() {
    // return new MatchFilter[]{testCaseMatchFilter, productCmptMatchFilter};
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public void setActiveMatchFilters(MatchFilter[] filters) {
    // super.setActiveMatchFilters(filters);
    // boolean filterTestCase = false;
    // boolean filterProductCmpt = false;
    // for (int i = 0; i < filters.length; i++) {
    // if (filters[i] instanceof TestCaseMatchFilter) {
    // filterTestCase = true;
    // } else if (filters[i] instanceof ProductCmptMatchFilter) {
    // filterProductCmpt = true;
    // }
    // }
    //        
    // page.setActiveMatchFilter(filterTestCase, filterProductCmpt);
    // }

    public void setActiveMatchedFilterFor(boolean testCaseMatchFilter, boolean productCmptMatchFilter) {
        // Eclipse 3.3
        // List matchedFilterList = new ArrayList(2);
        // if (testCase){
        // matchedFilterList.add(testCaseMatchFilter);
        // }
        // if (productCmpt){
        // matchedFilterList.add(productCmptMatchFilter);
        // }
        // setActiveMatchFilters((MatchFilter[]) matchedFilterList.toArray(new
        // MatchFilter[matchedFilterList.size()]));
    }
}
