package model.user;

import lombok.Getter;
import model.Entity;
import model.restaurant.Meal;
import model.restaurant.Restaurant;
import service.DateService;
import service.RateService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class Order implements Entity
{

    @Getter
    private final DateService dateService;

    @Getter
    private final RateService rateService;

    @Getter
    private final LocalDate date;

    @Getter
    private final Restaurant restaurant;

    @Getter
    private final Customer customer;

    @Getter
    private final List<Meal> meals;

    Order(Map<Restaurant, List<String>> restaurantMeals, Customer customer, DateService dateService, RateService rateService)
    {
        this.dateService = dateService;
        this.rateService = rateService;
        this.date = dateService.now();

        Map.Entry<Restaurant, List<String>> entry = restaurantMeals.entrySet().iterator().next();
        this.restaurant = entry.getKey().withReceivedOrder(this);
        this.meals = entry.getValue().stream().map(restaurant::getMealByName).toList();
        this.customer = customer;
    }

    public String getName()
    {
        return format("From %s - in %s", customer.getName(), restaurant.getName());
    }

    public double getPrice()
    {
        double totalAmount = 0D;

        int mealNumber = 0;
        for (Meal each : meals) {
            mealNumber += 1;

            double sameWeekRate = rateService.getSameWeekItemRate(mealNumber, customer, dateService.now());
            totalAmount += each.getPrice() * (1 - sameWeekRate);
        }

        double platformRate = rateService.getPlatformRate(customer);
        double restaurantRate = rateService.getRestaurantRate(customer, restaurant, platformRate);
        double customerRate = rateService.getCustomerRate(customer.getType());

        return totalAmount * (1 - platformRate - restaurantRate - customerRate);
    }
}
