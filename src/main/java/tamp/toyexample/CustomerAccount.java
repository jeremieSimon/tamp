package tamp.toyexample;

import com.priceminister.account.*;
import com.sun.istack.internal.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BinaryOperator;

/**
 * It is assumed that
 *  1. All operations we are dealing with the same currency
 *  2. The balance of a client cannot go over Double.MAX_VALUE
 *
 * The class is threadsafe and use a single lock on withdraw which leaves the add function lock free.
 *
 */
public class CustomerAccount implements Account {

    private final BinaryOperator<Double> additioner;
    private final AtomicReference<Double> balance;
    private final Lock lock;

    public CustomerAccount() {
        additioner = (x, y) -> x + y;
        this.balance = new AtomicReference<>(0d);
        lock = new ReentrantLock();
    }

    public Double getBalance() {
        return balance.get();
    }

    public void add(@Nullable final Double addedAmount) {
        if (addedAmount < 0) throw new IllegalArgumentException(
                "Cannot add a negative value: "
                        + " addedAmount = " + addedAmount);

        double currentBalance = balance.get();
        if (addedAmount != 0 && currentBalance + addedAmount <= currentBalance)
            throw new IllegalArgumentException("Number overflown, cannot add the given amount");

        balance.accumulateAndGet(addedAmount, additioner);
    }

    public Double withdrawAndReportBalance(@Nullable final Double withdrawnAmount,
                                           @Nullable final AccountRule rule)
            throws IllegalBalanceException {

        if (withdrawnAmount < 0) throw new IllegalArgumentException(
                "Cannot withdrow a negative value."
                + " value client tried to withdrew = " + withdrawnAmount);

        lock.lock();
        try {
            double currentBalance = balance.get();
            if (!rule.withdrawPermitted(currentBalance - withdrawnAmount))
                throw new IllegalBalanceException(currentBalance - withdrawnAmount);

            return balance.accumulateAndGet(-withdrawnAmount, additioner);
        } finally {
            lock.unlock();
        }
    }
}
