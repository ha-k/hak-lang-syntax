//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * Signals an unrecoverable error in the parsing.
 * 
 * @see         GenericParser
 *
 * @version     Last modified on Fri Apr 13 19:58:18 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class FatalParseErrorException extends RuntimeException
{
  /**
   * Constructs a new FatalParseErrorException.
   */
  public FatalParseErrorException ()
    {
    }

  /**
   * Constructs a new FatalParseErrorException with a message.
   */
  public FatalParseErrorException (String msg)
    {
      super(msg);
    }
}
