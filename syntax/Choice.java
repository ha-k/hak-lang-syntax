//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.Stack;
import hlt.language.tools.Misc;
import hlt.language.util.TimeStamped;

import java.util.Iterator;
import java.util.AbstractList;

/**
 * This implements the class of the choice point object pushed on the
 * choice stack by a dynamic parser.
 *
 * @see         DynamicParser
 *
 * @version     Last modified on Fri Apr 13 19:55:33 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

class Choice implements TimeStamped
{
  Stack options;
  private boolean isTokenChoice = true;
  private long stamp;

  Choice ()
    {
      options = new Stack(2);
    }

  Choice (int size)
    {
      options = new Stack(size);
    }

  Choice (Stack options)
    {
      this.options = options;
    }

  boolean isTokenChoice ()
    {
      return isTokenChoice;
    }

  void setIsTokenChoice (boolean flag)
    {
      isTokenChoice = flag;
    }

  boolean isEmpty ()
    {
      return options.isEmpty();
    }

  public long getTimeStamp ()
    {
      return stamp;
    }      

  public void setTimeStamp (long stamp)
    {
      this.stamp = stamp;
    }      

  void addOption (Object object)
    {
      options.push(object);
    }

  void addOptions (AbstractList list)
    {
      for (Iterator i=list.iterator(); i.hasNext();)
	options.push(i.next());
    }

  public String toString ()
    {
      return Misc.view(options,"(stamp:"+getTimeStamp()+") ",18,64);
    }
}
