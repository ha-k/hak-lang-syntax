//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * This abstract class defines some of the information needed to complete
 * the implementation of a parser.
 *
 * @see         FileTokenizer
 *
 * @version     Last modified on Fri Apr 13 19:53:26 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public abstract class AbstractFileTokenizer implements FileTokenizer
{
  public String location ()
    {
      return "file "+fileName()+", line "+lineNumber();
    }
}
