<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:counter="MyCounter"
                extension-element-prefixes="counter"
                version="1.0">

  <lxslt:component prefix="counter"
                   elements="init incr" functions="read">
    <lxslt:script lang="ruby">
      $counters = Hash.new

      def init (xslproc, elem)
        name = elem.attribute "name";
	value = elem.attribute("value").to_i;
	$counters[name] = value;
	nil
      end

      def read (name)
        $counters[name].to_s;
      end

      def incr (xslproc, elem)
        name = elem.attribute "name";
	$counters[name] += 1;
	nil
      end
    </lxslt:script>
  </lxslt:component>

  <xsl:template match="page">
    <!--DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"-->
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>JRuby - @title</title>
        <style>
        <!--
          a:active {
            color: black;
          }
	  a:link {
            color: black;
          }
          a:visited {
            color: black;
          }
        -->
        </style>
      </head>
      <body bgcolor="#99ccff">
        <table width="100%" cellspacing="0">
          <tr><td width="290" rowspan="2">
            <img src="jruby_logo.png" width=285 height=110>
          </td><td bgcolor="#0099ff">
        </table>
      </body>
  </xsl:template>
  
</xsl:stylesheet>
