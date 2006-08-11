package org.jruby.promo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jruby.IRuby;
import org.jruby.Ruby;

public class TryJRubyServlet extends HttpServlet {
    private static final long serialVersionUID = 7876410281022107353L;
    
    private static final String script =
        "require 'irb'\n" +
        "IRB.start(__FILE__)\n";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String command = request.getParameter("cmd");
        
        if (command == null || command.equals("") || command.equals("!INIT!IRB!")) {
            // FIXME: teardown old runtime
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            final PrintStream printOut = new PrintStream(out, true);
            final PrintStream printErr = new PrintStream(err, true);
            PipedOutputStream inPipe = new PipedOutputStream();
            final PipedInputStream in = new PipedInputStream(inPipe);
            
            request.getSession().setAttribute("out", out);
            request.getSession().setAttribute("err", err);
            request.getSession().setAttribute("inPipe", inPipe);
            
            
            Thread runtimeThread = new Thread() {
                public void run() {
                    IRuby runtime = Ruby.newInstance(in, printOut, printErr);
                    runtime.defineGlobalConstant("ARGV", runtime.newArray());
                    runtime.getObject().setConstant("$VERBOSE", runtime.getNil());
                    runtime.getLoadService().init(new ArrayList());
                    runtime.evalScript(script);
                }
            };

            request.getSession().setAttribute("thread", runtimeThread);
            
            runtimeThread.start();

            writePage(response, out);
        } else {
            OutputStream inPipe = (OutputStream)request.getSession().getAttribute("inPipe");
            ByteArrayOutputStream out = (ByteArrayOutputStream)request.getSession().getAttribute("out");
            
            String commandOut = command + "\n";
            
            inPipe.write(commandOut.getBytes());
            out.write(commandOut.getBytes());
            
            writePage(response, out);
        }
    }

    private void writePage(HttpServletResponse response, ByteArrayOutputStream out) throws IOException {
        int length = out.size();
        while (out.size() == length) Thread.yield();
        
        ServletOutputStream responseOut = response.getOutputStream();
        
        responseOut.print("<html><head>\n");
        responseOut.print("<script type=\"text/javascript\">\nfunction setfocus()\n{\ndocument.forms[0].cmd.focus()\n}</script>");
        responseOut.print("<body onload=\"setfocus()\"><form action=\"/tryjruby/irb\"><input name=\"cmd\" type=\"text\" length=\"80\"/><br/><br/>");
        
        responseOut.print("<pre>\n");
        responseOut.write(out.toByteArray());
        responseOut.print("</pre></form>");
        responseOut.print("</body></html>");
    }

    public void init(ServletConfig config) throws ServletException {
        String jrubyHome = config.getInitParameter("JRUBY_HOME");
        String jrubyLib = jrubyHome + "/lib";
        String jrubyShell = "DISABLED";
        String jrubyScript = "DISABLED";
        
        System.setProperty("jruby.home", jrubyHome);
        System.setProperty("jruby.base", jrubyHome);
        System.setProperty("jruby.lib", jrubyLib);
        System.setProperty("jruby.shell", jrubyShell);
        System.setProperty("jruby.script", jrubyScript);
    }
}
