//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * This interface refines <tt>Tokenizer</tt> with information
 * specific to an input file.
 *
 * @see		Tokenizer
 * @version	Last modified on Fri Apr 13 19:58:30 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright	&copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public interface FileTokenizer extends Tokenizer
{
  String fileName ();
  void setFileName (String name);
}
