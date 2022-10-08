package com.adiaz.hellomongo.repositories;

public interface CustomItemRepository {
  void updateItemQuantity(String name, float newQuantity);
}
