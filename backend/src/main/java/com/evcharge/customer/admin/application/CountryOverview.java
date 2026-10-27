package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerFigures;
import com.evcharge.customer.charging.SessionFigures;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.wallet.WalletFigures;

public record CountryOverview(Country country, CustomerFigures customers, SessionFigures sessions,
                              WalletFigures wallets) {
}
