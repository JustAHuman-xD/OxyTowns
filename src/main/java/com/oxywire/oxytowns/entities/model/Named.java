package com.oxywire.oxytowns.entities.model;

public interface Named {

    /**
     * Get the name for an entity.
     *
     * @return The entity's name.
     */
    String getName();

    /**
     * Set's the name for an entity.
     *
     * @param name Thew name for the entity.
     * @return if the name was set successfully.
     */
    boolean setName(String name);
}
