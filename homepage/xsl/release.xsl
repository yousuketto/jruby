<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version="1.0">

  <xsl:template match="actual-releases">
    <xsl:variable name="releases" select="document('../data/releases.xml')/releases"/>

    <table width="100%">
      <tr><td bgcolor="#99b6ff">
        <h2>Actual Releases:</h2>
	
	<table width="100%" cellspacing="0">
          <xsl:apply-templates select="$releases/release"/>
        </table>
      </td></tr>
    </table>
  </xsl:template>

  <xsl:template match="release">
    <tr bgcolor="#89a4e5"><td>
      <xsl:value-of select="@name"/>
    </td><td>
      <xsl:value-of select="@version"/>
    </td></tr><tr><td>
      <xsl:value-of select="@date"/>
    </td><td>
      <xsl:choose>
        <xsl:when test="changelog[@link = '']">
          No resources yet.
        </xsl:when>
	<xsl:otherwise>
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="download/@link"/>
            </xsl:attribute>
            Download
	  </a><a>
            <xsl:attribute name="href">
              <xsl:value-of select="changelog/@link"/>
            </xsl:attribute>
            Changelog
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </td></tr>
  </xsl:template>
  
</xsl:stylesheet>
