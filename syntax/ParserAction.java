//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.ArrayIndexed;

/**
 * Class of LR parsing actions used by the parser.
 *
 * @version	Last modified on Fri Apr 13 20:09:18 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright	&copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */
public class ParserAction extends ArrayIndexed
{
  public int type;  // SHIFT, REDUCE, ACCEPT, DYNAMIC, or ERROR.
  public int info;  // If REDUCE, the rule's index; if SHIFT the next state's;
                    // DYNAMIC/CHOICE: the current state's dynamic actions array index.

  public ParserAction (int type)
    {
      super(GenericParser.actions);
      this.type = type;
    }

  public ParserAction (int type, int info)
    {
      super(GenericParser.actions);
      this.type = type;
      this.info = info;
    }

  public ParserAction (int type, int info, int index)
    {
      super(GenericParser.actions,index);
      this.type = type;
      this.info = info;
    }

  public boolean equals (Object object)
    {
      if (this == object)
	return true;

      if (!(object instanceof ParserAction))
	return false;

      ParserAction that = (ParserAction)object;
      return (this.type == that.type) && (this.info == that.info);
    }

  public boolean isError ()
    {
      return type == Action.ERROR;
    }

  public boolean isReduce ()
    {
      return type == Action.REDUCE;
    }

  public boolean isShift ()
    {
      return type == Action.SHIFT;
    }

  public boolean isDynamic ()
    {
      return type == Action.DYNAMIC;
    }

  public boolean isChoice ()
    {
      return type == Action.CHOICE;
    }

  public String toString ()
    {
      switch (type)
	{
	case Action.REDUCE:
	  return "R"+info;
	case Action.SHIFT:
	  return "S"+info;
	case Action.DYNAMIC:
	  return "D"+info;
	case Action.CHOICE:
	  return "C"+info;
	case Action.ACCEPT:
	  return "A";
	default:
	  return "E";
	}
    }
		     
}
