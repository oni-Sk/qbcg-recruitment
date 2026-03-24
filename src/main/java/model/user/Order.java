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

    public Double getPrice()
    {
        Double totalAmount = 0D;
        int mealNumber = 0;
        Iterator mealIterator = meals.iterator();
        while(mealIterator.hasNext()) {
            Meal each = (Meal) mealIterator.next();
            mealNumber +=1;

            boolean hasOrderedInThePastWeek = false;
            for (Order order : customer.getOrders())
            {
                if (order != this && ChronoUnit.DAYS.between(order.date, dateService.now()) <= 7)
                    hasOrderedInThePastWeek = true;
            }
            if (hasOrderedInThePastWeek && mealNumber == 2)
                continue;

            boolean isTenthOrderOnThePlatform = customer.getOrders().size() % 10 == 0;
            boolean isFifthOrderInTheRestaurant = customer.getOrders().stream().filter(o -> o.getRestaurant().equals(restaurant)).count() % 5 == 0;

            double TENTH_PLATFORM_ORDER_RATE = 0.15;
            double FIFTH_RESTAURANT_ORDER_RATE = 0.10;
            switch (customer.getType()) {
                case CHILD:
                    double CHILD_RATE = 0.5;
                    if (isTenthOrderOnThePlatform)
                        totalAmount += each.getPrice() * (1 - CHILD_RATE - FIFTH_RESTAURANT_ORDER_RATE - TENTH_PLATFORM_ORDER_RATE);
                    else if (isFifthOrderInTheRestaurant)
                        totalAmount += each.getPrice() * (1 - CHILD_RATE - FIFTH_RESTAURANT_ORDER_RATE);
                    else totalAmount += each.getPrice() * (1 - CHILD_RATE);
                    break;
                case STUDENT:
                    double STUDENT_RATE = 0.25;
                    if (isTenthOrderOnThePlatform)
                        totalAmount += each.getPrice() * (1 - STUDENT_RATE - FIFTH_RESTAURANT_ORDER_RATE - TENTH_PLATFORM_ORDER_RATE);
                    else if (isFifthOrderInTheRestaurant)
                        totalAmount += each.getPrice() * (1 - STUDENT_RATE - FIFTH_RESTAURANT_ORDER_RATE);
                    else totalAmount += each.getPrice() * (1 - STUDENT_RATE);
                    break;
                default:
                    if (isTenthOrderOnThePlatform)
                        totalAmount += each.getPrice() * (1 - FIFTH_RESTAURANT_ORDER_RATE - TENTH_PLATFORM_ORDER_RATE);
                    else if (isFifthOrderInTheRestaurant)
                        totalAmount += each.getPrice() * (1 - FIFTH_RESTAURANT_ORDER_RATE);
                    else totalAmount += each.getPrice();
            }
        }
        return totalAmount;
    }
}
