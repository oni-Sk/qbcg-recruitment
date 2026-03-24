package model.user;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import model.Entity;
import model.restaurant.Meal;
import model.restaurant.Restaurant;
import service.DateService;

import static java.lang.String.format;

public class Order implements Entity
{

    @Getter
    private final DateService dateService;

    @Getter
    private final LocalDate date;

    @Getter
    private final Restaurant restaurant;

    @Getter
    private final Customer customer;

    @Getter
    private final List<Meal> meals;

    Order(Restaurant restaurant, Customer customer, List<String> mealNames, DateService dateService)
    {
        this.dateService = dateService;
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

            double TENTH_PLATFORM_ORDER_RATE = 0.15;
            double platformRate = 0D;
            boolean isTenthOrderOnThePlatform = customer.getOrders().size() % 10 == 0;
            if(isTenthOrderOnThePlatform)
            {
                platformRate = TENTH_PLATFORM_ORDER_RATE;
            }

            double FIFTH_RESTAURANT_ORDER_RATE = 0.10;
            double restaurantRate = 0D;
            boolean isFifthOrderInTheRestaurant = customer.getOrders().stream().filter(o -> o.getRestaurant().equals(restaurant)).count() % 5 == 0;
            if(isTenthOrderOnThePlatform || isFifthOrderInTheRestaurant)
            {
                restaurantRate = FIFTH_RESTAURANT_ORDER_RATE;
            }

            double CHILD_RATE = 0.5;
            double STUDENT_RATE = 0.25;
            double customerRate = 0D;
            switch (customer.getType()) {
                case CHILD:
                    customerRate = CHILD_RATE;
                    break;
                case STUDENT:
                    customerRate = STUDENT_RATE;
                    break;
                default:
            }
            totalAmount += itemFullPrice * (1 - platformRate - restaurantRate - customerRate);
        }
        return totalAmount;
    }
}
