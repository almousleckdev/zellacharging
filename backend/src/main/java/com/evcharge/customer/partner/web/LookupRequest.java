package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.DriverQuery;

import java.util.List;

record LookupRequest(List<DriverQuery> drivers) {
}
