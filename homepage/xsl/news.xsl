<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	        xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.lib.Redirect"
                extension-element-prefixes="redirect"
		version="1.0">

  <xsl:template match="news-titles">
    <xsl:variable name="news_doc" select="document('../data/news.xml')"/>
    <xsl:variable name="news_count" select="count($news_doc/news/message)"/>
    <xsl:variable name="index" select="@index"/>
    <xsl:variable name="length" select="@length"/>

    <table width="100%">
    <tr><td bgcolor="#99b6ff">
        <xsl:apply-templates select="header"/>
	
	<table width="100%" cellspacing="0">
		<xsl:for-each select="$news_doc/news/message[(position() >= $index) and ((position() &lt;= ($length + $index)) or ($length = -1))]">
            <tr bgcolor="#89a4e5"><td width="30%">
              <xsl:value-of select="@date"/>
	    </td><td width="70%">
	      <xsl:value-of select="@title"/>
            </td></tr><tr><td colspan="2">
              <p>
                <xsl:value-of select="short"/>
              </p>
	      <div align="right"><a>
                <xsl:attribute name="href">
                  <xsl:value-of select="concat('news_', position(), '.html')"/>
                </xsl:attribute>
                More
	      </a></div>
            </td></tr>
          </xsl:for-each>
        </table>
	<xsl:apply-templates select="footer"/>
      </td></tr>
    </table>
  </xsl:template>

  <xsl:template match="news">
    <xsl:apply-templates select="message"/>
  </xsl:template>
  
  <xsl:template match="message">
    <redirect:write select="concat('../tmp/news_', position(), '.xml')">
      <page section="general">
        <xsl:attribute name="title">
          <xsl:value-of select="@title"/>
        </xsl:attribute>	      
        <path>
          <entry name="General" link="general.html"/>
          <entry name="News" link="news.html"/>
        </path>
        <content>
          <left>
            <layout type="vertical">
              <entry>
                <news-titles index="0" length="6">
                  <header>
                    <h2>Latest News:</h2>
                  </header>
		  <footer>
                    <align-right>
                      <link href="news.html" name="Old News"/>
		    </align-right>
		  </footer>
                </news-titles>
	      </entry>
            </layout>
	  </left><center>
	    <layout type="vertical">
              <entry>
	        <news-title date="{@date}" title="{@title}" short="{short}"/>
	      </entry><entry>
		<text>
                  <xsl:copy-of select="long"/>
	        </text>
	      </entry>
            </layout>
	  </center>
	  <right/>
        </content>
      </page>					      
    </redirect:write>
  </xsl:template>

  <xsl:template match="news-title">
    <table width="100%">
      <tr><td bgcolor="#99b6ff">
        <table width="100%" cellspacing="0">
          <tr bgcolor="#89a4e5"><td width="30%">
            <xsl:value-of select="@date"/>
          </td><td width="70%">
            <xsl:value-of select="@title"/>
          </td></tr><tr><td colspan="2">
            <p>
              <xsl:value-of select="@short"/>
            </p>
          </td></tr>
        </table>
      </td></tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
