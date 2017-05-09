package models;

import javax.persistence.*;

@Entity
@Table(name = "contact")
public class Contact {
    private String id;
    private String name;
    private String address;

    @ManyToOne
    @JoinColumn(name = "id")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
