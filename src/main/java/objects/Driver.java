package objects;

import java.io.Serializable;

public class Driver implements Serializable {

    private int id;
    private String name;
    private String data;
    private String status;

    public Driver(int id, String name, String data, String status) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.status = status;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
