<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!--xsl:template match="*|@*">
     <xsl:copy-of select="."/-->
  <!--/xsl:template-->

  <xsl:template match="text">
    <p>
      <xsl:copy-of select="child::node()"/>
    </p>
  </xsl:template>

  <xsl:template match="p">
    <xsl:if test="@name != ''">
    <h3>
      <xsl:value-of select="@name"/>
    </h3>
    </xsl:if>
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="b">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="li">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@name}"/>
  </xsl:template>
  
  <xsl:template match="align-right">
    <div align="right">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="h2">
    <h2>
      <xsl:apply-templates/>
    </h2>
  </xsl:template>

  <xsl:template match="link">
    <a>
      <xsl:attribute name="href">
	<xsl:value-of select="@href"/>
      </xsl:attribute>
      <xsl:value-of select="@name"/>
    </a>
  </xsl:template>

  <xsl:template match="link[@img]">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@href"/>
      </xsl:attribute>
      <img src="{@img}" alt="{@name}"/>
    </a>
  </xsl:template>
  
  <xsl:template match="list[@type='bullet']">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="item">
    <li>
      <xsl:value-of select="@name"/>
    </li>
  </xsl:template>
  
</xsl:stylesheet>
