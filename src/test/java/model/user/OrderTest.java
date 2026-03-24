package model.user;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import model.restaurant.Restaurant;
import service.DateService;

import static model.user.Customer.Type.CHILD;
import static model.user.Customer.Type.OTHER;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest
{
    @Test
    void getPrice_whenChild_when1stOrder_then50PercentDiscount()
    {
        // Given
        Customer customer = new Customer("Ba", "Bar", CHILD);
        Restaurant restaurant = new Restaurant("The restaurant");
        restaurant.addMeal("Meal 1", 15.0);
        restaurant.addMeal("Meal 2", 10.0);
        customer.makeOrder(restaurant, List.of("Meal 1", "Meal 2"), new DateService());

        // When
        Order order = customer.getOrders().get(0);

        // Then
        assertEquals((15.0+10.0)*0.5, order.getPrice());
    }

    @Test
    void getPrice_whenOther_when1stOrder_thenFullPrice()
    {
        // Given
        Customer customer = new Customer("Ba", "Bar", OTHER);
        Restaurant restaurant = new Restaurant("The restaurant");
        restaurant.addMeal("Meal 1", 15.0);
        restaurant.addMeal("Meal 2", 10.0);
        customer.makeOrder(restaurant, List.of("Meal 1", "Meal 2"), new DateService());

        // When
        Order order = customer.getOrders().get(0);

        // Then
        assertEquals(15.0+10.0, order.getPrice());
    }

    @Test
    void getPrice_whenHasOrderedPastWeek_thenOffer2ndMeal()
    {
        // Given
        MockDateService mockDateService = new MockDateService();
        LocalDate dateOrderCreatedNow = LocalDate.of(2026, 03, 26);
        LocalDate dateOrderCreatedOneWeekAgo = LocalDate.of(2026, 03, 19);
        mockDateService.setFirstDate(dateOrderCreatedOneWeekAgo);
        mockDateService.setSecondDate(dateOrderCreatedNow);

        Customer customer = new Customer("Ba", "Bar", OTHER);
        Restaurant restaurant = new Restaurant("The restaurant");
        restaurant.addMeal("Meal 1", 15.0);
        restaurant.addMeal("Meal 2", 10.0);
        restaurant.addMeal("Meal 3", 5.0);
        customer.makeOrder(restaurant, List.of("Meal 1", "Meal 2"), mockDateService);
        customer.makeOrder(restaurant, List.of("Meal 2", "Meal 3"), mockDateService);

        // When
        Order order = customer.getOrders().getLast();

        // Then
        assertEquals(10.0, order.getPrice());
    }

    @Test
    void getPrice_whenHasNotOrderedPastWeek_thenOffer2ndMeal()
    {
        // Given
        MockDateService mockDateService = new MockDateService();
        LocalDate dateOrderCreatedNow = LocalDate.of(2026, 03, 26);
        LocalDate dateOrderCreatedOneWeekAndOneDayAgo = LocalDate.of(2026, 03, 18);
        mockDateService.setFirstDate(dateOrderCreatedOneWeekAndOneDayAgo);
        mockDateService.setSecondDate(dateOrderCreatedNow);

        Customer customer = new Customer("Ba", "Bar", OTHER);
        Restaurant restaurant = new Restaurant("The restaurant");
        restaurant.addMeal("Meal 1", 15.0);
        restaurant.addMeal("Meal 2", 10.0);
        restaurant.addMeal("Meal 3", 5.0);
        customer.makeOrder(restaurant, List.of("Meal 1", "Meal 2"), mockDateService);
        customer.makeOrder(restaurant, List.of("Meal 2", "Meal 3"), mockDateService);

        // When
        Order order = customer.getOrders().getLast();

        // Then
        assertEquals((10.0+5.0), order.getPrice());
    }
}

class MockDateService extends DateService
{
    @Getter
    private int nowCallNumber;

    @Setter
    private LocalDate firstDate;

    @Setter
    private LocalDate secondDate;

    public MockDateService()
    {
        this.nowCallNumber = 0;
    }

    @Override
    public LocalDate now()
    {
        nowCallNumber++;
        if(nowCallNumber == 1)
        {
            return firstDate;
        }
        return secondDate;
    }
}