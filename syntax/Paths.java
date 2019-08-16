//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * This the class of objects stored in the path table of
 * a nonterminal. It consists of a sequence of rule paths
 * between a pair of nonterminals, and a set of terminals,
 * which is the union of all the FIRST sets of these paths.
 * It also contains the start and end nonterminals.
 *
 * @version     Last modified on Fri Apr 13 20:11:45 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */


import hlt.language.util.ArrayList;
import hlt.language.util.SetOf;

class Paths
{
  /**
   * The nonterminal at the origin of the paths.
   */
  NonTerminal start;

  /**
   * The nonterminal at the end of the paths.
   */
  NonTerminal end;

  /**
   * The arrayList of paths.
   */
  ArrayList paths;

  /**
   * The FIRST set - union of all paths between start and end.
   */
  SetOf first;

  /**
   * This is <tt>true</tt> whenever one of the paths in this derives
   * the EMPTY symbol.
   */
  boolean isNullable;

  /**
   * Constructs a new Paths object with the given rule path.
   */
  Paths (RulePath path)
    {
      paths = new ArrayList();

      paths.add(path);
      start = path.start;
      end = path.end;
      first = new SetOf(path.first);
      start.paths.add(path);
      isNullable = path.isNullable;
    }

  /**
   * Adds the given path to this Paths object, and returns <tt>true</tt>
   * iff the given path contributes to the FIRST union or empty derivation.
   */
  final boolean add (RulePath path)
    {
      if (path.first.isSubsetOf(first)
          && (isNullable || !path.isNullable))
        return false;
      
      paths.add(path);
      path.start.paths.add(path);
      
      first.union(path.first);
      isNullable |= path.isNullable;
      
      return true;
    }
}
