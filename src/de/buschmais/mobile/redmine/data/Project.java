package de.buschmais.mobile.redmine.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Object representing a "project" in redmine.
 */
public class Project implements Serializable
{
    /** serial version UID */
    private static final long serialVersionUID = -1100310280260553735L;

    private final int id;
    private final String identifier;
    private final String name;
    private final String description;
    private final Date created;
    private final Date updated;

    /**
     * Create a new {@link Project} using the given properties.
     * 
     * @param id
     * @param name
     * @param identifier
     * @param description
     * @param created
     * @param updated
     */
    public Project(int id, String name, String identifier, String description, Date created, Date updated)
    {
        super();
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.description = description;
        this.created = created;
        this.updated = updated;
    }

    public int getId()
    {
        return id;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Date getCreated()
    {
        return created;
    }

    public Date getUpdated()
    {
        return updated;
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
        Project other = (Project) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "Project [id=" + id + ", " + "name=" + name + ", " + "identifier=" + identifier + ", " + "created="
                + created + ", " + "updated=" + updated + "]";
    }
}
