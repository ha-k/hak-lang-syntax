//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.ArrayList;
import hlt.language.util.IntArrayList;
import hlt.language.tools.Misc;

/**
 * The class <tt>XmlTextInfo</tt> packages information parsed from an
 * XML serialization annotation, which may be of one of the following
 * three possible kinds:
 *
 * <ul>
 *
 * <li> a <i>literal</i> (unquoted, single-quoted, or double-quoted) text
 *      string; or,
 *
 * <li> a <i>path expression</i> (of type an <a
 *      href="XmlTreePath.html"><tt>XmlTreePath</tt></a>) of the form
 *      <tt>c[x<sub>1</sub>...x<sub>n</sub>]/a</tt>, where <tt>c</tt>
 *      and the <tt>x<sub>i</sub></tt>'s are positive integers,
 *      <tt>a</tt> is a symbol, and where both
 *      <tt>[x<sub>1</sub>...x<sub>n</sub>]</tt> and <tt>/a</tt>, are
 *      optional; or,
 *
 * <li> a <i>special form</i> of the form <tt>$VALUE</tt> or <tt>$TEXT(...)</tt>.
 *
 * </ul>
 *
 * Thus, its structure extends that of an <a
 * href="XmlTextInfoArg.html"><tt>XmlTextInfoArg</tt></a>,
 * which explains why it is a subclass of it.
 *
 * <p>
 * 
 *
 * Basically, this contains all the information needed to manufacture a
 * piece of text out of the partial CST and XML trees at parse time that
 * will be the actual value of <i>text</i>. It can have one of five forms:
 *
 * <ol>
 *
 * <li> a literal (unquoted, or posssibly single- or double-quote) string;
 *
 * <li> a reference to an XML tree node's
 *      <ol type="a">
 *      <li> text contents, in the form   <tt>c</tt> or <tt>c[x1 ... xn]</tt>; or
 *
 *      <li> attribute's value (in the form <tt>c/a</tt> or <tt>c[x1 ... xn]/a</tt>);
 *      </ol>
 *
 * <li> the special form
 *      <ol type="a">
 *      <li> <tt>$VALUE</tt>; or,
 *
 *      <li> <tt>$TEXT(...)</tt>, where each argument is
 *           either:
 *           <ol type="i">
 *
 *           <li> a literal (unquoted, or posssibly single- or double-quote)
 *                string; or,
 *
 *           <li> a reference to an XML tree node (in the form
 *                <tt>c</tt> or <tt>c[x1 ... xn]</tt>); or,
 *
 *           <li> a reference to an XML tree node's attribute's value
 *                (in the form <tt>c/a</tt> or <tt>c[x1 ... xn]/a</tt>).
 *
 *           </ol>
 *      </ol>
 * </ol>
 *
 * According to which the attribute value computed in each case will
 * be, respectively:
 *
 * <ul>
 *
 * <li> <b>Case 1:</b> the literal string;
 *
 * <li> <b>Case 2.a:</b> the text content of the referenced XML tree node;
 *
 * <li> <b>Case 2.b:</b> the text value of the referenced  XML tree node's attribute;
 *
 * <li> <b>Case 3.a:</b> the value parsed for the corresponding terminal;
 *
 * <li> <b>Case 3.b:</b> the concatenation of the text strings corresponding
 *      to the parse-time values denoted by each argument.
 *
 * </ul>
 *
 * @version     Last modified on Fri Apr 13 20:22:30 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class XmlTextInfo extends XmlTextInfoArg
{
  /**
   * When non-null, this <tt>XmlTextInfo</tt> is a special form and
   * <tt>_specialForm</tt> contains the special form data it needs.
   */
  private XmlSpecialForm _specialForm = null;

  /**
   * <tt>isSpecialForm()</tt> returns <tt>true</tt> whenever
   * this is a special form.
   */
  public boolean isSpecialForm ()
    {
      return _specialForm != null;
    }
      
  /**
   * Returns this <tt>XmlTextInfo</tt>'s <tt>_specialForm</tt>.
   */
  public XmlSpecialForm specialForm ()
    {
      return _specialForm;
    }
      
  /**
   * <tt>isXmlTreePath()</tt> returns <tt>true</tt> whenever
   * this is an XML tree path.
   */
  public boolean isXmlTreePath ()
    {
      return hasXmlTreePath ();
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Constructs an empty <tt>XmlTextInfoArg</tt>.
   */
  public XmlTextInfo ()
    {
    }

  /**
   * Constructs an <tt>XmlTextInfo</tt> with specified <a
   * href="XmlTreePath.html"><tt>XmlTreePath</tt></a>.
   */
  public XmlTextInfo (XmlTreePath xmlTreePath)
    {
      super(xmlTreePath);
    }

  /**
   * Constructs an <tt>XmlTextInfo</tt> with specified child position,
   * (possibly null) xml path, and a (possibly null) attribute name.
   */
  public XmlTextInfo (int child,
		      int[] path,
		      String attribute)
    {
      super(child,path,attribute);
    }

  /**
   * Constructs an <tt>XmlTextInfo</tt> with specified child position,
   * (possibly null) xml path, and a (possibly null) attribute name.
   */
  public XmlTextInfo (int child,
		      IntArrayList path,
		      String attribute)
    {
      super(child,path,attribute);
    }

  /**
   * Constructs an <tt>XmlTextInfo</tt> with specified special
   * form.
   */
  public XmlTextInfo (XmlSpecialForm form)
    {
      _specialForm = form;
    }

  /**
   * Constructs an <tt>XmlTextInfo</tt> with specified attribute name.
   */
  public XmlTextInfo (String attribute)
    {
      super(attribute);
    }

  /**
   * Constructs an <tt>XmlTextInfo</tt> with specified child position,
   * 
   */
  public XmlTextInfo (int child,
		      IntArrayList path,
		      boolean isTerminal,
		      ArrayList textInfo,
		      String attribute)
    {
      this(child,path,attribute);
      if (isTerminal)
	_specialForm = XmlSpecialForm.value();
      else
	_specialForm = new XmlSpecialForm(textInfo);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Returns a <tt>String</tt> form of this <tt>XmlTextInfo</tt>.
   */
  public final String toString ()
  {
    if (isSpecialForm())
      return _specialForm.toString();

    return super.toString();
  }
}
