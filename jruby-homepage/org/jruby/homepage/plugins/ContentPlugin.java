package org.jruby.homepage.plugins;

import java.io.*;
import org.apache.velocity.*;
import org.jruby.homepage.*;

public class ContentPlugin extends AbstractPlugin implements ContextPlugin {

    /*
     * @see ContextPlugin#execute(VelocityContext)
     */
    public void execute(VelocityContext context) {
        InputPlugin input = (InputPlugin) getPlugins().getPlugin(getProperty("input"));

        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader reader = new BufferedReader(input.getReader());

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            reader.close();
        } catch (IOException ioExcptn) {
            ioExcptn.printStackTrace();
        }

        context.put("content", sb.toString());
    }
}