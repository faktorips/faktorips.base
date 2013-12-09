<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />

	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/TableStructure/UniqueKey">
		<Index uniqueKey="true">
			<xsl:apply-templates select="@* | node()" />
		</Index>
	</xsl:template>

	<xsl:template match="/EnumType/@containingValues[.='true']">
		<xsl:attribute name="extensible">false</xsl:attribute>
	</xsl:template>

	<xsl:template match="/EnumType/@containingValues[.='false']">
		<xsl:attribute name="extensible">true</xsl:attribute>
	</xsl:template>

</xsl:stylesheet>