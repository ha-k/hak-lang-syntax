//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import org.jdom2.Element;

import hlt.language.util.ArrayList;
import hlt.language.util.IntToIntMap;
import hlt.language.util.IntArrayList;
import hlt.language.tools.Misc;

/**
 * This is the class of information packaging an argument of a
 * <tt>$TEXT</tt> special forms. A special form <tt>$TEXT(...)</tt> has
 * arguments, each one being either:
 *
 * <ol>
 *
 * <li> a literal (unquoted, or posssibly single- or double-quote)
 *      string; or,
 *
 * <li> an <a href="XmlTreePath.html"><tt>XmlTreePath</tt></a> representing
 *      a <i>path expression</i> of the form <tt>c[x<sub>1</sub>...x<sub>n</sub>]/a</tt>,
 *      where <tt>c</tt> and the <tt>x<sub>i</sub></tt>'s are positive
 *      integers, <tt>a</tt> is a symbol, and where both
 *      <tt>[x<sub>1</sub>...x<sub>n</sub>]</tt> and <tt>/a</tt>, are
 *      optional.
 *
 * </ol>
 *
 * @version     Last modified on Wed Jul 25 07:08:51 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class XmlTextInfoArg
{
  /**
   * If non-null, <tt>xmlTreePath</tt> indicates a node in an XML tree
   * below whose text contents is then the value of <tt>_text</tt>.
   */
  protected XmlTreePath _xmlTreePath;

  /**
   * When <tt>_xmlTreePath == null</tt>, <tt>_text</tt>, this argument
   * contains a literal text string.
   */
  protected String _text;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Constructs an empty <tt>XmlTextInfoArg</tt>.
   */
  public XmlTextInfoArg ()
    {
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Constructs an <tt>XmlTextInfoArg</tt> with specified child position
   * and an attribute name.
   */
  public XmlTextInfoArg (XmlTreePath xmlTreePath)
    {
      _xmlTreePath = xmlTreePath;
    }

  /**
   * Constructs an <tt>XmlTextInfoArg</tt> with specified child position,
   * (possibly null) xml path, and a (possibly null) attribute name.
   */
  public XmlTextInfoArg (String attribute)
    {
      this(new XmlTreePath(attribute));
    }

  /**
   * Constructs an <tt>XmlTextInfoArg</tt> with specified child position
   * and a (possibly null) attribute name.
   */
  public XmlTextInfoArg (int child,
			 String attribute)
    {
      this(new XmlTreePath(child,attribute));
    }

  /**
   * Constructs an <tt>XmlTextInfoArg</tt> with specified child position,
   * (possibly null) xml path, and a (possibly null) attribute name.
   */
  public XmlTextInfoArg (int child,
			 int[] path,
			 String attribute)
    {
      this(new XmlTreePath(child,path,attribute));
    }

  /**
   * Constructs an <tt>XmlTextInfoArg</tt> with specified child position,
   * (possibly null) xml path, and a (possibly null) attribute name.
   */
  public XmlTextInfoArg (int child,
			 IntArrayList path,
			 String attribute)
    {
      this(new XmlTreePath(child,path,attribute));
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Returns <tt>true</tt> whenever this represents a literal string.
   */
  public boolean isLiteral ()
    {
      return _text != null;
    }

  /**
   * Returns the text string.
   */
  public String text ()
    {
      return _text;
    }

  /**
   * Sets the <tt>_text</tt> filed to <tt>text</tt> and returns
   * this.
   */
  public XmlTextInfoArg setText (String text)
    {
      _text = text;
      return this;
    }

  /**
   * Returns the child index, when there is one.
   */
  public int child ()
    {
      return _xmlTreePath.child();
    }

  /**
   * Returns <tt>true</tt> whenever this has a child index.
   */
  public boolean hasChild ()
    {
      return _xmlTreePath != null && _xmlTreePath.hasChild();
    }

  /**
   * Sets the child position index to the specified integer
   * and returns this <tt>XmlTextInfoArg</tt>.
   */
  public XmlTextInfoArg setChild (int child)
    {
      _xmlTreePath.setChild(child);
      return this;
    }

  /**
   * Returns the <a href="XmlTreePath"><tt>XmlTreePath</tt></a> object
   * if there is one, or <tt>null</tt>, otherwise.
   */
  public XmlTreePath xmlTreePath ()
    {
      return _xmlTreePath;
    }

  /**
   * Sets <tt>_xmlTreePath</tt> to the specified <tt>XmlTreePath</tt> and
   * returns this <tt>XmlTextInfoArg</tt>.
   */
  public XmlTextInfoArg setXmlTreePath (XmlTreePath path)
    {
      _xmlTreePath = path;
      return this;
    }

  /**
   * Returns <tt>true</tt> whenever this has a non-null
   * <tt>XmlTreePath</tt>.
   */
  public boolean hasXmlTreePath ()
    {
      return _xmlTreePath != null;
    }

  /**
   * Returns the XML tree address, when there is one.
   */
  public int[] path ()
    {
      return _xmlTreePath.path();
    }

  /**
   * Returns <tt>true</tt> whenever this has a non-vacuous XML tree
   * address.
   */
  public boolean hasPath ()
    {
      return hasXmlTreePath() && path() != null;
    }

  /**
   * Sets the XML tree address path to the specified <tt>int[]</tt> and
   * returns this <tt>XmlTextInfoArg</tt>.
   */
  public XmlTextInfoArg setPath (int[] path)
    {
      _xmlTreePath.setPath(path);
      return this;
    }

  /**
   * Returns the Xml tree attribute, when there is one.
   */
  public String attribute ()
    {
      return _xmlTreePath.attribute();
    }

  /**
   * Returns the <tt>true</tt> whenever this refers to an attribute.
   */
  public boolean hasAttribute ()
    {
      return _xmlTreePath != null && _xmlTreePath.hasAttribute();
    }

  /**
   * Returns a text form for this <tt>XmlTextInfoArg</tt> given
   * the supplied context.
   */
  public String makeTextForm (ArrayList xmlForms, IntToIntMap childMap)
    {
      if (isLiteral())
	return _text;

      int formPos = childMap.get(child());
      ArrayList xmlForm = (ArrayList)xmlForms.get(formPos);
      Element ref = (Element)xmlForm.get(0);

      if (hasPath())
	for (int j=0; j<path().length; j++)
	  ref = (Element)ref.getContent(path()[j]-1);

      if (hasAttribute())
	return ref.getAttributeValue(attribute());

      return ref.getText();
    }

  /**
   * Returns a <tt>String</tt> form of this <tt>XmlTextInfoArg</tt>.
   */
  public String toString ()
    {
      if (isLiteral())
	return "\""+_text+"\"";

      return _xmlTreePath.toString();
    }
}
