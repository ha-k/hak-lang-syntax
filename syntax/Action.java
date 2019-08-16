//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.AbstractListIndexed;

/**
 * Class of LR parsing actions used by the parser generator.
 *
 * @version     Last modified on Fri Apr 13 19:54:26 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class Action extends AbstractListIndexed
{
  final static int SHIFT   = 0;
  final static int REDUCE  = 1;
  final static int ACCEPT  = 2;
  final static int DYNAMIC = 3;
  final static int CHOICE  = 4;
  final static int ERROR   = 5;

  public int type;  // One of SHIFT, REDUCE, ACCEPT, DYNAMIC, CHOICE, or ERROR.
  public int info;  // REDUCE: the rule's index; SHIFT: the next state's;
                    // DYNAMIC/CHOICE: the current state's dynamic actions array index.

  public Action (int type)
    {
      super(ParserGenerator.actions);
      this.type = type;
    }

  public Action (int type, int info)
    {
      super(ParserGenerator.actions);
      this.type = type;
      this.info = info;
    }

  public Action (int type, int info, int index)
    {
      super(ParserGenerator.actions,index);
      this.type = type;
      this.info = info;
    }

  public boolean equals (Object object)
    {
      if (this == object)
        return true;

      if (!(object instanceof Action))
        return false;

      Action that = (Action)object;
      return (this.type == that.type) && (this.info == that.info);
    }

  final String conflict (Action contender)
    {
      return toString().charAt(0) + "/" + contender.toString().charAt(0);
    }

  public String toString ()
    {
      switch (type)
        {
        case REDUCE:
          return "R"+info;
        case SHIFT:
          return "S"+info;
        case DYNAMIC:
          return "D"+info;
        case CHOICE:
          return "C"+info;
        case ACCEPT:
          return "A";
        default:
          return "E";
        }
    }
                     
}
