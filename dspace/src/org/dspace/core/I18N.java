package org.dspace.core;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for internationalisation of message generating code. The 
 * message(String, Class) and getMessage(String, Class) methods should be 
 * favoured.  
 * 
 * @author Jim Downing
 */
public class I18N {

	private static final I18N DEFAULT = new I18N(Locale.getDefault());

	/**
	 * Utility method for finding the class of the calling object to this class.
	 * @param stack
	 * @return classname 
	 */
//	private static String callerClassName(StackTraceElement[] stack) {
//		for (int i = 0; i < stack.length; i++) {
//			String currentClass = stack[i].getClassName();
//			if (currentClass.indexOf("java.") == 0)
//				continue;
//			if (I18N.class.getName().equals(currentClass))
//				continue;
//			return currentClass;
//		}
//		return "";
//	}

	/**
	 * Super convenience method that works out the calling class by examining
	 * the Thread's stack at the point of invocation. This is probably naughty
	 * and certainly excessively expensive, so prefer to use the message(String,
	 * Class) method if you're making repeated calls.
	 * 
	 * @param msg
	 *            the key of the message required, omitting the fully qualified
	 *            class name
	 * @return message for the JVM default Locale
	 */
//	public static String message(String msg) {
//		return DEFAULT.getMessage(msg);
//	}

	/**
	 * Convenience method to get localized messages in code
	 * 
	 * @param msg
	 *            message key, omitting fully qualified class name
	 * @param clazz
	 *            The class object the messages are filed under
	 * @return Localized message for the JVM default Locale
	 */
	public static String message(String msg, Class clazz) {
		return DEFAULT.getMessage(msg, clazz);
	}

	/**
	 * <p>
	 * Convenience method to get localized messages in code. Usage will be of
	 * the form: -
	 * </p>
	 * 
	 * <pre>
	 * I18N.message(&quot;my-key&quot;, this);
	 * </pre>
	 * 
	 * <p>
	 * N.B. That this method uses the runtime type of the object, and hence
	 * should only be used in final classes.
	 * </p>
	 * 
	 * @param msg
	 *            Key for the message, omitting the fully qualified class name
	 * @param obj
	 *            the calling object
	 * @return Localized message for the JVM default Locale
	 */
	public static String message(String msg, Object obj) {
		return DEFAULT.getMessage(msg, obj);
	}

	/**
	 * Slightly convenient method that appends together a msg key and a class
	 * name together and retrieves the corresponding message from
	 * Messages.properties
	 * 
	 * @param msg
	 *            final part of message key, omitting fully qualified class name
	 * @param classname
	 *            fully qualified class name.
	 * @return Localized message for the JVM default Locale
	 */
	public static String message(String msg, String classname) {
		return DEFAULT.getMessage(msg, classname);
	}

	private ResourceBundle messages = null;

	private I18N() {
		;
	}

	public I18N(Locale locale) {
		messages = ResourceBundle.getBundle("Messages", locale);
	}

	/**
	 * Super convenience method that works out the calling class by examining
	 * the Thread's stack at the point of invocation. This is probably naughty
	 * and certainly excessively expensive, so prefer to use the message(String,
	 * Class) method if you're making repeated calls.
	 * 
	 * @param msg
	 *            the key of the message required, omitting the fully qualified
	 *            class name
	 * @return message 
	 */
//	public String getMessage(String msg) {
//		return getMessage(msg, callerClassName(Thread.currentThread()
//				.getStackTrace()));
//	}

	/**
	 * Convenience method to get localized messages in code
	 * 
	 * @param msg
	 *            message key, omitting fully qualified class name
	 * @param clazz
	 *            The class object the messages are filed under
	 * @return Localized message 
	 */
	public String getMessage(String msg, Class clazz) {
		String className = clazz.getName();
		return messages.getString(new StringBuilder(50).append(className)
				.append(".").append(msg).toString());
	}

	/**
	 * <p>
	 * Convenience method to get localized messages in code. Usage will be of
	 * the form: -
	 * </p>
	 * 
	 * <pre>
	 * I18N.message(&quot;my-key&quot;, this);
	 * </pre>
	 * 
	 * <p>
	 * N.B. That this method uses the runtime type of the object, and hence
	 * should only be used in final classes.
	 * </p>
	 * 
	 * @param msg
	 *            Key for the message, omitting the fully qualified class name
	 * @param obj
	 *            the calling object
	 * @return Localized message 
	 */
	public String getMessage(String msg, Object obj) {
		return getMessage(msg, obj.getClass());
	}

	/**
	 * Slightly convenient method that appends together a msg key and a class
	 * name together and retrieves the corresponding message from
	 * Messages.properties
	 * 
	 * @param msg
	 *            final part of message key, omitting fully qualified class name
	 * @param classname
	 *            fully qualified class name.
	 * @return Localized message
	 */
//	public String getMessage(String msg, String classname) {
//		return messages.getString(new StringBuilder(50).append(classname)
//				.append(".").append(msg).toString());
//	}

}
