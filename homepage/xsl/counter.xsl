<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
		xmlns:counter="ACounter"
                extension-element-prefixes="counter"
                version="1.0">
  
  <lxslt:component prefix="counter" elements="init incr" functions="read, even">
    <lxslt:script lang="ruby">
      $counters = Hash.new;
      def init(xslproc, elem)
        name = elem.attribute "name";
        value = elem.attribute "value";
	$counters[name] = value.to_i;
        nil;
      end

      def read(name)
        return $counters[name];
      end

      def incr(xslproc, elem)
        name = elem.attribute "name";
        $counters[name] += 1;
        nil
      end

      def even(name)
        return $counters[name] % 2 == 0
      end
    </lxslt:script>
  </lxslt:component>
</xsl:stylesheet>
