/** Copyright 2024 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.my.goldmanager.entity.Item;
import com.my.goldmanager.service.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/items")

public class ItemController {

	@Autowired
	private ItemService itemService;

	
	/**
	 * Create Item
	 * @param item
	 * @return
	 */
	@PostMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Item> create(@RequestBody Item item) {
		Item savedItem =itemService.create(item); 
		return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
		
	}
	/**
	 * Update Item
	 * @param id
	 * @param item
	 * @return
	 */
	@PutMapping(path = "/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Item> update(@PathVariable(name = "id") String id, @RequestBody Item item){
		Optional<Item> result = itemService.update(id, item);
		if(result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Gets the item by provided id
	 * @param id
	 * @return
	 */
	@GetMapping(path ="/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Item> get(@PathVariable(name = "id") String id){
		Optional<Item> result = itemService.getById(id);
		if(result.isPresent()) {
			return ResponseEntity.ok(result.get());
		}
		return ResponseEntity.notFound().build();
	}
	
	/**
	 * Deletes the item with provided id
	 * @param id
	 * @return
	 */
	@DeleteMapping(path="/{id}")
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> delete(@PathVariable(name = "id") String id){
		if(itemService.delete(id)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
	
	/**
	 * List all Items
	 * @return
	 */
	@GetMapping
	@Operation(security = @SecurityRequirement(name = "bearerAuth"))
	public List<Item> list(){
		return itemService.list();
	}
	
	
}
