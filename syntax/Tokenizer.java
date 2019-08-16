//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.io.Reader;

/**
 * This interface defines the information needed to complete
 * the implementation of a parser.
 *
 * @see         Tokenizer
 * @see         GenericParser
 * @see         StaticParser
 * @see         DynamicParser
 *
 * @version     Last modified on Fri Jul 27 07:48:23 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public interface Tokenizer
{
  public ParseNode nextToken () throws java.io.IOException;

  public void setReader (Reader rd);

  public Reader getReader ();

  public int lineNumber ();
}
