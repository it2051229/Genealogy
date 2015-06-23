package it2051229.genealogy.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Genealogy implements Serializable {
    private HashMap<String, Person> people;

    /**
     * Initialize the system
     */
    public Genealogy() {
        people = new HashMap<>();
    }

    /**
     * Initialize the system with predefined data
     */
    public Genealogy(HashMap<String, Person> data) {
        people = data;
    }

    /**
     * Access to the data
     */
    public HashMap<String, Person> getData() {
        return people;
    }

    /**
     * Get the names of the people
     */
    public ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();

        for(Map.Entry<String, Person> entry : people.entrySet()) {
            names.add(entry.getValue().getName());
        }

        return names;
    }

    /**
     * Get all names that has the keyword
     */
    public ArrayList<String> getNamesContaining(String keyword) {
        keyword = keyword.toLowerCase().trim();

        // Return everything if the keyword is empty
        if(keyword.isEmpty()) {
            return getNames();
        }

        ArrayList<String> names = new ArrayList<>();

        for(Map.Entry<String, Person> entry : people.entrySet()) {
            Person person = entry.getValue();

            if(person.getName().toLowerCase().contains(keyword)) {
                names.add(person.getName());
            }
        }

        return names;
    }

    /**
     * Get all names having partial connections
     */
    public ArrayList<String> getNamesHavingPartialConnections() {
        ArrayList<String> names = new ArrayList<>();

        for(Map.Entry<String, Person> entry : people.entrySet()) {
            Person person = entry.getValue();

            if(person.getSpouse() == null || person.getDad() == null || person.getMom() == null) {
                names.add(person.getName());
            }
        }

        return names;
    }

    /**
     * Makes names in uniform order. All lower case and first character is upper case
     */
    public String normalizeName(String name) {
        name = name.trim();

        // Never normalize an empty name
        if(name.isEmpty()) {
            return "";
        }

        String[] tokens = name.split(" ");
        name = "";

        for(String token : tokens) {
            char[] tokenArray = token.toLowerCase().toCharArray();
            tokenArray[0] = Character.toUpperCase(tokenArray[0]);

            name += new String(tokenArray) + " ";
        }

        return name.trim();
    }

    /**
     * Attempt to add the person's name. It fails if the person's name is not unique
     */
    public boolean addPerson(String name) {
        name = name.trim();

        // Avoid empty names
        if(name.isEmpty()) {
            return false;
        }

        // Make name uniform
        name = normalizeName(name);

        // Avoid duplicates
        if(people.containsKey(name)) {
            return false;
        }

        // Add if no duplicates
        people.put(name, new Person(name));
        return true;
    }

    /**
     * Remove the person with the given name
     */
    public boolean removePerson(String name) {
        Person targetPerson = getPerson(name);

        if(targetPerson == null) {
            return false;
        }

        // Disconnect the name from any moms and dads
        for(Map.Entry<String, Person> entry : people.entrySet()) {
            Person person = entry.getValue();

            if(person.getMom() == targetPerson) {
                person.setMom(null);
            }

            if(person.getDad() == targetPerson) {
                person.setDad(null);
            }
        }

        // Remove the target person
        people.remove(targetPerson.getName());

        return true;
    }

    /**
     * Search a person by name
     */
    public Person getPerson(String name) {
        name = name.trim();

        // Avoid empty name
        if(name.isEmpty()) {
            return null;
        }

        // Make name uniform
        name = normalizeName(name);

        // Do a search
        if(!people.containsKey(name)) {
            return null;
        }

        return people.get(name);
    }

    /**
     * Get the person's old name and update it to the new name
     */
    public boolean updateName(String oldName, String newName) {
        oldName = normalizeName(oldName);
        newName = normalizeName(newName);

        // Skip if the old and new name is the same
        if(oldName.equalsIgnoreCase(newName)) {
            return true;
        }

        // Stop if the new name is already taken
        if(getPerson(newName) != null) {
            return false;
        }

        // Perform an update
        Person person = people.remove(oldName);
        person.setName(newName);

        people.put(newName, person);
        return true;
    }

    /**
     * Get the siblings of a person
     */
    public ArrayList<String> getSiblingsOf(String name) {
        ArrayList<String> siblings = new ArrayList<>();

        Person targetPerson = getPerson(name);

        if(targetPerson == null) {
            return siblings;
        }

        for(Map.Entry<String, Person> entry : people.entrySet()) {
            Person person = entry.getValue();

            // Skip if the person is the same as the other person
            if(person.compareTo(targetPerson) == 0) {
                continue;
            }

            // They are siblings if the target person and the person has the same mom or dad
            if((targetPerson.getDad() != null && person.getDad() != null && targetPerson.getDad().compareTo(person.getDad()) == 0) ||
                    (targetPerson.getMom() != null && person.getMom() != null && targetPerson.getMom().compareTo(person.getMom()) == 0)) {
                siblings.add(person.getName());
            }
        }

        return siblings;
    }

    /**
     * Get the grand parents of a person
     */
    public ArrayList<String> getGrandParentsOf(String name) {
        ArrayList<String> grandParents = new ArrayList<>();

        Person targetPerson = getPerson(name);

        if(targetPerson == null) {
            return grandParents;
        }

        if(targetPerson.getDad() != null) {
            if(targetPerson.getDad().getDad() != null) {
                grandParents.add(targetPerson.getDad().getDad().getName());
            }

            if(targetPerson.getDad().getMom() != null) {
                grandParents.add(targetPerson.getDad().getMom().getName());
            }
        }

        if(targetPerson.getMom() != null) {
            if(targetPerson.getMom().getDad() != null) {
                grandParents.add(targetPerson.getMom().getDad().getName());
            }

            if(targetPerson.getMom().getMom() != null) {
                grandParents.add(targetPerson.getMom().getMom().getName());
            }
        }

        return grandParents;
    }

    /**
     * Get the children of the person
     */
    public ArrayList<String> getChildrenOf(String name) {
        ArrayList<String> children = new ArrayList<>();

        Person targetPerson = getPerson(name);

        if(targetPerson == null) {
            return children;
        }

        for(Map.Entry<String, Person> entry : people.entrySet()) {
            Person person = entry.getValue();

            if((person.getDad() != null && person.getDad() == targetPerson) ||
               (person.getMom() != null && person.getMom() == targetPerson)) {
                children.add(person.getName());
            }
        }

        return children;
    }

    /**
     * Get the grand children of a person
     */
    public ArrayList<String> getGrandChildrenOf(String name) {
        ArrayList<String> grandChildren = new ArrayList<>();

        Person targetPerson = getPerson(name);

        if(targetPerson == null) {
            return grandChildren;
        }

        ArrayList<String> children = getChildrenOf(name);

        for(String childName : children) {
            grandChildren.addAll(getChildrenOf(childName));
        }

        return grandChildren;
    }
}
