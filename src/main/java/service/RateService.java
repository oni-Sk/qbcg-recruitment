package service;

import model.user.Customer;

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

    public double getCustomerRate(Customer.Type type)
    {
        return customerRateMap.get(type);
    }
}
