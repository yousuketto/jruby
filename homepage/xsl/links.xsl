<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!--xsl:template match="*|@*">
     <xsl:copy-of select="."/-->
  <!--/xsl:template-->

  <xsl:template match="lnk-JanArnePetersen">
    <xsl:call-template name="create-link">
      <xsl:with-param name="href">mailto:jpetersen@uni-bonn.de</xsl:with-param>
      <xsl:with-param name="name">Jan Arne Petersen</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="lnk-Ruby">
    <xsl:call-template name="create-link">
      <xsl:with-param name="href">http://www.ruby-lang.org</xsl:with-param>
      <xsl:with-param name="name">Ruby</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  
  <xsl:template name="create-link">
    <xsl:param name="href"/>
    <xsl:param name="name"/>
    <a>
      <xsl:attribute name="href">
	<xsl:value-of select="@href"/>
      </xsl:attribute>
      <xsl:value-of select="@name"/>
    </a>
  </xsl:template>
  
</xsl:stylesheet>
