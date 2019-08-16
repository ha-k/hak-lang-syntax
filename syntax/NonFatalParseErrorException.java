//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * Signals an non fatal error while parsing.
 * 
 * @see         GenericParser
 *
 * @version     Last modified on Fri Apr 13 20:05:36 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class NonFatalParseErrorException extends RuntimeException
{
  private String _msg = "non fatal parse error";

  /**
   * Constructs a new NonFatalParseErrorException.
   */
  public NonFatalParseErrorException ()
    {
    }

  /**
   * Constructs a new NonFatalParseErrorException with a message.
   */
  public NonFatalParseErrorException (String msg)
    {
      _msg = msg;
    }

  /**
   * Returns this exception's message.
   */
  public final String msg ()
    {
      return _msg;
    }
}
