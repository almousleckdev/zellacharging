package com.evcharge.customer.account;

public record CustomerCounts(long total, long publicUsers, long ndzelaDrivers, long ndzelaStaff, long suspended) {

    public CustomerCounts plus(CustomerCounts other) {
        return new CustomerCounts(total + other.total, publicUsers + other.publicUsers,
                ndzelaDrivers + other.ndzelaDrivers, ndzelaStaff + other.ndzelaStaff, suspended + other.suspended);
    }
}
