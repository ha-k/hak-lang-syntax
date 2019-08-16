//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.tools.Misc;

/**
 * Signals that something is wrong with an XML annaotation in the grammar.
 *
 * @see         Grammar
 * @version     Last modified on Fri Apr 13 19:55:24 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class BadXmlAnnotationException extends RuntimeException
{
  private String _msg = "Illegal XML annotation";

  /**
   * Constructs a new BadXmlAnnotationException.
   */
  public BadXmlAnnotationException ()
    {
    }

  /**
   * Constructs a new BadXmlAnnotationException with a message.
   */
  public BadXmlAnnotationException (String msg)
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
