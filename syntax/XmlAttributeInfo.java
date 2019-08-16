//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.IntArrayList;

/**
 * This is the class of information parsed from an XML attribute
 * annotation. This information enables computing a text value for a
 * given attribute at parse time. Hence, what is denoted (<i>i.e.</i>,
 * what must be must be computed) from this information is a
 * <i>name/value</i> pair, where <i>name</i> is an attribute name, and
 * <i>value</i> is a text string manufactured at parse time from the
 * information packaged in an <a
 * href="XmlTextInfo.html"><tt>XmlTextInfo</tt></a> object.
 *
 * @version     Last modified on Thu Mar 24 11:11:28 2016 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class XmlAttributeInfo
{
  /**
   * <tt>_name</tt> is the XML attribute's name.
   */
  private String _name;

  /**
   * <tt>_value</tt> is the XML attribute's value.
   */
  private XmlTextInfo _value;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private static final byte _ISATTRIBUTE = 0;
  private static final byte _LITERAL_SYM = 1;
  private static final byte _ELT_CONTENT = 2;

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified name and
   * text that will stand for either (1) a reference to the name of an
   * attribute, or (2) a literal piece of text, or (3) the text contents
   * of a JDOM <tt>Element</tt>, depending on the value of kind.
   */
  private XmlAttributeInfo (String name, String text, byte kind)
    {
      _name = name;
      switch (kind)
	{
	case _ISATTRIBUTE:
	  // in this case text is an attribute name reference:
	  _value = new XmlTextInfo(text);
	  break;

	case _LITERAL_SYM:
	  // in this case text is a literal string:
	  (_value = new XmlTextInfo()).setText(text);
	  break;

	case _ELT_CONTENT:
	  // in this case text is null and the value is initialized
	  // to an empty XML tree path ...
	  _value = new XmlTextInfo(new XmlTreePath());
	  break;
	}
    }

  /**
   * Creates and returns a new <tt>XmlAttributeInfo</tt> for specified
   * attribute name and attribute reference.
   */
  static public XmlAttributeInfo refXmlAttributeInfo (String name, String ref)
    {
      return new XmlAttributeInfo(name,ref,_ISATTRIBUTE);
    }

  /**
   * Creates and returns a new <tt>XmlAttributeInfo</tt> for specified
   * attribute name and literal text.
   */
  static public XmlAttributeInfo literalXmlAttributeInfo (String name, String literal)
    {
      return new XmlAttributeInfo(name,literal,_LITERAL_SYM);
    }

  /**
   * Creates and returns a new <tt>XmlAttributeInfo</tt> for specified
   * attribute name to be initialized to some child XML node's text
   * content.
   */
  static public XmlAttributeInfo eltXmlAttributeInfo (String name)
    {
      return new XmlAttributeInfo(name,null,_ELT_CONTENT);
    }

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified
   * name and value.
   */
  public XmlAttributeInfo (String name, XmlTextInfo value)
    {
      _name = name;
      _value = value;
    }

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified name and <a
   * href="XmlTreePath.html"><tt>XmlTreePath</tt></a>.
   */
  public XmlAttributeInfo (String name, XmlTreePath path)
    {
      _name = name;
      _value = new XmlTextInfo(path);
    }

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified name, child
   * position, an <tt>int[]</tt> XML path, and a (possibly null)
   * attribute name.
   */
  public XmlAttributeInfo (String name,
			   int child,
			   int[] path,
			   String attribute)
    {
      _name = name;
      _value = new XmlTextInfo(child,path,attribute);
    }

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified name, child
   * position, an <tt>IntArrayList</tt> XML path, and a (possibly null)
   * attribute name.
   */
  public XmlAttributeInfo (String name,
			   int child,
			   IntArrayList path,
			   String attribute)
    {
      _name = name;
      _value = new XmlTextInfo(child,path,attribute);
    }

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified name and
   * special form.
   */
  public XmlAttributeInfo (String name, XmlSpecialForm form)
    {
      _name = name;
      _value = new XmlTextInfo(form);
    }

  /**
   * Constructs an <tt>XmlAttributeInfo</tt> with specified name.
   */
  public XmlAttributeInfo (String name)
    {
      this(name,XmlSpecialForm.value());
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Returns the attribute's name.
   (<i>i.e.</i>, its name).
   */
  public final String name ()
    {
      return _name;
    }

  /**
   * Returns the attribute's value.
   */
  public final XmlTextInfo value ()
    {
      return _value;
    }

  /**
   * Returns <tt>true</tt> whenever this attribute has a literak
   * string for a value.
   */
  public final boolean hasLiteralValue ()
    {
      return _value.isLiteral();
    }

  /**
   * Returns this attribute's value special form when it is one.
   */
  public final XmlSpecialForm specialForm ()
    {
      return _value.specialForm();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute's value
   * represents special form.
   */
  public final boolean isSpecialForm ()
    {
      return _value.isSpecialForm();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute's value
   * represents a <tt>$VALUE</tt> special form.
   */
  public final boolean hasTerminalValue ()
    {
      return isSpecialForm() && specialForm().isValue();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute's value
   * represents a <tt>$TEXT</tt> special form.
   */
  public final boolean hasTextForm ()
    {
      return isSpecialForm() && !specialForm().isValue();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute's value is an <a
   * href="XmlTreePath.html"><tt>XmlTreePath</tt></a> object.
   */
  public final boolean hasXmlTreePathValue ()
    {
      return _value.isXmlTreePath();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute is a reference into
   * and XML tree with non empty XML tree address (the <tt>int[]</tt>
   * path).
   */
  public final boolean hasPath ()
    {
      return _value.hasPath();
    }

  /**
   * Whenever this attribute's value is an <a
   * href="XmlTreePath.html"><tt>XmlTreePath</tt></a> object, this
   * returns the path as an <tt>int[]</tt>.
   */
  public final int[] xmlPath ()
    {
      return _value.path();
    }

  /**
   * Sets this attribute's value child position to the specified integer
   * and returns this <tt>XmlAttributeInfo</tt>.
   */
  public final XmlAttributeInfo setXmlPath (int[] path)
    {
      _value.setPath(path);
      return this;
    }

  /**
   * Returns <tt>true</tt> whenever this attribute's value is the valkue
   * of a deeply nested attribute in a child's XML tree.
   */
  public final boolean isDeepAttributeReference ()
    {
      return _value.hasXmlTreePath();
    }

  /**
   * Sets this attribute's value child position to the specified integer
   * and returns this <tt>XmlAttributeInfo</tt>.
   */
  public final XmlAttributeInfo setChild (int child)
    {
      _value.setChild(child);
      return this;
    }

  /**
   * Returns the child index of this attribute's value when this
   * value is an <a href="XmlTreePath.html"><tt>XmlTreePath</tt></a>
   * object.
   */
  public final int child ()
    {
      return _value.child();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute refers to
   * child CST.
   */
  public final boolean hasChild ()
    {
      return _value.hasChild();
    }

  /**
   * Returns <tt>true</tt> whenever this attribute is a reference to
   * the value of another attribute.
   */
  public final boolean hasAttribute ()
    {
      return _value.hasAttribute();
    }

  /**
   * Returns the name of the attribute referenced by the XML tree path.
   * This assumes that <tt>_value</tt> is an <tt>XmlTreePath</tt>.
   */
  public final String attributeNameRef ()
    {
      return _value.xmlTreePath().attribute();
    }

  /**
   * Returns a <tt>String</tt> form for this <tt>XmlAttributeInfo</tt>.
   */
  public final String toString ()
    {
      return _name + " = " + _value;
    }
}
