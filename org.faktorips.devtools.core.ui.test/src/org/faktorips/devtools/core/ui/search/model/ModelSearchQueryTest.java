/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.search.model.scope.ModelSearchScope;
import org.junit.Before;
import org.junit.Test;

public class ModelSearchQueryTest {

    private ModelSearchPresentationModel model;
    private ModelSearchQuery query;
    private ModelSearchResult searchResult;

    private ModelSearchScope scope;

    @Before
    public void setUp() throws Exception {
        model = mock(ModelSearchPresentationModel.class);

        scope = mock(ModelSearchScope.class);

        when(model.getSearchScope()).thenReturn(scope);

        query = new ModelSearchQuery(model);
        searchResult = (ModelSearchResult)query.getSearchResult();
    }

    @Test
    public void testSucheKlassenName() throws CoreException {

        when(model.getTypeName()).thenReturn("SrcF");

        IIpsSrcFile srcFile1 = mock(IpsSrcFile.class);
        when(srcFile1.getIpsObjectName()).thenReturn("SrcFile1");
        when(srcFile1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsObject object1 = mock(IpsObject.class);
        when(srcFile1.getIpsObject()).thenReturn(object1);

        IIpsSrcFile srcFile2 = mock(IpsSrcFile.class);
        when(srcFile2.getIpsObjectName()).thenReturn("SourceFile2");
        when(srcFile2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsObject object2 = mock(IpsObject.class);
        when(srcFile2.getIpsObject()).thenReturn(object2);

        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
        srcFiles.add(srcFile1);
        srcFiles.add(srcFile2);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(srcFiles);

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(srcFile1.getIpsObject());
    }

    protected void assertObjectMatched(Object object) {
        Set<IIpsElement> matches = searchResult.getMatchingIpsElements();
        assertTrue(matches.contains(object));

        /*
         * Match[] matches = searchResult.getMatches(object); assertEquals(object,
         * matches[0].getElement());
         */
    }

    protected void assertObjectNotMatched(Object object) {
        assertFalse(searchResult.getMatchingIpsElements().contains(object));
    }

    @Test
    public void testCoreException() throws CoreException {

        IStatus exceptionStatus = new IpsStatus(IStatus.ERROR, "xyz");
        when(scope.getSelectedIpsSrcFiles()).thenThrow(new CoreException(exceptionStatus));

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.ERROR, status.getSeverity());
    }

    @Test
    public void testSucheAttribute() throws CoreException {

        when(model.getTypeName()).thenReturn("SrcF");
        when(model.getSearchTerm()).thenReturn("MatchingAttr");

        when(model.isSearchAttributes()).thenReturn(true);

        IIpsSrcFile srcFile1 = mock(IpsSrcFile.class);
        when(srcFile1.getIpsObjectName()).thenReturn("SrcFile1");
        when(srcFile1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile srcFile2 = mock(IpsSrcFile.class);
        when(srcFile2.getIpsObjectName()).thenReturn("SourceFile2");
        when(srcFile2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
        srcFiles.add(srcFile1);
        srcFiles.add(srcFile2);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(srcFiles);

        IType type1 = mock(IType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        IType type2 = mock(IType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        IAttribute matchingAttribute = mock(IAttribute.class);
        when(matchingAttribute.getName()).thenReturn("MatchingAttribute");

        IAttribute notMatchingAttribute = mock(IAttribute.class);
        when(notMatchingAttribute.getName()).thenReturn("NoMatchingAttribute");

        List<IAttribute> attributes = new ArrayList<IAttribute>();
        attributes.add(notMatchingAttribute);
        attributes.add(matchingAttribute);

        when(type1.getAttributes()).thenReturn(attributes);

        IAttribute nameMatchingAttributeAtWrongClass = mock(IAttribute.class);
        when(nameMatchingAttributeAtWrongClass.getName()).thenReturn("MatchingAttributeAtWrongClass");

        when(type2.getAttributes()).thenReturn(Collections.singletonList(nameMatchingAttributeAtWrongClass));

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingAttribute);
        assertObjectNotMatched(notMatchingAttribute);
        assertObjectNotMatched(nameMatchingAttributeAtWrongClass);

        when(model.isSearchAttributes()).thenReturn(false);

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());

    }

    @Test
    public void testSucheMethoden() throws CoreException {

        when(model.getTypeName()).thenReturn("SrcF");
        when(model.getSearchTerm()).thenReturn("MatchingMet");

        when(model.isSearchMethods()).thenReturn(true);

        IIpsSrcFile srcFile1 = mock(IpsSrcFile.class);
        when(srcFile1.getIpsObjectName()).thenReturn("SrcFile1");
        when(srcFile1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile srcFile2 = mock(IpsSrcFile.class);
        when(srcFile2.getIpsObjectName()).thenReturn("SourceFile2");
        when(srcFile2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
        srcFiles.add(srcFile1);
        srcFiles.add(srcFile2);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(srcFiles);

        IType type1 = mock(IType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        IType type2 = mock(IType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        IMethod matchingMethod = mock(IMethod.class);
        when(matchingMethod.getName()).thenReturn("MatchingMethod");

        IMethod notMatchingMethod = mock(IMethod.class);
        when(notMatchingMethod.getName()).thenReturn("NoMatchingMethod");

        ArrayList<IMethod> methods = new ArrayList<IMethod>();
        methods.add(notMatchingMethod);
        methods.add(matchingMethod);

        when(type1.getMethods()).thenReturn(methods);

        IMethod nameMatchingMethodAtWrongClass = mock(IMethod.class);
        when(nameMatchingMethodAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getMethods()).thenReturn(Collections.singletonList(nameMatchingMethodAtWrongClass));

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingMethod);
        assertObjectNotMatched(notMatchingMethod);
        assertObjectNotMatched(nameMatchingMethodAtWrongClass);

        when(model.isSearchMethods()).thenReturn(false);

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());

    }

    @Test
    public void testSucheAssoziationen() throws CoreException {

        when(model.getTypeName()).thenReturn("SrcF");
        when(model.getSearchTerm()).thenReturn("MatchingMet");

        when(model.isSearchAssociations()).thenReturn(true);

        IIpsSrcFile srcFile1 = mock(IpsSrcFile.class);
        when(srcFile1.getIpsObjectName()).thenReturn("SrcFile1");
        when(srcFile1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile srcFile2 = mock(IpsSrcFile.class);
        when(srcFile2.getIpsObjectName()).thenReturn("SourceFile2");
        when(srcFile2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
        srcFiles.add(srcFile1);
        srcFiles.add(srcFile2);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(srcFiles);

        IType type1 = mock(IType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        IType type2 = mock(IType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        IAssociation matchingAssociation = mock(IAssociation.class);
        when(matchingAssociation.getName()).thenReturn("MatchingMethod");

        IAssociation notMatchingAssociation = mock(IAssociation.class);
        when(notMatchingAssociation.getName()).thenReturn("NoMatchingAssociation");

        ArrayList<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(notMatchingAssociation);
        associations.add(matchingAssociation);

        when(type1.getAssociations()).thenReturn(associations);

        IAssociation nameMatchingAssociationAtWrongClass = mock(IAssociation.class);
        when(nameMatchingAssociationAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getAssociations()).thenReturn(Collections.singletonList(nameMatchingAssociationAtWrongClass));

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingAssociation);
        assertObjectNotMatched(notMatchingAssociation);
        assertObjectNotMatched(nameMatchingAssociationAtWrongClass);

        when(model.isSearchAssociations()).thenReturn(false);

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());

    }

    @Test
    public void testSucheTableStructureUsages() throws CoreException {

        when(model.getTypeName()).thenReturn("SrcF");
        when(model.getSearchTerm()).thenReturn("MatchingMet");

        when(model.isSearchTableStructureUsages()).thenReturn(true);

        IIpsSrcFile srcFile1 = mock(IpsSrcFile.class);
        when(srcFile1.getIpsObjectName()).thenReturn("SrcFile1");
        when(srcFile1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile srcFile2 = mock(IpsSrcFile.class);
        when(srcFile2.getIpsObjectName()).thenReturn("SourceFile2");
        when(srcFile2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
        srcFiles.add(srcFile1);
        srcFiles.add(srcFile2);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(srcFiles);

        ProductCmptType type1 = mock(ProductCmptType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        ProductCmptType type2 = mock(ProductCmptType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        ITableStructureUsage matchingTableStructureUsage = mock(ITableStructureUsage.class);
        when(matchingTableStructureUsage.getName()).thenReturn("MatchingMethod");

        ITableStructureUsage notMatchingTableStructureUsage = mock(ITableStructureUsage.class);
        when(notMatchingTableStructureUsage.getName()).thenReturn("NoMatchingTableStructureUsage");

        ArrayList<ITableStructureUsage> tableStructureUsages = new ArrayList<ITableStructureUsage>();
        tableStructureUsages.add(notMatchingTableStructureUsage);
        tableStructureUsages.add(matchingTableStructureUsage);

        when(type1.getTableStructureUsages()).thenReturn(tableStructureUsages);

        ITableStructureUsage nameMatchingTableStructureUsageAtWrongClass = mock(ITableStructureUsage.class);
        when(nameMatchingTableStructureUsageAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getTableStructureUsages()).thenReturn(
                Collections.singletonList(nameMatchingTableStructureUsageAtWrongClass));

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingTableStructureUsage);
        assertObjectNotMatched(notMatchingTableStructureUsage);
        assertObjectNotMatched(nameMatchingTableStructureUsageAtWrongClass);

        when(model.isSearchTableStructureUsages()).thenReturn(false);

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());
    }

    @Test
    public void testSucheRules() throws CoreException {

        when(model.getTypeName()).thenReturn("SrcF");
        when(model.getSearchTerm()).thenReturn("MatchingMet");

        when(model.isSearchValidationRules()).thenReturn(true);

        IIpsSrcFile srcFile1 = mock(IpsSrcFile.class);
        when(srcFile1.getIpsObjectName()).thenReturn("SrcFile1");
        when(srcFile1.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile srcFile2 = mock(IpsSrcFile.class);
        when(srcFile2.getIpsObjectName()).thenReturn("SourceFile2");
        when(srcFile2.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
        srcFiles.add(srcFile1);
        srcFiles.add(srcFile2);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(srcFiles);

        PolicyCmptType type1 = mock(PolicyCmptType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        PolicyCmptType type2 = mock(PolicyCmptType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        IValidationRule matchingRule = mock(IValidationRule.class);
        when(matchingRule.getName()).thenReturn("MatchingMethod");

        IValidationRule notMatchingRule = mock(IValidationRule.class);
        when(notMatchingRule.getName()).thenReturn("NoMatchingRule");

        ArrayList<IValidationRule> Rules = new ArrayList<IValidationRule>();
        Rules.add(notMatchingRule);
        Rules.add(matchingRule);

        when(type1.getValidationRules()).thenReturn(Rules);

        IValidationRule nameMatchingRuleAtWrongClass = mock(IValidationRule.class);
        when(nameMatchingRuleAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getValidationRules()).thenReturn(Collections.singletonList(nameMatchingRuleAtWrongClass));

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingRule);
        assertObjectNotMatched(notMatchingRule);
        assertObjectNotMatched(nameMatchingRuleAtWrongClass);

        when(model.isSearchValidationRules()).thenReturn(false);

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());
    }
}
