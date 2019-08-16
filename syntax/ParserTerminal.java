//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * This is the class of terminal symbols used by the parser at parse
 * time.
 *
 * @see         ParserSymbol
 *
 * @version	Last modified on Fri Apr 13 20:11:36 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright	&copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParserTerminal extends ParserSymbol
{
  public ParserTerminal (String name, int index, int precedence, int associativity)
    {
      super(name,GenericParser.terminals,index);
      _precedence = precedence;
      _associativity = associativity;
    }

  private int _precedence;

  /**
   * This symbol's precedence.
   */
  public int precedence ()
    {
      return _precedence;
    }

  private int _associativity;

  /**
   * This symbol's associativity.
   */
  public int associativity ()
    {
      return _associativity;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // XML serialization information
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * A storage for recording xml annotation for this terminal if any.
   */
  private XmlInfo _xmlInfo = null;

  XmlInfo xmlInfo ()
    {
      return _xmlInfo;
    }
    
  public ParserTerminal setXmlInfo (XmlInfo info)
    {
      _xmlInfo = info.setIsTerminal();
      return this;
    }
  
  public ParserTerminal addXmlInfo (int[] children)
    {
      _xmlInfo.setChildren(children);
      return this;
    }
  
  public ParserTerminal addXmlInfo (XmlAttributeInfo[] attributes)
    {
      _xmlInfo.setAttributes(attributes);
      return this;
    }
  
  public ParserTerminal addXmlInfo (XmlWrapper[][] wrapperPaths)
    {
      _xmlInfo.setWrapperPaths(wrapperPaths);
      return this;
    }
  
  public ParserTerminal addXmlInfo (String[] attributeRefs)
    {
      _xmlInfo.setAttributeRefs(attributeRefs);
      return this;
    }
  
  public ParserTerminal setTerminalValueFlag ()
    {
      _xmlInfo.setTerminalValueFlag();
      return this;
    }
  
  public String toString()
    {
      return '\''+name()+'\'';
    }
}
