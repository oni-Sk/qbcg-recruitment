package service;

import model.restaurant.Restaurant;
import model.user.Customer;
import model.user.Order;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;

public class RateService
{
    private final Map<Customer.Type, Double> customerRateMap;

    public RateService() {
        this.customerRateMap = new EnumMap<Customer.Type, Double>(Customer.Type.class);
        customerRateMap.put(Customer.Type.CHILD, 0.5);
        customerRateMap.put(Customer.Type.STUDENT, 0.25);
        customerRateMap.put(Customer.Type.OTHER, 0D);

    }
;
    public double getPlatformRate(Customer customer)
    {
        boolean isTenthOrderOnThePlatform = customer.getOrders().size() % 10 == 0;
        return isTenthOrderOnThePlatform ? 0.15 : 0D;
    }

    public double getRestaurantRate(Customer customer, Restaurant restaurant, double platformRate)
    {
        boolean isFifthOrderInTheRestaurant = customer.getOrders().stream().filter(o -> o.getRestaurant().equals(restaurant)).count() % 5 == 0;
        return (platformRate > 0D || isFifthOrderInTheRestaurant) ? 0.10 : 0;
    }

    public double getCustomerRate(Customer.Type type)
    {
        return customerRateMap.get(type);
    }

    public  double getSameWeekItemRate (int mealNumber, Customer customer, LocalDate orderDate)
    {
        int currentWeekOrderCount = 0;
        if (mealNumber == 2) {
            for (Order order : customer.getOrders())
            {
                if (ChronoUnit.DAYS.between(order.getDate(), orderDate) <= 7)
                {
                    currentWeekOrderCount++;
                }
            }
        }
        return currentWeekOrderCount >= 2 ? 1.0 : 0D;
    }
}
