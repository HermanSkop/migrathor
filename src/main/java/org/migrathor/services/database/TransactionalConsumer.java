package org.migrathor.services.database;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalConsumer<C, Q> {
    void accept(C connection, Q query) throws SQLException;
}

