<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="sectionindex">
    <xsl:variable name="section_doc" select="document(concat('../data/section_', @name, '.xml'))/section"/>

    <table width="100%" cellspacing="0" class="section-{@name}">
      <tr><td width="10">
        <xsl:attribute name="rowspan">
          <xsl:value-of select="count($section_doc/entry) + 1"/>
        </xsl:attribute>
      </td><td>
        <h2><a href="{$section_doc/link}"><xsl:value-of select="$section_doc/title"/></a></h2>
      </td>
      </tr>

      <xsl:choose>
        <xsl:when test="@description = 'true'">
          <xsl:apply-templates select="$section_doc/entry" mode="section-description">
            <xsl:with-param name="section" select="@name"/>
          </xsl:apply-templates>
	</xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="$section_doc/entry" mode="section">
            <xsl:with-param name="section" select="@name"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template match="entry" mode="section">
    <xsl:param name="section"/>
    <tr><td class="section-{$section}-light">
      <xsl:apply-templates/>
    </td></tr>
  </xsl:template>

  <xsl:template match="entry[position() mod 2 = 0]" mode="section">
    <xsl:param name="section"/>
    <tr><td class="section-{$section}">
      <xsl:apply-templates/>
    </td></tr>
  </xsl:template>

  <xsl:template match="entry" mode="section-description">
    <xsl:param name="section"/>
	  
    <tr><td class="section-{$section}-light">
      <table width="100%" cellspacing="0">
      <tr><td colspan="2">
        <xsl:apply-templates/>
      </td></tr><tr><td width="20"></td><td>
        <xsl:value-of select="@description"/>
      </td></tr>
      </table>
    </td></tr>
  </xsl:template>

  <xsl:template match="entry[position() mod 2 = 0]" mode="section-description">
    <xsl:param name="section"/>

    <tr><td class="section-{$section}">
      <table width="100%" cellspacing="0">
        <tr><td colspan="2">
          <xsl:apply-templates/>
        </td></tr><tr><td width="20"></td><td>
          <xsl:value-of select="@description"/>
        </td></tr>
      </table>
    </td></tr>
  </xsl:template>

</xsl:stylesheet>
