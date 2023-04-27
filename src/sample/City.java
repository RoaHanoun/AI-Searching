package sample;


public class City implements Comparable<City> {
    private  int id;
    private String cityName;
    private String arabicName;
    private double cityXAxis;
    private double cityYAxis;
    private double cost;
    private double gN;
    private double hN;
    private double fN;
    City predecessor;

    public City(int id,String cityName,double cityXAxis, double cityYAxis,String arabicName) {
        this.id=id;
        this.cityName = cityName;
        this.cityXAxis = cityXAxis;
        this.cityYAxis = cityYAxis;
        this.arabicName = arabicName;
        this.cost=Double.POSITIVE_INFINITY;
        predecessor=null;
    }
    public City() {

    }


    public String getArabicName() {
        return arabicName;
    }
    public String getCityName() {
        return this.cityName;
    }
    public double getCityXAxis() {
        return this.cityXAxis;
    }
    public double getCityYAxis() {
        return this.cityYAxis;
    }
    public int getId() {
        return id;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getgN() {
        return gN;
    }

    public void setgN(double gN) {
        this.gN = gN;
    }

    public double gethN() {
        return hN;
    }

    public void sethN(double hN) {
        this.hN = hN;
    }

    public double getfN() {
        return fN;
    }

    public void setfN(double fN) {
        this.fN = fN;
    }

    @Override
    public int compareTo(City city) {

        return Double.compare(this.fN,city.fN);

    }

}