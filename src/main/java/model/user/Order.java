package model.user;

import lombok.Getter;
import model.Entity;
import model.restaurant.Meal;
import model.restaurant.Restaurant;
import service.DateService;
import service.RateService;

import java.time.LocalDate;
import java.util.HashMap;
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
    private final Map<Restaurant, List<Meal>> restaurantMeals;

    @Getter
    private final Customer customer;

    Order(Map<Restaurant, List<String>> restaurantMeals, Customer customer, DateService dateService, RateService rateService)
    {
        this.dateService = dateService;
        this.rateService = rateService;
        this.date = dateService.now();

        this.restaurantMeals = new HashMap<>();
        for(Map.Entry<Restaurant, List<String>> entry : restaurantMeals.entrySet())
        {
            Restaurant restaurant = entry.getKey();
            this.restaurantMeals.put(
                restaurant.withReceivedOrder(this),
                entry.getValue().stream().map(restaurant::getMealByName).toList()
            );
        }
        this.customer = customer;
    }

    public String getName()
    {
        String restaurantNames = restaurantMeals.keySet().stream().reduce(
                "",
                (names, restaurant) -> names + restaurant.getName(), String::join);
        return format("From %s - in %s", customer.getName(), restaurantNames);
    }

    public double getPrice()
    {
        double totalAmount = 0D;

        int mealNumber = 0;
        double restaurantAmount;
        for(Map.Entry<Restaurant, List<Meal>> entry : restaurantMeals.entrySet())
        {
            restaurantAmount = 0D;
            for (Meal each : entry.getValue()) {
                mealNumber += 1;

                double sameWeekRate = rateService.getSameWeekItemRate(mealNumber, customer, dateService.now());
                restaurantAmount += each.getPrice() * (1 - sameWeekRate);
            }
            double platformRate = rateService.getPlatformRate(customer);
            double restaurantRate = rateService.getRestaurantRate(customer, entry.getKey(), platformRate);
            double customerRate = rateService.getCustomerRate(customer.getType());
            totalAmount += restaurantAmount * (1 - platformRate - restaurantRate - customerRate);
        }
        return totalAmount;
    }
}
