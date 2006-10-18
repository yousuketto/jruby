// created by jay 1.0.2 (c) 2002-2004 ats@cs.rit.edu
// skeleton Java 1.0 (c) 2002 ats@cs.rit.edu

					// line 1 "DefaultRubyParser.y"

/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004-2006 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.parser;

import java.io.IOException;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.AssignableNode;
import org.jruby.ast.BackRefNode;
import org.jruby.ast.BeginNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.BlockPassNode;
import org.jruby.ast.BreakNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.CaseNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.DRegexpNode;
import org.jruby.ast.DStrNode;
import org.jruby.ast.DSymbolNode;
import org.jruby.ast.DXStrNode;
import org.jruby.ast.DefinedNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.DotNode;
import org.jruby.ast.EnsureNode;
import org.jruby.ast.EvStrNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.ForNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.NextNode;
import org.jruby.ast.NilNode;
import org.jruby.ast.Node;
import org.jruby.ast.NotNode;
import org.jruby.ast.OpAsgnAndNode;
import org.jruby.ast.OpAsgnNode;
import org.jruby.ast.OpAsgnOrNode;
import org.jruby.ast.OpElementAsgnNode;
import org.jruby.ast.PostExeNode;
import org.jruby.ast.RedoNode;
import org.jruby.ast.RegexpNode;
import org.jruby.ast.RescueBodyNode;
import org.jruby.ast.RescueNode;
import org.jruby.ast.RetryNode;
import org.jruby.ast.ReturnNode;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SValueNode;
import org.jruby.ast.ScopeNode;
import org.jruby.ast.SelfNode;
import org.jruby.ast.SplatNode;
import org.jruby.ast.StarNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.ToAryNode;
import org.jruby.ast.UndefNode;
import org.jruby.ast.UntilNode;
import org.jruby.ast.VAliasNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.WhenNode;
import org.jruby.ast.WhileNode;
import org.jruby.ast.XStrNode;
import org.jruby.ast.YieldNode;
import org.jruby.ast.ZArrayNode;
import org.jruby.ast.ZSuperNode;
import org.jruby.ast.ZeroArgNode;
import org.jruby.ast.types.ILiteralNode;
import org.jruby.ast.types.INameNode;
import org.jruby.common.IRubyWarnings;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.lexer.yacc.ISourcePositionHolder;
import org.jruby.lexer.yacc.LexState;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.RubyYaccLexer;
import org.jruby.lexer.yacc.StrTerm;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.lexer.yacc.Token;
import org.jruby.runtime.Visibility;
import org.jruby.util.IdUtil;

public class DefaultRubyParser {
    private ParserSupport support;
    private RubyYaccLexer lexer;
    private IRubyWarnings warnings;

    public DefaultRubyParser() {
        support = new ParserSupport();
        lexer = new RubyYaccLexer();
        lexer.setParserSupport(support);
    }

    public void setWarnings(IRubyWarnings warnings) {
        this.warnings = warnings;

        support.setWarnings(warnings);
        lexer.setWarnings(warnings);
    }
					// line 150 "-"
  // %token constants
  public static final int kCLASS = 257;
  public static final int kMODULE = 258;
  public static final int kDEF = 259;
  public static final int kUNDEF = 260;
  public static final int kBEGIN = 261;
  public static final int kRESCUE = 262;
  public static final int kENSURE = 263;
  public static final int kEND = 264;
  public static final int kIF = 265;
  public static final int kUNLESS = 266;
  public static final int kTHEN = 267;
  public static final int kELSIF = 268;
  public static final int kELSE = 269;
  public static final int kCASE = 270;
  public static final int kWHEN = 271;
  public static final int kWHILE = 272;
  public static final int kUNTIL = 273;
  public static final int kFOR = 274;
  public static final int kBREAK = 275;
  public static final int kNEXT = 276;
  public static final int kREDO = 277;
  public static final int kRETRY = 278;
  public static final int kIN = 279;
  public static final int kDO = 280;
  public static final int kDO_COND = 281;
  public static final int kDO_BLOCK = 282;
  public static final int kRETURN = 283;
  public static final int kYIELD = 284;
  public static final int kSUPER = 285;
  public static final int kSELF = 286;
  public static final int kNIL = 287;
  public static final int kTRUE = 288;
  public static final int kFALSE = 289;
  public static final int kAND = 290;
  public static final int kOR = 291;
  public static final int kNOT = 292;
  public static final int kIF_MOD = 293;
  public static final int kUNLESS_MOD = 294;
  public static final int kWHILE_MOD = 295;
  public static final int kUNTIL_MOD = 296;
  public static final int kRESCUE_MOD = 297;
  public static final int kALIAS = 298;
  public static final int kDEFINED = 299;
  public static final int klBEGIN = 300;
  public static final int klEND = 301;
  public static final int k__LINE__ = 302;
  public static final int k__FILE__ = 303;
  public static final int tIDENTIFIER = 304;
  public static final int tFID = 305;
  public static final int tGVAR = 306;
  public static final int tIVAR = 307;
  public static final int tCONSTANT = 308;
  public static final int tCVAR = 309;
  public static final int tNTH_REF = 310;
  public static final int tBACK_REF = 311;
  public static final int tSTRING_CONTENT = 312;
  public static final int tINTEGER = 313;
  public static final int tFLOAT = 314;
  public static final int tREGEXP_END = 315;
  public static final int tUPLUS = 316;
  public static final int tUMINUS = 317;
  public static final int tUMINUS_NUM = 318;
  public static final int tPOW = 319;
  public static final int tCMP = 320;
  public static final int tEQ = 321;
  public static final int tEQQ = 322;
  public static final int tNEQ = 323;
  public static final int tGEQ = 324;
  public static final int tLEQ = 325;
  public static final int tANDOP = 326;
  public static final int tOROP = 327;
  public static final int tMATCH = 328;
  public static final int tNMATCH = 329;
  public static final int tDOT = 330;
  public static final int tDOT2 = 331;
  public static final int tDOT3 = 332;
  public static final int tAREF = 333;
  public static final int tASET = 334;
  public static final int tLSHFT = 335;
  public static final int tRSHFT = 336;
  public static final int tCOLON2 = 337;
  public static final int tCOLON3 = 338;
  public static final int tOP_ASGN = 339;
  public static final int tASSOC = 340;
  public static final int tLPAREN = 341;
  public static final int tLPAREN2 = 342;
  public static final int tRPAREN = 343;
  public static final int tLPAREN_ARG = 344;
  public static final int tLBRACK = 345;
  public static final int tRBRACK = 346;
  public static final int tLBRACE = 347;
  public static final int tLBRACE_ARG = 348;
  public static final int tSTAR = 349;
  public static final int tSTAR2 = 350;
  public static final int tAMPER = 351;
  public static final int tAMPER2 = 352;
  public static final int tTILDE = 353;
  public static final int tPERCENT = 354;
  public static final int tDIVIDE = 355;
  public static final int tPLUS = 356;
  public static final int tMINUS = 357;
  public static final int tLT = 358;
  public static final int tGT = 359;
  public static final int tPIPE = 360;
  public static final int tBANG = 361;
  public static final int tCARET = 362;
  public static final int tLCURLY = 363;
  public static final int tRCURLY = 364;
  public static final int tBACK_REF2 = 365;
  public static final int tSYMBEG = 366;
  public static final int tSTRING_BEG = 367;
  public static final int tXSTRING_BEG = 368;
  public static final int tREGEXP_BEG = 369;
  public static final int tWORDS_BEG = 370;
  public static final int tQWORDS_BEG = 371;
  public static final int tSTRING_DBEG = 372;
  public static final int tSTRING_DVAR = 373;
  public static final int tSTRING_END = 374;
  public static final int tLOWEST = 375;
  public static final int tLAST_TOKEN = 376;
  public static final int yyErrorCode = 256;

  /** number of final state.
    */
  protected static final int yyFinal = 1;

  /** parser tables.
      Order is mandated by <i>jay</i>.
    */
  protected static final short[] yyLhs = {
//yyLhs 495
    -1,    99,     0,    20,    19,    21,    21,    21,    21,   102,
    22,    22,    22,    22,    22,    22,    22,    22,    22,    22,
   103,    22,    22,    22,    22,    22,    22,    22,    22,    22,
    22,    22,    22,    22,    22,    23,    23,    23,    23,    23,
    23,    27,    18,    18,    18,    18,    18,    43,    43,    43,
   104,    78,    26,    26,    26,    26,    26,    26,    26,    26,
    79,    79,    81,    81,    80,    80,    80,    80,    80,    80,
    51,    51,    67,    67,    52,    52,    52,    52,    52,    52,
    52,    52,    59,    59,    59,    59,    59,    59,    59,    59,
    91,    91,    17,    17,    17,    92,    92,    92,    92,    92,
    85,    85,    47,   106,    47,    93,    93,    93,    93,    93,
    93,    93,    93,    93,    93,    93,    93,    93,    93,    93,
    93,    93,    93,    93,    93,    93,    93,    93,    93,    93,
    93,   105,   105,   105,   105,   105,   105,   105,   105,   105,
   105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
   105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
   105,   105,   105,   105,   105,   105,   105,   105,   105,   105,
   105,   105,    24,    24,    24,    24,    24,    24,    24,    24,
    24,    24,    24,    24,    24,    24,    24,    24,    24,    24,
    24,    24,    24,    24,    24,    24,    24,    24,    24,    24,
    24,    24,    24,    24,    24,    24,    24,    24,    24,    24,
    24,    24,    24,   108,    24,    24,    24,    53,    56,    56,
    56,    56,    56,    56,    37,    37,    37,    37,    41,    41,
    33,    33,    33,    33,    33,    33,    33,    33,    33,    34,
    34,    34,    34,    34,    34,    34,    34,    34,    34,    34,
    34,   110,    39,    35,   111,    35,   112,    35,    72,    71,
    71,    65,    65,    50,    50,    50,    25,    25,    25,    25,
    25,    25,    25,    25,    25,    25,   113,    25,    25,    25,
    25,    25,    25,    25,    25,    25,    25,    25,   114,    25,
    25,    25,    25,    25,    25,   116,   118,    25,   119,   120,
    25,    25,    25,    25,   121,   122,    25,   123,    25,   125,
   126,    25,   127,    25,   128,    25,   129,   130,    25,    25,
    25,    25,    25,    28,   115,   115,   115,   115,   117,   117,
   117,    31,    31,    29,    29,    57,    57,    58,    58,    58,
    58,   131,    77,    42,    42,    42,    10,    10,    10,    10,
    10,    10,   132,    76,   133,    76,    54,    66,    66,    66,
    30,    30,    82,    82,    55,    55,    55,    32,    32,    36,
    36,    14,    14,    14,     2,     3,     3,     4,     5,     6,
    11,    11,    62,    62,    13,    13,    12,    12,    61,    61,
     7,     7,     8,     8,     9,   134,     9,   135,     9,    48,
    48,    48,    48,    87,    86,    86,    86,    86,    16,    15,
    15,    15,    15,    84,    84,    84,    84,    84,    84,    84,
    84,    84,    84,    84,    40,    83,    49,    49,    38,   136,
    38,    38,    44,    44,    45,    45,    45,    45,    45,    45,
    45,    45,    45,    94,    94,    94,    94,    63,    63,    46,
    64,    64,    97,    97,    95,    95,    98,    98,    75,    74,
    74,     1,   137,     1,    70,    70,    70,    68,    68,    69,
    88,    88,    88,    89,    89,    89,    89,    90,    90,    90,
    96,    96,   100,   100,   107,   107,   109,   109,   109,   124,
   124,   101,   101,    60,    73,
    }, yyLen = {
//yyLen 495
     2,     0,     2,     4,     2,     1,     1,     3,     2,     0,
     4,     3,     3,     3,     2,     3,     3,     3,     3,     3,
     0,     5,     4,     3,     3,     3,     6,     5,     5,     5,
     3,     3,     3,     3,     1,     1,     3,     3,     2,     2,
     1,     1,     1,     1,     2,     2,     2,     1,     4,     4,
     0,     5,     2,     3,     4,     5,     4,     5,     2,     2,
     1,     3,     1,     3,     1,     2,     3,     2,     2,     1,
     1,     3,     2,     3,     1,     4,     3,     3,     3,     3,
     2,     1,     1,     4,     3,     3,     3,     3,     2,     1,
     1,     1,     2,     1,     3,     1,     1,     1,     1,     1,
     1,     1,     1,     0,     4,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     3,     5,     3,     6,     5,     5,     5,     5,
     4,     3,     3,     3,     3,     3,     3,     3,     3,     3,
     4,     4,     2,     2,     3,     3,     3,     3,     3,     3,
     3,     3,     3,     3,     3,     3,     3,     2,     2,     3,
     3,     3,     3,     0,     4,     5,     1,     1,     1,     2,
     2,     5,     2,     3,     3,     4,     4,     6,     1,     1,
     1,     2,     5,     2,     5,     4,     7,     3,     1,     4,
     3,     5,     7,     2,     5,     4,     6,     7,     9,     3,
     1,     0,     2,     1,     0,     3,     0,     4,     2,     2,
     1,     1,     3,     3,     4,     2,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     3,     0,     5,     3,     3,
     2,     4,     3,     3,     1,     4,     3,     1,     0,     6,
     2,     1,     2,     6,     6,     0,     0,     7,     0,     0,
     7,     5,     4,     5,     0,     0,     9,     0,     6,     0,
     0,     8,     0,     5,     0,     6,     0,     0,     9,     1,
     1,     1,     1,     1,     1,     1,     1,     2,     1,     1,
     1,     1,     5,     1,     2,     1,     1,     1,     2,     1,
     3,     0,     5,     2,     4,     4,     2,     4,     4,     3,
     2,     1,     0,     5,     0,     5,     5,     1,     4,     2,
     1,     1,     6,     0,     1,     1,     1,     2,     1,     2,
     1,     1,     1,     1,     1,     1,     2,     3,     3,     3,
     3,     3,     0,     3,     1,     2,     3,     3,     0,     3,
     0,     2,     0,     2,     1,     0,     3,     0,     4,     1,
     1,     1,     1,     2,     1,     1,     1,     1,     3,     1,
     1,     2,     2,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
     4,     2,     4,     2,     6,     4,     4,     2,     4,     2,
     2,     1,     0,     1,     1,     1,     1,     1,     3,     3,
     1,     3,     1,     1,     2,     1,     1,     1,     2,     2,
     0,     1,     0,     5,     1,     2,     2,     1,     3,     3,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     0,     1,     0,     1,     0,     1,     1,     1,
     1,     1,     2,     0,     0,
    }, yyDefRed = {
//yyDefRed 886
     1,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   295,   298,     0,     0,     0,   321,   322,     0,
     0,     0,   419,   418,   420,   421,     0,     0,     0,    20,
     0,   423,   422,     0,     0,   415,   414,     0,   417,   426,
   427,   409,   410,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   390,   392,   392,     0,     0,
   267,     0,   375,   268,   269,     0,   270,   271,   266,   371,
   373,    35,     2,     0,     0,     0,     0,     0,     0,     0,
   272,     0,    43,     0,     0,    70,     0,     5,     0,     0,
    60,     0,     0,   372,     0,     0,   319,   320,   284,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   323,
     0,   273,   424,     0,    93,   312,   140,   151,   141,   164,
   137,   157,   147,   146,   162,   145,   144,   139,   165,   149,
   138,   152,   156,   158,   150,   143,   159,   166,   161,     0,
     0,     0,     0,   136,   155,   154,   167,   168,   169,   170,
   171,   135,   142,   133,   134,     0,     0,     0,    97,     0,
   126,   127,   124,   108,   109,   110,   113,   115,   111,   128,
   129,   116,   117,   462,   121,   120,   107,   125,   123,   122,
   118,   119,   114,   112,   105,   106,   130,     0,   461,   314,
    98,    99,   160,   153,   163,   148,   131,   132,    95,    96,
     0,     0,   102,   101,   100,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   490,   489,     0,     0,
     0,   491,     0,     0,     0,     0,     0,     0,   335,   336,
     0,     0,     0,     0,     0,   230,    45,     0,     0,     0,
   467,   238,    46,    44,     0,    59,     0,     0,   350,    58,
    38,     0,     9,   485,     0,     0,     0,   192,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   218,     0,     0,   464,     0,     0,     0,     0,     0,
     0,    68,     0,   208,    39,   207,   406,   405,   407,     0,
   403,   404,     0,     0,     0,     0,     0,     0,     0,   376,
   354,   352,   292,     4,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   341,   343,
     0,     0,     0,     0,     0,     0,    72,     0,     0,     0,
     0,     0,     0,   346,     0,   290,     0,   411,   412,     0,
    90,     0,    92,     0,   429,   307,   428,     0,     0,     0,
     0,     0,   480,   481,   316,     0,   103,     0,     0,   275,
     0,   326,   325,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   492,     0,     0,     0,
     0,     0,     0,   304,     0,   258,     0,     0,   231,   260,
     0,   233,   286,     0,     0,   253,   252,     0,     0,     0,
     0,     0,    11,    13,    12,     0,   288,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   278,     0,     0,     0,
   219,   282,     0,   487,   220,     0,   222,     0,   466,   465,
   283,     0,     0,     0,     0,   394,   397,   395,   408,   393,
   377,   391,   378,   379,   380,   381,   384,     0,   386,     0,
   387,     0,     0,     0,    15,    16,    17,    18,    19,    36,
    37,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   475,
     0,     0,   476,     0,     0,     0,     0,   349,     0,     0,
   473,   474,     0,     0,    30,     0,     0,    23,     0,    31,
   261,     0,     0,    66,    73,    24,    33,     0,    25,     0,
    50,    53,     0,   431,     0,     0,     0,     0,     0,     0,
    94,     0,     0,     0,     0,     0,   444,   443,   445,     0,
   453,   452,   457,   456,     0,     0,   450,     0,     0,   441,
   447,     0,     0,     0,     0,   365,     0,     0,   366,     0,
     0,   333,     0,   327,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   302,   330,   329,   296,
   328,   299,     0,     0,     0,     0,     0,     0,     0,   237,
   469,     0,     0,     0,   259,     0,     0,   468,   285,     0,
     0,   256,     0,     0,   250,     0,     0,     0,     0,     0,
   224,     0,    10,     0,     0,     0,    22,     0,     0,     0,
     0,     0,   223,     0,   262,     0,     0,     0,     0,     0,
     0,     0,   383,   385,   389,   339,     0,     0,   337,     0,
     0,     0,     0,     0,   229,     0,   347,   228,     0,     0,
   348,     0,     0,    48,   344,    49,   345,   265,     0,     0,
    71,     0,   310,     0,     0,   281,   313,     0,   317,     0,
     0,     0,   433,     0,   437,     0,   439,     0,   440,   454,
   458,   104,     0,     0,   368,   334,     0,     3,   370,     0,
   331,     0,     0,     0,     0,     0,     0,   301,   303,   359,
     0,     0,     0,     0,     0,     0,     0,     0,   235,     0,
     0,     0,     0,     0,   243,   255,   225,     0,     0,   226,
     0,     0,     0,    21,   277,     0,     0,     0,   399,   400,
   401,   396,   402,   338,     0,     0,     0,     0,     0,    27,
     0,    28,     0,    55,    29,     0,     0,    57,     0,     0,
     0,     0,     0,     0,   430,   308,   463,     0,   449,     0,
   315,     0,   459,   448,     0,     0,   451,     0,     0,     0,
     0,   367,     0,     0,   369,     0,   293,     0,   294,     0,
     0,     0,     0,   305,   232,     0,   234,   249,   257,     0,
     0,     0,   240,     0,     0,   289,   221,   398,   340,   355,
   353,   342,    26,     0,   264,     0,     0,     0,   432,     0,
   435,   436,   438,     0,     0,     0,     0,     0,     0,   358,
   360,   356,   361,   297,   300,     0,     0,     0,     0,   239,
     0,   245,     0,   227,    51,   311,     0,     0,     0,     0,
     0,     0,     0,   362,     0,     0,   236,   241,     0,     0,
     0,   244,   318,   434,     0,   332,   306,     0,     0,   246,
     0,   242,     0,   247,     0,   248,
    }, yyDgoto = {
//yyDgoto 138
     1,   187,    60,    61,    62,    63,    64,   292,   289,   459,
    65,    66,    67,   467,    68,    69,    70,   108,    71,   205,
   206,    73,    74,    75,    76,    77,    78,   209,   258,   710,
   841,   711,   703,   236,   621,   416,   707,   664,   365,   245,
    80,   666,    81,    82,   564,   565,   566,   201,   751,   211,
   529,    84,    85,   237,   395,   577,   270,   227,   657,   212,
    87,   298,   296,   567,   568,   272,   595,    88,   273,   240,
   277,   408,   614,   409,   694,   782,   355,   339,   541,    89,
    90,   266,   378,   213,   214,   202,   290,    93,   113,   546,
   517,   114,   204,   512,   570,   571,   374,   572,   573,     2,
   219,   220,   425,   255,   681,   191,   574,   254,   427,   444,
   246,   625,   731,   438,   633,   383,   222,   599,   722,   223,
   723,   607,   845,   545,   384,   542,   773,   370,   375,   554,
   777,   507,   472,   471,   651,   650,   544,   371,
    }, yySindex = {
//yySindex 886
     0,     0,  3855,  5293, 16166, 16511, 17076, 16966,  3855,  4849,
  4849,  4732,     0,     0, 16281, 13636, 13636,     0,     0, 13636,
  -179,  -162,     0,     0,     0,     0,  4849, 16856,   201,     0,
  -137,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0, 15821, 15821,   -36,   -64,  4335,  4849, 15016,
 15821, 16626, 15821, 15936, 17185,     0,     0,     0,   224,   235,
     0,   -87,     0,     0,     0,  -207,     0,     0,     0,     0,
     0,     0,     0,    55,   749,  -183,  3280,     0,    43,   -43,
     0,  -148,     0,   -57,   241,     0,   273,     0, 16396,   288,
     0,    12,     0,     0,  -219,   749,     0,     0,     0,  -179,
  -162,   201,     0,     0,   -18,  4849,   -47,  3855,    23,     0,
   150,     0,     0,  -219,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  -140,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
 17185,   316,     0,     0,     0,   102,   183,    88,  -183,   111,
   386,   124,   428,   157,     0,   111,     0,     0,    55,   -65,
   459,     0,  4849,  4849,   243,   400,     0,   230,     0,     0,
     0, 15821, 15821, 15821,  3280,     0,     0,   185,   508,   511,
     0,     0,     0,     0, 13406,     0, 13751, 13636,     0,     0,
     0,   282,     0,     0,   215,   195,  3855,     0,   450,   250,
   253,   254,   221,  4335,   234,     0,   242,  -183, 15821,   201,
   229,     0,   118,   127,     0,   139,   127,   227,   291,   451,
     0,     0,     0,     0,     0,     0,     0,     0,     0,  -206,
     0,     0,  -188,  -159,   286,   236,  -152,   238,  -236,     0,
     0,     0,     0,     0, 13291,  4849,  4849,  4849,  4849,  5293,
  4849,  4849, 15821, 15821, 15821, 15821, 15821, 15821, 15821, 15821,
 15821, 15821, 15821, 15821, 15821, 15821, 15821, 15821, 15821, 15821,
 15821, 15821, 15821, 15821, 15821, 15821, 15821, 15821,     0,     0,
  5886,  9216, 15016, 17353, 17353, 15936,     0, 15131,  4335, 16626,
   573, 15131, 15936,     0,   271,     0,   215,     0,     0,  -183,
     0,     0,     0,    55,     0,     0,     0, 17353, 17412, 15016,
  3855,  4849,     0,     0,     0,  1118,     0, 15246,   356,     0,
   221,     0,     0,  3855,   359, 17471, 17530, 15016, 15821, 15821,
 15821,  3855,   352,  3855, 15361,   365,     0,    99,    99,     0,
 17589, 17648, 15016,     0,   588,     0, 15821, 13866,     0,     0,
 13981,     0,     0,   293, 13521,     0,     0,    43,   201,    40,
   303,   605,     0,     0,     0, 16966,     0, 15821,  3855,   287,
 17471, 17530, 15821, 15821, 15821,   309,     0,     0,   201,  2744,
     0,     0, 15476,     0,     0, 15821,     0, 15821,     0,     0,
     0,     0, 17707, 17766, 15016,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,    21,     0,   624,
     0,  -173,  -173,   749,     0,     0,     0,     0,     0,     0,
     0,   250,  2456,  2456,  2456,  2456,  1967,  1967,  2849,  2379,
  2456,  2456,  2323,  2323,   312,   312,   250,  1079,   250,   250,
   211,   211,  1967,  1967,   602,   602,  5142,  -173,   318,     0,
   321,  -162,     0,   326,     0,   336,  -162,     0,     0,   331,
     0,     0,  -162,  -162,     0,  3280, 15821,     0,  2915,     0,
     0,   634,   338,     0,     0,     0,     0,     0,     0,  3280,
     0,     0,    55,     0,  4849,  3855,  -162,     0,     0,  -162,
     0,   339,   420,   138, 17294,   626,     0,     0,     0,  1233,
     0,     0,     0,     0,  3855,    55,     0,   644,   645,     0,
     0,   646,   388,   389, 16966,     0,     0,   360,     0,  3855,
   431,     0,    89,     0,   357,   367,   368,   336,   362,  2915,
   356,   446,   447, 15821,   670,   111,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   375,  4849,   373,     0,
     0, 15821,   185,   681,     0, 15821,   185,     0,     0, 15821,
  3280,     0,    -5,   682,     0,   385,   391, 17353, 17353,   395,
     0, 14096,     0,  4849,  3280,   378,     0,   250,   250,  3280,
     0,   396,     0, 15821,     0,     0,     0,     0,     0,   397,
  3855,   328,     0,     0,     0,     0,  3409,  3855,     0,  3855,
 15821,  3855, 15936, 15936,     0,   271,     0,     0, 15936, 15821,
     0,   271,   392,     0,     0,     0,     0,     0, 15821, 15591,
     0,  -173,     0,    55,   468,     0,     0,   401,     0, 15821,
   201,   482,     0,  1233,     0,   232,     0,   162,     0,     0,
     0,     0, 16741,   111,     0,     0,  3855,     0,     0,  4849,
     0,   486, 15821, 15821, 15821,   408,   493,     0,     0,     0,
 15706,  3855,  3855,  3855,     0,    99,   588, 14211,     0,   588,
   588,   415, 14326, 14441,     0,     0,     0,  -162,  -162,     0,
    43,    40,  -178,     0,     0,  2744,     0,   406,     0,     0,
     0,     0,     0,     0,   399,   496,   410,  3280,   519,     0,
  3280,     0,  3280,     0,     0,  3280,  3280,     0, 15936,  3280,
 15821,     0,  3855,  3855,     0,     0,     0,  1118,     0,   448,
     0,   745,     0,     0,   646,   626,     0,   646,   487,   527,
     0,     0,     0,  3855,     0,   111,     0, 15821,     0, 15821,
   -46,   529,   539,     0,     0, 15821,     0,     0,     0, 15821,
   763,   766,     0, 15821,   471,     0,     0,     0,     0,     0,
     0,     0,     0,  3280,     0,   452,   555,  3855,     0,   232,
     0,     0,     0,     0, 17825, 17884, 15016,   102,  3855,     0,
     0,     0,     0,     0,     0,  3855,  4184,   588, 14556,     0,
 14671,     0,   588,     0,     0,     0,   567,   646,     0,     0,
     0,     0,   488,     0,    89,   572,     0,     0, 15821,   793,
 15821,     0,     0,     0,     0,     0,     0,   588, 14786,     0,
   588,     0, 15821,     0,   588,     0,
    }, yyRindex = {
//yyRindex 886
     0,     0,   133,     0,     0,     0,     0,     0,   726,     0,
     0,   -20,     0,     0,     0,  7575,  7680,     0,     0,  7789,
  4606,  4011,     0,     0,     0,     0,     0,     0,  5818,     0,
     0,     0,     0,  2086,  3166,     0,     0,  2206,     0,     0,
     0,     0,     0,     0,     0,     0,     0,    57,     0,   495,
   475,   132,     0,     0,  -100,     0,     0,     0,   -82,  -215,
     0,  6871,     0,     0,     0,  6976,     0,     0,     0,     0,
     0,     0,     0,   260,   549,  1443,  1307,  7085,  8857,     0,
     0, 11366,     0,  8839,     0,     0,     0,     0,   147,     0,
     0,     0,  8722,     0, 14901,   741,     0,     0,     0,  7224,
  6006,   500, 12357, 12475,     0,     0,     0,    57,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   676,
  1783,  1863,  2448,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,  2813,  3235,  3246,     0,  3715,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,  1774,     0,     0,     0,   284,     0,     0, 11949,     0,
     0,  6520,     0,     0,  6109,     0,     0,     0,   575,     0,
    -6,     0,     0,     0,     0,     0,   570,     0,     0,     0,
   732,     0,     0,     0, 11177,     0,     0, 12005,  2328,  2328,
     0,     0,     0,     0,     0,     0,     0,   504,     0,     0,
     0,     0,     0,     0, 16051,     0,    42,     0,     0,  7909,
  7327,  7438,  8948,    57,     0,     5,     0,    39,     0,   497,
     0,     0,   506,   506,     0,   492,   492,     0,     0,     0,
   550,     0,  1333,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   606,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,   495,     0,     0,     0,     0,     0,    57,   174,
   203,     0,     0,     0,  6460,     0,     0,     0,     0,   122,
     0, 12825,     0,     0,     0,     0,     0,     0,     0,   495,
   726,     0,     0,     0,     0,   137,     0,    26,    76,     0,
  6623,     0,     0,   509, 12941,     0,     0,   495,     0,     0,
     0,   528,     0,   191,     0,     0,     0,     0,     0,   807,
     0,     0,   495,     0,  2328,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   522,     0,     0,    80,   523,   523,
     0,    92,     0,     0,     0,     0,     0,     0,    42,     0,
     0,     0,     0,     0,     0,     0,     0,    74,   523,   497,
     0,     0,   516,     0,     0,  -155,     0,   512,     0,     0,
     0,  1436,     0,     0,   495,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0, 13057, 13175,   906,     0,     0,     0,     0,     0,     0,
     0,  8018,  1961, 10468, 10591, 10685, 10012, 10134, 10775, 11050,
 10866, 10960,  6357, 11141,  9453,  9557,  8123,  9668,  8260,  8371,
  9146,  9331, 10252, 10351,  9797,  9908,     0, 13057,  4969,     0,
  5084,  4126,     0,  5449,  3531,  5564, 14901,     0,  3646,     0,
     0,     0,  5691,  5691,     0, 11267,     0,     0,   786,     0,
     0,     0,     0,     0,     0,     0,     0,  1717,     0, 11305,
     0,     0,     0,     0,     0,   726,  6213, 12591, 12707,     0,
     0,     0,     0,   523,     0,    67,     0,     0,     0,   100,
     0,     0,     0,     0,   726,     0,     0,    71,    71,     0,
     0,    71,    81,     0,     0,     0,   117,   136,     0,   561,
   609,     0,   609,     0,  2571,  2686,  3051,  4491,     0, 12097,
   609,     0,     0,     0,   141,     0,     0,     0,     0,     0,
     0,     0,   611,   885,   929,   237,     0,     0,     0,     0,
     0,     0, 12132,  2328,     0,     0,     0,     0,     0,     0,
   154,     0,     0,   535,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 11417,     0,     0,  8474,  8597, 11453,
    28,     0,     0,     0,     0,  1360,  1613,  1839,  1220,     0,
    42,     0,     0,     0,     0,     0,     0,   191,     0,    42,
     0,   191,     0,     0,     0, 12219,     0,     0,     0,     0,
     0, 12261,  9024,     0,     0,     0,     0,     0,     0,     0,
     0, 13175,     0,     0,     0,     0,     0,     0,     0,     0,
   523,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   191,     0,     0,     0,
     0,     0,     0,     0,     0,  6734,     0,     0,     0,     0,
     0,   217,   191,   191,   975,     0,  2328,     0,     0,  2328,
   535,     0,     0,     0,     0,     0,     0,   121,   121,     0,
     0,   523,     0,     0,     0,   497,  1901,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0, 11545,     0,     0,
 11581,     0, 11693,     0,     0, 11729, 11821,     0,     0, 11857,
     0,  1567,    42,   726,     0,     0,     0,   137,     0,     0,
     0,    71,     0,     0,    71,     0,     0,    71,     0,     0,
   119,     0,   442,   726,     0,     0,     0,     0,     0,     0,
   609,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   535,   535,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0, 11969,     0,     0,     0,   726,     0,     0,
     0,     0,     0,   485,     0,     0,   495,   284,   509,     0,
     0,     0,     0,     0,     0,   191,  2328,   535,     0,     0,
     0,     0,   535,     0,     0,     0,     0,    71,    83,   909,
  1065,   265,     0,     0,   609,     0,     0,     0,     0,   535,
     0,     0,     0,     0,   905,     0,     0,   535,     0,     0,
   535,     0,     0,     0,   535,     0,
    }, yyGindex = {
//yyGindex 138
     0,     0,     0,     0,   821,     0,     0,     0,   483,  -196,
     0,     0,     0,     0,     0,     0,     0,   879,    86,   222,
  -357,     0,   170,  1337,   -15,    27,    -4,    36,   457,  -352,
     0,    22,     0,   614,     0,     0,     0,   -14,     0,     4,
   893,  -454,  -226,     0,   131,   345,  -624,     0,     0,   668,
  -232,   826,     3,  1464,  -378,     0,  -299,   261,  -415,  1096,
  1426,     0,     0,     0,   216,     8,     0,     0,   -10,  -366,
     0,   532,     1,     0,   354,  -356,   853,     0,  -432,   -12,
   -17,  -169,    90,  1196,  1027,   -24,     0,    15,   886,  -281,
     0,   -42,     2,    68,   233,  -559,     0,     0,     0,     0,
    10,   847,     0,     0,     0,     0,     0,   -50,     0,    13,
     0,     0,     0,     0,     0,  -205,     0,  -380,     0,     0,
     0,     0,     0,     0,    44,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,
    };
    protected static final short[] yyTable = YyTables.yyTable();
    protected static final short[] yyCheck = YyTables.yyCheck();

  /** maps symbol value to printable name.
      @see #yyExpecting
    */
  protected static final String[] yyNames = {
    "end-of-file",null,null,null,null,null,null,null,null,null,"'\\n'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,"' '",null,null,null,null,null,
    null,null,null,null,null,null,"','",null,null,null,null,null,null,
    null,null,null,null,null,null,null,"':'","';'",null,"'='",null,"'?'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "'['",null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "kCLASS","kMODULE","kDEF","kUNDEF","kBEGIN","kRESCUE","kENSURE",
    "kEND","kIF","kUNLESS","kTHEN","kELSIF","kELSE","kCASE","kWHEN",
    "kWHILE","kUNTIL","kFOR","kBREAK","kNEXT","kREDO","kRETRY","kIN",
    "kDO","kDO_COND","kDO_BLOCK","kRETURN","kYIELD","kSUPER","kSELF",
    "kNIL","kTRUE","kFALSE","kAND","kOR","kNOT","kIF_MOD","kUNLESS_MOD",
    "kWHILE_MOD","kUNTIL_MOD","kRESCUE_MOD","kALIAS","kDEFINED","klBEGIN",
    "klEND","k__LINE__","k__FILE__","tIDENTIFIER","tFID","tGVAR","tIVAR",
    "tCONSTANT","tCVAR","tNTH_REF","tBACK_REF","tSTRING_CONTENT",
    "tINTEGER","tFLOAT","tREGEXP_END","tUPLUS","tUMINUS","tUMINUS_NUM",
    "tPOW","tCMP","tEQ","tEQQ","tNEQ","tGEQ","tLEQ","tANDOP","tOROP",
    "tMATCH","tNMATCH","tDOT","tDOT2","tDOT3","tAREF","tASET","tLSHFT",
    "tRSHFT","tCOLON2","tCOLON3","tOP_ASGN","tASSOC","tLPAREN","tLPAREN2",
    "tRPAREN","tLPAREN_ARG","tLBRACK","tRBRACK","tLBRACE","tLBRACE_ARG",
    "tSTAR","tSTAR2","tAMPER","tAMPER2","tTILDE","tPERCENT","tDIVIDE",
    "tPLUS","tMINUS","tLT","tGT","tPIPE","tBANG","tCARET","tLCURLY",
    "tRCURLY","tBACK_REF2","tSYMBEG","tSTRING_BEG","tXSTRING_BEG",
    "tREGEXP_BEG","tWORDS_BEG","tQWORDS_BEG","tSTRING_DBEG",
    "tSTRING_DVAR","tSTRING_END","tLOWEST","tLAST_TOKEN",
    };

  /** thrown for irrecoverable syntax errors and stack overflow.
      Nested for convenience, does not depend on parser class.
    */
  public static class yyException extends java.lang.Exception {
    private static final long serialVersionUID = 1L;
    public yyException (String message) {
      super(message);
    }
  }

  /** must be implemented by a scanner object to supply input to the parser.
      Nested for convenience, does not depend on parser class.
    */
  public interface yyInput {

    /** move on to next token.
        @return <tt>false</tt> if positioned beyond tokens.
        @throws IOException on input error.
      */
    boolean advance () throws java.io.IOException;

    /** classifies current token.
        Should not be called if {@link #advance()} returned <tt>false</tt>.
        @return current <tt>%token</tt> or single character.
      */
    int token ();

    /** associated with current token.
        Should not be called if {@link #advance()} returned <tt>false</tt>.
        @return value for {@link #token()}.
      */
    Object value ();
  }

  /** simplified error message.
      @see #yyerror(java.lang.String, java.lang.String[])
    */
  public void yyerror (String message) {
    yyerror(message, null, null);
  }

  /** (syntax) error message.
      Can be overwritten to control message format.
      @param message text to be displayed.
      @param expected list of acceptable tokens, if available.
    */
  public void yyerror (String message, String[] expected, String found) {
    StringBuffer text = new StringBuffer(message);

    if (expected != null && expected.length > 0) {
      text.append(", expecting");
      for (int n = 0; n < expected.length; ++ n) {
        text.append("\t").append(expected[n]);
      }
      text.append(" but found " + found + " instead\n");
    }

    throw new SyntaxException(getPosition(null), text.toString());
  }

  /** computes list of expected tokens on error by tracing the tables.
      @param state for which to compute the list.
      @return list of token names.
    */
  protected String[] yyExpecting (int state) {
    int token, n, len = 0;
    boolean[] ok = new boolean[yyNames.length];

    if ((n = yySindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < yyNames.length && n+token < yyTable.length; ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }
    if ((n = yyRindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < yyNames.length && n+token < yyTable.length; ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }

    String result[] = new String[len];
    for (n = token = 0; n < len;  ++ token)
      if (ok[token]) result[n++] = yyNames[token];
    return result;
  }

  /** the generated parser, with debugging messages.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @param yydebug debug message writer implementing <tt>yyDebug</tt>, or <tt>null</tt>.
      @return result of the last reduction, if any.
      @throws yyException on irrecoverable parse error.
    */
  public Object yyparse (RubyYaccLexer yyLex, Object ayydebug)
				throws java.io.IOException, yyException {
    return yyparse(yyLex);
  }

  /** initial size and increment of the state/value stack [default 256].
      This is not final so that it can be overwritten outside of invocations
      of {@link #yyparse}.
    */
  protected int yyMax;

  /** executed at the beginning of a reduce action.
      Used as <tt>$$ = yyDefault($1)</tt>, prior to the user-specified action, if any.
      Can be overwritten to provide deep copy, etc.
      @param first value for <tt>$1</tt>, or <tt>null</tt>.
      @return first.
    */
  protected Object yyDefault (Object first) {
    return first;
  }

  /** the generated parser.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @return result of the last reduction, if any.
      @throws yyException on irrecoverable parse error.
    */
  public Object yyparse (RubyYaccLexer yyLex) throws java.io.IOException, yyException {
    if (yyMax <= 0) yyMax = 256;			// initial size
    int yyState = 0, yyStates[] = new int[yyMax];	// state stack
    Object yyVal = null, yyVals[] = new Object[yyMax];	// value stack
    int yyToken = -1;					// current input
    int yyErrorFlag = 0;				// #tokens to shift

    yyLoop: for (int yyTop = 0;; ++ yyTop) {
      if (yyTop >= yyStates.length) {			// dynamically increase
        int[] i = new int[yyStates.length+yyMax];
        System.arraycopy(yyStates, 0, i, 0, yyStates.length);
        yyStates = i;
        Object[] o = new Object[yyVals.length+yyMax];
        System.arraycopy(yyVals, 0, o, 0, yyVals.length);
        yyVals = o;
      }
      yyStates[yyTop] = yyState;
      yyVals[yyTop] = yyVal;

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
            yyToken = yyLex.advance() ? yyLex.token() : 0;
          }
          if ((yyN = yySindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken) {
            yyState = yyTable[yyN];		// shift to yyN
            yyVal = yyLex.value();
            yyToken = -1;
            if (yyErrorFlag > 0) -- yyErrorFlag;
            continue yyLoop;
          }
          if ((yyN = yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken)
            yyN = yyTable[yyN];			// reduce (yyN)
          else
            switch (yyErrorFlag) {
  
            case 0:
              yyerror("syntax error", yyExpecting(yyState), yyNames[yyToken]);
  
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < yyTable.length
                    && yyCheck[yyN] == yyErrorCode) {
                  yyState = yyTable[yyN];
                  yyVal = yyLex.value();
                  continue yyLoop;
                }
              } while (-- yyTop >= 0);
              throw new yyException("irrecoverable syntax error");
  
            case 3:
              if (yyToken == 0) {
                throw new yyException("irrecoverable syntax error at end-of-file");
              }
              yyToken = -1;
              continue yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-yyLen[yyN];
        yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
        switch (yyN) {
case 1:
					// line 257 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_BEG);
                  support.initTopLocalVariables();
              }
  break;
case 2:
					// line 260 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[0+yyTop]) != null) {
                      /* last expression should not be void */
                      if (((Node)yyVals[0+yyTop]) instanceof BlockNode) {
                          support.checkUselessStatement(((BlockNode)yyVals[0+yyTop]).getLast());
                      } else {
                          support.checkUselessStatement(((Node)yyVals[0+yyTop]));
                      }
                  }
                  support.getResult().setAST(support.appendToBlock(support.getResult().getAST(), ((Node)yyVals[0+yyTop])));
                  support.updateTopLocalVariables();
              }
  break;
case 3:
					// line 273 "DefaultRubyParser.y"
  {
                  Node node = ((Node)yyVals[-3+yyTop]);

		  if (((RescueBodyNode)yyVals[-2+yyTop]) != null) {
		      node = new RescueNode(getPosition(((Node)yyVals[-3+yyTop]), true), ((Node)yyVals[-3+yyTop]), ((RescueBodyNode)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]));
		  } else if (((Node)yyVals[-1+yyTop]) != null) {
		      warnings.warn(getPosition(((Node)yyVals[-3+yyTop])), "else without rescue is useless");
                      node = support.appendToBlock(((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]));
		  }
		  if (((Node)yyVals[0+yyTop]) != null) {
		      node = new EnsureNode(getPosition(((Node)yyVals[-3+yyTop])), node, ((Node)yyVals[0+yyTop]));
		  }

	          yyVal = node;
              }
  break;
case 4:
					// line 289 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[-1+yyTop]) instanceof BlockNode) {
                      support.checkUselessStatements(((BlockNode)yyVals[-1+yyTop]));
		  }
                  yyVal = ((Node)yyVals[-1+yyTop]);
              }
  break;
case 6:
					// line 297 "DefaultRubyParser.y"
  {
                  yyVal = support.newline_node(((Node)yyVals[0+yyTop]), getPosition(((Node)yyVals[0+yyTop]), true));
              }
  break;
case 7:
					// line 300 "DefaultRubyParser.y"
  {
	          yyVal = support.appendToBlock(((Node)yyVals[-2+yyTop]), support.newline_node(((Node)yyVals[0+yyTop]), getPosition(((Node)yyVals[-2+yyTop]), true)));
              }
  break;
case 8:
					// line 303 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 9:
					// line 307 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_FNAME);
              }
  break;
case 10:
					// line 309 "DefaultRubyParser.y"
  {
                  yyVal = new AliasNode(getPosition(((Token)yyVals[-3+yyTop])), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 11:
					// line 312 "DefaultRubyParser.y"
  {
                  yyVal = new VAliasNode(getPosition(((Token)yyVals[-2+yyTop])), (String) ((Token)yyVals[-1+yyTop]).getValue(), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 12:
					// line 315 "DefaultRubyParser.y"
  {
                  yyVal = new VAliasNode(getPosition(((Token)yyVals[-2+yyTop])), (String) ((Token)yyVals[-1+yyTop]).getValue(), "$" + ((BackRefNode)yyVals[0+yyTop]).getType()); /* XXX
*/
              }
  break;
case 13:
					// line 318 "DefaultRubyParser.y"
  {
                  yyerror("can't make alias for the number variables");
              }
  break;
case 14:
					// line 321 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 15:
					// line 324 "DefaultRubyParser.y"
  {
                  yyVal = new IfNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), null);
              }
  break;
case 16:
					// line 327 "DefaultRubyParser.y"
  {
                  yyVal = new IfNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), null, ((Node)yyVals[-2+yyTop]));
              }
  break;
case 17:
					// line 330 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[-2+yyTop]) != null && ((Node)yyVals[-2+yyTop]) instanceof BeginNode) {
                      yyVal = new WhileNode(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((BeginNode)yyVals[-2+yyTop]).getBodyNode(), false);
                  } else {
                      yyVal = new WhileNode(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), true);
                  }
              }
  break;
case 18:
					// line 337 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[-2+yyTop]) != null && ((Node)yyVals[-2+yyTop]) instanceof BeginNode) {
                      yyVal = new UntilNode(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((BeginNode)yyVals[-2+yyTop]).getBodyNode());
                  } else {
                      yyVal = new UntilNode(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]));
                  }
              }
  break;
case 19:
					// line 344 "DefaultRubyParser.y"
  {
	          yyVal = new RescueNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[-2+yyTop]), new RescueBodyNode(getPosition(((Node)yyVals[-2+yyTop])), null,((Node)yyVals[0+yyTop]), null), null);
              }
  break;
case 20:
					// line 347 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
                      yyerror("BEGIN in method");
                  }
                  support.getLocalNames().push(new LocalNamesElement());
              }
  break;
case 21:
					// line 352 "DefaultRubyParser.y"
  {
                  support.getResult().addBeginNode(new ScopeNode(getPosition(((Token)yyVals[-4+yyTop]), true), ((LocalNamesElement) support.getLocalNames().peek()).getNamesArray(), ((Node)yyVals[-1+yyTop])));
                  support.getLocalNames().pop();
                  yyVal = null; /*XXX 0;
*/
              }
  break;
case 22:
					// line 357 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
                      yyerror("END in method; use at_exit");
                  }
                  support.getResult().addEndNode(new IterNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), null, new PostExeNode(getPosition(((Token)yyVals[-3+yyTop]))), ((Node)yyVals[-1+yyTop])));
                  yyVal = null;
              }
  break;
case 23:
					// line 364 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[0+yyTop]));
                  yyVal = support.node_assign(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 24:
					// line 368 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[0+yyTop]));
		  if (((MultipleAsgnNode)yyVals[-2+yyTop]).getHeadNode() != null) {
		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ToAryNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
		  } else {
		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ArrayNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
		  }
		  yyVal = ((MultipleAsgnNode)yyVals[-2+yyTop]);
              }
  break;
case 25:
					// line 377 "DefaultRubyParser.y"
  {
 	          support.checkExpression(((Node)yyVals[0+yyTop]));

	          String name = ((INameNode)yyVals[-2+yyTop]).getName();
		  String asgnOp = (String) ((Token)yyVals[-1+yyTop]).getValue();
		  if (asgnOp.equals("||")) {
	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
	              yyVal = new OpAsgnOrNode(getPosition(((AssignableNode)yyVals[-2+yyTop])), support.gettable(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
		  } else if (asgnOp.equals("&&")) {
	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
                      yyVal = new OpAsgnAndNode(getPosition(((AssignableNode)yyVals[-2+yyTop])), support.gettable(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
		  } else {
                      ((AssignableNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), asgnOp, ((Node)yyVals[0+yyTop])));
		      yyVal = ((AssignableNode)yyVals[-2+yyTop]);
		  }
	      }
  break;
case 26:
					// line 393 "DefaultRubyParser.y"
  {
                  yyVal = new OpElementAsgnNode(getPosition(((Node)yyVals[-5+yyTop])), ((Node)yyVals[-5+yyTop]), (String) ((Token)yyVals[-1+yyTop]).getValue(), ((Node)yyVals[-3+yyTop]), ((Node)yyVals[0+yyTop]));

              }
  break;
case 27:
					// line 397 "DefaultRubyParser.y"
  {
                  yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
              }
  break;
case 28:
					// line 400 "DefaultRubyParser.y"
  {
                  yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
              }
  break;
case 29:
					// line 403 "DefaultRubyParser.y"
  {
                  yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
              }
  break;
case 30:
					// line 406 "DefaultRubyParser.y"
  {
                  support.backrefAssignError(((Node)yyVals[-2+yyTop]));
              }
  break;
case 31:
					// line 409 "DefaultRubyParser.y"
  {
                  yyVal = support.node_assign(((Node)yyVals[-2+yyTop]), new SValueNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
              }
  break;
case 32:
					// line 412 "DefaultRubyParser.y"
  {
                  if (((MultipleAsgnNode)yyVals[-2+yyTop]).getHeadNode() != null) {
		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ToAryNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
		  } else {
		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ArrayNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
		  }
		  yyVal = ((MultipleAsgnNode)yyVals[-2+yyTop]);
	      }
  break;
case 33:
					// line 420 "DefaultRubyParser.y"
  {
                  ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
		  yyVal = ((MultipleAsgnNode)yyVals[-2+yyTop]);
	      }
  break;
case 36:
					// line 427 "DefaultRubyParser.y"
  {
                  yyVal = support.newAndNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 37:
					// line 430 "DefaultRubyParser.y"
  {
                  yyVal = support.newOrNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 38:
					// line 433 "DefaultRubyParser.y"
  {
                  yyVal = new NotNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])));
              }
  break;
case 39:
					// line 436 "DefaultRubyParser.y"
  {
                  yyVal = new NotNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])));
              }
  break;
case 41:
					// line 441 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[0+yyTop]));
	      }
  break;
case 44:
					// line 447 "DefaultRubyParser.y"
  {
                  yyVal = new ReturnNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.ret_args(((Node)yyVals[0+yyTop]), getPosition(((Token)yyVals[-1+yyTop]))));
              }
  break;
case 45:
					// line 450 "DefaultRubyParser.y"
  {
                  yyVal = new BreakNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.ret_args(((Node)yyVals[0+yyTop]), getPosition(((Token)yyVals[-1+yyTop]))));
              }
  break;
case 46:
					// line 453 "DefaultRubyParser.y"
  {
                  yyVal = new NextNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.ret_args(((Node)yyVals[0+yyTop]), getPosition(((Token)yyVals[-1+yyTop]))));
              }
  break;
case 48:
					// line 458 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 49:
					// line 461 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 50:
					// line 465 "DefaultRubyParser.y"
  {
                    support.getBlockNames().push(new BlockNamesElement());
		}
  break;
case 51:
					// line 467 "DefaultRubyParser.y"
  {
                    yyVal = new IterNode(getPosition(((Token)yyVals[-4+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), null);
                    support.getBlockNames().pop();
		}
  break;
case 52:
					// line 472 "DefaultRubyParser.y"
  {
                  yyVal = support.new_fcall(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 53:
					// line 475 "DefaultRubyParser.y"
  {
                  yyVal = support.new_fcall(((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop])); 
	          if (((IterNode)yyVals[0+yyTop]) != null) {
                      if (yyVal instanceof BlockPassNode) {
                          throw new SyntaxException(getPosition(((Token)yyVals[-2+yyTop])), "Both block arg and actual block given.");
                      }
                      ((IterNode)yyVals[0+yyTop]).setIterNode(((Node)yyVal));
                      yyVal = ((Node)yyVals[-1+yyTop]);
		  }
              }
  break;
case 54:
					// line 485 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])); /*.setPosFrom($1);
*/
              }
  break;
case 55:
					// line 488 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-4+yyTop]), ((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop])); 
		  if (((IterNode)yyVals[0+yyTop]) != null) {
		      if (yyVal instanceof BlockPassNode) {
                          throw new SyntaxException(getPosition(((Node)yyVals[-4+yyTop])), "Both block arg and actual block given.");
                      }
                      ((IterNode)yyVals[0+yyTop]).setIterNode(((Node)yyVal));
		      yyVal = ((IterNode)yyVals[0+yyTop]);
		  }
	      }
  break;
case 56:
					// line 498 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 57:
					// line 501 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-4+yyTop]), ((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop])); 
		  if (((IterNode)yyVals[0+yyTop]) != null) {
		      if (yyVal instanceof BlockPassNode) {
                          throw new SyntaxException(getPosition(((Node)yyVals[-4+yyTop])), "Both block arg and actual block given.");
                      }
                      ((IterNode)yyVals[0+yyTop]).setIterNode(((Node)yyVal));
		      yyVal = ((IterNode)yyVals[0+yyTop]);
		  }
	      }
  break;
case 58:
					// line 511 "DefaultRubyParser.y"
  {
		  yyVal = support.new_super(((Node)yyVals[0+yyTop]), ((Token)yyVals[-1+yyTop])); /* .setPosFrom($2);
*/
	      }
  break;
case 59:
					// line 514 "DefaultRubyParser.y"
  {
                  yyVal = support.new_yield(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]));
	      }
  break;
case 61:
					// line 519 "DefaultRubyParser.y"
  {
                  yyVal = ((MultipleAsgnNode)yyVals[-1+yyTop]);
	      }
  break;
case 63:
					// line 524 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((Token)yyVals[-2+yyTop])), new ArrayNode(getPosition(((Token)yyVals[-2+yyTop])), ((MultipleAsgnNode)yyVals[-1+yyTop])), null);
              }
  break;
case 64:
					// line 528 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[0+yyTop])), ((ListNode)yyVals[0+yyTop]), null);
              }
  break;
case 65:
					// line 531 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[-1+yyTop])), ((ListNode)yyVals[-1+yyTop]).add(((Node)yyVals[0+yyTop])), null);
              }
  break;
case 66:
					// line 534 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[-2+yyTop])), ((ListNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 67:
					// line 537 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[-1+yyTop])), ((ListNode)yyVals[-1+yyTop]), new StarNode(getPosition(null)));
              }
  break;
case 68:
					// line 540 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((Token)yyVals[-1+yyTop])), null, ((Node)yyVals[0+yyTop]));
              }
  break;
case 69:
					// line 543 "DefaultRubyParser.y"
  {
                  yyVal = new MultipleAsgnNode(getPosition(((Token)yyVals[0+yyTop])), null, new StarNode(getPosition(null)));
              }
  break;
case 71:
					// line 548 "DefaultRubyParser.y"
  {
                  yyVal = ((MultipleAsgnNode)yyVals[-1+yyTop]);
              }
  break;
case 72:
					// line 552 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(((Node)yyVals[-1+yyTop]).getPosition(), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 73:
					// line 555 "DefaultRubyParser.y"
  {
                  yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[-1+yyTop]));
              }
  break;
case 74:
					// line 559 "DefaultRubyParser.y"
  {
                  yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
              }
  break;
case 75:
					// line 562 "DefaultRubyParser.y"
  {
                  yyVal = support.getElementAssignmentNode(((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 76:
					// line 565 "DefaultRubyParser.y"
  {
                  yyVal = support.getAttributeAssignmentNode(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 77:
					// line 568 "DefaultRubyParser.y"
  {
                  yyVal = support.getAttributeAssignmentNode(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 78:
					// line 571 "DefaultRubyParser.y"
  {
                  yyVal = support.getAttributeAssignmentNode(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 79:
					// line 574 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
		      yyerror("dynamic constant assignment");
		  }
			
		  yyVal = new ConstDeclNode(support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue(), null);
	      }
  break;
case 80:
					// line 581 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
		      yyerror("dynamic constant assignment");
		  }

		  /* ERROR:  VEry likely a big error. */
                  yyVal = new Colon3Node(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
		  /* ruby $$ = NEW_CDECL(0, 0, NEW_COLON3($2)); */
	      }
  break;
case 81:
					// line 590 "DefaultRubyParser.y"
  {
	          support.backrefAssignError(((Node)yyVals[0+yyTop]));
              }
  break;
case 82:
					// line 594 "DefaultRubyParser.y"
  {
                  yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
              }
  break;
case 83:
					// line 597 "DefaultRubyParser.y"
  {
                  yyVal = support.getElementAssignmentNode(((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 84:
					// line 600 "DefaultRubyParser.y"
  {
                  yyVal = support.getAttributeAssignmentNode(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 85:
					// line 603 "DefaultRubyParser.y"
  {
                  yyVal = support.getAttributeAssignmentNode(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
 	      }
  break;
case 86:
					// line 606 "DefaultRubyParser.y"
  {
                  yyVal = support.getAttributeAssignmentNode(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 87:
					// line 609 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
		      yyerror("dynamic constant assignment");
		  }
			
                  yyVal = new ConstDeclNode(support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue(), null);
	      }
  break;
case 88:
					// line 616 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
		      yyerror("dynamic constant assignment");
		  }

		  /* ERROR:  VEry likely a big error. */
                  yyVal = new Colon3Node(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
		  /* ruby $$ = NEW_CDECL(0, 0, NEW_COLON3($2)); */
	      }
  break;
case 89:
					// line 625 "DefaultRubyParser.y"
  {
                   support.backrefAssignError(((Node)yyVals[0+yyTop]));
	      }
  break;
case 90:
					// line 629 "DefaultRubyParser.y"
  {
                  yyerror("class/module name must be CONSTANT");
              }
  break;
case 92:
					// line 634 "DefaultRubyParser.y"
  {
                  yyVal = new Colon3Node(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
	      }
  break;
case 93:
					// line 637 "DefaultRubyParser.y"
  {
                  yyVal = new Colon2Node(((Token)yyVals[0+yyTop]).getPosition(), null, (String) ((Token)yyVals[0+yyTop]).getValue());
 	      }
  break;
case 94:
					// line 640 "DefaultRubyParser.y"
  {
                  yyVal = new Colon2Node(support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
	      }
  break;
case 98:
					// line 646 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_END);
                  yyVal = ((Token)yyVals[0+yyTop]);
              }
  break;
case 99:
					// line 651 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_END);
                  yyVal = yyVals[0+yyTop];
              }
  break;
case 102:
					// line 658 "DefaultRubyParser.y"
  {
                  yyVal = new UndefNode(getPosition(((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 103:
					// line 661 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_FNAME);
	      }
  break;
case 104:
					// line 663 "DefaultRubyParser.y"
  {
                  yyVal = support.appendToBlock(((Node)yyVals[-3+yyTop]), new UndefNode(getPosition(((Node)yyVals[-3+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue()));
              }
  break;
case 172:
					// line 682 "DefaultRubyParser.y"
  {
                  yyVal = support.node_assign(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
		  /* FIXME: Consider fixing node_assign itself rather than single case
*/
		  ((Node)yyVal).setPosition(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
              }
  break;
case 173:
					// line 687 "DefaultRubyParser.y"
  {
                  ISourcePosition position = support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
                  yyVal = support.node_assign(((Node)yyVals[-4+yyTop]), new RescueNode(position, ((Node)yyVals[-2+yyTop]), new RescueBodyNode(position, null, ((Node)yyVals[0+yyTop]), null), null));
	      }
  break;
case 174:
					// line 691 "DefaultRubyParser.y"
  {
		  support.checkExpression(((Node)yyVals[0+yyTop]));
	          String name = ((INameNode)yyVals[-2+yyTop]).getName();
		  String asgnOp = (String) ((Token)yyVals[-1+yyTop]).getValue();

		  if (asgnOp.equals("||")) {
	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
	              yyVal = new OpAsgnOrNode(getPosition(((AssignableNode)yyVals[-2+yyTop])), support.gettable(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
		  } else if (asgnOp.equals("&&")) {
	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
                      yyVal = new OpAsgnAndNode(getPosition(((AssignableNode)yyVals[-2+yyTop])), support.gettable(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
		  } else {
		      ((AssignableNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), asgnOp, ((Node)yyVals[0+yyTop])));
		      yyVal = ((AssignableNode)yyVals[-2+yyTop]);
		  }
              }
  break;
case 175:
					// line 707 "DefaultRubyParser.y"
  {
                  yyVal = new OpElementAsgnNode(getPosition(((Node)yyVals[-5+yyTop])), ((Node)yyVals[-5+yyTop]), (String) ((Token)yyVals[-1+yyTop]).getValue(), ((Node)yyVals[-3+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 176:
					// line 710 "DefaultRubyParser.y"
  {
                  yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
              }
  break;
case 177:
					// line 713 "DefaultRubyParser.y"
  {
                  yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
              }
  break;
case 178:
					// line 716 "DefaultRubyParser.y"
  {
                  yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
              }
  break;
case 179:
					// line 719 "DefaultRubyParser.y"
  {
	          yyerror("constant re-assignment");
	      }
  break;
case 180:
					// line 722 "DefaultRubyParser.y"
  {
		  yyerror("constant re-assignment");
	      }
  break;
case 181:
					// line 725 "DefaultRubyParser.y"
  {
                  support.backrefAssignError(((Node)yyVals[-2+yyTop]));
              }
  break;
case 182:
					// line 728 "DefaultRubyParser.y"
  {
		  support.checkExpression(((Node)yyVals[-2+yyTop]));
		  support.checkExpression(((Node)yyVals[0+yyTop]));
                  yyVal = new DotNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]), false);
              }
  break;
case 183:
					// line 733 "DefaultRubyParser.y"
  {
		  support.checkExpression(((Node)yyVals[-2+yyTop]));
		  support.checkExpression(((Node)yyVals[0+yyTop]));
                  yyVal = new DotNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]), true);
              }
  break;
case 184:
					// line 738 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "+", ((Node)yyVals[0+yyTop]));
              }
  break;
case 185:
					// line 741 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "-", ((Node)yyVals[0+yyTop]));
              }
  break;
case 186:
					// line 744 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "*", ((Node)yyVals[0+yyTop]));
              }
  break;
case 187:
					// line 747 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "/", ((Node)yyVals[0+yyTop]));
              }
  break;
case 188:
					// line 750 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "%", ((Node)yyVals[0+yyTop]));
              }
  break;
case 189:
					// line 753 "DefaultRubyParser.y"
  {
		  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "**", ((Node)yyVals[0+yyTop]));
              }
  break;
case 190:
					// line 756 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "**", ((Node)yyVals[0+yyTop])), "-@");
              }
  break;
case 191:
					// line 759 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "**", ((Node)yyVals[0+yyTop])), "-@");
              }
  break;
case 192:
					// line 762 "DefaultRubyParser.y"
  {
 	          if (((Node)yyVals[0+yyTop]) != null && ((Node)yyVals[0+yyTop]) instanceof ILiteralNode) {
		      yyVal = ((Node)yyVals[0+yyTop]);
		  } else {
                      yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "+@");
		  }
              }
  break;
case 193:
					// line 769 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "-@");
	      }
  break;
case 194:
					// line 772 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "|", ((Node)yyVals[0+yyTop]));
              }
  break;
case 195:
					// line 775 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "^", ((Node)yyVals[0+yyTop]));
              }
  break;
case 196:
					// line 778 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "&", ((Node)yyVals[0+yyTop]));
              }
  break;
case 197:
					// line 781 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<=>", ((Node)yyVals[0+yyTop]));
              }
  break;
case 198:
					// line 784 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), ">", ((Node)yyVals[0+yyTop]));
              }
  break;
case 199:
					// line 787 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), ">=", ((Node)yyVals[0+yyTop]));
              }
  break;
case 200:
					// line 790 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<", ((Node)yyVals[0+yyTop]));
              }
  break;
case 201:
					// line 793 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<=", ((Node)yyVals[0+yyTop]));
              }
  break;
case 202:
					// line 796 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "==", ((Node)yyVals[0+yyTop]));
              }
  break;
case 203:
					// line 799 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "===", ((Node)yyVals[0+yyTop]));
              }
  break;
case 204:
					// line 802 "DefaultRubyParser.y"
  {
                  yyVal = new NotNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "==", ((Node)yyVals[0+yyTop])));
              }
  break;
case 205:
					// line 805 "DefaultRubyParser.y"
  {
                  yyVal = support.getMatchNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 206:
					// line 808 "DefaultRubyParser.y"
  {
                  yyVal = new NotNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getMatchNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
              }
  break;
case 207:
					// line 811 "DefaultRubyParser.y"
  {
                  yyVal = new NotNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])));
              }
  break;
case 208:
					// line 814 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "~");
              }
  break;
case 209:
					// line 817 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<<", ((Node)yyVals[0+yyTop]));
              }
  break;
case 210:
					// line 820 "DefaultRubyParser.y"
  {
                  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), ">>", ((Node)yyVals[0+yyTop]));
              }
  break;
case 211:
					// line 823 "DefaultRubyParser.y"
  {
                  yyVal = support.newAndNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 212:
					// line 826 "DefaultRubyParser.y"
  {
                  yyVal = support.newOrNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 213:
					// line 829 "DefaultRubyParser.y"
  {
	          support.setInDefined(true);
	      }
  break;
case 214:
					// line 831 "DefaultRubyParser.y"
  {
                  support.setInDefined(false);
                  yyVal = new DefinedNode(getPosition(((Token)yyVals[-3+yyTop])), ((Node)yyVals[0+yyTop]));
              }
  break;
case 215:
					// line 835 "DefaultRubyParser.y"
  {
                  yyVal = new IfNode(getPosition(((Node)yyVals[-4+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 216:
					// line 838 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 217:
					// line 842 "DefaultRubyParser.y"
  {
	          support.checkExpression(((Node)yyVals[0+yyTop]));
	          yyVal = ((Node)yyVals[0+yyTop]);   
	      }
  break;
case 219:
					// line 848 "DefaultRubyParser.y"
  {
                  warnings.warn(getPosition(((Node)yyVals[-1+yyTop])), "parenthesize argument(s) for future version");
                  yyVal = new ArrayNode(getPosition(((Node)yyVals[-1+yyTop])), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 220:
					// line 852 "DefaultRubyParser.y"
  {
                  yyVal = ((ListNode)yyVals[-1+yyTop]);
              }
  break;
case 221:
					// line 855 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), ((ListNode)yyVals[-4+yyTop]), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 222:
					// line 859 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(getPosition(((ListNode)yyVals[-1+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
              }
  break;
case 223:
					// line 862 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[-1+yyTop]));
		  yyVal = new NewlineNode(getPosition(((Token)yyVals[-2+yyTop])), new SplatNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[-1+yyTop])));
              }
  break;
case 224:
					// line 867 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
              }
  break;
case 225:
					// line 870 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[-2+yyTop]);
		  ((Node)yyVal).setPosition(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])));
              }
  break;
case 226:
					// line 874 "DefaultRubyParser.y"
  {
                  warnings.warn(getPosition(((Token)yyVals[-3+yyTop])), "parenthesize argument(s) for future version");
                  yyVal = new ArrayNode(getPosition(((Token)yyVals[-3+yyTop])), ((Node)yyVals[-2+yyTop]));
              }
  break;
case 227:
					// line 878 "DefaultRubyParser.y"
  {
                  warnings.warn(getPosition(((Token)yyVals[-5+yyTop])), "parenthesize argument(s) for future version");
                  yyVal = ((ListNode)yyVals[-4+yyTop]).add(((Node)yyVals[-2+yyTop]));
              }
  break;
case 230:
					// line 886 "DefaultRubyParser.y"
  {
                  warnings.warn(getPosition(((Node)yyVals[0+yyTop])), "parenthesize argument(s) for future version");
                  yyVal = new ArrayNode(getPosition(((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
              }
  break;
case 231:
					// line 890 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_blk_pass(((ListNode)yyVals[-1+yyTop]), ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 232:
					// line 893 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), ((ListNode)yyVals[-4+yyTop]), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass(((Node)yyVal), ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 233:
					// line 897 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(getPosition(((ListNode)yyVals[-1+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 234:
					// line 901 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), new ArrayNode(getPosition(((ListNode)yyVals[-4+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 235:
					// line 905 "DefaultRubyParser.y"
  {
                  yyVal = ((ListNode)yyVals[-3+yyTop]).add(new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 236:
					// line 909 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[-1+yyTop]));
		  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-6+yyTop])), ((ListNode)yyVals[-6+yyTop]).add(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 237:
					// line 914 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_blk_pass(new SplatNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[-1+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 238:
					// line 917 "DefaultRubyParser.y"
  {}
  break;
case 239:
					// line 919 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_blk_pass(support.list_concat(new ArrayNode(getPosition(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-3+yyTop])), ((ListNode)yyVals[-1+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 240:
					// line 922 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_blk_pass(new ArrayNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[-2+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
              }
  break;
case 241:
					// line 925 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((Node)yyVals[-4+yyTop])), new ArrayNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 242:
					// line 929 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((Node)yyVals[-6+yyTop])), support.list_concat(new ArrayNode(getPosition(((Node)yyVals[-6+yyTop])), ((Node)yyVals[-6+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 243:
					// line 933 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(getPosition(((ListNode)yyVals[-1+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 244:
					// line 937 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), new ArrayNode(getPosition(((ListNode)yyVals[-4+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 245:
					// line 941 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(getPosition(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-3+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 246:
					// line 945 "DefaultRubyParser.y"
  {
                  yyVal = support.list_concat(new ArrayNode(getPosition(((Node)yyVals[-5+yyTop])), ((Node)yyVals[-5+yyTop])), ((ListNode)yyVals[-3+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 247:
					// line 949 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((Node)yyVals[-6+yyTop])), new ArrayNode(getPosition(((Node)yyVals[-6+yyTop])), ((Node)yyVals[-6+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 248:
					// line 953 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((Node)yyVals[-8+yyTop])), support.list_concat(new ArrayNode(getPosition(((Node)yyVals[-8+yyTop])), ((Node)yyVals[-8+yyTop])), ((ListNode)yyVals[-6+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                  yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 249:
					// line 957 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_blk_pass(new SplatNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[-1+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
	      }
  break;
case 250:
					// line 960 "DefaultRubyParser.y"
  {}
  break;
case 251:
					// line 962 "DefaultRubyParser.y"
  { 
	          yyVal = new Long(lexer.getCmdArgumentState().begin());
	      }
  break;
case 252:
					// line 964 "DefaultRubyParser.y"
  {
                  lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
                  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 254:
					// line 970 "DefaultRubyParser.y"
  {                    
		  lexer.setState(LexState.EXPR_ENDARG);
	      }
  break;
case 255:
					// line 972 "DefaultRubyParser.y"
  {
                  warnings.warn(getPosition(((Token)yyVals[-2+yyTop])), "don't put space before argument parentheses");
	          yyVal = null;
	      }
  break;
case 256:
					// line 976 "DefaultRubyParser.y"
  {
		  lexer.setState(LexState.EXPR_ENDARG);
	      }
  break;
case 257:
					// line 978 "DefaultRubyParser.y"
  {
                  warnings.warn(getPosition(((Token)yyVals[-3+yyTop])), "don't put space before argument parentheses");
		  yyVal = ((Node)yyVals[-2+yyTop]);
	      }
  break;
case 258:
					// line 983 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[0+yyTop]));
                  yyVal = new BlockPassNode(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]));
              }
  break;
case 259:
					// line 988 "DefaultRubyParser.y"
  {
                  yyVal = ((BlockPassNode)yyVals[0+yyTop]);
              }
  break;
case 261:
					// line 993 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(getPosition2(((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
              }
  break;
case 262:
					// line 996 "DefaultRubyParser.y"
  {
                  yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[0+yyTop]));
              }
  break;
case 263:
					// line 1000 "DefaultRubyParser.y"
  {
		  yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[0+yyTop]));
              }
  break;
case 264:
					// line 1003 "DefaultRubyParser.y"
  {
                  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-3+yyTop])), ((ListNode)yyVals[-3+yyTop]), ((Node)yyVals[0+yyTop]));
	      }
  break;
case 265:
					// line 1006 "DefaultRubyParser.y"
  {  
                  yyVal = new SplatNode(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]));
	      }
  break;
case 274:
					// line 1018 "DefaultRubyParser.y"
  {
                  yyVal = new VCallNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
	      }
  break;
case 275:
					// line 1021 "DefaultRubyParser.y"
  {
                  yyVal = new BeginNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-1+yyTop]));
	      }
  break;
case 276:
					// line 1024 "DefaultRubyParser.y"
  { 
                  lexer.setState(LexState.EXPR_ENDARG); 
              }
  break;
case 277:
					// line 1026 "DefaultRubyParser.y"
  {
		  warnings.warning(getPosition(((Token)yyVals[-4+yyTop])), "(...) interpreted as grouped expression");
                  yyVal = ((Node)yyVals[-3+yyTop]);
	      }
  break;
case 278:
					// line 1030 "DefaultRubyParser.y"
  {
		  yyVal = ((Node)yyVals[-1+yyTop]);
              }
  break;
case 279:
					// line 1033 "DefaultRubyParser.y"
  {
                  yyVal = new Colon2Node(support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 280:
					// line 1036 "DefaultRubyParser.y"
  {
                  yyVal = new Colon3Node(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 281:
					// line 1039 "DefaultRubyParser.y"
  {
                  yyVal = new CallNode(getPosition(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-3+yyTop]), "[]", ((Node)yyVals[-1+yyTop]));
              }
  break;
case 282:
					// line 1042 "DefaultRubyParser.y"
  {
                  ISourcePosition position = support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
                  if (((Node)yyVals[-1+yyTop]) == null) {
                      yyVal = new ZArrayNode(position); /* zero length array */
                  } else {
                      yyVal = ((Node)yyVals[-1+yyTop]);
                      ((ISourcePositionHolder)yyVal).setPosition(position);
                  }
              }
  break;
case 283:
					// line 1051 "DefaultRubyParser.y"
  {
                  yyVal = new HashNode(getPosition(((Token)yyVals[-2+yyTop])), ((ListNode)yyVals[-1+yyTop]));
              }
  break;
case 284:
					// line 1054 "DefaultRubyParser.y"
  {
		  yyVal = new ReturnNode(((Token)yyVals[0+yyTop]).getPosition(), null);
              }
  break;
case 285:
					// line 1057 "DefaultRubyParser.y"
  {
                  yyVal = support.new_yield(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 286:
					// line 1060 "DefaultRubyParser.y"
  {
                  yyVal = new YieldNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), null, false);
              }
  break;
case 287:
					// line 1063 "DefaultRubyParser.y"
  {
                  yyVal = new YieldNode(((Token)yyVals[0+yyTop]).getPosition(), null, false);
              }
  break;
case 288:
					// line 1066 "DefaultRubyParser.y"
  {
	          support.setInDefined(true);
	      }
  break;
case 289:
					// line 1068 "DefaultRubyParser.y"
  {
                  support.setInDefined(false);
                  yyVal = new DefinedNode(getPosition(((Token)yyVals[-5+yyTop])), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 290:
					// line 1072 "DefaultRubyParser.y"
  {
                  ((IterNode)yyVals[0+yyTop]).setIterNode(new FCallNode(support.union(((Token)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])), (String) ((Token)yyVals[-1+yyTop]).getValue(), null));
                  yyVal = ((IterNode)yyVals[0+yyTop]);
              }
  break;
case 292:
					// line 1077 "DefaultRubyParser.y"
  {
		  if (((Node)yyVals[-1+yyTop]) != null && ((Node)yyVals[-1+yyTop]) instanceof BlockPassNode) {
                      throw new SyntaxException(getPosition(((Node)yyVals[-1+yyTop])), "Both block arg and actual block given.");
		  }
                  ((IterNode)yyVals[0+yyTop]).setIterNode(((Node)yyVals[-1+yyTop]));
                  yyVal = ((IterNode)yyVals[0+yyTop]);
		  /* XXXEnebo: Having iter around a call seems backwards
*/
                  ((Node)yyVals[-1+yyTop]).setPosition(support.union(((Node)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])));
              }
  break;
case 293:
					// line 1086 "DefaultRubyParser.y"
  {
                  yyVal = new IfNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 294:
					// line 1089 "DefaultRubyParser.y"
  {
                  yyVal = new IfNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[-2+yyTop]));
              }
  break;
case 295:
					// line 1092 "DefaultRubyParser.y"
  { 
                  lexer.getConditionState().begin();
	      }
  break;
case 296:
					// line 1094 "DefaultRubyParser.y"
  {
		  lexer.getConditionState().end();
	      }
  break;
case 297:
					// line 1096 "DefaultRubyParser.y"
  {
                  yyVal = new WhileNode(getPosition(((Token)yyVals[-6+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 298:
					// line 1099 "DefaultRubyParser.y"
  {
                  lexer.getConditionState().begin();
              }
  break;
case 299:
					// line 1101 "DefaultRubyParser.y"
  {
                  lexer.getConditionState().end();
              }
  break;
case 300:
					// line 1103 "DefaultRubyParser.y"
  {
                  yyVal = new UntilNode(getPosition(((Token)yyVals[-6+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 301:
					// line 1106 "DefaultRubyParser.y"
  {
                  yyVal = new CaseNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]));
              }
  break;
case 302:
					// line 1109 "DefaultRubyParser.y"
  {
                  yyVal = new CaseNode(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), null, ((Node)yyVals[-1+yyTop]));
              }
  break;
case 303:
					// line 1112 "DefaultRubyParser.y"
  {
		  yyVal = ((Node)yyVals[-1+yyTop]);
              }
  break;
case 304:
					// line 1115 "DefaultRubyParser.y"
  {
                  lexer.getConditionState().begin();
              }
  break;
case 305:
					// line 1117 "DefaultRubyParser.y"
  {
                  lexer.getConditionState().end();
              }
  break;
case 306:
					// line 1119 "DefaultRubyParser.y"
  {
                  yyVal = new ForNode(support.union(((Token)yyVals[-8+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-7+yyTop]), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[-4+yyTop]));
              }
  break;
case 307:
					// line 1122 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) {
                      yyerror("class definition in method body");
                  }
                  support.getLocalNames().push(new LocalNamesElement());
              }
  break;
case 308:
					// line 1127 "DefaultRubyParser.y"
  {
                  yyVal = new ClassNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-4+yyTop]), new ScopeNode(getPosition(((Node)yyVals[-1+yyTop])), ((LocalNamesElement) support.getLocalNames().peek()).getNamesArray(), ((Node)yyVals[-1+yyTop])), ((Node)yyVals[-3+yyTop]));
                  support.getLocalNames().pop();
              }
  break;
case 309:
					// line 1131 "DefaultRubyParser.y"
  {
                  yyVal = new Boolean(support.isInDef());
                  support.setInDef(false);
              }
  break;
case 310:
					// line 1134 "DefaultRubyParser.y"
  {
                  yyVal = new Integer(support.getInSingle());
                  support.setInSingle(0);
                  support.getLocalNames().push(new LocalNamesElement());
              }
  break;
case 311:
					// line 1138 "DefaultRubyParser.y"
  {
                  yyVal = new SClassNode(support.union(((Token)yyVals[-7+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-5+yyTop]), new ScopeNode(getPosition(((Node)yyVals[-1+yyTop])), ((LocalNamesElement) support.getLocalNames().peek()).getNamesArray(), ((Node)yyVals[-1+yyTop])));
                  support.getLocalNames().pop();
                  support.setInDef(((Boolean)yyVals[-4+yyTop]).booleanValue());
                  support.setInSingle(((Integer)yyVals[-2+yyTop]).intValue());
              }
  break;
case 312:
					// line 1144 "DefaultRubyParser.y"
  {
                  if (support.isInDef() || support.isInSingle()) { 
                      yyerror("module definition in method body");
                  }
                  support.getLocalNames().push(new LocalNamesElement());
              }
  break;
case 313:
					// line 1149 "DefaultRubyParser.y"
  {
                  yyVal = new ModuleNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-3+yyTop]), new ScopeNode(getPosition(((Node)yyVals[-1+yyTop])), ((LocalNamesElement) support.getLocalNames().peek()).getNamesArray(), ((Node)yyVals[-1+yyTop])));
                  support.getLocalNames().pop();
              }
  break;
case 314:
					// line 1153 "DefaultRubyParser.y"
  {
                  support.setInDef(true);
                  support.getLocalNames().push(new LocalNamesElement());
              }
  break;
case 315:
					// line 1156 "DefaultRubyParser.y"
  {
                    /* NOEX_PRIVATE for toplevel */
                  yyVal = new DefnNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), new ArgumentNode(((Token)yyVals[-4+yyTop]).getPosition(), (String) ((Token)yyVals[-4+yyTop]).getValue()), ((Node)yyVals[-2+yyTop]), new ScopeNode(getPosition(((Node)yyVals[-1+yyTop])), ((LocalNamesElement) support.getLocalNames().peek()).getNamesArray(), ((Node)yyVals[-1+yyTop])), Visibility.PRIVATE);
                  support.getLocalNames().pop();
                  support.setInDef(false);
              }
  break;
case 316:
					// line 1162 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_FNAME);
              }
  break;
case 317:
					// line 1164 "DefaultRubyParser.y"
  {
                  support.setInSingle(support.getInSingle() + 1);
                  support.getLocalNames().push(new LocalNamesElement());
                  lexer.setState(LexState.EXPR_END); /* force for args */
              }
  break;
case 318:
					// line 1168 "DefaultRubyParser.y"
  {
                  yyVal = new DefsNode(support.union(((Token)yyVals[-8+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-7+yyTop]), (String) ((Token)yyVals[-4+yyTop]).getValue(), ((Node)yyVals[-2+yyTop]), new ScopeNode(getPosition(null), ((LocalNamesElement) support.getLocalNames().peek()).getNamesArray(), ((Node)yyVals[-1+yyTop])));
                  support.getLocalNames().pop();
                  support.setInSingle(support.getInSingle() - 1);
              }
  break;
case 319:
					// line 1173 "DefaultRubyParser.y"
  {
                  yyVal = new BreakNode(((Token)yyVals[0+yyTop]).getPosition());
              }
  break;
case 320:
					// line 1176 "DefaultRubyParser.y"
  {
                  yyVal = new NextNode(((Token)yyVals[0+yyTop]).getPosition());
              }
  break;
case 321:
					// line 1179 "DefaultRubyParser.y"
  {
                  yyVal = new RedoNode(((Token)yyVals[0+yyTop]).getPosition());
              }
  break;
case 322:
					// line 1182 "DefaultRubyParser.y"
  {
                  yyVal = new RetryNode(((Token)yyVals[0+yyTop]).getPosition());
              }
  break;
case 323:
					// line 1186 "DefaultRubyParser.y"
  {
                  support.checkExpression(((Node)yyVals[0+yyTop]));
		  yyVal = ((Node)yyVals[0+yyTop]);
	      }
  break;
case 332:
					// line 1201 "DefaultRubyParser.y"
  {
                  yyVal = new IfNode(getPosition(((Token)yyVals[-4+yyTop])), support.getConditionNode(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 334:
					// line 1206 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 336:
					// line 1211 "DefaultRubyParser.y"
  {}
  break;
case 338:
					// line 1214 "DefaultRubyParser.y"
  {
                  yyVal = new ZeroArgNode(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])));
              }
  break;
case 339:
					// line 1217 "DefaultRubyParser.y"
  {
                  yyVal = new ZeroArgNode(((Token)yyVals[0+yyTop]).getPosition());
	      }
  break;
case 340:
					// line 1220 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[-1+yyTop]);

		  /* Include pipes on multiple arg type
*/
                  if (((Node)yyVals[-1+yyTop]) instanceof MultipleAsgnNode) {
		      ((Node)yyVals[-1+yyTop]).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
		  } 
              }
  break;
case 341:
					// line 1229 "DefaultRubyParser.y"
  {
                  support.getBlockNames().push(new BlockNamesElement());
	      }
  break;
case 342:
					// line 1231 "DefaultRubyParser.y"
  {
                  yyVal = new IterNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), null);
                  support.getBlockNames().pop();
              }
  break;
case 343:
					// line 1236 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[-1+yyTop]) instanceof BlockPassNode) {
                      throw new SyntaxException(getPosition(((Node)yyVals[-1+yyTop])), "Both block arg and actual block given.");
                  }
                  ((IterNode)yyVals[0+yyTop]).setIterNode(((Node)yyVals[-1+yyTop]));
                  yyVal = ((IterNode)yyVals[0+yyTop]);
              }
  break;
case 344:
					// line 1243 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 345:
					// line 1246 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 346:
					// line 1250 "DefaultRubyParser.y"
  {
                  yyVal = support.new_fcall(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 347:
					// line 1253 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 348:
					// line 1256 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 349:
					// line 1259 "DefaultRubyParser.y"
  {
                  yyVal = support.new_call(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]), null);
              }
  break;
case 350:
					// line 1262 "DefaultRubyParser.y"
  {
                  yyVal = support.new_super(((Node)yyVals[0+yyTop]), ((Token)yyVals[-1+yyTop]));
              }
  break;
case 351:
					// line 1265 "DefaultRubyParser.y"
  {
                  yyVal = new ZSuperNode(getPosition(((Token)yyVals[0+yyTop])));
              }
  break;
case 352:
					// line 1270 "DefaultRubyParser.y"
  {
                  support.getBlockNames().push(new BlockNamesElement());
	      }
  break;
case 353:
					// line 1272 "DefaultRubyParser.y"
  {
                  yyVal = new IterNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), null);
                  support.getBlockNames().pop();
              }
  break;
case 354:
					// line 1276 "DefaultRubyParser.y"
  {
                  support.getBlockNames().push(new BlockNamesElement());
	      }
  break;
case 355:
					// line 1278 "DefaultRubyParser.y"
  {
	          yyVal = new IterNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), null);
                  support.getBlockNames().pop();
              }
  break;
case 356:
					// line 1283 "DefaultRubyParser.y"
  {
                  yyVal = new WhenNode(((Token)yyVals[-4+yyTop]).getPosition(), ((ListNode)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 358:
					// line 1288 "DefaultRubyParser.y"
  {
                  yyVal = ((ListNode)yyVals[-3+yyTop]).add(new WhenNode(getPosition(((ListNode)yyVals[-3+yyTop])), ((Node)yyVals[0+yyTop]), null, null));
              }
  break;
case 359:
					// line 1291 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(getPosition(((Token)yyVals[-1+yyTop])), new WhenNode(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]), null, null));
              }
  break;
case 362:
					// line 1298 "DefaultRubyParser.y"
  {
                  Node node;
                  if (((Node)yyVals[-3+yyTop]) != null) {
                     node = support.appendToBlock(support.node_assign(((Node)yyVals[-3+yyTop]), new GlobalVarNode(getPosition(((Token)yyVals[-5+yyTop])), "$!")), ((Node)yyVals[-1+yyTop]));
		  } else {
		     node = ((Node)yyVals[-1+yyTop]);
                  }
                  yyVal = new RescueBodyNode(getPosition(((Token)yyVals[-5+yyTop]), true), ((Node)yyVals[-4+yyTop]), node, ((RescueBodyNode)yyVals[0+yyTop]));
	      }
  break;
case 363:
					// line 1307 "DefaultRubyParser.y"
  {yyVal = null;}
  break;
case 364:
					// line 1309 "DefaultRubyParser.y"
  {
                  yyVal = new ArrayNode(((Node)yyVals[0+yyTop]).getPosition(), ((Node)yyVals[0+yyTop]));
	      }
  break;
case 367:
					// line 1315 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 369:
					// line 1320 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[0+yyTop]) != null) {
                      yyVal = ((Node)yyVals[0+yyTop]);
                  } else {
                      yyVal = new NilNode(getPosition(null));
                  }
              }
  break;
case 372:
					// line 1330 "DefaultRubyParser.y"
  {
                  yyVal = new SymbolNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
              }
  break;
case 374:
					// line 1335 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[0+yyTop]) instanceof EvStrNode) {
                      yyVal = new DStrNode(getPosition(((Node)yyVals[0+yyTop]))).add(((Node)yyVals[0+yyTop]));
                  } else {
                      yyVal = ((Node)yyVals[0+yyTop]);
                  }
	      }
  break;
case 376:
					// line 1344 "DefaultRubyParser.y"
  {
                  yyVal = support.literal_concat(((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 377:
					// line 1348 "DefaultRubyParser.y"
  {
                  yyVal = ((Node)yyVals[-1+yyTop]);
                  ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
		  int extraLength = ((String) ((Token)yyVals[-2+yyTop]).getValue()).length() - 1;

                  /* We may need to subtract addition offset off of first 
*/
		  /* string fragment (we optimistically take one off in
*/
		  /* ParserSupport.literal_concat).  Check token length
*/
		  /* and subtract as neeeded.
*/
		  if ((((Node)yyVals[-1+yyTop]) instanceof DStrNode) && extraLength > 0) {
		     Node strNode = ((DStrNode)((Node)yyVals[-1+yyTop])).get(0);
		     assert strNode != null;
		     strNode.getPosition().adjustStartOffset(-extraLength);
		  }
              }
  break;
case 378:
					// line 1364 "DefaultRubyParser.y"
  {
                  ISourcePosition position = support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));

		  if (((Node)yyVals[-1+yyTop]) == null) {
		      yyVal = new XStrNode(position, null);
		  } else {
		      if (((Node)yyVals[-1+yyTop]) instanceof StrNode) {
			  yyVal = new XStrNode(position, ((StrNode)yyVals[-1+yyTop]).getValue());
		      } else if (((Node)yyVals[-1+yyTop]) instanceof DStrNode) {
		          ((Node)yyVals[-1+yyTop]).setPosition(position);
		          yyVal = new DXStrNode(position).add(((Node)yyVals[-1+yyTop]));
		      } else {
			  yyVal = new DXStrNode(position).add(new ArrayNode(position, ((Node)yyVals[-1+yyTop])));
		      }
		  }
              }
  break;
case 379:
					// line 1381 "DefaultRubyParser.y"
  {
		  int options = ((RegexpNode)yyVals[0+yyTop]).getOptions();
		  Node node = ((Node)yyVals[-1+yyTop]);

		  if (node == null) {
		      yyVal = new RegexpNode(getPosition(((Token)yyVals[-2+yyTop])), "", options & ~ReOptions.RE_OPTION_ONCE);
		  } else if (node instanceof StrNode) {
		      yyVal = new RegexpNode(getPosition(((Token)yyVals[-2+yyTop])), ((StrNode) node).getValue(), options & ~ReOptions.RE_OPTION_ONCE);
		  } else {
		      if (node instanceof DStrNode == false) {
			  node = new DStrNode(getPosition(((Token)yyVals[-2+yyTop]))).add(new ArrayNode(getPosition(((Token)yyVals[-2+yyTop])), node));
		      } 

		      yyVal = new DRegexpNode(getPosition(((Token)yyVals[-2+yyTop])), options, (options & ReOptions.RE_OPTION_ONCE) != 0).add(node);
		    }
	       }
  break;
case 380:
					// line 1398 "DefaultRubyParser.y"
  {
                   yyVal = new ZArrayNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
	       }
  break;
case 381:
					// line 1401 "DefaultRubyParser.y"
  {
		   yyVal = ((ListNode)yyVals[-1+yyTop]);
                   ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
	       }
  break;
case 382:
					// line 1406 "DefaultRubyParser.y"
  {
                   yyVal = new ArrayNode(getPosition(null));
	       }
  break;
case 383:
					// line 1409 "DefaultRubyParser.y"
  {
                   yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[-1+yyTop]) instanceof EvStrNode ? new DStrNode(getPosition(((ListNode)yyVals[-2+yyTop]))).add(((Node)yyVals[-1+yyTop])) : ((Node)yyVals[-1+yyTop]));
	       }
  break;
case 385:
					// line 1414 "DefaultRubyParser.y"
  {
                   yyVal = support.literal_concat(((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
	       }
  break;
case 386:
					// line 1418 "DefaultRubyParser.y"
  {
                   yyVal = new ZArrayNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
	       }
  break;
case 387:
					// line 1421 "DefaultRubyParser.y"
  {
		   yyVal = ((ListNode)yyVals[-1+yyTop]);
                   ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
	       }
  break;
case 388:
					// line 1426 "DefaultRubyParser.y"
  {
                   yyVal = new ArrayNode(getPosition(null));
	       }
  break;
case 389:
					// line 1429 "DefaultRubyParser.y"
  {
                   yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[-1+yyTop]));
	       }
  break;
case 390:
					// line 1433 "DefaultRubyParser.y"
  {
                   yyVal = new StrNode(getPosition(null), "");
	       }
  break;
case 391:
					// line 1436 "DefaultRubyParser.y"
  {
                   yyVal = support.literal_concat(((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
	       }
  break;
case 392:
					// line 1440 "DefaultRubyParser.y"
  {
		   yyVal = null;
	       }
  break;
case 393:
					// line 1443 "DefaultRubyParser.y"
  {
                   yyVal = support.literal_concat(((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
	       }
  break;
case 394:
					// line 1447 "DefaultRubyParser.y"
  {
                   yyVal = ((Node)yyVals[0+yyTop]);
               }
  break;
case 395:
					// line 1450 "DefaultRubyParser.y"
  {
                   yyVal = lexer.getStrTerm();
		   lexer.setStrTerm(null);
		   lexer.setState(LexState.EXPR_BEG);
	       }
  break;
case 396:
					// line 1454 "DefaultRubyParser.y"
  {
		   lexer.setStrTerm(((StrTerm)yyVals[-1+yyTop]));
	           yyVal = new EvStrNode(support.union(((Token)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
	       }
  break;
case 397:
					// line 1458 "DefaultRubyParser.y"
  {
		   yyVal = lexer.getStrTerm();
		   lexer.setStrTerm(null);
		   lexer.setState(LexState.EXPR_BEG);
	       }
  break;
case 398:
					// line 1462 "DefaultRubyParser.y"
  {
		   lexer.setStrTerm(((StrTerm)yyVals[-2+yyTop]));

		   yyVal = support.newEvStrNode(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-1+yyTop]));
	       }
  break;
case 399:
					// line 1468 "DefaultRubyParser.y"
  {
                   yyVal = new GlobalVarNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
  break;
case 400:
					// line 1471 "DefaultRubyParser.y"
  {
                   yyVal = new InstVarNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
  break;
case 401:
					// line 1474 "DefaultRubyParser.y"
  {
                   yyVal = new ClassVarNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
  break;
case 403:
					// line 1480 "DefaultRubyParser.y"
  {
                   lexer.setState(LexState.EXPR_END);
                   yyVal = ((Token)yyVals[0+yyTop]);
		   ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])));
               }
  break;
case 408:
					// line 1488 "DefaultRubyParser.y"
  {
                   lexer.setState(LexState.EXPR_END);

		   /* DStrNode: :"some text #{some expression}"
*/
                   /* StrNode: :"some text"
*/
		   /* EvStrNode :"#{some expression}"
*/
		   DStrNode node;

		   if (((Node)yyVals[-1+yyTop]) instanceof DStrNode) {
		       node = (DStrNode) ((Node)yyVals[-1+yyTop]);
		   } else {
                       ISourcePosition position = support.union(((Node)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));

                       /* We substract one since tsymbeg is longer than one
*/
		       /* and we cannot union it directly so we assume quote
*/
                       /* and subtract for it.
*/
		       position.adjustStartOffset(-1);
		       node = new DStrNode(position);
		       ((Node)yyVals[-1+yyTop]).setPosition(position);
		       node.add(new ArrayNode(position, ((Node)yyVals[-1+yyTop])));
                   }
		   yyVal = new DSymbolNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), node);
	       }
  break;
case 411:
					// line 1514 "DefaultRubyParser.y"
  {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "-@");
	       }
  break;
case 412:
					// line 1517 "DefaultRubyParser.y"
  {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "-@");
	       }
  break;
case 418:
					// line 1523 "DefaultRubyParser.y"
  { 
		   yyVal = new Token("nil", ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 419:
					// line 1526 "DefaultRubyParser.y"
  {
		   yyVal = new Token("self", ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 420:
					// line 1529 "DefaultRubyParser.y"
  { 
		   yyVal = new Token("true", ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 421:
					// line 1532 "DefaultRubyParser.y"
  {
		   yyVal = new Token("false", ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 422:
					// line 1535 "DefaultRubyParser.y"
  {
		   yyVal = new Token("__FILE__", ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 423:
					// line 1538 "DefaultRubyParser.y"
  {
		   yyVal = new Token("__LINE__", ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 424:
					// line 1542 "DefaultRubyParser.y"
  {
		   yyVal = support.gettable((String) ((Token)yyVals[0+yyTop]).getValue(), ((Token)yyVals[0+yyTop]).getPosition());
               }
  break;
case 425:
					// line 1546 "DefaultRubyParser.y"
  {
                   yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
               }
  break;
case 428:
					// line 1552 "DefaultRubyParser.y"
  {
                   yyVal = null;
               }
  break;
case 429:
					// line 1555 "DefaultRubyParser.y"
  {
                   lexer.setState(LexState.EXPR_BEG);
               }
  break;
case 430:
					// line 1557 "DefaultRubyParser.y"
  {
                   yyVal = ((Node)yyVals[-1+yyTop]);
               }
  break;
case 431:
					// line 1560 "DefaultRubyParser.y"
  {
                   yyerrok();
                   yyVal = null;
               }
  break;
case 432:
					// line 1566 "DefaultRubyParser.y"
  {
                   yyVal = ((Node)yyVals[-2+yyTop]);
                   ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])));
                   lexer.setState(LexState.EXPR_BEG);
               }
  break;
case 433:
					// line 1571 "DefaultRubyParser.y"
  {
                   yyVal = ((Node)yyVals[-1+yyTop]);
               }
  break;
case 434:
					// line 1575 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-5+yyTop])), ((ListNode)yyVals[-5+yyTop]), ((ListNode)yyVals[-3+yyTop]), ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 435:
					// line 1578 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-3+yyTop])), ((ListNode)yyVals[-3+yyTop]), ((ListNode)yyVals[-1+yyTop]), -1, ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 436:
					// line 1581 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-3+yyTop])), ((ListNode)yyVals[-3+yyTop]), null, ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 437:
					// line 1584 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(((ISourcePositionHolder)yyVals[-1+yyTop]).getPosition(), ((ListNode)yyVals[-1+yyTop]), null, -1, ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 438:
					// line 1587 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-3+yyTop])), null, ((ListNode)yyVals[-3+yyTop]), ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 439:
					// line 1590 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-1+yyTop])), null, ((ListNode)yyVals[-1+yyTop]), -1, ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 440:
					// line 1593 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((Token)yyVals[-1+yyTop])), null, null, ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 441:
					// line 1596 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(((BlockArgNode)yyVals[0+yyTop])), null, null, -1, ((BlockArgNode)yyVals[0+yyTop]));
               }
  break;
case 442:
					// line 1599 "DefaultRubyParser.y"
  {
                   yyVal = new ArgsNode(getPosition(null), null, null, -1, null);
               }
  break;
case 443:
					// line 1603 "DefaultRubyParser.y"
  {
                   yyerror("formal argument cannot be a constant");
               }
  break;
case 444:
					// line 1606 "DefaultRubyParser.y"
  {
                   yyerror("formal argument cannot be an instance variable");
               }
  break;
case 445:
					// line 1609 "DefaultRubyParser.y"
  {
                   yyerror("formal argument cannot be a class variable");
               }
  break;
case 446:
					// line 1612 "DefaultRubyParser.y"
  {
                   String identifier = (String) ((Token)yyVals[0+yyTop]).getValue();
                   if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                       yyerror("formal argument must be local variable");
                   } else if (((LocalNamesElement) support.getLocalNames().peek()).isLocalRegistered(identifier)) {
                       yyerror("duplicate argument name");
                   }
		   /* Register new local var or die trying (side-effect)
*/
                   ((LocalNamesElement) support.getLocalNames().peek()).getLocalIndex(identifier);
                   yyVal = ((Token)yyVals[0+yyTop]);
               }
  break;
case 447:
					// line 1624 "DefaultRubyParser.y"
  {
                    yyVal = new ListNode(((ISourcePositionHolder)yyVals[0+yyTop]).getPosition());
                    ((ListNode) yyVal).add(new ArgumentNode(((ISourcePositionHolder)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue()));
               }
  break;
case 448:
					// line 1628 "DefaultRubyParser.y"
  {
                   ((ListNode)yyVals[-2+yyTop]).add(new ArgumentNode(((ISourcePositionHolder)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue()));
                   ((ListNode)yyVals[-2+yyTop]).setPosition(support.union(((ListNode)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
		   yyVal = ((ListNode)yyVals[-2+yyTop]);
               }
  break;
case 449:
					// line 1634 "DefaultRubyParser.y"
  {
                   String identifier = (String) ((Token)yyVals[-2+yyTop]).getValue();

                   if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                       yyerror("formal argument must be local variable");
                   } else if (((LocalNamesElement) support.getLocalNames().peek()).isLocalRegistered(identifier)) {
                       yyerror("duplicate optional argument name");
                   }
		   ((LocalNamesElement) support.getLocalNames().peek()).getLocalIndex(identifier);
                   yyVal = support.assignable(((Token)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 450:
					// line 1646 "DefaultRubyParser.y"
  {
                  yyVal = new BlockNode(getPosition(((Node)yyVals[0+yyTop]))).add(((Node)yyVals[0+yyTop]));
              }
  break;
case 451:
					// line 1649 "DefaultRubyParser.y"
  {
                  yyVal = support.appendToBlock(((ListNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
              }
  break;
case 454:
					// line 1655 "DefaultRubyParser.y"
  {
                  String identifier = (String) ((Token)yyVals[0+yyTop]).getValue();

                  if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                      yyerror("rest argument must be local variable");
                  } else if (((LocalNamesElement) support.getLocalNames().peek()).isLocalRegistered(identifier)) {
                      yyerror("duplicate rest argument name");
                  }
		  ((Token)yyVals[-1+yyTop]).setValue(new Integer(((LocalNamesElement) support.getLocalNames().peek()).getLocalIndex(identifier)));
                  yyVal = ((Token)yyVals[-1+yyTop]);
              }
  break;
case 455:
					// line 1666 "DefaultRubyParser.y"
  {
                  ((Token)yyVals[0+yyTop]).setValue(new Integer(-2));
                  yyVal = ((Token)yyVals[0+yyTop]);
              }
  break;
case 458:
					// line 1673 "DefaultRubyParser.y"
  {
                  String identifier = (String) ((Token)yyVals[0+yyTop]).getValue();

                  if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                      yyerror("block argument must be local variable");
                  } else if (((LocalNamesElement) support.getLocalNames().peek()).isLocalRegistered(identifier)) {
                      yyerror("duplicate block argument name");
                  }
                  yyVal = new BlockArgNode(getPosition(((Token)yyVals[-1+yyTop])), ((LocalNamesElement) support.getLocalNames().peek()).getLocalIndex(identifier), identifier);
              }
  break;
case 459:
					// line 1684 "DefaultRubyParser.y"
  {
                  yyVal = ((BlockArgNode)yyVals[0+yyTop]);
              }
  break;
case 460:
					// line 1687 "DefaultRubyParser.y"
  {
	          yyVal = null;
	      }
  break;
case 461:
					// line 1691 "DefaultRubyParser.y"
  {
                  if (!(((Node)yyVals[0+yyTop]) instanceof SelfNode)) {
		      support.checkExpression(((Node)yyVals[0+yyTop]));
		  }
		  yyVal = ((Node)yyVals[0+yyTop]);
              }
  break;
case 462:
					// line 1697 "DefaultRubyParser.y"
  {
                  lexer.setState(LexState.EXPR_BEG);
              }
  break;
case 463:
					// line 1699 "DefaultRubyParser.y"
  {
                  if (((Node)yyVals[-2+yyTop]) instanceof ILiteralNode) {
                      yyerror("Can't define single method for literals.");
                  }
		  support.checkExpression(((Node)yyVals[-2+yyTop]));
                  yyVal = ((Node)yyVals[-2+yyTop]);
              }
  break;
case 464:
					// line 1709 "DefaultRubyParser.y"
  { /* [!null]
*/
                  yyVal = new ArrayNode(getPosition(null));
              }
  break;
case 465:
					// line 1712 "DefaultRubyParser.y"
  { /* [!null]
*/
                  yyVal = ((ListNode)yyVals[-1+yyTop]);
              }
  break;
case 466:
					// line 1715 "DefaultRubyParser.y"
  {
                  if (((ListNode)yyVals[-1+yyTop]).size() % 2 != 0) {
                      yyerror("Odd number list for Hash.");
                  }
                  yyVal = ((ListNode)yyVals[-1+yyTop]);
              }
  break;
case 468:
					// line 1724 "DefaultRubyParser.y"
  { /* [!null]
*/
                  yyVal = ((ListNode)yyVals[-2+yyTop]).addAll(((ListNode)yyVals[0+yyTop]));
              }
  break;
case 469:
					// line 1729 "DefaultRubyParser.y"
  { /* [!null]
*/
                  yyVal = new ArrayNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop])).add(((Node)yyVals[0+yyTop]));
              }
  break;
case 489:
					// line 1741 "DefaultRubyParser.y"
  {
                  yyerrok();
              }
  break;
case 492:
					// line 1747 "DefaultRubyParser.y"
  {
                  yyerrok();
              }
  break;
case 493:
					// line 1751 "DefaultRubyParser.y"
  {
                  yyVal = null;
              }
  break;
case 494:
					// line 1755 "DefaultRubyParser.y"
  {  
                  yyVal = null;
	      }
  break;
					// line 7461 "-"
        }
        yyTop -= yyLen[yyN];
        yyState = yyStates[yyTop];
        int yyM = yyLhs[yyN];
        if (yyState == 0 && yyM == 0) {
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.advance() ? yyLex.token() : 0;
          }
          if (yyToken == 0) {
            return yyVal;
          }
          continue yyLoop;
        }
        if ((yyN = yyGindex[yyM]) != 0 && (yyN += yyState) >= 0
            && yyN < yyTable.length && yyCheck[yyN] == yyState)
          yyState = yyTable[yyN];
        else
          yyState = yyDgoto[yyM];
        continue yyLoop;
      }
    }
  }

					// line 1759 "DefaultRubyParser.y"

      
    /** The parse method use an lexer stream and parse it to an AST node 
     * structure
     */
    public RubyParserResult parse(LexerSource source) {
        support.reset();
        support.setResult(new RubyParserResult());
        
        lexer.reset();
        lexer.setSource(source);
        support.setPositionFactory(lexer.getPositionFactory());
        try {
	    //yyparse(lexer, new jay.yydebug.yyAnim("JRuby", 9));
	    //yyparse(lexer, new jay.yydebug.yyDebugAdapter());
	    yyparse(lexer, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (yyException e) {
            e.printStackTrace();
        }
        
        return support.getResult();
    }

    public void init(RubyParserConfiguration configuration) {
        support.setConfiguration(configuration);
    }

    // +++
    // Helper Methods
    
    void yyerrok() {}

    /**
     * Since we can recieve positions at times we know can be null we
     * need an extra safety net here.
     */
    private ISourcePosition getPosition2(ISourcePositionHolder pos) {
        return pos == null ? lexer.getPosition(null, false) : pos.getPosition();
    }

    private ISourcePosition getPosition(ISourcePositionHolder start) {
        return getPosition(start, false);
    }

    private ISourcePosition getPosition(ISourcePositionHolder start, boolean inclusive) {
        if (start != null) {
	    return lexer.getPosition(start.getPosition(), inclusive);
	} 
	
	return lexer.getPosition(null, inclusive);
    }
}
					// line 7546 "-"
