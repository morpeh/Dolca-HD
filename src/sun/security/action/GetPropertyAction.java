package sun.security.action;

@SuppressWarnings("unchecked")
public class GetPropertyAction implements java.security.PrivilegedAction {
    private String theProp;
    private String defaultVal;

    /**
     * Constructor that takes the name of the system property whose
     * string value needs to be determined.
     *
     * @param theProp the name of the system property.
     */
    public GetPropertyAction(String theProp) {
	this.theProp = theProp;
    }

    /**
     * Constructor that takes the name of the system property and the default
     * value of that property.
     *
     * @param theProp the name of the system property.
     * @param defaulVal the default value.
     */
    public GetPropertyAction(String theProp, String defaultVal) {
	this.theProp = theProp;
	this.defaultVal = defaultVal;
    }

    /**
     * Determines the string value of the system property whose
     * name was specified in the constructor.
     *
     * @return the string value of the system property,
     *         or the default value if there is no property with that key.
     */
    public Object run() {
	String value = System.getProperty(theProp);
	return (value == null) ? defaultVal : value;
    }
}