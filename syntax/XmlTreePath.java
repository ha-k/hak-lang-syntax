//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.ArrayList;
import hlt.language.util.IntArrayList;
import hlt.language.tools.Misc;

/**
 * This class packages the information parsed from an XML serialization
 * annotation to denote a path in the XML tree corresponding to a given
 * CST child. It is of the form
 * <tt>c[x<sub>1</sub>...x<sub>n</sub>]/a</tt>, where <tt>c</tt> is a
 * positive integer denoting a CST child position, the
 * <tt>x<sub>i</sub></tt>'s are positive integers denoting a path in the
 * XML tree for the CST corresponding to this child position, and
 * <tt>a</tt> is a symbol; both <tt>[x<sub>1</sub>...x<sub>n</sub>]</tt>
 * and <tt>/a</tt> are optional.
 *
 * @version     Last modified on Fri Apr 13 20:20:47 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class XmlTreePath
{
  /**
   * This is the child CST's index.
   */
  private int _child;

  /**
   * <tt>_path</tt> is the XML path: if non <tt>null</tt>, a tree
   * address in the XML form of the CST child indicated by
   * <tt>_child</tt>.
   */
  private int[] _path = null;

  /**
   * When non-null, <tt>_attribute</tt> is a <tt>String</tt>
   * that is the name of a deep XML tree attribute reference.
   */
  private String _attribute = null;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  public XmlTreePath ()
    {
    }

  /**
   * Constructs an <tt>XmlTreePath</tt> with child position and
   * attribute reference name.
   */
  public XmlTreePath (int child, String attribute)
    {
      _child = child;
      _attribute = attribute;
    }

  /**
   * Constructs an <tt>XmlTreePath</tt> with child position, an
   * <tt>int[]</tt> xml tree address, and attribute reference name.
   */
  public XmlTreePath (int child, int[] path, String attribute)
    {
      _child = child;
      _path = path;
      _attribute = attribute;
    }

  /**
   * Constructs an <tt>XmlTreePath</tt> with child position, an
   * <tt>IntArreyList</tt> xml tree address, and attribute reference name.
   */
  public XmlTreePath (int child,
		      IntArrayList path,
		      String attribute)
    {
      this(child,
	   path == null ? null : path.toArray(),
	   attribute);
    }

  /**
   * Constructs an <tt>XmlTreePath</tt> with unspecified child position
   * nor tree address, and specified attribute reference name.
   */
  public XmlTreePath (String attribute)
    {
      _attribute = attribute;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Sets the child index to the specified integer and returns this
   * <tt>XmlTreePath</tt>.
   */
  public XmlTreePath setChild (int child)
    {
      _child = child;
      return this;
    }

  /**
   * Returns the child index.
   */
  public int child ()
    {
      return _child;
    }

  /**
   * Returns <tt>true</tt> whenever this has a known child index.
   */
  public boolean hasChild ()
    {
      return _child > 0;
    }
  /**
   * Sets the XML path to the specified <tt>int[]</tt> and returns this
   * <tt>XmlTreePath</tt>.
   */
  public XmlTreePath setPath (int[] path)
    {
      _path = path;
      return this;
    }

  /**
   * Sets the XML path to the specified <tt>int[]</tt> and returns this
   * <tt>XmlTreePath</tt>.
   */
  public XmlTreePath setPath (IntArrayList path)
    {
      _path = path.toArray();
      return this;
    }

  /**
   * Returns the path.
   */
  public int[] path ()
    {
      return _path;
    }

  /**
   * Returns attribute.
   */
  public String attribute ()
    {
      return _attribute;
    }

  /**
   * Returns <tt>true</tt> whenever this actually refers to an
   * attribute.
   */
  public boolean hasAttribute ()
    {
      return _attribute != null;
    }
  /**
   * Returns a <tt>String</tt> form of this <tt>XmlTreePath</tt>
   * of the form <tt>c[x<sub>1</sub> ... x<sub>n</sub>]/a</tt>.
   */
  public final String toString ()
  {
    StringBuilder s = new StringBuilder();

    s.append(_child);

    if (_path != null && _path.length != 0)
      s.append(Misc.arrayToString(_path,"[",".","]"));

    if (_attribute != null)
      s.append("/").append(_attribute);

    return s.toString();
  }
}
