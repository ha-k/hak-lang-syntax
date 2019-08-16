//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.io.PrintStream;
import hlt.language.util.Error;

/**
 * This the abstract class of objects for managing and reporting errors. The method
 * <tt>reportError(Error error)</tt> must be provided by a concrete class deriving
 * from this class. For common uses, this package also provides the concrete class
 * <a href="DefaultErrorManager.html"><tt>DefaultErrorManager</tt></a> whose error
 * reporting method amounts to printing the specified error on <tt>System.err</tt>
 * (which is the default error stream and may be reset to another <tt>PrintStream</tt>
 * if desired).
 *
 * @version     Last modified on Fri Apr 13 19:58:05 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 *
 * @see DefaultErrorManager
 */

public abstract class ErrorManager
{
  private PrintStream _errorStream = System.err;

  private boolean _isReportingErrors = true;
  private boolean _isRecoveringErrors = true;

  private int _errorCount = 0;
  private int _warningCount = 0;
  private int _deprecatedCount = 0;

  /**
   * Reports the specified error as an error.
   */
  abstract public void reportError (Error error);

  /**
   * Reports the specified error as a warning.
   */
  abstract public void reportWarning (Error error);

  /**
   * Reports the specified error as a deprecation warning.
   */
  abstract public void reportDeprecated (Error error);

  /**
   * Reports a recapitulation of all errors.
   */
  abstract public void tallyErrors ();

  /**
   * Reports a recapitulation of all warnings.
   */
  abstract public void tallyWarnings ();

  /**
   * Reports a recapitulation of all deprecation warnings.
   */
  abstract public void tallyDeprecateds ();

  /**
   * Sets the print stream for error reporting to the specified one.
   */
  public final void setErrorStream (PrintStream stream)
    {
      _errorStream = stream;
    }

  /**
   * Returns this error manager's error reporting stream. The default is
   * <tt>System.err</tt>.
   */
  public final PrintStream errorStream ()
    {
      return _errorStream;
    }

  /**
   * Enables (resp., disables) error reporting for this error manager iff
   * the specified flag is <tt>true</tt> (resp., <tt>false</tt>).
   */
  public final void reportErrors (boolean flag)
    {
      _isReportingErrors = flag;
    }

  /**
   * Returns <tt>true</tt> iff this error manager is currently set to report
   * errors.
   */
  public final boolean isReportingErrors ()
    {
      return _isReportingErrors;
    }

  /**
   * Enables (resp., disables) error recovery for this error manager iff
   * the specified flag is <tt>true</tt> (resp., <tt>false</tt>).
   */
  public final void recoverFromErrors (boolean flag)
    {
      _isRecoveringErrors = flag;
    }

  /**
   * Returns <tt>true</tt> iff this error manager is currently set to
   * recover from errors.
   */
  public final boolean isRecoveringErrors ()
    {
      return _isRecoveringErrors;
    }

  /**
   * Returns <tt>true</tt> iff this error manager has errors.
   */
  public final boolean hasErrors ()
    {
      return _errorCount > 0;
    }

  /**
   * Returns <tt>true</tt> iff this error manager has errors.
   */
  public final boolean hasWarnings ()
    {
      return _warningCount > 0;
    }

  /**
   * Returns <tt>true</tt> iff this error manager has deprecated warnings.
   */
  public final boolean hasDeprecateds ()
    {
      return _deprecatedCount > 0;
    }

  /**
   * Resets the error manager to a fresh state.
   */
  public final void reset ()
    {
      _isReportingErrors = true;
      _isRecoveringErrors = true;
      _errorCount = 0;
      _warningCount = 0;
      _deprecatedCount = 0;
    }  

  /**
   * Returns the current count of (syntax???) errors.
   */
  public final int errorCount ()
    {
      return _errorCount;
    }

  /**
   * Increments the number of errors.
   */
  public final void countError ()
    {
      _errorCount++;
    }

  /**
   * Returns the current count of warnings.
   */
  public final int warningCount ()
    {
      return _warningCount;
    }

  /**
   * Increments the number of warnings.
   */
  public final void countWarning ()
    {
      _warningCount++;
    }

  /**
   * Returns the current count of deprecation warnings.
   */
  public final int deprecatedCount ()
    { 
      return _deprecatedCount;
    }

  /**
   * Increments the number of deprecation warning.
   */
  public final void countDeprecated ()
    {
      _deprecatedCount++;
    }

}
