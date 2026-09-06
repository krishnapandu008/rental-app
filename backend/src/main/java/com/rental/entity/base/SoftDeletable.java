package com.rental.entity.base;

public interface SoftDeletable {
    boolean isActive();
    void softDelete();
    void restore();
}