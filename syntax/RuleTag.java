//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * This is the class of dummy tag symbols used in defining a precedence
 * and associativity for a grammar rule using <tt>%prec</tt>.
 *
 * @version     Last modified on Fri Apr 13 20:12:21 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

class RuleTag implements Taggable
{
  RuleTag (int precedence, String specifier) throws NonFatalParseErrorException
    {
      _precedence = precedence;
      decode(specifier);
    }

  /**
   * This symbol's precedence.
   */
  private int _precedence = Grammar.MIN_PRECEDENCE;

  /**
   * This symbol's associativity.
   */
  private int _associativity = Grammar.NON_ASSOCIATIVE;

  /**
   * This symbol's fixity.
   */
  private int _fixity = OperatorSymbol.INFIX;

  public final int precedence ()
    {
      return _precedence;
    }
    
  public final int associativity ()
    {
      return _associativity;
    }

  public final int fixity ()
    {
      return _fixity;
    }

  public final boolean isOperator ()
    {
      return false;
    }

  /**
   * Decodes and interprets the contents of a Prolog-style operator
   * specifier string.
   */
  private final void decode (String specifier) throws NonFatalParseErrorException
    {
      NonFatalParseErrorException badSpecifier
        = new NonFatalParseErrorException("Bad operator specifier ("+specifier+")");

      if (specifier.length() == 2)
        {
          if (specifier.indexOf('f') == 0)
            {
              _fixity = OperatorSymbol.PREFIX;
              if (specifier.charAt(1) == 'y')
                {
                  _associativity = Grammar.RIGHT_ASSOCIATIVE;
                  return;
                }
              if (specifier.charAt(1) == 'x')
                {
                  _associativity = Grammar.NON_ASSOCIATIVE;
                  return;
                }
              throw badSpecifier;
            }
          if (specifier.indexOf('f') == 1)
            {
              _fixity = OperatorSymbol.POSTFIX;       
              if (specifier.charAt(0) == 'y')
                {
                  _associativity = Grammar.LEFT_ASSOCIATIVE;
                  return;
                }
              if (specifier.charAt(0) == 'x')
                {
                  _associativity = Grammar.NON_ASSOCIATIVE;
                  return;
                }
            }
          throw badSpecifier;
        }

      if (specifier.length() == 3 && specifier.indexOf('f') == 1)
        {
          _fixity = OperatorSymbol.INFIX;
          if (specifier.charAt(0) == 'y')
            {
              if (specifier.charAt(2) != 'x') throw badSpecifier;
              _associativity = Grammar.LEFT_ASSOCIATIVE;
              return;
            }
          if (specifier.charAt(0) == 'x')
            {
              if (specifier.charAt(2) == 'y')
                _associativity = Grammar.RIGHT_ASSOCIATIVE;
              else
                if (specifier.charAt(2) == 'x')
                  _associativity = Grammar.NON_ASSOCIATIVE;
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
      if (_associativity == Grammar.LEFT_ASSOCIATIVE)
        s += "y";
      else
        if (_fixity != OperatorSymbol.PREFIX) s += "x";
      s += "f";
      if (_associativity == Grammar.RIGHT_ASSOCIATIVE)
        s += "y";
      else
        if (_fixity != OperatorSymbol.POSTFIX) s += "x";
      return s;
    }

  public boolean equals (Object other)
    {
      if (!(other instanceof RuleTag)) return false;

      RuleTag t = (RuleTag)other;

      return _precedence == t.precedence()
          && _associativity == t.associativity()
          && _fixity == t.fixity();
    }

  public String toString()
    {
      return "<" + Grammar.prologPrecedence(_precedence) + "," + specifier() + ">";
    }
}
