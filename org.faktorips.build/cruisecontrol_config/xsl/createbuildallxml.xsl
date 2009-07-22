<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    
    <!-- root node -->
    <xsl:template match="faktoripscruisecontrol">
        <project>
        <xsl:attribute name="name">org.faktorips.build.all</xsl:attribute>
        <xsl:attribute name="default">build.all</xsl:attribute>
		    <target>
      	 	<xsl:attribute name="name">build.all</xsl:attribute>
            <xsl:apply-templates select="projects/project"/>
		    </target>
        </project>
    </xsl:template>

    <xsl:template match="project">
        <ant>
        <xsl:attribute name="dir"><xsl:value-of select="concat('../',@name,'/build')"/></xsl:attribute>
        <xsl:attribute name="antfile">build.xml</xsl:attribute>
        <xsl:attribute name="target">buildjar</xsl:attribute>
        </ant>
    </xsl:template>

</xsl:stylesheet>
