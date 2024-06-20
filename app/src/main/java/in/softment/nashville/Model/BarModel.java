package in.softment.nashville.Model;

import java.io.Serializable;
import java.util.Date;

public class BarModel implements Serializable {

    public BarModel(){

    }

    public String id = "";
    public String image = "";
    public String name = "";
    public Double rating = 0.0;
    public String address = "";
    public String about = "";
    public Date createDate = new Date();
    public String webUrl = "";
    public int cityId = 2;
    public  Double latitude = 0.0;
    public Double longitude = 0.0;

}
