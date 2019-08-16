//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import org.jdom2.*;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.io.PrintStream;

import hlt.language.util.ArrayList;
import hlt.language.util.IntToIntMap;
import hlt.language.tools.Misc;

/**
 * This is the class of information concerning XML annotation.
 *
 *
 * @version     Last modified on Wed Jul 25 07:08:31 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class XmlInfo
{
  // The contents:

  /**
   * <tt>_isTerminal</tt> is a flag that is set to <tt>true</tt> to
   * indicate that this <tt>XmlInfo</tt> comes from a terminal's
   * annotation. (Otherwise, it is that of a rule.)
   */
  private boolean _isTerminal = false;

  /**
   * <tt>_terminalValueFlag</tt> may be <tt>true</tt> only when this
   * <tt>XmlInfo</tt> comes from the annotation of a terminal rather
   * than that of rule, and when it is so, it indicates that the
   * terminal will be serialized as a JDOM element whose content
   * is the string value parsed for this terminal.
   */
  private boolean _terminalValueFlag;

  /**
   * <tt>_nsPrefix</tt> is the XML namespace prefix, if any. If not "",
   * it contains the identifier label to use as the short name for the
   * qualified name of this XML element.
   */
  private String _nsPrefix = "";

  /**
   * <tt>_localName</tt> is the XML element local name, if any. If non
   * null, it contains the label identifier to use for local name of
   * this XML element.
   */
  private String _localName = null;

  /**
   * <tt>_attributes</tt> is the XML element's attribute table, if any,
   * to use for the XML element specified by this <tt>xmlInfo</tt>. If
   * non null, it is an array of <tt>XmlAttributeInfo</tt>'s, each
   * consisting of a </tt>String</tt> (the name of the attribute), and
   * its value setting information.
   */
  private XmlAttributeInfo[] _attributes = null;

  /**
   * <tt>_children</tt> is an array of indices - those of the children
   * of the CST this corresponds to.
   */
  private int[] _children;

  /**
   * <tt>_xmlPaths</tt> is an array of arrays of XML tree indices (a
   * tree address) - those of a path into a descendant's XML tree.
   * (Therefore <tt>_XmlPaths.length == _children.length</tt>.)
   */
  private int[][] _xmlPaths;

  /**
   * <tt>_wrapperPaths</tt> is an array of </tt>XmlWrapper</tt>s each
   * containing an XML tag and its wrapping distribution flag.
   * (Therefore <tt>_wrapperPaths.length == _children.length</tt>.)
   */
  private XmlWrapper[][] _wrapperPaths;

  /**
   * <tt>_attributeRefs</tt> is an array of <tt>String</tt>s.  When
   * such a string is non null, it is an attribute name of an XML tree
   * node and denotes the contents of this attribute.  (Therefore
   * <tt>_attributeRefs.length == _children.length</tt>.)
   */
  private String[] _attributeRefs;

  /**
   * <tt>_textInfos</tt> contains the text patterns, if any, for
   * building text as the contents standing for a child XML element.
   */
  private XmlTextInfo[][] _textInfos = null;

  //////////////////////////////////////////////////////////////////////

  // The constructors:

  public XmlInfo (String localName)
    {
      _localName = localName;
    }

  public XmlInfo (String localName, String nsPrefix)
    {
      _localName = localName;
      _nsPrefix = nsPrefix;
    }

  public XmlInfo (String localName, String nsPrefix,
                  XmlAttributeInfo[] attributes,
                  int[] children, int[][] xmlPaths,
                  XmlWrapper[][] wrapperPaths,
                  String[] attributeRefs,
                  boolean terminalValueFlag,
                  XmlTextInfo[][] textInfos)
    {
      _localName = localName;
      _nsPrefix = nsPrefix;
      _attributes = attributes;
      _children = children;
      _xmlPaths = xmlPaths;
      _wrapperPaths = wrapperPaths;
      _attributeRefs = attributeRefs;
      _terminalValueFlag = terminalValueFlag;
      _textInfos = textInfos;
    }

  // Getters, setters, and testers:

  public final boolean terminalValueFlag ()
    {
      return _terminalValueFlag;
    }

  public XmlInfo setTerminalValueFlag (boolean flag)
    {
      _terminalValueFlag = flag;
      return this;
    }

  public XmlInfo setTerminalValueFlag ()
    {
      _terminalValueFlag = true;
      return this;
    }

  public final boolean hasTerminalValue ()
    {
      return _terminalValueFlag;
    }

  public final boolean isTerminal ()
    {
      return _isTerminal;
    }

  public XmlInfo setIsTerminal (boolean flag)
    {
      _isTerminal = flag;
      return this;
    }

  public XmlInfo setIsTerminal ()
    {
      _isTerminal = true;
      return this;
    }

  public final String nsPrefix ()
    {
      return _nsPrefix;
    }

  public final boolean hasNsPrefix ()
    {
      return _nsPrefix != "";
    }

  public XmlInfo setNsPrefix (String nsPrefix)
    {
      _nsPrefix = nsPrefix;
      return this;
    }

  public final String localName ()
    {
      return _localName;
    }

  public XmlInfo setLocalName (String localName)
    {
      _localName = localName;
      return this;
    }

  public final XmlAttributeInfo[] attributes ()
    {
      return _attributes;
    }

  public XmlInfo setAttributes (XmlAttributeInfo[] attributes)
    {
      _attributes = attributes;
      return this;
    }

  public final boolean hasAttributes ()
    {
      return _attributes != null && _attributes.length != 0;
    }

  public final int[] children ()
    {
      return _children;
    }

  public XmlInfo setChildren (int[] children)
    {
      _children = children;
      return this;
    }

  public final boolean hasChildren ()
    {
      return _children != null && _children.length != 0;
    }

  public final int[][] xmlPaths ()
    {
      return _xmlPaths;
    }

  public XmlInfo setXmlPaths (int[][] paths)
    {
      _xmlPaths = paths;
      return this;
    }

  public final boolean hasXmlPaths ()
    {
      if (_xmlPaths == null || _xmlPaths.length == 0)
        return false;

      for (int i=0; i<_xmlPaths.length; i++)
        if (_xmlPaths[i] != null)
          return true;

      return false;
    }

  public final XmlWrapper[][] wrapperPaths ()
    {
      return _wrapperPaths;
    }

  public XmlInfo setWrapperPaths (XmlWrapper[][] paths)
    {
      _wrapperPaths = paths;
      return this;
    }

  public final boolean hasWrapperPaths ()
    {
      if (_wrapperPaths == null || _wrapperPaths.length == 0)
        return false;

      for (int i=0; i<_wrapperPaths.length; i++)
        if (_wrapperPaths[i] != null)
          return true;

      return false;
    }

  public final String[] attributeRefs ()
    {
      return _attributeRefs;
    }

  public XmlInfo setAttributeRefs (String[] refs)
    {
      _attributeRefs = refs;
      return this;
    }

  public String attributeRefs (int i)
    {
      return _attributeRefs[i];
    }

  /**
   * <tt>hasAttributeRefs()</tt> returns <tt>true</tt> iff the array of
   * strings <tt>_attributeRefs</tt> is non null and contains at least
   * one non-null string.
   */
  public final boolean hasAttributeRefs ()
    {
      if (_attributeRefs == null)
        return false;

      for (int i=_attributeRefs.length; i-->0;)
        if (_attributeRefs[i] != null)
          return true;

      return false;
    }

  public XmlInfo setTextInfos (XmlTextInfo[][] infos)
    {
      _textInfos = infos;
      return this;
    }

  /**
   * <tt>hasTextInfos()</tt> returns <tt>true</tt> iff the array of
   * string arrays <tt>_textInfos</tt> is non null and contains at least
   * one non-null array.
   */
  public final boolean hasTextInfos ()
    {
      if (_textInfos == null)
        return false;

      for (int i=_textInfos.length; i-->0;)
        if (_textInfos[i] != null)
          return true;

      return false;
    }

  ////////////////////////////////////////////////////////////////////////

  /**
   * Throws a <tt>BadXmlAnnotationException</tt> if and only if
   * this <tt>XmlInfo</tt> is inconsistent with the specified
   * rule.
   *
   * <p><hr><p>
   *
   * <span style="color:tan; font-size:smaller"><tt><i>[See what <a
   * href="xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#consistency">
   * consistent annotation</a> means for a rule.]</i></tt></span>
   */
  public void checkConsistency (Rule r) throws BadXmlAnnotationException
    {
      // Does nothing for now ... To do later.

      //throw new BadXmlAnnotationException();
    }

  /**
   * Throws a <tt>BadXmlAnnotationException</tt> if and only if
   * this <tt>XmlInfo</tt> is inconsistent with the specified
   * terminal.
   *
   * <p><hr><p>
   *
   * <span style="color:tan; font-size:smaller"><tt><i>[See what <a
   * href="xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#consistency">
   * consistent annotation</a> means for a terminal.]</i></tt></span>
   */
  public void checkConsistency (Terminal t) throws BadXmlAnnotationException
    {
      // Does nothing for now ... To do later.

      //throw new BadXmlAnnotationException();
    }

  ////////////////////////////////////////////////////////////////////////

  // The real work starts here...

  /**
   * Uses the specified <a
   * href="ParserGenerator.html"><tt>ParserGenerator</tt></a> to
   * generate the parser's Java code recording this <tt>XmlInfo</tt>
   * when it is that of a <tt>Terminal</tt>.  Namely, it generates the
   * code that will initialize <tt>terminals[index].xmlInfo()</tt>.  The
   * setup is that of classical instruction generation, writing out
   * packaged instructions that will do the actual work at parse time
   * for each annotated terminal from the information present in that
   * terminal being recognized. Hence, <a
   * href="ParserTerminal.html"><tt>ParserTerminal</tt></a> has methods
   * defined to set <tt>XmlInfo</tt> contents - typically called
   * <tt>setXmlInfo(...)</tt> with arguments of appropriate types of XML
   * info components.  That information is then used at parse time by
   * the methods <a
   * href="#processTerminal"><tt>processTerminal</tt></a>, that takes
   * the context of a <a href="ParseNode.html"><tt>ParseNode</tt></a> to
   * generate the actual JDOM constructs corresponding to the annotation
   * that speciified this terminal's <tt>XmlInfo</tt>.
   */

  public void generateTerminalXmlInfo (ParserGenerator g, int index) throws IOException
  {
    // generate an open local scope for local array declarations:
    g.pl("      { // Code for XML serialization annotation:");
    g.pl("        // "+this);    

    // generate the setXmlInfo instruction for terminals[index] to
    // construct a new XmlInfo object with the info's local name and
    // namespace prefix:
    g.pl("        terminals["+index+"].setXmlInfo(new XmlInfo(\""+_localName+"\""
         +(_nsPrefix == "" ? "" : ",\""+_nsPrefix+"\"")+"));");

    if (hasAttributes())
      {
        // define an array of XmlAttributeInfo's (called
        // xmlAttributes) of length that of _attributes:
        g.pl("        XmlAttributeInfo[] xmlAttributes = new XmlAttributeInfo["
             +_attributes.length+"];");
        // generate instructions initializing the array
        // xmlAttributes with the contents of _attributes:
        for (int i=0; i<_attributes.length; i++)
          generateAttributeInitializers(g,i,_attributes[i]);
        g.pl("        terminals["+index+"].addXmlInfo(xmlAttributes);");
      }

    if (hasWrapperPaths())
      {
        g.pl("        XmlWrapper[][] paths = new XmlWrapper["
             +_wrapperPaths.length+"][];");
        g.pl("        XmlWrapper[] path;");
        for (int i=0; i<_wrapperPaths.length; i++)
          {
            if (_wrapperPaths[i]!=null)
              {
                g.pl("        path = new XmlWrapper["
                     +_wrapperPaths[i].length+"];");
                for (int j=0; j<_wrapperPaths[i].length; j++)
                  g.pl("        path["+j+
                       "] = new XmlWrapper(\""+_wrapperPaths[i][j].getTag()
                       +(_wrapperPaths[i][j].isStarred()?"\",true":"\"")+");");
                g.pl("        paths["+i+"] = path;");
              }
          }
        g.pl("        terminals["+index+"].addXmlInfo(paths);");
      }

    if (isTerminal() && hasTerminalValue())
      g.pl("        terminals["+index+"].setTerminalValueFlag();");

    // generate a closing local scope for local array declarations:
    g.pl("      }");      
  }

  /**
   * Uses the specified <a
   * href="ParserGenerator.html"><tt>ParserGenerator</tt></a> to
   * generate the parser's Java code recording this rule's
   * <tt>XmlInfo</tt> where appropriate. Namely, at index <tt>index</tt>
   * in the array <tt>rules</tt>. The set up is that of classical
   * instruction generation writing out packaged instructions that will
   * do the actual work at parse time for each annotated terminal and
   * rule from the information present in that rule being reduced.
   * Hence, a <a href="ParserRule.html"><tt>ParserRule</tt></a> has
   * methods defined to attach <tt>XmlInfo</tt> contents to a rule -
   * typically called <tt>setXmlInfo(...)</tt>, with arguments of
   * appropriate types of XML info components.  That information is then
   * used at parse time by the methods <a
   * href="#processAttributes"><tt>processAttributes</tt></a> and <a
   * href="#processChildren"><tt>processChildren</tt></a> that both take
   * the context of a <a href="ParseNode.html"><tt>ParseNode</tt></a> to
   * generate the actual JDOM constructs corresponding to the annotation
   * that yielded this rule's <tt>XmlInfo</tt>.
   */
  public void generateRuleXmlInfo (ParserGenerator g, int index) throws IOException
  {
    // generate a local scope for local array declarations:
    g.pl("      { // Code for XML serialization annotation:");
    g.pl("        // "+this);    

    // generate the setXmlInfo instruction for rules[index] to
    // construct a new XmlInfo object with the info's local name and
    // namespace prefix:
    g.pl("        rules["+index+"].setXmlInfo(new XmlInfo(\""+_localName+"\""
         +(_nsPrefix == "" ? "" : ",\""+_nsPrefix+"\"")+"));");

    // if there are attributes or children, generate the instructions
    // to add those to this new XmlInfo object:
    if (hasAttributes() || hasChildren())
      {
        if (hasAttributes())
          {
            // define an array of XmlAttributeInfo's (called
            // xmlAttributes) of length that of _attributes:
            g.pl("        XmlAttributeInfo[] xmlAttributes = new XmlAttributeInfo["
                 +_attributes.length+"];");
            // generate instructions initializing the array
            // xmlAttributes with the contents of _attributes:
            for (int i=0; i<_attributes.length; i++)
              generateAttributeInitializers(g,i,_attributes[i]);
            g.pl("        rules["+index+"].addXmlInfo(xmlAttributes);");
          }

        if (hasChildren())
          {
            // define an array of int's (called xmlChildren) containing
            // the elements of _children:
            g.pl("        int[] xmlChildren = "
                 +Misc.arrayToString(_children,"{",",","}")+";");
            g.pl("        rules["+index+"].addXmlInfo(xmlChildren);");

            if (hasXmlPaths())
              {
                g.pl("          {");
                g.pl("             int[][] paths = new int["+_xmlPaths.length+"][];");
                g.pl("             int[] path;");
                for (int i=0; i<_xmlPaths.length; i++)
                  {
                    if (_xmlPaths[i]!=null)
                      {
                        g.pl("             path = new int["+_xmlPaths[i].length+"];");
                        for (int j=0; j<_xmlPaths[i].length; j++)
                          g.pl("             path["+j+"] = "+_xmlPaths[i][j]+";");
                        g.pl("             paths["+i+"] = path;");
                      }
                  }
                g.pl("             rules["+index+"].addXmlInfo(paths);");
                g.pl("          }");
              }

            if (hasWrapperPaths())
              {
                g.pl("        XmlWrapper[][] paths = new XmlWrapper["
                     +_wrapperPaths.length+"][];");
                g.pl("        XmlWrapper[] path;");
                for (int i=0; i<_wrapperPaths.length; i++)
                  {
                    if (_wrapperPaths[i]!=null)
                      {
                        g.pl("        path = new XmlWrapper["
                             +_wrapperPaths[i].length+"];");
                        for (int j=0; j<_wrapperPaths[i].length; j++)
                          g.pl("        path["+j+
                               "] = new XmlWrapper(\""+_wrapperPaths[i][j].getTag()
                               +(_wrapperPaths[i][j].isStarred()?"\",true":"\"")+");");
                        g.pl("        paths["+i+"] = path;");
                      }
                  }
                g.pl("        rules["+index+"].addXmlInfo(paths);");
              }

            if (hasAttributeRefs())
              {
                g.pl("        String[] refs = new String["+_attributeRefs.length+"];");
                for (int i=0; i<_attributeRefs.length; i++)
                  if (_attributeRefs[i]!=null)
                    g.pl("        refs["+i+"] = \""+_attributeRefs[i]+"\";");
                g.pl("        rules["+index+"].addXmlInfo(refs);");
              }

            if (hasTextInfos())
              {
                g.pl("        XmlTextInfo[][] infos = new XmlTextInfo["+_textInfos.length+"][];");

                for (int i=0; i<_textInfos.length; i++)
                  if (_textInfos[i]!=null)
                    {
                      g.pl("        infos["+i+"] = new XmlTextInfo["+_textInfos[i].length+"];");
                      for (int j=0; j<_textInfos[i].length; j++)
                        g.pl("        infos["+i+"]["+j+"] = new XmlTextInfo("+
                             (_textInfos[i][j].child() == 0
                              ? ""
                              : _textInfos[i][j].child()+",")+
                             "\""+_textInfos[i][j].text()+"\");");
                    }
                g.pl("        rules["+index+"].addXmlInfo(infos);");
              }
          }

      }
    g.pl("      }");
  }
  
  /**
   * Uses the specified <a
   * href="ParserGenerator.html"><tt>ParserGenerator</tt></a> to
   * generate the parser's Java code recording the specified
   * <tt>XmlAttributeInfo</tt> where appropriate. Namely, at index
   * <tt>index</tt> in the array <tt>XmlAttributes</tt>.
   */
  public void generateAttributeInitializers (ParserGenerator g, int index,
                                             XmlAttributeInfo a)
    throws IOException
  {
    if (a.isSpecialForm())
      {
        if (a.hasTerminalValue())
          { // $VALUE form:
            g.pl("        xmlAttributes["+index+"] = new XmlAttributeInfo(\""
                 +a.name()+"\");");

            return;
          }

        // $TEXT form:
        XmlTextInfoArg[] args = a.value().specialForm().textArgs();
        g.pl("        XmlTextInfoArg[] formArgs = new XmlTextInfoArg["
             +args.length+"];");

        for (int i=0; i<args.length; i++)
          g.pl(makeTextInfoArg(args[i],i));

        g.pl("        XmlSpecialForm textForm = new XmlSpecialForm(formArgs);");
        g.pl("        xmlAttributes["+index+"] = new XmlAttributeInfo(\""
             +a.name()+"\",textForm);");

        return;
      }

    if (a.hasLiteralValue())
      { // literal text form:
        g.pl("        xmlAttributes["+index+"] = XmlAttributeInfo.literalXmlAttributeInfo(\""
             +a.name()+"\",\""+a.value().text()+"\");");

        return;
      }

    // Child reference form:

    if (a.hasAttribute())
      g.pl("        xmlAttributes["+index+"] = XmlAttributeInfo.refXmlAttributeInfo(\""
           +a.name()+"\",\""+a.attributeNameRef()+"\");");
    else
      // then, a must be of the form c[x...xn] with no attribute trailing:
      g.pl("        xmlAttributes["+index+"] = XmlAttributeInfo.eltXmlAttributeInfo(\""
           +a.name()+"\");");

    if (a.hasChild())
      g.pl("        xmlAttributes["+index+"].setChild("+a.child()+");");

    if (a.hasPath())
      {
        g.pl("        {");
        g.pl("           int[] path = "
             +Misc.arrayToString(a.xmlPath(),"{",",","}")+";");
        g.pl("           xmlAttributes["+index+"].setXmlPath(path);");
        g.pl("        }");
      }
  }

  private final String makeTextInfoArg (XmlTextInfoArg arg, int i)
    {
      if (arg.isLiteral())
        return "        (formArgs["+i+"] = new XmlTextInfoArg()).setText(\""+arg.text()+"\");";

      return "        formArgs["+i+"] = new XmlTextInfoArg("
        +arg.child()+","
        +(arg.path() != null ? (arg.path()+",") : "")
        +"\""+arg.attribute()+"\");";
    }

  /**
   * <a name="createXmlForm"></a>
   * This method synthesizes and returns a JDOM <tt>Element</tt>
   * computed using the information in this <tt>XmlInfo</tt> in the
   * context of the specified XML document root <tt>root</tt>.
   *
   * <p>
   *
   * In the simpler case (<i>i.e.</i>, that of a <a
   * href="xm/XmlAnnotationDoc/XmlAnnotationSpecification.html#homo">homomorphic
   * tree transduction</a>), this builds the XML element tree inductively
   * from leaves to root with mutually recursive calls to <a
   * href="ParseNode.html#xmlify"><tt>xmlify (Element root)</tt></a>.
   *
   * <p>
   *
   * In the more complex case (<i>i.e.</i>, that of a <a
   * href="xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#hetero">heteromorphic
   * tree transduction</a>), this also builds the XML element tree
   * inductively from leaves to root; but now, the attribute values and
   * XML components of the element being constructed may be those of
   * already constructed elements beneath this one. In that case, the
   * "lower" element must first be detached from its parent element. This
   * is because the XML DOM tree model does not support sharing;
   * <i>i.e.</i>, an element may not belong to more than one container -
   * in other words, this is indeed a <i><u>tree</u></i> rather than a
   * <i>DAG</i> (directed acyclic graph).  Of course, this will modify the
   * parent of the referenced element, but this is fine because that
   * container is now harmlessless useless anyway (since it will now never
   * be attached to any element or document higher up).
   *
   * <p>
   *
   * However, we can't just carelessly slice up the referenced element's
   * container by removing the referenced element from its parent's
   * children's list.  Indeed, since other references to other children of
   * this container may yet take place, it is important to keep the number
   * and order of all the elements' components consistent.  To achieve
   * this, we simply substitute a dummy element in the place previously
   * occupied by the element being detached (and returned to be
   * re-attached where appropriate).
   *
   * <p><hr><p>
   *
   * <tt style="color:tan; font-size:smaller"><i>[See also the method <a
   * href="ParseNode.html#xmlify">xmlify(Element container)</a> in the
   * class <a href="ParseNode.html">ParseNode</a>.]</i></tt>
   *
   * <p><hr><p>
   *
   * <tt style="color:tan; font-size:smaller"><i><b>N.B.</b>: The code
   * below is guaranteed to work safely only if the annotation is <a
   * href="http://hassan-ait-kaci.net/hlt/doc/hlt/code/language/syntax/xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#consistency">
   * strictly consistent</a>.</i></tt>
   *
   */
  public final Element createXmlForm (ParseNode node, Element root)
    {
      // // BEGIN DEBUGGING
      // System.err.println("==> Creating XML element from XML annotation for \""+node+"\":\n\t"
      //                         +this);
      // // END DEBUGGING

      // create a new element with the local name:
      Element element = new Element(_localName);

      // if needed, set the new element's namespace in root's context:
      if (_nsPrefix != "")
        element.setNamespace(root.getNamespace(_nsPrefix));

      // we first materialize all the needed XML forms from all the
      // relevant descendant CSTs, if any are mentioned in either the
      // _children or _attributes arrays, and store them in the
      // ArrayList xmlForms. We also build an IntToIntMap that
      // associates to the RHS position of each "xmlified" CST child its
      // corresponding XML form index in xmlForms. These two structures
      // are then used to process first the attributes, then the
      // children.

      ArrayList xmlForms = new ArrayList(); // a list of lists
      IntToIntMap childMap = new IntToIntMap();
      int xmlFormIndex = 0;

      if (hasChildren() && !isTerminal())
        for (int i=0; i<_children.length; i++)
          {
            if (childMap.containsKey(_children[i]))
              // deja vu!
              continue;

            // retrieve the child's CST:
            ParseNode childCst = node.getChild(_children[i]-1);

            // create its XML form:
            ArrayList childXmlForm = childCst.xmlify(root);

            // process wrappers if any:
            if (hasWrapperPaths())
              { // this child has wrappers: on we go...  we iterate
                // backward down the wrapper path and whenever a wrapper
                // specifier is starred we need to "distribute" it on
                // all the elements being wrapped:

                XmlWrapper[] wrapperPath = _wrapperPaths[i];

                for (int j=wrapperPath.length;j-->0;)
                  if (wrapperPath[j].isStarred())
                    // we wrap and replace each element in childXmlForm
                    // for each Element elt in childXmlForm:
                    for (int x=0; x<childXmlForm.size(); x++)
                      // replace the element at this index
                      childXmlForm.set(x,
                                       // with an element with the new tag wrapping its old self
                                       (new Element(wrapperPath[j].getTag()))
                                       .addContent((Element)childXmlForm.get(x)));
                  else
                    { // wrapperPath[j] is not starred:
                      // we wrap childXmlForm in a single Element:
                      ArrayList wrappedXmlForm = new ArrayList(1);
                      wrappedXmlForm.add(new Element(wrapperPath[j].getTag())
                                         .addContent(childXmlForm));
                      childXmlForm = wrappedXmlForm;
                    }
              }

            // record the new XML form in xmlForms:
            xmlForms.add(childXmlForm);    
            // record its index in xmlForms for the child's CST
            childMap.put(_children[i],xmlFormIndex++);

            // // BEGIN DEBUGGING
            // try
            //   {
            //  System.err.println("==> generating "+node
            //                     +"'s child CST tree "+childCst
            //                     +"'s XML form (elt "
            //                     +_children[i]+"):\n\t\t");
            //  showXmlElements(childXmlForm,System.err);
            //  System.err.println();
            //   }
            // catch (IOException e)
            //   {
            //  e.printStackTrace();
            //   }
            // // END DEBUGGING
          }
      
      if (hasAttributes())
        for (int i=0; i<_attributes.length; i++)
          {
            if (_attributes[i].hasTextForm())
              {
                XmlTextInfoArg[] textArgs = _attributes[i].specialForm().textArgs();

                for (int j=0; j<textArgs.length; j++)
                  {
                    if (textArgs[j].isLiteral())
                      // nothing to do...
                      continue;

                    if (childMap.containsKey(textArgs[j].child()))
                      // deja vu!
                      continue;

                    ParseNode childCst = node.getChild(textArgs[j].child()-1);
                    xmlForms.add(childCst.xmlify(root));
                    childMap.put(textArgs[j].child(),xmlFormIndex++);
                  }

                continue;
              }

            if (_attributes[i].isDeepAttributeReference())
              {
                if (childMap.containsKey(_attributes[i].child()))
                  // deja vu!
                  continue;

                ParseNode childCst = node.getChild(_attributes[i].child()-1);
                xmlForms.add(childCst.xmlify(root));
                childMap.put(_attributes[i].child(),xmlFormIndex++);
              }
          }

      // process the attributes (must come before processing the children)
      if (hasAttributes())
        processAttributes(node,element,xmlForms,childMap);

      // processing of a terminal or children (must come after processing the attributes)

      if (isTerminal() && hasTerminalValue())
          processTerminal(node,element);
      else
        if (hasChildren())
          processChildren(node,element,xmlForms,childMap);
      
      return element;
    }

  /**
   * <a name="processAttributes"><tt>processAttributes</tt></a>
   * processes the <tt>attributes</tt> annotation directive for the
   * specified <tt>Element</tt>, in the context of the specified
   * <tt>ParseNode</tt>. When one of this <tt>XmlInfo</tt>'s children is
   * a local reference (homomorphic case), then this simply sets the
   * attribute from the name and info of the <tt>Xmlattributeinfo</tt>.
   * Otherwise, this retrieves the deep-referenced <tt>Element</tt>, and
   * then sets the specified element's attribute accordingly.
   */
  final void processAttributes (ParseNode node, Element element,
                                ArrayList xmlForms, IntToIntMap childMap)
    {
      for (int i=0; i<_attributes.length; i++)
        {
          XmlAttributeInfo a = _attributes[i];
          String value = "";

          if (a.isSpecialForm())
            { // Special forms cases:
              if (a.hasTerminalValue())
                // $VALUE case:
                value = Misc.unquotify(node.stringValue());
              else
                // $TEXT case:
                {
                  XmlTextInfoArg[] args = a.specialForm().textArgs();
                  for (int j=0; j<args.length; j++)
                    value += args[j].makeTextForm(xmlForms,childMap);
                }

              element.setAttribute(a.name(),value);
              continue;
            }

          if (a.hasLiteralValue())
            { // Literal attribute value case:
              element.setAttribute(a.name(),a.value().text());
              continue;
            }

          // c[x1...xn]/a case:

          // Deep ref - extract the (necessarily?) unique XML form:
          int childPos = a.child();
          int formPos = childMap.get(childPos);
          ArrayList xmlForm = (ArrayList)xmlForms.get(formPos);
          Element ref = (Element)xmlForm.get(0);

          if (a.hasPath())
            {
              int[] path = a.xmlPath(); // XML tree address
              for (int j=0; i<path.length; j++)
                ref = (Element)ref.getContent(path[j]-1);
            }

          if (a.hasAttribute())
            {
              // // BEGIN DEBUGGING
              // System.err.println("==> Setting "+element.getName()
              //                         +"'s \""+a.name()
              //                         +"\" to the value of "+ref.getName()
              //                         +"'s \""+a.value().attribute()+"\" ("
              //                         +ref.getAttributeValue(a.value().attribute())
              //                         +")");
              // // END DEBUGGING
              element.setAttribute(a.name(),
                                   ref.getAttributeValue(a.attributeNameRef()));
            }
          else
            {
              // // BEGIN DEBUGGING
              // System.err.println("==> Setting "+element.getName()
              //                         +"'s \""+a.name()
              //                         +"\" to the text contents of "+ref.getName()
              //                         +"'s \""+ref+"\" (the string \""
              //                         +ref.getText()
              //                         +"\")");
              // // END DEBUGGING
              element.setAttribute(a.name(),
                                   ref.getText());
            }
        }
    }

  /**
   * <a name="processTerminal"><tt>processTerminal</tt></a>
   * processes a terminal node's annotation for the specified
   * <tt>Element</tt> in the context of the specified
   * <tt>ParseNode</tt>.
   */
  final void processTerminal (ParseNode node, Element element)
    {
      if (hasWrapperPaths())
        {
          XmlWrapper[] wrapperPath = _wrapperPaths[0];
          Element elt = new Element(wrapperPath[wrapperPath.length-1].getTag())
                            .addContent(node.stringValue());
          for (int i = wrapperPath.length; i-->0;)
            if (i == 0)
              element.addContent(elt);
            else
              elt = new Element(wrapperPath[i-1].getTag()).addContent(elt);
        }
      else
        element.addContent(node.stringValue());
    }
  
  /**
   * <a name="processChildren"><tt>processChildren</tt></a> processes
   * each <tt>children</tt> annotation for the specified
   * <tt>Element</tt> in the context of the specified
   * <tt>ParseNode</tt>, using the computed XML forms in
   * <tt>xmlForms</tt> and child index map <tt>childMap</tt>.
   *
   * <p>
   *
   * When one of this <tt>XmlInfo</tt>'s children is a local reference
   * (homomorphic case), then this simply attaches the referenced child
   * CST's XML form to the specified <tt>Element</tt>. Otherwise
   * (heteromorphic case), this is a deep reference into the XML tree of
   * the child CST; so we must first detach all targeted elements from
   * their original containers before re-attaching them to
   * <tt>element</tt>.
   */
  final void processChildren (ParseNode node, Element element,
                              ArrayList xmlForms, IntToIntMap childMap)
    {
      // for each CST child specified in this XmlInfo:
      for (int i=0; i<_children.length; i++)
        {
          // retrieve the relevant XML form (an ArrayList of Element's)
          // from xmlForms using childMap:
          ArrayList xmlForm = (ArrayList)xmlForms.get(childMap.get(_children[i]));

          // for each Element ref in xmlForm:
          for (Iterator it=xmlForm.iterator(); it.hasNext();)
            {
              Element ref = (Element)it.next();
              boolean noStringsAttached = true;
              boolean collectChildrenPath = false;

              // // BEGIN DEBUGGING
              // try
              //        {
              //          System.err.println("==> Processing child of "+node+" CST tree ref ("
              //                             +_children[i]+"):\n\t");
              //          showXmlElement(ref,System.err);
              //          System.err.println();
              //        }
              // catch (IOException e)
              //        {
              //          e.printStackTrace();
              //        }

              // System.err.println("==> XML info:\n"+this);

              // System.err.println("==> _xmlPath = "
              //                         +(hasXmlPaths()?Misc.arrayToString(_xmlPaths[i]):"NONE"));
              // // END DEBUGGING
              
              if (hasXmlPaths() && _xmlPaths[i] != null)
                { // Heteromorphic Case :- this is a deep reference: we
                  // must go down the XML tree according to the
                  // specified path (an XML tree address), to the
                  // deepest level. Once there, the deepest target
                  // element is "surgically" excised and replaced with a
                  // dummy element to "fill the hole"; at each level as
                  // we go down the XML tree, we save the level's list
                  // of children as we process the next level in order
                  // to allow us to perform the eventual in-place
                  // "surgery" on the containing penultimate XML node's
                  // children list:

                  int[] path = _xmlPaths[i]; // XML tree address

                  // // BEGIN DEBUGGING
                  // System.err.println("==> Deep reference: path = "
                  //                 +Misc.arrayToString(path));
                  // // END DEBUGGING

                  for (int j=0; j<path.length; j++)
                    {
                      // save this level's children list:
                      List xmlChildren = ref.getChildren();

                      // NB: WHENEVER THE XML CHILDREN OF REF IS EMPTY
                      // ITS CONTENTS MUST BE ONLY TEXT ...
                      if (xmlChildren.isEmpty())
                        { // attach the content of the text string to element:
                          element.setText(Misc.unquotify(ref.getText()));
                          noStringsAttached = false;
                        }
                      else
                        {
                          if (path[j] != 0) // this is a regular "single" tree address index
                            {
                              // the index in xmlChildren of the XML child (next level down):
                              int childIndex = path[j]-1; // -1 because tree addresses are 1-based
                                                          // but path indices are 0-based

                              if (j == path.length-1) // have we reached the end of the path yet?
                                {
				  // yes, we're here - we should detach and
                                  // save the targeted element. But it is
                                  // unsafe to do just that; namely,
                                  // ref = (Element)((Element)xmlChildren.get(childIndex)).detach();
                                  // because doing so will invalidate other
                                  // references to elements of xmlChildren
                                  // using tree addresses. To preserve other
                                  // tree addresses in the same XML tree, we
                                  // must excise and save the targeted element,
                                  // and replace it with an appropriately named
                                  // dummy as a place saver:
                                  Element dummy = new Element("_DUMMY_");
                                  ref = (Element)xmlChildren.set(childIndex,dummy);
                                  dummy.setName(dummy.getName()+ref.getName()+"_");
                                  // // BEGIN DEBUGGING
                                  // System.err.println("==> "+dummy);
                                  // // END DEBUGGING
                                }
                              else
                                // no, not yet: move down one level
                                ref = (Element)xmlChildren.get(childIndex);

                              // // BEGIN DEBUGGING
                              // try
                              //   {
                              //     System.err.println("==> "+node+" XML tree ref ("
                              //                         +path[j]+"):\n");
                              //     showXmlElement(ref,System.err);
                              //     System.err.println();
                              //   }
                              // catch (IOException e)
                              //   {
                              //     e.printStackTrace();
                              //   }
                              // // END DEBUGGING
                            }
                          else // this is a "collect" index
                            // this means that we must collect all the
                            // children of this XML node:
                            { 
                              // if (j != path.length-1) // this should be the end of the path but isn't!
                              //        throw new BadXmlCollectPathException();

                              // BEGIN DEBUGGING
                              // try
                              //        {
                              //          System.err.println("==> element:");
                              //          showXmlElement(element,System.err);
                              //          System.err.println("\n");
                              //          System.err.println("==> xmlChildren");
                              //          for (Iterator ch=xmlChildren.iterator(); ch.hasNext();)
                              //            {
                              //              showXmlElement((Element)ch.next(),System.err);
                              //              System.err.println();
                              //            }
                              //          System.err.println();
                              //        }
                              // catch (IOException e)
                              //   {
                              //     e.printStackTrace();
                              //   }
                              // END DEBUGGING

                              // set the flag to add all xmlChildren to the children of element:
                              collectChildrenPath = true;
                              // add all the children of the end-of-path node to the contents of element;
                              // here, it is safe just to detach all the nodes in xmlChildren, and add
                              // them as the children of the current element; this is because whenever
			      // an XML tree path ends with 0, it must be the only one with its prefix.
                              while (xmlChildren.size() > 0)
                                {
                                  ref = (Element)((Element)xmlChildren.get(0)).detach();
                                  element.addContent(ref);
                                }
                            }
                        }
                    }
                }

              // if that wasn't a "collect" path (i.e., one ending with 0), check
              // whether there is an attribute; if so extract it as text; if not
              // just add the ref node to the content of element:
              if (!collectChildrenPath)
                {
                  if (hasAttributeRefs() && _attributeRefs[i] != null)
                    { // attach the string value of this attribute in ref as text to element:
                      element.setText(ref.getAttributeValue(_attributeRefs[i]));
                      noStringsAttached = false;
                    }

                  // if no string's attached (!) add ref to the contents of element:
                  if (noStringsAttached)
                    element.addContent(ref);
                }
            }
        }
    }

  //////////////////////////////////////////////////////////////////////

  /**
   * For debugging purposes ...
   */
  final void showXmlElement (Element element, PrintStream out)
    throws IOException
    {
      if (element != null)
        GenericParser.xmlWriter().output(element,out);
      else
        out.println("null");
    }
 
  /**
   * For debugging purposes ...
   */
  final void showXmlElements (ArrayList elements, PrintStream out)
    throws IOException
    {
      for (Iterator i=elements.iterator(); i.hasNext();)
        showXmlElement((Element)i.next(),out);
    }
 
  //////////////////////////////////////////////////////////////////////

  // The following methods are used for generating HTML documentation...

  /**
   * <tt>margin(int offset)</tt> returns a string of white spaces of
   * length <tt>offset</tt>.
   */
  final String margin (int offset)
    {
      return Misc.repeat(offset,' ');
    }

  static final String PREFIX_COLOR    = "#226688"; // dark slate blue
  static final String NAME_COLOR      = "BLUE";
  static final String ATTRIBUTE_COLOR = "#EE6622"; // brownish orange
  static final String CHILDREN_COLOR  = "BROWN";

  /**
   * <tt>color (Object o, String color)</tt> returns the string form of
   * <tt>o</tt> wrapped in HTML code to render it in color
   * <tt>color</tt>.
   */
  static final String color (Object o, String color)
    {
      return "<SPAN STYLE=\"COLOR:"+color+"\">"+o+"</SPAN>";
    }

  /**
   * <tt>colorPrefix()</tt> returns a string formatting the namespace
   * prefix wrapped in HTML code to color it as a namespace prefix.
   */
  final String colorPrefix ()
    {
      return colorPrefix(true);
    }

  /**
   * <tt>colorPrefix(boolean html)</tt> returns a string formatting the
   * namespace prefix: when <tt>html</tt> is <tt>true</tt> it is wrapped
   * in HTML code to color it as a namespace prefix.
   */
  final String colorPrefix (boolean html)
    {
      if (html)
        return color(_nsPrefix,PREFIX_COLOR);
      
      return _nsPrefix;
    }

  /**
   * <tt>colorName(boolean html)</tt> returns a string formatting the
   * local name wrapped in HTML code to color it as a local name.
   */
  final String colorName ()
    {
      return colorName(true);
    }

  /**
   * <tt>colorName(boolean html)</tt> returns a string formatting the
   * local name: when <tt>html</tt> is <tt>true</tt> it is wrapped
   * in HTML code to color it as a local name.
   */
  final String colorName (boolean html)
    {
      if (html)
        return color(_localName,NAME_COLOR);
      
      return _localName;
    }

  /**
   * <tt>colorName (Object o)</tt> returns a string formatting the string
   * form of <tt>Object o</tt> wrapped in HTML code to color it as an
   * name.
   */
  static final String colorName (Object o)
    {
      return colorName(o,true);
    }

  /**
   * <tt>colorName(Object o, boolean html)</tt> returns a string
   * formatting the string form of the object <tt>o</tt>: when
   * <tt>html</tt> is <tt>true</tt> it is wrapped in HTML code to color
   * it as a local name.
   */
  static final String colorName (Object o, boolean html)
    {
      if (html)
        return color(o,NAME_COLOR);
      
      return o.toString();
    }

  /**
   * <tt>colorAttribute(Object o)</tt> returns a string formatting the
   * string form of <tt>Object o</tt> wrapped in HTML code to color it
   * as an attribute.
   */
  final String colorAttribute (Object o)
    {
      return colorAttribute(o,true);
    }

  /**
   * <tt>colorAttribute (Object o, boolean html)</tt> returns a string
   * formatting the string form of <tt>Object o</tt>: when <tt>html</tt>
   * is <tt>true</tt> it is wrapped in HTML code to color it as an
   * attribute.
   */
  static final String colorAttribute (Object o, boolean html)
    {
      if (html)
        return color(o,ATTRIBUTE_COLOR);
      
      return o.toString();
    }

  /**
   * <tt>colorChild(Object o)</tt> returns the string form of <tt>o</tt>
   * wrapped in HTML code to color it as child.
   */
  final String colorChild (Object o)
    {
      return colorChild(o,true);
    }

  /**
   * <tt>colorChild(Object o, boolean html)</tt> returns the string form
   * of <tt>o</tt>: when <tt>html</tt> is <tt>true</tt> it is wrapped in
   * HTML code to color it as child.
   */
  final String colorChild (Object o, boolean html)
    {
      if (html)
        return color(o,CHILDREN_COLOR);
      
      return o.toString();
    }

  /**
   * <tt>_legend</tt> contains the strings that are the lines of HTML
   * text describing the details of the serialization pattern encoded by
   * this <tt>XmlInfo</tt>.
   */
  private ArrayList _legend;

  final ArrayList legend ()
    {
      return _legend;
    }

  final boolean hasLegend ()
    {
      return _legend != null && _legend.size() > 0;
    }

  final void resetLegend ()
    {
      _legend = new ArrayList();
    }

  /**
   * <tt>ordinal(int i)</tt> returns the HTML string formatting the
   * ordinal name of rank <tt>i</tt> (<i>viz.</i>,
   * <tt>"1<sup>st</sup>"<tt>, <tt>"2<sup>nd</sup>"<tt>,
   * <tt>"3<sup>rd</sup>"<tt>, <tt>"4<sup>th</sup>"<tt>,
   * <tt>"5<sup>th</sup>"<tt>, <i>etc.</i>, ...
   */
  final String ordinal (int i)
    {
      return i+"<SUP>"+Misc.ordinal(i)+"</SUP>";
    }

  /**
   * <tt>formatAttributeRef (XmlAttributeInfo a, int index, String
   * type)</tt> returns the HTML-formatted string for the
   * <tt>index<sup><i>th</i></sup></tt> <tt>XmlAttributeInfo a</tt> when
   * it corresponds to a deep reference to an attribute in the XML tree
   * of the RHS symbol <tt>type<tt>.
   */
  final String formatAttributeRef (XmlAttributeInfo a, int index, String type)
    {
      String attribute = colorAttribute(a.name()+" = <TT><I>A<SUB>"+index+"</SUB></I></TT>");

      // Build the legend:

      String kind = a.hasAttribute()
        ? "value of attribute <TT>\""+ colorAttribute(a.attributeNameRef())+"\"</TT>"
        : "XML node text content";

      StringBuilder buf = new StringBuilder(colorAttribute("<TT><I>A<SUB>"+index
                                                           +"</SUB></I></TT>")
                                            +" is the "+kind);

      if (a.hasPath())
        {
          int[] path = a.xmlPath();
          for (int j=0; j<path.length; j++)
            buf.append(" of the "+ordinal(path[j])+" XML component");
        }

      buf.append(" of the ")
         .append(ordinal(a.child()))
         .append(" RHS symbol")
         .append(" (")
         .append(type)
         .append(")");

      _legend.add(buf.toString());

      return attribute;
    }

  /**
   * <tt>formatAttributeTextForm (XmlAttributeInfo a, int index, String
   * type)</tt> returns the HTML-formatted string for the
   * <tt>index<sup><i>th</i></sup></tt> <tt>XmlAttributeInfo a</tt> when
   * it corresponds to a <tt>$TEXT</tt> special form.
   */
  final String formatAttributeTextForm (XmlAttributeInfo a, int index, Rule r)
    {

      String attribute = colorAttribute(a.name()+ " = <TT><I>A<SUB>"+index+"</SUB></I></TT>");

      // Build the legend:

      StringBuilder buf = new StringBuilder(colorAttribute("<TT><I>A<SUB>"+index+"</SUB></I></TT>"));

      buf.append(" is the concatenation of the following pieces of text:");

      // Go down the arguments of the $TEXT form and for each
      // arg, generate a line of legend describing its meaning

      buf.append("\n<UL>\n");

      XmlTextInfoArg[] args = a.specialForm().textArgs();

      for (int i=0; i<args.length; i++)
        if (args[i].isLiteral())
          buf.append("<LI> the literal string: <TT>")
             .append(colorAttribute(args[i]))
             .append("</TT>");
        else
          { // args[i] is of the form: c[x1...xn]/a
            buf.append("<LI> the value of the ");

            String type = r.sequence[args[i].child()].htmlRef();

            String kind = args[i].hasAttribute()
              ? "attribute <TT>"+colorAttribute(args[i].attribute())+"</TT>"
              : "XML node text content";

            buf.append(kind);

            if (args[i].hasPath())
              {
                int[] path = args[i].path();
                for (int j=0; j<path.length; j++)
                  buf.append(" of the ")
                     .append(colorAttribute(ordinal(path[j])))
                     .append(" XML component");
              }

            buf.append(" of the ")
               .append(colorAttribute(ordinal(args[i].child())))
               .append(" RHS symbol")
               .append(" (")
               .append(type)
               .append(")");
          }
        
      buf.append("\n</UL>\n");

      _legend.add(buf.toString());

      return attribute;
    }

  /**
   * <tt>childRef(int child, int[] path)</tt> returns the documentation
   * string describing a deep reference (given by <tt>path</tt>) into
   * the child in position <tt>child</tt> in the RHS.
   */
  final String childRef (int child, int[] path)
    {
      StringBuilder buf = new StringBuilder();

      if (path != null)
        for (int j=0; j<path.length; j++)
          buf.append(" of the ")
             .append(ordinal(path[j]))
             .append(" XML component");

      buf.append(" of the ")
         .append(ordinal(child))
         .append(" RHS symbol");

      return buf.toString();
    }

  /**
   * <tt>toString(int offset, String sep, boolean htmlPrefix, aboolean
   * htmlName, boolean htmlAttribute)</tt> returns a formatted string for
   * this <tt>XmlInfo</tt>. It is a generic method that can be adapted
   * depending on its arguments as a string with plain contents, or
   * with HTML code, or pretty-printed. The arguments are:
   *
   * <p>
   * <ul>
   *
   * <li><tt>int offset</tt>:
   * a margin offset for pretty-printing;
   *
   * <p><li><tt>String sep</tt>:
   * a string separating documentation items (<i>i.e.</i>, white space,
   * or newline, <i>etc.</i>);
   *
   * <p><li><tt>boolean htmlPrefix</tt>:  
   * when <tt>true</tt>, the namespace prefix is HTML-ified;
   *
   * <p><li><tt>boolean htmlName</tt>:
   * when <tt>true</tt>, the local name is HTML-ified;
   *
   * <p><li><tt>boolean htmlAttribute</tt>:
   * when <tt>true</tt>, the attributes are HTML-ified.
   *
   * </ul>
   */
  public final String toString (int offset, String sep, boolean html)
    {
      if (!html) sep = "";

      String tabOrSpace = html ? "\t" : " ";
      String margin = margin(offset);
      StringBuilder s = new StringBuilder(margin).append("[").append(sep);

      margin = html ? margin(offset+2) : " ";

      if (_nsPrefix != "")
        s.append(margin)
         .append("nsprefix")
         .append(tabOrSpace)
         .append("= \"")
         .append(colorPrefix(html))
         .append("\"")
         .append(sep);

      if (_localName != null)
        s.append(margin)
         .append("localname")
         .append(tabOrSpace)
         .append("= \"")
         .append(colorName(html))
         .append("\"")
         .append(sep);

      if (hasAttributes())
        {
          s.append(margin)
           .append("attributes")
           .append(tabOrSpace)
           .append("= { ");

          for (int i = 0; i<_attributes.length;i++)
            {
              if (html)
                s.append("\n                    ");

              s.append(colorAttribute(_attributes[i].toString(),html));

              if (!html)
                s.append(" ");
            }

          if (html)
            s.append("\n                  ");
          
          s.append("}")
           .append(sep);
        }

      if (isTerminal() && hasTerminalValue())
        {
          s.append(margin)
           .append("child")
           .append(tabOrSpace)
           .append("= ( ");

          if (hasWrapperPaths())
            s.append(Misc.arrayToString(_wrapperPaths[0],"","",""));

          s.append(colorAttribute("$VALUE",html));

          s.append(" )")
            .append(sep);
        }
      else
        if (hasChildren())
          {
            s.append(margin)
             .append("children")
             .append(tabOrSpace)
             .append("= ( ");

            for (int i = 0; i<_children.length; i++)
              {
                if (html)
                  s.append("\n                    ");

                if (hasWrapperPaths())
                  s.append(colorChild(Misc.arrayToString(_wrapperPaths[i],"","",""),html));

                s.append(colorChild(String.valueOf(_children[i]),html));

                if (hasXmlPaths() && _xmlPaths[i] != null)
                  s.append(colorChild(Misc.arrayToString(_xmlPaths[i],"[",".","]"),html));

                if (hasAttributeRefs() && _attributeRefs[i] != null)
                  s.append("/").append(colorChild(_attributeRefs[i],html));

                if (!html)
                  s.append(" ");
              }

            if (html)
              s.append("\n                  ");
            s.append(")")
             .append(sep);
          }

      margin = html ? margin(offset) : " ";

      return s.append(margin)
              .append("]")
              .toString();
    }

  public final String toHtmlString (int offset, String sep)
    {
      return toString(offset,sep,true);
    }

  public final String toPrettyHtmlString (int offset)
    {
      return toString(offset,"\n",true);
    }

  public final String toString ()
    {
      return toString(0," ",false);
    }

}
