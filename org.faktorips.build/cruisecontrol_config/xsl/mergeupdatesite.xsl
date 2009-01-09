<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
         
    <xsl:param name="doc2"/> 
    
    <xsl:template match="site">
        <site>
        <xsl:copy-of select="./*"/>
              <xsl:copy-of select="document($doc2)/site/feature"/>
              <xsl:copy-of select="document($doc2)/site/category-def"/>
        </site>
    </xsl:template>

</xsl:stylesheet>