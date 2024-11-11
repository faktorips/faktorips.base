package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

public class SortAlphabeticallyTest extends AbstractIpsPluginTest {

    private static final String ASSOCIATION = "association";

    private IIpsProject ipsProject;

    private ProductCmpt associated1;
    private ProductCmpt associated2;
    private ProductCmpt associated3;
    private ProductCmpt associated4;
    private ProductCmpt associated5;
    private ProductCmpt associated6;
    private ProductCmpt associated7;
    private ProductCmpt associated8;
    private ProductCmpt associated9;
    private ProductCmpt associated10;
    private ProductCmpt associated11;
    private ProductCmpt associated12;
    private IProductCmptType type;
    private IProductCmptType associatedType;

    private IProductCmptTypeAssociation association;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");

        type = newProductCmptType(ipsProject, "model.ProductType");

        associatedType = newProductCmptType(ipsProject, "model.AssociatedType");
    }

    // Tests for ignoring cases, prefixes should not have an impact at all!
    @Test
    public void testGetExistingLinksInOrder_OneNameWithCapitalLetter() {
        associated1 = newProductCmpt(associatedType, "produkt.A.aBa");
        associated2 = newProductCmpt(associatedType, "produkt.B.aaa");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("aaa"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("aBa"));
    }

    @Test
    public void testGetExistingLinksInOrder_SamePrefixes() {
        associated1 = newProductCmpt(associatedType, "produkt.a.B");
        associated2 = newProductCmpt(associatedType, "produkt.a.a");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("a"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("B"));
    }

    @Test
    public void testGetExistingLinksInOrder_OnePrefixInCapital() {
        associated1 = newProductCmpt(associatedType, "produkt.B.B");
        associated2 = newProductCmpt(associatedType, "produkt.a.a");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("a"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("B"));
    }

    @Test
    public void testGetExistingLinksInOrder_BothPrefixesInCapital() {
        associated1 = newProductCmpt(associatedType, "produkt.A.z");
        associated2 = newProductCmpt(associatedType, "produkt.Z.a");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("a"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("z"));

    }

    @Test
    public void testGetExistingLinksInOrder_SonderzeichenAndNumbers() {
        associated1 = newProductCmpt(associatedType, "produkt.A.aa");
        associated2 = newProductCmpt(associatedType, "produkt.Z.a_a");
        associated3 = newProductCmpt(associatedType, "produkt.Z.b");
        associated4 = newProductCmpt(associatedType, "produkt.A.A#");
        associated5 = newProductCmpt(associatedType, "produkt.A.1a");
        associated6 = newProductCmpt(associatedType, "produkt.A.a2a");
        associated7 = newProductCmpt(associatedType, "produkt.A.a1a");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated3.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated4.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated5.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated6.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated7.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("1a"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("A#"));
        assertThat(generation.getLinks(ASSOCIATION)[2].getTargetRuntimeId(), is("a1a"));
        assertThat(generation.getLinks(ASSOCIATION)[3].getTargetRuntimeId(), is("a2a"));
        assertThat(generation.getLinks(ASSOCIATION)[4].getTargetRuntimeId(), is("a_a"));
        assertThat(generation.getLinks(ASSOCIATION)[5].getTargetRuntimeId(), is("aa"));
        assertThat(generation.getLinks(ASSOCIATION)[6].getTargetRuntimeId(), is("b"));
    }

    @Test
    public void testGetExistingLinksInOrder_UmlauteAndScharfesS() {
        associated1 = newProductCmpt(associatedType, "produkt.Ä");
        associated2 = newProductCmpt(associatedType, "produkt.z");
        associated3 = newProductCmpt(associatedType, "produkt.Ü");
        associated4 = newProductCmpt(associatedType, "produkt.ö");
        associated5 = newProductCmpt(associatedType, "produkt.ß");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated3.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated4.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated5.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("z"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("ß"));
        assertThat(generation.getLinks(ASSOCIATION)[2].getTargetRuntimeId(), is("Ä"));
        assertThat(generation.getLinks(ASSOCIATION)[3].getTargetRuntimeId(), is("ö"));
        assertThat(generation.getLinks(ASSOCIATION)[4].getTargetRuntimeId(), is("Ü"));
    }

    @Test
    public void testGetExistingLinksInOrder_Sorted() {
        associated1 = newProductCmpt(associatedType, "produkt.aaa");
        associated2 = newProductCmpt(associatedType, "produkt.aBa");
        associated3 = newProductCmpt(associatedType, "produkt.B");
        associated4 = newProductCmpt(associatedType, "produkt.Bc");
        associated5 = newProductCmpt(associatedType, "produkt.BD");
        associated6 = newProductCmpt(associatedType, "produkt.za");
        associated7 = newProductCmpt(associatedType, "produkt.Zb");
        associated8 = newProductCmpt(associatedType, "produkt.ZC");
        associated9 = newProductCmpt(associatedType, "produkt.ß");
        associated10 = newProductCmpt(associatedType, "produkt.ä");
        associated11 = newProductCmpt(associatedType, "produkt.ö");
        associated12 = newProductCmpt(associatedType, "produkt.ü");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated3.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated4.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated5.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated6.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated7.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated8.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated9.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated10.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated11.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated12.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("aaa"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("aBa"));
        assertThat(generation.getLinks(ASSOCIATION)[2].getTargetRuntimeId(), is("B"));
        assertThat(generation.getLinks(ASSOCIATION)[3].getTargetRuntimeId(), is("Bc"));
        assertThat(generation.getLinks(ASSOCIATION)[4].getTargetRuntimeId(), is("BD"));
        assertThat(generation.getLinks(ASSOCIATION)[5].getTargetRuntimeId(), is("za"));
        assertThat(generation.getLinks(ASSOCIATION)[6].getTargetRuntimeId(), is("Zb"));
        assertThat(generation.getLinks(ASSOCIATION)[7].getTargetRuntimeId(), is("ZC"));
        assertThat(generation.getLinks(ASSOCIATION)[8].getTargetRuntimeId(), is("ß"));
        assertThat(generation.getLinks(ASSOCIATION)[9].getTargetRuntimeId(), is("ä"));
        assertThat(generation.getLinks(ASSOCIATION)[10].getTargetRuntimeId(), is("ö"));
        assertThat(generation.getLinks(ASSOCIATION)[11].getTargetRuntimeId(), is("ü"));
    }

    @Test
    public void testGetExistingLinksInOrder_ReverseSorted() {
        associated1 = newProductCmpt(associatedType, "produkt.aaa");
        associated2 = newProductCmpt(associatedType, "produkt.aBa");
        associated3 = newProductCmpt(associatedType, "produkt.B");
        associated4 = newProductCmpt(associatedType, "produkt.Bc");
        associated5 = newProductCmpt(associatedType, "produkt.BD");
        associated6 = newProductCmpt(associatedType, "produkt.za");
        associated7 = newProductCmpt(associatedType, "produkt.Zb");
        associated8 = newProductCmpt(associatedType, "produkt.ZC");
        associated9 = newProductCmpt(associatedType, "produkt.ß");
        associated10 = newProductCmpt(associatedType, "produkt.ä");
        associated11 = newProductCmpt(associatedType, "produkt.ö");
        associated12 = newProductCmpt(associatedType, "produkt.ü");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated12.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated11.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated10.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated9.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated8.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated7.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated6.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated5.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated4.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated3.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("aaa"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("aBa"));
        assertThat(generation.getLinks(ASSOCIATION)[2].getTargetRuntimeId(), is("B"));
        assertThat(generation.getLinks(ASSOCIATION)[3].getTargetRuntimeId(), is("Bc"));
        assertThat(generation.getLinks(ASSOCIATION)[4].getTargetRuntimeId(), is("BD"));
        assertThat(generation.getLinks(ASSOCIATION)[5].getTargetRuntimeId(), is("za"));
        assertThat(generation.getLinks(ASSOCIATION)[6].getTargetRuntimeId(), is("Zb"));
        assertThat(generation.getLinks(ASSOCIATION)[7].getTargetRuntimeId(), is("ZC"));
        assertThat(generation.getLinks(ASSOCIATION)[8].getTargetRuntimeId(), is("ß"));
        assertThat(generation.getLinks(ASSOCIATION)[9].getTargetRuntimeId(), is("ä"));
        assertThat(generation.getLinks(ASSOCIATION)[10].getTargetRuntimeId(), is("ö"));
        assertThat(generation.getLinks(ASSOCIATION)[11].getTargetRuntimeId(), is("ü"));
    }

    @Test
    public void testGetExistingLinksInOrder_Unsorted() {
        associated1 = newProductCmpt(associatedType, "produkt.aaa");
        associated2 = newProductCmpt(associatedType, "produkt.aBa");
        associated3 = newProductCmpt(associatedType, "produkt.B");
        associated4 = newProductCmpt(associatedType, "produkt.Bc");
        associated5 = newProductCmpt(associatedType, "produkt.BD");
        associated6 = newProductCmpt(associatedType, "produkt.za");
        associated7 = newProductCmpt(associatedType, "produkt.Zb");
        associated8 = newProductCmpt(associatedType, "produkt.ZC");
        associated9 = newProductCmpt(associatedType, "produkt.ß");
        associated10 = newProductCmpt(associatedType, "produkt.ä");
        associated11 = newProductCmpt(associatedType, "produkt.ö");
        associated12 = newProductCmpt(associatedType, "produkt.ü");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        var newLink = generation.newLink(association.getName());
        newLink.setTarget(associated12.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated10.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated9.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated11.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated7.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated4.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated5.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated8.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated3.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated6.getQualifiedName());

        SortAlphabetically.getExistingLinksInOrder(ipsProject, generation, association);
        assertThat(generation.getLinks(ASSOCIATION)[0].getTargetRuntimeId(), is("aaa"));
        assertThat(generation.getLinks(ASSOCIATION)[1].getTargetRuntimeId(), is("aBa"));
        assertThat(generation.getLinks(ASSOCIATION)[2].getTargetRuntimeId(), is("B"));
        assertThat(generation.getLinks(ASSOCIATION)[3].getTargetRuntimeId(), is("Bc"));
        assertThat(generation.getLinks(ASSOCIATION)[4].getTargetRuntimeId(), is("BD"));
        assertThat(generation.getLinks(ASSOCIATION)[5].getTargetRuntimeId(), is("za"));
        assertThat(generation.getLinks(ASSOCIATION)[6].getTargetRuntimeId(), is("Zb"));
        assertThat(generation.getLinks(ASSOCIATION)[7].getTargetRuntimeId(), is("ZC"));
        assertThat(generation.getLinks(ASSOCIATION)[8].getTargetRuntimeId(), is("ß"));
        assertThat(generation.getLinks(ASSOCIATION)[9].getTargetRuntimeId(), is("ä"));
        assertThat(generation.getLinks(ASSOCIATION)[10].getTargetRuntimeId(), is("ö"));
        assertThat(generation.getLinks(ASSOCIATION)[11].getTargetRuntimeId(), is("ü"));
    }

}
