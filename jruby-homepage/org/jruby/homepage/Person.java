package org.jruby.homepage;

public class Person {
	private String name;
	private String role;
	private String email;
	
	public Person(String name, String email, String role) {
	    this.name = name;
	    this.email = email;
	    this.role = role;
	}
	
    /**
     * Gets the email.
     * @return Returns a String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     * @param email The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the name.
     * @return Returns a String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the role.
     * @return Returns a String
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role.
     * @param role The role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
}