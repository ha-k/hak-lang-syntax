//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;
import hlt.language.util.Named;

/**
 * This class is the type of tokens that are dynamic operators
 * used by the parser at parse time.
 *
 * @see         OperatorSymbol
 *
 * @version     Last modified on Fri Apr 13 20:10:31 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParserOperator extends OperatorSymbol
{
  DynamicParser parser;
  ParserNonTerminal category;
  ParserTerminal subCategory;

  ParserOperator (DynamicParser parser, String name,
                  ParserNonTerminal category, int precedence,
                  String specifier) throws NonFatalParseErrorException
    {
      super(name,parser.operators,precedence,specifier);
      this.category = category;
      String subcat = category.name().toUpperCase();
      switch (fixity)
        {
        case PREFIX:
          subCategory = GenericParser.terminal(subcat+"_");
          break;
        case INFIX:
          subCategory = GenericParser.terminal("_"+subcat+"_");
          break;
        case POSTFIX:
          subCategory = GenericParser.terminal("_"+subcat);
        }
    }

  ParserOperator (DynamicParser parser, String name,
                  ParserNonTerminal category, int precedence,
                  int associativity, int fixity)
    {
      super(name,parser.operators,precedence,associativity,fixity);
      this.category = category;
      String subcat = category.name().toUpperCase();
      switch (fixity)
        {
        case PREFIX:
          subCategory = GenericParser.terminal(subcat+"_");
          break;
        case INFIX:
          subCategory = GenericParser.terminal("_"+subcat+"_");
          break;
        case POSTFIX:
          subCategory = GenericParser.terminal("_"+subcat);
        }
    }

  Named category ()
    {
      return category;
    }
}
