//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import hlt.language.io.FileTools;
import hlt.language.tools.Command;

/**
 * Jacc is just another compiler-compiler (or rather, more than just
 * that).  It is a 100% pure Java yacc-compatible compiler generator,
 * augmented with many features, not the least of which being its
 * ability to accommodate dynamic operators <i>&agrave; la</i>
 * Prolog. For details, consult the documentation on the <a
 * href="http://hassan-ait-kaci.net/hlt/doc/hlt/jaccdoc/000_START_HERE.html"><b>Jacc</b></a>
 * system and grammar format.
 *
 * @version     Last modified on Wed Nov 28 12:50:56 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 * 
 * @see         Options
 * @see         Grammar
 * @see         GenericParser
 * @see         ParserGenerator
 */


public class Jacc extends Command
{
//   private final static String jaccVersion = JACCVERSION;
//   private final static String jaccGenDate = JACCGENDATE;

  public static void main (String args[])
    {
      defineOption("p",
                   Options.getParserPrefix(),
                   "name of java parser file  (default: grammar=\"Foo.grm\" => parser=\"Foo"
                   +Options.getParserPrefix()+".java\")");
      defineOption("v",
                   String.valueOf(Options.getVerbosity()),
                   "information verbosity (number between 0 and 4)");
      defineOption("m",
                   String.valueOf(Options.getInitMethodSize()),
                   "max number of instructions in parser initialization method");
      defineOption("amb",
                   "",
                   "allow ambiguous actions (for dynamic parsing)");
      defineOption("trail",
                   String.valueOf(Options.getTrailHistory()),
                   "max size of trail history (for dynamic parsing)");
      defineOption("choices",
                   String.valueOf(Options.getChoiceHistory()),
                   "max size of choice history (for dynamic parsing)");
      defineOption("n",
                   "",
                   "do not generate the parser");
      defineOption("rf",
                   "",
                   "copy doc resource files");
      defineOption("rrp",
                   "",
                   "resolve R/R conflicts based on precedence (dangerous!)");
      defineOption("doc",
                   "",
                   "only generate the grammar's html documentation (no parser code)");
      defineOption("base",
                   ".",
                   "Jacc's %include command's file base directory");
      defineOption("dest",
                   ".",
                   "destination directory where Jacc will write the generated parser file");
      defineOption("i",
                   "",
                   "tolerate an incomplete grammar (no parser is generated)");
      defineOption("o",
                   "System.out",
                   "redirect the log to the specified file");
      defineOption("e",
                   "System.err",
                   "redirect errors to the specified file");
      defineOption("s",
                   Options.getSeparator(),
                   "file separator character");
      
      optionalArgument(Options.getGrammarPrefix());

      setUsage("\nUsage: jacc [options] grammar_file(s)\n");

      if (args.length == 0)
        {
          printHelp();
          System.exit(1); // exit with a non-zero status code...
        }

      if (parseCommandLine(args))
        {
          FileTools.setSeparator(getOption("s"));

	  // This is the argument passed to Jacc (of the form [PATH/]FILE[.EXT]):
          String argument      = getArgument();
	  // pathedArgument = [PATH/]FILE:
          String pathedArgument = fullFileNamePrefix(argument);
	  // grammarName = FILE:
          String grammarName    = FileTools.prefix(argument);
	  // fileExtension = EXT:
          String fileExtension  = fileNameSuffix(argument);
	  // parserClass is what is specified as -p if any:
          String parserClass    = getOption("p");

	  // When the argument is present and no parser name is
	  // specified, given that the grammar is named "Foo", the
	  // parser class name defaults to "FooParser":
          if (argumentIsPresent() && !optionIsPresent("p"))
            parserClass = grammarName + "Parser";

// 	  System.out.println("argument       = "+argument);
// 	  System.out.println("pathedArgument = "+pathedArgument);
// 	  System.out.println("grammarName    = "+grammarName);
// 	  System.out.println("fileExtension  = "+fileExtension);
// 	  System.out.println("parserClass    = "+parserClass);

          Options.setGrammarPathedName(pathedArgument);
          Options.setGrammarPrefix(grammarName);
          if (fileExtension.length() > 0)
	    Options.setGrammarSuffix(fileExtension);

	  // The name of the parser file is the same as its class':
          Options.setParserPrefix(parserClass);

          Options.setDocOnly(optionIsPresent("doc"));
          Options.setNoParser(optionIsPresent("n"));
          Options.setCopyResourceFiles(optionIsPresent("rf"));
          Options.setPermissible(optionIsPresent("i"));
	  Options.setIncludeBase(getOption("base"));
	  Options.setDestination(getOption("dest"));

          Options.setVerbosity(Integer.parseInt(getOption("v")));
          Options.setInitMethodSize(Integer.parseInt(getOption("m")));
          Options.setTrailHistory(Integer.parseInt(getOption("trail")));
          Options.setChoiceHistory(Integer.parseInt(getOption("choices")));
          Options.setResolveRRsWithPrecedence(optionIsPresent("rrp"));
          Options.setAllowChoiceActions(optionIsPresent("amb"));

          String outFile = getOption("o");
          if (!outFile.equals("System.out"))
            {
              try
                {
                  FileOutputStream out_stream = new FileOutputStream(outFile);
                  PrintStream out = new PrintStream(out_stream,true);
                  Options.setOutStream(out);
                }
              catch (Exception e)
                {
                  System.err.println(e);
                  System.err.println("*** Couldn't create file "+outFile);
                  System.exit(1); // exit with a non-zero status code...
                }
            }

          String errFile = getOption("e");
          if (!errFile.equals("System.err"))
            {
              try
                {
                  FileOutputStream err_stream = new FileOutputStream(errFile);
                  PrintStream err = new PrintStream(err_stream,true);
                  Options.setErrStream(err);
                }
              catch (Exception e)
                {
                  System.err.println(e);
                  System.err.println("*** Couldn't create file "+errFile);
                  System.exit(1); // exit with a non-zero status code...
                }
            }

	  new ParserGenerator();
	}
    }
}
