package rpost.jpalabs;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class MyEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String s;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }


}
