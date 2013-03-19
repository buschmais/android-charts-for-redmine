package de.buschmais.mobile.redmine.data;

import java.io.Serializable;

/**
 * Class representing a redmine user.
 */
public class User implements Serializable, Comparable<User>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1807593851118717615L;
    
    private final int id;
    private final String name;
    
    /**
     * Creates a new user with the given attributes.
     * @param id the id of the user
     * @param name the name of the user
     */
    public User(int id, String name)
    {
        super();
        this.id = id;
        this.name = name;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int compareTo(User another)
    {
        return Integer.valueOf(id).compareTo(Integer.valueOf(another.id));
    }
}
