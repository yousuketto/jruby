<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:import href="links.xsl"/>
  <xsl:import href="text.xsl"/>
  <xsl:import href="sectionindex.xsl"/>
  <xsl:import href="news.xsl"/>
  <xsl:import href="release.xsl"/>
  <xsl:import href="layout.xsl"/>
  <xsl:import href="team.xsl"/>
		
  <xsl:template match="page">
	  
    <!--DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"-->
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>JRuby - <xsl:value-of select="@title"/></title>
	<link rel="stylesheet" href="jruby.css" type="text/css"/>
      </head>
      <body>
        <table width="100%">
          <tbody>
          <tr><td width="290" rowspan="2">
            <img src="jruby_logo.jpg" width="285" height="110"/>
	  </td><td colspan="2" class="header_{@section}">
            <a href="index.html">Home</a> 
	    <xsl:for-each select="path/entry">
              <xsl:text disable-output-escaping="yes">
                &amp;nbsp;|&amp;nbsp;
	      </xsl:text>
	      <a href="{@link}">
		<xsl:value-of select="@name"/>
	      </a>
	    </xsl:for-each>
          </td></tr><tr><td colspan="2" class="header_{@section}">
            <h1><xsl:value-of select="@title"/></h1>
          </td></tr><tr><td valign="top" width="290">
            <small><div align="center">
              <xsl:text disable-output-escaping="yes">
		      &amp;copy; 2001-2002 by
	      </xsl:text>
	      <a href="mailto:jpetersen@uni-bonn.de">Jan Arne Petersen</a>
	    </div></small>
	    <xsl:apply-templates select="content/left"/>
	    <div valign="top"><small>Hosted by:
              <a href="http://sourceforge.net">
                <img src="http://sourceforge.net/sflogo.php?group_id=35413&amp;type=4" width="125" height="37" border="0" alt="SourceForge.net Logo"/>
              </a>
	    </small></div>
	  </td><td valign="top" width="50%">
	    <xsl:apply-templates select="content/center"/>
	  </td><td valign="top" width="*">
	    <xsl:apply-templates select="content/right"/>
	  </td></tr>
	</tbody>
        </table>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>
