package by.matrosov.demodemo.model;

import javax.persistence.*;

@Entity
@Table(name = "dictionary")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "word")
    private String word;

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", word='" + word + '\'' +
                '}';
    }
}
