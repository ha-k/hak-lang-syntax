//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.Iterator;
import hlt.language.util.ArrayList;
import hlt.language.tools.Misc;

/**
 * The class <tt>XmlSpecialForm</tt> represents objects denoting some
 * text value to be computed at parse time from the CST and XML tree
 * being built. A special form may be one of the following expressions:
 *
 * <ul>
 *
 * <li> <tt>$VALUE</tt>
 *
 * <li> <tt>$TEXT ( <i>arg<sub>1</sub></i> ... <i>arg<sub>n</sub></i> )</tt>
 *
 * </ul>
 *
 * where each <tt><i>arg<sub>i</sub></i></tt> is an <a
 * href="XmlTextInfoArg.html"><tt>XmlTextInfoArg</tt></a>.
 *
 * @version     Last modified on Fri Apr 13 20:22:18 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class XmlSpecialForm
{
  /**
   * When <tt>_textArgs</tt> is non-null, it consists of an array of <a
   * href="XmlTextInfoArg.html"><tt>XmlTextInfoArg</tt></a> objects,
   * each containing the data needed to compute a text value from the
   * XML tree at parse time.
   */
  private XmlTextInfoArg[] _textArgs = null;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Constructs an <tt>XmlSpecialForm</tt> denoting a terminal value.
   */
  private XmlSpecialForm ()
    {
    }

  /**
   * Creates a canonical <tt>XmlSpecialForm</tt> denoting a terminal
   * value.
   */
  static private XmlSpecialForm _VALUE = new XmlSpecialForm();

  /**
   * Returns <i>the</i> canonical <tt>XmlSpecialForm</tt> denoting a
   * terminal value.
   */
  static public XmlSpecialForm value ()
    {
      return _VALUE;
    }

  /**
   * Constructs an <tt>XmlSpecialForm</tt> denoting a text form with the
   * given <tt><a href="XmlTextInfoArg.html">XmlTextInfoArg</a>[]</tt>
   * array of arguments.
   */
  public XmlSpecialForm (XmlTextInfoArg[] textArgs)
    {
      _textArgs = textArgs;
    }

  /**
   * Constructs an <tt>XmlSpecialForm</tt> denoting a text form with the
   * given <tt>ArrayList</tt> of <a
   * href="XmlTextInfoArg.html"><tt>XmlTextInfoArg</tt></a> arguments.
   */
  public XmlSpecialForm (ArrayList textArgs)
    {
      _textArgs = new XmlTextInfoArg[textArgs.size()];
      int i = 0;
      for (Iterator it=textArgs.iterator(); it.hasNext();)
	_textArgs[i++] = (XmlTextInfoArg)it.next();      
    }

  /**
   * Returns <tt>true</tt> iff this denotes a terminal value.
   */
  public boolean isValue ()
    {
      return _textArgs == null;
    }

  /**
   * Returns the <tt>textArgs[]</tt> array.
   */
  public XmlTextInfoArg[] textArgs ()
    {
      return _textArgs;
    }

  /**
   * Returns a <tt>String</tt> form of this <tt>XmlSpecialForm</tt>.
   */
  public final String toString ()
  {
    if (_textArgs == null)
      return "$VALUE";

    return "$TEXT " + Misc.arrayToString(_textArgs,"( "," "," )");
  }
}
