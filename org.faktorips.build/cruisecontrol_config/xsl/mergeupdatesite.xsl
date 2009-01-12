<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:param name="doc2"/> 
    <xsl:variable name="features" select="/site/feature"/>
    <xsl:variable name="category-defs" select="/site/category-def"/>
    
    <xsl:template match="site">
        <site>
        <!-- copy elements from old site.xml -->
        <xsl:copy-of select="./*"/>
        
        <!-- copy new features -->
        <xsl:for-each select="document($doc2)/site/feature">
            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="version" select="@version"/>
            <xsl:if test="count($features[@id=$id][@version=$version])=0">
                <!-- copy only new features -->
                <xsl:copy-of select="."/>
            </xsl:if>
        </xsl:for-each>

        <!-- copy new category-defs -->
        <xsl:for-each select="document($doc2)/site/category-def">
            <xsl:variable name="name" select="@name"/>
            <xsl:if test="count($category-defs[@name=$name])=0">
                <!-- copy only new category-defs -->
                <xsl:copy-of select="."/>
            </xsl:if>
        </xsl:for-each>

        </site>
    </xsl:template>

</xsl:stylesheet>
