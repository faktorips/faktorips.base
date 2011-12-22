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
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.search.ui.ISearchQuery;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.search.IpsSearchResult;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;
import org.junit.Before;
import org.junit.Test;

public class ModelSearchQueryTest {

    private ModelSearchPresentationModel searchModel;
    private ISearchQuery query;
    private IpsSearchResult searchResult;
    private IIpsModel ipsModel;

    private IIpsSearchScope scope;

    @Before
    public void setUp() throws Exception {
        searchModel = mock(ModelSearchPresentationModel.class);

        ipsModel = mock(IIpsModel.class);
        when(ipsModel.getExtensionPropertyDefinitions(any(Class.class), anyBoolean())).thenReturn(
                new IExtensionPropertyDefinition[0]);

        scope = mock(IIpsSearchScope.class);

        when(searchModel.getSearchScope()).thenReturn(scope);

        query = new ModelSearchQuery(searchModel, ipsModel);
        searchResult = (IpsSearchResult)query.getSearchResult();
    }

    @Test
    public void testSucheKlassenName() throws CoreException {

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");

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

    protected void assertObjectMatched(IIpsElement object) {
        assertNotSame("Object " + object + " is not matched", 0, searchResult.getMatchCount(object));
    }

    protected void assertObjectNotMatched(Object object) {
        assertEquals("Object " + object + " is matched, but should not.", 0, searchResult.getMatchCount(object));
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

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");
        when(searchModel.getSearchTerm()).thenReturn("MatchingAttr");

        Set<Class<? extends IIpsObjectPart>> set = new HashSet<Class<? extends IIpsObjectPart>>();
        set.add(IAttribute.class);
        when(searchModel.getSearchedClazzes()).thenReturn(set);

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

        IIpsElement[] attributes = new IIpsElement[] { notMatchingAttribute, matchingAttribute };

        when(type1.getChildren()).thenReturn(attributes);

        IAttribute nameMatchingAttributeAtWrongClass = mock(IAttribute.class);
        when(nameMatchingAttributeAtWrongClass.getName()).thenReturn("MatchingAttributeAtWrongClass");

        when(type2.getChildren()).thenReturn(new IIpsElement[] { nameMatchingAttributeAtWrongClass });

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingAttribute);
        assertObjectNotMatched(notMatchingAttribute);
        assertObjectNotMatched(nameMatchingAttributeAtWrongClass);

        when(searchModel.getSearchedClazzes()).thenReturn(new HashSet<Class<? extends IIpsObjectPart>>());

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());

    }

    @Test
    public void testSucheMethoden() throws CoreException {

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");
        when(searchModel.getSearchTerm()).thenReturn("MatchingMet");

        Set<Class<? extends IIpsObjectPart>> set = new HashSet<Class<? extends IIpsObjectPart>>();
        set.add(IMethod.class);
        when(searchModel.getSearchedClazzes()).thenReturn(set);

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

        IIpsElement[] methods = new IIpsElement[] { notMatchingMethod, matchingMethod };

        when(type1.getChildren()).thenReturn(methods);

        IMethod nameMatchingMethodAtWrongClass = mock(IMethod.class);
        when(nameMatchingMethodAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getChildren()).thenReturn(new IIpsElement[] { nameMatchingMethodAtWrongClass });

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingMethod);
        assertObjectNotMatched(notMatchingMethod);
        assertObjectNotMatched(nameMatchingMethodAtWrongClass);

        when(searchModel.getSearchedClazzes()).thenReturn(new HashSet<Class<? extends IIpsObjectPart>>());

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());

    }

    @Test
    public void testSucheAssoziationen() throws CoreException {

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");
        when(searchModel.getSearchTerm()).thenReturn("MatchingMet");

        Set<Class<? extends IIpsObjectPart>> set = new HashSet<Class<? extends IIpsObjectPart>>();
        set.add(IAssociation.class);
        when(searchModel.getSearchedClazzes()).thenReturn(set);

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

        IIpsElement[] associations = new IIpsElement[] { notMatchingAssociation, matchingAssociation };

        when(type1.getChildren()).thenReturn(associations);

        IAssociation nameMatchingAssociationAtWrongClass = mock(IAssociation.class);
        when(nameMatchingAssociationAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getChildren()).thenReturn(new IIpsElement[] { nameMatchingAssociationAtWrongClass });

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingAssociation);
        assertObjectNotMatched(notMatchingAssociation);
        assertObjectNotMatched(nameMatchingAssociationAtWrongClass);

        when(searchModel.getSearchedClazzes()).thenReturn(new HashSet<Class<? extends IIpsObjectPart>>());

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());

    }

    @Test
    public void testSucheTableStructureUsages() throws CoreException {

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");
        when(searchModel.getSearchTerm()).thenReturn("MatchingMet");

        Set<Class<? extends IIpsObjectPart>> set = new HashSet<Class<? extends IIpsObjectPart>>();
        set.add(ITableStructureUsage.class);
        when(searchModel.getSearchedClazzes()).thenReturn(set);

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

        IProductCmptType type1 = mock(IProductCmptType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        IProductCmptType type2 = mock(IProductCmptType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        ITableStructureUsage matchingTableStructureUsage = mock(ITableStructureUsage.class);
        when(matchingTableStructureUsage.getName()).thenReturn("MatchingMethod");

        ITableStructureUsage notMatchingTableStructureUsage = mock(ITableStructureUsage.class);
        when(notMatchingTableStructureUsage.getName()).thenReturn("NoMatchingTableStructureUsage");

        IIpsElement[] tableStructureUsages = new IIpsElement[] { notMatchingTableStructureUsage,
                matchingTableStructureUsage };

        when(type1.getChildren()).thenReturn(tableStructureUsages);

        ITableStructureUsage nameMatchingTableStructureUsageAtWrongClass = mock(ITableStructureUsage.class);
        when(nameMatchingTableStructureUsageAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getChildren()).thenReturn(new IIpsElement[] { nameMatchingTableStructureUsageAtWrongClass });

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingTableStructureUsage);
        assertObjectNotMatched(notMatchingTableStructureUsage);
        assertObjectNotMatched(nameMatchingTableStructureUsageAtWrongClass);

        when(searchModel.getSearchedClazzes()).thenReturn(new HashSet<Class<? extends IIpsObjectPart>>());

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());
    }

    @Test
    public void testSucheRules() throws CoreException {

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");
        when(searchModel.getSearchTerm()).thenReturn("MatchingMet");

        Set<Class<? extends IIpsObjectPart>> set = new HashSet<Class<? extends IIpsObjectPart>>();
        set.add(IValidationRule.class);
        when(searchModel.getSearchedClazzes()).thenReturn(set);

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

        IPolicyCmptType type1 = mock(IPolicyCmptType.class);
        when(type1.getName()).thenReturn("SrcFile1");

        IPolicyCmptType type2 = mock(IPolicyCmptType.class);
        when(type2.getName()).thenReturn("SourceFile2");

        when(srcFile1.getIpsObject()).thenReturn(type1);
        when(srcFile2.getIpsObject()).thenReturn(type2);

        IValidationRule matchingRule = mock(IValidationRule.class);
        when(matchingRule.getName()).thenReturn("MatchingMethod");

        IValidationRule notMatchingRule = mock(IValidationRule.class);
        when(notMatchingRule.getName()).thenReturn("NoMatchingRule");

        IIpsElement[] rules = new IIpsElement[] { notMatchingRule, matchingRule };

        when(type1.getChildren()).thenReturn(rules);

        IValidationRule nameMatchingRuleAtWrongClass = mock(IValidationRule.class);
        when(nameMatchingRuleAtWrongClass.getName()).thenReturn("MatchingMethodAtWrongClass");

        when(type2.getChildren()).thenReturn(new IIpsElement[] { nameMatchingRuleAtWrongClass });

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingRule);
        assertObjectNotMatched(notMatchingRule);
        assertObjectNotMatched(nameMatchingRuleAtWrongClass);

        when(searchModel.getSearchedClazzes()).thenReturn(new HashSet<Class<? extends IIpsObjectPart>>());

        status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(0, searchResult.getMatchCount());
    }

    @Test
    public void testSucheLabels() throws CoreException {

        when(searchModel.getSrcFilePattern()).thenReturn("SrcF");
        when(searchModel.getSearchTerm()).thenReturn("Matching");

        Set<Class<? extends IIpsObjectPart>> set = new HashSet<Class<? extends IIpsObjectPart>>();
        set.add(IAttribute.class);
        when(searchModel.getSearchedClazzes()).thenReturn(set);

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
        when(matchingAttribute.getName()).thenReturn("Name");

        ILabel label = mock(ILabel.class);
        when(label.getValue()).thenReturn("MatchingLabel");

        List<ILabel> labels = new ArrayList<ILabel>();
        labels.add(label);
        when(matchingAttribute.getLabels()).thenReturn(labels);

        IIpsElement[] attributes = new IIpsElement[] { matchingAttribute };

        when(type1.getChildren()).thenReturn(attributes);

        IStatus status = query.run(new NullProgressMonitor());

        assertEquals(IStatus.OK, status.getSeverity());

        assertEquals(1, searchResult.getMatchCount());
        assertObjectMatched(matchingAttribute);
    }
}
