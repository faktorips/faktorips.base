/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IndexNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QualifierAndIndexParserTest extends AbstractParserTest {

    private static final String RUNTIME_ID = "RuntimeID";

    private static final int MY_INDEX = 12;

    private static final String MY_QUALIFIER = "myQualifier";

    private static final String INDEX = MY_INDEX + "]";

    private static final String QUALIFIER = "\"" + MY_QUALIFIER + "\"]";

    @Mock
    private IPolicyCmptTypeAssociation association;

    @Mock
    private IPolicyCmptType targetSubType;

    @Mock
    private IPolicyCmptType targetType;

    @Mock
    private IProductCmpt productCmpt;

    @Mock
    private IProductCmpt productCmptOther;

    @Mock
    private IProductCmptType productCmptTypeOther;

    @Mock
    private IIpsSrcFile productCmptIpsSrcFile;

    @Mock
    private IIpsSrcFile productCmptIpsSrcFileOther;

    private QualifierAndIndexParser qualifierAndIndexParser;

    @Mock
    private IdentifierNodeFactory nodeFactory;

    @Before
    public void initParser() {
        qualifierAndIndexParser = new QualifierAndIndexParser(getParsingContext());
    }

    @Test
    public void testParse_noIndexFor1To1Association() throws Exception {
        when(association.is1ToMany()).thenReturn(false);
        initSourceFile();
        getParsingContext().pushNode(createAssociationNode(association, false));

        InvalidIdentifierNode node = (InvalidIdentifierNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0,
                INDEX.length()));

        assertEquals(ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION, node.getMessage().getCode());
    }

    @Test
    public void testParse_findAssociationQualified1To1IgnoringQualifier() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(false);
        initProdCmptAndType();
        initSourceFile();
        getParsingContext().pushNode(createAssociationNode(association, false));
        QualifierNode node = (QualifierNode)qualifierAndIndexParser.parse(new TextRegion(QUALIFIER, 0, QUALIFIER
                .length()));

        assertEquals(RUNTIME_ID, node.getRuntimeId());
        assertEquals(targetSubType, node.getDatatype());
        assertEquals(productCmpt, node.getProductCmpt());
    }

    private void initProdCmptAndType() {
        ArrayList<IIpsSrcFile> list = new ArrayList<>();
        list.add(productCmptIpsSrcFileOther);
        list.add(productCmptIpsSrcFile);

        when(getIpsProject().findProductCmptByUnqualifiedName(MY_QUALIFIER)).thenReturn(list);
        when(productCmptIpsSrcFile.getIpsObject()).thenReturn(productCmpt);
        when(productCmptIpsSrcFileOther.getIpsObject()).thenReturn(productCmptOther);
        when(productCmpt.findProductCmptType(getIpsProject())).thenReturn(getProductCmptType());
        when(productCmptOther.findProductCmptType(getIpsProject())).thenReturn(productCmptTypeOther);
        when(getProductCmptType().isSubtypeOrSameType(getProductCmptType(), getIpsProject())).thenReturn(true);
        when(productCmptTypeOther.isSubtypeOrSameType(getProductCmptType(), getIpsProject())).thenReturn(false);
    }

    @Test
    public void testParse_findAssociationQualified1ToMany() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(true);
        initProdCmptAndType();
        initSourceFile();
        getParsingContext().pushNode(createAssociationNode(association, false));

        QualifierNode node = (QualifierNode)qualifierAndIndexParser.parse(new TextRegion(QUALIFIER, 0, QUALIFIER
                .length()));

        assertEquals(RUNTIME_ID, node.getRuntimeId());
        assertEquals(new ListOfTypeDatatype(targetSubType), node.getDatatype());
        assertEquals(productCmpt, node.getProductCmpt());
    }

    @Test
    public void testParse_findAssociationQualified1To1FromMany() throws Exception {
        when(association.is1ToManyIgnoringQualifier()).thenReturn(false);
        initProdCmptAndType();
        initSourceFile();
        getParsingContext().pushNode(createAssociationNode(association, true));

        QualifierNode node = (QualifierNode)qualifierAndIndexParser.parse(new TextRegion(QUALIFIER, 0, QUALIFIER
                .length()));

        assertEquals(RUNTIME_ID, node.getRuntimeId());
        assertEquals(new ListOfTypeDatatype(targetSubType), node.getDatatype());
        assertEquals(productCmpt, node.getProductCmpt());
    }

    @Test
    public void testParse_findAssociationQualified_NoRuntimeID() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(false);
        initProdCmptAndType();
        initSourceFileNoRuntimeID();
        getParsingContext().pushNode(createAssociationNode(association, false));

        InvalidIdentifierNode node = (InvalidIdentifierNode)qualifierAndIndexParser.parse(new TextRegion(QUALIFIER, 0,
                QUALIFIER.length()));
        assertEquals(ExprCompiler.UNKNOWN_QUALIFIER, node.getMessage().getCode());
    }

    @Test
    public void testParse_findAssociationIndex() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        initSourceFileNoRuntimeID();
        getParsingContext().pushNode(createAssociationNode(association, false));

        IndexNode node = (IndexNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0, INDEX.length()));

        assertEquals(MY_INDEX, node.getIndex());
        assertEquals(targetType, node.getDatatype());
    }

    @Test
    public void testParse_IndexAtTo1AssociationButListContext() throws Exception {
        when(association.is1ToMany()).thenReturn(false);
        initSourceFileNoRuntimeID();
        getParsingContext().pushNode(createAssociationNode(association, true));

        IndexNode node = (IndexNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0, INDEX.length()));

        assertEquals(MY_INDEX, node.getIndex());
        assertEquals(targetType, node.getDatatype());
    }

    @Test
    public void testParse_invalidAssociationTo1Index() throws Exception {
        when(association.is1ToMany()).thenReturn(false);
        initSourceFileNoRuntimeID();
        getParsingContext().pushNode(createAssociationNode(association, false));

        InvalidIdentifierNode node = (InvalidIdentifierNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0,
                INDEX.length()));

        assertEquals(ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION, node.getMessage().getCode());
    }

    @Test
    public void testParse_associationInvalidIndex() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        initSourceFileNoRuntimeID();
        getParsingContext().pushNode(createAssociationNode(association, true));

        InvalidIdentifierNode node = (InvalidIdentifierNode)qualifierAndIndexParser.parse(new TextRegion("[asd]", 0,
                "[asd]".length()));

        assertEquals(ExprCompiler.UNKNOWN_QUALIFIER, node.getMessage().getCode());
    }

    @Test
    public void testParse_findAssociation1ToManyIndexedFromList() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(true);
        initSourceFileNoRuntimeID();
        getParsingContext().pushNode(createAssociationNode(association, true));

        IndexNode node = (IndexNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0, INDEX.length()));

        assertEquals(MY_INDEX, node.getIndex());
        assertEquals(targetType, node.getDatatype());
    }

    @Test
    public void testParse_findIndexAfterQualifierOnList() throws Exception {
        initSourceFile(RUNTIME_ID);
        getParsingContext().pushNode(createQualifierNode(true));

        IndexNode node = (IndexNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0, INDEX.length()));

        assertEquals(MY_INDEX, node.getIndex());
        assertEquals(targetSubType, node.getDatatype());
    }

    @Test
    public void testParse_noIndexAfterQualifierOnOneElement() throws Exception {
        initSourceFile(RUNTIME_ID);
        getParsingContext().pushNode(createQualifierNode(false));

        InvalidIdentifierNode node = (InvalidIdentifierNode)qualifierAndIndexParser.parse(new TextRegion(INDEX, 0,
                INDEX.length()));

        assertEquals(ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION, node.getMessage().getCode());
    }

    private IPolicyCmptType initSourceFile() throws Exception {
        return initSourceFile(RUNTIME_ID);
    }

    private IPolicyCmptType initSourceFileNoRuntimeID() throws Exception {
        return initSourceFile(null);
    }

    private IPolicyCmptType initSourceFile(String runtimeID) throws Exception {
        when(association.findTarget(getIpsProject())).thenReturn(targetType);
        when(targetType.findProductCmptType(getIpsProject())).thenReturn(getProductCmptType());
        IIpsSrcFile sourceFile = mock(IIpsSrcFile.class);
        IIpsSrcFile[] ipsSourceFiles = { sourceFile };
        when(getIpsProject().findAllProductCmptSrcFiles(getProductCmptType(), true)).thenReturn(ipsSourceFiles);
        when(sourceFile.getIpsObjectName()).thenReturn(MY_QUALIFIER);
        when(sourceFile.getIpsObject()).thenReturn(productCmpt);
        when(productCmpt.getRuntimeId()).thenReturn(runtimeID);
        when(productCmpt.findPolicyCmptType(getIpsProject())).thenReturn(targetSubType);
        return targetType;
    }

    private AssociationNode createAssociationNode(IPolicyCmptTypeAssociation association, boolean listContext) {
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory(null, getIpsProject());
        return (AssociationNode)nodeFactory.createAssociationNode(association, listContext);
    }

    private IdentifierNode createQualifierNode(boolean listOfTypes) {
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory(null, getIpsProject());
        return nodeFactory.createQualifierNode(productCmpt, QUALIFIER, listOfTypes);
    }
}
