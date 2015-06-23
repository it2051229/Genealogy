package it2051229.genealogy.entities;

import java.io.Serializable;

public class Person implements Serializable, Comparable<Person> {
    private String name;
    private Person spouse;
    private Person dad;
    private Person mom;
    private String notes;

    /**
     * Initialize the person properties
     */
    public Person(String name) {
        setName(name);
        setSpouse(null);
        setDad(null);
        setMom(null);
        setNotes("");
    }

    /**
     * Initialize a note for the person
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Access to the person's note
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Initialize the name property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Access to the name property
     */
    public String getName() {
        return name;
    }

    /**
     * Initialize the person's spouse
     */
    public void setSpouse(Person person) {
        spouse = person;
    }

    /**
     * Get the person's spouse
     */
    public Person getSpouse() {
        return spouse;
    }

    /**
     * Initialize the person's dad
     */
    public void setDad(Person person) {
        dad = person;
    }

    /**
     * Access the person's dad
     */
    public Person getDad() {
        return dad;
    }

    /**
     * Initialize the person's mom
     */
    public void setMom(Person person) {
        mom = person;
    }

    /**
     * Access the person's mom
     */
    public Person getMom() {
        return mom;
    }

    /**
     * Persons are the same if they have the same name
     */
    @Override
    public int compareTo(Person otherPerson) {
        return name.compareTo(otherPerson.name);
    }
}
