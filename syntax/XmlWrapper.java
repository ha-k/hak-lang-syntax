//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

/**
 * A data type for packaging all relevant XML wrapper annotation
 * information.
 *
 * @version     Last modified on Fri Apr 13 20:21:38 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

package hlt.language.syntax;

public class XmlWrapper
  {
    private String _tag = null;
    private boolean _starred = false;

    public XmlWrapper (String tag)
      {
	_tag = tag;
      }

    public XmlWrapper (String tag, boolean starred)
      {
	_tag = tag;
	_starred = starred;
      }

    public final void setTag (String tag)
      {
	_tag = tag;
      }

    public final String getTag ()
      {
	return _tag;
      }

    public final void makeStarred ()
      {
	_starred = true;
      }

    public final boolean isStarred ()
      {
	return _starred;
      }

    public final String toString ()
      {
	return _starred ? _tag+"*" : _tag+".";
      }
  }
