package model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import model.restaurant.Restaurant;
import service.DateService;
import service.RateService;

public class Customer implements User
{
    @Getter
    private final String firstName;

    @Getter
    private final String lastName;

    @Getter
    private final Type type;

    @Getter
    private final List<Order> orders;

    public Customer(String firstName, String lastName,Type type)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.orders = new ArrayList<>();
    }

    public void makeOrder(Map<Restaurant, List<String>> restaurantMeals, DateService dateService)
    {
        orders.add(new Order(restaurantMeals, this, dateService, new RateService()));
    }

    public enum Type {
        CHILD,
        STUDENT,
        OTHER
    }
}
