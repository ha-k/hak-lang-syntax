//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.Error;

/**
 * This the class of objects used by default to manage and report errors.
 * Its error reporting method amounts to printing the specified error on
 * <tt>System.err</tt> (which is the default error stream and may be reset
 * to another <tt>PrintStream</tt> if desired).
 *
 * @version     Last modified on Fri Apr 13 19:55:50 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 *
 * @see ErrorManager
 */

public class DefaultErrorManager extends ErrorManager
{
  /**
   * Increments the error count and prints the specified error on the
   * error reporting stream. This method may be overridden.
   */
  public void reportError (Error error)
    {
      countError();
      if (isReportingErrors())
	errorStream().println(error);
    }

  /**
   * Reports the total of errors up to this point.
   */
  public void tallyErrors ()
    {
      if (isReportingErrors())
	if (errorCount() > 0)
	  errorStream().println("*** Number of errors: " + errorCount());
	else
	  errorStream().println("*** There are no errors");
    }

  /**
   * Increments the warning count and prints the specified error on the
   * error reporting stream. This method may be overridden.
   */
  public void reportWarning (Error error)
    {
      countWarning();
      if (isReportingErrors())
	errorStream().println(error);
    }

  /**
   * Reports the total of warnings up to this point.
   */
  public void tallyWarnings ()
    {
      if (isReportingErrors() && warningCount() > 0)
	errorStream().println("*** Number of warnings: " + warningCount());
    }

  /**
   * Increments the deprecation warning count and prints the specified
   * error on the error reporting stream. This method may be overridden.
   */
  public void reportDeprecated (Error error)
    {
      countDeprecated();
      if (isReportingErrors())
	errorStream().println(error);
    }

  /**
   * Reports the total of deprecateds up to this point.
   */
  public void tallyDeprecateds ()
    {
      if (isReportingErrors() && deprecatedCount() > 0)
	errorStream().println("*** Number of deprecateds: " + deprecatedCount());
    }
}
