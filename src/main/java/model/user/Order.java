package model.user;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import model.Entity;
import model.restaurant.Meal;
import model.restaurant.Restaurant;
import service.DateService;
import service.RateService;

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

    Order(Restaurant restaurant, Customer customer, List<String> mealNames, DateService dateService, RateService rateService)
    {
        this.dateService = dateService;
        this.rateService = rateService;
        this.date = dateService.now();
        this.restaurant = restaurant.withReceivedOrder(this);
        this.customer = customer;
        this.meals = mealNames.stream().map(restaurant::getMealByName).toList();
    }

    public String getName()
    {
        return format("From %s - in %s", customer.getName(), restaurant.getName());
    }

    public double getPrice()
    {
        double totalAmount = 0D;
        int mealNumber = 0;
        Iterator mealIterator = meals.iterator();
        while(mealIterator.hasNext()) {
            Meal each = (Meal) mealIterator.next();
            mealNumber +=1;

            double itemFullPrice = each.getPrice();

            boolean hasOrderedInThePastWeek = false;
            for (Order order : customer.getOrders())
            {
                if (order != this && ChronoUnit.DAYS.between(order.date, dateService.now()) <= 7)
                    hasOrderedInThePastWeek = true;
            }
            if (hasOrderedInThePastWeek && mealNumber == 2) {
                double sameWeekRate = 1D;
                totalAmount += itemFullPrice * (1 - sameWeekRate);
                continue;
            }
            totalAmount += itemFullPrice;
        }
        double platformRate = rateService.getPlatformRate(customer);
        double restaurantRate = rateService.getRestaurantRate(customer, restaurant, platformRate);
        double customerRate = rateService.getCustomerRate(customer.getType());

        return totalAmount * (1 - platformRate - restaurantRate - customerRate);
    }
}
