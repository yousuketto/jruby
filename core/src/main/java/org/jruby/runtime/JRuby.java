/*
 **** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2013 The JRuby Team (jruby@jruby.org)
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.runtime;

import com.sun.tracing.Provider;
import com.sun.tracing.dtrace.FunctionName;

/**
  * Implementation of JRuby DTrace provider interface
  * 
  * This will define the DTrace probes that will exist in JRuby 
  */

public interface JRuby extends Provider {
    
    @FunctionName("org.jruby.parser.Parser.parse()") void parseBegin(String filename, int lineno);
    @FunctionName("org.jruby.parser.Parser.parse()") void parseEnd(String filename, int lineno);
    @FunctionName("org.jruby.RubyKernel.raise()") void raise(String classname, String filename, int lineno);
    @FunctionName("org.jruby.runtime.LoadService.load()") void loadEntry(String loadedFile, String fileName, int lineNo);
    @FunctionName("org.jruby.runtime.LoadService.load()") void loadReturn(String loadedFile, String fileName, int lineNo);
    @FunctionName("org.jruby.runtime.LoadService.findFileForLoad()") void findRequireEntry(String requiredFile, String fileName, int lineNo);
    @FunctionName("org.jruby.runtime.LoadService.findFileForLoad()") void findRequireReturn(String requiredFile, String fileName, int lineNo);
    @FunctionName("org.jruby.runtime.LoadService.require()") void requireEntry(String reqiredFile, String fileName, int lineNo);
    @FunctionName("org.jruby.runtime.LoadService.require()") void requireReturn(String requiredFile, String fileName, int lineNo);
    @FunctionName("org.jruby.internal.runtime.methods.DynamicMethod.call()") void methodEntry(String classname, String methodName, String fileName, int lineNo);
    @FunctionName("org.jruby.internal.runtime.methods.DynamicMethod.call()") void methodReturn(String classname, String methodName, String fileName, int lineNo);
    @FunctionName("org.jruby.RubyBasicObject()") void objectCreate(String className, String fileName, int lineNo);
    @FunctionName("org.jruby.RubyString()") void stringCreate(int length, String fileName, int lineNo);
    @FunctionName("org.jruby.RubyArray()") void arrayCreate(int length, String fileName, int lineNo);

}