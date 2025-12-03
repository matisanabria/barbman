package app.barbman.core.model;

import java.util.Objects;

public class Client {

    private int id;
    private String name;      // Name and surname OR business name
    private String document;  // CI o RUC
    private String phone;
    private String email;
    private String notes;
    private boolean active;

    // Constructor for creating new clients
    public Client(String name, String document, String phone, String email, String notes, boolean active) {
        this.name = name;
        this.document = document;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
        this.active = active;
    }

    // Constructor for reading from DB
    public Client(int id, String name, String document, String phone, String email, String notes, boolean active) {
        this(name, document, phone, email, notes, active);
        this.id = id;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDocument() { return document; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDocument(String document) { this.document = document; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + " (" + document + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id && active == client.active && Objects.equals(name, client.name) && Objects.equals(document, client.document) && Objects.equals(phone, client.phone) && Objects.equals(email, client.email) && Objects.equals(notes, client.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, document, phone, email, notes, active);
    }
}
