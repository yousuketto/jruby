<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!--xsl:output method="xml" version="1.0" encoding="iso-8859-15" indent="yes"/-->

  <xsl:import href="links.xsl"/>
  <xsl:import href="text.xsl"/>
  <xsl:import href="sectionindex.xsl"/>
  <xsl:import href="news.xsl"/>
  <xsl:import href="release.xsl"/>
  <xsl:import href="layout.xsl"/>
  <xsl:import href="team.xsl"/>

  <xsl:output method="xml" version="1.0" encoding="iso-8859-15" indent="no"/>
		
  <xsl:template match="page">
	  
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
	  </td><td colspan="2" class="header-{@section}">
            <a href="index.html">Home</a> 
	    <xsl:for-each select="path/entry">
              <xsl:text disable-output-escaping="yes">
                &amp;nbsp;|&amp;nbsp;
	      </xsl:text>
	      <a href="{@link}">
		<xsl:value-of select="@name"/>
	      </a>
	    </xsl:for-each>
          </td></tr><tr><td colspan="2" class="header-{@section}">
            <h1><xsl:value-of select="@title"/></h1>
          </td></tr><tr><td valign="top" width="290">
            <small><div align="center">
              <xsl:text disable-output-escaping="yes">
		      (C) 2001-2002 by
	      </xsl:text>
	      <a href="mailto:jpetersen@uni-bonn.de">Jan Arne Petersen</a>
	    </div></small>
	    <xsl:apply-templates select="content/left"/>
	  </td><td valign="top" width="50%">
	    <xsl:apply-templates select="content/center"/>
	  </td><td valign="top" width="*">
	    <xsl:apply-templates select="content/right"/>
	  </td></tr><tr>
          <td colspan="3" align="right">
	    <xsl:apply-templates select="content/bottom"/>
	    <a href="http://sourceforge.net">
            <img src="http://sourceforge.net/sflogo.php?group_id=35413&amp;type=4"
	         width="125" height="37" border="0"
		 alt="SourceForge.net Logo"/>
            </a>
	    <a href="http://validator.w3.org/check/referer">
              <img border="0" src="http://www.w3.org/Icons/valid-html401"
	           alt="Valid HTML 4.01!" height="31" width="88"/>
            </a>
            <a href="http://jigsaw.w3.org/css-validator/">
              <img border="0" width="88" height="31"
                   src="http://jigsaw.w3.org/css-validator/images/vcss" 
                   alt="Valid CSS!"/>
            </a>
	  </td></tr>
        </tbody>
        </table>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>
