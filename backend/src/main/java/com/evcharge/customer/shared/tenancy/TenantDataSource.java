package com.evcharge.customer.shared.tenancy;

import com.evcharge.customer.shared.TenantContext;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class TenantDataSource extends DelegatingDataSource {

    private static final String BIND = "select set_config('app.country', ?, false)";

    TenantDataSource(DataSource target) {
        super(target);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return bound(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return bound(super.getConnection(username, password));
    }

    private static Connection bound(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(BIND)) {
            statement.setString(1, TenantContext.current().orElse(""));
            statement.execute();
            return connection;
        } catch (SQLException | RuntimeException e) {
            connection.close();
            throw e;
        }
    }
}
