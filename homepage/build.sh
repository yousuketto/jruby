#!/bin/sh

#CP=/opt/java/xalan/bin/xalan.jar:/opt/java/xalan/bin/xml-apis.jar:/opt/java/xalan/bin/xerces.jar
CP="-Duser.language=en -Duser.country=US"

echo "Transform pages:"
for i in pages/*.xml; do
   j=${i//#*\//}
   echo $i "=>" build/${j/\.xml/\.html}

   java $CP org.apache.xalan.xslt.Process -in $i -xsl xsl/main.xsl -out build/${j/\.xml/\.html} -HTML

done

echo "Generate News:"
for i in tmp/; do
   rm -f i
done
java $CP org.apache.xalan.xslt.Process -in data/news.xml -xsl xsl/news.xsl 2>error.log

for i in tmp/news*.xml; do
   j=${i//#*\//}
   echo $i "=>" build/${j/\.xml/\.html}

   java $CP org.apache.xalan.xslt.Process -in $i -xsl xsl/main.xsl -out build/${j/\.xml/\.html} -HTML
done

for i in images/*; do
    echo $i
    cp $i build/
done
