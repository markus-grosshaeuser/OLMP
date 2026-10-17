package com.grosshaeuser.olmp.ui.internal_ui.shared;

public interface Delegatable {

    @FunctionalInterface
    interface CreateCallback<T> {
        void create(T component);
    }

    @FunctionalInterface
    interface UpdateCallback<T> {
        T update(T component);
    }

    @FunctionalInterface
    interface DeleteCallback<T> {
        void delete(T component);
    }

    @FunctionalInterface
    interface ErrorCallback {
        void handleException(RuntimeException exception);
    }
}
