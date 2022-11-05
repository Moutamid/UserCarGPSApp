package com.moutamid.usercargpsapp;

public class CarDetails {

    private String id;
    private String car;
    private String username;
    private String email;
    private String password;
    private String speed;
    private String location;
    private String distance;
    private String consumption;
    private String time;
    private double lat;
    private double lng;
    private String status;

    public CarDetails(){

    }

    public CarDetails(String id, String car, String username, String email, String password, String speed,
                      String location,double lat,double lng, String distance, String consumption, String time, String status) {
        this.id = id;
        this.car = car;
        this.username = username;
        this.email = email;
        this.password = password;
        this.speed = speed;
        this.location = location;
        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
        this.consumption = consumption;
        this.time = time;
        this.status = status;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
