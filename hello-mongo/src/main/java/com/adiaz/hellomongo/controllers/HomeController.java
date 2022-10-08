package com.adiaz.hellomongo.controllers;

import com.adiaz.hellomongo.model.GroceryItem;
import com.adiaz.hellomongo.repositories.CustomItemRepository;
import com.adiaz.hellomongo.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HomeController {

  @Autowired
  ItemRepository groceryItemRepo;

  @Autowired
  CustomItemRepository customRepo;

  @GetMapping("/hello")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok("toma toma");
  }

  @GetMapping("/create")
  public ResponseEntity<String> create() {
    System.out.println("Data creation started...");
    groceryItemRepo.save(new GroceryItem("Whole Wheat Biscuit", "Whole Wheat Biscuit", 5, "snacks"));
    groceryItemRepo.save(new GroceryItem("Kodo Millet", "XYZ Kodo Millet healthy", 2, "millets"));
    groceryItemRepo.save(new GroceryItem("Dried Red Chilli", "Dried Whole Red Chilli", 2, "spices"));
    groceryItemRepo.save(new GroceryItem("Pearl Millet", "Healthy Pearl Millet", 1, "millets"));
    groceryItemRepo.save(new GroceryItem("Cheese Crackers", "Bonny Cheese Crackers Plain", 6, "snacks"));
    System.out.println("Data creation complete...");
    return ResponseEntity.ok("created");
  }

  //** Fetch all the documents (grocery items) using the findAll() method. */
  @GetMapping("/get-all")
  public ResponseEntity<String> getAll() {
    List<GroceryItem> all = groceryItemRepo.findAll();
    String allNames = all.stream().map(GroceryItem::toString).collect(Collectors.joining("<br>"));
    return ResponseEntity.ok(allNames);
  }

  /** Get a single item (document) by its name field using the findItemByName method. */
  @GetMapping("/get-by-name")
  public ResponseEntity<String> getByName(@RequestParam String name) {
    GroceryItem itemByName = groceryItemRepo.findItemByName(name);
    if (itemByName!=null) {
      return ResponseEntity.ok(itemByName.toString());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/get-by-category")
  public ResponseEntity<List<GroceryItem>> getByCategory(@RequestParam String category) {
    List<GroceryItem> groceryItems = groceryItemRepo.findAll(category);
    return ResponseEntity.ok(groceryItems);
  }

  @GetMapping("/get-count")
  public ResponseEntity<Long> getCount() {
    return ResponseEntity.ok(groceryItemRepo.count());
  }

  @GetMapping("/update")
  public ResponseEntity<GroceryItem> update(@RequestParam String name, @RequestParam String category) {
    GroceryItem itemByName = groceryItemRepo.findItemByName(name);
    if (itemByName==null) {
      return ResponseEntity.notFound().build();
    }
    itemByName.setCategory(category);
    GroceryItem savedGrocery = groceryItemRepo.save(itemByName);
    return ResponseEntity.ok(savedGrocery);
  }

  @GetMapping("/update-all")
  public ResponseEntity<List<GroceryItem>> updateAll(@RequestParam String categoryOld, @RequestParam String categoryNew) {
    List<GroceryItem> groceryItemList = groceryItemRepo.findAll();
    groceryItemList.stream().filter(e -> e.getCategory().equals(categoryOld)).forEach(e->e.setCategory(categoryNew));
    List<GroceryItem> groceryItemListUpdated = groceryItemRepo.saveAll(groceryItemList);
    return ResponseEntity.ok(groceryItemListUpdated);
  }

  @GetMapping("/delete-by-id")
  public ResponseEntity<String> deleteById(@RequestParam String id) {
    if (groceryItemRepo.findById(id).isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    groceryItemRepo.deleteById(id);
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/update-quantity")
  public ResponseEntity<String> updateQuantity(@RequestParam String name, @RequestParam  Float newQuantity) {
    customRepo.updateItemQuantity(name, newQuantity);
    return ResponseEntity.ok("updated");
  }
}
