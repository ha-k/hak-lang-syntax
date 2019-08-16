//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * @version     Last modified on Sat Aug 04 05:26:02 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; by <a href="http://www.hassan-ait-kaci.net/">the author</a>
 */

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;            // For the configuration

import hlt.language.util.SetOf;
import hlt.language.util.ArrayList;
import hlt.language.tools.Misc;
import hlt.language.io.FileTools;

/**
 * This defines a class for documenting a Jacc grammar as a set
 * of interlinked HTML files. All the work is done in the constructor.
 */
public class Documentor
{
  Date today = new Date();

  /**
   * <center><table border bgcolor="#EEEEEE" cellpadding="10"><tr>
   * <td><span style="color:red"><i>
   * <b>NB:</b> no reconfigurable parameter should be given a hard-coded value!
   * </i></span></td>
   * </tr></table></center>
   *
   * <p>
   *
   * All external reconfigurable parameters (like colors, resource file
   * names, <i>etc.</i>), should be made easily (re-)configurable from
   * persistent and editable property files (including the names of
   * these very property files themselves!).  Only default values may be
   * hard-coded, to use when all else fails - that is, when the external
   * resources may not be found or aren't available.
   *
   * <p> 
   *
   * <tt>configFile</tt> is the name of the configuration file. The
   * default file name is "<tt>Syntax.Documentor.Configuration</tt>".
   */
  String configFile = "Syntax.Documentor.Configuration";

  /**
   * Set this to <tt>true</tt> in order for Jacc -doc to copy images files.
   */
  boolean needToCopyImages = Options.copyResourceFiles();

  /**
   * Default value for the resources path directory.
   */
  String RESOURCES_PATH = Options.getResourcesPath();

  /**
   * Returns the value of the images resources path directory.
   */
  String imagesPath ()
    {
      return RESOURCES_PATH+"/images";
    }

  /**
   * Initiates the configuration of the Documentor by looking for a
   * configuration file called "<tt>Syntax.Documentor.Configuration</tt>"
   * in the resources directory.  If one is found, the value of the
   * <tt>RESOURCES_PATH</tt> is set to its namesake present in that file
   * if any; otherwise, the default value is that of
   * <tt>Options.getResourcesPath();<tt>.
   */
  final void configure ()
    {
      File configurationFile = new File(RESOURCES_PATH+"/"+configFile);
//    System.out.println("*** Accessing configuration file: "+configurationFile);
      if (configurationFile.exists())
        {
          try
            {
              configuration.load(new FileInputStream(configurationFile));
            }
	  catch (IOException e)
	    {
	      System.err.println
		("*** Can't load Jacc Documentor config file "
		 +configFile+" ("+e+")");
	    }
        }
      else
        System.err.println("*** Jacc Documentor config file "
			   +configFile+" not found (using default: "
			   +RESOURCES_PATH+"/"+configFile+")");

      configureValues();
    }

  /**
   * Configure the various redefinable parameters.
   */
  final void configureValues ()
    {
      RESOURCES_PATH = configure("RESOURCES_PATH");
//    System.err.println("*** Setting RESOURCES_PATH to \""+RESOURCES_PATH+"\"");
    }

  /**
   * <tt>configuration</tt> is the property table loaded from the
   * configuration file <tt>configFile</tt>.
   */
  Properties configuration = new Properties();  

  /**
   * Gets the specified attribute from the configuration's properties.
   * @param attribute the property's name
   */
  final String configure (String attribute)
    {
      return configuration.getProperty(attribute);      
    }

  /**
   * Gets the specified attribute to the value of configuration's
   * properties if there is one (returning <tt>null</tt> when it is
   * equal to <tt>\*</tt>). If there is not, returns the specified
   * default value.
   * @param attribute the property's name
   * @param defaultValue the default value
   */
  final String configure (String attribute, String defaultValue)
    {
      String value = configuration.getProperty(attribute);
      return (value == null) ? defaultValue : (value.equals("*") ? null : value);
    }

  static final Grammar grammar = Grammar.currentGrammar;

  static boolean hasError = false;
  boolean hasOperators = !grammar.operators.isEmpty();
  boolean hasXmlSerialization = grammar.hasXmlSerialization;
  boolean hasRoots = !grammar.roots.isEmpty() && grammar.roots.size() > 1;

  File docdir;
  String DOCDIR = Options.getGrammarPrefix()+"Doc";

  // The following 4 files are copied and exported along the generated
  // html files.

  String RAFILE = "images/arrows/misc/rarrow.gif";
  String UAFILE = "images/arrows/misc/uarrow.gif";
  String DAFILE = "images/arrows/misc/darrow.gif";
  String LGFILE = "images/hlt/hlt-logo.gif";

  String RARROW = "<IMG SRC=\""+RAFILE+"\">";
  String UARROW = "<IMG ALIGN=\"MIDDLE\" SRC=\""+UAFILE+"\">";
  String DARROW = "<IMG ALIGN=\"MIDDLE\" SRC=\""+DAFILE+"\">";
  String LOGOIM = "<IMG ALIGN=\"MIDDLE\" SRC=\""+LGFILE+"\" WIDTH=\"90\" HEIGHT=\"90\">";
  String HLTLNK = "<A HREF=\"http://www.hak-language-technologies.com/\" TARGET=\"MAIN\">"+LOGOIM+"</A>";

  /**
   * Copies the image files into the <tt>images</tt> directory if needed.
   * This works only when -rf option is on.
   */
  final void copyImageFiles () throws IOException
    {
      String imagePathName = DOCDIR+"/images";
      String arrowsPath = imagesPath() + "/arrows/misc";
      String logoPath = imagesPath() + "/hlt";

      File file = new File(imagePathName);
      if (!file.exists())
        file.mkdir();

      file = new File(imagePathName+"/hlt-logo.gif");
      if (!file.exists())
	FileTools.copy(new File(logoPath+"/hlt-logo.gif"),file);

      file = new File(imagePathName+"/rarrow.gif");
      if (!file.exists())
	FileTools.copy(new File(arrowsPath+"/rarrow.gif"),file);

      file = new File(imagePathName+"/darrow.gif");
      if (!file.exists())
	FileTools.copy(new File(arrowsPath+"/darrow.gif"),file);

      file = new File(imagePathName+"/uarrow.gif");
      if (!file.exists())
	FileTools.copy(new File(arrowsPath+"/uarrow.gif"),file);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  ArrayList terminalIndexes;
  ArrayList nonterminalIndexes;

  BufferedWriter out;
  String file;

  /**
   * Constructs a <tt>Documentor</tt> object for the grammar
   * and generates the documentation files.
   */
  Documentor ()
    {
      try
        {
 	  grammar.out.println("*** Creating documentation directory: " + DOCDIR);
          (docdir = new File(DOCDIR)).mkdir();
// 	  java.io.FilePermission docDirAccess = new java.io.FilePermission(DOCDIR,"read,write,execute");
// 	  java.security.AccessController.checkPermission(docDirAccess);
// 	  grammar.out.println("*** EXEC: ls -al docs/" + DOCDIR);
// 	  Runtime.getRuntime().exec("ls -al docs/" + DOCDIR);
// 	  grammar.out.println("*** EXEC: chmod 755 docs/" + DOCDIR);
// 	  Runtime.getRuntime().exec("chmod 755 docs/" + DOCDIR);
// 	  grammar.out.println("*** EXEC: ls -al docs/" + DOCDIR);
// 	  Runtime.getRuntime().exec("ls -al docs/" + DOCDIR);

	  configure();

          scanRules();

          grammar.out.println("*** Sorting terminals ... ");
          Misc.sort(grammar.terminals);
          terminalIndexes = indexes(grammar.terminals);

          grammar.out.println("*** Sorting nonterminals ... ");
          Misc.sort(grammar.nonterminals);
          nonterminalIndexes = indexes(grammar.nonterminals);

          if (hasOperators)
            {
              grammar.out.println("*** Sorting operators ... ");
              Misc.sort(grammar.operators);
            }

          generateHTMLFiles();
	  if (needToCopyImages)
	    {
	      System.out.println("*** Copying resources files to: "+DOCDIR);
	      copyImageFiles();
	    }
        }
      catch (IOException e)
        {
          System.err.println(e);
          System.err.println("\n*** Couldn't create documentation files");
          return;
        }
      catch (Exception e)
	{
	  e.printStackTrace();
	}
    }

  /**
   * Returns <tt>true</tt> iff the given given symbol is hidden
   * and should not be documented.
   */ 
  final boolean isHidden (GrammarSymbol s)
    {
      if (s instanceof Terminal)
        {
          Terminal t = (Terminal)s;
          return t.isError()  && !hasError
            || !t.isError() && t.isSpecial();
        }

      return s.isSpecial();
    }

  /**
   * Returns <tt>true</tt> iff the given given symbol is the
   * terminal symbol <tt>error</tt>.
   */ 
  final boolean isError (GrammarSymbol s)
    {
      return s instanceof Terminal ? ((Terminal)s).isError() : false;
    }

  /**
   * Returns a vector of symbols from the given sorted vector
   * to index with a letter.
   */
  final ArrayList indexes (ArrayList symbols)
    {
      ArrayList indexes = new ArrayList(26);

      int index = 0;

      for (; index < symbols.size(); index++)
        {
          GrammarSymbol s = (GrammarSymbol)symbols.get(index);
          if (isHidden(s)) continue;

          if (Character.isLetter(s.letterName().charAt(0)))
            break;
        }

      if (index < symbols.size())
        {
          GrammarSymbol s = (GrammarSymbol)symbols.get(index);
          char currentChar = s.initChar();
          indexes.add(s);
          index++;

          for (;index < symbols.size(); index++)
            {
              s = (GrammarSymbol)symbols.get(index);
              if (isHidden(s)) continue;

              char startChar = s.initChar();
              if (startChar != currentChar)
                {
                  currentChar = startChar;
                  indexes.add(s);
                }
            }
        }

      return indexes;
    }     

  /**
   * Creates links between the grammar symbols and the rules in
   * which they appear. This also generates rule documentation
   * files for those rules that have a doc string.
   */
  final void scanRules () throws IOException
    {
      for (Iterator rls=grammar.rules.iterator(); rls.hasNext();)
        {
          Rule rule = (Rule)rls.next();

          for (int i=1; i<rule.sequence.length; i++)
            rule.sequence[i].link(rule);

          if (rule.doc != null || rule.xmlInfo() != null)
            generateRuleFile(rule);
        }
    }

  /**
   * Creates the HTML documentation files from the linked symbols.
   */
  final void generateHTMLFiles () throws IOException
    {
      grammar.out.println("*** Generating index files ... ");

      generateScriptFile();
      generateStyleFile();
      generateTOCFile();
      generateMainFile();
      generateMainDocFile();
      generateNTFile();
      generateNTIndexFile();
      generateNTTableFile();
      generateTTFile();
      generateTTIndexFile();
      generateTTTableFile();
      if (hasRoots)
	generateRootsFile();
      if (hasOperators)
	generateOPFile();
      if (hasXmlSerialization)
	generateSerializationTable();

      grammar.out.println("*** Generating terminal symbol files ... ");
      generateTTFiles();
      grammar.out.println("*** Generating nonterminal symbol files ... ");
      generateNTFiles();

      generateYaccFormFile();

      System.out.println("*** See index.html in "+docdir.getAbsolutePath());
    }

  final void generateScriptFile () throws IOException
    {
      setOutput("script.jvs");

      String features
        = "width=800,height=800,"+
        "resizable,scrollbars=no,status=no,"+
        "directories=no,hotkeys=no,menubar=no";

      wl("function showRuleDoc (ref)");
      wl("  {");
      wl("    window.open(ref+\".html\",ref,\""+features+"\").focus();");
      wl("  }");

      out.close();
    }

  final void generateStyleFile () throws IOException
    {
      setOutput("style.css");

      wl(".KBD       { color: #AA5577; font-family: courier }");
      wl(".CODE      { color: #448899; font-family: courier }");
      wl(".TINY      { color: #AA0088; font-family: courier; font-size: x-small }");
      wl(".CENTER    { text-align: center }");
      wl(".OUTDENT   { margin-left: -2em }");

  //     wl("BODY       { margin-left: 2em; margin-right: 2em;");
  //     wl("             font-size: 16pt;");
  //     wl("             font-family: sans-serif }");

      wl("BODY       { margin-left: 2em; margin-right: 2em }");
      wl("BODY.MAIN  { background-color: #CCCCFF }");
      wl("BODY.TOC   { background-color: #FFFFCC }");
      wl("BODY.INDEX { margin-left: 1em; margin-right: 0em;");
      wl("             background-color: #CCFFFF }");

  //     wl("H1         { font-size: 125% }");
  //     wl("H2         { font-size: 110% }");
  //     wl("H3, H4     { font-size: 105% }");
  //     wl("H5, H6     { font-size: 100%; font-style: italic }");
  //     wl("A:link     { color: blue }");
  //     wl("A:visited  { color: blue }");
  //     wl("A:active   { color: purple }");

      wl("A:hover    { color: orange }");
      wl("A.SYMBOL   { color: #11AA55; text-decoration: none }");
      wl("A.INDEX    { text-decoration: none }");
      wl("DIV        { background: white; width: 100%; padding: 1em;");
      wl("             border: black }");
      out.close();
    }

  // BLOCKQUOTE style:
  String bqstyle = "margin-top: -10pt; margin-bottom: -7pt; margin-left: 1.3em; margin-right: 0pt";

  String GRAMMAR
    = Options.getGrammarPrefix()+"."+Options.getGrammarSuffix();
  String MAIN_BODY
  //    = "<BODY CLASS=\"MAIN\"  LINK=\"BLUE\" VLINK=\"BLUE\" ALINK=\"PURPLE\">";
    = "<BODY CLASS=\"MAIN\">";
  String TOC_BODY
  //    = "<BODY CLASS=\"TOC\"   LINK=\"BLUE\" VLINK=\"BLUE\" ALINK=\"PURPLE\">";
    = "<BODY CLASS=\"TOC\">";
  String INDEX_BODY
  //    = "<BODY CLASS=\"INDEX\" LINK=\"BLUE\" VLINK=\"BLUE\" ALINK=\"PURPLE\">";
    = "<BODY CLASS=\"INDEX\">";
  String LINK_STYLE
    = "<LINK REL=\"STYLESHEET\" TYPE=\"text/css\" HREF=\"style.css\">";
  String LOAD_SCRIPT
    = "<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"script.jvs\"></SCRIPT>";

  final void preamble (String title, boolean loadScript) throws IOException
    {
      wl("<HTML>");
      wl("<HEAD>");
      wl(LINK_STYLE);
      if (loadScript) wl(LOAD_SCRIPT);
      wl("<TITLE>");
      wl(title);
      wl("</TITLE>");
      wl("</HEAD>");
    }

  final void generateTOCFile () throws IOException
    {
      setOutput("toc.html");

      preamble("Table of Contents",false);

      wl(TOC_BODY);

      wl("<CENTER>");

      wl("<TABLE WIDTH=\"100%\">");

      wl("<TR><TD ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">"+HLTLNK+"</TD>");

      wl("<TD ALIGN=\"CENTER\" VALIGN=\"MIDDLE\"><TABLE WIDTH=\"70%\">");
      wl("<TR><TH ALIGN=\"CENTER\">");
      wl("<SPAN STYLE=\"FONT-SIZE:LARGER\">Hyperdocumentation for grammar <TT>"
         +GRAMMAR+"</TT></SPAN>");
      wl("</TH></TR>");

      wl("</TABLE>");

      wl("<TABLE BGCOLOR=\"WHITE\" WIDTH=\"75%\"BORDER=3 CELLPADDING=\"5\">");
      wl("<TR>");
      wl("<TH>");
      wl("<A HREF=\"MainDoc.html\" TARGET=\"MAIN\">Main</A>");
      wl("</TH>");
      wl("<TH>");
      wl("<A HREF=\""+grammar.startSymbol().htmlFileName()+"\" TARGET=\"MAIN\">Start</A>");
      wl("</TH>");
      if (hasRoots)
        {
          wl("<TH>");
          wl("<A HREF=\"Roots.html\" TARGET=\"MAIN\">Roots</A>");
          wl("</TH>");
        }
      wl("<TH>");
      wl("<A HREF=\"NT.html\" TARGET=\"MAIN\">Nonterminals</A>");
      wl("</TH>");
      wl("<TH>");
      wl("<A HREF=\"TT.html\" TARGET=\"MAIN\">Terminals</A>");
      wl("</TH>");
      if (hasOperators)
        {
          wl("<TH>");
          wl("<A HREF=\"OP.html\" TARGET=\"MAIN\">Operators</A>");
          wl("</TH>");
        }
      wl("<TH>");
      wl("<A HREF=\"YaccForm.html\" TARGET=\"MAIN\">Yacc Form</A>");
      wl("</TH>");
      if (hasXmlSerialization)
        {
          wl("<TH>");
          wl("<A HREF=\"XML.html\" TARGET=\"MAIN\">Xml Serialization</A>");
          wl("</TH>");
        }
      wl("</TR>");
      wl("</TABLE>");

      wl("</TD></TR>");
      wl("</TABLE>");

      wl("<SPAN STYLE=\"FONT-SIZE:XX-SMALL\">");
      wl("Documentation generated on "+today.toString());
      wl("</SPAN>");

      wl("</CENTER>");
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateMainFile () throws IOException
    {
      setOutput("index.html");

      preamble("Hyperdocumentation for "+GRAMMAR,false);

      wl("<FRAMESET ROWS=\"20%,*\">");
      wl("<FRAME NORESIZE SRC=\"toc.html\" NAME=\"TOC\">");
      wl("<FRAME SRC=\"MainDoc.html\" NAME=\"MAIN\">");
      wl("<NOFRAMES>");
      wl(MAIN_BODY);
      wl("<H1><A HREF=\"MainDoc.html\">Main</A></H1>");
      wl("<H1>"+grammar.startSymbol().htmlRef("Start")+"</H1>");
      wl("<H1><A HREF=\"NT.html\">Nonterminals</A></H1>");
      wl("<H1><A HREF=\"TT.html\">Terminals</A></H1>");
      if (hasRoots)
        wl("<H1><A HREF=\"Roots.html\">Roots</A></H1>");
      if (hasOperators)
        wl("<H1><A HREF=\"OP.html\">Operators</A></H1>");
      wl("<H1><A HREF=\"YaccForm.html\">Main</A></H1>");
      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</NOFRAMES>");
      wl("</FRAMESET>");
      wl("</HTML>");

      out.close();
    }

  final void generateMainDocFile () throws IOException
    {
      setOutput("MainDoc.html");

      preamble("Main Documentation for "+GRAMMAR,false);

      wl(MAIN_BODY);

      wl("<H1 ALIGN=\"CENTER\">Main Documentation for grammar <SPAN STYLE=\"COLOR:MAROON\">"
         +grammar.name()
         +"</SPAN></H1>");

      if (grammar.mainDoc != null)
        formatDoc(grammar.mainDoc.toString());

      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateRootsFile () throws IOException
    {
      NonTerminal[] roots = new NonTerminal[grammar.roots.size()-1];

      int i = 0;
      for (Iterator it=grammar.roots.keySet().iterator(); it.hasNext();)
        {
          NonTerminal n = (NonTerminal)it.next();
          if (!n.isStart())
            roots[i++] = n;
        }

      Misc.sort(roots);
      String plural = roots.length>1?"s":"";

      setOutput("Roots.html");

      preamble("Root symbols of grammar "+GRAMMAR,false);

      wl(MAIN_BODY);

      wl("<H1 ALIGN=\"CENTER\">Root symbols of grammar <SPAN STYLE=\"COLOR:MAROON\">"
         +grammar.name()
         +"</SPAN></H1>");

      wl("<HR><P>");
      wl("<TABLE WIDTH=\"100%\" BGCOLOR=\"WHITE\" CELLPADDING=\"15\"><TR><TD>");

      wl("<SPAN STYLE=\"FONT-SIZE:LARGER\">This grammar allows partial parsing: in addition to");
      wl("the start symbol (<A CLASS=\"SYMBOL\"HREF=\""
         +grammar.startSymbol().htmlFileName()+"\">"
         +grammar.startSymbol().label()+"</A>), the following nonterminal symbol"
         +plural+" may be parsed as stand-alone unit"+plural+":"
         +"</SPAN>");

      wl("<P>");
      wl("<UL>");

      for (i=0; i<roots.length; i++)
        wl("<P><LI><A CLASS=\"SYMBOL\" HREF=\""
           +roots[i].htmlFileName()+"\" TARGET=\"MAIN\">"
           +roots[i].label()+"</A>");

      wl("</UL>");
      wl("<P>");

      wl("</TD></TR></TABLE>");

      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateYaccFormFile () throws IOException
    {
      grammar.RULE_ORDER_MODE = true;
      Misc.sort(grammar.nonterminals);

      setOutput("YaccForm.html");

      preamble("Yacc Form of grammar "+GRAMMAR,true);

      wl(MAIN_BODY);

      wl("<H1 ALIGN=\"CENTER\">Yacc Form of grammar <SPAN STYLE=\"COLOR:MAROON\">"
         +grammar.name()
         +"</SPAN></H1>");

      wl("<CENTER>");
      wl("<HR>");
      wl("<SPAN STYLE=\"COLOR:#F07070; FONT-SIZE:X-SMALL\">"+hltCopyrightNotice()+"</SPAN>");
      wl("<P>");
      wl("<SPAN STYLE=\"COLOR:#F00770; FONT-SIZE:XX-SMALL\"\">\n"+
         "This yacc grammar was generated on "+today+
         " from the annotated Jacc grammar file <SPAN STYLE=\"COLOR:BROWN\"><TT>"+
         grammar.name()+"</TT></SPAN>.");
      wl("<HR><P>");

      wl("<TABLE BGCOLOR=\"WHITE\" BORDER=\"5\" CELLPADDING=\"20\">");
      wl("<TR><TD>");

      wl("<PRE>");

      ArrayList ops = grammar.isDynamic ? new ArrayList() : null;

      for (int i=0; i<grammar.ncount; i++)
        {
          NonTerminal n = (NonTerminal)grammar.nonterminals.get(i);

          if (isHidden(n)) continue;
          if (n.isOperator)
            {
              ops.add(n);
              continue;
            }

          Iterator rls = n.rules.iterator();
          formatYaccRule((Rule)rls.next(),n,true);
          for (;rls.hasNext();)
            formatYaccRule((Rule)rls.next(),n,false);
          wl("\n\t;\n");
        }    

      if (ops != null)
        for (int i=0; i<ops.size(); i++)
          {
            NonTerminal op = (NonTerminal)ops.get(i);
            Iterator rls = op.rules.iterator();
            formatYaccRule((Rule)rls.next(),op,true);
            for (;rls.hasNext();)
              formatYaccRule((Rule)rls.next(),op,false);
            wl("\n\t;\n");
          }

      wl("</PRE>");

      wl("</TD></TR>");
      wl("</TABLE>");
      wl("</CENTER>");

      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void hltCopyrightStamp () throws IOException
    {
      wl("<P>");
      wl("<HR>");
      wl("<SPAN STYLE=\"COLOR:#F07070; FONT-SIZE:XX-SMALL\">");
      wl("<P ALIGN=\"RIGHT\">"+hltCopyrightNotice()+"</SPAN>");
    }

  final String hltCopyrightNotice () throws IOException
    {
      GregorianCalendar calendar = new GregorianCalendar();
      return
        "<B>Copyright &copy; "+ calendar.get(calendar.YEAR) +
        " by <A HREF=\"http://www.hassan-ait-kaci.net/\">Hassan A&iuml;t-Kaci</A>; All Rights Reserved.</B>";
    }

  final String yaccFormRef (GrammarSymbol symbol)
    {
      return "<A CLASS=\"SYMBOL\" HREF=\"#"+symbol.name()+"\">"+symbol.name()+"</A>";      
    }

  final String yaccFormAnchor (NonTerminal symbol)
    {
      return "<A CLASS=\"SYMBOL\" NAME=\""+symbol.name()+"\"></A>";      
    }

  final String quotify (String label, GrammarSymbol gs)
    {
      if (gs instanceof Terminal && !Misc.isUpperCase(gs.name()))
        return "'"+label+"'";

      return label;
    }

  final void formatYaccRule (Rule r, NonTerminal n, boolean isFirst) throws IOException
    {
      if (isFirst)
        {
          w(yaccFormAnchor(n));
          w(n.htmlRef());
        }

      yaccRuleLink(r,n,isFirst);

      if (r != null)
        if (r.sequence.length == 1)
          w("<SPAN STYLE=\"COLOR:ORANGE\">/* empty */</SPAN>");
        else
          for (int i=1; i<r.sequence.length; i++)
            {
              GrammarSymbol gs = r.sequence[i];
              String symbol = gs instanceof Terminal ?  gs.htmlRef() : yaccFormRef(gs);

              if (isError(gs))
                symbol = "<B><SPAN STYLE=\"COLOR:#4444FF\">"+gs.name()+"</SPAN></B>";
              else
                {
                  if (isHidden(gs)) continue;
                  symbol = quotify(symbol,gs);
                }

              w(symbol+" ");
            }
    }

  final void yaccRuleLink (Rule r, GrammarSymbol s, boolean isFirst) throws IOException
    {
      String neck = isFirst ? ":" : "|";

      w("\n\t");

      if (r == null)
        w("<BLINK><SPAN STYLE=\"COLOR:red\">&lt;THIS SYMBOL HAS NO RULES!&gt;</SPAN></BLINK>");
      else
        if (r.doc == null && r.xmlInfo() == null)
          w(neck);
        else
          w("<A CLASS=\"SYMBOL\" HREF=\"javascript:showRuleDoc('"+r.refName()
            +"')\">"+neck+"</A>");

      w(" ");
    }   

  final void generateNTFile () throws IOException
    {
      setOutput("NT.html");

      preamble("Non Terminals",false);
      wl("<FRAMESET COLS=\"12%,*\">");
      wl("<FRAME NORESIZE SRC=\"NT_index.html\" NAME=\"INDEX\">");
      wl("<FRAME SRC=\"NT_table.html\" NAME=\"TABLE\">");
      wl("<NOFRAMES>");
      wl(MAIN_BODY);
      wl("<H1><A HREF=\"NT_index.html\">Index of Nonterminals</A></H1>");
      wl("<H1><A HREF=\"NT_table.html\">Table of Nonterminals</A></H1>");
      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</NOFRAMES>");
      wl("</FRAMESET>");
      wl("</HTML>");

      out.close();
    }

  final void generateNTIndexFile () throws IOException
    {
      setOutput("NT_index.html");

      preamble(file,false);

      wl(INDEX_BODY);

      wl("<H1>Index</H1>");

      wl("<CENTER>");

      wl("<TABLE>");

      wl("<TR><TH ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">");
      wl("<A CLASS=\"INDEX\" HREF=\"NT_table.html#TOP\" TARGET=\"TABLE\">"
         +UARROW+"</A>");
      wl("</TH></TR>");

      for (int i=0; i<nonterminalIndexes.size(); i++)
        {
          NonTerminal n = (NonTerminal)nonterminalIndexes.get(i);
          String indexRef = String.valueOf(n.initChar());
          wl("<TR><TH ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">");
          wl("<A CLASS=\"INDEX\" HREF=\"NT_table.html#"
             +indexRef+"\" TARGET=\"TABLE\"><TT>"
             +indexRef+"</TT></A>");
          wl("</TH></TR>");
        }

      wl("<TR><TH ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">");
      wl("<A CLASS=\"INDEX\" HREF=\"NT_table.html#BOTTOM\" TARGET=\"TABLE\">"
         +DARROW+"</A>");
      wl("</TH></TR>");

      wl("</TABLE>");

      wl("</CENTER>");
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateNTTableFile () throws IOException
    {
      setOutput("NT_table.html");

      preamble(file,false);

      wl(MAIN_BODY);

      wl("<A NAME=\"TOP\"></A><H1>Nonterminals</H1>");
      wl("<CENTER>");
      wl("<TABLE BGCOLOR=\"WHITE\" BORDER CELLPADDING=4>");

      int index = 0;

      for (Iterator ns=grammar.nonterminals.iterator(); ns.hasNext();)
        {
          NonTerminal n = (NonTerminal)(ns.next());

          if (isHidden(n)) continue;

          String ref = n.htmlRef();
          wl("<TR>");
          if (index < nonterminalIndexes.size() && n == nonterminalIndexes.get(index))
            {
              wl("<TD></TD></TR><TR>");
              ref="<A NAME=\""+String.valueOf(n.initChar())+"\">"+ref+"</A>";
              index++;
            }
          wl("<TD>"+ref+"</TD>");
          wl("</TR>");
        }

      wl("</TABLE>");
      wl("</CENTER>");
      wl("<A NAME=\"BOTTOM\">&nbsp;</A>");
      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateTTFile () throws IOException
    {
      setOutput("TT.html");

      preamble("Terminals",false);

      wl("<FRAMESET COLS=\"12%,*\">");
      wl("<FRAME NORESIZE SRC=\"TT_index.html\" NAME=\"INDEX\">");
      wl("<FRAME SRC=\"TT_table.html\" NAME=\"TABLE\">");
      wl("<NOFRAMES>");
      wl(MAIN_BODY);
      wl("<H1><A HREF=\"TT_index.html\">Index of Terminals</A></H1>");
      wl("<H1><A HREF=\"TT_table.html\">Table of Terminals</A></H1>");
      wl("</BODY>");
      wl("</NOFRAMES>");
      wl("</FRAMESET>");
      wl("</HTML>");

      out.close();
    }

  final void generateTTIndexFile () throws IOException
    {
      setOutput("TT_index.html");

      preamble(file,false);

      wl(INDEX_BODY);

      wl("<H1>Index</H1>");
      wl("<CENTER>");
      wl("<TABLE>");

      wl("<TR><TH ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">");
      wl("<A CLASS=\"INDEX\" HREF=\"TT_table.html#TOP\" TARGET=\"TABLE\">"
         +UARROW+"</A>");
      wl("</TH></TR>");

      for (int i=0; i<terminalIndexes.size(); i++)
        {
          Terminal t = (Terminal)terminalIndexes.get(i);
          String indexRef = String.valueOf(t.initChar());
          wl("<TR><TH ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">");
          wl("<A CLASS=\"INDEX\" HREF=\"TT_table.html#"
             +indexRef+"\" TARGET=\"TABLE\"><TT>"
             +indexRef+"</TT></A>");
          wl("</TH></TR>");
        }

      wl("<TR><TH ALIGN=\"CENTER\" VALIGN=\"MIDDLE\">");
      wl("<A CLASS=\"INDEX\" HREF=\"TT_table.html#BOTTOM\" TARGET=\"TABLE\">"
         +DARROW+"</A>");
      wl("</TH></TR>");

      wl("</TABLE>");
      wl("</CENTER>");

      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateTTTableFile () throws IOException
    {
      setOutput("TT_table.html");

      preamble(file,false);

      wl(MAIN_BODY);

      wl("<A NAME=\"TOP\"></A><H1>Terminals</H1>");

      wl("<CENTER>");

      wl("<TABLE BGCOLOR=\"WHITE\" BORDER CELLPADDING=4>");
      wl("<TR>");
      wl("<TH>Symbol</TH>");
      wl("<TH>Associativity</TH>");
      wl("<TH>Looseness</TH>");
      wl("<TH>Precedence</TH>");
      wl("</TR>");

      int index = 0;

      for (Iterator ts=grammar.terminals.iterator(); ts.hasNext();)
        {
          Terminal t = (Terminal)(ts.next());

          if (isHidden(t)) continue;

          String ref = t.htmlRef();

          wl("<TR>");
          if (index < terminalIndexes.size() && t == terminalIndexes.get(index))
            {
              wl("<TD></TD></TR><TR>");
              ref="<A NAME=\""+String.valueOf(t.initChar())+"\">"+ref+"</A>";
              index++;
            }
          wl("<TD ALIGN=\"CENTER\">"+ref+"</TD>");
          wl("<TD ALIGN=\"CENTER\">"+grammar.associativity(t)+"</TD>");
          wl("<TD ALIGN=\"CENTER\">"+grammar.prologPrecedence(t.precedence)+"</TD>");
          wl("<TD ALIGN=\"CENTER\">"+t.precedence+"</TD>");
          wl("</TR>");
        }

      wl("</TABLE>");
      wl("</CENTER>");
      wl("<A NAME=\"BOTTOM\">&nbsp;</A>");
      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateOPFile () throws IOException
    {
      setOutput("OP.html");

      preamble(file,false);

      wl(MAIN_BODY);

      wl("<H1>Operators</H1>");

      wl("<CENTER>");
      wl("<TABLE BGCOLOR=\"WHITE\" BORDER CELLPADDING=4>");
      wl("<TR>");
      wl("<TH>Operator</TH>");
      wl("<TH>Category</TH>");
      wl("<TH>Specifier</TH>");
      wl("<TH>Looseness</TH>");
      wl("<TH>Precedence</TH>");
      wl("</TR>");

      for (Iterator ops=grammar.operators.iterator(); ops.hasNext();)
        {
          Operator o = (Operator)(ops.next());

          wl("<TR>");
          wl("<TD ALIGN=\"CENTER\"><TT>"+o.name+"</TT></TD>");
          wl("<TD>"+o.category.htmlRef()+"</TD>");
          wl("<TD ALIGN=\"CENTER\">"+o.specifier()+"</TD>");
          wl("<TD ALIGN=\"CENTER\">"+grammar.prologPrecedence(o.precedence)+"</TD>");
          wl("<TD ALIGN=\"CENTER\">"+o.precedence+"</TD>");
          wl("</TR>");
        }
      wl("</TABLE>");
      wl("</CENTER>");
      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void generateSerializationTable () throws IOException
    {
      setOutput("XML.html");

      preamble(file,false);

      wl(MAIN_BODY);

      wl("<H1 ALIGN=\"CENTER\">Summary of XML Serialization Patterns for Grammar <SPAN STYLE=\"COLOR:MAROON\">"
         +grammar.name()
         +"</SPAN></H1>");

      wl("<CENTER>");
      wl("<TABLE BGCOLOR=\"WHITE\" CELLPADDING=10>");
      wl("<TR>");
      wl("<TH ALIGN=\"LEFT\">Terminal or Rule:</TH>");
      wl("<TH ALIGN=\"LEFT\">XML Serialization Pattern:</TH>");
      wl("</TR>");

      for (Iterator ts=grammar.terminals.iterator(); ts.hasNext();)
        {
          Terminal t = (Terminal)(ts.next());
	  if (t.hasXmlInfo())
	    {
	      wl("<TR VALIGN=\"TOP\"><TD>");
	      wl("<BLOCKQUOTE><TABLE BORDER BGCOLOR=\"#EEEEEE\" CELLPADDING=\"20\"><TR><TD>");
	      wl(t.htmlRef());
	      wl("\n</TD></TR></TABLE></BLOCKQUOTE>");
	      wl("</TD><TD ALIGN=\"LEFT\">");
	      XmlDescriptor x = new XmlDescriptor();
	      generateXmlPatternDescription(t,x);
	      generateTerminalLegend(t.xmlInfo(),x);
	      wl("</TD></TR>");
	    }
        }

      for (Iterator rs=grammar.rules.iterator(); rs.hasNext();)
        {
          Rule r = (Rule)(rs.next());
	  if (r.hasXmlInfo())
	    {
	      wl("<TR VALIGN=\"TOP\"><TD>");
	      wl("<BLOCKQUOTE><TABLE BORDER BGCOLOR=\"#EEEEEE\" CELLPADDING=\"20\"><TR><TD>");
	      wl(ruleHtmlString(r));
	      wl("\n</TD></TR></TABLE></BLOCKQUOTE>");
	      wl("</TD><TD ALIGN=\"LEFT\">");
	      XmlDescriptor x = new XmlDescriptor();
	      generateXmlPatternDescription(r,x);
	      generateRuleLegend(r.xmlInfo(),x);
	      wl("</TD></TR>");
	    }
        }

      wl("</TABLE>");
      wl("</CENTER>");
      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  /**
   * Return an HTML-formatted string for this rule.
   */
  public String ruleHtmlString (Rule r)    
    {
      StringBuilder buf = new StringBuilder("<TABLE>");

      buf.append("<TR VALIGN=\"TOP\">");
      buf.append("<TD>");
      buf.append(r.head().htmlRef()).append("&emsp;").append(RARROW);
      buf.append("</TD>");
      buf.append("<TD>");
      buf.append("<TD>");
      buf.append("<TABLE>");
      for (int i=1; i<r.sequence.length; i++)
	{
	  buf.append("<TR><TD>");
	  buf.append(r.body(i).htmlRef());
	  buf.append("</TD></TR>");
	}
      buf.append("</TABLE>");
      buf.append("</TD>");
      buf.append("</TR>");

      return buf.append("</TABLE>").toString();
    }

  final void generateTTFiles () throws IOException
    {
      for (Iterator ts=grammar.terminals.iterator(); ts.hasNext();)
        {
          Terminal t = (Terminal)(ts.next());

          if (isHidden(t)) continue;

          setOutput(t.htmlFileName());

          preamble(file,true);

          wl(MAIN_BODY);

          wl("<H1 ALIGN=\"CENTER\">Terminal symbol <SPAN STYLE=\"COLOR:MAROON\">"
             +quotify(t.label(),t)+"</SPAN></H1>");

          wl("<SPAN STYLE=\"FONT-SIZE:LARGER\">Occurrences of symbol");
          wl(quotify("<SPAN STYLE=\"COLOR:MAROON; FONT-SIZE:LARGER\">"+t.label()+"</SPAN>",t));
          wl("in grammar rules:</SPAN><P>");

          wl("<CENTER>");
          wl("<TABLE BGCOLOR=\"WHITE\" BORDER=\"5\" CELLPADDING=\"20\">");
          wl("<TR><TD>");
          wl("<TABLE BGCOLOR=\"WHITE\" CELLPADDING=\"6\">");

          for (Iterator rls=t.ruleOccurrences.iterator(); rls.hasNext();)
            formatRule((Rule)rls.next(),t);

          wl("</TABLE>");
          wl("</TD></TR>");
          wl("</TABLE>");
          wl("</CENTER>");

          if (t.doc != null)
            {
              formatDoc(t.doc.toString());
              wl("<P><HR><P>");
            }

          if (t.xmlInfo()!= null)
	    {
	      wl("<P>");
	      generateXmlInfoDescription(t);
              wl("<P>");
	    }
// 	  else
// 	    {
// 	      String !name = XmlInfo.colorName(t.name());
// 	      String attributes = "";

// 	      wl("<P>");
// 	      wl("<TABLE WIDTH=\"100%\" BGCOLOR=\"WHITE\" CELLPADDING=\"15\"><TR><TD>");

// 	      wl("<H4 ALIGN=\"CENTER\">XML Serialization Annotation</H4>");

// 	      w("This terminal has no explicit serialization annotation;. ");
// 	      wl("This means that, by default, terminal "+quotify(t.label(),t)
// 		 +" is XML-serialized as follows:");

// 	      wl("<P><BLOCKQUOTE><TABLE BORDER BGCOLOR=\"#EEEEEE\" CELLPADDING=\"20\"><TR><TD>");

// 	      wl("<P>");

// 	      wl("<B><TT>");

// 	      wl("&lt;"+name+"&gt;"+
// 		 XmlInfo.colorChild("$value")+
// 		 "&lt;/"+name+"&gt;");

// 	      wl("</TT></B>");
	      
// 	      wl("\n</TD></TR></TABLE></BLOCKQUOTE><P>");

// 	      wl("where "+XmlInfo.colorChile("$value")
// 		 +" evaluates to the actual value of the parsed "
// 		 +quotify(t.label(),t)+".");
	      
// 	      wl("</TD></TR></TABLE>");
//               wl("<P>");
// 	    }
	  
          hltCopyrightStamp();
          wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
          wl("</BODY>");
          wl("</HTML>");

          out.close();
        }
    }

  /**
   * This inner class is to records parameters describing an XML pattern.
   */
  class XmlDescriptor
    {
      /**
       * A flag to generate an explanation about what a starred tag
       * means if there is any, while composing an XML pattern description.
       */
      boolean starredTag = false;

      /*
       * An examplar name that is actually used in the footnote explanation
       * of a starred tag while composing an XML pattern description.
       */
      String starredName = null;

      /*
       * To record the namespace while composing an XML pattern description.
       */
      String name = null;

      /*
       * To record the prefix while composing an XML pattern description.
       */
      String prefix = null;

      /*
       * To record attributes while composing an XML pattern description.
       */
      String attributes = "";
    }

  void generateXmlInfoDescription (Terminal t) throws IOException
    {
      XmlInfo info = t.xmlInfo();
      XmlDescriptor x = new XmlDescriptor();

      x.prefix = info.hasNsPrefix() ? (info.colorPrefix()+":") : "";
      x.name = info.colorName();

      wl("<TABLE WIDTH=\"100%\" BGCOLOR=\"WHITE\" CELLPADDING=\"15\"><TR><TD>");

      wl("<H4 ALIGN=\"CENTER\">XML Serialization Annotation</H4>");

      wl("This terminal has the following XML serialization annotation:");

      wl("<PRE>");
      wl(info.toPrettyHtmlString(2));
      wl("</PRE>");
      wl("This means that terminal "+quotify(t.label(),t)
	 +" is serialized thus:<P>");
      wl("<CENTER>");
      generateXmlPatternDescription(t,x);
      wl("</CENTER>");
      generateTerminalLegend(t.xmlInfo(),x);
      wl("</TD></TR></TABLE>");
    }

  void generateTerminalLegend (XmlInfo info, XmlDescriptor x) throws IOException
    {						       
      if (info.hasLegend())
	{
	  wl("<B>Legend:</B>\n<UL>\n");
	  for (Iterator i=info.legend().iterator(); i.hasNext();)
	    wl("<P><LI>"+i.next()+"\n");
	  wl("\n</UL>\n");
	}
    }

  void generateRuleLegend (XmlInfo info, XmlDescriptor x) throws IOException
    {						       
      if (info.hasLegend())
	{
	  wl("<SPAN STYLE=\"FONT-SIZE:SMALLER\"><B>Legend of attribute values for <TT>"
	     +x.prefix+x.name+":</TT></B></SPAN>\n<UL>\n");
	  for (Iterator i=info.legend().iterator(); i.hasNext();)
	    wl("<LI>"+i.next()+"\n");
	  wl("\n</UL>\n");
	}
    }

  void generateXmlInfoDescription (Rule r) throws IOException
    {
      XmlInfo info = r.xmlInfo();
      XmlDescriptor x = new XmlDescriptor();

      x.prefix = info.hasNsPrefix() ? (info.colorPrefix()+":") : "";
      x.name = info.colorName();

      wl("<CENTER>");
      wl("<P><HR><P>");

      wl("<TABLE BGCOLOR=\"WHITE\" CELLPADDING=\"15\"><TR><TD>");

      wl("<H4 ALIGN=\"CENTER\">XML Serialization Annotation</H4>");

      wl("This rule has the following XML serialization annotation:");      
//       w("<SPAN STYLE=\"FONT-SIZE:LARGER\">");
      w("<PRE>");
      wl(info.toPrettyHtmlString(2));
      wl("</PRE>");
//       wl("</SPAN>");

      wl("This means that a "
	 +r.sequence[0].htmlRef()
	 +" derived through this rule will be serialized thus:");

      wl("<P><CENTER>");
      generateXmlPatternDescription(r,x);
      wl("</CENTER>");
      generateRuleLegend(info,x);

      if (x.starredTag)
	{
	  wl("<P><HR><P>");
	  wl("<SPAN STYLE=\"COLOR:#445577\" SIZE=\"-2\">");
	  wl("<I><B>N.B.:</B> an XML construct such as ");
	  wl("<B><SPAN STYLE=\"COLOR:BLUE\"><TT>&lt;"+x.starredName+
	     "*&gt;...&lt;/"+x.starredName+"*&gt;</TT></SPAN></B> ");
	  wl("stands for either zero, one, or many occurrences of ");
	  wl("<B><SPAN STYLE=\"COLOR:BLUE\"><TT>&lt;"+x.starredName+
	     "&gt;...&lt;/"+x.starredName+"&gt;</TT></SPAN></B>. ");
	  wl("distributed over the element's contents");
	  wl("(the \"<B><SPAN STYLE=\"COLOR:BLUE\"><TT>...</TT></SPAN></B>\"), which, accordingly, ");
	  wl("may be either empty, a single XML element, or a list thereof.</I>");
	  wl("</SPAN>");
	  wl("<P><HR><P>");
	}

      wl("</TD></TR></TABLE>");
      wl("</CENTER>");
    }

  void generateXmlPatternDescription (Terminal t, XmlDescriptor x) throws IOException
    {
      wl("<BLOCKQUOTE><TABLE BORDER BGCOLOR=\"#EEEEEE\" CELLPADDING=\"20\"><TR><TD>");

      XmlInfo info = t.xmlInfo();
      x.prefix = info.hasNsPrefix() ? (info.colorPrefix()+":") : "";
      x.name = info.colorName();

      info.resetLegend();

      if (info.hasAttributes())
	{
	  XmlAttributeInfo[] atts = info.attributes();
	  StringBuilder s = new StringBuilder();
	  for (int i=0; i<atts.length; i++)
	    s.append(" ")
	      .append(info.colorAttribute(atts[i].toString()));
	  x.attributes = s.toString();
	}

      wl("<P>");

      wl("<B><TT>");

      if (info.hasChildren())
	wl("&lt;"+x.prefix+x.name+x.attributes+"&gt;&nbsp;"
	   +info.colorChild("</TT></B><I><SPAN STYLE=\"FONT-SIZE:SMALLER\">the "
			    +t.htmlName().toLowerCase()
			    +"</SPAN></I><B><TT>")
	   +"&nbsp;&lt;/"+x.prefix+x.name+"&gt;");
      else
	wl("&lt;"+
	   x.prefix+x.name+x.attributes.replaceAll("\\$[Vv][Aa][Ll][Uu][Ee]",
						   "<I>"+t.label().toLowerCase()+"</I>")+
	   "/&gt;");

      wl("</TT></B>");

      wl("\n</TD></TR></TABLE></BLOCKQUOTE>");
    }

  void generateXmlPatternDescription (Rule r, XmlDescriptor x) throws IOException
    {
      wl("<BLOCKQUOTE><TABLE BORDER BGCOLOR=\"#EEEEEE\" CELLPADDING=\"20\"><TR><TD>");

      XmlInfo info = r.xmlInfo();

      x.prefix = info.hasNsPrefix() ? (info.colorPrefix()+":") : "";
      x.name = info.colorName();

      info.resetLegend();

      if (info.hasAttributes())
	{
	  XmlAttributeInfo[] atts = info.attributes();
	  StringBuilder s = new StringBuilder();
	  for (int i=0; i<atts.length; i++)
	    {
	      s.append(" ");
	      
	      if (atts[i].isDeepAttributeReference())
		{
		  s.append(info.formatAttributeRef(atts[i],
						   i,
						   r.sequence[atts[i].child()].htmlRef()));
		  continue;
		}

	      if (atts[i].hasTextForm())
		{
		  s.append(info.formatAttributeTextForm(atts[i],i,r));
		  continue;
		}

	      s.append(info.colorAttribute(atts[i].toString()));
	    }

	  x.attributes = s.toString();
	}
      

      if (info.hasChildren())
	{
 	  wl("<PRE STYLE=\"line-height: 150%\">");
// 	  wl("<SPAN STYLE=\"FONT-SIZE:LARGER\">");
	  wl("<B>&lt;"+x.prefix+x.name+x.attributes+"&gt;</B>");
// 	  wl("</SPAN>");

	  int[] children = info.children();

	  w("<SPAN STYLE=\"COLOR:"+XmlInfo.CHILDREN_COLOR+"\">");
	  for (int i=0; i<children.length; i++)
	    {
	      // extract the wrapper path, in any:
	      XmlWrapper[] wrapperPath = info.wrapperPaths()[i];
// 	      System.err.println(Misc.arrayToString(wrapperPath,"","",""));

	      if (wrapperPath == null)
		if (info.hasAttributeRefs() && info.attributeRefs(i) != null)
		  {
		    wl("</PRE><BLOCKQUOTE STYLE=\""+bqstyle+"\"><SPAN STYLE=\"FONT-SIZE:SMALLER\"><I>Value"
		       +" of attribute named <TT>'<SPAN STYLE=\"COLOR:"+XmlInfo.ATTRIBUTE_COLOR+"\">"
		       +info.attributeRefs(i)+"</SPAN>'</TT> of the XML serialization "
		       +info.childRef(children[i],null)
		       +"</I></SPAN> ("+r.sequence[children[i]].htmlRef()+")</BLOCKQUOTE>");
		    wl("<PRE STYLE=\"line-height: 150%\">");
		  }
		else
		  {
		    wl("</PRE><BLOCKQUOTE STYLE=\""+bqstyle+"\"><SPAN STYLE=\"FONT-SIZE:SMALLER\"><I>XML serialization"
		       +info.childRef(children[i],info.xmlPaths()[i])
		       +"</I></SPAN> ("+r.sequence[children[i]].htmlRef()+")</BLOCKQUOTE>");
		    wl("<PRE STYLE=\"line-height: 150%\">");
		  }
	      else
		{ // OK - so it has a wrapper path: we need to iterate down
		  // it and nest begin/end pairs of xml tags for each entry

		  // the increasingly indented opening tags in ascending order:
		  for (int j=0; j<wrapperPath.length; j++)
		    {
		      x.starredTag = wrapperPath[j].isStarred();
		      if (x.starredTag && x.starredName == null)
			x.starredName = wrapperPath[j].getTag();
		      for (int k=0; k<=j; k++) w("  ");
		      wl("<B>&lt;"
			 +XmlInfo.colorName(wrapperPath[j].getTag()+(x.starredTag?"*":""))
			 +"&gt;</B>");
		    }

		  // the deeply nested description of the contents:
		  wl("</PRE>");
		  for (int j=wrapperPath.length; j-->0;)
		    wl("<BLOCKQUOTE STYLE=\""+bqstyle+"\">");
		  wl("<SPAN STYLE=\"FONT-SIZE:SMALLER\"><I>XML serialization"
		     +info.childRef(children[i],info.xmlPaths()[i])+"</I></SPAN> ("
		     +r.sequence[children[i]].htmlRef()+")");
		  for (int j=wrapperPath.length; j-->0;)
		    wl("</BLOCKQUOTE>");
		  wl("<PRE STYLE=\"line-height: 150%\">");
		  
		  // the decreasingly indented closing tags in descending order:
		  for (int j=wrapperPath.length; j-->0;)
		    {
		      x.starredTag = wrapperPath[j].isStarred();
		      for (int k=0; k<=j; k++) w("  ");
		      wl("<B>&lt;/"
			 +XmlInfo.colorName(wrapperPath[j].getTag()+(x.starredTag?"*":""))
			 +"&gt;</B>");
		    }

		}
	    }
	  w("</SPAN>");

// 	  wl("<SPAN STYLE=\"FONT-SIZE:LARGER\">");
	  wl("<B>&lt;/"+x.prefix+x.name+"&gt;</B>");
// 	  wl("</SPAN>");
 	  wl("</PRE>");
	}
      else
	{
// 	  wl("<SPAN STYLE=\"FONT-SIZE:LARGER\">");
	  wl("<TT><B>&lt;"+x.prefix+x.name+x.attributes+"/&gt;</B></TT>");
// 	  wl("</SPAN>");
	}

      wl("\n</TD></TR></TABLE></BLOCKQUOTE>");
  }

  final void generateNTFiles () throws IOException
    {
      for (Iterator ns=grammar.nonterminals.iterator(); ns.hasNext();)
        {
          NonTerminal n = (NonTerminal)(ns.next());

          if (isHidden(n)) continue;

          setOutput(n.htmlFileName());

          preamble(file,true);

          wl(MAIN_BODY);

          wl("<H1 ALIGN=\"CENTER\">Non-terminal symbol <SPAN STYLE=\"COLOR:MAROON\">"
             +n.label()+"</SPAN></H1>");
          wl("<CENTER>");
          wl("<SPAN STYLE=\"FONT-SIZE:XX-SMALL\"><TABLE BORDER=\"2\",CELLPADDING=\"8\"><TR>"
             +"<TD BGCOLOR=\"PINK\"><A HREF=\"#RULES\"><SPAN STYLE=\"FONT-SIZE:XX-SMALL\">"
             +"rule(s)</SPAN></TD>"
             +"<TD BGCOLOR=\"PINK\"><A HREF=\"#OCCS\"><SPAN STYLE=\"FONT-SIZE:XX-SMALL\">"
             +"occurrences</SPAN></A></TD></TR></TABLE></SPAN>");
          wl("</CENTER>");

          if (n.doc != null)
            formatDoc(n.doc.toString());

          wl("<P><HR><P>");

          wl("<SPAN STYLE=\"FONT-SIZE:LARGER\"><A NAME=\"RULES\">Defining</A> rules for nonterminal symbol");
          wl("<SPAN STYLE=\"COLOR:MAROON; FONT-SIZE:LARGER\">"+n.label()+"</SPAN>:</SPAN><P>");

          wl("<CENTER>");
          wl("<TABLE BGCOLOR=\"WHITE\" BORDER=\"5\" CELLPADDING=\"20\">");
          wl("<TR><TD>");
          wl("<TABLE BGCOLOR=\"WHITE\" CELLPADDING=\"6\">");

          Iterator rls = n.rules.iterator();
          formatRule((Rule)rls.next(),n,true);
          for (;rls.hasNext();)
            formatRule((Rule)rls.next(),n,false);

          wl("</TABLE>");
          wl("</TD></TR>");
          wl("</TABLE>");
          wl("</CENTER>");
          wl("<P>");

          SetOf rules = n.ruleOccurrences;

          if (rules != null && !rules.isEmpty())
            {
              wl("<HR><P>");
              wl("<SPAN STYLE=\"FONT-SIZE:LARGER\"><A NAME=\"OCCS\">Occurrences</A> of symbol");
              wl("<SPAN STYLE=\"COLOR:MAROON; FONT-SIZE:LARGER\">"+n.label()+"</SPAN>");
              wl("in body of other rules:</SPAN><P>");

              wl("<CENTER>");
              wl("<TABLE BGCOLOR=\"WHITE\" BORDER=\"5\" CELLPADDING=\"20\">");
              wl("<TR><TD>");
              wl("<TABLE BGCOLOR=\"WHITE\" CELLPADDING=\"6\">");

              for (rls=rules.iterator(); rls.hasNext();)
                formatRule((Rule)rls.next(),n);

              wl("</TABLE>");
              wl("</TD></TR>");
              wl("</TABLE>");
              wl("</CENTER>");
            }

          hltCopyrightStamp();
          wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
          wl("</BODY>");
          wl("</HTML>");

          out.close();
        }
    }

  final void generateRuleFile (Rule r) throws IOException
    {
      setOutput(r.htmlFileName());

      preamble(file,false);

      wl(MAIN_BODY);

      wl("<H2 ALIGN=\"CENTER\">Rule "+r.head().ruleLabel(r)+"</H2>");

      formatRule(r);
      formatXmlInfo(r);
      if (r.doc != null)
	formatDoc(r.doc.toString());

      hltCopyrightStamp();
      wl("<P ALIGN=\"RIGHT\">"+LOGOIM);
      wl("</BODY>");
      wl("</HTML>");

      out.close();
    }

  final void formatXmlInfo (Rule r) throws IOException
    {
      if (r == null)
	return;

      if (r.xmlInfo() == null)
	return;

      wl("<P>");
      generateXmlInfoDescription(r);
      wl("<P>");
    }

  final void formatRule (Rule r) throws IOException
    {
      if (r == null)
        return;

      wl("<CENTER>");
      wl("<TABLE BGCOLOR=\"WHITE\" BORDER=\"5\" CELLPADDING=\"20\">");
      wl("<TR><TD>");
      wl("<TABLE BGCOLOR=\"WHITE\" CELLPADDING=\"6\">");

      wl("<TR>");

      wl("<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\">");
      wl(r.sequence[0].htmlRef());
      wl("</TD>");

      wl("<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\">");
      wl(RARROW);
      wl("</TD>");

      wl("<TD VALIGN=\"TOP\">");

      for (int i=1; i<r.sequence.length; i++)
        {
          GrammarSymbol gs = r.sequence[i];
          String symbol = gs.htmlRef();

          if (isError(gs))
            symbol = "<B><TT><SPAN STYLE=\"COLOR:#4444FF\">"+gs.name()+"</SPAN></TT></B>";
          else
            {
              if (isHidden(gs)) continue;
              symbol = quotify(symbol,gs);
            }

          w(symbol+"&nbsp;");
        }

      wl("</TD>");

      wl("</TR>");

      wl("</TABLE>");
      wl("</TD></TR>");
      wl("</TABLE>");
      wl("</CENTER>");
    }

  final void formatRule (Rule r, GrammarSymbol s) throws IOException
    {
      if (r == null)
        return;

      wl("<TR>");

      wl("<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\">");
      wl(r.sequence[0].htmlRef());
      wl("</TD>");

      wl("<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\">");
      ruleLink(r,s);
      wl("</TD>");

      wl("<TD VALIGN=\"TOP\">");      

      for (int i=1; i<r.sequence.length; i++)
          {
            GrammarSymbol gs = r.sequence[i];
            String symbol = gs.htmlRef();

            if (isError(gs))
              symbol = "<B><TT><SPAN STYLE=\"COLOR:#4444FF\">"+gs.name()+"</SPAN></TT></B>";
            else
              {
                if (isHidden(gs)) continue;
                symbol = quotify(symbol,gs);
              }

            if (gs == s)
              wl(quotify("<SPAN STYLE=\"COLOR:MAROON\">"+s.label()+"</SPAN>",s)+"&nbsp;");
            else
              wl(symbol+"&nbsp;");
          }

      wl("</TD>");

      wl("</TR>");
    }

  final void formatRule (Rule r, NonTerminal n, boolean isFirst) throws IOException
    {
      if (r == null)
        return;

      wl("<TR>");

      wl("<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\">");
      if (isFirst)
        wl("<SPAN STYLE=\"COLOR:MAROON\">"+n.label()+"</SPAN>");
      wl("</TD>");

      wl("<TD ALIGN=\"RIGHT\" VALIGN=\"TOP\">");
      ruleLink(r,n);
      wl("</TD>");

      wl("<TD VALIGN=\"TOP\">");      

      for (int i=1; i<r.sequence.length; i++)
        {
          GrammarSymbol gs = r.sequence[i];
          String symbol = gs.htmlRef();

          if (isError(gs))
            symbol = "<B><TT><SPAN STYLE=\"COLOR:#4444FF\">"+gs.name()+"</SPAN></TT></B>";
          else
            {
              if (isHidden(gs)) continue;
              symbol = quotify(symbol,gs);
            }

          if (gs == n)
            wl("<SPAN STYLE=\"COLOR:MAROON\">"+n.label()+"</SPAN>&nbsp;");
          else
            wl(symbol+"&nbsp;");
        }

      wl("</TD>");
      wl("</TR>");
    }

  final void ruleLink (Rule r, GrammarSymbol s) throws IOException
    {
      if (r.doc == null && r.xmlInfo() == null)
        wl(RARROW);
      else
        wl("\n<A CLASS=\"SYMBOL\" HREF=\"javascript:showRuleDoc('"+r.refName()
           +"')\">"+RARROW+"</A>");
    }   

  final void setOutput (String fileName) throws IOException
    {
      file = DOCDIR+"/"+fileName;
      out = new BufferedWriter(new FileWriter(file));
    }

  final void w (String s) throws IOException
    {
      out.write(s);
    }

  final void w (char c) throws IOException
    {
      w(String.valueOf(c));
    }

  final void wl (String s) throws IOException
    {
      out.write(s+"\n");
    }

  final void wl () throws IOException
    {
      out.write("\n");
    }

  /**
   * This translates the documentation string. Any star ('\*') is
   * ignored unless escaped with a backslash (<i>i.e.</i>, '\\\*'), or
   * followed by a slash ('/') to end the comment.
   */
  final void formatDoc (String doc) throws IOException
    {
      if (doc == null) return;

      HashMap tags = new HashMap();

      wl("<P><HR><P>");
      wl("<H4 ALIGN=\"CENTER\">Description</H4>");
      wl("<TABLE WIDTH=\"100%\" BGCOLOR=\"WHITE\" CELLPADDING=\"15\"><TR><TD>");

      for (int i = 0; i<doc.length(); i++)
        {
          switch (doc.charAt(i))
            {
            case '*':   // skip stars:
              break;
            case '\\':  // escape: next (if any) is literal
              if (i == doc.length()-1)
                w('\\');
              else
                {
                  w(doc.charAt(i+1));
                  i++;
                }
              break;
            case '%': case '$': // symbol reference
              i = processSymbolRef(i+1,doc,doc.charAt(i));
              break;
            case '@':   // javadoc tag
              i = recordTag(i+1,doc,tags);
              break;
            default:    // just output the character
              w(doc.charAt(i));
            }
        }

      if (!tags.isEmpty())              // end with the javadoc tags if any
        formatTags(tags);

      wl("</TD></TR></TABLE>");
    }

  final int processSymbolRef (int i, String doc, char end) throws IOException
    {
      int start = i;

      while (i < doc.length() && doc.charAt(i) != end)
        i++;

      if (i == doc.length())
        w(doc.substring(start-1));
      else
        {
          String ref = doc.substring(start,i).intern();
          GrammarSymbol symbol = Grammar.getTerminal(ref);

          if (symbol == null)
            symbol = Grammar.getNonTerminal(ref);

          if (symbol == null)
            {
              Grammar.warning("Unknown symbol reference in documentation: "+ref);
//            w("<BLINK><SPAN STYLE=\"COLOR:RED\"><b><i><tt>"+ref+"</tt></i></b></SPAN></BLINK>");
              w("<SPAN STYLE=\"COLOR:RED\"><b><i><tt>"+ref+"</tt></i></b></SPAN>");
            }
          else
            w(symbol.htmlRef());
        }

      return i;
    }

  /**
   * Extracts the tag definition starting at position <tt>i</tt> in the
   * string <tt>doc</tt> and records it in the table <tt>tag</tt>. Then,
   * returns the position in doc following the tag definition.
   */  
  final int recordTag (int i, String doc, HashMap tags) throws IOException
    {
      if (i == doc.length() || !Character.isLetter(doc.charAt(i)))
        {
          w('@');
          return i;
        }

      // extract the tag:
      StringBuilder word = new StringBuilder();
      do word.append(doc.charAt(i));
      while (++i < doc.length() && Character.isLetter(doc.charAt(i)));

      // get the definitions for this tag:
      ArrayList defs = (ArrayList)tags.get(word.toString());
      if (defs == null)
        tags.put(word.toString(),defs = new ArrayList());

      // extract the tag's definition:
      word = new StringBuilder();
      while (i < doc.length() && doc.charAt(i) != '\n')
        word.append(doc.charAt(i++));

      // record the definition:
      defs.add(word.toString().trim());

      return i;
    }      

  /**
   * Formats the recorded tag definitions.
   */  
  final void formatTags(HashMap tags) throws IOException
    {
      String tag;

      wl("\n<DL>");

      for (Iterator k = tags.keySet().iterator(); k.hasNext();)
        {
          tag = (String)k.next();

          wl("<DT><STRONG>"+label(tag)+"</STRONG></DT>");

          ArrayList defs = (ArrayList)tags.get(tag);

          if (!defs.isEmpty())
            {
              boolean first = true;
              boolean paramTag = tag.equalsIgnoreCase("param");

              w("<DD>");
              if (paramTag) wl("<TABLE BORDER=0>");
              for (Iterator e = defs.iterator(); e.hasNext();)
                {
                  if (first)
                    first = false;
                  else
                    w(paramTag ? "\n" : ", ");
                  w(tagDefFormat(tag,(String)e.next()));
                }
              if (paramTag) wl("</TABLE>");
              wl("</DD>");
            }
        }

      wl("</DL>");
    }

  static HashMap knownTags = new HashMap(); 

  static 
    {
      // Filling the knownTags table:

      knownTags.put("deprecated","<BLINK>Deprecated!</BLINK>");
      knownTags.put("exception","Throws:");
      knownTags.put("param","Parameters:");
      knownTags.put("return","Returns:");
      knownTags.put("see","See also:");
      knownTags.put("since","Since:");
      knownTags.put("version","Version:");
    }      

  /**
   * Returns a label for the given tag.
   */
  final String label (String tag)
    {
      String label = (String)knownTags.get(tag);
      if (label != null) return label;

      // If not a standard tag, the label is the capitalized tag followed with ':'
      label = String.valueOf(Character.toUpperCase(tag.charAt(0)))+
        tag.toLowerCase().substring(1)+":";
      return label;
    }

  /**
   * Returns the formatted definition as per the given tag. The string
   * <tt>def</tt> contains the definition (the term and the definition)
   * separated by either a space or a tab character.
   */
  final String tagDefFormat (String tag, String def)
    {
      if (tag.equalsIgnoreCase("param"))
        {
          int i = def.indexOf(' ');
          int j = def.indexOf('\t');
          if (i>= 0 && j>= 0)       
            i = Math.min(i,j);
          else  // (i < 0 || j < 0)
            i = Math.max(i,j);

          return "<TR><TD VALIGN=BASELINE><B><I><TT>"+
            (i >= 0?def.substring(0,i):def)+
            "&nbsp;</TT></I></B></TD><TD>- "+
            (i >= 0?def.substring(i):def)+
            "</TD></TR>";
        }

      if (!tag.equalsIgnoreCase("see") || def.regionMatches(true,0,"<A HREF",0,7))
        return def;

      int i = def.lastIndexOf('#');
      String refBase = (i<0) ? def : def.substring(0,i);
      String refTag  = (i<0) ? ""  : def.substring(i);

      return "<A HREF=\""+refBase+(refBase.length()>0?".html":"")+refTag+"\">"+
        ((i<0) ? refBase.substring(refBase.lastIndexOf('.')+1)
         : refTag.substring(1))
        +"</A>";
    }
}
