//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.tools.Misc;
import hlt.language.util.Location;
import hlt.language.util.Locatable;
import hlt.language.util.Span;
import hlt.language.util.ArrayList;

import java.util.Date;
import java.util.Iterator;
import java.io.PrintStream;
import java.io.IOException;

import org.jdom2.*;

/**
 * This class is the type of objects pushed on the semantic evaluation
 * stack along with parser's states.  It comes with two predefined
 * attributes that may be used in a rule's semantic action: a numeric
 * one (<tt>nvalue</tt>) of type <tt>double</tt>, and a symbolic one
 * (<tt>svalue</tt>) of type <tt>String</tt>. If attributes of other
 * types are needed, one may use the <tt>%nodeclass</tt> command for the
 * appropriate symbol, which has for effect to make the type of the
 * value it returns a subclass of this one containing the new fields.
 * <p>
 * It is also the root of the parse tree which is built automatically
 * when parsing is done with <tt>GenericParser.parse(boolean)</tt>.
 *
 * @see         GenericParser
 * @version     Last modified on Wed Jul 25 07:08:18 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParseNode implements Locatable
{
  /* **************************************************************** */
  // Constructors
  /* **************************************************************** */

  /**
   * Constructs an unititialized <tt>ParseNode</tt> object.
   */
  public ParseNode ()
    {
    }

  /**
   * Constructs an anonymous <tt>ParseNode</tt> object (<i>i.e.</i>,
   * with a <tt>null</tt> symbol), and sets its <tt>svalue</tt> field to
   * the specified string. This is used in <tt>DynamicParser</tt> when
   * a symbol that has been read is not a known terminal symbol, but may
   * be a potential dynamic operator.
   */
  public ParseNode (String value)
    {
      _svalue = value;
    }

  /**
   * Constructs a <tt>ParseNode</tt> object with its <tt>symbol</tt>
   * field initialized to the specified parser symbol. This is used
   * by a <tt>DynamicParser</tt> reading a new token and wrapping it
   * into a <tt>DynamicToken</tt> where it can be restored in its
   * original for upon backtracking.
   *
   * @see DynamicParser
   * @see DynamicToken
   */
  public ParseNode (ParserSymbol symbol)
    {
      _symbol = symbol;
    }

  /**
   * Constructs a <tt>ParseNode</tt> object with its <tt>symbol</tt>
   * field initialized to the specified parser symbol, and <tt>nvalue</tt>
   * field initialized to the specified double value.
   */
  public ParseNode (ParserSymbol symbol, double value)
    {
      _symbol = symbol;
      _nvalue = value;
    }

  /**
   * Constructs a <tt>ParseNode</tt> object with its <tt>symbol</tt>
   * field initialized to the specified parser symbol, and <tt>nvalue</tt>
   * field initialized to the specified integer value.
   */
  public ParseNode (ParserSymbol symbol, int value)
    {
      _symbol = symbol;
      _nvalue = value;
      _isInteger = true;
    }

  /**
   * Constructs a <tt>ParseNode</tt> object with its <tt>symbol</tt>
   * field initialized to the specified parser symbol and <tt>svalue</tt>
   * field initialized to the specified string.
   */
  public ParseNode (ParserSymbol symbol, String value)
    {
      _symbol = symbol;
      _svalue = value;
    }
  
  /**
   * Constructs a <tt>ParseNode</tt> object that is a (shallow) copy of
   * of the specified <tt>ParseNode</tt>.
   */
  public ParseNode (ParseNode node)
    {
      _symbol       = node.symbol();
      _svalue       = node.svalue();
      _nvalue       = node.nvalue();
      _isInteger    = node.isInteger();
      _alternatives = node.alternatives();
      _children     = node.children();
      _span         = node.getSpan();
    }

  /* **************************************************************** */
  // Fields and methods
  /* **************************************************************** */

  private ParserSymbol _symbol;

  /**
   * The grammar symbol this node stands for.
   */
  public ParserSymbol symbol ()
    {
      return _symbol;
    }

  /**
   * This is overridden to <code>true</code> in <tt>DynamicToken</tt>.
   * @see DynamicToken
   */
  boolean isDynamic ()
    {
      return false;
    }

  /**
   * Sets the grammar symbol this node stands for.
   */
  public void setSymbol (ParserSymbol symbol)
    {
      _symbol = symbol;
    }

  /* **************************************************************** */
  // Token Node Value Information:
  /* **************************************************************** */

  private double _nvalue = Double.NaN;

  /**
   * The number value of this node.
   */
  public double nvalue ()
    {
      return _nvalue;
    }

  /**
   * Sets the number value of this node.
   */
  public void setNvalue (double nvalue)
    {
      _nvalue = nvalue;
    }

  private boolean _isInteger = false;

  /**
   * Returns <tt>true</tt> iff this <tt>ParseNode</tt> is an integer number.
   */
  public boolean isInteger ()
    {
      return _isInteger;
    }

  /**
   * Sets the integer flag to the given boolean argument (<i>i.e.</i>,
   * whenever this argument is <tt>true</tt>, this node's numerical
   * value will be cast as an <tt>int</tt>).
   */
  public void makeInteger (boolean isInteger)
    {
      _isInteger = isInteger;
    }

  private String _svalue;

  /**
   * The String value of this node.
   */
  public String svalue ()
    {
      return _svalue;
    }

  /**
   * Sets the String value of this node.
   */
  public void setSvalue (String svalue)
    {
      _svalue = svalue;
    }

  /* **************************************************************** */
  // Non-Deterministic Token Node Information:
  /* **************************************************************** */

  private ArrayList _alternatives;

  /**
   * If non-null, this contains alternative forms of an ambiguous
   * token.
   */
  public ArrayList alternatives ()
    {
      return _alternatives;
    }

  /**
   * Add an alternative form for this ParseNode.
   */
  public void addAlternative (ParseNode alternative)
    {
      if (_alternatives == null)
        _alternatives = new ArrayList(2);

      _alternatives.add(alternative);
    }

  /**
   * Returns <tt>true</tt> iff this ParseNode has one or more alternative
   * forms.
   */
  public boolean hasAlternatives ()
    {
      return _alternatives != null;
    }

  public boolean isUnknown ()
    {
      return _symbol == null;
    }

  public boolean isError ()
    {
      return !isUnknown() && _symbol.name() == "error";
    }

  public boolean isEOI ()
    {
      return !isUnknown() && _symbol.name() == "$E_O_I$";
    }

  /* **************************************************************** */
  // Location Information:
  /* **************************************************************** */

  private Span _span;
  
  /**
   * Returns information to locate this node's extent in the input stream.
   */
  public final Span span ()
    {
      return _span == null ? _span = new Span() : _span;
    }

  /**
   * Returns information to locate this node's extent in the input stream
   * or <tt>null</tt>.
   */
  public final Span getSpan ()
    {
      return _span;
    }

  final ParseNode setSpan (ParserStackElement[] handle)
    {
      ParseNode start = null;
      ParseNode end = null;

      for (int i=0; i<handle.length; i++)
        {
          start = handle[i].getNode();
          if (start.isLocated()) break;
        }

      for (int i=handle.length; i-->0;)
        {
          end = handle[i].getNode();
          if (end.isLocated()) break;
        }

      if (start != null)
        if (end != null)
          setSpan(start,end);
        else
          setSpan(start);
      else
        if (end !=null)
          setSpan(end);

      locate();

      return this;
    }
    
  /**
   * This method (by default empty) is invoked anytime this node's span
   * is set.  It is meant to be overridden for locating specific
   * <tt>Locatable</tt> members using the span of this parsenode as the
   * extent (either implicitly using the <tt>locates</tt> option of a
   * <tt>%nodeclass</tt> declaration, or explicitly in the body of such
   * a declaration).
   */
  public void locate ()
    {
    }

  final void resetSpan ()
    {
      _span = null;
    }

  final Locatable setSpan (ParseNode start, ParseNode end)
    {
      return setStart(start.getStart()).setEnd(end.getEnd());
    }

  final Locatable setSpan (Location start, Location end)
    {
      return setStart(start).setEnd(end);
    }

  public final Locatable setSpan (ParseNode node)
    {
      return setStart(node.getStart()).setEnd(node.getEnd());
    }

  public final boolean isLocated ()
    {
      return span().isKnown();
    }

  public final Location getStart ()
    {
      return span().start();
    }

  public final Locatable setStart (Location location)
    {
      span().setStart(location);
      return this;
    }

  public final Location getEnd ()
    {
      return span().end();
    }

  public final Locatable setEnd (Location location)
    {
      span().setEnd(location);
      return this;
    }

  /**
   * Returns the file name where this node's constituents start from
   * if any; or <tt>null</tt>
   */
  public final String getFile ()
    {
      return span().getStartFile();
    }
      
  /**
   * Returns the line number in the input stream corresponding to the
   * start location of this node's extent.
   */
  public final int getLineNumber ()
    {
      return span().getStartLine();
    }
      
  /**
   * Sets the file name where this node's constituents are, and returns
   * this parse node.
   */
  public final ParseNode setFile (String file)
    {
      span().setStartFile(file);
      span().setEndFile(file);
      return this;
    }
      
  /**
   * Sets the line number in the input stream corresponding to this
   * node's location extent start, and returns this parse node.  This is
   * a dangerous method to use as it can set the start line number to a
   * value inconsistent with its end's. Therefore, a warning is issued.
   */
  public final ParseNode setLineNumber (int line)
    {
      Grammar.loudWarning("Setting the line number of a ParseNode should not be done! "
                          + nodeInfo());
      span().setStartLine(line);
      return this;
    }
      
  /**
   * Returns an explicit string for this node's location.
   */
  public final String locationString ()
    {
      return Misc.locationString(this);
    }

  /* **************************************************************** */
  // Subtree Information:
  /* **************************************************************** */

  private ArrayList _children;

  /**
   * When constructing the parse tree, this is the set of constituents.
   * If a parse tree has been built (by <tt>GenericParser.parse(boolean)</tt>),
   * this returns the sequence of <tt>ParseNode</tt>s that are this node's
   * constituents. Otherwise, returns <tt>null</tt>.
   */
  public final ArrayList children ()
    {
      return _children;
    }

  public void setChildren (ArrayList children)
    {
      _children = children;
    }

  /**
   * Returns the number of constituents of this node.
   */
  public final int numberOfChildren ()
    {
      if (_children == null)
        return 0;
      return _children.size();
    }

  /**
   * Gets <tt>n</tt>-th constituents of this node it it exists;
   * return <tt>null</tt> otherwise.
   */
  final ParseNode getChild (int n)
    {
      if (_children == null || n < 0 || n > _children.size())
        return null;

      return (ParseNode)_children.get(n);
    }

  /**
   * Adds the specified <tt>ParseNode</tt> to the set of constituents of
   * this node to build a parse tree.
   */
  final void addChild (ParseNode kid)
    {
      if (_children == null)
        _children = new ArrayList();

      _children.add(kid);
    }

  /**
   * When <tt>treeType</tt> is <tt>GenericParser.FULL_TREE</tt>, or
   * <tt>GenericParser.FULL_TREE</tt> (<i>i.e.</i>, when building the
   * full concrete syntax tree), this will add the specified child as is;
   * otherwise, adds a child that is a "leaner" tree structure than the
   * raw syntax tree of the specified child node obtained from it by
   * eliminating empty reduction branches, collapsing single-child
   * branches into a node labeled like its lowermost node, and
   * flattenning immediate symbol recursion (<i>i.e.</i>, a nonterminal
   * occurring in the RHS of a rule having it as LHS).
   */
  final void addChild (ParseNode kid, int treeType)
    {
	if (treeType == GenericParser.FULL_TREE
	 || treeType == GenericParser.XML_TREE
	 || kid.isLeafNode())
        {
          addChild(kid);
          return;
        }

      // COMPACT_TREE:

      // eliminate empty nodes:
      if (kid.isEmptyNode())
        return;

      // shorten linear branches to the most specific node:
      if (kid.children().size() == 1)
        {
          addChild(kid.getChild(0));
          return;
        }

      // flatten sequences:
      if (_symbol.name() == kid.symbol().name())
        for (int j=0; j<kid.children().size(); j++)
          addChild(kid.getChild(j));
      else
        addChild(kid);
    }

  Object[] nodes (ParserStackElement[] handle)
    {
      Object[] ns = new Object[handle.length];
      for (int i=0; i<ns.length; i++)
        ns[i] = handle[i].getNode();
      return ns;
    }

  /**
   * Returns <tt>true</tt> iff this node corresponds to a terminal symbol.
   */       
  public final boolean isTerminal ()
    {
      return _symbol instanceof ParserTerminal;
    }

  /**
   * Returns <tt>true</tt> iff this node does not have any child.
   */       
  public final boolean hasChildren ()
    {
      return _children != null && _children.size() != 0;
    }

  /**
   * Returns <tt>true</tt> iff this node is a leaf in the parse tree
   * (<i>i.e.</i>, iff it corresponds to a terminal symbol or an
   * anonymous symbol).
   */       
  public final boolean isLeafNode ()
    {
      return (isTerminal() || _symbol == null);
    }

  /**
   * Returns <tt>true</tt> iff this node is an inner node in the parse
   * tree (<i>i.e.</i>, iff it corresponds to a nonterminal symbol).
   */       
  public final boolean isInnerNode ()
    {
      return (_symbol instanceof ParserNonTerminal);
    }

  /**
   * Returns <tt>true</tt> iff this node is a null inner node (<i>i.e.</i>,
   * iff it corresponds to an empty derivation). These nodes have no children.
   */       
  public final boolean isEmptyNode ()
    { 
      return (isInnerNode() && _children == null);
    }

  /**
   * Returns <tt>true</tt> iff this node carries a value (which is either
   * in its <tt>svalue</tt> or <tt>nvalue</tt> field).
   */       
  public final boolean hasValue ()
    {
      return (_svalue!=null || !Double.isNaN(_nvalue));
    }

  /**
   * Returns the value carried by this node as a String.
   */       
  public final String stringValue ()
    {
      return _svalue != null ? _svalue
                             : _isInteger ? Integer.toString((int)_nvalue)
                                          : Double.toString(_nvalue);
    }

  /**
   * Returns a string representation of this node's information
   * contents: its string form and its location if known.
   */
  public final String nodeInfo ()
    {
      String s = toString();

      if (!isLocated())
        return s;

      return s + ": " + span();
    }

  /* **************************************************************** */

  /**
   * If a parse tree has been built, this returns the <tt>i</tt>-th in the
   * sequence of <tt>ParseNode</tt>s that are this node's constituents.
   * Otherwise, returns <tt>null</tt>. <b>NB:</b> the argument <tt>i</tt>
   * ranges from <tt>1</tt> to <tt>numberOfChildren</tt>.
   */
  public final ParseNode child (int i)
    {
      if (_children == null)
        return null;
      return (ParseNode)_children.get(i-1);
    }

  /**
   * If a parse tree has been built, this returns the first in the sequence of
   * <tt>ParseNode</tt>s that are this node's constituents. Otherwise, returns
   * <tt>null</tt>.
   */
  public final ParseNode firstChild ()
    {
      return child(1);
    }

  /**
   * If a parse tree has been built, this returns the last in the sequence of
   * <tt>ParseNode</tt>s that are this node's constituents. Otherwise, returns
   * <tt>null</tt>.
   */
  public final ParseNode lastChild ()
    {
      if (_children == null)
        return null;
      return (ParseNode)_children.get(_children.size()-1);
    }

  /* **************************************************************** */
  // Operator Information:
  /* **************************************************************** */

  private ParserOperator _operator;

  /**
   * If this is a dynamic operator, the actual specific operator.
   */
  public ParserOperator operator ()
    {
      return _operator;
    }

  /**
   * Sets the operator.
   */
  public void setOperator (ParserOperator operator)
    {
      _operator = operator;
    }

  /**
   * Returns <tt>true</tt> iff this is a dynamic operator.
   */
  public final boolean isOperator ()
    {
      return (_operator != null);
    }

  /**
   * Returns the precedence value of this node. This assumes that this
   * is a token node.
   */
  public final int precedence ()
    {
      if (isOperator()) return _operator.precedence();
      return ((ParserTerminal)_symbol).precedence();
    }

  /**
   * Returns the associativity value of this node. This assumes that this
   * is a token node.
   */
  public final int associativity ()
    {
      if (isOperator()) return _operator.associativity();
      return ((ParserTerminal)_symbol).associativity();
    }

  /**
   * Returns the fixity value of this node. This assumes that this
   * is a token node. If this is not an operator, returns -1.
   */
  public final int fixity ()
    {
      if (isOperator()) return _operator.fixity();
      return -1;
    }

  /* **************************************************************** */
  // Structure Duplication:
  /* **************************************************************** */

  /**
   * Returns an <i>almost</i> complete copy of the specified node by
   * copying the values of all its attributes except that of its
   * <tt>symbol</tt>, <tt>children</tt>, and location <tt>span</tt>. The
   * symbol of the copy is set to that of <i>this</i> (not the
   * specified) node, and the children and span are set to
   * <tt>null</tt>.
   */
  public final ParseNode copy (ParseNode node)
    {
      ParseNode copy = new ParseNode(node);
      copy.setSymbol(_symbol);
      copy.setChildren(null);
      copy.resetSpan();
      return copy;
    }

  /**
   * Returns a new ParseNode that is a shallow copy of this ParseNode.
   */
  public ParseNode copy ()
    {
      return new ParseNode(this);
    }

  /* **************************************************************** */
  // Display
  /* **************************************************************** */

  /**
   * Spawns a framed graphical display of the parse tree
   * rooted in this node.
   */
  public final void display ()
    {
      new TreeDisplay(this);
    }

  /**
   * Spawns a framed graphical display of the parse tree
   * rooted in this node and corresponding to the specified
   * source file.
   */
  public final void display (String source)
    {
      new TreeDisplay(this,source);
    }

  /**
   * Prints the parse tree rooted in this node on stdout.
   */
  public final void show ()
    {
      show(0,System.out);
    }

  /**
   * Prints the parse tree rooted in this node on the specified
   * output stream.
   */
  public final void show (PrintStream out)
    {
      show(0,out);
    }

  /**
   * Prints the parse tree rooted in this node on the specified
   * output stream with the given initial margin.
   */
  public final void show (int margin, PrintStream out)
    {
      for (int i=0; i<margin; i++) out.print(" ");

      out.println(this);

      if (!isLeafNode() && _children != null)
        for (int i=0; i<_children.size(); i++)
          getChild(i).show(margin+3,out);
    }

  /**
   *
   * <p><hr><p>
   *
   * <h3>XML representation</h3>
   *
   * <p><hr><p>
   *
   *
   * The XML representation of a <tt>ParseNode</tt> may be generated
   * only after a successful parse resulted in constructing the full
   * concrete syntax tree (CST) - <i>i.e.</i>, such that a
   * <tt>ParseNode</tt> is the root of the CST obtained by keeping all
   * the RHS members of a rule upon reduction with this rule. The CST is
   * then <i>transduced</i> into an XML tree of JDOM objects. To each
   * <tt>ParseNode</tt> in the CST is associated an <i>XML form</i>
   * <tt>xmlForm()</tt>, a list of XML elements corresponding to what it
   * would look like when serialized as the contents of an XML document.
   *
   * <p><hr><p>
   *
   */

  /**
   * This is the list of XML elements associated with this parse node.
   * This list will be:
   *
   * <ul>
   *
   * <li> <b style="color:blue">empty</b>, whenever this node
   *      corresponds to a punctuation terminal or a reduction with an
   *      <i><u>un</u>annotated</i> empty derivation;
   *
   * <li> <span style="color:blue">a <b>single</b> <tt>Element</tt></span>,
   *      whenever this node corresponds to an <i>annotated</i> terminal, or
   *      a reduction with any <i>annotated</i> derivation;
   *
   * <li> <span style="color:blue">the <b>concatenation</b> of the <tt>_xmlForms</tt>
   *      of this <tt>ParseNode</tt>'s children</span>, whenever this
   *      node corresponds to an reduction with <i><u>un</u>annotated</i>
   *      <u>non</u>-empty derivation.
   *
   * </ul>
   *
   */
  private ArrayList _xmlForm;

  /**
   * Returns the (possibly empty) list of XML elements associated with
   * this parse node.  This XML form is synthesized from the concrete
   * parse tree (CST) made of <tt>ParseNode</tt>'s rooted at this node,
   * using the XML annotation specified for the rules and terminals (if
   * any) and the default "xmlification". Note that in the total absence
   * of any rule or terminal annotation, the XML form will simply amount
   * to the sequence of XML elements corresponding to the value-carrying
   * (<i>i.e.</i>, non-punctuation) terminals at the leaves of the CST.
   *
   * <p>
   *
   * <span style="color:tan; font-size:smaller"><i> [<b>N.B.</b>: This
   * list is "populated" by calling <a
   * href="#xmlify"><b><tt>xmlify(Element root)</tt></b></a>.]
   * </i></span>
   */
  public final ArrayList xmlForm ()
    {
      return _xmlForm;
    }

  /**
   * Returns <tt>true</tt> iff <tt>xmlForm()</tt> is non-empty.
   */
  public final boolean hasXmlForm ()
    {
      return !xmlForm().isEmpty();
    }

  /**
   * Adds each element in this node's <tt>xmlForm()</tt> to the
   * specified <tt>container</tt>.
   */
  final void attachXmlFormTo (Element container)
    {
      for (Iterator i=xmlForm().iterator(); i.hasNext();)
	container.addContent((Element)i.next());
    }

  /**
   * Serializes the specified XML element on the specified output stream.
   */
  public final void showXmlElement (Element element, PrintStream out) throws IOException
    {
      if (element != null)
	    GenericParser.xmlWriter().output(element,out);
	  else
	    out.println("null");
    }
 
  /**
   * This is the JDOM XML document associated with this parse node, if any.
   */
  private Document _xmlDocument;

  /**
   * Returns the JDOM XML document associated with this parse node, if any,
   * or <tt>null</tt> otherwise.
   *
   * <p>
   *
   * <span style="color:tan; font-size:smaller"><i> [<b>N.B.</b>: This
   * will return <tt>null</tt> as long as <a
   * href="#xmldoc"><b><tt>xmlDocument(xmlroot,nsprefix,namespaces)</tt></b></a>
   * has not been invoked.]  </i></span>
   */
  public final Document xmlDocument ()
    {
      return _xmlDocument;
    }

  /**
   * Returns the XML document associated with this parse node with a
   * root element named as specified by <tt>xmlroot</tt> and
   * <tt>nsprefix</tt> in the context of the namespaces declarations
   * specified in <tt>namespaces</tt>.  The first time this is called,
   * this will trigger computation of the XML form of this node (and
   * inductively of that of this node's descendants below it in the CST)
   * as a (possibly empty) list of JDOM XML <tt>Element</tt>s for the
   * CST rooted in this <tt>ParseNode</tt>.  This XML form is then
   * recorded as this node's <tt>_xmlForm</tt>. Finally, all the
   * elements in this form are attached in the order they come as
   * contents to the root of a JDOM XML <tt>Document</tt>, which is then
   * saved as this node's <tt>_xmlDocument</tt>.  Further calls will
   * simply return the saved <tt>_xmlDocument</tt>.
   *
   */
  public Document xmlDocument (String xmlroot, String nsprefix, String[] namespaces)
    {
      if (_xmlDocument == null)
      // this does not have an associated XML Document;
      // so we need to create one:
	{
	  // create the root Element:
	  Element root = new Element(xmlroot);
	  // Process namespace declarations if any:
	  if (namespaces != null)
	    for (int i=0; i<namespaces.length/2; i++)
	      {
		String prefix = namespaces[2*i];
		String url = namespaces[2*i+1];
		Namespace ns = Namespace.getNamespace(prefix,url);
		root.addNamespaceDeclaration(ns);
		if (prefix == nsprefix)
		  root.setNamespace(ns);
	      }

	  // generate this node's XML form to be the contents of root,
	  // and store it in _xmlForm:
	  _xmlForm = xmlify(root);
	  
	  // add each element in _xmlForm to the contents of root:
	  for (Iterator i=_xmlForm.iterator(); i.hasNext();)
	    root.addContent((Element)i.next());

	  // create an XML document with root as root element:
	  _xmlDocument = new Document(root);

	  // time-stamp the XML document:
	  _xmlDocument
	    .addContent(new Comment("XML document generated on "
				    +(new Date())));
	}

      // return the XML document:
      return _xmlDocument;
    }

  /**
   * This is a storage recording the XML annotation for this parse node
   * if any.
   */
  private XmlInfo _xmlInfo = null;

  /**
   * Returns the XML annotation for this parse node, if any, or
   * <tt>null</tt>. If non-<tt>null</tt>, it is used to guide the
   * building of the <a href="http://www.jdom.org/docs/apidocs/">JDOM</a> tree
   * for this node's XML contents [<span style="font-size=smaller">see
   * the method <a href="#xmlify"><tt>xmlify(root)</tt>)</a></span>].
   *
   * <p>
   *
   * If this node's symbol is a non-terminal, then its
   * <tt>xmlInfo()</tt> is inherited at parse time from the rule
   * performing the reduction - <i>i.e.</i>, creating this node as the
   * parent of all the RHS's symbols' CST's. If this node's symbol is a
   * terminal, then it comes from annotation in the grammar if any.
   */
  XmlInfo xmlInfo ()
    {
      return _xmlInfo;
    }
    
  /**
   * Sets this parse node's XML info to the specified <tt>info</tt> and
   * returns this node.
   */
  public ParseNode setXmlInfo (XmlInfo info)
    {
      _xmlInfo = info;
      return this;
    }
  
  /**
   * <a name="xmlify"></a> This method builds and returns the XML form
   * of this parse node as a (possibly empty) <tt>ArrayList</tt>. The
   * argument is the uppermost XML <tt>Element</tt> that is the root of
   * the containing XML document. It is passed down to provide the
   * context for retrieving global document information needed in the
   * construction such as namespace prefix definitions, <i>etc.</i>, ...
   * It generates the creation of a list of zero, one, or more <a
   * href="http://www.jdom.org/docs/apidocs/apidocs/">JDOM</a> <a
   * href="http://www.jdom.org/docs/apidocs/uml/org/jdom/Element.html">
   * <tt>Element</tt></a> objects corresponding to the form of this
   * node's XML serialization. This list is then recorded as this parse
   * node's <tt>_xmlForm</tt>. This method uses the information stored
   * in this node (which is a full CST) together with the (possibly
   * <tt>null</tt>) XML info stored in its private field
   * <tt>_xmlInfo</tt>. When non-<tt>null</tt>, this <tt>_xmlInfo</tt>
   * specifies a guiding pattern according to which this node's XML form
   * is generated as a singleton <tt>ArrayList</tt> <tt>Element</tt>.
   *
   * <p>
   *
   * <b><a name="treetran">Specification</a></b>
   *
   * The process of building the XML tree from the concrete syntax tree
   * is one of <i>tree transduction</i>. The source is the full concrete
   * parse tree (<i>i.e.</i>, the one rooted in this
   * <tt>ParseNode</tt>); and the target is the (possibly empty)
   * <tt>ArrayList</tt> of <a
   * href="http://www.jdom.org/docs/apidocs/">JDOM</a> XML trees (each
   * of of type <a
   * href="http://www.jdom.org/docs/uml/org/jdom/Element.html">
   * <tt>Element</tt></a>), which is then recorded as the
   * <tt>_xmlForm</tt> field of this <tt>ParseNode</tt>. Thus, a parse
   * node may be transduced into either (1) nothing, or (2) an
   * <tt>Element</tt>, or (3) a sequence thereof. This transduction is
   * performed inductively (<i>i.e.</i>, from the leaves to the root)
   * using the transduction information specified as this node's
   * <tt>XmlInfo</tt> attribute <tt>_xmlInfo</tt> (which may be possibly
   * <tt>null</tt>).
   *
   * <p>
   *
   * The XML info is of type <a href="XmlInfo.html"><tt>XmlInfo</tt></a>
   * and consists of:
   *
   * <p>
   *
   * <ul>
   *
   * <li><tt style="color:blue">_xmlInfo.localName()</tt>:
   *        an XML element's name (a <tt>String</tt>);
   *
   * <p><li><tt style="color:blue">_xmlInfo.nsPrefix()</tt>:
   *         an XML namespace's prefix (a <tt>String</tt>);
   *
   * <p><li><tt style="color:blue">_xmlInfo.attributes()</tt>: an
   *        array of <a
   *        href="XmlAttributeInfo.html"><tt>XmlAttributeInfo</tt></a>'s.
   *        Each <tt>XmlAttributeInfo</tt> specifies an attribute key/value
   *        pair of this node's XML form.
   *
   * <p><li><tt style="color:blue">_xmlInfo.children()</tt>:
   *        an <tt>int[]</tt> containing indices in this
   *        <tt>ParseNode</tt>'s <tt>children()</tt>.  If
   *        <tt>_xmlInfo.children()[i]=j</tt>, this means that the XML
   *        form of the <tt>j</tt><sup>th</sup> child CST constitutes the
   *        <tt>i</tt><sup>th</sup> (possibly empty) subsequence of this
   *        node's XML form.
   *
   * <p><li><tt style="color:blue">_xmlInfo.xmlPaths()</tt>: an
   *        <tt>int[][]</tt> containing paths of XML subtree indices
   *        such that, if <tt>_xmlInfo.xmlPaths()[i]</tt> is non
   *        <tt>null</tt> and equal to some <tt>int[] path<sub>i</sub></tt>,
   *        then <tt>path<sub>i</sub></tt> denotes the XML tree address
   *        consisting of a sequence of indices of XML subcomponents,
   *        starting at an element in the XML form of this node's
   *        child <tt>ParseNode</tt> at index <tt>children()[i]</tt>, and
   *        navigating down the XML tree therefrom.
   *
   * <p><li><tt style="color:blue">_xmlInfo.wrapperPaths()</tt>: a
   *        <tt>String[][]</tt> containing sequences of XML tags
   *        such that, if <tt>_xmlInfo.wrapperPaths()[i]</tt> is non
   *        <tt>null</tt> and equal to some <tt>String[] path<sub>i</sub></tt>,
   *        then <tt>path<sub>i</sub></tt> denotes the wrapping XML
   *        elements in which to nest the XML tree constructed from
   *        this node's descendants.
   *
   * </ul>
   *
   * This information comes from explicit annotations of the grammar's
   * rules or terminals. When it is absent (<i>i.e.</i>,
   * <tt>_xmlInfo</tt> is <tt>null</tt>), a (sensible) default behavior
   * for building the XML tree is carried out (see <span
   * style="color:red"> Case II</span> below).
   *
   * <p>
   *
   * Thus, we need to specify how to transduce this node into an
   * <tt>ArrayList</tt> following the following steps:
   *
   * <ol type="I" style="color:red">
   * <li> Create a new <tt>ArrayList</tt> called <b><tt>xmlForm</tt></b>;
   * <p>
   * <li> If there is no explicit annotation (<i>i.e.</i>, if <tt>_xmlInfo</tt>
   *      <u>is</u> <tt>null</tt>), the default behavior is as follows:  
   *
   *      <ol type="1" style="color:brown">
   *      <li> If this is a leaf node (<i>i.e.</i>, if this has no children):
   *
   *           <ol type="a" style="color:blue">
   *           <li> If this node's symbol is a non-terminal with no children:<br>
   *                
   *           <li style="color:black"> If this node's symbol is a terminal:
   *
   *                <ol type="i" style="color:magenta">
   *                <li> If it is punctuation (<i>i.e.</i>, a literal
   *                     terminal, not carrying any value but itself):<br>
   *                     
   *                <li style="color:black"> Else (it has a value):
   *                     
   *                     <ul style="color:black">
   *                        <li> create a new XML <tt>Element</tt> using the name of
   *                             the terminal symbol as this element's local name;
   *                        <li> add to it only one <tt>Text</tt> component
   *                             containing the value of the node as contents;
   *                        <li> add this new element to <tt>xmlForm</tt>.
   *                     </ul>
   *                     
   *                </ol>
   *           </ol>
   *
   *      <li> Else (<i>i.e.</i>, this is an interior node):
   *
   *           <ul style="color:black">
   *           <li> for each of this node's children, append the list returned
   *                by calling <tt>xmlify(root)</tt> to <tt>xmlForm</tt>;
   *           </ul>
   *
   *      </ol>
   *
   * <p>
   * <li> Else, there is an explicit annotation (<i>i.e.</i>, <tt>_xmlInfo</tt>
   *      is <u>not</u> <tt>null</tt>):
   *
   *      <ol type="1" style="color:brown">
   *      <li> If this is a leaf node (i.e., it has no children):
   *
   *           <ol type="a" style="color:blue">
   *           <li> If this node's symbol is a non-terminal with no children:<br>
   *
   *                <i style="color:tan">[<b>N.B.</b>,
   *                <tt>_xmlInfo.children()</tt> is then null or empty
   *                since there may be no reference to children in an
   *                empty derivation's annotation - <a
   *                href="http://www.hassan-ait-kaci.net/hlt/doc/hlt/code/language/syntax/xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#consistency">
   *                here's why</a>]</i>
   *                
   *                <ul style="color:black">
   *                   <li> create a new XML <tt>Element</tt> using <tt>_xmlInfo</tt>;
   *                   <li> add this element to <tt>xmlForm</tt>;
   *                </ul>
   *                
   *           <li> If this node's symbol is a terminal:
   *
   *                <ol type="i" style="color:magenta">
   *                <li> If it is punctuation (<i>i.e.</i>, a literal
   *                     terminal, not carrying any value but itself):<br>
   *
   *                     <i style="color:tan">[<b>N.B</b>: this should not happen and be flagged
   *                     as an annotation error at grammar-processing time since
   *                     punctuation terminals may not be annotated;
   *                     only rules and value-carrying tokens may be
   *                     annotated in the grammar. Thus, punctuation
   *                     terminals are systematically ignored.]</i>
   *
   *                <li> Else (it has a value):<br>
   *
   *                     <i style="color:tan">[<b>N.B.</b>: <tt>_xmlInfo.children()</tt> is then
   *                     null or empty since there may be no reference to children
   *                     in a terminal's annotation - <a
   *                     href="http://hassan-ait-kaci.net/hlt/doc/hlt/code/language/syntax/xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#consistency">here's why</a>]</i>
   *
   *
   *                     <ul style="color:black">
   *                        <li> create a new XML <tt>Element</tt> using
   *                             <tt>_xmlInfo</tt>;
   *                        <li> add to it only one <tt>Text</tt> component
   *                             containing the value of the node as contents;
   *                        <li> add this new element to <tt>xmlForm</tt>;
   *                     </ul>
   *
   *                </ol>
   *
   *           </ol>
   *
   *      <li> Else (<i>i.e.</i>, this is an interior node):
   *
   *           <ul style="color:black">
   *               <li> create a new XML <tt>Element</tt> using <tt>_xmlInfo</tt>;
   *               <li> add it to <tt>xmlForm</tt>;
   *           </ul>
   *
   *      </ol>
   *
   * <p>
   * <li> Return <b><tt>xmlForm</tt></b>.
   * </ol>
   *
   * <p><hr><p>
   *
   * <tt style="color:tan; font-size:smaller"><i>[See also the method <a
   * href="XmlInfo.html#createXmlForm"><tt>createXmlForm(ParseNode node,
   * Element root)</tt></a> in the class <a
   * href="XmlInfo.html"><tt>XmlInfo</tt></a>.]</i></tt>
   *
   * <p><hr><p>
   *
   * <tt style="color:tan; font-size:smaller"><i><b>N.B.</b>: The code below is
   * guaranteed to work safely only if the annotation is <a
   * href="http://hassan-ait-kaci.net/hlt/doc/hlt/code/language/syntax/xml/XmlAnnotationDoc/XmlAnnotationSpecification.html#consistency">
   * strictly consistent</a>.</i></tt>
   *
   */
  public final ArrayList xmlify (Element root)
  {
    // create an empty xmlForm:
    ArrayList xmlForm = new ArrayList(hasChildren()?_children.size():0);

    if (_xmlInfo == null) // no annotation - default behavior:
      if (isTerminal() && hasValue())
	{ // this is a non-annotated value-carrying terminal:
	  // create a new element for it:
	  Element element = new Element(_symbol.name());
	  element.addContent(new Text(stringValue()));
	  // add it to the XML form:
	  xmlForm.add(element);
	}
      else
	{ // this is a valueless terminal or a non-annotated non-terminal:
	  if (hasChildren())
	    // this is an unannotated interior node - concatenate the XML
	    // forms of all its consituents into xmlForm:
	    for (Iterator i=_children.iterator(); i.hasNext();)
	      xmlForm.addAll(((ParseNode)i.next()).xmlify(root));
	}
    else // follow annotation:
      { // this may be a non-terminal or a value-carrying terminal:
	Element element = _xmlInfo.createXmlForm(this,root);
	xmlForm.add(element);

// 	try
// 	  {
// 	    System.err.print("===> XML form of node "+this+":\n");
// 	    showXmlElement(element,System.err);
// 	    hlt.language.tools.Debug.step();
// 	  }
// 	catch (Exception e)
// 	  {
// 	    e.printStackTrace();
// 	  }
      }

    // return the (possibly empty) xmlForm:
    return xmlForm;
  }

  /* **************************************************************** */
  // Equality
  /* **************************************************************** */

  /**
   * Two ParseNodes are considered equal iff they carry the same info.
   */
  public final boolean equals (Object other)
    {
      if (!(other instanceof ParseNode) || other == null)
        return false;

      ParseNode that = (ParseNode)other;

      return this.nodeInfo().equals(that.nodeInfo());
    }

  /* **************************************************************** */
  // String form
  /* **************************************************************** */

  /**
   * Returns a string representation of this node (if non-deterministic,
   * as a single alternative).
   */
  private String stringForm ()
    {
      if (isOperator()) return _operator.toString();

      String s = (_symbol == null ? "'DYNAMIC'" : _symbol.toString());

      if (isLeafNode() && hasValue())
        {
          s += "(";
          if (_svalue!=null)
            {
              s += _svalue;
              if (!Double.isNaN(_nvalue)) s += ",";
            }
          if (!Double.isNaN(_nvalue))
            if (_isInteger)
              s += String.valueOf((int)_nvalue);
            else
              s += _nvalue;
          s += ")";
        }
      return s;
    }

  /**
   * Returns a string representation of this node, along, possibly, with
   * all its alternatives if any.
   */
  public String toString ()
    {
      String s = stringForm();

//       if (hasAlternatives())
//      for (Iterator i=_alternatives.iterator(); i.hasNext();)
//        s += " | " + ((ParseNode)i.next()).stringForm();

      return s;
    }
}


