<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="team-index">
    <xsl:variable name="team" select="document('../data/team.xml')/team"/>
    <xsl:variable name="version-split" select="$team/version-split"/>

    <p><ul>
      <li><a href="#admin">Administrators</a></li>
      <li><a href="#developer-new">Developer since <xsl:value-of select="$version-split"/> release</a></li>
      <li><a href="#developer-old">Developer up to <xsl:value-of select="$version-split"/> release</a></li>
      <li><a href="#bugs">Bug reports</a></li>
      <li><a href="#other">Others</a></li>
    </ul></p>
  </xsl:template>

  <xsl:template match="team-list">
    <xsl:variable name="team" select="document('../data/team.xml')/team"/>
    <xsl:variable name="version-split" select="$team/version-split"/>

    <table>
      <tr><td colspan="3">
      <a name="admin"/><h3>Administrators</h3>
      </td></tr>
      <xsl:apply-templates select="$team/person[@type='admin']"/>

      <tr><td colspan="3">
        <a name="developer-new"/><h3>Developer since <xsl:value-of select="$version-split"/> release:</h3>
      </td></tr>
      <xsl:apply-templates select="$team/person[@type='developer*']"/>

      <tr><td colspan="3">
        <a name="developer-old"/><h3>Developer up to <xsl:value-of select="$version-split"/> release:</h3>
      </td></tr>
      <xsl:apply-templates select="$team/person[@type='developer']"/>

      <tr><td colspan="3">
      <a name="bugs"/><h3>Bug reports</h3>
      </td></tr>
      <xsl:apply-templates select="$team/person[@type='bug']"/>
      
      <tr><td colspan="3">
      <a name="other"/><h3>Others</h3>
      </td></tr>
      <xsl:apply-templates select="$team/person[@type='other']"/>
    </table>

  </xsl:template>

  <xsl:template match="person">
     <tr><td width="25%"><xsl:value-of select="@name"/></td><td width="25%"><a href="mailto:{@email}"><xsl:value-of select="@email"/></a></td><td><xsl:value-of select="@comment"/></td></tr>
  </xsl:template>

</xsl:stylesheet>
