echo "Generate JRuby homepage ..."

VELOCITY_PATH=/home/jpetersen/development/velocity/

_VELCP=.:$VELOCITY_PATH/velocity.jar

for i in $VELOCITY_PATH/build/lib/*.jar
do
    _VELCP=$_VELCP:"$i"
done

 
# convert the unix path to windows
#  [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
#   _VELCP=`cygpath --path --windows "$_VELCP"`
# 

java -cp $_VELCP org.jruby.homepage.PageGenerator jruby-template.vm jruby.xml menu.xml ./ 
 
