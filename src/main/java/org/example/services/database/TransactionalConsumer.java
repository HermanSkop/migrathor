package org.example.services.database;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalConsumer<T> {
    void accept(T t) throws SQLException;
}

