//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.AbstractList;
import hlt.language.util.Named;

/**
 * This class is an abstract representation of the type of tokens
 * that are dynamic operators, whether <tt>Operator</tt> (used use
 * by the grammar at parser construction time), or <tt>ParserOperator</tt>
 * (used by the parser at parse time).
 *
 * @see         Symbol
 * @see         Operator
 * @see         ParserOperator
 *
 * @version     Last modified on Fri Apr 13 20:06:16 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public abstract class OperatorSymbol extends Symbol
{
  public final static int PREFIX  = 0;
  public final static int POSTFIX = 1;
  public final static int INFIX   = 2;

  static final Grammar grammar = Grammar.currentGrammar;

  OperatorSymbol (String name, AbstractList set, int precedence, String specifier)
    throws NonFatalParseErrorException
    {
      super(name,set);
      this.precedence = precedence;
      decode(specifier);
    }

  OperatorSymbol (String name, AbstractList set,
		  int precedence, int associativity, int fixity)
    {
      super(name,set);
      this.precedence = precedence;
      this.associativity = associativity;
      this.fixity = fixity;
    }

  int precedence;
  int associativity;
  int fixity;

  abstract Named category ();

  public final void redefine (int precedence, String specifier)
    throws NonFatalParseErrorException
    {
      this.precedence = precedence;
      decode(specifier);
    }
  
  public final boolean equals (Object other)
    {
      if (this == other) return true;
      if (!(other instanceof OperatorSymbol)) return false;
      OperatorSymbol that = (OperatorSymbol)other;
      return (this.category() == that.category()
          &&  this.fixity == that.fixity
          &&  this.name.equals(that.name));
    }

  /**
   * Decodes and interprets the contents of a Prolog-style operator
   * specifier string.
   */
  private final void decode (String specifier) throws NonFatalParseErrorException
    {
      NonFatalParseErrorException badSpecifier
        = new NonFatalParseErrorException("Bad dynamic operator specifier ("+specifier+")");

      if (specifier.length() == 2)
        {
          if (specifier.indexOf('f') == 0)
            {
              fixity = PREFIX;
              if (specifier.charAt(1) == 'y')
                {
                  associativity = Grammar.RIGHT_ASSOCIATIVE;
                  return;
                }
              if (specifier.charAt(1) == 'x')
                {
                  associativity = Grammar.NON_ASSOCIATIVE;
                  return;
                }
              throw badSpecifier;
            }
          if (specifier.indexOf('f') == 1)
            {
              fixity = POSTFIX;       
              if (specifier.charAt(0) == 'y')
                {
                  associativity = Grammar.LEFT_ASSOCIATIVE;
                  return;
                }
              if (specifier.charAt(0) == 'x')
                {
                  associativity = Grammar.NON_ASSOCIATIVE;
                  return;
                }
            }
          throw badSpecifier;
        }

      if (specifier.length() == 3 && specifier.indexOf('f') == 1)
        {
          fixity = INFIX;
          if (specifier.charAt(0) == 'y')
            {
              if (specifier.charAt(2) != 'x') throw badSpecifier;
              associativity = Grammar.LEFT_ASSOCIATIVE;
              return;
            }
          if (specifier.charAt(0) == 'x')
            {
              if (specifier.charAt(2) == 'y')
                associativity = Grammar.RIGHT_ASSOCIATIVE;
              else
                if (specifier.charAt(2) == 'x')
                  associativity = Grammar.NON_ASSOCIATIVE;
                else
                  throw badSpecifier;
              return;
            }
        }
      throw badSpecifier;
    }

  public final String specifier ()
    {
      String s = "";
      if (associativity == Grammar.LEFT_ASSOCIATIVE)
        s += "y";
      else
        if (fixity != PREFIX) s += "x";
      s += "f";
      if (associativity == Grammar.RIGHT_ASSOCIATIVE)
        s += "y";
      else
        if (fixity != POSTFIX) s += "x";
      return s;
    }


  public int precedence ()
    {
      return precedence;
    }

  public int associativity ()
    {
      return associativity;
    }

  public int fixity ()
    {
      return fixity;
    }

  public String toString()
    {
      return category().name()+"("+Grammar.prologPrecedence(precedence)
                              +","+specifier()
                              +","+name
                              +")";
    }
}
