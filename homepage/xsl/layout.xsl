<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="layout[@type='vertical']">
    <table width="100%">
      <tbody>
        <xsl:apply-templates/>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template match="entry">
    <tr><td>
      <xsl:apply-templates/>
    </td></tr>
  </xsl:template>
  
</xsl:stylesheet>
